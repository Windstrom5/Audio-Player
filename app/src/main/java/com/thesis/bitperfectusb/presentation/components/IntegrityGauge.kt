package com.thesis.bitperfectusb.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesis.bitperfectusb.presentation.theme.ErrorCoral
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.theme.WarnAmber

@Composable
fun IntegrityGauge(score: Int, modifier: Modifier = Modifier, diameter: Dp = 120.dp) {
    val color = when {
        score >= 100 -> SignalTeal
        score >= 60 -> WarnAmber
        else -> ErrorCoral
    }
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokeWidth = size.minDimension * 0.08f
            val glowWidth = size.minDimension * 0.14f
            val arcSize = Size(size.width - glowWidth, size.height - glowWidth)
            val arcOffset = Offset(glowWidth / 2, glowWidth / 2)
            val sweepAngle = 360f * (score.coerceIn(0, 100) / 100f)

            // Background track
            drawArc(
                color = OutlineSubtle,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = arcOffset
            )
            // Outer glow layer
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = glowWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = arcOffset
            )
            // Main arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = arcOffset
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontFamily = TelemetryFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = color
            )
            Text(
                text = "/100",
                fontFamily = TelemetryFontFamily,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
