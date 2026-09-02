package com.thesis.bitperfectusb.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.domain.model.AnalyticsSummary
import com.thesis.bitperfectusb.domain.usecase.GetAnalyticsSummaryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val summary: AnalyticsSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AnalyticsViewModel(
    private val getAnalyticsSummaryUseCase: GetAnalyticsSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val summary = getAnalyticsSummaryUseCase()
                _uiState.value = AnalyticsUiState(summary = summary, isLoading = false)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = t.message ?: "Failed to load analytics.")
            }
        }
    }
}
