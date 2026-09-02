package com.thesis.bitperfectusb.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.thesis.bitperfectusb.`data`.local.db.dao.AudioTrackDao
import com.thesis.bitperfectusb.`data`.local.db.dao.AudioTrackDao_Impl
import com.thesis.bitperfectusb.`data`.local.db.dao.BenchmarkDao
import com.thesis.bitperfectusb.`data`.local.db.dao.BenchmarkDao_Impl
import com.thesis.bitperfectusb.`data`.local.db.dao.DacBenchmarkDao
import com.thesis.bitperfectusb.`data`.local.db.dao.DacBenchmarkDao_Impl
import com.thesis.bitperfectusb.`data`.local.db.dao.DacProfileDao
import com.thesis.bitperfectusb.`data`.local.db.dao.DacProfileDao_Impl
import com.thesis.bitperfectusb.`data`.local.db.dao.ExperimentDao
import com.thesis.bitperfectusb.`data`.local.db.dao.ExperimentDao_Impl
import com.thesis.bitperfectusb.`data`.local.db.dao.WatchedFolderDao
import com.thesis.bitperfectusb.`data`.local.db.dao.WatchedFolderDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _audioTrackDao: Lazy<AudioTrackDao> = lazy {
    AudioTrackDao_Impl(this)
  }

  private val _dacProfileDao: Lazy<DacProfileDao> = lazy {
    DacProfileDao_Impl(this)
  }

  private val _benchmarkDao: Lazy<BenchmarkDao> = lazy {
    BenchmarkDao_Impl(this)
  }

  private val _experimentDao: Lazy<ExperimentDao> = lazy {
    ExperimentDao_Impl(this)
  }

  private val _watchedFolderDao: Lazy<WatchedFolderDao> = lazy {
    WatchedFolderDao_Impl(this)
  }

  private val _dacBenchmarkDao: Lazy<DacBenchmarkDao> = lazy {
    DacBenchmarkDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(2,
        "694e7e3cfc2cbc89fedb74f509733d6a", "11da1b5368f755d35370ce180db6befa") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `audio_tracks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `filePath` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT, `durationMs` INTEGER NOT NULL, `format` TEXT NOT NULL, `sampleRateHz` INTEGER NOT NULL, `bitDepth` INTEGER NOT NULL, `channels` INTEGER NOT NULL, `fileSizeBytes` INTEGER NOT NULL, `dateAddedEpochMs` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_audio_tracks_filePath` ON `audio_tracks` (`filePath`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dac_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `vendorId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `productName` TEXT NOT NULL, `manufacturerName` TEXT, `isUac2` INTEGER NOT NULL, `supportedSampleRatesCsv` TEXT NOT NULL, `supportedBitDepthsCsv` TEXT NOT NULL, `maxChannels` INTEGER NOT NULL, `maxPacketSizeBytes` INTEGER NOT NULL, `hasAsyncFeedbackEndpoint` INTEGER NOT NULL, `dateProfiledEpochMs` INTEGER NOT NULL, `streamingOptionsEncoded` TEXT NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_dac_profiles_vendorId_productId` ON `dac_profiles` (`vendorId`, `productId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playback_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `trackId` INTEGER, `dacProfileId` INTEGER, `engineType` TEXT NOT NULL, `startEpochMs` INTEGER NOT NULL, `endEpochMs` INTEGER, `integrityScore` INTEGER NOT NULL, `avgLatencyMs` REAL NOT NULL, `avgCpuPercent` REAL NOT NULL, `avgMemoryMb` REAL NOT NULL, `dropoutCount` INTEGER NOT NULL, `bufferSizeBytes` INTEGER NOT NULL, `verifiedBitPerfect` INTEGER)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `benchmark_samples` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `timestampMs` INTEGER NOT NULL, `cpuPercent` REAL NOT NULL, `memoryMb` REAL NOT NULL, `latencyMs` REAL NOT NULL, `cumulativeDropouts` INTEGER NOT NULL, `engineType` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `experiment_runs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `experimentType` TEXT NOT NULL, `configLabel` TEXT NOT NULL, `timestampEpochMs` INTEGER NOT NULL, `sampleSize` INTEGER NOT NULL, `meanLatencyMs` REAL NOT NULL, `sdLatencyMs` REAL NOT NULL, `meanCpuPercent` REAL NOT NULL, `meanMemoryMb` REAL NOT NULL, `dropouts` INTEGER NOT NULL, `integrityScore` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `watched_folders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uriString` TEXT NOT NULL, `displayName` TEXT NOT NULL, `dateAddedEpochMs` INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_watched_folders_uriString` ON `watched_folders` (`uriString`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `dac_benchmarks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dacLabel` TEXT NOT NULL, `timestampEpochMs` INTEGER NOT NULL, `sampleRateHz` INTEGER NOT NULL, `bitDepth` INTEGER NOT NULL, `stable` INTEGER NOT NULL, `dropouts` INTEGER NOT NULL, `latencyMs` REAL NOT NULL, `bufferSizeBytes` INTEGER NOT NULL, `note` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '694e7e3cfc2cbc89fedb74f509733d6a')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `audio_tracks`")
        connection.execSQL("DROP TABLE IF EXISTS `dac_profiles`")
        connection.execSQL("DROP TABLE IF EXISTS `playback_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `benchmark_samples`")
        connection.execSQL("DROP TABLE IF EXISTS `experiment_runs`")
        connection.execSQL("DROP TABLE IF EXISTS `watched_folders`")
        connection.execSQL("DROP TABLE IF EXISTS `dac_benchmarks`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsAudioTracks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsAudioTracks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("filePath", TableInfo.Column("filePath", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("artist", TableInfo.Column("artist", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("durationMs", TableInfo.Column("durationMs", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("format", TableInfo.Column("format", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("sampleRateHz", TableInfo.Column("sampleRateHz", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("bitDepth", TableInfo.Column("bitDepth", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("channels", TableInfo.Column("channels", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("fileSizeBytes", TableInfo.Column("fileSizeBytes", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsAudioTracks.put("dateAddedEpochMs", TableInfo.Column("dateAddedEpochMs", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysAudioTracks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesAudioTracks: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesAudioTracks.add(TableInfo.Index("index_audio_tracks_filePath", true,
            listOf("filePath"), listOf("ASC")))
        val _infoAudioTracks: TableInfo = TableInfo("audio_tracks", _columnsAudioTracks,
            _foreignKeysAudioTracks, _indicesAudioTracks)
        val _existingAudioTracks: TableInfo = read(connection, "audio_tracks")
        if (!_infoAudioTracks.equals(_existingAudioTracks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |audio_tracks(com.thesis.bitperfectusb.data.local.db.entity.AudioTrackEntity).
              | Expected:
              |""".trimMargin() + _infoAudioTracks + """
              |
              | Found:
              |""".trimMargin() + _existingAudioTracks)
        }
        val _columnsDacProfiles: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDacProfiles.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("vendorId", TableInfo.Column("vendorId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("productId", TableInfo.Column("productId", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("productName", TableInfo.Column("productName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("manufacturerName", TableInfo.Column("manufacturerName", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("isUac2", TableInfo.Column("isUac2", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("supportedSampleRatesCsv",
            TableInfo.Column("supportedSampleRatesCsv", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("supportedBitDepthsCsv", TableInfo.Column("supportedBitDepthsCsv",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("maxChannels", TableInfo.Column("maxChannels", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("maxPacketSizeBytes", TableInfo.Column("maxPacketSizeBytes",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("hasAsyncFeedbackEndpoint",
            TableInfo.Column("hasAsyncFeedbackEndpoint", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("dateProfiledEpochMs", TableInfo.Column("dateProfiledEpochMs",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacProfiles.put("streamingOptionsEncoded",
            TableInfo.Column("streamingOptionsEncoded", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDacProfiles: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDacProfiles: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDacProfiles.add(TableInfo.Index("index_dac_profiles_vendorId_productId", true,
            listOf("vendorId", "productId"), listOf("ASC", "ASC")))
        val _infoDacProfiles: TableInfo = TableInfo("dac_profiles", _columnsDacProfiles,
            _foreignKeysDacProfiles, _indicesDacProfiles)
        val _existingDacProfiles: TableInfo = read(connection, "dac_profiles")
        if (!_infoDacProfiles.equals(_existingDacProfiles)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dac_profiles(com.thesis.bitperfectusb.data.local.db.entity.DacProfileEntity).
              | Expected:
              |""".trimMargin() + _infoDacProfiles + """
              |
              | Found:
              |""".trimMargin() + _existingDacProfiles)
        }
        val _columnsPlaybackSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaybackSessions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("trackId", TableInfo.Column("trackId", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("dacProfileId", TableInfo.Column("dacProfileId", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("engineType", TableInfo.Column("engineType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("startEpochMs", TableInfo.Column("startEpochMs", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("endEpochMs", TableInfo.Column("endEpochMs", "INTEGER", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("integrityScore", TableInfo.Column("integrityScore", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("avgLatencyMs", TableInfo.Column("avgLatencyMs", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("avgCpuPercent", TableInfo.Column("avgCpuPercent", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("avgMemoryMb", TableInfo.Column("avgMemoryMb", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("dropoutCount", TableInfo.Column("dropoutCount", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("bufferSizeBytes", TableInfo.Column("bufferSizeBytes",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaybackSessions.put("verifiedBitPerfect", TableInfo.Column("verifiedBitPerfect",
            "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaybackSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaybackSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlaybackSessions: TableInfo = TableInfo("playback_sessions",
            _columnsPlaybackSessions, _foreignKeysPlaybackSessions, _indicesPlaybackSessions)
        val _existingPlaybackSessions: TableInfo = read(connection, "playback_sessions")
        if (!_infoPlaybackSessions.equals(_existingPlaybackSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playback_sessions(com.thesis.bitperfectusb.data.local.db.entity.PlaybackSessionEntity).
              | Expected:
              |""".trimMargin() + _infoPlaybackSessions + """
              |
              | Found:
              |""".trimMargin() + _existingPlaybackSessions)
        }
        val _columnsBenchmarkSamples: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBenchmarkSamples.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("sessionId", TableInfo.Column("sessionId", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("timestampMs", TableInfo.Column("timestampMs", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("cpuPercent", TableInfo.Column("cpuPercent", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("memoryMb", TableInfo.Column("memoryMb", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("latencyMs", TableInfo.Column("latencyMs", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("cumulativeDropouts", TableInfo.Column("cumulativeDropouts",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBenchmarkSamples.put("engineType", TableInfo.Column("engineType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBenchmarkSamples: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBenchmarkSamples: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBenchmarkSamples: TableInfo = TableInfo("benchmark_samples",
            _columnsBenchmarkSamples, _foreignKeysBenchmarkSamples, _indicesBenchmarkSamples)
        val _existingBenchmarkSamples: TableInfo = read(connection, "benchmark_samples")
        if (!_infoBenchmarkSamples.equals(_existingBenchmarkSamples)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |benchmark_samples(com.thesis.bitperfectusb.data.local.db.entity.BenchmarkSampleEntity).
              | Expected:
              |""".trimMargin() + _infoBenchmarkSamples + """
              |
              | Found:
              |""".trimMargin() + _existingBenchmarkSamples)
        }
        val _columnsExperimentRuns: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExperimentRuns.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("experimentType", TableInfo.Column("experimentType", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("configLabel", TableInfo.Column("configLabel", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("timestampEpochMs", TableInfo.Column("timestampEpochMs",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("sampleSize", TableInfo.Column("sampleSize", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("meanLatencyMs", TableInfo.Column("meanLatencyMs", "REAL", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("sdLatencyMs", TableInfo.Column("sdLatencyMs", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("meanCpuPercent", TableInfo.Column("meanCpuPercent", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("meanMemoryMb", TableInfo.Column("meanMemoryMb", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("dropouts", TableInfo.Column("dropouts", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExperimentRuns.put("integrityScore", TableInfo.Column("integrityScore", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExperimentRuns: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExperimentRuns: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExperimentRuns: TableInfo = TableInfo("experiment_runs", _columnsExperimentRuns,
            _foreignKeysExperimentRuns, _indicesExperimentRuns)
        val _existingExperimentRuns: TableInfo = read(connection, "experiment_runs")
        if (!_infoExperimentRuns.equals(_existingExperimentRuns)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |experiment_runs(com.thesis.bitperfectusb.data.local.db.entity.ExperimentRunEntity).
              | Expected:
              |""".trimMargin() + _infoExperimentRuns + """
              |
              | Found:
              |""".trimMargin() + _existingExperimentRuns)
        }
        val _columnsWatchedFolders: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsWatchedFolders.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWatchedFolders.put("uriString", TableInfo.Column("uriString", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsWatchedFolders.put("displayName", TableInfo.Column("displayName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsWatchedFolders.put("dateAddedEpochMs", TableInfo.Column("dateAddedEpochMs",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysWatchedFolders: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesWatchedFolders: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesWatchedFolders.add(TableInfo.Index("index_watched_folders_uriString", true,
            listOf("uriString"), listOf("ASC")))
        val _infoWatchedFolders: TableInfo = TableInfo("watched_folders", _columnsWatchedFolders,
            _foreignKeysWatchedFolders, _indicesWatchedFolders)
        val _existingWatchedFolders: TableInfo = read(connection, "watched_folders")
        if (!_infoWatchedFolders.equals(_existingWatchedFolders)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |watched_folders(com.thesis.bitperfectusb.data.local.db.entity.WatchedFolderEntity).
              | Expected:
              |""".trimMargin() + _infoWatchedFolders + """
              |
              | Found:
              |""".trimMargin() + _existingWatchedFolders)
        }
        val _columnsDacBenchmarks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDacBenchmarks.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("dacLabel", TableInfo.Column("dacLabel", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("timestampEpochMs", TableInfo.Column("timestampEpochMs",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("sampleRateHz", TableInfo.Column("sampleRateHz", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("bitDepth", TableInfo.Column("bitDepth", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("stable", TableInfo.Column("stable", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("dropouts", TableInfo.Column("dropouts", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("latencyMs", TableInfo.Column("latencyMs", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("bufferSizeBytes", TableInfo.Column("bufferSizeBytes", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDacBenchmarks.put("note", TableInfo.Column("note", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDacBenchmarks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDacBenchmarks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDacBenchmarks: TableInfo = TableInfo("dac_benchmarks", _columnsDacBenchmarks,
            _foreignKeysDacBenchmarks, _indicesDacBenchmarks)
        val _existingDacBenchmarks: TableInfo = read(connection, "dac_benchmarks")
        if (!_infoDacBenchmarks.equals(_existingDacBenchmarks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |dac_benchmarks(com.thesis.bitperfectusb.data.local.db.entity.DacBenchmarkEntity).
              | Expected:
              |""".trimMargin() + _infoDacBenchmarks + """
              |
              | Found:
              |""".trimMargin() + _existingDacBenchmarks)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "audio_tracks", "dac_profiles",
        "playback_sessions", "benchmark_samples", "experiment_runs", "watched_folders",
        "dac_benchmarks")
  }

  public override fun clearAllTables() {
    super.performClear(false, "audio_tracks", "dac_profiles", "playback_sessions",
        "benchmark_samples", "experiment_runs", "watched_folders", "dac_benchmarks")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AudioTrackDao::class, AudioTrackDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DacProfileDao::class, DacProfileDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BenchmarkDao::class, BenchmarkDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ExperimentDao::class, ExperimentDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(WatchedFolderDao::class, WatchedFolderDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DacBenchmarkDao::class, DacBenchmarkDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun audioTrackDao(): AudioTrackDao = _audioTrackDao.value

  public override fun dacProfileDao(): DacProfileDao = _dacProfileDao.value

  public override fun benchmarkDao(): BenchmarkDao = _benchmarkDao.value

  public override fun experimentDao(): ExperimentDao = _experimentDao.value

  public override fun watchedFolderDao(): WatchedFolderDao = _watchedFolderDao.value

  public override fun dacBenchmarkDao(): DacBenchmarkDao = _dacBenchmarkDao.value
}
