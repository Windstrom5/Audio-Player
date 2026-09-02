package com.thesis.bitperfectusb.playback

/**
 * DoP (DSD over PCM v1.1) Marker and Frame Packer.
 *
 * Implements the standard DoP open specification (v1.1):
 * - Takes raw 1-bit DSD streams and frames them into 24-bit PCM containers.
 * - Upper 8 bits alternate between 0x05 and 0xFA marker bytes frame by frame.
 * - Lower 16 bits carry 16 raw DSD audio bits per channel.
 * - For DSD64 (2.8224 MHz), the resulting DoP PCM stream is 176.4 kHz @ 24-bit.
 */
class DopPacker {
    private var markerState = false

    /**
     * Packs raw 1-bit DSD bytes into 24-bit PCM DoP samples (3 bytes per sample, Little-Endian or 24-in-32 container).
     * @param dsdBytes Raw 1-bit DSD data block
     * @param channels Number of audio channels (e.g. 2 for stereo)
     * @return 24-bit DoP PCM byte array with 0x05/0xFA markers inserted
     */
    fun packDsdToDoP24(dsdBytes: ByteArray, channels: Int = 2): ByteArray {
        val bytesPerChannel = dsdBytes.size / channels
        val samplesPerChannel = bytesPerChannel / 2 // 16 DSD bits per 24-bit DoP sample
        val totalOutputBytes = samplesPerChannel * channels * 3

        val out = ByteArray(totalOutputBytes)
        var outIdx = 0

        for (s in 0 until samplesPerChannel) {
            val marker = if (markerState) 0xFA.toByte() else 0x05.toByte()
            markerState = !markerState

            for (ch in 0 until channels) {
                val dsdOffset = ch * bytesPerChannel + s * 2
                val dsdByte1 = if (dsdOffset < dsdBytes.size) dsdBytes[dsdOffset] else 0
                val dsdByte2 = if (dsdOffset + 1 < dsdBytes.size) dsdBytes[dsdOffset + 1] else 0

                // 24-bit DoP layout (Little-Endian):
                // Byte 0: DSD Byte 1 (LSB)
                // Byte 1: DSD Byte 2
                // Byte 2: Marker 0x05 / 0xFA (MSB)
                out[outIdx++] = dsdByte1
                out[outIdx++] = dsdByte2
                out[outIdx++] = marker
            }
        }

        return out
    }

    fun reset() {
        markerState = false
    }

    companion object {
        const val DOP_MARKER_1 = 0x05.toByte()
        const val DOP_MARKER_2 = 0xFA.toByte()
    }
}
