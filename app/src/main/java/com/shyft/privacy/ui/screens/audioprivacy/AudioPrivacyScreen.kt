package com.shyft.privacy.ui.screens.audioprivacy

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.shyft.privacy.data.model.RelativeAmbientLevel
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.PrivacyStatusBadge
import com.shyft.privacy.ui.components.ShyftSectionHeader

@Composable
fun AudioPrivacyScreen(
    privacyStateRepository: IPrivacyStateRepository,
    onNavigateToCallDemo: () -> Unit,
    onNavigateToMediaDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioState by privacyStateRepository.audioPrivacyState.collectAsState()
    val scrollState = rememberScrollState()

    // Permission launcher requested ONLY when user enables Audio Privacy
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        privacyStateRepository.updateAudioPrivacy { it.copy(hasMicPermission = isGranted) }
        if (isGranted) {
            privacyStateRepository.startAudioPrivacyEngine(context)
        } else {
            privacyStateRepository.updateAudioPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    statusMessage = "Microphone permission denied. Audio Privacy requires ambient sensor permission."
                )
            }
        }
    }

    val currentVolPercent = (audioState.recommendedVolumeFactor * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShyftSectionHeader(
            title = "Audio Privacy Guard",
            subtitle = "Microphone ambient sound sensor & adaptive volume recommendation.",
            icon = Icons.Default.GraphicEq
        )

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
                        text = "Ambient Sound Sensor Guard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PrivacyStatusBadge(
                        isActive = audioState.isMonitoringActive,
                        label = if (audioState.isMonitoringActive) audioState.relativeAmbientLevel.name else "Inactive"
                    )
                }
                Switch(
                    checked = audioState.isMonitoringActive,
                    onCheckedChange = { active ->
                        if (active) {
                            val check = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            if (check == PackageManager.PERMISSION_GRANTED) {
                                privacyStateRepository.updateAudioPrivacy { it.copy(hasMicPermission = true) }
                                privacyStateRepository.startAudioPrivacyEngine(context)
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        } else {
                            privacyStateRepository.stopAudioPrivacyEngine()
                        }
                    }
                )
            }
        }

        // Relative Ambient Level Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Relative Ambient Level Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Current Level", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = audioState.relativeAmbientLevel.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Smoothed RMS Amplitude", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "%.4f".format(audioState.smoothedRmsAmplitude), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = audioState.statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Diagnostic Telemetry Debug Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Audio Hardware Diagnostics (Debug)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                DiagnosticRow(label = "AudioRecord Initialized", value = if (audioState.isAudioRecordInitialized) "YES" else "NO")
                DiagnosticRow(label = "Samples Read (last frame)", value = "${audioState.lastReadSampleCount} samples")
                DiagnosticRow(label = "Raw RMS (before EMA)", value = "%.4f".format(audioState.rawRmsAmplitude))
                DiagnosticRow(label = "Smoothed RMS (EMA 0.15)", value = "%.4f".format(audioState.smoothedRmsAmplitude))
                DiagnosticRow(label = "Classified Level", value = audioState.relativeAmbientLevel.name)
            }
        }

        // Adaptive Volume Recommendation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Adaptive Volume Recommendation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Recommended Volume", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$currentVolPercent%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = audioState.recommendedVolumeFactor,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Action Buttons: Call Demo & Media Demo
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToCallDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Call Privacy Demo", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onNavigateToMediaDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Media Volume Demo", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
