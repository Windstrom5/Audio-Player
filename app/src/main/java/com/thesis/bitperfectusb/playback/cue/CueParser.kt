package com.thesis.bitperfectusb.playback.cue

data class CueTrack(
    val trackNumber: Int,
    val title: String,
    val performer: String? = null,
    val startMs: Long = 0L,
    val endMs: Long? = null
) {
    val durationMs: Long get() = (endMs ?: (startMs + 180000L)) - startMs
}

data class CueSheet(
    val albumTitle: String? = null,
    val albumPerformer: String? = null,
    val audioFileName: String? = null,
    val tracks: List<CueTrack> = emptyList()
)

object CueParser {

    /**
     * Parses standard Redbook CUE sheet text into structured tracks with millisecond start/end points.
     */
    fun parse(cueContent: String, totalAudioDurationMs: Long? = null): CueSheet {
        var globalAlbumTitle: String? = null
        var globalPerformer: String? = null
        var audioFileName: String? = null

        val parsedTracks = mutableListOf<CueTrackBuilder>()
        var currentTrackBuilder: CueTrackBuilder? = null

        val lines = cueContent.lines()
        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank() || line.startsWith("REM", ignoreCase = true)) continue

            val lower = line.lowercase()
            when {
                lower.startsWith("performer") -> {
                    val value = extractQuotedOrRest(line, "performer")
                    if (currentTrackBuilder == null) {
                        globalPerformer = value
                    } else {
                        currentTrackBuilder.performer = value
                    }
                }
                lower.startsWith("title") -> {
                    val value = extractQuotedOrRest(line, "title")
                    if (currentTrackBuilder == null) {
                        globalAlbumTitle = value
                    } else {
                        currentTrackBuilder.title = value
                    }
                }
                lower.startsWith("file") -> {
                    audioFileName = extractQuotedOrRest(line, "file")
                }
                lower.startsWith("track") -> {
                    currentTrackBuilder?.let { parsedTracks.add(it) }
                    val parts = line.split("\\s+".toRegex())
                    val trackNum = parts.getOrNull(1)?.toIntOrNull() ?: (parsedTracks.size + 1)
                    currentTrackBuilder = CueTrackBuilder(trackNumber = trackNum)
                }
                lower.startsWith("index 01") -> {
                    val parts = line.split("\\s+".toRegex())
                    val timeStr = parts.getOrNull(2) ?: "00:00:00"
                    currentTrackBuilder?.startMs = parseCueTimestamp(timeStr)
                }
            }
        }
        currentTrackBuilder?.let { parsedTracks.add(it) }

        // Compute endMs for each track from the startMs of the subsequent track
        val finalizedTracks = mutableListOf<CueTrack>()
        for (i in parsedTracks.indices) {
            val current = parsedTracks[i]
            val next = parsedTracks.getOrNull(i + 1)
            val endMs = next?.startMs ?: totalAudioDurationMs

            finalizedTracks.add(
                CueTrack(
                    trackNumber = current.trackNumber,
                    title = current.title ?: "Track ${current.trackNumber}",
                    performer = current.performer ?: globalPerformer,
                    startMs = current.startMs,
                    endMs = endMs
                )
            )
        }

        return CueSheet(
            albumTitle = globalAlbumTitle,
            albumPerformer = globalPerformer,
            audioFileName = audioFileName,
            tracks = finalizedTracks
        )
    }

    /**
     * Converts standard CUE timestamp "MM:SS:FF" (75 frames per second) to milliseconds.
     */
    fun parseCueTimestamp(timeStr: String): Long {
        val parts = timeStr.split(":")
        if (parts.size != 3) return 0L

        val minutes = parts[0].toLongOrNull() ?: 0L
        val seconds = parts[1].toLongOrNull() ?: 0L
        val frames = parts[2].toLongOrNull() ?: 0L

        return (minutes * 60L + seconds) * 1000L + (frames * 1000L / 75L)
    }

    private fun extractQuotedOrRest(line: String, command: String): String {
        val firstQuote = line.indexOf('"')
        val lastQuote = line.lastIndexOf('"')
        if (firstQuote != -1 && lastQuote > firstQuote) {
            return line.substring(firstQuote + 1, lastQuote)
        }
        val remainder = line.substring(command.length).trim()
        val parts = remainder.split("\\s+".toRegex())
        return parts.firstOrNull() ?: remainder
    }

    private class CueTrackBuilder(
        val trackNumber: Int,
        var title: String? = null,
        var performer: String? = null,
        var startMs: Long = 0L
    )
}
