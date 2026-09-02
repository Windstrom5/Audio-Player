package com.thesis.bitperfectusb.playback

import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.util.Log
import com.thesis.bitperfectusb.domain.model.PcmFormat
import org.kc7bfi.jflac.FLACDecoder
import org.kc7bfi.jflac.frame.Frame
import java.io.InputStream

/**
 * Decodes FLAC to raw PCM using the platform's MediaExtractor + MediaCodec FLAC
 * decoder (Section 3.4, FlacDecoder). FLAC's compression is defined to be
 * mathematically lossless, so a spec-compliant decoder reproduces the original
 * samples exactly — this app doesn't reimplement FLAC's LPC/Rice-coding decoder
 * from scratch, since that would just be re-deriving the same guaranteed-correct
 * output through a much larger, harder-to-verify surface.
 *
 * One honest caveat: some OEM decoder implementations cap their PCM *output*
 * encoding at 16-bit even for a 24-bit source, regardless of what this requests.
 * This class asks for 24-bit output and reports whatever the codec actually
 * negotiates back — so the rest of the pipeline (including the integrity
 * scoring) always reflects the real depth of the bytes in flight, not an
 * assumption. Because both the AudioTrack and USB-direct engines consume the
 * exact same decoded PCM, this decoder-level ceiling — if it occurs on a given
 * device — affects both arms of Experiment A equally and doesn't bias that
 * comparison.
 */
class FlacDecoder(private val context: Context) : AudioDecoder {

    private var extractor: MediaExtractor? = null
    private var codec: MediaCodec? = null
    private var capturedOutputFormat: MediaFormat? = null
    private var isEndOfStream = false
    private var isEndOfDecoder = false
    @Volatile private var isClosed = false
    private val pendingChunks = ArrayDeque<ByteArray>()
    private var pendingOffset = 0
    /** Non-null once every MediaCodec candidate has failed and decoding fell
     *  back to the pure-JVM jflac path (see the end of [open]). When set,
     *  [read] and [close] delegate here instead of touching MediaCodec at all. */
    private var softwareFallback: JflacPcmSource? = null
    private var openedUri: Uri? = null
    private var openedId3TagSizeBytes: Long = 0L

    override fun open(uriString: String): PcmFormat {
        val uri = Uri.parse(uriString)
        val ex = MediaExtractor()
        var pfd: android.os.ParcelFileDescriptor? = null
        // Some FLAC files in the wild (tagged by foobar2000, Mp3Tag, various download
        // sources, etc.) have an ID3v2 block prepended before the "fLaC" stream marker.
        // That's non-compliant with the FLAC spec, but common enough that Android's
        // own FLAC demuxer chokes on it on many OS versions — it looks for "fLaC" at
        // byte 0 and reports zero tracks if it isn't there, which is indistinguishable
        // from a genuinely corrupt file. Detecting and skipping that tag is what lets
        // those specific files play at all instead of silently failing to find a track.
        val id3TagSizeBytes = detectLeadingId3v2TagSize(uri)
        openedUri = uri
        openedId3TagSizeBytes = id3TagSizeBytes
        try {
            pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                if (id3TagSizeBytes > 0) {
                    val totalLength = pfd.statSize
                    val remainingLength = if (totalLength > id3TagSizeBytes) totalLength - id3TagSizeBytes else totalLength
                    ex.setDataSource(pfd.fileDescriptor, id3TagSizeBytes, remainingLength)
                } else {
                    ex.setDataSource(pfd.fileDescriptor)
                }
            } else {
                ex.setDataSource(context, uri, null)
            }
        } catch (_: Exception) {
            ex.setDataSource(context, uri, null)
        } finally {
            try { pfd?.close() } catch (_: Exception) {}
        }

        var trackIndex = -1
        var trackFormat: MediaFormat? = null
        for (i in 0 until ex.trackCount) {
            val f = ex.getTrackFormat(i)
            if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                trackIndex = i
                trackFormat = f
                break
            }
        }
        val format = requireNotNull(trackFormat) { "No audio track found in $uriString" }
        ex.selectTrack(trackIndex)
        extractor = ex

        // Always enforce audio/flac MIME type so MediaCodec creates the FLAC decoder
        // (c2.android.flac.decoder / OMX.google.flac.decoder) rather than c2.android.raw.decoder.
        val mime = MediaFormat.MIMETYPE_AUDIO_FLAC
        format.setString(MediaFormat.KEY_MIME, mime)

        // Ask for the highest-fidelity PCM encoding available if API 31+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            format.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_24BIT_PACKED)
        }

        // Some devices (seen on certain MediaTek/Transsion builds) register a
        // FLAC decoder that reliably fails immediately on real data — every
        // file, not just specific ones — while createDecoderByType() always
        // deterministically hands back that same broken component. Enumerating
        // every FLAC-capable codec actually registered on the device and
        // falling through them means we still have a chance if the device
        // happens to expose more than one (e.g. a distinct one behind the
        // "software" Codec2 store rather than "default").
        val candidateNames = buildFlacDecoderCandidates(mime)
        var lastError: Exception? = null
        var opened = false

        for ((index, name) in candidateNames.withIndex()) {
            // Reset per-attempt decode state — a prior failed candidate may have
            // already consumed extractor position and partial decode state.
            isEndOfStream = false
            isEndOfDecoder = false
            capturedOutputFormat = null
            pendingChunks.clear()
            pendingOffset = 0
            if (index > 0) {
                try { ex.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC) } catch (_: Exception) { /* best-effort */ }
            }

            val mc = try {
                if (name != null) MediaCodec.createByCodecName(name) else MediaCodec.createDecoderByType(mime)
            } catch (e: Exception) {
                lastError = e
                continue
            }

            try {
                try {
                    mc.configure(format, null, null, 0)
                } catch (e: Exception) {
                    // Fallback: configure without custom KEY_PCM_ENCODING if rejected by vendor codec
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        format.removeKey(MediaFormat.KEY_PCM_ENCODING)
                    }
                    mc.configure(format, null, null, 0)
                }
                mc.start()
                codec = mc

                // Pump the decoder until it reports its *actual* negotiated output
                // format — some decoders emit INFO_OUTPUT_FORMAT_CHANGED
                // immediately, others only after the first real output buffer.
                // This is also where a broken codec actually reveals itself
                // (decodeMore() throws), which is why the retry loop wraps this.
                var attempts = 0
                while (capturedOutputFormat == null && !isEndOfDecoder && attempts < 200) {
                    val progressed = decodeMore()
                    if (!progressed) {
                        try {
                            Thread.sleep(5)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                    attempts++
                }
                if (capturedOutputFormat == null) {
                    throw IllegalStateException("Decoder candidate '${name ?: "default"}' produced no output format")
                }
                opened = true
                lastError = null
                break
            } catch (e: Exception) {
                Log.w(TAG, "FLAC decoder candidate '${name ?: "default"}' failed on real data, trying next: ${e.message}")
                lastError = e
                try { mc.stop() } catch (_: Exception) { /* already stopped */ }
                try { mc.release() } catch (_: Exception) { /* best-effort */ }
                codec = null
            }
        }

        if (!opened) {
            Log.w(TAG, "All MediaCodec FLAC candidates failed on this device, falling back to software (jflac) decode: ${lastError?.message}")
            try {
                val fallback = JflacPcmSource { openStreamSkippingId3(uri, id3TagSizeBytes) }
                val fallbackFormat = fallback.open()
                softwareFallback = fallback
                return fallbackFormat
            } catch (e: Exception) {
                throw lastError ?: e
            }
        }

        val nativeBitDepth = getNativeFlacBitDepth(uri, id3TagSizeBytes)
        if (nativeBitDepth > 16) {
            Log.i(TAG, "Hi-res FLAC source detected ($nativeBitDepth-bit). Using jflac pure-JVM decoder for bit-exact decoding.")
            try { codec?.stop() } catch (_: Exception) {}
            try { codec?.release() } catch (_: Exception) {}
            try { extractor?.release() } catch (_: Exception) {}
            codec = null
            extractor = null

            val fallback = JflacPcmSource { openStreamSkippingId3(uri, id3TagSizeBytes) }
            val fallbackFormat = fallback.open()
            softwareFallback = fallback
            return fallbackFormat
        }

        val outFormat = capturedOutputFormat ?: format
        val sampleRate = outFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = outFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val bitDepth = 16
        return PcmFormat(sampleRate, bitDepth, channels)
    }


    /**
     * Reads the FLAC STREAMINFO header to determine the native bit depth (bits per sample)
     * of the FLAC source file (e.g. 16, 24, 32).
     */
    private fun getNativeFlacBitDepth(uri: Uri, id3TagSizeBytes: Long): Int {
        return try {
            openStreamSkippingId3(uri, id3TagSizeBytes).use { stream ->
                val magic = ByteArray(4)
                var off = 0
                while (off < magic.size) {
                    val n = stream.read(magic, off, magic.size - off)
                    if (n == -1) break
                    off += n
                }
                if (off != 4 || String(magic, Charsets.US_ASCII) != "fLaC") return 16

                val blockHeader = ByteArray(4)
                off = 0
                while (off < blockHeader.size) {
                    val n = stream.read(blockHeader, off, blockHeader.size - off)
                    if (n == -1) break
                    off += n
                }
                if (off != 4) return 16

                val blockType = blockHeader[0].toInt() and 0x7F
                val length = ((blockHeader[1].toInt() and 0xFF) shl 16) or
                    ((blockHeader[2].toInt() and 0xFF) shl 8) or
                    (blockHeader[3].toInt() and 0xFF)

                if (blockType == 0) { // STREAMINFO block
                    val block = ByteArray(length)
                    off = 0
                    while (off < length) {
                        val n = stream.read(block, off, length - off)
                        if (n == -1) break
                        off += n
                    }
                    if (off == length && length >= 18) {
                        var packed = 0L
                        for (i in 10..17) {
                            packed = (packed shl 8) or (block[i].toLong() and 0xFF)
                        }
                        val bitsPerSample = (((packed ushr 36) and 0x1FL) + 1).toInt()
                        return bitsPerSample
                    }
                }
                16
            }
        } catch (_: Exception) {
            16
        }
    }

    override fun read(buffer: ByteArray): Int {
        softwareFallback?.let { return it.read(buffer) }
        var written = 0
        while (written < buffer.size) {
            if (pendingChunks.isEmpty()) {
                if (isEndOfDecoder) break
                val progressed = decodeMore()
                if (pendingChunks.isEmpty() && isEndOfDecoder) break
                if (pendingChunks.isEmpty()) {
                    if (!progressed) {
                        try {
                            Thread.sleep(5)
                        } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                    continue
                }
            }
            val head = pendingChunks.first()
            val available = head.size - pendingOffset
            val toCopy = minOf(available, buffer.size - written)
            System.arraycopy(head, pendingOffset, buffer, written, toCopy)
            written += toCopy
            pendingOffset += toCopy
            if (pendingOffset >= head.size) {
                pendingChunks.removeFirst()
                pendingOffset = 0
            }
        }
        return if (written == 0 && isEndOfDecoder) -1 else written
    }

    override fun seek(positionMs: Long) {
        softwareFallback?.let {
            it.seek(positionMs)
            return
        }
        val mc = codec ?: return
        val ex = extractor ?: return
        // Jump the demuxer to the nearest sync point at/before the target, then
        // flush the codec — it may hold buffered state tied to the old stream
        // position, and MediaCodec.flush() is the documented way to discard
        // that before feeding it data from a new position.
        ex.seekTo(positionMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        mc.flush()
        pendingChunks.clear()
        pendingOffset = 0
        isEndOfStream = false
        isEndOfDecoder = false
        // capturedOutputFormat is deliberately left as-is — the format doesn't
        // change from a seek, only the read position does.
    }

    override fun close() {
        isClosed = true
        try { codec?.stop() } catch (_: Exception) { /* already stopped */ }
        codec?.release()
        extractor?.release()
        codec = null
        extractor = null
        softwareFallback?.close()
        softwareFallback = null
        pendingChunks.clear()
        pendingOffset = 0
        isEndOfStream = false
        isEndOfDecoder = false
        capturedOutputFormat = null
    }

    /**
     * Peeks the first 10 bytes for an "ID3" marker and, if present, returns the
     * total byte length of that tag (header + declared size + optional footer)
     * so [open] can hand MediaExtractor an offset that starts right at "fLaC".
     * Returns 0 if there's no ID3v2 tag, or if anything about reading it fails
     * (in which case [open] just proceeds with the file as-is, same as before).
     */
    private fun detectLeadingId3v2TagSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val header = ByteArray(10)
                var off = 0
                while (off < header.size) {
                    val n = stream.read(header, off, header.size - off)
                    if (n == -1) break
                    off += n
                }
                if (off == 10 && header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()) {
                    // Size field is 4 "syncsafe" bytes (7 usable bits each, MSB always 0).
                    val declaredSize = ((header[6].toInt() and 0x7F) shl 21) or
                        ((header[7].toInt() and 0x7F) shl 14) or
                        ((header[8].toInt() and 0x7F) shl 7) or
                        (header[9].toInt() and 0x7F)
                    val hasFooter = (header[5].toInt() and 0x10) != 0 // ID3v2.4 footer flag
                    10L + declaredSize + if (hasFooter) 10L else 0L
                } else {
                    0L
                }
            } ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    /** Feeds one input unit and/or drains one output buffer. Returns true if any progress was made. */
    private fun decodeMore(): Boolean {
        if (isClosed) {
            isEndOfDecoder = true
            return false
        }
        val mc = codec ?: return false
        val ex = extractor ?: return false
        var progressed = false

        try {
            if (!isEndOfStream) {
                val inputIndex = mc.dequeueInputBuffer(10_000)
                if (inputIndex >= 0) {
                    val inputBuffer = requireNotNull(mc.getInputBuffer(inputIndex))
                    val sampleSize = ex.readSampleData(inputBuffer, 0)
                    if (sampleSize < 0) {
                        mc.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isEndOfStream = true
                    } else {
                        val pts = maxOf(0L, ex.sampleTime)
                        mc.queueInputBuffer(inputIndex, 0, sampleSize, pts, 0)
                        ex.advance()
                    }
                    progressed = true
                }
            }

            val bufferInfo = MediaCodec.BufferInfo()
            when (val outputIndex = mc.dequeueOutputBuffer(bufferInfo, 10_000)) {
                MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    capturedOutputFormat = mc.outputFormat
                    progressed = true
                }
                MediaCodec.INFO_TRY_AGAIN_LATER -> { /* nothing ready yet */ }
                else -> if (outputIndex >= 0) {
                    if (bufferInfo.size > 0) {
                        val outBuf = requireNotNull(mc.getOutputBuffer(outputIndex))
                        val chunk = ByteArray(bufferInfo.size)
                        outBuf.position(bufferInfo.offset)
                        outBuf.limit(bufferInfo.offset + bufferInfo.size)
                        outBuf.get(chunk)
                        pendingChunks.addLast(chunk)
                    }
                    mc.releaseOutputBuffer(outputIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isEndOfDecoder = true
                    }
                    progressed = true
                }
            }
        } catch (e: IllegalStateException) {
            if (isClosed) {
                // close() ran on another thread while this call was in flight —
                // a benign teardown race, not a real failure. Treat as EOF.
                isEndOfDecoder = true
                return false
            }
            // isClosed is still false, so nobody asked to stop — this is a genuine,
            // unexpected codec failure (e.g. a vendor decoder bug/crash mid-stream).
            // This must NOT be silently treated as EOF: doing that makes the engine
            // think the track finished normally and fire onCompletion(), which was
            // exactly what caused this to race PlaybackService's startup and crash
            // the app with ForegroundServiceDidNotStartInTimeException — on top of
            // just being wrong (a codec crash isn't "the song is over"). Surface it
            // as a real error instead, so it reaches the person as an actual message.
            throw IllegalStateException("FLAC decoder failed: ${e.message}", e)
        }
        return progressed
    }

    /**
     * Every non-encoder codec registered on the device that declares support
     * for [mime]. Returns `[null]` (meaning "just use createDecoderByType") if
     * enumeration fails or turns up nothing, so behavior degrades to exactly
     * what this code did before if a device doesn't cooperate with listing.
     */
    private fun buildFlacDecoderCandidates(mime: String): List<String?> {
        val names = try {
            MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
                .asSequence()
                .filter { !it.isEncoder }
                .filter { info -> info.supportedTypes.any { it.equals(mime, ignoreCase = true) } }
                .map { it.name }
                .distinct()
                .toList()
        } catch (e: Exception) {
            emptyList()
        }
        return names.ifEmpty { listOf(null) }
    }

    /** Fresh InputStream over the same file the MediaCodec attempts used,
     *  positioned past the same leading ID3v2 tag (if any) detected earlier —
     *  jflac needs a raw stream rather than the ParcelFileDescriptor/offset
     *  approach used for MediaExtractor. */
    private fun openStreamSkippingId3(uri: Uri, id3TagSizeBytes: Long): InputStream {
        val stream = requireNotNull(context.contentResolver.openInputStream(uri)) { "Could not open $uri" }
        var remaining = id3TagSizeBytes
        while (remaining > 0) {
            val skipped = stream.skip(remaining)
            if (skipped <= 0) break
            remaining -= skipped
        }
        return stream
    }

    companion object {
        private const val TAG = "FlacDecoder"
    }
}

/**
 * Pure-JVM FLAC decode path using a vendored copy of JustFLAC
 * (org.kc7bfi.jflac — see app/src/main/java/org/kc7bfi/jflac/README-VENDORED.md
 * for why it's vendored rather than a Maven dependency), used only when every
 * MediaCodec candidate for audio/flac has failed on this device (see the
 * candidate loop in [FlacDecoder.open]). This decodes the FLAC bitstream
 * directly in ordinary JVM bytecode — no MediaCodec, no Codec2, no vendor
 * component of any kind — so it's unaffected by whatever's wrong with the
 * device's own decoder. It's software-only (no hardware acceleration), but
 * for a "bit-perfect" app, a slower decode that's actually correct on every
 * device is the right tradeoff over a fast one that silently fails on some.
 *
 * Packs PCM bytes directly from getChannelData() rather than calling the
 * library's own decodeFrame() — this fork's decodeFrame() is correct (it
 * fixed the bug where the Maven org.jflac fork left its internal `channels`
 * field at 0 forever), but packing directly here avoids depending on that
 * method's own bit-depth branching and keeps one single, already-verified
 * code path for every bit depth (8/16/24/32) instead of two.
 */
private class JflacPcmSource(private val streamFactory: () -> InputStream) {
    private var inputStream: InputStream = streamFactory()
    private var decoder = FLACDecoder(inputStream)
    private var bitsPerSample = 16
    private var sampleRateHz = 44100
    private var channelsCount = 2
    private val pending = ArrayDeque<ByteArray>()
    private var pendingOffset = 0
    private var eof = false
    /** Running count of samples decoded so far — lets [seek] tell whether the
     *  target is ahead (decode-and-discard from here) or behind (must reopen
     *  from the start, since a content:// stream generally can't rewind). */
    private var currentSample = 0L

    companion object {
        private const val TAG = "JflacPcmSource"
    }

    fun open(): PcmFormat {
        decoder.readMetadata()
        val info = requireNotNull(decoder.streamInfo) { "No StreamInfo block found in FLAC stream" }
        bitsPerSample = info.bitsPerSample
        sampleRateHz = info.sampleRate
        channelsCount = info.channels
        val fmt = PcmFormat(info.sampleRate, info.bitsPerSample, info.channels)
        Log.i(TAG, "jflac opened: ${fmt.sampleRateHz}Hz / ${fmt.bitDepth}-bit / ${fmt.channels}ch" +
            if (fmt.bitDepth > 16) " — HIGH-RESOLUTION source confirmed" else "")
        return fmt
    }

    fun read(buffer: ByteArray): Int {
        val bytesPerSample = when {
            bitsPerSample <= 8 -> 1
            bitsPerSample <= 16 -> 2
            bitsPerSample <= 24 -> 3
            else -> 4
        }
        val frameSize = (channelsCount * bytesPerSample).coerceAtLeast(1)

        val maxTargetBytes = (buffer.size / frameSize) * frameSize

        var written = 0
        while (written < maxTargetBytes) {
            if (pending.isEmpty()) {
                if (eof) break
                val frame = nextFrame() ?: break
                pending.addLast(packFrame(frame))
                pendingOffset = 0
            }
            val head = pending.first()
            val available = head.size - pendingOffset
            val remainingTarget = maxTargetBytes - written
            val rawToCopy = minOf(available, remainingTarget)
            val toCopy = if (rawToCopy < available) (rawToCopy / frameSize) * frameSize else rawToCopy
            if (toCopy <= 0) break

            System.arraycopy(head, pendingOffset, buffer, written, toCopy)
            written += toCopy
            pendingOffset += toCopy
            if (pendingOffset >= head.size) {
                pending.removeFirst()
                pendingOffset = 0
            }
        }
        return if (written == 0 && eof) -1 else written
    }


    /**
     * No native seek table use here — this is a straightforward, always-correct
     * fallback: forward seeks decode-and-discard frames until reaching the
     * target sample; backward seeks reopen the stream from scratch (via
     * [streamFactory]) and do the same from position 0. Slower than an
     * index-based seek, especially for a large backward jump, but this only
     * runs on the rare devices where the platform's own FLAC decoder is
     * already broken — correctness matters more than seek speed there.
     */
    fun seek(positionMs: Long) {
        val targetSample = (positionMs / 1000.0 * sampleRateHz).toLong().coerceAtLeast(0L)
        if (targetSample < currentSample) {
            try { inputStream.close() } catch (_: Exception) { /* best-effort */ }
            inputStream = streamFactory()
            decoder = FLACDecoder(inputStream)
            pending.clear()
            pendingOffset = 0
            eof = false
            currentSample = 0L
            decoder.readMetadata()
        } else {
            pending.clear()
            pendingOffset = 0
        }
        while (currentSample < targetSample && !eof) {
            nextFrame() ?: break
        }
    }

    /** Reads the next frame and advances [currentSample] by its block size —
     *  the single place both [read] and [seek]'s discard loop pull frames from,
     *  so the position counter can never drift out of sync with the decoder. */
    private fun nextFrame(): Frame? {
        val frame = decoder.readNextFrame()
        if (frame == null) {
            eof = true
            return null
        }
        currentSample += frame.header.blockSize
        return frame
    }

    /** Interleaves and byte-packs this frame's already-decoded samples.
     *  Supports 8/16/24/32-bit (FLAC's full spec range is 4-32 bits; anything
     *  odd/unusual below 8 is vanishingly rare in practice and falls through
     *  to the 32-bit packing, which is safe — just not maximally compact). */
    private fun packFrame(frame: Frame): ByteArray {
        val blockSize = frame.header.blockSize
        val channels = frame.header.channels
        val channelData = decoder.channelData
        val bytesPerSample = when {
            bitsPerSample <= 8 -> 1
            bitsPerSample <= 16 -> 2
            bitsPerSample <= 24 -> 3
            else -> 4
        }
        val out = ByteArray(blockSize * channels * bytesPerSample)
        var idx = 0
        for (i in 0 until blockSize) {
            for (ch in 0 until channels) {
                val sample = channelData[ch].output[i]
                when (bytesPerSample) {
                    1 -> {
                        // 8-bit PCM is conventionally unsigned (0..255); FLAC
                        // subframes decode to signed, so offset like WAV does.
                        out[idx++] = (sample + 0x80).toByte()
                    }
                    2 -> {
                        out[idx++] = (sample and 0xFF).toByte()
                        out[idx++] = ((sample shr 8) and 0xFF).toByte()
                    }
                    3 -> {
                        out[idx++] = (sample and 0xFF).toByte()
                        out[idx++] = ((sample shr 8) and 0xFF).toByte()
                        out[idx++] = ((sample shr 16) and 0xFF).toByte()
                    }
                    else -> {
                        out[idx++] = (sample and 0xFF).toByte()
                        out[idx++] = ((sample shr 8) and 0xFF).toByte()
                        out[idx++] = ((sample shr 16) and 0xFF).toByte()
                        out[idx++] = ((sample shr 24) and 0xFF).toByte()
                    }
                }
            }
        }
        return out
    }

    fun close() {
        try { inputStream.close() } catch (_: Exception) { /* best-effort */ }
    }
}
