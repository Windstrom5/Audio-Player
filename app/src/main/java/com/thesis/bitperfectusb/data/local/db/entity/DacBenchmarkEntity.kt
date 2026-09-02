package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dac_benchmarks")
data class DacBenchmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dacLabel: String,
    val timestampEpochMs: Long,
    val sampleRateHz: Int,
    val bitDepth: Int,
    val stable: Boolean,
    val dropouts: Int,
    val latencyMs: Double,
    val bufferSizeBytes: Int,
    val note: String
)
