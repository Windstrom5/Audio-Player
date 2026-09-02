package com.thesis.bitperfectusb.domain.abx

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AbxGameEngineTest {

    private lateinit var engine: AbxGameEngine

    @Before
    fun setUp() {
        engine = AbxGameEngine()
    }

    @Test
    fun `test startNewGame initializes round 1 with zero score`() {
        val state = engine.startNewGame(AbxTestMode.FLAC_VS_MP3_320, 10)
        assertEquals(1, state.currentRound)
        assertEquals(10, state.totalRounds)
        assertEquals(0, state.correctGuesses)
        assertEquals(0, state.currentStreak)
        assertEquals(AbxSample.NONE, state.activePlayingSample)
        assertEquals(false, state.isCompleted)
    }

    @Test
    fun `test submitGuess increments round and updates score on correct answer`() {
        val state = engine.startNewGame(AbxTestMode.FLAC_VS_MP3_128, 5)
        val sampleXIsA = state.isSampleXAssignedToA

        // Guess correctly according to secret sample assignment
        val (isCorrect, nextState) = engine.submitGuess(guessIsA = sampleXIsA)

        assertTrue(isCorrect)
        assertEquals(1, nextState.correctGuesses)
        assertEquals(1, nextState.currentStreak)
        assertEquals(2, nextState.currentRound)
        assertEquals(1, nextState.history.size)
    }

    @Test
    fun `test binomial p-value calculation for 10 out of 10 correct`() {
        val pVal = calculateBinomialPValue(10, 10)
        // 0.5^10 = 0.0009765625 (p < 0.001)
        assertTrue(pVal < 0.001)
    }

    @Test
    fun `test binomial p-value calculation for 5 out of 10 random guessing`() {
        val pVal = calculateBinomialPValue(10, 5)
        // P(K >= 5) for 10 trials is > 0.5
        assertTrue(pVal > 0.5)
    }

    @Test
    fun `test processSampleB lowpass filter and dither does not crash`() {
        val floats = FloatArray(1024) { 0.5f }
        engine.processSampleB(floats, AbxTestMode.FLAC_VS_MP3_320, 44100)
        assertNotNull(floats)
        assertTrue(floats[0] <= 1.0f && floats[0] >= -1.0f)
    }
}
