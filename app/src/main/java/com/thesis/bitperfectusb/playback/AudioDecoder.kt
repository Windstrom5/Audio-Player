package com.thesis.bitperfectusb.playback

import com.thesis.bitperfectusb.domain.model.PcmFormat

/**
 * Common interface for lossless decoders (Section 3.4). Implementations read
 * raw interleaved PCM bytes exactly as they'll be sent to the DAC — no
 * resampling, no bit-depth conversion, no channel remixing happens here.
 */
interface AudioDecoder {
    /** Opens the source and returns its native PCM format; must be called before [read]. */
    fun open(uriString: String): PcmFormat

    /**
     * Reads up to [buffer].size bytes of raw PCM into [buffer].
     * Returns the number of bytes read, or -1 at end of stream.
     */
    fun read(buffer: ByteArray): Int

    /**
     * Jumps decode position to [positionMs] milliseconds from the start of the
     * track. Must be called after [open] and before (or between) [read] calls
     * — never concurrently with an in-progress [read] from another thread.
     * PlaybackController enforces this by fully stopping playback before
     * calling seek and only then restarting, rather than seeking a live
     * decoder out from under its playback thread.
     */
    fun seek(positionMs: Long)

    fun close()
}
