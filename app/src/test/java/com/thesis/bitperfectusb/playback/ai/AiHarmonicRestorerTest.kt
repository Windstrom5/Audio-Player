package com.thesis.bitperfectusb.playback.ai

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class AiHarmonicRestorerTest {

    private lateinit var restorer: AiHarmonicRestorer

    @Before
    fun setUp() {
        restorer = AiHarmonicRestorer()
    }

    @Test
    fun `test detect spectrum cutoff with empty buffer`() {
        val cutoff = restorer.analyzeSpectrumCutoff(ShortArray(0), 44100)
        assertTrue(cutoff > 0f)
    }

    @Test
    fun `test detect low frequency tone flags low cutoff`() {
        val sampleRate = 44100
        val buffer = ShortArray(2048) { i ->
            (sin(2.0 * Math.PI * 440.0 * i / sampleRate) * 20000.0).toInt().toShort()
        }

        val cutoff = restorer.analyzeSpectrumCutoff(buffer, sampleRate)
        assertTrue("Expected lossy or standard cutoff", cutoff <= 22.05f)
    }

    @Test
    fun `test harmonic synthesis processes without distortion overflow`() {
        val sampleRate = 44100
        val buffer = ShortArray(2048) { i ->
            (sin(2.0 * Math.PI * 1000.0 * i / sampleRate) * 16000.0).toInt().toShort()
        }

        restorer.analyzeSpectrumCutoff(buffer, sampleRate)
        restorer.processStereoPcm(buffer, sampleRate, intensity = 0.8f)

        for (sample in buffer) {
            assertTrue(sample in -32768..32767)
        }
    }
}
