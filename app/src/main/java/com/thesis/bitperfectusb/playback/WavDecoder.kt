package com.thesis.bitperfectusb.playback

import android.content.Context
import android.net.Uri
import com.thesis.bitperfectusb.domain.model.PcmFormat
import java.io.InputStream

/**
 * WAV is already raw, uncompressed PCM — "decoding" is just locating the `data`
 * chunk and streaming its bytes through completely unmodified. This is the most
 * direct possible bit-perfect path from file to DAC (Section 3.4, WavDecoder).
 */
class WavDecoder(private val context: Context) : AudioDecoder {

    private var inputStream: InputStream? = null
    private var bytesRemainingInDataChunk: Long = 0

    // Retained from open() so seek() can reopen and relocate the data chunk
    // without needing the caller to call open() again first.
    private var uri: Uri? = null
    private var dataChunkSizeBytes: Long = 0
    private var bytesPerFrame: Int = 0
    private var sampleRateForSeek: Int = 0

    override fun open(uriString: String): PcmFormat {
        val parsedUri = if (uriString.startsWith("/") && !uriString.startsWith("content://") && !uriString.startsWith("file://")) {
            Uri.fromFile(java.io.File(uriString))
        } else {
            Uri.parse(uriString)
        }
        uri = parsedUri
        val format = openAndLocateDataChunk(parsedUri)
        return format
    }

    /** Parses the RIFF header and chunk list, leaving [inputStream] positioned
     *  at the start of PCM data and [bytesRemainingInDataChunk] / [dataChunkSizeBytes]
     *  set to the data chunk's full size. Used by both [open] and [seek]. */
    private fun openAndLocateDataChunk(target: Uri): PcmFormat {
        val stream = if (target.scheme == "file") {
            val file = java.io.File(target.path ?: target.schemeSpecificPart)
            if (file.exists()) java.io.FileInputStream(file)
            else context.contentResolver.openInputStream(target) ?: error("Unable to open $target")
        } else {
            try {
                context.contentResolver.openInputStream(target)
            } catch (_: Exception) {
                if (target.path != null && java.io.File(target.path!!).exists()) {
                    java.io.FileInputStream(java.io.File(target.path!!))
                } else null
            } ?: error("Unable to open $target")
        }
        inputStream = stream

        val riffHeader = ByteArray(12)
        readFully(stream, riffHeader)
        require(String(riffHeader, 0, 4, Charsets.US_ASCII) == "RIFF") { "Not a RIFF/WAV file" }

        var sampleRate = 0
        var bitDepth = 0
        var channels = 0
        val chunkHeader = ByteArray(8)
        while (readFully(stream, chunkHeader) == 8) {
            val chunkId = String(chunkHeader, 0, 4, Charsets.US_ASCII)
            val chunkSize = leInt(chunkHeader, 4)
            when (chunkId) {
                "fmt " -> {
                    val fmt = ByteArray(chunkSize)
                    readFully(stream, fmt)
                    channels = leShort(fmt, 2)
                    sampleRate = leInt(fmt, 4)
                    bitDepth = leShort(fmt, 14)
                }
                "data" -> {
                    val size = chunkSize.toLong() and 0xFFFFFFFFL
                    bytesRemainingInDataChunk = size
                    dataChunkSizeBytes = size
                    sampleRateForSeek = sampleRate
                    bytesPerFrame = (bitDepth / 8) * channels
                    return PcmFormat(sampleRate, bitDepth, channels).also {
                        require(sampleRate > 0 && bitDepth > 0 && channels > 0) { "Malformed WAV header in $target" }
                    }
                }
                else -> skipFully(stream, chunkSize.toLong() and 0xFFFFFFFFL)
            }
        }
        error("No data chunk found in $target")
    }

    override fun read(buffer: ByteArray): Int {
        if (bytesRemainingInDataChunk <= 0) return -1
        val stream = inputStream ?: return -1
        var totalRead = 0
        while (totalRead < buffer.size && bytesRemainingInDataChunk > 0) {
            val toRead = minOf((buffer.size - totalRead).toLong(), bytesRemainingInDataChunk).toInt()
            val n = stream.read(buffer, totalRead, toRead)
            if (n <= 0) break
            totalRead += n
            bytesRemainingInDataChunk -= n
        }
        return if (totalRead == 0) -1 else totalRead
    }

    /**
     * WAV's underlying stream (from contentResolver.openInputStream) generally
     * can't seek backward, so this always reopens fresh and re-locates the data
     * chunk, then skips forward to the target byte offset — simple and correct,
     * at the cost of re-parsing a small header on every seek (cheap relative to
     * the actual data skip).
     */
    override fun seek(positionMs: Long) {
        val target = uri ?: return
        try { inputStream?.close() } catch (_: Exception) { /* best-effort */ }
        openAndLocateDataChunk(target)

        if (bytesPerFrame <= 0 || sampleRateForSeek <= 0) return
        val targetFrame = (positionMs / 1000.0 * sampleRateForSeek).toLong().coerceAtLeast(0L)
        var targetByteOffset = targetFrame * bytesPerFrame
        targetByteOffset = targetByteOffset.coerceIn(0L, dataChunkSizeBytes)

        val stream = inputStream ?: return
        skipFully(stream, targetByteOffset)
        bytesRemainingInDataChunk = dataChunkSizeBytes - targetByteOffset
    }

    override fun close() {
        inputStream?.close()
        inputStream = null
    }

    private fun readFully(input: InputStream, buf: ByteArray): Int {
        var off = 0
        while (off < buf.size) {
            val n = input.read(buf, off, buf.size - off)
            if (n == -1) break
            off += n
        }
        return off
    }

    private fun skipFully(input: InputStream, count: Long) {
        var remaining = count
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped <= 0) {
                if (input.read() == -1) return
                remaining -= 1
            } else remaining -= skipped
        }
    }

    private fun leShort(b: ByteArray, offset: Int): Int =
        (b[offset].toInt() and 0xFF) or ((b[offset + 1].toInt() and 0xFF) shl 8)

    private fun leInt(b: ByteArray, offset: Int): Int =
        (b[offset].toInt() and 0xFF) or
            ((b[offset + 1].toInt() and 0xFF) shl 8) or
            ((b[offset + 2].toInt() and 0xFF) shl 16) or
            ((b[offset + 3].toInt() and 0xFF) shl 24)
}
