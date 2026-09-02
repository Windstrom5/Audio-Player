package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "audio_tracks",
    indices = [Index(value = ["filePath"], unique = true)]
)
data class AudioTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val filePath: String,
    val title: String,
    val artist: String?,
    val durationMs: Long,
    val format: String,       // AudioFileFormat.name
    val sampleRateHz: Int,
    val bitDepth: Int,
    val channels: Int,
    val fileSizeBytes: Long,
    val dateAddedEpochMs: Long
)
