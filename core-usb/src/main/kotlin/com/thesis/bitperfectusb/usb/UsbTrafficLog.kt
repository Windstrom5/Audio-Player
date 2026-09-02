package com.thesis.bitperfectusb.usb

import com.thesis.bitperfectusb.domain.model.TrafficLogEntry
import com.thesis.bitperfectusb.domain.model.TrafficStatus
import java.util.concurrent.atomic.AtomicLong

/**
 * Bounded ring buffer of the most recent isochronous packet events — the "USB
 * Traffic Logger" advanced feature (per-packet size, timestamp, and status),
 * distinct from the aggregate [UsbIsochronousAudioStreamer.dropoutCount] this
 * app already tracked. Owned per playback session by `UsbDirectPlaybackEngine`,
 * same lifecycle as `TransferVerifier`.
 *
 * [recordSuccess] and [recordDropout] are called from the real-time USB writer
 * thread (`usb-isoc-audio-writer`, `Thread.MAX_PRIORITY` — see
 * [UsbIsochronousAudioStreamer]'s class doc on why that thread can't afford
 * anything expensive), so both are O(1) with a single fixed-size array write
 * and no per-entry heap allocation beyond one small data class instance.
 * [snapshot] is called from the 500ms UI monitor tick instead, not the audio
 * thread, so its O(capacity) copy is not a real-time concern.
 */
class UsbTrafficLog(private val capacity: Int = DEFAULT_CAPACITY) {

    private val entries = arrayOfNulls<TrafficLogEntry>(capacity)
    private var nextIndex = 0
    private var filledCount = 0
    private val sequence = AtomicLong(0)
    private val startNanos = System.nanoTime()
    private var lastNanos = System.nanoTime()

    @Synchronized
    fun recordSuccess(sizeBytes: Int) {
        record(sizeBytes, TrafficStatus.OK)
    }

    @Synchronized
    fun recordDropout() {
        record(0, TrafficStatus.DROPPED)
    }

    private fun record(sizeBytes: Int, status: TrafficStatus) {
        val nowNanos = System.nanoTime()
        val seq = sequence.incrementAndGet()
        val elapsedMs = (nowNanos - startNanos) / 1_000_000
        val deltaUs = (nowNanos - lastNanos) / 1_000
        lastNanos = nowNanos
        
        entries[nextIndex] = TrafficLogEntry(seq, sizeBytes, elapsedMs, status, deltaUs)
        nextIndex = (nextIndex + 1) % capacity
        if (filledCount < capacity) filledCount++
    }

    /** Most recent entries first. Safe to call frequently — capacity is small
     *  and fixed, and this only allocates the returned list, not new entries. */
    @Synchronized
    fun snapshot(): List<TrafficLogEntry> {
        if (filledCount == 0) return emptyList()
        val result = ArrayList<TrafficLogEntry>(filledCount)
        var idx = (nextIndex - 1 + capacity) % capacity
        repeat(filledCount) {
            entries[idx]?.let { result.add(it) }
            idx = (idx - 1 + capacity) % capacity
        }
        return result
    }

    val totalPackets: Long get() = sequence.get()

    companion object {
        private const val DEFAULT_CAPACITY = 200
    }
}
