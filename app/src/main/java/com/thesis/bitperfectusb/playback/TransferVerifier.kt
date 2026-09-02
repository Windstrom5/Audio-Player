package com.thesis.bitperfectusb.playback

import com.thesis.bitperfectusb.domain.model.VerificationSnapshot
import java.util.zip.CRC32

/**
 * Provides checkable *proof* of bit-perfect transfer rather than just a
 * format-matching claim — a direct extension of the Section 3.5 integrity
 * model (RO2/RQ2: "automated playback integrity verification framework").
 *
 * Computes two independent running CRC32 checksums: one over bytes as read
 * from the decoder ("source"), one over bytes as actually handed to the
 * transport ("transmitted" — the USB isochronous queue, or
 * `AudioTrack.write()`). For the USB-direct engine, decoder bytes pass
 * through completely unmodified end to end, so these two checksums matching
 * at equal byte counts is direct, checkable evidence that not a single byte
 * was altered anywhere in this app's own pipeline — not an assumption, a
 * verification anyone could reproduce by hashing the source file themselves.
 *
 * For the AudioTrack engine, a mismatch is *expected and correct* whenever
 * this app's own bit-depth truncation runs (Section 5.1.1) — the checksums
 * diverging is itself the demonstrable evidence of exactly the modification
 * RQ1 is about. Either way this measures this app's own boundary; it can't
 * see further modification AudioFlinger itself might apply downstream of
 * `AudioTrack.write()`, which is why the AudioTrack path is still understood
 * to incur the mixer's own reformatting on top of whatever this reports.
 *
 * @param sourceBytesPerSample Bytes per sample as the decoder produces them
 *   (bitDepth / 8 — always tight packing, see FlacDecoder/WavDecoder).
 * @param containerBytes The USB-direct engine's actual per-sample container
 *   size on the wire (UsbStreamingOption.containerBytes), when different from
 *   [sourceBytesPerSample] — e.g. 24-bit samples sent in a 4-byte subslot
 *   (see PcmContainerPacker). Both default to 1 (a no-op ratio), so every
 *   existing caller — and the AudioTrack engine, which never repacks —
 *   behaves exactly as before: raw byte-for-byte comparison, no stripping.
 */
class TransferVerifier(
    private val sourceBytesPerSample: Int = 1,
    private val containerBytes: Int = 1
) {

    private val sourceCrc = CRC32()
    private val transmittedCrc = CRC32()
    private var sourceBytes = 0L
    private var transmittedBytes = 0L
    /** Running byte offset mod [containerBytes] carried across calls to
     *  [recordTransmittedBytes] — isochronous packets aren't guaranteed to
     *  land on sample-container boundaries (IsochronousPacketScheduler sizes
     *  them fractionally), so stripping padding needs a position counter
     *  that survives across packets, not just within one. */
    private var activeBitMask = 0
    private var wirePosition = 0

    fun recordSourceBytes(buffer: ByteArray, length: Int = buffer.size) {
        if (length <= 0) return
        sourceCrc.update(buffer, 0, length)
        sourceBytes += length

        val step = maxOf(1, sourceBytesPerSample)
        var i = 0
        while (i + step <= length) {
            var sample = 0
            for (b in 0 until step) {
                sample = sample or ((buffer[i + b].toInt() and 0xFF) shl (8 * b))
            }
            activeBitMask = activeBitMask or sample
            i += step
        }
    }

    /**
     * [buffer] is what actually reached the wire. When [containerBytes]
     * exceeds [sourceBytesPerSample], that wire form includes zero-padding
     * this app itself added to satisfy the DAC's declared container size
     * (see PcmContainerPacker) — a real hardware requirement, not
     * app-introduced modification, so it's stripped back out here before
     * folding into transmittedCrc. That keeps this comparable against
     * recordSourceBytes (which always sees the decoder's tight packing) and
     * keeps [snapshot]'s `verified` bit meaningful: it still asks "did every
     * real audio byte reach the DAC unaltered", not "did we send exactly as
     * many bytes as we read", which a legitimately wider wire container would
     * otherwise always fail.
     */
    fun recordTransmittedBytes(buffer: ByteArray, length: Int = buffer.size) {
        if (length <= 0) return
        if (containerBytes <= sourceBytesPerSample) {
            transmittedCrc.update(buffer, 0, length)
            transmittedBytes += length
            return
        }
        for (i in 0 until length) {
            if (wirePosition < sourceBytesPerSample) {
                transmittedCrc.update(buffer[i].toInt())
                transmittedBytes += 1
            }
            wirePosition = (wirePosition + 1) % containerBytes
        }
    }

    /**
     * A checksum comparison is only meaningful when both sides have processed
     * the same number of bytes (CRC32 is a running hash over everything fed so
     * far, so unequal prefixes aren't comparable) — [VerificationSnapshot.verified]
     * is null until byte counts happen to align, which reliably happens at
     * track completion and often during steady-state playback too.
     */
    fun snapshot(): VerificationSnapshot = VerificationSnapshot(
        sourceBytes = sourceBytes,
        transmittedBytes = transmittedBytes,
        sourceChecksum = sourceCrc.value,
        transmittedChecksum = transmittedCrc.value,
        verified = if (sourceBytes > 0 && sourceBytes == transmittedBytes) {
            sourceCrc.value == transmittedCrc.value
        } else null,
        activeBitMask = activeBitMask
    )

    fun reset() {
        sourceCrc.reset()
        transmittedCrc.reset()
        sourceBytes = 0L
        transmittedBytes = 0L
        wirePosition = 0
        activeBitMask = 0
    }
}
