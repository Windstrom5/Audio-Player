package com.thesis.bitperfectusb.presentation.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thesis.bitperfectusb.playback.ai.AiEngineState
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary

/**
 * On-Device TensorFlow Lite AI Neural Studio Modal.
 * Visualizes live neural genre classification, DSEE high-frequency upscaling, and AI Auto-Pilot EQ.
 */
@Composable
fun AiNeuralStudioDialog(
    aiState: AiEngineState,
    onDismiss: () -> Unit,
    onToggleAutoPilot: (Boolean) -> Unit,
    onToggleDsee: (Boolean) -> Unit,
    onToggleVocalSuppression: (Boolean) -> Unit,
    onApplySuggestedEq: (List<Float>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceCharcoal,
            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("AI NEURAL STUDIO", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SignalTeal)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(HifiGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .border(BorderStroke(0.5.dp, HifiGold), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text("TFLITE ON-DEVICE", fontFamily = TelemetryFontFamily, fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                                }
                            }
                            Text("Real-Time Neural Genre Classification & DSEE Upscaling", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = OutlineSubtle)

                // Top Card: Real-Time Detected Genre & Confidence
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                    border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("LIVE NEURAL ACOUSTIC SIGNATURE", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            Text("${aiState.confidencePct.toInt()}% CONFIDENCE", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                        }

                        Text(
                            text = aiState.detectedGenre,
                            fontFamily = TelemetryFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignalTeal
                        )

                        val animatedConfidence by animateFloatAsState(targetValue = aiState.confidencePct / 100f, label = "confidence")
                        LinearProgressIndicator(
                            progress = { animatedConfidence },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = SignalTeal,
                            trackColor = OutlineSubtle,
                        )
                    }
                }

                // AI Features Controls Row
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundCharcoal, RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Feature 1: AI Auto-Pilot EQ
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = HifiGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("AI Auto-Pilot Genre Equalizer", fontFamily = TelemetryFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Auto-tunes 10-band EQ to detected genre", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = aiState.isAutoPilotActive,
                            onCheckedChange = onToggleAutoPilot,
                            colors = SwitchDefaults.colors(checkedThumbColor = HifiGold, checkedTrackColor = HifiGold.copy(alpha = 0.25f))
                        )
                    }

                    HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.5f))

                    // Feature 2: Neural DSEE Audio Upscaler
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Neural DSEE Harmonic Upscaler", fontFamily = TelemetryFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "(${aiState.detectedCutoffKHz.toInt()} kHz)",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 7.5.sp,
                                        color = if (aiState.isLossyDetected) HifiGold else SignalTeal
                                    )
                                }
                                Text("Synthesizes lost >16kHz high overtones", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = aiState.isDseeUpscalerActive,
                            onCheckedChange = onToggleDsee,
                            colors = SwitchDefaults.colors(checkedThumbColor = SignalTeal, checkedTrackColor = SignalTeal.copy(alpha = 0.25f))
                        )
                    }

                    HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.5f))

                    // Feature 3: AI Vocal Karaoke Isolator
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Mic, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("AI Vocal Karaoke Suppressor", fontFamily = TelemetryFontFamily, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    if (aiState.detectedMusicalNote != "--") {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("[Key: ${aiState.detectedMusicalNote}]", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = HifiGold)
                                    }
                                }
                                Text("Cancels lead vocal for sing-along practice", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = aiState.isVocalSuppressionActive || aiState.karaokeModeEnabled,
                            onCheckedChange = onToggleVocalSuppression,
                            colors = SwitchDefaults.colors(checkedThumbColor = SignalTeal, checkedTrackColor = SignalTeal.copy(alpha = 0.25f))
                        )
                    }
                }

                // Detailed Genre Breakdown List
                Text("NEURAL PROBABILITY BREAKDOWN", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = HifiGold)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val entries = aiState.genreProbabilities.entries.sortedByDescending { it.value }
                    items(entries.toList()) { (genre, prob) ->
                        val isTop = genre == aiState.detectedGenre
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isTop) SignalTeal.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                genre,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 8.5.sp,
                                fontWeight = if (isTop) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTop) SignalTeal else TextPrimary
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(4.dp)
                                        .background(OutlineSubtle, RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth((prob / 100f).coerceIn(0f, 1f))
                                            .background(if (isTop) SignalTeal else TextSecondary, RoundedCornerShape(2.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${prob.toInt()}%",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    color = if (isTop) SignalTeal else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Apply Suggested EQ Button
                Button(
                    onClick = { onApplySuggestedEq(aiState.suggestedEqGains) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = BackgroundCharcoal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("APPLY AI SUGGESTED EQ PROFILE", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                    }
                }
            }
        }
    }
}
