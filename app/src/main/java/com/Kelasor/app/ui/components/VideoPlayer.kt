package com.Kelasor.app.ui.components

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onFullScreenClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    var isFullScreen by remember { mutableStateOf(false) }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    if (isFullScreen) {
        FullscreenPlayer(
            exoPlayer = exoPlayer,
            onDismiss = { isFullScreen = false }
        )
    } else {
        CompactPlayer(
            exoPlayer = exoPlayer,
            modifier = modifier,
            onFullScreenToggle = {
                if (onFullScreenClick != null) {
                    onFullScreenClick()
                } else {
                    isFullScreen = true
                }
            }
        )
    }
}

@Composable
fun CompactPlayer(
    exoPlayer: ExoPlayer,
    modifier: Modifier,
    onFullScreenToggle: () -> Unit
) {
    VideoPlayerBase(
        exoPlayer = exoPlayer,
        modifier = modifier,
        onFullScreenToggle = onFullScreenToggle,
        isFullScreen = false
    )
}

@Composable
fun FullscreenPlayer(
    exoPlayer: ExoPlayer,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            VideoPlayerBase(
                exoPlayer = exoPlayer,
                modifier = Modifier.fillMaxSize(),
                onFullScreenToggle = onDismiss,
                isFullScreen = true
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerBase(
    exoPlayer: ExoPlayer,
    modifier: Modifier,
    onFullScreenToggle: () -> Unit,
    isFullScreen: Boolean
) {
    val context = LocalContext.current
    
    var isPlaying by remember { mutableStateOf(exoPlayer.isPlaying) }
    var currentPosition by remember { mutableLongStateOf(exoPlayer.currentPosition) }
    var duration by remember { mutableLongStateOf(exoPlayer.duration) }
    var showControls by remember { mutableStateOf(true) }
    
    // Smooth Scrubbing Logic
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableFloatStateOf(0f) }

    // Use a listener to keep state in sync
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                duration = exoPlayer.duration.coerceAtLeast(0L)
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isDragging) {
                currentPosition = exoPlayer.currentPosition
            }
            duration = exoPlayer.duration.coerceAtLeast(0L)
            delay(500)
        }
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Premium Overlay
        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                // Center Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { exoPlayer.seekBack() }) {
                        Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    
                    IconButton(
                        onClick = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(onClick = { exoPlayer.seekForward() }) {
                        Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }

                // Bottom Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = if (isFullScreen) 24.dp else 8.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Time Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(if (isDragging) dragPosition.toLong() else currentPosition),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                        Text(
                            formatTime(duration),
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }

                    // Luxury Seekbar
                    val sliderValue = if (isDragging) dragPosition else currentPosition.toFloat()
                    Slider(
                        value = sliderValue.coerceIn(0f, duration.toFloat().coerceAtLeast(1f)),
                        onValueChange = { 
                            isDragging = true
                            dragPosition = it
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(dragPosition.toLong())
                            isDragging = false
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier.height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = com.Kelasor.app.ui.theme.MessageAppTheme.extendedColors.accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onFullScreenToggle) {
                            Icon(
                                imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
