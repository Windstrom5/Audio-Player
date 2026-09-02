package com.thesis.bitperfectusb.usb

/**
 * Groups the flat descriptor stream [UsbDescriptorParser] produces into a
 * Configuration > Interface > [descriptors within it] tree, with a human-readable
 * label for every descriptor this app knows how to identify — the "USB Descriptor
 * Explorer" feature (Device-Manager-style browsing of exactly what the DAC
 * reported, distinct from the folded/summarized view [DacCapabilityAnalyzer]
 * produces for the Capability Matrix).
 *
 * Class-specific descriptor subtypes (bDescriptorType 0x24/0x25) are only
 * meaningful in context: the same subtype number means something different on an
 * AudioControl interface than on an AudioStreaming one, and UAC1 vs UAC2 renumber
 * several of them entirely. This tracks the interface currently being walked
 * (bInterfaceSubClass, bInterfaceProtocol) to disambiguate correctly. Anything
 * genuinely outside the tables below — a vendor-specific or otherwise
 * unrecognized descriptor — is labeled honestly as unknown rather than guessed
 * at, the same policy the rest of this app's descriptor handling follows.
 */
object DescriptorTreeBuilder {

    data class DescriptorNode(
        val title: String,
        val subtitle: String,
        val rawSummary: String,
        val hexBytes: String,
        val children: List<DescriptorNode> = emptyList()
    )

    private const val TYPE_DEVICE = 0x01
    private const val TYPE_CONFIGURATION = 0x02
    private const val TYPE_STRING = 0x03
    private const val TYPE_INTERFACE = 0x04
    private const val TYPE_ENDPOINT = 0x05
    private const val TYPE_INTERFACE_ASSOCIATION = 0x0B
    private const val TYPE_CS_INTERFACE = 0x24
    private const val TYPE_CS_ENDPOINT = 0x25

    private const val SUBCLASS_AUDIOCONTROL = 0x01
    private const val SUBCLASS_AUDIOSTREAMING = 0x02

    private const val PROTOCOL_UAC1 = 0x00
    private const val PROTOCOL_UAC2 = 0x20

    fun build(rawDescriptors: List<UsbDescriptorParser.RawDescriptor>): DescriptorNode {
        val root = mutableListOf<DescriptorNode>()
        var currentInterfaceChildren: MutableList<DescriptorNode>? = null
        var currentInterfaceNode: MutableInterfaceNode? = null
        var currentSubclass = 0
        var currentProtocol = 0

        for (d in rawDescriptors) {
            when (d.type) {
                TYPE_CONFIGURATION -> {
                    // Configuration is the root itself — nothing to nest, just skip past
                    // it as a node (its own summary line is added at the end from the
                    // count of interfaces found).
                }
                TYPE_INTERFACE -> {
                    currentInterfaceNode?.let { root += it.finish() }
                    val ifaceNum = safeU8(d, 2)
                    val altSetting = safeU8(d, 3)
                    val numEndpoints = safeU8(d, 4)
                    val ifaceClass = safeU8(d, 5)
                    currentSubclass = safeU8(d, 6)
                    currentProtocol = safeU8(d, 7)
                    val children = mutableListOf<DescriptorNode>()
                    currentInterfaceChildren = children
                    currentInterfaceNode = MutableInterfaceNode(
                        title = "Interface $ifaceNum, Alt $altSetting",
                        subtitle = interfaceSubtitle(ifaceClass, currentSubclass, currentProtocol, numEndpoints),
                        rawSummary = descriptorSummary(d, "Standard Interface"),
                        hexBytes = toHex(d.bytes),
                        children = children
                    )
                }
                TYPE_INTERFACE_ASSOCIATION -> {
                    currentInterfaceNode?.let { root += it.finish() }
                    currentInterfaceNode = null
                    currentInterfaceChildren = null
                    root += DescriptorNode(
                        title = "Interface Association",
                        subtitle = "Groups related interfaces (e.g. AudioControl + its AudioStreaming interfaces)",
                        rawSummary = descriptorSummary(d, "Interface Association"),
                        hexBytes = toHex(d.bytes)
                    )
                }
                TYPE_ENDPOINT -> {
                    val addr = safeU8(d, 2)
                    val attrs = safeU8(d, 3)
                    val maxPacket = if (d.bytes.size > 5) (safeU8(d, 4) or (safeU8(d, 5) shl 8)) else 0
                    val dir = if ((addr and 0x80) != 0) "IN" else "OUT"
                    val transferType = when (attrs and 0x03) {
                        0 -> "Control"
                        1 -> "Isochronous"
                        2 -> "Bulk"
                        else -> "Interrupt"
                    }
                    val node = DescriptorNode(
                        title = "Endpoint 0x%02X".format(addr),
                        subtitle = "$dir · $transferType · max $maxPacket bytes/packet",
                        rawSummary = descriptorSummary(d, "Standard Endpoint"),
                        hexBytes = toHex(d.bytes)
                    )
                    if (currentInterfaceChildren != null) currentInterfaceChildren!!.add(node) else root += node
                }
                TYPE_CS_INTERFACE -> {
                    val node = labelClassSpecificInterface(d, currentSubclass, currentProtocol)
                    if (currentInterfaceChildren != null) currentInterfaceChildren!!.add(node) else root += node
                }
                TYPE_CS_ENDPOINT -> {
                    val node = DescriptorNode(
                        title = "Class-Specific Endpoint",
                        subtitle = classSpecificEndpointSubtitle(d),
                        rawSummary = descriptorSummary(d, "CS Endpoint"),
                        hexBytes = toHex(d.bytes)
                    )
                    if (currentInterfaceChildren != null) currentInterfaceChildren!!.add(node) else root += node
                }
                TYPE_DEVICE, TYPE_STRING -> {
                    // Not part of the configuration descriptor block this app reads
                    // (getRawDescriptors() returns the config block only) — present
                    // defensively in case a device includes one anyway, but not
                    // interesting enough to nest specially.
                    root += DescriptorNode(
                        title = if (d.type == TYPE_DEVICE) "Device Descriptor" else "String Descriptor",
                        subtitle = "",
                        rawSummary = descriptorSummary(d, "Standard"),
                        hexBytes = toHex(d.bytes)
                    )
                }
                else -> {
                    val node = DescriptorNode(
                        title = "Unknown descriptor (type 0x%02X)".format(d.type),
                        subtitle = "Not a type this explorer recognizes — shown as-is rather than guessed at",
                        rawSummary = descriptorSummary(d, "Unrecognized"),
                        hexBytes = toHex(d.bytes)
                    )
                    if (currentInterfaceChildren != null) currentInterfaceChildren!!.add(node) else root += node
                }
            }
        }
        currentInterfaceNode?.let { root += it.finish() }

        return DescriptorNode(
            title = "Configuration",
            subtitle = "${root.count { it.title.startsWith("Interface ") }} interface altsetting(s) found",
            rawSummary = "${rawDescriptors.size} total descriptors",
            hexBytes = "",
            children = root
        )
    }

    /** Interface node under construction — children list is mutated in place while
     *  walking, then frozen into an immutable [DescriptorNode] via [finish]. */
    private class MutableInterfaceNode(
        val title: String,
        val subtitle: String,
        val rawSummary: String,
        val hexBytes: String,
        val children: MutableList<DescriptorNode>
    ) {
        fun finish() = DescriptorNode(title, subtitle, rawSummary, hexBytes, children.toList())
    }

    private fun interfaceSubtitle(ifaceClass: Int, subclass: Int, protocol: Int, numEndpoints: Int): String {
        val kind = when (subclass) {
            SUBCLASS_AUDIOCONTROL -> "AudioControl"
            SUBCLASS_AUDIOSTREAMING -> "AudioStreaming"
            3 -> "MIDIStreaming"
            else -> "Class 0x%02X / Subclass 0x%02X".format(ifaceClass, subclass)
        }
        val uac = when (protocol) {
            PROTOCOL_UAC1 -> "UAC1"
            PROTOCOL_UAC2 -> "UAC2"
            0x30 -> "UAC3"
            else -> "protocol 0x%02X".format(protocol)
        }
        return "$kind · $uac · $numEndpoints endpoint(s)"
    }

    private fun classSpecificEndpointSubtitle(d: UsbDescriptorParser.RawDescriptor): String =
        "Subtype 0x%02X — typically carries sampling-frequency control capability flags".format(d.subtype)

    private fun labelClassSpecificInterface(
        d: UsbDescriptorParser.RawDescriptor,
        subclass: Int,
        protocol: Int
    ): DescriptorNode {
        val isUac2 = protocol == PROTOCOL_UAC2
        val (name, detail) = when (subclass) {
            SUBCLASS_AUDIOCONTROL -> labelAudioControlSubtype(d, isUac2)
            SUBCLASS_AUDIOSTREAMING -> labelAudioStreamingSubtype(d, isUac2)
            else -> "Class-Specific Interface" to "Subtype 0x%02X (interface class not AudioControl/AudioStreaming)".format(d.subtype)
        }
        return DescriptorNode(
            title = name,
            subtitle = detail,
            rawSummary = descriptorSummary(d, "CS Interface"),
            hexBytes = toHex(d.bytes)
        )
    }

    /** AudioControl (bInterfaceSubClass=1) descriptor subtypes — numbering is shared
     *  between UAC1 and UAC2 for the entries both versions define; UAC2 adds several
     *  (Clock Source/Selector/Multiplier, Sample Rate Converter) that don't exist in
     *  UAC1 at all. */
    private fun labelAudioControlSubtype(d: UsbDescriptorParser.RawDescriptor, isUac2: Boolean): Pair<String, String> {
        return when (d.subtype) {
            0x01 -> "AC Header" to "Declares the AudioControl interface's UAC version and total class-specific descriptor length"
            0x02 -> "Input Terminal" to "Where audio enters this function (e.g. the USB streaming input)"
            0x03 -> "Output Terminal" to "Where audio leaves this function (e.g. the physical speaker/line-out jack)"
            0x04 -> "Mixer Unit" to "Combines multiple input channels into fewer output channels"
            0x05 -> "Selector Unit" to "Selects one of several inputs to pass through"
            0x06 -> "Feature Unit" to "Per-channel controls (volume, mute) available on this signal path"
            0x07 -> if (isUac2) "Effect Unit" to "UAC2 audio effect processing (rare on simple DACs)"
                else "Processing Unit" to "UAC1 signal processing block (e.g. stereo enhancement)"
            0x08 -> "Extension Unit" to "Vendor-defined processing block"
            0x0A -> if (isUac2) "Clock Source" to "UAC2 clock entity — sampling-frequency control targets this, not the streaming interface itself"
                else "Class-Specific Interface" to "Subtype 0x0A has no defined meaning in UAC1"
            0x0B -> "Clock Selector" to "UAC2 — chooses among multiple Clock Source entities"
            0x0C -> "Clock Multiplier" to "UAC2 — derives a clock rate from another clock entity"
            0x0D -> "Sample Rate Converter" to "UAC2 — an on-device SRC block"
            else -> "Class-Specific Interface" to "AudioControl subtype 0x%02X (not in this app's lookup table)".format(d.subtype)
        }
    }

    /** AudioStreaming (bInterfaceSubClass=2) descriptor subtypes — this app only ever
     *  parses Type I PCM format descriptors (see DacCapabilityAnalyzer), so subtype
     *  0x02 is always labeled "Format Type" without distinguishing Type I/II/III here;
     *  the capability matrix screen is where the actual parsed fields live. */
    private fun labelAudioStreamingSubtype(d: UsbDescriptorParser.RawDescriptor, isUac2: Boolean): Pair<String, String> {
        return when (d.subtype) {
            0x01 -> "AS General" to "Declares this altsetting's terminal link and (UAC2) supported format bitmap"
            0x02 -> "Format Type" to formatTypeDetail(d, isUac2)
            0x03 -> if (isUac2) "Encoder" to "UAC2 encoder descriptor (not applicable to PCM-only DACs)"
                else "Format-Specific" to "UAC1 format-specific descriptor"
            else -> "Class-Specific Interface" to "AudioStreaming subtype 0x%02X (not in this app's lookup table)".format(d.subtype)
        }
    }

    private fun formatTypeDetail(d: UsbDescriptorParser.RawDescriptor, isUac2: Boolean): String {
        return try {
            if (isUac2 && d.bytes.size > 5) {
                val subslot = d.bytes[4].toInt() and 0xFF
                val bitRes = d.bytes[5].toInt() and 0xFF
                "Subslot size $subslot byte(s), $bitRes-bit resolution — this is what PcmContainerPacker matches against"
            } else if (!isUac2 && d.bytes.size > 6) {
                val channels = d.bytes[4].toInt() and 0xFF
                val subframe = d.bytes[5].toInt() and 0xFF
                val bitRes = d.bytes[6].toInt() and 0xFF
                "$channels channel(s), subframe size $subframe byte(s), $bitRes-bit resolution"
            } else {
                "PCM Type I format (too short to read subslot/resolution fields)"
            }
        } catch (e: Exception) {
            "PCM Type I format"
        }
    }

    private fun descriptorSummary(d: UsbDescriptorParser.RawDescriptor, kindLabel: String): String {
        val subtypeSuffix = if (d.type == TYPE_CS_INTERFACE || d.type == TYPE_CS_ENDPOINT) {
            ", subtype 0x%02X".format(d.subtype)
        } else {
            ""
        }
        return "$kindLabel — type 0x%02X$subtypeSuffix, %d bytes".format(d.type, d.length)
    }

    private fun safeU8(d: UsbDescriptorParser.RawDescriptor, index: Int): Int =
        if (d.bytes.size > index) d.bytes[index].toInt() and 0xFF else 0

    private fun toHex(bytes: ByteArray): String =
        bytes.joinToString(" ") { "%02X".format(it) }
}
