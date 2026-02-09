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

    // Helper to animate border drawing
    fun animateBorderDrawing(points: List<Point>) {
        mapboxMap?.getStyle { style ->
            val source = style.getSource("highlight-source") as? GeoJsonSource
            val lineString = LineString.fromLngLats(points)
            source?.feature(com.mapbox.geojson.Feature.fromGeometry(lineString))

            coroutineScope.launch {
                val anim = Animatable(0f)
                anim.animateTo(1f, tween(1500, easing = LinearOutSlowInEasing)) {
                    val currentVal = this.value.toDouble()
                    style.getLayer("highlight-line-layer")?.let { layer ->
                        (layer as LineLayer).lineTrimOffset(listOf(currentVal, 1.0))
                    }
                    style.getLayer("highlight-glow-layer")?.let { layer ->
                        (layer as LineLayer).lineTrimOffset(listOf(currentVal, 1.0))
                    }
                }
            }
        }
    }

    // Helper to generate a smooth cyber ring (circle) around a point
    fun getCyberRingPoints(center: Point, radiusKm: Double = 50.0): List<Point> {
        val points = mutableListOf<Point>()
        val segments = 64 // Use 64 segments for a smooth circle animation
        for (i in 0..segments) {
            val angle = Math.toRadians((i * 360.0 / segments))
            val lat = center.latitude() + (radiusKm / 111.0) * Math.sin(angle)
            val lon = center.longitude() + (radiusKm / (111.0 * Math.cos(Math.toRadians(center.latitude())))) * Math.cos(angle)
            points.add(Point.fromLngLat(lon, lat))
        }
        return points
    }

    val iranBorder = listOf(
        Point.fromLngLat(44.0, 39.0), Point.fromLngLat(48.0, 38.0), Point.fromLngLat(50.0, 37.0),
        Point.fromLngLat(54.0, 37.0), Point.fromLngLat(59.0, 37.0), Point.fromLngLat(61.0, 35.0),
        Point.fromLngLat(62.0, 30.0), Point.fromLngLat(61.0, 25.0), Point.fromLngLat(57.0, 26.0),
        Point.fromLngLat(53.0, 27.0), Point.fromLngLat(50.0, 29.0), Point.fromLngLat(48.0, 30.0),
        Point.fromLngLat(45.0, 33.0), Point.fromLngLat(44.0, 37.0), Point.fromLngLat(44.0, 39.0)
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
        
        Spacer(Modifier.height(8.dp))
        Text(
            "تارکت یافت شده: ${filteredUniversities.size} دانشگاه",
            color = if (filteredUniversities.isEmpty()) Color.Red.copy(alpha = 0.6f) else textColor.copy(alpha = 0.6f),
            fontSize = 12.sp
        )

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
        
        Spacer(Modifier.height(24.dp))
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

