package com.thesis.bitperfectusb.playback.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KaraokeMicScorerTest {

    private lateinit var scorer: KaraokeMicScorer

    @Before
    fun setUp() {
        scorer = KaraokeMicScorer()
    }

    @Test
    fun `test initial score state is ready and empty`() {
        val state = scorer.scoreState.value
        assertEquals("READY", state.currentGrade)
        assertEquals(0, state.totalScore)
        assertEquals(0, state.combo)
        assertEquals("--", state.singerNote)
    }

    @Test
    fun `test resetScore resets score and combo to zero`() {
        scorer.resetScore()
        val state = scorer.scoreState.value
        assertEquals(0, state.totalScore)
        assertEquals(0, state.combo)
        assertEquals(0, state.maxCombo)
    }
}
