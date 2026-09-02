package com.thesis.bitperfectusb.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.usecase.ObservePlaybackStateUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Settings > Keep Screen On During Playback. Mitigates the exact "OS Power
 * Management" internal validity threat Section 5.2.1 documents — a
 * screen-off device can trigger Doze/CPU throttling that inflates latency
 * and dropouts, confounding the very measurements this app is trying to
 * take cleanly. This is app-scoped (consumed once from MainActivity) rather
 * than screen-scoped, so it's a tiny dedicated ViewModel with exactly one
 * job, using the same koinViewModel() mechanism every other screen already uses.
 */
class KeepScreenOnViewModel(
    observePlaybackStateUseCase: ObservePlaybackStateUseCase,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val shouldKeepScreenOn: StateFlow<Boolean> = combine(
        observePlaybackStateUseCase(), settingsRepository.settings
    ) { playback, settings ->
        playback.isPlaying && settings.keepScreenOnDuringPlayback
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
