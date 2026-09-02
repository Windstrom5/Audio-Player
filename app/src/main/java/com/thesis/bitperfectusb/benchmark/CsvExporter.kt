package com.thesis.bitperfectusb.benchmark

import java.io.File
import java.io.FileWriter

/**
 * Serializes benchmark data to CSV (Section 3.7, CsvExporter). Used both for
 * timestamped live-monitor exports and for exporting the full session/experiment
 * history for offline analysis (e.g. in a spreadsheet or R/Python notebook).
 */
class CsvExporter {

    fun write(destinationPath: String, header: List<String>, rows: List<List<Any?>>): Int {
        val file = File(destinationPath)
        file.parentFile?.mkdirs()
        FileWriter(file, false).use { writer ->
            writer.append(header.joinToString(",") { escape(it) })
            writer.append("\n")
            for (row in rows) {
                writer.append(row.joinToString(",") { escape(it?.toString() ?: "") })
                writer.append("\n")
            }
        }
        return rows.size
    }

    private fun escape(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
}
