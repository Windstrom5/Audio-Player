package com.thesis.bitperfectusb.playback.cue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CueSheetParserTest {

    private val sampleCue = """
        PERFORMER "Daft Punk"
        TITLE "Random Access Memories"
        FILE "Daft Punk - Random Access Memories.flac" WAVE
          TRACK 01 AUDIO
            TITLE "Give Life Back to Music"
            PERFORMER "Daft Punk"
            INDEX 01 00:00:00
          TRACK 02 AUDIO
            TITLE "Giorgio by Moroder"
            PERFORMER "Daft Punk"
            INDEX 01 04:34:40
          TRACK 03 AUDIO
            TITLE "Get Lucky"
            PERFORMER "Daft Punk feat. Pharrell Williams"
            INDEX 01 13:39:15
    """.trimIndent()

    @Test
    fun `parses CUE sheet tracks and timecodes accurately`() {
        val sheet = CueParser.parse(sampleCue, totalAudioDurationMs = 1200000L)

        assertEquals("Random Access Memories", sheet.albumTitle)
        assertEquals("Daft Punk", sheet.albumPerformer)
        assertEquals(3, sheet.tracks.size)

        // Track 1: Give Life Back to Music
        assertEquals(1, sheet.tracks[0].trackNumber)
        assertEquals("Give Life Back to Music", sheet.tracks[0].title)
        assertEquals("Daft Punk", sheet.tracks[0].performer)
        assertEquals(0L, sheet.tracks[0].startMs)

        // Track 2: Giorgio by Moroder at 04:34:40 (4m 34s 40 frames)
        assertEquals(2, sheet.tracks[1].trackNumber)
        assertEquals("Giorgio by Moroder", sheet.tracks[1].title)
        val expectedT2Ms = (4L * 60L * 1000L) + (34L * 1000L) + ((40L * 1000L) / 75L)
        assertEquals(expectedT2Ms, sheet.tracks[1].startMs)

        // Track 3: Get Lucky
        assertEquals(3, sheet.tracks[2].trackNumber)
        assertEquals("Get Lucky", sheet.tracks[2].title)
        assertEquals("Daft Punk feat. Pharrell Williams", sheet.tracks[2].performer)
    }

    @Test
    fun `timecode conversion matches CD frame rate standards`() {
        val ms = CueParser.parseCueTimestamp("01:23:45")
        val expected = (1L * 60L * 1000L) + (23L * 1000L) + ((45L * 1000L) / 75L)
        assertEquals(expected, ms)
    }
}
