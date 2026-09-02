package com.thesis.bitperfectusb.data.scanner

import android.content.ContentResolver
import android.net.Uri
import com.thesis.bitperfectusb.domain.model.AudioFileFormat
import com.thesis.bitperfectusb.domain.model.PcmFormat
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Parses raw file headers to read the *native* sample rate, bit depth, and channel
 * configuration of a lossless source file (Section 3.2, MetadataExtractor). This is a
 * hand-rolled binary parser rather than a call into MediaExtractor, because
 * MediaExtractor's reported format can itself be normalized by the platform — which
 * would defeat the purpose of an integrity-verification pipeline built to catch
 * exactly that kind of silent reformatting.
 */
class MetadataExtractor(private val contentResolver: ContentResolver) {

    data class ExtractedMetadata(
        val format: AudioFileFormat,
        val pcm: PcmFormat,
        val durationMs: Long
    )

    fun extract(uri: Uri): ExtractedMetadata? = try {
        contentResolver.openInputStream(uri)?.use { raw ->
            val input = BufferedInputStream(raw, 64 * 1024)
            skipLeadingId3v2Tag(input)
            input.mark(4)
            val magic = ByteArray(4)
            val read = readFully(input, magic)
            input.reset()
            when {
                read == 4 && String(magic, Charsets.US_ASCII) == "RIFF" -> parseWav(input)
                read == 4 && String(magic, Charsets.US_ASCII) == "fLaC" -> parseFlac(input)
                else -> null
            }
        }
    } catch (e: IOException) {
        null
    }

    /**
     * Some FLAC (and occasionally WAV) files in the wild have an ID3v2 tag
     * prepended before the real container magic ("fLaC" / "RIFF"), even though
     * that's non-compliant. Without skipping it, every check below sees the
     * "ID3" bytes instead and the file gets silently dropped from the library
     * via the `?: continue` in LocalAudioScanner — indistinguishable from an
     * actually corrupt file. This leaves the stream positioned right at the
     * real container magic either way.
     */
    private fun skipLeadingId3v2Tag(input: BufferedInputStream) {
        input.mark(10)
        val header = ByteArray(10)
        val read = readFully(input, header)
        if (read == 10 && header[0] == 'I'.code.toByte() && header[1] == 'D'.code.toByte() && header[2] == '3'.code.toByte()) {
            val declaredSize = ((header[6].toInt() and 0x7F) shl 21) or
                ((header[7].toInt() and 0x7F) shl 14) or
                ((header[8].toInt() and 0x7F) shl 7) or
                (header[9].toInt() and 0x7F)
            val hasFooter = (header[5].toInt() and 0x10) != 0 // ID3v2.4 footer flag
            skipFully(input, declaredSize.toLong() + if (hasFooter) 10L else 0L)
        } else {
            input.reset()
        }
    }

    // -------------------------------------------------------------------
    // WAV (RIFF/PCM) — all multi-byte fields are little-endian
    // -------------------------------------------------------------------

    private fun parseWav(input: InputStream): ExtractedMetadata? {
        val riffHeader = ByteArray(12)
        if (readFully(input, riffHeader) != 12) return null
        if (String(riffHeader, 8, 4, Charsets.US_ASCII) != "WAVE") return null

        var sampleRate = 0
        var bitDepth = 0
        var channels = 0
        var byteRate = 0
        var dataSize = 0L

        val chunkHeader = ByteArray(8)
        while (readFully(input, chunkHeader) == 8) {
            val chunkId = String(chunkHeader, 0, 4, Charsets.US_ASCII)
            val chunkSize = leInt(chunkHeader, 4)
            when (chunkId) {
                "fmt " -> {
                    val fmt = ByteArray(chunkSize)
                    readFully(input, fmt)
                    channels = leShort(fmt, 2)
                    sampleRate = leInt(fmt, 4)
                    byteRate = leInt(fmt, 8)
                    bitDepth = leShort(fmt, 14)
                }
                "data" -> {
                    dataSize = chunkSize.toLong() and 0xFFFFFFFFL
                    break // fmt is required to appear before data in a valid WAV; we're done
                }
                else -> skipFully(input, chunkSize.toLong() and 0xFFFFFFFFL)
            }
        }

        if (sampleRate <= 0 || bitDepth <= 0 || channels <= 0) return null
        val durationMs = if (byteRate > 0) (dataSize * 1000) / byteRate else 0L
        return ExtractedMetadata(AudioFileFormat.WAV, PcmFormat(sampleRate, bitDepth, channels), durationMs)
    }

    // -------------------------------------------------------------------
    // FLAC — STREAMINFO metadata block, big-endian bit-packed fields
    // -------------------------------------------------------------------

    private fun parseFlac(input: InputStream): ExtractedMetadata? {
        val magic = ByteArray(4)
        if (readFully(input, magic) != 4) return null

        while (true) {
            val blockHeader = ByteArray(4)
            if (readFully(input, blockHeader) != 4) return null
            val isLast = (blockHeader[0].toInt() and 0x80) != 0
            val blockType = blockHeader[0].toInt() and 0x7F
            val length = ((blockHeader[1].toInt() and 0xFF) shl 16) or
                ((blockHeader[2].toInt() and 0xFF) shl 8) or
                (blockHeader[3].toInt() and 0xFF)

            if (blockType == 0) { // STREAMINFO — required to be the first metadata block
                val block = ByteArray(length)
                if (readFully(input, block) != length) return null
                return parseStreamInfo(block)
            }
            skipFully(input, length.toLong())
            if (isLast) return null // malformed: no STREAMINFO found before the last block
        }
    }

    private fun parseStreamInfo(block: ByteArray): ExtractedMetadata {
        // Bytes 10..17: 20-bit sample rate | 3-bit (channels-1) | 5-bit (bitsPerSample-1) | 36-bit total samples
        var packed = 0L
        for (i in 10..17) {
            packed = (packed shl 8) or (block[i].toLong() and 0xFF)
        }
        val sampleRate = ((packed ushr 44) and 0xFFFFFL).toInt()
        val channels = (((packed ushr 41) and 0x7L) + 1).toInt()
        val bitsPerSample = (((packed ushr 36) and 0x1FL) + 1).toInt()
        val totalSamples = packed and ((1L shl 36) - 1)
        val durationMs = if (sampleRate > 0) (totalSamples * 1000) / sampleRate else 0L
        return ExtractedMetadata(AudioFileFormat.FLAC, PcmFormat(sampleRate, bitsPerSample, channels), durationMs)
    }

    // -------------------------------------------------------------------
    // Byte-level helpers
    // -------------------------------------------------------------------

    private fun readFully(input: InputStream, buffer: ByteArray): Int {
        var offset = 0
        while (offset < buffer.size) {
            val n = input.read(buffer, offset, buffer.size - offset)
            if (n == -1) break
            offset += n
        }
        return offset
    }

    private fun skipFully(input: InputStream, count: Long) {
        var remaining = count
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped <= 0) {
                if (input.read() == -1) return
                remaining -= 1
            } else {
                remaining -= skipped
            }
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