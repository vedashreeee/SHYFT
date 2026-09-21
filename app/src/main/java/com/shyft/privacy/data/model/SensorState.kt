package com.shyft.privacy.data.model

data class EnvironmentalSensor(
    val id: String,
    val name: String,
    val typeName: String,
    val currentValue: String,
    val unit: String,
    val statusText: String,
    val isHealthy: Boolean
)

data class SensorState(
    val movementState: DeviceMovementState = DeviceMovementState.STATIONARY,
    val orientationState: DeviceOrientationState = DeviceOrientationState.PHONE_FACING_USER,
    val proximityTelemetryState: TelemetryProximityState = TelemetryProximityState.AWAY_FROM_EAR,
    val accelerometerMagnitude: Float = 9.81f,
    val accelerometerX: Float = 0.0f,
    val accelerometerY: Float = 9.81f,
    val accelerometerZ: Float = 0.0f,
    val gyroscopeMagnitude: Float = 0.02f,
    val gyroscopeX: Float = 0.0f,
    val gyroscopeY: Float = 0.0f,
    val gyroscopeZ: Float = 0.0f,
    val proximityDistanceCm: Float = 5.0f,
    val sensors: List<EnvironmentalSensor> = emptyList()
)
