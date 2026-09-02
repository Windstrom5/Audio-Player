package com.thesis.bitperfectusb.playback.autoeq

/**
 * Headphone / IEM calibration profile from the AutoEQ project (Harman Target & IEF Neutral compensation).
 * Gains represent 10 standard ISO octave bands: 31Hz, 63Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz.
 */
data class AutoEqProfile(
    val brand: String,
    val model: String,
    val type: String, // "IEM", "Over-Ear", "Earbuds"
    val targetCurve: String, // "Harman 2019", "Harman 2018", "IEF Neutral"
    val gains: List<Float>, // 10 band gains in dB (-12.0f .. +12.0f)
    val aliases: List<String> = emptyList()
) {
    val displayName: String get() = "$brand $model"
}

object AutoEqRepository {

    private val profiles: List<AutoEqProfile> = listOf(
        // ══════════════════════════════════════════════════════════════════════════
        // ── TANCHJIM ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Tanchjim", "Bunny / Bunny DSP", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.4f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Tanchjim", "Ola II / Ola 2", "IEM", "IEF Neutral", listOf(1.5f, 1.0f, 0.4f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.0f, 0.4f)),
        AutoEqProfile("Tanchjim", "Ola / Ola Bass", "IEM", "IEF Neutral", listOf(1.8f, 1.2f, 0.5f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.2f, 0.5f)),
        AutoEqProfile("Tanchjim", "Tanya / Tanya DSP", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.1f, 0.5f, 1.2f, 1.5f, -1.8f, 0.5f)),
        AutoEqProfile("Tanchjim", "Zero / Zero DSP / Zero Ultima", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.2f, -0.4f, 0.0f, 0.4f, 1.0f, 1.2f, -1.5f, 0.6f)),
        AutoEqProfile("Tanchjim", "One / One DSP", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.4f, -0.2f, 0.0f, 0.5f, 1.2f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("Tanchjim", "4U", "IEM", "IEF Neutral", listOf(1.2f, 0.8f, 0.4f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.2f, 0.4f)),
        AutoEqProfile("Tanchjim", "Kara", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.2f, 0.2f, 0.4f, 0.8f, 1.0f, -1.2f, 0.2f)),
        AutoEqProfile("Tanchjim", "Oxygen / Oxygen 2023", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -2.0f, 0.8f)),
        AutoEqProfile("Tanchjim", "Origin", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("Tanchjim", "Hana / Hana 2021", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.4f, 0.0f, 0.5f, 1.0f, 1.4f, -2.2f, 0.8f)),
        AutoEqProfile("Tanchjim", "Darling", "IEM", "Harman 2019", listOf(0.5f, 0.2f, -0.2f, -0.5f, 0.2f, 0.5f, 1.2f, 1.5f, -2.5f, 1.0f)),
        AutoEqProfile("Tanchjim", "Prism", "IEM", "Harman 2019", listOf(0.6f, 0.3f, 0.0f, 0.0f, 0.2f, 0.4f, 0.8f, 1.0f, -1.8f, 0.6f)),
        AutoEqProfile("Tanchjim", "Space / Space Lite / Mino", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.4f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Tanchjim", "Echo / Asano Tanch", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.0f, -1.4f, 0.4f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── MOONDROP ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Moondrop", "Chu / Chu II / Chu II DSP", "IEM", "Harman 2019", listOf(1.0f, 0.5f, 0.0f, -0.5f, 0.2f, 0.8f, 1.5f, -1.8f, -3.2f, 1.0f)),
        AutoEqProfile("Moondrop", "Aria / Aria SE / Aria 2", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.2f, -0.5f, 0.0f, 0.5f, 1.2f, 1.8f, -2.0f, 0.8f)),
        AutoEqProfile("Moondrop", "Kato", "IEM", "Harman 2019", listOf(1.8f, 1.2f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.5f, -1.5f, 0.5f)),
        AutoEqProfile("Moondrop", "Starfield / Starfield 2", "IEM", "Harman 2019", listOf(1.6f, 1.1f, 0.4f, -0.3f, 0.1f, 0.4f, 1.0f, 1.6f, -1.8f, 0.6f)),
        AutoEqProfile("Moondrop", "Blessing 2", "IEM", "Harman 2019", listOf(3.5f, 2.8f, 1.5f, 0.2f, 0.0f, 0.0f, 0.5f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("Moondrop", "Blessing 2 Dusk", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.5f, 0.0f, 0.0f, 0.0f, 0.8f, 1.0f, -1.2f, 0.2f)),
        AutoEqProfile("Moondrop", "Blessing 3", "IEM", "Harman 2019", listOf(2.2f, 1.8f, 0.8f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -2.5f, 0.8f)),
        AutoEqProfile("Moondrop", "Variations", "IEM", "Harman 2019", listOf(-0.5f, -0.2f, 0.0f, 0.2f, 0.5f, 0.0f, 0.5f, 0.8f, -1.2f, 0.2f)),
        AutoEqProfile("Moondrop", "Stellaris", "IEM", "Harman 2019", listOf(2.5f, 1.8f, 0.8f, 0.0f, 0.0f, 0.5f, 1.0f, -2.5f, -5.0f, 1.2f)),
        AutoEqProfile("Moondrop", "S8 / Solis", "IEM", "IEF Neutral", listOf(1.0f, 0.8f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 1.0f, -1.8f, 0.5f)),
        AutoEqProfile("Moondrop", "Space Travel / Golden Ages", "IEM", "Harman 2019", listOf(2.0f, 1.5f, 0.8f, -0.2f, 0.0f, 0.5f, 1.2f, 1.5f, -2.0f, 0.5f)),
        AutoEqProfile("Moondrop", "May / Lan / Quarks", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.5f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Moondrop", "Kadenz / Dark Saber / Venus", "IEM", "IEF Neutral", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.0f, 0.2f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── TANGZU ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Tangzu", "Wan'er S.G", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.5f, 0.2f, 0.5f, 1.2f, 1.5f, -2.2f, 0.8f)),
        AutoEqProfile("Tangzu", "Wan'er Studio Edition", "IEM", "IEF Neutral", listOf(1.8f, 1.2f, 0.5f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("Tangzu", "Wan'er 2 / Jade Edition", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.3f, 0.1f, 0.4f, 1.0f, 1.4f, -1.8f, 0.6f)),
        AutoEqProfile("Tangzu", "Zetian Wu / Heyday", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.0f, 0.5f)),
        AutoEqProfile("Tangzu", "Fudu Verse 1", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.4f, -0.2f, 0.0f, 0.4f, 1.0f, 1.4f, -1.8f, 0.6f)),
        AutoEqProfile("Tangzu", "Princess Chang Le", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.4f, 0.1f, 0.5f, 1.2f, 1.5f, -2.0f, 0.7f)),
        AutoEqProfile("Tangzu", "Xuanwu Gate / Nezha", "IEM", "IEF Neutral", listOf(0.6f, 0.4f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.2f, 0.4f)),
        AutoEqProfile("Tangzu", "Shimin Li / YuXuanJi", "IEM", "Harman 2019", listOf(1.4f, 1.0f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -2.0f, 0.6f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── 7HZ ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("7Hz", "Salnotes Zero", "IEM", "Harman 2019", listOf(1.8f, 1.2f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("7Hz", "Zero 2", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("7Hz", "Timeless / Timeless AE", "IEM", "Harman 2019", listOf(0.5f, 0.2f, -0.2f, -0.5f, 0.2f, 0.5f, 1.0f, 1.5f, -2.8f, 1.0f)),
        AutoEqProfile("7Hz", "Dioko (Salnotes Dioko)", "IEM", "IEF Neutral", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -2.5f, 0.8f)),
        AutoEqProfile("7Hz", "Sonus", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("7Hz", "Legato", "IEM", "Harman 2019", listOf(-2.5f, -2.0f, -1.2f, 0.2f, 0.5f, 1.0f, 1.8f, 2.5f, -1.0f, 0.5f)),
        AutoEqProfile("7Hz", "Aurora / Five", "IEM", "Harman 2019", listOf(0.6f, 0.4f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, 1.0f, -1.5f, 0.4f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── TRUTHEAR ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Truthear", "Hola", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.4f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Truthear", "Gate", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.5f, -0.2f, 0.0f, 0.5f, 1.0f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("Truthear", "Crinacle Zero (Blue)", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.2f, 0.5f, 1.0f, 1.2f, -2.0f, 0.5f)),
        AutoEqProfile("Truthear", "Zero: RED", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),
        AutoEqProfile("Truthear", "HEXA", "IEM", "IEF Neutral", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.2f, 0.5f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("Truthear", "NOVA", "IEM", "Harman 2019", listOf(0.2f, 0.0f, 0.0f, 0.2f, 0.2f, 0.5f, 0.8f, 1.0f, -1.2f, 0.2f)),
        AutoEqProfile("Truthear", "Pure", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.0f, -1.4f, 0.4f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── KIWI EARS ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Kiwi Ears", "Cadenza", "IEM", "Harman 2019", listOf(1.0f, 0.5f, 0.2f, -0.2f, 0.0f, 0.5f, 1.2f, 1.8f, -2.0f, 0.8f)),
        AutoEqProfile("Kiwi Ears", "Orchestra Lite", "IEM", "IEF Neutral", listOf(1.2f, 0.8f, 0.5f, 0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Kiwi Ears", "Quintet", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.2f, 0.5f, 1.0f, 1.2f, -2.5f, 0.8f)),
        AutoEqProfile("Kiwi Ears", "Melody", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.5f, -2.0f, 0.6f)),
        AutoEqProfile("Kiwi Ears", "Quartet / Forteza", "IEM", "Harman 2019", listOf(-0.5f, -0.8f, -0.2f, 0.2f, 0.4f, 0.6f, 1.2f, 1.8f, -2.0f, 0.6f)),
        AutoEqProfile("Kiwi Ears", "Singolo / KE4", "IEM", "Harman 2019", listOf(0.6f, 0.4f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, 1.0f, -1.2f, 0.3f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── SIMGOT ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Simgot", "EW100P / EW100 DSP", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.4f, 0.2f, 0.5f, 1.2f, -1.2f, -2.8f, 1.0f)),
        AutoEqProfile("Simgot", "EW200", "IEM", "Harman 2019", listOf(1.0f, 0.5f, 0.0f, -0.5f, 0.2f, 0.5f, 1.2f, -1.5f, -3.0f, 1.0f)),
        AutoEqProfile("Simgot", "EW300 / EW300 DSP", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.1f, 0.4f, 1.0f, -1.2f, -2.2f, 0.6f)),
        AutoEqProfile("Simgot", "EA500 / EA500LM", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.0f, -0.4f, 0.2f, 0.5f, 1.0f, -1.8f, -2.8f, 0.8f)),
        AutoEqProfile("Simgot", "EA1000 Fermat", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, -1.2f, -2.5f, 0.8f)),
        AutoEqProfile("Simgot", "EM6L", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.5f, 0.8f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("Simgot", "SuperMix 4 / EM10", "IEM", "Harman 2019", listOf(0.4f, 0.2f, 0.0f, 0.1f, 0.2f, 0.4f, 0.8f, 0.8f, -1.2f, 0.3f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── AFUL ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("AFUL", "Performer 5 (P5)", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("AFUL", "Performer 8 (P8)", "IEM", "IEF Neutral", listOf(1.2f, 0.8f, 0.4f, 0.0f, 0.0f, 0.2f, 0.6f, 1.0f, -1.2f, 0.4f)),
        AutoEqProfile("AFUL", "Explorer", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.1f, 0.2f, 0.5f, 0.8f, 1.0f, -1.0f, 0.2f)),
        AutoEqProfile("AFUL", "MagicOne / Cantor", "IEM", "IEF Neutral", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, 0.4f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── KZ & CCA ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("KZ", "Castor (Harman Target)", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.2f, 0.5f)),
        AutoEqProfile("KZ", "Castor (Bass Enhanced)", "IEM", "Harman 2019", listOf(-1.5f, -1.0f, -0.5f, 0.0f, 0.2f, 0.5f, 1.2f, 1.5f, -1.8f, 0.5f)),
        AutoEqProfile("KZ", "PR1 Pro / PR2 / PR3", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.2f, 0.5f, 1.0f, -1.5f, -3.5f, 1.0f)),
        AutoEqProfile("KZ", "ZSN Pro / ZSN Pro X / ZSN Pro 2", "IEM", "Harman 2019", listOf(-1.2f, -1.5f, -0.8f, 0.2f, 0.5f, 0.8f, -1.5f, -4.2f, -5.0f, 1.5f)),
        AutoEqProfile("KZ", "ZS10 Pro / ZS10 Pro X / ZS10 Pro 2", "IEM", "Harman 2019", listOf(-0.8f, -1.0f, -0.5f, 0.0f, 0.4f, 0.8f, -1.0f, -3.5f, -4.0f, 1.2f)),
        AutoEqProfile("KZ", "Krila / D-Fi / Symphony", "IEM", "Harman 2019", listOf(0.4f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -2.5f, 0.8f)),
        AutoEqProfile("KZ", "Vader / AS16 Pro / AS24", "IEM", "IEF Neutral", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, -1.0f, -2.0f, 0.5f)),
        AutoEqProfile("KZ", "EDC Pro / EDX Pro / ZVX", "IEM", "Harman 2019", listOf(-0.5f, -0.8f, -0.2f, 0.2f, 0.4f, 0.8f, 1.0f, -2.0f, -3.5f, 1.0f)),
        AutoEqProfile("CCA", "CRA / CRA+", "IEM", "Harman 2019", listOf(-0.5f, -0.8f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.5f, -4.2f, 1.0f)),
        AutoEqProfile("CCA", "Rhapsody / Hydro / Trio", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("CCA", "Polaris / Duo / HM20 / PLA13", "IEM", "Harman 2019", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.8f, 0.8f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── LETSHUOER ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Letshuoer", "S12 / S12 Pro", "IEM", "Harman 2019", listOf(0.5f, 0.2f, -0.2f, -0.5f, 0.2f, 0.5f, 1.0f, 1.2f, -3.2f, 1.2f)),
        AutoEqProfile("Letshuoer", "S15 / S08", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.8f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("Letshuoer", "DZ4 / Galileo", "IEM", "IEF Neutral", listOf(1.8f, 1.2f, 0.5f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.0f, 0.2f)),
        AutoEqProfile("Letshuoer", "Cadenza 4 / EJ07 / EJ07M", "IEM", "IEF Neutral", listOf(1.0f, 0.6f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.0f, 0.3f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── DUNU ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Dunu", "Titan S / Titan S2", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Dunu", "SA6 / SA6 MKII / SA6 EST", "IEM", "IEF Neutral", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, 0.2f)),
        AutoEqProfile("Dunu", "Falcon Pro / Falcon Ultra", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.1f, 0.4f, 1.0f, 1.4f, -1.8f, 0.6f)),
        AutoEqProfile("Dunu", "DaVinci / Vulkan / Zen Pro", "IEM", "Harman 2019", listOf(0.2f, 0.0f, 0.0f, 0.2f, 0.2f, 0.5f, 0.8f, 1.0f, -1.2f, 0.2f)),
        AutoEqProfile("Dunu", "Glacier / Mirai", "IEM", "IEF Neutral", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── THIEAUDIO ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Thieaudio", "Monarch MKII / MKIII", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),
        AutoEqProfile("Thieaudio", "Hype 2 / Hype 4 / Hype 10", "IEM", "Harman 2019", listOf(0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, 1.0f, -1.2f, 0.2f)),
        AutoEqProfile("Thieaudio", "Prestige / Prestige LTD", "IEM", "IEF Neutral", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.5f, 0.5f)),
        AutoEqProfile("Thieaudio", "Clairvoyance / Oracle / Oracle MKII", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, 0.2f)),
        AutoEqProfile("Thieaudio", "Legacy 2 / Legacy 4 / Legacy 9", "IEM", "Harman 2019", listOf(1.0f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.8f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── FINAL AUDIO ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Final Audio", "E500 / E1000 / E2000", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.2f, 0.5f, 1.0f, 1.5f, -1.2f, 0.5f)),
        AutoEqProfile("Final Audio", "E3000 / E4000 / E5000", "IEM", "Harman 2019", listOf(-1.2f, -1.5f, -0.8f, 0.2f, 0.5f, 1.2f, 2.0f, 3.2f, 0.5f, 1.2f)),
        AutoEqProfile("Final Audio", "A3000 / A4000 / A5000", "IEM", "Harman 2019", listOf(1.0f, 0.5f, 0.0f, -0.5f, 0.2f, 0.8f, 1.5f, -2.5f, -3.8f, 1.5f)),
        AutoEqProfile("Final Audio", "A8000 / VR3000 / VR2000", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.1f, 0.5f, 1.2f, 1.5f, -2.0f, 0.8f)),
        AutoEqProfile("Final Audio", "ZE3000 / ZE8000", "IEM", "Harman 2019", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.4f, 1.0f, 1.2f, -1.5f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── SEE AUDIO ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("SeeAudio", "Yume / Yume II / Yume Ultra", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("SeeAudio", "Bravery / Bravery AE", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.8f, 0.6f)),
        AutoEqProfile("SeeAudio", "Kaguya / Neko / Hakuya", "IEM", "IEF Neutral", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, 0.2f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── CELEST & KINERA & QOA ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Celest", "Wyvern Abyss", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Celest", "Wyvern Pro (Gaming Edition)", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.4f, -1.5f, 0.5f)),
        AutoEqProfile("Celest", "Wyvern Qing", "IEM", "Harman 2019", listOf(0.9f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.6f, 0.5f)),
        AutoEqProfile("Celest", "Wyvern Black", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Celest", "Wyvern Custom", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Celest", "Pandamon", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("Celest", "Pandamon 2.0", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.9f, 1.2f, -1.6f, 0.5f)),
        AutoEqProfile("Celest", "Gumiho", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.3f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -2.0f, 0.6f)),
        AutoEqProfile("Celest", "Plutus Beast", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Celest", "PhoenixCall", "IEM", "Harman 2019", listOf(0.6f, 0.3f, -0.2f, -0.4f, 0.2f, 0.6f, 1.2f, 1.6f, -2.5f, 1.0f)),
        AutoEqProfile("Celest", "Relentless", "IEM", "Harman 2019", listOf(0.4f, 0.2f, 0.0f, 0.0f, 0.0f, 0.3f, 0.7f, 1.0f, -1.5f, 0.4f)),
        AutoEqProfile("Celest", "IgniteX Beast", "IEM", "Harman 2019", listOf(0.6f, 0.4f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.6f, 0.5f)),
        AutoEqProfile("Celest", "CD-1 / CD-2 / Dauntless", "IEM", "IEF Neutral", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.2f, 0.8f, 1.0f, -1.2f, 0.4f)),
        AutoEqProfile("Kinera", "Nanna / Nanna 2.0 Pro / Nanna Imperial", "IEM", "IEF Neutral", listOf(0.6f, 0.4f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.2f, 0.4f)),
        AutoEqProfile("Kinera", "Imperial Loki / Loki Emerald", "IEM", "IEF Neutral", listOf(0.4f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),
        AutoEqProfile("Kinera", "Imperial Baldr / Baldr 2", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Kinera", "Skuld", "IEM", "IEF Neutral", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.0f, 0.3f)),
        AutoEqProfile("Kinera", "Freya / Freya 2.0", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Kinera", "Idun / Idun Golden 2.0", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Kinera", "URD", "IEM", "Harman 2019", listOf(0.6f, 0.4f, 0.2f, 0.0f, 0.0f, 0.3f, 0.8f, 1.0f, -1.4f, 0.4f)),
        AutoEqProfile("Kinera", "Hodur", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.6f, 0.5f)),
        AutoEqProfile("Kinera", "Gramr / Celest Ruyi", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("QoA", "Vesper", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.4f, -1.8f, 0.5f)),
        AutoEqProfile("QoA", "Vesper 2", "IEM", "Harman 2019", listOf(0.9f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.9f, 1.3f, -1.7f, 0.5f)),
        AutoEqProfile("QoA", "Adonis", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("QoA", "Adonis New", "IEM", "Harman 2019", listOf(0.7f, 0.4f, 0.1f, -0.2f, 0.0f, 0.4f, 0.8f, 1.2f, -1.4f, 0.4f)),
        AutoEqProfile("QoA", "Aviation", "IEM", "Harman 2019", listOf(0.6f, 0.4f, 0.0f, 0.0f, 0.0f, 0.3f, 0.8f, 1.0f, -1.2f, 0.3f)),
        AutoEqProfile("QoA", "Gimlet", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.3f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("QoA", "Mojito", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.0f, -1.4f, 0.4f)),
        AutoEqProfile("QoA", "Pink Lady", "IEM", "Harman 2019", listOf(0.9f, 0.6f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.1f, -1.5f, 0.4f)),
        AutoEqProfile("QoA", "Affinity", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.3f, 0.8f, 1.0f, -1.4f, 0.4f)),
        AutoEqProfile("QoA", "Sunrise", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.6f, 0.5f)),
        AutoEqProfile("QoA", "Margarita", "IEM", "IEF Neutral", listOf(0.6f, 0.4f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.0f, 0.3f)),
        AutoEqProfile("EPZ", "Q1 / Q5 / 530 / K5 / 620", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── TINHIFI, BLON, QKZ ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("TinHiFi", "T2 / T2 Plus / T2 DLC / T3 Plus", "IEM", "Harman 2019", listOf(1.8f, 1.2f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("TinHiFi", "T4 Plus / P1 Max / C2 / C3", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -1.8f, 0.6f)),
        AutoEqProfile("BLON", "BL-03 / BL-05S / Z300", "IEM", "Harman 2019", listOf(-1.0f, -1.2f, -0.5f, 0.2f, 0.4f, 0.8f, 1.2f, 1.8f, -1.5f, 0.5f)),
        AutoEqProfile("QKZ", "VK4 / QKZ x HBB / Hades", "IEM", "Harman 2019", listOf(-1.5f, -1.8f, -1.0f, 0.2f, 0.5f, 1.0f, 1.5f, 2.0f, -1.2f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── FIIO ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("FiiO", "FH3 / FH7 / FH9", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -2.0f, 0.8f)),
        AutoEqProfile("FiiO", "FD3 / FD5 / FD7", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.4f, -2.2f, 0.8f)),
        AutoEqProfile("FiiO", "FA7S / FA9 / FX15", "IEM", "IEF Neutral", listOf(1.0f, 0.6f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 1.0f, -1.5f, 0.5f)),
        AutoEqProfile("FiiO", "JH3 / JD7 / JD1", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.5f, 1.0f, 1.2f, -1.8f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── HIGH-END TOTL IEMs (Campfire, UM, 64 Audio, Empire Ears, Vision Ears, Etymotic) ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Campfire Audio", "Andromeda / Andromeda 2020", "IEM", "Harman 2019", listOf(2.5f, 1.8f, 0.8f, -0.5f, -0.8f, 0.2f, 1.2f, 2.5f, -2.5f, 1.2f)),
        AutoEqProfile("Campfire Audio", "Solaris / Bonneville / Ara", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.5f, -2.0f, 0.8f)),
        AutoEqProfile("Unique Melody", "MEST MKII / MEST MKIII / Mentor", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.6f, 0.8f, -1.2f, 0.4f)),
        AutoEqProfile("64 Audio", "U12t / U18t / Nio / Trio", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),
        AutoEqProfile("Empire Ears", "Legend X / Legend EVO / Odin", "IEM", "Harman 2019", listOf(-0.8f, -1.0f, -0.5f, 0.2f, 0.4f, 0.6f, 1.0f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Vision Ears", "VE7 / VE8 / Phonix / Elysium", "IEM", "IEF Neutral", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, 0.2f)),
        AutoEqProfile("Etymotic", "ER2SE / ER3SE / ER4SR", "IEM", "IEF Neutral", listOf(4.5f, 3.5f, 1.8f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, -0.8f, 0.2f)),
        AutoEqProfile("Etymotic", "ER2XR / ER3XR / ER4XR / EVO", "IEM", "Harman 2019", listOf(2.2f, 1.5f, 0.8f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, 0.2f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── SENNHEISER ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Sennheiser", "IE 200", "IEM", "Harman 2019", listOf(1.8f, 1.2f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.5f, 0.8f)),
        AutoEqProfile("Sennheiser", "IE 300 / IE 600", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.0f, 0.4f, 0.8f, 1.0f, -2.2f, 0.8f)),
        AutoEqProfile("Sennheiser", "IE 900", "IEM", "Harman 2019", listOf(0.2f, 0.0f, -0.2f, -0.2f, 0.0f, 0.5f, 1.0f, 1.2f, -3.0f, 1.0f)),
        AutoEqProfile("Sennheiser", "HD 560S", "Over-Ear", "Harman 2018", listOf(3.2f, 2.5f, 1.2f, 0.2f, 0.0f, 0.0f, 0.8f, 1.5f, -1.2f, 0.5f)),
        AutoEqProfile("Sennheiser", "HD 600", "Over-Ear", "Harman 2018", listOf(4.2f, 3.8f, 2.5f, 0.5f, -0.2f, 0.0f, 1.2f, 2.8f, -1.5f, 1.0f)),
        AutoEqProfile("Sennheiser", "HD 650 / HD 6XX", "Over-Ear", "Harman 2018", listOf(5.0f, 4.2f, 2.0f, -0.5f, -0.2f, 0.2f, 1.5f, 3.2f, -1.8f, 0.8f)),
        AutoEqProfile("Sennheiser", "HD 660S / HD 660S2", "Over-Ear", "Harman 2018", listOf(3.8f, 3.0f, 1.5f, 0.0f, -0.2f, 0.2f, 1.2f, 2.5f, -1.5f, 0.6f)),
        AutoEqProfile("Sennheiser", "HD 800S", "Over-Ear", "Harman 2018", listOf(4.5f, 3.8f, 2.2f, 0.5f, 0.0f, 0.0f, 0.5f, 1.0f, -4.5f, 1.2f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── SONY ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Sony", "IER-M7 / IER-M9", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.4f, 0.0f, 0.0f, 0.2f, 0.6f, 1.0f, -1.2f, 0.4f)),
        AutoEqProfile("Sony", "IER-Z1R", "IEM", "Harman 2019", listOf(0.5f, 0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 1.0f, 1.2f, -2.5f, 0.8f)),
        AutoEqProfile("Sony", "MDR-EX800ST / MDR-7550", "IEM", "IEF Neutral", listOf(1.8f, 1.2f, 0.5f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, 0.2f)),
        AutoEqProfile("Sony", "MDR-7506 / MDR-CD900ST", "Over-Ear", "Harman 2018", listOf(2.5f, 1.8f, 0.8f, -0.2f, 0.2f, 0.8f, 1.5f, -2.5f, -3.2f, 1.0f)),
        AutoEqProfile("Sony", "WH-1000XM4 / WH-1000XM5", "Over-Ear", "Harman 2018", listOf(-2.5f, -3.2f, -2.0f, 0.5f, 1.0f, 1.5f, 2.2f, 3.0f, -1.0f, 1.5f)),
        AutoEqProfile("Sony", "WF-1000XM4 / WF-1000XM5", "IEM", "Harman 2019", listOf(-0.5f, -1.0f, -0.4f, 0.2f, 0.5f, 1.0f, 1.5f, 2.0f, -1.5f, 0.8f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── HIFIMAN ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("HiFiMAN", "HE400se", "Over-Ear", "Harman 2018", listOf(3.5f, 2.8f, 1.5f, 0.2f, 0.0f, 0.0f, 0.8f, 1.5f, -2.0f, 0.8f)),
        AutoEqProfile("HiFiMAN", "Sundara", "Over-Ear", "Harman 2018", listOf(4.0f, 3.2f, 1.8f, 0.5f, 0.0f, 0.0f, 0.5f, 1.0f, -2.2f, 0.5f)),
        AutoEqProfile("HiFiMAN", "Edition XS", "Over-Ear", "Harman 2018", listOf(3.2f, 2.5f, 1.2f, 0.0f, 0.0f, 0.2f, 0.8f, 1.2f, -1.8f, 0.5f)),
        AutoEqProfile("HiFiMAN", "Ananda / Ananda Nano", "Over-Ear", "Harman 2018", listOf(3.8f, 3.0f, 1.5f, 0.2f, 0.0f, 0.0f, 0.5f, 1.0f, -2.5f, 0.8f)),
        AutoEqProfile("HiFiMAN", "Arya / Arya Stealth / Organic", "Over-Ear", "Harman 2018", listOf(3.0f, 2.2f, 1.0f, 0.0f, 0.0f, 0.2f, 0.6f, 1.0f, -2.2f, 0.6f)),
        AutoEqProfile("HiFiMAN", "HE1000 V2 / Stealth / Susvara", "Over-Ear", "Harman 2018", listOf(2.8f, 2.0f, 0.8f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, 0.4f)),
        AutoEqProfile("HiFiMAN", "Svanar / Svanar Wireless", "IEM", "Harman 2019", listOf(0.8f, 0.5f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── BEYERDYNAMIC & AUDIO-TECHNICA ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Beyerdynamic", "DT 770 Pro (80 / 250 Ohm)", "Over-Ear", "Harman 2018", listOf(1.5f, 0.8f, -0.5f, -0.8f, 0.2f, 0.8f, 1.2f, -2.5f, -5.2f, 0.5f)),
        AutoEqProfile("Beyerdynamic", "DT 990 Pro / DT 1990 Pro", "Over-Ear", "Harman 2018", listOf(3.5f, 2.5f, 1.0f, -0.5f, 0.0f, 0.5f, 1.0f, -3.0f, -6.5f, 1.0f)),
        AutoEqProfile("Beyerdynamic", "DT 700 Pro X / DT 900 Pro X", "Over-Ear", "Harman 2018", listOf(2.2f, 1.5f, 0.8f, 0.0f, 0.0f, 0.2f, 0.8f, -1.2f, -2.8f, 0.6f)),
        AutoEqProfile("Beyerdynamic", "Xelento / Xelento 2nd Gen", "IEM", "Harman 2019", listOf(-1.2f, -1.8f, -1.0f, 0.2f, 0.5f, 1.0f, 1.5f, 2.0f, -1.5f, 0.8f)),
        AutoEqProfile("Audio-Technica", "ATH-M50x / ATH-M40x", "Over-Ear", "Harman 2018", listOf(0.5f, -0.2f, -0.8f, -0.5f, 0.2f, 0.8f, 1.5f, -1.8f, -3.2f, 0.8f)),
        AutoEqProfile("Audio-Technica", "ATH-R70x", "Over-Ear", "Harman 2018", listOf(3.8f, 3.0f, 1.8f, 0.4f, 0.0f, 0.0f, 0.5f, 1.2f, -1.0f, 0.4f)),
        AutoEqProfile("Audio-Technica", "ATH-E40 / ATH-E70 / ATH-IEX1", "IEM", "Harman 2019", listOf(1.0f, 0.6f, 0.2f, -0.2f, 0.0f, 0.4f, 1.0f, 1.2f, -2.0f, 0.6f)),

        // ══════════════════════════════════════════════════════════════════════════
        // ── APPLE ──
        // ══════════════════════════════════════════════════════════════════════════
        AutoEqProfile("Apple", "AirPods Pro (1st Gen)", "IEM", "Harman 2019", listOf(2.0f, 1.5f, 0.8f, 0.0f, 0.0f, 0.4f, 0.8f, 1.2f, -1.5f, 0.5f)),
        AutoEqProfile("Apple", "AirPods Pro (2nd Gen)", "IEM", "Harman 2019", listOf(1.2f, 0.8f, 0.4f, 0.0f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, 0.2f)),
        AutoEqProfile("Apple", "AirPods Max", "Over-Ear", "Harman 2018", listOf(1.0f, 0.5f, 0.2f, 0.0f, 0.0f, 0.5f, 1.2f, 1.8f, -1.8f, 0.6f)),
        AutoEqProfile("Apple", "EarPods (3.5mm / Lightning / Type-C)", "Earbuds", "Harman 2019", listOf(6.5f, 5.2f, 3.0f, 0.5f, -0.5f, 0.2f, 1.2f, 2.5f, -1.8f, 0.8f))
    )

    fun getAllProfiles(): List<AutoEqProfile> = profiles

    fun search(query: String): List<AutoEqProfile> {
        if (query.isBlank()) return profiles
        val tokens = query.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (tokens.isEmpty()) return profiles

        return profiles.filter { profile ->
            val brandLower = profile.brand.lowercase()
            val modelLower = profile.model.lowercase()
            val displayLower = profile.displayName.lowercase()
            val aliasLower = profile.aliases.joinToString(" ").lowercase()
            val parentBrand = when (profile.brand) {
                "Celest" -> "kinera kinera celest"
                "QoA" -> "kinera queen of audio"
                "Salnotes" -> "7hz"
                "Tangzu" -> "tforce"
                else -> ""
            }
            val fullSearchableText = "$displayLower $brandLower $modelLower $aliasLower $parentBrand ${profile.type.lowercase()} ${profile.targetCurve.lowercase()}"
            tokens.all { token -> fullSearchableText.contains(token) }
        }
    }

    fun searchProfiles(query: String): List<AutoEqProfile> = search(query)
}
