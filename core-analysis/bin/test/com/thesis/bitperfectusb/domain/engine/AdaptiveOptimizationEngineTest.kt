package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.BufferRecommendation
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveOptimizationEngineTest {

    private val engine = AdaptiveOptimizationEngine()

    @Test
    fun `dropouts trigger a doubled buffer recommendation`() {
        val result = engine.tick(dropoutsSinceLastTick = 2, currentLatencyMs = 12.0, currentBufferSizeBytes = 4096)
        assertEquals(BufferRecommendation.INCREASE_FOR_STABILITY, result.recommendation)
        assertEquals(8192, result.recommendedBufferSizeBytes)
        assertEquals(90.0, result.stabilityPercentage, 0.001) // 100 - 2*5
    }

    @Test
    fun `buffer growth is capped at 32768 bytes`() {
        val result = engine.tick(dropoutsSinceLastTick = 1, currentLatencyMs = 96.0, currentBufferSizeBytes = 32768)
        assertEquals(32768, result.recommendedBufferSizeBytes)
    }

    @Test
    fun `stable high latency triggers a halved buffer recommendation`() {
        val result = engine.tick(dropoutsSinceLastTick = 0, currentLatencyMs = 150.0, currentBufferSizeBytes = 8192)
        assertEquals(BufferRecommendation.DECREASE_FOR_LATENCY, result.recommendation)
        assertEquals(4096, result.recommendedBufferSizeBytes)
    }

    @Test
    fun `buffer shrink is floored at 1024 bytes`() {
        val result = engine.tick(dropoutsSinceLastTick = 0, currentLatencyMs = 150.0, currentBufferSizeBytes = 1024)
        assertEquals(BufferRecommendation.MAINTAIN, result.recommendation)
        assertEquals(1024, result.recommendedBufferSizeBytes)
    }

    @Test
    fun `stable low latency maintains the current buffer`() {
        val result = engine.tick(dropoutsSinceLastTick = 0, currentLatencyMs = 12.0, currentBufferSizeBytes = 4096)
        assertEquals(BufferRecommendation.MAINTAIN, result.recommendation)
        assertEquals(4096, result.recommendedBufferSizeBytes)
    }
}
