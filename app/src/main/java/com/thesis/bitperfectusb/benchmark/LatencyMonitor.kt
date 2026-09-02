package com.thesis.bitperfectusb.benchmark

import kotlin.math.sqrt

/**
 * Tracks a rolling window of latency samples to support the "High Latency
 * Jitter" validity-threat flag from Section 5.2.1: runs whose latency standard
 * deviation exceeds 20ms are flagged as likely affected by background
 * scheduling noise rather than the architecture actually under test.
 */
class LatencyMonitor {

    private val samples = mutableListOf<Double>()

    fun record(latencyMs: Double) {
        samples += latencyMs
    }

    fun currentStdDevMs(): Double {
        if (samples.size < 2) return 0.0
        val mean = samples.average()
        val variance = samples.sumOf { (it - mean) * (it - mean) } / (samples.size - 1)
        return sqrt(variance)
    }

    fun hasHighJitter(): Boolean = currentStdDevMs() > HIGH_JITTER_THRESHOLD_MS

    fun reset() = samples.clear()

    fun snapshot(): List<Double> = samples.toList()

    companion object {
        const val HIGH_JITTER_THRESHOLD_MS = 20.0
    }
}
