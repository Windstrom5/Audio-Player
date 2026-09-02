package com.thesis.bitperfectusb.domain.usecase

import com.thesis.bitperfectusb.domain.engine.PlaybackIntegrityEngine
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.IntegrityResult
import com.thesis.bitperfectusb.domain.model.PlaybackQueue
import com.thesis.bitperfectusb.domain.model.PlaybackState
import com.thesis.bitperfectusb.playback.PlaybackController
import kotlinx.coroutines.flow.StateFlow

class StartPlaybackUseCase(
    private val controller: PlaybackController
) {
    suspend operator fun invoke(track: AudioTrackModel, engineType: EngineType, dac: DacProfile?) =
        controller.start(track, engineType, dac)
}

class StopPlaybackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke() = controller.stop()
}

class SeekPlaybackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke(positionMs: Long) = controller.seekTo(positionMs)
}

class ObservePlaybackStateUseCase(private val controller: PlaybackController) {
    operator fun invoke(): StateFlow<PlaybackState> = controller.state
}

/** RO2 support: on-demand integrity check, independent of whether playback is active. */
class VerifyIntegrityUseCase(private val engine: PlaybackIntegrityEngine) {
    operator fun invoke(track: AudioTrackModel, dac: DacProfile?, engineType: EngineType): IntegrityResult =
        engine.evaluate(track.pcm, dac, engineType)
}

class PausePlaybackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke() = controller.pause()
}

class ResumePlaybackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke() = controller.resume()
}

class NextTrackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke() = controller.nextTrack()
}

class PreviousTrackUseCase(private val controller: PlaybackController) {
    suspend operator fun invoke() = controller.previousTrack()
}

class ToggleShuffleUseCase(private val controller: PlaybackController) {
    operator fun invoke() = controller.toggleShuffle()
}

/** Sets the Next/Previous queue — see PlaybackController.setQueue. Doesn't itself
 *  start playback; pair with [StartPlaybackUseCase]. */
class SetQueueUseCase(private val controller: PlaybackController) {
    operator fun invoke(tracks: List<AudioTrackModel>, startAt: AudioTrackModel) = controller.setQueue(tracks, startAt)
}

class ObserveQueueUseCase(private val controller: PlaybackController) {
    operator fun invoke(): StateFlow<PlaybackQueue> = controller.queue
}
