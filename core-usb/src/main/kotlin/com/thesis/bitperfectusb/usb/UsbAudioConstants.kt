package com.thesis.bitperfectusb.usb

/**
 * Constants from the USB Audio Class 1.0 / 2.0 specifications (Chapter 2.2).
 * Android's UsbInterface/UsbEndpoint object model only exposes *standard* USB
 * descriptors — class-specific descriptors (CS_INTERFACE / CS_ENDPOINT) must be
 * parsed by hand from the raw descriptor block, which is exactly what
 * UsbDescriptorParser + DacCapabilityAnalyzer do.
 */
object UsbAudioConstants {

    // Class-specific descriptor types (USB Audio Class spec, Table A-4)
    const val CS_INTERFACE = 0x24
    const val CS_ENDPOINT = 0x25
    const val STANDARD_INTERFACE_DESCRIPTOR = 0x04
    const val STANDARD_ENDPOINT_DESCRIPTOR = 0x05

    // bInterfaceSubClass values for the Audio class
    const val SUBCLASS_AUDIOCONTROL = 0x01
    const val SUBCLASS_AUDIOSTREAMING = 0x02

    // bInterfaceProtocol values distinguishing UAC1 from UAC2
    const val PROTOCOL_UAC1 = 0x00
    const val PROTOCOL_UAC2 = 0x20

    // Audio Control interface descriptor subtypes (Table A-5 / UAC2 Table A-8)
    const val AC_HEADER = 0x01
    const val AC_INPUT_TERMINAL = 0x02
    const val AC_OUTPUT_TERMINAL = 0x03
    const val AC_FEATURE_UNIT = 0x06
    const val AC_CLOCK_SOURCE = 0x0A // UAC2 only
    const val AC_CLOCK_SELECTOR = 0x0B // UAC2 only

    // Audio Streaming interface descriptor subtypes (Table A-6 / UAC2 Table A-9)
    const val AS_GENERAL = 0x01
    const val AS_FORMAT_TYPE = 0x02

    const val FORMAT_TYPE_I = 0x01

    // UAC1 Endpoint Control Selectors (Table A-19, on the isochronous data endpoint itself)
    const val EP_SAMPLING_FREQ_CONTROL = 0x01

    // UAC2 Clock Source Control Selectors (Table A-17.1)
    const val CS_SAM_FREQ_CONTROL = 0x01

    // UAC1/UAC2 Feature Unit Control Selectors (Table A-11)
    const val FU_MUTE_CONTROL = 0x01
    const val FU_VOLUME_CONTROL = 0x02

    // Control request codes
    const val REQUEST_CUR = 0x01 // GET_CUR / SET_CUR
    const val REQUEST_RANGE = 0x02 // GET_RANGE (UAC2)
    const val REQUEST_MIN = 0x02 // GET_MIN (UAC1)
    const val REQUEST_MAX = 0x03 // GET_MAX (UAC1)
    const val REQUEST_RES = 0x04 // GET_RES (UAC1)

    // USB control transfer request-type bytes for class-specific requests
    const val REQTYPE_CLASS_INTERFACE_IN = 0xA1 // USB_DIR_IN | USB_TYPE_CLASS | USB_RECIP_INTERFACE
    const val REQTYPE_CLASS_INTERFACE_OUT = 0x21 // USB_DIR_OUT | USB_TYPE_CLASS | USB_RECIP_INTERFACE
    const val REQTYPE_CLASS_ENDPOINT_OUT = 0x22 // USB_DIR_OUT | USB_TYPE_CLASS | USB_RECIP_ENDPOINT (UAC1 rate-set target)

    // Isochronous endpoint synchronization type (bmAttributes bits 3-2)
    const val SYNC_TYPE_NONE = 0x00
    const val SYNC_TYPE_ASYNC = 0x01
    const val SYNC_TYPE_ADAPTIVE = 0x02
    const val SYNC_TYPE_SYNCHRONOUS = 0x03

    // USB device/interface class code
    const val USB_CLASS_AUDIO = 0x01

    // bmAttributes transfer-type mask on endpoint descriptors
    const val ENDPOINT_XFER_TYPE_MASK = 0x03
    const val ENDPOINT_XFER_ISOC = 0x01

    /** Common lossless-audio sample rates used to expand UAC1 continuous ranges into a usable list. */
    val CANONICAL_SAMPLE_RATES = listOf(44_100, 48_000, 88_200, 96_000, 176_400, 192_000, 352_800, 384_000)
}
