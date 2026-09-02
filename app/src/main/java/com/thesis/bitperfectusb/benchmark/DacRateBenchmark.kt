package com.thesis.bitperfectusb.benchmark

import com.thesis.bitperfectusb.domain.model.DacBenchmarkReport
import com.thesis.bitperfectusb.domain.model.DacBenchmarkResult
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import com.thesis.bitperfectusb.playback.PlaybackController
import com.thesis.bitperfectusb.playback.SyntheticToneDecoder
import kotlinx.coroutines.delay

/**
 * "DAC Benchmark" advanced feature (roadmap: Maximum Stable Hz / Buffer /
 * Latency) — distinct in purpose from the four formal research protocols in
 * [ExperimentOrchestrator] (Section 3.8), which hold configuration fixed
 * across a controlled A/B comparison for the thesis's specific RQs. This
 * instead sweeps every PCM format the *attached DAC itself* declared
 * supporting ([DacProfile.streamingOptions]), highest rate first, using a
 * short synthetic-tone burst per candidate — the same proven mechanism as
 * `ExperimentOrchestrator.runOneConfig` — and reports which ones actually
 * streamed with zero dropouts. A DAC can legitimately declare 384kHz support
 * and still not sustain it reliably over a given cable or host controller in
 * practice; this measures that live rather than repeating what the
 * capability matrix already shows from the descriptor alone.
 *
 * Results are returned directly to the caller and not persisted to the
 * benchmark database — this is a diagnostic/audiophile tool for the
 * currently-attached DAC, not one of the four formal experiment protocols
 * that feed the Session History / Analytics dashboard. Persisting a history
 * of benchmark runs per DAC would be a reasonable follow-up if that turns
 * out to be useful in practice.
 */
class DacRateBenchmark(
    private val playbackController: PlaybackController,
    private val benchmarkRepository: BenchmarkRepository,
    private val runDurationMs: Long = DEFAULT_RUN_DURATION_MS
) {
    /**
     * Runs the full sweep. [onProgress] is called before each candidate starts,
     * so the UI can show "Testing 192000Hz / 24-bit (3/9)…" style progress —
     * this can take a while for real (each candidate genuinely streams for
     * [runDurationMs]), which is expected for a live hardware stress test, not
     * a bug to hide.
     */
    suspend fun run(dac: DacProfile, onProgress: suspend (String) -> Unit = {}): DacBenchmarkReport {
        val candidates = dac.streamingOptions
            .flatMap { option -> option.supportedSampleRates.map { rate -> rate to option.bitDepth } }
            .distinct()
            .sortedWith(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })
            .take(MAX_CANDIDATES)

        val results = mutableListOf<DacBenchmarkResult>()
        candidates.forEachIndexed { index, (rate, bitDepth) ->
            onProgress("Testing ${rate}Hz / $bitDepth-bit (${index + 1}/${candidates.size})…")
            results += runOneCandidate(rate, bitDepth, dac)
            if (index != candidates.lastIndex) delay(COOL_DOWN_MS)
        }

        val best = results
            .filter { it.stable }
            .maxWithOrNull(compareBy<DacBenchmarkResult> { it.sampleRateHz }.thenBy { it.bitDepth })

        return DacBenchmarkReport(
            dacLabel = dac.productName,
            results = results,
            maxStable = best
        )
    }

    private suspend fun runOneCandidate(sampleRateHz: Int, bitDepth: Int, dac: DacProfile): DacBenchmarkResult {
        val decoder = SyntheticToneDecoder()
        // Must exactly match "synthetic://<sampleRateHz>/<bitDepth>/<channels>" —
        // SyntheticToneDecoder.open() parses the format straight out of this string
        // via removePrefix("synthetic://").split("/"); it has no constructor
        // parameters of its own.
        val sourceUri = "synthetic://$sampleRateHz/$bitDepth/2"

        val sessionId = try {
            playbackController.startWithDecoder(
                decoder = decoder,
                sourceUri = sourceUri,
                engineType = EngineType.CUSTOM_USB_DIRECT,
                dac = dac,
                initialBufferSizeBytes = 4096
            )
        } catch (t: Throwable) {
            playbackController.stop()
            return DacBenchmarkResult(
                sampleRateHz = sampleRateHz,
                bitDepth = bitDepth,
                stable = false,
                dropouts = -1,
                latencyMs = 0.0,
                bufferSizeBytes = 0,
                note = t.message ?: "Could not start this format on this DAC"
            )
        }

        delay(runDurationMs)

        val latency = playbackController.state.value.liveLatencyMs
        val bufferBytes = playbackController.state.value.bufferSizeBytes
        playbackController.stop()

        val samples = benchmarkRepository.getSamplesForSession(sessionId)
        val dropouts = samples.lastOrNull()?.cumulativeDropouts ?: 0

        return DacBenchmarkResult(
            sampleRateHz = sampleRateHz,
            bitDepth = bitDepth,
            stable = dropouts == 0,
            dropouts = dropouts,
            latencyMs = latency,
            bufferSizeBytes = bufferBytes,
            note = if (dropouts == 0) "Clean" else "$dropouts dropout(s) during test"
        )
    }

    companion object {
        private const val DEFAULT_RUN_DURATION_MS = 4_000L
        private const val COOL_DOWN_MS = 500L
        /** Defensive cap — real DACs declare a modest, bounded set of rates, but this
         *  keeps a pathological descriptor from turning into an unbounded-length test. */
        private const val MAX_CANDIDATES = 20
    }
}
