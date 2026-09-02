package com.thesis.bitperfectusb.domain.model

/**
 * Output of the PlaybackIntegrityEngine (Section 3.5). Score of 100 means the
 * active playback path is verified bit-perfect; anything less pinpoints exactly
 * which stage of the chain is expected to modify the sample values.
 */
data class IntegrityResult(
    val score: Int,
    val sampleRatePenalty: Int,
    val bitDepthPenalty: Int,
    val channelPenalty: Int,
    val sourceFormat: PcmFormat,
    val dacNativeFormat: PcmFormat?,
    val engineType: EngineType,
    val reasons: List<String>
) {
    val isBitPerfect: Boolean get() = score == 100

    companion object {
        const val SAMPLE_RATE_PENALTY = 50
        const val BIT_DEPTH_PENALTY = 20
        const val CHANNEL_PENALTY = 10
    }
}
