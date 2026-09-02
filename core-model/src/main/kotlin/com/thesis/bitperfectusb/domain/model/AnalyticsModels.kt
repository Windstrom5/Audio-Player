package com.thesis.bitperfectusb.domain.model

/** One point on a time-series chart (day-bucketed average). */
data class TrendPoint(val label: String, val value: Double)

/** Aggregate comparison of the two engines across every logged session, for the Analytics tab. */
data class EngineComparison(
    val engineType: EngineType,
    val sessionCount: Int,
    val avgIntegrityScore: Double,
    val avgLatencyMs: Double,
    val avgCpuPercent: Double,
    val avgMemoryMb: Double,
    val totalDropouts: Int
)

data class DacUsageBreakdown(
    val dacLabel: String,
    val sessionCount: Int,
    val avgIntegrityScore: Double
)

/**
 * Everything the Analytics dashboard needs, computed by AnalyticsEngine from the
 * full session/benchmark/experiment history stored in Room. This is the
 * "analytical value" layer on top of the raw thesis feature set: it turns individual
 * playback sessions and experiment runs into longitudinal insight.
 */
data class AnalyticsSummary(
    val totalSessions: Int,
    val bitPerfectSessionPercentage: Double,
    /** Sessions with a definitive checksum comparison (TransferVerifier), not just a format-matching claim. */
    val checksumVerifiedSessionCount: Int,
    /** Of the sessions with a definitive checksum comparison, how many verified bit-perfect. */
    val checksumVerifiedBitPerfectPercentage: Double?,
    val avgIntegrityScoreOverall: Double,
    val integrityScoreTrend: List<TrendPoint>,
    val latencyTrend: List<TrendPoint>,
    val dropoutTrend: List<TrendPoint>,
    val engineComparison: List<EngineComparison>,
    val dacUsageBreakdown: List<DacUsageBreakdown>,
    val mostStableBufferSizeBytes: Int?,
    val totalListeningTimeMs: Long,
    val insights: List<String>
)
