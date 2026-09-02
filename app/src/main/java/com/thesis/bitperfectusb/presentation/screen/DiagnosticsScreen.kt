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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thesis.bitperfectusb.domain.model.AnovaResult
import com.thesis.bitperfectusb.domain.model.EngineComparison
import com.thesis.bitperfectusb.domain.model.EngineType
import com.thesis.bitperfectusb.domain.model.ExperimentRun
import com.thesis.bitperfectusb.domain.model.ExperimentType
import com.thesis.bitperfectusb.domain.model.TrafficLogEntry
import com.thesis.bitperfectusb.domain.model.TrafficStatus
import com.thesis.bitperfectusb.domain.model.TrendPoint
import com.thesis.bitperfectusb.domain.model.WelchTTestResult
import com.thesis.bitperfectusb.presentation.components.IntegrityGauge
import com.thesis.bitperfectusb.presentation.components.StatCard
import com.thesis.bitperfectusb.presentation.components.charts.BarChart
import com.thesis.bitperfectusb.presentation.components.charts.BarEntry
import com.thesis.bitperfectusb.presentation.components.charts.LineChart
import com.thesis.bitperfectusb.presentation.theme.BackgroundCharcoal
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceCharcoal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextPrimary
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.WarnAmber
import com.thesis.bitperfectusb.presentation.viewmodel.AnalyticsViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.ExperimentViewModel
import com.thesis.bitperfectusb.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

enum class DiagnosticsTab {
    LAB_SWEEPS, SESSION_HISTORY, TRAFFIC_LOG
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    experimentViewModel: ExperimentViewModel = koinViewModel(),
    analyticsViewModel: AnalyticsViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel()
) {
    var activeTab by remember { mutableStateOf(DiagnosticsTab.LAB_SWEEPS) }
    val context = LocalContext.current

    // Trigger initial analytics fetch
    LaunchedEffect(activeTab) {
        if (activeTab == DiagnosticsTab.SESSION_HISTORY) {
            analyticsViewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "DIAGNOSTICS PANEL",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                },
                actions = {
                    if (activeTab == DiagnosticsTab.SESSION_HISTORY) {
                        IconButton(onClick = { analyticsViewModel.refresh() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh history", tint = TextSecondary)
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
            // ── Custom Glassmorphic Segment Switcher ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .background(SurfaceRaised, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, OutlineSubtle), RoundedCornerShape(8.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DiagnosticsTabButton(
                    label = "LAB PROTOCOLS",
                    selected = activeTab == DiagnosticsTab.LAB_SWEEPS,
                    onClick = { activeTab = DiagnosticsTab.LAB_SWEEPS },
                    modifier = Modifier.weight(1f)
                )
                DiagnosticsTabButton(
                    label = "SESSION HISTORY",
                    selected = activeTab == DiagnosticsTab.SESSION_HISTORY,
                    onClick = { activeTab = DiagnosticsTab.SESSION_HISTORY },
                    modifier = Modifier.weight(1f)
                )
                DiagnosticsTabButton(
                    label = "TRAFFIC LOG",
                    selected = activeTab == DiagnosticsTab.TRAFFIC_LOG,
                    onClick = { activeTab = DiagnosticsTab.TRAFFIC_LOG },
                    modifier = Modifier.weight(1f)
                )
            }

            // Scrollable content area
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                when (activeTab) {
                    DiagnosticsTab.LAB_SWEEPS -> LabSweepsTab(experimentViewModel, context)
                    DiagnosticsTab.SESSION_HISTORY -> SessionHistoryTab(analyticsViewModel)
                    DiagnosticsTab.TRAFFIC_LOG -> TrafficLogTab(playerViewModel)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DiagnosticsTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) SignalTeal.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) SignalTeal.copy(alpha = 0.4f) else Color.Transparent
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
            fontSize = 11.sp,
            color = if (selected) SignalTeal else TextSecondary,
            letterSpacing = 1.sp
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  Tab 3: USB Traffic Log — live isochronous packet-by-packet view
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun TrafficLogTab(playerViewModel: PlayerViewModel) {
    val state by playerViewModel.uiState.collectAsStateWithLifecycle()
    val entries = state.playback.trafficLog

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            "USB TRAFFIC LOG",
            fontFamily = TelemetryFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.5.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "The most recent isochronous packets on the USB-direct engine, most recent first — " +
                "empty on the AudioTrack engine, since there are no USB packets to log there.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))

        when {
            state.selectedEngine != EngineType.CUSTOM_USB_DIRECT -> {
                Text(
                    "Switch to the USB-Direct engine on the Player screen to populate this log.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            entries.isEmpty() -> {
                Text(
                    if (state.playback.isPlaying) "Waiting for the first packet…" else "No traffic yet — start USB-direct playback.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            else -> {
                val dropped = entries.count { it.status == TrafficStatus.DROPPED }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(label = "Logged", value = entries.size.toString(), modifier = Modifier.weight(1f))
                    StatCard(
                        label = "Dropped",
                        value = dropped.toString(),
                        modifier = Modifier.weight(1f),
                        valueColor = if (dropped > 0) ErrorCoral else SignalTeal
                    )
                    StatCard(label = "Latest Seq", value = "#${entries.first().sequenceNumber}", modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = OutlineSubtle)
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("SEQ", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.width(50.dp))
                    Text("TIME", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.width(60.dp))
                    Text("JITTER", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.width(65.dp))
                    Text("SIZE", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                    Text("STATUS", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary.copy(alpha = 0.6f))
                }
                Spacer(Modifier.height(4.dp))

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    entries.forEach { entry -> TrafficLogRow(entry) }
                }
            }
        }
    }
}

@Composable
private fun TrafficLogRow(entry: TrafficLogEntry) {
    val ok = entry.status == TrafficStatus.OK
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "#${entry.sequenceNumber}",
            fontFamily = TelemetryFontFamily,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.width(50.dp)
        )
        Text(
            "%.3fs".format(entry.elapsedMs / 1000.0),
            fontFamily = TelemetryFontFamily,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.width(60.dp)
        )
        Text(
            "${entry.jitterUs}µs",
            fontFamily = TelemetryFontFamily,
            fontSize = 10.sp,
            color = SignalTeal,
            modifier = Modifier.width(65.dp)
        )
        Text(
            "${entry.sizeBytes}B",
            fontFamily = TelemetryFontFamily,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (ok) "OK" else "DROPPED",
            fontFamily = TelemetryFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = if (ok) SignalTeal else ErrorCoral
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  Tab 1: Lab sweeps & Live Experiments (Merged)
// ────────────────────────────────────────────────────────────────────────────
@Composable
private fun LabSweepsTab(viewModel: ExperimentViewModel, context: android.content.Context) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Text(
        "Runs real-time hardware measurements on this phone. Values below are captured live — not hardcoded.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        modifier = Modifier.padding(vertical = 6.dp)
    )

    Text(
        "SELECT PROTOCOL TEST",
        fontFamily = TelemetryFontFamily,
        fontSize = 9.sp,
        letterSpacing = 1.5.sp,
        color = TextSecondary,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ExperimentType.entries.forEach { type ->
            ExperimentProtocolButton(
                type = type,
                isRunning = state.isRunning,
                onClick = { viewModel.runExperiment(type) }
            )
        }
    }

    if (state.isRunning) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SignalTeal.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SignalTeal)
                Column {
                    Text(
                        "RUNNING TEST SWEEP...",
                        fontFamily = TelemetryFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SignalTeal
                    )
                    state.progressMessage?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }

    state.error?.let {
        Text(it, color = ErrorCoral, fontFamily = TelemetryFontFamily, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
    }

    state.lastReport?.let { report ->
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = OutlineSubtle)

        Text(
            "VERIFIED RESULTS — ${experimentLabel(report.experimentType).uppercase()}",
            fontFamily = TelemetryFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = SignalTeal,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                BarChart(
                    title = "Latency A/B Comparison (ms)",
                    entries = report.runs.map {
                        BarEntry(it.configLabel.removePrefix("exp_"), it.meanLatencyMs)
                    }
                )
            }
        }

        report.welchLatencyResult?.let { WelchResultCard("Latency Jitter", it) }
        report.welchCpuResult?.let { WelchResultCard("CPU Overhead", it) }
        report.anovaResult?.let { AnovaResultCard(it) }

        Text(
            "COMPILATION RUN LOG",
            fontFamily = TelemetryFontFamily,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            report.runs.forEach { run -> ExperimentRunRow(run) }
        }

        OutlinedButton(
            onClick = { viewModel.exportCurrentReport(context) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            border = BorderStroke(1.dp, SignalTeal.copy(alpha = 0.3f))
        ) {
            Text("EXPORT DIAGNOSTICS CSV", fontFamily = TelemetryFontFamily, fontSize = 11.sp, color = SignalTeal, letterSpacing = 1.sp)
        }
        state.exportedFilePath?.let {
            Text("EXPORT DIRECTORY: $it", fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = SignalTeal, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
//  Tab 2: Historical Telemetry trends (Merged)
// ────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionHistoryTab(viewModel: AnalyticsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SignalTeal)
        }
        return
    }

    state.error?.let {
        Text(it, color = ErrorCoral, fontFamily = TelemetryFontFamily, fontSize = 11.sp, modifier = Modifier.padding(vertical = 8.dp))
    }

    val summary = state.summary
    if (summary == null || summary.totalSessions == 0) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NO SESSIONS DETECTED",
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = TextSecondary
                )
                Text(
                    "Trigger normal playback in the Player screen first to compile session history metrics.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary.copy(alpha = 0.6f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, OutlineSubtle)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IntegrityGauge(score = summary.avgIntegrityScoreOverall.toInt(), diameter = 100.dp)
            Column {
                Text(
                    "HISTORICAL QUALITY INDEX",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 9.sp,
                    letterSpacing = 1.5.sp,
                    color = TextSecondary
                )
                Text(
                    "${"%.1f".format(summary.bitPerfectSessionPercentage)}% bit-perfect runs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                summary.checksumVerifiedBitPerfectPercentage?.let { pct ->
                    Text(
                        "${"%.1f".format(pct)}% hardware CRC checks verified",
                        fontFamily = TelemetryFontFamily,
                        fontSize = 10.sp,
                        color = if (pct >= 99.9) SignalTeal else WarnAmber,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }

    FlowRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard("Total Runs", "${summary.totalSessions}")
        StatCard("Total Duration", "%.0f min".format(summary.totalListeningTimeMs / 60000.0))
        summary.mostStableBufferSizeBytes?.let { StatCard("Stable Buf", "${it} B") }
    }

    if (summary.insights.isNotEmpty()) {
        Text(
            "COMPUTED ANOMALY INSIGHTS",
            fontFamily = TelemetryFontFamily,
            fontSize = 9.sp,
            letterSpacing = 1.5.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(
                modifier = Modifier
                    .background(BackgroundCharcoal.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                summary.insights.forEachIndexed { idx, insight ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("[%03d]".format(idx + 1), fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = SignalTeal.copy(alpha = 0.6f))
                        Text(insight, fontFamily = TelemetryFontFamily, fontSize = 9.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    if (summary.integrityScoreTrend.size >= 2) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                LineChart(title = "Integrity Score Trend", points = summary.integrityScoreTrend)
            }
        }
    }

    if (summary.latencyTrend.size >= 2) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                LineChart(title = "Jitter Latency Trend (ms)", points = summary.latencyTrend, lineColor = WarnAmber)
            }
        }
    }

    if (summary.engineComparison.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
            border = BorderStroke(1.dp, OutlineSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                BarChart(
                    title = "Latency mean by Output Driver (ms)",
                    entries = summary.engineComparison.map {
                        BarEntry(
                            when (it.engineType) {
                                EngineType.ANDROID_AUDIOTRACK -> "AudioTrack"
                                EngineType.CUSTOM_USB_DIRECT -> "USB-Direct"
                            },
                            it.avgLatencyMs
                        )
                    }
                )
            }
        }
    }
}

// Helper components
@Composable
private fun ExperimentProtocolButton(type: ExperimentType, isRunning: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isRunning,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceRaised,
            disabledContainerColor = SurfaceRaised.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, OutlineSubtle),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = experimentLabel(type).uppercase(),
                    fontFamily = TelemetryFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = if (isRunning) TextSecondary else TextPrimary
                )
                Text(
                    text = experimentDescription(type),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Text(
                "TEST RUN ▶",
                fontFamily = TelemetryFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = if (isRunning) TextSecondary else SignalTeal
            )
        }
    }
}

@Composable
private fun ExperimentRunRow(run: ExperimentRun) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
        border = BorderStroke(1.dp, OutlineSubtle),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(run.configLabel, fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TelemetryCell("n", "${run.sampleSize}")
                TelemetryCell("latency", "${"%.1f".format(run.meanLatencyMs)}±${"%.1f".format(run.sdLatencyMs)}ms")
                TelemetryCell("cpu", "${"%.1f".format(run.meanCpuPercent)}%")
                TelemetryCell("integrity", "${run.integrityScore}/100")
            }
        }
    }
}

@Composable
private fun TelemetryCell(label: String, value: String) {
    Column {
        Text(label.uppercase(), fontFamily = TelemetryFontFamily, fontSize = 7.sp, color = TextSecondary, letterSpacing = 0.8.sp)
        Text(value, fontFamily = TelemetryFontFamily, fontSize = 10.sp, color = TextPrimary)
    }
}

@Composable
private fun WelchResultCard(metricLabel: String, result: WelchTTestResult) {
    val isSignificant = result.significant
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSignificant) SignalTeal.copy(alpha = 0.05f) else SurfaceRaised),
        border = BorderStroke(1.dp, if (isSignificant) SignalTeal.copy(alpha = 0.25f) else OutlineSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("WELCH T-TEST — $metricLabel", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                Text(
                    text = if (isSignificant) "H₀ REJECTED" else "H₀ ACCEPTED",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSignificant) SignalTeal else WarnAmber,
                    modifier = Modifier
                        .background(if (isSignificant) SignalTeal.copy(alpha = 0.1f) else WarnAmber.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TelemetryCell("t-stat", "%.3f".format(result.tStatistic))
                TelemetryCell("df", "%.1f".format(result.degreesOfFreedom))
                TelemetryCell("p-value", if (result.pValue < 0.0001) "<0.0001" else "%.4f".format(result.pValue))
            }
        }
    }
}

@Composable
private fun AnovaResultCard(result: AnovaResult) {
    val isSignificant = result.significant
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSignificant) SignalTeal.copy(alpha = 0.05f) else SurfaceRaised),
        border = BorderStroke(1.dp, if (isSignificant) SignalTeal.copy(alpha = 0.25f) else OutlineSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ONE-WAY ANOVA", fontFamily = TelemetryFontFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextPrimary)
                Text(
                    text = if (isSignificant) "H₀ REJECTED" else "H₀ ACCEPTED",
                    fontFamily = TelemetryFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSignificant) SignalTeal else WarnAmber,
                    modifier = Modifier
                        .background(if (isSignificant) SignalTeal.copy(alpha = 0.1f) else WarnAmber.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TelemetryCell("F-ratio", "%.3f".format(result.fStatistic))
                TelemetryCell("dfBetween", "${result.dfBetween}")
                TelemetryCell("dfWithin", "${result.dfWithin}")
                TelemetryCell("p-value", if (result.pValue < 0.0001) "<0.0001" else "%.4f".format(result.pValue))
            }
        }
    }
}

private fun experimentLabel(type: ExperimentType): String = when (type) {
    ExperimentType.EXPERIMENT_A_ARCHITECTURE -> "Architecture Latency Sweep"
    ExperimentType.EXPERIMENT_B_SAMPLE_RATE -> "Sample Rate Throughput test"
    ExperimentType.EXPERIMENT_C_DAC_HARDWARE -> "DAC Endpoint Consistency validation"
    ExperimentType.EXPERIMENT_D_BUFFER_SIZE -> "Isochronous Buffer Size Sweep"
}

private fun experimentDescription(type: ExperimentType): String = when (type) {
    ExperimentType.EXPERIMENT_A_ARCHITECTURE -> "Custom JNI USB direct vs AudioTrack"
    ExperimentType.EXPERIMENT_B_SAMPLE_RATE -> "THD/Jitter throughput load at 44.1-192 kHz"
    ExperimentType.EXPERIMENT_C_DAC_HARDWARE -> "Bit integrity variance across hardware layers"
    ExperimentType.EXPERIMENT_D_BUFFER_SIZE -> "Buffer packets optimization threshold sweep"
}
