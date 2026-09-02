package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.BufferRecommendation
import com.thesis.bitperfectusb.domain.model.PlaybackConfig
import com.thesis.bitperfectusb.domain.model.PlaybackConfig.Companion.DEFAULT_BUFFER_BYTES
import com.thesis.bitperfectusb.domain.model.PlaybackConfig.Companion.LATENCY_HIGH_THRESHOLD_MS
import com.thesis.bitperfectusb.domain.model.PlaybackConfig.Companion.MAX_BUFFER_BYTES
import com.thesis.bitperfectusb.domain.model.PlaybackConfig.Companion.MIN_BUFFER_BYTES
import com.thesis.bitperfectusb.domain.model.StabilityMetrics
import kotlin.math.roundToInt

/**
 * Domain engine implementing the Chapter 3.6 feedback loop:
 *
 *   Stability % = 100 - (Dropouts * 5)
 *
 *   - dropouts > 0                          -> double buffer, cap at 32,768 B
 *   - stable AND latency > 100ms AND buffer > 1,024 B -> halve buffer
 *   - otherwise                             -> maintain
 *
 * This is a pure function of its inputs so it can be unit tested and re-used both
 * by the live PlaybackController and by ExperimentOrchestrator's Experiment D sweep.
 */
class AdaptiveOptimizationEngine {

    fun tick(
        dropoutsSinceLastTick: Int,
        currentLatencyMs: Double,
        currentBufferSizeBytes: Int
    ): StabilityMetrics {
        val stabilityPercentage = (100 - dropoutsSinceLastTick * 5).coerceIn(0, 100).toDouble()

        val (recommendation, newBufferSize) = when {
            dropoutsSinceLastTick > 0 -> {
                val doubled = (currentBufferSizeBytes * 2).coerceAtMost(MAX_BUFFER_BYTES)
                BufferRecommendation.INCREASE_FOR_STABILITY to doubled
            }
            stabilityPercentage >= 100.0 &&
                currentLatencyMs > LATENCY_HIGH_THRESHOLD_MS &&
                currentBufferSizeBytes > MIN_BUFFER_BYTES -> {
                val halved = (currentBufferSizeBytes / 2).coerceAtLeast(MIN_BUFFER_BYTES)
                BufferRecommendation.DECREASE_FOR_LATENCY to halved
            }
            else -> BufferRecommendation.MAINTAIN to currentBufferSizeBytes
        }

        return StabilityMetrics(
            dropouts = dropoutsSinceLastTick,
            stabilityPercentage = stabilityPercentage,
            currentLatencyMs = currentLatencyMs,
            recommendedBufferSizeBytes = newBufferSize,
            recommendation = recommendation
        )
    }

    /** Snaps an arbitrary byte count to the nearest supported power-of-two buffer size. */
    fun clampToValidBufferSize(bytes: Int): Int {
        val clamped = bytes.coerceIn(MIN_BUFFER_BYTES, MAX_BUFFER_BYTES)
        val power = (Math.log(clamped.toDouble()) / Math.log(2.0)).roundToInt()
        return (1 shl power).coerceIn(MIN_BUFFER_BYTES, MAX_BUFFER_BYTES)
    }

    fun defaultConfig(engineType: com.thesis.bitperfectusb.domain.model.EngineType, format: com.thesis.bitperfectusb.domain.model.PcmFormat) =
        PlaybackConfig(engineType, format, DEFAULT_BUFFER_BYTES)
}
