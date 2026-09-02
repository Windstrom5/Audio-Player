package com.thesis.bitperfectusb.playback.dsp

import kotlin.math.pow

/**
 * Foobar2000-style ReplayGain & EBU R128 Loudness Normalizer.
 * Applies track/album perceptual loudness adjustments with anti-clipping headroom limiter.
 */
class ReplayGainProcessor(
    var enabled: Boolean = false,
    var preampDb: Float = 0.0f,
    var preventClipping: Boolean = true
) {
    /**
     * Calculates the combined linear gain multiplier for the given track.
     * @param replayGainDb Track or album gain in dB (e.g. -6.5 dB)
     * @param peak Linear peak amplitude (e.g. 0.98), or 1.0 if not present
     */
    fun calculateGainMultiplier(replayGainDb: Float, peak: Float = 1.0f): Float {
        if (!enabled) return 1.0f

        val totalDb = replayGainDb + preampDb
        var multiplier = 10.0f.pow(totalDb / 20.0f)

        if (preventClipping && peak > 0.0f) {
            val maxAllowed = 1.0f / peak
            if (multiplier > maxAllowed) {
                multiplier = maxAllowed
            }
        }
        return multiplier.coerceIn(0.01f, 4.0f)
    }

    /**
     * Applies the computed gain multiplier to interleaved stereo float samples in-place.
     */
    fun process(samples: FloatArray, frameCount: Int, multiplier: Float) {
        if (!enabled || multiplier == 1.0f) return

        for (i in 0 until frameCount * 2) {
            if (i >= samples.size) break
            val s = samples[i] * multiplier
            // Soft-knee limiting for values exceeding +/- 1.0
            samples[i] = if (s > 1.0f) {
                1.0f - (1.0f / (s + 1.0f))
            } else if (s < -1.0f) {
                -1.0f + (1.0f / (-s + 1.0f))
            } else {
                s
            }
        }
    }
}
