package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.ExperimentRunEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ExperimentDao_Impl(
  __db: RoomDatabase,
) : ExperimentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExperimentRunEntity: EntityInsertAdapter<ExperimentRunEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfExperimentRunEntity = object : EntityInsertAdapter<ExperimentRunEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `experiment_runs` (`id`,`experimentType`,`configLabel`,`timestampEpochMs`,`sampleSize`,`meanLatencyMs`,`sdLatencyMs`,`meanCpuPercent`,`meanMemoryMb`,`dropouts`,`integrityScore`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExperimentRunEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.experimentType)
        statement.bindText(3, entity.configLabel)
        statement.bindLong(4, entity.timestampEpochMs)
        statement.bindLong(5, entity.sampleSize.toLong())
        statement.bindDouble(6, entity.meanLatencyMs)
        statement.bindDouble(7, entity.sdLatencyMs)
        statement.bindDouble(8, entity.meanCpuPercent)
        statement.bindDouble(9, entity.meanMemoryMb)
        statement.bindLong(10, entity.dropouts.toLong())
        statement.bindLong(11, entity.integrityScore.toLong())
      }
    }
  }

  public override suspend fun insert(run: ExperimentRunEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfExperimentRunEntity.insertAndReturnId(_connection, run)
    _result
  }

  public override fun observeByType(type: String): Flow<List<ExperimentRunEntity>> {
    val _sql: String =
        "SELECT * FROM experiment_runs WHERE experimentType = ? ORDER BY timestampEpochMs ASC"
    return createFlow(__db, false, arrayOf("experiment_runs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExperimentType: Int = getColumnIndexOrThrow(_stmt, "experimentType")
        val _columnIndexOfConfigLabel: Int = getColumnIndexOrThrow(_stmt, "configLabel")
        val _columnIndexOfTimestampEpochMs: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMs")
        val _columnIndexOfSampleSize: Int = getColumnIndexOrThrow(_stmt, "sampleSize")
        val _columnIndexOfMeanLatencyMs: Int = getColumnIndexOrThrow(_stmt, "meanLatencyMs")
        val _columnIndexOfSdLatencyMs: Int = getColumnIndexOrThrow(_stmt, "sdLatencyMs")
        val _columnIndexOfMeanCpuPercent: Int = getColumnIndexOrThrow(_stmt, "meanCpuPercent")
        val _columnIndexOfMeanMemoryMb: Int = getColumnIndexOrThrow(_stmt, "meanMemoryMb")
        val _columnIndexOfDropouts: Int = getColumnIndexOrThrow(_stmt, "dropouts")
        val _columnIndexOfIntegrityScore: Int = getColumnIndexOrThrow(_stmt, "integrityScore")
        val _result: MutableList<ExperimentRunEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExperimentRunEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExperimentType: String
          _tmpExperimentType = _stmt.getText(_columnIndexOfExperimentType)
          val _tmpConfigLabel: String
          _tmpConfigLabel = _stmt.getText(_columnIndexOfConfigLabel)
          val _tmpTimestampEpochMs: Long
          _tmpTimestampEpochMs = _stmt.getLong(_columnIndexOfTimestampEpochMs)
          val _tmpSampleSize: Int
          _tmpSampleSize = _stmt.getLong(_columnIndexOfSampleSize).toInt()
          val _tmpMeanLatencyMs: Double
          _tmpMeanLatencyMs = _stmt.getDouble(_columnIndexOfMeanLatencyMs)
          val _tmpSdLatencyMs: Double
          _tmpSdLatencyMs = _stmt.getDouble(_columnIndexOfSdLatencyMs)
          val _tmpMeanCpuPercent: Double
          _tmpMeanCpuPercent = _stmt.getDouble(_columnIndexOfMeanCpuPercent)
          val _tmpMeanMemoryMb: Double
          _tmpMeanMemoryMb = _stmt.getDouble(_columnIndexOfMeanMemoryMb)
          val _tmpDropouts: Int
          _tmpDropouts = _stmt.getLong(_columnIndexOfDropouts).toInt()
          val _tmpIntegrityScore: Int
          _tmpIntegrityScore = _stmt.getLong(_columnIndexOfIntegrityScore).toInt()
          _item =
              ExperimentRunEntity(_tmpId,_tmpExperimentType,_tmpConfigLabel,_tmpTimestampEpochMs,_tmpSampleSize,_tmpMeanLatencyMs,_tmpSdLatencyMs,_tmpMeanCpuPercent,_tmpMeanMemoryMb,_tmpDropouts,_tmpIntegrityScore)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getByType(type: String): List<ExperimentRunEntity> {
    val _sql: String =
        "SELECT * FROM experiment_runs WHERE experimentType = ? ORDER BY timestampEpochMs ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExperimentType: Int = getColumnIndexOrThrow(_stmt, "experimentType")
        val _columnIndexOfConfigLabel: Int = getColumnIndexOrThrow(_stmt, "configLabel")
        val _columnIndexOfTimestampEpochMs: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMs")
        val _columnIndexOfSampleSize: Int = getColumnIndexOrThrow(_stmt, "sampleSize")
        val _columnIndexOfMeanLatencyMs: Int = getColumnIndexOrThrow(_stmt, "meanLatencyMs")
        val _columnIndexOfSdLatencyMs: Int = getColumnIndexOrThrow(_stmt, "sdLatencyMs")
        val _columnIndexOfMeanCpuPercent: Int = getColumnIndexOrThrow(_stmt, "meanCpuPercent")
        val _columnIndexOfMeanMemoryMb: Int = getColumnIndexOrThrow(_stmt, "meanMemoryMb")
        val _columnIndexOfDropouts: Int = getColumnIndexOrThrow(_stmt, "dropouts")
        val _columnIndexOfIntegrityScore: Int = getColumnIndexOrThrow(_stmt, "integrityScore")
        val _result: MutableList<ExperimentRunEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExperimentRunEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpExperimentType: String
          _tmpExperimentType = _stmt.getText(_columnIndexOfExperimentType)
          val _tmpConfigLabel: String
          _tmpConfigLabel = _stmt.getText(_columnIndexOfConfigLabel)
          val _tmpTimestampEpochMs: Long
          _tmpTimestampEpochMs = _stmt.getLong(_columnIndexOfTimestampEpochMs)
          val _tmpSampleSize: Int
          _tmpSampleSize = _stmt.getLong(_columnIndexOfSampleSize).toInt()
          val _tmpMeanLatencyMs: Double
          _tmpMeanLatencyMs = _stmt.getDouble(_columnIndexOfMeanLatencyMs)
          val _tmpSdLatencyMs: Double
          _tmpSdLatencyMs = _stmt.getDouble(_columnIndexOfSdLatencyMs)
          val _tmpMeanCpuPercent: Double
          _tmpMeanCpuPercent = _stmt.getDouble(_columnIndexOfMeanCpuPercent)
          val _tmpMeanMemoryMb: Double
          _tmpMeanMemoryMb = _stmt.getDouble(_columnIndexOfMeanMemoryMb)
          val _tmpDropouts: Int
          _tmpDropouts = _stmt.getLong(_columnIndexOfDropouts).toInt()
          val _tmpIntegrityScore: Int
          _tmpIntegrityScore = _stmt.getLong(_columnIndexOfIntegrityScore).toInt()
          _item =
              ExperimentRunEntity(_tmpId,_tmpExperimentType,_tmpConfigLabel,_tmpTimestampEpochMs,_tmpSampleSize,_tmpMeanLatencyMs,_tmpSdLatencyMs,_tmpMeanCpuPercent,_tmpMeanMemoryMb,_tmpDropouts,_tmpIntegrityScore)
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
