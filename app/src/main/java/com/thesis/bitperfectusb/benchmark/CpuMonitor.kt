package com.thesis.bitperfectusb.benchmark

import java.io.RandomAccessFile

/**
 * Reads this process's own CPU utilization from /proc/self/stat (Section 3.7,
 * CpuMonitor). Reading a process's own /proc entry doesn't require any special
 * permission on Android — the historical /proc restrictions apply to reading
 * *other* processes' entries, not your own.
 *
 * Returns a percentage of total system CPU capacity (normalized by core count),
 * matching how "CPU %" is reported throughout Chapter 4 (values well under 100%
 * even on multi-core devices).
 */
class CpuMonitor {

    private var lastTotalTicks: Long = 0
    private var lastSampleTimeNanos: Long = 0
    private val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

    fun samplePercent(): Double {
        val ticks = readProcessTicks() ?: return 0.0
        val totalTicks = ticks.first + ticks.second
        val now = System.nanoTime()

        if (lastSampleTimeNanos == 0L) {
            lastTotalTicks = totalTicks
            lastSampleTimeNanos = now
            return 0.0
        }

        val elapsedSeconds = (now - lastSampleTimeNanos) / 1_000_000_000.0
        val deltaTicks = (totalTicks - lastTotalTicks).coerceAtLeast(0)
        lastTotalTicks = totalTicks
        lastSampleTimeNanos = now

        if (elapsedSeconds <= 0.0) return 0.0
        val deltaSeconds = deltaTicks.toDouble() / CLOCK_TICKS_PER_SECOND
        return ((deltaSeconds / elapsedSeconds) / coreCount * 100.0).coerceIn(0.0, 100.0)
    }

    private fun readProcessTicks(): Pair<Long, Long>? = try {
        RandomAccessFile("/proc/self/stat", "r").use { file ->
            val line = file.readLine()
            // "comm" (field 2) is parenthesized and may itself contain spaces, so
            // split from just after the closing ')' to keep field alignment simple.
            val afterName = line.substringAfterLast(')').trim()
            val fields = afterName.split(" ")
            // Index 0 here == original field 3 (state). utime is field 14 -> index 11; stime is field 15 -> index 12.
            val utime = fields[11].toLong()
            val stime = fields[12].toLong()
            utime to stime
        }
    } catch (t: Throwable) {
        null
    }

    companion object {
        private const val CLOCK_TICKS_PER_SECOND = 100L // USER_HZ — standard on Android/Linux
    }
}
