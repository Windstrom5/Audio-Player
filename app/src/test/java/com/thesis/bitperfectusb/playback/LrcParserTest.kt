package com.thesis.bitperfectusb.playback

import com.thesis.bitperfectusb.playback.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun `parse standard timestamps and metadata tags`() {
        val lrc = """
            [ti:Audiophile Anthem]
            [ar:High-Res Artist]
            [00:01.50]First line of high fidelity sound
            [00:04.20]Second line with crystal clear mids
            [00:10.00]Third line deep bass drop
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)

        assertEquals("Audiophile Anthem", parsed.title)
        assertEquals("High-Res Artist", parsed.artist)
        assertTrue(parsed.isSynchronized)
        assertEquals(3, parsed.lines.size)

        assertEquals(1500L, parsed.lines[0].timestampMs)
        assertEquals("First line of high fidelity sound", parsed.lines[0].text)

        assertEquals(4200L, parsed.lines[1].timestampMs)
        assertEquals(10000L, parsed.lines[2].timestampMs)
    }

    @Test
    fun `parse multi-timestamp lines correctly ordered`() {
        val lrc = """
            [00:02.00][00:08.00]Repeated vocal chorus
            [00:05.00]Bridge line
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)
        assertEquals(3, parsed.lines.size)

        assertEquals(2000L, parsed.lines[0].timestampMs)
        assertEquals("Repeated vocal chorus", parsed.lines[0].text)

        assertEquals(5000L, parsed.lines[1].timestampMs)
        assertEquals("Bridge line", parsed.lines[1].text)

        assertEquals(8000L, parsed.lines[2].timestampMs)
        assertEquals("Repeated vocal chorus", parsed.lines[2].text)
    }

    @Test
    fun `findActiveIndex performs accurate binary search`() {
        val lrc = """
            [00:01.00]Line 1
            [00:05.00]Line 2
            [00:10.00]Line 3
            [00:15.00]Line 4
        """.trimIndent()

        val parsed = LrcParser.parse(lrc)

        assertEquals(-1, parsed.findActiveIndex(500L)) // Before start
        assertEquals(0, parsed.findActiveIndex(1000L)) // Exactly on Line 1
        assertEquals(0, parsed.findActiveIndex(3000L)) // During Line 1
        assertEquals(1, parsed.findActiveIndex(5000L)) // Exactly on Line 2
        assertEquals(1, parsed.findActiveIndex(8500L)) // During Line 2
        assertEquals(2, parsed.findActiveIndex(10000L)) // Line 3
        assertEquals(3, parsed.findActiveIndex(20000L)) // After last line
    }

    @Test
    fun `fallback to unsynchronized plain text when no timestamps present`() {
        val text = """
            Just some lyrics without timestamps.
            Second line of text.
        """.trimIndent()

        val parsed = LrcParser.parse(text)
        assertFalse(parsed.isSynchronized)
        assertEquals(0, parsed.lines.size)
        assertEquals(text, parsed.plainText)
    }
}
