package com.thesis.bitperfectusb.playback.dsp

/**
 * Poweramp-style Stereo Soundstage Spatial Expander.
 * Uses a phase-aligned Mid/Side (M/S) matrix to widen or collapse the stereo acoustic field:
 * - width = 0.0: Pure Mono (Mid only)
 * - width = 1.0: Original Stereo mix
 * - width = 2.0: Super-Wide Holographic 3D Soundstage
 */
class SpatialSoundstageProcessor(
    var width: Float = 1.0f
) {
    /**
     * Processes interleaved stereo float samples in-place.
     */
    fun process(samples: FloatArray, frameCount: Int) {
        if (width == 1.0f) return // Bypass when at unity 1.0

        val w = width.coerceIn(0.0f, 2.0f)
        for (i in 0 until frameCount) {
            val leftIndex = i * 2
            val rightIndex = leftIndex + 1
            if (rightIndex >= samples.size) break

            val left = samples[leftIndex]
            val right = samples[rightIndex]

            val mid = (left + right) * 0.5f
            val side = (left - right) * 0.5f * w

            samples[leftIndex] = (mid + side).coerceIn(-1.0f, 1.0f)
            samples[rightIndex] = (mid - side).coerceIn(-1.0f, 1.0f)
        }
    }
}
