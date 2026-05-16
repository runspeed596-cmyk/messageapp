package com.Kelasor.app.ui.screens.mosbat_elm

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.Kelasor.app.data.repository.CourseRepository
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 My Courses Screen — Full Course Management
// ═══════════════════════════════════════════════════════════════════════════════

data class MyCoursesState(
    val myCourses: List<Course> = emptyList(),
    val enrolledCourses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrganizer: Boolean = false
)

@HiltViewModel
class MyCoursesViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MyCoursesState())
    val state: StateFlow<MyCoursesState> = _state.asStateFlow()
    init {
        loadData()
    }
    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            // Observe organizer status dynamically
            viewModelScope.launch {
                userRepository.observeCurrentUser().collect { user ->
                    if (user != null) {
                        val isOrg: Boolean = user.institutionId != null || user.isTeacher
                        _state.update { it.copy(isOrganizer = isOrg) }
                    }
                }
            }
            try {
                // Initial fetch to ensure up-to-date user info if needed
                userRepository.getCurrentUser(forceRefresh = false).collect { /* just trigger fetch */ }
            } catch (e: Exception) {
                Log.e("MyCoursesVM", "Error checking organizer", e)
            }
            try {
                val createdCourses: List<Course> = courseRepository.getMyCourses()
                val enrolledCourses: List<Course> = courseRepository.getMyEnrollments()
                
                _state.update { it.copy(
                    myCourses = createdCourses.sortedByDescending { c -> c.createdAt },
                    enrolledCourses = enrolledCourses.sortedByDescending { c -> c.createdAt },
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val result: Result<Unit> = courseRepository.deleteCourse(courseId)
                if (result.isSuccess) {
                    _state.update { state ->
                        state.copy(myCourses = state.myCourses.filter { it.id != courseId })
                    }
                }
            } catch (e: Exception) {
                Log.e("MyCoursesVM", "Error deleting course", e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit = {},
    onEditCourseClick: (String) -> Unit = {},
    onCreateCourseClick: () -> Unit = {},
    viewModel: MyCoursesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    
    var currentTime by remember { mutableStateOf(java.time.Instant.now()) }
    LaunchedEffect(Unit) {
        viewModel.loadData()
        while(true) {
            kotlinx.coroutines.delay(1000)
            currentTime = java.time.Instant.now()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isOrganizer) "دوره‌های من" else "دوره‌های خریداری شده",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (state.isOrganizer) {
                        IconButton(onClick = onCreateCourseClick) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "ایجاد دوره",
                                tint = extendedColors.accent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.loadData() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val currentList = if (state.isOrganizer && selectedTab == 0) state.myCourses else state.enrolledCourses
            
            Column(Modifier.fillMaxSize()) {
                if (state.isOrganizer) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = extendedColors.accent,
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                androidx.compose.material3.TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = extendedColors.accent
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("دوره‌های ایجاد شده", fontFamily = DanaFontFamily, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("دوره‌های خریداری شده", fontFamily = DanaFontFamily, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                if (currentList.isEmpty() && !state.isLoading) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (state.isOrganizer && selectedTab == 0) "هنوز دوره‌ای ایجاد نکرده‌اید"
                                       else "هنوز در دوره‌ای ثبت‌نام نکرده‌اید",
                                fontFamily = DanaFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                            if (state.isOrganizer && selectedTab == 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onCreateCourseClick,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = extendedColors.accent
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("ایجاد اولین دوره", fontFamily = DanaFontFamily)
                                }
                            }
                            if (state.error != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.error ?: "",
                                    fontFamily = DanaFontFamily,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stats header
                        if (state.isOrganizer && selectedTab == 0) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    StatCard(
                                        label = "تعداد دوره‌ها",
                                        value = currentList.size.toString(),
                                        icon = Icons.Default.School,
                                        color = extendedColors.accent,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        label = "فعال",
                                        value = currentList.count { it.status == "APPROVED" }.toString(),
                                        icon = Icons.Default.CheckCircle,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        label = "در انتظار تایید",
                                        value = currentList.count { it.status == "PENDING" }.toString(),
                                        icon = Icons.Default.HourglassEmpty,
                                        color = Color(0xFFFFA000),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        items(currentList, key = { it.id }) { course: Course ->
                            CourseManagementCard(
                                course = course,
                                isOrganizer = state.isOrganizer && selectedTab == 0,
                                currentTime = currentTime,
                                onClick = { onCourseClick(course.id) },
                                onEdit = { onEditCourseClick(course.id) },
                                onDelete = { showDeleteDialog = course.id },
                                accentColor = extendedColors.accent
                            )
                        }
                    }
                }
            }
        }
    }
    // Delete confirmation dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = {
                Text("حذف دوره", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "آیا از حذف این دوره اطمینان دارید؟ این عمل قابل بازگشت نیست.",
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteCourse(it) }
                    showDeleteDialog = null
                }) {
                    Text("حذف", fontFamily = DanaFontFamily, color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = color
            )
            Text(
                text = label,
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CourseManagementCard(
    course: Course,
    isOrganizer: Boolean,
    currentTime: java.time.Instant,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Course image — vertical poster
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                if (course.posterUrl != null) {
                    coil3.compose.AsyncImage(
                        model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl),
                        contentDescription = course.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(accentColor.copy(alpha = 0.3f), accentColor.copy(alpha = 0.1f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                // Discount badge
                val discountPct = course.discountPercentage ?: 0
                if (discountPct > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE53935).copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "${discountPct}% تخفیف",
                            fontFamily = DanaFontFamily,
                            fontSize = 9.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                // Status badge
                val statusColor: Color = when (course.status) {
                    "APPROVED" -> Color(0xFF4CAF50)
                    "PENDING" -> Color(0xFFFFA000)
                    "REJECTED" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val statusLabel: String = when (course.status) {
                    "APPROVED" -> "فعال"
                    "PENDING" -> "در انتظار تایید توسط ادمین"
                    "REJECTED" -> "رد شده"
                    else -> course.status ?: ""
                }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = statusLabel,
                        fontFamily = DanaFontFamily,
                        fontSize = 8.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (course.organizerName != null) {
                    Text(
                        text = "مدرس: ${course.organizerName}",
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Session Countdown Timer
                val nextChapter = course.chapters?.firstOrNull { chapter ->
                    val start = chapter.sessionStartTime
                    start != null && currentTime.isBefore(start.plus(java.time.Duration.ofHours(2)))
                }
                
                if (nextChapter != null) {
                    val startTime = nextChapter.sessionStartTime
                    val endTime = nextChapter.sessionEndTime
                    
                    if (startTime != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val (timerText, timerColor) = remember(currentTime, startTime, endTime) {
                            when {
                                currentTime.isBefore(startTime) -> {
                                    val diff = java.time.Duration.between(currentTime, startTime)
                                    val h = diff.toHours()
                                    val m = (diff.toMinutes() % 60).toInt()
                                    val s = (diff.seconds % 60).toInt()
                                    "شروع جلسه در: ${h}:${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}" to Color(0xFF2196F3)
                                }
                                endTime != null && currentTime.isBefore(endTime) -> "  در حال برگزاری" to Color(0xFFE91E63)
                                endTime == null && currentTime.isBefore(startTime.plus(java.time.Duration.ofHours(2))) -> "  در حال برگزاری" to Color(0xFFE91E63)
                                else -> "" to Color.Transparent
                            }
                        }
                        
                        if (timerText.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = timerColor.copy(alpha = 0.1f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (timerText.contains(" ")) Icons.Default.LiveTv else Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = timerColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = timerText,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = timerColor
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                // Price info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (course.priceRials > 0) {
                        val discountPct = course.discountPercentage ?: 0
                        val price = course.priceRials / 10
                        val discountedPrice = if (discountPct > 0) {
                            price - (price * discountPct / 100)
                        } else price

                        if (discountPct > 0) {
                            Text(
                                text = "${price.toPersianPrice()} تومان",
                                fontFamily = DanaFontFamily,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }

                        Text(
                            text = "${discountedPrice.toPersianPrice()} تومان",
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    } else {
                        Text(
                            text = "رایگان ✓",
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                // Organizer actions
                if (isOrganizer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onEdit,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ویرایش", fontFamily = DanaFontFamily, fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = onDelete,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFFF44336).copy(alpha = 0.1f),
                                contentColor = Color(0xFFF44336)
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", fontFamily = DanaFontFamily, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
