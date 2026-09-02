package com.thesis.bitperfectusb.domain.model

/** Mutable playback parameters the AdaptiveOptimizationEngine is allowed to tune. */
data class PlaybackConfig(
    val engineType: EngineType,
    val format: PcmFormat,
    val bufferSizeBytes: Int
) {
    companion object {
        const val MIN_BUFFER_BYTES = 1_024
        const val MAX_BUFFER_BYTES = 32_768
        const val DEFAULT_BUFFER_BYTES = 4_096
        const val LATENCY_HIGH_THRESHOLD_MS = 100
    }
}

/** Result of one AdaptiveOptimizationEngine tick (Section 3.6). */
data class StabilityMetrics(
    val dropouts: Int,
    val stabilityPercentage: Double,
    val currentLatencyMs: Double,
    val recommendedBufferSizeBytes: Int,
    val recommendation: BufferRecommendation
)

enum class BufferRecommendation { INCREASE_FOR_STABILITY, DECREASE_FOR_LATENCY, MAINTAIN }

/**
 * Checksum-based proof of transfer integrity (an extension of the Section 3.5
 * integrity model): two independent running checksums, one over bytes read
 * from the decoder ("source") and one over bytes actually handed to the
 * transport ("transmitted"). See TransferVerifier for how these are computed.
 *
 * @param verified null = not yet comparable (byte counts still diverge); true =
 *                 checksums matched at equal byte counts, proven bit-perfect so
 *                 far; false = checksums differed, proven modification occurred.
 */
data class VerificationSnapshot(
    val sourceBytes: Long,
    val transmittedBytes: Long,
    val sourceChecksum: Long,
    val transmittedChecksum: Long,
    val verified: Boolean?,
    val activeBitMask: Int = 0
)

/** One isochronous packet's outcome — the USB Traffic Logger advanced feature
 *  (per-packet size/timestamp/status), distinct from the aggregate dropoutCount
 *  this app already tracked. Only ever populated on the USB-direct engine; see
 *  [PlaybackEngine.trafficLog]'s default (empty) implementation for AudioTrack. */
data class TrafficLogEntry(
    val sequenceNumber: Long,
    val sizeBytes: Int,
    /** Milliseconds since this streaming session started (not wall-clock time). */
    val elapsedMs: Long,
    val status: TrafficStatus,
    /** Inter-packet arrival delta jitter in microseconds relative to expected interval. */
    val jitterUs: Long = 0L
)

enum class TrafficStatus { OK, DROPPED }

/** One 500ms tick's worth of live telemetry, kept in a rolling window for the real-time chart. */
data class LiveMetricPoint(
    val elapsedSeconds: Double,
    val latencyMs: Double,
    val cpuPercent: Double
)

/** Live playback engine state exposed to the UI (Player + Integrity dashboard). */
data class PlaybackState(
    val isPlaying: Boolean = false,
    /** True when playback was explicitly paused (as opposed to stopped with no
     *  track loaded) — currentTrack/positionMs stay at the paused point so
     *  resume() can restart from exactly there. See PlaybackController.pause. */
    val isPaused: Boolean = false,
    val currentTrack: AudioTrackModel? = null,
    val positionMs: Long = 0L,
    val engineType: EngineType = EngineType.CUSTOM_USB_DIRECT,
    val bufferSizeBytes: Int = PlaybackConfig.DEFAULT_BUFFER_BYTES,
    val integrity: IntegrityResult? = null,
    val stability: StabilityMetrics? = null,
    val liveLatencyMs: Double = 0.0,
    val liveCpuPercent: Double = 0.0,
    val liveMemoryMb: Double = 0.0,
    /** Last ~60 ticks (~30s at the 500ms tick interval) for the live-updating chart. */
    val recentHistory: List<LiveMetricPoint> = emptyList(),
    /** Live checksum-based proof of transfer integrity — see TransferVerifier. */
    val verification: VerificationSnapshot? = null,
    /** Most recent packet events — see TrafficLogEntry. Empty on the AudioTrack engine. */
    val trafficLog: List<TrafficLogEntry> = emptyList(),
    /** Source file size in bytes, when known (null for the synthetic tone used by experiments). */
    val totalBytesExpected: Long? = null,
    /** Instantaneous transmitted-bytes rate over the last tick, not a session average. */
    val transferRateBytesPerSec: Double = 0.0,
    /** Official TT Dynamic Range Meter integer score (e.g. 14 for DR14). */
    val liveDrScore: Int = 14,
    /** Audiophile dynamic range classification rating. */
    val drRating: String = "Audiophile Master Dynamics (DR14+)",
    /** Set when a background playback thread fails — see PlaybackController's onError handling. */
    val error: String? = null
)

/**
 * The ordered list of tracks queued for playback and where we are in it — owned
 * by PlaybackController (a singleton) rather than any one screen's ViewModel, so
 * the Library and Player screens can never disagree about what's playing next
 * (see PlaybackController's own doc comment on why it's the single shared
 * orchestrator both screens drive playback through).
 */
data class PlaybackQueue(
    val tracks: List<AudioTrackModel> = emptyList(),
    /** Indices into [tracks], in the order Next/Previous actually walk them —
     *  identity order with shuffle off, a shuffled permutation with it on
     *  (current track kept first so toggling shuffle never interrupts it). */
    val playOrder: List<Int> = emptyList(),
    val playPosition: Int = -1,
    val shuffleEnabled: Boolean = false
) {
    val hasQueue: Boolean get() = playOrder.isNotEmpty()
    /** False for a lone track with nothing queued around it — Next/Previous
     *  still wrap a real queue, but a single-track "queue" shouldn't loop. */
    val hasMultiple: Boolean get() = playOrder.size > 1
}

enum class LyricsProvider(val id: String, val displayName: String, val description: String) {
    AUTO("auto", "Auto (Best Match)", "Cascades across LRCLIB, Netease, and Kugou"),
    LRCLIB("lrclib", "LRCLIB.net", "Open-source global synchronized lyrics repository"),
    NETEASE("netease", "Netease (163 Music)", "High-accuracy millisecond synced lyrics catalog"),
    KUGOU("kugou", "Kugou Music", "Extensive multi-release lyrics database"),
    LOCAL_ONLY("local", "Local Only", "Embedded ID3 tags and companion .lrc sidecar files only")
}

data class LyricsSearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationSeconds: Int = 0,
    val isSynced: Boolean = true,
    val provider: LyricsProvider = LyricsProvider.LRCLIB,
    val rawLrcContent: String = ""
)

/** A single timestamped lyric line parsed from an .lrc file or embedded metadata. */
data class LyricLine(
    val timestampMs: Long,
    val text: String
)

/** Parsed lyrics data supporting synchronized lines or plain unsynced text. */
data class LyricsData(
    val title: String? = null,
    val artist: String? = null,
    val lines: List<LyricLine> = emptyList(),
    val plainText: String? = null,
    val isSynchronized: Boolean = lines.isNotEmpty(),
    val provider: LyricsProvider = LyricsProvider.AUTO,
    val sourceDescription: String = ""
) {
    /** Binary searches for the active lyric line at [currentPositionMs]. */
    fun findActiveIndex(currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        var low = 0
        var high = lines.size - 1
        var result = -1

        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timestampMs <= currentPositionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return result
    }
}

/** Sleep timer state for audiophile auto-shutdown with volume ramp down. */
data class SleepTimerState(
    val isEnabled: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val isEndOfTrack: Boolean = false
)

/** A-B repeat looper state for critical audio inspection. */
data class AbLoopState(
    val isEnabled: Boolean = false,
    val pointAMs: Long? = null,
    val pointBMs: Long? = null
)


