package com.thesis.bitperfectusb.usb

/**
 * Distributes an exact sample rate across fixed-interval isochronous packets
 * using a running-remainder accumulator, the same technique real USB audio
 * drivers (including Linux's snd-usb-audio) use for this exact problem: most
 * sample rates don't divide evenly into either a 1ms full-speed frame or a
 * 125µs high-speed microframe.
 *
 * 44.1kHz over 1000 full-speed frames/second is 44.1 frames per interval —
 * not a whole number. Sending a flat 44 every interval loses 0.1 frames/ms,
 * which is 100 frames/second of drift — audibly wrong within a couple of
 * seconds. This alternates 44 and 45 (using [intervalsPerSecond] and the
 * remainder) so that over any one-second window, exactly [sampleRateHz]
 * frames go out — long-term sample-accurate, not just approximately right.
 */
class IsochronousPacketScheduler(
    private val sampleRateHz: Int,
    private val frameSizeBytes: Int,
    private val intervalsPerSecond: Int
) {
    private var remainder = 0
    private val baseFramesPerInterval = sampleRateHz / intervalsPerSecond
    private val remainderPerInterval = sampleRateHz % intervalsPerSecond

    /** Number of audio frames (not bytes) to send in the next packet, with optional trim offset from PI controller. */
    fun nextPacketFrameCount(trimOffset: Int = 0): Int {
        remainder += remainderPerInterval
        val calculated = if (remainder >= intervalsPerSecond) {
            remainder -= intervalsPerSecond
            baseFramesPerInterval + 1
        } else {
            baseFramesPerInterval
        }
        return (calculated + trimOffset).coerceAtLeast(1)
    }

    fun nextPacketByteSize(trimOffset: Int = 0): Int = nextPacketFrameCount(trimOffset) * frameSizeBytes

    fun reset() {
        remainder = 0
    }
}
