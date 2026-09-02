package com.thesis.bitperfectusb.benchmark

import android.content.Context
import android.os.BatteryManager
import android.os.PowerManager

/**
 * Surfaces the internal validity threats discussed in Section 5.2.1 so the
 * Research dashboard can warn the researcher instead of silently reporting
 * numbers that may be confounded by device state (power saver, thermal/battery
 * throttling, background scheduling jitter).
 */
class ValidityThreatsDetector(private val context: Context) {

    fun isPowerSaveModeActive(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isPowerSaveMode
    }

    fun isBatteryLow(): Boolean {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return level in 0..20
    }

    fun checkThreats(latencyStdDevMs: Double): List<String> {
        val threats = mutableListOf<String>()
        if (isPowerSaveModeActive()) {
            threats += "Power saver mode is active — CPU frequency may be throttled, inflating latency."
        }
        if (isBatteryLow()) {
            threats += "Battery below 20% — the OS may be limiting CPU frequency."
        }
        if (latencyStdDevMs > LatencyMonitor.HIGH_JITTER_THRESHOLD_MS) {
            threats += "High Latency Jitter: latency SD (%.1fms) exceeds the %.0fms threshold — ".format(
                latencyStdDevMs, LatencyMonitor.HIGH_JITTER_THRESHOLD_MS
            ) + "background scheduling noise may be affecting this run."
        }
        return threats
    }

    companion object {
        /** Mandatory cool-down between experiment runs to avoid thermal bias (Section 5.2.1). */
        const val COOL_DOWN_MS = 2_000L
    }
}
