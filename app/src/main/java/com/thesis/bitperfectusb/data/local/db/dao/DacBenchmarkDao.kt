package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesis.bitperfectusb.data.local.db.entity.DacBenchmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DacBenchmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DacBenchmarkEntity>)

    @Query("SELECT * FROM dac_benchmarks WHERE dacLabel = :dacLabel ORDER BY timestampEpochMs DESC")
    fun getHistoryForDac(dacLabel: String): Flow<List<DacBenchmarkEntity>>

    @Query("SELECT * FROM dac_benchmarks ORDER BY timestampEpochMs DESC")
    suspend fun getAllBenchmarks(): List<DacBenchmarkEntity>

    @Query("DELETE FROM dac_benchmarks")
    suspend fun deleteAll()
}
