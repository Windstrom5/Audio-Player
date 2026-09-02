package com.thesis.bitperfectusb.playback.dsp

import org.junit.Assert.assertEquals
import org.junit.Test

class SpatialSoundstageProcessorTest {

    @Test
    fun `width 0 collapses stereo signal into mono`() {
        val processor = SpatialSoundstageProcessor(width = 0.0f)
        val samples = floatArrayOf(0.8f, 0.2f) // Left = 0.8, Right = 0.2 -> Mid = 0.5

        processor.process(samples, frameCount = 1)

        assertEquals(0.5f, samples[0], 0.001f) // Left becomes 0.5
        assertEquals(0.5f, samples[1], 0.001f) // Right becomes 0.5
    }

    @Test
    fun `width 1 preserves exact original stereo signal`() {
        val processor = SpatialSoundstageProcessor(width = 1.0f)
        val samples = floatArrayOf(0.8f, 0.2f)

        processor.process(samples, frameCount = 1)

        assertEquals(0.8f, samples[0], 0.001f)
        assertEquals(0.2f, samples[1], 0.001f)
    }

    @Test
    fun `width 2 expands side difference signal`() {
        val processor = SpatialSoundstageProcessor(width = 2.0f)
        val samples = floatArrayOf(0.6f, 0.2f) // Mid = 0.4, Side = 0.2 -> Side*2 = 0.4 -> L=0.8, R=0.0

        processor.process(samples, frameCount = 1)

        assertEquals(0.8f, samples[0], 0.001f)
        assertEquals(0.0f, samples[1], 0.001f)
    }
}
