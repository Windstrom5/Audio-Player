package com.thesis.bitperfectusb.presentation.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import kotlin.random.Random

private val ValidGreen = Color(0xFF44E088)

/**
 * Foobar2000-style ABX Double-Blind Listening Comparator Modal.
 * Enables objective audio testing (e.g. Bit-Perfect Direct vs Android Mixer Resampled).
 */
@Composable
fun AbxTestDialog(
    onDismiss: () -> Unit,
    onPlaySampleA: () -> Unit,
    onPlaySampleB: () -> Unit
) {
    var trialNumber by remember { mutableIntStateOf(1) }
    var correctAnswers by remember { mutableIntStateOf(0) }
    var currentTargetIsA by remember { mutableStateOf(Random.nextBoolean()) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var isTestComplete by remember { mutableStateOf(false) }

    val totalTrials = 10

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = SurfaceCharcoal,
            border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                        Icon(Icons.Filled.Headphones, contentDescription = null, tint = HifiGold, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ABX DOUBLE-BLIND TEST", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HifiGold)
                            Text("Foobar2000 Scientific Hearing Comparator", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = OutlineSubtle)

                if (!isTestComplete) {
                    Text(
                        "Trial $trialNumber of $totalTrials • Score: $correctAnswers / ${trialNumber - 1}",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SignalTeal
                    )

                    // Reference Buttons: Play A, Play B
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onPlaySampleA,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = HifiGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAMPLE A (DIRECT)", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = HifiGold)
                            }
                        }

                        OutlinedButton(
                            onClick = onPlaySampleB,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAMPLE B (DSP/MIX)", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = SignalTeal)
                            }
                        }
                    }

                    // Mystery Target Button: Play X
                    Button(
                        onClick = {
                            if (currentTargetIsA) onPlaySampleA() else onPlaySampleB()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = HifiGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, tint = BackgroundCharcoal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PLAY MYSTERY SAMPLE 'X'", fontFamily = TelemetryFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                        }
                    }

                    // User Guess Row: "X is A" vs "X is B"
                    Text("GUESS WHICH SOURCE 'X' IS:", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val correct = currentTargetIsA
                                if (correct) correctAnswers++
                                isAnswerCorrect = correct
                                feedbackMessage = if (correct) "✓ CORRECT! X was Sample A" else "✗ INCORRECT! X was Sample B"

                                if (trialNumber >= totalTrials) {
                                    isTestComplete = true
                                } else {
                                    trialNumber++
                                    currentTargetIsA = Random.nextBoolean()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundCharcoal),
                            border = BorderStroke(1.dp, HifiGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("X IS SAMPLE A", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                        }

                        Button(
                            onClick = {
                                val correct = !currentTargetIsA
                                if (correct) correctAnswers++
                                isAnswerCorrect = correct
                                feedbackMessage = if (correct) "✓ CORRECT! X was Sample B" else "✗ INCORRECT! X was Sample A"

                                if (trialNumber >= totalTrials) {
                                    isTestComplete = true
                                } else {
                                    trialNumber++
                                    currentTargetIsA = Random.nextBoolean()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BackgroundCharcoal),
                            border = BorderStroke(1.dp, SignalTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("X IS SAMPLE B", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                        }
                    }

                    feedbackMessage?.let { msg ->
                        Text(
                            text = msg,
                            fontFamily = TelemetryFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAnswerCorrect == true) ValidGreen else ErrorCoral
                        )
                    }
                } else {
                    // Test Summary
                    val confidencePct = (correctAnswers.toFloat() / totalTrials) * 100
                    val isStatisticallySignificant = correctAnswers >= 8

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ABX TEST RESULTS", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HifiGold)
                        Text("Total Score: $correctAnswers / $totalTrials (${confidencePct.toInt()}%)", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = TextPrimary)
                        Text(
                            text = if (isStatisticallySignificant) "✓ STATISTICALLY SIGNIFICANT AUDIBLE DIFFERENCE (p < 0.05). Your ears can reliably discern the source."
                            else "ℹ NO STATISTICAL DIFFERENCE (p > 0.05). Inaudible difference or within chance threshold.",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 8.5.sp,
                            color = if (isStatisticallySignificant) ValidGreen else SignalTeal
                        )
                    }

                    Button(
                        onClick = {
                            trialNumber = 1
                            correctAnswers = 0
                            currentTargetIsA = Random.nextBoolean()
                            feedbackMessage = null
                            isTestComplete = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESTART ABX TEST", fontFamily = TelemetryFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                    }
                }
            }
        }
    }
}
