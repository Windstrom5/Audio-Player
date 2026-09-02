package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesis.bitperfectusb.data.local.db.entity.AudioTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {

    @Query("SELECT * FROM audio_tracks ORDER BY title ASC")
    fun observeAll(): Flow<List<AudioTrackEntity>>

    @Query("SELECT * FROM audio_tracks WHERE id = :id")
    suspend fun getById(id: Long): AudioTrackEntity?

    @Query("SELECT * FROM audio_tracks")
    suspend fun getAllEntities(): List<AudioTrackEntity>

    @Query("SELECT filePath FROM audio_tracks")
    suspend fun getAllPaths(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<AudioTrackEntity>)

    @Query("DELETE FROM audio_tracks WHERE filePath NOT IN (:existingPaths)")
    suspend fun pruneMissing(existingPaths: List<String>)
}
