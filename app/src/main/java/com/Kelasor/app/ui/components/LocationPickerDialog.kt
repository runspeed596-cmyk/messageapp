package com.Kelasor.app.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.Kelasor.app.data.location.LocationData
import com.Kelasor.app.data.location.LocationManager
import com.Kelasor.app.data.location.LocationState
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import java.util.Base64

/**
 * Location picker dialog using Map.ir Web SDK via WebView.
 * Shows map, current location button, and send button.
 */

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("SetJavaScriptEnabled")
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
    
    // WebView reference
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no" />
            <link href="https://cdn.map.ir/web-sdk/1.4.2/css/mapp.min.css" rel="stylesheet" />
            <link href="https://cdn.map.ir/web-sdk/1.4.2/css/fa/style.css" rel="stylesheet" />
            <style>
                body { margin: 0; padding: 0; background-color: #f5f5f5; overflow: hidden; }
                #map { position: absolute; top: 0; bottom: 0; width: 100%; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/jquery-3.2.1.min.js"></script>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/mapp.env.js"></script>
            <script src="https://cdn.map.ir/web-sdk/1.4.2/js/mapp.min.js"></script>
            <script>
                window.map = new Mapp({
                    element: '#map',
                    presets: {
                        latlng: { lat: 35.6892, lng: 51.3890 },
                        zoom: 12
                    },
                    apiKey: 'YOUR_MAPIR_API_KEY'
                });
                
                map.addPlugin("MapirZoom", { position: "bottom-left" });
                map.addPlugin("MapirLogo", { position: "bottom-right" });

                map.on('click', function(e) {
                    var lat = e.lngLat.lat;
                    var lng = e.lngLat.lng;
                    if (window.Android) {
                        window.Android.onLocationSelected(lat, lng);
                    }
                });
                
                function flyTo(lat, lng, zoom) {
                    if (window.map) {
                        map.flyTo({
                            center: [lng, lat],
                            zoom: zoom || 15,
                            speed: 1.2
                        });
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()
    
    val base64Html = Base64.getEncoder().encodeToString(htmlContent.toByteArray())

    // Update map when location changes
    LaunchedEffect(locationState) {
        if (locationState is LocationState.Success) {
            val location = (locationState as LocationState.Success).location
            selectedLocation = location
            
            webView?.evaluateJavascript("flyTo(${location.latitude}, ${location.longitude}, 15)", null)
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
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        webView = view as WebView
                                    }
                                }
                                webChromeClient = WebChromeClient()
                                addJavascriptInterface(object {
                                    @android.webkit.JavascriptInterface
                                    fun onLocationSelected(lat: Double, lng: Double) {
                                        selectedLocation = LocationData(lat, lng)
                                    }
                                }, "Android")
                                loadData(base64Html, "text/html; charset=utf-8", "base64")
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { /* Updates handled by LaunchedEffect */ }
                    )
                    
                    // Center PIN
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
                }
                
                // Location info and Send button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    selectedLocation?.let { location ->
                        Text(
                            text = "📍 ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}",
                            style = MessageAppTypography.chatTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
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
