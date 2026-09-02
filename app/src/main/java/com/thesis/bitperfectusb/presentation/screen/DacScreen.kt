package com.thesis.bitperfectusb.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesis.bitperfectusb.domain.model.DacBenchmarkResult
import com.thesis.bitperfectusb.domain.model.DacProfile
import com.thesis.bitperfectusb.domain.model.UsbStreamingOption
import com.thesis.bitperfectusb.presentation.components.AccentBar
import com.thesis.bitperfectusb.presentation.components.SpecChip
import com.thesis.bitperfectusb.presentation.components.StatCard
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.McIntoshBlue
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.viewmodel.DacUiState
import com.thesis.bitperfectusb.presentation.viewmodel.DacViewModel
import com.thesis.bitperfectusb.usb.DescriptorTreeBuilder
import org.koin.androidx.compose.koinViewModel

/**
 * DAC Capability Scanner — the missing UI for the descriptor-parsing/capability-analysis
 * backend that already existed (DacCapabilityAnalyzer, UsbDescriptorParser, DacViewModel)
 * but had no screen consuming it. Maps to the thesis roadmap's Phase 1 (USB detection),
 * Phase 2 (descriptor-derived data — surfaced here, not re-parsed), and Phase 3
 * (capability scanner grid).
 *
 * Deliberately honest about what this analyzer does and doesn't detect: it walks PCM
 * Format Type descriptors only, so DSD/DoP support is never shown as a checkmark here
 * even though the roadmap sketch included it — claiming DSD support without a DSD-aware
 * parser behind it would be exactly the kind of unverified claim this app's "Bit-Perfect"
 * badge is supposed to avoid making elsewhere.
 */
private val CanonicalPcmSampleRatesHz = listOf(44100, 48000, 88200, 96000, 176400, 192000, 352800, 384000)
private val CanonicalBitDepths = listOf(16, 24, 32)

private enum class DacTab { CAPABILITIES, DESCRIPTORS, BENCHMARK }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DacScreen(viewModel: DacViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf(DacTab.CAPABILITIES) }

    // Auto-fetch the descriptor tree once when switching to that tab, if a device is
    // attached and nothing's been fetched yet — matches the pattern DiagnosticsScreen
    // uses for its Session History tab (LaunchedEffect(activeTab) { ...refresh()... }).
    LaunchedEffect(activeTab, state.attachedDevice) {
        if (activeTab == DacTab.DESCRIPTORS &&
            state.attachedDevice != null &&
            state.descriptorTree == null &&
            !state.isLoadingDescriptorTree
        ) {
            viewModel.fetchDescriptorTree()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DAC CAPABILITY SCANNER",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    if (activeTab == DacTab.DESCRIPTORS) {
                        IconButton(onClick = { viewModel.fetchDescriptorTree() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Re-read descriptors", tint = TextSecondary)
                        }
                    } else {
                        IconButton(onClick = { viewModel.rescan() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Rescan USB devices", tint = TextSecondary)
                        }
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .background(SurfaceRaised, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DacTabButton(
                    label = "CAPABILITIES",
                    selected = activeTab == DacTab.CAPABILITIES,
                    onClick = { activeTab = DacTab.CAPABILITIES },
                    modifier = Modifier.weight(1f)
                )
                DacTabButton(
                    label = "DESCRIPTORS",
                    selected = activeTab == DacTab.DESCRIPTORS,
                    onClick = { activeTab = DacTab.DESCRIPTORS },
                    modifier = Modifier.weight(1f)
                )
                DacTabButton(
                    label = "BENCHMARK",
                    selected = activeTab == DacTab.BENCHMARK,
                    onClick = { activeTab = DacTab.BENCHMARK },
                    modifier = Modifier.weight(1f)
                )
            }

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                when (activeTab) {
                    DacTab.CAPABILITIES -> CapabilitiesTab(state, viewModel)
                    DacTab.DESCRIPTORS -> DescriptorsTab(state, viewModel)
                    DacTab.BENCHMARK -> BenchmarkTab(state, viewModel)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DacTabButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) SignalTeal.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                BorderStroke(1.dp, if (selected) SignalTeal.copy(alpha = 0.4f) else Color.Transparent),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = TelemetryFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (selected) SignalTeal else TextSecondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CapabilitiesTab(state: DacUiState, viewModel: DacViewModel) {
    Spacer(Modifier.height(2.dp))
    DeviceSection(state, viewModel)

    state.error?.let { message ->
        Spacer(Modifier.height(16.dp))
        ErrorBanner(message)
    }

    val profile = state.activeProfile
    if (profile != null) {
        Spacer(Modifier.height(20.dp))
        CapabilitySection(profile)
        Spacer(Modifier.height(20.dp))
        StreamingOptionsSection(profile)
    } else if (!state.isAnalyzing) {
        Spacer(Modifier.height(16.dp))
        EmptyCapabilityHint()
    }

    if (state.knownProfiles.isNotEmpty()) {
        Spacer(Modifier.height(20.dp))
        KnownProfilesSection(state.knownProfiles, state.activeProfile)
    }
}

@Composable
private fun DescriptorsTab(state: DacUiState, viewModel: DacViewModel) {
    Spacer(Modifier.height(2.dp))
    SectionLabel("USB DESCRIPTOR EXPLORER")
    Text(
        "Every descriptor this DAC reported, exactly as parsed — a Device-Manager-style " +
            "view distinct from the folded summary on the Capabilities tab.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(12.dp))

    when {
        state.attachedDevice == null -> {
            Text("No USB DAC attached.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        state.isLoadingDescriptorTree -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SignalTeal, strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Reading descriptors…", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        state.error != null && state.descriptorTree == null -> {
            ErrorBanner(state.error!!)
        }
        state.descriptorTree != null -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                border = BorderStroke(1.dp, OutlineSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(8.dp)) {
                    DescriptorTreeItem(state.descriptorTree!!, depth = 0)
                }
            }
        }
        else -> {
            Text(
                "Tap the refresh icon above to read this device's raw descriptors.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun BenchmarkTab(state: DacUiState, viewModel: DacViewModel) {
    Spacer(Modifier.height(2.dp))
    SectionLabel("DAC BENCHMARK")
    Text(
        "Live-tests every sample rate and bit depth this DAC's own descriptors declared " +
            "supporting, highest first, and reports which ones actually streamed cleanly. " +
            "A DAC can declare a rate it doesn't reliably sustain over a given cable or host " +
            "controller — this measures that instead of repeating the capability matrix. " +
            "Takes over playback while it runs (same as the Lab Protocols experiments) and " +
            "can take a minute or two for a DAC with several supported rates.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(Modifier.height(12.dp))

    when {
        state.activeProfile == null -> {
            Text(
                "Analyze this DAC's capabilities first (Capabilities tab) — the benchmark " +
                    "tests the rates that analysis found.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        else -> {
            Button(
                onClick = { viewModel.runBenchmark() },
                enabled = !state.isBenchmarking,
                colors = ButtonDefaults.buttonColors(containerColor = SignalTeal, contentColor = BackgroundCharcoal),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isBenchmarking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundCharcoal, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "RUNNING…",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                } else {
                    Text(
                        "RUN BENCHMARK",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            state.benchmarkProgress?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }

            val report = state.benchmarkReport
            if (report != null) {
                Spacer(Modifier.height(20.dp))
                val best = report.maxStable
                if (best != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SignalTeal.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "MAXIMUM STABLE",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.5.sp,
                                color = SignalTeal
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "${formatKhz(best.sampleRateHz)} kHz / ${best.bitDepth}-bit",
                                fontFamily = TelemetryFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatCard(label = "Buffer", value = "${best.bufferSizeBytes}B", modifier = Modifier.weight(1f))
                                StatCard(label = "Latency", value = "%.1f ms".format(best.latencyMs), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    ErrorBanner("Nothing tested came back clean — every declared rate saw at least one dropout.")
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "ALL RESULTS (${report.results.size})",
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.results.forEach { result -> BenchmarkResultRow(result) }
                }
            }
        }
    }
}

@Composable
private fun BenchmarkResultRow(result: DacBenchmarkResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, if (result.stable) OutlineSubtle else ErrorCoral.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${formatKhz(result.sampleRateHz)} kHz / ${result.bitDepth}-bit",
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(result.note, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
            SpecChip(
                if (result.stable) "STABLE" else "UNSTABLE",
                accent = if (result.stable) SignalTeal else ErrorCoral,
                emphasized = result.stable
            )
        }
    }
}

@Composable
private fun DescriptorTreeItem(node: DescriptorTreeBuilder.DescriptorNode, depth: Int) {
    var expanded by remember(node) { mutableStateOf(depth < 2) }
    val hasChildren = node.children.isNotEmpty()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasChildren) { expanded = !expanded }
                .padding(start = (depth * 16).dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                if (hasChildren) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    node.title,
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                if (node.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(node.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
                if (node.rawSummary.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        node.rawSummary,
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = TextSecondary.copy(alpha = 0.6f)
                    )
                }
                if (expanded && !hasChildren && node.hexBytes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        node.hexBytes,
                        fontFamily = TelemetryFontFamily,
                        fontSize = 9.sp,
                        color = SignalTeal.copy(alpha = 0.7f)
                    )
                }
            }
        }
        if (expanded && hasChildren) {
            node.children.forEach { child -> DescriptorTreeItem(child, depth = depth + 1) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontFamily = TelemetryFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
        color = TextSecondary
    )
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeviceSection(state: DacUiState, viewModel: DacViewModel) {
    SectionLabel("CONNECTED HARDWARE")

    val device = state.attachedDevice
    if (device == null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("No USB DAC attached", color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Plug in a class-compliant USB DAC and grant the permission prompt. If it doesn't " +
                        "show up automatically, tap rescan.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                AccentBar(color = SignalTeal, modifier = Modifier.height(40.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        device.productName ?: "Unknown USB Device",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        device.manufacturerName ?: "Unknown manufacturer",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpecChip("0x%04X:0x%04X".format(device.vendorId, device.productId))
                        state.activeProfile?.let { profile ->
                            SpecChip(if (profile.isUac2) "UAC2" else "UAC1", accent = HifiGold, emphasized = true)
                        }
                        val version = device.version
                        if (version != null) {
                            SpecChip("USB $version")
                        }
                    }
                }
            }
        }
    }

    if (state.allDevices.size > 1) {
        Spacer(Modifier.height(10.dp))
        Text(
            "${state.allDevices.size} USB devices attached — tap to pick the DAC",
            color = TextSecondary,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.allDevices.forEach { candidate ->
                val selected = candidate.deviceId == device?.deviceId
                DeviceChip(
                    label = candidate.productName
                        ?: "0x%04X:0x%04X".format(candidate.vendorId, candidate.productId),
                    selected = selected,
                    looksLikeAudio = viewModel.looksLikeAudioDevice(candidate),
                    onClick = { viewModel.selectDevice(candidate) }
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    Button(
        onClick = { viewModel.analyzeAttachedDevice() },
        enabled = device != null && !state.isAnalyzing,
        colors = ButtonDefaults.buttonColors(containerColor = SignalTeal, contentColor = BackgroundCharcoal),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (state.isAnalyzing) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BackgroundCharcoal, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("ANALYZING…", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        } else {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ANALYZE CAPABILITIES", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun DeviceChip(label: String, selected: Boolean, looksLikeAudio: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) SignalTeal.copy(alpha = 0.14f) else SurfaceRaised)
            .border(
                BorderStroke(1.dp, if (selected) SignalTeal else OutlineSubtle),
                RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (looksLikeAudio) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (selected) SignalTeal else HifiGold)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                fontFamily = TelemetryFontFamily,
                fontSize = 11.sp,
                color = if (selected) SignalTeal else TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CapabilitySection(profile: DacProfile) {
    SectionLabel("CAPABILITY MATRIX")

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, OutlineSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("PCM SAMPLE RATES", color = TextSecondary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CanonicalPcmSampleRatesHz.forEach { rate ->
                    RateCheckChip(label = "${formatKhz(rate)} kHz", supported = rate in profile.supportedSampleRates)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = OutlineSubtle)
            Spacer(Modifier.height(16.dp))

            Text("BIT DEPTH", color = TextSecondary, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CanonicalBitDepths.forEach { depth ->
                    RateCheckChip(label = "$depth-bit", supported = depth in profile.supportedBitDepths)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = OutlineSubtle)
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(label = "Max Channels", value = profile.maxChannels.toString(), modifier = Modifier.weight(1f))
                StatCard(
                    label = "Async Feedback",
                    value = if (profile.hasAsyncFeedbackEndpoint) "YES" else "NO",
                    modifier = Modifier.weight(1f),
                    valueColor = if (profile.hasAsyncFeedbackEndpoint) SignalTeal else TextSecondary
                )
                StatCard(label = "Streaming Modes", value = profile.streamingOptions.size.toString(), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "DSD is not detected by this analyzer — only PCM Format Type descriptors are parsed. A DAC with " +
                    "native DSD/DoP support won't show it here yet.",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun RateCheckChip(label: String, supported: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (supported) SignalTeal.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                BorderStroke(1.dp, if (supported) SignalTeal.copy(alpha = 0.5f) else OutlineSubtle),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (supported) "✓" else "—",
                fontFamily = TelemetryFontFamily,
                fontSize = 11.sp,
                color = if (supported) SignalTeal else TextSecondary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                fontFamily = TelemetryFontFamily,
                fontSize = 11.sp,
                color = if (supported) TextPrimary else TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun StreamingOptionsSection(profile: DacProfile) {
    SectionLabel("STREAMING INTERFACES (${profile.streamingOptions.size})")

    if (profile.streamingOptions.isEmpty()) {
        Text(
            "No activatable streaming altsettings were found on this device.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        profile.streamingOptions.forEach { option ->
            StreamingOptionRow(option)
        }
    }
}

@Composable
private fun StreamingOptionRow(option: UsbStreamingOption) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, OutlineSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AccentBar(color = if (option.isUac2) HifiGold else McIntoshBlue, modifier = Modifier.height(36.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "IF ${option.interfaceNumber} · ALT ${option.alternateSetting}",
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                val containerNote = if (option.containerBytes > option.bitDepth / 8) {
                    " · packed in ${option.containerBytes}B"
                } else {
                    ""
                }
                Text(
                    "${option.bitDepth}-bit · ${option.channels}ch · ${option.supportedSampleRates.size} rate(s) · " +
                        "EP 0x%02X".format(option.endpointAddress) + containerNote,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            if (option.hasFeedbackEndpoint) {
                SpecChip("FEEDBACK", accent = SignalTeal, emphasized = true)
            }
        }
    }
}

@Composable
private fun KnownProfilesSection(profiles: List<DacProfile>, active: DacProfile?) {
    SectionLabel("PREVIOUSLY ANALYZED DACS")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        profiles.forEach { p ->
            val isActive = active?.id == p.id
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) SignalTeal.copy(alpha = 0.06f) else SurfaceRaised
                ),
                border = BorderStroke(1.dp, if (isActive) SignalTeal.copy(alpha = 0.4f) else OutlineSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(p.productName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "${p.usbIdLabel} · ${if (p.isUac2) "UAC2" else "UAC1"} · " +
                                "${p.supportedBitDepths.maxOrNull() ?: 0}-bit / " +
                                "${formatKhz(p.supportedSampleRates.maxOrNull() ?: 0)}kHz max",
                            color = TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (isActive) {
                        SpecChip("ACTIVE", accent = SignalTeal, emphasized = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCapabilityHint() {
    Text(
        "Tap Analyze Capabilities once a DAC is attached to parse its USB Audio Class descriptors — sample " +
            "rates, bit depths, and every activatable streaming altsetting.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ErrorCoral.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, ErrorCoral.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            color = ErrorCoral,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
}

/** "44100" -> "44.1", "48000" -> "48" — matches the label style audiophiles expect on a spec sheet. */
private fun formatKhz(hz: Int): String {
    val khz = hz / 1000.0
    return if (khz == khz.toInt().toDouble()) "${khz.toInt()}" else "%.1f".format(khz)
}
