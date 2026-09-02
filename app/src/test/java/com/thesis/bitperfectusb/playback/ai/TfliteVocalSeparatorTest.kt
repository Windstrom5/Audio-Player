package com.thesis.bitperfectusb.playback.ai

import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class TfliteVocalSeparatorTest {

    private lateinit var separator: TfliteVocalSeparator

    @Before
    fun setUp() {
        separator = TfliteVocalSeparator(null)
    }

    @Test
    fun `test STFT engine handles stereo float buffer without crashing`() {
        val sampleRate = 44100
        val buffer = FloatArray(2048) { i ->
            (0.5 * sin(2.0 * PI * 1000.0 * (i / 2) / sampleRate)).toFloat()
        }

        separator.processFloats(
            floats = buffer,
            sampleRate = sampleRate,
            mode = KaraokeModeType.INSTRUMENTAL_ONLY,
            strength = 0.95f,
            preserveBass = true
        )

        assertEquals(2048, buffer.size)
    }

    @Test
    fun `test OFF mode passes buffer through untouched`() {
        val original = FloatArray(1024) { 0.42f }
        val buffer = original.clone()

        separator.processFloats(
            floats = buffer,
            sampleRate = 44100,
            mode = KaraokeModeType.OFF,
            strength = 1.0f,
            preserveBass = true
        )

        for (i in buffer.indices) {
            assertEquals(original[i], buffer[i], 1e-6f)
        }
    }
}
