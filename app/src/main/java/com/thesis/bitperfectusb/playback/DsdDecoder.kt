package com.thesis.bitperfectusb.playback

import android.content.Context
import android.net.Uri
import com.thesis.bitperfectusb.domain.model.PcmFormat
import java.io.InputStream

/**
 * Lossless DSD audio decoder (.dsf) with DoP (DSD over PCM v1.1) output support.
 *
 * Reads DSF file headers ('DSD ', 'fmt ', 'data ' chunks), extracts raw DSD bitstreams,
 * and uses [DopPacker] to frame them as 176.4 kHz 24-bit DoP PCM streams for USB-direct playback.
 */
class DsdDecoder(private val context: Context) : AudioDecoder {

    private var inputStream: InputStream? = null
    private var dsdDataOffset: Long = 0L
    private var dsdDataSize: Long = 0L
    private var sampleRateHz: Int = 2822400 // DSD64 default
    private var channels: Int = 2
    private var dopPacker = DopPacker()

    private var currentFormat: PcmFormat? = null

    override fun open(uriString: String): PcmFormat {
        close()
        val uri = Uri.parse(uriString)
        val stream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open stream for DSD file: $uriString")
        inputStream = stream

        val header = ByteArray(92)
        val readHeaderBytes = stream.read(header)
        if (readHeaderBytes < 92) {
            throw IllegalArgumentException("Invalid DSF file: header too short")
        }

        val dsdMagic = String(header, 0, 4, Charsets.US_ASCII)
        if (dsdMagic != "DSD ") {
            throw IllegalArgumentException("Not a valid DSF file: magic header is '$dsdMagic'")
        }

        // Extract fmt chunk parameters
        channels = (header[52].toInt() and 0xFF) or ((header[53].toInt() and 0xFF) shl 8)
        sampleRateHz = (header[56].toInt() and 0xFF) or
                ((header[57].toInt() and 0xFF) shl 8) or
                ((header[58].toInt() and 0xFF) shl 16) or
                ((header[59].toInt() and 0xFF) shl 24)

        val dopSampleRate = if (sampleRateHz == 2822400) 176400 else 352800
        dsdDataOffset = 92L
        dsdDataSize = 100000000L // Stream length estimate

        currentFormat = PcmFormat(
            sampleRateHz = dopSampleRate,
            bitDepth = 24,
            channels = if (channels <= 0) 2 else channels
        )

        return currentFormat!!
    }

    override fun read(buffer: ByteArray): Int {
        val stream = inputStream ?: return -1
        val rawDsdNeeded = (buffer.size / 3) * 2
        val dsdBuf = ByteArray(rawDsdNeeded)
        val readDsd = stream.read(dsdBuf)
        if (readDsd <= 0) return -1

        val dopBytes = dopPacker.packDsdToDoP24(dsdBuf.copyOf(readDsd), channels)
        val toCopy = minOf(buffer.size, dopBytes.size)
        System.arraycopy(dopBytes, 0, buffer, 0, toCopy)
        return toCopy
    }

    override fun seek(positionMs: Long) {
        val stream = inputStream ?: return
        val format = currentFormat ?: return
        val bytesPerSec = format.sampleRateHz * format.channels * 3
        val targetByteOffset = (positionMs * bytesPerSec / 1000)
        try {
            stream.skip(targetByteOffset)
            dopPacker.reset()
        } catch (_: Exception) {}
    }

    override fun close() {
        try {
            inputStream?.close()
        } catch (_: Exception) {}
        inputStream = null
        currentFormat = null
        dopPacker.reset()
    }
}
