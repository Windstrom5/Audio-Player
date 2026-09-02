package com.thesis.bitperfectusb.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesis.bitperfectusb.domain.abx.AbxGameEngine
import com.thesis.bitperfectusb.domain.abx.AbxSample
import com.thesis.bitperfectusb.domain.abx.AbxTestMode
import com.thesis.bitperfectusb.domain.abx.AbxTrialState
import com.thesis.bitperfectusb.playback.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AbxUiState(
    val selectedMode: AbxTestMode = AbxTestMode.FLAC_VS_MP3_320,
    val trialState: AbxTrialState = AbxTrialState(),
    val isPlaying: Boolean = false,
    val isUsbDacConnected: Boolean = false,
    val currentTrackTitle: String? = null,
    val lastFeedbackMessage: String? = null
)

class AbxViewModel(
    private val abxGameEngine: AbxGameEngine,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val _uiState = MutableStateFlow(AbxUiState())
    val uiState: StateFlow<AbxUiState> = _uiState.asStateFlow()

    init {
        startNewGame(AbxTestMode.FLAC_VS_MP3_320, 10)
        observePlayback()
    }

    private fun observePlayback() {
        viewModelScope.launch {
            playbackController.state.collect { playback ->
                _uiState.update {
                    it.copy(
                        isPlaying = playback.isPlaying,
                        currentTrackTitle = playback.currentTrack?.title
                    )
                }
            }
        }
    }

    fun selectMode(mode: AbxTestMode) {
        _uiState.update { it.copy(selectedMode = mode) }
        startNewGame(mode, _uiState.value.trialState.totalRounds)
    }

    fun startNewGame(mode: AbxTestMode = _uiState.value.selectedMode, rounds: Int = 10) {
        val newState = abxGameEngine.startNewGame(mode, rounds)
        _uiState.update {
            it.copy(
                selectedMode = mode,
                trialState = newState,
                lastFeedbackMessage = "New ${mode.title} challenge started! Listen to A, B, and X, then vote."
            )
        }
    }

    fun selectSample(sample: AbxSample) {
        val updatedState = abxGameEngine.setActiveSample(sample)
        _uiState.update {
            it.copy(
                trialState = updatedState,
                lastFeedbackMessage = when (sample) {
                    AbxSample.A -> "Now listening to Sample A (${_uiState.value.selectedMode.sampleATitle})"
                    AbxSample.B -> "Now listening to Sample B (${_uiState.value.selectedMode.sampleBTitle})"
                    AbxSample.X -> "Now listening to Mystery Sample X. Is it A or B?"
                    AbxSample.NONE -> null
                }
            )
        }
    }

    fun submitGuess(guessIsA: Boolean) {
        val (isCorrect, updatedTrial) = abxGameEngine.submitGuess(guessIsA)
        val feedback = if (isCorrect) {
            " Correct! Mystery X was ${if (guessIsA) "Sample A" else "Sample B"}."
        } else {
            " Incorrect! Mystery X was actually ${if (guessIsA) "Sample B" else "Sample A"}."
        }

        _uiState.update {
            it.copy(
                trialState = updatedTrial,
                lastFeedbackMessage = feedback
            )
        }
    }
}
