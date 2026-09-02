package com.thesis.bitperfectusb.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(val route: String, val label: String, val icon: ImageVector) {
    LIBRARY("library", "Library", Icons.Filled.LibraryMusic),
    PLAYER("player", "Player", Icons.Filled.PlayCircle),
    ABX("abx", "ABX Lab", Icons.Filled.Headphones),
    SETTINGS("settings", "Settings", Icons.Filled.Settings)
}
