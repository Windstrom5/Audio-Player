package com.thesis.bitperfectusb.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily

/**
 * A small bordered monospace pill for a technical spec ("24-bit", "96kHz",
 * "FLAC") — reads like a value printed on a piece of hi-fi gear rather than a
 * generic Material chip.
 */
@Composable
fun SpecChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = SignalTeal,
    emphasized: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (emphasized) accent.copy(alpha = 0.14f) else Color.Transparent)
    ) {
        Text(
            text = text,
            fontFamily = TelemetryFontFamily,
            style = MaterialTheme.typography.labelSmall,
            color = if (emphasized) accent else accent.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * A thin horizontal level-meter bar (VU-meter style) — used anywhere a plain
 * number would otherwise sit alone, e.g. next to a CPU/latency readout, to
 * give it the feel of a hardware panel gauge rather than a text label.
 */
@Composable
fun LevelMeterBar(
    fraction: Float,
    modifier: Modifier = Modifier,
    color: Color = SignalTeal,
    barHeight: Dp = 4.dp,
    barWidth: Dp = 160.dp
) {
    Box(
        modifier = modifier
            .height(barHeight)
            .width(barWidth)
            .clip(RoundedCornerShape(2.dp))
            .background(OutlineSubtle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(barWidth * fraction.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

/**
 * A short vertical accent bar used as a left-edge marker on list rows — a nod
 * to channel-strip / hardware-panel edge indicators rather than a plain card border.
 */
@Composable
fun AccentBar(color: Color, modifier: Modifier = Modifier, width: Dp = 3.dp) {
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

/**
 * A 24-bit visual LED matrix widget showing live bit activity (LSB to MSB)
 * in real-time. Active bits illuminate in vibrant teal/green, proving bit depth fidelity.
 */
@Composable
fun BitActivityLedGrid(
    activeBitMask: Int,
    modifier: Modifier = Modifier,
    bitsCount: Int = 24
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (bit in (bitsCount - 1) downTo 0) {
            val isActive = ((activeBitMask ushr bit) and 1) == 1
            val ledColor = if (isActive) SignalTeal else OutlineSubtle.copy(alpha = 0.3f)
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(ledColor)
            )
        }
    }
}
