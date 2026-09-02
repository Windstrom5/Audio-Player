package com.thesis.bitperfectusb.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.thesis.bitperfectusb.benchmark.BenchmarkRunner
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.engine.AdaptiveOptimizationEngine
import com.thesis.bitperfectusb.domain.engine.PlaybackIntegrityEngine
import com.thesis.bitperfectusb.domain.model.AudioFileFormat
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.BenchmarkSample
import com.thesis.bitperfectusb.domain.model.BufferMode
import com.thesis.bitperfectusb.domain.model.BufferRecommendation
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.IntegrityResult
import com.thesis.bitperfectusb.domain.model.LiveMetricPoint
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.PlaybackConfig
import com.thesis.bitperfectusb.domain.model.PlaybackQueue
import com.thesis.bitperfectusb.domain.model.PlaybackSession
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.domain.model.PlaybackState
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import com.thesis.bitperfectusb.usb.UsbDacManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * Application-scoped orchestrator tying together decoder selection, engine
 * selection, live integrity verification, adaptive buffer optimization, and
 * benchmark/session logging (Sections 3.4–3.7). Both the Player screen and
 * ExperimentOrchestrator drive playback through this one class, so the two
 * consumers can never observe inconsistent behavior.
 */
class PlaybackController(
    private val context: Context,
    private val usbDacManager: UsbDacManager,
    private val integrityEngine: PlaybackIntegrityEngine,
    private val optimizationEngine: AdaptiveOptimizationEngine,
    private val benchmarkRunner: BenchmarkRunner,
    private val benchmarkRepository: BenchmarkRepository,
    private val settingsRepository: SettingsRepository,
    private val vorbisCommentReader: FlacVorbisCommentReader
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitorJob: Job? = null
    private var currentEngine: PlaybackEngine? = null
    private var currentDecoder: AudioDecoder? = null
    private var currentSessionId: Long? = null
    private val dropoutsThisTick = AtomicInteger(0)
    private var accumulator = SessionAccumulator()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    /** Epoch ms of the last PlaybackService.start() call; 0 if the service isn't
     *  currently expected to be running. Lets stop() guarantee the service a
     *  minimum lifetime — see the comment on MIN_SERVICE_LIFETIME_MS below. */
    private var serviceStartedAtMs: Long = 0L
    /** What seekTo() needs to restart the same track/engine/DAC at a new
     *  position — remembered here rather than requiring the caller to supply
     *  them again, the same way stop() needs no params from its callers. */
    private var lastTrack: AudioTrackModel? = null
    private var lastEngineType: EngineType = EngineType.CUSTOM_USB_DIRECT
    private var lastDac: DacProfile? = null
    private var lastSampleRateHz: Int = 0

    private val _clockReLockEvent = kotlinx.coroutines.flow.MutableSharedFlow<Pair<Int, Int>>(extraBufferCapacity = 1)
    val clockReLockEvent: kotlinx.coroutines.flow.SharedFlow<Pair<Int, Int>> = _clockReLockEvent

    fun setHardwareVolume(volumePercent: Float): Boolean {
        val device = usbDacManager.currentDevice ?: return false
        val conn = usbDacManager.openConnection(device) ?: return false
        return try {
            usbDacManager.setHardwareVolume(conn, volumePercent)
        } finally {
            try { conn.close() } catch (_: Exception) {}
        }
    }

    /** The track position (ms) this session's engine started counting from —
     *  0 for a fresh play, or the seek target when resuming from a seek. The
     *  monitor loop adds engine.currentPositionMs() (which always counts from
     *  0 per-session) to this to get an absolute track position. */
    private var sessionStartPositionMs: Long = 0L

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state

    private val _queue = MutableStateFlow(PlaybackQueue())
    val queue: StateFlow<PlaybackQueue> = _queue

    /** Library-driven entry point used by the Player screen. */
    suspend fun start(
        track: AudioTrackModel,
        engineType: EngineType,
        dac: DacProfile?,
        initialPositionMs: Long = 0L
    ): Long {
        lastTrack = track
        lastEngineType = engineType
        lastDac = dac

        val settings = settingsRepository.current
        val cachedStem = if (settings.karaokeModeEnabled && settings.karaokeModeType != com.thesis.bitperfectusb.domain.model.KaraokeModeType.OFF) {
            val stemDir = java.io.File(context.externalCacheDir ?: context.cacheDir, "stem_cache")
            val prefix = if (settings.karaokeModeType == com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY) "instrumental" else "acapella"
            val file = java.io.File(stemDir, "stem_${track.id}_$prefix.wav")
            if (file.exists() && file.length() > 44) file else null
        } else {
            null
        }

        val decoder: AudioDecoder
        val effectiveFilePath: String
        if (cachedStem != null) {
            decoder = WavDecoder(context)
            effectiveFilePath = android.net.Uri.fromFile(cachedStem).toString()
            Log.i(TAG, "Routing playback to clean pre-rendered cached stem: ${cachedStem.name}")
        } else {
            decoder = when (track.format) {
                AudioFileFormat.WAV -> WavDecoder(context)
                AudioFileFormat.FLAC -> FlacDecoder(context)
            }
            effectiveFilePath = track.filePath
        }

        // Expected total is the *decoded PCM* byte count, not the on-disk file size —
        // those match closely for WAV but diverge a lot for FLAC, which is compressed.
        val bytesPerSecond = track.pcm.sampleRateHz * track.pcm.channels * (track.pcm.bitDepth / 8)
        val expectedPcmBytes = (track.durationMs / 1000.0 * bytesPerSecond).toLong().takeIf { it > 0L }

        val effectiveBufferSize = if (settings.bufferMode == BufferMode.MANUAL) {
            settings.manualBufferSizeBytes
        } else {
            PlaybackConfig.DEFAULT_BUFFER_BYTES
        }

        return startWithDecoder(
            decoder, effectiveFilePath, engineType, dac, track.id.takeIf { it != 0L },
            initialBufferSizeBytes = effectiveBufferSize,
            track = track, totalBytesExpected = expectedPcmBytes,
            respectBufferMode = true,
            initialPositionMs = initialPositionMs,
            isPlayingCachedStem = (cachedStem != null)
        )
    }

    /**
     * Jumps the current track to [positionMs]. Implemented as a full
     * stop-then-restart at the new position rather than seeking a live
     * decoder in place — the engine's playback thread is continuously
     * calling decoder.read() on its own thread, and seeking that same
     * decoder concurrently from here would reproduce the exact kind of
     * MediaCodec teardown race this app already hit once (see FlacDecoder's
     * isClosed handling). Stopping first (which now correctly waits for that
     * thread to actually exit before returning) makes seeking single-threaded
     * and safe, at the cost of a brief stop/restart instead of a seamless
     * jump — a fine trade for correctness over a still-fairly-fast seek.
     */
    suspend fun seekTo(positionMs: Long) {
        val track = lastTrack ?: return
        val clamped = positionMs.coerceIn(0L, track.durationMs.takeIf { it > 0L } ?: positionMs)
        start(track, lastEngineType, lastDac, initialPositionMs = clamped)
    }

    /**
     * Captures [tracks] (typically whatever list a track was tapped from — the
     * Library screen's currently filtered/sorted view) as the queue Next/Previous
     * will walk, with [startAt] as the starting point. Does not itself start
     * playback — call [start] separately, same as before. Re-tapping any track
     * from any list replaces the queue; nothing sets it automatically.
     */
    fun setQueue(tracks: List<AudioTrackModel>, startAt: AudioTrackModel) {
        val startIndex = tracks.indexOfFirst { it.id == startAt.id }.let { if (it < 0) 0 else it }
        val shuffle = _queue.value.shuffleEnabled
        val order = buildPlayOrder(tracks.size, startIndex, shuffle)
        _queue.value = PlaybackQueue(
            tracks = tracks,
            playOrder = order,
            playPosition = order.indexOf(startIndex).let { if (it < 0) 0 else it },
            shuffleEnabled = shuffle
        )
    }

    /**
     * Pauses without discarding anything needed to resume — implemented as a
     * full engine stop (same underlying mechanism as [seekTo]'s stop-then-restart,
     * for the same single-threaded-decoder-safety reason) that deliberately
     * leaves currentTrack/positionMs alone so [resume] knows exactly where to
     * restart from. A no-op if nothing is currently playing.
     *
     * IMPORTANT: uses `isPausedOverride = true` so stop() writes
     * `isPaused = true` atomically in its own state update rather than
     * setting it on the line after — this eliminates the transient
     * `isPlaying=false, isPaused=false` window that caused Compose to
     * recompose into the "else → replayCurrentTrack()" branch and
     * restart playback behind the user's back.
     */
    suspend fun pause() {
        if (!_state.value.isPlaying) return
        stop(stopService = false, isPausedOverride = true)
    }

    /** Resumes from wherever [pause] left off. A no-op if not currently paused. */
    suspend fun resume() {
        if (!_state.value.isPaused) return
        val track = lastTrack ?: return
        start(track, lastEngineType, lastDac, initialPositionMs = _state.value.positionMs)
    }

    suspend fun nextTrack() = advanceQueue(+1)

    suspend fun previousTrack() = advanceQueue(-1)

    /**
     * Toggles shuffle without interrupting whatever's currently playing — the
     * playing track is kept at the front of the new order either way, so this
     * only changes what Next/Previous visit from here on, not what's audible
     * right now.
     */
    fun toggleShuffle() {
        val q = _queue.value
        if (!q.hasQueue) {
            _queue.value = q.copy(shuffleEnabled = !q.shuffleEnabled)
            return
        }
        val newShuffle = !q.shuffleEnabled
        val currentQueueIndex = q.playOrder[q.playPosition]
        val newOrder = buildPlayOrder(q.tracks.size, currentQueueIndex, newShuffle)
        _queue.value = q.copy(
            playOrder = newOrder,
            playPosition = newOrder.indexOf(currentQueueIndex).let { if (it < 0) 0 else it },
            shuffleEnabled = newShuffle
        )
    }

    private suspend fun advanceQueue(direction: Int) {
        val q = _queue.value
        if (!q.hasQueue) return
        val newPosition = (q.playPosition + direction + q.playOrder.size) % q.playOrder.size
        val queueIndex = q.playOrder[newPosition]
        val track = q.tracks.getOrNull(queueIndex) ?: return
        _queue.value = q.copy(playPosition = newPosition)
        start(track, lastEngineType, lastDac)
    }

    /** Called when a track finishes naturally (not a manual stop) — advances to
     *  whatever's next in the queue if there's genuinely more than one track
     *  queued AND this was a real queue-eligible session, otherwise stops
     *  exactly as before. Both conditions matter: hasMultiple alone isn't
     *  enough, because ExperimentOrchestrator's synthetic-tone sweeps call
     *  startWithDecoder directly with no AudioTrackModel (so currentTrack is
     *  null) — without the currentTrack check, a sweep finishing naturally
     *  while a multi-track queue happened to be left over from a previous
     *  Library session would wrongly auto-advance into a real track
     *  mid-experiment. */
    private suspend fun onTrackFinished() {
        if (_queue.value.hasMultiple && _state.value.currentTrack != null) {
            advanceQueue(+1)
        } else {
            stop()
        }
    }

    /** Identity order with shuffle off; a shuffled permutation with [startIndex]
     *  kept first (so enabling shuffle never interrupts what's currently
     *  playing) with shuffle on. */
    private fun buildPlayOrder(size: Int, startIndex: Int, shuffle: Boolean): List<Int> {
        if (size <= 0) return emptyList()
        val indices = (0 until size).toMutableList()
        if (!shuffle) return indices
        indices.remove(startIndex)
        indices.shuffle()
        return listOf(startIndex) + indices
    }

    /**
     * Lower-level entry point used by both [start] and ExperimentOrchestrator: takes an
     * already-selected decoder (real file or [SyntheticToneDecoder]) so experiment sweeps
     * can hold the signal source constant while varying engine/format/buffer size.
     */
    suspend fun startWithDecoder(
        decoder: AudioDecoder,
        sourceUri: String,
        engineType: EngineType,
        dac: DacProfile?,
        trackId: Long? = null,
        initialBufferSizeBytes: Int = PlaybackConfig.DEFAULT_BUFFER_BYTES,
        track: AudioTrackModel? = null,
        totalBytesExpected: Long? = null,
        /** True only for regular listening (see [start]) — governs whether the
         *  AdaptiveOptimizationEngine is allowed to resize the buffer mid-session
         *  per Settings > Buffer Mode. Experiment sweeps always ignore this and
         *  stay adaptive, since they need reproducible, uniform conditions across
         *  every config regardless of what the person has personally pinned. */
        respectBufferMode: Boolean = false,
        /** Non-zero when this session is resuming from a seek rather than
         *  starting fresh — see [seekTo]. Applied to the decoder right after
         *  open(), before the engine ever starts reading from it. */
        initialPositionMs: Long = 0L,
        isPlayingCachedStem: Boolean = false
    ): Long {
        stop(stopService = false) // ensure a clean slate (leave the foreground service running — we're about to restart it below anyway)

        // decoder.open() does blocking I/O and, for FLAC, a MediaCodec warm-up loop
        // that can take up to ~1s worst case. This function is called directly from
        // LibraryViewModel's viewModelScope (Dispatchers.Main.immediate), so without
        // this withContext, that work — plus stop()'s own blocking teardown above —
        // runs straight on the UI thread. That's what was starving the Looper right
        // when PlaybackService.start() below needs it free to dispatch
        // onStartCommand() and call startForeground() before Android's timeout,
        // which is what was crashing the app with ForegroundServiceDidNotStartInTimeException.
        val actualFormat = try {
            withContext(Dispatchers.IO) {
                val format = decoder.open(sourceUri)
                // Seeking here — after open(), before the engine's playback thread
                // ever starts calling read() — is what keeps this single-threaded
                // and safe. See AudioDecoder.seek's doc for why concurrent seek
                // would be unsafe with the MediaCodec/jflac decoders underneath.
                if (initialPositionMs > 0L) decoder.seek(initialPositionMs)
                if (lastSampleRateHz > 0 && lastSampleRateHz != format.sampleRateHz) {
                    _clockReLockEvent.tryEmit(lastSampleRateHz to format.sampleRateHz)
                }
                lastSampleRateHz = format.sampleRateHz
                format
            }

        } catch (e: Exception) {
            // This is the failure that was previously invisible: if opening the
            // decoder throws here, it used to propagate straight back to whichever
            // screen called startWithDecoder (e.g. LibraryViewModel.playTrack's own
            // try/catch) — but by the time that catch runs, the person has already
            // navigated to the Player screen, which reads a *different* error source
            // (this controller's own _state.error) and never saw it. Setting it here
            // means it shows up wherever playback state is actually being observed.
            Log.e(TAG, "Failed to open decoder for $sourceUri", e)
            _state.value = _state.value.copy(
                isPlaying = false,
                error = e.message ?: "Couldn't open this track."
            )
            currentDecoder = null
            throw e
        }
        currentDecoder = decoder

        // ReplayGain/Normalization's data source — real Vorbis-comment tags read
        // from the file, not computed. Skipped for the synthetic-tone URIs
        // ExperimentOrchestrator/DacRateBenchmark use (nothing to read there, and
        // FlacVorbisCommentReader would just fail the contentResolver.openInputStream
        // call anyway — this just avoids that pointless attempt).
        val replayGainTags = if (!sourceUri.startsWith("synthetic://")) {
            withContext(Dispatchers.IO) { vorbisCommentReader.read(sourceUri) }
        } else {
            FlacVorbisCommentReader.ReplayGainTags(null, null)
        }

        var activeEngineType = engineType
        val engine: PlaybackEngine = if (engineType == EngineType.CUSTOM_USB_DIRECT) {
            val device = usbDacManager.currentDevice
            if (device == null || dac == null) {
                Log.w(TAG, "No USB DAC attached or analyzed profile available. Falling back to AudioTrack engine.")
                activeEngineType = EngineType.ANDROID_AUDIOTRACK
                AudioTrackPlaybackEngine(context, settingsRepository).apply {
                    this.isPlayingCachedStem = isPlayingCachedStem
                }
            } else {
                try {
                    buildUsbDirectEngine(
                        actualFormat, dac, settingsRepository.current.usbRequestPoolSize, settingsRepository.current.usbTransferStrategy
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to initialize USB Direct engine. Falling back to AudioTrack engine.", e)
                    activeEngineType = EngineType.ANDROID_AUDIOTRACK
                    AudioTrackPlaybackEngine(context, settingsRepository).apply {
                        this.isPlayingCachedStem = isPlayingCachedStem
                    }
                }
            }
        } else {
            AudioTrackPlaybackEngine(context, settingsRepository).apply {
                this.isPlayingCachedStem = isPlayingCachedStem
            }
        }

        val integrity = integrityEngine.evaluate(actualFormat, dac, activeEngineType)
        var bufferSize = initialBufferSizeBytes
        val isManualBuffer = respectBufferMode && settingsRepository.current.bufferMode == BufferMode.MANUAL

        currentEngine = engine
        accumulator = SessionAccumulator()
        benchmarkRunner.resetForNewRun()

        val sessionId = benchmarkRepository.startSession(
            PlaybackSession(
                trackId = trackId,
                dacProfileId = dac?.id,
                engineType = activeEngineType,
                startEpochMs = System.currentTimeMillis(),
                endEpochMs = null,
                integrityScore = integrity.score,
                avgLatencyMs = 0.0,
                avgCpuPercent = 0.0,
                avgMemoryMb = 0.0,
                dropoutCount = 0,
                bufferSizeBytes = bufferSize
            )
        )
        currentSessionId = sessionId

        sessionStartPositionMs = initialPositionMs
        _state.value = PlaybackState(
            isPlaying = true,
            currentTrack = track,
            positionMs = initialPositionMs,
            engineType = activeEngineType,
            bufferSizeBytes = bufferSize,
            integrity = integrity,
            totalBytesExpected = totalBytesExpected
        )

        try {
            PlaybackService.start(context)
            serviceStartedAtMs = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start PlaybackService: ${e.message}")
            serviceStartedAtMs = 0L
        }
        requestAudioFocus()

        engine.start(
            decoder = decoder,
            format = actualFormat,
            bufferSizeBytes = bufferSize,
            onDropout = {
                dropoutsThisTick.incrementAndGet()
                benchmarkRunner.dropoutDetector.onDropout()
            },
            onCompletion = { scope.launch { onTrackFinished() } },
            onError = { throwable -> scope.launch { handleEngineError(throwable) } },
            replayGainDb = replayGainTags.trackGainDb,
            trackPeak = replayGainTags.trackPeak
        )

        monitorJob = scope.launch {
            val tickStartMs = System.currentTimeMillis()
            val history = ArrayDeque<LiveMetricPoint>()
            var previousTransmittedBytes = 0L

            while (isActive) {
                try {
                    delay(TICK_INTERVAL_MS)
                    val cpu = benchmarkRunner.sampleCpuPercent()
                    val mem = benchmarkRunner.sampleMemoryMb()
                    val latency = engine.currentLatencyEstimateMs()
                    val dropoutsSinceLastTick = dropoutsThisTick.getAndSet(0)

                    benchmarkRunner.latencyMonitor.record(latency)
                    accumulator.record(cpu, mem, latency, dropoutsSinceLastTick)

                    val stability = optimizationEngine.tick(dropoutsSinceLastTick, latency, bufferSize)
                    if (!isManualBuffer && stability.recommendation != BufferRecommendation.MAINTAIN) {
                        bufferSize = stability.recommendedBufferSizeBytes
                        engine.setBufferSize(bufferSize)
                    }

                    try {
                        benchmarkRepository.recordSample(
                            sessionId,
                            BenchmarkSample(
                                timestampMs = System.currentTimeMillis(),
                                cpuPercent = cpu,
                                memoryMb = mem,
                                latencyMs = latency,
                                cumulativeDropouts = accumulator.totalDropouts,
                                engineType = engineType
                            )
                        )
                    } catch (dbEx: Exception) {
                        Log.e("PlaybackController", "Failed to record sample to database", dbEx)
                    }

                    val elapsedSeconds = (System.currentTimeMillis() - tickStartMs) / 1000.0
                    history.addLast(LiveMetricPoint(elapsedSeconds, latency, cpu))
                    while (history.size > MAX_HISTORY_POINTS) history.removeFirst()

                    val verification = engine.verificationSnapshot()
                    val transferRate = ((verification.transmittedBytes - previousTransmittedBytes) / (TICK_INTERVAL_MS / 1000.0))
                        .coerceAtLeast(0.0)
                    previousTransmittedBytes = verification.transmittedBytes
                    val traffic = engine.trafficLog()

                    var positionMs = sessionStartPositionMs + engine.currentPositionMs()
                    track?.durationMs?.let { duration ->
                        if (duration > 0L) positionMs = positionMs.coerceAtMost(duration)
                    }

                    _state.value = _state.value.copy(
                        bufferSizeBytes = bufferSize,
                        stability = stability,
                        liveLatencyMs = latency,
                        liveCpuPercent = cpu,
                        liveMemoryMb = mem,
                        recentHistory = history.toList(),
                        verification = verification,
                        trafficLog = traffic,
                        transferRateBytesPerSec = transferRate,
                        positionMs = positionMs
                    )
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("PlaybackController", "Error in monitor loop", e)
                }
            }
        }

        return sessionId
    }

    /**
     * @param stopService Whether to also tear down the actual Android Service.
     *   Pass false when this is being called from [startWithDecoder] to clear
     *   state before immediately starting a new track — stopping the service
     *   there and then starting it again moments later (stopService()
     *   immediately followed by startForegroundService() for the same
     *   component) is exactly the pattern that produces
     *   ForegroundServiceDidNotStartInTimeException: the stop can land at
     *   ActivityManagerService in a way that cancels the pending foreground
     *   promotion before onStartCommand() ever gets to call startForeground().
     *   There's no need to tear it down at all when we're about to restart it
     *   for the next track anyway — startForegroundService() on an
     *   already-running instance just redelivers onStartCommand(), which is
     *   exactly what we want.
     */
    /**
     * @param isPausedOverride When true (set only by [pause]), the final state
     *   update writes `isPaused = true` atomically alongside `isPlaying = false`,
     *   preventing a transient frame where both are false — that brief window
     *   was causing Compose to hit the `else → replayCurrentTrack()` branch
     *   and restart playback behind the user's back.
     */
    suspend fun stop(stopService: Boolean = true, isPausedOverride: Boolean = false) {
        // Immediately mark as not-playing so any concurrent Compose recompose
        // sees the correct state even before the engine is fully torn down.
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = isPausedOverride
        )

        monitorJob?.cancel()
        monitorJob = null
        // Capture the final verdict before the engine is torn down — this is the
        // most meaningful comparison point, since source/transmitted byte counts
        // reliably converge at end of track.
        val finalVerification = currentEngine?.verificationSnapshot()
        withContext(Dispatchers.IO) {
            currentEngine?.stop()
            currentDecoder?.close()
        }

        currentSessionId?.let { sessionId ->
            benchmarkRepository.endSession(
                sessionId = sessionId,
                endEpochMs = System.currentTimeMillis(),
                avgLatencyMs = accumulator.avgLatency(),
                avgCpuPercent = accumulator.avgCpu(),
                avgMemoryMb = accumulator.avgMemory(),
                dropoutCount = accumulator.totalDropouts,
                verifiedBitPerfect = finalVerification?.verified
            )
        }
        currentSessionId = null
        currentEngine = null
        currentDecoder = null
        if (stopService) {
            // Guarantee the service a minimum lifetime before stopping it. Without
            // this, anything that makes playback end very quickly after it started
            // — a track that fails to decode almost instantly (as seen on some
            // MediaTek codec builds), a very short file, or rapid track-switching —
            // can call stopService() while startForegroundService()'s
            // onStartCommand()/startForeground() is still pending. That specific
            // ordering is what throws ForegroundServiceDidNotStartInTimeException
            // and kills the whole app. This is an AOSP-level timing requirement,
            // not a MediaTek-specific one, so the same race can happen on any
            // chipset/OEM skin (Snapdragon, Exynos, Tensor, etc.) — the guard has
            // to be unconditional, not gated on device type.
            val elapsedSinceServiceStart = System.currentTimeMillis() - serviceStartedAtMs
            if (serviceStartedAtMs > 0L && elapsedSinceServiceStart < MIN_SERVICE_LIFETIME_MS) {
                delay(MIN_SERVICE_LIFETIME_MS - elapsedSinceServiceStart)
            }
            serviceStartedAtMs = 0L
            try {
                PlaybackService.stop(context)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop PlaybackService: ${e.message}")
            }
        }
        abandonAudioFocus()
        // Final state write — preserves isPausedOverride if set, otherwise
        // clears isPaused (genuine stop while paused, or a seek restart).
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = isPausedOverride,
            verification = finalVerification ?: _state.value.verification
        )
    }

    private fun requestAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { focusChange ->
                        Log.d(TAG, "Audio focus changed: $focusChange")
                    }
                    .build()
                audioFocusRequest = focusRequest
                audioManager.requestAudioFocus(focusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    { focusChange -> Log.d(TAG, "Audio focus changed: $focusChange") },
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to request AudioFocus: ${e.message}")
        }
    }

    private fun abandonAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus({ /* no-op */ })
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to abandon AudioFocus: ${e.message}")
        }
    }

    fun verifyIntegrity(format: PcmFormat, dac: DacProfile?, engineType: EngineType): IntegrityResult =
        integrityEngine.evaluate(format, dac, engineType)

    /**
     * Handles a failure surfaced from a background playback thread (see
     * PlaybackEngine.onError). This is the difference between "the app shows an
     * error message" and "the app disappears" — an uncaught exception on a raw
     * background Thread otherwise terminates the whole process with no recovery
     * chance at all, which is exactly the crash this exists to prevent.
     */
    private suspend fun handleEngineError(throwable: Throwable) {
        Log.e(TAG, "Playback engine failed", throwable)
        val message = throwable.message?.takeIf { it.isNotBlank() }
            ?: "Playback stopped unexpectedly (${throwable::class.simpleName})."
        stop()
        _state.value = _state.value.copy(error = message)
    }

    private fun buildUsbDirectEngine(
        format: PcmFormat,
        dac: DacProfile?,
        requestPoolSize: Int,
        transferStrategy: UsbTransferStrategy
    ): UsbDirectPlaybackEngine {
        val device = usbDacManager.currentDevice ?: error("No USB DAC attached for the custom engine.")
        val profile = dac ?: error("This DAC hasn't been analyzed yet — visit the DAC tab and tap Analyze Capabilities first.")
        val connection = usbDacManager.openConnection(device) ?: error("USB permission not granted for this DAC.")

        // First try an exact match (same rate + bit depth + channels). If the DAC's analyzed
        // profile doesn't have the exact combination, try best-effort (same bit depth, nearest
        // rate) before giving up. Logging makes clear which path was taken — a format log in
        // logcat is the fastest way to diagnose a "plays but wrong rate" situation.
        val exactOption = profile.findExactStreamingOption(format)
        val activated: com.thesis.bitperfectusb.usb.ActivatedStreamingEndpoint = if (exactOption != null) {
            Log.i(TAG, "USB Direct: exact format match ${format.sampleRateHz}Hz/${format.bitDepth}-bit/${format.channels}ch → " +
                "altsetting ${exactOption.alternateSetting} on interface ${exactOption.interfaceNumber}")
            usbDacManager.activateStreamingOption(device, connection, profile, format)
                ?: run {
                    connection.close()
                    error("USB Direct: failed to activate exact-match altsetting for ${format.sampleRateHz}Hz/${format.bitDepth}-bit.")
                }
        } else {
            val bestOption = profile.findBestStreamingOption(format)
            if (bestOption == null) {
                connection.close()
                error(
                    "No streaming option for ${format.channels}ch found on this DAC. " +
                        "Re-run Analyze Capabilities or check the DAC's USB Audio Class descriptors."
                )
            }
            val bestRate = bestOption.supportedSampleRates.minByOrNull { kotlin.math.abs(it - format.sampleRateHz) }
                ?: bestOption.supportedSampleRates.first()
            Log.w(TAG, "USB Direct: no exact match for ${format.sampleRateHz}Hz/${format.bitDepth}-bit. " +
                "Best-effort: using altsetting ${bestOption.alternateSetting} " +
                "(${bestOption.bitDepth}-bit, rate=${bestRate}Hz). " +
                "Re-run Analyze Capabilities if the DAC should support ${format.sampleRateHz}Hz natively.")
            val bestFormat = PcmFormat(bestRate, bestOption.bitDepth, format.channels)
            usbDacManager.activateStreamingOption(device, connection, profile, bestFormat)
                ?: run {
                    connection.close()
                    error("USB Direct: failed to activate best-effort altsetting for ${bestOption.bitDepth}-bit/${bestRate}Hz.")
                }
        }

        return UsbDirectPlaybackEngine(usbDacManager, device, connection, activated, requestPoolSize, transferStrategy)
    }


    private class SessionAccumulator {
        private var cpuSum = 0.0
        private var memSum = 0.0
        private var latencySum = 0.0
        private var tickCount = 0
        var totalDropouts = 0
            private set

        fun record(cpu: Double, mem: Double, latency: Double, dropoutsThisTick: Int) {
            cpuSum += cpu
            memSum += mem
            latencySum += latency
            tickCount++
            totalDropouts += dropoutsThisTick
        }

        fun avgCpu() = if (tickCount > 0) cpuSum / tickCount else 0.0
        fun avgMemory() = if (tickCount > 0) memSum / tickCount else 0.0
        fun avgLatency() = if (tickCount > 0) latencySum / tickCount else 0.0
    }

    companion object {
        private const val TAG = "PlaybackController"
        private const val TICK_INTERVAL_MS = 500L
        private const val MAX_HISTORY_POINTS = 60 // ~30 seconds at the 500ms tick interval
        // Comfortably above what onStartCommand()->startForeground() needs even on a
        // stalled/loaded main thread or a slower OEM skin; short enough to be
        // imperceptible on a manual stop.
        private const val MIN_SERVICE_LIFETIME_MS = 600L
    }
}
