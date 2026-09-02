package com.thesis.bitperfectusb.presentation.dialog

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.thesis.bitperfectusb.domain.model.KaraokeModeType
import com.thesis.bitperfectusb.playback.ai.AiEngineState
import com.thesis.bitperfectusb.playback.ai.KaraokeScoreState
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary

/**
 * Cyber-Audiophile Real-Time AI Karaoke Studio Console.
 * Features:
 * 1. Full Instrumental Vocal Removal / Pure Acapella Extractor (100% Complete Mute)
 * 2. Real-Time Pitch Shifter / Key Transposer (-6 to +6 semitones)
 * 3. Low-End Bass & Kick Drum Preservation Filter (< 140Hz)
 * 4. Real-time Live Vocal Fundamental Note & Pitch Tracker
 * 5. Live Microphone Singing Evaluation & Real-Time Pitch Scorer with Permissions
 */
@Composable
fun KaraokeStudioDialog(
    aiState: AiEngineState,
    scoreState: KaraokeScoreState,
    onDismiss: () -> Unit,
    onSetKaraokeMode: (KaraokeModeType) -> Unit,
    onSetStrength: (Float) -> Unit,
    onSetKeyShift: (Int) -> Unit,
    onToggleBassPreservation: () -> Unit,
    onToggleMicScoring: (Boolean) -> Unit,
    onResetScore: () -> Unit,
    isInstrumentalCached: Boolean = false,
    isAcapellaCached: Boolean = false,
    cacheSizeBytes: Long = 0L,
    stemProgress: com.thesis.bitperfectusb.playback.ai.StemExtractionProgress? = null,
    onExtractInstrumental: () -> Unit = {},
    onExtractAcapella: () -> Unit = {},
    onClearStemCache: () -> Unit = {},
    onDismissStemProgress: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleMicScoring(true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceCharcoal,
            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Mic, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI KARAOKE STUDIO", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SignalTeal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(HifiGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(BorderStroke(0.5.dp, HifiGold), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                    Text(
                                        text = if (aiState.isTfliteModelLoaded) "TFLITE XNNPACK" else "NEURAL STFT",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HifiGold
                                    )
                                }
                            }
                            Text("Neural Stem Separation & Live Mic Scorer (Demixr Architecture)", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = OutlineSubtle)

                // ── 1. LIVE MICROPHONE SINGING SCORING CONSOLE ──
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                    border = BorderStroke(1.dp, if (scoreState.isMicActive) SignalTeal else OutlineSubtle),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (scoreState.isMicActive) Icons.Filled.Mic else Icons.Filled.MicOff,
                                    contentDescription = null,
                                    tint = if (scoreState.isMicActive) SignalTeal else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "LIVE SINGING EVALUATION",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (scoreState.isMicActive) SignalTeal else TextSecondary
                                )
                            }

                            // Mic Toggle Pill Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (scoreState.isMicActive) SignalTeal else SurfaceCharcoal)
                                    .border(BorderStroke(1.dp, if (scoreState.isMicActive) SignalTeal else OutlineSubtle), RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (!scoreState.isMicActive) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasPermission) {
                                                onToggleMicScoring(true)
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        } else {
                                            onToggleMicScoring(false)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (scoreState.isMicActive) "MIC ON (SCORING)" else "ENABLE MIC",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (scoreState.isMicActive) BackgroundCharcoal else TextPrimary
                                )
                            }
                        }

                        // Score & Grade Telemetry Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TOTAL SCORE", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${scoreState.totalScore}",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scoreState.totalScore >= 80) HifiGold else SignalTeal
                                    )
                                    Text(" / 100", fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 3.dp))
                                }
                            }

                            // Grade Badge
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            when {
                                                scoreState.currentGrade.contains("PERFECT") -> HifiGold.copy(alpha = 0.2f)
                                                scoreState.currentGrade.contains("GREAT") -> SignalTeal.copy(alpha = 0.2f)
                                                else -> SurfaceCharcoal
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                when {
                                                    scoreState.currentGrade.contains("PERFECT") -> HifiGold
                                                    scoreState.currentGrade.contains("GREAT") -> SignalTeal
                                                    else -> OutlineSubtle
                                                }
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = scoreState.currentGrade,
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            scoreState.currentGrade.contains("PERFECT") -> HifiGold
                                            scoreState.currentGrade.contains("GREAT") -> SignalTeal
                                            else -> TextPrimary
                                        }
                                    )
                                }
                                if (scoreState.combo > 1) {
                                    Text(
                                        text = "COMBO x${scoreState.combo} 🔥",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HifiGold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            // Reset Score Button
                            IconButton(onClick = onResetScore, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Reset Score", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Live Dual Pitch Gauge (Singer Note vs Target Note)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCharcoal, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("SINGER PITCH", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                                Text(
                                    text = if (scoreState.singerNote != "--") "${scoreState.singerNote} (${scoreState.singerPitchHz.toInt()} Hz)" else "--",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignalTeal
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("GUIDE KEY", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                                Text(
                                    text = if (aiState.detectedMusicalNote != "--") aiState.detectedMusicalNote else "AUTO",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HifiGold
                                )
                            }
                        }

                        // Live Mic VU Level Meter
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("MIC INPUT LEVEL", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, color = TextSecondary)
                                Text(
                                    text = if (scoreState.micLevel > 0.05f) "VOICE DETECTED" else "SILENT",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 6.5.sp,
                                    color = if (scoreState.micLevel > 0.05f) SignalTeal else TextSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { scoreState.micLevel.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (scoreState.micLevel > 0.8f) Color(0xFFEF5350) else SignalTeal,
                                trackColor = SurfaceCharcoal
                            )
                        }
                    }
                }

                // ── 2. AUDIO ISOLATION MODE SELECTOR & CACHE ROUTING ──
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AUDIO ISOLATION SOURCE & MODE", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        if (isInstrumentalCached) {
                            Text("✓ CACHED STEM READY", fontFamily = TelemetryFontFamily, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val isInstrumental = aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.INSTRUMENTAL_ONLY
                        val isAcapella = aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.VOCAL_ISOLATION
                        val isOff = !aiState.karaokeModeEnabled || aiState.karaokeModeType == KaraokeModeType.OFF

                        // Instrumental Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isInstrumental) SignalTeal else BackgroundCharcoal)
                                .border(BorderStroke(1.dp, if (isInstrumental) SignalTeal else OutlineSubtle), RoundedCornerShape(10.dp))
                                .clickable { onSetKaraokeMode(KaraokeModeType.INSTRUMENTAL_ONLY) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = if (isInstrumental) BackgroundCharcoal else SignalTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("INSTRUMENTAL", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isInstrumental) BackgroundCharcoal else TextPrimary)
                                if (isInstrumentalCached) {
                                    Text("✓ CACHED", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = if (isInstrumental) BackgroundCharcoal else SignalTeal)
                                }
                            }
                        }

                        // Acapella Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isAcapella) HifiGold else BackgroundCharcoal)
                                .border(BorderStroke(1.dp, if (isAcapella) HifiGold else OutlineSubtle), RoundedCornerShape(10.dp))
                                .clickable { onSetKaraokeMode(KaraokeModeType.VOCAL_ISOLATION) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Mic, contentDescription = null, tint = if (isAcapella) BackgroundCharcoal else HifiGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ACAPELLA", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isAcapella) BackgroundCharcoal else TextPrimary)
                                if (isAcapellaCached) {
                                    Text("✓ CACHED", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = if (isAcapella) BackgroundCharcoal else HifiGold)
                                }
                            }
                        }

                        // Off Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOff) TextSecondary.copy(alpha = 0.2f) else BackgroundCharcoal)
                                .border(BorderStroke(1.dp, if (isOff) TextSecondary else OutlineSubtle), RoundedCornerShape(10.dp))
                                .clickable { onSetKaraokeMode(KaraokeModeType.OFF) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Close, contentDescription = null, tint = if (isOff) TextPrimary else TextSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("ORIGINAL MIX", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (isOff) TextPrimary else TextSecondary)
                                Text("FULL STEREO", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Active Stream Source Banner
                    val (sourceText, sourceColor, sourceIcon) = when {
                        aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.INSTRUMENTAL_ONLY && isInstrumentalCached ->
                            Triple("⚡ PLAYING FROM APP CACHE (CLEAN PRE-RENDERED LOSSLESS MASTER, 0% CPU)", SignalTeal, Icons.Filled.GraphicEq)
                        aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.INSTRUMENTAL_ONLY ->
                            Triple("🎛️ PLAYING WITH LIVE REAL-TIME DSP VOCAL REMOVER", HifiGold, Icons.Filled.GraphicEq)
                        aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.VOCAL_ISOLATION && isAcapellaCached ->
                            Triple("⚡ PLAYING FROM APP CACHE (CLEAN PRE-RENDERED ACAPELLA STEM)", HifiGold, Icons.Filled.GraphicEq)
                        aiState.karaokeModeEnabled && aiState.karaokeModeType == KaraokeModeType.VOCAL_ISOLATION ->
                            Triple("🎤 PLAYING WITH LIVE REAL-TIME VOCAL ISOLATION", HifiGold, Icons.Filled.Mic)
                        else ->
                            Triple("🎵 PLAYING ORIGINAL UNMODIFIED AUDIO (BIT-PERFECT / STEREO)", TextSecondary, Icons.Filled.MusicNote)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCharcoal, RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, sourceColor.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(sourceIcon, contentDescription = null, tint = sourceColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(sourceText, fontFamily = TelemetryFontFamily, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = sourceColor)
                    }
                }

                // ── 3. VOCAL SUPPRESSION STRENGTH SLIDER ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("VOCAL ATTENUATION DEPTH", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary)
                        Text(
                            text = "${(aiState.karaokeVocalSuppressionStrength * 100).toInt()}% (COMPLETE MUTE)",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalTeal
                        )
                    }

                    Slider(
                        value = aiState.karaokeVocalSuppressionStrength,
                        onValueChange = onSetStrength,
                        valueRange = 0.10f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = SignalTeal,
                            activeTrackColor = SignalTeal,
                            inactiveTrackColor = OutlineSubtle
                        )
                    )
                }

                // ── 4. REAL-TIME PITCH KEY TRANSPOSER (-6 to +6 semitones) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Piano, contentDescription = null, tint = HifiGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KEY SHIFTER / TRANSPOSER", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        val keyText = when {
                            aiState.karaokeKeyShiftSemitones > 0 -> "+${aiState.karaokeKeyShiftSemitones} SEMITONES"
                            aiState.karaokeKeyShiftSemitones < 0 -> "${aiState.karaokeKeyShiftSemitones} SEMITONES"
                            else -> "ORIGINAL KEY (0)"
                        }
                        Text(keyText, fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // [-] Pitch Down Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCharcoal)
                                .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(8.dp))
                                .clickable { onSetKeyShift((aiState.karaokeKeyShiftSemitones - 1).coerceAtLeast(-6)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Remove, contentDescription = "Pitch Down", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("-1 SEMITONE", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextPrimary)
                            }
                        }

                        // [RESET] Button
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (aiState.karaokeKeyShiftSemitones != 0) HifiGold.copy(alpha = 0.2f) else SurfaceCharcoal)
                                .border(BorderStroke(1.dp, if (aiState.karaokeKeyShiftSemitones != 0) HifiGold else OutlineSubtle), RoundedCornerShape(8.dp))
                                .clickable { onSetKeyShift(0) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RESET (0)", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = if (aiState.karaokeKeyShiftSemitones != 0) HifiGold else TextSecondary)
                        }

                        // [+] Pitch Up Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCharcoal)
                                .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(8.dp))
                                .clickable { onSetKeyShift((aiState.karaokeKeyShiftSemitones + 1).coerceAtMost(6)) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Add, contentDescription = "Pitch Up", tint = TextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+1 SEMITONE", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                // ── 5. BASS & KICK DRUM PRESERVATION SWITCH ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Bass & Kick Drum Preservation (< 140Hz)", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Protects sub-bass lines & 808 kick drums with zero vocal bleed", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                        }
                    }

                    Switch(
                        checked = aiState.karaokeBassPreservation,
                        onCheckedChange = { onToggleBassPreservation() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SignalTeal,
                            checkedTrackColor = SignalTeal.copy(alpha = 0.25f)
                        )
                    )
                }

                // ── 6. ON-DEVICE AI STEM EXTRACTION (NO PC NEEDED) ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ON-DEVICE AI STEM RENDERER", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("100% STANDALONE (NO PC)", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                    }

                    Text(
                        "Pre-renders this song into high-speed app cache directly on your phone using multi-threaded STFT neural processing. (Keeps your phone's Music folder clean & uncluttered).",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 7.5.sp,
                        color = TextSecondary
                    )

                    if (stemProgress == null) {
                        // Instrumental Extractor Card
                        if (isInstrumentalCached) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCharcoal, RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, SignalTeal), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("✓ INSTRUMENTAL CACHED", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                        Text("Clean pre-rendered master active. 0% CPU, 100% vocal removal.", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SignalTeal.copy(alpha = 0.2f))
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓ CACHED (ACTIVE)", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(6.dp))
                                            .clickable { onExtractInstrumental() }
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔄 RE-EXTRACT", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SignalTeal.copy(alpha = 0.2f))
                                    .border(BorderStroke(1.dp, SignalTeal), RoundedCornerShape(8.dp))
                                    .clickable { onExtractInstrumental() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ EXTRACT FULL INSTRUMENTAL FILE (ON-DEVICE)", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                }
                            }
                        }

                        // Acapella Extractor Card
                        if (isAcapellaCached) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCharcoal, RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, HifiGold), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, tint = HifiGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("✓ ACAPELLA CACHED", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                                        Text("Solo vocal stem extracted & ready for instant playback.", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(HifiGold.copy(alpha = 0.2f))
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓ CACHED (ACTIVE)", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(6.dp))
                                            .clickable { onExtractAcapella() }
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🔄 RE-EXTRACT", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HifiGold.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, HifiGold.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                                    .clickable { onExtractAcapella() }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, tint = HifiGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("🎤 EXTRACT ACAPELLA / SOLO VOCAL STEM", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                                }
                            }
                        }

                        // Storage Management Footer
                        if (cacheSizeBytes > 0L) {
                            val cacheMb = String.format("%.1f", cacheSizeBytes / (1024.0 * 1024.0))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceCharcoal, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("STEM CACHE USAGE: $cacheMb MB", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Red.copy(alpha = 0.15f))
                                        .border(BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                        .clickable { onClearStemCache() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🗑️ CLEAR CACHE", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceCharcoal, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stemProgress.statusMessage,
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stemProgress.error != null) Color.Red else SignalTeal
                                )
                                Text(
                                    "${stemProgress.progressPercent}%",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignalTeal
                                )
                            }

                            LinearProgressIndicator(
                                progress = { stemProgress.progressPercent / 100.0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SignalTeal,
                                trackColor = OutlineSubtle
                            )

                            if (stemProgress.isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SignalTeal)
                                        .clickable { onDismissStemProgress() }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("DONE — SAVED TO APP CACHE", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

