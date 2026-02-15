package com.Kelasor.app.ui.components

import android.Manifest
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.Kelasor.app.data.video.VideoNoteRecorderManager
import com.Kelasor.app.data.video.VideoNoteRecordingState
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════════
// 🎥 Circular Video Note Recorder Overlay
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Full-screen overlay that shows a circular camera preview for recording
 * circular video messages (video notes). Max 60 seconds.
 */
@Composable
fun CircularVideoRecorder(
    videoNoteRecorderManager: VideoNoteRecorderManager,
    onRecordComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val extendedColors = MessageAppTheme.extendedColors
    val maxDurationMs = VideoNoteRecorderManager.MAX_DURATION_MS
    var currentDuration by remember { mutableLongStateOf(0L) }
    val recordingInfo by videoNoteRecorderManager.recordingInfo.collectAsState()
    // Pulsing animation for recording ring
    val infiniteTransition = rememberInfiniteTransition(label = "recordPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    // Create PreviewView
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    // Bind camera on composition
    LaunchedEffect(Unit) {
        Log.d("CircularVideoRecorder", "📹 Binding camera...")
        videoNoteRecorderManager.bindCamera(lifecycleOwner, previewView)
    }
    // Start recording once camera is ready
    val isCameraReady by videoNoteRecorderManager.cameraReady.collectAsState()
    LaunchedEffect(isCameraReady) {
        if (isCameraReady && recordingInfo.state != VideoNoteRecordingState.RECORDING) {
            Log.d("CircularVideoRecorder", "📹 Camera ready, starting recording...")
            videoNoteRecorderManager.startRecording()
        }
    }
    // Watch for recording completion (async from VideoRecordEvent.Finalize)
    LaunchedEffect(recordingInfo.state) {
        if (recordingInfo.state == VideoNoteRecordingState.COMPLETED) {
            Log.d("CircularVideoRecorder", "✅ Recording completed, calling onRecordComplete")
            onRecordComplete()
        }
    }
    // Track duration and auto-stop
    LaunchedEffect(Unit) {
        while (true) {
            currentDuration = videoNoteRecorderManager.getCurrentDuration()
            if (currentDuration >= maxDurationMs) {
                videoNoteRecorderManager.stopRecording()
                break
            }
            delay(100)
        }
    }
    // Release camera on dispose
    DisposableEffect(Unit) {
        onDispose {
            videoNoteRecorderManager.releaseCamera()
        }
    }
    // Format time
    val seconds = (currentDuration / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val timeText = String.format("%d:%02d", minutes, remainingSeconds)
    val progress = (currentDuration.toFloat() / maxDurationMs).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) { /* consume clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular camera preview with progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.scale(pulseScale)
            ) {
                // Progress ring (background track)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(240.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    strokeWidth = 4.dp
                )
                // Progress ring (elapsed indicator)
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(240.dp),
                    color = Color.Red,
                    strokeWidth = 4.dp
                )
                // Circular camera preview
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.Red, CircleShape)
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timer display
            Text(
                text = timeText,
                style = MessageAppTypography.appBarTitle,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls row
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                IconButton(
                    onClick = {
                        videoNoteRecorderManager.cancelRecording()
                        onCancel()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "لغو",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                // Stop / Send button — only stops recording.
                // onRecordComplete is called by LaunchedEffect when
                // recording state transitions to COMPLETED
                IconButton(
                    onClick = {
                        Log.d("CircularVideoRecorder", "🛑 Stop button pressed")
                        videoNoteRecorderManager.stopRecording()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "ارسال",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
