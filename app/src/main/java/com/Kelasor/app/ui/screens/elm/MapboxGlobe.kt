package com.Kelasor.app.ui.screens.elm

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.extension.style.projection.generated.setProjection
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.atmosphere.generated.setAtmosphere
import com.mapbox.maps.extension.style.atmosphere.generated.Atmosphere
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.geojson.LineString
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.getSource

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.MessageAppTheme

@Composable
fun MapboxGlobe(
    modifier: Modifier = Modifier,
    onMapReady: (MapView) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Explicitly set access token before creating MapView
    val accessToken = context.getString(com.Kelasor.app.R.string.mapbox_access_token)
    com.mapbox.maps.ResourceOptionsManager.getDefault(context, accessToken)
    
    var isMapReady by remember { mutableStateOf(false) }
    var showPlaceholder by remember { mutableStateOf(false) }

    // Proper MapView lifecycle management
    val mapView = remember {
        MapView(context).apply {
            val map = this.getMapboxMap()
            
            // Disable gestures to allow page scrolling
            this.gestures.updateSettings {
                 scrollEnabled = false
                 pinchToZoomEnabled = false
                 rotateEnabled = false
                 doubleTapToZoomInEnabled = false
                 quickZoomEnabled = false
                 pitchEnabled = false
            }
            
            // Hide UI Elements
            this.attribution.updateSettings { enabled = false }
            this.logo.updateSettings { enabled = false }
            this.compass.updateSettings { enabled = false }
            this.scalebar.updateSettings { enabled = false }
            
            // Use Dark Style for Cyber feel
            map.loadStyleUri(Style.DARK) { style ->
                isMapReady = true
                try {
                    style.setProjection(projection(ProjectionName.GLOBE))
                    style.setAtmosphere(Atmosphere().apply {
                            color(android.graphics.Color.parseColor("#001021"))
                            highColor(android.graphics.Color.parseColor("#00E5FF"))
                            horizonBlend(0.4)
                            starIntensity(0.9) 
                        }
                    )
                    
                    val highlightSourceId = "highlight-source"
                    if (style.getSource(highlightSourceId) == null) {
                        style.addSource(geoJsonSource(highlightSourceId) { lineMetrics(true) })
                    }
                    if (style.getLayer("highlight-glow-layer") == null) {
                        style.addLayer(lineLayer("highlight-glow-layer", highlightSourceId) {
                            lineColor("#00B0FF"); lineWidth(8.0); lineBlur(5.0); lineOpacity(0.6)
                        })
                    }
                    if (style.getLayer("highlight-line-layer") == null) {
                        style.addLayer(lineLayer("highlight-line-layer", highlightSourceId) {
                            lineColor("#00E5FF"); lineWidth(2.5)
                        })
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MapboxGlobe", "Error configuring style: ${e.message}")
                }
                
                map.setCamera(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(53.6880, 32.4279))
                        .zoom(1.8)
                        .build()
                )
                onMapReady(this@apply)
            }
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(6000)
        if (!isMapReady) {
            showPlaceholder = true
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> try { mapView.onStart() } catch(e: Exception) {}
                Lifecycle.Event.ON_STOP -> try { mapView.onStop() } catch(e: Exception) {}
                Lifecycle.Event.ON_DESTROY -> try { mapView.onDestroy() } catch(e: Exception) {}
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try { mapView.onDestroy() } catch(e: Exception) {}
        }
    }

    // Aesthetic Container for the Globe
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(330.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.3f), 
                            Color(0xFF0D47A1).copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        // The Mapbox Map itself, clipped to a circle
        AndroidView(
            modifier = Modifier
                .size(310.dp)
                .clip(CircleShape)
                .alpha(if (isMapReady) 1f else 0f),
            factory = { mapView },
            update = { _ -> }
        )

        // Loading Indicator
        if (!isMapReady && !showPlaceholder) {
            CircularProgressIndicator(
                color = Color(0xFF00E5FF),
                modifier = Modifier.size(40.dp)
            )
        }

        // Professional Placeholder for Failure
        if (showPlaceholder) {
            Column(
                modifier = Modifier
                    .size(310.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF001021), Color(0xFF003366))
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Public,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF).copy(alpha = 0.6f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "نقشه جهان علم",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "در حال اتصال به شبکه...",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        
        // Animated Holographic Ring
        val infiniteTransition = rememberInfiniteTransition(label = "RingAnimation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween<Float>(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "GlobeRotation"
        )

        Box(
            modifier = Modifier
                .size(312.dp)
                .scale(1.02f)
                .padding(1.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF00E5FF).copy(alpha = 0.1f),
                            Color(0xFF00E5FF),
                            Color(0xFF00E5FF).copy(alpha = 0.1f)
                        ),
                        center = center
                    ),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
