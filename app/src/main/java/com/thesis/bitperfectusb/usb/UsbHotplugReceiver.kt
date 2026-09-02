package com.thesis.bitperfectusb.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.thesis.bitperfectusb.playback.PlaybackController
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling USB DAC attach and detach hotplug events dynamically.
 * Automatically pauses playback when a USB DAC is unplugged and updates UsbDacManager.
 */
class UsbHotplugReceiver : BroadcastReceiver(), KoinComponent {

    private val usbDacManager: UsbDacManager by inject()
    private val playbackController: PlaybackController by inject()
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

        when (action) {
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                // Auto-pause playback if active when DAC is unplugged
                val state = playbackController.state.value
                if (state.isPlaying) {
                    scope.launch { playbackController.pause() }
                }
                usbDacManager.rescanDevices()
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                usbDacManager.rescanDevices()
            }
        }
    }
}
