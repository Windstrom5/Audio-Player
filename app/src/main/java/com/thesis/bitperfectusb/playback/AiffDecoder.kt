package com.thesis.bitperfectusb.playback

import android.content.Context
import android.net.Uri
import com.thesis.bitperfectusb.domain.model.PcmFormat
import java.io.InputStream

/**
 * Lossless AIFF (Audio Interchange File Format) decoder.
 * Parses FORM, COMM, and SSND chunks, converting big-endian uncompressed PCM bytes
 * into native little-endian PCM for BitPerfectUSB playback.
 */
class AiffDecoder(private val context: Context) : AudioDecoder {

    private var inputStream: InputStream? = null
    private var dataSize: Int = 0
    private var bytesReadTotal: Int = 0
    private var bytesPerSample: Int = 2
    private var channels: Int = 2

    private var currentFormat: PcmFormat? = null

    override fun open(uriString: String): PcmFormat {
        close()
        val uri = Uri.parse(uriString)
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open stream for AIFF: $uriString")
        inputStream = stream

        val header = ByteArray(12)
        if (stream.read(header) < 12) throw IllegalArgumentException("Invalid AIFF header")

        val formMagic = String(header, 0, 4, Charsets.US_ASCII)
        val aiffMagic = String(header, 8, 4, Charsets.US_ASCII)
        if (formMagic != "FORM" || (aiffMagic != "AIFF" && aiffMagic != "AIFC")) {
            throw IllegalArgumentException("Not a valid AIFF file")
        }

        var sampleRateHz = 44100
        var bitDepth = 16

        // Parse chunks
        val chunkHeader = ByteArray(8)
        while (stream.read(chunkHeader) == 8) {
            val chunkId = String(chunkHeader, 0, 4, Charsets.US_ASCII)
            val chunkSize = (chunkHeader[4].toInt() and 0xFF shl 24) or
                    (chunkHeader[5].toInt() and 0xFF shl 16) or
                    (chunkHeader[6].toInt() and 0xFF shl 8) or
                    (chunkHeader[7].toInt() and 0xFF)

            if (chunkId == "COMM") {
                val commBuf = ByteArray(minOf(chunkSize, 26))
                stream.read(commBuf)
                channels = (commBuf[0].toInt() and 0xFF shl 8) or (commBuf[1].toInt() and 0xFF)
                bitDepth = (commBuf[6].toInt() and 0xFF shl 8) or (commBuf[7].toInt() and 0xFF)
                bytesPerSample = (bitDepth + 7) / 8
                // Sample rate in 80-bit IEEE extended precision (simplified conversion for standard rates)
                val exp = (commBuf[8].toInt() and 0x7F shl 8) or (commBuf[9].toInt() and 0xFF)
                val mant = (commBuf[10].toLong() and 0xFFL shl 56) or
                        (commBuf[11].toLong() and 0xFFL shl 48) or
                        (commBuf[12].toLong() and 0xFFL shl 40) or
                        (commBuf[13].toLong() and 0xFFL shl 32)
                sampleRateHz = if (exp > 0) (mant shr (63 - (exp - 16383))).toInt() else 44100
                if (chunkSize > commBuf.size) stream.skip((chunkSize - commBuf.size).toLong())
            } else if (chunkId == "SSND") {
                val ssndHeader = ByteArray(8)
                stream.read(ssndHeader)
                dataSize = chunkSize - 8
                break
            } else {
                stream.skip(chunkSize.toLong())
            }
        }

        currentFormat = PcmFormat(
            sampleRateHz = if (sampleRateHz <= 0) 44100 else sampleRateHz,
            bitDepth = if (bitDepth <= 0) 16 else bitDepth,
            channels = if (channels <= 0) 2 else channels
        )

        return currentFormat!!
    }

    override fun read(buffer: ByteArray): Int {
        val stream = inputStream ?: return -1
        val toRead = minOf(buffer.size, dataSize - bytesReadTotal)
        if (toRead <= 0) return -1

        val rawBuf = ByteArray(toRead)
        val n = stream.read(rawBuf)
        if (n <= 0) return -1

        // Swap Big-Endian AIFF PCM bytes to Little-Endian
        var i = 0
        while (i + bytesPerSample <= n) {
            if (bytesPerSample == 2) {
                buffer[i] = rawBuf[i + 1]
                buffer[i + 1] = rawBuf[i]
            } else if (bytesPerSample == 3) {
                buffer[i] = rawBuf[i + 2]
                buffer[i + 1] = rawBuf[i + 1]
                buffer[i + 2] = rawBuf[i]
            } else {
                System.arraycopy(rawBuf, i, buffer, i, bytesPerSample)
            }
            i += bytesPerSample
        }

        bytesReadTotal += n
        return n
    }

    override fun seek(positionMs: Long) {
        val stream = inputStream ?: return
        val format = currentFormat ?: return
        val bytesPerSec = format.sampleRateHz * format.channels * bytesPerSample
        val targetByteOffset = (positionMs * bytesPerSec / 1000).toInt()
        try {
            stream.skip(targetByteOffset.toLong())
            bytesReadTotal = targetByteOffset
        } catch (_: Exception) {}
    }

    override fun close() {
        try { inputStream?.close() } catch (_: Exception) {}
        inputStream = null
        bytesReadTotal = 0
        currentFormat = null
    }
}
