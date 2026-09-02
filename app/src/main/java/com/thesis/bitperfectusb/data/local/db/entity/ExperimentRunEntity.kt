package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiment_runs")
data class ExperimentRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val experimentType: String,    // ExperimentType.name
    val configLabel: String,       // e.g. "exp_b_96000hz"
    val timestampEpochMs: Long,
    val sampleSize: Int,
    val meanLatencyMs: Double,
    val sdLatencyMs: Double,
    val meanCpuPercent: Double,
    val meanMemoryMb: Double,
    val dropouts: Int,
    val integrityScore: Int
)
