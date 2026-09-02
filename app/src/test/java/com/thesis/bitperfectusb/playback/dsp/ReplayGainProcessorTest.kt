package com.thesis.bitperfectusb.playback.dsp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayGainProcessorTest {

    @Test
    fun `gain multiplier calculation scales correctly`() {
        val processor = ReplayGainProcessor(enabled = true, preampDb = 0.0f, preventClipping = true)

        // 0dB gain should give multiplier 1.0
        val m0 = processor.calculateGainMultiplier(0.0f, 1.0f)
        assertEquals(1.0f, m0, 0.01f)

        // -6dB gain should be ~0.501
        val mMinus6 = processor.calculateGainMultiplier(-6.0f, 1.0f)
        assertEquals(0.501f, mMinus6, 0.01f)

        // +6dB gain with 1.0 peak and anti-clipping should be clamped to 1.0
        val mPlus6Clipped = processor.calculateGainMultiplier(6.0f, 1.0f)
        assertEquals(1.0f, mPlus6Clipped, 0.01f)

        // +6dB gain without anti-clipping (or lower peak 0.25) can amplify
        val mPlus6WithHeadroom = processor.calculateGainMultiplier(6.0f, 0.25f)
        assertTrue(mPlus6WithHeadroom > 1.9f)
    }

    @Test
    fun `soft knee limiter prevents hard clipping distortion`() {
        val processor = ReplayGainProcessor(enabled = true)
        val samples = floatArrayOf(0.5f, -0.5f, 0.9f, -0.9f)

        processor.process(samples, frameCount = 2, multiplier = 2.0f)

        for (s in samples) {
            assertTrue("Sample $s must not exceed +/-1.0", s in -1.0f..1.0f)
        }
    }
}
