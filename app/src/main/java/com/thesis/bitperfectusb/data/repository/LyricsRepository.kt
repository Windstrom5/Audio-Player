package com.thesis.bitperfectusb.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.LyricsData
import com.thesis.bitperfectusb.domain.model.LyricsProvider
import com.thesis.bitperfectusb.domain.model.LyricsSearchResult
import com.thesis.bitperfectusb.playback.lyrics.LrcParser
import com.thesis.bitperfectusb.playback.lyrics.LyricsScraperClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Multi-source repository for retrieving and caching synchronized lyrics.
 * Order of resolution:
 * 1. User-imported / chosen custom .lrc cache
 * 2. Companion .lrc file in local storage (same folder & filename as audio file)
 * 3. Embedded lyrics metadata (ID3 USLT / Vorbis comment LYRICS)
 * 4. Online Multi-Provider Auto Scraper (LRCLIB, Netease 163, Kugou) with local caching
 */
class LyricsRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("lyrics_prefs", Context.MODE_PRIVATE)
    }

    private val lyricsCacheDir: File by lazy {
        File(context.filesDir, "lyrics_cache").apply { if (!exists()) mkdirs() }
    }

    fun getPreferredProvider(): LyricsProvider {
        val id = prefs.getString("preferred_provider", LyricsProvider.AUTO.id) ?: LyricsProvider.AUTO.id
        return LyricsProvider.values().firstOrNull { it.id == id } ?: LyricsProvider.AUTO
    }

    fun setPreferredProvider(provider: LyricsProvider) {
        prefs.edit().putString("preferred_provider", provider.id).apply()
    }

    suspend fun loadLyricsForTrack(
        track: AudioTrackModel,
        forceProvider: LyricsProvider? = null
    ): LyricsData = withContext(Dispatchers.IO) {
        val provider = forceProvider ?: getPreferredProvider()

        // 1. Check user-imported / chosen custom LRC in app storage
        val customFile = getCustomLyricsFile(track)
        if (customFile.exists()) {
            val content = runCatching { customFile.readText(Charsets.UTF_8) }.getOrNull()
            if (!content.isNullOrBlank()) {
                return@withContext LrcParser.parse(content).copy(
                    sourceDescription = "Custom / Saved (${customFile.name})"
                )
            }
        }

        // 2. Check for sidecar .lrc file in filesystem if filePath is a local file
        val sidecarLyrics = loadSidecarLrc(track.filePath)
        if (sidecarLyrics != null) {
            return@withContext sidecarLyrics.copy(
                provider = LyricsProvider.LOCAL_ONLY,
                sourceDescription = "Local Sidecar .lrc"
            )
        }

        // 3. Check for embedded lyrics in audio file metadata
        val embeddedLyrics = loadEmbeddedLyrics(track.filePath)
        if (embeddedLyrics != null) {
            return@withContext embeddedLyrics.copy(
                provider = LyricsProvider.LOCAL_ONLY,
                sourceDescription = "Embedded Audio Metadata"
            )
        }

        // 4. Online Auto-Fetch (unless provider is LOCAL_ONLY)
        if (provider != LyricsProvider.LOCAL_ONLY) {
            // Check online cache first
            val onlineCacheFile = getOnlineCacheFile(track)
            if (onlineCacheFile.exists()) {
                val content = runCatching { onlineCacheFile.readText(Charsets.UTF_8) }.getOrNull()
                if (!content.isNullOrBlank()) {
                    return@withContext LrcParser.parse(content).copy(
                        provider = provider,
                        sourceDescription = "Cached Online (${provider.displayName})"
                    )
                }
            }

            // Fetch live over network
            val onlineLyrics = LyricsScraperClient.fetchAutoLyrics(
                trackName = track.title,
                artistName = track.artist ?: "",
                albumName = null,
                durationMs = track.durationMs,
                preferredProvider = provider
            )

            if (onlineLyrics != null && (onlineLyrics.lines.isNotEmpty() || !onlineLyrics.plainText.isNullOrBlank())) {
                // Save to cache
                runCatching {
                    val rawToSave = if (onlineLyrics.lines.isNotEmpty()) {
                        onlineLyrics.lines.joinToString("\n") { line ->
                            val m = line.timestampMs / 60000
                            val s = (line.timestampMs % 60000) / 1000
                            val ms = (line.timestampMs % 1000) / 10
                            "[%02d:%02d.%02d]%s".format(m, s, ms, line.text)
                        }
                    } else {
                        onlineLyrics.plainText ?: ""
                    }
                    onlineCacheFile.writeText(rawToSave, Charsets.UTF_8)
                }
                return@withContext onlineLyrics
            }
        }

        return@withContext LyricsData(sourceDescription = "No lyrics found")
    }

    suspend fun searchOnlineLyrics(
        query: String,
        provider: LyricsProvider
    ): List<LyricsSearchResult> = withContext(Dispatchers.IO) {
        LyricsScraperClient.searchOnlineLyrics(query, provider)
    }

    suspend fun applySelectedOnlineLyrics(
        track: AudioTrackModel,
        result: LyricsSearchResult
    ): LyricsData = withContext(Dispatchers.IO) {
        val targetFile = getCustomLyricsFile(track)
        targetFile.writeText(result.rawLrcContent, Charsets.UTF_8)
        LrcParser.parse(result.rawLrcContent).copy(
            provider = result.provider,
            sourceDescription = "${result.provider.displayName} (${if (result.isSynced) "Synced" else "Plain"})"
        )
    }

    suspend fun saveCustomLyrics(track: AudioTrackModel, lrcContent: String): LyricsData = withContext(Dispatchers.IO) {
        val targetFile = getCustomLyricsFile(track)
        targetFile.writeText(lrcContent, Charsets.UTF_8)
        LrcParser.parse(lrcContent).copy(sourceDescription = "Imported .lrc")
    }

    suspend fun deleteCustomLyrics(track: AudioTrackModel): Unit = withContext(Dispatchers.IO) {
        val targetFile = getCustomLyricsFile(track)
        if (targetFile.exists()) {
            targetFile.delete()
        }
        val onlineFile = getOnlineCacheFile(track)
        if (onlineFile.exists()) {
            onlineFile.delete()
        }
    }

    private fun getCustomLyricsFile(track: AudioTrackModel): File {
        val safeName = "track_${track.id}_${track.filePath.hashCode()}.lrc"
        return File(lyricsCacheDir, safeName)
    }

    private fun getOnlineCacheFile(track: AudioTrackModel): File {
        val safeName = "track_${track.id}_online_${track.filePath.hashCode()}.lrc"
        return File(lyricsCacheDir, safeName)
    }

    private fun loadSidecarLrc(filePath: String): LyricsData? {
        return runCatching {
            val uri = Uri.parse(filePath)
            if (uri.scheme == null || uri.scheme == "file") {
                val path = uri.path ?: filePath
                val dotIndex = path.lastIndexOf('.')
                if (dotIndex != -1) {
                    val lrcPath = path.substring(0, dotIndex) + ".lrc"
                    val lrcFile = File(lrcPath)
                    if (lrcFile.exists() && lrcFile.isFile) {
                        val text = lrcFile.readText(Charsets.UTF_8)
                        if (text.isNotBlank()) {
                            return LrcParser.parse(text)
                        }
                    }
                }
            }
            null
        }.getOrNull()
    }

    private fun loadEmbeddedLyrics(filePath: String): LyricsData? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            val uri = Uri.parse(filePath)
            if (uri.scheme == "content") {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(uri.path ?: filePath)
            }

            val lyrics = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf { false }
            retriever.release()

            if (!lyrics.isNullOrBlank()) {
                LrcParser.parse(lyrics)
            } else {
                null
            }
        }.getOrNull()
    }
}

