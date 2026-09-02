package com.thesis.bitperfectusb.usb

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbRequest
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.UsbStreamingOption
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Streams raw PCM bytes to a USB DAC's isochronous OUT endpoint using Android's
 * UsbRequest queueing API, entirely in user-space (Section 3.4: BufferManager /
 * PlaybackController). This is the component that actually bypasses AudioFlinger,
 * the Audio HAL, and ALSA — PCM bytes go from the decoder straight to the wire.
 *
 * Supports two genuinely different, equally bit-perfect transfer strategies
 * (Settings > USB Transfer Strategy — see [UsbTransferStrategy] for the
 * detailed tradeoff explanation). Both do the two things a naive "just queue
 * some bytes" implementation skips, and a real bit-perfect driver (UAPP-style)
 * can't:
 *
 * 1. **Tells the DAC what rate to actually run at** before any data flows
 *    ([setSampleRateOnDevice]) via the class-specific SET_CUR control transfer.
 * 2. **Sizes every packet with a fractional accumulator**
 *    ([IsochronousPacketScheduler]) instead of flat fixed-size chunks, since
 *    most sample rates don't divide evenly into a 1ms full-speed frame or
 *    125µs high-speed microframe.
 *
 * A note on reliability: Android's public isochronous transfer support is
 * best-effort and its real-world behavior varies by device, kernel, and USB
 * host controller — this is a platform characteristic, not something any
 * app can fully paper over from user-space (see Chapter 5.2.2). The feedback
 * endpoint (when present) is read as a best-effort drift *monitor* — a fully
 * closed-loop adaptive correction algorithm is one of the genuinely hardest
 * remaining pieces of a production-grade implementation; see the README.
 */
class UsbIsochronousAudioStreamer(
    private val connection: UsbDeviceConnection,
    private val usbInterface: UsbInterface,
    private val endpoint: UsbEndpoint,
    private val feedbackEndpoint: UsbEndpoint?,
    private val option: UsbStreamingOption,
    private val format: PcmFormat,
    /** N-buffering depth — how many isochronous requests stay queued ahead at
     *  once. Only used in PIPELINED mode; ignored in SYNCHRONOUS mode, which
     *  always uses exactly one request by design. User-tunable via
     *  Settings > USB Buffering Depth. */
    private val requestPoolSize: Int = DATA_REQUEST_POOL_SIZE,
    /** Which of the two bit-perfect transfer strategies to drive with — see class doc. */
    private val transferStrategy: UsbTransferStrategy = UsbTransferStrategy.PIPELINED
) {
    private val running = AtomicBoolean(false)
    private var workerThread: Thread? = null
    val dropoutCount = AtomicInteger(0)

    /** Best-effort raw feedback reading, exposed for diagnostics/logging — see [interpretFeedback]. */
    val lastRawFeedbackValue = AtomicInteger(0)

    /** True once the SET_CUR sample-rate control transfer has round-tripped successfully. */
    @Volatile var deviceRateConfirmed: Boolean = false
        private set

    /**
     * Starts the streaming loop on a dedicated max-priority thread.
     * [pcmSource] is pulled from repeatedly (in reasonably large chunks — this
     * streamer slices them into individual packet-sized pieces internally) until
     * it returns null (end of stream). [onDropout] fires whenever a queued
     * transfer fails or a completion times out. [onError] fires (and stops the
     * streamer) on any exception that would otherwise crash the process on this
     * background thread — decode edge cases, unexpected USB state, etc.
     */
    fun start(
        pcmSource: () -> ByteArray?,
        onDropout: () -> Unit,
        onBytesTransmitted: (ByteArray, Int) -> Unit = { _, _ -> },
        onError: (Throwable) -> Unit = {}
    ) {
        if (running.getAndSet(true)) return

        deviceRateConfirmed = setSampleRateOnDevice()

        // The DAC's declared per-sample container size (UsbStreamingOption.containerBytes)
        // is what's actually on the wire once PcmContainerPacker has repacked the source —
        // not format.bitDepth / 8, which is only the *source's* packing and can be narrower
        // than what this specific altsetting requires (see DacCapabilityAnalyzer / the class
        // doc on PcmContainerPacker). Falls back to the source's own packing only if a
        // legacy/malformed profile somehow left containerBytes unset.
        val effectiveContainerBytes = option.containerBytes.takeIf { it > 0 } ?: (format.bitDepth / 8)
        val frameSizeBytes = effectiveContainerBytes * format.channels
        val intervalsPerSecond = estimateIntervalsPerSecond()
        val scheduler = IsochronousPacketScheduler(format.sampleRateHz, frameSizeBytes, intervalsPerSecond)

        val claimed = try {
            connection.claimInterface(usbInterface, true)
        } catch (t: Throwable) {
            running.set(false)
            onError(t)
            return
        }
        if (!claimed) {
            running.set(false)
            onError(IllegalStateException("Failed to claim the USB interface for streaming."))
            return
        }

        workerThread = Thread({
            try {
                when (transferStrategy) {
                    UsbTransferStrategy.PIPELINED -> runPipelinedLoop(pcmSource, onDropout, onBytesTransmitted, scheduler)
                    UsbTransferStrategy.SYNCHRONOUS -> runSynchronousLoop(pcmSource, onDropout, onBytesTransmitted, scheduler)
                }
            } catch (t: Throwable) {
                onError(t)
            } finally {
                connection.releaseInterface(usbInterface)
                running.set(false)
            }
        }, "usb-isoc-audio-writer").apply {
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running.set(false)
        workerThread?.join(500)
        workerThread = null
    }

    // -------------------------------------------------------------------
    // Strategy 1: PIPELINED — N-buffered pool, absorbs scheduling jitter
    // -------------------------------------------------------------------

    private fun runPipelinedLoop(
        pcmSource: () -> ByteArray?,
        onDropout: () -> Unit,
        onBytesTransmitted: (ByteArray, Int) -> Unit,
        scheduler: IsochronousPacketScheduler
    ) {
        val intervalsPerSecond = estimateIntervalsPerSecond()
        val piController = UsbAsyncFeedbackPiController(format.sampleRateHz, intervalsPerSecond, option.isUac2)
        val packetSource = PacketSource(pcmSource)
        val dataRequestPool = (0 until requestPoolSize).map { UsbRequest().apply { initialize(connection, endpoint) } }
        val inFlightData = ArrayDeque<UsbRequest>()
        val feedbackRequest = feedbackEndpoint?.let { UsbRequest().apply { initialize(connection, it) } }
        val feedbackBuffer = ByteBuffer.allocateDirect(4)
        var feedbackInFlight = false
        var lastFeedbackQueueTimeNanos = 0L

        fun queueFeedbackIfDue(nowNanos: Long) {
            if (feedbackRequest == null || feedbackInFlight) return
            if (nowNanos - lastFeedbackQueueTimeNanos < FEEDBACK_POLL_INTERVAL_NANOS) return
            feedbackBuffer.clear()
            if (feedbackRequest.queue(feedbackBuffer)) {
                feedbackInFlight = true
                lastFeedbackQueueTimeNanos = nowNanos
            }
        }

        try {
            for (request in dataRequestPool) {
                val packet = packetSource.nextPacket(scheduler.nextPacketByteSize(piController.currentFrameOffset)) ?: break
                if (request.queue(ByteBuffer.wrap(packet))) {
                    inFlightData.addLast(request)
                    onBytesTransmitted(packet, packet.size)
                } else {
                    dropoutCount.incrementAndGet()
                    onDropout()
                }
            }
            queueFeedbackIfDue(System.nanoTime())

            while (running.get() && inFlightData.isNotEmpty()) {
                val completed = connection.requestWait(TRANSFER_TIMEOUT_MS.toLong())
                if (completed == null) {
                    dropoutCount.incrementAndGet()
                    onDropout()
                    continue
                }

                if (completed === feedbackRequest) {
                    feedbackInFlight = false
                    val raw = interpretFeedback(feedbackBuffer)
                    piController.processFeedbackRaw(raw)
                    queueFeedbackIfDue(System.nanoTime())
                    continue
                }

                inFlightData.remove(completed)
                val packet = packetSource.nextPacket(scheduler.nextPacketByteSize(piController.currentFrameOffset))
                if (packet == null) break

                if (completed.queue(ByteBuffer.wrap(packet))) {
                    inFlightData.addLast(completed)
                    onBytesTransmitted(packet, packet.size)
                } else {
                    dropoutCount.incrementAndGet()
                    onDropout()
                }
                queueFeedbackIfDue(System.nanoTime())
            }
        } finally {
            dataRequestPool.forEach { it.close() }
            feedbackRequest?.close()
        }
    }

    // -------------------------------------------------------------------
    // Strategy 2: SYNCHRONOUS — one request at a time, blocking queue/wait/requeue
    // -------------------------------------------------------------------

    private fun runSynchronousLoop(
        pcmSource: () -> ByteArray?,
        onDropout: () -> Unit,
        onBytesTransmitted: (ByteArray, Int) -> Unit,
        scheduler: IsochronousPacketScheduler
    ) {
        val packetSource = PacketSource(pcmSource)
        val dataRequest = UsbRequest().apply { initialize(connection, endpoint) }
        val feedbackRequest = feedbackEndpoint?.let { UsbRequest().apply { initialize(connection, it) } }
        val feedbackBuffer = ByteBuffer.allocateDirect(4)
        var feedbackInFlight = false
        var lastFeedbackQueueTimeNanos = 0L

        fun queueFeedbackIfDue(nowNanos: Long) {
            if (feedbackRequest == null || feedbackInFlight) return
            if (nowNanos - lastFeedbackQueueTimeNanos < FEEDBACK_POLL_INTERVAL_NANOS) return
            feedbackBuffer.clear()
            if (feedbackRequest.queue(feedbackBuffer)) {
                feedbackInFlight = true
                lastFeedbackQueueTimeNanos = nowNanos
            }
        }

        try {
            while (running.get()) {
                val packet = packetSource.nextPacket(scheduler.nextPacketByteSize()) ?: break

                if (!dataRequest.queue(ByteBuffer.wrap(packet))) {
                    dropoutCount.incrementAndGet()
                    onDropout()
                    continue // try the next packet rather than getting stuck on this one
                }
                onBytesTransmitted(packet, packet.size)
                queueFeedbackIfDue(System.nanoTime())

                // Block until this exact request (or the feedback request) completes —
                // the request object can't be reused for the next packet until it does.
                var dataCompleted = false
                while (!dataCompleted && running.get()) {
                    val completed = connection.requestWait(TRANSFER_TIMEOUT_MS.toLong())
                    if (completed == null) {
                        dropoutCount.incrementAndGet()
                        onDropout()
                        break // give up waiting on this one; move to the next packet
                    }
                    if (completed === feedbackRequest) {
                        feedbackInFlight = false
                        interpretFeedback(feedbackBuffer)
                        continue
                    }
                    if (completed === dataRequest) dataCompleted = true
                }
            }
        } finally {
            dataRequest.close()
            feedbackRequest?.close()
        }
    }

    // -------------------------------------------------------------------
    // Shared packet-building buffer — re-chunks decoder reads into exact
    // per-interval packet sizes without altering byte content or order.
    // -------------------------------------------------------------------

    private class PacketSource(private val pcmSource: () -> ByteArray?) {
        private val pendingChunks = ArrayDeque<ByteArray>()
        private var pendingOffset = 0
        private var sourceExhausted = false

        fun nextPacket(sizeBytes: Int): ByteArray? {
            if (sizeBytes <= 0) return ByteArray(0)
            val out = ByteArray(sizeBytes)
            var written = 0
            while (written < sizeBytes) {
                if (pendingChunks.isEmpty()) {
                    if (sourceExhausted) break
                    val chunk = pcmSource()
                    if (chunk == null || chunk.isEmpty()) {
                        sourceExhausted = true
                        break
                    }
                    pendingChunks.addLast(chunk)
                    pendingOffset = 0
                }
                val head = pendingChunks.first()
                val available = head.size - pendingOffset
                val toCopy = minOf(available, sizeBytes - written)
                System.arraycopy(head, pendingOffset, out, written, toCopy)
                written += toCopy
                pendingOffset += toCopy
                if (pendingOffset >= head.size) {
                    pendingChunks.removeFirst()
                    pendingOffset = 0
                }
            }
            if (written == 0) return null
            return if (written == sizeBytes) out else out.copyOf(written)
        }
    }

    // -------------------------------------------------------------------
    // Device rate configuration
    // -------------------------------------------------------------------

    /**
     * Issues the SET_CUR sampling-frequency control transfer so the DAC's own
     * clock actually switches to the source's native rate before data starts
     * flowing. Returns false (non-fatal — streaming still proceeds) if the
     * device doesn't acknowledge the request, since some firmware auto-detects
     * rate from the incoming stream instead of honoring this control transfer.
     */
    private fun setSampleRateOnDevice(): Boolean {
        val rate = format.sampleRateHz
        return try {
            if (option.isUac2) {
                val clockId = option.clockSourceId ?: return false
                val acIface = option.acInterfaceNumber ?: return false
                val data = byteArrayOf(
                    (rate and 0xFF).toByte(),
                    ((rate shr 8) and 0xFF).toByte(),
                    ((rate shr 16) and 0xFF).toByte(),
                    ((rate shr 24) and 0xFF).toByte()
                )
                val wValue = UsbAudioConstants.CS_SAM_FREQ_CONTROL shl 8
                val wIndex = (clockId shl 8) or acIface
                val result = connection.controlTransfer(
                    UsbAudioConstants.REQTYPE_CLASS_INTERFACE_OUT, UsbAudioConstants.REQUEST_CUR,
                    wValue, wIndex, data, data.size, 500
                )
                result == data.size
            } else {
                val data = byteArrayOf(
                    (rate and 0xFF).toByte(),
                    ((rate shr 8) and 0xFF).toByte(),
                    ((rate shr 16) and 0xFF).toByte()
                )
                val wValue = UsbAudioConstants.EP_SAMPLING_FREQ_CONTROL shl 8
                val result = connection.controlTransfer(
                    UsbAudioConstants.REQTYPE_CLASS_ENDPOINT_OUT, UsbAudioConstants.REQUEST_CUR,
                    wValue, option.endpointAddress, data, data.size, 500
                )
                result == data.size
            }
        } catch (t: Throwable) {
            false
        }
    }

    private fun estimateIntervalsPerSecond(): Int {
        // Android has no direct public API to query negotiated USB bus speed.
        // High-speed isochronous endpoints run at 125µs microframes (8000/sec);
        // full-speed ones at 1ms frames (1000/sec). Full-speed isochronous
        // packets are capped at 1023 bytes/interval, so if this format needs
        // more than that per interval at 1000/sec, the endpoint must actually
        // be operating high-speed regardless of what we can directly query.
        // Uses the DAC's actual container size, not just the source's bit depth —
        // a wider container (see PcmContainerPacker) means more bytes/sec on the
        // wire than the source alone would suggest.
        val containerBytes = option.containerBytes.takeIf { it > 0 } ?: (format.bitDepth / 8)
        val bytesPerSecond = format.sampleRateHz * format.channels * containerBytes
        val requiredPerFullSpeedFrame = bytesPerSecond / 1000
        return if (requiredPerFullSpeedFrame > 1023) 8000 else 1000
    }

    /**
     * Best-effort interpretation of a feedback endpoint reading. UAC1 feedback
     * is a 3-byte 10.14 fixed-point value; UAC2 commonly uses a 4-byte 16.16
     * fixed-point value — which exact variant a given device sends is spec-path
     * dependent enough that this stores the raw integer for diagnostics/logging
     * rather than asserting one fixed-point interpretation and silently
     * mis-scaling on a device that uses the other.
     */
    private fun interpretFeedback(buffer: ByteBuffer): Int {
        buffer.rewind()
        var raw = 0
        val byteCount = minOf(buffer.remaining(), 4)
        for (i in 0 until byteCount) {
            raw = raw or ((buffer.get(i).toInt() and 0xFF) shl (8 * i))
        }
        lastRawFeedbackValue.set(raw)
        return raw
    }

    companion object {
        private const val DATA_REQUEST_POOL_SIZE = 4
        private const val TRANSFER_TIMEOUT_MS = 200
        private val FEEDBACK_POLL_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1)
    }
}
