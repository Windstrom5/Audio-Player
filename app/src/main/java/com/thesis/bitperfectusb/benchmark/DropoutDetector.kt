package com.thesis.bitperfectusb.benchmark

import java.util.concurrent.atomic.AtomicInteger

/**
 * Aggregates underflow/dropout events reported by the active PlaybackEngine
 * (Section 3.7, DropoutDetector). The engines themselves detect the real
 * underrun (AudioTrack.getUnderrunCount) or isochronous queue-failure events;
 * this class just accumulates and exposes them for benchmarking and the
 * validity-threat report.
 */
class DropoutDetector {

    private val total = AtomicInteger(0)

    fun onDropout() {
        total.incrementAndGet()
    }

    fun totalDropouts(): Int = total.get()

    fun reset() = total.set(0)
}
