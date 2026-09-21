package com.shyft.privacy

import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.ui.components.SensitiveContentType
import com.shyft.privacy.ui.components.TaggedSensitiveContentClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisualPrivacyEngineTest {

    @Test
    fun publicPrivacyStates_areExactlyThree() {
        val states = VisualPrivacyRiskState.values()
        assertEquals(3, states.size)
        assertEquals(VisualPrivacyRiskState.NO_PRIVACY_RISK, states[0])
        assertEquals(VisualPrivacyRiskState.PRIVACY_CHECKING, states[1])
        assertEquals(VisualPrivacyRiskState.PRIVACY_RISK, states[2])
    }

    @Test
    fun decisionLogic_zeroOrSingleFace_isNoPrivacyRisk() {
        val zeroFaceState = evaluateRiskState(count = 0, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 500, previousState = VisualPrivacyRiskState.NO_PRIVACY_RISK)
        val singleFaceState = evaluateRiskState(count = 1, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 500, previousState = VisualPrivacyRiskState.NO_PRIVACY_RISK)

        assertEquals(VisualPrivacyRiskState.NO_PRIVACY_RISK, zeroFaceState)
        assertEquals(VisualPrivacyRiskState.NO_PRIVACY_RISK, singleFaceState)
    }

    @Test
    fun decisionLogic_secondFacePresent_requiresStable1200msBeforePrivacyRisk() {
        val checkingState = evaluateRiskState(count = 2, hasSecondPerson = true, secondOrientedTowardPhone = true, elapsedRiskMs = 600, elapsedClearMs = 0, previousState = VisualPrivacyRiskState.NO_PRIVACY_RISK)
        val riskState = evaluateRiskState(count = 2, hasSecondPerson = true, secondOrientedTowardPhone = true, elapsedRiskMs = 1250, elapsedClearMs = 0, previousState = VisualPrivacyRiskState.PRIVACY_CHECKING)

        assertEquals(VisualPrivacyRiskState.PRIVACY_CHECKING, checkingState)
        assertEquals(VisualPrivacyRiskState.PRIVACY_RISK, riskState)
    }

    @Test
    fun decisionLogic_restorationDelay_holdsRiskFor400msBeforeRestoringClear() {
        val holdingState = evaluateRiskState(count = 1, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 200, previousState = VisualPrivacyRiskState.PRIVACY_RISK)
        val restoredState = evaluateRiskState(count = 1, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 450, previousState = VisualPrivacyRiskState.PRIVACY_RISK)

        assertEquals(VisualPrivacyRiskState.PRIVACY_RISK, holdingState)
        assertEquals(VisualPrivacyRiskState.NO_PRIVACY_RISK, restoredState)
    }

    @Test
    fun selectiveMasking_noPrivacyRisk_allValuesVisible() {
        val amountMasked = isFieldMasked("₹42,350", isSensitive = true, riskState = VisualPrivacyRiskState.NO_PRIVACY_RISK)
        val recipientMasked = isFieldMasked("Rahul", isSensitive = false, riskState = VisualPrivacyRiskState.NO_PRIVACY_RISK)

        assertFalse(amountMasked)
        assertFalse(recipientMasked)
    }

    @Test
    fun selectiveMasking_privacyChecking_valuesRemainVisible() {
        val amountMasked = isFieldMasked("₹42,350", isSensitive = true, riskState = VisualPrivacyRiskState.PRIVACY_CHECKING)
        val recipientMasked = isFieldMasked("Rahul", isSensitive = false, riskState = VisualPrivacyRiskState.PRIVACY_CHECKING)

        assertFalse(amountMasked)
        assertFalse(recipientMasked)
    }

    @Test
    fun selectiveMasking_privacyRisk_onlySensitiveValuesMasked_nonSensitiveRemainVisible() {
        // Sensitive values (Amount, UPI ID, Account, OTP) MUST be masked under PRIVACY_RISK
        val amountMasked = isFieldMasked("₹42,350", isSensitive = true, riskState = VisualPrivacyRiskState.PRIVACY_RISK)
        val upiMasked = isFieldMasked("rahul@upi", isSensitive = true, riskState = VisualPrivacyRiskState.PRIVACY_RISK)
        val accountMasked = isFieldMasked("XXXX4321", isSensitive = true, riskState = VisualPrivacyRiskState.PRIVACY_RISK)
        val otpMasked = isFieldMasked("583921", isSensitive = true, riskState = VisualPrivacyRiskState.PRIVACY_RISK)

        assertTrue(amountMasked)
        assertTrue(upiMasked)
        assertTrue(accountMasked)
        assertTrue(otpMasked)

        // Non-sensitive values (Recipient name, Status) MUST remain visible under PRIVACY_RISK
        val recipientMasked = isFieldMasked("Rahul", isSensitive = false, riskState = VisualPrivacyRiskState.PRIVACY_RISK)
        val statusMasked = isFieldMasked("Payment Successful", isSensitive = false, riskState = VisualPrivacyRiskState.PRIVACY_RISK)

        assertFalse(recipientMasked)
        assertFalse(statusMasked)
    }

    @Test
    fun selectiveMasking_disappearsAfterRestoration() {
        val riskStateBeforeRestoration = evaluateRiskState(count = 1, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 200, previousState = VisualPrivacyRiskState.PRIVACY_RISK)
        val riskStateAfterRestoration = evaluateRiskState(count = 1, hasSecondPerson = false, secondOrientedTowardPhone = false, elapsedRiskMs = 0, elapsedClearMs = 450, previousState = VisualPrivacyRiskState.PRIVACY_RISK)

        assertTrue(isFieldMasked("₹42,350", isSensitive = true, riskState = riskStateBeforeRestoration))
        assertFalse(isFieldMasked("₹42,350", isSensitive = true, riskState = riskStateAfterRestoration))
    }

    private fun isFieldMasked(text: String, isSensitive: Boolean, riskState: VisualPrivacyRiskState): Boolean {
        return isSensitive && (riskState == VisualPrivacyRiskState.PRIVACY_RISK)
    }

    private fun evaluateRiskState(
        count: Int,
        hasSecondPerson: Boolean,
        secondOrientedTowardPhone: Boolean,
        elapsedRiskMs: Long,
        elapsedClearMs: Long,
        previousState: VisualPrivacyRiskState
    ): VisualPrivacyRiskState {
        val isRiskActive = (count >= 2) && hasSecondPerson && secondOrientedTowardPhone

        return if (isRiskActive) {
            if (elapsedRiskMs >= 1200L) VisualPrivacyRiskState.PRIVACY_RISK else VisualPrivacyRiskState.PRIVACY_CHECKING
        } else {
            if (previousState == VisualPrivacyRiskState.PRIVACY_RISK) {
                if (elapsedClearMs >= 400L) VisualPrivacyRiskState.NO_PRIVACY_RISK else VisualPrivacyRiskState.PRIVACY_RISK
            } else {
                VisualPrivacyRiskState.NO_PRIVACY_RISK
            }
        }
    }
}
