package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benchmark_samples")
data class BenchmarkSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sessionId: Long,
    val timestampMs: Long,
    val cpuPercent: Double,
    val memoryMb: Double,
    val latencyMs: Double,
    val cumulativeDropouts: Int,
    val engineType: String
)
