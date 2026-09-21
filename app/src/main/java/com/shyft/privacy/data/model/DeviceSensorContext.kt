package com.shyft.privacy.data.model

enum class DeviceMovementState {
    STATIONARY,
    MOVING
}

enum class DeviceOrientationState {
    PHONE_FACING_USER,
    FACE_DOWN,
    TILTED
}

enum class TelemetryProximityState {
    AWAY_FROM_EAR,
    NEAR_EAR
}
