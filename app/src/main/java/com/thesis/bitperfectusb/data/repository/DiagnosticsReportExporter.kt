package com.thesis.bitperfectusb.data.repository

import com.thesis.bitperfectusb.data.local.db.entity.DacBenchmarkEntity
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.TrafficLogEntry

/**
 * Generates CSV and JSON diagnostic reports for exporting hardware DAC benchmarks,
 * capability profiles, and USB traffic logs.
 */
object DiagnosticsReportExporter {

    fun exportDacBenchmarkCsv(benchmarks: List<DacBenchmarkEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,DAC_Label,TimestampEpochMs,SampleRateHz,BitDepth,Stable,Dropouts,LatencyMs,BufferBytes,Note\n")
        for (b in benchmarks) {
            sb.append("${b.id},\"${b.dacLabel}\",${b.timestampEpochMs},${b.sampleRateHz},${b.bitDepth},${b.stable},${b.dropouts},${b.latencyMs},${b.bufferSizeBytes},\"${b.note}\"\n")
        }
        return sb.toString()
    }

    fun exportTrafficLogCsv(trafficLog: List<TrafficLogEntry>): String {
        val sb = StringBuilder()
        sb.append("Seq,SizeBytes,ElapsedMs,Status,JitterUs\n")
        for (e in trafficLog) {
            sb.append("${e.sequenceNumber},${e.sizeBytes},${e.elapsedMs},${e.status.name},${e.jitterUs}\n")
        }
        return sb.toString()
    }

    fun exportDacProfileJson(profile: DacProfile): String {
        val rates = profile.supportedSampleRates.joinToString(",")
        val depths = profile.supportedBitDepths.joinToString(",")
        return """
            {
              "vendorId": ${profile.vendorId},
              "productId": ${profile.productId},
              "productName": "${profile.productName}",
              "manufacturerName": "${profile.manufacturerName ?: ""}",
              "isUac2": ${profile.isUac2},
              "supportedSampleRates": [$rates],
              "supportedBitDepths": [$depths],
              "maxChannels": ${profile.maxChannels},
              "hasAsyncFeedbackEndpoint": ${profile.hasAsyncFeedbackEndpoint}
            }
        """.trimIndent()
    }
}
