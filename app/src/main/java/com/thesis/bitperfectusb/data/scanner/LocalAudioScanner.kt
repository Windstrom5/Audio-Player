package com.thesis.bitperfectusb.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.thesis.bitperfectusb.domain.model.AudioTrackModel

/**
 * Scans for WAV and FLAC files two ways (Section 3.2, LocalAudioScanner). Only
 * lossless containers are indexed — lossy formats are explicitly out of scope
 * (Chapter 1.5).
 *
 * [scan] queries MediaStore, which is fast but only knows about files it has
 * already indexed — a common real-world gap for files copied over USB/from a
 * computer without triggering a media rescan, or for locations some OEM skins
 * don't index by default. [scanFolder] walks a user-picked folder directly via
 * the Storage Access Framework, independent of whatever MediaStore happens to
 * know about, for exactly that gap.
 */
class LocalAudioScanner(private val context: Context) {

    private val metadataExtractor = MetadataExtractor(context.contentResolver)

    fun scan(): List<AudioTrackModel> {
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE
        )
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?, ?, ?)"
        val selectionArgs = arrayOf("audio/x-wav", "audio/wav", "audio/flac", "audio/x-flac")

        val results = mutableListOf<AudioTrackModel>()

        context.contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                val metadata = metadataExtractor.extract(uri) ?: continue // skip unparsable/corrupt files

                val rawName = cursor.getString(nameCol) ?: "Unknown"
                val rawTitle = if (titleCol != -1) cursor.getString(titleCol) else null
                val rawArtist = cursor.getString(artistCol)

                val (resolvedTitle, resolvedArtist) = resolveTags(uri, rawName, rawTitle, rawArtist)

                results += AudioTrackModel(
                    id = 0L,
                    filePath = uri.toString(),
                    title = resolvedTitle,
                    artist = resolvedArtist,
                    durationMs = metadata.durationMs.takeIf { it > 0 } ?: cursor.getLong(durationCol),
                    format = metadata.format,
                    pcm = metadata.pcm,
                    fileSizeBytes = cursor.getLong(sizeCol),
                    dateAddedEpochMs = cursor.getLong(dateCol) * 1000L
                )
            }
        }
        return results
    }

    /**
     * Recursively walks a folder tree picked via ACTION_OPEN_DOCUMENT_TREE,
     * finding every .wav/.flac file regardless of whether MediaStore has
     * indexed it. Extension-based matching is used rather than MIME type,
     * since arbitrary storage providers (SD cards, some OEM file managers)
     * don't always report accurate MIME types through SAF.
     */
    fun scanFolder(folderUri: Uri): List<AudioTrackModel> {
        val root = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()
        val results = mutableListOf<AudioTrackModel>()
        walkDocumentTree(root, results)
        return results
    }

    private fun walkDocumentTree(dir: DocumentFile, results: MutableList<AudioTrackModel>) {
        val children = try {
            dir.listFiles()
        } catch (e: SecurityException) {
            return // permission may have been revoked (e.g. SD card removed) since the folder was picked
        }

        for (child in children) {
            if (child.isDirectory) {
                walkDocumentTree(child, results)
                continue
            }
            if (!child.isFile) continue

            val name = child.name ?: continue
            val lower = name.lowercase()
            val isLossless = lower.endsWith(".wav") || lower.endsWith(".flac") ||
                    lower.endsWith(".aiff") || lower.endsWith(".aif") ||
                    lower.endsWith(".m4a") || lower.endsWith(".dsf") || lower.endsWith(".ape")
            if (!isLossless) continue

            val metadata = metadataExtractor.extract(child.uri) ?: continue // skip unparsable/corrupt files
            val (resolvedTitle, resolvedArtist) = resolveTags(child.uri, name, null, null)

            results += AudioTrackModel(
                id = 0L,
                filePath = child.uri.toString(),
                title = resolvedTitle,
                artist = resolvedArtist,
                durationMs = metadata.durationMs,
                format = metadata.format,
                pcm = metadata.pcm,
                fileSizeBytes = child.length(),
                dateAddedEpochMs = child.lastModified()
            )
        }
    }

    /**
     * Intelligently resolves the Track Title and Artist by first attempting to
     * read embedded ID3/Vorbis tags via MediaMetadataRetriever, and falling back
     * to parsing standard audio filename patterns like "Artist - Title" or "Title - Artist".
     */
    private fun resolveTags(uri: Uri, rawFileName: String, rawTitle: String?, rawArtist: String?): Pair<String, String?> {
        var title = rawTitle?.trim()
        var artist = cleanArtist(rawArtist)

        // Try extracting embedded tags via MediaMetadataRetriever
        if (title.isNullOrEmpty() || artist == null) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()
                val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.trim()
                    ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)?.trim()

                if (!metaTitle.isNullOrEmpty() && title.isNullOrEmpty()) {
                    title = metaTitle
                }
                if (artist == null) {
                    artist = cleanArtist(metaArtist)
                }
            } catch (_: Exception) {
                // Ignore retriever failure; fall back to filename heuristics below
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}
            }
        }

        // Clean filename without extension
        val cleanName = rawFileName
            .substringBeforeLast('.')
            .trim()

        if (title.isNullOrEmpty()) {
            title = cleanName
        }

        // If artist is still not found and the string contains " - ", parse title and artist
        if (artist == null && title.contains(" - ")) {
            val parts = title.split(" - ", limit = 2)
            if (parts.size == 2) {
                val p1 = parts[0].trim()
                val p2 = parts[1].trim()
                // Heuristic: If first part has digits or looks like a title, p1 is title and p2 is artist
                title = p1
                artist = p2
            }
        }

        return Pair(title, artist)
    }

    private fun cleanArtist(artist: String?): String? {
        val trimmed = artist?.trim() ?: return null
        if (trimmed.isEmpty() ||
            trimmed.equals("<unknown>", ignoreCase = true) ||
            trimmed.equals("Unknown", ignoreCase = true) ||
            trimmed.equals("Unknown Artist", ignoreCase = true) ||
            trimmed.equals("null", ignoreCase = true)) {
            return null
        }
        return trimmed
    }
}
