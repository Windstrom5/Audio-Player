package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thesis.bitperfectusb.data.local.db.entity.BenchmarkSampleEntity
import com.thesis.bitperfectusb.data.local.db.entity.PlaybackSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BenchmarkDao {

    @Insert
    suspend fun insertSession(session: PlaybackSessionEntity): Long

    @Update
    suspend fun updateSession(session: PlaybackSessionEntity)

    @Query("SELECT * FROM playback_sessions WHERE id = :id")
    suspend fun getSession(id: Long): PlaybackSessionEntity?

    @Query("SELECT * FROM playback_sessions ORDER BY startEpochMs DESC")
    fun observeSessions(): Flow<List<PlaybackSessionEntity>>

    @Query("SELECT * FROM playback_sessions ORDER BY startEpochMs DESC")
    suspend fun getAllSessions(): List<PlaybackSessionEntity>

    @Insert
    suspend fun insertSample(sample: BenchmarkSampleEntity)

    @Query("SELECT * FROM benchmark_samples WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getSamplesForSession(sessionId: Long): List<BenchmarkSampleEntity>
}
