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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import com.shyft.privacy.ui.components.PrivacyStatusBadge
import com.shyft.privacy.ui.components.SensitiveContent
import com.shyft.privacy.ui.components.SensitiveContentType
import com.shyft.privacy.ui.components.ShyftSectionHeader
import com.shyft.privacy.ui.theme.ShieldActiveGreen

@Composable
fun PaymentDemoScreen(
    privacyStateRepository: IPrivacyStateRepository,
    modifier: Modifier = Modifier
) {
    val visualPrivacyState by privacyStateRepository.visualPrivacyState.collectAsState()
    val riskState = visualPrivacyState.riskState
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShyftSectionHeader(
            title = "SHYFT Payment Demo",
            subtitle = "Controlled sandbox demonstration of surroundings-adaptive visual privacy masking.",
            icon = Icons.Default.Payment
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
                    text = "FICTIONAL DEMO: Controlled SHYFT screen testing environment.",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Live Risk State Telemetry Banner
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
                        text = "Current Privacy Engine State",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when (riskState) {
                            VisualPrivacyRiskState.PRIVACY_RISK -> "Privacy Risk Active — Sensitive fields masked"
                            VisualPrivacyRiskState.PRIVACY_CHECKING -> "Checking surroundings..."
                            VisualPrivacyRiskState.NO_PRIVACY_RISK -> "No Privacy Risk — All content clear"
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

        // Fictional Realistic Payment Confirmation Card
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
                // Success Badge Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ShieldActiveGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = ShieldActiveGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Payment Successful",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Amount (SENSITIVE)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SensitiveContent(
                        text = "₹42,350",
                        type = SensitiveContentType.PAYMENT_AMOUNT,
                        riskState = riskState,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Details List
                PaymentDetailRow(
                    label = "Recipient",
                    value = "Rahul",
                    isSensitive = false,
                    type = SensitiveContentType.PAYMENT_AMOUNT,
                    riskState = riskState
                )

                PaymentDetailRow(
                    label = "UPI ID",
                    value = "rahul@upi",
                    isSensitive = true,
                    type = SensitiveContentType.UPI_ID,
                    riskState = riskState
                )

                PaymentDetailRow(
                    label = "Account Number",
                    value = "XXXX4321",
                    isSensitive = true,
                    type = SensitiveContentType.ACCOUNT_NUMBER,
                    riskState = riskState
                )

                PaymentDetailRow(
                    label = "Transaction OTP",
                    value = "583921",
                    isSensitive = true,
                    type = SensitiveContentType.OTP,
                    riskState = riskState
                )

                PaymentDetailRow(
                    label = "Status",
                    value = "Payment Successful",
                    isSensitive = false,
                    type = SensitiveContentType.PAYMENT_AMOUNT,
                    riskState = riskState
                )
            }
        }
    }
}

@Composable
private fun PaymentDetailRow(
    label: String,
    value: String,
    isSensitive: Boolean,
    type: SensitiveContentType,
    riskState: VisualPrivacyRiskState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        if (isSensitive) {
            SensitiveContent(
                text = value,
                type = type,
                riskState = riskState,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
