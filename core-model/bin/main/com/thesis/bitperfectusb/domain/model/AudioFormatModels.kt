package com.thesis.bitperfectusb.domain.model

/** Lossless container formats supported by this research build (Chapter 1, Scope). */
enum class AudioFileFormat { WAV, FLAC }

/** Which playback path produced a given result — the independent variable of Experiment A. */
enum class EngineType { ANDROID_AUDIOTRACK, CUSTOM_USB_DIRECT }

/** The four standardized research protocols from Chapter 3.8 / Chapter 4. */
enum class ExperimentType { EXPERIMENT_A_ARCHITECTURE, EXPERIMENT_B_SAMPLE_RATE, EXPERIMENT_C_DAC_HARDWARE, EXPERIMENT_D_BUFFER_SIZE }

/**
 * A concrete PCM format triple. Two formats are "native-compatible" only if all
 * three fields match exactly — this is the basis of the integrity scoring model.
 */
data class PcmFormat(
    val sampleRateHz: Int,
    val bitDepth: Int,
    val channels: Int
) {
    fun matchesSampleRate(other: PcmFormat) = sampleRateHz == other.sampleRateHz
    fun matchesBitDepth(other: PcmFormat) = bitDepth == other.bitDepth
    fun matchesChannels(other: PcmFormat) = channels == other.channels

    companion object {
        val CD_QUALITY = PcmFormat(44_100, 16, 2)
    }
}
