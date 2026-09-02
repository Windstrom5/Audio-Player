package com.thesis.bitperfectusb.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class DopPackerTest {

    @Test
    fun `packDsdToDoP24 inserts alternating 0x05 and 0xFA marker bytes`() {
        val packer = DopPacker()
        val dsdData = ByteArray(8) { 0xAA.toByte() } // 8 DSD bytes = 2 samples stereo
        val dopOutput = packer.packDsdToDoP24(dsdData, channels = 2)

        // Expected 2 samples * 2 channels * 3 bytes = 12 output bytes
        assertEquals(12, dopOutput.size)

        // Sample 0, Ch 0 & Ch 1 MSB must be 0x05
        assertEquals(0x05.toByte(), dopOutput[2])
        assertEquals(0x05.toByte(), dopOutput[5])

        // Sample 1, Ch 0 & Ch 1 MSB must be 0xFA
        assertEquals(0xFA.toByte(), dopOutput[8])
        assertEquals(0xFA.toByte(), dopOutput[11])
    }
}
