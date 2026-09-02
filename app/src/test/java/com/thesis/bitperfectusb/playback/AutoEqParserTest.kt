package com.thesis.bitperfectusb.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEqParserTest {

    @Test
    fun `parses standard AutoEQ text format correctly`() {
        val autoEqText = """
            Preamp: -3.5 dB
            Filter 1: ON LSC Fc 105 Hz Gain 4.5 dB Q 0.71
            Filter 2: ON PK Fc 1500 Hz Gain -2.0 dB Q 1.41
            Filter 3: OFF PK Fc 4000 Hz Gain 1.0 dB Q 2.0
        """.trimIndent()

        val bands = AutoEqParser.parseAutoEqText(autoEqText)

        assertEquals(5, bands.size)
        assertTrue(bands[0].enabled)
        assertEquals(FilterType.LOW_SHELF, bands[0].type)
        assertEquals(105f, bands[0].fcHz, 0.1f)
        assertEquals(4.5f, bands[0].gainDb, 0.1f)

        assertTrue(bands[1].enabled)
        assertEquals(FilterType.PEAK, bands[1].type)
        assertEquals(1500f, bands[1].fcHz, 0.1f)
        assertEquals(-2.0f, bands[1].gainDb, 0.1f)
    }
}
