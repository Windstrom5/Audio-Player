package com.thesis.bitperfectusb.playback

/**
 * Re-packs the decoder's tightly-packed PCM (bytesPerSample = bitDepth / 8 —
 * see [FlacDecoder]/[WavDecoder], which always produce the minimal packing)
 * into the exact per-sample container size a specific USB DAC altsetting
 * actually declared in its Format Type descriptor: bSubslotSize for UAC2,
 * bSubframeSize for UAC1 (see [com.thesis.bitperfectusb.usb.DacCapabilityAnalyzer]).
 *
 * This is the runtime half of the fix for 24-bit playback going silent or
 * garbled on DACs that report a 24-bit bit resolution inside a *wider*
 * container — commonly 4 bytes ("24-in-32"), which is a very common real-world
 * UAC2 configuration. A driver that assumes "24-bit always means 3 packed
 * bytes" desyncs every sample boundary on that hardware: it's still sending
 * real audio bytes, just misaligned against what the DAC's isochronous
 * pipeline is actually expecting to read per sample, which is why the
 * symptom is "doesn't play" / garbled rather than a clean, gradual quality
 * loss.
 *
 * Padding convention: **right-justified** — each sample's own bytes occupy
 * the low-order [sourceBytesPerSample] bytes of the container, in the same
 * little-endian order the decoder already produced, with any remaining
 * high-order bytes of the container set to zero. This is the convention the
 * reference Linux USB-audio driver uses for a bSubslotSize=4 / bBitResolution=24
 * descriptor (ALSA's S24_LE, as distinct from the tightly-packed S24_3LE),
 * and is required by the spec whenever bBitResolution is less than
 * 8 * bSubslotSize (USB Audio Data Formats 2.0, Section 2.3.1).
 *
 * Honest caveat: no physical DAC was available to empirically confirm this
 * against real hardware — see the README's USB Direct troubleshooting note.
 * If a specific device still won't play correctly after this fix, the other
 * real-world convention — left-justified (MSB-aligned, zero-padding the low
 * bits instead) — is the next thing to try.
 */
class PcmContainerPacker(
    private val sourceBytesPerSample: Int,
    private val containerBytes: Int,
    private val channels: Int
) {
    private val frameBytesIn = (sourceBytesPerSample * channels).coerceAtLeast(1)
    private var carry = EMPTY

    /** True when the DAC's container already matches the source's tight
     *  packing (the common case, and every format below 24-bit in practice) —
     *  [repack] then returns its input completely unchanged, so this is a
     *  strict, zero-cost no-op for every previously-working scenario. */
    val isNoOp: Boolean = containerBytes <= sourceBytesPerSample

    /**
     * Repacks [input] (any length — not necessarily a whole number of frames)
     * into container-sized samples. Any trailing partial frame is held back
     * in [carry] and prefixed onto the next call, so frame boundaries are
     * never split across a repack even though decoder reads are arbitrary
     * byte-length chunks.
     */
    fun repack(input: ByteArray): ByteArray {
        if (isNoOp || input.isEmpty()) return input

        val combined = if (carry.isEmpty()) input else carry + input
        val completeFrames = combined.size / frameBytesIn
        val usableBytes = completeFrames * frameBytesIn
        carry = if (usableBytes < combined.size) combined.copyOfRange(usableBytes, combined.size) else EMPTY
        if (completeFrames == 0) return EMPTY

        val samplesTotal = completeFrames * channels
        val out = ByteArray(samplesTotal * containerBytes)
        var inPos = 0
        var outPos = 0
        repeat(samplesTotal) {
            System.arraycopy(combined, inPos, out, outPos, sourceBytesPerSample)
            for (pad in sourceBytesPerSample until containerBytes) {
                out[outPos + pad] = 0
            }
            inPos += sourceBytesPerSample
            outPos += containerBytes
        }
        return out
    }

    companion object {
        private val EMPTY = ByteArray(0)
    }
}
