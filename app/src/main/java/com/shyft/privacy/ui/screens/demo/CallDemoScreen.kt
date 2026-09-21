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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.PrivacyStatusBadge
import com.shyft.privacy.ui.components.SensitiveContent
import com.shyft.privacy.ui.components.SensitiveContentType
import com.shyft.privacy.ui.components.ShyftSectionHeader
import com.shyft.privacy.ui.theme.ShieldActiveGreen

@Composable
fun CallDemoScreen(
    privacyStateRepository: IPrivacyStateRepository,
    modifier: Modifier = Modifier
) {
    val visualPrivacyState by privacyStateRepository.visualPrivacyState.collectAsState()
    val audioPrivacyState by privacyStateRepository.audioPrivacyState.collectAsState()
    val riskState = visualPrivacyState.riskState

    val scrollState = rememberScrollState()

    // Controlled demo audio volume calculation
    val demoAudioVolumePercent = if (riskState == VisualPrivacyRiskState.PRIVACY_RISK && audioPrivacyState.isMonitoringActive) {
        25 // Reduced audio volume during Privacy Risk
    } else {
        (audioPrivacyState.recommendedVolumeFactor * 100).toInt()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShyftSectionHeader(
            title = "SHYFT Call Privacy Demo",
            subtitle = "Controlled fictional call demonstration of combined visual and audio privacy protection.",
            icon = Icons.Default.Call
        )

        // Fictional Demo Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONTROLLED DEMO: Fictional SHYFT call privacy sandbox environment.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Live Privacy State Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (riskState) {
                    VisualPrivacyRiskState.PRIVACY_RISK -> MaterialTheme.colorScheme.errorContainer
                    VisualPrivacyRiskState.PRIVACY_CHECKING -> MaterialTheme.colorScheme.surfaceVariant
                    VisualPrivacyRiskState.NO_PRIVACY_RISK -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Call Privacy Engine State",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (riskState) {
                            VisualPrivacyRiskState.PRIVACY_RISK -> "Privacy Risk Active — Caller ID masked & Audio volume reduced"
                            VisualPrivacyRiskState.PRIVACY_CHECKING -> "Checking surroundings..."
                            VisualPrivacyRiskState.NO_PRIVACY_RISK -> "No Risk — Caller ID clear & Audio volume normal"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                PrivacyStatusBadge(
                    isActive = riskState == VisualPrivacyRiskState.PRIVACY_RISK,
                    label = riskState.name
                )
            }
        }

        // Fictional Call Screen Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ACTIVE CALL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Caller Avatar Photo (Masked under PRIVACY_RISK)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = if (riskState == VisualPrivacyRiskState.PRIVACY_RISK) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (riskState == VisualPrivacyRiskState.PRIVACY_RISK) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Photo Masked",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Caller Photo",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Caller Name (SENSITIVE)
                SensitiveContent(
                    text = "Rahul",
                    type = SensitiveContentType.EMAIL,
                    riskState = riskState,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Caller Phone Number (SENSITIVE)
                SensitiveContent(
                    text = "+91 98765 43210",
                    type = SensitiveContentType.PHONE_NUMBER,
                    riskState = riskState,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Controlled Adaptive Speaker Volume Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Controlled Demo Audio Volume",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "$demoAudioVolumePercent%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Call Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(ShieldActiveGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "In Call",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
