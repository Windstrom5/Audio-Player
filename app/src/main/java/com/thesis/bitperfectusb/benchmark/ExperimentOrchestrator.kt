package com.thesis.bitperfectusb.benchmark

import com.thesis.bitperfectusb.domain.engine.StatisticsEngine
import com.thesis.bitperfectusb.domain.model.BenchmarkSample
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.ExperimentReport
import com.thesis.bitperfectusb.domain.model.ExperimentRun
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.model.PcmFormat
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import com.thesis.bitperfectusb.playback.PlaybackController
import com.thesis.bitperfectusb.playback.SyntheticToneDecoder
import kotlinx.coroutines.delay

/**
 * Runs the four standardized research protocols from Section 3.8, driving
 * [PlaybackController] with a synthetic test tone so the source signal is held
 * constant while the variable under test (architecture, sample rate, hardware,
 * buffer size) is swept. Every number this produces is measured live on the
 * device it's running on — this does not reproduce the historical figures
 * printed in Chapter 4 of the thesis; it's a live instrument, not a replay.
 *
 * Now that the USB-direct engine requires an *exact* alternate-setting match
 * for the requested format (Section 3.4 — no more silently grabbing whatever
 * isochronous endpoint happened to be first), a sweep can legitimately hit a
 * format the attached DAC doesn't support at some point along the way. Each
 * config is run defensively so one unsupported combination is skipped and
 * reported rather than aborting the whole sweep.
 */
class ExperimentOrchestrator(
    private val playbackController: PlaybackController,
    private val benchmarkRepository: BenchmarkRepository,
    private val statisticsEngine: StatisticsEngine,
    private val validityThreatsDetector: ValidityThreatsDetector,
    private val runDurationMs: Long = DEFAULT_RUN_DURATION_MS
) {

    suspend fun run(
        type: ExperimentType,
        dac: DacProfile?,
        onProgress: (String) -> Unit = {}
    ): ExperimentReport = when (type) {
        ExperimentType.EXPERIMENT_A_ARCHITECTURE -> runExperimentA(dac, onProgress)
        ExperimentType.EXPERIMENT_B_SAMPLE_RATE -> runExperimentB(dac, onProgress)
        ExperimentType.EXPERIMENT_C_DAC_HARDWARE -> runExperimentC(dac, onProgress)
        ExperimentType.EXPERIMENT_D_BUFFER_SIZE -> runExperimentD(dac, onProgress)
    }

    // --- Experiment A: architecture comparison (RQ1) ---
    private suspend fun runExperimentA(dac: DacProfile?, onProgress: (String) -> Unit): ExperimentReport {
        val format = PcmFormat(44_100, 16, 2) // controlled: fixed 44.1kHz/16-bit/stereo, 4096B buffer

        onProgress("Running AudioTrack control path…")
        val audioTrackResult = runOneConfig(
            "exp_a_audiotrack", ExperimentType.EXPERIMENT_A_ARCHITECTURE, format,
            EngineType.ANDROID_AUDIOTRACK, dac, 4096, onProgress
        )

        coolDown(onProgress)

        onProgress("Running custom USB-direct treatment path…")
        val usbResult = runOneConfig(
            "exp_a_usb_direct", ExperimentType.EXPERIMENT_A_ARCHITECTURE, format,
            EngineType.CUSTOM_USB_DIRECT, dac, 4096, onProgress
        )

        val runs = listOfNotNull(audioTrackResult?.first, usbResult?.first)
        val welchLatency = if (audioTrackResult != null && usbResult != null) {
            welchOrNull(audioTrackResult.second.map { it.latencyMs }, usbResult.second.map { it.latencyMs })
        } else null
        val welchCpu = if (audioTrackResult != null && usbResult != null) {
            welchOrNull(audioTrackResult.second.map { it.cpuPercent }, usbResult.second.map { it.cpuPercent })
        } else null

        return ExperimentReport(
            experimentType = ExperimentType.EXPERIMENT_A_ARCHITECTURE,
            runs = runs,
            welchLatencyResult = welchLatency,
            welchCpuResult = welchCpu
        )
    }

    // --- Experiment B: sample rate scaling (Section 4.2) ---
    private suspend fun runExperimentB(dac: DacProfile?, onProgress: (String) -> Unit): ExperimentReport {
        val rates = listOf(44_100, 48_000, 96_000, 192_000)
        val runs = mutableListOf<ExperimentRun>()
        val cpuByRate = mutableMapOf<String, List<Double>>()

        rates.forEachIndexed { index, rate ->
            onProgress("Sweeping sample rate: ${rate}Hz (${index + 1}/${rates.size})…")
            val format = PcmFormat(rate, 24, 2)
            val label = "exp_b_${rate}hz"
            val result = runOneConfig(
                label, ExperimentType.EXPERIMENT_B_SAMPLE_RATE, format,
                EngineType.CUSTOM_USB_DIRECT, dac, 4096, onProgress
            )
            if (result != null) {
                runs += result.first
                cpuByRate[label] = result.second.map { it.cpuPercent }
            }
            if (index != rates.lastIndex) coolDown(onProgress)
        }

        val anova = anovaOrNull(cpuByRate)
        return ExperimentReport(ExperimentType.EXPERIMENT_B_SAMPLE_RATE, runs, anovaResult = anova)
    }

    // --- Experiment C: hardware DAC variation (Section 4.3) ---
    // Only the currently-attached DAC can actually be measured from this device;
    // the multi-DAC comparison table in Chapter 4.3 required physically swapping
    // hardware between runs, so this records one real, honest run against
    // whatever is plugged in right now.
    private suspend fun runExperimentC(dac: DacProfile?, onProgress: (String) -> Unit): ExperimentReport {
        val format = PcmFormat(44_100, 16, 2)
        val label = dac?.let { "exp_c_${it.usbIdLabel.replace(":", "_").replace("0x", "")}" } ?: "exp_c_no_dac"
        onProgress("Benchmarking attached DAC${dac?.let { " (${it.productName})" } ?: ""}…")
        val result = runOneConfig(
            label, ExperimentType.EXPERIMENT_C_DAC_HARDWARE, format,
            EngineType.CUSTOM_USB_DIRECT, dac, 4096, onProgress
        )
        return ExperimentReport(ExperimentType.EXPERIMENT_C_DAC_HARDWARE, listOfNotNull(result?.first))
    }

    // --- Experiment D: buffer size sweep (Section 4.4) ---
    private suspend fun runExperimentD(dac: DacProfile?, onProgress: (String) -> Unit): ExperimentReport {
        val bufferSizes = listOf(1024, 2048, 4096, 8192, 16384, 32768)
        val format = PcmFormat(44_100, 16, 2)
        val runs = mutableListOf<ExperimentRun>()
        val latencyByBuffer = mutableMapOf<String, List<Double>>()

        bufferSizes.forEachIndexed { index, bufferSize ->
            onProgress("Sweeping buffer size: ${bufferSize}B (${index + 1}/${bufferSizes.size})…")
            val label = "exp_d_buf$bufferSize"
            val result = runOneConfig(
                label, ExperimentType.EXPERIMENT_D_BUFFER_SIZE, format,
                EngineType.CUSTOM_USB_DIRECT, dac, bufferSize, onProgress
            )
            if (result != null) {
                runs += result.first
                latencyByBuffer[label] = result.second.map { it.latencyMs }
            }
            if (index != bufferSizes.lastIndex) coolDown(onProgress)
        }

        val anova = anovaOrNull(latencyByBuffer)
        return ExperimentReport(ExperimentType.EXPERIMENT_D_BUFFER_SIZE, runs, anovaResult = anova)
    }

    /**
     * Runs one fixed configuration for [runDurationMs] and returns its saved
     * ExperimentRun plus raw samples — or null if this exact format/engine
     * combination can't be started on the current hardware (e.g. the attached
     * DAC has no alternate setting matching this sample rate/bit depth), in
     * which case the failure is reported via [onProgress] instead of thrown.
     */
    private suspend fun runOneConfig(
        label: String,
        type: ExperimentType,
        format: PcmFormat,
        engineType: EngineType,
        dac: DacProfile?,
        bufferSizeBytes: Int,
        onProgress: (String) -> Unit
    ): Pair<ExperimentRun, List<BenchmarkSample>>? {
        val decoder = SyntheticToneDecoder()
        val sourceUri = "synthetic://${format.sampleRateHz}/${format.bitDepth}/${format.channels}"

        val sessionId = try {
            playbackController.startWithDecoder(
                decoder = decoder,
                sourceUri = sourceUri,
                engineType = engineType,
                dac = dac,
                trackId = null,
                initialBufferSizeBytes = bufferSizeBytes
            )
        } catch (t: Throwable) {
            onProgress("⚠ Skipped $label — ${t.message ?: "not supported on this hardware"}")
            playbackController.stop() // clean up any partially-opened decoder from the failed attempt
            return null
        }

        delay(runDurationMs)
        playbackController.stop()

        val samples = benchmarkRepository.getSamplesForSession(sessionId)
        val latencyStats = samples.takeIf { it.isNotEmpty() }?.let { statisticsEngine.descriptiveStats(it.map { s -> s.latencyMs }) }
        val cpuStats = samples.takeIf { it.isNotEmpty() }?.let { statisticsEngine.descriptiveStats(it.map { s -> s.cpuPercent }) }
        val memMean = if (samples.isNotEmpty()) samples.map { it.memoryMb }.average() else 0.0
        val dropouts = samples.lastOrNull()?.cumulativeDropouts ?: 0
        val integrity = playbackController.verifyIntegrity(format, dac, engineType)

        validityThreatsDetector.checkThreats(latencyStats?.stdDev ?: 0.0).forEach { onProgress("⚠ $it") }

        val run = ExperimentRun(
            experimentType = type,
            configLabel = label,
            timestampEpochMs = System.currentTimeMillis(),
            sampleSize = samples.size,
            meanLatencyMs = latencyStats?.mean ?: 0.0,
            sdLatencyMs = latencyStats?.stdDev ?: 0.0,
            meanCpuPercent = cpuStats?.mean ?: 0.0,
            meanMemoryMb = memMean,
            dropouts = dropouts,
            integrityScore = integrity.score
        )
        benchmarkRepository.saveExperimentRun(run)
        return run to samples
    }

    private fun welchOrNull(a: List<Double>, b: List<Double>) =
        if (a.size > 1 && b.size > 1) statisticsEngine.welchTTest(a, b) else null

    private fun anovaOrNull(groups: Map<String, List<Double>>) =
        if (groups.size >= 2 && groups.values.all { it.size > 1 }) statisticsEngine.oneWayAnova(groups) else null

    private suspend fun coolDown(onProgress: (String) -> Unit) {
        onProgress("Cooling down to avoid thermal bias…")
        delay(ValidityThreatsDetector.COOL_DOWN_MS)
    }

    companion object {
        const val DEFAULT_RUN_DURATION_MS = 8_000L
    }
}
