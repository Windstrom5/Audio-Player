package com.thesis.bitperfectusb.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.UsbStreamingOption
import com.thesis.bitperfectusb.usb.UsbAudioConstants.AC_CLOCK_SOURCE
import com.thesis.bitperfectusb.usb.UsbAudioConstants.AS_FORMAT_TYPE
import com.thesis.bitperfectusb.usb.UsbAudioConstants.AS_GENERAL
import com.thesis.bitperfectusb.usb.UsbAudioConstants.CS_INTERFACE
import com.thesis.bitperfectusb.usb.UsbAudioConstants.CS_SAM_FREQ_CONTROL
import com.thesis.bitperfectusb.usb.UsbAudioConstants.PROTOCOL_UAC2
import com.thesis.bitperfectusb.usb.UsbAudioConstants.REQTYPE_CLASS_INTERFACE_IN
import com.thesis.bitperfectusb.usb.UsbAudioConstants.REQUEST_RANGE
import com.thesis.bitperfectusb.usb.UsbAudioConstants.STANDARD_ENDPOINT_DESCRIPTOR
import com.thesis.bitperfectusb.usb.UsbAudioConstants.STANDARD_INTERFACE_DESCRIPTOR
import com.thesis.bitperfectusb.usb.UsbAudioConstants.SUBCLASS_AUDIOCONTROL
import com.thesis.bitperfectusb.usb.UsbAudioConstants.SUBCLASS_AUDIOSTREAMING
import com.thesis.bitperfectusb.usb.UsbDescriptorParser.RawDescriptor

/**
 * Builds a [DacProfile] by parsing the DAC's raw USB Audio Class descriptors in
 * user-space (Section 3.3) — never relying on Android's audio policy manager,
 * which may impose defaults that don't reflect the hardware's true capability.
 *
 * The critical thing a "just find an isochronous endpoint" implementation gets
 * wrong: audio streaming interfaces expose **multiple alternate settings**, each
 * a distinct (format, endpoint) pair, sitting behind alternate-setting 0 (zero
 * bandwidth, no endpoints — the interface's idle state). This walks every
 * altsetting of every AudioStreaming interface into a [UsbStreamingOption], so
 * playback can later activate the *exact* one matching the source file instead
 * of guessing at whatever the first endpoint happened to be.
 *
 * UAC1 devices list their supported sample rates directly in each altsetting's
 * Format Type descriptor. UAC2 moved sample-rate discovery to a runtime control
 * request (GET_RANGE on the Clock Source entity's Sampling Frequency Control) —
 * this class issues that request once and attaches the result to every UAC2
 * altsetting that shares the clock (the common case). Some OEM firmware
 * responds to this request inconsistently; when it fails or times out, this
 * falls back to the canonical rate list rather than leaving the DAC unusable.
 */
class DacCapabilityAnalyzer(private val parser: UsbDescriptorParser = UsbDescriptorParser()) {

    fun analyze(device: UsbDevice, connection: UsbDeviceConnection): DacProfile {
        val raw = connection.rawDescriptors
            ?: error("Unable to read raw USB descriptors — device may have been detached.")
        val descriptors = parser.parseAll(raw)

        var acInterfaceNumber = -1
        /** All UAC2 Clock Source entity IDs found in the AudioControl interface.
         *  Many real DACs (iFi, xDuoo, Topping) have two separate clock sources —
         *  one for the 44.1kHz family (44100/88200/176400) and one for the 48kHz
         *  family (48000/96000/192000). The previous code kept only the last one
         *  seen, silently losing whichever clock family happened to be described
         *  first — which is why some DACs appeared to support fewer rates than
         *  their specs and USB descriptors advertise. */
        val allClockSourceIds = mutableListOf<Int>()
        var isUac2 = false

        val finishedOptions = mutableListOf<UsbStreamingOption>()
        var current: AltBuilder? = null

        fun finishCurrent() {
            current?.build()?.let { finishedOptions += it }
            current = null
        }

        for (d in descriptors) {
            when (d.type) {
                STANDARD_INTERFACE_DESCRIPTOR -> {
                    finishCurrent()
                    // bInterfaceNumber(2) bAlternateSetting(3) bNumEndpoints(4) bInterfaceClass(5) bInterfaceSubClass(6) bInterfaceProtocol(7)
                    val ifaceNum = parser.u8(d, 2)
                    val altSetting = parser.u8(d, 3)
                    val subclass = parser.u8(d, 6)
                    val protocol = parser.u8(d, 7)

                    when (subclass) {
                        SUBCLASS_AUDIOCONTROL -> {
                            acInterfaceNumber = ifaceNum
                            if (protocol == PROTOCOL_UAC2) isUac2 = true
                        }
                        SUBCLASS_AUDIOSTREAMING -> {
                            current = AltBuilder(ifaceNum, altSetting, protocol == PROTOCOL_UAC2)
                        }
                    }
                }
                CS_INTERFACE -> when (d.subtype) {
                    // Clock Source descriptors live in the AudioControl interface's own
                    // block, which is always walked before any AudioStreaming altsetting.
                    // Collect ALL clock source IDs rather than overwriting with the last one —
                    // see allClockSourceIds doc comment above for why this matters.
                    AC_CLOCK_SOURCE -> allClockSourceIds += parser.u8(d, 3) // bClockID(3)
                    AS_GENERAL -> current?.applyGeneral(d)
                    AS_FORMAT_TYPE -> current?.applyFormatType(d)
                }
                STANDARD_ENDPOINT_DESCRIPTOR -> current?.applyEndpoint(d)
            }
        }
        finishCurrent()

        // UAC2: none of the altsettings carry a sample rate in their descriptors at
        // all — query every discovered Clock Source and merge the results, then attach
        // the union to every UAC2 option. Using all clock sources (not just the first/last)
        // is what surfaces the full supported rate set on DACs with separate 44.1k and
        // 48k clock families (common in professional/audiophile USB DACs).
        val resolvedOptions = if (isUac2 && allClockSourceIds.isNotEmpty() && acInterfaceNumber >= 0) {
            val firstClockId = allClockSourceIds.first()
            val mergedRates = allClockSourceIds
                .flatMap { clockId -> queryUac2ClockRange(connection, acInterfaceNumber, clockId) }
                .distinct()
                .sorted()
                .ifEmpty { UsbAudioConstants.CANONICAL_SAMPLE_RATES.take(4) }
            finishedOptions.map { option ->
                if (option.isUac2) {
                    // Attach the first clock source ID for SET_CUR targeting (the common case
                    // is that one clock handles all rates; multi-clock support here is only
                    // about *discovery* — SET_CUR still goes to the first/primary clock).
                    option.copy(supportedSampleRates = mergedRates, clockSourceId = firstClockId, acInterfaceNumber = acInterfaceNumber)
                } else option
            }
        } else {
            finishedOptions
        }

        val streamableOptions = resolvedOptions.filter { it.supportedSampleRates.isNotEmpty() }

        // Flattened union views, kept for the simple pass/fail checks elsewhere
        // (PlaybackIntegrityEngine, the DAC screen's summary card) that don't need
        // to know which specific altsetting a rate lives on.
        val allSampleRates = streamableOptions.flatMap { it.supportedSampleRates }.toSortedSet()
        val allBitDepths = streamableOptions.map { it.bitDepth }.toSortedSet()
        val maxChannels = streamableOptions.maxOfOrNull { it.channels } ?: 2
        val hasAsyncFeedback = streamableOptions.any { it.hasFeedbackEndpoint }
        val maxPacketSize = streamableOptions.maxOfOrNull { it.maxPacketSizeBytes } ?: 0

        return DacProfile(
            id = 0L,
            vendorId = device.vendorId,
            productId = device.productId,
            productName = device.productName ?: "USB Audio Device",
            manufacturerName = device.manufacturerName,
            isUac2 = isUac2,
            supportedSampleRates = if (allSampleRates.isEmpty()) listOf(44_100, 48_000, 96_000, 192_000) else allSampleRates.toList(),
            supportedBitDepths = if (allBitDepths.isEmpty()) listOf(16) else allBitDepths.toList(),
            maxChannels = maxChannels,
            maxPacketSizeBytes = maxPacketSize,
            hasAsyncFeedbackEndpoint = hasAsyncFeedback,
            dateProfiledEpochMs = System.currentTimeMillis(),
            streamingOptions = streamableOptions
        )
    }

    /**
     * UAC2 sample-rate discovery: GET_RANGE on the Clock Source's Sampling Frequency
     * Control (USB Audio 2.0 spec, Section 5.2.5.4 / Table A-17.1). Response layout:
     * wNumSubRanges (2 bytes) followed by that many (MIN:4, MAX:4, RES:4) byte triples,
     * each field a 32-bit unsigned integer in Hz.
     */
    private fun queryUac2ClockRange(
        connection: UsbDeviceConnection,
        acInterfaceNumber: Int,
        clockSourceId: Int
    ): List<Int> {
        val buffer = ByteArray(2 + 12 * 8) // room for up to 8 sub-ranges
        val wValue = CS_SAM_FREQ_CONTROL shl 8
        val wIndex = (clockSourceId shl 8) or acInterfaceNumber

        val bytesRead = try {
            connection.controlTransfer(
                REQTYPE_CLASS_INTERFACE_IN, REQUEST_RANGE, wValue, wIndex, buffer, buffer.size, 500
            )
        } catch (t: Throwable) {
            -1
        }
        if (bytesRead < 4) return emptyList()

        val numSubRanges = (buffer[0].toInt() and 0xFF) or ((buffer[1].toInt() and 0xFF) shl 8)
        val rates = mutableListOf<Int>()
        for (i in 0 until numSubRanges) {
            val base = 2 + i * 12
            if (base + 12 > bytesRead) break
            val min = parser.u32(buffer, base).toInt()
            val max = parser.u32(buffer, base + 4).toInt()
            if (min == max) {
                rates += min
            } else {
                UsbAudioConstants.CANONICAL_SAMPLE_RATES.filter { it in min..max }.forEach { rates += it }
            }
        }
        return rates
    }

    /** Accumulates descriptors belonging to one (interfaceNumber, alternateSetting) pair. */
    private inner class AltBuilder(
        private val interfaceNumber: Int,
        private val alternateSetting: Int,
        private val isUac2: Boolean
    ) {
        private var channels = 2
        private var bitResolution = 16
        private var containerBytes = 2
        private val discreteSampleRates = linkedSetOf<Int>()
        private var continuousLow: Int? = null
        private var continuousHigh: Int? = null
        private var primaryEndpointAddress: Int? = null
        private var maxPacketSizeBytes = 0
        private var feedbackEndpointAddress: Int? = null

        fun applyGeneral(d: RawDescriptor) {
            if (isUac2 && d.bytes.size > 10) {
                // UAC2 AS_GENERAL: bTerminalLink(3) bmControls(4) bFormatType(5) bmFormats(6..9) bNrChannels(10)
                channels = parser.u8(d, 10)
            }
            // UAC1 channel count comes from the Format Type descriptor instead (applyFormatType).
        }

        fun applyFormatType(d: RawDescriptor) {
            if (isUac2) {
                // UAC2 Type I: bFormatType(3) bSubslotSize(4) bBitResolution(5)
                // bSubslotSize is the actual per-sample byte count on the wire — it can
                // legitimately be wider than ceil(bBitResolution/8) (e.g. 24-bit resolution
                // packed into a 4-byte subslot), and the wire format is dictated by the
                // subslot size, not the resolution. Losing this was the root cause of
                // 24-bit playback failing on any DAC using that (common) configuration.
                if (d.bytes.size > 4) containerBytes = parser.u8(d, 4).coerceAtLeast(1)
                if (d.bytes.size > 5) bitResolution = parser.u8(d, 5)
                return
            }
            // UAC1 Type I: bFormatType(3) bNrChannels(4) bSubframeSize(5) bBitResolution(6) bSamFreqType(7) [tSamFreq...]
            if (d.bytes.size <= 7) return
            channels = parser.u8(d, 4)
            containerBytes = parser.u8(d, 5).coerceAtLeast(1)
            bitResolution = parser.u8(d, 6)
            val samFreqType = parser.u8(d, 7)
            if (samFreqType == 0) {
                if (d.bytes.size >= 14) {
                    continuousLow = parser.u24(d, 8)
                    continuousHigh = parser.u24(d, 11)
                }
            } else {
                for (i in 0 until samFreqType) {
                    val offset = 8 + i * 3
                    if (offset + 3 <= d.bytes.size) discreteSampleRates += parser.u24(d, offset)
                }
            }
        }

        fun applyEndpoint(d: RawDescriptor) {
            // bLength(0) bDescriptorType(1) bEndpointAddress(2) bmAttributes(3) wMaxPacketSize(4-5) bInterval(6)
            if (d.bytes.size <= 5) return
            val address = parser.u8(d, 2)
            val isIn = (address and 0x80) != 0
            val xferType = parser.u8(d, 3) and UsbAudioConstants.ENDPOINT_XFER_TYPE_MASK
            if (xferType != UsbAudioConstants.ENDPOINT_XFER_ISOC) return
            val packetSize = parser.u16(d, 4) and 0x07FF // low 11 bits carry the base packet size

            if (!isIn) {
                primaryEndpointAddress = address
                maxPacketSizeBytes = packetSize
            } else {
                feedbackEndpointAddress = address
            }
        }

        fun build(): UsbStreamingOption? {
            val endpoint = primaryEndpointAddress ?: return null // altsetting 0 (idle) or malformed — not streamable
            if (continuousLow != null && continuousHigh != null) {
                UsbAudioConstants.CANONICAL_SAMPLE_RATES
                    .filter { it in continuousLow!!..continuousHigh!! }
                    .forEach { discreteSampleRates += it }
            }
            return UsbStreamingOption(
                interfaceNumber = interfaceNumber,
                alternateSetting = alternateSetting,
                bitDepth = bitResolution,
                channels = channels,
                supportedSampleRates = discreteSampleRates.toList(), // UAC2 filled in by the caller after the clock query
                endpointAddress = endpoint,
                maxPacketSizeBytes = maxPacketSizeBytes,
                isUac2 = isUac2,
                feedbackEndpointAddress = feedbackEndpointAddress,
                clockSourceId = null, // attached by the caller once the shared clock is queried
                // A malformed descriptor could in principle report a container smaller
                // than the resolution needs (e.g. containerBytes=2 with bitResolution=24) —
                // never trust that below what the sample data actually requires.
                containerBytes = containerBytes.coerceAtLeast((bitResolution + 7) / 8)
            )
        }
    }
}
