package com.Kelasor.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.Kelasor.app.ui.theme.MessageAppTheme
import kotlinx.coroutines.delay

@Composable
fun AudioPlayer(
    audioUrl: String,
    title: String,
    artist: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(audioUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(exoPlayer) {
        while (true) {
            isPlaying = exoPlayer.isPlaying
            progress = if (exoPlayer.duration > 0) exoPlayer.currentPosition.toFloat() / exoPlayer.duration else 0f
            delay(500)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skip Backward Button
            IconButton(
                onClick = { exoPlayer.seekBack() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay10,
                    contentDescription = "Skip Back 10s",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Play/Pause Button with Glow
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(extendedColors.accent, extendedColors.accentSecondary)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            // Skip Forward Button
            IconButton(
                onClick = { exoPlayer.seekForward() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "Skip Forward 10s",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    title, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    artist, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), 
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Seek Bar
                Slider(
                    value = progress,
                    onValueChange = { 
                        progress = it
                        val seekTo = (it * exoPlayer.duration).toLong()
                        exoPlayer.seekTo(seekTo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = extendedColors.accent,
                        activeTrackColor = extendedColors.accent,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )
                )

                // Time Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentSec = (exoPlayer.currentPosition / 1000) % 60
                    val currentMin = (exoPlayer.currentPosition / 1000) / 60
                    val durationSec = (exoPlayer.duration.coerceAtLeast(0) / 1000) % 60
                    val durationMin = (exoPlayer.duration.coerceAtLeast(0) / 1000) / 60
                    
                    Text(
                        String.format("%02d:%02d", currentMin, currentSec),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        String.format("%02d:%02d", durationMin, durationSec),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
