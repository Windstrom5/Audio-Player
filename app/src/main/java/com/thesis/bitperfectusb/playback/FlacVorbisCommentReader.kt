package com.thesis.bitperfectusb.playback

import android.content.Context
import android.net.Uri
import java.io.DataInputStream

/**
 * Reads REPLAYGAIN_TRACK_GAIN / REPLAYGAIN_TRACK_PEAK from a FLAC file's
 * Vorbis comment metadata block — the actual data source behind the
 * ReplayGain and Normalization DSP toggles. Real ReplayGain is a tag some
 * other tool embedded when it encoded or tagged the file (foobar2000,
 * mp3gain-family tools, etc.); this app doesn't compute loudness itself,
 * there's no full-track pre-scan here, only reading what the file already
 * declares. Returns null for either value when the tag isn't present — the
 * honest, expected outcome for most files, not something to work around.
 *
 * FLAC only. WAV has no standardized ReplayGain tagging convention in
 * practice, so [WavDecoder]'s files are never checked here.
 */
class FlacVorbisCommentReader(private val context: Context) {

    data class ReplayGainTags(val trackGainDb: Float?, val trackPeak: Float?)

    fun read(uriString: String): ReplayGainTags {
        return try {
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use { raw ->
                readInternal(DataInputStream(raw.buffered()))
            } ?: NONE
        } catch (e: Exception) {
            // Malformed/unreadable file, or genuinely not FLAC — treated the same as
            // "no tag found": the toggles just have no effect, not a playback failure.
            NONE
        }
    }

    private fun readInternal(input: DataInputStream): ReplayGainTags {
        val magic = ByteArray(4)
        input.readFully(magic)
        if (magic[0] != 'f'.code.toByte() || magic[1] != 'L'.code.toByte() ||
            magic[2] != 'a'.code.toByte() || magic[3] != 'C'.code.toByte()
        ) {
            return NONE
        }

        while (true) {
            val header = input.readUnsignedByte()
            val isLast = (header and 0x80) != 0
            val blockType = header and 0x7F
            val length = (input.readUnsignedByte() shl 16) or
                (input.readUnsignedByte() shl 8) or
                input.readUnsignedByte()

            if (blockType == VORBIS_COMMENT_BLOCK_TYPE) {
                val blockData = ByteArray(length)
                input.readFully(blockData)
                return parseVorbisComment(blockData)
            }
            skipFully(input, length.toLong())
            if (isLast) break
        }
        return NONE
    }

    private fun parseVorbisComment(data: ByteArray): ReplayGainTags {
        if (data.size < 8) return NONE
        var offset = 0

        fun readLeU32(): Int {
            val v = (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                ((data[offset + 2].toInt() and 0xFF) shl 16) or
                ((data[offset + 3].toInt() and 0xFF) shl 24)
            offset += 4
            return v
        }

        val vendorLength = readLeU32()
        if (vendorLength < 0 || offset + vendorLength + 4 > data.size) return NONE
        offset += vendorLength
        val commentCount = readLeU32()
        if (commentCount < 0) return NONE

        var trackGainDb: Float? = null
        var trackPeak: Float? = null

        for (i in 0 until commentCount) {
            if (offset + 4 > data.size) break
            val commentLength = readLeU32()
            if (commentLength < 0 || offset + commentLength > data.size) break
            val comment = String(data, offset, commentLength, Charsets.UTF_8)
            offset += commentLength

            val eq = comment.indexOf('=')
            if (eq <= 0) continue
            val key = comment.substring(0, eq).uppercase()
            val value = comment.substring(eq + 1).trim()

            when (key) {
                // Standard tag format is e.g. "-6.20 dB" — strip a trailing unit if present.
                "REPLAYGAIN_TRACK_GAIN" -> trackGainDb = value.removeSuffix("dB").removeSuffix("DB").trim().toFloatOrNull()
                "REPLAYGAIN_TRACK_PEAK" -> trackPeak = value.toFloatOrNull()
            }
        }

        return ReplayGainTags(trackGainDb, trackPeak)
    }

    private fun skipFully(input: DataInputStream, bytes: Long) {
        var remaining = bytes
        while (remaining > 0) {
            val skipped = input.skip(remaining)
            if (skipped <= 0) break
            remaining -= skipped
        }
    }

    companion object {
        private const val VORBIS_COMMENT_BLOCK_TYPE = 4
        private val NONE = ReplayGainTags(null, null)
    }
}
