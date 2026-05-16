package com.Kelasor.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.domain.model.MessageStatus

/**
 * Location message bubble with Map.ir static map preview and navigation button.
 * Uses Map.ir Static Map API to show a preview without needing a native SDK or full WebView.
 */

@Composable
fun LocationMessageBubble(
    latitude: Double,
    longitude: Double,
    isMyMessage: Boolean,
    time: String,
    status: MessageStatus = MessageStatus.SENT,
    modifier: Modifier = Modifier,
    reactions: Map<String, Int> = emptyMap(),
    myReaction: String? = null,
    onReactionClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val accentColor = if (isMyMessage) extendedColors.accent else MaterialTheme.colorScheme.primary
    
    // Map.ir Static Map URL
    // Format: https://map.ir/static?width=600&height=400&zoom_level=14&markers=color:red|35.70014,51.33647
    // Using a placeholder API Key - in production this should be the user's key
    val staticMapUrl = "https://map.ir/static?width=500&height=300&zoom_level=14&markers=color:red|$latitude,$longitude&x-api-key=YOUR_MAPIR_API_KEY"

    Column(
        modifier = modifier
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable {
                openLocationInMapsApp(context, latitude, longitude)
            }
    ) {
        // Static Map Preview using Coil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = staticMapUrl,
                contentDescription = "نقشه موقعیت",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            )
            
            // Center Pin Overlay (Compose-based)
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
                    .padding(bottom = 16.dp)
            )
            
            // Navigation hint overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مسیریابی",
                        style = MessageAppTypography.messageTime,
                        color = Color.White
                    )
                }
            }
        }
        
        // Location info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "موقعیت مکانی",
                        style = MessageAppTypography.chatTime,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}",
                        style = MessageAppTypography.messageTime,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
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
        
        // Reactions
        if (reactions.isNotEmpty()) {
            ReactionRow(
                reactions = reactions,
                myReaction = myReaction,
                isMyMessage = isMyMessage,
                onReactionClick = onReactionClick,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Open location in external maps app (Google Maps, Balad, Neshan, Waze, etc.)
 */
private fun openLocationInMapsApp(context: Context, latitude: Double, longitude: Double) {
    try {
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        val chooser = Intent.createChooser(intent, "مسیریابی با...")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps?q=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در باز کردن نقشه", Toast.LENGTH_SHORT).show()
    }
}
