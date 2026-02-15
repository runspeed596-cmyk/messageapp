package com.Kelasor.app.data.video

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 🎥 Video Note Recording State
// ═══════════════════════════════════════════════════════════════════════════════

enum class VideoNoteRecordingState {
    IDLE,
    RECORDING,
    COMPLETED,
    ERROR
}

data class VideoNoteRecordingInfo(
    val state: VideoNoteRecordingState = VideoNoteRecordingState.IDLE,
    val durationMs: Long = 0L,
    val filePath: String? = null,
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎥 Video Note Recorder Manager
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class VideoNoteRecorderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "VideoNoteRecorderMgr"
        const val MAX_DURATION_MS: Long = 60_000L // 60 seconds
    }

    private var recording: Recording? = null
    private var recordingFile: File? = null
    private var recordingStartTime: Long = 0L
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var isCancelled: Boolean = false
    private var recordingSessionId: Long = 0L

    private val _recordingInfo = MutableStateFlow(VideoNoteRecordingInfo())
    val recordingInfo: StateFlow<VideoNoteRecordingInfo> = _recordingInfo.asStateFlow()
    private val _cameraReady = MutableStateFlow(false)
    val cameraReady: StateFlow<Boolean> = _cameraReady.asStateFlow()

    /**
     * Bind camera preview and prepare video capture.
     * Must be called before startRecording.
     */
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        Log.i(TAG, "📹 bindCamera called")
        _cameraReady.value = false
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                provider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val qualitySelector = QualitySelector.from(Quality.SD)
                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture
                )
                _cameraReady.value = true
                Log.i(TAG, "📹 Camera bound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Camera bind failed", e)
                _cameraReady.value = false
                _recordingInfo.value = VideoNoteRecordingInfo(
                    state = VideoNoteRecordingState.ERROR,
                    error = e.message ?: "Camera bind failed"
                )
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Start recording a circular video note.
     */
    @androidx.annotation.OptIn(androidx.camera.video.ExperimentalPersistentRecording::class)
    fun startRecording(): Boolean {
        val capture = videoCapture ?: run {
            Log.e(TAG, "VideoCapture not initialized. Call bindCamera first.")
            return false
        }
        if (_recordingInfo.value.state == VideoNoteRecordingState.RECORDING) {
            Log.w(TAG, "Already recording")
            return false
        }
        isCancelled = false
        try {
            val videoDir = File(context.cacheDir, "video_notes")
            if (!videoDir.exists()) videoDir.mkdirs()
            val sessionId = System.currentTimeMillis()
            recordingSessionId = sessionId

            recordingFile = File(videoDir, "videonote_${sessionId}.mp4")
            val outputOptions = FileOutputOptions.Builder(recordingFile!!).build()

            recording = capture.output
                .prepareRecording(context, outputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            Log.i(TAG, "🎥 Recording started")
                        }
                        is VideoRecordEvent.Finalize -> {
                            // 🛡️ CRITICAL: Check if this event belongs to the ACTUAL active recording session.
                            // If user cancelled and restarted quickly, we might get a Finalize event from the OLD recording.
                            if (recordingSessionId != sessionId || isCancelled) {
                                Log.i(TAG, "🎥 Finalize received for a cancelled or old recording, ignoring")
                                return@start
                            }

                            if (event.hasError()) {
                                Log.e(TAG, "❌ Recording error: ${event.cause?.message}")
                                _recordingInfo.value = VideoNoteRecordingInfo(
                                    state = VideoNoteRecordingState.ERROR,
                                    error = event.cause?.message ?: "Recording error"
                                )
                            } else {
                                val duration = System.currentTimeMillis() - recordingStartTime
                                _recordingInfo.value = VideoNoteRecordingInfo(
                                    state = VideoNoteRecordingState.COMPLETED,
                                    durationMs = duration,
                                    filePath = recordingFile?.absolutePath
                                )
                                Log.i(TAG, "✅ Recording completed: ${recordingFile?.absolutePath}")
                            }
                        }
                    }
                }
            recordingStartTime = System.currentTimeMillis()
            _recordingInfo.value = VideoNoteRecordingInfo(
                state = VideoNoteRecordingState.RECORDING,
                durationMs = 0L,
                filePath = recordingFile?.absolutePath
            )
            return true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start recording", e)
            _recordingInfo.value = VideoNoteRecordingInfo(
                state = VideoNoteRecordingState.ERROR,
                error = e.message ?: "Failed to start recording"
            )
            return false
        }
    }

    /**
     * Stop recording and finalize the video file.
     */
    fun stopRecording() {
        Log.i(TAG, "🛑 stopRecording called")
        recording?.stop()
        recording = null
    }

    /**
     * Cancel recording and delete the file.
     */
    fun cancelRecording() {
        Log.i(TAG, "❌ cancelRecording called")
        isCancelled = true
        recordingSessionId = -1L // 🛑 Invalidate session immediately
        recording?.stop()
        recording = null
        recordingFile?.delete()
        recordingFile = null
        _recordingInfo.value = VideoNoteRecordingInfo(state = VideoNoteRecordingState.IDLE)
    }

    /**
     * Get current recording duration in milliseconds.
     */
    fun getCurrentDuration(): Long {
        return if (_recordingInfo.value.state == VideoNoteRecordingState.RECORDING) {
            System.currentTimeMillis() - recordingStartTime
        } else {
            _recordingInfo.value.durationMs
        }
    }

    /**
     * Reset state to idle. Call after sending or cancelling.
     */
    fun reset() {
        Log.i(TAG, "🔄 reset called")
        isCancelled = false
        recording = null
        recordingFile = null
        recordingSessionId = -1L // 🛑 Invalidate session
        _cameraReady.value = false
        _recordingInfo.value = VideoNoteRecordingInfo(state = VideoNoteRecordingState.IDLE)
    }

    /**
     * Release camera resources.
     */
    fun releaseCamera() {
        Log.i(TAG, "📷 releaseCamera called")
        cameraProvider?.unbindAll()
        cameraProvider = null
        videoCapture = null
    }
}
