package com.thesis.bitperfectusb.data.repository

import android.content.Context
import android.net.Uri
import com.thesis.bitperfectusb.data.local.db.dao.AudioTrackDao
import com.thesis.bitperfectusb.data.local.db.dao.WatchedFolderDao
import com.thesis.bitperfectusb.data.local.db.entity.AudioTrackEntity
import com.thesis.bitperfectusb.data.local.db.entity.WatchedFolderEntity
import com.thesis.bitperfectusb.data.scanner.LocalAudioScanner
import com.thesis.bitperfectusb.domain.model.AudioFileFormat
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.domain.repository.AudioLibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AudioLibraryRepositoryImpl(
    private val context: Context,
    private val dao: AudioTrackDao,
    private val watchedFolderDao: WatchedFolderDao,
    private val scanner: LocalAudioScanner
) : AudioLibraryRepository {

    override fun observeTracks(): Flow<List<AudioTrackModel>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeWatchedFolders(): Flow<List<WatchedFolder>> =
        watchedFolderDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addWatchedFolder(uri: Uri, displayName: String) = withContext(Dispatchers.IO) {
        // Persist permission across app/device restarts — without this, the URI stops
        // being readable the next time the process is killed and relaunched.
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Some providers don't support persistable permissions; the folder will
            // still work for this session, just won't survive a process restart.
        }
        watchedFolderDao.insert(
            WatchedFolderEntity(uriString = uri.toString(), displayName = displayName, dateAddedEpochMs = System.currentTimeMillis())
        )
        Unit
    }

    override suspend fun removeWatchedFolder(id: Long) = withContext(Dispatchers.IO) {
        watchedFolderDao.deleteById(id)
        Unit
    }

    /**
     * Combines a MediaStore scan with a scan of every watched (user-picked) folder
     * before writing to the DB. This has to happen together, not as two separate
     * rescans — pruning tracks not seen in *this* scan is what keeps deleted files
     * out of the library, and running it against only one source at a time would
     * wipe out everything the other source had added.
     */
    override suspend fun rescanLibrary(): Int = withContext(Dispatchers.IO) {
        val fromMediaStore = scanner.scan()
        val watchedFolders = watchedFolderDao.getAll()
        val fromFolders = watchedFolders.flatMap { folder ->
            try {
                scanner.scanFolder(Uri.parse(folder.uriString))
            } catch (e: SecurityException) {
                emptyList() // permission revoked (e.g. SD card unmounted) since this folder was added
            }
        }

        // De-duplicate by URI in case a watched folder overlaps with what MediaStore
        // already indexed (e.g. a folder inside the default Music directory).
        val scanned = (fromMediaStore + fromFolders).distinctBy { it.filePath }

        val existingIdByPath = dao.getAllEntities().associate { it.filePath to it.id }
        val entities = scanned.map { it.toEntity(existingIdByPath[it.filePath] ?: 0L) }
        dao.insertAll(entities)
        if (scanned.isNotEmpty()) {
            // Only prune when the scan actually found something — an empty result is far
            // more likely a transient MediaStore/permission hiccup than a genuinely empty
            // library, and NOT IN () with a zero-length list isn't something to rely on either way.
            dao.pruneMissing(scanned.map { it.filePath })
        }
        scanned.size
    }

    override suspend fun getTrack(id: Long): AudioTrackModel? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    private fun AudioTrackEntity.toDomain() = AudioTrackModel(
        id = id,
        filePath = filePath,
        title = title,
        artist = artist,
        durationMs = durationMs,
        format = AudioFileFormat.valueOf(format),
        pcm = PcmFormat(sampleRateHz, bitDepth, channels),
        fileSizeBytes = fileSizeBytes,
        dateAddedEpochMs = dateAddedEpochMs
    )

    private fun AudioTrackModel.toEntity(preservedId: Long) = AudioTrackEntity(
        id = preservedId,
        filePath = filePath,
        title = title,
        artist = artist,
        durationMs = durationMs,
        format = format.name,
        sampleRateHz = pcm.sampleRateHz,
        bitDepth = pcm.bitDepth,
        channels = pcm.channels,
        fileSizeBytes = fileSizeBytes,
        dateAddedEpochMs = dateAddedEpochMs
    )

    private fun WatchedFolderEntity.toDomain() = WatchedFolder(
        id = id,
        uriString = uriString,
        displayName = displayName,
        dateAddedEpochMs = dateAddedEpochMs
    )
}
