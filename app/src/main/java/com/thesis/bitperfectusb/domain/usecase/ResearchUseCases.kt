package com.thesis.bitperfectusb.domain.usecase

import com.thesis.bitperfectusb.benchmark.ExperimentOrchestrator
import com.thesis.bitperfectusb.domain.engine.AnalyticsEngine
import com.thesis.bitperfectusb.domain.model.AnalyticsSummary
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.ExperimentReport
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import com.thesis.bitperfectusb.domain.repository.DacProfileRepository

class RunExperimentUseCase(private val orchestrator: ExperimentOrchestrator) {
    suspend operator fun invoke(
        type: ExperimentType,
        dac: DacProfile?,
        onProgress: (String) -> Unit = {}
    ): ExperimentReport = orchestrator.run(type, dac, onProgress)
}

class ExportBenchmarkCsvUseCase(private val repository: BenchmarkRepository) {
    suspend fun exportSessions(destinationPath: String): Int = repository.exportSessionsToCsv(destinationPath)
    suspend fun exportExperiment(type: ExperimentType, destinationPath: String): Int =
        repository.exportExperimentRunsToCsv(type, destinationPath)
}

/**
 * Computes the historical Analytics dashboard summary from all logged sessions
 * (the "analytical value" layer requested on top of the thesis's raw feature set).
 */
class GetAnalyticsSummaryUseCase(
    private val benchmarkRepository: BenchmarkRepository,
    private val dacProfileRepository: DacProfileRepository,
    private val analyticsEngine: AnalyticsEngine
) {
    suspend operator fun invoke(): AnalyticsSummary {
        val sessions = benchmarkRepository.getAllSessionsSnapshot()

        val dacLabels = mutableMapOf<Long, String>()
        sessions.mapNotNull { it.dacProfileId }.distinct().forEach { id ->
            dacProfileRepository.getProfile(id)?.let { dacLabels[id] = "${it.productName} (${it.usbIdLabel})" }
        }

        return analyticsEngine.summarize(sessions, dacLabels)
    }
}
