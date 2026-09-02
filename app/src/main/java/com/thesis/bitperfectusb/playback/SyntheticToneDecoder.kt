package com.thesis.bitperfectusb.playback

import com.thesis.bitperfectusb.domain.model.PcmFormat
import kotlin.math.PI
import kotlin.math.sin

/**
 * Generates a continuous 440Hz sine-wave test tone at an exact, controllable
 * PCM format and never signals end-of-stream. ExperimentOrchestrator
 * (Section 3.8) uses this so Experiments B, C, and D can sweep sample rate,
 * hardware, or buffer size while holding the source signal itself constant —
 * the controlled-variable design the thesis specifies — independent of
 * whatever the user happens to have in their local library.
 */
class SyntheticToneDecoder : AudioDecoder {

    private lateinit var format: PcmFormat
    private var samplePosition: Long = 0
    private val toneFrequencyHz = 440.0
    private val amplitude = 0.5 // -6dBFS headroom, avoids clipping at the integer boundary

    /** uriString is expected in the form "synthetic://<sampleRateHz>/<bitDepth>/<channels>". */
    override fun open(uriString: String): PcmFormat {
        val parts = uriString.removePrefix("synthetic://").split("/")
        format = PcmFormat(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        samplePosition = 0
        return format
    }

    override fun read(buffer: ByteArray): Int {
        val bytesPerSample = format.bitDepth / 8
        val frameSize = bytesPerSample * format.channels
        val frameCount = buffer.size / frameSize
        var offset = 0

        repeat(frameCount) {
            val t = samplePosition.toDouble() / format.sampleRateHz
            val sampleValue = sin(2.0 * PI * toneFrequencyHz * t) * amplitude
            val intValue = when (format.bitDepth) {
                16 -> (sampleValue * Short.MAX_VALUE).toInt()
                24 -> (sampleValue * 8_388_607).toInt() // 2^23 - 1
                32 -> (sampleValue * Int.MAX_VALUE).toInt()
                else -> (sampleValue * Short.MAX_VALUE).toInt()
            }
            for (ch in 0 until format.channels) {
                writeLittleEndianSample(buffer, offset, intValue, bytesPerSample)
                offset += bytesPerSample
            }
            samplePosition++
        }
        return offset
    }

    private fun writeLittleEndianSample(buffer: ByteArray, offset: Int, value: Int, bytesPerSample: Int) {
        for (b in 0 until bytesPerSample) {
            buffer[offset + b] = ((value shr (8 * b)) and 0xFF).toByte()
        }
    }

    override fun close() { /* nothing to release — purely synthetic */ }

    override fun seek(positionMs: Long) {
        samplePosition = (positionMs / 1000.0 * format.sampleRateHz).toLong().coerceAtLeast(0L)
    }
}
