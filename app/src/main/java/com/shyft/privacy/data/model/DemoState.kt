package com.shyft.privacy.data.model

enum class DemoScenario {
    IDLE,
    SHOULDER_SURFER_DETECTED,
    EAVESDROPPING_ALERT,
    ENTERED_PUBLIC_ZONE,
    PROTECTED_APP_OPENED
}

data class DemoState(
    val isDemoActive: Boolean = false,
    val currentScenario: DemoScenario = DemoScenario.IDLE,
    val simulatedAppPackage: String? = null,
    val simulationMessage: String = "Select a scenario to simulate surroundings adaptive privacy."
)
