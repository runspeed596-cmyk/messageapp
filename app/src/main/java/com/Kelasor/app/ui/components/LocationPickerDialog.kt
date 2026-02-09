package com.Kelasor.app.ui.components

import android.Manifest
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.Kelasor.app.R
import com.Kelasor.app.data.location.LocationData
import com.Kelasor.app.data.location.LocationManager
import com.Kelasor.app.data.location.LocationState
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

/**
 * Location picker dialog with OpenStreetMap.
 * Shows map, current location button, and send button.
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPickerDialog(
    visible: Boolean,
    locationManager: LocationManager,
    onDismiss: () -> Unit,
    onSendLocation: (latitude: Double, longitude: Double) -> Unit
) {
    if (!visible) return
    
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    
    val locationState by locationManager.locationState.collectAsState()
    
    // Permission state
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    // Selected location
    var selectedLocation by remember { mutableStateOf<LocationData?>(null) }
    
    // MapView reference
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    
    // Update map when location changes
    LaunchedEffect(locationState) {
        if (locationState is LocationState.Success) {
            val location = (locationState as LocationState.Success).location
            selectedLocation = location
            
            // Fly to location
            mapboxMap?.flyTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(location.longitude, location.latitude))
                    .zoom(15.0)
                    .build()
            )
        }
    }
    
    Dialog(
        onDismissRequest = {
            locationManager.reset()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(extendedColors.accent)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب موقعیت",
                        style = MessageAppTypography.chatName,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = {
                            locationManager.reset()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = Color.White
                        )
                    }
                }
                
                // Map
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Mapbox View
                    AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                mapboxMap = this.getMapboxMap()
                                
                                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                                    // Default to Tehran
                                    getMapboxMap().setCamera(
                                        CameraOptions.Builder()
                                            .center(Point.fromLngLat(51.3890, 35.6892))
                                            .zoom(12.0)
                                            .build()
                                    )
                                    
                                    // Add click listener
                                    getMapboxMap().addOnMapClickListener { point ->
                                        selectedLocation = LocationData(
                                             latitude = point.latitude(),
                                             longitude = point.longitude()
                                        )
                                        // Add Marker (Simplification: Just center camera on click?)
                                        // Or use ViewAnnotation/PointAnnotation in full implementation.
                                        // For now, we fly to the point to indicate selection.
                                        getMapboxMap().flyTo(
                                            CameraOptions.Builder()
                                                .center(point)
                                                .build()
                                        )
                                        true
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Center PIN (To indicate selection center if we use center-based selection)
                    // Since we use click-to-select and fly-to, the center IS the selection.
                    Icon(
                         imageVector = Icons.Default.LocationOn,
                         contentDescription = null,
                         tint = Color.Red,
                         modifier = Modifier.size(48.dp).align(Alignment.Center).padding(bottom = 24.dp)
                    )
                    
                    // Get My Location Button
                    FloatingActionButton(
                        onClick = {
                            if (locationPermissionState.status.isGranted) {
                                locationManager.getCurrentLocation()
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = extendedColors.accent
                    ) {
                        if (locationState is LocationState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "موقعیت من",
                                tint = Color.White
                            )
                        }
                    }
                    
                    // Error message
                    if (locationState is LocationState.Error) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red.copy(alpha = 0.8f))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = (locationState as LocationState.Error).message,
                                color = Color.White,
                                style = MessageAppTypography.chatTime
                            )
                        }
                    }
                }
                
                // Location info and Send button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    // Coordinates display
                    selectedLocation?.let { location ->
                        Text(
                            text = "📍 ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                            style = MessageAppTypography.chatTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Send button
                    Button(
                        onClick = {
                            selectedLocation?.let { location ->
                                onSendLocation(location.latitude, location.longitude)
                                locationManager.reset()
                                onDismiss()
                            }
                        },
                        enabled = selectedLocation != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = extendedColors.accent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارسال موقعیت",
                            style = MessageAppTypography.buttonText
                        )
                    }
                }
            }
        }
    }
}
