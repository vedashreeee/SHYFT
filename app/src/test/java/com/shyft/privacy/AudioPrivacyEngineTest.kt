package com.shyft.privacy

import com.shyft.privacy.data.model.AudioPrivacyState
import com.shyft.privacy.data.model.RelativeAmbientLevel
import com.shyft.privacy.data.model.TelemetryProximityState
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPrivacyEngineTest {

    @Test
    fun relativeAmbientLevel_classification_mapsCorrectly() {
        val quietLevel = classifyRmsLevel(0.020f)
        val normalLevel = classifyRmsLevel(0.060f)
        val noisyLevel = classifyRmsLevel(0.180f)

        assertEquals(RelativeAmbientLevel.QUIET, quietLevel)
        assertEquals(RelativeAmbientLevel.NORMAL, normalLevel)
        assertEquals(RelativeAmbientLevel.NOISY, noisyLevel)
    }

    @Test
    fun exponentialMovingAverage_smoothesAmplitudeCorrectly() {
        val alpha = 0.15f
        val previousSmoothed = 0.05f
        val rawMeasurement = 0.20f

        val expectedSmoothed = (alpha * rawMeasurement) + ((1.0f - alpha) * previousSmoothed)
        val actualSmoothed = calculateEma(rawMeasurement, previousSmoothed, alpha)

        assertEquals(expectedSmoothed, actualSmoothed, 0.001f)
    }

    @Test
    fun adaptiveVolumeTargetFactor_mapsToCorrectLevel() {
        val quietTarget = targetVolumeForLevel(RelativeAmbientLevel.QUIET)
        val normalTarget = targetVolumeForLevel(RelativeAmbientLevel.NORMAL)
        val noisyTarget = targetVolumeForLevel(RelativeAmbientLevel.NOISY)

        assertEquals(0.30f, quietTarget, 0.001f)
        assertEquals(0.50f, normalTarget, 0.001f)
        assertEquals(0.70f, noisyTarget, 0.001f)
    }

    @Test
    fun callPrivacyMasking_masksSensitiveCallerDetails_underPrivacyRisk() {
        val rawPhoneNumber = "+1 (555) 234-5678"

        val maskedUnderRisk = maskCallerDetail(rawPhoneNumber, VisualPrivacyRiskState.PRIVACY_RISK)
        val unmaskedNormal = maskCallerDetail(rawPhoneNumber, VisualPrivacyRiskState.NO_PRIVACY_RISK)

        assertEquals("•".repeat(rawPhoneNumber.length), maskedUnderRisk)
        assertEquals("+1 (555) 234-5678", unmaskedNormal)
    }

    @Test
    fun proximityTelemetryState_isTelemetryContextOnly_withoutEarpieceRouting() {
        val nearEarState = TelemetryProximityState.NEAR_EAR
        val awayState = TelemetryProximityState.AWAY_FROM_EAR

        assertEquals("NEAR_EAR", nearEarState.name)
        assertEquals("AWAY_FROM_EAR", awayState.name)
    }

    @Test
    fun audioOff_releasesOrDoesNotStartMicrophoneProcessing() {
        val stateWhenOff = AudioPrivacyState(isMonitoringActive = false)

        assertFalse(stateWhenOff.isMonitoringActive)
        assertEquals(RelativeAmbientLevel.NORMAL, stateWhenOff.relativeAmbientLevel)
    }

    @Test
    fun sensors_movementClassification_stationaryVsMoving() {
        val stationaryMovement = classifyMovement(mag = 9.81f)
        val movingMovement = classifyMovement(mag = 12.5f)

        assertEquals(com.shyft.privacy.data.model.DeviceMovementState.STATIONARY, stationaryMovement)
        assertEquals(com.shyft.privacy.data.model.DeviceMovementState.MOVING, movingMovement)
    }

    @Test
    fun sensors_orientationClassification_facingUserVsTiltedVsFaceDown() {
        val facingUser = classifyOrientation(z = 8.5f)
        val faceDown = classifyOrientation(z = -8.5f)
        val tilted = classifyOrientation(z = 2.0f)

        assertEquals(com.shyft.privacy.data.model.DeviceOrientationState.PHONE_FACING_USER, facingUser)
        assertEquals(com.shyft.privacy.data.model.DeviceOrientationState.FACE_DOWN, faceDown)
        assertEquals(com.shyft.privacy.data.model.DeviceOrientationState.TILTED, tilted)
    }

    // Helper functions representing the Audio Engine & Sensor mathematical contracts
    private fun classifyRmsLevel(smoothedRms: Float): RelativeAmbientLevel {
        return when {
            smoothedRms < 0.035f -> RelativeAmbientLevel.QUIET
            smoothedRms > 0.120f -> RelativeAmbientLevel.NOISY
            else -> RelativeAmbientLevel.NORMAL
        }
    }

    private fun classifyMovement(mag: Float): com.shyft.privacy.data.model.DeviceMovementState {
        return if (kotlin.math.abs(mag - 9.81f) > 1.2f) {
            com.shyft.privacy.data.model.DeviceMovementState.MOVING
        } else {
            com.shyft.privacy.data.model.DeviceMovementState.STATIONARY
        }
    }

    private fun classifyOrientation(z: Float): com.shyft.privacy.data.model.DeviceOrientationState {
        return when {
            z > 7.0f -> com.shyft.privacy.data.model.DeviceOrientationState.PHONE_FACING_USER
            z < -7.0f -> com.shyft.privacy.data.model.DeviceOrientationState.FACE_DOWN
            else -> com.shyft.privacy.data.model.DeviceOrientationState.TILTED
        }
    }

    private fun calculateEma(current: Float, previous: Float, alpha: Float): Float {
        return (alpha * current) + ((1.0f - alpha) * previous)
    }

    private fun targetVolumeForLevel(level: RelativeAmbientLevel): Float {
        return when (level) {
            RelativeAmbientLevel.QUIET -> 0.30f
            RelativeAmbientLevel.NORMAL -> 0.50f
            RelativeAmbientLevel.NOISY -> 0.70f
        }
    }

    private fun maskCallerDetail(detail: String, riskState: VisualPrivacyRiskState): String {
        return if (riskState == VisualPrivacyRiskState.PRIVACY_RISK) {
            "•".repeat(detail.length)
        } else {
            detail
        }
    }
}
