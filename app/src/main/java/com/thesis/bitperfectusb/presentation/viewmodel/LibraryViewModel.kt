package com.thesis.bitperfectusb.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.domain.usecase.AddWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveDacProfilesUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveLibraryUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveWatchedFoldersUseCase
import com.thesis.bitperfectusb.domain.usecase.RemoveWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.RescanLibraryUseCase
import com.thesis.bitperfectusb.domain.usecase.SetQueueUseCase
import com.thesis.bitperfectusb.domain.usecase.StartPlaybackUseCase
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.usb.UsbDacManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab {
    TRACKS, ALBUMS, ARTISTS, FOLDERS, HIRES_DSD
}

enum class LibrarySortOption {
    TITLE, ARTIST, SAMPLE_RATE, BIT_DEPTH, FORMAT, DATE_ADDED, SMART_SHUFFLE
}

enum class SortDirection {
    ASCENDING, DESCENDING
}

data class LibraryFilterState(
    val query: String = "",
    val sortBy: LibrarySortOption = LibrarySortOption.TITLE,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val activeTab: LibraryTab = LibraryTab.TRACKS,
    val isGridView: Boolean = false
)

data class ScanStatus(
    val isScanning: Boolean = false,
    val lastScanCount: Int? = null,
    val nowPlayingTrackId: Long? = null,
    val error: String? = null
)

data class LibraryUiState(
    val tracks: List<AudioTrackModel> = emptyList(),
    val isScanning: Boolean = false,
    val lastScanCount: Int? = null,
    val nowPlayingTrackId: Long? = null,
    val watchedFolders: List<WatchedFolder> = emptyList(),
    val searchQuery: String = "",
    val sortBy: LibrarySortOption = LibrarySortOption.TITLE,
    val sortDirection: SortDirection = SortDirection.ASCENDING,
    val activeTab: LibraryTab = LibraryTab.TRACKS,
    val isGridView: Boolean = false,
    val selectedTrackForSpecs: AudioTrackModel? = null,
    val selectedTrackForMenu: AudioTrackModel? = null,
    val error: String? = null
)

class LibraryViewModel(
    observeLibraryUseCase: ObserveLibraryUseCase,
    private val rescanLibraryUseCase: RescanLibraryUseCase,
    private val startPlaybackUseCase: StartPlaybackUseCase,
    observeDacProfilesUseCase: ObserveDacProfilesUseCase,
    private val settingsRepository: SettingsRepository,
    private val addWatchedFolderUseCase: AddWatchedFolderUseCase,
    private val removeWatchedFolderUseCase: RemoveWatchedFolderUseCase,
    observeWatchedFoldersUseCase: ObserveWatchedFoldersUseCase,
    private val usbDacManager: UsbDacManager,
    private val setQueueUseCase: SetQueueUseCase
) : ViewModel() {

    private val tracks: StateFlow<List<AudioTrackModel>> = observeLibraryUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dacProfiles = observeDacProfilesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val watchedFolders = observeWatchedFoldersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    private val _lastScanCount = MutableStateFlow<Int?>(null)
    private val _nowPlayingTrackId = MutableStateFlow<Long?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortBy = MutableStateFlow(LibrarySortOption.TITLE)
    private val _sortDirection = MutableStateFlow(SortDirection.ASCENDING)
    private val _error = MutableStateFlow<String?>(null)
    private val _activeTab = MutableStateFlow(LibraryTab.TRACKS)
    private val _isGridView = MutableStateFlow(false)
    private val _selectedTrackForSpecs = MutableStateFlow<AudioTrackModel?>(null)
    private val _selectedTrackForMenu = MutableStateFlow<AudioTrackModel?>(null)

    init {
        rescan()
    }

    // Combine UI configurations
    private val filterState = combine(_searchQuery, _sortBy, _sortDirection, _activeTab, _isGridView) { query, sort, direction, tab, grid ->
        LibraryFilterState(query, sort, direction, tab, grid)
    }

    // Combine scanning and playing states
    private val scanStatus = combine(_isScanning, _lastScanCount, _nowPlayingTrackId, _error) { scanning, lastCount, nowPlayingId, err ->
        ScanStatus(scanning, lastCount, nowPlayingId, err)
    }

    // Combine track list and search/sorting/tab filters
    private val filteredAndSortedTracks = combine(tracks, filterState) { trackList, filter ->
        val tabFiltered = when (filter.activeTab) {
            LibraryTab.TRACKS -> trackList
            LibraryTab.ALBUMS -> trackList
            LibraryTab.ARTISTS -> trackList
            LibraryTab.FOLDERS -> trackList
            LibraryTab.HIRES_DSD -> trackList.filter { it.pcm.sampleRateHz >= 96000 || it.pcm.bitDepth >= 24 }
        }

        val filtered = tabFiltered.filter {
            it.title.contains(filter.query, ignoreCase = true) ||
                (it.artist?.contains(filter.query, ignoreCase = true) ?: false)
        }
        val sorted = when (filter.sortBy) {
            LibrarySortOption.TITLE -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
            LibrarySortOption.ARTIST -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist ?: "" })
            LibrarySortOption.SAMPLE_RATE -> filtered.sortedBy { it.pcm.sampleRateHz }
            LibrarySortOption.BIT_DEPTH -> filtered.sortedBy { it.pcm.bitDepth }
            LibrarySortOption.FORMAT -> filtered.sortedBy { it.format.name }
            LibrarySortOption.DATE_ADDED -> filtered.sortedBy { it.dateAddedEpochMs }
            LibrarySortOption.SMART_SHUFFLE -> {
                val grouped = filtered.groupBy { it.pcm.sampleRateHz }
                val rates = grouped.keys.sortedDescending()
                val resultList = mutableListOf<AudioTrackModel>()
                val rng = java.util.Random(1337)
                val shuffledGroups = grouped.mapValues { (_, list) -> list.shuffled(rng) }
                val maxGroupSize = shuffledGroups.values.maxOfOrNull { it.size } ?: 0
                for (step in 0 until maxGroupSize) {
                    rates.forEach { rate ->
                        val listForRate = shuffledGroups[rate] ?: emptyList()
                        if (step < listForRate.size) {
                            resultList.add(listForRate[step])
                        }
                    }
                }
                val (highRes, normalRes) = resultList.partition { it.pcm.sampleRateHz >= 96000 || it.pcm.bitDepth >= 24 }
                val finalShuffled = mutableListOf<AudioTrackModel>()
                var hiIdx = 0
                var loIdx = 0
                while (hiIdx < highRes.size || loIdx < normalRes.size) {
                    if (hiIdx < highRes.size) finalShuffled.add(highRes[hiIdx++])
                    if (hiIdx < highRes.size) finalShuffled.add(highRes[hiIdx++])
                    if (loIdx < normalRes.size) finalShuffled.add(normalRes[loIdx++])
                }
                finalShuffled
            }
        }
        if (filter.sortDirection == SortDirection.DESCENDING && filter.sortBy != LibrarySortOption.SMART_SHUFFLE) sorted.reversed() else sorted
    }

    private val selectionState = combine(_selectedTrackForSpecs, _selectedTrackForMenu) { specs, menu ->
        Pair(specs, menu)
    }

    // Final UI state mapping
    val uiState: StateFlow<LibraryUiState> = combine(
        filteredAndSortedTracks,
        watchedFolders,
        filterState,
        scanStatus,
        selectionState
    ) { tracks, folders, filter, status, selection ->
        LibraryUiState(
            tracks = tracks,
            isScanning = status.isScanning,
            lastScanCount = status.lastScanCount,
            nowPlayingTrackId = status.nowPlayingTrackId,
            watchedFolders = folders,
            searchQuery = filter.query,
            sortBy = filter.sortBy,
            sortDirection = filter.sortDirection,
            activeTab = filter.activeTab,
            isGridView = filter.isGridView,
            selectedTrackForSpecs = selection.first,
            selectedTrackForMenu = selection.second,
            error = status.error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun setActiveTab(tab: LibraryTab) {
        _activeTab.value = tab
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setSelectedTrackForSpecs(track: AudioTrackModel?) {
        _selectedTrackForSpecs.value = track
    }

    fun setSelectedTrackForMenu(track: AudioTrackModel?) {
        _selectedTrackForMenu.value = track
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(option: LibrarySortOption) {
        _sortBy.value = option
    }

    fun toggleSortDirection() {
        _sortDirection.value = if (_sortDirection.value == SortDirection.ASCENDING) {
            SortDirection.DESCENDING
        } else {
            SortDirection.ASCENDING
        }
    }

    fun rescan() {
        viewModelScope.launch {
            _isScanning.value = true
            _error.value = null
            try {
                _lastScanCount.value = rescanLibraryUseCase()
            } catch (t: Throwable) {
                _error.value = t.message ?: "Scan failed."
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun addFolder(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _error.value = null
            try {
                addWatchedFolderUseCase(uri, displayName)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Failed to add folder."
            }
        }
    }

    fun removeFolder(id: Long) {
        viewModelScope.launch {
            _error.value = null
            try {
                removeWatchedFolderUseCase(id)
            } catch (t: Throwable) {
                _error.value = t.message ?: "Failed to remove folder."
            }
        }
    }

    fun playTrack(track: AudioTrackModel) {
        viewModelScope.launch {
            _error.value = null
            try {
                val connectedDevice = usbDacManager.currentDevice
                val dac = if (connectedDevice != null) dacProfiles.value.firstOrNull() else null
                val engine = if (dac == null || connectedDevice == null) EngineType.ANDROID_AUDIOTRACK else settingsRepository.current.defaultEngine
                setQueueUseCase(uiState.value.tracks, track)
                startPlaybackUseCase(track, engine, dac)
                _nowPlayingTrackId.value = track.id
            } catch (t: Throwable) {
                _error.value = t.message ?: "Playback failed to start."
            }
        }
    }

    fun playTrackDirectUsb(track: AudioTrackModel) {
        viewModelScope.launch {
            _error.value = null
            try {
                val connectedDevice = usbDacManager.currentDevice
                val dac = if (connectedDevice != null) dacProfiles.value.firstOrNull() else null
                setQueueUseCase(uiState.value.tracks, track)
                startPlaybackUseCase(track, EngineType.CUSTOM_USB_DIRECT, dac)
                _nowPlayingTrackId.value = track.id
            } catch (t: Throwable) {
                _error.value = t.message ?: "USB Direct Playback failed to start."
            }
        }
    }

    fun playTrackAudioTrack(track: AudioTrackModel) {
        viewModelScope.launch {
            _error.value = null
            try {
                val connectedDevice = usbDacManager.currentDevice
                val dac = if (connectedDevice != null) dacProfiles.value.firstOrNull() else null
                setQueueUseCase(uiState.value.tracks, track)
                startPlaybackUseCase(track, EngineType.ANDROID_AUDIOTRACK, dac)
                _nowPlayingTrackId.value = track.id
            } catch (t: Throwable) {
                _error.value = t.message ?: "AudioTrack Playback failed to start."
            }
        }
    }
}
