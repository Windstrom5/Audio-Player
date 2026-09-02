package com.thesis.bitperfectusb.presentation.viewmodel

import android.hardware.usb.UsbDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.benchmark.DacRateBenchmark
import com.thesis.bitperfectusb.domain.model.DacBenchmarkReport
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.usecase.AnalyzeDacUseCase
import com.thesis.bitperfectusb.domain.usecase.FetchDescriptorTreeUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveDacProfilesUseCase
import com.thesis.bitperfectusb.usb.DescriptorTreeBuilder
import com.thesis.bitperfectusb.usb.UsbDacManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DacUiState(
    val attachedDevice: UsbDevice? = null,
    val allDevices: List<UsbDevice> = emptyList(),
    val knownProfiles: List<DacProfile> = emptyList(),
    val activeProfile: DacProfile? = null,
    val isAnalyzing: Boolean = false,
    val error: String? = null,
    /** USB Descriptor Explorer — null until fetchDescriptorTree() has been called
     *  at least once for the currently attached device. */
    val descriptorTree: DescriptorTreeBuilder.DescriptorNode? = null,
    val isLoadingDescriptorTree: Boolean = false,
    /** DAC Benchmark — see DacRateBenchmark. */
    val isBenchmarking: Boolean = false,
    val benchmarkProgress: String? = null,
    val benchmarkReport: DacBenchmarkReport? = null
)

class DacViewModel(
    private val usbDacManager: UsbDacManager,
    private val analyzeDacUseCase: AnalyzeDacUseCase,
    observeDacProfilesUseCase: ObserveDacProfilesUseCase,
    private val fetchDescriptorTreeUseCase: FetchDescriptorTreeUseCase,
    private val dacRateBenchmark: DacRateBenchmark
) : ViewModel() {

    private val _analyzing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _activeProfile = MutableStateFlow<DacProfile?>(null)
    private val _descriptorTree = MutableStateFlow<DescriptorTreeBuilder.DescriptorNode?>(null)
    private val _loadingDescriptorTree = MutableStateFlow(false)
    private val _benchmarking = MutableStateFlow(false)
    private val _benchmarkProgress = MutableStateFlow<String?>(null)
    private val _benchmarkReport = MutableStateFlow<DacBenchmarkReport?>(null)

    private val deviceAndProfiles = combine(
        usbDacManager.connectedDevice,
        usbDacManager.allDevices,
        observeDacProfilesUseCase()
    ) { device, allDevices, profiles -> Triple(device, allDevices, profiles) }

    private val analysisAndTree = combine(_activeProfile, _analyzing, _descriptorTree) { active, analyzing, tree ->
        Triple(active, analyzing, tree)
    }

    private val benchmarkState = combine(_benchmarking, _benchmarkProgress, _benchmarkReport) { running, progress, report ->
        Triple(running, progress, report)
    }

    val uiState: StateFlow<DacUiState> = combine(
        deviceAndProfiles,
        analysisAndTree,
        benchmarkState
    ) { devProfiles, analysisTree, benchmark ->
        val (device, allDevices, profiles) = devProfiles
        val (active, analyzing, tree) = analysisTree
        val (benchmarking, progress, report) = benchmark
        DacUiState(
            attachedDevice = device,
            allDevices = allDevices,
            knownProfiles = profiles,
            activeProfile = active ?: profiles.find { it.vendorId == device?.vendorId && it.productId == device?.productId },
            isAnalyzing = analyzing,
            error = _error.value,
            descriptorTree = tree,
            isLoadingDescriptorTree = _loadingDescriptorTree.value,
            isBenchmarking = benchmarking,
            benchmarkProgress = progress,
            benchmarkReport = report
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DacUiState())

    /** Whether a given device looks like an audio-class device — for UI hints/sorting only. */
    fun looksLikeAudioDevice(device: UsbDevice): Boolean = usbDacManager.looksLikeAudioDevice(device)

    /** Re-queries the USB host stack directly, in case an attach broadcast was missed. */
    fun rescan() {
        _error.value = null
        usbDacManager.rescanDevices()
    }

    /** Manually picks which attached device to treat as the active DAC — the fallback for
     *  when auto-detection doesn't recognize a particular device. */
    fun selectDevice(device: UsbDevice) {
        _error.value = null
        _activeProfile.value = null // clear any stale profile from a previously-selected device
        _descriptorTree.value = null // and any descriptor tree fetched for the old device
        usbDacManager.selectDevice(device)
    }

    fun analyzeAttachedDevice() {
        val device = usbDacManager.currentDevice ?: run {
            _error.value = "No USB device selected."
            return
        }
        viewModelScope.launch {
            _analyzing.value = true
            _error.value = null
            if (!usbDacManager.hasPermission(device)) {
                usbDacManager.observePermissionRequest(device).collect { granted ->
                    if (granted) runAnalysis() else {
                        _error.value = "USB permission was denied."
                        _analyzing.value = false
                    }
                }
            } else {
                runAnalysis()
            }
        }
    }

    private suspend fun runAnalysis() {
        val result = analyzeDacUseCase()
        result.onSuccess { _activeProfile.value = it }
            .onFailure { _error.value = it.message ?: "Failed to analyze DAC. This device may not be USB Audio Class compliant." }
        _analyzing.value = false
    }

    /** Fetches and parses the raw descriptor tree for the USB Descriptor Explorer —
     *  independent of [analyzeAttachedDevice]; doesn't require a saved profile to exist. */
    fun fetchDescriptorTree() {
        val device = usbDacManager.currentDevice ?: run {
            _error.value = "No USB device selected."
            return
        }
        viewModelScope.launch {
            _loadingDescriptorTree.value = true
            _error.value = null
            if (!usbDacManager.hasPermission(device)) {
                usbDacManager.observePermissionRequest(device).collect { granted ->
                    if (granted) runDescriptorFetch() else {
                        _error.value = "USB permission was denied."
                        _loadingDescriptorTree.value = false
                    }
                }
            } else {
                runDescriptorFetch()
            }
        }
    }

    private suspend fun runDescriptorFetch() {
        val result = fetchDescriptorTreeUseCase()
        result.onSuccess { _descriptorTree.value = it }
            .onFailure { _error.value = it.message ?: "Failed to read descriptors from this device." }
        _loadingDescriptorTree.value = false
    }

    /** Runs the DAC Benchmark sweep against the currently active profile — see
     *  [DacRateBenchmark]. Requires a profile to already exist (tests every rate it
     *  declared), so this needs [analyzeAttachedDevice] to have succeeded first. */
    fun runBenchmark() {
        val profile = uiState.value.activeProfile ?: run {
            _error.value = "Analyze this DAC's capabilities first — the benchmark tests the rates it declared supporting."
            return
        }
        if (_benchmarking.value) return
        viewModelScope.launch {
            _benchmarking.value = true
            _error.value = null
            _benchmarkProgress.value = "Starting…"
            try {
                val report = dacRateBenchmark.run(profile) { message -> _benchmarkProgress.value = message }
                _benchmarkReport.value = report
            } catch (t: Throwable) {
                _error.value = t.message ?: "Benchmark failed partway through."
            } finally {
                _benchmarking.value = false
                _benchmarkProgress.value = null
            }
        }
    }
}
