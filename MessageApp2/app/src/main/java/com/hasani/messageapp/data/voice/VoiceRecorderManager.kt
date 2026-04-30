package com.hasani.messageapp.data.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VoiceRecorderManager handles audio recording using MediaRecorder.
 * Records to M4A (AAC) format for broad compatibility.
 */

// ═══════════════════════════════════════════════════════════════════════════════
// 🎙️ Recording State
// ═══════════════════════════════════════════════════════════════════════════════

enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    COMPLETED,
    ERROR
}

data class RecordingInfo(
    val state: RecordingState = RecordingState.IDLE,
    val durationMs: Long = 0L,
    val amplitude: Int = 0,
    val filePath: String? = null,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎙️ Voice Recorder Manager
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class VoiceRecorderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VoiceRecorderManager"
        private const val SAMPLE_RATE = 44100
        private const val BIT_RATE = 128000
    }

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _recordingInfo = MutableStateFlow(RecordingInfo())
    val recordingInfo: StateFlow<RecordingInfo> = _recordingInfo.asStateFlow()

    /**
     * Start recording audio to a new file.
     * @return true if recording started successfully
     */
    fun startRecording(): Boolean {
        if (_recordingInfo.value.state == RecordingState.RECORDING) {
            Log.w(TAG, "Already recording")
            return false
        }

        try {
            // Create output file
            val voiceDir = File(context.cacheDir, "voice_messages")
            if (!voiceDir.exists()) voiceDir.mkdirs()
            
            recordingFile = File(voiceDir, "voice_${System.currentTimeMillis()}.m4a")
            
            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(SAMPLE_RATE)
                setAudioEncodingBitRate(BIT_RATE)
                setOutputFile(recordingFile!!.absolutePath)
                
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _recordingInfo.value = RecordingInfo(
                state = RecordingState.RECORDING,
                durationMs = 0L,
                filePath = recordingFile?.absolutePath
            )

            Log.i(TAG, "🎙️ Recording started: ${recordingFile?.absolutePath}")
            return true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
            _recordingInfo.value = RecordingInfo(
                state = RecordingState.ERROR,
                error = e.message ?: "Failed to start recording"
            )
            releaseRecorder()
            return false
        }
    }

    /**
     * Stop recording and return the recorded file.
     * @return The recorded audio File, or null if recording failed
     */
    fun stopRecording(): File? {
        if (_recordingInfo.value.state != RecordingState.RECORDING) {
            Log.w(TAG, "Not currently recording")
            return null
        }

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val duration = System.currentTimeMillis() - recordingStartTime
            val file = recordingFile

            _recordingInfo.value = RecordingInfo(
                state = RecordingState.COMPLETED,
                durationMs = duration,
                filePath = file?.absolutePath
            )

            Log.i(TAG, "✅ Recording completed: ${file?.absolutePath}, duration: ${duration}ms")
            return file

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to stop recording", e)
            _recordingInfo.value = RecordingInfo(
                state = RecordingState.ERROR,
                error = e.message ?: "Failed to stop recording"
            )
            releaseRecorder()
            return null
        }
    }

    /**
     * Cancel recording and delete the recorded file.
     */
    fun cancelRecording() {
        Log.i(TAG, "🗑️ Recording cancelled")
        releaseRecorder()
        recordingFile?.delete()
        recordingFile = null
        _recordingInfo.value = RecordingInfo(state = RecordingState.IDLE)
    }

    /**
     * Get current recording duration in milliseconds.
     */
    fun getCurrentDuration(): Long {
        return if (_recordingInfo.value.state == RecordingState.RECORDING) {
            System.currentTimeMillis() - recordingStartTime
        } else {
            _recordingInfo.value.durationMs
        }
    }

    /**
     * Get current audio amplitude (for waveform visualization).
     * @return amplitude value (0-32767)
     */
    fun getAmplitude(): Int {
        return try {
            mediaRecorder?.maxAmplitude ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Check if currently recording.
     */
    fun isRecording(): Boolean = _recordingInfo.value.state == RecordingState.RECORDING

    /**
     * Reset state to idle.
     */
    fun reset() {
        releaseRecorder()
        recordingFile = null
        _recordingInfo.value = RecordingInfo(state = RecordingState.IDLE)
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing recorder", e)
        }
        mediaRecorder = null
    }
}
