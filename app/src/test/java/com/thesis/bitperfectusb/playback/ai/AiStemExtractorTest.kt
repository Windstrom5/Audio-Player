package com.thesis.bitperfectusb.playback.ai

import android.net.Uri
import com.thesis.bitperfectusb.domain.model.AudioFileFormat
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.domain.repository.AudioLibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiStemExtractorTest {

    private lateinit var extractor: AiStemExtractor

    private val fakeRepo = object : AudioLibraryRepository {
        override fun observeTracks(): Flow<List<AudioTrackModel>> = flowOf(emptyList())
        override suspend fun rescanLibrary(): Int = 0
        override suspend fun getTrack(id: Long): AudioTrackModel? = null
        override fun observeWatchedFolders(): Flow<List<WatchedFolder>> = flowOf(emptyList())
        override suspend fun addWatchedFolder(uri: Uri, displayName: String) {}
        override suspend fun removeWatchedFolder(id: Long) {}
    }

    @Before
    fun setUp() {
        extractor = AiStemExtractor(null, fakeRepo)
    }

    @Test
    fun `test extractStem emits initial progress status`() = runBlocking {
        val dummyTrack = AudioTrackModel(
            id = 1L,
            filePath = "non_existent_file.flac",
            title = "Test Track",
            artist = "Artist",
            durationMs = 1000L,
            format = AudioFileFormat.FLAC,
            pcm = PcmFormat(44100, 16, 2),
            fileSizeBytes = 1024L,
            dateAddedEpochMs = 0L
        )

        val progressList = extractor.extractStem(dummyTrack, KaraokeModeType.INSTRUMENTAL_ONLY).toList()
        assertTrue(progressList.isNotEmpty())
        assertNotNull(progressList.first().statusMessage)
    }
}
