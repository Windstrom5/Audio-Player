package com.thesis.bitperfectusb.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.ExperimentReport
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.usecase.ExportBenchmarkCsvUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveDacProfilesUseCase
import com.thesis.bitperfectusb.domain.usecase.RunExperimentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class ExperimentUiState(
    val dacProfiles: List<DacProfile> = emptyList(),
    val selectedDac: DacProfile? = null,
    val isRunning: Boolean = false,
    val progressMessage: String? = null,
    val lastReport: ExperimentReport? = null,
    val exportedFilePath: String? = null,
    val error: String? = null
)

class ExperimentViewModel(
    private val runExperimentUseCase: RunExperimentUseCase,
    observeDacProfilesUseCase: ObserveDacProfilesUseCase,
    private val exportBenchmarkCsvUseCase: ExportBenchmarkCsvUseCase
) : ViewModel() {

    private val _selectedDac = MutableStateFlow<DacProfile?>(null)
    private val _isRunning = MutableStateFlow(false)
    private val _progress = MutableStateFlow<String?>(null)
    private val _lastReport = MutableStateFlow<ExperimentReport?>(null)
    private val _exportedPath = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExperimentUiState> = combine(
        observeDacProfilesUseCase(), _selectedDac, _isRunning, _progress
    ) { dacs, selectedOverride, running, progress ->
        ExperimentUiState(
            dacProfiles = dacs,
            selectedDac = selectedOverride ?: dacs.firstOrNull(),
            isRunning = running,
            progressMessage = progress,
            lastReport = _lastReport.value,
            exportedFilePath = _exportedPath.value,
            error = _error.value
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExperimentUiState())

    fun selectDac(dac: DacProfile) {
        _selectedDac.value = dac
    }

    fun runExperiment(type: ExperimentType) {
        if (_isRunning.value) return
        viewModelScope.launch {
            _isRunning.value = true
            _error.value = null
            _progress.value = "Starting ${type.name}…"
            try {
                val report = runExperimentUseCase(type, uiState.value.selectedDac) { message ->
                    _progress.value = message
                }
                _lastReport.value = report
            } catch (t: Throwable) {
                _error.value = t.message ?: "Experiment failed."
            } finally {
                _isRunning.value = false
                _progress.value = null
            }
        }
    }

    fun exportCurrentReport(context: Context) {
        val report = _lastReport.value ?: return
        viewModelScope.launch {
            val fileName = "${report.experimentType.name.lowercase()}_${System.currentTimeMillis()}.csv"
            val destination = File(context.getExternalFilesDir(null), fileName).absolutePath
            try {
                exportBenchmarkCsvUseCase.exportExperiment(report.experimentType, destination)
                _exportedPath.value = destination
            } catch (t: Throwable) {
                _error.value = "CSV export failed: ${t.message}"
            }
        }
    }
}
