package com.thesis.bitperfectusb.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.data.repository.LyricsRepository
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.model.AbLoopState
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.LyricsData
import com.thesis.bitperfectusb.domain.model.LyricsProvider
import com.thesis.bitperfectusb.domain.model.LyricsSearchResult
import com.thesis.bitperfectusb.domain.model.PlaybackQueue
import com.thesis.bitperfectusb.domain.model.PlaybackState
import com.thesis.bitperfectusb.domain.model.SleepTimerState
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.domain.usecase.NextTrackUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveDacProfilesUseCase
import com.thesis.bitperfectusb.domain.usecase.ObservePlaybackStateUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveQueueUseCase
import com.thesis.bitperfectusb.domain.usecase.PausePlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.PreviousTrackUseCase
import com.thesis.bitperfectusb.domain.usecase.ResumePlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.SeekPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.SetQueueUseCase
import com.thesis.bitperfectusb.domain.usecase.StartPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.StopPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.ToggleShuffleUseCase
import com.thesis.bitperfectusb.usb.UsbDacManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class AudioRoute(val label: String, val fidelityBase: Int) {
    INTERNAL_SPEAKER("Internal Speaker", 65),
    WIRED_HEADPHONES("Wired (3.5mm Jack)", 75),
    BLUETOOTH_STANDARD("Bluetooth (SBC/AAC)", 35),
    BLUETOOTH_LDAC("Bluetooth (LDAC 990k)", 55),
    USB_DAC("External USB DAC", 100)
}

data class PlayerUiState(
    val dacProfiles: List<DacProfile> = emptyList(),
    val selectedDac: DacProfile? = null,
    val selectedEngine: EngineType = EngineType.CUSTOM_USB_DIRECT,
    val playback: PlaybackState = PlaybackState(),
    val activeTransferStrategy: UsbTransferStrategy = UsbTransferStrategy.PIPELINED,
    val error: String? = null,
    val eqEnabled: Boolean = false,
    val eqGains: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val crossfeedEnabled: Boolean = false,
    val crossfeedStrength: Float = 0.3f,
    val replayGainEnabled: Boolean = false,
    val normalizationEnabled: Boolean = false,
    val simulatedRoute: AudioRoute = AudioRoute.INTERNAL_SPEAKER,
    val videlityScore: Int = 100,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val formatQualityLabel: String = "",
    val noDacWarning: Boolean = false,
    val hardwareVolumePercent: Float = 1.0f,
    val hardwareVolumeEnabled: Boolean = true,
    val hardwareVolumeSteps: Int = 100,
    val hardwareVolumeStepIndex: Int = 85,
    val usbBufferLatencyMs: Int = 32,
    val dsdMode: com.thesis.bitperfectusb.domain.model.DsdPlaybackMode = com.thesis.bitperfectusb.domain.model.DsdPlaybackMode.DOP,
    val spatialWidth: Float = 1.0f,
    val reverbEnabled: Boolean = false,
    val reverbPreset: com.thesis.bitperfectusb.domain.model.ReverbPreset = com.thesis.bitperfectusb.domain.model.ReverbPreset.STUDIO_ROOM,
    val reverbWetDry: Float = 0.25f,
    val replayGainMode: com.thesis.bitperfectusb.domain.model.ReplayGainMode = com.thesis.bitperfectusb.domain.model.ReplayGainMode.TRACK,
    val replayGainPreampDb: Float = 0.0f,
    val preventClipping: Boolean = true,
    val bufferMode: com.thesis.bitperfectusb.domain.model.BufferMode = com.thesis.bitperfectusb.domain.model.BufferMode.ADAPTIVE,
    val manualBufferSizeBytes: Int = com.thesis.bitperfectusb.domain.model.PlaybackConfig.DEFAULT_BUFFER_BYTES,
    val queuePosition: Int = 0,
    val queueTotalTracks: Int = 0,
    val lyrics: LyricsData = LyricsData(),
    val activeLyricIndex: Int = -1,
    val showLyricsView: Boolean = false,
    val lyricsProvider: LyricsProvider = LyricsProvider.AUTO,
    val isSearchingLyrics: Boolean = false,
    val onlineSearchResults: List<LyricsSearchResult> = emptyList(),
    val sleepTimer: SleepTimerState = SleepTimerState(),
    val abLoop: AbLoopState = AbLoopState(),
    val playbackQueue: PlaybackQueue = PlaybackQueue(),
    val aiState: com.thesis.bitperfectusb.playback.ai.AiEngineState = com.thesis.bitperfectusb.playback.ai.AiEngineState(),
    val karaokeScore: com.thesis.bitperfectusb.playback.ai.KaraokeScoreState = com.thesis.bitperfectusb.playback.ai.KaraokeScoreState()
)

class PlayerViewModel(
    observeDacProfilesUseCase: ObserveDacProfilesUseCase,
    private val startPlaybackUseCase: StartPlaybackUseCase,
    private val stopPlaybackUseCase: StopPlaybackUseCase,
    private val seekPlaybackUseCase: SeekPlaybackUseCase,
    observePlaybackStateUseCase: ObservePlaybackStateUseCase,
    private val settingsRepository: SettingsRepository,
    private val usbDacManager: UsbDacManager,
    private val pausePlaybackUseCase: PausePlaybackUseCase,
    private val resumePlaybackUseCase: ResumePlaybackUseCase,
    private val nextTrackUseCase: NextTrackUseCase,
    private val previousTrackUseCase: PreviousTrackUseCase,
    private val toggleShuffleUseCase: ToggleShuffleUseCase,
    observeQueueUseCase: ObserveQueueUseCase,
    private val lyricsRepository: LyricsRepository,
    private val setQueueUseCase: SetQueueUseCase,
    private val stemExtractor: com.thesis.bitperfectusb.playback.ai.AiStemExtractor? = null
) : ViewModel() {

    private val _selectedDac = MutableStateFlow<DacProfile?>(null)
    private val _selectedEngine = MutableStateFlow(settingsRepository.current.defaultEngine)
    private val _simulatedRoute = MutableStateFlow(AudioRoute.INTERNAL_SPEAKER)
    private val _error = MutableStateFlow<String?>(null)
    private val _showLyricsView = MutableStateFlow(false)
    private val _lyrics = MutableStateFlow(LyricsData())
    private val _lyricsProvider = MutableStateFlow(lyricsRepository.getPreferredProvider())
    private val _isSearchingLyrics = MutableStateFlow(false)
    private val _onlineSearchResults = MutableStateFlow<List<LyricsSearchResult>>(emptyList())
    private val _sleepTimer = MutableStateFlow(SleepTimerState())
    private val _abLoop = MutableStateFlow(AbLoopState())

    private var sleepTimerJob: Job? = null
    private var lastLoadedLyricsTrackId: Long? = null

    /** Serializes pause/resume/seek so they never race each other:
     *  - pause() + immediate resume() won't start playing over a still-running stop
     *  - seek spam won't queue N concurrent stop+restart cycles */
    private val playbackMutex = Mutex()

    /** Holds the pending debounced seek coroutine — cancelled and replaced each
     *  time the user moves the seekbar before it fires, so only the final
     *  resting position triggers the expensive stop+restart. */
    private var seekDebounceJob: Job? = null

    /** Track id that [_error] was last cleared/set for. Starting a *different*
     *  track happens through LibraryViewModel (a separate instance), which has
     *  no way to touch this ViewModel's _error — without this, an error from
     *  one track would keep shadowing playback.error forever, even once a
     *  totally different, successfully-playing track was current. */
    private var errorTrackId: Long? = null

    val aiAudioEngine = com.thesis.bitperfectusb.playback.ai.AiAudioEngine()
    val karaokeMicScorer = com.thesis.bitperfectusb.playback.ai.KaraokeMicScorer()

    init {
        // Sync initial settings to AI engine
        val initSettings = settingsRepository.current
        aiAudioEngine.setAutoPilotActive(initSettings.aiAutoPilotEnabled)
        aiAudioEngine.setDseeUpscalerActive(initSettings.aiDseeUpscalerEnabled)
        aiAudioEngine.setKaraokeConfig(
            enabled = initSettings.karaokeModeEnabled || initSettings.aiVocalSuppressMode,
            mode = initSettings.karaokeModeType,
            strength = initSettings.karaokeVocalSuppressionStrength,
            preserveBass = initSettings.karaokeBassPreservation,
            keyShift = initSettings.karaokeKeyShiftSemitones
        )

        // Automatically load lyrics when current track changes
        viewModelScope.launch {
            observePlaybackStateUseCase().collect { playback ->
                val track = playback.currentTrack
                if (track != null && track.id != lastLoadedLyricsTrackId) {
                    lastLoadedLyricsTrackId = track.id
                    _lyrics.value = lyricsRepository.loadLyricsForTrack(track, _lyricsProvider.value)
                    val isCached = isStemCached(settingsRepository.current.karaokeModeType)
                    aiAudioEngine.setStemCachedActive(isCached)
                } else if (track == null) {
                    lastLoadedLyricsTrackId = null
                    _lyrics.value = LyricsData()
                    aiAudioEngine.setStemCachedActive(false)
                }

                // Check A-B repeat looper
                val loop = _abLoop.value
                val pointA = loop.pointAMs
                val pointB = loop.pointBMs
                if (loop.isEnabled && pointA != null && pointB != null) {
                    if (playback.positionMs >= pointB) {
                        seekTo(pointA)
                    }
                }
            }
        }
    }

    private val engineRouteAndDevice = combine(_selectedEngine, _simulatedRoute, usbDacManager.connectedDevice) { engine, route, device ->
        Triple(engine, route, device)
    }

    private val lyricsAndExtraUi = combine(
        _lyrics,
        _showLyricsView,
        _lyricsProvider,
        _isSearchingLyrics,
        _onlineSearchResults
    ) { lyrics, showLyrics, provider, isSearching, results ->
        LyricsUiTuple(lyrics, showLyrics, provider, isSearching, results)
    }

    private val extraStates = combine(_sleepTimer, _abLoop, aiAudioEngine.aiState, karaokeMicScorer.scoreState) { timer, loop, ai, score ->
        ScoreMegaExtra(timer, loop, ai, score)
    }

    private val combinedFlows = combine(engineRouteAndDevice, observeQueueUseCase(), lyricsAndExtraUi, extraStates) { a, b, c, extra ->
        CombineMegaTuple(a, b, c, extra.sleepTimer, extra.abLoop, extra.aiState, extra.karaokeScore)
    }

    val uiState: StateFlow<PlayerUiState> = combine(
        observeDacProfilesUseCase(),
        _selectedDac,
        combinedFlows,
        observePlaybackStateUseCase(),
        settingsRepository.settings
    ) { dacs, selectedDacOverride, megaTuple, playback, userSettings ->
        val engineTuple = megaTuple.engineRouteAndDevice
        val queue = megaTuple.queue
        val lyricsTuple = megaTuple.lyricsTuple
        val timer = megaTuple.sleepTimer
        val loop = megaTuple.abLoop
        val aiState = megaTuple.aiState

        val selected = selectedDacOverride ?: dacs.firstOrNull()
        val engine = engineTuple.first
        val simRoute = engineTuple.second
        val connectedUsbDevice = engineTuple.third

        if (playback.currentTrack?.id != errorTrackId) {
            _error.value = null
            errorTrackId = playback.currentTrack?.id
        }
        
        val actualEngine = if (selected == null || connectedUsbDevice == null) EngineType.ANDROID_AUDIOTRACK else engine
        
        val activeRoute = if (actualEngine == EngineType.CUSTOM_USB_DIRECT && selected != null && connectedUsbDevice != null) {
            AudioRoute.USB_DAC
        } else {
            simRoute
        }

        var baseFidelity = activeRoute.fidelityBase
        if (userSettings.eqEnabled && baseFidelity > 35) baseFidelity -= 5
        if (userSettings.crossfeedEnabled && baseFidelity > 35) baseFidelity -= 8
        if (userSettings.replayGainEnabled && baseFidelity > 35) baseFidelity -= 3
        if (userSettings.normalizationEnabled && baseFidelity > 35) baseFidelity -= 3

        val sourceTrack = playback.currentTrack
        val sourcePcm = sourceTrack?.pcm
        val formatQualityLabel = when {
            actualEngine == EngineType.CUSTOM_USB_DIRECT && selected != null && connectedUsbDevice != null ->
                "USB DIRECT"
            sourcePcm == null -> ""
            actualEngine == EngineType.ANDROID_AUDIOTRACK && (sourcePcm.bitDepth > 16 || sourcePcm.sampleRateHz > 48_000) ->
                "RESAMPLED"
            else -> "16-BIT"
        }

        val noDacWarning = engine == EngineType.CUSTOM_USB_DIRECT &&
            (selected == null || connectedUsbDevice == null)

        val activeLyricIdx = lyricsTuple.lyrics.findActiveIndex(playback.positionMs)

        PlayerUiState(
            dacProfiles = dacs,
            selectedDac = selected,
            selectedEngine = actualEngine,
            playback = playback,
            activeTransferStrategy = userSettings.usbTransferStrategy,
            error = _error.value ?: playback.error,
            eqEnabled = userSettings.eqEnabled,
            eqGains = userSettings.eqGains,
            crossfeedEnabled = userSettings.crossfeedEnabled,
            crossfeedStrength = userSettings.crossfeedStrength,
            replayGainEnabled = userSettings.replayGainEnabled,
            normalizationEnabled = userSettings.normalizationEnabled,
            simulatedRoute = activeRoute,
            videlityScore = baseFidelity.coerceIn(0, 100),
            hasNext = queue.hasQueue,
            hasPrevious = queue.hasQueue,
            shuffleEnabled = queue.shuffleEnabled,
            formatQualityLabel = formatQualityLabel,
            noDacWarning = noDacWarning,
            hardwareVolumePercent = (userSettings.hardwareVolumeStepIndex.toFloat() / userSettings.hardwareVolumeSteps.coerceAtLeast(1)),
            hardwareVolumeEnabled = userSettings.hardwareVolumeEnabled,
            hardwareVolumeSteps = userSettings.hardwareVolumeSteps,
            hardwareVolumeStepIndex = userSettings.hardwareVolumeStepIndex,
            usbBufferLatencyMs = userSettings.usbBufferLatencyMs,
            dsdMode = userSettings.dsdMode,
            spatialWidth = userSettings.spatialWidth,
            reverbEnabled = userSettings.reverbEnabled,
            reverbPreset = userSettings.reverbPreset,
            reverbWetDry = userSettings.reverbWetDry,
            replayGainMode = userSettings.replayGainMode,
            replayGainPreampDb = userSettings.replayGainPreampDb,
            preventClipping = userSettings.preventClipping,
            bufferMode = userSettings.bufferMode,
            manualBufferSizeBytes = userSettings.manualBufferSizeBytes,
            queuePosition = if (queue.hasQueue && queue.playPosition >= 0) queue.playPosition + 1 else 1,
            queueTotalTracks = if (queue.hasQueue) queue.tracks.size else 1,
            lyrics = lyricsTuple.lyrics,
            activeLyricIndex = activeLyricIdx,
            showLyricsView = lyricsTuple.showLyrics,
            lyricsProvider = lyricsTuple.provider,
            isSearchingLyrics = lyricsTuple.isSearching,
            onlineSearchResults = lyricsTuple.searchResults,
            sleepTimer = timer,
            abLoop = loop,
            playbackQueue = queue,
            karaokeScore = megaTuple.karaokeScore,
            aiState = aiState.copy(
                isAutoPilotActive = userSettings.aiAutoPilotEnabled,
                isDseeUpscalerActive = userSettings.aiDseeUpscalerEnabled,
                isVocalSuppressionActive = userSettings.aiVocalSuppressMode || userSettings.karaokeModeEnabled,
                karaokeModeEnabled = userSettings.karaokeModeEnabled,
                karaokeModeType = userSettings.karaokeModeType,
                karaokeVocalSuppressionStrength = userSettings.karaokeVocalSuppressionStrength,
                karaokeBassPreservation = userSettings.karaokeBassPreservation,
                karaokeKeyShiftSemitones = userSettings.karaokeKeyShiftSemitones
            )
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    fun toggleLyricsView() {
        _showLyricsView.value = !_showLyricsView.value
    }

    fun setLyricsProvider(provider: LyricsProvider) {
        _lyricsProvider.value = provider
        lyricsRepository.setPreferredProvider(provider)
        reloadLyricsOnline(forceProvider = provider)
    }

    fun reloadLyricsOnline(forceProvider: LyricsProvider? = null) {
        val currentTrack = uiState.value.playback.currentTrack ?: return
        viewModelScope.launch {
            _lyrics.value = lyricsRepository.loadLyricsForTrack(currentTrack, forceProvider ?: _lyricsProvider.value)
        }
    }

    fun searchOnlineLyrics(query: String) {
        viewModelScope.launch {
            _isSearchingLyrics.value = true
            _onlineSearchResults.value = lyricsRepository.searchOnlineLyrics(query, _lyricsProvider.value)
            _isSearchingLyrics.value = false
        }
    }

    fun selectLyricsResult(result: LyricsSearchResult) {
        val currentTrack = uiState.value.playback.currentTrack ?: return
        viewModelScope.launch {
            _lyrics.value = lyricsRepository.applySelectedOnlineLyrics(currentTrack, result)
            _onlineSearchResults.value = emptyList()
        }
    }

    fun importCustomLyrics(content: String) {
        val currentTrack = uiState.value.playback.currentTrack ?: return
        viewModelScope.launch {
            val parsed = lyricsRepository.saveCustomLyrics(currentTrack, content)
            _lyrics.value = parsed
        }
    }

    fun deleteCustomLyrics() {
        val currentTrack = uiState.value.playback.currentTrack ?: return
        viewModelScope.launch {
            lyricsRepository.deleteCustomLyrics(currentTrack)
            _lyrics.value = lyricsRepository.loadLyricsForTrack(currentTrack, _lyricsProvider.value)
        }
    }

    fun setSleepTimer(minutes: Int, isEndOfTrack: Boolean = false) {
        sleepTimerJob?.cancel()
        if (isEndOfTrack) {
            val track = uiState.value.playback.currentTrack
            val remainingMs = if (track != null) {
                (track.durationMs - uiState.value.playback.positionMs).coerceAtLeast(1000L)
            } else 60_000L
            val totalSec = (remainingMs / 1000).toInt()
            _sleepTimer.value = SleepTimerState(isEnabled = true, remainingSeconds = totalSec, totalSeconds = totalSec, isEndOfTrack = true)
        } else {
            val totalSec = minutes * 60
            _sleepTimer.value = SleepTimerState(isEnabled = true, remainingSeconds = totalSec, totalSeconds = totalSec, isEndOfTrack = false)
        }

        sleepTimerJob = viewModelScope.launch {
            while (_sleepTimer.value.isEnabled && _sleepTimer.value.remainingSeconds > 0) {
                delay(1000L)
                val newRemaining = _sleepTimer.value.remainingSeconds - 1
                _sleepTimer.value = _sleepTimer.value.copy(remainingSeconds = newRemaining)
                if (newRemaining <= 0) break
            }
            if (_sleepTimer.value.isEnabled) {
                _sleepTimer.value = SleepTimerState(isEnabled = false, remainingSeconds = 0, totalSeconds = 0)
                pause()
            }
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimer.value = SleepTimerState(isEnabled = false, remainingSeconds = 0, totalSeconds = 0)
    }

    fun setLoopPointA() {
        val currentPos = uiState.value.playback.positionMs
        _abLoop.value = _abLoop.value.copy(pointAMs = currentPos, isEnabled = _abLoop.value.pointBMs != null)
    }

    fun setLoopPointB() {
        val currentPos = uiState.value.playback.positionMs
        val pointA = _abLoop.value.pointAMs ?: 0L
        if (currentPos > pointA) {
            _abLoop.value = _abLoop.value.copy(pointBMs = currentPos, isEnabled = true)
        }
    }

    fun clearAbLoop() {
        _abLoop.value = AbLoopState(isEnabled = false, pointAMs = null, pointBMs = null)
    }

    fun playQueueTrack(track: com.thesis.bitperfectusb.domain.model.AudioTrackModel) {
        viewModelScope.launch {
            playbackMutex.withLock {
                try {
                    _error.value = null
                    val queue = uiState.value.playbackQueue
                    if (queue.tracks.isNotEmpty()) {
                        setQueueUseCase(queue.tracks, track)
                    }
                    startPlaybackUseCase(track, uiState.value.selectedEngine, uiState.value.selectedDac)
                } catch (t: Throwable) {
                    _error.value = t.message ?: "Failed to play selected track."
                }
            }
        }
    }

    fun selectDac(dac: DacProfile) {
        _selectedDac.value = dac
    }

    fun selectEngine(engine: EngineType) {
        _selectedEngine.value = engine
        settingsRepository.setDefaultEngine(engine)
    }

    fun setUsbTransferStrategy(strategy: UsbTransferStrategy) {
        settingsRepository.setUsbTransferStrategy(strategy)
    }

    fun setBufferMode(mode: com.thesis.bitperfectusb.domain.model.BufferMode) {
        settingsRepository.setBufferMode(mode)
    }

    fun setManualBufferSize(bytes: Int) {
        settingsRepository.setManualBufferSize(bytes)
    }

    fun selectSimulatedRoute(route: AudioRoute) {
        _simulatedRoute.value = route
    }

    fun toggleEq() {
        val currentEnabled = uiState.value.eqEnabled
        settingsRepository.setEqEnabled(!currentEnabled)
    }

    fun updateEqGains(gains: List<Float>) {
        settingsRepository.setEqGains(gains)
    }

    fun toggleCrossfeed() {
        settingsRepository.setCrossfeedEnabled(!uiState.value.crossfeedEnabled)
    }

    fun setCrossfeedStrength(strength: Float) {
        settingsRepository.setCrossfeedStrength(strength)
    }

    fun toggleReplayGain() {
        settingsRepository.setReplayGainEnabled(!uiState.value.replayGainEnabled)
    }

    fun toggleNormalization() {
        settingsRepository.setNormalizationEnabled(!uiState.value.normalizationEnabled)
    }

    fun replayCurrentTrack() {
        val track = uiState.value.playback.currentTrack ?: return
        viewModelScope.launch {
            playbackMutex.withLock {
                try {
                    _error.value = null
                    startPlaybackUseCase(track, uiState.value.selectedEngine, uiState.value.selectedDac)
                } catch (t: Throwable) {
                    _error.value = t.message ?: "Playback failed to start."
                }
            }
        }
    }

    fun seekTo(positionMs: Long) {
        seekDebounceJob?.cancel()
        seekDebounceJob = viewModelScope.launch {
            playbackMutex.withLock {
                try {
                    seekPlaybackUseCase(positionMs)
                } catch (t: Throwable) {
                    _error.value = t.message ?: "Seek failed."
                }
            }
        }
    }

    fun stop() {
        viewModelScope.launch { stopPlaybackUseCase() }
    }

    fun pause() {
        seekDebounceJob?.cancel()
        viewModelScope.launch {
            playbackMutex.withLock {
                try {
                    pausePlaybackUseCase()
                } catch (t: Throwable) {
                    _error.value = t.message ?: "Pause failed."
                }
            }
        }
    }

    fun resume() {
        viewModelScope.launch {
            playbackMutex.withLock {
                try {
                    resumePlaybackUseCase()
                } catch (t: Throwable) {
                    _error.value = t.message ?: "Resume failed."
                }
            }
        }
    }

    fun nextTrack() {
        viewModelScope.launch {
            try {
                nextTrackUseCase()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Couldn't skip to the next track."
            }
        }
    }

    fun previousTrack() {
        viewModelScope.launch {
            try {
                previousTrackUseCase()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Couldn't skip to the previous track."
            }
        }
    }

    fun setHardwareVolume(percent: Float) {
        val clamped = percent.coerceIn(0.0f, 1.0f)
        viewModelScope.launch(Dispatchers.IO) {
            val dev = usbDacManager.currentDevice ?: return@launch
            val conn = usbDacManager.openConnection(dev) ?: return@launch
            try {
                usbDacManager.setHardwareVolume(conn, clamped)
            } finally {
                try { conn.close() } catch (_: Exception) {}
            }
        }
    }

    fun setHardwareVolumeStep(step: Int, maxSteps: Int) {
        settingsRepository.setHardwareVolumeSteps(maxSteps)
        val clampedPercent = (step.toFloat() / maxSteps.coerceAtLeast(1)).coerceIn(0.0f, 1.0f)
        setHardwareVolume(clampedPercent)
    }

    fun toggleHardwareVolume() {
        settingsRepository.setHardwareVolumeEnabled(!uiState.value.hardwareVolumeEnabled)
    }

    fun setSpatialWidth(width: Float) {
        settingsRepository.setSpatialWidth(width)
    }

    fun toggleReverb() {
        settingsRepository.setReverbEnabled(!uiState.value.reverbEnabled)
    }

    fun setReverbPreset(preset: com.thesis.bitperfectusb.domain.model.ReverbPreset) {
        settingsRepository.setReverbPreset(preset)
    }

    fun setReverbWetDry(wetDry: Float) {
        settingsRepository.setReverbWetDry(wetDry)
    }

    fun setReplayGainMode(mode: com.thesis.bitperfectusb.domain.model.ReplayGainMode) {
        settingsRepository.setReplayGainMode(mode)
    }

    fun setReplayGainPreampDb(preampDb: Float) {
        settingsRepository.setReplayGainPreampDb(preampDb)
    }

    fun togglePreventClipping() {
        settingsRepository.setPreventClipping(!uiState.value.preventClipping)
    }

    fun setUsbBufferLatency(latencyMs: Int) {
        settingsRepository.setUsbBufferLatency(latencyMs)
    }

    fun setDsdMode(mode: com.thesis.bitperfectusb.domain.model.DsdPlaybackMode) {
        settingsRepository.setDsdMode(mode)
    }

    fun toggleAiAutoPilot(enabled: Boolean) {
        settingsRepository.setAiAutoPilotEnabled(enabled)
        aiAudioEngine.setAutoPilotActive(enabled)
        if (enabled) {
            val suggested = aiAudioEngine.aiState.value.suggestedEqGains
            updateEqGains(suggested)
        }
    }

    fun toggleAiDsee(enabled: Boolean) {
        settingsRepository.setAiDseeUpscalerEnabled(enabled)
        aiAudioEngine.setDseeUpscalerActive(enabled)
    }

    fun toggleAiVocalSuppression(enabled: Boolean) {
        settingsRepository.setAiVocalSuppressMode(enabled)
        settingsRepository.setKaraokeModeEnabled(enabled)
        aiAudioEngine.setVocalSuppressionActive(enabled)
    }

    fun setKaraokeConfig(
        enabled: Boolean,
        mode: com.thesis.bitperfectusb.domain.model.KaraokeModeType = settingsRepository.current.karaokeModeType,
        strength: Float = settingsRepository.current.karaokeVocalSuppressionStrength,
        preserveBass: Boolean = settingsRepository.current.karaokeBassPreservation,
        keyShift: Int = settingsRepository.current.karaokeKeyShiftSemitones
    ) {
        settingsRepository.setKaraokeModeEnabled(enabled)
        settingsRepository.setKaraokeModeType(mode)
        settingsRepository.setKaraokeVocalSuppressionStrength(strength)
        settingsRepository.setKaraokeBassPreservation(preserveBass)
        settingsRepository.setKaraokeKeyShiftSemitones(keyShift)
        aiAudioEngine.setKaraokeConfig(enabled, mode, strength, preserveBass, keyShift)
    }

    fun toggleKaraokeMode() {
        val currentEnabled = settingsRepository.current.karaokeModeEnabled
        setKaraokeConfig(!currentEnabled)
    }

    fun setKaraokeModeType(mode: com.thesis.bitperfectusb.domain.model.KaraokeModeType) {
        val isEnabled = mode != com.thesis.bitperfectusb.domain.model.KaraokeModeType.OFF
        val isCached = if (isEnabled) isStemCached(mode) else false
        settingsRepository.setKaraokeModeEnabled(isEnabled)
        settingsRepository.setKaraokeModeType(mode)
        aiAudioEngine.setKaraokeConfig(
            enabled = isEnabled,
            mode = mode,
            isStemCached = isCached
        )

        // Hot-reload track seamlessly if playback is currently active so it immediately switches
        // between the original track and the clean cached stem without losing playback position!
        val currentTrack = uiState.value.playback.currentTrack
        if (currentTrack != null && uiState.value.playback.isPlaying) {
            val currentPos = uiState.value.playback.positionMs
            viewModelScope.launch {
                playbackMutex.withLock {
                    seekPlaybackUseCase(currentPos)
                }
            }
        }
    }

    fun isStemCached(mode: com.thesis.bitperfectusb.domain.model.KaraokeModeType = com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY): Boolean {
        val track = uiState.value.playback.currentTrack ?: return false
        val extractor = stemExtractor ?: return false
        return extractor.getCachedStem(track.id, mode) != null
    }

    fun setKaraokeStrength(strength: Float) {
        settingsRepository.setKaraokeVocalSuppressionStrength(strength)
        aiAudioEngine.setKaraokeConfig(
            enabled = settingsRepository.current.karaokeModeEnabled,
            strength = strength
        )
    }

    fun setKaraokeKeyShift(semitones: Int) {
        settingsRepository.setKaraokeKeyShiftSemitones(semitones)
        aiAudioEngine.setKaraokeConfig(
            enabled = settingsRepository.current.karaokeModeEnabled,
            keyShift = semitones
        )
    }

    fun toggleKaraokeBassPreservation() {
        val newPreserve = !settingsRepository.current.karaokeBassPreservation
        settingsRepository.setKaraokeBassPreservation(newPreserve)
        aiAudioEngine.setKaraokeConfig(
            enabled = settingsRepository.current.karaokeModeEnabled,
            preserveBass = newPreserve
        )
    }

    fun toggleKaraokeMicScoring(context: android.content.Context, enabled: Boolean) {
        settingsRepository.setKaraokeMicScoringEnabled(enabled)
        if (enabled) {
            karaokeMicScorer.start(
                context = context,
                getReferencePitch = { aiAudioEngine.getVocalIsolator().detectedPitchHz },
                getReferenceNote = { aiAudioEngine.getVocalIsolator().detectedMusicalNote }
            )
        } else {
            karaokeMicScorer.stop()
        }
    }

    fun resetKaraokeScore() {
        karaokeMicScorer.resetScore()
    }

    fun applyAiSuggestedEq(gains: List<Float>) {
        updateEqGains(gains)
    }

    fun toggleShuffle() {
        toggleShuffleUseCase()
    }

    private val _stemExtractionProgress = MutableStateFlow<com.thesis.bitperfectusb.playback.ai.StemExtractionProgress?>(null)
    val stemExtractionProgress: StateFlow<com.thesis.bitperfectusb.playback.ai.StemExtractionProgress?> = _stemExtractionProgress.asStateFlow()

    fun extractInstrumentalStem(mode: com.thesis.bitperfectusb.domain.model.KaraokeModeType = com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY) {
        val track = uiState.value.playback.currentTrack ?: return
        val extractor = stemExtractor ?: return
        if (isStemCached(mode)) {
            // Already in cache: instantly select mode and show cached completion
            setKaraokeModeType(mode)
            _stemExtractionProgress.value = com.thesis.bitperfectusb.playback.ai.StemExtractionProgress(
                progressPercent = 100,
                statusMessage = "Stem already in cache! Auto-selected.",
                isCompleted = true,
                outputFile = extractor.getCachedStem(track.id, mode)
            )
            return
        }
        viewModelScope.launch {
            extractor.extractStem(track, mode).collect { progress ->
                _stemExtractionProgress.value = progress
                if (progress.isCompleted && progress.outputFile != null) {
                    // Auto-route playback to the freshly generated clean cached stem!
                    setKaraokeModeType(mode)
                }
            }
        }
    }

    fun dismissStemExtractionProgress() {
        _stemExtractionProgress.value = null
    }

    fun getStemCacheSizeBytes(): Long {
        return stemExtractor?.getCacheSizeBytes() ?: 0L
    }

    fun clearStemCache(): Boolean {
        val result = stemExtractor?.clearStemCache() ?: false
        val currentTrack = uiState.value.playback.currentTrack
        if (currentTrack != null && uiState.value.playback.isPlaying) {
            val currentPos = uiState.value.playback.positionMs
            viewModelScope.launch {
                playbackMutex.withLock {
                    seekPlaybackUseCase(currentPos)
                }
            }
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        karaokeMicScorer.stop()
    }

    companion object {
        private const val SEEK_DEBOUNCE_MS = 280L
    }
}

private data class LyricsUiTuple(
    val lyrics: LyricsData,
    val showLyrics: Boolean,
    val provider: LyricsProvider,
    val isSearching: Boolean,
    val searchResults: List<LyricsSearchResult>
)

private data class ScoreMegaExtra(
    val sleepTimer: SleepTimerState,
    val abLoop: AbLoopState,
    val aiState: com.thesis.bitperfectusb.playback.ai.AiEngineState,
    val karaokeScore: com.thesis.bitperfectusb.playback.ai.KaraokeScoreState
)

private data class CombineMegaTuple(
    val engineRouteAndDevice: Triple<EngineType, AudioRoute, android.hardware.usb.UsbDevice?>,
    val queue: PlaybackQueue,
    val lyricsTuple: LyricsUiTuple,
    val sleepTimer: SleepTimerState,
    val abLoop: AbLoopState,
    val aiState: com.thesis.bitperfectusb.playback.ai.AiEngineState,
    val karaokeScore: com.thesis.bitperfectusb.playback.ai.KaraokeScoreState
)



