package com.thesis.bitperfectusb.playback.autoeq

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEqOnlineScraperTest {

    private val sampleGraphicEq = """
        GraphicEQ: 20 1.5; 31.5 2.0; 63 3.5; 125 1.5; 250 -0.5; 500 0.0; 1000 0.8; 2000 1.8; 4000 -1.5; 8000 -3.0; 16000 0.5
    """.trimIndent()

    @Test
    fun `parseGraphicEqFile extracts exactly 10 ISO standard bands with accurate interpolation`() {
        val gains = AutoEqOnlineScraper.parseGraphicEqFile(sampleGraphicEq)

        assertEquals("Must return 10 EQ bands", 10, gains.size)

        // 31.25Hz -> ~2.0dB
        assertEquals(2.0f, gains[0], 0.1f)

        // 62.5Hz -> ~3.5dB
        assertEquals(3.5f, gains[1], 0.1f)

        // 125Hz -> 1.5dB
        assertEquals(1.5f, gains[2], 0.1f)

        // 250Hz -> -0.5dB
        assertEquals(-0.5f, gains[3], 0.1f)

        // 500Hz -> 0.0dB
        assertEquals(0.0f, gains[4], 0.1f)

        // 1000Hz -> 0.8dB
        assertEquals(0.8f, gains[5], 0.1f)

        // 2000Hz -> 1.8dB
        assertEquals(1.8f, gains[6], 0.1f)

        // 4000Hz -> -1.5dB
        assertEquals(-1.5f, gains[7], 0.1f)

        // 8000Hz -> -3.0dB
        assertEquals(-3.0f, gains[8], 0.1f)

        // 16000Hz -> 0.5dB
        assertEquals(0.5f, gains[9], 0.1f)
    }

    @Test
    fun `handles empty or malformed strings gracefully`() {
        val emptyGains = AutoEqOnlineScraper.parseGraphicEqFile("")
        assertEquals(10, emptyGains.size)
        assertTrue(emptyGains.all { it == 0.0f })
    }
}
