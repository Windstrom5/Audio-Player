package com.thesis.bitperfectusb.data.settings

import android.content.Context
import com.thesis.bitperfectusb.domain.model.BufferMode
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.PlaybackConfig
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the audiophile-tuning settings (Section: "add more settings for
 * audiophile features"). Plain SharedPreferences rather than DataStore — this
 * is a handful of simple values changed rarely by direct user action, so a
 * synchronous cached StateFlow over SharedPreferences is simpler and avoids
 * pulling in another dependency for no real benefit at this scale.
 */
class SettingsRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadFromDisk())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    val current: UserSettings get() = _settings.value

    fun setBufferMode(mode: BufferMode) {
        prefs.edit().putString(KEY_BUFFER_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(bufferMode = mode)
    }

    fun setManualBufferSize(bytes: Int) {
        val clamped = bytes.coerceIn(PlaybackConfig.MIN_BUFFER_BYTES, PlaybackConfig.MAX_BUFFER_BYTES)
        prefs.edit().putInt(KEY_MANUAL_BUFFER, clamped).apply()
        _settings.value = _settings.value.copy(manualBufferSizeBytes = clamped)
    }

    fun setDefaultEngine(engine: EngineType) {
        prefs.edit().putString(KEY_DEFAULT_ENGINE, engine.name).apply()
        _settings.value = _settings.value.copy(defaultEngine = engine)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _settings.value = _settings.value.copy(keepScreenOnDuringPlayback = enabled)
    }

    fun setUsbRequestPoolSize(size: Int) {
        val clamped = size.coerceIn(UserSettings.USB_REQUEST_POOL_RANGE.first, UserSettings.USB_REQUEST_POOL_RANGE.last)
        prefs.edit().putInt(KEY_USB_POOL_SIZE, clamped).apply()
        _settings.value = _settings.value.copy(usbRequestPoolSize = clamped)
    }

    fun setUsbTransferStrategy(strategy: UsbTransferStrategy) {
        prefs.edit().putString(KEY_TRANSFER_STRATEGY, strategy.name).apply()
        _settings.value = _settings.value.copy(usbTransferStrategy = strategy)
    }

    fun setEqEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EQ_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(eqEnabled = enabled)
    }

    fun setEqGains(gains: List<Float>) {
        val serialized = gains.joinToString(",") { "%.1f".format(it) }
        prefs.edit().putString(KEY_EQ_GAINS, serialized).apply()
        _settings.value = _settings.value.copy(eqGains = gains)
    }

    fun setCrossfeedEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CROSSFEED_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(crossfeedEnabled = enabled)
    }

    fun setCrossfeedStrength(strength: Float) {
        val clamped = strength.coerceIn(0f, 1f)
        prefs.edit().putFloat(KEY_CROSSFEED_STRENGTH, clamped).apply()
        _settings.value = _settings.value.copy(crossfeedStrength = clamped)
    }

    fun setReplayGainEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REPLAY_GAIN_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(replayGainEnabled = enabled)
    }

    fun setNormalizationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NORMALIZATION_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(normalizationEnabled = enabled)
    }

    fun setEqPreset(preset: com.thesis.bitperfectusb.domain.model.EqPreset) {
        prefs.edit().putString(KEY_EQ_PRESET, preset.name).apply()
        setEqGains(preset.gains)
        _settings.value = _settings.value.copy(eqPreset = preset, eqGains = preset.gains)
    }

    fun setAutoBypassDspInBitPerfect(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BYPASS_DSP, enabled).apply()
        _settings.value = _settings.value.copy(autoBypassDspInBitPerfect = enabled)
    }

    fun setUsbBufferLatency(latencyMs: Int) {
        prefs.edit().putInt(KEY_USB_BUFFER_LATENCY, latencyMs).apply()
        _settings.value = _settings.value.copy(usbBufferLatencyMs = latencyMs)
    }

    fun setHardwareVolumeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HW_VOL_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(hardwareVolumeEnabled = enabled)
    }

    fun setHardwareVolumeSteps(steps: Int) {
        prefs.edit().putInt(KEY_HW_VOL_STEPS, steps).apply()
        _settings.value = _settings.value.copy(hardwareVolumeSteps = steps)
    }

    fun setDsdMode(mode: com.thesis.bitperfectusb.domain.model.DsdPlaybackMode) {
        prefs.edit().putString(KEY_DSD_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(dsdMode = mode)
    }

    fun setSpatialWidth(width: Float) {
        val clamped = width.coerceIn(0.0f, 2.0f)
        prefs.edit().putFloat(KEY_SPATIAL_WIDTH, clamped).apply()
        _settings.value = _settings.value.copy(spatialWidth = clamped)
    }

    fun setReverbEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REVERB_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(reverbEnabled = enabled)
    }

    fun setReverbPreset(preset: com.thesis.bitperfectusb.domain.model.ReverbPreset) {
        prefs.edit().putString(KEY_REVERB_PRESET, preset.name).apply()
        _settings.value = _settings.value.copy(reverbPreset = preset)
    }

    fun setReverbWetDry(wetDry: Float) {
        val clamped = wetDry.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_REVERB_WET_DRY, clamped).apply()
        _settings.value = _settings.value.copy(reverbWetDry = clamped)
    }

    fun setReplayGainMode(mode: com.thesis.bitperfectusb.domain.model.ReplayGainMode) {
        prefs.edit().putString(KEY_REPLAY_GAIN_MODE, mode.name).apply()
        _settings.value = _settings.value.copy(replayGainMode = mode)
    }

    fun setReplayGainPreampDb(preampDb: Float) {
        val clamped = preampDb.coerceIn(-12.0f, 12.0f)
        prefs.edit().putFloat(KEY_REPLAY_GAIN_PREAMP, clamped).apply()
        _settings.value = _settings.value.copy(replayGainPreampDb = clamped)
    }

    fun setPreventClipping(prevent: Boolean) {
        prefs.edit().putBoolean(KEY_PREVENT_CLIPPING, prevent).apply()
        _settings.value = _settings.value.copy(preventClipping = prevent)
    }

    fun setAiAutoPilotEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_AUTO_PILOT, enabled).apply()
        _settings.value = _settings.value.copy(aiAutoPilotEnabled = enabled)
    }

    fun setAiDseeUpscalerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_DSEE, enabled).apply()
        _settings.value = _settings.value.copy(aiDseeUpscalerEnabled = enabled)
    }

    fun setAiVocalSuppressMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_VOCAL_SUPPRESS, enabled).apply()
        _settings.value = _settings.value.copy(aiVocalSuppressMode = enabled, karaokeModeEnabled = enabled)
    }

    fun setKaraokeModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KARAOKE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(karaokeModeEnabled = enabled, aiVocalSuppressMode = enabled)
    }

    fun setKaraokeModeType(type: com.thesis.bitperfectusb.domain.model.KaraokeModeType) {
        val isEnabled = type != com.thesis.bitperfectusb.domain.model.KaraokeModeType.OFF
        prefs.edit()
            .putString(KEY_KARAOKE_TYPE, type.name)
            .putBoolean(KEY_KARAOKE_ENABLED, isEnabled)
            .putBoolean(KEY_AI_VOCAL_SUPPRESS, isEnabled)
            .apply()
        _settings.value = _settings.value.copy(
            karaokeModeType = type,
            karaokeModeEnabled = isEnabled,
            aiVocalSuppressMode = isEnabled
        )
    }

    fun setKaraokeVocalSuppressionStrength(strength: Float) {
        val clamped = strength.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_KARAOKE_STRENGTH, clamped).apply()
        _settings.value = _settings.value.copy(karaokeVocalSuppressionStrength = clamped)
    }

    fun setKaraokeBassPreservation(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KARAOKE_BASS_PRESERVE, enabled).apply()
        _settings.value = _settings.value.copy(karaokeBassPreservation = enabled)
    }

    fun setKaraokeKeyShiftSemitones(semitones: Int) {
        val clamped = semitones.coerceIn(-6, 6)
        prefs.edit().putInt(KEY_KARAOKE_KEY_SHIFT, clamped).apply()
        _settings.value = _settings.value.copy(karaokeKeyShiftSemitones = clamped)
    }

    fun setKaraokeMicScoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KARAOKE_MIC_SCORE, enabled).apply()
        _settings.value = _settings.value.copy(karaokeMicScoringEnabled = enabled)
    }

    private fun loadFromDisk(): UserSettings {
        val bufferModeName = prefs.getString(KEY_BUFFER_MODE, BufferMode.ADAPTIVE.name) ?: BufferMode.ADAPTIVE.name
        val transferStratName = prefs.getString(KEY_TRANSFER_STRATEGY, UsbTransferStrategy.PIPELINED.name) ?: UsbTransferStrategy.PIPELINED.name
        val engineName = prefs.getString(KEY_DEFAULT_ENGINE, EngineType.CUSTOM_USB_DIRECT.name) ?: EngineType.CUSTOM_USB_DIRECT.name
        val dsdModeName = prefs.getString(KEY_DSD_MODE, com.thesis.bitperfectusb.domain.model.DsdPlaybackMode.DOP.name) ?: com.thesis.bitperfectusb.domain.model.DsdPlaybackMode.DOP.name
        val reverbPresetName = prefs.getString(KEY_REVERB_PRESET, com.thesis.bitperfectusb.domain.model.ReverbPreset.STUDIO_ROOM.name) ?: com.thesis.bitperfectusb.domain.model.ReverbPreset.STUDIO_ROOM.name
        val replayGainModeName = prefs.getString(KEY_REPLAY_GAIN_MODE, com.thesis.bitperfectusb.domain.model.ReplayGainMode.TRACK.name) ?: com.thesis.bitperfectusb.domain.model.ReplayGainMode.TRACK.name
        val eqPresetName = prefs.getString(KEY_EQ_PRESET, com.thesis.bitperfectusb.domain.model.EqPreset.FLAT.name) ?: com.thesis.bitperfectusb.domain.model.EqPreset.FLAT.name
        val karaokeTypeName = prefs.getString(KEY_KARAOKE_TYPE, com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY.name) ?: com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY.name

        val savedGainsStr = prefs.getString(KEY_EQ_GAINS, null)
        val gains = if (savedGainsStr != null) {
            savedGainsStr.split(",").mapNotNull { it.toFloatOrNull() }.takeIf { it.size == 10 }
                ?: listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        } else {
            listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        }

        return UserSettings(
            bufferMode = try { BufferMode.valueOf(bufferModeName) } catch (_: Exception) { BufferMode.ADAPTIVE },
            manualBufferSizeBytes = prefs.getInt(KEY_MANUAL_BUFFER, PlaybackConfig.DEFAULT_BUFFER_BYTES),
            defaultEngine = try { EngineType.valueOf(engineName) } catch (_: Exception) { EngineType.CUSTOM_USB_DIRECT },
            keepScreenOnDuringPlayback = prefs.getBoolean(KEY_KEEP_SCREEN_ON, true),
            usbRequestPoolSize = prefs.getInt(KEY_USB_POOL_SIZE, 4).coerceIn(UserSettings.USB_REQUEST_POOL_RANGE),
            usbTransferStrategy = try { UsbTransferStrategy.valueOf(transferStratName) } catch (_: Exception) { UsbTransferStrategy.PIPELINED },
            usbBufferLatencyMs = prefs.getInt(KEY_USB_BUFFER_LATENCY, 32),
            hardwareVolumeEnabled = prefs.getBoolean(KEY_HW_VOL_ENABLED, true),
            hardwareVolumeSteps = prefs.getInt(KEY_HW_VOL_STEPS, 100),
            dsdMode = try { com.thesis.bitperfectusb.domain.model.DsdPlaybackMode.valueOf(dsdModeName) } catch (_: Exception) { com.thesis.bitperfectusb.domain.model.DsdPlaybackMode.DOP },
            eqEnabled = prefs.getBoolean(KEY_EQ_ENABLED, false),
            eqGains = gains,
            eqPreset = try { com.thesis.bitperfectusb.domain.model.EqPreset.valueOf(eqPresetName) } catch (_: Exception) { com.thesis.bitperfectusb.domain.model.EqPreset.FLAT },
            crossfeedEnabled = prefs.getBoolean(KEY_CROSSFEED_ENABLED, false),
            crossfeedStrength = prefs.getFloat(KEY_CROSSFEED_STRENGTH, 0.3f),
            spatialWidth = prefs.getFloat(KEY_SPATIAL_WIDTH, 1.0f),
            reverbEnabled = prefs.getBoolean(KEY_REVERB_ENABLED, false),
            reverbPreset = try { com.thesis.bitperfectusb.domain.model.ReverbPreset.valueOf(reverbPresetName) } catch (_: Exception) { com.thesis.bitperfectusb.domain.model.ReverbPreset.STUDIO_ROOM },
            reverbWetDry = prefs.getFloat(KEY_REVERB_WET_DRY, 0.25f),
            replayGainEnabled = prefs.getBoolean(KEY_REPLAY_GAIN_ENABLED, false),
            replayGainMode = try { com.thesis.bitperfectusb.domain.model.ReplayGainMode.valueOf(replayGainModeName) } catch (_: Exception) { com.thesis.bitperfectusb.domain.model.ReplayGainMode.TRACK },
            replayGainPreampDb = prefs.getFloat(KEY_REPLAY_GAIN_PREAMP, 0.0f),
            preventClipping = prefs.getBoolean(KEY_PREVENT_CLIPPING, true),
            normalizationEnabled = prefs.getBoolean(KEY_NORMALIZATION_ENABLED, false),
            autoBypassDspInBitPerfect = prefs.getBoolean(KEY_AUTO_BYPASS_DSP, true),
            aiAutoPilotEnabled = prefs.getBoolean(KEY_AI_AUTO_PILOT, false),
            aiDseeUpscalerEnabled = prefs.getBoolean(KEY_AI_DSEE, true),
            aiVocalSuppressMode = prefs.getBoolean(KEY_AI_VOCAL_SUPPRESS, false),
            karaokeModeEnabled = prefs.getBoolean(KEY_KARAOKE_ENABLED, false) || prefs.getBoolean(KEY_AI_VOCAL_SUPPRESS, false),
            karaokeModeType = try { com.thesis.bitperfectusb.domain.model.KaraokeModeType.valueOf(karaokeTypeName) } catch (_: Exception) { com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY },
            karaokeVocalSuppressionStrength = prefs.getFloat(KEY_KARAOKE_STRENGTH, 0.90f),
            karaokeBassPreservation = prefs.getBoolean(KEY_KARAOKE_BASS_PRESERVE, true),
            karaokeKeyShiftSemitones = prefs.getInt(KEY_KARAOKE_KEY_SHIFT, 0),
            karaokeMicScoringEnabled = prefs.getBoolean(KEY_KARAOKE_MIC_SCORE, false)
        )
    }

    companion object {
        private const val PREFS_NAME = "bitperfect_user_settings"
        private const val KEY_BUFFER_MODE = "buffer_mode"
        private const val KEY_MANUAL_BUFFER = "manual_buffer_bytes"
        private const val KEY_DEFAULT_ENGINE = "default_engine"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_USB_POOL_SIZE = "usb_request_pool_size"
        private const val KEY_TRANSFER_STRATEGY = "usb_transfer_strategy"
        private const val KEY_USB_BUFFER_LATENCY = "usb_buffer_latency"
        private const val KEY_HW_VOL_ENABLED = "hw_vol_enabled"
        private const val KEY_HW_VOL_STEPS = "hw_vol_steps"
        private const val KEY_DSD_MODE = "dsd_mode"
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_EQ_GAINS = "eq_gains"
        private const val KEY_EQ_PRESET = "eq_preset"
        private const val KEY_CROSSFEED_ENABLED = "crossfeed_enabled"
        private const val KEY_CROSSFEED_STRENGTH = "crossfeed_strength"
        private const val KEY_SPATIAL_WIDTH = "spatial_width"
        private const val KEY_REVERB_ENABLED = "reverb_enabled"
        private const val KEY_REVERB_PRESET = "reverb_preset"
        private const val KEY_REVERB_WET_DRY = "reverb_wet_dry"
        private const val KEY_REPLAY_GAIN_ENABLED = "replay_gain_enabled"
        private const val KEY_REPLAY_GAIN_MODE = "replay_gain_mode"
        private const val KEY_REPLAY_GAIN_PREAMP = "replay_gain_preamp"
        private const val KEY_PREVENT_CLIPPING = "prevent_clipping"
        private const val KEY_NORMALIZATION_ENABLED = "normalization_enabled"
        private const val KEY_AUTO_BYPASS_DSP = "auto_bypass_dsp"
        private const val KEY_AI_AUTO_PILOT = "ai_auto_pilot"
        private const val KEY_AI_DSEE = "ai_dsee"
        private const val KEY_AI_VOCAL_SUPPRESS = "ai_vocal_suppress"
        private const val KEY_KARAOKE_ENABLED = "karaoke_enabled"
        private const val KEY_KARAOKE_TYPE = "karaoke_type"
        private const val KEY_KARAOKE_STRENGTH = "karaoke_strength"
        private const val KEY_KARAOKE_BASS_PRESERVE = "karaoke_bass_preserve"
        private const val KEY_KARAOKE_KEY_SHIFT = "karaoke_key_shift"
        private const val KEY_KARAOKE_MIC_SCORE = "karaoke_mic_score"
    }
}
