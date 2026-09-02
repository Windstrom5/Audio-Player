package com.thesis.bitperfectusb.domain.model

/**
 * A lossless audio file indexed from MediaStore/local storage, with the native
 * PCM parameters read directly from the file header (Section 3.2, MetadataExtractor).
 * This is the "source of truth" the PlaybackIntegrityEngine matches against DAC capability.
 */
/**
 * A user-picked folder (via Storage Access Framework) that gets scanned
 * alongside MediaStore — covers files MediaStore hasn't indexed yet.
 */
data class WatchedFolder(
    val id: Long = 0L,
    val uriString: String,
    val displayName: String,
    val dateAddedEpochMs: Long
)

data class AudioTrackModel(
    val id: Long = 0L,
    val filePath: String,
    val title: String,
    val artist: String?,
    val durationMs: Long,
    val format: AudioFileFormat,
    val pcm: PcmFormat,
    val fileSizeBytes: Long,
    val dateAddedEpochMs: Long
)
