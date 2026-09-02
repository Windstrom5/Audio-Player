package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "watched_folders",
    indices = [Index(value = ["uriString"], unique = true)]
)
data class WatchedFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val uriString: String,
    val displayName: String,
    val dateAddedEpochMs: Long
)
