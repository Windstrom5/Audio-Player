package com.thesis.bitperfectusb.domain.model

/**
 * One concrete, activatable USB Audio Class streaming configuration: a specific
 * (interface number, alternate setting) pair, the exact PCM shape it carries, and
 * everything needed to actually switch the hardware into that mode — which
 * endpoint to stream to, which control transfer sets the sample rate, and
 * whether there's a feedback endpoint to synchronize against.
 *
 * This is the piece a "just parse the first isochronous endpoint" implementation
 * skips, and exactly the piece a real bit-perfect driver (UAPP-style) can't skip:
 * audio streaming interfaces sit at alternate-setting 0 (zero bandwidth, no
 * endpoints) until you explicitly SET_INTERFACE to the altsetting that matches
 * the format you actually want to play, and — critically — the DAC's clock isn't
 * automatically at the source's rate just because you started sending bytes; you
 * have to tell it to switch first.
 */
data class UsbStreamingOption(
    val interfaceNumber: Int,
    val alternateSetting: Int,
    val bitDepth: Int,
    val channels: Int,
    /** For UAC1 this comes straight from the Format Type descriptor. For UAC2 it's
     *  the shared Clock Source's queried range, attached to every altsetting that
     *  shares that clock — see DacCapabilityAnalyzer. */
    val supportedSampleRates: List<Int>,
    val endpointAddress: Int,
    val maxPacketSizeBytes: Int,
    val isUac2: Boolean,
    val feedbackEndpointAddress: Int?,
    /** UAC2 only — needed to target the SET_CUR sampling-frequency control at the right entity. */
    val clockSourceId: Int?,
    /** UAC2 only — the AudioControl interface number the clock source's control lives on. */
    val acInterfaceNumber: Int? = null,
    /**
     * The actual per-sample container size on the wire — UAC2's bSubslotSize or
     * UAC1's bSubframeSize (see DacCapabilityAnalyzer), which is not always equal
     * to `bitDepth / 8`. Many real 24-bit DACs declare a 4-byte container with a
     * 24-bit resolution inside it ("24-in-32"), not a tightly-packed 3-byte one —
     * sending 3-byte-packed samples to one of those plays as silence or garbage.
     * See PcmContainerPacker, which repacks decoder output to this size before
     * it reaches the wire. Defaults to `bitDepth / 8` (today's assumption) for
     * any construction site that hasn't supplied the real descriptor value.
     */
    val containerBytes: Int = bitDepth / 8
) {
    fun supports(format: PcmFormat): Boolean =
        format.sampleRateHz in supportedSampleRates &&
            format.bitDepth == bitDepth &&
            format.channels <= channels

    val hasFeedbackEndpoint: Boolean get() = feedbackEndpointAddress != null
}

/**
 * The capability map of an attached USB DAC, built by parsing raw USB Audio Class
 * descriptors in user-space (Section 3.3, DacCapabilityAnalyzer). This never relies
 * on Android's audio policy manager, since that layer may impose defaults that do
 * not reflect the hardware's true native capability.
 */
data class DacProfile(
    val id: Long = 0L,
    val vendorId: Int,
    val productId: Int,
    val productName: String,
    val manufacturerName: String?,
    val isUac2: Boolean,
    val supportedSampleRates: List<Int>,
    val supportedBitDepths: List<Int>,
    val maxChannels: Int,
    val maxPacketSizeBytes: Int,
    val hasAsyncFeedbackEndpoint: Boolean,
    val dateProfiledEpochMs: Long,
    /** Every activatable (interface, altsetting) streaming configuration this DAC exposes. */
    val streamingOptions: List<UsbStreamingOption> = emptyList()
) {
    fun supports(format: PcmFormat): Boolean =
        format.sampleRateHz in supportedSampleRates &&
            format.bitDepth in supportedBitDepths &&
            format.channels <= maxChannels

    /**
     * Finds the exact hardware configuration to activate for [format] — an exact
     * match only, since silently picking a "close enough" altsetting is exactly
     * the kind of substitution that breaks bit-perfect playback.
     */
    fun findExactStreamingOption(format: PcmFormat): UsbStreamingOption? =
        streamingOptions.firstOrNull { it.supports(format) }

    /**
     * Best-effort fallback used when [findExactStreamingOption] returns null — the
     * source's native rate isn't supported but the DAC may still be able to play it
     * at a slightly different rate rather than forcing a silent AudioTrack fallback.
     *
     * Priority:
     * 1. Same bit depth + nearest supported sample rate (e.g. 96kHz source → 48kHz
     *    altsetting if 96kHz isn't available, both in the 48k family).
     * 2. Any altsetting supporting the source's channel count (bit depth may differ).
     *
     * Returns null if no altsetting can carry this channel count at all.
     */
    fun findBestStreamingOption(format: PcmFormat): UsbStreamingOption? {
        val sameBitDepth = streamingOptions.filter { it.bitDepth == format.bitDepth && format.channels <= it.channels }
        if (sameBitDepth.isNotEmpty()) {
            // Among same-bit-depth options, pick the one whose supported rate is
            // nearest to (but not exceeding) the source rate — avoids upsampling.
            return sameBitDepth
                .flatMap { option -> option.supportedSampleRates.map { rate -> option to rate } }
                .filter { (_, rate) -> rate <= format.sampleRateHz }
                .maxByOrNull { (_, rate) -> rate }
                ?.first
                ?: sameBitDepth.minByOrNull { option ->
                    option.supportedSampleRates.minOfOrNull { kotlin.math.abs(it - format.sampleRateHz) } ?: Int.MAX_VALUE
                }
        }
        // Last resort: any option that can carry this channel count
        return streamingOptions
            .filter { format.channels <= it.channels }
            .maxByOrNull { it.bitDepth }
    }

    /** A human label like "0x08BB:0x2704" used throughout dashboards and CSV exports. */
    val usbIdLabel: String
        get() = "0x%04X:0x%04X".format(vendorId, productId)
}
