package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.BenchmarkSampleEntity
import com.thesis.bitperfectusb.`data`.local.db.entity.PlaybackSessionEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BenchmarkDao_Impl(
  __db: RoomDatabase,
) : BenchmarkDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPlaybackSessionEntity: EntityInsertAdapter<PlaybackSessionEntity>

  private val __insertAdapterOfBenchmarkSampleEntity: EntityInsertAdapter<BenchmarkSampleEntity>

  private val __updateAdapterOfPlaybackSessionEntity:
      EntityDeleteOrUpdateAdapter<PlaybackSessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPlaybackSessionEntity = object :
        EntityInsertAdapter<PlaybackSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `playback_sessions` (`id`,`trackId`,`dacProfileId`,`engineType`,`startEpochMs`,`endEpochMs`,`integrityScore`,`avgLatencyMs`,`avgCpuPercent`,`avgMemoryMb`,`dropoutCount`,`bufferSizeBytes`,`verifiedBitPerfect`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaybackSessionEntity) {
        statement.bindLong(1, entity.id)
        val _tmpTrackId: Long? = entity.trackId
        if (_tmpTrackId == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmpTrackId)
        }
        val _tmpDacProfileId: Long? = entity.dacProfileId
        if (_tmpDacProfileId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpDacProfileId)
        }
        statement.bindText(4, entity.engineType)
        statement.bindLong(5, entity.startEpochMs)
        val _tmpEndEpochMs: Long? = entity.endEpochMs
        if (_tmpEndEpochMs == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndEpochMs)
        }
        statement.bindLong(7, entity.integrityScore.toLong())
        statement.bindDouble(8, entity.avgLatencyMs)
        statement.bindDouble(9, entity.avgCpuPercent)
        statement.bindDouble(10, entity.avgMemoryMb)
        statement.bindLong(11, entity.dropoutCount.toLong())
        statement.bindLong(12, entity.bufferSizeBytes.toLong())
        val _tmpVerifiedBitPerfect: Boolean? = entity.verifiedBitPerfect
        val _tmp: Int? = _tmpVerifiedBitPerfect?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmp.toLong())
        }
      }
    }
    this.__insertAdapterOfBenchmarkSampleEntity = object :
        EntityInsertAdapter<BenchmarkSampleEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `benchmark_samples` (`id`,`sessionId`,`timestampMs`,`cpuPercent`,`memoryMb`,`latencyMs`,`cumulativeDropouts`,`engineType`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: BenchmarkSampleEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.sessionId)
        statement.bindLong(3, entity.timestampMs)
        statement.bindDouble(4, entity.cpuPercent)
        statement.bindDouble(5, entity.memoryMb)
        statement.bindDouble(6, entity.latencyMs)
        statement.bindLong(7, entity.cumulativeDropouts.toLong())
        statement.bindText(8, entity.engineType)
      }
    }
    this.__updateAdapterOfPlaybackSessionEntity = object :
        EntityDeleteOrUpdateAdapter<PlaybackSessionEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `playback_sessions` SET `id` = ?,`trackId` = ?,`dacProfileId` = ?,`engineType` = ?,`startEpochMs` = ?,`endEpochMs` = ?,`integrityScore` = ?,`avgLatencyMs` = ?,`avgCpuPercent` = ?,`avgMemoryMb` = ?,`dropoutCount` = ?,`bufferSizeBytes` = ?,`verifiedBitPerfect` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: PlaybackSessionEntity) {
        statement.bindLong(1, entity.id)
        val _tmpTrackId: Long? = entity.trackId
        if (_tmpTrackId == null) {
          statement.bindNull(2)
        } else {
          statement.bindLong(2, _tmpTrackId)
        }
        val _tmpDacProfileId: Long? = entity.dacProfileId
        if (_tmpDacProfileId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpDacProfileId)
        }
        statement.bindText(4, entity.engineType)
        statement.bindLong(5, entity.startEpochMs)
        val _tmpEndEpochMs: Long? = entity.endEpochMs
        if (_tmpEndEpochMs == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndEpochMs)
        }
        statement.bindLong(7, entity.integrityScore.toLong())
        statement.bindDouble(8, entity.avgLatencyMs)
        statement.bindDouble(9, entity.avgCpuPercent)
        statement.bindDouble(10, entity.avgMemoryMb)
        statement.bindLong(11, entity.dropoutCount.toLong())
        statement.bindLong(12, entity.bufferSizeBytes.toLong())
        val _tmpVerifiedBitPerfect: Boolean? = entity.verifiedBitPerfect
        val _tmp: Int? = _tmpVerifiedBitPerfect?.let { if (it) 1 else 0 }
        if (_tmp == null) {
          statement.bindNull(13)
        } else {
          statement.bindLong(13, _tmp.toLong())
        }
        statement.bindLong(14, entity.id)
      }
    }
  }

  public override suspend fun insertSession(session: PlaybackSessionEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfPlaybackSessionEntity.insertAndReturnId(_connection,
        session)
    _result
  }

  public override suspend fun insertSample(sample: BenchmarkSampleEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfBenchmarkSampleEntity.insert(_connection, sample)
  }

  public override suspend fun updateSession(session: PlaybackSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfPlaybackSessionEntity.handle(_connection, session)
  }

  public override suspend fun getSession(id: Long): PlaybackSessionEntity? {
    val _sql: String = "SELECT * FROM playback_sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfDacProfileId: Int = getColumnIndexOrThrow(_stmt, "dacProfileId")
        val _columnIndexOfEngineType: Int = getColumnIndexOrThrow(_stmt, "engineType")
        val _columnIndexOfStartEpochMs: Int = getColumnIndexOrThrow(_stmt, "startEpochMs")
        val _columnIndexOfEndEpochMs: Int = getColumnIndexOrThrow(_stmt, "endEpochMs")
        val _columnIndexOfIntegrityScore: Int = getColumnIndexOrThrow(_stmt, "integrityScore")
        val _columnIndexOfAvgLatencyMs: Int = getColumnIndexOrThrow(_stmt, "avgLatencyMs")
        val _columnIndexOfAvgCpuPercent: Int = getColumnIndexOrThrow(_stmt, "avgCpuPercent")
        val _columnIndexOfAvgMemoryMb: Int = getColumnIndexOrThrow(_stmt, "avgMemoryMb")
        val _columnIndexOfDropoutCount: Int = getColumnIndexOrThrow(_stmt, "dropoutCount")
        val _columnIndexOfBufferSizeBytes: Int = getColumnIndexOrThrow(_stmt, "bufferSizeBytes")
        val _columnIndexOfVerifiedBitPerfect: Int = getColumnIndexOrThrow(_stmt,
            "verifiedBitPerfect")
        val _result: PlaybackSessionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTrackId: Long?
          if (_stmt.isNull(_columnIndexOfTrackId)) {
            _tmpTrackId = null
          } else {
            _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          }
          val _tmpDacProfileId: Long?
          if (_stmt.isNull(_columnIndexOfDacProfileId)) {
            _tmpDacProfileId = null
          } else {
            _tmpDacProfileId = _stmt.getLong(_columnIndexOfDacProfileId)
          }
          val _tmpEngineType: String
          _tmpEngineType = _stmt.getText(_columnIndexOfEngineType)
          val _tmpStartEpochMs: Long
          _tmpStartEpochMs = _stmt.getLong(_columnIndexOfStartEpochMs)
          val _tmpEndEpochMs: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMs)) {
            _tmpEndEpochMs = null
          } else {
            _tmpEndEpochMs = _stmt.getLong(_columnIndexOfEndEpochMs)
          }
          val _tmpIntegrityScore: Int
          _tmpIntegrityScore = _stmt.getLong(_columnIndexOfIntegrityScore).toInt()
          val _tmpAvgLatencyMs: Double
          _tmpAvgLatencyMs = _stmt.getDouble(_columnIndexOfAvgLatencyMs)
          val _tmpAvgCpuPercent: Double
          _tmpAvgCpuPercent = _stmt.getDouble(_columnIndexOfAvgCpuPercent)
          val _tmpAvgMemoryMb: Double
          _tmpAvgMemoryMb = _stmt.getDouble(_columnIndexOfAvgMemoryMb)
          val _tmpDropoutCount: Int
          _tmpDropoutCount = _stmt.getLong(_columnIndexOfDropoutCount).toInt()
          val _tmpBufferSizeBytes: Int
          _tmpBufferSizeBytes = _stmt.getLong(_columnIndexOfBufferSizeBytes).toInt()
          val _tmpVerifiedBitPerfect: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfVerifiedBitPerfect)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfVerifiedBitPerfect).toInt()
          }
          _tmpVerifiedBitPerfect = _tmp?.let { it != 0 }
          _result =
              PlaybackSessionEntity(_tmpId,_tmpTrackId,_tmpDacProfileId,_tmpEngineType,_tmpStartEpochMs,_tmpEndEpochMs,_tmpIntegrityScore,_tmpAvgLatencyMs,_tmpAvgCpuPercent,_tmpAvgMemoryMb,_tmpDropoutCount,_tmpBufferSizeBytes,_tmpVerifiedBitPerfect)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSessions(): Flow<List<PlaybackSessionEntity>> {
    val _sql: String = "SELECT * FROM playback_sessions ORDER BY startEpochMs DESC"
    return createFlow(__db, false, arrayOf("playback_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfDacProfileId: Int = getColumnIndexOrThrow(_stmt, "dacProfileId")
        val _columnIndexOfEngineType: Int = getColumnIndexOrThrow(_stmt, "engineType")
        val _columnIndexOfStartEpochMs: Int = getColumnIndexOrThrow(_stmt, "startEpochMs")
        val _columnIndexOfEndEpochMs: Int = getColumnIndexOrThrow(_stmt, "endEpochMs")
        val _columnIndexOfIntegrityScore: Int = getColumnIndexOrThrow(_stmt, "integrityScore")
        val _columnIndexOfAvgLatencyMs: Int = getColumnIndexOrThrow(_stmt, "avgLatencyMs")
        val _columnIndexOfAvgCpuPercent: Int = getColumnIndexOrThrow(_stmt, "avgCpuPercent")
        val _columnIndexOfAvgMemoryMb: Int = getColumnIndexOrThrow(_stmt, "avgMemoryMb")
        val _columnIndexOfDropoutCount: Int = getColumnIndexOrThrow(_stmt, "dropoutCount")
        val _columnIndexOfBufferSizeBytes: Int = getColumnIndexOrThrow(_stmt, "bufferSizeBytes")
        val _columnIndexOfVerifiedBitPerfect: Int = getColumnIndexOrThrow(_stmt,
            "verifiedBitPerfect")
        val _result: MutableList<PlaybackSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaybackSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTrackId: Long?
          if (_stmt.isNull(_columnIndexOfTrackId)) {
            _tmpTrackId = null
          } else {
            _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          }
          val _tmpDacProfileId: Long?
          if (_stmt.isNull(_columnIndexOfDacProfileId)) {
            _tmpDacProfileId = null
          } else {
            _tmpDacProfileId = _stmt.getLong(_columnIndexOfDacProfileId)
          }
          val _tmpEngineType: String
          _tmpEngineType = _stmt.getText(_columnIndexOfEngineType)
          val _tmpStartEpochMs: Long
          _tmpStartEpochMs = _stmt.getLong(_columnIndexOfStartEpochMs)
          val _tmpEndEpochMs: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMs)) {
            _tmpEndEpochMs = null
          } else {
            _tmpEndEpochMs = _stmt.getLong(_columnIndexOfEndEpochMs)
          }
          val _tmpIntegrityScore: Int
          _tmpIntegrityScore = _stmt.getLong(_columnIndexOfIntegrityScore).toInt()
          val _tmpAvgLatencyMs: Double
          _tmpAvgLatencyMs = _stmt.getDouble(_columnIndexOfAvgLatencyMs)
          val _tmpAvgCpuPercent: Double
          _tmpAvgCpuPercent = _stmt.getDouble(_columnIndexOfAvgCpuPercent)
          val _tmpAvgMemoryMb: Double
          _tmpAvgMemoryMb = _stmt.getDouble(_columnIndexOfAvgMemoryMb)
          val _tmpDropoutCount: Int
          _tmpDropoutCount = _stmt.getLong(_columnIndexOfDropoutCount).toInt()
          val _tmpBufferSizeBytes: Int
          _tmpBufferSizeBytes = _stmt.getLong(_columnIndexOfBufferSizeBytes).toInt()
          val _tmpVerifiedBitPerfect: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfVerifiedBitPerfect)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfVerifiedBitPerfect).toInt()
          }
          _tmpVerifiedBitPerfect = _tmp?.let { it != 0 }
          _item =
              PlaybackSessionEntity(_tmpId,_tmpTrackId,_tmpDacProfileId,_tmpEngineType,_tmpStartEpochMs,_tmpEndEpochMs,_tmpIntegrityScore,_tmpAvgLatencyMs,_tmpAvgCpuPercent,_tmpAvgMemoryMb,_tmpDropoutCount,_tmpBufferSizeBytes,_tmpVerifiedBitPerfect)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSessions(): List<PlaybackSessionEntity> {
    val _sql: String = "SELECT * FROM playback_sessions ORDER BY startEpochMs DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTrackId: Int = getColumnIndexOrThrow(_stmt, "trackId")
        val _columnIndexOfDacProfileId: Int = getColumnIndexOrThrow(_stmt, "dacProfileId")
        val _columnIndexOfEngineType: Int = getColumnIndexOrThrow(_stmt, "engineType")
        val _columnIndexOfStartEpochMs: Int = getColumnIndexOrThrow(_stmt, "startEpochMs")
        val _columnIndexOfEndEpochMs: Int = getColumnIndexOrThrow(_stmt, "endEpochMs")
        val _columnIndexOfIntegrityScore: Int = getColumnIndexOrThrow(_stmt, "integrityScore")
        val _columnIndexOfAvgLatencyMs: Int = getColumnIndexOrThrow(_stmt, "avgLatencyMs")
        val _columnIndexOfAvgCpuPercent: Int = getColumnIndexOrThrow(_stmt, "avgCpuPercent")
        val _columnIndexOfAvgMemoryMb: Int = getColumnIndexOrThrow(_stmt, "avgMemoryMb")
        val _columnIndexOfDropoutCount: Int = getColumnIndexOrThrow(_stmt, "dropoutCount")
        val _columnIndexOfBufferSizeBytes: Int = getColumnIndexOrThrow(_stmt, "bufferSizeBytes")
        val _columnIndexOfVerifiedBitPerfect: Int = getColumnIndexOrThrow(_stmt,
            "verifiedBitPerfect")
        val _result: MutableList<PlaybackSessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaybackSessionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTrackId: Long?
          if (_stmt.isNull(_columnIndexOfTrackId)) {
            _tmpTrackId = null
          } else {
            _tmpTrackId = _stmt.getLong(_columnIndexOfTrackId)
          }
          val _tmpDacProfileId: Long?
          if (_stmt.isNull(_columnIndexOfDacProfileId)) {
            _tmpDacProfileId = null
          } else {
            _tmpDacProfileId = _stmt.getLong(_columnIndexOfDacProfileId)
          }
          val _tmpEngineType: String
          _tmpEngineType = _stmt.getText(_columnIndexOfEngineType)
          val _tmpStartEpochMs: Long
          _tmpStartEpochMs = _stmt.getLong(_columnIndexOfStartEpochMs)
          val _tmpEndEpochMs: Long?
          if (_stmt.isNull(_columnIndexOfEndEpochMs)) {
            _tmpEndEpochMs = null
          } else {
            _tmpEndEpochMs = _stmt.getLong(_columnIndexOfEndEpochMs)
          }
          val _tmpIntegrityScore: Int
          _tmpIntegrityScore = _stmt.getLong(_columnIndexOfIntegrityScore).toInt()
          val _tmpAvgLatencyMs: Double
          _tmpAvgLatencyMs = _stmt.getDouble(_columnIndexOfAvgLatencyMs)
          val _tmpAvgCpuPercent: Double
          _tmpAvgCpuPercent = _stmt.getDouble(_columnIndexOfAvgCpuPercent)
          val _tmpAvgMemoryMb: Double
          _tmpAvgMemoryMb = _stmt.getDouble(_columnIndexOfAvgMemoryMb)
          val _tmpDropoutCount: Int
          _tmpDropoutCount = _stmt.getLong(_columnIndexOfDropoutCount).toInt()
          val _tmpBufferSizeBytes: Int
          _tmpBufferSizeBytes = _stmt.getLong(_columnIndexOfBufferSizeBytes).toInt()
          val _tmpVerifiedBitPerfect: Boolean?
          val _tmp: Int?
          if (_stmt.isNull(_columnIndexOfVerifiedBitPerfect)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(_columnIndexOfVerifiedBitPerfect).toInt()
          }
          _tmpVerifiedBitPerfect = _tmp?.let { it != 0 }
          _item =
              PlaybackSessionEntity(_tmpId,_tmpTrackId,_tmpDacProfileId,_tmpEngineType,_tmpStartEpochMs,_tmpEndEpochMs,_tmpIntegrityScore,_tmpAvgLatencyMs,_tmpAvgCpuPercent,_tmpAvgMemoryMb,_tmpDropoutCount,_tmpBufferSizeBytes,_tmpVerifiedBitPerfect)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSamplesForSession(sessionId: Long): List<BenchmarkSampleEntity> {
    val _sql: String =
        "SELECT * FROM benchmark_samples WHERE sessionId = ? ORDER BY timestampMs ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfTimestampMs: Int = getColumnIndexOrThrow(_stmt, "timestampMs")
        val _columnIndexOfCpuPercent: Int = getColumnIndexOrThrow(_stmt, "cpuPercent")
        val _columnIndexOfMemoryMb: Int = getColumnIndexOrThrow(_stmt, "memoryMb")
        val _columnIndexOfLatencyMs: Int = getColumnIndexOrThrow(_stmt, "latencyMs")
        val _columnIndexOfCumulativeDropouts: Int = getColumnIndexOrThrow(_stmt,
            "cumulativeDropouts")
        val _columnIndexOfEngineType: Int = getColumnIndexOrThrow(_stmt, "engineType")
        val _result: MutableList<BenchmarkSampleEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: BenchmarkSampleEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSessionId: Long
          _tmpSessionId = _stmt.getLong(_columnIndexOfSessionId)
          val _tmpTimestampMs: Long
          _tmpTimestampMs = _stmt.getLong(_columnIndexOfTimestampMs)
          val _tmpCpuPercent: Double
          _tmpCpuPercent = _stmt.getDouble(_columnIndexOfCpuPercent)
          val _tmpMemoryMb: Double
          _tmpMemoryMb = _stmt.getDouble(_columnIndexOfMemoryMb)
          val _tmpLatencyMs: Double
          _tmpLatencyMs = _stmt.getDouble(_columnIndexOfLatencyMs)
          val _tmpCumulativeDropouts: Int
          _tmpCumulativeDropouts = _stmt.getLong(_columnIndexOfCumulativeDropouts).toInt()
          val _tmpEngineType: String
          _tmpEngineType = _stmt.getText(_columnIndexOfEngineType)
          _item =
              BenchmarkSampleEntity(_tmpId,_tmpSessionId,_tmpTimestampMs,_tmpCpuPercent,_tmpMemoryMb,_tmpLatencyMs,_tmpCumulativeDropouts,_tmpEngineType)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
