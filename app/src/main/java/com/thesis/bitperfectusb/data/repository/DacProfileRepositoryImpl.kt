package com.thesis.bitperfectusb.data.repository

import com.thesis.bitperfectusb.data.local.db.dao.DacProfileDao
import com.thesis.bitperfectusb.data.local.db.entity.DacProfileEntity
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.UsbStreamingOption
import com.thesis.bitperfectusb.domain.repository.DacProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DacProfileRepositoryImpl(private val dao: DacProfileDao) : DacProfileRepository {

    override fun observeProfiles(): Flow<List<DacProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveProfile(profile: DacProfile): Long = withContext(Dispatchers.IO) {
        dao.insert(profile.toEntity())
    }

    override suspend fun findByUsbIds(vendorId: Int, productId: Int): DacProfile? = withContext(Dispatchers.IO) {
        dao.findByUsbIds(vendorId, productId)?.toDomain()
    }

    override suspend fun getProfile(id: Long): DacProfile? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    private fun DacProfileEntity.toDomain() = DacProfile(
        id = id,
        vendorId = vendorId,
        productId = productId,
        productName = productName,
        manufacturerName = manufacturerName,
        isUac2 = isUac2,
        supportedSampleRates = supportedSampleRatesCsv.split(",").filter { it.isNotBlank() }.map { it.toInt() },
        supportedBitDepths = supportedBitDepthsCsv.split(",").filter { it.isNotBlank() }.map { it.toInt() },
        maxChannels = maxChannels,
        maxPacketSizeBytes = maxPacketSizeBytes,
        hasAsyncFeedbackEndpoint = hasAsyncFeedbackEndpoint,
        dateProfiledEpochMs = dateProfiledEpochMs,
        streamingOptions = decodeStreamingOptions(streamingOptionsEncoded)
    )

    private fun DacProfile.toEntity() = DacProfileEntity(
        id = id,
        vendorId = vendorId,
        productId = productId,
        productName = productName,
        manufacturerName = manufacturerName,
        isUac2 = isUac2,
        supportedSampleRatesCsv = supportedSampleRates.joinToString(","),
        supportedBitDepthsCsv = supportedBitDepths.joinToString(","),
        maxChannels = maxChannels,
        maxPacketSizeBytes = maxPacketSizeBytes,
        hasAsyncFeedbackEndpoint = hasAsyncFeedbackEndpoint,
        dateProfiledEpochMs = dateProfiledEpochMs,
        streamingOptionsEncoded = encodeStreamingOptions(streamingOptions)
    )

    // ---------------------------------------------------------------------
    // UsbStreamingOption codec — Room's type converters are overkill for one
    // column; a flat, self-delimited text format is simpler to read in a DB
    // browser too, which is handy when debugging descriptor-parsing issues.
    // Per option: "iface:altSetting:bitDepth:channels:rate1|rate2:endpointAddr:maxPacket:isUac2:feedbackAddr:clockId:acIface:containerBytes"
    // (-1 stands in for "absent" on the nullable integer fields.)
    //
    // containerBytes (field 11) was added after the initial release, to fix
    // 24-bit playback on DACs whose declared container is wider than their bit
    // resolution (see DacCapabilityAnalyzer / PcmContainerPacker). Rows written
    // before that fix only have 11 fields; decodeStreamingOptions accepts both
    // lengths rather than dropping every previously-analyzed DAC from history —
    // an old row just falls back to the pre-fix assumption (tight packing)
    // until the DAC is re-analyzed, which overwrites it via the unique
    // (vendorId, productId) index (see DacProfileDao.insert).
    // ---------------------------------------------------------------------

    private fun encodeStreamingOptions(options: List<UsbStreamingOption>): String =
        options.joinToString(";") { opt ->
            listOf(
                opt.interfaceNumber,
                opt.alternateSetting,
                opt.bitDepth,
                opt.channels,
                opt.supportedSampleRates.joinToString("|"),
                opt.endpointAddress,
                opt.maxPacketSizeBytes,
                if (opt.isUac2) 1 else 0,
                opt.feedbackEndpointAddress ?: -1,
                opt.clockSourceId ?: -1,
                opt.acInterfaceNumber ?: -1,
                opt.containerBytes
            ).joinToString(":")
        }

    private fun decodeStreamingOptions(encoded: String): List<UsbStreamingOption> {
        if (encoded.isBlank()) return emptyList()
        return encoded.split(";").mapNotNull { entry ->
            val fields = entry.split(":")
            if (fields.size != 11 && fields.size != 12) return@mapNotNull null
            try {
                val bitDepth = fields[2].toInt()
                UsbStreamingOption(
                    interfaceNumber = fields[0].toInt(),
                    alternateSetting = fields[1].toInt(),
                    bitDepth = bitDepth,
                    channels = fields[3].toInt(),
                    supportedSampleRates = fields[4].split("|").filter { it.isNotBlank() }.map { it.toInt() },
                    endpointAddress = fields[5].toInt(),
                    maxPacketSizeBytes = fields[6].toInt(),
                    isUac2 = fields[7] == "1",
                    feedbackEndpointAddress = fields[8].toInt().takeIf { it != -1 },
                    clockSourceId = fields[9].toInt().takeIf { it != -1 },
                    acInterfaceNumber = fields[10].toInt().takeIf { it != -1 },
                    containerBytes = if (fields.size == 12) fields[11].toInt() else bitDepth / 8
                )
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
