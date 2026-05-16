package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.crossfade
import com.Kelasor.app.R
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.domain.model.Institution
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AppConnectionState
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.Kelasor.app.util.toPersianDateTime
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosbatElmScreen(
    onNavigateToElm: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToOrganizerSetup: () -> Unit = {},
    onNavigateToCreateCourse: () -> Unit = {},
    appConnectionState: AppConnectionState = AppConnectionState.CONNECTED,
    onNavigateToCourseDetail: (String) -> Unit = {},
    onNavigateToInstitution: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToTeacherProfile: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToCategory: (String) -> Unit = {},
    viewModel: MosbatElmScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors

    LaunchedEffect(appConnectionState) {
        if (appConnectionState == AppConnectionState.CONNECTED && state.publicCourses.isEmpty() && !state.isLoading) {
            viewModel.loadData()
        }
    }

    // Search filter state (must be outside LazyColumn for BottomSheet to work)
    var searchPriceType by remember { mutableStateOf("همه") }
    var searchMinRating by remember { mutableFloatStateOf(0f) }
    var searchDuration by remember { mutableStateOf("همه") }
    var searchStatus by remember { mutableStateOf("همه") }
    var searchSortOrder by remember { mutableStateOf("جدیدترین") }
    var showSearchFilter by remember { mutableStateOf(false) }
    val searchActiveFilterCount: Int = remember(searchPriceType, searchMinRating, searchDuration, searchStatus, searchSortOrder) {
        var c = 0
        if (searchPriceType != "همه") c++
        if (searchMinRating > 0f) c++
        if (searchDuration != "همه") c++
        if (searchStatus != "همه") c++
        if (searchSortOrder != "جدیدترین") c++
        c
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                MosbatElmHeader(
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToCreateCourse = onNavigateToCreateCourse,
                    onNavigateToOrganizerSetup = onNavigateToOrganizerSetup,
                    onNavigateToNotifications = onNavigateToNotifications,
                    appConnectionState = appConnectionState,
                    isOrganizer = state.isOrganizer,
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    isSearchActive = state.isSearchActive,
                    onToggleSearch = { viewModel.onToggleSearch(it) }
                )
            }
            
            if (state.isSearchActive) {
                val filteredCourses: List<Course> = state.publicCourses.filter {
                    it.title.contains(state.searchQuery, ignoreCase = true) ||
                    it.organizerName?.contains(state.searchQuery, ignoreCase = true) == true ||
                    it.slogan?.contains(state.searchQuery, ignoreCase = true) == true
                }.filter { course ->
                    val matchPrice = when (searchPriceType) {
                        "رایگان" -> course.isFree || course.priceRials == 0L
                        "نقدی" -> !course.isFree && course.priceRials > 0L
                        else -> true
                    }
                    val matchRating = course.rating >= searchMinRating
                    val matchDuration = when (searchDuration) {
                        "زیر ۲ ساعت" -> (course.durationMinutes ?: 0) < 120
                        "۲ تا ۱۰ ساعت" -> (course.durationMinutes ?: 0) in 120..600
                        "بالای ۱۰ ساعت" -> (course.durationMinutes ?: 0) > 600
                        else -> true
                    }
                    val matchStatus = when (searchStatus) {
                        "در حال برگزاری" -> course.startsAt?.isBefore(java.time.Instant.now()) == true
                        "به‌زودی" -> course.startsAt?.isAfter(java.time.Instant.now()) == true
                        "به اتمام رسیده" -> course.endsAt?.isBefore(java.time.Instant.now()) == true
                        else -> true
                    }
                    matchPrice && matchRating && matchDuration && matchStatus
                }.let { filtered ->
                    when (searchSortOrder) {
                        "جدیدترین" -> filtered.sortedByDescending { it.createdAt }
                        "محبوب‌ترین" -> filtered.sortedByDescending { it.enrolledCount }
                        "بالاترین امتیاز" -> filtered.sortedByDescending { it.rating }
                        "ارزان‌ترین" -> filtered.sortedBy { it.priceRials }
                        "گران‌ترین" -> filtered.sortedByDescending { it.priceRials }
                        else -> filtered
                    }
                }

                // Filter Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${filteredCourses.size.toString().toPersianNumbers()} نتیجه",
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Box {
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (searchActiveFilterCount > 0) extendedColors.accent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { showSearchFilter = true }
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.FilterList, contentDescription = "فیلتر", tint = if (searchActiveFilterCount > 0) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فیلتر", fontFamily = DanaFontFamily, fontSize = 13.sp, color = if (searchActiveFilterCount > 0) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (searchActiveFilterCount > 0) {
                                Badge(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(2.dp),
                                    containerColor = extendedColors.accent,
                                    contentColor = Color.White
                                ) {
                                    Text(searchActiveFilterCount.toString().toPersianNumbers(), fontFamily = DanaFontFamily, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                if (filteredCourses.isEmpty() && state.searchQuery.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("دوره‌ای یافت نشد", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                } else {
                    item {
                        LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.height(1200.dp)
                        ) {
                            items(filteredCourses) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onNavigateToCourseDetail(course.id) }
                                )
                            }
                        }
                    }
                }

                // Search filter bottom sheet - rendered outside LazyColumn
            } else {

            // Slider / Carousel
            item {
                MosbatElmCarousel(banners = state.banners)
            }

            // 3D Categories
            item {
                MosbatElmCategories(
                    categoriesFromApi = state.categories,
                    onCategoryClick = { categoryName ->
                        if (categoryName == "دانشگاه‌ها") {
                            onNavigateToElm()
                        } else {
                            onNavigateToCategory(categoryName)
                        }
                    }
                )
            }

            // Featured Courses (Special Section)
            // User requested: Light Green to Dark Green gradient
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(vertical = 24.dp)
                ) {
                    SectionTitle(
                        title = "ویژه مثبت علم", 
                        icon = Icons.Rounded.WorkspacePremium,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconSize = 44.dp,
                        onViewAllClick = { onNavigateToCategory("ویژه") }
                    )
                    
                    // Fixed message as requested: "فعلا روش نوشته بشه هیچ دوره ای وجود ندارد"
                    Text(
                        "هیچ دوره‌ای وجود ندارد",
                        color = Color.White.copy(alpha = 0.9f),
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                    )
                }
            }

            // New Courses
            if (state.newCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "جدیدترین دوره‌ها", 
                        icon = Icons.Rounded.FiberNew,
                        onViewAllClick = { onNavigateToCategory("همه دسته‌ها") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.newCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Top Instructors - MOVED TO END

            // Discounted Courses
            if (state.discountedCourses.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تخفیف‌دار!",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire))
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = "مشاهده همه",
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onNavigateToCategory("تخفیف‌دار!") }
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.discountedCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Institute Courses
            if (state.instituteCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "آکادمی و موسسات", 
                        icon = Icons.Rounded.Business,
                        onViewAllClick = { onNavigateToCategory("آکادمی و موسسات") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.instituteCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Club Courses
            if (state.clubCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "کانون‌ها", 
                        icon = Icons.Rounded.School,
                        onViewAllClick = { onNavigateToCategory("کانون‌ها") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.clubCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Association Courses
            if (state.associationCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "انجمن‌های علمی دانشجویی", 
                        icon = Icons.Rounded.Science,
                        onViewAllClick = { onNavigateToCategory("انجمن‌های علمی دانشجویی") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.associationCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Student Org Courses
            if (state.studentOrgCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "تشکل‌های دانشجویی", 
                        icon = Icons.Rounded.Groups,
                        onViewAllClick = { onNavigateToCategory("تشکل‌های دانشجویی") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.studentOrgCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Research Center Courses
            if (state.researchCenterCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "دانشگاه‌ها و مراکز تحقیقاتی", 
                        icon = Icons.Rounded.AccountBalance,
                        onViewAllClick = { onNavigateToElm() }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.researchCenterCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // All Public Courses (now horizontal)
            if (state.publicCourses.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "همه دوره‌ها", 
                        icon = Icons.Rounded.School,
                        onViewAllClick = { onNavigateToCategory("همه دسته‌ها") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.publicCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }

            // Popular Organizers
            if (state.popularInstitutions.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "برگزارکنندگان محبوب", 
                        icon = Icons.Rounded.Stars,
                        onViewAllClick = { onNavigateToCategory("برگزارکنندگان محبوب") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.popularInstitutions) { institution ->
                            InstitutionCard(
                                institution = institution,
                                onClick = { onNavigateToInstitution(institution.id) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }



            // Top Instructors (Popular Teachers)
            if (state.topInstructors.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "اساتید محبوب", 
                        icon = Icons.Rounded.People,
                        onViewAllClick = { onNavigateToCategory("مدرسین محبوب") }
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.topInstructors) { instructor ->
                            InstructorCard(
                                instructor = instructor,
                                onClick = { onNavigateToTeacherProfile(instructor.id) }
                            )
                        }
                    }
                }
            }
            }
        }
        // Search Filter BottomSheet — must be outside LazyColumn
        if (showSearchFilter) {
            FilterBottomSheet(
                currentState = CategoryState(
                    selectedPriceType = searchPriceType,
                    minRating = searchMinRating,
                    durationFilter = searchDuration,
                    statusFilter = searchStatus,
                    sortOrder = searchSortOrder
                ),
                onDismiss = { showSearchFilter = false },
                onApply = { price, rating, duration, status, sort ->
                    searchPriceType = price
                    searchMinRating = rating
                    searchDuration = duration
                    searchStatus = status
                    searchSortOrder = sort
                    showSearchFilter = false
                }
            )
        }
    }
}

@Composable
fun MosbatElmHeader(
    onNavigateToProfile: () -> Unit,
    onNavigateToCreateCourse: () -> Unit,
    onNavigateToOrganizerSetup: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    appConnectionState: AppConnectionState,
    isOrganizer: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    isSearchActive: Boolean = false,
    onToggleSearch: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        if (!isSearchActive) {
            // Top Row: Title + Action Button + Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "مثبت علم",
                    fontFamily = DanaFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (isOrganizer) {
                    Button(
                        onClick = onNavigateToCreateCourse,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ایجاد دوره", fontFamily = DanaFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onNavigateToOrganizerSetup,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("برگزارکننده هستم", fontFamily = DanaFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .size(40.dp)
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "نوتیفیکیشن‌ها", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onNavigateToProfile() }
                ) {
                    AvatarImage(imageUrl = null, name = "M", size = AvatarSize.MEDIUM)
                }
            }
            // Fake Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
                    .height(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onToggleSearch(true) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("جستجو در مثبت علم...", fontFamily = DanaFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Real Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onToggleSearch(false); onSearchQueryChange("") }) {
                    Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("جستجو در مثبت علم...", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(21.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@Composable
fun MosbatElmCarousel(banners: List<com.Kelasor.app.data.remote.dto.BannerDto>) {
    if (banners.isEmpty()) return

    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(banners.size) {
        while (banners.isNotEmpty()) {
            delay(4000)
            currentPage = (currentPage + 1) % banners.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "banner_transition"
                ) { page ->
                    val banner = banners.getOrNull(page)
                    AsyncImage(
                        model = com.Kelasor.app.util.UrlUtils.getFullUrl(banner?.imageUrl),
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 300f
                            )
                        )
                )
                
                banners.getOrNull(currentPage)?.let { banner ->
                    Text(
                        text = banner.title,
                        color = Color.White,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            banners.indices.forEach { index ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == index) 24.dp else 8.dp,
                    label = "indicator_width"
                )
                val color by animateColorAsState(
                    targetValue = if (currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    label = "indicator_color"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun MosbatElmCategories(categoriesFromApi: List<String>, onCategoryClick: (String) -> Unit = {}) {
    // Standard categories as requested
    val defaultCategories = listOf(
        Pair("انجمن‌های علمی دانشجویی", R.drawable.ic3d_student),
        Pair("کانون‌ها", R.drawable.ic3d_center),
        Pair("دانشگاه‌ها", R.drawable.ic3d_school),
        Pair("آکادمی و موسسات", R.drawable.ic3d_academy),
        Pair("عمومی", R.drawable.ic3d_public),
        Pair("ویژه", R.drawable.ic3d_special),
        Pair("تخفیف‌دار!", R.drawable.ic3d_price),
        Pair("همه دسته‌ها", R.drawable.ic3d_all)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultCategories.take(4).forEach { (title, iconRes) ->
                CategoryItem(
                    title = title, 
                    iconRes = iconRes, 
                    modifier = Modifier.weight(1f), 
                    onClick = { onCategoryClick(title) }
                )
            }
        }
        
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultCategories.drop(4).forEach { (title, iconRes) ->
                val isLarge = title.contains("ویژه") || title.contains("تخفیف")
                val iconSize = if (isLarge) 110.dp else 48.dp
                CategoryItem(
                    title = title, 
                    iconRes = iconRes, 
                    modifier = Modifier.weight(1f), 
                    iconSize = iconSize,
                    showBackground = !isLarge,
                    onClick = { onCategoryClick(title) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    title: String, 
    iconRes: Int, 
    modifier: Modifier = Modifier, 
    iconSize: androidx.compose.ui.unit.Dp = 48.dp, 
    showBackground: Boolean = true,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(
                    if (showBackground) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontFamily = DanaFontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

 @Composable
fun SectionTitle(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    onViewAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(iconSize + 10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = "مشاهده همه",
            fontFamily = DanaFontFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Premium CourseCard — World-Class Design
// ═══════════════════════════════════════════════════════════════════════════════

private val chipColors: List<Color> = listOf(
    Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
    Color(0xFF96CEB4), Color(0xFFF0A500), Color(0xFFDDA0DD),
    Color(0xFF98D8C8), Color(0xFFF7DC6F)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountPct: Int = course.discountPercentage
    val price: Long = course.priceRials / 10
    val hasDiscount: Boolean = !course.isFree && discountPct > 0 && price > 0
    Card(
        modifier = modifier
            .width(180.dp)
            .height(420.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Top: Highly Portrait Poster ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            ) {
                // Background image (blurred and stretched)
                AsyncImage(
                    model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl)
                        ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                
                // Foreground image (fitted)
                AsyncImage(
                    model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl)
                        ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
                    contentDescription = course.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 140f - 60f
                            )
                        )
                )
                // Top Right: Popular badge
                if (course.enrolledCount > 30) {
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFE67E22), RoundedCornerShape(4.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, Modifier.size(7.dp), tint = Color.White)
                            Spacer(Modifier.width(1.dp))
                            Text("محبوب", color = Color.White, fontFamily = DanaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Top Left: Capacity Badge
                if (course.capacity != null && course.capacity > 0) {
                    val isFull = course.enrolledCount >= course.capacity
                    val capacityText = if (isFull) "تکمیل" else "ظرفیت: ${course.capacity.toString().toPersianNumbers()}"
                    val bgColor = if (isFull) MaterialTheme.colorScheme.error else Color(0xFFFF66B2)
                    Box(
                        modifier = Modifier
                            .padding(3.dp)
                            .align(Alignment.TopStart)
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = capacityText,
                            color = if (isFull) Color.White else Color.Black,
                            fontFamily = DanaFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Bottom Right: Discount Badge
                if (hasDiscount && discountPct in 5..100) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val discountResName = "discount_$discountPct"
                    val discountResId = remember(discountPct) {
                        context.resources.getIdentifier(discountResName, "drawable", context.packageName)
                    }
                    if (discountResId != 0) {
                        Image(
                            painter = painterResource(id = discountResId),
                            contentDescription = "تخفیف $discountPct درصد",
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(x = (-45).dp, y = 53.dp)
                                .size(140.dp)
                        )
                    }
                }
            }

            // ── Body ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = course.title.toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(6.dp))

                // Instructor & Organizer
                val instructor = course.instructors.firstOrNull()
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Instructor (Individual)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AvatarImage(
                            imageUrl = instructor?.displayAvatarUrl,
                            name = instructor?.displayName ?: "ناشناس",
                            size = AvatarSize.EXTRA_SMALL
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مدرس: ${(instructor?.displayName ?: "ثبت نشده").toPersianNumbers()}",
                            fontFamily = DanaFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Organizer (Academy/Institution)
                    if (!course.organizerName.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AvatarImage(
                                imageUrl = course.organizerAvatarUrl,
                                name = course.organizerName,
                                size = AvatarSize.EXTRA_SMALL
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "برگزارکننده: ${course.organizerName.toPersianNumbers()}",
                                fontFamily = DanaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Tags (Simplified for tiny card)
                val fieldsToDisplay = mutableListOf<String>()
                if (course.suitableFor.isNotEmpty()) {
                    fieldsToDisplay.addAll(course.suitableFor)
                } else {
                    if (!course.fieldOfStudy.isNullOrEmpty()) fieldsToDisplay.add(course.fieldOfStudy)
                    fieldsToDisplay.addAll(course.tags)
                }
                val displayFields = fieldsToDisplay.map { it.replace("مخصوص", "").replace("(", "").replace(")", "").trim() }.filter { it.isNotEmpty() }.distinct().take(3)
                
                Box(modifier = Modifier.height(20.dp)) {
                    if (displayFields.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            displayFields.forEach { fieldText ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFD54F), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 2.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = fieldText,
                                        color = Color.Black,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                Spacer(Modifier.height(2.dp))

                // Price Section
                // Price Section
                Box(modifier = Modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        if (course.isFree || price == 0L) {
                            Text(
                                text = "رایگان",
                                fontFamily = DanaFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else if (hasDiscount) {
                            val discountedPrice: Long = price - (price * discountPct / 100)
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = price.toPersianPrice(),
                                    fontFamily = DanaFontFamily,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${discountedPrice.toPersianPrice()} تومان",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = DanaFontFamily, 
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = "${price.toPersianPrice()} تومان",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = DanaFontFamily, 
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // No spacer here for ultra-tight layout

                // Time and Users
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if ((course.durationMinutes ?: 0) > 0) {
                            val hours = course.durationMinutes!! / 60
                            val mins = course.durationMinutes % 60
                            val durationText = if (hours > 0 && mins > 0) "$hours ساعت و $mins" else if (hours > 0) "$hours ساعت" else "$mins دقیقه"
                            Icon(Icons.Rounded.AccessTime, null, Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = durationText.toPersianNumbers(),
                                fontFamily = DanaFontFamily,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Icon(Icons.Rounded.PeopleOutline, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = course.enrolledCount.toString().toPersianNumbers(),
                            fontFamily = DanaFontFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    CourseTimeStatus(course = course)
                }
                Spacer(Modifier.height(4.dp))
                // Register Button
                val isFull = course.capacity != null && course.enrolledCount >= course.capacity

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFull) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (isFull) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    enabled = !isFull
                ) {
                    if (isFull) {
                        Icon(Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "تکمیل",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Rounded.HowToReg, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "ثبت نام",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseTimeStatus(course: Course) {
    val now = java.time.Instant.now()
    if (course.chapters.isEmpty()) return // No chapters -> Hide timer completely
    
    val nextChapter = course.chapters.firstOrNull { chapter ->
        val start = chapter.sessionStartTime
        start != null && now.isBefore(start.plus(java.time.Duration.ofHours(2)))
    }
    
    if (nextChapter == null) return
    val startsAt = nextChapter.sessionStartTime ?: return
    
    val duration: java.time.Duration = java.time.Duration.between(now, startsAt)
    if (duration.isNegative) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
            Spacer(Modifier.width(4.dp))
            Text("در حال برگزاری", fontFamily = DanaFontFamily, fontSize = 9.sp,
                color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
        return
    }
    var tick: Long by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); tick++ } }
    val remaining: java.time.Duration = java.time.Duration.between(java.time.Instant.now(), startsAt)
    val days: Long = remaining.toDays()
    val hours: Long = remaining.toHours() % 24
    val mins: Long = remaining.toMinutes() % 60
    val secs: Long = remaining.seconds % 60
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Timer, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(3.dp))
        val timeText: String = if (days > 0) "$days روز ${String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, mins, secs)}"
        else String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        Text(timeText.toPersianNumbers(), fontFamily = DanaFontFamily, fontSize = 9.sp,
            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InstitutionCard(
    institution: Institution,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(institution.logoUrl)
                if (!fullUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = institution.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val ratingText = if (institution.averageRating > 0.0) String.format(java.util.Locale.US, "%.1f", institution.averageRating) else "جدید"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ratingText.toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = institution.name,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${institution.courseCount} دوره",
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun InstructorCard(
    instructor: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(instructor.avatarUrl)
                if (!fullUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = instructor.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(Modifier.fillMaxSize().clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val ratingText = if (instructor.averageRating > 0.0) String.format(java.util.Locale.US, "%.1f", instructor.averageRating) else "جدید"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = ratingText.toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = instructor.displayName,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Note: Since User currently doesn't expose courseCount directly on the User model in MosbatElmScreen, we might omit the course count text or show role.
            Text(
                text = "استاد",
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun MyCourseItem(
    course: Course,
    modifier: Modifier = Modifier.width(280.dp),
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                AsyncImage(
                    model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Status Badge
                val (statusText, statusColor) = when (course.status) {
                    "APPROVED" -> "تأیید شده" to Color(0xFF4CAF50)
                    "PENDING" -> "در انتظار تأیید" to Color(0xFFFF9800)
                    "REJECTED" -> "رد شده" to Color(0xFFF44336)
                    else -> "نامشخص" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = statusColor.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontFamily = DanaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Discount or Price badge
                val price = course.priceRials / 10
                val formattedPrice = if (price > 0) "${price.toPersianPrice()} تومان" else "رایگان"
                val discountPct = course.discountPercentage ?: 0
                
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (course.isFree || price == 0L) {
                            Text(
                                text = "رایگان",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontFamily = DanaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (discountPct > 0) {
                            val discountedPrice = price - (price * discountPct / 100)
                            val formattedDiscounted = discountedPrice.toPersianPrice()
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "%${discountPct.toString().toPersianNumbers()}",
                                        color = Color.White,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedPrice,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 9.sp,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                    Text(
                                        text = "$formattedDiscounted تومان",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "$formattedPrice تومان",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontFamily = DanaFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Capacity Badge
                if (course.capacity != null && course.capacity > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.BottomEnd)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ظرفیت: ${course.capacity.toString().toPersianNumbers()}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = DanaFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title.toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Teacher / Organizer
                val displayName = course.instructors.firstOrNull()?.displayName ?: course.organizerName ?: "ناشناس"
                val displayAvatar = course.instructors.firstOrNull()?.displayAvatarUrl ?: course.organizerAvatarUrl
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AvatarImage(
                        imageUrl = displayAvatar,
                        name = displayName,
                        size = AvatarSize.SMALL
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayName.toPersianNumbers(),
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time / Date info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = course.startsAt?.toString()?.toPersianDateTime() ?: "تاریخ نامشخص",
                            fontFamily = DanaFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = (course.syllabusDuration ?: "نامشخص").toPersianNumbers(),
                            fontFamily = DanaFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseListItem(
    course: Course,
    onClick: () -> Unit
) {
    val price = course.priceRials / 10
    val discountPct = course.discountPercentage
    val hasDiscount: Boolean = !course.isFree && discountPct > 0 && price > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(95.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = coil3.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = course.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            if (hasDiscount) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "%${discountPct.toString().toPersianNumbers()}",
                        color = Color.White,
                        fontFamily = DanaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.title.toPersianNumbers(),
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = course.startsAt?.toString()?.toPersianDateTime() ?: "تاریخ نامشخص",
                    fontFamily = DanaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val instructorName = course.instructors.firstOrNull()?.displayName 
                    ?: course.manualInstructors.firstOrNull()?.name 
                    ?: course.organizerName 
                    ?: "ناشناس"
                Text(
                    text = instructorName.toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    if (course.isFree || price == 0L) {
                        Text(
                            text = "رایگان",
                            fontFamily = DanaFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    } else if (hasDiscount) {
                        val discountedPrice = price - (price * discountPct / 100)
                        Text(
                            text = price.toPersianPrice(),
                            fontFamily = DanaFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                        Text(
                            text = "${discountedPrice.toPersianPrice()} تومان",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = "${price.toPersianPrice()} تومان",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.SearchOff,
            contentDescription = "Empty",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "موردی یافت نشد",
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "در حال حاضر دوره‌ای برای نمایش وجود ندارد.",
            fontFamily = DanaFontFamily,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
