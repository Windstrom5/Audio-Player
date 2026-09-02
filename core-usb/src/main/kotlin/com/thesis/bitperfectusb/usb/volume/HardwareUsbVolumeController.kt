package com.thesis.bitperfectusb.usb.volume

import android.hardware.usb.UsbDeviceConnection
import android.util.Log
import com.thesis.bitperfectusb.usb.UsbAudioConstants
import kotlin.math.roundToInt

/**
 * Direct USB Audio Class (UVC / UAC1 / UAC2) Hardware Volume & Mute Controller.
 * Bypasses Android's coarse 15-25 step software volume slider by transmitting
 * class-specific control transfers directly to the DAC's Feature Unit internal registers.
 *
 * Volume values in USB Audio Class specs are expressed as 16-bit signed Q8.8 fixed-point
 * numbers representing attenuation in units of 1/256 dB (e.g. 0x0000 = 0 dB, 0xFE00 = -2 dB).
 */
class HardwareUsbVolumeController(
    private val connection: UsbDeviceConnection?,
    private val audioControlInterfaceNumber: Int = 0,
    private val featureUnitId: Int = 0x02, // Typical Feature Unit ID in USB Audio DACs
    private val isUac2: Boolean = true
) {
    private val TAG = "HardwareUsbVolume"

    var currentVolumeDb: Float = 0.0f
        private set

    var isMuted: Boolean = false
        private set

    /**
     * Sets hardware volume attenuation directly in decibels (0.0 dB down to -96.0 dB).
     */
    fun setVolumeDb(db: Float): Boolean {
        val clampedDb = db.coerceIn(-96.0f, 0.0f)
        val q88Val = (clampedDb * 256.0f).roundToInt().toShort()

        val data = byteArrayOf(
            (q88Val.toInt() and 0xFF).toByte(),
            ((q88Val.toInt() shr 8) and 0xFF).toByte()
        )

        // wValue: (Control Selector << 8) | Channel Number (0 = Master Channel)
        val wValue = (UsbAudioConstants.FU_VOLUME_CONTROL shl 8) or 0x00
        // wIndex: (Unit ID << 8) | Interface Number
        val wIndex = (featureUnitId shl 8) or (audioControlInterfaceNumber and 0xFF)

        val transferred = connection?.controlTransfer(
            UsbAudioConstants.REQTYPE_CLASS_INTERFACE_OUT,
            UsbAudioConstants.REQUEST_CUR,
            wValue,
            wIndex,
            data,
            data.size,
            1000
        ) ?: -1

        val success = transferred >= 0
        if (success) {
            currentVolumeDb = clampedDb
            Log.d(TAG, "Hardware volume set successfully to $clampedDb dB (Q8.8: $q88Val)")
        } else {
            Log.w(TAG, "Hardware volume control transfer returned $transferred (DAC may use fixed output or different FeatureUnit ID)")
        }
        return success
    }

    /**
     * Sets volume using discrete step counts (e.g. 64, 100, or 256 fine steps, UAPP style).
     */
    fun setVolumeStep(step: Int, maxSteps: Int = 100): Boolean {
        val normalized = (step.toFloat() / maxSteps.coerceAtLeast(1)).coerceIn(0.0f, 1.0f)
        // Logarithmic volume taper: 0% -> -96 dB, 100% -> 0 dB
        val db = if (normalized <= 0.001f) -96.0f else (1.0f - normalized) * -60.0f
        return setVolumeDb(-db.coerceAtLeast(0.0f))
    }

    /**
     * Sets DAC hardware mute status.
     */
    fun setMuteStatus(mute: Boolean): Boolean {
        val data = byteArrayOf(if (mute) 0x01 else 0x00)
        val wValue = (UsbAudioConstants.FU_MUTE_CONTROL shl 8) or 0x00
        val wIndex = (featureUnitId shl 8) or (audioControlInterfaceNumber and 0xFF)

        val transferred = connection?.controlTransfer(
            UsbAudioConstants.REQTYPE_CLASS_INTERFACE_OUT,
            UsbAudioConstants.REQUEST_CUR,
            wValue,
            wIndex,
            data,
            data.size,
            1000
        ) ?: -1

        val success = transferred >= 0
        if (success) {
            isMuted = mute
            Log.d(TAG, "Hardware mute set to $mute")
        }
        return success
    }
}
