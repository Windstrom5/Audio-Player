package com.thesis.bitperfectusb.domain.model

/** One 500ms tick captured by BenchmarkRunner (Section 3.7). */
data class BenchmarkSample(
    val timestampMs: Long,
    val cpuPercent: Double,
    val memoryMb: Double,
    val latencyMs: Double,
    val cumulativeDropouts: Int,
    val engineType: EngineType
)

/** A completed listening/benchmark session, the backbone of the Analytics dashboard. */
data class PlaybackSession(
    val id: Long = 0L,
    val trackId: Long?,
    val dacProfileId: Long?,
    val engineType: EngineType,
    val startEpochMs: Long,
    val endEpochMs: Long?,
    val integrityScore: Int,
    val avgLatencyMs: Double,
    val avgCpuPercent: Double,
    val avgMemoryMb: Double,
    val dropoutCount: Int,
    val bufferSizeBytes: Int,
    /** Final checksum-verification verdict (TransferVerifier) at session end —
     *  null if never reached a comparable byte count (e.g. stopped very early). */
    val verifiedBitPerfect: Boolean? = null
)

/** Descriptive statistics for a single numeric sample set. */
data class DescriptiveStats(
    val n: Int,
    val mean: Double,
    val stdDev: Double,
    val ci95Low: Double,
    val ci95High: Double
)

/** Welch's two-sample t-test output (used by Experiment A, Section 4.1.2). */
data class WelchTTestResult(
    val tStatistic: Double,
    val degreesOfFreedom: Double,
    val pValue: Double,
    val groupA: DescriptiveStats,
    val groupB: DescriptiveStats,
    val significant: Boolean
)

/** One-way ANOVA output (used by Experiments B & D, Sections 4.2.2 / 4.4.2). */
data class AnovaResult(
    val fStatistic: Double,
    val dfBetween: Int,
    val dfWithin: Int,
    val pValue: Double,
    val significant: Boolean,
    val groupMeans: Map<String, Double>
)

/** A single labeled run inside an experiment (e.g. "exp_b_96000hz"). */
data class ExperimentRun(
    val id: Long = 0L,
    val experimentType: ExperimentType,
    val configLabel: String,
    val timestampEpochMs: Long,
    val sampleSize: Int,
    val meanLatencyMs: Double,
    val sdLatencyMs: Double,
    val meanCpuPercent: Double,
    val meanMemoryMb: Double,
    val dropouts: Int,
    val integrityScore: Int
)

/** The full result bundle for one experiment execution, including its statistical test(s). */
data class ExperimentReport(
    val experimentType: ExperimentType,
    val runs: List<ExperimentRun>,
    val welchLatencyResult: WelchTTestResult? = null,
    val welchCpuResult: WelchTTestResult? = null,
    val anovaResult: AnovaResult? = null
)

/**
 * One tested (sample rate, bit depth) combination for the "DAC Benchmark"
 * advanced feature — a live stress test of every format the attached DAC's
 * own descriptors declared supporting, not a repeat of the capability matrix.
 * [stable] means it streamed for the full test window with zero dropouts; a
 * DAC can legitimately declare support for a rate it can't actually sustain
 * over a given cable/host controller, and this is what catches that rather
 * than trusting the descriptor alone. See DacRateBenchmark.
 */
data class DacBenchmarkResult(
    val sampleRateHz: Int,
    val bitDepth: Int,
    val stable: Boolean,
    /** -1 if this format couldn't even be started (no matching altsetting, permission, etc). */
    val dropouts: Int,
    val latencyMs: Double,
    val bufferSizeBytes: Int,
    val note: String
)

/** Full result of one DacRateBenchmark run. [maxStable] is null if nothing tested came back clean. */
data class DacBenchmarkReport(
    val dacLabel: String,
    val results: List<DacBenchmarkResult>,
    val maxStable: DacBenchmarkResult?
)
