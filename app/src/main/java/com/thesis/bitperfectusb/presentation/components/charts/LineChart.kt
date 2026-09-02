package com.thesis.bitperfectusb.presentation.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesis.bitperfectusb.domain.model.TrendPoint
import com.thesis.bitperfectusb.presentation.theme.GridLine
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextSecondary

@Composable
fun LineChart(
    title: String,
    points: List<TrendPoint>,
    lineColor: Color = SignalTeal,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontFamily = TelemetryFontFamily,
            letterSpacing = 1.sp
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 8.dp)
        ) {
            if (points.size < 2) return@Canvas
            val maxValue = points.maxOf { it.value }.let { if (it <= 0.0) 1.0 else it }
            val minValue = minOf(0.0, points.minOf { it.value })
            val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
            val stepX = size.width / (points.size - 1)

            // Horizontal grid lines (oscilloscope style)
            val gridCount = 4
            for (i in 0..gridCount) {
                val y = size.height * i / gridCount
                drawLine(
                    color = GridLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Vertical grid lines
            val vertGridCount = 6
            for (i in 0..vertGridCount) {
                val x = size.width * i / vertGridCount
                drawLine(
                    color = GridLine,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            // Bottom baseline
            drawLine(OutlineSubtle, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.5f)

            // Build paths
            val linePath = Path()
            val fillPath = Path()
            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = size.height - ((point.value - minValue) / range * size.height).toFloat()
                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }
            fillPath.lineTo((points.size - 1) * stepX, size.height)
            fillPath.close()

            // Draw paths
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                    startY = 0f,
                    endY = size.height
                )
            )
            drawPath(linePath, color = lineColor.copy(alpha = 0.2f), style = Stroke(width = 8f, cap = StrokeCap.Round))
            drawPath(linePath, color = lineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

            // Data dots
            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = size.height - ((point.value - minValue) / range * size.height).toFloat()
                drawCircle(lineColor.copy(alpha = 0.3f), radius = 6f, center = Offset(x, y))
                drawCircle(lineColor, radius = 3f, center = Offset(x, y))
            }
        }
    }
}
