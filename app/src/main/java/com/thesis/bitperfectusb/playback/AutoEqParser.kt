package com.thesis.bitperfectusb.playback

/**
 * Parser for AutoEQ headphone export files (EqualizerAPO / Parametric EQ text formats).
 * Converts lines like "Filter 1: ON PK Fc 105 Hz Gain 4.5 dB Q 1.41" into [PeqBand] objects.
 */
object AutoEqParser {

    fun parseAutoEqText(text: String): List<PeqBand> {
        val bands = mutableListOf<PeqBand>()
        val lines = text.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (!trimmed.startsWith("Filter", ignoreCase = true)) continue

            try {
                val isEnabled = !trimmed.contains("OFF", ignoreCase = true)
                val type = when {
                    trimmed.contains("LSC", ignoreCase = true) || trimmed.contains("Low Shelf", ignoreCase = true) -> FilterType.LOW_SHELF
                    trimmed.contains("HSC", ignoreCase = true) || trimmed.contains("High Shelf", ignoreCase = true) -> FilterType.HIGH_SHELF
                    trimmed.contains("LP", ignoreCase = true) || trimmed.contains("Low Pass", ignoreCase = true) -> FilterType.LOW_PASS
                    trimmed.contains("HP", ignoreCase = true) || trimmed.contains("High Pass", ignoreCase = true) -> FilterType.HIGH_PASS
                    trimmed.contains("NO", ignoreCase = true) || trimmed.contains("Notch", ignoreCase = true) -> FilterType.NOTCH
                    else -> FilterType.PEAK
                }

                val fc = extractValueAfterToken(trimmed, "Fc", "Hz") ?: 1000f
                val gain = extractValueAfterToken(trimmed, "Gain", "dB") ?: 0f
                val q = extractValueAfterToken(trimmed, "Q", null) ?: 1.414f

                bands.add(PeqBand(enabled = isEnabled, type = type, fcHz = fc, gainDb = gain, q = q))
                if (bands.size >= 5) break
            } catch (_: Exception) {}
        }

        while (bands.size < 5) {
            bands.add(PeqBand(enabled = false))
        }

        return bands
    }

    private fun extractValueAfterToken(line: String, token: String, unit: String?): Float? {
        val idx = line.indexOf(token, ignoreCase = true)
        if (idx == -1) return null
        val sub = line.substring(idx + token.length).trim()
        val tokens = sub.split("\\s+".toRegex())
        if (tokens.isEmpty()) return null
        val numStr = tokens[0].replace(",", ".")
        return numStr.toFloatOrNull()
    }
}
