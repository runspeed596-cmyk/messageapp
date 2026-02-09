package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.util.Constants

/**
 * Image/Video message bubble with thumbnail and click-to-preview.
 */

@Composable
fun ImageMessageBubble(
    mediaUrl: String,
    isVideo: Boolean = false,
    isMyMessage: Boolean,
    time: String,
    onPreviewClick: (String, MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    
    // Resolve full URL
    val fullUrl = if (mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://")) {
        mediaUrl
    } else {
        "${Constants.BASE_URL.removeSuffix("/")}$mediaUrl"
    }
    
    var isLoading by remember { mutableStateOf(true) }
    
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
            // Thumbnail image
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(fullUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = if (isVideo) "ویدیو" else "تصویر",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth(),
                onSuccess = { isLoading = false },
                onError = { isLoading = false }
            )
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.matchParentSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = extendedColors.accent,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Video play overlay
            if (isVideo && !isLoading) {
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
        
        // Time
        Text(
            text = time,
            style = MessageAppTypography.messageTime,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
