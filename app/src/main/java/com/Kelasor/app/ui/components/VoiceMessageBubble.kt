package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.Kelasor.app.data.audio.AudioPlayerManager
import com.Kelasor.app.data.audio.PlaybackState
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures

/**
 * VoiceMessageBubble displays a voice message with playback controls.
 * Shows play/pause button, draggable seek slider, and duration.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMessageBubble(
    mediaUrl: String,
    durationMs: Long,
    isMyMessage: Boolean,
    audioPlayerManager: AudioPlayerManager,
    amplitudes: List<Int>? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val playbackInfo by audioPlayerManager.playbackInfo.collectAsState()
    
    // Resolve URL: Prepend BASE_URL if relative path
    val fullMediaUrl = if (mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://")) {
        mediaUrl
    } else {
        "${com.Kelasor.app.util.Constants.BASE_URL.removeSuffix("/")}$mediaUrl"
    }
    
    val isThisPlaying = playbackInfo.currentUrl == fullMediaUrl
    val isPlaying = isThisPlaying && playbackInfo.state == PlaybackState.PLAYING
    val progress = if (isThisPlaying) playbackInfo.progress else 0f
    val currentPositionMs = if (isThisPlaying) playbackInfo.currentPositionMs else 0L
    val actualDurationMs = if (isThisPlaying && playbackInfo.durationMs > 0) playbackInfo.durationMs else durationMs
    
    // Track if user is dragging slider
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    // Background color based on message owner
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val accentColor = if (isMyMessage) {
        extendedColors.accent
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .width(260.dp), // Slightly wider for waveform
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable { audioPlayerManager.togglePlayPause(fullMediaUrl) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "توقف" else "پخش",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Waveform/Slider and Duration
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val currentProgress = if (isDragging) dragProgress else progress
            
            if (!amplitudes.isNullOrEmpty()) {
                // Render Waveform
                Waveform(
                    amplitudes = amplitudes,
                    progress = currentProgress,
                    onProgressChange = { 
                        isDragging = true
                        dragProgress = it
                    },
                    onProgressChangeFinished = {
                        audioPlayerManager.seekToProgress(dragProgress)
                        isDragging = false
                    },
                    activeColor = accentColor,
                    inactiveColor = accentColor.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )
            } else {
                // Fallback Slider
                Slider(
                    value = currentProgress,
                    onValueChange = { newValue ->
                        isDragging = true
                        dragProgress = newValue
                    },
                    onValueChangeFinished = {
                        audioPlayerManager.seekToProgress(dragProgress)
                        isDragging = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = accentColor.copy(alpha = 0.3f)
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Duration text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatVoiceDuration(if (isDragging) (dragProgress * actualDurationMs).toLong() else currentPositionMs),
                    style = MessageAppTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatVoiceDuration(actualDurationMs),
                    style = MessageAppTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun Waveform(
    amplitudes: List<Int>,
    progress: Float,
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: () -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                         val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                         onProgressChange(newProgress)
                    },
                    onDragEnd = { onProgressChangeFinished() },
                    onHorizontalDrag = { change, _ ->
                        val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onProgressChange(newProgress)
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onProgressChange(newProgress)
                    onProgressChangeFinished()
                }
            }
    ) {
        val barWidth = 3.dp.toPx()
        val gap = 1.dp.toPx()
        val totalBars = (size.width / (barWidth + gap)).toInt()
        
        // Normalize amplitudes to fit in bars
        val resampledAmplitudes = if (amplitudes.size > totalBars) {
            val chunkSize = amplitudes.size / totalBars
            (0 until totalBars).map { i ->
                val start = i * chunkSize
                val end = (start + chunkSize).coerceAtMost(amplitudes.size)
                if (start < end) amplitudes.subList(start, end).maxOrNull() ?: 0 else 0
            }
        } else {
             amplitudes
        }
        
        val maxAmp = resampledAmplitudes.maxOrNull()?.toFloat() ?: 1f
        val normFactor = if (maxAmp > 0) size.height / maxAmp else 0f
        
        val barTotalWidth = barWidth + gap
        
        // Center the waveform vertically
        val centerY = size.height / 2
        
        resampledAmplitudes.forEachIndexed { index, amp ->
            val barHeight = (amp * normFactor).coerceAtLeast(2.dp.toPx()) // Min height
            val x = index * barTotalWidth
            
            val isPlayed = (x / size.width) <= progress
            
            drawRoundRect(
                color = if (isPlayed) activeColor else inactiveColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, centerY - barHeight / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )
        }
    }
}

/**
 * Format duration in milliseconds to MM:SS format.
 */
private fun formatVoiceDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
