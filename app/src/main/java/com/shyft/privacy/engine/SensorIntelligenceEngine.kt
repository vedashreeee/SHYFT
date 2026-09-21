package com.shyft.privacy.engine

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.shyft.privacy.data.model.DeviceMovementState
import com.shyft.privacy.data.model.DeviceOrientationState
import com.shyft.privacy.data.model.EnvironmentalSensor
import com.shyft.privacy.data.model.TelemetryProximityState
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import kotlin.math.sqrt

class SensorIntelligenceEngine(
    private val context: Context,
    private val repository: IPrivacyStateRepository
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var proximitySensor: Sensor? = null

    private var isListening = false

    fun startListening() {
        if (isListening || sensorManager == null) return

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroscope?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        proximitySensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }

        isListening = true

        updateTelemetryList()
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = sqrt(x * x + y * y + z * z)

                // Movement state heuristic
                val movement = if (kotlin.math.abs(mag - 9.81f) > 1.2f) {
                    DeviceMovementState.MOVING
                } else {
                    DeviceMovementState.STATIONARY
                }

                // Orientation heuristic
                val orientation = when {
                    z > 7.0f -> DeviceOrientationState.PHONE_FACING_USER
                    z < -7.0f -> DeviceOrientationState.FACE_DOWN
                    else -> DeviceOrientationState.TILTED
                }

                repository.updateSensors {
                    it.copy(
                        accelerometerMagnitude = mag,
                        accelerometerX = x,
                        accelerometerY = y,
                        accelerometerZ = z,
                        movementState = movement,
                        orientationState = orientation
                    )
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                val mag = sqrt(gx * gx + gy * gy + gz * gz)

                repository.updateSensors {
                    it.copy(
                        gyroscopeMagnitude = mag,
                        gyroscopeX = gx,
                        gyroscopeY = gy,
                        gyroscopeZ = gz
                    )
                }
            }

            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                // Proximity telemetry ONLY (no earpiece routing, no audio output switching)
                val proximityState = if (distance < 2.0f) {
                    TelemetryProximityState.NEAR_EAR
                } else {
                    TelemetryProximityState.AWAY_FROM_EAR
                }

                repository.updateSensors {
                    it.copy(
                        proximityDistanceCm = distance,
                        proximityTelemetryState = proximityState
                    )
                }
            }
        }

        updateTelemetryList()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateTelemetryList() {
        val current = repository.sensorState.value

        val sensorList = listOf(
            EnvironmentalSensor(
                id = "accel_1",
                name = "3-Axis Accelerometer",
                typeName = "Motion",
                currentValue = "%.2f m/s²".format(current.accelerometerMagnitude),
                unit = "m/s²",
                statusText = "Movement: ${current.movementState.name} • Orientation: ${current.orientationState.name}",
                isHealthy = true
            ),
            EnvironmentalSensor(
                id = "gyro_1",
                name = "3-Axis Gyroscope",
                typeName = "Angular Velocity",
                currentValue = "%.2f rad/s".format(current.gyroscopeMagnitude),
                unit = "rad/s",
                statusText = if (gyroscope != null) "Gyroscope active" else "Hardware not available",
                isHealthy = gyroscope != null
            ),
            EnvironmentalSensor(
                id = "prox_1",
                name = "Proximity Sensor",
                typeName = "Telemetry Distance",
                currentValue = "%.1f cm".format(current.proximityDistanceCm),
                unit = "cm",
                statusText = "Proximity Telemetry: ${current.proximityTelemetryState.name} (Context Only)",
                isHealthy = proximitySensor != null
            )
        )

        repository.updateSensors { it.copy(sensors = sensorList) }
    }
}
