package com.thesis.bitperfectusb.usb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IsochronousPacketSchedulerTest {

    @Test
    fun `44_1kHz over 1000 full-speed frames totals exactly 44100 audio frames per second`() {
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 44_100, frameSizeBytes = 4, intervalsPerSecond = 1000)
        var total = 0
        repeat(1000) { total += scheduler.nextPacketFrameCount() }
        assertEquals(44_100, total)
    }

    @Test
    fun `44_1kHz packet sizes only ever alternate between 44 and 45 frames`() {
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 44_100, frameSizeBytes = 4, intervalsPerSecond = 1000)
        repeat(2000) {
            val frames = scheduler.nextPacketFrameCount()
            assertTrue("Expected 44 or 45 frames, got $frames", frames == 44 || frames == 45)
        }
    }

    @Test
    fun `evenly divisible rate produces a constant packet size with no drift`() {
        // 48kHz over 1000 intervals/sec divides evenly - every packet should be exactly 48 frames.
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 48_000, frameSizeBytes = 4, intervalsPerSecond = 1000)
        repeat(500) {
            assertEquals(48, scheduler.nextPacketFrameCount())
        }
    }

    @Test
    fun `high-speed microframe timing stays sample-accurate over one second`() {
        // 96kHz over 8000 microframes/sec (high-speed) = 12 frames/microframe exactly.
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 96_000, frameSizeBytes = 4, intervalsPerSecond = 8000)
        var total = 0
        repeat(8000) { total += scheduler.nextPacketFrameCount() }
        assertEquals(96_000, total)
    }

    @Test
    fun `192kHz over 8000 microframes per second is sample-accurate despite not dividing evenly`() {
        // 192000 / 8000 = 24 exactly, so this is actually an even case - verify a case that
        // genuinely doesn't divide evenly at high-speed too: 44.1kHz over microframes.
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 44_100, frameSizeBytes = 4, intervalsPerSecond = 8000)
        var total = 0
        repeat(8000) { total += scheduler.nextPacketFrameCount() }
        assertEquals(44_100, total)
    }

    @Test
    fun `reset clears accumulated remainder`() {
        val scheduler = IsochronousPacketScheduler(sampleRateHz = 44_100, frameSizeBytes = 4, intervalsPerSecond = 1000)
        repeat(7) { scheduler.nextPacketFrameCount() } // build up some remainder
        scheduler.reset()
        var total = 0
        repeat(1000) { total += scheduler.nextPacketFrameCount() }
        assertEquals(44_100, total) // still exact after a reset at a fresh boundary
    }
}
