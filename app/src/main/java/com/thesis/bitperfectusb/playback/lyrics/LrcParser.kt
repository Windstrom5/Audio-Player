package com.thesis.bitperfectusb.playback.lyrics

import com.thesis.bitperfectusb.domain.model.LyricLine
import com.thesis.bitperfectusb.domain.model.LyricsData
import java.util.regex.Pattern

/**
 * Pure Kotlin parser for multi-format lyrics:
 * - Standard LRC (.lrc) synchronized lyric files ([mm:ss.xx])
 * - SubRip Subtitles (.srt) (`00:01:23,456 --> 00:01:25,789`)
 * - WebVTT (.vtt) (`00:01.456 --> 00:03.789`)
 * - Advanced SubStation Alpha (.ass / .ssa) (`Dialogue: 0,0:01:23.45,...`)
 * - Plain text (.txt) fallback
 */
object LrcParser {

    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")
    private val ID_TAG_PATTERN = Pattern.compile("\\[(ti|ar|al|offset):\\s*(.*?)\\]", Pattern.CASE_INSENSITIVE)
    private val SRT_VTT_PATTERN = Pattern.compile("(?:(\\d{1,2}):)?(\\d{2}):(\\d{2})[,.](\\d{2,3})\\s*-->")
    private val ASS_DIALOGUE_PATTERN = Pattern.compile("Dialogue:\\s*[^,]*,(\\d{1,2}):(\\d{2}):(\\d{2})\\.(\\d{2}),[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,[^,]*,(.*)", Pattern.CASE_INSENSITIVE)

    fun parse(content: String): LyricsData {
        if (content.isBlank()) return LyricsData()

        val lines = content.lines()
        val parsedLines = mutableListOf<LyricLine>()
        var title: String? = null
        var artist: String? = null
        var offsetMs = 0L
        var hasTimestamp = false

        // Check if content looks like SRT/VTT format
        if (content.contains("-->")) {
            var currentStartMs: Long? = null
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty() || line.all { it.isDigit() } || line.startsWith("WEBVTT")) continue

                val srtMatcher = SRT_VTT_PATTERN.matcher(line)
                if (srtMatcher.find()) {
                    val hours = srtMatcher.group(1)?.toLongOrNull() ?: 0L
                    val minutes = srtMatcher.group(2)?.toLongOrNull() ?: 0L
                    val seconds = srtMatcher.group(3)?.toLongOrNull() ?: 0L
                    val fracStr = srtMatcher.group(4) ?: "0"
                    val fracMs = if (fracStr.length == 2) fracStr.toLong() * 10L else fracStr.take(3).toLong()
                    currentStartMs = (hours * 3600_000L) + (minutes * 60_000L) + (seconds * 1000L) + fracMs
                    hasTimestamp = true
                } else if (currentStartMs != null) {
                    val cleanText = line.replace(Regex("<[^>]*>"), "").trim()
                    if (cleanText.isNotBlank()) {
                        parsedLines.add(LyricLine(timestampMs = currentStartMs, text = cleanText))
                    }
                    currentStartMs = null
                }
            }
        } else if (content.contains("[Events]") || content.lines().any { it.trimStart().startsWith("Dialogue:", ignoreCase = true) }) {
            // ASS / SSA format
            for (rawLine in lines) {
                val matcher = ASS_DIALOGUE_PATTERN.matcher(rawLine.trim())
                if (matcher.find()) {
                    val hours = matcher.group(1)?.toLongOrNull() ?: 0L
                    val minutes = matcher.group(2)?.toLongOrNull() ?: 0L
                    val seconds = matcher.group(3)?.toLongOrNull() ?: 0L
                    val centis = matcher.group(4)?.toLongOrNull() ?: 0L
                    val totalMs = (hours * 3600_000L) + (minutes * 60_000L) + (seconds * 1000L) + (centis * 10L)
                    val text = matcher.group(5)?.replace(Regex("\\{[^}]*\\}"), "")?.replace("\\N", " ")?.trim() ?: ""
                    if (text.isNotBlank()) {
                        parsedLines.add(LyricLine(timestampMs = totalMs, text = text))
                        hasTimestamp = true
                    }
                }
            }
        } else {
            // Standard LRC parsing
            for (rawLine in lines) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue

                // Check for metadata tags
                val idMatcher = ID_TAG_PATTERN.matcher(line)
                if (idMatcher.matches()) {
                    val key = idMatcher.group(1)?.lowercase()
                    val value = idMatcher.group(2)?.trim() ?: ""
                    when (key) {
                        "ti" -> title = value
                        "ar" -> artist = value
                        "offset" -> offsetMs = value.toLongOrNull() ?: 0L
                    }
                    continue
                }

                // Extract all timestamps in this line
                val timeMatcher = TIME_TAG_PATTERN.matcher(line)
                val timestamps = mutableListOf<Long>()
                var lastMatchEnd = 0

                while (timeMatcher.find()) {
                    hasTimestamp = true
                    val minutes = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                    val seconds = timeMatcher.group(2)?.toLongOrNull() ?: 0L
                    val fractionStr = timeMatcher.group(3)

                    val fractionMs = when {
                        fractionStr == null -> 0L
                        fractionStr.length == 1 -> fractionStr.toLong() * 100L
                        fractionStr.length == 2 -> fractionStr.toLong() * 10L
                        fractionStr.length >= 3 -> fractionStr.take(3).toLong()
                        else -> 0L
                    }

                    val totalMs = (minutes * 60_000L) + (seconds * 1_000L) + fractionMs
                    timestamps.add(totalMs)
                    lastMatchEnd = timeMatcher.end()
                }

                if (timestamps.isNotEmpty()) {
                    val lyricText = line.substring(lastMatchEnd).trim()
                    for (ts in timestamps) {
                        parsedLines.add(LyricLine(timestampMs = (ts + offsetMs).coerceAtLeast(0L), text = lyricText))
                    }
                }
            }
        }

        // If no timestamp tags were found, treat the entire string as unsynchronized plain text
        if (!hasTimestamp || parsedLines.isEmpty()) {
            return LyricsData(
                title = title,
                artist = artist,
                plainText = content.trim(),
                lines = emptyList()
            )
        }

        parsedLines.sortBy { it.timestampMs }

        return LyricsData(
            title = title,
            artist = artist,
            lines = parsedLines,
            plainText = null
        )
    }
}
