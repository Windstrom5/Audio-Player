package com.thesis.bitperfectusb.`data`.local.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.thesis.bitperfectusb.`data`.local.db.entity.WatchedFolderEntity
import javax.`annotation`.processing.Generated
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
public class WatchedFolderDao_Impl(
  __db: RoomDatabase,
) : WatchedFolderDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfWatchedFolderEntity: EntityInsertAdapter<WatchedFolderEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfWatchedFolderEntity = object : EntityInsertAdapter<WatchedFolderEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `watched_folders` (`id`,`uriString`,`displayName`,`dateAddedEpochMs`) VALUES (nullif(?, 0),?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: WatchedFolderEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.uriString)
        statement.bindText(3, entity.displayName)
        statement.bindLong(4, entity.dateAddedEpochMs)
      }
    }
  }

  public override suspend fun insert(folder: WatchedFolderEntity): Long = performSuspending(__db,
      false, true) { _connection ->
    val _result: Long = __insertAdapterOfWatchedFolderEntity.insertAndReturnId(_connection, folder)
    _result
  }

  public override fun observeAll(): Flow<List<WatchedFolderEntity>> {
    val _sql: String = "SELECT * FROM watched_folders ORDER BY dateAddedEpochMs DESC"
    return createFlow(__db, false, arrayOf("watched_folders")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUriString: Int = getColumnIndexOrThrow(_stmt, "uriString")
        val _columnIndexOfDisplayName: Int = getColumnIndexOrThrow(_stmt, "displayName")
        val _columnIndexOfDateAddedEpochMs: Int = getColumnIndexOrThrow(_stmt, "dateAddedEpochMs")
        val _result: MutableList<WatchedFolderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WatchedFolderEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUriString: String
          _tmpUriString = _stmt.getText(_columnIndexOfUriString)
          val _tmpDisplayName: String
          _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          val _tmpDateAddedEpochMs: Long
          _tmpDateAddedEpochMs = _stmt.getLong(_columnIndexOfDateAddedEpochMs)
          _item = WatchedFolderEntity(_tmpId,_tmpUriString,_tmpDisplayName,_tmpDateAddedEpochMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAll(): List<WatchedFolderEntity> {
    val _sql: String = "SELECT * FROM watched_folders ORDER BY dateAddedEpochMs DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUriString: Int = getColumnIndexOrThrow(_stmt, "uriString")
        val _columnIndexOfDisplayName: Int = getColumnIndexOrThrow(_stmt, "displayName")
        val _columnIndexOfDateAddedEpochMs: Int = getColumnIndexOrThrow(_stmt, "dateAddedEpochMs")
        val _result: MutableList<WatchedFolderEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: WatchedFolderEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpUriString: String
          _tmpUriString = _stmt.getText(_columnIndexOfUriString)
          val _tmpDisplayName: String
          _tmpDisplayName = _stmt.getText(_columnIndexOfDisplayName)
          val _tmpDateAddedEpochMs: Long
          _tmpDateAddedEpochMs = _stmt.getLong(_columnIndexOfDateAddedEpochMs)
          _item = WatchedFolderEntity(_tmpId,_tmpUriString,_tmpDisplayName,_tmpDateAddedEpochMs)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteById(id: Long) {
    val _sql: String = "DELETE FROM watched_folders WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
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
