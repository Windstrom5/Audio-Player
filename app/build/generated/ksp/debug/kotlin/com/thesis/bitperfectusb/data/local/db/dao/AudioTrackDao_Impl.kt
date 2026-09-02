package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.AudioTrackEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AudioTrackDao_Impl(
  __db: RoomDatabase,
) : AudioTrackDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAudioTrackEntity: EntityInsertAdapter<AudioTrackEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAudioTrackEntity = object : EntityInsertAdapter<AudioTrackEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `audio_tracks` (`id`,`filePath`,`title`,`artist`,`durationMs`,`format`,`sampleRateHz`,`bitDepth`,`channels`,`fileSizeBytes`,`dateAddedEpochMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AudioTrackEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.filePath)
        statement.bindText(3, entity.title)
        val _tmpArtist: String? = entity.artist
        if (_tmpArtist == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpArtist)
        }
        statement.bindLong(5, entity.durationMs)
        statement.bindText(6, entity.format)
        statement.bindLong(7, entity.sampleRateHz.toLong())
        statement.bindLong(8, entity.bitDepth.toLong())
        statement.bindLong(9, entity.channels.toLong())
        statement.bindLong(10, entity.fileSizeBytes)
        statement.bindLong(11, entity.dateAddedEpochMs)
      }
    }
  }

  public override suspend fun insertAll(tracks: List<AudioTrackEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAudioTrackEntity.insert(_connection, tracks)
  }

  public override fun observeAll(): Flow<List<AudioTrackEntity>> {
    val _sql: String = "SELECT * FROM audio_tracks ORDER BY title ASC"
    return createFlow(__db, false, arrayOf("audio_tracks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfFormat: Int = getColumnIndexOrThrow(_stmt, "format")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitDepth: Int = getColumnIndexOrThrow(_stmt, "bitDepth")
        val _columnIndexOfChannels: Int = getColumnIndexOrThrow(_stmt, "channels")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDateAddedEpochMs: Int = getColumnIndexOrThrow(_stmt, "dateAddedEpochMs")
        val _result: MutableList<AudioTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AudioTrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String?
          if (_stmt.isNull(_columnIndexOfArtist)) {
            _tmpArtist = null
          } else {
            _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpFormat: String
          _tmpFormat = _stmt.getText(_columnIndexOfFormat)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitDepth: Int
          _tmpBitDepth = _stmt.getLong(_columnIndexOfBitDepth).toInt()
          val _tmpChannels: Int
          _tmpChannels = _stmt.getLong(_columnIndexOfChannels).toInt()
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDateAddedEpochMs: Long
          _tmpDateAddedEpochMs = _stmt.getLong(_columnIndexOfDateAddedEpochMs)
          _item =
              AudioTrackEntity(_tmpId,_tmpFilePath,_tmpTitle,_tmpArtist,_tmpDurationMs,_tmpFormat,_tmpSampleRateHz,_tmpBitDepth,_tmpChannels,_tmpFileSizeBytes,_tmpDateAddedEpochMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getById(id: Long): AudioTrackEntity? {
    val _sql: String = "SELECT * FROM audio_tracks WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfFormat: Int = getColumnIndexOrThrow(_stmt, "format")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitDepth: Int = getColumnIndexOrThrow(_stmt, "bitDepth")
        val _columnIndexOfChannels: Int = getColumnIndexOrThrow(_stmt, "channels")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDateAddedEpochMs: Int = getColumnIndexOrThrow(_stmt, "dateAddedEpochMs")
        val _result: AudioTrackEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String?
          if (_stmt.isNull(_columnIndexOfArtist)) {
            _tmpArtist = null
          } else {
            _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpFormat: String
          _tmpFormat = _stmt.getText(_columnIndexOfFormat)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitDepth: Int
          _tmpBitDepth = _stmt.getLong(_columnIndexOfBitDepth).toInt()
          val _tmpChannels: Int
          _tmpChannels = _stmt.getLong(_columnIndexOfChannels).toInt()
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDateAddedEpochMs: Long
          _tmpDateAddedEpochMs = _stmt.getLong(_columnIndexOfDateAddedEpochMs)
          _result =
              AudioTrackEntity(_tmpId,_tmpFilePath,_tmpTitle,_tmpArtist,_tmpDurationMs,_tmpFormat,_tmpSampleRateHz,_tmpBitDepth,_tmpChannels,_tmpFileSizeBytes,_tmpDateAddedEpochMs)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllEntities(): List<AudioTrackEntity> {
    val _sql: String = "SELECT * FROM audio_tracks"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFilePath: Int = getColumnIndexOrThrow(_stmt, "filePath")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfDurationMs: Int = getColumnIndexOrThrow(_stmt, "durationMs")
        val _columnIndexOfFormat: Int = getColumnIndexOrThrow(_stmt, "format")
        val _columnIndexOfSampleRateHz: Int = getColumnIndexOrThrow(_stmt, "sampleRateHz")
        val _columnIndexOfBitDepth: Int = getColumnIndexOrThrow(_stmt, "bitDepth")
        val _columnIndexOfChannels: Int = getColumnIndexOrThrow(_stmt, "channels")
        val _columnIndexOfFileSizeBytes: Int = getColumnIndexOrThrow(_stmt, "fileSizeBytes")
        val _columnIndexOfDateAddedEpochMs: Int = getColumnIndexOrThrow(_stmt, "dateAddedEpochMs")
        val _result: MutableList<AudioTrackEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AudioTrackEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpFilePath: String
          _tmpFilePath = _stmt.getText(_columnIndexOfFilePath)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String?
          if (_stmt.isNull(_columnIndexOfArtist)) {
            _tmpArtist = null
          } else {
            _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          }
          val _tmpDurationMs: Long
          _tmpDurationMs = _stmt.getLong(_columnIndexOfDurationMs)
          val _tmpFormat: String
          _tmpFormat = _stmt.getText(_columnIndexOfFormat)
          val _tmpSampleRateHz: Int
          _tmpSampleRateHz = _stmt.getLong(_columnIndexOfSampleRateHz).toInt()
          val _tmpBitDepth: Int
          _tmpBitDepth = _stmt.getLong(_columnIndexOfBitDepth).toInt()
          val _tmpChannels: Int
          _tmpChannels = _stmt.getLong(_columnIndexOfChannels).toInt()
          val _tmpFileSizeBytes: Long
          _tmpFileSizeBytes = _stmt.getLong(_columnIndexOfFileSizeBytes)
          val _tmpDateAddedEpochMs: Long
          _tmpDateAddedEpochMs = _stmt.getLong(_columnIndexOfDateAddedEpochMs)
          _item =
              AudioTrackEntity(_tmpId,_tmpFilePath,_tmpTitle,_tmpArtist,_tmpDurationMs,_tmpFormat,_tmpSampleRateHz,_tmpBitDepth,_tmpChannels,_tmpFileSizeBytes,_tmpDateAddedEpochMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPaths(): List<String> {
    val _sql: String = "SELECT filePath FROM audio_tracks"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun pruneMissing(existingPaths: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM audio_tracks WHERE filePath NOT IN (")
    val _inputSize: Int = existingPaths.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in existingPaths) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
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
