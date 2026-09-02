package com.thesis.bitperfectusb.domain.engine

import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.PcmFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackIntegrityEngineTest {

    private val engine = PlaybackIntegrityEngine()

    private fun sampleDac(
        sampleRates: List<Int> = listOf(44_100, 48_000, 96_000, 192_000),
        bitDepths: List<Int> = listOf(16, 24),
        maxChannels: Int = 2
    ) = DacProfile(
        id = 1L,
        vendorId = 0x08BB,
        productId = 0x2704,
        productName = "Test DAC",
        manufacturerName = "Test Vendor",
        isUac2 = true,
        supportedSampleRates = sampleRates,
        supportedBitDepths = bitDepths,
        maxChannels = maxChannels,
        maxPacketSizeBytes = 512,
        hasAsyncFeedbackEndpoint = true,
        dateProfiledEpochMs = 0L
    )

    @Test
    fun `usb-direct path scores 100 when source exactly matches dac capability`() {
        val source = PcmFormat(44_100, 16, 2)
        val result = engine.evaluate(source, sampleDac(), EngineType.CUSTOM_USB_DIRECT)
        assertEquals(100, result.score)
        assertTrue(result.isBitPerfect)
    }

    @Test
    fun `usb-direct path penalizes an unsupported sample rate`() {
        val source = PcmFormat(352_800, 16, 2) // not in the DAC's supported list
        val result = engine.evaluate(source, sampleDac(), EngineType.CUSTOM_USB_DIRECT)
        assertEquals(50, result.sampleRatePenalty)
        assertEquals(50, result.score)
    }

    @Test
    fun `usb-direct path penalizes an unsupported bit depth`() {
        val source = PcmFormat(44_100, 32, 2)
        val result = engine.evaluate(source, sampleDac(), EngineType.CUSTOM_USB_DIRECT)
        assertEquals(20, result.bitDepthPenalty)
        assertEquals(80, result.score)
    }

    @Test
    fun `usb-direct path with no dac scores zero`() {
        val source = PcmFormat(44_100, 16, 2)
        val result = engine.evaluate(source, null, EngineType.CUSTOM_USB_DIRECT)
        assertEquals(0, result.score)
    }

    @Test
    fun `audiotrack path always incurs the bit-depth penalty for the mixer reformat`() {
        val source = PcmFormat(48_000, 16, 2) // sample rate already matches AudioFlinger's native rate
        val result = engine.evaluate(source, sampleDac(), EngineType.ANDROID_AUDIOTRACK)
        assertEquals(0, result.sampleRatePenalty)
        assertEquals(20, result.bitDepthPenalty)
        assertEquals(80, result.score)
    }

    @Test
    fun `audiotrack path penalizes resampling when source rate differs from 48kHz`() {
        val source = PcmFormat(44_100, 16, 2)
        val result = engine.evaluate(source, sampleDac(), EngineType.ANDROID_AUDIOTRACK)
        assertEquals(50, result.sampleRatePenalty)
        assertEquals(20, result.bitDepthPenalty)
        assertEquals(30, result.score)
    }

    @Test
    fun `usb-direct path with per-altsetting data requires one option to match all three fields at once`() {
        // Two altsettings that each individually "cover" the requested format across
        // different fields, but no single one supports all three together.
        val dacWithSplitOptions = sampleDac().copy(
            streamingOptions = listOf(
                com.thesis.bitperfectusb.domain.model.UsbStreamingOption(
                    interfaceNumber = 1, alternateSetting = 1, bitDepth = 16, channels = 2,
                    supportedSampleRates = listOf(44_100, 48_000), endpointAddress = 0x03,
                    maxPacketSizeBytes = 192, isUac2 = true, feedbackEndpointAddress = null,
                    clockSourceId = 9, acInterfaceNumber = 0
                ),
                com.thesis.bitperfectusb.domain.model.UsbStreamingOption(
                    interfaceNumber = 1, alternateSetting = 2, bitDepth = 24, channels = 2,
                    supportedSampleRates = listOf(96_000, 192_000), endpointAddress = 0x03,
                    maxPacketSizeBytes = 768, isUac2 = true, feedbackEndpointAddress = null,
                    clockSourceId = 9, acInterfaceNumber = 0
                )
            )
        )
        // 44.1kHz/24-bit isn't offered by EITHER single altsetting together, even though
        // 44.1kHz appears on one and 24-bit appears on the other.
        val result = engine.evaluate(PcmFormat(44_100, 24, 2), dacWithSplitOptions, EngineType.CUSTOM_USB_DIRECT)
        assertEquals(50, result.score)
    }

    @Test
    fun `usb-direct path with per-altsetting data scores 100 when one altsetting matches exactly`() {
        val dacWithOptions = sampleDac().copy(
            streamingOptions = listOf(
                com.thesis.bitperfectusb.domain.model.UsbStreamingOption(
                    interfaceNumber = 1, alternateSetting = 1, bitDepth = 24, channels = 2,
                    supportedSampleRates = listOf(44_100, 48_000, 96_000, 192_000), endpointAddress = 0x03,
                    maxPacketSizeBytes = 768, isUac2 = true, feedbackEndpointAddress = null,
                    clockSourceId = 9, acInterfaceNumber = 0
                )
            )
        )
        val result = engine.evaluate(PcmFormat(96_000, 24, 2), dacWithOptions, EngineType.CUSTOM_USB_DIRECT)
        assertEquals(100, result.score)
        assertTrue(result.isBitPerfect)
    }
}
