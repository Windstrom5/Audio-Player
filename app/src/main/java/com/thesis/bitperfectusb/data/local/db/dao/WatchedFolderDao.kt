package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesis.bitperfectusb.data.local.db.entity.WatchedFolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedFolderDao {

    @Query("SELECT * FROM watched_folders ORDER BY dateAddedEpochMs DESC")
    fun observeAll(): Flow<List<WatchedFolderEntity>>

    @Query("SELECT * FROM watched_folders ORDER BY dateAddedEpochMs DESC")
    suspend fun getAll(): List<WatchedFolderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(folder: WatchedFolderEntity): Long

    @Query("DELETE FROM watched_folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
