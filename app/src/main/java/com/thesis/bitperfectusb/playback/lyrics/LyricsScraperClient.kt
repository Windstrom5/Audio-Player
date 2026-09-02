package com.thesis.bitperfectusb.playback.lyrics

import android.util.Base64
import com.thesis.bitperfectusb.domain.model.LyricsData
import com.thesis.bitperfectusb.domain.model.LyricsProvider
import com.thesis.bitperfectusb.domain.model.LyricsSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Free, unlimited multi-provider online lyrics scraper and search engine.
 * Supports:
 * - LRCLIB.net (Free open-source global synchronized lyrics API)
 * - Netease Cloud Music 163 (Comprehensive millisecond-synced database)
 * - Kugou Music (High-accuracy synced catalog)
 */
object LyricsScraperClient {

    private const val USER_AGENT = "BitPerfectUSB-AudioPlayer/1.0 (Android; Audiophile Hi-Res Engine)"
    private const val TIMEOUT_MS = 6000

    suspend fun fetchAutoLyrics(
        trackName: String,
        artistName: String,
        albumName: String? = null,
        durationMs: Long? = null,
        preferredProvider: LyricsProvider = LyricsProvider.AUTO
    ): LyricsData? = withContext(Dispatchers.IO) {
        val cleanTrack = cleanSearchTerm(trackName)
        val cleanArtist = cleanSearchTerm(artistName)
        if (cleanTrack.isBlank()) return@withContext null

        when (preferredProvider) {
            LyricsProvider.LRCLIB -> fetchFromLrclib(cleanTrack, cleanArtist, albumName, durationMs)
            LyricsProvider.NETEASE -> fetchFromNetease(cleanTrack, cleanArtist)
            LyricsProvider.KUGOU -> fetchFromKugou(cleanTrack, cleanArtist, durationMs)
            LyricsProvider.LOCAL_ONLY -> null
            LyricsProvider.AUTO -> {
                // Cascade: LRCLIB -> Netease -> Kugou
                fetchFromLrclib(cleanTrack, cleanArtist, albumName, durationMs)
                    ?: fetchFromNetease(cleanTrack, cleanArtist)
                    ?: fetchFromKugou(cleanTrack, cleanArtist, durationMs)
            }
        }
    }

    suspend fun searchOnlineLyrics(
        query: String,
        provider: LyricsProvider
    ): List<LyricsSearchResult> = withContext(Dispatchers.IO) {
        val cleanQ = cleanSearchTerm(query)
        if (cleanQ.isBlank()) return@withContext emptyList()

        when (provider) {
            LyricsProvider.LRCLIB -> searchLrclib(cleanQ)
            LyricsProvider.NETEASE -> searchNetease(cleanQ)
            LyricsProvider.KUGOU -> searchKugou(cleanQ)
            LyricsProvider.LOCAL_ONLY -> emptyList()
            LyricsProvider.AUTO -> coroutineScope {
                // Search all online databases concurrently and combine results
                val lrclibDeferred = async { runCatching { searchLrclib(cleanQ) }.getOrElse { emptyList() } }
                val neteaseDeferred = async { runCatching { searchNetease(cleanQ) }.getOrElse { emptyList() } }
                val kugouDeferred = async { runCatching { searchKugou(cleanQ) }.getOrElse { emptyList() } }

                val allResults = mutableListOf<LyricsSearchResult>()
                allResults.addAll(lrclibDeferred.await())
                allResults.addAll(neteaseDeferred.await())
                allResults.addAll(kugouDeferred.await())
                allResults
            }
        }
    }

    // ── 1. LRCLIB.net API ──────────────────────────────────────────────────
    private fun fetchFromLrclib(
        trackName: String,
        artistName: String,
        albumName: String?,
        durationMs: Long?
    ): LyricsData? {
        return runCatching {
            val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            var urlStr = "https://lrclib.net/api/get?track_name=$encodedTrack&artist_name=$encodedArtist"
            if (!albumName.isNullOrBlank()) {
                urlStr += "&album_name=${URLEncoder.encode(albumName, "UTF-8")}"
            }
            if (durationMs != null && durationMs > 0) {
                urlStr += "&duration=${durationMs / 1000}"
            }

            val jsonStr = httpGet(urlStr) ?: return@runCatching searchLrclibFallback(trackName, artistName)
            val json = JSONObject(jsonStr)
            val synced = json.optString("syncedLyrics", "").takeIf { it.isNotBlank() }
            val plain = json.optString("plainLyrics", "").takeIf { it.isNotBlank() }

            if (synced != null) {
                LrcParser.parse(synced).copy(
                    provider = LyricsProvider.LRCLIB,
                    sourceDescription = "LRCLIB.net (Synced)"
                )
            } else if (plain != null) {
                LyricsData(
                    title = json.optString("trackName"),
                    artist = json.optString("artistName"),
                    plainText = plain,
                    isSynchronized = false,
                    provider = LyricsProvider.LRCLIB,
                    sourceDescription = "LRCLIB.net (Plain Text)"
                )
            } else null
        }.getOrNull()
    }

    private fun searchLrclibFallback(trackName: String, artistName: String): LyricsData? {
        val results = searchLrclib("$trackName $artistName")
        val match = results.firstOrNull { it.rawLrcContent.isNotBlank() } ?: return null
        return LrcParser.parse(match.rawLrcContent).copy(
            provider = LyricsProvider.LRCLIB,
            sourceDescription = "LRCLIB.net (${if (match.isSynced) "Synced" else "Plain"})"
        )
    }

    private fun searchLrclib(query: String): List<LyricsSearchResult> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val urlStr = "https://lrclib.net/api/search?q=$encoded"
            val jsonStr = httpGet(urlStr) ?: return emptyList()
            val array = JSONArray(jsonStr)
            val results = mutableListOf<LyricsSearchResult>()

            for (i in 0 until minOf(array.length(), 10)) {
                val item = array.getJSONObject(i)
                val synced = item.optString("syncedLyrics", "")
                val plain = item.optString("plainLyrics", "")
                val raw = synced.ifBlank { plain }
                if (raw.isNotBlank()) {
                    results.add(
                        LyricsSearchResult(
                            id = item.optString("id", i.toString()),
                            title = item.optString("trackName", "Unknown Track"),
                            artist = item.optString("artistName", "Unknown Artist"),
                            album = item.optString("albumName", null),
                            durationSeconds = item.optInt("duration", 0),
                            isSynced = synced.isNotBlank(),
                            provider = LyricsProvider.LRCLIB,
                            rawLrcContent = raw
                        )
                    )
                }
            }
            results
        }.getOrElse { emptyList() }
    }

    // ── 2. Netease Cloud Music 163 API ─────────────────────────────────────
    private fun fetchFromNetease(trackName: String, artistName: String): LyricsData? {
        return runCatching {
            val results = searchNetease("$trackName $artistName")
            val match = results.firstOrNull() ?: return null
            if (match.rawLrcContent.isNotBlank()) {
                LrcParser.parse(match.rawLrcContent).copy(
                    provider = LyricsProvider.NETEASE,
                    sourceDescription = "Netease 163 (Synced)"
                )
            } else null
        }.getOrNull()
    }

    private fun searchNetease(query: String): List<LyricsSearchResult> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://music.163.com/api/search/get/web?csrf_token=&s=$encoded&type=1&offset=0&total=true&limit=6"
            val searchJsonStr = httpGet(searchUrl) ?: return emptyList()
            val root = JSONObject(searchJsonStr)
            val resultObj = root.optJSONObject("result") ?: return emptyList()
            val songs = resultObj.optJSONArray("songs") ?: return emptyList()

            val list = mutableListOf<LyricsSearchResult>()
            for (i in 0 until minOf(songs.length(), 6)) {
                val song = songs.getJSONObject(i)
                val songId = song.optLong("id")
                val name = song.optString("name", "Unknown Track")
                val durationMs = song.optLong("duration", 0)
                val artistsArr = song.optJSONArray("artists")
                val artistStr = if (artistsArr != null && artistsArr.length() > 0) {
                    artistsArr.getJSONObject(0).optString("name", "Unknown Artist")
                } else "Unknown Artist"
                val albumObj = song.optJSONObject("album")
                val albumStr = albumObj?.optString("name")

                // Fetch lyric content for this song ID
                val lyricUrl = "https://music.163.com/api/song/lyric?os=pc&id=$songId&lv=-1&kv=-1&tv=-1"
                val lyricJsonStr = httpGet(lyricUrl)
                var rawLrc = ""
                if (lyricJsonStr != null) {
                    val lyricObj = JSONObject(lyricJsonStr)
                    rawLrc = lyricObj.optJSONObject("lrc")?.optString("lyric", "") ?: ""
                }

                if (rawLrc.isNotBlank()) {
                    list.add(
                        LyricsSearchResult(
                            id = songId.toString(),
                            title = name,
                            artist = artistStr,
                            album = albumStr,
                            durationSeconds = (durationMs / 1000).toInt(),
                            isSynced = rawLrc.contains("["),
                            provider = LyricsProvider.NETEASE,
                            rawLrcContent = rawLrc
                        )
                    )
                }
            }
            list
        }.getOrElse { emptyList() }
    }

    // ── 3. Kugou Music API ─────────────────────────────────────────────────
    private fun fetchFromKugou(trackName: String, artistName: String, durationMs: Long?): LyricsData? {
        return runCatching {
            val results = searchKugou("$trackName $artistName")
            val match = results.firstOrNull() ?: return null
            if (match.rawLrcContent.isNotBlank()) {
                LrcParser.parse(match.rawLrcContent).copy(
                    provider = LyricsProvider.KUGOU,
                    sourceDescription = "Kugou Music (Synced)"
                )
            } else null
        }.getOrNull()
    }

    private fun searchKugou(query: String): List<LyricsSearchResult> {
        return runCatching {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "http://krcs.kugou.com/search?ver=1&man=yes&client=mobi&keyword=$encoded&duration=&hash="
            val jsonStr = httpGet(searchUrl) ?: return emptyList()
            val root = JSONObject(jsonStr)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()

            val list = mutableListOf<LyricsSearchResult>()
            for (i in 0 until minOf(candidates.length(), 6)) {
                val cand = candidates.getJSONObject(i)
                val id = cand.optString("id")
                val accessKey = cand.optString("accesskey")
                val songTitle = cand.optString("song", query)
                val singer = cand.optString("singer", "Unknown Artist")
                val durationSec = cand.optInt("duration", 0)

                // Download lyric
                val dlUrl = "http://krcs.kugou.com/download?ver=1&client=mobi&id=$id&accesskey=$accessKey&fmt=lrc&charset=utf8"
                val dlJsonStr = httpGet(dlUrl)
                var lrcText = ""
                if (dlJsonStr != null) {
                    val dlObj = JSONObject(dlJsonStr)
                    val contentBase64 = dlObj.optString("content", "")
                    if (contentBase64.isNotBlank()) {
                        val decodedBytes = Base64.decode(contentBase64, Base64.DEFAULT)
                        lrcText = String(decodedBytes, Charsets.UTF_8)
                    }
                }

                if (lrcText.isNotBlank()) {
                    list.add(
                        LyricsSearchResult(
                            id = id,
                            title = songTitle,
                            artist = singer,
                            album = null,
                            durationSeconds = durationSec,
                            isSynced = lrcText.contains("["),
                            provider = LyricsProvider.KUGOU,
                            rawLrcContent = lrcText
                        )
                    )
                }
            }
            list
        }.getOrElse { emptyList() }
    }

    // ── HTTP Helper ────────────────────────────────────────────────────────
    private fun httpGet(urlStr: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            if (connection.responseCode in 200..299) {
                BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** Strips file extensions and bracket annotations like "(Remastered 2024)" to improve query hit rates. */
    private fun cleanSearchTerm(term: String): String {
        return term
            .replace(Regex("""\.(mp3|flac|wav|m4a|aac|ogg|dsf|dff|aiff)$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s*[\(\[\{](feat\.|ft\.|remastered|official|video|audio|explicit|version|mix)[\)\]\}]""", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}
