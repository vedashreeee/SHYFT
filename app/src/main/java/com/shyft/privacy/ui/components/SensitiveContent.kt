package com.shyft.privacy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shyft.privacy.data.model.VisualPrivacyRiskState

enum class SensitiveContentType {
    OTP,
    UPI_ID,
    ACCOUNT_NUMBER,
    PHONE_NUMBER,
    EMAIL,
    PASSWORD,
    PAYMENT_AMOUNT,
    CARD_NUMBER,
    CUSTOM_ZONE
}

/**
 * Classification architecture strategy interface for sensitive content.
 * Structured to seamlessly allow on-device ML Kit OCR / NLP classification in future phases.
 */
interface ISensitiveContentClassifier {
    fun isContentSensitive(text: String, explicitType: SensitiveContentType?): Boolean
}

class TaggedSensitiveContentClassifier : ISensitiveContentClassifier {
    override fun isContentSensitive(text: String, explicitType: SensitiveContentType?): Boolean {
        // Tagged classification strategy for controlled payment demo
        return explicitType != null
    }
}

@Composable
fun SensitiveContent(
    text: String,
    type: SensitiveContentType,
    riskState: VisualPrivacyRiskState,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    classifier: ISensitiveContentClassifier = remember { TaggedSensitiveContentClassifier() }
) {
    val isSensitive = classifier.isContentSensitive(text, type)
    val shouldMask = isSensitive && (riskState == VisualPrivacyRiskState.PRIVACY_RISK)

    if (shouldMask) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Masked",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "••••••••",
                    style = style,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SHYFT PRIVACY ACTIVE",
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    } else {
        Text(
            text = text,
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = modifier
        )
    }
}
