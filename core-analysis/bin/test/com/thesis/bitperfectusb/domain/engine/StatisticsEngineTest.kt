package com.thesis.bitperfectusb.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StatisticsEngineTest {

    private val engine = StatisticsEngine()

    @Test
    fun `descriptive stats compute correct mean and stddev`() {
        val stats = engine.descriptiveStats(listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0))
        assertEquals(5.0, stats.mean, 0.001)
        assertEquals(2.1381, stats.stdDev, 0.001) // sample stddev, n-1 denominator
    }

    @Test
    fun `welch t-test detects an obvious difference as significant`() {
        val groupA = List(50) { 10.0 } // AudioTrack-like: consistently high latency
        val groupB = List(50) { 2.0 }  // USB-direct-like: consistently low latency
        val result = engine.welchTTest(groupA, groupB)
        assertTrue("Expected a significant result for two clearly separated groups", result.significant)
        assertTrue("p-value should be extremely small", result.pValue < 0.0001)
    }

    @Test
    fun `welch t-test does not flag identical groups as significant`() {
        val groupA = List(30) { 5.0 }
        val groupB = List(30) { 5.0 }
        val result = engine.welchTTest(groupA, groupB)
        assertFalse(result.significant)
    }

    @Test
    fun `one-way anova detects an obvious group difference`() {
        val groups = mapOf(
            "44100hz" to List(20) { 4.8 },
            "96000hz" to List(20) { 7.9 },
            "192000hz" to List(20) { 14.8 }
        )
        val result = engine.oneWayAnova(groups)
        assertTrue(result.significant)
        assertTrue(result.pValue < 0.0001)
    }

    @Test
    fun `t distribution p-value is 1 at t=0`() {
        val p = engine.tDistributionTwoTailedPValue(0.0, 30.0)
        assertEquals(1.0, p, 0.01)
    }

    @Test
    fun `t distribution p-value approximates the classic 1_96 critical value at large df`() {
        // For large df, the two-tailed p-value at t=1.96 should be close to 0.05.
        val p = engine.tDistributionTwoTailedPValue(1.96, 1000.0)
        assertTrue("Expected p close to 0.05, got $p", abs(p - 0.05) < 0.01)
    }
}
