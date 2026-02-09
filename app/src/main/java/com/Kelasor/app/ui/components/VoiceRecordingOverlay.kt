package com.Kelasor.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme

/**
 * VoiceRecordingOverlay displays during active voice recording.
 * Shows recording duration, waveform visualization, and cancel option.
 */

@Composable
fun VoiceRecordingOverlay(
    durationMs: Long,
    amplitude: Int,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors

    // Pulsing animation for recording indicator
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Cancel button
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "لغو",
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Recording indicator and duration
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Pulsing red dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color.Red)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Duration text
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Simple waveform visualization
            WaveformVisualizer(
                amplitude = amplitude,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Recording microphone icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(extendedColors.accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "در حال ضبط",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun WaveformVisualizer(
    amplitude: Int,
    modifier: Modifier = Modifier
) {
    // Normalize amplitude (0-32767) to bar heights
    val normalizedAmplitude = (amplitude / 32767f).coerceIn(0.1f, 1f)
    
    Row(
        modifier = modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(16) { index ->
            // Create variation based on index and amplitude
            val variationFactor = when {
                index % 3 == 0 -> 0.7f
                index % 2 == 0 -> 0.85f
                else -> 1f
            }
            val barHeight = (normalizedAmplitude * variationFactor * 24).coerceAtLeast(4f)
            
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
