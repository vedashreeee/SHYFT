package com.shyft.privacy.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.shyft.privacy.data.model.AudioPrivacyState
import com.shyft.privacy.data.model.RelativeAmbientLevel
import com.shyft.privacy.data.repository.IPrivacyStateRepository
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

class AudioPrivacyEngine(
    private val context: Context,
    private val repository: IPrivacyStateRepository
) {
    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private var workerThread: Thread? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    // Exponential Moving Average (EMA) smoothing alpha
    private val EMA_ALPHA = 0.15f
    private var smoothedRms = 0.05f
    private var currentTargetVolumeFactor = 0.50f
    private var currentActualVolumeFactor = 0.50f

    companion object {
        private const val TAG = "SHYFT_AUDIO_DIAG"
    }

    @SuppressLint("MissingPermission")
    fun startMonitoring() {
        if (isRecording.get()) return

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        
        // getMinBufferSize returns buffer size in BYTES
        val minBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (minBufferSizeInBytes <= 0) {
            Log.e(TAG, "AudioRecord getMinBufferSize failed with code: $minBufferSizeInBytes")
            repository.updateAudioPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    isAudioRecordInitialized = false,
                    statusMessage = "Audio hardware initialization error (invalid buffer size: $minBufferSizeInBytes)."
                )
            }
            return
        }

        // Allocate internal AudioRecord byte buffer safely (at least 2x minBufferSize)
        val bufferSizeInBytes = maxOf(minBufferSizeInBytes * 2, 4096)
        // Convert to SHORT count for ShortArray buffer
        val shortBufferSize = bufferSizeInBytes / 2

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSizeInBytes
            )

            val isInitialized = audioRecord?.state == AudioRecord.STATE_INITIALIZED
            Log.d(TAG, "AudioRecord created: sampleRate=$sampleRate, minBytes=$minBufferSizeInBytes, allocBytes=$bufferSizeInBytes, initialized=$isInitialized")

            if (!isInitialized) {
                Log.e(TAG, "AudioRecord failed to initialize! state=${audioRecord?.state}")
                repository.updateAudioPrivacy {
                    it.copy(
                        isMonitoringActive = false,
                        isAudioRecordInitialized = false,
                        statusMessage = "Microphone initialization failed (state != INITIALIZED)."
                    )
                }
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            isRecording.set(true)

            repository.updateAudioPrivacy {
                it.copy(
                    isMonitoringActive = true,
                    hasMicPermission = true,
                    isAudioRecordInitialized = true,
                    statusMessage = "Relative Ambient Level monitoring active. Audio frames processed in RAM and discarded immediately."
                )
            }

            workerThread = Thread {
                val audioBuffer = ShortArray(shortBufferSize)
                while (isRecording.get()) {
                    val readSize = audioRecord?.read(audioBuffer, 0, shortBufferSize) ?: 0
                    if (readSize > 0) {
                        // 1. Calculate short-term raw RMS amplitude (normalized [0.0, 1.0])
                        var sumSquares = 0.0
                        for (i in 0 until readSize) {
                            val sample = audioBuffer[i] / 32768.0
                            sumSquares += sample * sample
                        }
                        val rawRms = sqrt(sumSquares / readSize).toFloat()

                        // 2. Clear buffer array immediately (zero out memory for privacy safety)
                        audioBuffer.fill(0)

                        // 3. Smooth RMS measurement using Exponential Moving Average
                        smoothedRms = (EMA_ALPHA * rawRms) + ((1.0f - EMA_ALPHA) * smoothedRms)

                        // 4. Classify Relative Ambient Level
                        val level = when {
                            smoothedRms < 0.035f -> RelativeAmbientLevel.QUIET
                            smoothedRms > 0.120f -> RelativeAmbientLevel.NOISY
                            else -> RelativeAmbientLevel.NORMAL
                        }

                        // Log safe diagnostic metadata ONLY (No audio samples logged)
                        Log.d(TAG, "AudioRecord read: $readSize samples, rawRMS=$rawRms, smoothedRMS=$smoothedRms, level=${level.name}")

                        // 5. Compute smooth target adaptive volume recommendation
                        val targetFactor = when (level) {
                            RelativeAmbientLevel.QUIET -> 0.30f
                            RelativeAmbientLevel.NORMAL -> 0.50f
                            RelativeAmbientLevel.NOISY -> 0.70f
                        }

                        currentTargetVolumeFactor = targetFactor
                        // Smooth interpolation (lerp) to prevent rapid volume jumps
                        currentActualVolumeFactor += (currentTargetVolumeFactor - currentActualVolumeFactor) * 0.1f

                        val maxSystemVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
                        val currentSysVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7

                        repository.updateAudioPrivacy { currentState ->
                            currentState.copy(
                                isAudioRecordInitialized = true,
                                lastReadSampleCount = readSize,
                                rawRmsAmplitude = rawRms,
                                smoothedRmsAmplitude = smoothedRms,
                                relativeAmbientLevel = level,
                                recommendedVolumeFactor = currentActualVolumeFactor,
                                systemVolumeLevel = currentSysVol,
                                maxSystemVolumeLevel = maxSystemVol,
                                statusMessage = "Relative Ambient Level: ${level.name} (Smoothed RMS: ${String.format("%.3f", smoothedRms)})"
                            )
                        }
                    } else {
                        Log.e(TAG, "AudioRecord read returned non-positive code: $readSize")
                        repository.updateAudioPrivacy { currentState ->
                            currentState.copy(
                                lastReadSampleCount = readSize,
                                statusMessage = "AudioRecord read returned code: $readSize"
                            )
                        }
                    }

                    try {
                        Thread.sleep(100)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            }.apply { start() }

        } catch (e: Exception) {
            Log.e(TAG, "Audio recording start exception: ${e.localizedMessage}", e)
            isRecording.set(false)
            repository.updateAudioPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    isAudioRecordInitialized = false,
                    statusMessage = "Audio recording start error: ${e.localizedMessage}"
                )
            }
        }
    }

    fun stopMonitoring() {
        isRecording.set(false)
        try {
            workerThread?.interrupt()
            workerThread = null

            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            Log.d(TAG, "AudioRecord stopped and released.")

            repository.updateAudioPrivacy {
                it.copy(
                    isMonitoringActive = false,
                    isAudioRecordInitialized = false,
                    lastReadSampleCount = 0,
                    rawRmsAmplitude = 0f,
                    relativeAmbientLevel = RelativeAmbientLevel.NORMAL,
                    smoothedRmsAmplitude = 0.05f,
                    statusMessage = "Audio Privacy stopped. Microphone released."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord: ${e.localizedMessage}", e)
        }
    }
}
