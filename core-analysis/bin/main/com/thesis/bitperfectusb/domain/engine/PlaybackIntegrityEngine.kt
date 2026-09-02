package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.IntegrityResult
import com.thesis.bitperfectusb.domain.model.IntegrityResult.Companion.BIT_DEPTH_PENALTY
import com.thesis.bitperfectusb.domain.model.IntegrityResult.Companion.CHANNEL_PENALTY
import com.thesis.bitperfectusb.domain.model.IntegrityResult.Companion.SAMPLE_RATE_PENALTY
import com.thesis.bitperfectusb.domain.model.PcmFormat

/**
 * Domain engine implementing the Chapter 3.5 integrity model:
 *
 *   Integrity Score = 100 - Penalty(SampleRate) - Penalty(BitDepth) - Penalty(Channels)
 *
 * The AudioTrack path is treated as *always* incurring the sample-rate and bit-depth
 * penalties when the source format isn't already 48kHz/native-mixer-format, because
 * AudioFlinger unconditionally resamples/reformats every stream before mixing
 * (Chapter 2.1.1). The custom USB-direct path only incurs a penalty if the connected
 * DAC genuinely cannot represent the source format natively.
 */
class PlaybackIntegrityEngine {

    /** AudioFlinger's internal mixing format, per Chapter 2.1.1 (32-bit float since Android 5.0). */
    private val audioFlingerNativeRateHz = 48_000

    fun evaluate(
        source: PcmFormat,
        dac: DacProfile?,
        engineType: EngineType
    ): IntegrityResult {
        return when (engineType) {
            EngineType.ANDROID_AUDIOTRACK -> evaluateAudioTrackPath(source, dac)
            EngineType.CUSTOM_USB_DIRECT -> evaluateUsbDirectPath(source, dac)
        }
    }

    private fun evaluateAudioTrackPath(source: PcmFormat, dac: DacProfile?): IntegrityResult {
        val reasons = mutableListOf<String>()
        var sampleRatePenalty = 0
        var bitDepthPenalty = 0
        var channelPenalty = 0

        if (source.sampleRateHz != audioFlingerNativeRateHz) {
            sampleRatePenalty = SAMPLE_RATE_PENALTY
            reasons += "AudioFlinger resampled ${source.sampleRateHz}Hz source to its " +
                "$audioFlingerNativeRateHz Hz mixing rate before output."
        }
        // AudioFlinger's internal float mixer normalizes everything; any source that
        // isn't already floating point representation is reformatted on the way in/out.
        bitDepthPenalty = BIT_DEPTH_PENALTY
        reasons += "Mixer normalized ${source.bitDepth}-bit source to its internal " +
            "32-bit float representation, then requantized on output."

        if (dac != null && source.channels > dac.maxChannels) {
            channelPenalty = CHANNEL_PENALTY
            reasons += "Channel count ${source.channels} exceeds DAC's ${dac.maxChannels}-channel output; downmixed."
        }

        val score = (100 - sampleRatePenalty - bitDepthPenalty - channelPenalty).coerceIn(0, 100)
        return IntegrityResult(
            score = score,
            sampleRatePenalty = sampleRatePenalty,
            bitDepthPenalty = bitDepthPenalty,
            channelPenalty = channelPenalty,
            sourceFormat = source,
            dacNativeFormat = dac?.let { PcmFormat(audioFlingerNativeRateHz, 32, source.channels) },
            engineType = EngineType.ANDROID_AUDIOTRACK,
            reasons = reasons
        )
    }

    private fun evaluateUsbDirectPath(source: PcmFormat, dac: DacProfile?): IntegrityResult {
        if (dac == null) {
            return IntegrityResult(
                score = 0,
                sampleRatePenalty = SAMPLE_RATE_PENALTY,
                bitDepthPenalty = BIT_DEPTH_PENALTY,
                channelPenalty = CHANNEL_PENALTY,
                sourceFormat = source,
                dacNativeFormat = null,
                engineType = EngineType.CUSTOM_USB_DIRECT,
                reasons = listOf("No DAC connected — cannot verify native format support.")
            )
        }

        // When the DAC has been fully analyzed, per-altsetting data is available, and
        // that's what actually determines whether playback can start at all (Section
        // 3.4): a real DAC switch requires ONE alternate setting that supports sample
        // rate + bit depth + channels *together* — not each field individually,
        // possibly on three different altsettings that can't all be active at once.
        if (dac.streamingOptions.isNotEmpty()) {
            return if (dac.findExactStreamingOption(source) != null) {
                IntegrityResult(
                    score = 100,
                    sampleRatePenalty = 0,
                    bitDepthPenalty = 0,
                    channelPenalty = 0,
                    sourceFormat = source,
                    dacNativeFormat = source,
                    engineType = EngineType.CUSTOM_USB_DIRECT,
                    reasons = listOf("Source format matches an alternate setting exactly — bit-perfect.")
                )
            } else {
                IntegrityResult(
                    score = 50,
                    sampleRatePenalty = SAMPLE_RATE_PENALTY,
                    bitDepthPenalty = 0,
                    channelPenalty = 0,
                    sourceFormat = source,
                    dacNativeFormat = null,
                    engineType = EngineType.CUSTOM_USB_DIRECT,
                    reasons = listOf(
                        "No single alternate setting on this DAC supports " +
                            "${source.sampleRateHz}Hz/${source.bitDepth}-bit/${source.channels}ch together " +
                            "(individual fields may appear supported on different altsettings, but real " +
                            "playback needs one that matches all three at once)."
                    )
                )
            }
        }

        // Fallback for a profile without per-altsetting data (e.g. built before this
        // check existed) — a coarser field-by-field comparison against the flattened
        // union view, better than nothing but not a guarantee playback will start.
        val reasons = mutableListOf<String>()
        val sampleRatePenalty = if (source.sampleRateHz !in dac.supportedSampleRates) {
            reasons += "DAC does not natively support ${source.sampleRateHz}Hz " +
                "(supports: ${dac.supportedSampleRates.joinToString()}); resampling required."
            SAMPLE_RATE_PENALTY
        } else 0

        val bitDepthPenalty = if (source.bitDepth !in dac.supportedBitDepths) {
            reasons += "DAC does not natively support ${source.bitDepth}-bit depth " +
                "(supports: ${dac.supportedBitDepths.joinToString()}); padding/truncation required."
            BIT_DEPTH_PENALTY
        } else 0

        val channelPenalty = if (source.channels > dac.maxChannels) {
            reasons += "DAC supports at most ${dac.maxChannels} channel(s); source has ${source.channels}."
            CHANNEL_PENALTY
        } else 0

        if (reasons.isEmpty()) {
            reasons += "Source format matches DAC native capability exactly — bit-perfect."
        }

        val score = (100 - sampleRatePenalty - bitDepthPenalty - channelPenalty).coerceIn(0, 100)
        return IntegrityResult(
            score = score,
            sampleRatePenalty = sampleRatePenalty,
            bitDepthPenalty = bitDepthPenalty,
            channelPenalty = channelPenalty,
            sourceFormat = source,
            dacNativeFormat = PcmFormat(source.sampleRateHz, source.bitDepth, dac.maxChannels),
            engineType = EngineType.CUSTOM_USB_DIRECT,
            reasons = reasons
        )
    }
}
