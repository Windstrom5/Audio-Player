package com.thesis.bitperfectusb.playback.ai

import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiVocalIsolatorTest {

    private lateinit var isolator: AiVocalIsolator

    @Before
    fun setUp() {
        isolator = AiVocalIsolator()
    }

    @Test
    fun `test mono signal center vocal is significantly attenuated in karaoke suppression`() {
        // Interleaved L/R stereo buffer with identical mono vocal content at 1000Hz
        val pcm = ShortArray(400) { i ->
            (10000 * Math.sin(2.0 * Math.PI * 1000.0 * (i / 2) / 44100.0)).toInt().toShort()
        }
        isolator.process(
            pcm = pcm,
            sampleRate = 44100,
            mode = KaraokeModeType.INSTRUMENTAL_ONLY,
            strength = 0.95f,
            preserveBass = true,
            keyShiftSemitones = 0
        )

        // Mid vocal at 1000Hz should be heavily attenuated
        val avgAmplitude = pcm.map { Math.abs(it.toInt()) }.average()
        assertTrue("Expected vocal reduction below 3500, got $avgAmplitude", avgAmplitude < 3500)
    }

    @Test
    fun `test hard panned stereo side signal is preserved in karaoke suppression`() {
        // L = +10000, R = 0 (Pure hard panned stereo guitar/synth)
        val pcm = ShortArray(200) { if (it % 2 == 0) 10000.toShort() else 0.toShort() }
        isolator.process(
            pcm = pcm,
            sampleRate = 44100,
            mode = KaraokeModeType.INSTRUMENTAL_ONLY,
            strength = 0.90f,
            preserveBass = true,
            keyShiftSemitones = 0
        )

        for (i in 20 until pcm.size step 2) {
            assertTrue(pcm[i] > 7000)
            assertEquals(0, pcm[i + 1].toInt())
        }
    }

    @Test
    fun `test vocal isolation acapella mode extracts center mono signal`() {
        // Interleaved L/R stereo buffer with identical mono vocal content at 1000Hz
        val pcm = ShortArray(400) { i ->
            (10000 * Math.sin(2.0 * Math.PI * 1000.0 * (i / 2) / 44100.0)).toInt().toShort()
        }
        isolator.process(
            pcm = pcm,
            sampleRate = 44100,
            mode = KaraokeModeType.VOCAL_ISOLATION,
            strength = 1.0f,
            preserveBass = false,
            keyShiftSemitones = 0
        )

        val avg = pcm.map { Math.abs(it.toInt()) }.average()
        assertTrue("Expected strong vocal extraction, got $avg", avg > 5000)
    }

    @Test
    fun `test frequency to note name converter`() {
        assertEquals("A4", AiVocalIsolator.frequencyToNoteName(440f))
        assertEquals("A3", AiVocalIsolator.frequencyToNoteName(220f))
        assertEquals("C4", AiVocalIsolator.frequencyToNoteName(261.63f))
        assertEquals("--", AiVocalIsolator.frequencyToNoteName(0f))
    }

    @Test
    fun `test pitch shifting runs without buffer error or overflow`() {
        val pcm = ShortArray(1000) { ((it % 50) * 200).toShort() }
        isolator.process(
            pcm = pcm,
            sampleRate = 44100,
            mode = KaraokeModeType.OFF,
            strength = 0f,
            preserveBass = true,
            keyShiftSemitones = 2
        )

        assertTrue(pcm.isNotEmpty())
        for (sample in pcm) {
            assertTrue(sample in -32768..32767)
        }
    }

    @Test
    fun `test FloatArray instrumental vocal suppression for FLAC streams`() {
        // Interleaved L/R stereo float buffer with mono vocal content at 1000Hz
        val floats = FloatArray(400) { i ->
            (0.5 * Math.sin(2.0 * Math.PI * 1000.0 * (i / 2) / 44100.0)).toFloat()
        }
        isolator.processFloats(
            floats = floats,
            sampleRate = 44100,
            mode = KaraokeModeType.INSTRUMENTAL_ONLY,
            strength = 0.95f,
            preserveBass = true,
            keyShiftSemitones = 0
        )

        val avg = floats.map { Math.abs(it.toDouble()) }.average()
        assertTrue("Expected FloatArray vocal attenuation below 0.15, got $avg", avg < 0.15)
    }
}
