package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playback_sessions")
data class PlaybackSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val trackId: Long?,
    val dacProfileId: Long?,
    val engineType: String,        // EngineType.name
    val startEpochMs: Long,
    val endEpochMs: Long?,
    val integrityScore: Int,
    val avgLatencyMs: Double,
    val avgCpuPercent: Double,
    val avgMemoryMb: Double,
    val dropoutCount: Int,
    val bufferSizeBytes: Int,
    val verifiedBitPerfect: Boolean? = null
)
