package com.thesis.bitperfectusb.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = SignalTeal,
    onPrimary = BackgroundCharcoal,
    secondary = SignalTealDim,
    background = BackgroundCharcoal,
    onBackground = TextPrimary,
    surface = SurfaceCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = TextSecondary,
    outline = OutlineSubtle,
    error = ErrorCoral
)

@Composable
fun BitPerfectUsbTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
