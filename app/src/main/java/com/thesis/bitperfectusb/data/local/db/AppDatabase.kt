package com.thesis.bitperfectusb.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thesis.bitperfectusb.data.local.db.dao.AudioTrackDao
import com.thesis.bitperfectusb.data.local.db.dao.BenchmarkDao
import com.thesis.bitperfectusb.data.local.db.dao.DacProfileDao
import com.thesis.bitperfectusb.data.local.db.dao.ExperimentDao
import com.thesis.bitperfectusb.data.local.db.dao.WatchedFolderDao
import com.thesis.bitperfectusb.data.local.db.entity.AudioTrackEntity
import com.thesis.bitperfectusb.data.local.db.entity.BenchmarkSampleEntity
import com.thesis.bitperfectusb.data.local.db.entity.DacProfileEntity
import com.thesis.bitperfectusb.data.local.db.entity.ExperimentRunEntity
import com.thesis.bitperfectusb.data.local.db.entity.PlaybackSessionEntity
import com.thesis.bitperfectusb.data.local.db.entity.WatchedFolderEntity

import com.thesis.bitperfectusb.data.local.db.dao.DacBenchmarkDao
import com.thesis.bitperfectusb.data.local.db.entity.DacBenchmarkEntity

@Database(
    entities = [
        AudioTrackEntity::class,
        DacProfileEntity::class,
        PlaybackSessionEntity::class,
        BenchmarkSampleEntity::class,
        ExperimentRunEntity::class,
        WatchedFolderEntity::class,
        DacBenchmarkEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun dacProfileDao(): DacProfileDao
    abstract fun benchmarkDao(): BenchmarkDao
    abstract fun experimentDao(): ExperimentDao
    abstract fun watchedFolderDao(): WatchedFolderDao
    abstract fun dacBenchmarkDao(): DacBenchmarkDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bitperfect_usb.db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
    }
}
