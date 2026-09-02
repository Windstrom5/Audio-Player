package com.thesis.bitperfectusb.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesis.bitperfectusb.domain.model.AudioTrackModel
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.presentation.components.TrackArtwork
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.viewmodel.LibrarySortOption
import com.thesis.bitperfectusb.presentation.viewmodel.LibraryTab
import com.thesis.bitperfectusb.presentation.viewmodel.LibraryViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.PlayerViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.SortDirection
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlayer: () -> Unit = {},
    viewModel: LibraryViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val displayName = DocumentFile.fromTreeUri(context, uri)?.name ?: uri.lastPathSegment ?: "Folder"
            viewModel.addFolder(uri, displayName)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "MUSIC LIBRARY",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = HifiGold
                            )
                            if (state.tracks.isNotEmpty()) {
                                Text(
                                    "  [${state.tracks.size} TRACKS]",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleGridView() }) {
                            Icon(
                                imageVector = if (state.isGridView) Icons.Filled.ViewList else Icons.Filled.GridView,
                                contentDescription = "Toggle View Mode",
                                tint = HifiGold
                            )
                        }
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Library Settings", tint = TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundCharcoal,
                        titleContentColor = TextPrimary
                    )
                )

                // Poweramp / UAPP Style Category Tab Row
                TabRow(
                    selectedTabIndex = state.activeTab.ordinal,
                    containerColor = BackgroundCharcoal,
                    contentColor = HifiGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[state.activeTab.ordinal]),
                            color = HifiGold,
                            height = 2.dp
                        )
                    }
                ) {
                    LibraryTab.entries.forEach { tab ->
                        Tab(
                            selected = state.activeTab == tab,
                            onClick = { viewModel.setActiveTab(tab) },
                            text = {
                                Text(
                                    text = when(tab) {
                                        LibraryTab.TRACKS -> "TRACKS"
                                        LibraryTab.ALBUMS -> "ALBUMS"
                                        LibraryTab.ARTISTS -> "ARTISTS"
                                        LibraryTab.FOLDERS -> "FOLDERS"
                                        LibraryTab.HIRES_DSD -> "HI-RES/DSD"
                                    },
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (state.activeTab == tab) HifiGold else TextSecondary
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = BackgroundCharcoal,
        bottomBar = {
            // Poweramp Style Persistent Mini Player Bar
            playerState.playback.currentTrack?.let { track ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable(onClick = onNavigateToPlayer),
                    colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                    border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackArtwork(
                            uriString = track.filePath,
                            title = track.title,
                            artist = track.artist,
                            isPlaying = playerState.playback.isPlaying,
                            isLarge = false,
                            cornerRadius = 8.dp,
                            modifier = Modifier.size(44.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                fontFamily = TelemetryFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = track.artist ?: "Unknown Artist",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (playerState.selectedEngine == com.thesis.bitperfectusb.domain.model.EngineType.CUSTOM_USB_DIRECT) "BIT-PERFECT" else "AUDIOTRACK",
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (playerState.selectedEngine == com.thesis.bitperfectusb.domain.model.EngineType.CUSTOM_USB_DIRECT) SignalTeal else HifiGold,
                                    modifier = Modifier
                                        .background(
                                            if (playerState.selectedEngine == com.thesis.bitperfectusb.domain.model.EngineType.CUSTOM_USB_DIRECT) SignalTeal.copy(alpha = 0.15f) else HifiGold.copy(alpha = 0.15f),
                                            RoundedCornerShape(3.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (playerState.playback.isPlaying) playerViewModel.pause() else playerViewModel.resume()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (playerState.playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = HifiGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(
                                onClick = { playerViewModel.nextTrack() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SkipNext,
                                    contentDescription = "Next",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = {
                    Text(
                        "SEARCH TITLE, ARTIST, OR FREQUENCY...",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = HifiGold,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = TelemetryFontFamily,
                    color = TextPrimary
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HifiGold,
                    unfocusedBorderColor = OutlineSubtle,
                    focusedContainerColor = SurfaceRaised,
                    unfocusedContainerColor = SurfaceRaised
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Sleek single-line horizontal sort row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dirText = if (state.sortDirection == SortDirection.ASCENDING) "ASC" else "DESC"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceRaised)
                        .border(BorderStroke(1.dp, HifiGold.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                        .clickable { viewModel.toggleSortDirection() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = "Toggle Sort Direction",
                        modifier = Modifier.size(13.dp),
                        tint = HifiGold
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = dirText,
                        fontFamily = TelemetryFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = HifiGold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LibrarySortOption.entries) { option ->
                        val selected = state.sortBy == option
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setSortBy(option) },
                            label = {
                                Text(
                                    text = sortOptionLabel(option),
                                    fontFamily = TelemetryFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HifiGold.copy(alpha = 0.2f),
                                selectedLabelColor = HifiGold
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = OutlineSubtle,
                                selectedBorderColor = HifiGold.copy(alpha = 0.5f),
                                enabled = true,
                                selected = selected
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            if (state.isScanning) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = HifiGold)
                    Text("SCANNING MEDIA LIBRARY…", fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = HifiGold, letterSpacing = 1.sp)
                }
            }

            state.error?.let {
                Text(it, color = ErrorCoral, fontFamily = TelemetryFontFamily, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }

            if (state.tracks.isEmpty() && !state.isScanning) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NO MATCHING AUDIO FILES FOUND", fontFamily = TelemetryFontFamily, fontSize = 12.sp, color = TextSecondary, letterSpacing = 1.5.sp)
                    Text("Tap Settings to add watched storage directories.", fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                }
            } else {
                if (state.isGridView) {
                    // 2-Column Album Cover Grid View
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.tracks) { track ->
                            TrackGridCard(
                                track = track,
                                isPlaying = track.id == state.nowPlayingTrackId,
                                onPlay = { viewModel.playTrack(track) },
                                onShowMenu = { viewModel.setSelectedTrackForMenu(track) }
                            )
                        }
                    }
                } else {
                    // Standard Sleek List View
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(state.tracks) { track ->
                            TrackRow(
                                track = track,
                                isPlaying = track.id == state.nowPlayingTrackId,
                                onPlay = { viewModel.playTrack(track) },
                                onShowMenu = { viewModel.setSelectedTrackForMenu(track) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Poweramp Style Detailed Audio Tech Specs Dialog
    state.selectedTrackForSpecs?.let { track ->
        AudioTechSpecsDialog(
            track = track,
            onDismiss = { viewModel.setSelectedTrackForSpecs(null) }
        )
    }

    // Track Context Menu Sheet
    state.selectedTrackForMenu?.let { track ->
        TrackContextMenuDialog(
            track = track,
            onPlayDirectUsb = {
                viewModel.playTrackDirectUsb(track)
                viewModel.setSelectedTrackForMenu(null)
            },
            onPlayAudioTrack = {
                viewModel.playTrackAudioTrack(track)
                viewModel.setSelectedTrackForMenu(null)
            },
            onShowSpecs = {
                viewModel.setSelectedTrackForSpecs(track)
                viewModel.setSelectedTrackForMenu(null)
            },
            onDismiss = { viewModel.setSelectedTrackForMenu(null) }
        )
    }

    // Media Index Config Settings Dialog
    if (showSettingsDialog) {
        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MEDIA INDEX CONFIG",
                            fontFamily = TelemetryFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 2.sp,
                            color = TextPrimary
                        )
                        IconButton(onClick = { showSettingsDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "WATCHED STORAGE DIRECTORIES",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 9.sp,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                        OutlinedButton(
                            onClick = { folderPicker.launch(null) },
                            border = BorderStroke(1.dp, HifiGold.copy(alpha = 0.4f)),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(Icons.Filled.CreateNewFolder, contentDescription = null, tint = HifiGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ADD FOLDER", fontFamily = TelemetryFontFamily, fontSize = 8.sp, color = HifiGold)
                        }
                    }

                    if (state.watchedFolders.isEmpty()) {
                        Text(
                            "Using standard MediaStore index. Add custom folders to scan SAF directories.",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 10.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.watchedFolders.forEach { folder ->
                                WatchedFolderRow(folder = folder, onRemove = { viewModel.removeFolder(folder.id) })
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineSubtle)

                    Button(
                        onClick = {
                            viewModel.rescan()
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HifiGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, tint = BackgroundCharcoal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FORCE RE-SCAN MEDIA LIBRARY", fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = BackgroundCharcoal, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: AudioTrackModel,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShowMenu: () -> Unit
) {
    val isHighRes = track.pcm.sampleRateHz >= 96000 || track.pcm.bitDepth >= 24
    val accentColor = if (isHighRes) HifiGold else SignalTeal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, if (isPlaying) accentColor else OutlineSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Cover Art Thumbnail
            TrackArtwork(
                uriString = track.filePath,
                title = track.title,
                artist = track.artist,
                isPlaying = isPlaying,
                isLarge = false,
                cornerRadius = 8.dp,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPlaying) accentColor else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isHighRes) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HI-RES",
                            fontFamily = TelemetryFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = HifiGold,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier
                                .background(HifiGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(BorderStroke(0.5.dp, HifiGold.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = track.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpecPill(track.format.name, accentColor)
                    SpecPill("${"%.1f".format(track.pcm.sampleRateHz / 1000.0)}kHz", TextSecondary)
                    SpecPill("${track.pcm.bitDepth}-bit", TextSecondary)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onShowMenu) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun TrackGridCard(
    track: AudioTrackModel,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onShowMenu: () -> Unit
) {
    val isHighRes = track.pcm.sampleRateHz >= 96000 || track.pcm.bitDepth >= 24
    val accentColor = if (isHighRes) HifiGold else SignalTeal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, if (isPlaying) accentColor else OutlineSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                TrackArtwork(
                    uriString = track.filePath,
                    title = track.title,
                    artist = track.artist,
                    isPlaying = isPlaying,
                    isLarge = false,
                    cornerRadius = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(SurfaceCharcoal.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                ) {
                    IconButton(onClick = onShowMenu, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = null, tint = TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) accentColor else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SpecPill(track.format.name, accentColor)
                SpecPill("${track.pcm.bitDepth}b/${"%.0f".format(track.pcm.sampleRateHz / 1000.0)}k", TextSecondary)
            }
        }
    }
}

// Poweramp / UAPP Style Audio File Tech Specs Dialog
@Composable
private fun AudioTechSpecsDialog(
    track: AudioTrackModel,
    onDismiss: () -> Unit
) {
    val bytesPerSec = track.pcm.sampleRateHz * track.pcm.channels * (track.pcm.bitDepth / 8)
    val bitrateKbps = (bytesPerSec * 8) / 1000

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, HifiGold)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "AUDIO FILE TECH SPECS",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp,
                        color = HifiGold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                HorizontalDivider(color = OutlineSubtle)

                Text(track.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                track.artist?.let { Text("Artist: $it", style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TechSpecRow("Container Format", track.format.name)
                    TechSpecRow("Native Sample Rate", "${track.pcm.sampleRateHz} Hz (${"%.1f".format(track.pcm.sampleRateHz / 1000.0)} kHz)")
                    TechSpecRow("Bit Depth", "${track.pcm.bitDepth}-bit PCM")
                    TechSpecRow("Channels", if (track.pcm.channels == 2) "Stereo (2 ch)" else "Mono (1 ch)")
                    TechSpecRow("Uncompressed Bitrate", "~$bitrateKbps kbps")
                    TechSpecRow("File Path", track.filePath)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SignalTeal.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        "✔ BIT-PERFECT VERIFIED: Native hardware support capable on UAC2 compliant DAC endpoints.",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = SignalTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun TechSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = TextSecondary)
        Text(value, fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TrackContextMenuDialog(
    track: AudioTrackModel,
    onPlayDirectUsb: () -> Unit,
    onPlayAudioTrack: () -> Unit,
    onShowSpecs: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCharcoal),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = HifiGold)
                HorizontalDivider(color = OutlineSubtle)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPlayDirectUsb)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Usb, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PLAY BIT-PERFECT (USB DIRECT)", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = SignalTeal, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPlayAudioTrack)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PLAY STANDARD (AUDIOTRACK)", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = TextPrimary)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onShowSpecs)
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = HifiGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("INSPECT FILE HEADER TECH SPECS", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = HifiGold)
                }
            }
        }
    }
}

@Composable
private fun WatchedFolderRow(folder: WatchedFolder, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Folder, contentDescription = null, tint = HifiGold, modifier = Modifier.size(18.dp))
                Text(
                    folder.displayName,
                    fontFamily = TelemetryFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SpecPill(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = TelemetryFontFamily,
        fontSize = 9.sp,
        color = color,
        letterSpacing = 0.5.sp,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

private fun sortOptionLabel(option: LibrarySortOption): String = when (option) {
    LibrarySortOption.TITLE -> "TITLE"
    LibrarySortOption.ARTIST -> "ARTIST"
    LibrarySortOption.SAMPLE_RATE -> "RATE"
    LibrarySortOption.BIT_DEPTH -> "DEPTH"
    LibrarySortOption.FORMAT -> "FORMAT"
    LibrarySortOption.DATE_ADDED -> "DATE"
    LibrarySortOption.SMART_SHUFFLE -> "SMART SHUFFLE"
}

@Composable
fun RotatingVinylRecord(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_lib")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Canvas(
        modifier = modifier
            .size(36.dp)
            .rotate(angle)
    ) {
        val radius = size.width / 2f
        drawCircle(color = Color(0xFF0F1219), radius = radius)
        drawCircle(color = HifiGold, radius = radius * 0.4f)
        drawCircle(color = Color.White.copy(alpha = 0.12f), radius = radius * 0.85f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
        drawCircle(color = Color.White.copy(alpha = 0.12f), radius = radius * 0.7f, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
        drawCircle(color = Color(0xFF04060A), radius = radius * 0.1f)
    }
}
