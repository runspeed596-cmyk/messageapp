package com.Kelasor.app.ui.screens.elm

import android.graphics.PointF
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.FabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.Kelasor.app.data.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.ElmViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.layers.generated.LineLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.geojson.LineString
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.*

enum class ElmDisplayMode {
    GLOBE_OVERVIEW,
    MAP_DETAIL
}

@Composable
fun ElmScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ElmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    // State
    var selectedProvince by remember { mutableStateOf<Province?>(null) }
    var selectedCity by remember { mutableStateOf<City?>(null) }
    var selectedUniversity by remember { mutableStateOf<University?>(null) }
    var universityToDetail by remember { mutableStateOf<University?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSubmissionForm by remember { mutableStateOf(false) }
    
    // Mapbox Controller
    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Annotation Managers
    var pointAnnotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var circleAnnotationManager by remember { mutableStateOf<CircleAnnotationManager?>(null) }
    var markerJob by remember { mutableStateOf<Job?>(null) }

    // Helper to animate border drawing with cascading multi-layer reveal
    fun animateBorderDrawing(points: List<Point>) {
        mapboxMap?.getStyle { style ->
            val source = style.getSource("highlight-source") as? GeoJsonSource
            val lineString = LineString.fromLngLats(points)
            source?.feature(com.mapbox.geojson.Feature.fromGeometry(lineString))
            coroutineScope.launch {
                // Animate all 4 layers with staggered timing for cascading neon reveal
                val layerIds = listOf("highlight-outer-glow", "highlight-glow-layer", "highlight-line-layer", "highlight-inner-core")
                layerIds.forEach { layerId ->
                    style.getLayer(layerId)?.let { layer ->
                        (layer as LineLayer).lineTrimOffset(listOf(1.0, 1.0)) // start hidden
                    }
                }
                // Outer glow leads
                launch {
                    val anim = Animatable(0f)
                    anim.animateTo(1f, tween(2500, easing = LinearOutSlowInEasing)) {
                        val v = this.value.toDouble()
                        style.getLayer("highlight-outer-glow")?.let { (it as LineLayer).lineTrimOffset(listOf(v, 1.0)) }
                    }
                }
                // Mid glow follows slightly
                launch {
                    kotlinx.coroutines.delay(200)
                    val anim = Animatable(0f)
                    anim.animateTo(1f, tween(2200, easing = LinearOutSlowInEasing)) {
                        val v = this.value.toDouble()
                        style.getLayer("highlight-glow-layer")?.let { (it as LineLayer).lineTrimOffset(listOf(v, 1.0)) }
                    }
                }
                // Core line follows
                launch {
                    kotlinx.coroutines.delay(350)
                    val anim = Animatable(0f)
                    anim.animateTo(1f, tween(2000, easing = LinearOutSlowInEasing)) {
                        val v = this.value.toDouble()
                        style.getLayer("highlight-line-layer")?.let { (it as LineLayer).lineTrimOffset(listOf(v, 1.0)) }
                    }
                }
                // Inner white core last
                launch {
                    kotlinx.coroutines.delay(500)
                    val anim = Animatable(0f)
                    anim.animateTo(1f, tween(1800, easing = LinearOutSlowInEasing)) {
                        val v = this.value.toDouble()
                        style.getLayer("highlight-inner-core")?.let { (it as LineLayer).lineTrimOffset(listOf(v, 1.0)) }
                    }
                }
            }
        }
    }

    // Helper to generate a smooth cyber ring (circle) around a point
    fun getCyberRingPoints(center: Point, radiusKm: Double = 50.0): List<Point> {
        val points = mutableListOf<Point>()
        val segments = 64
        for (i in 0..segments) {
            val angle = Math.toRadians((i * 360.0 / segments))
            val lat = center.latitude() + (radiusKm / 111.0) * Math.sin(angle)
            val lon = center.longitude() + (radiusKm / (111.0 * Math.cos(Math.toRadians(center.latitude())))) * Math.cos(angle)
            points.add(Point.fromLngLat(lon, lat))
        }
        return points
    }

    // Accurate Iran border coordinates (~90 points)
    val iranBorder = listOf(
        // Northwest: Turkey-Armenia-Azerbaijan border
        Point.fromLngLat(44.79, 39.71), Point.fromLngLat(44.81, 39.65), Point.fromLngLat(44.77, 39.55),
        Point.fromLngLat(44.59, 39.45), Point.fromLngLat(44.38, 39.42), Point.fromLngLat(44.28, 39.39),
        Point.fromLngLat(44.03, 39.38), Point.fromLngLat(44.02, 39.36),
        // Along Turkey border going south
        Point.fromLngLat(44.17, 39.25), Point.fromLngLat(44.28, 39.10), Point.fromLngLat(44.38, 38.94),
        Point.fromLngLat(44.30, 38.81), Point.fromLngLat(44.32, 38.45), Point.fromLngLat(44.45, 38.30),
        Point.fromLngLat(44.56, 38.22), Point.fromLngLat(44.67, 38.06),
        // Along Azerbaijan (Nakhchivan) and Turkey
        Point.fromLngLat(44.78, 37.82), Point.fromLngLat(44.77, 37.59), Point.fromLngLat(44.60, 37.44),
        Point.fromLngLat(44.56, 37.15), Point.fromLngLat(44.60, 36.82), Point.fromLngLat(44.75, 36.60),
        Point.fromLngLat(44.83, 36.42), Point.fromLngLat(44.85, 36.18),
        // Iraq border going south
        Point.fromLngLat(44.97, 36.01), Point.fromLngLat(45.05, 35.82), Point.fromLngLat(45.16, 35.63),
        Point.fromLngLat(45.36, 35.41), Point.fromLngLat(45.39, 35.16), Point.fromLngLat(45.44, 34.95),
        Point.fromLngLat(45.56, 34.69), Point.fromLngLat(45.65, 34.47), Point.fromLngLat(45.70, 34.22),
        Point.fromLngLat(45.78, 33.96), Point.fromLngLat(45.80, 33.72), Point.fromLngLat(45.85, 33.48),
        Point.fromLngLat(45.96, 33.20), Point.fromLngLat(46.09, 32.96), Point.fromLngLat(46.17, 32.68),
        Point.fromLngLat(46.28, 32.42), Point.fromLngLat(46.39, 32.18), Point.fromLngLat(46.56, 31.93),
        Point.fromLngLat(47.06, 31.69), Point.fromLngLat(47.35, 31.41), Point.fromLngLat(47.68, 31.00),
        Point.fromLngLat(47.85, 30.84), Point.fromLngLat(47.98, 30.62),
        // Shatt al-Arab / Persian Gulf coast
        Point.fromLngLat(48.02, 30.44), Point.fromLngLat(48.16, 30.32), Point.fromLngLat(48.42, 30.35),
        Point.fromLngLat(48.85, 30.42), Point.fromLngLat(49.20, 30.38), Point.fromLngLat(49.55, 30.15),
        Point.fromLngLat(49.80, 29.88), Point.fromLngLat(50.10, 29.60),
        // Bushehr coast
        Point.fromLngLat(50.35, 29.35), Point.fromLngLat(50.60, 29.10), Point.fromLngLat(50.84, 28.97),
        Point.fromLngLat(51.10, 28.68), Point.fromLngLat(51.35, 28.30), Point.fromLngLat(51.58, 27.95),
        // Kangan / Assaluyeh coast
        Point.fromLngLat(52.06, 27.84), Point.fromLngLat(52.62, 27.47), Point.fromLngLat(53.05, 27.18),
        Point.fromLngLat(53.50, 26.98), Point.fromLngLat(53.95, 26.72), Point.fromLngLat(54.35, 26.55),
        // Bandar Lengeh / Qeshm
        Point.fromLngLat(54.88, 26.56), Point.fromLngLat(55.35, 26.65), Point.fromLngLat(55.80, 26.80),
        Point.fromLngLat(56.27, 27.18),
        // Strait of Hormuz / Bandar Abbas down to Jask
        Point.fromLngLat(56.45, 27.06), Point.fromLngLat(56.80, 26.68), Point.fromLngLat(57.15, 26.30),
        Point.fromLngLat(57.48, 25.95), Point.fromLngLat(57.77, 25.65),
        // Makran / Gulf of Oman coast (Jask to Chabahar)
        Point.fromLngLat(58.20, 25.48), Point.fromLngLat(58.65, 25.38), Point.fromLngLat(59.10, 25.35),
        Point.fromLngLat(59.55, 25.32), Point.fromLngLat(60.02, 25.30), Point.fromLngLat(60.38, 25.35),
        Point.fromLngLat(60.64, 25.29),
        // Gwatar Bay / Pakistan border
        Point.fromLngLat(61.05, 25.20), Point.fromLngLat(61.38, 25.15), Point.fromLngLat(61.66, 25.13),
        // Pakistan border going north (stays along ~61.6°E)
        Point.fromLngLat(61.62, 25.45), Point.fromLngLat(61.58, 25.80), Point.fromLngLat(61.55, 26.20),
        Point.fromLngLat(61.58, 26.55), Point.fromLngLat(61.62, 26.95),
        Point.fromLngLat(61.65, 27.40), Point.fromLngLat(61.68, 27.75), Point.fromLngLat(61.70, 28.10),
        Point.fromLngLat(61.68, 28.45), Point.fromLngLat(61.65, 28.80),
        // Iran-Pakistan-Afghanistan tripoint
        Point.fromLngLat(61.65, 29.04),
        // Afghanistan border (runs along ~60.5-61.5°E going north)
        Point.fromLngLat(61.55, 29.40), Point.fromLngLat(61.40, 29.75), Point.fromLngLat(61.30, 30.10),
        Point.fromLngLat(61.15, 30.50), Point.fromLngLat(61.05, 30.85), Point.fromLngLat(60.90, 31.15),
        Point.fromLngLat(60.85, 31.40), Point.fromLngLat(60.77, 31.65),
        Point.fromLngLat(60.68, 31.85), Point.fromLngLat(60.58, 32.20), Point.fromLngLat(60.52, 32.60),
        Point.fromLngLat(60.48, 33.06), Point.fromLngLat(60.50, 33.52), Point.fromLngLat(60.48, 33.76),
        Point.fromLngLat(60.45, 34.09), Point.fromLngLat(60.52, 34.32), Point.fromLngLat(60.58, 34.52),
        Point.fromLngLat(60.72, 34.63), Point.fromLngLat(60.88, 35.27), Point.fromLngLat(61.05, 35.62),
        // Turkmenistan border
        Point.fromLngLat(61.15, 35.98), Point.fromLngLat(61.22, 36.18), Point.fromLngLat(61.12, 36.48),
        Point.fromLngLat(60.65, 36.81), Point.fromLngLat(60.30, 36.95), Point.fromLngLat(59.92, 37.05),
        Point.fromLngLat(59.50, 37.18), Point.fromLngLat(58.80, 37.52), Point.fromLngLat(58.42, 37.64),
        Point.fromLngLat(57.90, 37.78), Point.fromLngLat(57.38, 37.95), Point.fromLngLat(56.88, 37.92),
        Point.fromLngLat(56.40, 37.95), Point.fromLngLat(55.98, 37.93), Point.fromLngLat(55.58, 37.88),
        Point.fromLngLat(55.07, 37.58), Point.fromLngLat(54.75, 37.48), Point.fromLngLat(54.38, 37.32),
        Point.fromLngLat(54.06, 37.25),
        // Caspian Sea coast
        Point.fromLngLat(53.80, 37.10), Point.fromLngLat(53.50, 36.86), Point.fromLngLat(53.15, 36.78),
        Point.fromLngLat(52.62, 36.82), Point.fromLngLat(52.10, 36.88), Point.fromLngLat(51.55, 36.85),
        Point.fromLngLat(51.15, 36.78), Point.fromLngLat(50.82, 36.68), Point.fromLngLat(50.42, 36.68),
        Point.fromLngLat(50.05, 36.72), Point.fromLngLat(49.80, 36.82), Point.fromLngLat(49.45, 37.10),
        Point.fromLngLat(49.10, 37.28), Point.fromLngLat(48.88, 37.60), Point.fromLngLat(48.73, 37.82),
        Point.fromLngLat(48.60, 38.05), Point.fromLngLat(48.58, 38.22), Point.fromLngLat(48.35, 38.42),
        // Back up to Azerbaijan / Armenia
        Point.fromLngLat(48.02, 38.85), Point.fromLngLat(47.98, 39.01), Point.fromLngLat(47.77, 39.10),
        Point.fromLngLat(47.56, 39.18), Point.fromLngLat(46.55, 38.88), Point.fromLngLat(46.17, 38.82),
        Point.fromLngLat(45.95, 38.88), Point.fromLngLat(45.62, 39.02), Point.fromLngLat(45.35, 39.18),
        Point.fromLngLat(45.03, 39.34), Point.fromLngLat(44.79, 39.71) // Close loop
    )

    LaunchedEffect(mapboxMap) {
        if (mapboxMap != null) {
            animateBorderDrawing(iranBorder)
        }
    }

    // Advanced Filter States
    var showAdvancedFilters by remember { mutableStateOf(false) }
    var filterName by remember { mutableStateOf("") }
    var filterMinistry by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("") }
    var filterYearStart by remember { mutableStateOf("") }
    var filterMinStudents by remember { mutableStateOf(0) }
    var filterMaxIranRank by remember { mutableStateOf(1000) }
    var filterMaxWorldRank by remember { mutableStateOf(10000) }
    var filterMinPapers by remember { mutableStateOf(0) }
    var filterFaculty by remember { mutableStateOf("") }
    var filterMajor by remember { mutableStateOf("") }
    var filterFacility by remember { mutableStateOf("") }

    // Dynamic Provinces Derivation
    val dynamicProvinces by remember(state.universities) {
        derivedStateOf {
            state.universities
                .filter { it.province.isNotEmpty() }
                .groupBy { it.province }
                .map { (provinceName, provinceUnis) ->
                    Province(
                        id = "prov_$provinceName",
                        name = provinceName,
                        cities = provinceUnis
                            .filter { it.city.isNotEmpty() }
                            .groupBy { it.city }
                            .map { (cityName, cityUnis) ->
                                City(
                                    id = "city_$cityName",
                                    name = cityName,
                                    coordinates = cityUnis.firstOrNull()?.coordinates ?: Coordinates(35.6892, 51.3890),
                                    universities = cityUnis
                                )
                            }
                    )
                }
                .sortedBy { it.name }
        }
    }

    // Derived Filtered List
    val filteredUniversities by remember(
        state.universities, // CRITICAL: Added state.universities so list updates after fetch
        selectedProvince, selectedCity, filterName, filterMinistry, filterType, 
        filterYearStart, filterMinStudents, filterMaxIranRank, filterMaxWorldRank,
        filterMinPapers, filterFaculty, filterMajor, filterFacility
    ) {
        derivedStateOf {
            val baseList = when {
                selectedCity != null -> selectedCity?.universities ?: emptyList()
                selectedProvince != null -> selectedProvince?.cities?.flatMap { it.universities } ?: emptyList()
                else -> state.universities
            }
            
            baseList.filter { uni ->
                (filterName.isEmpty() || uni.name.contains(filterName, ignoreCase = true)) &&
                (filterMinistry.isEmpty() || uni.ministry.contains(filterMinistry, ignoreCase = true)) &&
                (filterType.isEmpty() || uni.type.contains(filterType, ignoreCase = true)) &&
                (filterYearStart.isEmpty() || uni.establishmentYear >= filterYearStart) &&
                (uni.studentCount >= filterMinStudents) &&
                (uni.iranRank <= filterMaxIranRank) &&
                (uni.worldRank <= filterMaxWorldRank) &&
                (uni.paperCount >= filterMinPapers) &&
                (filterFaculty.isEmpty() || uni.faculties.any { it.contains(filterFaculty, ignoreCase = true) }) &&
                (filterMajor.isEmpty() || uni.majors.any { it.contains(filterMajor, ignoreCase = true) }) &&
                (filterFacility.isEmpty() || uni.facilities.any { it.contains(filterFacility, ignoreCase = true) })
            }
        }
    }

    fun flyToLocation(
        lat: Double, 
        lon: Double, 
        zoomLevel: Double, 
        name: String = "",
        isCity: Boolean = false, 
        isUni: Boolean = false,
        isProvince: Boolean = false,
        batchUnis: List<University> = emptyList()
    ) {
        mapboxMap?.let { map ->
            map.getStyle { style ->
                style.getLayer("settlement-label")?.let { layer ->
                    layer.visibility(if (batchUnis.isNotEmpty()) Visibility.NONE else Visibility.VISIBLE)
                }
            }

            val cameraOptions = CameraOptions.Builder()
                .center(Point.fromLngLat(lon, lat))
                .zoom(zoomLevel)
                .pitch(if (zoomLevel > 11) 60.0 else 20.0) 
                .build()
                
            val animationOptions = MapAnimationOptions.Builder().duration(2000).build()
            map.flyTo(cameraOptions, animationOptions)
            
            val centerPoint = Point.fromLngLat(lon, lat)
            when {
                isProvince -> animateBorderDrawing(getCyberRingPoints(centerPoint, radiusKm = 120.0)) // Large for province coverage
                isCity -> animateBorderDrawing(getCyberRingPoints(centerPoint, radiusKm = 25.0))     // Medium for city area
                isUni -> animateBorderDrawing(getCyberRingPoints(centerPoint, radiusKm = 1.0))       // Small circle around campus
                else -> animateBorderDrawing(iranBorder) 
            }
        }
    }

    // Dedicated Reactive Marker Management
    LaunchedEffect(
        filteredUniversities, 
        pointAnnotationManager, 
        circleAnnotationManager, 
        selectedProvince, 
        selectedCity, 
        selectedUniversity
    ) {
        val pManager = pointAnnotationManager
        val cManager = circleAnnotationManager
        
        if (pManager == null || cManager == null) return@LaunchedEffect

        markerJob?.cancel()
        markerJob = launch {
            pManager.deleteAll()
            cManager.deleteAll()
            
            if (filteredUniversities.isEmpty()) return@launch

            val isUniFocus = selectedUniversity != null || (filteredUniversities.size == 1 && (filterName.isNotEmpty() || filterFaculty.isNotEmpty() || filterMajor.isNotEmpty()))
            
            if (isUniFocus) {
                val focusUni = selectedUniversity ?: filteredUniversities.first()
                val opts = PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(focusUni.coordinates.lon, focusUni.coordinates.lat))
                    .withIconImage("school-15") 
                    .withIconSize(2.5)
                    .withTextField(focusUni.name)
                    .withTextSize(16.0)
                    .withTextColor("#00E5FF")
                    .withTextHaloColor("#000000")
                    .withTextHaloWidth(2.0)
                    .withTextOffset(listOf(0.0, 1.5))
                
                pManager.create(opts).setData(JsonPrimitive(focusUni.name))
            } else {
                // Show PointAnnotations for ALL. It's more reliable than circles for clicks.
                filteredUniversities.forEachIndexed { index, uni ->
                    val opts = PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(uni.coordinates.lon, uni.coordinates.lat))
                        .withIconImage("school-15") 
                        .withIconSize(if (filteredUniversities.size < 20) 1.5 else 1.0)
                        .withTextField(if (filteredUniversities.size < 15) uni.name else "")
                        .withTextSize(10.0)
                        .withTextColor("#00E5FF")
                        .withTextHaloColor("#000000")
                        .withTextHaloWidth(1.0)
                        .withTextOffset(listOf(0.0, 1.2))
                    
                    pManager.create(opts).setData(JsonPrimitive(uni.name))
                    
                    // Fallback Circle for visibility if icon fails
                    val circleOptions = CircleAnnotationOptions()
                        .withPoint(Point.fromLngLat(uni.coordinates.lon, uni.coordinates.lat))
                        .withCircleRadius(6.0)
                        .withCircleColor("#00E5FF")
                        .withCircleStrokeWidth(1.5)
                        .withCircleStrokeColor("#000000")
                        .withCircleOpacity(0.8)
                    cManager.create(circleOptions).setData(JsonPrimitive(uni.name))

                    if (filteredUniversities.size > 100 && index % 20 == 0) yield()
                }
            }
        }
    }

    // Camera and Border Animation Control
    // IMPORTANT: Only trigger flyTo when user EXPLICITLY selects a province/city/uni.
    // On initial load (all nulls), stay on Iran overview.
    LaunchedEffect(selectedProvince, selectedCity, selectedUniversity) {
        // If user has explicitly selected something, fly to it
        if (selectedProvince != null || selectedCity != null || selectedUniversity != null) {
            val center = when {
                selectedUniversity != null -> selectedUniversity!!.coordinates
                selectedCity != null -> selectedCity!!.coordinates
                selectedProvince != null -> selectedProvince!!.cities.firstOrNull()?.coordinates
                else -> null
            }
            center?.let { coords ->
                val zoom = when {
                    selectedUniversity != null -> 16.0
                    selectedCity != null -> 12.0
                    selectedProvince != null -> 7.5
                    else -> 5.5
                }
                flyToLocation(
                    coords.lat, coords.lon, zoom,
                    name = selectedUniversity?.name ?: selectedCity?.name ?: selectedProvince?.name ?: "",
                    isProvince = selectedProvince != null && selectedCity == null && selectedUniversity == null,
                    isCity = selectedCity != null && selectedUniversity == null,
                    isUni = selectedUniversity != null,
                    batchUnis = filteredUniversities
                )
            }
        }
    }
    
    val isDark = isSystemInDarkTheme()
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF000000))) 
    } else {
          Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9)))
    }
    
    val textColor = if (isDark) Color.White else Color(0xFF0D47A1)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.6f)
    
    if (universityToDetail != null) {
        UniversityDetailScreen(university = universityToDetail!!, onBack = { universityToDetail = null })
    } else {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = textColor
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = Color(0xFF00E5FF))
                    }
                    
                    state.error?.let { err ->
                        Box(modifier = Modifier.fillMaxWidth().background(Color.Red.copy(alpha = 0.1f)).padding(16.dp)) {
                            Text(err, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                    
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Back", tint = Color.White)
                        }
                        Text("جهان علم", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                        Box(modifier = Modifier.size(48.dp))
                    }

                    // World of Science Header & Search
                    Spacer(Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        ScienceWorldContent(
                            state = state,
                            filteredUniversities = filteredUniversities,
                            selectedProvince = selectedProvince,
                            selectedCity = selectedCity,
                            selectedUniversity = selectedUniversity,
                            showAdvancedFilters = showAdvancedFilters,
                            textColor = textColor,
                            cardColor = cardColor,
                            mapboxMap = mapboxMap,
                            onProvinceSelected = { province ->
                                selectedProvince = province; selectedCity = null; selectedUniversity = null
                                val allUnis = province.cities.flatMap { it.universities }
                                val zoomLevel = if (province.name.contains("تهران")) 8.5 else 7.5
                                province.cities.firstOrNull()?.coordinates?.let { coords ->
                                    flyToLocation(coords.lat, coords.lon, zoomLevel, name = province.name, isProvince = true, batchUnis = allUnis)
                                }
                            },
                            onCitySelected = { city ->
                                selectedCity = city; selectedUniversity = null
                                val zoomLevel = if (city.name.contains("تهران")) 13.5 else 11.5
                                flyToLocation(city.coordinates.lat, city.coordinates.lon, zoomLevel, name = city.name, isCity = true, batchUnis = city.universities)
                            },
                            onUniversitySelected = { uni ->
                                selectedUniversity = uni
                                flyToLocation(uni.coordinates.lat, uni.coordinates.lon, 16.0, name = uni.name, isUni = true)
                            },
                            onToggleAdvancedFilters = { showAdvancedFilters = !showAdvancedFilters },
                            onFilterNameChange = { filterName = it },
                            onFilterMinistryChange = { filterMinistry = it },
                            onFilterTypeChange = { filterType = it },
                            onFilterYearChange = { filterYearStart = it },
                            onFilterMinStudentsChange = { filterMinStudents = it.toInt() },
                            onFilterMaxIranRankChange = { filterMaxIranRank = it.toInt() },
                            onFilterMaxWorldRankChange = { filterMaxWorldRank = it.toInt() },
                            onFilterFacultyChange = { filterFaculty = it },
                            onFilterMajorChange = { filterMajor = it },
                            onFilterFacilityChange = { filterFacility = it },
                            onUniversityDetail = { universityToDetail = it },
                            dynamicProvinces = dynamicProvinces,
                            onMapReady = { mapView ->
                                mapboxMap = mapView.getMapboxMap()
                                circleAnnotationManager = mapView.annotations.createCircleAnnotationManager()
                                pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
                                
                                pointAnnotationManager?.addClickListener { annotation ->
                                    val uniName = annotation.getData()?.asString ?: ""
                                    if (uniName.isNotEmpty()) state.universities.find { it.name == uniName }?.let { universityToDetail = it }
                                    true
                                }
                                circleAnnotationManager?.addClickListener { annotation ->
                                    val uniName = annotation.getData()?.asString ?: ""
                                    if (uniName.isNotEmpty()) state.universities.find { it.name == uniName }?.let { universityToDetail = it }
                                    true
                                }
                            },
                            filterName = filterName,
                            filterMinistry = filterMinistry,
                            filterType = filterType,
                            filterYearStart = filterYearStart,
                            filterMinStudents = filterMinStudents,
                            filterMaxIranRank = filterMaxIranRank,
                            filterMaxWorldRank = filterMaxWorldRank,
                            filterFaculty = filterFaculty,
                            filterMajor = filterMajor,
                            filterFacility = filterFacility
                        )
                    } else {
                        CompetitionsContent(
                            state = state,
                            textColor = textColor,
                            cardColor = cardColor,
                            onSubmitRequest = { showSubmissionForm = true }
                        )
                    }
                }
                
                // Fixed FAB at top-right (doesn't scroll)
                if (selectedTab != 0) {
                    FloatingActionButton(
                        onClick = { showSubmissionForm = true },
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 80.dp, end = 16.dp) // Below header
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "ثبت رویداد")
                    }
                }
            }
        }
    }

    if (showSubmissionForm) {
        EventSubmissionDialog(
            onDismiss = { showSubmissionForm = false },
            onSubmit = { title, desc, date, loc, link ->
                viewModel.reportEvent(title, desc, date, loc, link, com.Kelasor.app.data.remote.dto.ElmEventType.COMPETITION)
                showSubmissionForm = false
            }
        )
    }
}

@Composable
fun ScienceWorldContent(
    state: com.Kelasor.app.ui.viewmodel.ElmViewState,
    filteredUniversities: List<University>,
    selectedProvince: Province?,
    selectedCity: City?,
    selectedUniversity: University?,
    showAdvancedFilters: Boolean,
    textColor: Color,
    cardColor: Color,
    mapboxMap: MapboxMap?,
    onProvinceSelected: (Province) -> Unit,
    onCitySelected: (City) -> Unit,
    onUniversitySelected: (University) -> Unit,
    onToggleAdvancedFilters: () -> Unit,
    onFilterNameChange: (String) -> Unit,
    onFilterMinistryChange: (String) -> Unit,
    onFilterTypeChange: (String) -> Unit,
    onFilterYearChange: (String) -> Unit,
    onFilterMinStudentsChange: (Float) -> Unit,
    onFilterMaxIranRankChange: (Float) -> Unit,
    onFilterMaxWorldRankChange: (Float) -> Unit,
    onFilterFacultyChange: (String) -> Unit,
    onFilterMajorChange: (String) -> Unit,
    onFilterFacilityChange: (String) -> Unit,
    onUniversityDetail: (University) -> Unit,
    onMapReady: (com.mapbox.maps.MapView) -> Unit,
    filterName: String,
    filterMinistry: String,
    filterType: String,
    filterYearStart: String,
    filterMinStudents: Int,
    filterMaxIranRank: Int,
    filterMaxWorldRank: Int,
    filterFaculty: String,
    filterMajor: String,
    filterFacility: String,
    dynamicProvinces: List<Province>
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassmorphicFilterCard(backgroundColor = cardColor, modifier = Modifier.padding(horizontal = 24.dp)) {
            FilterRow("کشور", "ایران", textColor) {}
            Spacer(Modifier.height(16.dp))
            FilteringDropdown("استان", dynamicProvinces, selectedProvince, textColor = textColor, onOptionSelected = onProvinceSelected, nameProvider = { it.name })
            Spacer(Modifier.height(16.dp))
            FilteringDropdown("شهر", selectedProvince?.cities ?: emptyList(), selectedCity, textColor = textColor, enabled = selectedProvince != null, onOptionSelected = onCitySelected, nameProvider = { it.name })
            Spacer(Modifier.height(16.dp))
            FilteringDropdown("دانشگاه", selectedCity?.universities ?: emptyList(), selectedUniversity, textColor = textColor, enabled = selectedCity != null, onOptionSelected = onUniversitySelected, nameProvider = { it.name })
        }
        
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onToggleAdvancedFilters, 
                colors = ButtonDefaults.buttonColors(containerColor = textColor.copy(alpha = 0.1f)), 
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (showAdvancedFilters) Icons.Default.Close else Icons.Default.Search, contentDescription = null, tint = textColor)
                    Spacer(Modifier.width(8.dp))
                    Text(if (showAdvancedFilters) "بستن فیلترها" else "فیلترهای پیشرفته", color = textColor)
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    onFilterNameChange("")
                    onFilterMinistryChange("")
                    onFilterTypeChange("")
                    onFilterYearChange("")
                    onFilterMinStudentsChange(0f)
                    onFilterMaxIranRankChange(1000f)
                    onFilterMaxWorldRankChange(10000f)
                    onFilterFacultyChange("")
                    onFilterMajorChange("")
                    onFilterFacilityChange("")
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(textColor.copy(alpha = 0.05f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset All", tint = textColor)
            }
        }
        


        AnimatedVisibility(visible = showAdvancedFilters) {
            GlassmorphicFilterCard(backgroundColor = cardColor, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                FilterTextField("نام دانشگاه", filterName, textColor, onFilterNameChange)
                Spacer(Modifier.height(12.dp))
                FilterTextField("نام وزارت", filterMinistry, textColor, onFilterMinistryChange)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) { FilterTextField("نوع (دولتی/آزاد)", filterType, textColor, onFilterTypeChange) }
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.weight(1f)) { FilterTextField("سال تأسیس", filterYearStart, textColor, onFilterYearChange) }
                }
                Spacer(Modifier.height(12.dp))
                Text("حداقل دانشجو: ${filterMinStudents}", color = textColor, fontSize = 12.sp)
                Slider(value = filterMinStudents.toFloat(), onValueChange = onFilterMinStudentsChange, valueRange = 0f..50000f, colors = SliderDefaults.colors(thumbColor = textColor, activeTrackColor = textColor))
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("رتبه ایران (تا): ${filterMaxIranRank}", color = textColor, fontSize = 11.sp)
                        Slider(value = filterMaxIranRank.toFloat(), onValueChange = onFilterMaxIranRankChange, valueRange = 1f..1000f)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("رتبه جهان (تا): ${filterMaxWorldRank}", color = textColor, fontSize = 11.sp)
                        Slider(value = filterMaxWorldRank.toFloat(), onValueChange = onFilterMaxWorldRankChange, valueRange = 1f..10000f)
                    }
                }
                Spacer(Modifier.height(12.dp))
                FilterTextField("دانشکده خاص", filterFaculty, textColor, onFilterFacultyChange)
                Spacer(Modifier.height(8.dp))
                FilterTextField("رشته خاص", filterMajor, textColor, onFilterMajorChange)
                Spacer(Modifier.height(8.dp))
                FilterTextField("امکانات خاص", filterFacility, textColor, onFilterFacilityChange)
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(450.dp).padding(horizontal = 24.dp).clip(RoundedCornerShape(32.dp)), contentAlignment = Alignment.Center) {
            Globe3D(modifier = Modifier.fillMaxSize()) // Glow behind
            MapboxGlobe(onMapReady = onMapReady, modifier = Modifier.fillMaxSize())
        }
        
        Spacer(Modifier.height(32.dp))
        if (filteredUniversities.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text("نتایج جستجو (${filteredUniversities.size} مورد)", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                filteredUniversities.forEach { uni ->
                    UniListItem(university = uni, textColor = textColor, cardColor = cardColor, onClick = { onUniversityDetail(uni) })
                    Spacer(Modifier.height(12.dp))
                }
            }
        } else {
             Column(
                 modifier = Modifier.fillMaxWidth().padding(32.dp),
                 horizontalAlignment = Alignment.CenterHorizontally
             ) {
                 Icon(Icons.Default.School, contentDescription = null, tint = textColor.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                 Spacer(Modifier.height(16.dp))
                 Text(
                     if (state.isLoading) "در حال بارگذاری..." else "دانشگاهی یافت نشد",
                     color = textColor.copy(alpha = 0.5f)
                 )
             }
        }
        Spacer(Modifier.height(48.dp)) 
    }
}

@Composable
fun CompetitionsContent(
    state: com.Kelasor.app.ui.viewmodel.ElmViewState,
    textColor: Color,
    cardColor: Color,
    onSubmitRequest: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        if (state.featuredEvents.isNotEmpty()) {
            Text("رویدادهای برگزیده", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF00E5FF), fontWeight = FontWeight.Black)
            Spacer(Modifier.height(16.dp))
            state.featuredEvents.forEach { event ->
                EventListItem(event = event, textColor = textColor, cardColor = cardColor)
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("مسابقات علمی", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        if (state.competitions.isEmpty()) {
            Text("در حال حاضر مسابقه‌ای موجود نیست", color = textColor.copy(alpha = 0.5f))
        } else {
            state.competitions.forEach { event ->
                EventListItem(event = event, textColor = textColor, cardColor = cardColor)
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("استارتاپ‌ها", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        state.startups.forEach { event ->
            EventListItem(event = event, textColor = textColor, cardColor = cardColor)
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(32.dp))
        Text("کنگره‌ها و همایش‌ها", style = MaterialTheme.typography.titleLarge, color = textColor, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        state.congresses.forEach { event ->
            EventListItem(event = event, textColor = textColor, cardColor = cardColor)
            Spacer(Modifier.height(12.dp))
        }
        
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun EventListItem(event: com.Kelasor.app.data.remote.dto.ElmEventDto, textColor: Color, cardColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cardColor).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF00E5FF).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF00E5FF))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(event.title, color = textColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("${event.date} - ${event.location}", color = textColor.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
        }
        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textColor.copy(alpha = 0.5f))
    }
}

@Composable
fun EventSubmissionDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت رویداد جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("عنوان") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("توضیحات") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("تاریخ") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = loc, onValueChange = { loc = it }, label = { Text("مکان") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = link, onValueChange = { link = it }, label = { Text("لینک") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(title, desc, date, loc, link) }) {
                Text("ثبت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Helper Composables for Filters and Lists
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GlassmorphicFilterCard(
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun FilterRow(
    label: String,
    value: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(textColor.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = textColor.copy(alpha = 0.7f))
        Text(value, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun <T> FilteringDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    textColor: Color,
    enabled: Boolean = true,
    onOptionSelected: (T) -> Unit,
    nameProvider: (T) -> String,
    onClear: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column {
        Text(label, color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(textColor.copy(alpha = 0.05f))
                .clickable(enabled = enabled) { expanded = true }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected?.let { nameProvider(it) } ?: "انتخاب کنید",
                    color = if (selected != null) textColor else textColor.copy(alpha = 0.5f)
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (onClear != null) {
                DropdownMenuItem(
                    text = { Text("همه") },
                    onClick = {
                        onClear()
                        expanded = false
                    }
                )
            }
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(nameProvider(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilterTextField(
    label: String,
    value: String,
    textColor: Color,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = textColor.copy(alpha = 0.5f),
            unfocusedBorderColor = textColor.copy(alpha = 0.2f),
            focusedLabelColor = textColor.copy(alpha = 0.7f),
            unfocusedLabelColor = textColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@Composable
fun UniListItem(
    university: University,
    textColor: Color,
    cardColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF00E5FF).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF00E5FF))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                university.name,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${university.type} - ${university.ministry}",
                color = textColor.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium
            )
            if (university.iranRank > 0) {
                Text(
                    "رتبه ایران: ${university.iranRank}",
                    color = Color(0xFF00E5FF),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textColor.copy(alpha = 0.5f)
        )
    }
}

