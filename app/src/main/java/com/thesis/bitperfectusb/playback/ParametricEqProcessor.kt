package com.thesis.bitperfectusb.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Filter type for individual Parametric EQ bands.
 */
enum class FilterType {
    PEAK, LOW_SHELF, HIGH_SHELF, LOW_PASS, HIGH_PASS, NOTCH
}

/**
 * Single Parametric EQ Band configuration.
 */
data class PeqBand(
    val enabled: Boolean = true,
    val type: FilterType = FilterType.PEAK,
    val fcHz: Float = 1000f,
    val gainDb: Float = 0f,
    val q: Float = 1.414f
)

/**
 * Digital 2nd-order Biquad Filter implementation for Parametric Equalization.
 */
class BiquadFilter {
    private var b0 = 1.0f
    private var b1 = 0.0f
    private var b2 = 0.0f
    private var a1 = 0.0f
    private var a2 = 0.0f

    private var z1Left = 0.0f
    private var z2Left = 0.0f
    private var z1Right = 0.0f
    private var z2Right = 0.0f

    fun configure(band: PeqBand, sampleRateHz: Int) {
        if (!band.enabled || band.fcHz <= 0 || sampleRateHz <= 0) {
            setPassThrough()
            return
        }

        val w0 = (2.0 * PI * band.fcHz / sampleRateHz).toFloat()
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0f * band.q.coerceAtLeast(0.1f))
        val A = 10.0f.pow(band.gainDb / 40.0f)

        var b0Unscaled = 1.0f
        var b1Unscaled = 0.0f
        var b2Unscaled = 0.0f
        var a0Unscaled = 1.0f
        var a1Unscaled = 0.0f
        var a2Unscaled = 0.0f

        when (band.type) {
            FilterType.PEAK -> {
                b0Unscaled = 1.0f + alpha * A
                b1Unscaled = -2.0f * cosW0
                b2Unscaled = 1.0f - alpha * A
                a0Unscaled = 1.0f + alpha / A
                a1Unscaled = -2.0f * cosW0
                a2Unscaled = 1.0f - alpha / A
            }
            FilterType.LOW_SHELF -> {
                val sqrtA = sqrt(A)
                b0Unscaled = A * ((A + 1) - (A - 1) * cosW0 + 2 * sqrtA * alpha)
                b1Unscaled = 2 * A * ((A - 1) - (A + 1) * cosW0)
                b2Unscaled = A * ((A + 1) - (A - 1) * cosW0 - 2 * sqrtA * alpha)
                a0Unscaled = (A + 1) + (A - 1) * cosW0 + 2 * sqrtA * alpha
                a1Unscaled = -2 * ((A - 1) + (A + 1) * cosW0)
                a2Unscaled = (A + 1) + (A - 1) * cosW0 - 2 * sqrtA * alpha
            }
            FilterType.HIGH_SHELF -> {
                val sqrtA = sqrt(A)
                b0Unscaled = A * ((A + 1) + (A - 1) * cosW0 + 2 * sqrtA * alpha)
                b1Unscaled = -2 * A * ((A - 1) + (A + 1) * cosW0)
                b2Unscaled = A * ((A + 1) + (A - 1) * cosW0 - 2 * sqrtA * alpha)
                a0Unscaled = (A + 1) - (A - 1) * cosW0 + 2 * sqrtA * alpha
                a1Unscaled = 2 * ((A - 1) - (A + 1) * cosW0)
                a2Unscaled = (A + 1) - (A - 1) * cosW0 - 2 * sqrtA * alpha
            }
            FilterType.LOW_PASS -> {
                b0Unscaled = (1 - cosW0) / 2
                b1Unscaled = 1 - cosW0
                b2Unscaled = (1 - cosW0) / 2
                a0Unscaled = 1 + alpha
                a1Unscaled = -2 * cosW0
                a2Unscaled = 1 - alpha
            }
            FilterType.HIGH_PASS -> {
                b0Unscaled = (1 + cosW0) / 2
                b1Unscaled = -(1 + cosW0)
                b2Unscaled = (1 + cosW0) / 2
                a0Unscaled = 1 + alpha
                a1Unscaled = -2 * cosW0
                a2Unscaled = 1 - alpha
            }
            FilterType.NOTCH -> {
                b0Unscaled = 1.0f
                b1Unscaled = -2.0f * cosW0
                b2Unscaled = 1.0f
                a0Unscaled = 1.0f + alpha
                a1Unscaled = -2.0f * cosW0
                a2Unscaled = 1.0f - alpha
            }
        }

        b0 = b0Unscaled / a0Unscaled
        b1 = b1Unscaled / a0Unscaled
        b2 = b2Unscaled / a0Unscaled
        a1 = a1Unscaled / a0Unscaled
        a2 = a2Unscaled / a0Unscaled
    }

    fun setPassThrough() {
        b0 = 1.0f; b1 = 0.0f; b2 = 0.0f; a1 = 0.0f; a2 = 0.0f
    }

    fun processStereoSample(left: Float, right: Float): Pair<Float, Float> {
        val outL = b0 * left + z1Left
        z1Left = b1 * left - a1 * outL + z2Left
        z2Left = b2 * left - a2 * outL

        val outR = b0 * right + z1Right
        z1Right = b1 * right - a1 * outR + z2Right
        z2Right = b2 * right - a2 * outR

        return Pair(outL, outR)
    }

    fun reset() {
        z1Left = 0.0f; z2Left = 0.0f
        z1Right = 0.0f; z2Right = 0.0f
    }
}

/**
 * Multi-band Parametric EQ Processor for PCM Float/16-bit stereo buffers.
 */
class ParametricEqProcessor(
    private val sampleRateHz: Int,
    bands: List<PeqBand> = defaultBands()
) {
    private val filters = Array(5) { BiquadFilter() }

    init {
        setBands(bands)
    }

    fun setBands(bands: List<PeqBand>) {
        for (i in 0 until 5) {
            if (i < bands.size) {
                filters[i].configure(bands[i], sampleRateHz)
            } else {
                filters[i].setPassThrough()
            }
        }
    }

    fun processFloatStereoInPlace(floatArray: FloatArray) {
        var i = 0
        while (i + 1 < floatArray.size) {
            var left = floatArray[i]
            var right = floatArray[i + 1]

            for (f in filters) {
                val res = f.processStereoSample(left, right)
                left = res.first
                right = res.second
            }

            floatArray[i] = left
            floatArray[i + 1] = right
            i += 2
        }
    }

    fun reset() {
        filters.forEach { it.reset() }
    }

    companion object {
        fun defaultBands() = listOf(
            PeqBand(enabled = false, type = FilterType.LOW_SHELF, fcHz = 100f, gainDb = 0f, q = 0.707f),
            PeqBand(enabled = false, type = FilterType.PEAK, fcHz = 500f, gainDb = 0f, q = 1.414f),
            PeqBand(enabled = false, type = FilterType.PEAK, fcHz = 1000f, gainDb = 0f, q = 1.414f),
            PeqBand(enabled = false, type = FilterType.PEAK, fcHz = 4000f, gainDb = 0f, q = 1.414f),
            PeqBand(enabled = false, type = FilterType.HIGH_SHELF, fcHz = 10000f, gainDb = 0f, q = 0.707f)
        )
    }
}
