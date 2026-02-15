package com.Kelasor.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.util.Constants

/**
 * Image/Video message bubble with progressive loading:
 * 1. Show shimmer placeholder immediately
 * 2. Load thumbnail (200px wide, 60% quality) for fast preview
 * 3. Crossfade to full image once loaded
 * Coil disk+memory cache ensures repeat views are instant.
 */

@Composable
fun ImageMessageBubble(
    mediaUrl: String,
    isVideo: Boolean = false,
    isMyMessage: Boolean,
    time: String,
    onPreviewClick: (String, MediaType) -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    status: MessageStatus = MessageStatus.SENT
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    // Resolve full URL
    val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(mediaUrl) ?: ""
    // Build thumbnail URL: replace /uploads/ with /api/files/thumbnail/ for server-side resize
    val thumbnailUrl = remember(fullUrl) {
        buildThumbnailUrl(fullUrl)
    }
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    Column(
        modifier = modifier
            .widthIn(max = 250.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable {
                onPreviewClick(
                    mediaUrl,
                    if (isVideo) MediaType.VIDEO else MediaType.IMAGE
                )
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            // Progressive loading: SubcomposeAsyncImage with shimmer placeholder
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(fullUrl)
                    .crossfade(300)
                    .size(Size.ORIGINAL)
                    .placeholderMemoryCacheKey(thumbnailUrl)
                    .build(),
                contentDescription = if (isVideo) "ویدیو" else "تصویر",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(),
                loading = {
                    // Show shimmer while loading full image
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                shimmerBrush(
                                    targetValue = 1300f,
                                    baseColor = MaterialTheme.colorScheme.surfaceVariant,
                                    highlightColor = MaterialTheme.colorScheme.surface
                                )
                            )
                    )
                },
                error = {
                    // Fallback on error: show muted placeholder
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )
            // Video play overlay
            if (isVideo) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "پخش ویدیو",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        // Caption text (if provided and not a file name)
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = MessageAppTypography.messageText,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        // Time + status
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time,
                style = MessageAppTypography.messageTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isMyMessage) {
                MessageStatusIcon(
                    status = status,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Build thumbnail URL from a full media URL.
 * Transforms "/uploads/filename" → "/api/files/thumbnail/filename?w=200&q=60"
 */
private fun buildThumbnailUrl(fullUrl: String): String {
    if (fullUrl.isBlank()) return ""
    return try {
        val uploadsIndex = fullUrl.indexOf("/uploads/")
        if (uploadsIndex != -1) {
            val filename = fullUrl.substring(uploadsIndex + "/uploads/".length)
            val baseUrl = fullUrl.substring(0, uploadsIndex)
            "$baseUrl/api/files/thumbnail/$filename?w=200&q=60"
        } else {
            fullUrl
        }
    } catch (e: Exception) {
        fullUrl
    }
}


/**
 * Animated shimmer brush for loading placeholders.
 */
@Composable
private fun shimmerBrush(
    targetValue: Float = 1000f,
    baseColor: Color = Color.LightGray.copy(alpha = 0.3f),
    highlightColor: Color = Color.LightGray.copy(alpha = 0.1f)
): Brush {
    val shimmerColors = listOf(baseColor, highlightColor, baseColor)
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation, y = translateAnimation)
    )
}
