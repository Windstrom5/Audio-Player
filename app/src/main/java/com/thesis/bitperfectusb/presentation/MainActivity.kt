package com.thesis.bitperfectusb.presentation

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thesis.bitperfectusb.presentation.navigation.AppDestination
import com.thesis.bitperfectusb.presentation.screen.AbxScreen
import com.thesis.bitperfectusb.presentation.screen.DacScreen
import com.thesis.bitperfectusb.presentation.screen.DiagnosticsScreen
import com.thesis.bitperfectusb.presentation.screen.LibraryScreen
import com.thesis.bitperfectusb.presentation.screen.PlayerScreen
import com.thesis.bitperfectusb.presentation.screen.SettingsScreen
import com.thesis.bitperfectusb.presentation.theme.BitPerfectUsbTheme
import com.thesis.bitperfectusb.presentation.theme.HifiGold
import com.thesis.bitperfectusb.presentation.theme.OutlineSubtle
import com.thesis.bitperfectusb.presentation.theme.SignalTeal
import com.thesis.bitperfectusb.presentation.theme.SurfaceRaised
import com.thesis.bitperfectusb.presentation.theme.TelemetryFontFamily
import com.thesis.bitperfectusb.presentation.theme.TextSecondary
import com.thesis.bitperfectusb.presentation.viewmodel.KeepScreenOnViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            BitPerfectUsbTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    BitPerfectApp()
                }
            }
        }
    }
}

@Composable
private fun BitPerfectApp() {
    val navController = rememberNavController()
    KeepScreenOnEffect()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                destinations = AppDestination.entries
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.PLAYER.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(AppDestination.LIBRARY.route) {
                LibraryScreen(
                    onNavigateToPlayer = {
                        navController.navigate(AppDestination.PLAYER.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(AppDestination.PLAYER.route) { PlayerScreen() }
            composable(AppDestination.ABX.route) { AbxScreen() }
            composable(AppDestination.SETTINGS.route) { SettingsScreen() }
            composable("dac") { DacScreen() }
            composable("diagnostics") { DiagnosticsScreen() }
        }
    }
}

@Composable
private fun CustomBottomBar(
    navController: androidx.navigation.NavController,
    destinations: List<AppDestination>
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceRaised)
            .border(BorderStroke(1.dp, OutlineSubtle))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        if (currentRoute != destination.route) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.label,
                    tint = if (selected) HifiGold else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = destination.label.uppercase(),
                    fontFamily = TelemetryFontFamily,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) HifiGold else TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                // Clean glowing active bar indicator
                Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 1.5.dp)
                        .clip(RoundedCornerShape(0.5.dp))
                        .background(if (selected) HifiGold else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun KeepScreenOnEffect(viewModel: KeepScreenOnViewModel = koinViewModel()) {
    val shouldKeepOn by viewModel.shouldKeepScreenOn.collectAsStateWithLifecycle()
    val view = LocalView.current

    LaunchedEffect(shouldKeepOn) {
        view.keepScreenOn = shouldKeepOn
    }
}
