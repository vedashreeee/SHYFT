package com.shyft.privacy.ui.screens.visualprivacy

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shyft.privacy.data.model.ShieldMode
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.data.model.VisualProtectionLevel
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.PrivacyStatusBadge
import com.shyft.privacy.ui.components.ShyftSectionHeader

@Composable
fun VisualPrivacyScreen(
    privacyStateRepository: IPrivacyStateRepository,
    onNavigateToPaymentDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = activity ?: LocalLifecycleOwner.current
    val visualState by privacyStateRepository.visualPrivacyState.collectAsState()
    val scrollState = rememberScrollState()

    // Permission launcher requested ONLY when user turns on Visual Privacy
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        privacyStateRepository.updateVisualPrivacy { it.copy(hasCameraPermission = isGranted) }
        if (isGranted) {
            privacyStateRepository.startVisualPrivacyEngine(context, lifecycleOwner)
        } else {
            privacyStateRepository.updateVisualPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    statusMessage = "CAMERA permission denied. Visual Privacy requires camera access."
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShyftSectionHeader(
            title = "Visual Privacy Shield",
            subtitle = "Front-camera on-device ML Kit face & attention detection.",
            icon = Icons.Default.Visibility
        )

        // Real Camera vs Simulation Banner Distinction
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (visualState.isSimulationActive) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else if (visualState.isRealCameraActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (visualState.isRealCameraActive) Icons.Default.CameraFront else Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = when {
                            visualState.isSimulationActive -> "SIMULATED DEMO STATE"
                            visualState.isRealCameraActive -> "REAL CAMERA DETECTION ACTIVE"
                            else -> "CAMERA MONITORING INACTIVE"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = visualState.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Monitoring Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Visual Privacy Guard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    VisualRiskIndicatorBadge(riskState = visualState.riskState)
                }
                Switch(
                    checked = visualState.isMonitoringActive,
                    onCheckedChange = { active ->
                        if (active) {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                privacyStateRepository.updateVisualPrivacy { it.copy(hasCameraPermission = true) }
                                privacyStateRepository.startVisualPrivacyEngine(context, lifecycleOwner)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        } else {
                            privacyStateRepository.stopVisualPrivacyEngine()
                        }
                    }
                )
            }
        }

        // Live Telemetry Details Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Face & Attention Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                TelemetryRow(label = "Camera Status", value = if (visualState.isRealCameraActive) "Front Camera Active" else "Inactive")
                TelemetryRow(label = "Face Count", value = "${visualState.faceCount} face(s)")
                TelemetryRow(label = "Primary User Detected", value = if (visualState.primaryUserDetected) "Yes (Centered)" else "No")
                TelemetryRow(label = "Possible Second Person", value = if (visualState.possibleSecondPersonDetected) "DETECTED" else "None")
                TelemetryRow(label = "Secondary Head Orientation", value = visualState.headOrientationApproximation)
                TelemetryRow(label = "Privacy State", value = visualState.riskState.name)
                TelemetryRow(label = "Stability Timer", value = visualState.stabilityTimerText)
            }
        }

        // Protection Level Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Protection Sensitivity Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisualProtectionLevel.values().forEach { level ->
                        FilterChip(
                            selected = visualState.protectionLevel == level,
                            onClick = {
                                privacyStateRepository.updateVisualPrivacy { it.copy(protectionLevel = level) }
                            },
                            label = { Text(level.name) }
                        )
                    }
                }
            }
        }

        // Button Link to Controlled Payment Demo
        Button(
            onClick = onNavigateToPaymentDemo,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Open Payment Demo (Live Masking)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VisualRiskIndicatorBadge(
    riskState: VisualPrivacyRiskState,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (riskState) {
        VisualPrivacyRiskState.NO_PRIVACY_RISK -> "normal" to com.shyft.privacy.ui.theme.ShieldActiveGreen
        VisualPrivacyRiskState.PRIVACY_CHECKING -> "checking" to com.shyft.privacy.ui.theme.ShieldWarningAmber
        VisualPrivacyRiskState.PRIVACY_RISK -> "Privacy Active" to MaterialTheme.colorScheme.error
    }

    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

private fun Context.findActivity(): ComponentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is ComponentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

