package com.thesis.bitperfectusb.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.thesis.bitperfectusb.data.local.db.entity.DacProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DacProfileDao {

    @Query("SELECT * FROM dac_profiles ORDER BY dateProfiledEpochMs DESC")
    fun observeAll(): Flow<List<DacProfileEntity>>

    @Query("SELECT * FROM dac_profiles WHERE vendorId = :vendorId AND productId = :productId LIMIT 1")
    suspend fun findByUsbIds(vendorId: Int, productId: Int): DacProfileEntity?

    @Query("SELECT * FROM dac_profiles WHERE id = :id")
    suspend fun getById(id: Long): DacProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: DacProfileEntity): Long
}
