package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.DacProfileEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class DacProfileDao_Impl(
  __db: RoomDatabase,
) : DacProfileDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDacProfileEntity: EntityInsertAdapter<DacProfileEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDacProfileEntity = object : EntityInsertAdapter<DacProfileEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `dac_profiles` (`id`,`vendorId`,`productId`,`productName`,`manufacturerName`,`isUac2`,`supportedSampleRatesCsv`,`supportedBitDepthsCsv`,`maxChannels`,`maxPacketSizeBytes`,`hasAsyncFeedbackEndpoint`,`dateProfiledEpochMs`,`streamingOptionsEncoded`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DacProfileEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.vendorId.toLong())
        statement.bindLong(3, entity.productId.toLong())
        statement.bindText(4, entity.productName)
        val _tmpManufacturerName: String? = entity.manufacturerName
        if (_tmpManufacturerName == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpManufacturerName)
        }
        val _tmp: Int = if (entity.isUac2) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.supportedSampleRatesCsv)
        statement.bindText(8, entity.supportedBitDepthsCsv)
        statement.bindLong(9, entity.maxChannels.toLong())
        statement.bindLong(10, entity.maxPacketSizeBytes.toLong())
        val _tmp_1: Int = if (entity.hasAsyncFeedbackEndpoint) 1 else 0
        statement.bindLong(11, _tmp_1.toLong())
        statement.bindLong(12, entity.dateProfiledEpochMs)
        statement.bindText(13, entity.streamingOptionsEncoded)
      }
    }
  }

  public override suspend fun insert(profile: DacProfileEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfDacProfileEntity.insertAndReturnId(_connection, profile)
    _result
  }

  public override fun observeAll(): Flow<List<DacProfileEntity>> {
    val _sql: String = "SELECT * FROM dac_profiles ORDER BY dateProfiledEpochMs DESC"
    return createFlow(__db, false, arrayOf("dac_profiles")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfVendorId: Int = getColumnIndexOrThrow(_stmt, "vendorId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfManufacturerName: Int = getColumnIndexOrThrow(_stmt, "manufacturerName")
        val _columnIndexOfIsUac2: Int = getColumnIndexOrThrow(_stmt, "isUac2")
        val _columnIndexOfSupportedSampleRatesCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedSampleRatesCsv")
        val _columnIndexOfSupportedBitDepthsCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedBitDepthsCsv")
        val _columnIndexOfMaxChannels: Int = getColumnIndexOrThrow(_stmt, "maxChannels")
        val _columnIndexOfMaxPacketSizeBytes: Int = getColumnIndexOrThrow(_stmt,
            "maxPacketSizeBytes")
        val _columnIndexOfHasAsyncFeedbackEndpoint: Int = getColumnIndexOrThrow(_stmt,
            "hasAsyncFeedbackEndpoint")
        val _columnIndexOfDateProfiledEpochMs: Int = getColumnIndexOrThrow(_stmt,
            "dateProfiledEpochMs")
        val _columnIndexOfStreamingOptionsEncoded: Int = getColumnIndexOrThrow(_stmt,
            "streamingOptionsEncoded")
        val _result: MutableList<DacProfileEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DacProfileEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpVendorId: Int
          _tmpVendorId = _stmt.getLong(_columnIndexOfVendorId).toInt()
          val _tmpProductId: Int
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId).toInt()
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpManufacturerName: String?
          if (_stmt.isNull(_columnIndexOfManufacturerName)) {
            _tmpManufacturerName = null
          } else {
            _tmpManufacturerName = _stmt.getText(_columnIndexOfManufacturerName)
          }
          val _tmpIsUac2: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUac2).toInt()
          _tmpIsUac2 = _tmp != 0
          val _tmpSupportedSampleRatesCsv: String
          _tmpSupportedSampleRatesCsv = _stmt.getText(_columnIndexOfSupportedSampleRatesCsv)
          val _tmpSupportedBitDepthsCsv: String
          _tmpSupportedBitDepthsCsv = _stmt.getText(_columnIndexOfSupportedBitDepthsCsv)
          val _tmpMaxChannels: Int
          _tmpMaxChannels = _stmt.getLong(_columnIndexOfMaxChannels).toInt()
          val _tmpMaxPacketSizeBytes: Int
          _tmpMaxPacketSizeBytes = _stmt.getLong(_columnIndexOfMaxPacketSizeBytes).toInt()
          val _tmpHasAsyncFeedbackEndpoint: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHasAsyncFeedbackEndpoint).toInt()
          _tmpHasAsyncFeedbackEndpoint = _tmp_1 != 0
          val _tmpDateProfiledEpochMs: Long
          _tmpDateProfiledEpochMs = _stmt.getLong(_columnIndexOfDateProfiledEpochMs)
          val _tmpStreamingOptionsEncoded: String
          _tmpStreamingOptionsEncoded = _stmt.getText(_columnIndexOfStreamingOptionsEncoded)
          _item =
              DacProfileEntity(_tmpId,_tmpVendorId,_tmpProductId,_tmpProductName,_tmpManufacturerName,_tmpIsUac2,_tmpSupportedSampleRatesCsv,_tmpSupportedBitDepthsCsv,_tmpMaxChannels,_tmpMaxPacketSizeBytes,_tmpHasAsyncFeedbackEndpoint,_tmpDateProfiledEpochMs,_tmpStreamingOptionsEncoded)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findByUsbIds(vendorId: Int, productId: Int): DacProfileEntity? {
    val _sql: String = "SELECT * FROM dac_profiles WHERE vendorId = ? AND productId = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, vendorId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, productId.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfVendorId: Int = getColumnIndexOrThrow(_stmt, "vendorId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfManufacturerName: Int = getColumnIndexOrThrow(_stmt, "manufacturerName")
        val _columnIndexOfIsUac2: Int = getColumnIndexOrThrow(_stmt, "isUac2")
        val _columnIndexOfSupportedSampleRatesCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedSampleRatesCsv")
        val _columnIndexOfSupportedBitDepthsCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedBitDepthsCsv")
        val _columnIndexOfMaxChannels: Int = getColumnIndexOrThrow(_stmt, "maxChannels")
        val _columnIndexOfMaxPacketSizeBytes: Int = getColumnIndexOrThrow(_stmt,
            "maxPacketSizeBytes")
        val _columnIndexOfHasAsyncFeedbackEndpoint: Int = getColumnIndexOrThrow(_stmt,
            "hasAsyncFeedbackEndpoint")
        val _columnIndexOfDateProfiledEpochMs: Int = getColumnIndexOrThrow(_stmt,
            "dateProfiledEpochMs")
        val _columnIndexOfStreamingOptionsEncoded: Int = getColumnIndexOrThrow(_stmt,
            "streamingOptionsEncoded")
        val _result: DacProfileEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpVendorId: Int
          _tmpVendorId = _stmt.getLong(_columnIndexOfVendorId).toInt()
          val _tmpProductId: Int
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId).toInt()
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpManufacturerName: String?
          if (_stmt.isNull(_columnIndexOfManufacturerName)) {
            _tmpManufacturerName = null
          } else {
            _tmpManufacturerName = _stmt.getText(_columnIndexOfManufacturerName)
          }
          val _tmpIsUac2: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUac2).toInt()
          _tmpIsUac2 = _tmp != 0
          val _tmpSupportedSampleRatesCsv: String
          _tmpSupportedSampleRatesCsv = _stmt.getText(_columnIndexOfSupportedSampleRatesCsv)
          val _tmpSupportedBitDepthsCsv: String
          _tmpSupportedBitDepthsCsv = _stmt.getText(_columnIndexOfSupportedBitDepthsCsv)
          val _tmpMaxChannels: Int
          _tmpMaxChannels = _stmt.getLong(_columnIndexOfMaxChannels).toInt()
          val _tmpMaxPacketSizeBytes: Int
          _tmpMaxPacketSizeBytes = _stmt.getLong(_columnIndexOfMaxPacketSizeBytes).toInt()
          val _tmpHasAsyncFeedbackEndpoint: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHasAsyncFeedbackEndpoint).toInt()
          _tmpHasAsyncFeedbackEndpoint = _tmp_1 != 0
          val _tmpDateProfiledEpochMs: Long
          _tmpDateProfiledEpochMs = _stmt.getLong(_columnIndexOfDateProfiledEpochMs)
          val _tmpStreamingOptionsEncoded: String
          _tmpStreamingOptionsEncoded = _stmt.getText(_columnIndexOfStreamingOptionsEncoded)
          _result =
              DacProfileEntity(_tmpId,_tmpVendorId,_tmpProductId,_tmpProductName,_tmpManufacturerName,_tmpIsUac2,_tmpSupportedSampleRatesCsv,_tmpSupportedBitDepthsCsv,_tmpMaxChannels,_tmpMaxPacketSizeBytes,_tmpHasAsyncFeedbackEndpoint,_tmpDateProfiledEpochMs,_tmpStreamingOptionsEncoded)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): DacProfileEntity? {
    val _sql: String = "SELECT * FROM dac_profiles WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfVendorId: Int = getColumnIndexOrThrow(_stmt, "vendorId")
        val _columnIndexOfProductId: Int = getColumnIndexOrThrow(_stmt, "productId")
        val _columnIndexOfProductName: Int = getColumnIndexOrThrow(_stmt, "productName")
        val _columnIndexOfManufacturerName: Int = getColumnIndexOrThrow(_stmt, "manufacturerName")
        val _columnIndexOfIsUac2: Int = getColumnIndexOrThrow(_stmt, "isUac2")
        val _columnIndexOfSupportedSampleRatesCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedSampleRatesCsv")
        val _columnIndexOfSupportedBitDepthsCsv: Int = getColumnIndexOrThrow(_stmt,
            "supportedBitDepthsCsv")
        val _columnIndexOfMaxChannels: Int = getColumnIndexOrThrow(_stmt, "maxChannels")
        val _columnIndexOfMaxPacketSizeBytes: Int = getColumnIndexOrThrow(_stmt,
            "maxPacketSizeBytes")
        val _columnIndexOfHasAsyncFeedbackEndpoint: Int = getColumnIndexOrThrow(_stmt,
            "hasAsyncFeedbackEndpoint")
        val _columnIndexOfDateProfiledEpochMs: Int = getColumnIndexOrThrow(_stmt,
            "dateProfiledEpochMs")
        val _columnIndexOfStreamingOptionsEncoded: Int = getColumnIndexOrThrow(_stmt,
            "streamingOptionsEncoded")
        val _result: DacProfileEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpVendorId: Int
          _tmpVendorId = _stmt.getLong(_columnIndexOfVendorId).toInt()
          val _tmpProductId: Int
          _tmpProductId = _stmt.getLong(_columnIndexOfProductId).toInt()
          val _tmpProductName: String
          _tmpProductName = _stmt.getText(_columnIndexOfProductName)
          val _tmpManufacturerName: String?
          if (_stmt.isNull(_columnIndexOfManufacturerName)) {
            _tmpManufacturerName = null
          } else {
            _tmpManufacturerName = _stmt.getText(_columnIndexOfManufacturerName)
          }
          val _tmpIsUac2: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsUac2).toInt()
          _tmpIsUac2 = _tmp != 0
          val _tmpSupportedSampleRatesCsv: String
          _tmpSupportedSampleRatesCsv = _stmt.getText(_columnIndexOfSupportedSampleRatesCsv)
          val _tmpSupportedBitDepthsCsv: String
          _tmpSupportedBitDepthsCsv = _stmt.getText(_columnIndexOfSupportedBitDepthsCsv)
          val _tmpMaxChannels: Int
          _tmpMaxChannels = _stmt.getLong(_columnIndexOfMaxChannels).toInt()
          val _tmpMaxPacketSizeBytes: Int
          _tmpMaxPacketSizeBytes = _stmt.getLong(_columnIndexOfMaxPacketSizeBytes).toInt()
          val _tmpHasAsyncFeedbackEndpoint: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfHasAsyncFeedbackEndpoint).toInt()
          _tmpHasAsyncFeedbackEndpoint = _tmp_1 != 0
          val _tmpDateProfiledEpochMs: Long
          _tmpDateProfiledEpochMs = _stmt.getLong(_columnIndexOfDateProfiledEpochMs)
          val _tmpStreamingOptionsEncoded: String
          _tmpStreamingOptionsEncoded = _stmt.getText(_columnIndexOfStreamingOptionsEncoded)
          _result =
              DacProfileEntity(_tmpId,_tmpVendorId,_tmpProductId,_tmpProductName,_tmpManufacturerName,_tmpIsUac2,_tmpSupportedSampleRatesCsv,_tmpSupportedBitDepthsCsv,_tmpMaxChannels,_tmpMaxPacketSizeBytes,_tmpHasAsyncFeedbackEndpoint,_tmpDateProfiledEpochMs,_tmpStreamingOptionsEncoded)
        } else {
          _result = null
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
