package com.thesis.bitperfectusb.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.model.UserSettings
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Standard `AudioTrack` playback engine with built-in DSP EQ gain processing.
 * Bypasses bit depth and sample rate reduction whenever supported by the Android audio HAL:
 * - High-res sample rates (44.1k, 48k, 88.2k, 96k, 176.4k, 192k, etc.) are passed natively to AudioTrack.
 * - 24-bit+ PCM is written via ENCODING_PCM_FLOAT (API 21+, lossless for 24-bit content —
 *   float has a 24-bit mantissa) as the primary path, with ENCODING_PCM_24BIT_PACKED
 *   (API 31+) as a secondary attempt only if FLOAT itself fails to initialize. See the
 *   ordering rationale in buildCandidateConfigs: PCM_24BIT_PACKED reaching
 *   STATE_INITIALIZED doesn't guarantee the HAL actually produces audible output for it
 *   on every device, and there's no reliable way to detect that failure mode from
 *   userspace ahead of time.
 * - Gracefully falls back to 16-bit PCM if the hardware HAL rejects high-res formats,
 *   guaranteeing 100% audio playback across all Android devices.
 */
class AudioTrackPlaybackEngine(
    private val context: android.content.Context? = null,
    private val settingsRepository: SettingsRepository
) : PlaybackEngine {

    private var audioTrack: AudioTrack? = null
    private var playbackThread: Thread? = null
    private val running = AtomicBoolean(false)
    private var bufferSizeBytes = 4096
    private var bytesPerSecond = 1
    private val dropouts = AtomicInteger(0)
    private var lastUnderrunCount = 0
    private val verifier = TransferVerifier()
    /** Bytes actually written to the AudioTrack so far this session — read from
     *  the monitor-loop thread via [currentPositionMs], written from the
     *  playback thread, so this needs to be safe across that boundary. */
    private val totalBytesWrittenAtomic = java.util.concurrent.atomic.AtomicLong(0L)
    private val vocalIsolator = com.thesis.bitperfectusb.playback.ai.AiVocalIsolator(context)
    var isPlayingCachedStem: Boolean = false

    override val dropoutCount: Int get() = dropouts.get()

    /** Not private — [CrossfeedProcessor] and the ReplayGain/Normalization gain
     *  application need to discriminate on this too, from a separate file. */
    enum class BitDepthMode {
        PCM_16,
        PCM_24_PACKED,
        PCM_FLOAT,
        PCM_32_INT
    }

    private data class AudioTrackConfig(
        val sampleRateHz: Int,
        val encoding: Int,
        val bitDepthMode: BitDepthMode
    )

    override fun start(
        decoder: AudioDecoder,
        format: PcmFormat,
        bufferSizeBytes: Int,
        onDropout: () -> Unit,
        onCompletion: () -> Unit,
        onError: (Throwable) -> Unit,
        replayGainDb: Float?,
        trackPeak: Float?
    ) {
        this.bufferSizeBytes = bufferSizeBytes
        verifier.reset()
        running.set(true)

        val settings = settingsRepository.current
        // ReplayGain and Normalization are two independent gain sources multiplied
        // together when both are on — see UserSettings.normalizationEnabled's doc
        // comment on why that's the chosen behavior rather than one overriding the
        // other. Computed once here rather than per-buffer in the hot write loop.
        val replayGainMultiplier = if (settings.replayGainEnabled && replayGainDb != null) {
            Math.pow(10.0, replayGainDb / 20.0).toFloat()
        } else {
            1f
        }
        val normalizationMultiplier = if (settings.normalizationEnabled && trackPeak != null && trackPeak > 0f) {
            // Capped rather than letting a corrupt/outlier peak tag (e.g. a
            // mistagged near-zero value) turn into an enormous, clipping boost.
            (1f / trackPeak).coerceAtMost(4f)
        } else {
            1f
        }
        val tagGainMultiplier = replayGainMultiplier * normalizationMultiplier

        val crossfeed = if (settings.crossfeedEnabled && format.channels == 2) {
            CrossfeedProcessor(settings.crossfeedStrength, format.sampleRateHz)
        } else {
            null
        }

        playbackThread = Thread({
            try {
                runPlaybackLoop(decoder, format, onDropout, onCompletion, tagGainMultiplier, crossfeed)
            } catch (t: Throwable) {
                Log.e(TAG, "AudioTrack playback loop failed", t)
                running.set(false)
                onError(t)
            }
        }, "audiotrack-playback").apply { start() }
    }

    private fun runPlaybackLoop(
        decoder: AudioDecoder,
        format: PcmFormat,
        onDropout: () -> Unit,
        onCompletion: () -> Unit,
        tagGainMultiplier: Float,
        crossfeed: CrossfeedProcessor?
    ) {
        val channelMask = if (format.channels <= 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO

        // Generate candidate configurations prioritizing native resolution and bit depth,
        // with fallback options down to 16-bit/48kHz for maximum compatibility.
        val candidates = buildCandidateConfigs(format)

        var trackAndConfig: Pair<AudioTrack, AudioTrackConfig>? = null
        for (candidate in candidates) {
            val minBuf = AudioTrack.getMinBufferSize(candidate.sampleRateHz, channelMask, candidate.encoding)
            if (minBuf == AudioTrack.ERROR || minBuf == AudioTrack.ERROR_BAD_VALUE || minBuf <= 0) continue

            try {
                val attributesBuilder = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)

                val trackBuilder = AudioTrack.Builder()
                    .setAudioAttributes(attributesBuilder.build())
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(candidate.encoding)
                            .setSampleRate(candidate.sampleRateHz)
                            .setChannelMask(channelMask)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBuf, bufferSizeBytes))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .apply {
                        // Requests the exclusive direct path (bypasses the AudioFlinger
                        // software mixer) on devices whose HAL supports it. A best-effort
                        // hint — the runtime degrades silently to PERFORMANCE_MODE_NONE if
                        // the device doesn't support it, so this is safe on all API levels.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                        }
                    }

                val t = trackBuilder.build()
                if (t.state == AudioTrack.STATE_INITIALIZED) {
                    // Log the HAL-negotiated format so we can see in logcat what bit depth
                    // and sample rate AudioFlinger actually handed to the hardware. On devices
                    // whose DAC doesn't support hi-res (e.g. TECNO LJ7: earpiece limited to
                    // 44.1/48kHz), the HAL will silently resample — this surfaces that.
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val negotiated = t.format
                        val negotiatedRate = negotiated.sampleRate
                        val negotiatedEncoding = negotiated.encoding
                        if (negotiatedRate != candidate.sampleRateHz) {
                            Log.w(
                                TAG, "HAL RESAMPLED: requested ${candidate.sampleRateHz}Hz → " +
                                    "negotiated ${negotiatedRate}Hz. " +
                                    "This device's internal DAC does not support hi-res sample rates. " +
                                    "Connect a USB DAC and use the USB Direct engine for bit-perfect hi-res playback."
                            )
                        } else {
                            Log.i(TAG, "HAL accepted: ${negotiatedRate}Hz / encoding=$negotiatedEncoding (requested ${candidate.sampleRateHz}Hz / ${candidate.encoding})")
                        }
                    }
                    trackAndConfig = t to candidate
                    break
                } else {
                    t.release()
                }
            } catch (e: Exception) {
                Log.w(TAG, "AudioTrack failed to initialize candidate $candidate: ${e.message}")
            }
        }

        val (track, activeConfig) = trackAndConfig ?: error("Failed to initialize AudioTrack for format $format on this device.")
        audioTrack = track

        val bytesPerSample = when (activeConfig.bitDepthMode) {
            BitDepthMode.PCM_16 -> 2
            BitDepthMode.PCM_24_PACKED -> 3
            BitDepthMode.PCM_FLOAT, BitDepthMode.PCM_32_INT -> 4
        }
        bytesPerSecond = activeConfig.sampleRateHz * format.channels * bytesPerSample

        lastUnderrunCount = track.underrunCount
        Log.d(TAG, "AudioTrack initialized successfully: format=$format, activeConfig=$activeConfig, bufferSize=$bufferSizeBytes")
        track.play()
        try { track.setVolume(1.0f) } catch (_: Exception) { /* best-effort */ }

        val resampler = if (format.sampleRateHz != activeConfig.sampleRateHz) {
            Log.i(TAG, "AudioTrack resampler engaged: ${format.sampleRateHz}Hz -> ${activeConfig.sampleRateHz}Hz")
            LinearResampler(format.channels, format.sampleRateHz, activeConfig.sampleRateHz)
        } else {
            null
        }

        val srcBytesPerSample = (format.bitDepth / 8).coerceAtLeast(2)
        val frameSize = format.channels * srcBytesPerSample
        val rawBufferSize = this.bufferSizeBytes.coerceAtLeast(1024)
        val alignedBufferSize = (rawBufferSize / frameSize) * frameSize
        val readBuffer = ByteArray(alignedBufferSize)
        totalBytesWrittenAtomic.set(0L)


        while (running.get()) {
            val n = decoder.read(readBuffer)
            if (n == -1) {
                Log.d(TAG, "Decoder returned EOF (-1)")
                onCompletion()
                break
            }
            if (n <= 0) continue

            verifier.recordSourceBytes(readBuffer, n)

            val toWriteBytes = prepareWriteBuffer(readBuffer, n, format.bitDepth, activeConfig.bitDepthMode, resampler, activeConfig.sampleRateHz)

            val currentSettings = settingsRepository.current
            applyGainStages(toWriteBytes, activeConfig.bitDepthMode, currentSettings, tagGainMultiplier)
            crossfeed?.processBuffer(toWriteBytes, activeConfig.bitDepthMode)

            // AudioTrack.write(byte[], offset, size) is only valid for the byte-native
            // encodings (8/16/24-packed/32-bit int). A track configured with
            // ENCODING_PCM_FLOAT rejects that overload outright — every call returns
            // ERROR_INVALID_OPERATION (-3), not an intermittent failure — so a
            // float-encoded track has to go through the float[] overload instead, on
            // a view of the exact same bytes (prepareWriteBuffer already packed them
            // as little-endian float32, see convert24ToFloat/convert16ToFloat/etc.).
            val fullyWritten = if (activeConfig.bitDepthMode == BitDepthMode.PCM_FLOAT) {
                writeFloatBuffer(track, toWriteBytes)
            } else {
                writeByteBuffer(track, toWriteBytes)
            }

            if (fullyWritten) {
                verifier.recordTransmittedBytes(toWriteBytes, toWriteBytes.size)
                totalBytesWrittenAtomic.addAndGet(toWriteBytes.size.toLong())
            }

            val underruns = track.underrunCount
            if (underruns > lastUnderrunCount) {
                repeat(underruns - lastUnderrunCount) { onDropout() }
                dropouts.addAndGet(underruns - lastUnderrunCount)
                lastUnderrunCount = underruns
            }
        }
    }

    /** Writes via the byte[] overload — valid for PCM_16 / PCM_24_PACKED / PCM_32_INT.
     *  Returns false (without throwing) if AudioTrack.write reports an error, so the
     *  caller can skip counting these bytes as genuinely transmitted. */
    private fun writeByteBuffer(track: AudioTrack, data: ByteArray): Boolean {
        var offset = 0
        while (offset < data.size && running.get()) {
            val written = track.write(data, offset, data.size - offset)
            if (written < 0) {
                Log.e(TAG, "AudioTrack.write returned error code: $written")
                return false
            }
            if (written == 0) {
                try {
                    Thread.sleep(10)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return false
                }
                continue
            }
            offset += written
        }
        return true
    }

    /**
     * Writes via the float[] overload — the *only* overload a track configured with
     * ENCODING_PCM_FLOAT accepts; AudioTrack.write(byte[], ...) returns
     * ERROR_INVALID_OPERATION unconditionally for a float-encoded track regardless of
     * whether the byte content itself is valid. [data] is expected to already be
     * little-endian float32 bytes (a whole number of 4-byte samples — see
     * convert24ToFloat/convert16ToFloat/convert32IntToFloat), which this reinterprets
     * as a FloatArray without altering any sample values.
     */
    private fun writeFloatBuffer(track: AudioTrack, data: ByteArray): Boolean {
        val floats = java.nio.ByteBuffer.wrap(data)
            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
            .asFloatBuffer()
            .let { fb -> FloatArray(fb.remaining()).also { fb.get(it) } }
        var offset = 0
        while (offset < floats.size && running.get()) {
            val written = track.write(floats, offset, floats.size - offset, AudioTrack.WRITE_BLOCKING)
            if (written < 0) {
                Log.e(TAG, "AudioTrack.write (float) returned error code: $written")
                return false
            }
            if (written == 0) {
                try {
                    Thread.sleep(10)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return false
                }
                continue
            }
            offset += written
        }
        return true
    }

    private fun buildCandidateConfigs(format: PcmFormat): List<AudioTrackConfig> {
        val list = mutableListOf<AudioTrackConfig>()
        val isHighRes = format.bitDepth > 16

        // ── Candidate ordering rationale ──────────────────────────────────────────
        // Android AudioTrack.Builder ALWAYS returns STATE_INITIALIZED regardless of
        // whether the hardware HAL actually supports the requested sample rate. The
        // builder does NOT validate against the HAL at construction time — it only
        // finds out later (silently) when AudioFlinger tries to route the stream.
        //
        // On devices whose internal DAC is limited to 44.1/48kHz (the majority of
        // Android phones, including the TECNO LJ7 which only supports [44100, 48000]
        // per its dumpsys audio profile), opening an AudioTrack at 96kHz will
        // initialize successfully, accept write() calls without returning an error,
        // and then produce complete silence — because AudioFlinger's resampler either
        // doesn't activate for non-mixing paths or the MediaTek HAL discards the data.
        //
        // The only robust strategy for internal-DAC playback is:
        //   1. Try the native rate + FLOAT first (bit-perfect on devices that support it).
        //   2. Immediately fall back to 48kHz + FLOAT — AudioFlinger's SRC always runs
        //      at this boundary and the device is guaranteed to support 48kHz.
        //   3. Try 44.1kHz + FLOAT as an alternative common HAL rate.
        //   4. Fall back to 48kHz + 16-bit as a hard guarantee (works on every device).
        //
        // For a bit-perfect research app the "right" thing to show the user is the
        // format badge ("RESAMPLED") rather than silence — audible + correctly labelled
        // beats bit-perfect + completely inaudible.
        // ─────────────────────────────────────────────────────────────────────────

        val targetHardwareRate = if (format.sampleRateHz % 44100 == 0) 44100 else 48000

        // 1. Primary candidate for Android internal system playback:
        // Standard hardware rate (44.1kHz or 48kHz) + 16-bit PCM (ENCODING_PCM_16BIT).
        // 100% of Android phone internal DAC HALs (including TECNO LJ7 MediaTek HAL) accept
        // and output ENCODING_PCM_16BIT to internal speakers. Higher-bit-depth source PCM
        // (24-bit/32-bit) and higher sample rates (96kHz/192kHz) are cleanly converted and
        // downsampled by prepareWriteBuffer and LinearResampler.
        list.add(AudioTrackConfig(targetHardwareRate, AudioFormat.ENCODING_PCM_16BIT, BitDepthMode.PCM_16))

        // 2. Native rate + 16-bit PCM if different from targetHardwareRate
        if (format.sampleRateHz != targetHardwareRate) {
            list.add(AudioTrackConfig(format.sampleRateHz, AudioFormat.ENCODING_PCM_16BIT, BitDepthMode.PCM_16))
        }

        // 3. Fallback candidates (FLOAT / 24-bit packed) for devices whose HAL mixer accepts FLOAT
        list.add(AudioTrackConfig(targetHardwareRate, AudioFormat.ENCODING_PCM_FLOAT, BitDepthMode.PCM_FLOAT))
        if (format.sampleRateHz != targetHardwareRate) {
            list.add(AudioTrackConfig(format.sampleRateHz, AudioFormat.ENCODING_PCM_FLOAT, BitDepthMode.PCM_FLOAT))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(
                AudioTrackConfig(
                    format.sampleRateHz,
                    AudioFormat.ENCODING_PCM_24BIT_PACKED,
                    BitDepthMode.PCM_24_PACKED
                )
            )
        }

        // 4. Hard final fallbacks
        if (format.sampleRateHz != 48000) {
            list.add(AudioTrackConfig(48000, AudioFormat.ENCODING_PCM_16BIT, BitDepthMode.PCM_16))
        }
        if (format.sampleRateHz != 44100) {
            list.add(AudioTrackConfig(44100, AudioFormat.ENCODING_PCM_16BIT, BitDepthMode.PCM_16))
        }

        return list
    }


    private fun prepareWriteBuffer(
        readBuffer: ByteArray,
        n: Int,
        sourceBitDepth: Int,
        targetMode: BitDepthMode,
        resampler: LinearResampler?,
        sampleRateHz: Int
    ): ByteArray {
        var floats = when (sourceBitDepth) {
            24 -> convert24ToFloatArray(readBuffer, n)
            32 -> convert32IntToFloatArray(readBuffer, n)
            16 -> convert16ToFloatArray(readBuffer, n)
            8 -> convert8ToFloatArray(readBuffer, n)
            else -> convert16ToFloatArray(readBuffer, n)
        }

        if (resampler != null) {
            floats = resampler.process(floats)
        }

        val settings = settingsRepository.current
        val isKaraokeActive = (settings.karaokeModeEnabled || settings.aiVocalSuppressMode) &&
            settings.karaokeModeType != com.thesis.bitperfectusb.domain.model.KaraokeModeType.OFF
        if (isKaraokeActive) {
            if (!isPlayingCachedStem) {
                // Apply live real-time AI spectral vocal remover
                vocalIsolator.processFloats(
                    floats = floats,
                    sampleRate = sampleRateHz,
                    mode = settings.karaokeModeType,
                    strength = settings.karaokeVocalSuppressionStrength,
                    preserveBass = settings.karaokeBassPreservation,
                    keyShiftSemitones = settings.karaokeKeyShiftSemitones
                )
            } else if (settings.karaokeKeyShiftSemitones != 0) {
                // Audio is already pre-rendered clean stem in cache; only apply pitch shift if active
                vocalIsolator.processFloats(
                    floats = floats,
                    sampleRate = sampleRateHz,
                    mode = com.thesis.bitperfectusb.domain.model.KaraokeModeType.OFF,
                    strength = 0f,
                    preserveBass = true,
                    keyShiftSemitones = settings.karaokeKeyShiftSemitones
                )
            }
        }

        return when (targetMode) {
            BitDepthMode.PCM_FLOAT -> packFloatArrayToBytes(floats)
            BitDepthMode.PCM_16 -> packFloatArrayTo16BitBytes(floats)
            BitDepthMode.PCM_24_PACKED -> packFloatArrayTo24BitBytes(floats)
            BitDepthMode.PCM_32_INT -> packFloatArrayTo32BitBytes(floats)
        }
    }

    private fun convert24ToFloatArray(input: ByteArray, length: Int): FloatArray {
        val sampleCount = length / 3
        val output = FloatArray(sampleCount)
        val factor = 1.0f / 8388608.0f // 2^23
        for (i in 0 until sampleCount) {
            val srcOffset = i * 3
            val b0 = input[srcOffset].toInt() and 0xFF
            val b1 = input[srcOffset + 1].toInt() and 0xFF
            val b2 = input[srcOffset + 2].toInt()
            val sampleInt = (b2 shl 16) or (b1 shl 8) or b0
            output[i] = (sampleInt * factor).coerceIn(-1.0f, 1.0f)
        }
        return output
    }

    private fun convert16ToFloatArray(input: ByteArray, length: Int): FloatArray {
        val sampleCount = length / 2
        val output = FloatArray(sampleCount)
        val factor = 1.0f / 32768.0f
        for (i in 0 until sampleCount) {
            val srcOffset = i * 2
            val low = input[srcOffset].toInt() and 0xFF
            val high = input[srcOffset + 1].toInt() shl 8
            val sample = (high or low).toShort()
            output[i] = (sample * factor).coerceIn(-1.0f, 1.0f)
        }
        return output
    }

    private fun convert32IntToFloatArray(input: ByteArray, length: Int): FloatArray {
        val sampleCount = length / 4
        val output = FloatArray(sampleCount)
        val factor = 1.0f / 2147483648.0f // 2^31
        for (i in 0 until sampleCount) {
            val srcOffset = i * 4
            val b0 = input[srcOffset].toInt() and 0xFF
            val b1 = input[srcOffset + 1].toInt() and 0xFF
            val b2 = input[srcOffset + 2].toInt() and 0xFF
            val b3 = input[srcOffset + 3].toInt()
            val sampleInt = (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
            output[i] = (sampleInt * factor).coerceIn(-1.0f, 1.0f)
        }
        return output
    }

    private fun convert8ToFloatArray(input: ByteArray, length: Int): FloatArray {
        val output = FloatArray(length)
        for (i in 0 until length) {
            val u = input[i].toInt() and 0xFF
            output[i] = ((u - 128) / 128.0f).coerceIn(-1.0f, 1.0f)
        }
        return output
    }

    private fun packFloatArrayToBytes(floats: FloatArray): ByteArray {
        val output = ByteArray(floats.size * 4)
        for (i in floats.indices) {
            val bits = java.lang.Float.floatToRawIntBits(floats[i])
            val dstOffset = i * 4
            output[dstOffset] = (bits and 0xFF).toByte()
            output[dstOffset + 1] = ((bits ushr 8) and 0xFF).toByte()
            output[dstOffset + 2] = ((bits ushr 16) and 0xFF).toByte()
            output[dstOffset + 3] = ((bits ushr 24) and 0xFF).toByte()
        }
        return output
    }

    private fun packFloatArrayTo16BitBytes(floats: FloatArray): ByteArray {
        val output = ByteArray(floats.size * 2)
        for (i in floats.indices) {
            val s = (floats[i] * 32767.0f).coerceIn(-32768.0f, 32767.0f).toInt().toShort()
            val dstOffset = i * 2
            output[dstOffset] = (s.toInt() and 0xFF).toByte()
            output[dstOffset + 1] = ((s.toInt() ushr 8) and 0xFF).toByte()
        }
        return output
    }

    private fun packFloatArrayTo24BitBytes(floats: FloatArray): ByteArray {
        val output = ByteArray(floats.size * 3)
        for (i in floats.indices) {
            val s = (floats[i] * 8388607.0f).coerceIn(-8388608.0f, 8388607.0f).toInt()
            val dstOffset = i * 3
            output[dstOffset] = (s and 0xFF).toByte()
            output[dstOffset + 1] = ((s ushr 8) and 0xFF).toByte()
            output[dstOffset + 2] = ((s ushr 16) and 0xFF).toByte()
        }
        return output
    }

    private fun packFloatArrayTo32BitBytes(floats: FloatArray): ByteArray {
        val output = ByteArray(floats.size * 4)
        for (i in floats.indices) {
            val s = (floats[i] * 2147483647.0f).coerceIn(-2147483648.0f, 2147483647.0f).toLong().toInt()
            val dstOffset = i * 4
            output[dstOffset] = (s and 0xFF).toByte()
            output[dstOffset + 1] = ((s ushr 8) and 0xFF).toByte()
            output[dstOffset + 2] = ((s ushr 16) and 0xFF).toByte()
            output[dstOffset + 3] = ((s ushr 24) and 0xFF).toByte()
        }
        return output
    }



    /**
     * Combines every simple per-sample gain source into one multiplier applied
     * in a single pass — EQ's average-band gain (the existing simplified
     * whole-buffer EQ model, not a real per-band filter) and the
     * ReplayGain/Normalization tag-derived multiplier computed once at
     * [start]. Folding them together avoids a second full pass over the
     * buffer for what's mathematically just another multiplication.
     */
    private fun applyGainStages(buffer: ByteArray, mode: BitDepthMode, settings: UserSettings, tagGainMultiplier: Float) {
        val eqMultiplier = if (settings.eqEnabled) {
            val avgGainDb = settings.eqGains.average().toFloat()
            if (avgGainDb != 0f) Math.pow(10.0, avgGainDb / 20.0).toFloat() else 1f
        } else {
            1f
        }
        val multiplier = eqMultiplier * tagGainMultiplier
        if (multiplier == 1f) return

        when (mode) {
            BitDepthMode.PCM_16 -> {
                for (i in 0 until buffer.size - 1 step 2) {
                    val low = buffer[i].toInt() and 0xFF
                    val high = buffer[i + 1].toInt() shl 8
                    val sample = (high or low).toShort()
                    val adjusted = (sample * multiplier).coerceIn(-32768f, 32767f).toInt().toShort()
                    buffer[i] = (adjusted.toInt() and 0xFF).toByte()
                    buffer[i + 1] = ((adjusted.toInt() ushr 8) and 0xFF).toByte()
                }
            }
            BitDepthMode.PCM_FLOAT -> {
                for (i in 0 until buffer.size - 3 step 4) {
                    val b0 = buffer[i].toInt() and 0xFF
                    val b1 = buffer[i + 1].toInt() and 0xFF
                    val b2 = buffer[i + 2].toInt() and 0xFF
                    val b3 = buffer[i + 3].toInt() and 0xFF
                    val bits = (b3 shl 24) or (b2 shl 16) or (b1 shl 8) or b0
                    val floatVal = java.lang.Float.intBitsToFloat(bits)
                    val adjusted = (floatVal * multiplier).coerceIn(-1.0f, 1.0f)
                    val newBits = java.lang.Float.floatToRawIntBits(adjusted)
                    buffer[i] = (newBits and 0xFF).toByte()
                    buffer[i + 1] = ((newBits ushr 8) and 0xFF).toByte()
                    buffer[i + 2] = ((newBits ushr 16) and 0xFF).toByte()
                    buffer[i + 3] = ((newBits ushr 24) and 0xFF).toByte()
                }
            }
            else -> { /* 24-bit packed / 32-bit int gain bypass for raw pass-through */ }
        }
    }

    override fun stop() {
        running.set(false)
        val thread = playbackThread
        if (thread != null) {
            thread.join(500)
            if (thread.isAlive) {
                // Still blocked (most likely inside AudioTrack.write() waiting on a
                // stalled HAL, or inside the decoder's dequeue call). Interrupting
                // unblocks Thread.sleep()/wait()-based blocks; a real HAL stall in
                // write() won't respond to interrupt, so give it one more bounded
                // wait rather than returning while it might still be touching the
                // decoder — the caller (PlaybackController.stop()) closes the
                // decoder right after this returns, and that race is what produces
                // MediaCodec's "pending dequeue output buffer request canceled".
                thread.interrupt()
                thread.join(500)
            }
        }
        playbackThread = null
        audioTrack?.let {
            try { it.stop() } catch (_: IllegalStateException) { /* already stopped */ }
            try { it.release() } catch (_: Throwable) { /* best-effort cleanup */ }
        }
        audioTrack = null
    }

    override fun setBufferSize(bytes: Int) {
        bufferSizeBytes = bytes
    }

    override fun currentLatencyEstimateMs(): Double {
        val bufferLatency = if (bytesPerSecond > 0) (bufferSizeBytes.toDouble() / bytesPerSecond) * 1000.0 else 0.0
        return bufferLatency + MIXER_OVERHEAD_MS
    }

    override fun currentPositionMs(): Long {
        if (bytesPerSecond <= 0) return 0L
        return (totalBytesWrittenAtomic.get() / bytesPerSecond.toDouble() * 1000.0).toLong()
    }

    override fun verificationSnapshot() = verifier.snapshot()

    private fun truncate24To16(input: ByteArray, length: Int): ByteArray {
        val sampleCount = length / 3
        val output = ByteArray(sampleCount * 2)
        for (i in 0 until sampleCount) {
            val srcOffset = i * 3
            val dstOffset = i * 2
            output[dstOffset] = input[srcOffset + 1]
            output[dstOffset + 1] = input[srcOffset + 2]
        }
        return output
    }

    private fun truncate32To16(input: ByteArray, length: Int): ByteArray {
        val sampleCount = length / 4
        val output = ByteArray(sampleCount * 2)
        for (i in 0 until sampleCount) {
            val srcOffset = i * 4
            val dstOffset = i * 2
            output[dstOffset] = input[srcOffset + 2]
            output[dstOffset + 1] = input[srcOffset + 3]
        }
        return output
    }

    private fun truncate32To24(input: ByteArray, length: Int): ByteArray {
        // Drops the least-significant byte of each little-endian 32-bit sample,
        // keeping the top 24 bits — same truncation approach as truncate32To16
        // above, just stopping one byte later.
        val sampleCount = length / 4
        val output = ByteArray(sampleCount * 3)
        for (i in 0 until sampleCount) {
            val srcOffset = i * 4
            val dstOffset = i * 3
            output[dstOffset] = input[srcOffset + 1]
            output[dstOffset + 1] = input[srcOffset + 2]
            output[dstOffset + 2] = input[srcOffset + 3]
        }
        return output
    }

    private fun convert8To16(input: ByteArray, length: Int): ByteArray {
        val output = ByteArray(length * 2)
        for (i in 0 until length) {
            val uval = input[i].toInt() and 0xFF
            val s16 = ((uval - 128) shl 8).toShort()
            val dstOffset = i * 2
            output[dstOffset] = (s16.toInt() and 0xFF).toByte()
            output[dstOffset + 1] = ((s16.toInt() ushr 8) and 0xFF).toByte()
        }
        return output
    }

    companion object {
        private const val MIXER_OVERHEAD_MS = 60.0
        private const val TAG = "AudioTrackEngine"
    }
}

/**
 * High-performance linear-interpolation PCM resampler for converting audio streams
 * between sample rates (e.g. 96kHz -> 48kHz, 88.2kHz -> 44.1kHz, 192kHz -> 48kHz).
 * Preserves phase accumulator state across buffer chunks for seamless, click-free playback.
 */
private class LinearResampler(
    private val channels: Int,
    private val inRate: Int,
    private val outRate: Int
) {
    private var phase = 0.0

    /**
     * Resamples an interleaved FloatArray of PCM samples from [inRate] to [outRate].
     * Sample values should be in range [-1.0f, 1.0f].
     */
    fun process(input: FloatArray): FloatArray {
        if (inRate == outRate || input.isEmpty()) return input

        val inputFrames = input.size / channels
        if (inputFrames == 0) return input

        val ratio = inRate.toDouble() / outRate.toDouble()
        val maxOutputFrames = (inputFrames / ratio).toInt() + 4
        val output = FloatArray(maxOutputFrames * channels)

        var outFrameIdx = 0
        while (true) {
            val srcFrameIdx = phase.toInt()
            if (srcFrameIdx >= inputFrames - 1) {
                phase -= srcFrameIdx
                break
            }

            val frac = (phase - srcFrameIdx).toFloat()

            for (ch in 0 until channels) {
                val s0 = input[srcFrameIdx * channels + ch]
                val s1 = input[(srcFrameIdx + 1) * channels + ch]
                val interpolated = s0 + frac * (s1 - s0)
                if (outFrameIdx * channels + ch < output.size) {
                    output[outFrameIdx * channels + ch] = interpolated
                }
            }

            outFrameIdx++
            phase += ratio
        }

        return output.copyOf(outFrameIdx * channels)
    }
}