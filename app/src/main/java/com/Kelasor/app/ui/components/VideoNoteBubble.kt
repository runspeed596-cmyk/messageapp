package com.Kelasor.app.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.domain.model.MessageStatus

// ═══════════════════════════════════════════════════════════════════════════════
// 🎥 Circular Video Note Playback Bubble
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Circular video playback bubble for inline display in chat.
 * Renders the video note in a circle shape with play/pause on tap.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoNoteBubble(
    mediaUrl: String,
    isMyMessage: Boolean,
    time: String,
    modifier: Modifier = Modifier,
    durationText: String? = null,
    status: MessageStatus = MessageStatus.SENT,
    reactions: Map<String, Int> = emptyMap(),
    myReaction: String? = null,
    onReactionClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    var isPlaying by remember { mutableStateOf(false) }
    val borderColor = if (isMyMessage) extendedColors.accent else extendedColors.accentSecondary
    // Resolve full URL from potentially relative path
    val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(mediaUrl) ?: ""
    android.util.Log.d("VideoNoteBubble", "📹 mediaUrl=$mediaUrl → fullUrl=$fullUrl")
    // Create cached ExoPlayer
    val exoPlayer = remember(fullUrl) {
        @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
        val cache = com.Kelasor.app.data.media.MediaCacheProvider.getCache(context)
        val upstreamFactory = androidx.media3.datasource.DefaultDataSource.Factory(context)
        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(fullUrl)))
                prepare()
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 1f
            }
    }
    // Release player on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        horizontalAlignment = if (isMyMessage) Alignment.End else Alignment.Start,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        // Circular video
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(3.dp, borderColor, CircleShape)
                .background(Color.Black, CircleShape)
                .clickable {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                    isPlaying = !isPlaying
                }
        ) {
            // Video surface
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
            )
            // Play icon overlay (when not playing)
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "پخش",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        // Duration and time row
        Box(
            modifier = Modifier.padding(top = 2.dp, start = 8.dp, end = 8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        if (!durationText.isNullOrBlank()) {
                            append(durationText)
                            append(" • ")
                        }
                        append(time)
                    },
                    style = MessageAppTypography.messageTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (isMyMessage) {
                    MessageStatusIcon(
                        status = status,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        // Reaction row
        if (reactions.isNotEmpty()) {
            ReactionRow(
                reactions = reactions,
                myReaction = myReaction,
                isMyMessage = isMyMessage,
                onReactionClick = onReactionClick,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 4.dp)
                    .offset(y = (-8).dp)
            )
        }
    }
}
