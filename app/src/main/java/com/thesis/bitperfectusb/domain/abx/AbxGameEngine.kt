package com.thesis.bitperfectusb.domain.abx

import kotlin.math.pow
import kotlin.random.Random

/**
 * Double-blind ABX listening test mode.
 */
enum class AbxTestMode(
    val title: String,
    val description: String,
    val difficulty: String,
    val sampleATitle: String,
    val sampleBTitle: String
) {
    FLAC_VS_MP3_320(
        title = "Lossless FLAC vs MP3 320 kbps",
        description = "Can you distinguish uncompressed master audio from maximum-bitrate MP3 psychoacoustic compression?",
        difficulty = "EXPERT",
        sampleATitle = "Lossless Master (FLAC / 24-bit)",
        sampleBTitle = "Psychoacoustic MP3 (320 kbps Simulation)"
    ),
    FLAC_VS_MP3_128(
        title = "Lossless FLAC vs MP3 128 kbps",
        description = "Can you hear high-frequency cutoff (16kHz) and pre-echo compression artifacts of 128 kbps audio?",
        difficulty = "MODERATE",
        sampleATitle = "Lossless Master (FLAC)",
        sampleBTitle = "Compressed MP3 (128 kbps Simulation)"
    ),
    HIRES_24BIT_VS_16BIT(
        title = "24-bit Hi-Res vs 16-bit Dithered",
        description = "Tests dynamic range resolution and quantization noise floor between 24-bit Studio Master and 16-bit CD audio.",
        difficulty = "INSANE",
        sampleATitle = "24-bit Studio Master (144 dB Dynamic Range)",
        sampleBTitle = "16-bit Redbook Audio (TPDF Dithered 96 dB)"
    ),
    AI_DSEE_VS_LOSSY(
        title = "AI DSEE Upscaled vs Original Lossy",
        description = "Tests whether on-device neural harmonic high-frequency reconstruction produces an audible difference.",
        difficulty = "HARD",
        sampleATitle = "AI Neural Upscaled (High-Frequency Restored)",
        sampleBTitle = "Bandlimited Lossy Audio"
    ),
    TREBLE_CUT_20K_VS_15K(
        title = "High-Frequency Hearing (20 kHz vs 15 kHz)",
        description = "Tests the upper limits of your human hearing by applying a steep 15 kHz lowpass filter on Sample B.",
        difficulty = "MODERATE",
        sampleATitle = "Full Spectrum (20 kHz+ Air)",
        sampleBTitle = "15 kHz Lowpass Filtered"
    ),
    CUSTOM_LIBRARY_TRACK(
        title = "Custom Library Track ABX",
        description = "Performs an ABX test comparing your currently playing song against an artifact-simulated lossy version.",
        difficulty = "VARIABLE",
        sampleATitle = "Current Track (Pristine)",
        sampleBTitle = "Processed Simulated Variant"
    )
}

/**
 * State of the current ABX double-blind trial.
 */
data class AbxTrialState(
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val correctGuesses: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val isSampleXAssignedToA: Boolean = false,
    val activePlayingSample: AbxSample = AbxSample.NONE,
    val lastGuessResult: Boolean? = null,
    val lastCorrectSample: AbxSample? = null,
    val isCompleted: Boolean = false,
    val history: List<AbxRoundRecord> = emptyList()
) {
    val accuracyPercent: Float
        get() = if (currentRound > 1 || isCompleted) {
            val completedTrials = if (isCompleted) totalRounds else (currentRound - 1)
            if (completedTrials > 0) (correctGuesses.toFloat() / completedTrials) * 100f else 0f
        } else 0f

    /**
     * Calculates the exact binomial p-value for testing H0: p = 0.5 (random guessing).
     */
    val pValue: Double
        get() {
            val n = if (isCompleted) totalRounds else (currentRound - 1)
            val k = correctGuesses
            if (n <= 0) return 1.0
            return calculateBinomialPValue(n, k)
        }

    val earRank: String
        get() = when {
            !isCompleted && currentRound <= 3 -> "CALIBRATING..."
            pValue < 0.01 && accuracyPercent >= 85f -> "🏆 GOLDEN EARS (CERTIFIED)"
            pValue < 0.05 && accuracyPercent >= 75f -> "🥇 TRAINED AUDIOPHILE"
            accuracyPercent >= 60f -> "🥈 ATTENTIVE LISTENER"
            else -> "🎲 INAUDIBLE / GUESSING"
        }

    val earRankDescription: String
        get() = when {
            pValue < 0.01 -> "Statistically significant (p < 0.01). You can reliably detect micro-details and compression artifacts!"
            pValue < 0.05 -> "Statistically significant (p < 0.05). Your hearing can reliably distinguish audio nuances!"
            accuracyPercent >= 60f -> "Noticeable trend, but more trials needed to rule out chance."
            else -> "No statistically significant difference detected (p >= 0.05). Both samples sound identical to your ears."
        }
}

enum class AbxSample { NONE, A, B, X }

data class AbxRoundRecord(
    val round: Int,
    val userGuess: AbxSample,
    val actualX: AbxSample,
    val isCorrect: Boolean
)

/**
 * Core ABX Test Engine managing double-blind trial logic and real-time DSP artifact simulation.
 */
class AbxGameEngine {

    private var trialState = AbxTrialState()
    private var currentMode = AbxTestMode.FLAC_VS_MP3_320

    fun startNewGame(mode: AbxTestMode, rounds: Int = 10): AbxTrialState {
        currentMode = mode
        trialState = AbxTrialState(
            currentRound = 1,
            totalRounds = rounds,
            correctGuesses = 0,
            currentStreak = 0,
            maxStreak = 0,
            isSampleXAssignedToA = Random.nextBoolean(),
            activePlayingSample = AbxSample.NONE,
            lastGuessResult = null,
            lastCorrectSample = null,
            isCompleted = false,
            history = emptyList()
        )
        return trialState
    }

    fun getTrialState(): AbxTrialState = trialState

    fun setActiveSample(sample: AbxSample): AbxTrialState {
        trialState = trialState.copy(activePlayingSample = sample)
        return trialState
    }

    /**
     * Submits user's guess ("X is A" or "X is B").
     */
    fun submitGuess(guessIsA: Boolean): Pair<Boolean, AbxTrialState> {
        val actualIsA = trialState.isSampleXAssignedToA
        val isCorrect = (guessIsA == actualIsA)
        val newCorrect = if (isCorrect) trialState.correctGuesses + 1 else trialState.correctGuesses
        val newStreak = if (isCorrect) trialState.currentStreak + 1 else 0
        val newMaxStreak = maxOf(trialState.maxStreak, newStreak)

        val record = AbxRoundRecord(
            round = trialState.currentRound,
            userGuess = if (guessIsA) AbxSample.A else AbxSample.B,
            actualX = if (actualIsA) AbxSample.A else AbxSample.B,
            isCorrect = isCorrect
        )

        val isFinalRound = trialState.currentRound >= trialState.totalRounds
        val nextRound = if (isFinalRound) trialState.totalRounds else trialState.currentRound + 1

        trialState = trialState.copy(
            currentRound = nextRound,
            correctGuesses = newCorrect,
            currentStreak = newStreak,
            maxStreak = newMaxStreak,
            isSampleXAssignedToA = Random.nextBoolean(), // Re-randomize for next trial
            activePlayingSample = AbxSample.NONE,
            lastGuessResult = isCorrect,
            lastCorrectSample = if (actualIsA) AbxSample.A else AbxSample.B,
            isCompleted = isFinalRound,
            history = trialState.history + record
        )

        return isCorrect to trialState
    }

    /**
     * Real-time audio DSP filter for Sample B depending on test mode.
     * Takes pristine float samples [-1.0f, 1.0f] and applies psychoacoustic compression simulation,
     * bit reduction, or high-frequency filtering.
     */
    fun processSampleB(floats: FloatArray, mode: AbxTestMode, sampleRate: Int) {
        when (mode) {
            AbxTestMode.FLAC_VS_MP3_320 -> {
                // MP3 320k simulation: subtle high-frequency psychoacoustic rolloff above 19.5kHz + slight masking noise
                applyLowpassFilter(floats, sampleRate, cutoffHz = 19500f)
                applySubtleQuantization(floats, bits = 14)
            }
            AbxTestMode.FLAC_VS_MP3_128 -> {
                // MP3 128k simulation: hard 16kHz lowpass cutoff + psychoacoustic roughness
                applyLowpassFilter(floats, sampleRate, cutoffHz = 15800f)
                applySubtleQuantization(floats, bits = 10)
            }
            AbxTestMode.HIRES_24BIT_VS_16BIT -> {
                // 16-bit TPDF dither simulation
                applyTpdfDither16Bit(floats)
            }
            AbxTestMode.AI_DSEE_VS_LOSSY -> {
                // Lossy bandlimited (14kHz lowpass)
                applyLowpassFilter(floats, sampleRate, cutoffHz = 14000f)
            }
            AbxTestMode.TREBLE_CUT_20K_VS_15K -> {
                // Steep 15kHz lowpass
                applyLowpassFilter(floats, sampleRate, cutoffHz = 15000f)
            }
            AbxTestMode.CUSTOM_LIBRARY_TRACK -> {
                // Default 16kHz psychoacoustic cutoff
                applyLowpassFilter(floats, sampleRate, cutoffHz = 16000f)
            }
        }
    }

    private fun applyLowpassFilter(floats: FloatArray, sampleRate: Int, cutoffHz: Float) {
        val dt = 1.0f / sampleRate
        val rc = 1.0f / (2.0f * Math.PI.toFloat() * cutoffHz)
        val alpha = dt / (rc + dt)

        var prevL = 0f
        var prevR = 0f
        for (i in 0 until floats.size - 1 step 2) {
            val l = prevL + alpha * (floats[i] - prevL)
            val r = prevR + alpha * (floats[i + 1] - prevR)
            prevL = l
            prevR = r
            floats[i] = l
            floats[i + 1] = r
        }
    }

    private fun applySubtleQuantization(floats: FloatArray, bits: Int) {
        val steps = (1 shl (bits - 1)).toFloat()
        for (i in floats.indices) {
            floats[i] = kotlin.math.round(floats[i] * steps) / steps
        }
    }

    private fun applyTpdfDither16Bit(floats: FloatArray) {
        val steps = 32768.0f
        val ditherScale = 1.0f / steps
        for (i in floats.indices) {
            val r1 = (Random.nextFloat() - 0.5f) * ditherScale
            val r2 = (Random.nextFloat() - 0.5f) * ditherScale
            val dither = r1 + r2
            val quantized = kotlin.math.round((floats[i] + dither) * steps) / steps
            floats[i] = quantized.coerceIn(-1.0f, 1.0f)
        }
    }
}

/**
 * Computes exact cumulative binomial p-value: P(K >= k) with p = 0.5.
 */
internal fun calculateBinomialPValue(n: Int, k: Int): Double {
    if (k <= 0) return 1.0
    if (k > n) return 0.0

    var sum = 0.0
    for (i in k..n) {
        sum += nCr(n, i) * (0.5).pow(n.toDouble())
    }
    return sum.coerceIn(0.0, 1.0)
}

private fun nCr(n: Int, r: Int): Double {
    if (r < 0 || r > n) return 0.0
    if (r == 0 || r == n) return 1.0
    val k = minOf(r, n - r)
    var c = 1.0
    for (i in 1..k) {
        c = c * (n - (k - i)) / i
    }
    return c
}
