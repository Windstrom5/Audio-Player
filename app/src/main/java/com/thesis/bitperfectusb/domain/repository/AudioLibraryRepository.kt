package com.thesis.bitperfectusb.domain.repository

import android.net.Uri
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import kotlinx.coroutines.flow.Flow

interface AudioLibraryRepository {
    fun observeTracks(): Flow<List<AudioTrackModel>>
    suspend fun rescanLibrary(): Int
    suspend fun getTrack(id: Long): AudioTrackModel?

    fun observeWatchedFolders(): Flow<List<WatchedFolder>>
    suspend fun addWatchedFolder(uri: Uri, displayName: String)
    suspend fun removeWatchedFolder(id: Long)
}
