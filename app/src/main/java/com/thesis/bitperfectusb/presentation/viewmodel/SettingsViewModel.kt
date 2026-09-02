package com.thesis.bitperfectusb.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.model.BufferMode
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.domain.model.UserSettings
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.domain.usecase.AddWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveWatchedFoldersUseCase
import com.thesis.bitperfectusb.domain.usecase.RemoveWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.RescanLibraryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val watchedFolders: List<WatchedFolder> = emptyList(),
    val userSettings: UserSettings = UserSettings(),
    val isBusy: Boolean = false,
    val error: String? = null
)

/**
 * Owns both library-source management (watched folders) and the audiophile
 * tuning settings — buffer mode, default engine, screen-on-during-playback,
 * and USB isochronous buffering depth. Every one of these maps to a real,
 * already-built tunable in the playback pipeline rather than a cosmetic toggle.
 */
class SettingsViewModel(
    observeWatchedFoldersUseCase: ObserveWatchedFoldersUseCase,
    private val addWatchedFolderUseCase: AddWatchedFolderUseCase,
    private val removeWatchedFolderUseCase: RemoveWatchedFolderUseCase,
    private val rescanLibraryUseCase: RescanLibraryUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        observeWatchedFoldersUseCase(), settingsRepository.settings, _isBusy
    ) { folders, userSettings, busy ->
        SettingsUiState(watchedFolders = folders, userSettings = userSettings, isBusy = busy, error = _error.value)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    // --- Library sources ---

    /** Called with the URI returned by ACTION_OPEN_DOCUMENT_TREE once the user picks a folder. */
    fun addFolder(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _isBusy.value = true
            _error.value = null
            try {
                addWatchedFolderUseCase(uri, displayName)
                rescanLibraryUseCase() // immediately pick up whatever's in the newly-added folder
            } catch (t: Throwable) {
                _error.value = "Couldn't add that folder: ${t.message}"
            } finally {
                _isBusy.value = false
            }
        }
    }

    fun removeFolder(id: Long) {
        viewModelScope.launch {
            _isBusy.value = true
            removeWatchedFolderUseCase(id)
            rescanLibraryUseCase() // prunes tracks that only came from the now-removed folder
            _isBusy.value = false
        }
    }

    // --- Audiophile tuning ---

    fun setBufferMode(mode: BufferMode) = settingsRepository.setBufferMode(mode)

    fun setManualBufferSize(bytes: Int) = settingsRepository.setManualBufferSize(bytes)

    fun setDefaultEngine(engine: EngineType) = settingsRepository.setDefaultEngine(engine)

    fun setKeepScreenOn(enabled: Boolean) = settingsRepository.setKeepScreenOn(enabled)

    fun setUsbRequestPoolSize(size: Int) = settingsRepository.setUsbRequestPoolSize(size)

    fun setUsbTransferStrategy(strategy: UsbTransferStrategy) = settingsRepository.setUsbTransferStrategy(strategy)

    fun setEqPreset(preset: com.thesis.bitperfectusb.domain.model.EqPreset) = settingsRepository.setEqPreset(preset)

    fun toggleAutoBypassDsp(enabled: Boolean) = settingsRepository.setAutoBypassDspInBitPerfect(enabled)
}

