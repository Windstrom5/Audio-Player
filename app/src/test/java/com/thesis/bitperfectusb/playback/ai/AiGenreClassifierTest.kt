package com.thesis.bitperfectusb.playback.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class AiGenreClassifierTest {

    private lateinit var classifier: AiGenreClassifier

    @Before
    fun setUp() {
        classifier = AiGenreClassifier(context = null)
    }

    @Test
    fun `test classify empty buffer returns default pop with non-empty probabilities`() {
        val emptyBuffer = ShortArray(0)
        val result = classifier.classifyPcmSnapshot(emptyBuffer)

        assertNotNull(result)
        assertEquals("Pop & Modern", result.topGenre)
        assertEquals(10, result.suggestedEqGains.size)
        assertTrue(result.genreProbabilities.isNotEmpty())
    }

    @Test
    fun `test classify low frequency sine wave triggers bass prominent genre like EDM or Hip-Hop`() {
        val sampleRate = 44100
        val freqHz = 55.0 // Sub-bass A1
        val buffer = ShortArray(2048) { i ->
            (sin(2.0 * Math.PI * freqHz * i / sampleRate) * 30000.0).toInt().toShort()
        }

        val result = classifier.classifyPcmSnapshot(buffer, sampleRate)
        assertNotNull(result)
        assertTrue("Expected bass genre but got ${result.topGenre}", 
            result.topGenre.contains("EDM") || 
            result.topGenre.contains("Hip-Hop") || 
            result.topGenre.contains("Ambient") ||
            result.genreProbabilities["EDM & Dance"]!! > 5f
        )
        assertTrue(result.confidencePct in 40.0f..100.0f)
    }

    @Test
    fun `test supported genres has 9 categories and all have valid 10-band presets`() {
        assertEquals(9, AiGenreClassifier.SUPPORTED_GENRES.size)
        for (genre in AiGenreClassifier.SUPPORTED_GENRES) {
            val preset = AiGenreClassifier.GENRE_EQ_PRESETS[genre]
            assertNotNull("Missing preset for $genre", preset)
            assertEquals("Preset for $genre must have 10 bands", 10, preset!!.size)
        }
    }
}
