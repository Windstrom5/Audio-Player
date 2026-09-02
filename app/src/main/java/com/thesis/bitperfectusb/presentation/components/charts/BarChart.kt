package com.thesis.bitperfectusb.presentation.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesis.bitperfectusb.presentation.theme.GridLine
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextSecondary

data class BarEntry(val label: String, val value: Double)

@Composable
fun BarChart(
    title: String,
    entries: List<BarEntry>,
    barColor: Color = SignalTeal,
    modifier: Modifier = Modifier,
    valueFormatter: (Double) -> String = { "%.1f".format(it) }
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontFamily = TelemetryFontFamily,
            letterSpacing = 1.sp
        )
        if (entries.isEmpty()) return@Column

        val maxValue = entries.maxOf { it.value }.let { if (it <= 0.0) 1.0 else it }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(top = 8.dp)
        ) {
            val barCount = entries.size
            val gap = size.width * 0.06f / barCount
            val barWidth = (size.width - gap * (barCount + 1)) / barCount
            val chartBottom = size.height - 20.dp.toPx()

            // Horizontal grid lines
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = chartBottom * i / gridCount
                drawLine(
                    color = GridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            entries.forEachIndexed { index, entry ->
                val barHeight = (entry.value / maxValue * chartBottom).toFloat().coerceAtLeast(2f)
                val x = gap + index * (barWidth + gap)
                val y = chartBottom - barHeight

                // Glow behind bar
                drawRoundRect(
                    color = barColor.copy(alpha = 0.12f),
                    topLeft = Offset(x - 2f, y - 2f),
                    size = Size(barWidth + 4f, barHeight + 4f),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Gradient bar
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(barColor, barColor.copy(alpha = 0.5f)),
                        startY = y,
                        endY = y + barHeight
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = TelemetryFontFamily,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
