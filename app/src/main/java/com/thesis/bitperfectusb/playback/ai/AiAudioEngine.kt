package com.thesis.bitperfectusb.playback.ai

import android.content.Context
import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Live state of the on-device AI Audio Engine.
 */
data class AiEngineState(
    val isAutoPilotActive: Boolean = false,
    val isDseeUpscalerActive: Boolean = false,
    val isVocalSuppressionActive: Boolean = false,
    val detectedGenre: String = "Analyzing...",
    val confidencePct: Float = 0f,
    val genreProbabilities: Map<String, Float> = emptyMap(),
    val detectedCutoffKHz: Float = 22.05f,
    val isLossyDetected: Boolean = false,
    val suggestedEqGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    /** Studio Karaoke Engine State */
    val karaokeModeEnabled: Boolean = false,
    val karaokeModeType: KaraokeModeType = KaraokeModeType.INSTRUMENTAL_ONLY,
    val karaokeVocalSuppressionStrength: Float = 0.90f,
    val karaokeBassPreservation: Boolean = true,
    val karaokeKeyShiftSemitones: Int = 0,
    val detectedPitchHz: Float = 0f,
    val detectedMusicalNote: String = "--",
    val isTfliteModelLoaded: Boolean = false,
    val isStemCachedActive: Boolean = false
)

/**
 * Central coordinator for TensorFlow Lite AI audio processing.
 * Runs non-blocking neural classification and manages DSP enhancements.
 */
class AiAudioEngine(context: Context? = null) {

    private val classifier = AiGenreClassifier(context)
    private val harmonicRestorer = AiHarmonicRestorer()
    private val vocalIsolator = AiVocalIsolator(context)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _aiState = MutableStateFlow(AiEngineState(isTfliteModelLoaded = vocalIsolator.isTfliteModelLoaded))
    val aiState: StateFlow<AiEngineState> = _aiState.asStateFlow()

    private var lastAnalysisTimeMs = 0L

    /**
     * Feeds audio PCM buffer snapshot for async AI inference.
     * Guaranteed 0% blocking on playback thread.
     */
    fun feedPcmSnapshot(pcm: ShortArray, sampleRate: Int = 44100) {
        val now = System.currentTimeMillis()
        // Throttle neural classification to every 800ms to conserve battery & CPU
        if (now - lastAnalysisTimeMs < 800) return
        lastAnalysisTimeMs = now

        val bufferCopy = pcm.copyOf(pcm.size.coerceAtMost(4096))
        scope.launch {
            val genreResult = classifier.classifyPcmSnapshot(bufferCopy, sampleRate)
            val cutoff = harmonicRestorer.analyzeSpectrumCutoff(bufferCopy, sampleRate)
            val isLossy = harmonicRestorer.isLossy()

            _aiState.value = _aiState.value.copy(
                detectedGenre = genreResult.topGenre,
                confidencePct = genreResult.confidencePct,
                genreProbabilities = genreResult.genreProbabilities,
                detectedCutoffKHz = cutoff,
                isLossyDetected = isLossy,
                suggestedEqGains = genreResult.suggestedEqGains,
                detectedPitchHz = vocalIsolator.detectedPitchHz,
                detectedMusicalNote = vocalIsolator.detectedMusicalNote,
                isTfliteModelLoaded = vocalIsolator.isTfliteModelLoaded
            )
        }
    }

    /**
     * Applies active AI DSP filters (DSEE Upscaling & Vocal Isolation / Karaoke) to real-time playback buffer.
     */
    fun processPcmBuffer(pcm: ShortArray, sampleRate: Int = 44100) {
        val state = _aiState.value
        if (state.isDseeUpscalerActive && state.isLossyDetected) {
            harmonicRestorer.processStereoPcm(pcm, sampleRate, intensity = 0.6f)
        }
        if (state.karaokeModeEnabled && state.karaokeModeType != KaraokeModeType.OFF) {
            if (!state.isStemCachedActive) {
                // Real-time live DSP vocal separation when no cached stem exists
                vocalIsolator.process(
                    pcm = pcm,
                    sampleRate = sampleRate,
                    mode = state.karaokeModeType,
                    strength = state.karaokeVocalSuppressionStrength,
                    preserveBass = state.karaokeBassPreservation,
                    keyShiftSemitones = state.karaokeKeyShiftSemitones
                )
            } else if (state.karaokeKeyShiftSemitones != 0) {
                // When playing clean pre-rendered stem from cache, only apply pitch key transposition if active
                vocalIsolator.process(
                    pcm = pcm,
                    sampleRate = sampleRate,
                    mode = KaraokeModeType.OFF,
                    strength = 0f,
                    preserveBass = false,
                    keyShiftSemitones = state.karaokeKeyShiftSemitones
                )
            }
        } else if (state.isVocalSuppressionActive && !state.isStemCachedActive) {
            vocalIsolator.processKaraokeVocalSuppression(
                pcm = pcm,
                strength = 0.90f,
                preserveBass = true,
                sampleRate = sampleRate
            )
        }
    }

    fun setStemCachedActive(active: Boolean) {
        _aiState.value = _aiState.value.copy(isStemCachedActive = active)
    }

    fun setAutoPilotActive(active: Boolean) {
        _aiState.value = _aiState.value.copy(isAutoPilotActive = active)
    }

    fun setDseeUpscalerActive(active: Boolean) {
        _aiState.value = _aiState.value.copy(isDseeUpscalerActive = active)
    }

    fun setVocalSuppressionActive(active: Boolean) {
        _aiState.value = _aiState.value.copy(
            isVocalSuppressionActive = active,
            karaokeModeEnabled = active
        )
    }

    fun setKaraokeConfig(
        enabled: Boolean,
        mode: KaraokeModeType = _aiState.value.karaokeModeType,
        strength: Float = _aiState.value.karaokeVocalSuppressionStrength,
        preserveBass: Boolean = _aiState.value.karaokeBassPreservation,
        keyShift: Int = _aiState.value.karaokeKeyShiftSemitones,
        isStemCached: Boolean = _aiState.value.isStemCachedActive
    ) {
        vocalIsolator.setKeyShift(keyShift)
        _aiState.value = _aiState.value.copy(
            karaokeModeEnabled = enabled,
            karaokeModeType = mode,
            karaokeVocalSuppressionStrength = strength,
            karaokeBassPreservation = preserveBass,
            karaokeKeyShiftSemitones = keyShift,
            isVocalSuppressionActive = enabled && mode == KaraokeModeType.INSTRUMENTAL_ONLY,
            isStemCachedActive = isStemCached
        )
    }

    fun getClassifier(): AiGenreClassifier = classifier
    fun getHarmonicRestorer(): AiHarmonicRestorer = harmonicRestorer
    fun getVocalIsolator(): AiVocalIsolator = vocalIsolator
}
