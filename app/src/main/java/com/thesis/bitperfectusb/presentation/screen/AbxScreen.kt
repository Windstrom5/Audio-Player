package com.thesis.bitperfectusb.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesis.bitperfectusb.domain.abx.AbxSample
import com.thesis.bitperfectusb.domain.abx.AbxTestMode
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.WarnAmber
import com.thesis.bitperfectusb.presentation.viewmodel.AbxViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbxScreen(viewModel: AbxViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val trial = state.trialState
    val scrollState = rememberScrollState()

    var showResultDialog by remember { mutableStateOf(false) }
    if (trial.isCompleted && !showResultDialog) {
        showResultDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Headphones,
                            contentDescription = null,
                            tint = HifiGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ABX BLIND TEST LAB",
                            fontFamily = TelemetryFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.startNewGame(state.selectedMode, 10) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Restart Game", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundCharcoal,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = BackgroundCharcoal
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. TEST MODE SELECTOR PILLS ──
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "SELECT CHALLENGE MODE",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AbxTestMode.entries) { mode ->
                        val isSelected = state.selectedMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectMode(mode) },
                            label = {
                                Text(
                                    mode.title,
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HifiGold.copy(alpha = 0.2f),
                                selectedLabelColor = HifiGold
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = OutlineSubtle,
                                selectedBorderColor = HifiGold,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // ── 2. ACTIVE CHALLENGE CARD ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, OutlineSubtle),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            state.selectedMode.title,
                            fontFamily = TelemetryFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = HifiGold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SignalTeal.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "DIFFICULTY: ${state.selectedMode.difficulty}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalTeal
                            )
                        }
                    }

                    Text(
                        state.selectedMode.description,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    if (state.lastFeedbackMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundCharcoal)
                                .border(BorderStroke(1.dp, if (trial.lastGuessResult == true) Color(0xFF44E088) else if (trial.lastGuessResult == false) ErrorCoral else OutlineSubtle), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = state.lastFeedbackMessage!!,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 10.sp,
                                color = if (trial.lastGuessResult == true) Color(0xFF44E088) else if (trial.lastGuessResult == false) ErrorCoral else TextPrimary
                            )
                        }
                    }
                }
            }

            // ── 3. DOUBLE-BLIND LISTENING CONSOLE (A, B, X) ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "DOUBLE-BLIND LISTENING CONSOLE",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // SAMPLE A PAD
                    val isAPlaying = trial.activePlayingSample == AbxSample.A
                    AbxListeningPad(
                        letter = "A",
                        subtitle = "Sample A",
                        isPlaying = isAPlaying,
                        color = SignalTeal,
                        onClick = { viewModel.selectSample(AbxSample.A) },
                        modifier = Modifier.weight(1f)
                    )

                    // SAMPLE B PAD
                    val isBPlaying = trial.activePlayingSample == AbxSample.B
                    AbxListeningPad(
                        letter = "B",
                        subtitle = "Sample B",
                        isPlaying = isBPlaying,
                        color = WarnAmber,
                        onClick = { viewModel.selectSample(AbxSample.B) },
                        modifier = Modifier.weight(1f)
                    )

                    // MYSTERY X PAD
                    val isXPlaying = trial.activePlayingSample == AbxSample.X
                    AbxListeningPad(
                        letter = "X",
                        subtitle = "Mystery X",
                        isPlaying = isXPlaying,
                        color = HifiGold,
                        onClick = { viewModel.selectSample(AbxSample.X) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 4. VOTING DECISION BUTTONS ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "CAST YOUR VOTE (WHICH SAMPLE IS X?)",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.submitGuess(guessIsA = true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "X IS SAMPLE A",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BackgroundCharcoal
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.submitGuess(guessIsA = false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarnAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "X IS SAMPLE B",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = BackgroundCharcoal
                            )
                        }
                    }
                }
            }

            // ── 5. LIVE SCORECARD & STATISTICAL SIGNIFICANCE ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, OutlineSubtle),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "SCIENTIFIC SCORECARD",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            "ROUND ${trial.currentRound} / ${trial.totalRounds}",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HifiGold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (trial.currentRound.toFloat() / trial.totalRounds).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = HifiGold,
                        trackColor = BackgroundCharcoal
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreStatBox(title = "ACCURACY", value = "${trial.accuracyPercent.toInt()}%", color = if (trial.accuracyPercent >= 80) Color(0xFF44E088) else HifiGold)
                        ScoreStatBox(title = "SCORE", value = "${trial.correctGuesses} / ${if (trial.isCompleted) trial.totalRounds else (trial.currentRound - 1).coerceAtLeast(0)}", color = TextPrimary)
                        ScoreStatBox(title = "STREAK", value = "${trial.currentStreak} 🔥", color = WarnAmber)
                        ScoreStatBox(title = "P-VALUE", value = "%.3f".format(trial.pValue), color = if (trial.pValue < 0.05) Color(0xFF44E088) else TextSecondary)
                    }

                    HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.5f))

                    // Ear Rank
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("AUDIOPHILE EAR RANK", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
                            Text(
                                text = trial.earRank,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (trial.pValue < 0.05) HifiGold else TextPrimary
                            )
                        }
                    }
                    Text(
                        trial.earRankDescription,
                        color = TextSecondary,
                        fontSize = 9.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ── GAME OVER CERTIFICATE MODAL ──
    if (showResultDialog) {
        Dialog(onDismissRequest = { showResultDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = SurfaceRaised,
                border = BorderStroke(1.dp, HifiGold)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = HifiGold,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        "ABX TEST COMPLETED",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = HifiGold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        trial.earRank,
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    Text(
                        "Score: ${trial.correctGuesses} / ${trial.totalRounds} (${trial.accuracyPercent.toInt()}%)\nStatistical p-value: ${"%.4f".format(trial.pValue)}",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        trial.earRankDescription,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            showResultDialog = false
                            viewModel.startNewGame(state.selectedMode, 10)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HifiGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("PLAY AGAIN", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                    }
                }
            }
        }
    }
}

@Composable
private fun AbxListeningPad(
    letter: String,
    subtitle: String,
    isPlaying: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPlaying) color.copy(alpha = 0.2f) else SurfaceRaised)
            .border(
                BorderStroke(
                    if (isPlaying) 2.dp else 1.dp,
                    if (isPlaying) color else OutlineSubtle
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) color else BackgroundCharcoal),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontFamily = TelemetryFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) BackgroundCharcoal else color
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontFamily = TelemetryFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) color else TextPrimary
            )
            Text(
                text = if (isPlaying) "PLAYING 🔊" else "TAP TO PLAY",
                fontFamily = TelemetryFontFamily,
                fontSize = 7.5.sp,
                color = if (isPlaying) color else TextSecondary
            )
        }
    }
}

@Composable
private fun ScoreStatBox(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = TextSecondary)
        Text(
            text = value,
            fontFamily = TelemetryFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
