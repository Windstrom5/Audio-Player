package com.thesis.bitperfectusb.presentation.screen

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import com.thesis.bitperfectusb.presentation.components.rememberArtworkPalette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.thesis.bitperfectusb.domain.model.LyricsProvider
import com.thesis.bitperfectusb.domain.model.LyricsSearchResult
import com.thesis.bitperfectusb.playback.autoeq.AutoEqProfile
import com.thesis.bitperfectusb.playback.autoeq.AutoEqRepository
import com.thesis.bitperfectusb.playback.lyrics.LyricsRomanizer
import com.thesis.bitperfectusb.playback.lyrics.RomajiDisplayMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.TrendPoint
import com.thesis.bitperfectusb.domain.model.UserSettings
import com.thesis.bitperfectusb.presentation.components.BitActivityLedGrid
import com.thesis.bitperfectusb.playback.autoeq.AutoEqOnlineScraper
import com.thesis.bitperfectusb.presentation.dialog.AbxTestDialog
import com.thesis.bitperfectusb.presentation.dialog.AiNeuralStudioDialog
import com.thesis.bitperfectusb.presentation.dialog.KaraokeStudioDialog
import androidx.compose.material.icons.filled.Psychology
import com.thesis.bitperfectusb.domain.model.ReverbPreset
import com.thesis.bitperfectusb.domain.model.ReplayGainMode
import com.thesis.bitperfectusb.domain.model.DsdPlaybackMode
import com.thesis.bitperfectusb.presentation.components.IntegrityGauge
import com.thesis.bitperfectusb.presentation.components.StatCard
import com.thesis.bitperfectusb.presentation.components.TrackArtwork
import com.thesis.bitperfectusb.presentation.components.charts.LineChart
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.McIntoshBlue
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SignalTealDim
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.WarnAmber
import com.thesis.bitperfectusb.presentation.viewmodel.AudioRoute
import com.thesis.bitperfectusb.presentation.viewmodel.PlayerUiState
import com.thesis.bitperfectusb.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerScreen(viewModel: PlayerViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stemProgress by viewModel.stemExtractionProgress.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val lrcPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.bufferedReader(Charsets.UTF_8).readText()
                }
            }.getOrNull()
            if (!text.isNullOrBlank()) {
                viewModel.importCustomLyrics(text)
            }
        }
    }

    // Modals visibility states for feature panels
    var showEqModal by remember { mutableStateOf(false) }
    var showDspModal by remember { mutableStateOf(false) }
    var showVuModal by remember { mutableStateOf(false) }
    var showAudioInfoModal by remember { mutableStateOf(false) }
    var showSleepTimerModal by remember { mutableStateOf(false) }
    var showQueueModal by remember { mutableStateOf(false) }
    var showAbLoopModal by remember { mutableStateOf(false) }
    var showToolsModal by remember { mutableStateOf(false) }
    var showLyricsSourceModal by remember { mutableStateOf(false) }
    var showAutoEqModal by remember { mutableStateOf(false) }
    var showAbxModal by remember { mutableStateOf(false) }
    var showHardwareVolumeModal by remember { mutableStateOf(false) }
    var showAiStudioModal by remember { mutableStateOf(false) }
    var showKaraokeStudioModal by remember { mutableStateOf(false) }

    val isBitPerfect = state.selectedEngine == EngineType.CUSTOM_USB_DIRECT
    val currentTrack = state.playback.currentTrack

    // Dynamic Color Palette Extraction from Album Artwork
    val palette = rememberArtworkPalette(currentTrack?.filePath ?: "")
    val animatedDominantColor by animateColorAsState(
        targetValue = palette.dominantColor,
        animationSpec = tween(700),
        label = "dominant_color"
    )
    val animatedVibrantColor by animateColorAsState(
        targetValue = if (isBitPerfect) palette.vibrantColor else HifiGold,
        animationSpec = tween(700),
        label = "vibrant_color"
    )
    val animatedDarkColor by animateColorAsState(
        targetValue = palette.darkVibrantColor,
        animationSpec = tween(700),
        label = "dark_color"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "BITPERFECT AUDIO",
                            fontFamily = TelemetryFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = HifiGold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        val dotColor = if (state.playback.isPlaying) SignalTeal else TextSecondary
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showQueueModal = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Playback Queue",
                            tint = if (state.playbackQueue.hasQueue) SignalTeal else TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundCharcoal
    ) { innerPadding ->
        // Ambient Dynamic Glow Background Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedDominantColor.copy(alpha = 0.45f),
                            animatedDarkColor.copy(alpha = 0.75f),
                            BackgroundCharcoal,
                            BackgroundCharcoal
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 1. Real High-Res Album Cover Artwork with Glowing Backdrop or Synchronized Lyrics ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { viewModel.toggleLyricsView() },
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 14.dp,
                    color = SurfaceCharcoal.copy(alpha = 0.6f),
                    border = BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                animatedVibrantColor.copy(alpha = 0.5f),
                                OutlineSubtle.copy(alpha = 0.2f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.showLyricsView) {
                            SynchronizedLyricsView(
                                lyrics = state.lyrics,
                                activeLyricIndex = state.activeLyricIndex,
                                activeProvider = state.lyricsProvider,
                                onSeekTo = { viewModel.seekTo(it) },
                                onOpenSourceModal = { showLyricsSourceModal = true },
                                onImportLrc = { lrcPickerLauncher.launch("*/*") },
                                onDeleteCustomLyrics = { viewModel.deleteCustomLyrics() },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            TrackArtwork(
                                uriString = currentTrack?.filePath ?: "",
                                title = currentTrack?.title ?: "No Track Selected",
                                artist = currentTrack?.artist,
                                isPlaying = state.playback.isPlaying,
                                isLarge = true,
                                cornerRadius = 24.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Top Badge Overlay on Artwork
                        if (!state.showLyricsView) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(14.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        BackgroundCharcoal.copy(alpha = 0.85f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (isBitPerfect) SignalTeal.copy(alpha = 0.6f) else WarnAmber.copy(alpha = 0.6f)
                                        ),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { showAudioInfoModal = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isBitPerfect) "BIT-PERFECT USB ⚡" else "ANDROID MIXER 🎛️",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBitPerfect) SignalTeal else WarnAmber,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ── 2. Track Title & Artist with Horizontal Marquee ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = currentTrack?.title ?: "No Track Selected",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Text(
                        text = currentTrack?.artist ?: "Select a track from the library to start playback",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── 3. Live Spectrum Visualizer ──
                LiveFrequencyVisualizer(
                    isPlaying = state.playback.isPlaying,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ── 4. Seekbar Slider & Time Elapsed / Duration ──
                val durationMs = (currentTrack?.durationMs ?: 1L).coerceAtLeast(1L)
                var isDraggingSeek by remember(currentTrack?.id) { mutableStateOf(false) }
                var dragPositionMs by remember(currentTrack?.id) { mutableStateOf(0f) }
                var pendingSeekPositionMs by remember(currentTrack?.id) { mutableStateOf<Float?>(null) }
                var smoothPositionMs by remember(currentTrack?.id) { mutableStateOf(state.playback.positionMs.toFloat()) }

                // Sync with backend positionMs updates
                LaunchedEffect(state.playback.positionMs, state.playback.isPlaying) {
                    if (!isDraggingSeek) {
                        val backendPos = state.playback.positionMs.toFloat()
                        if (pendingSeekPositionMs != null) {
                            if (kotlin.math.abs(backendPos - pendingSeekPositionMs!!) < 1500f || !state.playback.isPlaying) {
                                pendingSeekPositionMs = null
                                smoothPositionMs = backendPos
                            }
                        } else {
                            smoothPositionMs = backendPos
                        }
                    }
                }

                // Smooth high-framerate local progression while playing
                LaunchedEffect(state.playback.isPlaying, isDraggingSeek, pendingSeekPositionMs) {
                    if (state.playback.isPlaying && !isDraggingSeek && pendingSeekPositionMs == null) {
                        var lastTime = System.currentTimeMillis()
                        while (isActive) {
                            delay(40L) // 25 fps smooth glide
                            val now = System.currentTimeMillis()
                            val delta = (now - lastTime).toFloat()
                            lastTime = now
                            smoothPositionMs = (smoothPositionMs + delta).coerceIn(0f, durationMs.toFloat())
                        }
                    }
                }

                val displayedPositionMs = when {
                    isDraggingSeek -> dragPositionMs
                    pendingSeekPositionMs != null -> pendingSeekPositionMs!!
                    else -> smoothPositionMs.coerceIn(0f, durationMs.toFloat())
                }

                Slider(
                    value = displayedPositionMs,
                    onValueChange = {
                        isDraggingSeek = true
                        dragPositionMs = it
                    },
                    onValueChangeFinished = {
                        val targetMs = dragPositionMs
                        pendingSeekPositionMs = targetMs
                        isDraggingSeek = false
                        viewModel.seekTo(targetMs.toLong())
                    },
                    valueRange = 0f..durationMs.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = animatedVibrantColor,
                        activeTrackColor = animatedVibrantColor,
                        inactiveTrackColor = OutlineSubtle.copy(alpha = 0.4f)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            formatPlaybackTime(displayedPositionMs.toLong()),
                            fontFamily = TelemetryFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.sleepTimer.isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🌙 ${state.sleepTimer.remainingSeconds / 60}:${"%02d".format(state.sleepTimer.remainingSeconds % 60)}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                color = animatedVibrantColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (state.abLoop.isEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🔁 A-B [${formatPlaybackTime(state.abLoop.pointAMs ?: 0L)} - ${formatPlaybackTime(state.abLoop.pointBMs ?: durationMs)}]",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                color = HifiGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val drScore = state.playback.liveDrScore
                        val drColor = if (drScore >= 14) Color(0xFF44E088) else if (drScore >= 10) animatedVibrantColor else HifiGold
                        Text(
                            "DR$drScore",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = drColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(drColor.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, drColor.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            formatPlaybackTime(durationMs),
                            fontFamily = TelemetryFontFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── 5. Playback Controls in Natural Thumb-Reach Zone ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleShuffle() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (state.shuffleEnabled) animatedVibrantColor else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.previousTrack() },
                        enabled = state.hasPrevious,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = if (state.hasPrevious) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 72dp Elevated Play/Pause Surface with Physics-Based Spring Scale
                    val playScale by animateFloatAsState(
                        targetValue = if (state.playback.isPlaying) 1f else 0.96f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "play_scale"
                    )

                    Surface(
                        onClick = {
                            when {
                                state.playback.isPlaying -> viewModel.pause()
                                state.playback.isPaused -> viewModel.resume()
                                else -> viewModel.replayCurrentTrack()
                            }
                        },
                        shape = CircleShape,
                        color = animatedVibrantColor,
                        tonalElevation = 8.dp,
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(playScale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (state.playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.playback.isPlaying) "Pause" else "Play",
                                tint = BackgroundCharcoal,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nextTrack() },
                        enabled = state.hasNext,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next Track",
                            tint = if (state.hasNext) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { showAbLoopModal = true },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Filled.Repeat,
                            contentDescription = "A-B Repeat Looper",
                            tint = if (state.abLoop.isEnabled) HifiGold else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ── 6. Streamlined Tactile Volume Slider ──
                val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
                var currentVol by remember {
                    mutableStateOf(audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 10f)
                }
                val maxVol = remember {
                    audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)?.toFloat()?.coerceAtLeast(1f) ?: 15f
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (currentVol == 0f) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeDown,
                        contentDescription = "Volume Down",
                        tint = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                currentVol = 0f
                                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                            }
                    )

                    Slider(
                        value = currentVol,
                        onValueChange = {
                            currentVol = it
                            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, it.toInt(), 0)
                        },
                        valueRange = 0f..maxVol,
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = animatedVibrantColor.copy(alpha = 0.9f),
                            activeTrackColor = animatedVibrantColor.copy(alpha = 0.6f),
                            inactiveTrackColor = OutlineSubtle.copy(alpha = 0.3f)
                        )
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume Up / Audio Info",
                        tint = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { showAudioInfoModal = true }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── 7. SIMPLIFIED & STREAMLINED AUDIOPHILE TOOLBAR ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    AudiophileToolButton(
                        label = "10-BAND EQ",
                        icon = Icons.Filled.Equalizer,
                        active = state.eqEnabled,
                        activeColor = HifiGold,
                        onClick = { showEqModal = true },
                        modifier = Modifier.weight(1f)
                    )
                    AudiophileToolButton(
                        label = "DSP",
                        icon = Icons.Filled.Tune,
                        active = state.crossfeedEnabled || state.replayGainEnabled || state.normalizationEnabled,
                        activeColor = HifiGold,
                        onClick = { showDspModal = true },
                        modifier = Modifier.weight(1f)
                    )
                    AudiophileToolButton(
                        label = if (state.aiState.karaokeModeEnabled) "KARAOKE 🎤" else "KARAOKE",
                        icon = Icons.Filled.Mic,
                        active = state.aiState.karaokeModeEnabled,
                        activeColor = SignalTeal,
                        onClick = { showKaraokeStudioModal = true },
                        modifier = Modifier.weight(1f)
                    )
                    AudiophileToolButton(
                        label = if (state.aiState.isAutoPilotActive) "AI (⚡)" else "AI STUDIO",
                        icon = Icons.Filled.Psychology,
                        active = state.aiState.isAutoPilotActive || state.aiState.isDseeUpscalerActive || state.aiState.isVocalSuppressionActive,
                        activeColor = SignalTeal,
                        onClick = { showAiStudioModal = true },
                        modifier = Modifier.weight(1f)
                    )
                    AudiophileToolButton(
                        label = if (state.sleepTimer.isEnabled) "TOOLS (🌙)" else "TOOLS",
                        icon = if (state.sleepTimer.isEnabled) Icons.Filled.Bedtime else Icons.AutoMirrored.Filled.FormatListBulleted,
                        active = state.sleepTimer.isEnabled || state.abLoop.isEnabled || state.playbackQueue.hasQueue,
                        activeColor = SignalTeal,
                        onClick = { showToolsModal = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── 8. Poweramp-Style Clickable Audio Info Pill ──
                val trackFormat = currentTrack?.format?.name ?: "FLAC"
                val bitDepth = currentTrack?.pcm?.bitDepth ?: 24
                val sampleRateKhz = "%.1f".format((currentTrack?.pcm?.sampleRateHz ?: 48000) / 1000.0)
                val bitrateKbps = ((currentTrack?.pcm?.sampleRateHz ?: 48000).toLong() * (currentTrack?.pcm?.bitDepth ?: 24) * (currentTrack?.pcm?.channels ?: 2) / 1000).toInt()

                val infoPillText = "$bitDepth-BIT  •  $sampleRateKhz kHz  •  $bitrateKbps KBPS  •  $trackFormat"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showAudioInfoModal = true },
                    colors = CardDefaults.cardColors(containerColor = SurfaceRaised.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, if (isBitPerfect) animatedVibrantColor.copy(alpha = 0.4f) else HifiGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = if (isBitPerfect) animatedVibrantColor else HifiGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = infoPillText,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "AUDIO INFO ❯",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBitPerfect) animatedVibrantColor else HifiGold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // ── MODAL 1: 10-BAND STUDIO MASTER EQUALIZER CONSOLE ──
    if (showEqModal) {
        Dialog(
            onDismissRequest = { showEqModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = HifiGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("STUDIO MASTER EQUALIZER", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HifiGold)
                                Text("10-Band Precision Audiophile DSP Engine", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (state.eqEnabled) "ENABLED" else "BYPASS",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.eqEnabled) SignalTeal else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = state.eqEnabled,
                                onCheckedChange = { viewModel.toggleEq() },
                                colors = SwitchDefaults.colors(checkedThumbColor = SignalTeal, checkedTrackColor = SignalTeal.copy(alpha = 0.25f))
                            )
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    if (state.eqEnabled) {
                        // ── Interactive Frequency Response Spline Curve Canvas ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(135.dp)
                                .background(BackgroundCharcoal, RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val midY = h / 2f
                                val bottomY = h - 18f

                                // Grid reference lines (+12dB, +6dB, 0dB, -6dB, -12dB)
                                val lineAlpha = 0.25f
                                drawLine(color = OutlineSubtle.copy(alpha = lineAlpha), start = Offset(0f, 12f), end = Offset(w, 12f), strokeWidth = 1f)
                                drawLine(color = OutlineSubtle.copy(alpha = lineAlpha * 0.7f), start = Offset(0f, (midY + 12f) / 2f), end = Offset(w, (midY + 12f) / 2f), strokeWidth = 0.8f)
                                drawLine(color = HifiGold.copy(alpha = 0.45f), start = Offset(0f, midY), end = Offset(w, midY), strokeWidth = 1.5f)
                                drawLine(color = OutlineSubtle.copy(alpha = lineAlpha * 0.7f), start = Offset(0f, (midY + bottomY) / 2f), end = Offset(w, (midY + bottomY) / 2f), strokeWidth = 0.8f)
                                drawLine(color = OutlineSubtle.copy(alpha = lineAlpha), start = Offset(0f, bottomY), end = Offset(w, bottomY), strokeWidth = 1f)

                                val gains = state.eqGains
                                val bandCount = UserSettings.EQ_BANDS.size
                                val stepX = w / (bandCount - 1).coerceAtLeast(1)

                                val points = mutableListOf<Offset>()
                                for (i in 0 until bandCount) {
                                    val g = gains.getOrElse(i) { 0f }.coerceIn(-12f, 12f)
                                    val y = midY - (g / 12f) * ((midY - 14f).coerceAtLeast(10f))
                                    points.add(Offset(i * stepX, y))

                                    // Vertical dashed grid line per band
                                    drawLine(
                                        color = if (g != 0f) SignalTeal.copy(alpha = 0.2f) else OutlineSubtle.copy(alpha = 0.12f),
                                        start = Offset(i * stepX, 10f),
                                        end = Offset(i * stepX, bottomY),
                                        strokeWidth = 1f
                                    )
                                }

                                if (points.isNotEmpty()) {
                                    val strokePath = Path()
                                    val fillPath = Path()

                                    strokePath.moveTo(points[0].x, points[0].y)
                                    fillPath.moveTo(points[0].x, bottomY)
                                    fillPath.lineTo(points[0].x, points[0].y)

                                    for (i in 0 until points.size - 1) {
                                        val p0 = points[i]
                                        val p1 = points[i + 1]
                                        val cx = (p0.x + p1.x) / 2f
                                        strokePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                        fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                                    }

                                    fillPath.lineTo(w, bottomY)
                                    fillPath.close()

                                    // Luxurious gradient fill under response curve
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                SignalTeal.copy(alpha = 0.35f),
                                                HifiGold.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            startY = 10f,
                                            endY = bottomY
                                        )
                                    )

                                    // Soft outer bloom/glow stroke
                                    drawPath(
                                        path = strokePath,
                                        color = SignalTeal.copy(alpha = 0.3f),
                                        style = Stroke(width = 6f)
                                    )

                                    // Crisp inner curve line
                                    drawPath(
                                        path = strokePath,
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                SignalTeal,
                                                HifiGold,
                                                SignalTeal
                                            )
                                        ),
                                        style = Stroke(width = 2.8f)
                                    )

                                    // Frequency node halos & dots
                                    for (i in points.indices) {
                                        val pt = points[i]
                                        val gain = gains.getOrElse(i) { 0f }
                                        val active = gain != 0f
                                        if (active) {
                                            drawCircle(color = SignalTeal.copy(alpha = 0.35f), radius = 7f, center = pt)
                                        }
                                        drawCircle(color = if (active) HifiGold else SignalTeal, radius = 4f, center = pt)
                                        drawCircle(color = BackgroundCharcoal, radius = 2f, center = pt)
                                    }
                                }
                            }

                            // Left dB level labels
                            Text("+12dB", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.TopStart))
                            Text(" 0dB", fontFamily = TelemetryFontFamily, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = HifiGold.copy(alpha = 0.85f), modifier = Modifier.align(Alignment.CenterStart))
                            Text("-12dB", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 12.dp))

                            // Right dB level labels
                            Text("+12", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.TopEnd))
                            Text(" 0", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = HifiGold.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.CenterEnd))
                            Text("-12", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 12.dp))
                        }

                        // ── Quick Sound Signature Preset Chips ──
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            listOf(
                                "FLAT" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
                                "HARMAN BASS" to listOf(4.5f, 3.8f, 2.5f, 0.8f, 0.0f, -0.5f, 0.5f, 1.2f, -1.8f, 0.5f),
                                "VOCAL AIR" to listOf(1.0f, 0.8f, 0.5f, 0.2f, 1.5f, 2.2f, 1.8f, 1.0f, -1.2f, 0.8f),
                                "TREBLE AIR" to listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.0f, 0.5f, 1.5f, 2.8f, 2.2f, 1.8f),
                                "V-SHAPED" to listOf(3.5f, 2.8f, 1.2f, -0.5f, -0.8f, -0.5f, 0.8f, 2.2f, 2.8f, 1.5f),
                                "WARM TUBE" to listOf(3.0f, 2.5f, 1.8f, 1.0f, 0.5f, 0.0f, -0.5f, -1.0f, -1.5f, -2.0f)
                            ).forEach { (presetName, presetGains) ->
                                OutlinedButton(
                                    onClick = { viewModel.updateEqGains(presetGains) },
                                    modifier = Modifier.height(26.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    border = BorderStroke(1.dp, if (presetName == "FLAT") HifiGold.copy(alpha = 0.5f) else SignalTeal.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        presetName,
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (presetName == "FLAT") HifiGold else SignalTeal
                                    )
                                }
                            }
                        }

                        // ── 10-Channel Studio Fader Board ──
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundCharcoal, RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(UserSettings.EQ_BANDS) { bandIndex, frequencyHz ->
                                    val gain = state.eqGains.getOrElse(bandIndex) { 0f }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(38.dp)
                                    ) {
                                        Text(
                                            text = if (gain > 0) "+${"%.1f".format(gain)}" else if (gain < 0) "%.1f".format(gain) else "0.0",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 7.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (gain != 0f) HifiGold else TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Slider(
                                            value = gain,
                                            onValueChange = { newGain ->
                                                val newGains = state.eqGains.toMutableList()
                                                newGains[bandIndex] = newGain
                                                viewModel.updateEqGains(newGains)
                                            },
                                            valueRange = -12f..12f,
                                            steps = 23,
                                            modifier = Modifier.height(105.dp),
                                            colors = SliderDefaults.colors(
                                                thumbColor = if (gain != 0f) HifiGold else TextSecondary,
                                                activeTrackColor = if (gain != 0f) HifiGold.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.3f),
                                                inactiveTrackColor = OutlineSubtle
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (frequencyHz >= 1000) "${frequencyHz / 1000}k" else "$frequencyHz",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 8.sp,
                                            color = SignalTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showAutoEqModal = true },
                                modifier = Modifier.weight(1.4f),
                                border = BorderStroke(1.dp, SignalTeal),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Headphones, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AUTOEQ PRESETS", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.updateEqGains(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("RESET FLAT", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 12.dp)) {
                            Text(
                                "Equalizer is currently BYPASSED DIRECT for bit-perfect accuracy.",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                            Button(
                                onClick = { viewModel.toggleEq() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("ENABLE EQUALIZER", fontFamily = TelemetryFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BackgroundCharcoal)
                            }
                            OutlinedButton(
                                onClick = { showAutoEqModal = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Headphones, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("BROWSE AUTOEQ IEM & HEADPHONE PRESETS", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                }
                            }
                        }
                    }

                    IconButton(onClick = { showEqModal = false }, modifier = Modifier.align(Alignment.End)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            }
        }
    }

    // ── MODAL 2: POWERAMP & FOOBAR2000 AUDIOPHILE DSP SUITE MODAL ──
    if (showDspModal) {
        Dialog(
            onDismissRequest = { showDspModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = HifiGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("AUDIOPHILE DSP SUITE", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HifiGold)
                                Text("Poweramp & Foobar2000 Floating-Point Precision Engines", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            }
                        }
                        IconButton(onClick = { showDspModal = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // ── 1. Poweramp Stereo Soundstage Expander ──
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("STEREO SOUNDSTAGE EXPANDER", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SignalTeal)
                                Text("Mid/Side holographic acoustic width matrix", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            }
                            Text(
                                if (state.spatialWidth == 0.0f) "MONO" else if (state.spatialWidth == 1.0f) "STANDARD 100%" else "${(state.spatialWidth * 100).toInt()}% WIDE",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HifiGold
                            )
                        }
                        Slider(
                            value = state.spatialWidth,
                            onValueChange = { viewModel.setSpatialWidth(it) },
                            valueRange = 0.0f..2.0f,
                            steps = 19,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(thumbColor = SignalTeal, activeTrackColor = SignalTeal)
                        )
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // ── 2. Poweramp Studio Reverb Simulator ──
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("STUDIO REVERB ENVIRONMENT", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = HifiGold)
                                Text("Schroeder acoustic room response modeling", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = state.reverbEnabled,
                                onCheckedChange = { viewModel.toggleReverb() },
                                colors = SwitchDefaults.colors(checkedThumbColor = HifiGold, checkedTrackColor = HifiGold.copy(alpha = 0.25f))
                            )
                        }

                        if (state.reverbEnabled) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ReverbPreset.entries.forEach { preset ->
                                    val isSelected = state.reverbPreset == preset
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setReverbPreset(preset) },
                                        label = { Text(preset.label, fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = HifiGold.copy(alpha = 0.2f),
                                            selectedLabelColor = HifiGold
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = OutlineSubtle,
                                            selectedBorderColor = HifiGold.copy(alpha = 0.6f),
                                            enabled = true,
                                            selected = isSelected
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(26.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("WET / DRY", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary, modifier = Modifier.width(64.dp))
                                Slider(
                                    value = state.reverbWetDry,
                                    onValueChange = { viewModel.setReverbWetDry(it) },
                                    valueRange = 0.0f..1.0f,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = HifiGold, activeTrackColor = HifiGold)
                                )
                                Text("${(state.reverbWetDry * 100).toInt()}%", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = HifiGold)
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // ── 3. Foobar2000 ReplayGain & EBU R128 Normalizer ──
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("REPLAYGAIN (EBU R128)", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = SignalTeal)
                                Text("Dynamic loudness normalization with anti-clipping", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                            }
                            Switch(
                                checked = state.replayGainEnabled,
                                onCheckedChange = { viewModel.toggleReplayGain() },
                                colors = SwitchDefaults.colors(checkedThumbColor = SignalTeal, checkedTrackColor = SignalTeal.copy(alpha = 0.25f))
                            )
                        }

                        if (state.replayGainEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ReplayGainMode.entries.forEach { mode ->
                                    val isSelected = state.replayGainMode == mode
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setReplayGainMode(mode) },
                                        label = { Text(mode.label, fontFamily = TelemetryFontFamily, fontSize = 8.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SignalTeal.copy(alpha = 0.2f),
                                            selectedLabelColor = SignalTeal
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("PREAMP", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary, modifier = Modifier.width(64.dp))
                                Slider(
                                    value = state.replayGainPreampDb,
                                    onValueChange = { viewModel.setReplayGainPreampDb(it) },
                                    valueRange = -12.0f..12.0f,
                                    steps = 23,
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(thumbColor = SignalTeal, activeTrackColor = SignalTeal)
                                )
                                Text("${if (state.replayGainPreampDb > 0) "+" else ""}${"%.1f".format(state.replayGainPreampDb)}dB", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = SignalTeal)
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // ── 4. Crossfeed ──
                    DspToggleRow("HEADPHONE CROSSFEED", "Meier natural acoustic room bleed", state.crossfeedEnabled) { viewModel.toggleCrossfeed() }
                    if (state.crossfeedEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("STRENGTH", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary, modifier = Modifier.width(64.dp))
                            Slider(
                                value = state.crossfeedStrength,
                                onValueChange = { viewModel.setCrossfeedStrength(it) },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(thumbColor = HifiGold, activeTrackColor = HifiGold)
                            )
                            Text("%.0f%%".format(state.crossfeedStrength * 100), fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = HifiGold)
                        }
                    }

                    // ── 5. ABX Listening Comparator Launcher ──
                    OutlinedButton(
                        onClick = {
                            showDspModal = false
                            showAbxModal = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Headphones, contentDescription = null, tint = HifiGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LAUNCH FOOBAR2000 ABX BLIND LISTENING TEST", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 3: MCINTOSH VU METERS & BIT-MATRIX MODAL ──
    if (showVuModal) {
        Dialog(onDismissRequest = { showVuModal = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).padding(12.dp),
                shape = RoundedCornerShape(18.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("ANALOG POWER & TELEMETRY", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SignalTeal)
                        IconButton(onClick = { showVuModal = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    // Dual Analog VU Meter
                    val ampL = if (state.playback.isPlaying) 0.6f else 0.02f
                    val ampR = if (state.playback.isPlaying) 0.58f else 0.02f
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AnalogVuMeter(amplitude = ampL, channelLabel = "LEFT CHANNEL", modifier = Modifier.weight(1f))
                        AnalogVuMeter(amplitude = ampR, channelLabel = "RIGHT CHANNEL", modifier = Modifier.weight(1f))
                    }

                    // 24-Bit Bit Activity Matrix
                    Text("24-BIT BIT-ACTIVITY LED MATRIX", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = SignalTeal)
                    BitActivityLedGrid(activeBitMask = state.playback.verification?.activeBitMask ?: 0x00FFFFFF)

                    // Line Charts
                    if (state.playback.recentHistory.size >= 2) {
                        LineChart(title = "Latency Jitter (ms)", points = state.playback.recentHistory.map { TrendPoint("", it.latencyMs) })
                    }
                }
            }
        }
    }

    // ── MODAL 4: POWERAMP-STYLE PIXEL-PERFECT AUDIO INFO DIALOG ──
    if (showAudioInfoModal) {
        val dac = state.selectedDac
        val currentTrack = state.playback.currentTrack
        var showHardwareSettings by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { showAudioInfoModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.88f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF202226),
                border = BorderStroke(1.dp, Color(0xFF32363D))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    // ── Poweramp Header ──
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Audio Info",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showHardwareSettings = !showHardwareSettings },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Settings,
                                    contentDescription = "Settings",
                                    tint = if (showHardwareSettings) SignalTeal else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { showAudioInfoModal = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Expandable Hardware & Buffer Settings Panel (Activated by Settings Gear or Gapless text) ──
                    if (showHardwareSettings) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181C)),
                            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("HARDWARE & BUFFER SETTINGS", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                                    Text("GAPLESS ACTIVE", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                                }
                                Text("BUFFER LATENCY TUNING", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf(512 to "1.3ms", 1024 to "2.7ms", 2048 to "5.3ms", 4096 to "10.6ms").forEach { (bytes, latency) ->
                                        val isSelected = state.bufferMode == com.thesis.bitperfectusb.domain.model.BufferMode.MANUAL && state.manualBufferSizeBytes == bytes
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setBufferMode(com.thesis.bitperfectusb.domain.model.BufferMode.MANUAL)
                                                viewModel.setManualBufferSize(bytes)
                                            },
                                            label = { Text("$bytes B\n($latency)", fontFamily = TelemetryFontFamily, fontSize = 7.sp, textAlign = TextAlign.Center) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SignalTeal.copy(alpha = 0.2f), selectedLabelColor = SignalTeal),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                var hwVol by remember { mutableStateOf(state.hardwareVolumePercent) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("DAC HARDWARE MASTER VOLUME", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                                    Text("${(hwVol * 100).toInt()}% (UAC FEATURE UNIT)", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = hwVol,
                                    onValueChange = {
                                        hwVol = it
                                        viewModel.setHardwareVolume(it)
                                    },
                                    colors = SliderDefaults.colors(thumbColor = HifiGold, activeTrackColor = HifiGold)
                                )
                            }
                        }
                    }

                    // ── Poweramp Vertical Chain ──
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // ── 1. Library Node ──
                        PowerampChainNode(
                            icon = Icons.Filled.LibraryMusic,
                            iconBg = Color.White,
                            iconTint = Color.Black,
                            title = "Library",
                            isLast = false
                        ) {
                            Text(
                                "All Songs — ${state.queuePosition} / ${state.queueTotalTracks}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (currentTrack != null) {
                                Text(
                                    currentTrack.title,
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // ── 2. Track Node ──
                        val trackFormat = currentTrack?.format?.name ?: "FLAC"
                        val channels = currentTrack?.pcm?.channels ?: 2
                        val sampleRate = currentTrack?.pcm?.sampleRateHz ?: 48000
                        val bitDepth = currentTrack?.pcm?.bitDepth ?: 24
                        val bitrateKbps = (sampleRate.toLong() * bitDepth * channels / 1000).toInt()

                        PowerampChainNode(
                            icon = Icons.Filled.MusicNote,
                            iconBg = Color.White,
                            iconTint = Color.Black,
                            title = "Track",
                            isLast = false
                        ) {
                            Text(
                                "$trackFormat $channels Channels $bitrateKbps kbps",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                "${sampleRate / 1000} kHz",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                "$bitDepth bit",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                "Gapless",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showHardwareSettings = !showHardwareSettings }
                                    .padding(vertical = 2.dp)
                            )
                        }

                        // ── 3. Decoder Node ──
                        val decoderName = when (currentTrack?.format) {
                            com.thesis.bitperfectusb.domain.model.AudioFileFormat.FLAC ->
                                "BitPerfect Built-in FLAC Decoder"
                            com.thesis.bitperfectusb.domain.model.AudioFileFormat.WAV ->
                                "BitPerfect Built-in PCM/WAV Decoder"
                            else -> "BitPerfect High-Res Audio Decoder"
                        }
                        val isResampling = !isBitPerfect && (sampleRate > 48000 || bitDepth > 16)

                        PowerampChainNode(
                            icon = Icons.Filled.Memory,
                            iconBg = Color(0xFF225533),
                            iconTint = Color(0xFF44E088),
                            title = "Decoder",
                            isLast = false
                        ) {
                            Text(
                                decoderName,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (isResampling) "Resampled to 48 kHz (Android AudioFlinger Mixer)" else "No Resampling (Bit-Exact Passthrough)",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = if (isResampling) WarnAmber else Color(0xFF44E088)
                            )
                        }

                        // ── 4. DSP Node ──
                        PowerampChainNode(
                            icon = Icons.Filled.Tune,
                            iconBg = Color.White,
                            iconTint = Color.Black,
                            title = "DSP",
                            isLast = false
                        ) {
                            Text(
                                "${sampleRate / 1000} kHz",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                if (bitDepth >= 24) "Float64/Float32 (Active)" else "Int16",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        showAudioInfoModal = false
                                        showDspModal = true
                                    }
                                    .padding(vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Equalizer",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Equalizer Mode: • ",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    if (state.eqEnabled) "parametric (10 bands)" else "parametric (bypassed)",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = if (state.eqEnabled) SignalTeal else Color.White,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            showAudioInfoModal = false
                                            showEqModal = true
                                        }
                                        .padding(vertical = 2.dp)
                                )
                            }
                            Text(
                                "Balance (0%)",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tone",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                "Bass 90. Q: 0.80 (0%)",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            if (state.crossfeedEnabled) {
                                Text(
                                    "Crossfeed (${(state.crossfeedStrength * 100).toInt()}%)",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 12.sp,
                                    color = SignalTeal
                                )
                            }
                        }

                        // ── 5. Output Node (Auto-Detected with explicit kHz and bit output) ──
                        val isUsbDac = isBitPerfect && dac != null
                        PowerampChainNode(
                            icon = if (isUsbDac) Icons.Filled.Usb else Icons.Filled.Headphones,
                            iconBg = if (isUsbDac) SignalTeal else Color.White,
                            iconTint = Color.Black,
                            title = "Output",
                            isLast = false
                        ) {
                            if (isUsbDac && dac != null) {
                                Text(
                                    dac.productName,
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignalTeal
                                )
                                Text(
                                    "${sampleRate / 1000} kHz",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    "$bitDepth bit",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    "USB Audio Direct (Bit-Perfect Passthrough)",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    "UAC ${if (dac.isUac2) "2.0" else "1.0"} • VID: 0x%04X PID: 0x%04X".format(dac.vendorId, dac.productId),
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            } else {
                                Text(
                                    "Android AudioTrack Mixer",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarnAmber
                                )
                                Text(
                                    "48 kHz",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    "16 bit",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    "AudioFlinger → HAL → Internal DAC",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Route: ${state.simulatedRoute.label} (OS Resampled/Mixed)",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Engine Switcher
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF16181C), RoundedCornerShape(8.dp))
                                    .border(BorderStroke(1.dp, Color(0xFF2C3038)), RoundedCornerShape(8.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                EngineSwitchButton(
                                    label = "⚡ USB DIRECT",
                                    selected = isBitPerfect,
                                    onClick = { viewModel.selectEngine(EngineType.CUSTOM_USB_DIRECT) },
                                    selectedColor = SignalTeal,
                                    modifier = Modifier.weight(1f)
                                )
                                EngineSwitchButton(
                                    label = "🎛️ ANDROID MIXER",
                                    selected = !isBitPerfect,
                                    onClick = { viewModel.selectEngine(EngineType.ANDROID_AUDIOTRACK) },
                                    selectedColor = WarnAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // ── 6. Signal Integrity & Clock Timing Node ──
                        PowerampChainNode(
                            icon = Icons.Filled.CheckCircle,
                            iconBg = if (isBitPerfect) Color(0xFF225533) else Color(0xFF443311),
                            iconTint = if (isBitPerfect) Color(0xFF44E088) else WarnAmber,
                            title = "Signal Integrity & Clock Sync",
                            isLast = false
                        ) {
                            Text(
                                if (isBitPerfect) "100% Bit-Exact Match (0 Alterations)" else "OS AudioFlinger Resampled / Mixed",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBitPerfect) Color(0xFF44E088) else WarnAmber
                            )
                            val drScore = state.playback.liveDrScore
                            val drRating = state.playback.drRating
                            Text(
                                "Dynamic Range: DR$drScore • $drRating",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (drScore >= 14) Color(0xFF44E088) else if (drScore >= 10) SignalTeal else WarnAmber
                            )
                            if (sampleRate >= 176400 || currentTrack?.filePath?.endsWith(".dsf", ignoreCase = true) == true || currentTrack?.filePath?.endsWith(".dff", ignoreCase = true) == true) {
                                Text(
                                    "DSD / DoP: ${if (sampleRate >= 352800) "DSD128 (DoP 352.8 kHz / 24-bit)" else "DSD64 (DoP 176.4 kHz / 24-bit)"} Active (0x05/0xFA Markers)",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 11.sp,
                                    color = SignalTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "Clock: ${if (isBitPerfect && (dac?.hasAsyncFeedbackEndpoint == true)) "Async Isochronous Feedback (Master Clock)" else if (isBitPerfect) "Adaptive Isochronous PLL Lock" else "Internal Android Audio Clock"}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                            Text(
                                "Jitter: ${if (isBitPerfect) "< 1 ps (Hardware Clock Locked)" else "High Software Jitter"}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = if (isBitPerfect) SignalTeal else WarnAmber
                            )
                            Text(
                                "Integrity Score: ${state.videlityScore}% Confidence",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = if (state.videlityScore >= 95) SignalTeal else WarnAmber
                            )
                        }

                        // ── 7. Bit-Activity LED Matrix & Telemetry Node ──
                        PowerampChainNode(
                            icon = Icons.Filled.Speed,
                            iconBg = Color.White,
                            iconTint = Color.Black,
                            title = "Bit-Activity & Performance",
                            isLast = true
                        ) {
                            Text(
                                "Buffer: ${state.playback.bufferSizeBytes}B • Latency: ${"%.1f".format(state.playback.liveLatencyMs)} ms",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Text(
                                "Stream Rate: ${"%.1f".format(state.playback.transferRateBytesPerSec / 1024.0)} KB/s • Drops: ${state.playback.stability?.dropouts ?: 0}",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 24-Bit Hardware Activity LED Matrix
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF16181C)),
                                border = BorderStroke(1.dp, Color(0xFF2C3038)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "24-BIT BIT-ACTIVITY MATRIX",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SignalTeal,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            if (isBitPerfect) "ACTIVE STREAM" else "SIMULATED",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 7.sp,
                                            color = Color.White.copy(alpha = 0.5f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    BitActivityLedGrid(activeBitMask = state.playback.verification?.activeBitMask ?: 0x00FFFFFF)
                                }
                            }

                            // Optional Buffer & Hardware Volume Tuning
                            if (isBitPerfect) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "BUFFER LATENCY TUNING",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    color = Color.White.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf(512 to "1.3ms", 1024 to "2.7ms", 2048 to "5.3ms", 4096 to "10.6ms").forEach { (bytes, latency) ->
                                        val isSelected = state.bufferMode == com.thesis.bitperfectusb.domain.model.BufferMode.MANUAL && state.manualBufferSizeBytes == bytes
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                viewModel.setBufferMode(com.thesis.bitperfectusb.domain.model.BufferMode.MANUAL)
                                                viewModel.setManualBufferSize(bytes)
                                            },
                                            label = { Text("$bytes B\n($latency)", fontFamily = TelemetryFontFamily, fontSize = 7.sp, textAlign = TextAlign.Center) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SignalTeal.copy(alpha = 0.2f), selectedLabelColor = SignalTeal),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                var hwVol by remember { mutableStateOf(state.hardwareVolumePercent) }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("DAC HARDWARE MASTER VOLUME", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                                    Text("${(hwVol * 100).toInt()}% (UAC FEATURE UNIT)", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = hwVol,
                                    onValueChange = {
                                        hwVol = it
                                        viewModel.setHardwareVolume(it)
                                    },
                                    colors = SliderDefaults.colors(thumbColor = HifiGold, activeTrackColor = HifiGold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 5: AUDIOPHILE TOOLS & UTILITIES HUB (Repacked Modal) ──
    if (showToolsModal) {
        Dialog(
            onDismissRequest = { showToolsModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "AUDIOPHILE TOOLS",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SignalTeal,
                                letterSpacing = 1.sp
                            )
                        }
                        IconButton(onClick = { showToolsModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Text(
                        "Essential audiophile utilities, queue management, and listening aids.",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = TextSecondary
                    )

                    HorizontalDivider(color = OutlineSubtle)

                    // Card 1: Up Next Queue
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showToolsModal = false
                                showQueueModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                        border = BorderStroke(1.dp, if (state.playbackQueue.hasQueue) HifiGold.copy(alpha = 0.6f) else OutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = HifiGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("UP NEXT QUEUE", fontFamily = TelemetryFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("${state.playbackQueue.tracks.size} tracks in playlist sequence", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary)
                                }
                            }
                            Text("VIEW ❯", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                        }
                    }

                    // Card 2: Sleep Timer
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showToolsModal = false
                                showSleepTimerModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                        border = BorderStroke(1.dp, if (state.sleepTimer.isEnabled) SignalTeal.copy(alpha = 0.6f) else OutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Bedtime, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("SLEEP TIMER", fontFamily = TelemetryFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        if (state.sleepTimer.isEnabled) "Active: ${state.sleepTimer.remainingSeconds / 60}m ${state.sleepTimer.remainingSeconds % 60}s remaining"
                                        else "Soft fade-out auto-pause",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 9.sp,
                                        color = if (state.sleepTimer.isEnabled) SignalTeal else TextSecondary
                                    )
                                }
                            }
                            Text("SET ❯", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                        }
                    }

                    // Card 3: A-B Repeat Looper
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showToolsModal = false
                                showAbLoopModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                        border = BorderStroke(1.dp, if (state.abLoop.isEnabled) SignalTeal.copy(alpha = 0.6f) else OutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Loop, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("A-B REPEAT LOOPER", fontFamily = TelemetryFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        if (state.abLoop.isEnabled) "Loop active between point A & B"
                                        else "Loop audio passages for critical analysis",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 9.sp,
                                        color = if (state.abLoop.isEnabled) SignalTeal else TextSecondary
                                    )
                                }
                            }
                            Text("OPEN ❯", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                        }
                    }

                    // Card 4: Live VU Meters
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showToolsModal = false
                                showVuModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                        border = BorderStroke(1.dp, OutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Speed, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("ANALOG VU METERS", fontFamily = TelemetryFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Dual analog needle & peak telemetry", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary)
                                }
                            }
                            Text("VIEW ❯", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                    }

                    // Card 5: AI Karaoke & Vocal Studio
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showToolsModal = false
                                showKaraokeStudioModal = true
                            },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                        border = BorderStroke(1.dp, if (state.aiState.karaokeModeEnabled) SignalTeal.copy(alpha = 0.6f) else OutlineSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Mic, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("AI KARAOKE STUDIO", fontFamily = TelemetryFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(
                                        if (state.aiState.karaokeModeEnabled) "Active: ${state.aiState.karaokeModeType.label}"
                                        else "Instrumental removal, acapella & key transposer",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 9.sp,
                                        color = if (state.aiState.karaokeModeEnabled) SignalTeal else TextSecondary
                                    )
                                }
                            }
                            Text("OPEN ❯", fontFamily = TelemetryFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 6: SLEEP TIMER MODAL ──
    if (showSleepTimerModal) {
        Dialog(
            onDismissRequest = { showSleepTimerModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bedtime, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SLEEP TIMER",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SignalTeal,
                                letterSpacing = 1.sp
                            )
                        }
                        IconButton(onClick = { showSleepTimerModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Text(
                        "Includes graceful 15-second soft volume ramp-down to protect sensitive DAC clocks & headphones from abrupt transient clicks.",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = TextSecondary
                    )

                    if (state.sleepTimer.isEnabled) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SignalTeal.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("ACTIVE COUNTDOWN", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = SignalTeal)
                                    Text(
                                        "${state.sleepTimer.remainingSeconds / 60}m ${"%02d".format(state.sleepTimer.remainingSeconds % 60)}s remaining",
                                        fontFamily = TelemetryFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelSleepTimer() },
                                    border = BorderStroke(1.dp, ErrorCoral.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("CANCEL", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = ErrorCoral)
                                }
                            }
                        }
                    }

                    Text("SELECT DURATION", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary, letterSpacing = 1.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = state.sleepTimer.isEnabled && !state.sleepTimer.isEndOfTrack && state.sleepTimer.totalSeconds == mins * 60,
                                onClick = {
                                    viewModel.setSleepTimer(mins)
                                    showSleepTimerModal = false
                                },
                                label = { Text("${mins}m", fontFamily = TelemetryFontFamily, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SignalTeal.copy(alpha = 0.2f), selectedLabelColor = SignalTeal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(90, 120).forEach { mins ->
                            FilterChip(
                                selected = state.sleepTimer.isEnabled && !state.sleepTimer.isEndOfTrack && state.sleepTimer.totalSeconds == mins * 60,
                                onClick = {
                                    viewModel.setSleepTimer(mins)
                                    showSleepTimerModal = false
                                },
                                label = { Text("${mins}m", fontFamily = TelemetryFontFamily, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SignalTeal.copy(alpha = 0.2f), selectedLabelColor = SignalTeal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FilterChip(
                            selected = state.sleepTimer.isEnabled && state.sleepTimer.isEndOfTrack,
                            onClick = {
                                viewModel.setSleepTimer(0, isEndOfTrack = true)
                                showSleepTimerModal = false
                            },
                            label = { Text("END OF TRACK", fontFamily = TelemetryFontFamily, fontSize = 8.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SignalTeal.copy(alpha = 0.2f), selectedLabelColor = SignalTeal),
                            modifier = Modifier.weight(2f)
                        )
                    }
                }
            }
        }
    }

    // ── MODAL 6: UP NEXT QUEUE MODAL ──
    if (showQueueModal) {
        val queue = state.playbackQueue
        Dialog(
            onDismissRequest = { showQueueModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.80f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = HifiGold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("UP NEXT QUEUE", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HifiGold)
                                Text("${queue.tracks.size} tracks loaded • ${if (queue.shuffleEnabled) "Shuffle On" else "Sequential"}", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary)
                            }
                        }
                        IconButton(onClick = { showQueueModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineSubtle)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (queue.tracks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Queue is empty. Select a song from Library.", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(queue.tracks) { index, track ->
                                val isCurrent = queue.playPosition == index
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.playQueueTrack(track)
                                            showQueueModal = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrent) SignalTeal.copy(alpha = 0.15f) else BackgroundCharcoal
                                    ),
                                    border = BorderStroke(1.dp, if (isCurrent) SignalTeal.copy(alpha = 0.5f) else OutlineSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCurrent) SignalTeal else TextSecondary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                track.title,
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrent) SignalTeal else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                "${track.artist ?: "Unknown"} • ${track.format.name} ${track.pcm.bitDepth}b/${track.pcm.sampleRateHz / 1000}k",
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = 9.sp,
                                                color = TextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            formatPlaybackTime(track.durationMs),
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 7: A-B REPEAT LOOPER MODAL ──
    if (showAbLoopModal) {
        val loop = state.abLoop
        val durationMs = (state.playback.currentTrack?.durationMs ?: 1L).coerceAtLeast(1L)
        Dialog(
            onDismissRequest = { showAbLoopModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .wrapContentHeight()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Loop, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("A-B REPEAT LOOPER", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SignalTeal, letterSpacing = 1.sp)
                        }
                        IconButton(onClick = { showAbLoopModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Text("Set Point A and Point B timestamps to repeat a passage for critical listening and audio inspection.", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                            border = BorderStroke(1.dp, if (loop.pointAMs != null) SignalTeal else OutlineSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("POINT A", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                                Text(loop.pointAMs?.let { formatPlaybackTime(it) } ?: "--:--", fontFamily = TelemetryFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                            border = BorderStroke(1.dp, if (loop.pointBMs != null) HifiGold else OutlineSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("POINT B", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                                Text(loop.pointBMs?.let { formatPlaybackTime(it) } ?: "--:--", fontFamily = TelemetryFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.setLoopPointA() },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, SignalTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SET A (${formatPlaybackTime(state.playback.positionMs)})", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = SignalTeal)
                        }
                        OutlinedButton(
                            onClick = { viewModel.setLoopPointB() },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, HifiGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SET B (${formatPlaybackTime(state.playback.positionMs)})", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                        }
                    }

                    if (loop.isEnabled || loop.pointAMs != null || loop.pointBMs != null) {
                        OutlinedButton(
                            onClick = { viewModel.clearAbLoop() },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, ErrorCoral.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLEAR A-B LOOP", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = ErrorCoral)
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 8: FOOBAR2000-STYLE LYRICS PROVIDER & WEB SCRAPER SEARCH MODAL ──
    if (showLyricsSourceModal) {
        val currentTrack = state.playback.currentTrack
        var searchQuery by remember(currentTrack?.id) {
            mutableStateOf(listOfNotNull(currentTrack?.title, currentTrack?.artist).joinToString(" "))
        }

        Dialog(
            onDismissRequest = { showLyricsSourceModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                            Icon(Icons.Filled.Public, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "LYRICS SOURCES & SCRAPER",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SignalTeal,
                                letterSpacing = 1.sp
                            )
                        }
                        IconButton(onClick = { showLyricsSourceModal = false }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Text(
                        "Auto-fetch & search synchronized lyrics across free online databases (Foobar2000 style).",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 8.sp,
                        color = TextSecondary
                    )

                    // ── Provider Selector Chips ──
                    Text("DEFAULT PROVIDER", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = HifiGold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            com.thesis.bitperfectusb.domain.model.LyricsProvider.AUTO to "Auto",
                            com.thesis.bitperfectusb.domain.model.LyricsProvider.LRCLIB to "LRCLIB",
                            com.thesis.bitperfectusb.domain.model.LyricsProvider.NETEASE to "163 Music",
                            com.thesis.bitperfectusb.domain.model.LyricsProvider.KUGOU to "Kugou"
                        ).forEach { (prov, label) ->
                            val isSelected = state.lyricsProvider == prov
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setLyricsProvider(prov) },
                                label = { Text(label, fontFamily = TelemetryFontFamily, fontSize = 8.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SignalTeal.copy(alpha = 0.2f),
                                    selectedLabelColor = SignalTeal
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // ── Search Online Bar ──
                    Text("SEARCH ONLINE MATCHES", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Track and artist name...", fontFamily = TelemetryFontFamily, fontSize = 10.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SignalTeal,
                                unfocusedBorderColor = OutlineSubtle
                            )
                        )
                        Button(
                            onClick = { viewModel.searchOnlineLyrics(searchQuery) },
                            enabled = !state.isSearchingLyrics && searchQuery.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(52.dp)
                        ) {
                            if (state.isSearchingLyrics) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundCharcoal, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = BackgroundCharcoal)
                            }
                        }
                    }

                    // ── Search Results List ──
                    if (state.onlineSearchResults.isNotEmpty()) {
                        Text(
                            "FOUND ${state.onlineSearchResults.size} RELEASES (TAP TO APPLY):",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = HifiGold
                        )
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.onlineSearchResults) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectLyricsResult(item)
                                            showLyricsSourceModal = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                                    border = BorderStroke(1.dp, OutlineSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, fontFamily = TelemetryFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("${item.artist} ${item.album?.let { "• $it" } ?: ""}", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                if (item.isSynced) "SYNCED" else "PLAIN",
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.isSynced) SignalTeal else WarnAmber
                                            )
                                            Text(
                                                item.provider.displayName,
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = 7.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // Quick Actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.reloadLyricsOnline()
                                showLyricsSourceModal = false
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, SignalTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RELOAD AUTO", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = SignalTeal)
                        }
                        OutlinedButton(
                            onClick = {
                                lrcPickerLauncher.launch("*/*")
                                showLyricsSourceModal = false
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, HifiGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("IMPORT .LRC", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.deleteCustomLyrics()
                                showLyricsSourceModal = false
                            },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, ErrorCoral),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CLEAR", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = ErrorCoral)
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 9: FOOBAR2000-STYLE AUTOEQ UNIVERSAL HEADPHONE & IEM DATABASE MODAL ──
    if (showAutoEqModal) {
        var autoEqSearchQuery by remember { mutableStateOf("") }
        var selectedBrandFilter by remember { mutableStateOf("All") }
        var onlineResults by remember { mutableStateOf<List<AutoEqProfile>>(emptyList()) }
        var isSearchingOnline by remember { mutableStateOf(false) }
        var isSyncingAll by remember { mutableStateOf(false) }
        var syncedProfiles by remember { mutableStateOf<List<AutoEqProfile>>(emptyList()) }
        val coroutineScope = rememberCoroutineScope()

        val allProfiles = remember(syncedProfiles) {
            (AutoEqRepository.getAllProfiles() + syncedProfiles).distinctBy { "${it.brand} ${it.model}".lowercase() }
        }

        // Live AutoEQ Online Search / Scraper Effect
        LaunchedEffect(autoEqSearchQuery) {
            if (autoEqSearchQuery.isNotBlank() && autoEqSearchQuery.length >= 2) {
                isSearchingOnline = true
                delay(300) // Debounce search input
                onlineResults = AutoEqOnlineScraper.searchOnline(autoEqSearchQuery)
                isSearchingOnline = false
            } else {
                onlineResults = emptyList()
                isSearchingOnline = false
            }
        }

        val filteredProfiles = remember(autoEqSearchQuery, selectedBrandFilter, onlineResults, allProfiles) {
            val localMatches = allProfiles.filter { profile ->
                val matchesBrand = when (selectedBrandFilter) {
                    "All" -> true
                    "IEM" -> profile.type == "IEM"
                    "Over-Ear" -> profile.type == "Over-Ear"
                    "KZ / CCA" -> profile.brand.contains("KZ", ignoreCase = true) || profile.brand.contains("CCA", ignoreCase = true)
                    "Kinera / Celest" -> profile.brand.contains("Kinera", ignoreCase = true) || profile.brand.contains("Celest", ignoreCase = true)
                    "BLON / QKZ" -> profile.brand.contains("BLON", ignoreCase = true) || profile.brand.contains("QKZ", ignoreCase = true)
                    "Campfire / TOTL" -> profile.brand.contains("Campfire", ignoreCase = true) || profile.brand.contains("Unique Melody", ignoreCase = true) || profile.brand.contains("64 Audio", ignoreCase = true) || profile.brand.contains("Empire Ears", ignoreCase = true) || profile.brand.contains("Vision Ears", ignoreCase = true) || profile.brand.contains("Etymotic", ignoreCase = true)
                    else -> profile.brand.equals(selectedBrandFilter, ignoreCase = true)
                }

                val matchesSearch = if (autoEqSearchQuery.isBlank()) true
                else "${profile.brand} ${profile.model} ${profile.type}".contains(autoEqSearchQuery.trim(), ignoreCase = true)

                matchesBrand && matchesSearch
            }

            // Merge local database with online scraped results
            (localMatches + onlineResults).distinctBy { "${it.brand} ${it.model}".lowercase() }
        }

        Dialog(
            onDismissRequest = { showAutoEqModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.88f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = SurfaceCharcoal,
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "AUTOEQ IEM & HEADPHONE DATABASE",
                                    fontFamily = TelemetryFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SignalTeal
                                )
                                Text(
                                    "Harman Target & IEF Neutral Acoustic Calibration",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 7.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Scrape / Sync Button
                            Button(
                                onClick = {
                                    if (!isSyncingAll) {
                                        coroutineScope.launch {
                                            isSyncingAll = true
                                            syncedProfiles = AutoEqOnlineScraper.syncAllNewProducts()
                                            isSyncingAll = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSyncingAll) SignalTeal.copy(alpha = 0.2f) else BackgroundCharcoal),
                                border = BorderStroke(1.dp, if (isSyncingAll) SignalTeal else OutlineSubtle),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                if (isSyncingAll) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = SignalTeal, strokeWidth = 1.5.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SYNCING...", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = SignalTeal)
                                } else {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SYNC ONLINE", fontFamily = TelemetryFontFamily, fontSize = 7.5.sp, color = SignalTeal)
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(onClick = { showAutoEqModal = false }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // Search input
                    OutlinedTextField(
                        value = autoEqSearchQuery,
                        onValueChange = { autoEqSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search IEM / Headphone (e.g. Tanchjim, Aria, Zero, Kato...)", fontFamily = TelemetryFontFamily, fontSize = 8.5.sp, color = TextSecondary) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(16.dp)) },
                        trailingIcon = {
                            if (autoEqSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { autoEqSearchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalTeal,
                            unfocusedBorderColor = OutlineSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Audiophile Brand & Type Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "All", "Tanchjim", "Moondrop", "Tangzu", "7Hz", "Truthear", "Kiwi Ears",
                            "Simgot", "AFUL", "KZ / CCA", "Letshuoer", "Dunu", "Thieaudio", "Final Audio",
                            "SeeAudio", "Kinera / Celest", "Hidizs", "EPZ", "TinHiFi", "BLON / QKZ",
                            "FiiO", "Campfire / TOTL", "Sennheiser", "Sony", "HiFiMAN",
                            "Beyerdynamic", "Audio-Technica", "Apple", "IEM", "Over-Ear"
                        ).forEach { filter ->
                            val isSelected = selectedBrandFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBrandFilter = filter },
                                label = { Text(filter, fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SignalTeal.copy(alpha = 0.2f),
                                    selectedLabelColor = SignalTeal
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = OutlineSubtle,
                                    selectedBorderColor = SignalTeal.copy(alpha = 0.6f),
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    // Results Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "FOUND ${filteredProfiles.size} CALIBRATED PROFILES:",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = HifiGold
                        )
                        if (isSearchingOnline) {
                            Text(
                                "FETCHING ONLINE...",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 7.5.sp,
                                color = SignalTeal
                            )
                        }
                    }

                    // Results list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredProfiles) { profile ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateEqGains(profile.gains)
                                        if (!state.eqEnabled) viewModel.toggleEq()
                                        showAutoEqModal = false
                                    },
                                colors = CardDefaults.cardColors(containerColor = BackgroundCharcoal),
                                border = BorderStroke(1.dp, OutlineSubtle),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(profile.displayName, fontFamily = TelemetryFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("${profile.type} • Target: ${profile.targetCurve}", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = TextSecondary)
                                        }
                                        Text(
                                            "APPLY ❯",
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SignalTeal
                                        )
                                    }

                                    // Mini EQ Curve preview bar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(18.dp)
                                            .background(Color(0xFF181A1F), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        profile.gains.forEach { gain ->
                                            val barColor = if (gain > 0) SignalTeal else if (gain < 0) HifiGold else TextSecondary
                                            Text(
                                                "${if (gain > 0) "+" else ""}${gain.toInt()}dB",
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = 6.sp,
                                                color = barColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── MODAL 8: FOOBAR2000 ABX BLIND LISTENING TEST DIALOG ──
    if (showAbxModal) {
        AbxTestDialog(
            onDismiss = { showAbxModal = false },
            onPlaySampleA = {
                // Sample A: 100% Bit-Perfect Direct
                viewModel.selectEngine(EngineType.CUSTOM_USB_DIRECT)
            },
            onPlaySampleB = {
                // Sample B: Android AudioTrack Mixer
                viewModel.selectEngine(EngineType.ANDROID_AUDIOTRACK)
            }
        )
    }

    // ── MODAL 9: ON-DEVICE TENSORFLOW LITE AI NEURAL AUDIO STUDIO ──
    if (showAiStudioModal) {
        AiNeuralStudioDialog(
            aiState = state.aiState,
            onDismiss = { showAiStudioModal = false },
            onToggleAutoPilot = { viewModel.toggleAiAutoPilot(it) },
            onToggleDsee = { viewModel.toggleAiDsee(it) },
            onToggleVocalSuppression = { viewModel.toggleAiVocalSuppression(it) },
            onApplySuggestedEq = { gains ->
                viewModel.applyAiSuggestedEq(gains)
                if (!state.eqEnabled) viewModel.toggleEq()
                showAiStudioModal = false
            }
        )
    }

    // ── MODAL 10: AI KARAOKE & VOCAL ISOLATOR STUDIO CONSOLE ──
    if (showKaraokeStudioModal) {
        val context = androidx.compose.ui.platform.LocalContext.current
        KaraokeStudioDialog(
            aiState = state.aiState,
            scoreState = state.karaokeScore,
            onDismiss = { showKaraokeStudioModal = false },
            onSetKaraokeMode = { viewModel.setKaraokeModeType(it) },
            onSetStrength = { viewModel.setKaraokeStrength(it) },
            onSetKeyShift = { viewModel.setKaraokeKeyShift(it) },
            onToggleBassPreservation = { viewModel.toggleKaraokeBassPreservation() },
            onToggleMicScoring = { viewModel.toggleKaraokeMicScoring(context, it) },
            onResetScore = { viewModel.resetKaraokeScore() },
            isInstrumentalCached = viewModel.isStemCached(com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY),
            isAcapellaCached = viewModel.isStemCached(com.thesis.bitperfectusb.domain.model.KaraokeModeType.VOCAL_ISOLATION),
            cacheSizeBytes = viewModel.getStemCacheSizeBytes(),
            stemProgress = stemProgress,
            onExtractInstrumental = { viewModel.extractInstrumentalStem(com.thesis.bitperfectusb.domain.model.KaraokeModeType.INSTRUMENTAL_ONLY) },
            onExtractAcapella = { viewModel.extractInstrumentalStem(com.thesis.bitperfectusb.domain.model.KaraokeModeType.VOCAL_ISOLATION) },
            onClearStemCache = { viewModel.clearStemCache() },
            onDismissStemProgress = { viewModel.dismissStemExtractionProgress() }
        )
    }
}

@Composable
private fun SynchronizedLyricsView(
    lyrics: com.thesis.bitperfectusb.domain.model.LyricsData,
    activeLyricIndex: Int,
    activeProvider: com.thesis.bitperfectusb.domain.model.LyricsProvider,
    onSeekTo: (Long) -> Unit,
    onOpenSourceModal: () -> Unit,
    onImportLrc: () -> Unit,
    onDeleteCustomLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var romajiMode by remember { mutableStateOf(RomajiDisplayMode.DUAL) }

    Surface(
        modifier = modifier,
        color = Color(0xFF14161A),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFF2C3038))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Header Toolbar inside Karaoke View ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Source badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E2128))
                        .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(6.dp))
                        .clickable(onClick = onOpenSourceModal)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        lyrics.sourceDescription.ifBlank { "Source: ${activeProvider.displayName}" },
                        fontFamily = TelemetryFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(" ❯", fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = SignalTeal)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Center-Right: Romaji Transliteration Mode Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (romajiMode != RomajiDisplayMode.OFF) HifiGold.copy(alpha = 0.15f) else Color(0xFF1E2128))
                            .border(BorderStroke(1.dp, if (romajiMode != RomajiDisplayMode.OFF) HifiGold.copy(alpha = 0.6f) else OutlineSubtle), RoundedCornerShape(6.dp))
                            .clickable {
                                romajiMode = when (romajiMode) {
                                    RomajiDisplayMode.DUAL -> RomajiDisplayMode.ROMAJI_ONLY
                                    RomajiDisplayMode.ROMAJI_ONLY -> RomajiDisplayMode.OFF
                                    RomajiDisplayMode.OFF -> RomajiDisplayMode.DUAL
                                }
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A/あ", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (romajiMode != RomajiDisplayMode.OFF) HifiGold else TextSecondary)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            when (romajiMode) {
                                RomajiDisplayMode.DUAL -> "ROMAJI: DUAL"
                                RomajiDisplayMode.ROMAJI_ONLY -> "ROMAJI: ONLY"
                                RomajiDisplayMode.OFF -> "ROMAJI: OFF"
                            },
                            fontFamily = TelemetryFontFamily,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (romajiMode != RomajiDisplayMode.OFF) HifiGold else TextSecondary
                        )
                    }

                    // Right: Search button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SignalTeal.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, SignalTeal.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenSourceModal)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SEARCH", fontFamily = TelemetryFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SignalTeal)
                    }
                }
            }

            HorizontalDivider(color = OutlineSubtle.copy(alpha = 0.5f))

            if (lyrics.lines.isEmpty() && lyrics.plainText.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Lyrics,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No Synchronized Lyrics Found",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Search online databases (LRCLIB, Netease, Kugou) or import an .lrc file.",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenSourceModal,
                            colors = ButtonDefaults.buttonColors(containerColor = SignalTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "SEARCH WEB LYRICS",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BackgroundCharcoal
                            )
                        }
                        OutlinedButton(
                            onClick = onImportLrc,
                            border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "IMPORT .LRC",
                                fontFamily = TelemetryFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = HifiGold
                            )
                        }
                    }
                }
            } else if (lyrics.isSynchronized) {
                val listState = rememberLazyListState()

                LaunchedEffect(activeLyricIndex) {
                    if (activeLyricIndex >= 0 && activeLyricIndex < lyrics.lines.size) {
                        listState.animateScrollToItem((activeLyricIndex - 2).coerceAtLeast(0))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(lyrics.lines) { index, line ->
                            val isActive = index == activeLyricIndex
                            val rawText = line.text
                            val hasNonRomaji = remember(rawText) { LyricsRomanizer.hasNonRomaji(rawText) }
                            val romajiText = remember(rawText, hasNonRomaji) {
                                if (hasNonRomaji) LyricsRomanizer.romanize(rawText) else null
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSeekTo(line.timestampMs) }
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                when (romajiMode) {
                                    RomajiDisplayMode.DUAL -> {
                                        Text(
                                            text = rawText,
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = if (isActive) 16.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive) SignalTeal else Color.White.copy(alpha = 0.45f),
                                            textAlign = TextAlign.Center
                                        )
                                        if (!romajiText.isNullOrBlank() && romajiText != rawText) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = romajiText,
                                                fontFamily = TelemetryFontFamily,
                                                fontSize = if (isActive) 11.sp else 9.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                color = if (isActive) HifiGold else Color.White.copy(alpha = 0.35f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    RomajiDisplayMode.ROMAJI_ONLY -> {
                                        Text(
                                            text = romajiText ?: rawText,
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = if (isActive) 16.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive) SignalTeal else Color.White.copy(alpha = 0.45f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    RomajiDisplayMode.OFF -> {
                                        Text(
                                            text = rawText,
                                            fontFamily = TelemetryFontFamily,
                                            fontSize = if (isActive) 16.sp else 13.sp,
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isActive) SignalTeal else Color.White.copy(alpha = 0.45f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Unsynchronized plain text
                val plainRaw = lyrics.plainText ?: ""
                val plainDisplay = remember(plainRaw, romajiMode) {
                    when (romajiMode) {
                        RomajiDisplayMode.ROMAJI_ONLY -> LyricsRomanizer.romanize(plainRaw)
                        RomajiDisplayMode.DUAL -> {
                            plainRaw.lines().joinToString("\n") { line ->
                                if (LyricsRomanizer.hasNonRomaji(line)) {
                                    "$line\n(${LyricsRomanizer.romanize(line)})\n"
                                } else {
                                    line
                                }
                            }
                        }
                        RomajiDisplayMode.OFF -> plainRaw
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = plainDisplay,
                        fontFamily = TelemetryFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AudiophileToolButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (active) activeColor.copy(alpha = 0.12f) else SurfaceRaised
        ),
        border = BorderStroke(
            1.dp,
            if (active) activeColor.copy(alpha = 0.5f) else OutlineSubtle
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = TelemetryFontFamily,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) activeColor else TextSecondary,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun PowerampChainNode(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    isLast: Boolean = false,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        // Left column: Circular Icon and continuous vertical line with arrow down
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "↓",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(34.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Right column: Title and details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 8.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontFamily = TelemetryFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            content()
        }
    }
}

@Composable
private fun PowerampSmallBadge(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = TelemetryFontFamily,
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.35f)), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun DspToggleRow(title: String, subtitle: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = TelemetryFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextPrimary
            )
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = HifiGold,
                checkedTrackColor = HifiGold.copy(alpha = 0.15f),
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = OutlineSubtle
            )
        )
    }
}

@Composable
private fun SpecBadge(text: String) {
    Text(
        text = text,
        fontFamily = TelemetryFontFamily,
        fontSize = 9.sp,
        color = SignalTeal,
        letterSpacing = 0.5.sp,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .background(SignalTeal.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

@Composable
private fun LiveFrequencyVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32
) {
    val infiniteTransition = rememberInfiniteTransition(label = "freq_viz")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceRaised)
    ) {
        val barWidth = size.width / (barCount * 1.5f)
        val gap = barWidth * 0.5f

        for (i in 0 until barCount) {
            val normalizedHeight = if (isPlaying) {
                val freq1 = sin(phase + i * 0.4f) * 0.3f + 0.5f
                val freq2 = sin(phase * 1.7f + i * 0.25f) * 0.2f
                val freq3 = sin(phase * 0.6f + i * 0.6f) * 0.15f
                (freq1 + freq2 + freq3).coerceIn(0.05f, 1f)
            } else {
                0.03f
            }

            val barHeight = normalizedHeight * size.height * 0.85f
            val x = i * (barWidth + gap) + gap
            val y = size.height - barHeight

            drawRoundRect(
                color = SignalTeal.copy(alpha = normalizedHeight * 0.15f),
                topLeft = Offset(x - 1f, y - 1f),
                size = Size(barWidth + 2f, barHeight + 2f),
                cornerRadius = CornerRadius(2f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(SignalTeal, SignalTealDim),
                    startY = y,
                    endY = y + barHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f)
            )
        }
    }
}

@Composable
private fun AnalogVuMeter(
    amplitude: Float,
    channelLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(95.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF04060A))
            .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(6.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(McIntoshBlue.copy(alpha = 0.22f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )

            val centerX = width / 2f
            val centerY = height - 8f
            val radius = height * 0.9f

            for (i in 0..12) {
                val angleDeg = -145f + (i * 9f)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val tickStart = radius - 8f
                val tickEnd = radius + 2f

                val startX = centerX + (tickStart * cos(angleRad)).toFloat()
                val startY = centerY + (tickStart * sin(angleRad)).toFloat()
                val endX = centerX + (tickEnd * cos(angleRad)).toFloat()
                val endY = centerY + (tickEnd * sin(angleRad)).toFloat()

                val tickColor = when {
                    angleDeg > -70f -> ErrorCoral
                    angleDeg > -90f -> WarnAmber
                    else -> TextSecondary.copy(alpha = 0.6f)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (i % 3 == 0) 3f else 1.5f
                )
            }

            val currentAngleDeg = -145f + (amplitude * 90f)
            val needleRad = Math.toRadians(currentAngleDeg.toDouble())
            val needleLength = radius - 5f

            val endX = centerX + (needleLength * cos(needleRad)).toFloat()
            val endY = centerY + (needleLength * sin(needleRad)).toFloat()

            drawLine(
                color = Color.Black.copy(alpha = 0.5f),
                start = Offset(centerX + 2f, centerY + 2f),
                end = Offset(endX + 2f, endY + 2f),
                strokeWidth = 2.5f
            )

            drawLine(
                color = if (currentAngleDeg > -70f) ErrorCoral else HifiGold,
                start = Offset(centerX, centerY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )

            drawCircle(color = SurfaceRaised, radius = 10f, center = Offset(centerX, centerY))
            drawCircle(color = HifiGold, radius = 4f, center = Offset(centerX, centerY))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 12.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = channelLabel,
                fontFamily = TelemetryFontFamily,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun EngineSwitchButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) selectedColor.copy(alpha = 0.5f) else Color.Transparent
                ),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = TelemetryFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = if (selected) selectedColor else TextSecondary,
            letterSpacing = 0.5.sp
        )
    }
}
