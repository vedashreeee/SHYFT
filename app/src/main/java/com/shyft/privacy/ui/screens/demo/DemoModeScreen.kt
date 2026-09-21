package com.shyft.privacy.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppBlocking
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shyft.privacy.data.model.DemoScenario
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.ShyftSectionHeader

@Composable
fun DemoModeScreen(
    privacyStateRepository: IPrivacyStateRepository,
    onNavigateToProtectedApps: () -> Unit = {},
    onNavigateToVisualPrivacy: () -> Unit = {},
    onNavigateToPaymentDemo: () -> Unit = {},
    onNavigateToMediaDemo: () -> Unit = {},
    onNavigateToCallDemo: () -> Unit = {},
    onNavigateToSensors: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val demoState by privacyStateRepository.demoState.collectAsState()
    val visualState by privacyStateRepository.visualPrivacyState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShyftSectionHeader(
            title = "Guided Presentation & Demo Sandbox",
            subtitle = "Follow the 6-step presentation flow or test simulated privacy scenarios.",
            icon = Icons.Default.PlayArrow
        )

        // Active State Banner (Real vs Simulation Labeling)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    visualState.isSimulationActive -> MaterialTheme.colorScheme.secondaryContainer
                    visualState.isRealCameraActive -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            visualState.isSimulationActive -> "[SIMULATION] Sandbox State Active"
                            visualState.isRealCameraActive -> "[REAL CAMERA] Live ML Kit Feed Active"
                            else -> "Sandbox Idle"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = demoState.simulationMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Privacy State: ${visualState.riskState.name}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Section: Guided 6-Step Presentation Flow
        Text(
            text = "Guided 6-Step Presentation Flow",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Step 1 Card
        DemoStepCard(
            stepNumber = 1,
            title = "Protected Apps Selection",
            description = "Verify user-choice model: privacy monitoring activates strictly for selected applications.",
            icon = Icons.Default.AppBlocking,
            buttonText = "Launch Step 1: Protected Apps",
            onClick = onNavigateToProtectedApps
        )

        // Step 2 Card
        DemoStepCard(
            stepNumber = 2,
            title = "Visual Privacy Engine",
            description = "Test front-camera CameraX feed, ML Kit face detection & head orientation attention calculation.",
            icon = Icons.Default.Visibility,
            buttonText = "Launch Step 2: Visual Privacy",
            onClick = onNavigateToVisualPrivacy
        )

        // Step 3 Card
        DemoStepCard(
            stepNumber = 3,
            title = "Payment Demo Masking",
            description = "Observe selective masking of sensitive financial fields (UPI, Amount, OTP) during PRIVACY_RISK.",
            icon = Icons.Default.Payment,
            buttonText = "Launch Step 3: Payment Demo",
            onClick = onNavigateToPaymentDemo
        )

        // Step 4 Card
        DemoStepCard(
            stepNumber = 4,
            title = "Audio Privacy & Media Demo",
            description = "Test microphone RMS ambient level measurement and smooth adaptive media volume recommendations.",
            icon = Icons.Default.MusicNote,
            buttonText = "Launch Step 4: Media Demo",
            onClick = onNavigateToMediaDemo
        )

        // Step 5 Card
        DemoStepCard(
            stepNumber = 5,
            title = "Call Privacy Demo",
            description = "Test proximity sensor integration and incoming call sensitive data shielding during PRIVACY_RISK.",
            icon = Icons.Default.Call,
            buttonText = "Launch Step 5: Call Privacy Demo",
            onClick = onNavigateToCallDemo
        )

        // Step 6 Card
        DemoStepCard(
            stepNumber = 6,
            title = "Sensors & Debug Telemetry",
            description = "Inspect real-time device orientation, accelerometer, gyroscope and proximity raw telemetry.",
            icon = Icons.Default.Sensors,
            buttonText = "Launch Step 6: Sensors & Debug",
            onClick = onNavigateToSensors
        )

        // Section: Sandbox Simulation Triggers
        Text(
            text = "Simulate Surroundings Adaptive Scenarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Scenario 1: Shoulder Surfer Simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Scenario 1: Shoulder Surfer Anomaly",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[SIMULATED]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulates a second person detected and oriented toward the screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { privacyStateRepository.triggerDemoScenario(DemoScenario.SHOULDER_SURFER_DETECTED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger [SIMULATED] Shoulder Surfer Risk")
                }
            }
        }

        // Scenario 2: Public Zone Simulation
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Scenario 2: Public Transit Zone Entry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "[SIMULATED]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulates entering a high-risk public zone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { privacyStateRepository.triggerDemoScenario(DemoScenario.ENTERED_PUBLIC_ZONE) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Trigger [SIMULATED] Public Zone Risk")
                }
            }
        }

        // Reset Sandbox Button
        OutlinedButton(
            onClick = { privacyStateRepository.resetDemo() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Simulation Sandbox")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DemoStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
    buttonText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}
