package com.thesis.bitperfectusb.playback.dsp

import com.thesis.bitperfectusb.domain.model.ReverbPreset

/**
 * Studio Acoustic Reverb Environment Simulator.
 * Schroeder reverberation engine featuring feedback comb filters and allpass delay lines
 * configured for authentic acoustic simulation (Studio Room, Concert Hall, Live Stage, etc.).
 */
class StudioReverbProcessor(
    var enabled: Boolean = false,
    var preset: ReverbPreset = ReverbPreset.STUDIO_ROOM,
    var wetDry: Float = 0.25f // 0.0 = 100% Dry, 1.0 = 100% Wet
) {
    // Delay line buffer sizes for 44.1kHz / 48kHz
    private val combDelays = intArrayOf(1116, 1188, 1277, 1356)
    private val allpassDelays = intArrayOf(225, 556)

    private val combBuffers = Array(4) { FloatArray(combDelays[it]) }
    private val combIndices = IntArray(4) { 0 }

    private val allpassBuffers = Array(2) { FloatArray(allpassDelays[it]) }
    private val allpassIndices = IntArray(2) { 0 }

    /**
     * Processes interleaved stereo float samples in-place.
     */
    fun process(samples: FloatArray, frameCount: Int) {
        if (!enabled || wetDry <= 0.001f) return

        val feedback = preset.roomSize.coerceIn(0.2f, 0.95f)
        val wet = wetDry.coerceIn(0.0f, 1.0f)
        val dry = 1.0f - (wet * 0.5f)

        for (i in 0 until frameCount) {
            val leftIdx = i * 2
            val rightIdx = leftIdx + 1
            if (rightIdx >= samples.size) break

            val inputMono = (samples[leftIdx] + samples[rightIdx]) * 0.5f

            // 1. Parallel Comb Filters
            var combOut = 0.0f
            for (c in 0 until 4) {
                val buf = combBuffers[c]
                val idx = combIndices[c]
                val delayed = buf[idx]

                buf[idx] = inputMono + delayed * feedback
                combIndices[c] = (idx + 1) % buf.size
                combOut += delayed
            }
            combOut *= 0.25f

            // 2. Series Allpass Filters
            var allpassOut = combOut
            for (a in 0 until 2) {
                val buf = allpassBuffers[a]
                val idx = allpassIndices[a]
                val delayed = buf[idx]

                val current = allpassOut + delayed * -0.5f
                buf[idx] = allpassOut + current * 0.5f
                allpassIndices[a] = (idx + 1) % buf.size
                allpassOut = delayed + current * 0.5f
            }

            // Mix dry and wet signals
            samples[leftIdx] = (samples[leftIdx] * dry + allpassOut * wet).coerceIn(-1.0f, 1.0f)
            samples[rightIdx] = (samples[rightIdx] * dry + allpassOut * wet).coerceIn(-1.0f, 1.0f)
        }
    }
}
