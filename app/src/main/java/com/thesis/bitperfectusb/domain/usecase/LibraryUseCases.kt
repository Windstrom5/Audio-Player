package com.thesis.bitperfectusb.domain.usecase

import android.net.Uri
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.domain.repository.AudioLibraryRepository
import kotlinx.coroutines.flow.Flow

class ObserveLibraryUseCase(private val repository: AudioLibraryRepository) {
    operator fun invoke(): Flow<List<AudioTrackModel>> = repository.observeTracks()
}

/** RO1 support: (re)indexes local FLAC/WAV files and extracts native PCM metadata (Section 3.2).
 *  Covers both MediaStore and every watched (user-picked) folder in one combined pass. */
class RescanLibraryUseCase(private val repository: AudioLibraryRepository) {
    suspend operator fun invoke(): Int = repository.rescanLibrary()
}

class ObserveWatchedFoldersUseCase(private val repository: AudioLibraryRepository) {
    operator fun invoke(): Flow<List<WatchedFolder>> = repository.observeWatchedFolders()
}

/** Adds a folder picked via ACTION_OPEN_DOCUMENT_TREE to be scanned alongside MediaStore. */
class AddWatchedFolderUseCase(private val repository: AudioLibraryRepository) {
    suspend operator fun invoke(uri: Uri, displayName: String) = repository.addWatchedFolder(uri, displayName)
}

class RemoveWatchedFolderUseCase(private val repository: AudioLibraryRepository) {
    suspend operator fun invoke(id: Long) = repository.removeWatchedFolder(id)
}
