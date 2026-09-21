package com.shyft.privacy.data.model

enum class ZoneType {
    HOME,
    WORK,
    PUBLIC,
    CUSTOM
}

data class PrivacyZone(
    val id: String,
    val name: String,
    val type: ZoneType,
    val radiusMeters: Int,
    val isCurrentlyActive: Boolean,
    val visualProtectionLevel: VisualProtectionLevel,
    val audioShieldLevel: AudioShieldLevel
)
