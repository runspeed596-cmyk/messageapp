package com.Kelasor.app.ui.screens.mosbat_elm

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.CourseRepository
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.util.toPersianNumbers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📂 Category Screen — Beautiful Course Listing by Category
// ═══════════════════════════════════════════════════════════════════════════════

data class CategoryState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedPriceType: String = "همه",
    val minRating: Float = 0f,
    val durationFilter: String = "همه",
    val statusFilter: String = "همه",
    val sortOrder: String = "جدیدترین"
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CategoryState())
    val state: StateFlow<CategoryState> = _state.asStateFlow()

    fun loadCoursesByCategory(categoryName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val allCourses: List<Course> = courseRepository.getPublicCourses()
                val filtered: List<Course> = filterByCategory(allCourses, categoryName)
                _state.update { it.copy(courses = filtered, isLoading = false) }
            } catch (e: Exception) {
                Log.e("CategoryVM", "Error loading courses", e)
                _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری دوره‌ها: ${e.message}") }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onFilterChange(priceType: String, minRating: Float, duration: String, status: String, sortOrder: String = "جدیدترین") {
        _state.update { it.copy(
            selectedPriceType = priceType,
            minRating = minRating,
            durationFilter = duration,
            statusFilter = status,
            sortOrder = sortOrder
        ) }
    }

    private fun filterByCategory(courses: List<Course>, categoryName: String): List<Course> {
        return when (categoryName) {
            "انجمن‌های علمی دانشجویی" -> courses.filter {
                it.organizerType?.uppercase() == "SCIENTIFIC_ASSOCIATION"
            }
            "کانون‌ها" -> courses.filter {
                it.organizerType?.uppercase() == "CLUB"
            }
            "دانشگاه‌ها" -> courses.filter {
                it.organizerType?.uppercase() in listOf("RESEARCH_CENTER", "UNIVERSITY")
            }
            "موسسات و آکادمی‌ها" -> courses.filter {
                it.organizerType?.uppercase() in listOf("ACADEMY", "INSTITUTE")
            }
            "تشکل‌های دانشجویی" -> courses.filter {
                it.organizerType?.uppercase() == "STUDENT_ORG"
            }
            "عمومی" -> courses.filter {
                it.organizerType?.uppercase() == "INDEPENDENT" || it.organizerType.isNullOrEmpty() ||
                it.organizerType?.uppercase() !in listOf("SCIENTIFIC_ASSOCIATION", "CLUB", "RESEARCH_CENTER", "UNIVERSITY", "ACADEMY", "INSTITUTE", "STUDENT_ORG")
            }
            "ویژه" -> courses.filter {
                it.rating >= 4.5 || it.favoritesCount >= 10 || it.enrolledCount >= 20
            }
            "تخفیف‌دار!" -> courses.filter {
                (it.discountPercentage ?: 0) > 0
            }
            "برگزارکنندگان محبوب" -> courses.sortedByDescending { it.enrolledCount }
            "مدرسین محبوب" -> courses.sortedByDescending { it.rating }
            "همه دسته‌ها" -> courses
            else -> courses
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors

    LaunchedEffect(categoryName) {
        viewModel.loadCoursesByCategory(categoryName)
    }

    val filteredCourses: List<Course> = remember(state.courses, state.searchQuery, state.selectedPriceType, state.minRating, state.durationFilter, state.statusFilter, state.sortOrder) {
        val filtered = state.courses.filter { course ->
            val matchSearch = if (state.searchQuery.isBlank()) true else course.title.contains(state.searchQuery, ignoreCase = true)
            val matchPrice = when (state.selectedPriceType) {
                "رایگان" -> course.isFree || course.priceRials == 0L
                "نقدی" -> !course.isFree && course.priceRials > 0L
                else -> true
            }
            val matchRating = course.rating >= state.minRating
            val matchDuration = when (state.durationFilter) {
                "زیر ۲ ساعت" -> (course.durationMinutes ?: 0) < 120
                "۲ تا ۱۰ ساعت" -> (course.durationMinutes ?: 0) in 120..600
                "بالای ۱۰ ساعت" -> (course.durationMinutes ?: 0) > 600
                else -> true
            }
            val matchStatus = when (state.statusFilter) {
                "در حال برگزاری" -> course.startsAt?.isBefore(java.time.Instant.now()) == true
                "به‌زودی" -> course.startsAt?.isAfter(java.time.Instant.now()) == true
                "به اتمام رسیده" -> course.endsAt?.isBefore(java.time.Instant.now()) == true
                else -> true
            }
            matchSearch && matchPrice && matchRating && matchDuration && matchStatus
        }
        when (state.sortOrder) {
            "جدیدترین" -> filtered.sortedByDescending { it.createdAt }
            "محبوب‌ترین" -> filtered.sortedByDescending { it.enrolledCount }
            "بالاترین امتیاز" -> filtered.sortedByDescending { it.rating }
            "ارزان‌ترین" -> filtered.sortedBy { it.priceRials }
            "گران‌ترین" -> filtered.sortedByDescending { it.priceRials }
            else -> filtered
        }
    }
    val activeFilterCount: Int = remember(state) {
        var count = 0
        if (state.selectedPriceType != "همه") count++
        if (state.minRating > 0f) count++
        if (state.durationFilter != "همه") count++
        if (state.statusFilter != "همه") count++
        if (state.sortOrder != "جدیدترین") count++
        count
    }

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName, fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.loadCoursesByCategory(categoryName) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Stats Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = extendedColors.accent.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "دوره‌های $categoryName",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${filteredCourses.size.toString().toPersianNumbers()} دوره",
                                fontFamily = DanaFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = extendedColors.accent,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Search & Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("جستجو در مثبت علم...", fontFamily = DanaFontFamily, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    val hasActiveFilters = activeFilterCount > 0
                    androidx.compose.foundation.layout.Box {
                        Row(
                            modifier = Modifier
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (hasActiveFilters) extendedColors.accent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { showFilterSheet = true }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "فیلتر پیشرفته",
                                tint = if (hasActiveFilters) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "فیلتر",
                                fontFamily = DanaFontFamily,
                                fontSize = 13.sp,
                                color = if (hasActiveFilters) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (activeFilterCount > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                                containerColor = extendedColors.accent,
                                contentColor = Color.White
                            ) {
                                Text(activeFilterCount.toString().toPersianNumbers(), fontFamily = DanaFontFamily, fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Active Filter Chips
                if (activeFilterCount > 0) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (state.selectedPriceType != "همه") {
                            item {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onFilterChange("همه", state.minRating, state.durationFilter, state.statusFilter, state.sortOrder) },
                                    label = { Text(state.selectedPriceType, fontFamily = DanaFontFamily, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                        if (state.minRating > 0f) {
                            item {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onFilterChange(state.selectedPriceType, 0f, state.durationFilter, state.statusFilter, state.sortOrder) },
                                    label = { Text("${state.minRating.toInt()} ستاره+", fontFamily = DanaFontFamily, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                        if (state.durationFilter != "همه") {
                            item {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onFilterChange(state.selectedPriceType, state.minRating, "همه", state.statusFilter, state.sortOrder) },
                                    label = { Text(state.durationFilter, fontFamily = DanaFontFamily, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                        if (state.statusFilter != "همه") {
                            item {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onFilterChange(state.selectedPriceType, state.minRating, state.durationFilter, "همه", state.sortOrder) },
                                    label = { Text(state.statusFilter, fontFamily = DanaFontFamily, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                        if (state.sortOrder != "جدیدترین") {
                            item {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onFilterChange(state.selectedPriceType, state.minRating, state.durationFilter, state.statusFilter, "جدیدترین") },
                                    label = { Text(state.sortOrder, fontFamily = DanaFontFamily, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (state.error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            state.error ?: "",
                            fontFamily = DanaFontFamily,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else if (filteredCourses.isEmpty() && !state.isLoading) {
                    // Beautiful empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "هنوز دوره‌ای در این دسته ثبت نشده است",
                                fontFamily = DanaFontFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "با عضویت به عنوان برگزارکننده، اولین دوره را ایجاد کنید!",
                                fontFamily = DanaFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Course Grid (Taghcheh-style 2-column)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCourses, key = { it.id }) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) }
                            )
                        }
                    }
                }
            }
        }
    } // End of Scaffold content padding block
    
    if (showFilterSheet) {
        FilterBottomSheet(
            currentState = state,
            onDismiss = { showFilterSheet = false },
            onApply = { price, rating, duration, status, sort ->
                viewModel.onFilterChange(price, rating, duration, status, sort)
                showFilterSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentState: CategoryState,
    onDismiss: () -> Unit,
    onApply: (String, Float, String, String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var priceType by remember { mutableStateOf(currentState.selectedPriceType) }
    var minRating by remember { mutableFloatStateOf(currentState.minRating) }
    var duration by remember { mutableStateOf(currentState.durationFilter) }
    var status by remember { mutableStateOf(currentState.statusFilter) }
    var sortOrder by remember { mutableStateOf(currentState.sortOrder) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                "فیلترهای پیشرفته",
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            // Sort Order
            FilterSection(title = "مرتب‌سازی") {
                val sortOptions = listOf("جدیدترین", "محبوب‌ترین", "بالاترین امتیاز", "ارزان‌ترین", "گران‌ترین")
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    sortOptions.forEach { s ->
                        FilterChip(
                            selected = sortOrder == s,
                            onClick = { sortOrder = s },
                            label = { Text(s, fontFamily = DanaFontFamily) }
                        )
                    }
                }
            }
            // Price Type
            FilterSection(title = "نوع قیمت") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("همه", "رایگان", "نقدی").forEach { type ->
                        FilterChip(
                            selected = priceType == type,
                            onClick = { priceType = type },
                            label = { Text(type, fontFamily = DanaFontFamily) }
                        )
                    }
                }
            }
            // Rating
            FilterSection(title = "حداقل امتیاز") {
                Slider(
                    value = minRating,
                    onValueChange = { minRating = it },
                    valueRange = 0f..5f,
                    steps = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${minRating.toInt().toString().toPersianNumbers()} ستاره به بالا",
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Duration
            FilterSection(title = "مدت زمان") {
                val durations = listOf("همه", "زیر ۲ ساعت", "۲ تا ۱۰ ساعت", "بالای ۱۰ ساعت")
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    durations.forEach { d ->
                        FilterChip(
                            selected = duration == d,
                            onClick = { duration = d },
                            label = { Text(d, fontFamily = DanaFontFamily) }
                        )
                    }
                }
            }
            // Status
            FilterSection(title = "وضعیت برگزاری") {
                val statuses = listOf("همه", "در حال برگزاری", "به‌زودی", "به اتمام رسیده")
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontFamily = DanaFontFamily) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onApply(priceType, minRating, duration, status, sortOrder) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("اعمال فیلترها", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = {
                    priceType = "همه"
                    minRating = 0f
                    duration = "همه"
                    status = "همه"
                    sortOrder = "جدیدترین"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("پاکسازی همه", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            title,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}
