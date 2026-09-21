package com.shyft.privacy.data.repository

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.shyft.privacy.data.model.AcousticEnvironment
import com.shyft.privacy.data.model.AudioPrivacyState
import com.shyft.privacy.data.model.AudioShieldLevel
import com.shyft.privacy.data.model.DemoScenario
import com.shyft.privacy.data.model.DemoState
import com.shyft.privacy.data.model.EnvironmentalSensor
import com.shyft.privacy.data.model.PrivacyZone
import com.shyft.privacy.data.model.SensorState
import com.shyft.privacy.data.model.ShieldMode
import com.shyft.privacy.data.model.VisualPrivacyRiskState
import com.shyft.privacy.data.model.VisualPrivacyState
import com.shyft.privacy.data.model.VisualProtectionLevel
import com.shyft.privacy.data.model.ZoneType
import com.shyft.privacy.engine.AudioPrivacyEngine
import com.shyft.privacy.engine.SensorIntelligenceEngine
import com.shyft.privacy.engine.VisualPrivacyEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface IPrivacyStateRepository {
    val visualPrivacyState: StateFlow<VisualPrivacyState>
    val audioPrivacyState: StateFlow<AudioPrivacyState>
    val privacyZones: StateFlow<List<PrivacyZone>>
    val sensorState: StateFlow<SensorState>
    val demoState: StateFlow<DemoState>

    fun updateVisualPrivacy(transform: (VisualPrivacyState) -> VisualPrivacyState)
    fun updateAudioPrivacy(transform: (AudioPrivacyState) -> AudioPrivacyState)
    fun updateSensors(transform: (SensorState) -> SensorState)
    fun toggleZone(zoneId: String)
    fun triggerDemoScenario(scenario: DemoScenario)
    fun resetDemo()

    fun startVisualPrivacyEngine(context: Context, lifecycleOwner: LifecycleOwner)
    fun stopVisualPrivacyEngine()

    fun startAudioPrivacyEngine(context: Context)
    fun stopAudioPrivacyEngine()

    fun startSensorEngine(context: Context)
    fun stopSensorEngine()
}

class PrivacyStateRepository : IPrivacyStateRepository {

    private var visualEngine: VisualPrivacyEngine? = null
    private var audioEngine: AudioPrivacyEngine? = null
    private var sensorEngine: SensorIntelligenceEngine? = null

    private val _visualPrivacyState = MutableStateFlow(VisualPrivacyState())
    override val visualPrivacyState: StateFlow<VisualPrivacyState> = _visualPrivacyState.asStateFlow()

    private val _audioPrivacyState = MutableStateFlow(AudioPrivacyState())
    override val audioPrivacyState: StateFlow<AudioPrivacyState> = _audioPrivacyState.asStateFlow()

    private val _privacyZones = MutableStateFlow(
        listOf(
            PrivacyZone(
                id = "home_zone",
                name = "Home Safe Space",
                type = ZoneType.HOME,
                radiusMeters = 50,
                isCurrentlyActive = true,
                visualProtectionLevel = VisualProtectionLevel.STANDARD,
                audioShieldLevel = AudioShieldLevel.OFF
            ),
            PrivacyZone(
                id = "office_zone",
                name = "Work Headquarters",
                type = ZoneType.WORK,
                radiusMeters = 100,
                isCurrentlyActive = false,
                visualProtectionLevel = VisualProtectionLevel.HIGH,
                audioShieldLevel = AudioShieldLevel.VOICE_MASKING
            ),
            PrivacyZone(
                id = "public_transit",
                name = "Metro / Bus Transit",
                type = ZoneType.PUBLIC,
                radiusMeters = 200,
                isCurrentlyActive = false,
                visualProtectionLevel = VisualProtectionLevel.MAXIMUM,
                audioShieldLevel = AudioShieldLevel.WHISPER_GUARD
            )
        )
    )
    override val privacyZones: StateFlow<List<PrivacyZone>> = _privacyZones.asStateFlow()

    private val _sensorState = MutableStateFlow(
        SensorState(
            accelerometerMagnitude = 9.81f,
            sensors = listOf(
                EnvironmentalSensor("lux_1", "Ambient Light Sensor", "Illuminance", "420 lux", "lux", "Normal indoor lighting", true),
                EnvironmentalSensor("prox_1", "Proximity Sensor", "Distance", "5.0 cm", "cm", "No obstruction detected", true),
                EnvironmentalSensor("accel_1", "3-Axis Accelerometer", "Motion Vector", "9.81 m/s²", "m/s²", "Device stationary", true),
                EnvironmentalSensor("gyro_1", "Gyroscope", "Angular Velocity", "0.02 rad/s", "rad/s", "Stable orientation", true)
            )
        )
    )
    override val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private val _demoState = MutableStateFlow(DemoState())
    override val demoState: StateFlow<DemoState> = _demoState.asStateFlow()

    override fun updateVisualPrivacy(transform: (VisualPrivacyState) -> VisualPrivacyState) {
        _visualPrivacyState.update(transform)
    }

    override fun updateAudioPrivacy(transform: (AudioPrivacyState) -> AudioPrivacyState) {
        _audioPrivacyState.update(transform)
    }

    override fun updateSensors(transform: (SensorState) -> SensorState) {
        _sensorState.update(transform)
    }

    override fun toggleZone(zoneId: String) {
        _privacyZones.update { list ->
            list.map { zone ->
                if (zone.id == zoneId) zone.copy(isCurrentlyActive = !zone.isCurrentlyActive) else zone
            }
        }
    }

    override fun startVisualPrivacyEngine(context: Context, lifecycleOwner: LifecycleOwner) {
        if (visualEngine == null) {
            visualEngine = VisualPrivacyEngine(context.applicationContext, this)
        }
        visualEngine?.startMonitoring(lifecycleOwner)
    }

    override fun stopVisualPrivacyEngine() {
        visualEngine?.stopMonitoring()
        visualEngine = null
    }

    override fun startAudioPrivacyEngine(context: Context) {
        if (audioEngine == null) {
            audioEngine = AudioPrivacyEngine(context.applicationContext, this)
        }
        audioEngine?.startMonitoring()
    }

    override fun stopAudioPrivacyEngine() {
        audioEngine?.stopMonitoring()
        audioEngine = null
    }

    override fun startSensorEngine(context: Context) {
        if (sensorEngine == null) {
            sensorEngine = SensorIntelligenceEngine(context.applicationContext, this)
        }
        sensorEngine?.startListening()
    }

    override fun stopSensorEngine() {
        sensorEngine?.stopListening()
        sensorEngine = null
    }

    override fun triggerDemoScenario(scenario: DemoScenario) {
        // Stop real camera when running sandbox simulation
        stopVisualPrivacyEngine()

        when (scenario) {
            DemoScenario.SHOULDER_SURFER_DETECTED -> {
                _visualPrivacyState.update {
                    it.copy(
                        isMonitoringActive = true,
                        isRealCameraActive = false,
                        isSimulationActive = true,
                        riskState = VisualPrivacyRiskState.PRIVACY_RISK,
                        faceCount = 2,
                        primaryUserDetected = true,
                        possibleSecondPersonDetected = true,
                        headOrientationApproximation = "[SIMULATED] Second face (Yaw: 12°, Attention: Toward Phone)",
                        protectionLevel = VisualProtectionLevel.MAXIMUM,
                        activeShieldMode = ShieldMode.FULL_SHIELD,
                        statusMessage = "[SIMULATION STATE] Simulated second-person anomaly looking over shoulder."
                    )
                }
                _demoState.update {
                    it.copy(
                        isDemoActive = true,
                        currentScenario = scenario,
                        simulationMessage = "[SIMULATION STATE] Simulated Shoulder Surfer anomaly! Privacy Risk active."
                    )
                }
            }
            DemoScenario.EAVESDROPPING_ALERT -> {
                _audioPrivacyState.update {
                    it.copy(
                        voiceEavesdropRisk = true,
                        environment = AcousticEnvironment.BUSY_STREET,
                        shieldLevel = AudioShieldLevel.WHISPER_GUARD
                    )
                }
                _demoState.update {
                    it.copy(
                        isDemoActive = true,
                        currentScenario = scenario,
                        simulationMessage = "[SIMULATION STATE] Elevated ambient voice level detected. Audio Shield engaged."
                    )
                }
            }
            DemoScenario.ENTERED_PUBLIC_ZONE -> {
                _privacyZones.update { list ->
                    list.map { if (it.type == ZoneType.PUBLIC) it.copy(isCurrentlyActive = true) else it.copy(isCurrentlyActive = false) }
                }
                _visualPrivacyState.update {
                    it.copy(
                        isMonitoringActive = true,
                        isSimulationActive = true,
                        protectionLevel = VisualProtectionLevel.HIGH,
                        riskState = VisualPrivacyRiskState.PRIVACY_RISK,
                        statusMessage = "[SIMULATION STATE] Entered Public Zone. Automatic Privacy Risk engaged."
                    )
                }
                _demoState.update {
                    it.copy(
                        isDemoActive = true,
                        currentScenario = scenario,
                        simulationMessage = "[SIMULATION STATE] Entered Public Zone (Metro/Bus). SHYFT elevated privacy shields."
                    )
                }
            }
            DemoScenario.PROTECTED_APP_OPENED -> {
                _demoState.update {
                    it.copy(
                        isDemoActive = true,
                        currentScenario = scenario,
                        simulatedAppPackage = "com.google.android.apps.wallet.nfcrel",
                        simulationMessage = "[SIMULATION STATE] Fictional Payment Demo app active with sensitive content masking."
                    )
                }
            }
            DemoScenario.IDLE -> resetDemo()
        }
    }

    override fun resetDemo() {
        stopVisualPrivacyEngine()
        _visualPrivacyState.update { VisualPrivacyState() }
        _audioPrivacyState.update { AudioPrivacyState() }
        _privacyZones.update { list ->
            list.map { if (it.type == ZoneType.HOME) it.copy(isCurrentlyActive = true) else it.copy(isCurrentlyActive = false) }
        }
        _demoState.update { DemoState() }
    }
}
