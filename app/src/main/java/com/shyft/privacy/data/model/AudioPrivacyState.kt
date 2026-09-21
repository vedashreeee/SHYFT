package com.shyft.privacy.data.model

data class AudioPrivacyState(
    val isMonitoringActive: Boolean = false,
    val hasMicPermission: Boolean = false,
    val isAudioRecordInitialized: Boolean = false,
    val lastReadSampleCount: Int = 0,
    val rawRmsAmplitude: Float = 0f,
    val smoothedRmsAmplitude: Float = 0.05f,
    val relativeAmbientLevel: RelativeAmbientLevel = RelativeAmbientLevel.NORMAL,
    val recommendedVolumeFactor: Float = 0.55f,
    val systemVolumeLevel: Int = 7,
    val maxSystemVolumeLevel: Int = 15,
    val manualOverrideActive: Boolean = false,
    val manualVolumeFactor: Float = 0.55f,
    val shieldLevel: AudioShieldLevel = AudioShieldLevel.VOICE_MASKING,
    val environment: AcousticEnvironment = AcousticEnvironment.OFFICE,
    val voiceEavesdropRisk: Boolean = false,
    val statusMessage: String = "Audio Privacy inactive. Enable to monitor ambient sound level."
)
