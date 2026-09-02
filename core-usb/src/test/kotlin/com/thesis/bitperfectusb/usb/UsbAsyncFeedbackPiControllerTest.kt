package com.thesis.bitperfectusb.usb

import org.junit.Assert.assertEquals
import org.junit.Test

class UsbAsyncFeedbackPiControllerTest {

    @Test
    fun `nominal feedback produces zero frame trim offset`() {
        val controller = UsbAsyncFeedbackPiController(
            sampleRateHz = 44100,
            intervalsPerSecond = 1000,
            isUac2 = false
        )
        // 44.1 frames/ms in 10.14 fixed point format: 44.1 * 16384 = 722534
        val nominalRaw1014 = (44.1 * 16384).toInt()
        controller.processFeedbackRaw(nominalRaw1014)

        assertEquals(0, controller.currentFrameOffset)
    }

    @Test
    fun `faster DAC rate produces positive frame trim offset`() {
        val controller = UsbAsyncFeedbackPiController(
            sampleRateHz = 44100,
            intervalsPerSecond = 1000,
            isUac2 = false,
            kp = 1.0f,
            ki = 0.1f
        )
        // 45.0 frames/ms in 10.14 fixed point format: 45 * 16384 = 737280
        val fastRaw1014 = (45.0 * 16384).toInt()
        repeat(5) {
            controller.processFeedbackRaw(fastRaw1014)
        }

        assertEquals(1, controller.currentFrameOffset)
    }
}
