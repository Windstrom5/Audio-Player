package com.thesis.bitperfectusb.usb

import kotlin.math.max
import kotlin.math.min

/**
 * Closed-loop Proportional-Integral (PI) feedback controller for UAC1 / UAC2
 * asynchronous endpoints.
 *
 * Async USB DACs send feedback values back to the host indicating their actual
 * sample consumption rate. UAC1 uses a 3-byte 10.14 fixed-point format (frames/ms);
 * UAC2 uses a 4-byte 16.16 fixed-point format (frames/interval).
 *
 * This controller calculates the rate error:
 *   e[k] = measuredRate - nominalRate
 * and computes a smooth adjustment trim offset for [IsochronousPacketScheduler]
 * so that packet sizes dynamically adapt to hardware clock drift without dropping
 * or corrupting audio samples.
 */
class UsbAsyncFeedbackPiController(
    private val sampleRateHz: Int,
    private val intervalsPerSecond: Int,
    private val isUac2: Boolean = true,
    private val kp: Float = 0.05f,
    private val ki: Float = 0.005f
) {
    private val nominalRatePerInterval: Double = sampleRateHz.toDouble() / intervalsPerSecond
    private var integralError: Double = 0.0
    
    @Volatile
    var currentFrameOffset: Int = 0
        private set

    @Volatile
    var lastMeasuredRateFramesPerInterval: Double = nominalRatePerInterval
        private set

    /**
     * Processes a raw integer feedback reading from UAC endpoint.
     * Decodes 10.14 (UAC1) or 16.16 (UAC2) fixed-point values into frames per interval.
     */
    fun processFeedbackRaw(rawFeedback: Int) {
        if (rawFeedback <= 0) return

        val measuredRateFramesPerInterval = if (isUac2) {
            // UAC2: 16.16 format -> value / 65536.0 gives frames per microframe (125us) or frame (1ms)
            rawFeedback.toDouble() / 65536.0
        } else {
            // UAC1: 10.14 format -> value / 16384.0 gives frames per ms
            val framesPerMs = (rawFeedback and 0xFFFFFF).toDouble() / 16384.0
            if (intervalsPerSecond == 8000) framesPerMs / 8.0 else framesPerMs
        }

        // Validate reasonable bounds (within 10% of nominal)
        if (measuredRateFramesPerInterval < nominalRatePerInterval * 0.90 ||
            measuredRateFramesPerInterval > nominalRatePerInterval * 1.10) {
            return
        }

        lastMeasuredRateFramesPerInterval = measuredRateFramesPerInterval

        val error = measuredRateFramesPerInterval - nominalRatePerInterval
        
        // Anti-windup clamping for integral accumulator
        integralError = max(-5.0, min(5.0, integralError + error))

        val outputAdjustment = (kp * error) + (ki * integralError)

        // Quantize adjustment trim into integer frame offset for scheduler (-2 to +2 frames per packet)
        currentFrameOffset = max(-2, min(2, kotlin.math.round(outputAdjustment).toInt()))
    }

    fun reset() {
        integralError = 0.0
        currentFrameOffset = 0
        lastMeasuredRateFramesPerInterval = nominalRatePerInterval
    }
}
