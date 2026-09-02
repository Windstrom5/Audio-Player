package com.thesis.bitperfectusb.data.repository

import com.thesis.bitperfectusb.benchmark.CsvExporter
import com.thesis.bitperfectusb.data.local.db.dao.BenchmarkDao
import com.thesis.bitperfectusb.data.local.db.dao.ExperimentDao
import com.thesis.bitperfectusb.data.local.db.entity.BenchmarkSampleEntity
import com.thesis.bitperfectusb.data.local.db.entity.ExperimentRunEntity
import com.thesis.bitperfectusb.data.local.db.entity.PlaybackSessionEntity
import com.thesis.bitperfectusb.domain.model.BenchmarkSample
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.ExperimentRun
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.model.PlaybackSession
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BenchmarkRepositoryImpl(
    private val benchmarkDao: BenchmarkDao,
    private val experimentDao: ExperimentDao,
    private val csvExporter: CsvExporter
) : BenchmarkRepository {

    override suspend fun startSession(session: PlaybackSession): Long = withContext(Dispatchers.IO) {
        benchmarkDao.insertSession(session.toEntity())
    }

    override suspend fun endSession(
        sessionId: Long,
        endEpochMs: Long,
        avgLatencyMs: Double,
        avgCpuPercent: Double,
        avgMemoryMb: Double,
        dropoutCount: Int,
        verifiedBitPerfect: Boolean?
    ) = withContext(Dispatchers.IO) {
        val existing = benchmarkDao.getSession(sessionId) ?: return@withContext
        benchmarkDao.updateSession(
            existing.copy(
                endEpochMs = endEpochMs,
                avgLatencyMs = avgLatencyMs,
                avgCpuPercent = avgCpuPercent,
                avgMemoryMb = avgMemoryMb,
                dropoutCount = dropoutCount,
                verifiedBitPerfect = verifiedBitPerfect
            )
        )
    }

    override fun observeSessions(): Flow<List<PlaybackSession>> =
        benchmarkDao.observeSessions().map { list -> list.map { it.toDomain() } }

    override suspend fun getAllSessionsSnapshot(): List<PlaybackSession> = withContext(Dispatchers.IO) {
        benchmarkDao.getAllSessions().map { it.toDomain() }
    }

    override suspend fun recordSample(sessionId: Long, sample: BenchmarkSample) = withContext(Dispatchers.IO) {
        benchmarkDao.insertSample(
            BenchmarkSampleEntity(
                sessionId = sessionId,
                timestampMs = sample.timestampMs,
                cpuPercent = sample.cpuPercent,
                memoryMb = sample.memoryMb,
                latencyMs = sample.latencyMs,
                cumulativeDropouts = sample.cumulativeDropouts,
                engineType = sample.engineType.name
            )
        )
    }

    override suspend fun getSamplesForSession(sessionId: Long): List<BenchmarkSample> = withContext(Dispatchers.IO) {
        benchmarkDao.getSamplesForSession(sessionId).map {
            BenchmarkSample(
                timestampMs = it.timestampMs,
                cpuPercent = it.cpuPercent,
                memoryMb = it.memoryMb,
                latencyMs = it.latencyMs,
                cumulativeDropouts = it.cumulativeDropouts,
                engineType = EngineType.valueOf(it.engineType)
            )
        }
    }

    override suspend fun saveExperimentRun(run: ExperimentRun): Long = withContext(Dispatchers.IO) {
        experimentDao.insert(
            ExperimentRunEntity(
                experimentType = run.experimentType.name,
                configLabel = run.configLabel,
                timestampEpochMs = run.timestampEpochMs,
                sampleSize = run.sampleSize,
                meanLatencyMs = run.meanLatencyMs,
                sdLatencyMs = run.sdLatencyMs,
                meanCpuPercent = run.meanCpuPercent,
                meanMemoryMb = run.meanMemoryMb,
                dropouts = run.dropouts,
                integrityScore = run.integrityScore
            )
        )
    }

    override fun observeExperimentRuns(type: ExperimentType): Flow<List<ExperimentRun>> =
        experimentDao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    override suspend fun exportSessionsToCsv(destinationPath: String): Int = withContext(Dispatchers.IO) {
        val sessions = benchmarkDao.getAllSessions()
        csvExporter.write(
            destinationPath,
            header = listOf(
                "id", "trackId", "dacProfileId", "engineType", "startEpochMs", "endEpochMs",
                "integrityScore", "avgLatencyMs", "avgCpuPercent", "avgMemoryMb", "dropoutCount",
                "bufferSizeBytes", "verifiedBitPerfect"
            ),
            rows = sessions.map {
                listOf(
                    it.id, it.trackId, it.dacProfileId, it.engineType, it.startEpochMs, it.endEpochMs,
                    it.integrityScore, it.avgLatencyMs, it.avgCpuPercent, it.avgMemoryMb, it.dropoutCount,
                    it.bufferSizeBytes, it.verifiedBitPerfect
                )
            }
        )
    }

    override suspend fun exportExperimentRunsToCsv(type: ExperimentType, destinationPath: String): Int =
        withContext(Dispatchers.IO) {
            val runs = experimentDao.getByType(type.name)
            csvExporter.write(
                destinationPath,
                header = listOf(
                    "id", "experimentType", "configLabel", "timestampEpochMs", "sampleSize",
                    "meanLatencyMs", "sdLatencyMs", "meanCpuPercent", "meanMemoryMb", "dropouts", "integrityScore"
                ),
                rows = runs.map {
                    listOf(
                        it.id, it.experimentType, it.configLabel, it.timestampEpochMs, it.sampleSize,
                        it.meanLatencyMs, it.sdLatencyMs, it.meanCpuPercent, it.meanMemoryMb, it.dropouts, it.integrityScore
                    )
                }
            )
        }

    private fun PlaybackSession.toEntity() = PlaybackSessionEntity(
        id = id,
        trackId = trackId,
        dacProfileId = dacProfileId,
        engineType = engineType.name,
        startEpochMs = startEpochMs,
        endEpochMs = endEpochMs,
        integrityScore = integrityScore,
        avgLatencyMs = avgLatencyMs,
        avgCpuPercent = avgCpuPercent,
        avgMemoryMb = avgMemoryMb,
        dropoutCount = dropoutCount,
        bufferSizeBytes = bufferSizeBytes,
        verifiedBitPerfect = verifiedBitPerfect
    )

    private fun PlaybackSessionEntity.toDomain() = PlaybackSession(
        id = id,
        trackId = trackId,
        dacProfileId = dacProfileId,
        engineType = EngineType.valueOf(engineType),
        startEpochMs = startEpochMs,
        endEpochMs = endEpochMs,
        integrityScore = integrityScore,
        avgLatencyMs = avgLatencyMs,
        avgCpuPercent = avgCpuPercent,
        avgMemoryMb = avgMemoryMb,
        dropoutCount = dropoutCount,
        bufferSizeBytes = bufferSizeBytes,
        verifiedBitPerfect = verifiedBitPerfect
    )

    private fun ExperimentRunEntity.toDomain() = ExperimentRun(
        id = id,
        experimentType = ExperimentType.valueOf(experimentType),
        configLabel = configLabel,
        timestampEpochMs = timestampEpochMs,
        sampleSize = sampleSize,
        meanLatencyMs = meanLatencyMs,
        sdLatencyMs = sdLatencyMs,
        meanCpuPercent = meanCpuPercent,
        meanMemoryMb = meanMemoryMb,
        dropouts = dropouts,
        integrityScore = integrityScore
    )
}
