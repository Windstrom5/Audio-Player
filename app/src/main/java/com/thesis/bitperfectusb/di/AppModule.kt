package com.thesis.bitperfectusb.di

import com.thesis.bitperfectusb.benchmark.BenchmarkRunner
import com.thesis.bitperfectusb.benchmark.CsvExporter
import com.thesis.bitperfectusb.benchmark.DacRateBenchmark
import com.thesis.bitperfectusb.benchmark.ExperimentOrchestrator
import com.thesis.bitperfectusb.benchmark.ValidityThreatsDetector
import com.thesis.bitperfectusb.data.local.db.AppDatabase
import com.thesis.bitperfectusb.data.repository.AudioLibraryRepositoryImpl
import com.thesis.bitperfectusb.data.repository.BenchmarkRepositoryImpl
import com.thesis.bitperfectusb.data.repository.DacProfileRepositoryImpl
import com.thesis.bitperfectusb.data.repository.LyricsRepository
import com.thesis.bitperfectusb.data.scanner.LocalAudioScanner
import com.thesis.bitperfectusb.data.settings.SettingsRepository
import com.thesis.bitperfectusb.domain.engine.AdaptiveOptimizationEngine
import com.thesis.bitperfectusb.domain.engine.AnalyticsEngine
import com.thesis.bitperfectusb.domain.engine.PlaybackIntegrityEngine
import com.thesis.bitperfectusb.domain.engine.StatisticsEngine
import com.thesis.bitperfectusb.domain.repository.AudioLibraryRepository
import com.thesis.bitperfectusb.domain.repository.BenchmarkRepository
import com.thesis.bitperfectusb.domain.repository.DacProfileRepository
import com.thesis.bitperfectusb.domain.usecase.AddWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.AnalyzeDacUseCase
import com.thesis.bitperfectusb.domain.usecase.ExportBenchmarkCsvUseCase
import com.thesis.bitperfectusb.domain.usecase.FetchDescriptorTreeUseCase
import com.thesis.bitperfectusb.domain.usecase.GetAnalyticsSummaryUseCase
import com.thesis.bitperfectusb.domain.usecase.NextTrackUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveDacProfilesUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveLibraryUseCase
import com.thesis.bitperfectusb.domain.usecase.ObservePlaybackStateUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveQueueUseCase
import com.thesis.bitperfectusb.domain.usecase.ObserveWatchedFoldersUseCase
import com.thesis.bitperfectusb.domain.usecase.PausePlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.PreviousTrackUseCase
import com.thesis.bitperfectusb.domain.usecase.RemoveWatchedFolderUseCase
import com.thesis.bitperfectusb.domain.usecase.RescanLibraryUseCase
import com.thesis.bitperfectusb.domain.usecase.ResumePlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.RunExperimentUseCase
import com.thesis.bitperfectusb.domain.usecase.SeekPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.SetQueueUseCase
import com.thesis.bitperfectusb.domain.usecase.StartPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.StopPlaybackUseCase
import com.thesis.bitperfectusb.domain.usecase.ToggleShuffleUseCase
import com.thesis.bitperfectusb.domain.usecase.VerifyIntegrityUseCase
import com.thesis.bitperfectusb.playback.FlacVorbisCommentReader
import com.thesis.bitperfectusb.playback.PlaybackController
import com.thesis.bitperfectusb.presentation.viewmodel.AnalyticsViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.DacViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.ExperimentViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.KeepScreenOnViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.LibraryViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.PlayerViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.SettingsViewModel
import com.thesis.bitperfectusb.usb.DacCapabilityAnalyzer
import com.thesis.bitperfectusb.usb.UsbDacManager
import com.thesis.bitperfectusb.usb.UsbDescriptorParser
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }
    single { get<AppDatabase>().audioTrackDao() }
    single { get<AppDatabase>().dacProfileDao() }
    single { get<AppDatabase>().benchmarkDao() }
    single { get<AppDatabase>().experimentDao() }
    single { get<AppDatabase>().watchedFolderDao() }
}

val dataModule = module {
    single { LocalAudioScanner(androidContext()) }
    single { CsvExporter() }
    single { SettingsRepository(androidContext()) }
    single { LyricsRepository(androidContext()) }
    single<AudioLibraryRepository> { AudioLibraryRepositoryImpl(androidContext(), get(), get(), get()) }
    single<DacProfileRepository> { DacProfileRepositoryImpl(get()) }
    single<BenchmarkRepository> { BenchmarkRepositoryImpl(get(), get(), get()) }
}

val usbModule = module {
    single { UsbDacManager(androidContext()) }
    single { UsbDescriptorParser() }
    single { DacCapabilityAnalyzer(get()) }
}

val domainModule = module {
    single { PlaybackIntegrityEngine() }
    single { AdaptiveOptimizationEngine() }
    single { StatisticsEngine() }
    single { AnalyticsEngine() }
    single { com.thesis.bitperfectusb.domain.abx.AbxGameEngine() }
    single { com.thesis.bitperfectusb.playback.ai.AiStemExtractor(androidContext(), get()) }

    factory { ObserveLibraryUseCase(get()) }
    factory { RescanLibraryUseCase(get()) }
    factory { ObserveWatchedFoldersUseCase(get()) }
    factory { AddWatchedFolderUseCase(get()) }
    factory { RemoveWatchedFolderUseCase(get()) }
    factory { ObserveDacProfilesUseCase(get()) }
    factory { AnalyzeDacUseCase(get(), get(), get()) }
    factory { FetchDescriptorTreeUseCase(get(), get()) }
    factory { StartPlaybackUseCase(get()) }
    factory { StopPlaybackUseCase(get()) }
    factory { SeekPlaybackUseCase(get()) }
    factory { ObservePlaybackStateUseCase(get()) }
    factory { VerifyIntegrityUseCase(get()) }
    factory { PausePlaybackUseCase(get()) }
    factory { ResumePlaybackUseCase(get()) }
    factory { NextTrackUseCase(get()) }
    factory { PreviousTrackUseCase(get()) }
    factory { ToggleShuffleUseCase(get()) }
    factory { SetQueueUseCase(get()) }
    factory { ObserveQueueUseCase(get()) }
    factory { RunExperimentUseCase(get()) }
    factory { ExportBenchmarkCsvUseCase(get()) }
    factory { GetAnalyticsSummaryUseCase(get(), get(), get()) }
}

val benchmarkModule = module {
    single { BenchmarkRunner(androidContext()) }
    single { ValidityThreatsDetector(androidContext()) }
    single { ExperimentOrchestrator(get(), get(), get(), get()) }
    single { DacRateBenchmark(get(), get()) }
}

val playbackModule = module {
    single { FlacVorbisCommentReader(androidContext()) }
    single { PlaybackController(androidContext(), get(), get(), get(), get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModel { LibraryViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DacViewModel(get(), get(), get(), get(), get()) }
    viewModel { PlayerViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ExperimentViewModel(get(), get(), get()) }
    viewModel { AnalyticsViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { KeepScreenOnViewModel(get(), get()) }
    viewModel { com.thesis.bitperfectusb.presentation.viewmodel.AbxViewModel(get(), get()) }
}

val allModules = listOf(
    databaseModule, dataModule, usbModule, domainModule, benchmarkModule, playbackModule, viewModelModule
)
