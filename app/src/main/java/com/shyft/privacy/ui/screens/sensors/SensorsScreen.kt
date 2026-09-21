package com.shyft.privacy.ui.screens.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shyft.privacy.data.model.EnvironmentalSensor
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.ShyftSectionHeader
import com.shyft.privacy.ui.theme.ShieldActiveGreen

@Composable
fun SensorsScreen(
    privacyStateRepository: IPrivacyStateRepository,
    modifier: Modifier = Modifier
) {
    val visualState by privacyStateRepository.visualPrivacyState.collectAsState()
    val audioState by privacyStateRepository.audioPrivacyState.collectAsState()
    val sensorState by privacyStateRepository.sensorState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ShyftSectionHeader(
                title = "Sensors & Debug Telemetry",
                subtitle = "Real-time Android hardware sensor state and privacy intelligence telemetry.",
                icon = Icons.Default.Sensors
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // VISUAL TELEMETRY CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VISUAL PRIVACY TELEMETRY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DebugRow("Camera Status:", if (visualState.isRealCameraActive) "Front Camera Active" else "Inactive", MaterialTheme.colorScheme.onPrimaryContainer)
                    DebugRow("Face Count:", "${visualState.faceCount} face(s)", MaterialTheme.colorScheme.onPrimaryContainer)
                    DebugRow("Primary User:", if (visualState.primaryUserDetected) "Yes (Centered)" else "No", MaterialTheme.colorScheme.onPrimaryContainer)
                    DebugRow("Secondary Person:", if (visualState.possibleSecondPersonDetected) "DETECTED" else "None", MaterialTheme.colorScheme.onPrimaryContainer)
                    DebugRow("Head Orientation:", visualState.headOrientationApproximation, MaterialTheme.colorScheme.onPrimaryContainer)
                    DebugRow("Privacy State:", visualState.riskState.name, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // AUDIO TELEMETRY CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AUDIO PRIVACY TELEMETRY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DebugRow("Microphone Status:", if (audioState.isMonitoringActive) "Ambient Mic Sensor Active" else "Inactive", MaterialTheme.colorScheme.onSecondaryContainer)
                    DebugRow("Smoothed RMS:", "%.3f".format(audioState.smoothedRmsAmplitude), MaterialTheme.colorScheme.onSecondaryContainer)
                    DebugRow("Ambient Level:", audioState.relativeAmbientLevel.name, MaterialTheme.colorScheme.onSecondaryContainer)
                    DebugRow("Recommended Volume:", "${(audioState.recommendedVolumeFactor * 100).toInt()}%", MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // SENSORS TELEMETRY CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HARDWARE SENSORS TELEMETRY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DebugRow("Movement State:", sensorState.movementState.name, MaterialTheme.colorScheme.onTertiaryContainer)
                    DebugRow("Orientation State:", sensorState.orientationState.name, MaterialTheme.colorScheme.onTertiaryContainer)
                    DebugRow("Accelerometer:", "%.2f m/s² (X:%.1f Y:%.1f Z:%.1f)".format(sensorState.accelerometerMagnitude, sensorState.accelerometerX, sensorState.accelerometerY, sensorState.accelerometerZ), MaterialTheme.colorScheme.onTertiaryContainer)
                    DebugRow("Gyroscope:", "%.2f rad/s (X:%.1f Y:%.1f Z:%.1f)".format(sensorState.gyroscopeMagnitude, sensorState.gyroscopeX, sensorState.gyroscopeY, sensorState.gyroscopeZ), MaterialTheme.colorScheme.onTertiaryContainer)
                    DebugRow("Proximity Distance:", "%.1f cm (${sensorState.proximityTelemetryState.name})".format(sensorState.proximityDistanceCm), MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }

        items(sensorState.sensors, key = { it.id }) { sensor ->
            SensorCardItem(sensor = sensor)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SensorCardItem(
    sensor: EnvironmentalSensor,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (sensor.isHealthy) ShieldActiveGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${sensor.typeName} • ${sensor.statusText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = sensor.currentValue,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DebugRow(label: String, value: String, textColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = textColor)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = textColor)
    }
}
