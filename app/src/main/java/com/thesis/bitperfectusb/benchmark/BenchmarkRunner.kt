package com.thesis.bitperfectusb.benchmark

import android.content.Context

/**
 * Coordinates the CPU, memory, latency, and dropout monitors and exposes a
 * simple per-tick sampling surface for PlaybackController and
 * ExperimentOrchestrator (Section 3.7, BenchmarkRunner). PlaybackController
 * drives the actual 500ms tick loop; this class just owns the monitor
 * instances so they aren't duplicated across the app.
 */
class BenchmarkRunner(context: Context) {

    private val cpuMonitor = CpuMonitor()
    private val memoryMonitor = MemoryMonitor(context)
    val latencyMonitor = LatencyMonitor()
    val dropoutDetector = DropoutDetector()

    fun sampleCpuPercent(): Double = cpuMonitor.samplePercent()

    fun sampleMemoryMb(): Double = memoryMonitor.sampleMb()

    fun resetForNewRun() {
        latencyMonitor.reset()
        dropoutDetector.reset()
    }
}
