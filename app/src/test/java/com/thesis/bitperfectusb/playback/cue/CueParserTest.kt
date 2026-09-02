package com.thesis.bitperfectusb.playback.cue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CueParserTest {

    private val sampleCue = """
        PERFORMER "Daft Punk"
        TITLE "Random Access Memories"
        FILE "Daft Punk - Random Access Memories.flac" WAVE
          TRACK 01 AUDIO
            TITLE "Give Life Back to Music"
            PERFORMER "Daft Punk"
            INDEX 01 00:00:00
          TRACK 02 AUDIO
            TITLE "The Game of Love"
            PERFORMER "Daft Punk"
            INDEX 01 04:34:53
          TRACK 03 AUDIO
            TITLE "Giorgio by Moroder"
            PERFORMER "Daft Punk"
            INDEX 01 10:00:00
    """.trimIndent()

    @Test
    fun testParseCueSheet() {
        val cueSheet = CueParser.parse(sampleCue, totalAudioDurationMs = 900000L)
        assertEquals("Random Access Memories", cueSheet.albumTitle)
        assertEquals("Daft Punk", cueSheet.albumPerformer)
        assertEquals("Daft Punk - Random Access Memories.flac", cueSheet.audioFileName)
        assertEquals(3, cueSheet.tracks.size)

        val track1 = cueSheet.tracks[0]
        assertEquals(1, track1.trackNumber)
        assertEquals("Give Life Back to Music", track1.title)
        assertEquals(0L, track1.startMs)
        assertTrue(track1.endMs!! > 0L)

        val track2 = cueSheet.tracks[1]
        assertEquals(2, track2.trackNumber)
        assertEquals("The Game of Love", track2.title)
        // 4 mins 34 secs 53 frames = (4*60 + 34)*1000 + (53*1000/75) = 274000 + 706 = 274706 ms
        assertEquals(274706L, track2.startMs)
    }

    @Test
    fun testParseTimestamp() {
        // 01:23:45 -> 1 min, 23 secs, 45 frames (45/75 = 600ms) = 83600 ms
        val ms = CueParser.parseCueTimestamp("01:23:45")
        assertEquals(83600L, ms)
    }
}
