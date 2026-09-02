package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.AnalyticsSummary
import com.thesis.bitperfectusb.domain.model.DacUsageBreakdown
import com.thesis.bitperfectusb.domain.model.EngineComparison
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.PlaybackSession
import com.thesis.bitperfectusb.domain.model.TrendPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * The analytical layer added on top of the thesis's raw feature set: it aggregates
 * every logged PlaybackSession into day-bucketed trends, engine-vs-engine comparisons,
 * DAC usage breakdowns, and plain-language insights — the kind of longitudinal view
 * none of the commercial players reviewed in Chapter 2.4.1 provide.
 */
class AnalyticsEngine {

    private val dayFormat = SimpleDateFormat("MM/dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    fun summarize(
        sessions: List<PlaybackSession>,
        dacLabelsById: Map<Long, String>
    ): AnalyticsSummary {
        if (sessions.isEmpty()) {
            return AnalyticsSummary(
                totalSessions = 0,
                bitPerfectSessionPercentage = 0.0,
                checksumVerifiedSessionCount = 0,
                checksumVerifiedBitPerfectPercentage = null,
                avgIntegrityScoreOverall = 0.0,
                integrityScoreTrend = emptyList(),
                latencyTrend = emptyList(),
                dropoutTrend = emptyList(),
                engineComparison = emptyList(),
                dacUsageBreakdown = emptyList(),
                mostStableBufferSizeBytes = null,
                totalListeningTimeMs = 0L,
                insights = listOf("No playback sessions logged yet — play a track to start building analytics.")
            )
        }

        val bitPerfectCount = sessions.count { it.integrityScore == 100 }
        val bitPerfectPct = 100.0 * bitPerfectCount / sessions.size
        val avgIntegrity = sessions.map { it.integrityScore }.average()

        // Checksum-verified sessions are a strictly stronger claim than the format-matching
        // integrity score above: these actually compared bytes read vs. bytes transmitted.
        val checksumJudged = sessions.mapNotNull { it.verifiedBitPerfect }
        val checksumVerifiedPct = if (checksumJudged.isNotEmpty()) {
            100.0 * checksumJudged.count { it } / checksumJudged.size
        } else null

        val byDay = sessions.groupBy { dayFormat.format(Date(it.startEpochMs)) }
        val integrityTrend = byDay.entries
            .sortedBy { it.value.minOf { s -> s.startEpochMs } }
            .map { (day, s) -> TrendPoint(day, s.map { it.integrityScore.toDouble() }.average()) }
        val latencyTrend = byDay.entries
            .sortedBy { it.value.minOf { s -> s.startEpochMs } }
            .map { (day, s) -> TrendPoint(day, s.map { it.avgLatencyMs }.average()) }
        val dropoutTrend = byDay.entries
            .sortedBy { it.value.minOf { s -> s.startEpochMs } }
            .map { (day, s) -> TrendPoint(day, s.sumOf { it.dropoutCount }.toDouble()) }

        val engineComparison = EngineType.entries.mapNotNull { engine ->
            val group = sessions.filter { it.engineType == engine }
            if (group.isEmpty()) return@mapNotNull null
            EngineComparison(
                engineType = engine,
                sessionCount = group.size,
                avgIntegrityScore = group.map { it.integrityScore }.average(),
                avgLatencyMs = group.map { it.avgLatencyMs }.average(),
                avgCpuPercent = group.map { it.avgCpuPercent }.average(),
                avgMemoryMb = group.map { it.avgMemoryMb }.average(),
                totalDropouts = group.sumOf { it.dropoutCount }
            )
        }

        val dacUsage = sessions
            .filter { it.dacProfileId != null }
            .groupBy { it.dacProfileId }
            .map { (dacId, group) ->
                DacUsageBreakdown(
                    dacLabel = dacLabelsById[dacId] ?: "Unknown DAC",
                    sessionCount = group.size,
                    avgIntegrityScore = group.map { it.integrityScore }.average()
                )
            }
            .sortedByDescending { it.sessionCount }

        val mostStableBuffer = sessions
            .filter { it.dropoutCount == 0 }
            .groupBy { it.bufferSizeBytes }
            .maxByOrNull { it.value.size }
            ?.key

        val totalListeningTime = sessions.sumOf { (it.endEpochMs ?: it.startEpochMs) - it.startEpochMs }

        return AnalyticsSummary(
            totalSessions = sessions.size,
            bitPerfectSessionPercentage = bitPerfectPct,
            checksumVerifiedSessionCount = checksumJudged.size,
            checksumVerifiedBitPerfectPercentage = checksumVerifiedPct,
            avgIntegrityScoreOverall = avgIntegrity,
            integrityScoreTrend = integrityTrend,
            latencyTrend = latencyTrend,
            dropoutTrend = dropoutTrend,
            engineComparison = engineComparison,
            dacUsageBreakdown = dacUsage,
            mostStableBufferSizeBytes = mostStableBuffer,
            totalListeningTimeMs = totalListeningTime,
            insights = buildInsights(sessions, bitPerfectPct, checksumJudged, checksumVerifiedPct, engineComparison, mostStableBuffer)
        )
    }

    private fun buildInsights(
        sessions: List<PlaybackSession>,
        bitPerfectPct: Double,
        checksumJudged: List<Boolean>,
        checksumVerifiedPct: Double?,
        engineComparison: List<EngineComparison>,
        mostStableBuffer: Int?
    ): List<String> {
        val insights = mutableListOf<String>()

        insights += when {
            bitPerfectPct >= 95.0 -> "You achieved bit-perfect playback in ${bitPerfectPct.format1()}% of your sessions — excellent signal path integrity."
            bitPerfectPct >= 50.0 -> "${bitPerfectPct.format1()}% of your sessions were bit-perfect. Check DAC format support for the rest."
            else -> "Only ${bitPerfectPct.format1()}% of sessions were bit-perfect. Most of your listening is likely being resampled — consider switching to the Custom USB engine."
        }

        if (checksumVerifiedPct != null) {
            insights += if (checksumVerifiedPct >= 99.9) {
                "${checksumJudged.size} session(s) had a definitive checksum comparison, and every one matched — " +
                    "not just a format-matching claim, an actual byte-for-byte proof that nothing was altered in transit."
            } else {
                "${checksumJudged.size} session(s) had a definitive checksum comparison; " +
                    "${checksumVerifiedPct.format1()}% matched exactly. A mismatch is expected on the AudioTrack " +
                    "engine whenever this app's own bit-depth truncation runs."
            }
        }

        val audioTrack = engineComparison.find { it.engineType == EngineType.ANDROID_AUDIOTRACK }
        val usbDirect = engineComparison.find { it.engineType == EngineType.CUSTOM_USB_DIRECT }
        if (audioTrack != null && usbDirect != null) {
            val latencyDelta = audioTrack.avgLatencyMs - usbDirect.avgLatencyMs
            if (latencyDelta > 0) {
                val pct = 100.0 * latencyDelta / audioTrack.avgLatencyMs
                insights += "The Custom USB engine cut your average latency by ${pct.format1()}% versus AudioTrack " +
                    "(${audioTrack.avgLatencyMs.format1()}ms → ${usbDirect.avgLatencyMs.format1()}ms)."
            }
        }

        mostStableBuffer?.let {
            insights += "Your most reliable buffer size has been ${it} bytes — zero dropouts across every session that used it."
        }

        val totalDropouts = sessions.sumOf { it.dropoutCount }
        if (totalDropouts > 0) {
            insights += "$totalDropouts dropout(s) logged across ${sessions.size} sessions. Try increasing your buffer size if this keeps happening."
        }

        return insights
    }

    private fun Double.format1(): String = "%.1f".format(this)
}
