package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thesis.bitperfectusb.data.local.db.entity.ExperimentRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {

    @Insert
    suspend fun insert(run: ExperimentRunEntity): Long

    @Query("SELECT * FROM experiment_runs WHERE experimentType = :type ORDER BY timestampEpochMs ASC")
    fun observeByType(type: String): Flow<List<ExperimentRunEntity>>

    @Query("SELECT * FROM experiment_runs WHERE experimentType = :type ORDER BY timestampEpochMs ASC")
    suspend fun getByType(type: String): List<ExperimentRunEntity>
}
