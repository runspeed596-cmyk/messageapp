package com.Kelasor.app.ui.screens.elm

import android.graphics.PointF
import android.webkit.WebView
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
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.ElmViewModel
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

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
    var webView by remember { mutableStateOf<WebView?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Annotation Managers
    var pointAnnotationManager by remember { mutableStateOf<Any?>(null) }
    var circleAnnotationManager by remember { mutableStateOf<Any?>(null) }
    var markerJob by remember { mutableStateOf<Job?>(null) }

    fun flyToLocation(lat: Double, lon: Double, zoomLevel: Double, name: String, isProvince: Boolean, isCity: Boolean, isUni: Boolean, batchUnis: List<University>? = null) {
        webView?.post {
            val js = "if(window.map) { map.flyTo({center: [$lon, $lat], zoom: $zoomLevel, speed: 1.2}); }"
            webView?.evaluateJavascript(js, null)
        }
    }
    
    fun animateBorderDrawing(points: List<Coordinates>) {
        val coords = points.joinToString(",") { "[${it.lon}, ${it.lat}]" }
        val js = """
            if(window.map) {
                if (map.getSource('iran-border')) {
                    map.getSource('iran-border').setData({
                        "type": "Feature",
                        "geometry": { "type": "LineString", "coordinates": [$coords] }
                    });
                } else {
                    map.addSource('iran-border', {
                        "type": "geojson",
                        "data": {
                            "type": "Feature",
                            "geometry": { "type": "LineString", "coordinates": [$coords] }
                        }
                    });
                    map.addLayer({
                        "id": "iran-border-glow",
                        "type": "line",
                        "source": "iran-border",
                        "paint": { "line-color": "#00E5FF", "line-width": 4, "line-blur": 2 }
                    });
                }
            }
        """.trimIndent()
        webView?.post {
            webView?.evaluateJavascript(js, null)
        }
    }

    fun getCyberRingPoints(center: Coordinates, radiusKm: Double = 50.0): List<Coordinates> {
        val points = mutableListOf<Coordinates>()
        val segments = 64
        for (i in 0..segments) {
            val angle = Math.toRadians((i * 360.0 / segments))
            val lat = center.lat + (radiusKm / 111.0) * Math.sin(angle)
            val lon = center.lon + (radiusKm / (111.0 * Math.cos(Math.toRadians(center.lat)))) * Math.cos(angle)
            points.add(Coordinates(lat, lon))
        }
        return points
    }

    // Accurate Iran border coordinates (~90 points)
    val iranBorder = listOf(
        // Northwest: Turkey-Armenia-Azerbaijan border
        Coordinates(39.71, 44.79), Coordinates(39.65, 44.81), Coordinates(39.55, 44.77),
        Coordinates(39.45, 44.59), Coordinates(39.42, 44.38), Coordinates(39.39, 44.28),
        Coordinates(39.38, 44.03), Coordinates(39.36, 44.02),
        // Along Turkey border going south
        Coordinates(39.25, 44.17), Coordinates(39.10, 44.28), Coordinates(38.94, 44.38),
        Coordinates(38.81, 44.30), Coordinates(38.45, 44.32), Coordinates(38.30, 44.45),
        Coordinates(38.22, 44.56), Coordinates(38.06, 44.67),
        // Along Azerbaijan (Nakhchivan) and Turkey
        Coordinates(37.82, 44.78), Coordinates(37.59, 44.77), Coordinates(37.44, 44.60),
        Coordinates(37.15, 44.56), Coordinates(36.82, 44.60), Coordinates(36.60, 44.75),
        Coordinates(36.42, 44.83), Coordinates(36.18, 44.85),
        // Iraq border going south
        Coordinates(36.01, 44.97), Coordinates(35.82, 45.05), Coordinates(35.63, 45.16),
        Coordinates(35.41, 45.36), Coordinates(35.16, 45.39), Coordinates(34.95, 45.44),
        Coordinates(34.69, 45.56), Coordinates(34.47, 45.65), Coordinates(34.22, 45.70),
        Coordinates(33.96, 45.78), Coordinates(33.72, 45.80), Coordinates(33.48, 45.85),
        Coordinates(33.20, 45.96), Coordinates(32.96, 46.09), Coordinates(32.68, 46.17),
        Coordinates(32.42, 46.28), Coordinates(32.18, 46.39), Coordinates(31.93, 46.56),
        Coordinates(31.69, 47.06), Coordinates(31.41, 47.35), Coordinates(31.00, 47.68),
        Coordinates(30.84, 47.85), Coordinates(30.62, 47.98),
        // Shatt al-Arab / Persian Gulf coast
        Coordinates(30.44, 48.02), Coordinates(30.32, 48.16), Coordinates(30.35, 48.42),
        Coordinates(30.42, 48.85), Coordinates(30.38, 49.20), Coordinates(30.15, 49.55),
        Coordinates(29.88, 49.80), Coordinates(29.60, 50.10),
        // Bushehr coast
        Coordinates(29.35, 50.35), Coordinates(29.10, 50.60), Coordinates(28.97, 50.84),
        Coordinates(28.68, 51.10), Coordinates(28.30, 51.35), Coordinates(27.95, 51.58),
        // Kangan / Assaluyeh coast
        Coordinates(27.84, 52.06), Coordinates(27.47, 52.62), Coordinates(27.18, 53.05),
        Coordinates(26.98, 53.50), Coordinates(26.72, 53.95), Coordinates(26.55, 54.35),
        // Bandar Lengeh / Qeshm
        Coordinates(26.56, 54.88), Coordinates(26.65, 55.35), Coordinates(26.80, 55.80),
        Coordinates(27.18, 56.27),
        // Strait of Hormuz / Bandar Abbas down to Jask
        Coordinates(27.06, 56.45), Coordinates(26.68, 56.80), Coordinates(26.30, 57.15),
        Coordinates(25.95, 57.48), Coordinates(25.65, 57.77),
        // Makran / Gulf of Oman coast (Jask to Chabahar)
        Coordinates(25.48, 58.20), Coordinates(25.38, 58.65), Coordinates(25.35, 59.10),
        Coordinates(25.32, 59.55), Coordinates(25.30, 60.02), Coordinates(25.35, 60.38),
        Coordinates(25.29, 60.64),
        // Gwatar Bay / Pakistan border
        Coordinates(25.20, 61.05), Coordinates(25.15, 61.38), Coordinates(25.13, 61.66),
        // Pakistan border going north (stays along ~61.6°E)
        Coordinates(25.45, 61.62), Coordinates(25.80, 61.58), Coordinates(26.20, 61.55),
        Coordinates(26.55, 61.58), Coordinates(26.95, 61.62),
        Coordinates(27.40, 61.65), Coordinates(27.75, 61.68), Coordinates(28.10, 61.70),
        Coordinates(28.45, 61.68), Coordinates(28.80, 61.65),
        // Iran-Pakistan-Afghanistan tripoint
        Coordinates(29.04, 61.65),
        // Afghanistan border (runs along ~60.5-61.5°E going north)
        Coordinates(29.40, 61.55), Coordinates(29.75, 61.40), Coordinates(30.10, 61.30),
        Coordinates(30.50, 61.15), Coordinates(30.85, 61.05), Coordinates(31.15, 60.90),
        Coordinates(31.40, 60.85), Coordinates(31.65, 60.77),
        Coordinates(31.85, 60.68), Coordinates(32.20, 60.58), Coordinates(32.60, 60.52),
        Coordinates(33.06, 60.48), Coordinates(33.52, 60.50), Coordinates(33.76, 60.48),
        Coordinates(34.09, 60.45), Coordinates(34.32, 60.52), Coordinates(34.52, 60.58),
        Coordinates(34.63, 60.72), Coordinates(35.27, 60.88), Coordinates(35.62, 61.05),
        // Turkmenistan border
        Coordinates(35.98, 61.15), Coordinates(36.18, 61.22), Coordinates(36.48, 61.12),
        Coordinates(36.81, 60.65), Coordinates(36.95, 60.30), Coordinates(37.05, 59.92),
        Coordinates(37.18, 59.50), Coordinates(37.52, 58.80), Coordinates(37.64, 58.42),
        Coordinates(37.78, 57.90), Coordinates(37.95, 57.38), Coordinates(37.92, 56.88),
        Coordinates(37.95, 56.40), Coordinates(37.93, 55.98), Coordinates(37.88, 55.58),
        Coordinates(37.58, 55.07), Coordinates(37.48, 54.75), Coordinates(37.32, 54.38),
        Coordinates(37.25, 54.06),
        // Caspian Sea coast
        Coordinates(37.10, 53.80), Coordinates(36.86, 53.50), Coordinates(36.78, 53.15),
        Coordinates(36.82, 52.62), Coordinates(36.88, 52.10), Coordinates(36.85, 51.55),
        Coordinates(36.78, 51.15), Coordinates(36.68, 50.82), Coordinates(36.68, 50.42),
        Coordinates(36.72, 50.05), Coordinates(36.82, 49.80), Coordinates(37.10, 49.45),
        Coordinates(37.28, 49.10), Coordinates(37.60, 48.88), Coordinates(37.82, 48.73),
        Coordinates(38.05, 48.60), Coordinates(38.22, 48.58), Coordinates(38.42, 48.35),
        // Back up to Azerbaijan / Armenia
        Coordinates(38.85, 48.02), Coordinates(39.01, 47.98), Coordinates(39.10, 47.77),
        Coordinates(39.18, 47.56), Coordinates(38.88, 46.55), Coordinates(38.82, 46.17),
        Coordinates(38.88, 45.95), Coordinates(39.02, 45.62), Coordinates(39.18, 45.35),
        Coordinates(39.34, 45.03), Coordinates(39.71, 44.79) // Close loop
    )

    LaunchedEffect(webView) {
        if (webView != null) {
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

    // Dedicated Reactive Marker Management
    LaunchedEffect(state.universities, webView) {
        if (webView != null && state.universities.isNotEmpty()) {
            val markersJs = JSONArray()
            state.universities.forEach { uni ->
                val obj = JSONObject()
                obj.put("lon", uni.coordinates.lon)
                obj.put("lat", uni.coordinates.lat)
                obj.put("name", uni.name)
                markersJs.put(obj)
            }
            val js = """
                if(window.map && !window.markersAdded) {
                    window.markersAdded = true;
                    var unis = $markersJs;
                    unis.forEach(function(uni) {
                        var el = document.createElement('div');
                        el.style.width = '12px';
                        el.style.height = '12px';
                        el.style.backgroundColor = '#00E5FF';
                        el.style.borderRadius = '50%';
                        el.style.border = '2px solid #000000';
                        el.style.cursor = 'pointer';
                        new mapboxgl.Marker(el)
                            .setLngLat([uni.lon, uni.lat])
                            .addTo(map);
                    });
                }
            """.trimIndent()
            webView?.evaluateJavascript(js, null)
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
        val uniCourses = state.publicCourses.filter { course ->
            (course.organizerName != null && course.organizerName.contains(universityToDetail!!.name, ignoreCase = true))
        }
        UniversityDetailScreen(
            university = universityToDetail!!,
            courses = uniCourses,
            onBack = { universityToDetail = null }
        )
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
                            onProvinceSelected = { province ->
                                selectedProvince = province; selectedCity = null; selectedUniversity = null
                                val allUnis = province.cities.flatMap { it.universities }
                                val zoomLevel = if (province.name.contains("تهران")) 8.5 else 7.5
                                province.cities.firstOrNull()?.coordinates?.let { coords ->
                                    flyToLocation(coords.lat, coords.lon, zoomLevel, name = province.name, isProvince = true, isCity = false, isUni = false, batchUnis = allUnis)
                                }
                            },
                            onCitySelected = { city ->
                                selectedCity = city; selectedUniversity = null
                                val zoomLevel = if (city.name.contains("تهران")) 13.5 else 11.5
                                flyToLocation(city.coordinates.lat, city.coordinates.lon, zoomLevel, name = city.name, isProvince = false, isCity = true, isUni = false, batchUnis = city.universities)
                            },
                            onUniversitySelected = { uni ->
                                selectedUniversity = uni
                                flyToLocation(uni.coordinates.lat, uni.coordinates.lon, 16.0, name = uni.name, isProvince = false, isCity = false, isUni = true)
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
                            onMapReady = { webViewInstance -> webView = webViewInstance },
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
    onMapReady: (WebView) -> Unit,
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

