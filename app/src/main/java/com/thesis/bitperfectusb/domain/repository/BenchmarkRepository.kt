package com.thesis.bitperfectusb.domain.repository

import com.thesis.bitperfectusb.domain.model.BenchmarkSample
import com.thesis.bitperfectusb.domain.model.ExperimentRun
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.model.PlaybackSession
import kotlinx.coroutines.flow.Flow

interface BenchmarkRepository {
    // Playback sessions (Analytics backbone)
    suspend fun startSession(session: PlaybackSession): Long
    suspend fun endSession(
        sessionId: Long,
        endEpochMs: Long,
        avgLatencyMs: Double,
        avgCpuPercent: Double,
        avgMemoryMb: Double,
        dropoutCount: Int,
        verifiedBitPerfect: Boolean? = null
    )
    fun observeSessions(): Flow<List<PlaybackSession>>
    suspend fun getAllSessionsSnapshot(): List<PlaybackSession>

    // Raw benchmark ticks
    suspend fun recordSample(sessionId: Long, sample: BenchmarkSample)
    suspend fun getSamplesForSession(sessionId: Long): List<BenchmarkSample>

    // Experiment runs (Research dashboard)
    suspend fun saveExperimentRun(run: ExperimentRun): Long
    fun observeExperimentRuns(type: ExperimentType): Flow<List<ExperimentRun>>
    suspend fun exportSessionsToCsv(destinationPath: String): Int
    suspend fun exportExperimentRunsToCsv(type: ExperimentType, destinationPath: String): Int
}
