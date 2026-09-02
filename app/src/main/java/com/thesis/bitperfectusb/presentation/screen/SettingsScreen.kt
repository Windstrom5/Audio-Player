package com.thesis.bitperfectusb.presentation.screen

import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesis.bitperfectusb.domain.model.BufferMode
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.PlaybackConfig
import com.thesis.bitperfectusb.domain.model.UsbTransferStrategy
import com.thesis.bitperfectusb.domain.model.UserSettings
import com.thesis.bitperfectusb.domain.model.WatchedFolder
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.WarnAmber
import com.thesis.bitperfectusb.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            val displayName = DocumentFile.fromTreeUri(context, uri)?.name ?: uri.lastPathSegment ?: "Folder"
            viewModel.addFolder(uri, displayName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SYSTEM CONTROLS",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
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
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Playback Engine ──
            SectionHeader("PLAYBACK ENGINE")
            Text(
                "Configures which engine starts by default when tapping a track in the Library.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val directSelected = state.userSettings.defaultEngine == EngineType.CUSTOM_USB_DIRECT
                FilterChip(
                    selected = directSelected,
                    onClick = { viewModel.setDefaultEngine(EngineType.CUSTOM_USB_DIRECT) },
                    label = { Text("USB-DIRECT", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                        selectedLabelColor = SignalTeal
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                        enabled = true,
                        selected = directSelected
                    )
                )
                val atSelected = state.userSettings.defaultEngine == EngineType.ANDROID_AUDIOTRACK
                FilterChip(
                    selected = atSelected,
                    onClick = { viewModel.setDefaultEngine(EngineType.ANDROID_AUDIOTRACK) },
                    label = { Text("AUDIOTRACK", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarnAmber.copy(alpha = 0.15f),
                        selectedLabelColor = WarnAmber
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = WarnAmber.copy(alpha = 0.5f),
                        enabled = true,
                        selected = atSelected
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── Buffer Mode ──
            SectionHeader("BUFFER OPTIMIZATION")
            Text(
                "Adaptive dynamic tuning balances latency and stability. Manual locks it to a constant value.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val adaptiveSelected = state.userSettings.bufferMode == BufferMode.ADAPTIVE
                FilterChip(
                    selected = adaptiveSelected,
                    onClick = { viewModel.setBufferMode(BufferMode.ADAPTIVE) },
                    label = { Text("ADAPTIVE (AUTO)", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                        selectedLabelColor = SignalTeal
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                        enabled = true,
                        selected = adaptiveSelected
                    )
                )
                val manualSelected = state.userSettings.bufferMode == BufferMode.MANUAL
                FilterChip(
                    selected = manualSelected,
                    onClick = { viewModel.setBufferMode(BufferMode.MANUAL) },
                    label = { Text("MANUAL LOCK", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarnAmber.copy(alpha = 0.15f),
                        selectedLabelColor = WarnAmber
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = WarnAmber.copy(alpha = 0.5f),
                        enabled = true,
                        selected = manualSelected
                    )
                )
            }

            if (state.userSettings.bufferMode == BufferMode.MANUAL) {
                Text(
                    "STATIC BUFFER VALUE: ${state.userSettings.manualBufferSizeBytes} B",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BUFFER_SIZE_OPTIONS) { size ->
                        val selected = state.userSettings.manualBufferSizeBytes == size
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setManualBufferSize(size) },
                            label = { Text("${size}B", fontFamily = TelemetryFontFamily, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                                selectedLabelColor = SignalTeal
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = OutlineSubtle,
                                selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                                enabled = true,
                                selected = selected
                            )
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── Bit-Perfect Driver Strategy ──
            SectionHeader("USB DRIVER STRATEGY")
            Text(
                "Pipelined isochronous requests absorb thread scheduling jitter. Synchronous waits for each transaction.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            val pipelinedSelected = state.userSettings.usbTransferStrategy == UsbTransferStrategy.PIPELINED
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = pipelinedSelected,
                    onClick = { viewModel.setUsbTransferStrategy(UsbTransferStrategy.PIPELINED) },
                    label = { Text("PIPELINED QUEUE", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                        selectedLabelColor = SignalTeal
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                        enabled = true,
                        selected = pipelinedSelected
                    )
                )
                val syncSelected = state.userSettings.usbTransferStrategy == UsbTransferStrategy.SYNCHRONOUS
                FilterChip(
                    selected = syncSelected,
                    onClick = { viewModel.setUsbTransferStrategy(UsbTransferStrategy.SYNCHRONOUS) },
                    label = { Text("SYNCHRONOUS BLOCK", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarnAmber.copy(alpha = 0.15f),
                        selectedLabelColor = WarnAmber
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = OutlineSubtle,
                        selectedBorderColor = WarnAmber.copy(alpha = 0.5f),
                        enabled = true,
                        selected = syncSelected
                    )
                )
            }

            Text(
                text = if (pipelinedSelected) {
                    "▸ Backlog queue absorbs micro-stuttering. Recommended."
                } else {
                    "▸ Zero jitter tolerance. Vulnerable to scheduler priority inversion."
                },
                color = if (pipelinedSelected) SignalTeal else WarnAmber,
                fontFamily = TelemetryFontFamily,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── USB Buffering Depth ──
            SectionHeader("USB BUFFERING DEPTH")
            Text(
                "Number of parallel endpoints actively queued. Higher prevents dropouts but increases latency.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (depth in UserSettings.USB_REQUEST_POOL_RANGE) {
                    val selected = state.userSettings.usbRequestPoolSize == depth
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setUsbRequestPoolSize(depth) },
                        label = { Text("$depth", fontFamily = TelemetryFontFamily, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                            selectedLabelColor = SignalTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = OutlineSubtle,
                            selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selected
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── Keep Screen On ──
            SectionHeader("WAKELOCK CORE CONTROL")
            Text(
                "Keeping the screen awake prevents low-power CPU throttling and scheduling degradation.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREVENT DOZE THROTTLING",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Switch(
                    checked = state.userSettings.keepScreenOnDuringPlayback,
                    onCheckedChange = { viewModel.setKeepScreenOn(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignalTeal,
                        checkedTrackColor = SignalTeal.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = OutlineSubtle
                    )
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── Audiophile DSP & Bit-Perfect Auto-Bypass ──
            SectionHeader("AUDIOPHILE DSP & BIT-PERFECT AUTO-BYPASS")
            Text(
                "When Custom USB Direct Engine is active, Bit-Perfect Auto-Bypass automatically disables all software DSP (EQ, Crossfeed, ReplayGain) to preserve 100% bit-exact PCM stream integrity.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AUTO-BYPASS DSP IN BIT-PERFECT MODE",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Switch(
                    checked = state.userSettings.autoBypassDspInBitPerfect,
                    onCheckedChange = { viewModel.toggleAutoBypassDsp(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignalTeal,
                        checkedTrackColor = SignalTeal.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = OutlineSubtle
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "AUDIOPHILE EQ PRESET (AUDIOTRACK MODE ONLY)",
                fontFamily = TelemetryFontFamily,
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(com.thesis.bitperfectusb.domain.model.EqPreset.entries) { preset ->
                    val selected = state.userSettings.eqPreset == preset
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setEqPreset(preset) },
                        label = { Text(preset.label, fontFamily = TelemetryFontFamily, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SignalTeal.copy(alpha = 0.15f),
                            selectedLabelColor = SignalTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = OutlineSubtle,
                            selectedBorderColor = SignalTeal.copy(alpha = 0.5f),
                            enabled = true,
                            selected = selected
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)


            // ── Digital Volume ──
            SectionHeader("DIGITAL VOLTAGE ATTENUATION")
            Text(
                "Bit-perfect delivery requires exactly 0 dB (unity gain) in the digital domain. Scaling multiplies every sample, altering the payload.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            OutlinedButton(
                onClick = { normalizeSystemVolume(context) },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp),
                    tint = SignalTeal
                )
                Text(
                    "CALIBRATE SYSTEM VOLUME (0 DB)",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 11.sp,
                    color = SignalTeal,
                    letterSpacing = 1.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── Library Sources ──
            SectionHeader("INDEXING DIRECTORIES")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "WATCHED DIRECTORIES (${state.watchedFolders.size})",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                OutlinedButton(
                    onClick = { folderPicker.launch(null) },
                    enabled = !state.isBusy,
                    border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Filled.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SignalTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "ADD",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 10.sp,
                        color = SignalTeal,
                        letterSpacing = 1.sp
                    )
                }
            }
            if (state.isBusy) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp), color = SignalTeal)
            }
            state.error?.let {
                Text(it, color = ErrorCoral, fontFamily = TelemetryFontFamily, fontSize = 11.sp)
            }
            if (state.watchedFolders.isEmpty()) {
                Text(
                    "No custom directories watched. MediaStore default scanner active.",
                    color = TextSecondary.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.watchedFolders.forEach { folder ->
                        WatchedFolderCard(folder, onRemove = { viewModel.removeFolder(folder.id) })
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

            // ── About ──
            SectionHeader("SPECIFICATIONS")
            Text(
                "BitPerfect USB Audio Client — direct user-space UAC1/UAC2 Direct JNI endpoints, async feedback loop drift telemetry, and Welch/ANOVA validation instrumentation.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun WatchedFolderCard(folder: WatchedFolder, onRemove: () -> Unit) {
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
                Icon(Icons.Filled.Folder, contentDescription = null, tint = SignalTeal, modifier = Modifier.size(18.dp))
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
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontFamily = TelemetryFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = SignalTeal,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

private val BUFFER_SIZE_OPTIONS = listOf(1024, 2048, 4096, 8192, 16384, 32768)
    .also { require(it.first() >= PlaybackConfig.MIN_BUFFER_BYTES && it.last() <= PlaybackConfig.MAX_BUFFER_BYTES) }

private fun normalizeSystemVolume(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
}
