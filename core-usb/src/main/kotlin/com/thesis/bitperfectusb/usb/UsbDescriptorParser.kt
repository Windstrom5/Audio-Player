package com.thesis.bitperfectusb.usb

/**
 * Walks the raw USB configuration descriptor block returned by
 * UsbDeviceConnection.getRawDescriptors(). Android's higher-level UsbInterface /
 * UsbEndpoint objects only expose *standard* descriptors, so any class-specific
 * (audio) descriptor — which is where sample rate, bit depth, and channel
 * capability actually live — has to be parsed from these raw bytes directly.
 */
class UsbDescriptorParser {

    /** One raw descriptor: [bLength][bDescriptorType][...bLength-2 bytes of payload]. */
    data class RawDescriptor(val length: Int, val type: Int, val bytes: ByteArray) {
        /** For class-specific descriptors, the first payload byte is the descriptor subtype. */
        val subtype: Int get() = if (bytes.size > 2) bytes[2].toInt() and 0xFF else -1
    }

    fun parseAll(rawDescriptors: ByteArray): List<RawDescriptor> {
        val result = mutableListOf<RawDescriptor>()
        var offset = 0
        while (offset + 2 <= rawDescriptors.size) {
            val length = rawDescriptors[offset].toInt() and 0xFF
            if (length <= 0 || offset + length > rawDescriptors.size) break
            val type = rawDescriptors[offset + 1].toInt() and 0xFF
            result += RawDescriptor(length, type, rawDescriptors.copyOfRange(offset, offset + length))
            offset += length
        }
        return result
    }

    // ---- little-endian field readers for descriptor payloads ----

    fun u8(d: RawDescriptor, index: Int): Int = d.bytes[index].toInt() and 0xFF

    fun u16(d: RawDescriptor, index: Int): Int =
        (d.bytes[index].toInt() and 0xFF) or ((d.bytes[index + 1].toInt() and 0xFF) shl 8)

    fun u24(d: RawDescriptor, index: Int): Int =
        (d.bytes[index].toInt() and 0xFF) or
            ((d.bytes[index + 1].toInt() and 0xFF) shl 8) or
            ((d.bytes[index + 2].toInt() and 0xFF) shl 16)

    fun u32(byteArray: ByteArray, index: Int): Long =
        (byteArray[index].toLong() and 0xFF) or
            ((byteArray[index + 1].toLong() and 0xFF) shl 8) or
            ((byteArray[index + 2].toLong() and 0xFF) shl 16) or
            ((byteArray[index + 3].toLong() and 0xFF) shl 24)
}
