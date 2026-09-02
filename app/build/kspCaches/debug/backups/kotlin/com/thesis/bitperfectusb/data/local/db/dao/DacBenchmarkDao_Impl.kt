package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.DacBenchmarkEntity
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
public class DacBenchmarkDao_Impl(
  __db: RoomDatabase,
) : DacBenchmarkDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDacBenchmarkEntity: EntityInsertAdapter<DacBenchmarkEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDacBenchmarkEntity = object : EntityInsertAdapter<DacBenchmarkEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `dac_benchmarks` (`id`,`dacLabel`,`timestampEpochMs`,`sampleRateHz`,`bitDepth`,`stable`,`dropouts`,`latencyMs`,`bufferSizeBytes`,`note`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DacBenchmarkEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.dacLabel)
        statement.bindLong(3, entity.timestampEpochMs)
        statement.bindLong(4, entity.sampleRateHz.toLong())
        statement.bindLong(5, entity.bitDepth.toLong())
        val _tmp: Int = if (entity.stable) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindLong(7, entity.dropouts.toLong())
        statement.bindDouble(8, entity.latencyMs)
        statement.bindLong(9, entity.bufferSizeBytes.toLong())
        statement.bindText(10, entity.note)
      }
    }
  }

  public override suspend fun insertAll(entities: List<DacBenchmarkEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDacBenchmarkEntity.insert(_connection, entities)
  }

  public override fun getHistoryForDac(dacLabel: String): Flow<List<DacBenchmarkEntity>> {
    val _sql: String =
        "SELECT * FROM dac_benchmarks WHERE dacLabel = ? ORDER BY timestampEpochMs DESC"
    return createFlow(__db, false, arrayOf("dac_benchmarks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, dacLabel)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDacLabel: Int = getColumnIndexOrThrow(_stmt, "dacLabel")
        val _columnIndexOfTimestampEpochMs: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMs")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitDepth: Int = getColumnIndexOrThrow(_stmt, "bitDepth")
        val _columnIndexOfStable: Int = getColumnIndexOrThrow(_stmt, "stable")
        val _columnIndexOfDropouts: Int = getColumnIndexOrThrow(_stmt, "dropouts")
        val _columnIndexOfLatencyMs: Int = getColumnIndexOrThrow(_stmt, "latencyMs")
        val _columnIndexOfBufferSizeBytes: Int = getColumnIndexOrThrow(_stmt, "bufferSizeBytes")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<DacBenchmarkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DacBenchmarkEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDacLabel: String
          _tmpDacLabel = _stmt.getText(_columnIndexOfDacLabel)
          val _tmpTimestampEpochMs: Long
          _tmpTimestampEpochMs = _stmt.getLong(_columnIndexOfTimestampEpochMs)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitDepth: Int
          _tmpBitDepth = _stmt.getLong(_columnIndexOfBitDepth).toInt()
          val _tmpStable: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStable).toInt()
          _tmpStable = _tmp != 0
          val _tmpDropouts: Int
          _tmpDropouts = _stmt.getLong(_columnIndexOfDropouts).toInt()
          val _tmpLatencyMs: Double
          _tmpLatencyMs = _stmt.getDouble(_columnIndexOfLatencyMs)
          val _tmpBufferSizeBytes: Int
          _tmpBufferSizeBytes = _stmt.getLong(_columnIndexOfBufferSizeBytes).toInt()
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item =
              DacBenchmarkEntity(_tmpId,_tmpDacLabel,_tmpTimestampEpochMs,_tmpSampleRateHz,_tmpBitDepth,_tmpStable,_tmpDropouts,_tmpLatencyMs,_tmpBufferSizeBytes,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllBenchmarks(): List<DacBenchmarkEntity> {
    val _sql: String = "SELECT * FROM dac_benchmarks ORDER BY timestampEpochMs DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDacLabel: Int = getColumnIndexOrThrow(_stmt, "dacLabel")
        val _columnIndexOfTimestampEpochMs: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMs")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitDepth: Int = getColumnIndexOrThrow(_stmt, "bitDepth")
        val _columnIndexOfStable: Int = getColumnIndexOrThrow(_stmt, "stable")
        val _columnIndexOfDropouts: Int = getColumnIndexOrThrow(_stmt, "dropouts")
        val _columnIndexOfLatencyMs: Int = getColumnIndexOrThrow(_stmt, "latencyMs")
        val _columnIndexOfBufferSizeBytes: Int = getColumnIndexOrThrow(_stmt, "bufferSizeBytes")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _result: MutableList<DacBenchmarkEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DacBenchmarkEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDacLabel: String
          _tmpDacLabel = _stmt.getText(_columnIndexOfDacLabel)
          val _tmpTimestampEpochMs: Long
          _tmpTimestampEpochMs = _stmt.getLong(_columnIndexOfTimestampEpochMs)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitDepth: Int
          _tmpBitDepth = _stmt.getLong(_columnIndexOfBitDepth).toInt()
          val _tmpStable: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfStable).toInt()
          _tmpStable = _tmp != 0
          val _tmpDropouts: Int
          _tmpDropouts = _stmt.getLong(_columnIndexOfDropouts).toInt()
          val _tmpLatencyMs: Double
          _tmpLatencyMs = _stmt.getDouble(_columnIndexOfLatencyMs)
          val _tmpBufferSizeBytes: Int
          _tmpBufferSizeBytes = _stmt.getLong(_columnIndexOfBufferSizeBytes).toInt()
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          _item =
              DacBenchmarkEntity(_tmpId,_tmpDacLabel,_tmpTimestampEpochMs,_tmpSampleRateHz,_tmpBitDepth,_tmpStable,_tmpDropouts,_tmpLatencyMs,_tmpBufferSizeBytes,_tmpNote)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM dac_benchmarks"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
