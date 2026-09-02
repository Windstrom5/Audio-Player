package com.thesis.bitperfectusb.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dac_profiles",
    indices = [Index(value = ["vendorId", "productId"], unique = true)]
)
data class DacProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val vendorId: Int,
    val productId: Int,
    val productName: String,
    val manufacturerName: String?,
    val isUac2: Boolean,
    val supportedSampleRatesCsv: String,   // e.g. "44100,48000,96000,192000"
    val supportedBitDepthsCsv: String,     // e.g. "16,24,32"
    val maxChannels: Int,
    val maxPacketSizeBytes: Int,
    val hasAsyncFeedbackEndpoint: Boolean,
    val dateProfiledEpochMs: Long,
    /** Serialized List<UsbStreamingOption> — see DacProfileRepositoryImpl for the codec. */
    val streamingOptionsEncoded: String = ""
)
