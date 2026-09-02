package com.thesis.bitperfectusb.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class DynamicRangeMeterTest {

    @Test
    fun testEmptyPcmReturnsDefaultMasterScore() {
        val res = DynamicRangeMeter.calculate(ByteArray(0))
        assertEquals(14, res.drScore)
        assertTrue(res.rating.contains("Audiophile"))
    }

    @Test
    fun testHighDynamicRangeSineWave() {
        // Generate a 16-bit PCM buffer with dynamic bursts and quiet passages (High DR)
        val sampleRate = 44100
        val durationSeconds = 1
        val numSamples = sampleRate * durationSeconds
        val pcmBytes = ByteArray(numSamples * 4) // Stereo 16-bit (4 bytes per sample)

        var idx = 0
        for (i in 0 until numSamples) {
            // Burst amplitude modulation
            val envelope = if (i % 8000 < 500) 0.95 else 0.05
            val sampleVal = (sin(2.0 * Math.PI * 440.0 * i / sampleRate) * envelope * 32767.0).toInt()

            val low = (sampleVal and 0xFF).toByte()
            val high = ((sampleVal shr 8) and 0xFF).toByte()

            // Left
            pcmBytes[idx++] = low
            pcmBytes[idx++] = high
            // Right
            pcmBytes[idx++] = low
            pcmBytes[idx++] = high
        }

        val res = DynamicRangeMeter.calculate(pcmBytes, bitDepth = 16, channels = 2)
        assertTrue("DR Score should be dynamic (> 8)", res.drScore >= 8)
        assertTrue(res.peakDbfs <= 0.0f)
        assertTrue(res.rmsDbfs < res.peakDbfs)
    }
}
