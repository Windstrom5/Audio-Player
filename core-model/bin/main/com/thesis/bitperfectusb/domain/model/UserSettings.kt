package com.thesis.bitperfectusb.domain.model

/**
 * Whether AdaptiveOptimizationEngine's buffer-resize recommendations are
 * actually applied during playback (ADAPTIVE, the default), or the buffer
 * size stays exactly where the person pinned it (MANUAL) — useful for
 * reproducing a specific point on the Experiment D stability-latency curve
 * during normal listening rather than only inside the Research tab.
 */
enum class BufferMode { ADAPTIVE, MANUAL }

/**
 * Two distinct, genuinely different strategies for driving the isochronous USB
 * endpoint — both bypass AudioFlinger entirely and are equally bit-perfect;
 * they differ in *how* they schedule transfers, not *whether* they modify data.
 */
enum class UsbTransferStrategy { PIPELINED, SYNCHRONOUS }

enum class EqPreset(val label: String, val gains: List<Float>) {
    FLAT("Flat / Neutral", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
    HARMAN_TARGET("Harman Target", listOf(3f, 2f, 1f, 0f, -1f, 0f, 1f, 2f, 2f, 3f)),
    AUDIOPHILE_NEUTRAL("Audiophile Reference", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 2f)),
    BASS_EXTENSION("Bass Extension", listOf(5f, 4f, 3f, 1f, 0f, 0f, 0f, 0f, 1f, 1f)),
    VOCAL_CLARITY("Vocal Clarity", listOf(-1f, 0f, 1f, 2f, 4f, 3f, 2f, 1f, 0f, 0f))
}

enum class DsdPlaybackMode(val label: String) {
    PCM_CONVERT("DSD-to-PCM"),
    DOP("DoP (DSD over PCM)"),
    NATIVE_DSD("Native DSD Direct")
}

enum class ReverbPreset(val label: String, val decaySec: Float, val roomSize: Float) {
    STUDIO_ROOM("Studio Room", 0.8f, 0.3f),
    CONCERT_HALL("Concert Hall", 2.2f, 0.75f),
    LIVE_STAGE("Live Stage", 1.5f, 0.55f),
    INTIMATE_CLUB("Intimate Club", 1.0f, 0.4f),
    CATHEDRAL("Cathedral", 3.8f, 0.95f)
}

enum class ReplayGainMode(val label: String) {
    TRACK("Track Loudness (EBU R128)"),
    ALBUM("Album Dynamic Range")
}

enum class KaraokeModeType(val label: String) {
    INSTRUMENTAL_ONLY("Pure Instrumental (AI Vocal Suppress)"),
    VOCAL_ISOLATION("Acapella (Solo Vocal Isolate)"),
    OFF("Standard Stereo")
}

/**
 * User-configurable settings tuning both bit-perfect hardware parameters
 * and precision audiophile DSP features (UAPP, Poweramp, Foobar2000 style).
 */
data class UserSettings(
    val bufferMode: BufferMode = BufferMode.ADAPTIVE,
    val manualBufferSizeBytes: Int = PlaybackConfig.DEFAULT_BUFFER_BYTES,
    val defaultEngine: EngineType = EngineType.CUSTOM_USB_DIRECT,
    val keepScreenOnDuringPlayback: Boolean = true,
    /** N-buffering depth for the isochronous USB streamer */
    val usbRequestPoolSize: Int = 4,
    /** Which bit-perfect USB transfer strategy to drive the DAC with */
    val usbTransferStrategy: UsbTransferStrategy = UsbTransferStrategy.PIPELINED,
    /** USB Buffer Latency preset in milliseconds (16ms, 32ms, 64ms, 128ms) */
    val usbBufferLatencyMs: Int = 32,
    /** Direct USB Audio Class (UVC) Hardware Register Volume Control */
    val hardwareVolumeEnabled: Boolean = true,
    val hardwareVolumeSteps: Int = 100,
    val hardwareVolumeStepIndex: Int = 85,
    /** DSD & DoP Stream transmission mode */
    val dsdMode: DsdPlaybackMode = DsdPlaybackMode.DOP,
    /** 10-Band Precision Equalizer */
    val eqEnabled: Boolean = false,
    val eqGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val eqPreset: EqPreset = EqPreset.FLAT,
    /** Headphone crossfeed */
    val crossfeedEnabled: Boolean = false,
    val crossfeedStrength: Float = 0.3f,
    /** Poweramp-style Stereo Soundstage Spatial Expander (0.0=Mono, 1.0=Normal, 2.0=Super-Wide) */
    val spatialWidth: Float = 1.0f,
    /** Poweramp-style Studio Acoustic Reverb Environment Simulator */
    val reverbEnabled: Boolean = false,
    val reverbPreset: ReverbPreset = ReverbPreset.STUDIO_ROOM,
    val reverbWetDry: Float = 0.25f,
    /** Foobar2000-style ReplayGain & EBU R128 Dynamic Range Normalizer */
    val replayGainEnabled: Boolean = false,
    val replayGainMode: ReplayGainMode = ReplayGainMode.TRACK,
    val replayGainPreampDb: Float = 0.0f,
    val preventClipping: Boolean = true,
    val normalizationEnabled: Boolean = false,
    /** Automatically bypasses EQ, Spatial, Reverb, and ReplayGain whenever Custom USB Direct is active */
    val autoBypassDspInBitPerfect: Boolean = true,
    /** Sleep timer with smooth 60-second fade-out */
    val sleepTimerMinutes: Int = 0,
    val sleepTimerFadeOutEnabled: Boolean = true,
    /** On-Device TensorFlow Lite AI Audio Suite */
    val aiAutoPilotEnabled: Boolean = false,
    val aiDseeUpscalerEnabled: Boolean = true,
    val aiVocalSuppressMode: Boolean = false,
    /** Real-time AI Karaoke Instrumental & Vocal Isolator Engine */
    val karaokeModeEnabled: Boolean = false,
    val karaokeModeType: KaraokeModeType = KaraokeModeType.INSTRUMENTAL_ONLY,
    val karaokeVocalSuppressionStrength: Float = 0.90f,
    val karaokeBassPreservation: Boolean = true,
    val karaokeKeyShiftSemitones: Int = 0,
    val karaokePlaybackSpeed: Float = 1.0f,
    val karaokeMicScoringEnabled: Boolean = false
) {
    companion object {
        val USB_REQUEST_POOL_RANGE = 2..8
        val EQ_BANDS = listOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
        val USB_BUFFER_PRESETS_MS = listOf(16, 32, 64, 128)
        val HARDWARE_STEP_MODES = listOf(64, 100, 256)
    }
}
