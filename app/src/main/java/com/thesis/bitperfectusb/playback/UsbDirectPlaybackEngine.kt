package com.thesis.bitperfectusb.playback

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.TrafficLogEntry
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.usb.ActivatedStreamingEndpoint
import com.thesis.bitperfectusb.usb.UsbDacManager
import com.thesis.bitperfectusb.usb.UsbIsochronousAudioStreamer
import com.thesis.bitperfectusb.usb.UsbTrafficLog
import java.util.concurrent.atomic.AtomicInteger

/**
 * The Chapter 4.1 "treatment" path: raw decoder bytes go straight to the DAC's
 * isochronous OUT endpoint via [UsbIsochronousAudioStreamer], completely
 * bypassing AudioFlinger, the Audio HAL, and ALSA (Section 3.4). No resampling,
 * no bit-depth conversion, no mixing — whatever the decoder produced is exactly
 * what reaches the wire, on the exact alternate setting matching that format,
 * with the DAC's clock explicitly switched to match before streaming starts.
 *
 * [activated] must already reflect the alternate setting selected for the
 * source's exact format — see UsbDacManager.activateStreamingOption. This
 * class owns releasing it back to idle on [stop].
 *
 * A [TransferVerifier] checksums bytes at both boundaries — as read from the
 * decoder, and as actually queued to the USB endpoint — so "bit-perfect" here
 * is checkable proof, not just a claim (see [PlaybackEngine.verificationSnapshot]).
 */
class UsbDirectPlaybackEngine(
    private val usbDacManager: UsbDacManager,
    private val device: UsbDevice,
    private val connection: UsbDeviceConnection,
    private val activated: ActivatedStreamingEndpoint,
    /** User-tunable via Settings > USB Buffering Depth — see UsbIsochronousAudioStreamer. */
    private val requestPoolSize: Int = 4,
    /** User-tunable via Settings > USB Transfer Strategy — see UsbTransferStrategy. */
    private val transferStrategy: UsbTransferStrategy = UsbTransferStrategy.PIPELINED
) : PlaybackEngine {

    private var streamer: UsbIsochronousAudioStreamer? = null
    private var bufferSizeBytes = 4096
    private var bytesPerSecond = 1
    private val dropouts = AtomicInteger(0)
    private var verifier = TransferVerifier()
    private var trafficLogBuffer = UsbTrafficLog()

    override val dropoutCount: Int get() = dropouts.get()

    override fun start(
        decoder: AudioDecoder,
        format: PcmFormat,
        bufferSizeBytes: Int,
        onDropout: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit,
        // Deliberately unused — see the interface doc on why USB-direct never
        // applies ReplayGain/Normalization or any other gain adjustment.
        replayGainDb: Float?,
        trackPeak: Float?
    ) {
        this.bufferSizeBytes = bufferSizeBytes
        bytesPerSecond = format.sampleRateHz * format.channels * (format.bitDepth / 8)

        // The DAC's declared per-sample container (activated.option.containerBytes) can be
        // wider than the source's own tight packing — e.g. 24-bit audio sent to a DAC whose
        // altsetting declares a 4-byte subslot ("24-in-32"), a common real-world UAC2
        // configuration (see DacCapabilityAnalyzer). PcmContainerPacker repacks decoder
        // output to match before it reaches the wire; TransferVerifier is told the same two
        // sizes so it strips that padding back out when comparing against source bytes,
        // rather than misreporting a correctly-repacked stream as a checksum mismatch.
        val sourceBytesPerSample = (format.bitDepth / 8).coerceAtLeast(1)
        val containerBytes = activated.option.containerBytes.takeIf { it > 0 } ?: sourceBytesPerSample
        val packer = PcmContainerPacker(sourceBytesPerSample, containerBytes, format.channels)
        verifier = TransferVerifier(sourceBytesPerSample, containerBytes)
        verifier.reset()
        trafficLogBuffer = UsbTrafficLog()

        val newStreamer = UsbIsochronousAudioStreamer(
            connection = connection,
            usbInterface = activated.usbInterface,
            endpoint = activated.dataEndpoint,
            feedbackEndpoint = activated.feedbackEndpoint,
            option = activated.option,
            format = format,
            requestPoolSize = requestPoolSize,
            transferStrategy = transferStrategy
        )
        streamer = newStreamer

        var completionSignaled = false
        newStreamer.start(
            pcmSource = {
                val chunk = ByteArray(this.bufferSizeBytes)
                val n = decoder.read(chunk)
                val raw: ByteArray? = when {
                    n == -1 -> {
                        if (!completionSignaled) {
                            completionSignaled = true
                            onCompletion()
                        }
                        null
                    }
                    n == chunk.size -> chunk
                    n <= 0 -> ByteArray(0) // transient stall, keep the pipe primed without stalling the isoc loop
                    else -> chunk.copyOf(n) // final partial chunk of the file
                }
                // Record every byte this app read from the source, before any repacking —
                // the other half of the verification boundary, always the decoder's own
                // tight packing regardless of what the DAC's wire container needs.
                if (raw != null && raw.isNotEmpty()) verifier.recordSourceBytes(raw, raw.size)
                when {
                    raw == null -> null
                    raw.isEmpty() -> raw
                    else -> packer.repack(raw)
                }
            },
            onDropout = {
                dropouts.incrementAndGet()
                trafficLogBuffer.recordDropout()
                onDropout()
            },
            onBytesTransmitted = { bytes, length ->
                // Only successfully-queued packets land here (Section: UsbIsochronousAudioStreamer) —
                // this is genuinely what reached the wire, not just what was attempted.
                verifier.recordTransmittedBytes(bytes, length)
                trafficLogBuffer.recordSuccess(length)
            },
            onError = onError
        )
    }

    override fun stop() {
        streamer?.stop()
        streamer = null
        usbDacManager.deactivateStreamingOption(device, connection, activated)
        connection.close()
    }

    override fun setBufferSize(bytes: Int) {
        bufferSizeBytes = bytes
    }

    override fun currentLatencyEstimateMs(): Double =
        if (bytesPerSecond > 0) (bufferSizeBytes.toDouble() / bytesPerSecond) * 1000.0 else 0.0

    override fun currentPositionMs(): Long {
        if (bytesPerSecond <= 0) return 0L
        // Source bytes (not transmitted) are the right measure here: this engine
        // does no bit-depth conversion or resampling (see the class doc comment),
        // so bytes read from the decoder correspond directly to playback time —
        // and using "read" rather than "successfully transmitted over USB" means
        // position keeps advancing correctly even through a transient dropout.
        return (verifier.snapshot().sourceBytes / bytesPerSecond.toDouble() * 1000.0).toLong()
    }

    override fun verificationSnapshot() = verifier.snapshot()

    override fun trafficLog(): List<TrafficLogEntry> = trafficLogBuffer.snapshot()
}
