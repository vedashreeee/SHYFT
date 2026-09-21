package com.shyft.privacy.engine

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.data.model.VisualPrivacyState
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import java.util.concurrent.Executors
import kotlin.math.abs

class VisualPrivacyEngine(
    private val context: Context,
    private val repository: IPrivacyStateRepository
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .enableTracking()
        .build()

    private val faceDetector: FaceDetector = FaceDetection.getClient(detectorOptions)

    // Hysteresis & Debounce tracking
    private var riskStartTimeMs: Long = 0L
    private var clearStartTimeMs: Long = 0L
    private var currentState: VisualPrivacyRiskState = VisualPrivacyRiskState.NO_PRIVACY_RISK
    private var hasVibratedForCurrentRisk: Boolean = false

    private val STABLE_RISK_REQUIRED_MS = 1200L // 1200 ms of stable risk before PRIVACY_RISK
    private val RESTORATION_DELAY_MS = 400L     // 400 ms delay before restoring NO_PRIVACY_RISK

    fun startMonitoring(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                cameraProvider?.unbindAll()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processFrame(imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    imageAnalysis
                )

                riskStartTimeMs = 0L
                clearStartTimeMs = 0L
                currentState = VisualPrivacyRiskState.NO_PRIVACY_RISK
                hasVibratedForCurrentRisk = false

                repository.updateVisualPrivacy {
                    it.copy(
                        isMonitoringActive = true,
                        isRealCameraActive = true,
                        isSimulationActive = false,
                        statusMessage = "REAL CAMERA DETECTION: Front camera active with ML Kit Face Analysis."
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                repository.updateVisualPrivacy {
                    it.copy(
                        isMonitoringActive = false,
                        isRealCameraActive = false,
                        statusMessage = "Camera initialization failed: ${e.localizedMessage}"
                    )
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun stopMonitoring() {
        try {
            cameraProvider?.unbindAll()
            cameraProvider = null
            riskStartTimeMs = 0L
            clearStartTimeMs = 0L
            currentState = VisualPrivacyRiskState.NO_PRIVACY_RISK
            hasVibratedForCurrentRisk = false

            repository.updateVisualPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    isRealCameraActive = false,
                    riskState = VisualPrivacyRiskState.NO_PRIVACY_RISK,
                    faceCount = 0,
                    primaryUserDetected = false,
                    possibleSecondPersonDetected = false,
                    headOrientationApproximation = "Camera released",
                    stabilityTimerText = "Idle",
                    statusMessage = "Visual Privacy monitoring stopped. Camera released."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                evaluateFaceTelemetry(faces)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun evaluateFaceTelemetry(faces: List<Face>) {
        val currentTime = System.currentTimeMillis()
        val count = faces.size

        if (count == 0) {
            evaluateStateTransition(
                currentTime = currentTime,
                count = 0,
                primaryDetected = false,
                secondDetected = false,
                secondOrientedTowardPhone = false,
                primaryOrientationStr = "No faces in view",
                secondOrientationStr = "None"
            )
            return
        }

        // Sort faces by bounding box area (largest face = primary user)
        val sortedFaces = faces.sortedByDescending { it.boundingBox.width() * it.boundingBox.height() }
        val primaryFace = sortedFaces.first()
        val secondFace = if (sortedFaces.size > 1) sortedFaces[1] else null

        val primaryEulerY = primaryFace.headEulerAngleY
        val primaryOrientationStr = "Primary User (Yaw: ${primaryEulerY.toInt()}°)"

        val hasSecondPerson = secondFace != null
        var secondPersonOrientedTowardPhone = false
        var secondOrientationStr = "None"

        if (secondFace != null) {
            val secondEulerY = secondFace.headEulerAngleY
            // Head orientation approximation: yaw between -35° and +35° indicates facing toward phone
            secondPersonOrientedTowardPhone = abs(secondEulerY) < 35f
            secondOrientationStr = "Second Person (Yaw: ${secondEulerY.toInt()}°, Attention: ${if (secondPersonOrientedTowardPhone) "Toward Phone" else "Away"})"
        }

        evaluateStateTransition(
            currentTime = currentTime,
            count = count,
            primaryDetected = true,
            secondDetected = hasSecondPerson,
            secondOrientedTowardPhone = secondPersonOrientedTowardPhone,
            primaryOrientationStr = primaryOrientationStr,
            secondOrientationStr = secondOrientationStr
        )
    }

    private fun evaluateStateTransition(
        currentTime: Long,
        count: Int,
        primaryDetected: Boolean,
        secondDetected: Boolean,
        secondOrientedTowardPhone: Boolean,
        primaryOrientationStr: String,
        secondOrientationStr: String
    ) {
        val isRiskConditionActive = primaryDetected && secondDetected && secondOrientedTowardPhone

        val nextState: VisualPrivacyRiskState
        val stabilityText: String

        if (isRiskConditionActive) {
            clearStartTimeMs = 0L

            if (riskStartTimeMs == 0L) {
                riskStartTimeMs = currentTime
            }

            val elapsedRiskMs = currentTime - riskStartTimeMs

            if (elapsedRiskMs >= STABLE_RISK_REQUIRED_MS) {
                nextState = VisualPrivacyRiskState.PRIVACY_RISK
                stabilityText = "Risk Stable (${elapsedRiskMs}ms / ${STABLE_RISK_REQUIRED_MS}ms)"
            } else {
                nextState = VisualPrivacyRiskState.PRIVACY_CHECKING
                stabilityText = "Checking Risk (${elapsedRiskMs}ms / ${STABLE_RISK_REQUIRED_MS}ms)"
            }
        } else {
            riskStartTimeMs = 0L

            if (currentState == VisualPrivacyRiskState.PRIVACY_RISK) {
                if (clearStartTimeMs == 0L) {
                    clearStartTimeMs = currentTime
                }

                val elapsedClearMs = currentTime - clearStartTimeMs

                if (elapsedClearMs >= RESTORATION_DELAY_MS) {
                    nextState = VisualPrivacyRiskState.NO_PRIVACY_RISK
                    clearStartTimeMs = 0L
                    stabilityText = "Clear (Restored after ${elapsedClearMs}ms)"
                } else {
                    nextState = VisualPrivacyRiskState.PRIVACY_RISK
                    stabilityText = "Restoring (${elapsedClearMs}ms / ${RESTORATION_DELAY_MS}ms)"
                }
            } else {
                nextState = VisualPrivacyRiskState.NO_PRIVACY_RISK
                clearStartTimeMs = 0L
                stabilityText = if (count == 0) "Clear (No faces)" else if (!secondDetected) "Clear (Single primary user)" else "Clear (Secondary user looking away)"
            }
        }

        // Single-pulse haptic feedback ONLY on transition INTO PRIVACY_RISK
        if (nextState == VisualPrivacyRiskState.PRIVACY_RISK && !hasVibratedForCurrentRisk) {
            triggerSubtleHaptic()
            hasVibratedForCurrentRisk = true
        } else if (nextState != VisualPrivacyRiskState.PRIVACY_RISK) {
            hasVibratedForCurrentRisk = false
        }

        currentState = nextState

        repository.updateVisualPrivacy {
            it.copy(
                riskState = nextState,
                faceCount = count,
                primaryUserDetected = primaryDetected,
                possibleSecondPersonDetected = secondDetected,
                headOrientationApproximation = if (secondDetected) "$primaryOrientationStr | $secondOrientationStr" else primaryOrientationStr,
                stabilityTimerText = stabilityText,
                statusMessage = when (nextState) {
                    VisualPrivacyRiskState.NO_PRIVACY_RISK -> "REAL CAMERA DETECTION: Clear surroundings."
                    VisualPrivacyRiskState.PRIVACY_CHECKING -> "REAL CAMERA DETECTION: Secondary viewer detected. Evaluating 1200ms stability..."
                    VisualPrivacyRiskState.PRIVACY_RISK -> "REAL CAMERA DETECTION: Privacy Risk active! Secondary viewer detected looking toward phone."
                }
            )
        }
    }

    private fun triggerSubtleHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(80)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
