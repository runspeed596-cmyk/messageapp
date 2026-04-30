package com.hasani.messageapp.ui.components

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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

/**
 * Location message bubble with static map preview and navigation button.
 */

@Composable
fun LocationMessageBubble(
    latitude: Double,
    longitude: Double,
    isMyMessage: Boolean,
    time: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val accentColor = if (isMyMessage) extendedColors.accent else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable {
                openLocationInMapsApp(context, latitude, longitude)
            }
    ) {
        // Static Map Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        // Disable user interaction
                        setOnTouchListener { _, _ -> true }
                        
                        getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                             getMapboxMap().setCamera(
                                 CameraOptions.Builder()
                                     .center(Point.fromLngLat(longitude, latitude))
                                     .zoom(14.0)
                                     .build()
                             )
                             
                             // Add Marker (using ViewAnnotation or similar in v10, but simplistic approach:
                             // Just center the map, the bubble has a pin icon? 
                             // Or we can add a simple ViewAnnotation if really needed.
                             // For now, let's trust the center is enough, or add a center view overlay in Compose.
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Overlay to handle clicks (since MapView consumes touches)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        openLocationInMapsApp(context, latitude, longitude)
                    }
            )
            
            // Tap hint overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
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
            
            Text(
                text = time,
                style = MessageAppTypography.messageTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Open location in external maps app (Google Maps, Balad, Neshan, Waze, etc.)
 */
private fun openLocationInMapsApp(context: Context, latitude: Double, longitude: Double) {
    try {
        // Use geo URI which is supported by most map apps
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        
        // Create a chooser to let the user select their preferred app (Balad, Neshan, etc.)
        val chooser = Intent.createChooser(intent, "مسیریابی با...")
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            // Fallback to Google Maps web URL if no app is installed
            val webUri = Uri.parse("https://www.google.com/maps?q=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در باز کردن نقشه", Toast.LENGTH_SHORT).show()
    }
}
