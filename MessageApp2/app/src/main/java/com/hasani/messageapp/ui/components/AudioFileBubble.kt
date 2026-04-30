package com.hasani.messageapp.ui.components

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
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.unit.dp
import com.hasani.messageapp.data.audio.AudioPlayerManager
import com.hasani.messageapp.data.audio.PlaybackState
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.util.Constants

/**
 * Audio file message bubble for displaying audio files (not voice recordings).
 * Shows music icon, file name, draggable seek slider, and play/pause controls.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioFileBubble(
    mediaUrl: String,
    fileName: String,
    durationMs: Long,
    isMyMessage: Boolean,
    audioPlayerManager: AudioPlayerManager,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val playbackInfo by audioPlayerManager.playbackInfo.collectAsState()
    
    // Resolve full URL
    val fullMediaUrl = if (mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://")) {
        mediaUrl
    } else {
        "${Constants.BASE_URL.removeSuffix("/")}$mediaUrl"
    }
    
    val isThisPlaying = playbackInfo.currentUrl == fullMediaUrl
    val isPlaying = isThisPlaying && playbackInfo.state == PlaybackState.PLAYING
    val progress = if (isThisPlaying) playbackInfo.progress else 0f
    val currentPositionMs = if (isThisPlaying) playbackInfo.currentPositionMs else 0L
    val actualDurationMs = if (isThisPlaying && playbackInfo.durationMs > 0) playbackInfo.durationMs else durationMs
    
    // Track if user is dragging slider
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val accentColor = if (isMyMessage) extendedColors.accent else MaterialTheme.colorScheme.primary
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .width(270.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Music icon with play button
        Box(
            modifier = Modifier
                .size(48.dp)
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
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // File name with music icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fileName.take(25) + if (fileName.length > 25) "..." else "",
                    style = MessageAppTypography.chatTime,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Draggable Seek Slider
            Slider(
                value = if (isDragging) dragProgress else progress,
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
                    .height(18.dp),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = accentColor.copy(alpha = 0.3f)
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            )
            
            // Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatAudioDuration(if (isDragging) (dragProgress * actualDurationMs).toLong() else currentPositionMs),
                    style = MessageAppTypography.messageTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatAudioDuration(actualDurationMs),
                    style = MessageAppTypography.messageTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAudioDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
