package com.shyft.privacy.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppCategory {
    COMMUNICATION,
    FINANCE,
    MEDIA,
    UTILITY,
    SOCIAL
}

data class ProtectedApp(
    val id: String,
    val name: String,
    val packageName: String,
    val category: AppCategory,
    val icon: ImageVector,
    val description: String
)

object DefaultProtectedApps {
    val APPS = listOf(
        ProtectedApp(
            id = "phone",
            name = "Phone",
            packageName = "com.android.dialer",
            category = AppCategory.COMMUNICATION,
            icon = Icons.Default.Phone,
            description = "Dialer, call logs, and contacts"
        ),
        ProtectedApp(
            id = "gpay",
            name = "Google Pay",
            packageName = "com.google.android.apps.wallet.nfcrel",
            category = AppCategory.FINANCE,
            icon = Icons.Default.AccountBalanceWallet,
            description = "Payment credentials, cards, and transactions"
        ),
        ProtectedApp(
            id = "whatsapp",
            name = "WhatsApp",
            packageName = "com.whatsapp",
            category = AppCategory.COMMUNICATION,
            icon = Icons.Default.Send,
            description = "End-to-end encrypted chats and call history"
        ),
        ProtectedApp(
            id = "youtube",
            name = "YouTube",
            packageName = "com.google.android.youtube",
            category = AppCategory.MEDIA,
            icon = Icons.Default.PlayArrow,
            description = "Watch history and subscriptions"
        ),
        ProtectedApp(
            id = "spotify",
            name = "Spotify",
            packageName = "com.spotify.music",
            category = AppCategory.MEDIA,
            icon = Icons.Default.MusicNote,
            description = "Audio streaming and listening activity"
        ),
        ProtectedApp(
            id = "instagram",
            name = "Instagram",
            packageName = "com.instagram.android",
            category = AppCategory.SOCIAL,
            icon = Icons.Default.CameraAlt,
            description = "Direct messages, feed, and photos"
        ),
        ProtectedApp(
            id = "gallery",
            name = "Gallery",
            packageName = "com.android.gallery3d",
            category = AppCategory.MEDIA,
            icon = Icons.Default.Image,
            description = "Photos, videos, and local media albums"
        ),
        ProtectedApp(
            id = "calculator",
            name = "Calculator",
            packageName = "com.android.calculator2",
            category = AppCategory.UTILITY,
            icon = Icons.Default.Calculate,
            description = "Personal math calculations and history"
        )
    )
}
