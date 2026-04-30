package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.Kelasor.app.R
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.domain.model.Institution
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AppConnectionState
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import kotlinx.coroutines.delay
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
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
                val filteredCourses = state.publicCourses.filter {
                    it.title.contains(state.searchQuery, ignoreCase = true)
                }
                
                if (filteredCourses.isEmpty() && state.searchQuery.isNotEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("دوره‌ای یافت نشد", fontFamily = VazirFontFamily, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                } else {
                    item {
                        LazyVerticalGrid(
                            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.height(600.dp) // Constrain grid height inside LazyColumn
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
            } else {

            // My Courses (Management) - Only for organizers
            if (state.isOrganizer && state.myCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "دوره‌های من (مدیریت)", icon = Icons.Rounded.ManageAccounts)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.myCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Slider / Carousel
            item {
                MosbatElmCarousel(banners = state.banners)
            }

            // 3D Categories
            item {
                MosbatElmCategories(categoriesFromApi = state.categories, onCategoryClick = onNavigateToCategory)
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
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (state.featuredCourses.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.featuredCourses) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { onNavigateToCourseDetail(course.id) },
                                    modifier = Modifier.width(200.dp)
                                )
                            }
                        }
                    } else {
                        // Placeholder for mock data if empty (as user allowed mock for special)
                        Text(
                            "در حال بارگذاری دوره‌های ویژه...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = VazirFontFamily,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
                        )
                    }
                }
            }

            // Top Institutions
            if (state.topInstitutions.isNotEmpty()) {
                item {
                    SectionTitle(title = "برگزارکنندگان محبوب", icon = Icons.Rounded.Business)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.topInstitutions) { institution ->
                            InstitutionCard(
                                institution = institution,
                                onClick = { onNavigateToInstitution(institution.id) }
                            )
                        }
                    }
                }
            }

            // New Courses
            if (state.newCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "جدیدترین دوره‌ها", icon = Icons.Rounded.FiberNew)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.newCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // Top Instructors
            if (state.topInstructors.isNotEmpty()) {
                item {
                    SectionTitle(title = "مدرسین محبوب", icon = Icons.Rounded.People)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.topInstructors) { instructor ->
                            InstructorCard(
                                instructor = instructor,
                                onClick = { onNavigateToUserProfile(instructor.id) }
                            )
                        }
                    }
                }
            }

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
                            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fire))
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تخفیف‌دار!",
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "مشاهده همه",
                            fontFamily = VazirFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { }
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
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // Institute Courses
            if (state.instituteCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "موسسات و آکادمی‌ها", icon = Icons.Rounded.Business)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.instituteCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // Club Courses
            if (state.clubCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "کانون‌ها", icon = Icons.Rounded.Diversity3)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.clubCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // Association Courses
            if (state.associationCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "انجمن‌های علمی دانشجویی", icon = Icons.Rounded.Science)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.associationCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

            // All Public Courses (now horizontal)
            if (state.publicCourses.isNotEmpty()) {
                item {
                    SectionTitle(title = "همه دوره‌ها", icon = Icons.Rounded.School)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.publicCourses) { course ->
                            CourseCard(
                                course = course,
                                onClick = { onNavigateToCourseDetail(course.id) },
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }
            }
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
                    fontFamily = VazirFontFamily,
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
                        Text("ایجاد دوره", fontFamily = VazirFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        Text("برگزارکننده هستم", fontFamily = VazirFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                Text("جستجو در دوره‌ها...", fontFamily = VazirFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Icon(Icons.Default.ArrowForward, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("جستجو در دوره‌ها...", fontFamily = VazirFontFamily) },
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
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
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
        Pair("کانون‌ها", R.drawable.ic3d_target),
        Pair("دانشگاه‌ها", R.drawable.ic3d_school),
        Pair("موسسات و آکادمی‌ها", R.drawable.ic3d_setting),
        Pair("عمومی", R.drawable.ic3d_shield),
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
                CategoryItem(title = title, iconRes = iconRes, modifier = Modifier.weight(1f), onClick = { onCategoryClick(title) })
            }
        }
        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultCategories.drop(4).forEach { (title, iconRes) ->
                CategoryItem(title = title, iconRes = iconRes, modifier = Modifier.weight(1f), onClick = { onCategoryClick(title) })
            }
        }
    }
}

@Composable
private fun CategoryItem(title: String, iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontFamily = VazirFontFamily,
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
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
        }
        Text(
            text = "مشاهده همه",
            fontFamily = VazirFontFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onViewAllClick() }
        )
    }
}

@Composable
fun CourseCard(
    course: Course,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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
                    .aspectRatio(0.85f)
            ) {
                AsyncImage(
                    model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
                    contentDescription = course.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (course.status != "APPROVED") {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (course.status == "PENDING") "در انتظار تأیید" else "رد شده",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontFamily = VazirFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (course.capacity != null && course.capacity > 0) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopStart)
                            .background(
                                color = Color(0xFFE91E63).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ظرفیت\n ${course.capacity.toString().toPersianNumbers()} نفر",
                            color = Color.White,
                            fontFamily = VazirFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                if (course.rating > 4.5) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .align(if (course.status != "APPROVED") Alignment.TopStart else Alignment.TopEnd)
                            .background(
                                color = Color(0xFFE65100).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "محبوب",
                                color = Color.White,
                                fontFamily = VazirFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Rounded.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }

                val discountPct = course.discountPercentage ?: 0
                if (discountPct > 0) {
                    val resId = context.resources.getIdentifier("discount_$discountPct", "drawable", context.packageName)
                    if (resId != 0) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = resId),
                            contentDescription = "تخفیف $discountPct درصد",
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(60.dp)
                                .offset(x = 8.dp, y = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color.Red, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${discountPct.toString().toPersianNumbers()}% تخفیف", color = Color.White, fontFamily = VazirFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${course.durationMinutes / 60} ساعت".toPersianNumbers(),
                        fontFamily = VazirFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = course.instructors.firstOrNull()?.displayName ?: course.organizerName ?: "ناشناس",
                        fontFamily = VazirFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AvatarImage(
                        imageUrl = course.instructors.firstOrNull()?.displayAvatarUrl ?: course.organizerAvatarUrl,
                        name = course.instructors.firstOrNull()?.displayName ?: course.organizerName ?: "M",
                        size = AvatarSize.SMALL,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                if (!course.fieldOfStudy.isNullOrEmpty() || course.tags.isNotEmpty()) {
                    val fields = mutableListOf<String>()
                    if (!course.fieldOfStudy.isNullOrEmpty()) fields.add(course.fieldOfStudy.replace("مخصوص", "").replace("(", "").replace(")", "").trim())
                    fields.addAll(course.tags.map { it.replace("مخصوص", "").replace("(", "").replace(")", "").trim() })
                    val displayFields = fields.filter { it.isNotEmpty() }.distinct().take(3)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        displayFields.forEach { fieldText ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.6f), RoundedCornerShape(4.dp))
                                    .padding(vertical = 2.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fieldText,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.People, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(course.enrolledCount.toString().toPersianNumbers(), fontFamily = VazirFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        val now = java.time.Instant.now()
                        val startsAt = course.startsAt
                        val daysToStart = if (startsAt != null) java.time.Duration.between(now, startsAt).toDays() else null
                        if (daysToStart != null && daysToStart > 0) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("$daysToStart روز تا شروع".toPersianNumbers(), fontFamily = VazirFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else if (daysToStart == 0L) {
                            Text("امروز", fontFamily = VazirFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        } else if (startsAt != null) {
                            Text("در حال برگزاری", fontFamily = VazirFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    val price = course.priceRials / 10
                    val formattedPrice = if (price > 0) price.toPersianPrice() else "۰"
                    val discountPct = course.discountPercentage ?: 0
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (course.isFree || price == 0L) {
                            Text(
                                text = "رایگان",
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = VazirFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                        } else if (discountPct > 0) {
                            val discountedPrice = price - (price * discountPct / 100)
                            val formattedDiscounted = discountedPrice.toPersianPrice()
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formattedPrice,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 10.sp,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formattedDiscounted,
                                        color = MaterialTheme.colorScheme.error,
                                        fontFamily = VazirFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "تومان",
                                        color = MaterialTheme.colorScheme.error,
                                        fontFamily = VazirFontFamily,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = formattedPrice,
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = VazirFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "تومان",
                                color = MaterialTheme.colorScheme.error,
                                fontFamily = VazirFontFamily,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (institution.rating > 0) String.format("%.1f", institution.rating) else "جدید",
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = institution.name,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${institution.courseCount} دوره",
                fontFamily = VazirFontFamily,
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
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AvatarImage(
            imageUrl = instructor.avatarUrl,
            name = instructor.displayName,
            size = AvatarSize.LARGE
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = instructor.displayName,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun MyCourseItem(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
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
                    model = course.posterUrl ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
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
                        fontFamily = VazirFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Discount or Price badge
                val price = course.priceRials / 10
                val formattedPrice = if (price > 0) String.format(java.util.Locale("en", "US"), "%,d", price) else "0"
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
                                fontFamily = VazirFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (discountPct > 0) {
                            val discountedPrice = price - (price * discountPct / 100)
                            val formattedDiscounted = String.format(java.util.Locale("en", "US"), "%,d", discountedPrice)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "%$discountPct",
                                        color = Color.White,
                                        fontFamily = VazirFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formattedPrice,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = VazirFontFamily,
                                        fontSize = 9.sp,
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    )
                                    Text(
                                        text = "$formattedDiscounted تومان",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontFamily = VazirFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "$formattedPrice تومان",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontFamily = VazirFontFamily,
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
                                text = "ظرفیت: ${course.capacity}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = VazirFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = course.title,
                    fontFamily = VazirFontFamily,
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
                        text = displayName,
                        fontFamily = VazirFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            text = course.startsAt?.toString()?.take(10) ?: "تاریخ نامشخص",
                            fontFamily = VazirFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = course.syllabusDuration ?: "نامشخص",
                            fontFamily = VazirFontFamily,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = course.posterUrl ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
            contentDescription = course.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = course.title,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.instructors.firstOrNull()?.displayName ?: course.organizerName ?: "ناشناس",
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = if (course.isFree) "رایگان" else "${course.priceRials / 10} تومان",
                    fontFamily = VazirFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "در حال حاضر دوره‌ای برای نمایش وجود ندارد.",
            fontFamily = VazirFontFamily,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
