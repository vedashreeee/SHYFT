package com.shyft.privacy.data.model

enum class VisualProtectionLevel {
    DISABLED,
    STANDARD,
    HIGH,
    MAXIMUM
}

enum class ShieldMode {
    BLUR_OVERLAY,
    SIDE_ANGLE_DIMMING,
    SENSITIVE_TEXT_MASKING,
    FULL_SHIELD
}

data class VisualPrivacyState(
    val isMonitoringActive: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val isRealCameraActive: Boolean = false,
    val isSimulationActive: Boolean = false,
    val riskState: VisualPrivacyRiskState = VisualPrivacyRiskState.NO_PRIVACY_RISK,
    val faceCount: Int = 0,
    val primaryUserDetected: Boolean = false,
    val possibleSecondPersonDetected: Boolean = false,
    val headOrientationApproximation: String = "No faces in view",
    val stabilityTimerText: String = "Idle",
    val activeShieldMode: ShieldMode = ShieldMode.BLUR_OVERLAY,
    val protectionLevel: VisualProtectionLevel = VisualProtectionLevel.STANDARD,
    val statusMessage: String = "Visual Privacy is inactive. Enable to start monitoring."
)
