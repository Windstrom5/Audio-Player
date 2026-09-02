package com.thesis.bitperfectusb.benchmark

import android.app.ActivityManager
import android.content.Context
import android.os.Process

/**
 * Queries this process's real memory footprint (Section 3.7, MemoryMonitor).
 * Uses ActivityManager's per-process PSS — the same figure Android's own
 * Settings > Apps memory screen reports — converted from KB to MB.
 */
class MemoryMonitor(context: Context) {

    private val activityManager =
        context.applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun sampleMb(): Double {
        val pid = Process.myPid()
        val infos = activityManager.getProcessMemoryInfo(intArrayOf(pid))
        val totalPssKb = infos.firstOrNull()?.totalPss ?: 0
        return totalPssKb / 1024.0
    }
}
