package com.shyft.privacy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenRoute(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object HomeDashboard : ScreenRoute("dashboard", "Dashboard", Icons.Default.Home)
    object ProtectedApps : ScreenRoute("protected_apps", "Protected Apps", Icons.Default.AppBlocking)
    object VisualPrivacy : ScreenRoute("visual_privacy", "Visual Privacy", Icons.Default.Visibility)
    object AudioPrivacy : ScreenRoute("audio_privacy", "Audio Privacy", Icons.Default.GraphicEq)
    object PrivacyZones : ScreenRoute("privacy_zones", "Privacy Zones", Icons.Default.LocationOn)
    object Sensors : ScreenRoute("sensors", "Sensors", Icons.Default.Sensors)
    object DemoMode : ScreenRoute("demo_mode", "Demo Mode", Icons.Default.PlayCircle)
    object PaymentDemo : ScreenRoute("payment_demo", "Payment Demo", Icons.Default.Payment)
    object CallDemo : ScreenRoute("call_demo", "Call Privacy Demo", Icons.Default.Call)
    object MediaDemo : ScreenRoute("media_demo", "Media Volume Demo", Icons.Default.MusicNote)
    object Settings : ScreenRoute("settings", "Settings", Icons.Default.Settings)

    companion object {
        val ALL_SCREENS: List<ScreenRoute>
            get() = listOf(
                HomeDashboard,
                ProtectedApps,
                VisualPrivacy,
                AudioPrivacy,
                PrivacyZones,
                Sensors,
                DemoMode,
                PaymentDemo,
                CallDemo,
                MediaDemo,
                Settings
            )
    }
}
