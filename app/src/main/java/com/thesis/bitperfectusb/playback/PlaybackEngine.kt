package com.thesis.bitperfectusb.playback

import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.TrafficLogEntry
import com.thesis.bitperfectusb.domain.model.VerificationSnapshot

/**
 * Common surface for both arms of Experiment A (Section 4.1): the standard
 * Android AudioTrack path and the custom USB-direct path. Keeping both behind
 * one interface is what lets ExperimentOrchestrator swap engines while holding
 * everything else constant — the architecture is the only independent variable.
 */
interface PlaybackEngine {
    fun start(
        decoder: AudioDecoder,
        format: PcmFormat,
        bufferSizeBytes: Int,
        onDropout: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit = {},
        /** REPLAYGAIN_TRACK_GAIN from the source file's Vorbis comments, in dB —
         *  see [FlacVorbisCommentReader]. Null when absent (most files) or not
         *  applicable (WAV). Only the AudioTrack engine ever applies this —
         *  UsbDirectPlaybackEngine doesn't reference this parameter at all,
         *  consistent with bit-perfect USB-direct playback never running any DSP. */
        replayGainDb: Float? = null,
        /** REPLAYGAIN_TRACK_PEAK from the same tag block — the Normalization
         *  toggle's data source, distinct from replayGainDb's perceptual gain. */
        trackPeak: Float? = null
    )

    fun stop()

    /** Applied live by the AdaptiveOptimizationEngine feedback loop (Section 3.6). */
    fun setBufferSize(bytes: Int)

    /**
     * A modeled latency estimate in milliseconds — see README "How measurements
     * work" for what is a real hardware/OS reading versus a documented model in
     * this app. Buffer fill time is real arithmetic; any fixed per-engine
     * overhead added on top represents typical published mixer/HAL latency
     * contributions, not a live round-trip timestamp measurement.
     */
    fun currentLatencyEstimateMs(): Double

    /** Elapsed playback position in milliseconds since [start], computed from
     *  actual bytes transferred (not a wall-clock timer, so it stays accurate
     *  across dropouts/stalls). Resets to 0 on every [start]; PlaybackController
     *  adds this to the position it started the session at to get an absolute
     *  track position — see PlaybackController's seek handling. */
    fun currentPositionMs(): Long

    /** Live checksum-based proof of transfer integrity — see [TransferVerifier]. */
    fun verificationSnapshot(): VerificationSnapshot

    /** Most recent isochronous packet events — see [com.thesis.bitperfectusb.usb.UsbTrafficLog].
     *  Defaults to empty: the AudioTrack engine has no isochronous packets to log, so it
     *  gets this for free rather than needing a meaningless stub implementation. */
    fun trafficLog(): List<TrafficLogEntry> = emptyList()

    val dropoutCount: Int
}
