package com.thesis.bitperfectusb.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.UsbStreamingOption
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow

/** Everything needed to stream to one activated (interface, altsetting) pair. */
data class ActivatedStreamingEndpoint(
    val usbInterface: UsbInterface,
    val dataEndpoint: UsbEndpoint,
    val feedbackEndpoint: UsbEndpoint?,
    val option: UsbStreamingOption
)

/**
 * Registers for ACTION_USB_DEVICE_ATTACHED / DETACHED, requests runtime permission
 * via UsbManager.requestPermission(), and hands out UsbDeviceConnections for
 * capability analysis and playback (Section 3.3, UsbDacManager).
 *
 * Detection is intentionally two-tiered rather than a single hard filter:
 * [allDevices] lists *everything* Android's USB host stack currently sees,
 * completely unfiltered, and [connectedDevice] is a best-guess auto-selection
 * from that list (preferring anything that declares a USB Audio Class
 * interface). A heuristic that silently hides devices it doesn't recognize is
 * exactly how one specific dongle ends up looking "not detected" when it's
 * really just an auto-selection miss — the fix is to always let the real,
 * unfiltered list be visible and manually selectable, never gated behind the
 * heuristic being right.
 */
class UsbDacManager(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    private val _allDevices = MutableStateFlow<List<UsbDevice>>(emptyList())
    /** Every USB device Android's host stack currently reports — unfiltered. */
    val allDevices: StateFlow<List<UsbDevice>> = _allDevices

    private val _connectedDevice = MutableStateFlow<UsbDevice?>(null)
    /** The device currently selected for analysis/playback — auto-picked, but overridable. */
    val connectedDevice: StateFlow<UsbDevice?> = _connectedDevice

    val currentDevice: UsbDevice? get() = _connectedDevice.value

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    refreshDeviceList()
                    val device = intent.getUsbDeviceExtra()
                    // Auto-select on attach only if nothing is currently selected, so plugging
                    // in a second device doesn't yank the user away from one they picked.
                    if (device != null && _connectedDevice.value == null) {
                        _connectedDevice.value = device
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    refreshDeviceList()
                    val device = intent.getUsbDeviceExtra()
                    if (device != null && device.deviceId == _connectedDevice.value?.deviceId) {
                        _connectedDevice.value = _allDevices.value.firstOrNull()
                    }
                }
                ACTION_USB_PERMISSION -> {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    val device = intent.getUsbDeviceExtra()
                    _permissionResult.tryEmitEvent(device to granted)
                }
            }
        }
    }

    private val _permissionResult = SinglePermissionEventBus()

    fun start() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        rescanDevices()
    }

    fun stop() {
        try { context.unregisterReceiver(receiver) } catch (_: IllegalArgumentException) { /* not registered */ }
    }

    /**
     * Re-queries the USB host stack from scratch and refreshes both [allDevices] and,
     * if nothing is currently selected, [connectedDevice]. Exposed for a manual
     * "Rescan" action — attach broadcasts can occasionally be missed (timing races
     * around process start, some OEM skins), so a user-triggerable re-query is a
     * cheap, reliable fallback rather than only ever trusting the broadcast stream.
     */
    fun rescanDevices() {
        refreshDeviceList()
        if (_connectedDevice.value == null) {
            _connectedDevice.value = pickBestGuess(_allDevices.value)
        }
    }

    /** Manually selects which attached device to treat as the active DAC. */
    fun selectDevice(device: UsbDevice) {
        _connectedDevice.value = device
    }

    private fun refreshDeviceList() {
        _allDevices.value = usbManager.deviceList.values.toList()
    }

    private fun pickBestGuess(devices: List<UsbDevice>): UsbDevice? =
        devices.firstOrNull { looksLikeAudioDevice(it) } ?: devices.firstOrNull()

    /**
     * Best-effort hint only — used to sort/highlight likely candidates in the UI,
     * never to hide a device from [allDevices]. Checks every interface's declared
     * class rather than the device-level class, since composite USB Audio devices
     * commonly declare bDeviceClass=0 ("defined at interface level") with the
     * actual Audio class living on the interface descriptors instead.
     */
    fun looksLikeAudioDevice(device: UsbDevice): Boolean =
        (0 until device.interfaceCount).any { device.getInterface(it).interfaceClass == UsbConstants.USB_CLASS_AUDIO }

    fun hasPermission(device: UsbDevice): Boolean = usbManager.hasPermission(device)

    /** Requests permission and suspends until the user responds (or times out via cancellation). */
    fun observePermissionRequest(device: UsbDevice): Flow<Boolean> = callbackFlow {
        if (usbManager.hasPermission(device)) {
            trySend(true)
            close()
            return@callbackFlow
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION).setPackage(context.packageName), flags
        )
        val job = _permissionResult.subscribe { (respondedDevice, granted) ->
            if (respondedDevice?.deviceId == device.deviceId) {
                trySend(granted)
                close()
            }
        }
        usbManager.requestPermission(device, pendingIntent)
        awaitClose { job() }
    }

    fun openConnection(device: UsbDevice): UsbDeviceConnection? =
        if (usbManager.hasPermission(device)) usbManager.openDevice(device) else null

    /**
     * Activates the exact alternate setting matching [format] and returns everything
     * needed to stream to it. This is the piece a "just find an isochronous endpoint"
     * implementation skips: audio streaming interfaces sit at alternate-setting 0
     * (zero bandwidth, no endpoints) until explicitly switched via SET_INTERFACE to
     * the altsetting that actually carries the desired format (Section 3.4).
     *
     * Requires [profile] to already have been built by DacCapabilityAnalyzer — you
     * cannot know which altsetting to activate without having parsed them first.
     */
    fun activateStreamingOption(
        device: UsbDevice,
        connection: UsbDeviceConnection,
        profile: DacProfile,
        format: PcmFormat
    ): ActivatedStreamingEndpoint? {
        val option = profile.findExactStreamingOption(format) ?: return null

        val targetInterface = (0 until device.interfaceCount)
            .map { device.getInterface(it) }
            .firstOrNull { it.id == option.interfaceNumber && it.alternateSetting == option.alternateSetting }
            ?: return null

        if (!connection.claimInterface(targetInterface, true)) return null
        if (!connection.setInterface(targetInterface)) return null

        var dataEndpoint: UsbEndpoint? = null
        var feedbackEndpoint: UsbEndpoint? = null
        for (e in 0 until targetInterface.endpointCount) {
            val ep = targetInterface.getEndpoint(e)
            if (ep.type != UsbConstants.USB_ENDPOINT_XFER_ISOC) continue
            if (ep.direction == UsbConstants.USB_DIR_OUT) dataEndpoint = ep
            else if (ep.direction == UsbConstants.USB_DIR_IN) feedbackEndpoint = ep
        }
        val data = dataEndpoint ?: return null

        return ActivatedStreamingEndpoint(targetInterface, data, feedbackEndpoint, option)
    }

    /**
     * Switches back to alternate-setting 0 (idle/zero-bandwidth) before releasing the
     * interface, so the DAC drops out of streaming mode cleanly and frees USB bandwidth
     * for other devices — the same pattern real UAC drivers follow on stop.
     */
    fun deactivateStreamingOption(device: UsbDevice, connection: UsbDeviceConnection, activated: ActivatedStreamingEndpoint) {
        val idleAltSetting = (0 until device.interfaceCount)
            .map { device.getInterface(it) }
            .firstOrNull { it.id == activated.usbInterface.id && it.alternateSetting == 0 }
        if (idleAltSetting != null) {
            try { connection.setInterface(idleAltSetting) } catch (_: Throwable) { /* best-effort cleanup */ }
        }
        connection.releaseInterface(activated.usbInterface)
    }

    private fun Intent.getUsbDeviceExtra(): UsbDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(UsbManager.EXTRA_DEVICE)
        }

    /**
     * Sends a USB Audio Class (UAC1/UAC2) Feature Unit volume control transfer
     * (`SET_CUR` request, Control Selector = VOLUME_CONTROL, 0x02).
     * [volumePercent] is in range 0.0f..1.0f.
     */
    fun setHardwareVolume(connection: UsbDeviceConnection, volumePercent: Float): Boolean {
        val clamped = volumePercent.coerceIn(0.0f, 1.0f)
        val volDb = if (clamped <= 0.01f) -128 else (20.0 * kotlin.math.log10(clamped.toDouble())).toInt().coerceIn(-127, 0)
        val valQ88 = (volDb shl 8) and 0xFFFF
        val data = byteArrayOf((valQ88 and 0xFF).toByte(), ((valQ88 ushr 8) and 0xFF).toByte())

        val requestType = 0x21
        val setCurRequest = 0x01
        val wValue = 0x0200

        val candidateIndices = intArrayOf(0x0200, 0x0500, 0x0100, 0x0000)
        for (wIndex in candidateIndices) {
            val res = connection.controlTransfer(
                requestType, setCurRequest, wValue, wIndex, data, data.size, 500
            )
            if (res >= 0) return true
        }
        return false
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.thesis.bitperfectusb.USB_PERMISSION"
    }
}


/**
 * Minimal single-event pub/sub used to bridge the permission-result broadcast
 * back into the suspend/Flow world without pulling in a full EventBus dependency.
 */
private class SinglePermissionEventBus {
    private val listeners = mutableListOf<(Pair<UsbDevice?, Boolean>) -> Unit>()

    fun subscribe(listener: (Pair<UsbDevice?, Boolean>) -> Unit): () -> Unit {
        listeners += listener
        return { listeners -= listener }
    }

    fun tryEmitEvent(event: Pair<UsbDevice?, Boolean>) {
        listeners.toList().forEach { it(event) }
    }
}
