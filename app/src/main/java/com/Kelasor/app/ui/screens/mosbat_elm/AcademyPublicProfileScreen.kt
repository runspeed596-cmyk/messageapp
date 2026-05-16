package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.domain.model.Institution
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice
import com.Kelasor.app.ui.theme.DanaFontFamily
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyPublicProfileScreen(
    institutionId: String,
    onBack: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToEditProfile: (String) -> Unit = {},
    onNavigateToChannel: (String) -> Unit = {},
    viewModel: AcademyPublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AcademyPublicProfileEvent.NavigateToChat -> onNavigateToChat(event.chatId)
                is AcademyPublicProfileEvent.NavigateToUserProfile -> onNavigateToUserProfile(event.userId)
                is AcademyPublicProfileEvent.NavigateToEditProfile -> onNavigateToEditProfile(event.institutionId)
                is AcademyPublicProfileEvent.NavigateToChannel -> onNavigateToChannel(event.channelId)
            }
        }
    }

    if (state.isLoading && state.institution == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val institution = state.institution ?: return

    val tabs = listOf("دوره‌ها", "افتخارات", "اساتید", "ادمین‌ها")
    if (state.isOwner && state.pendingCollaborations.isNotEmpty()) {
        // Option to add collaborations as a special tab or badge
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = institution.name,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var showMoreMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("اشتراک‌گذاری", fontFamily = DanaFontFamily) },
                                onClick = { showMoreMenu = false },
                                leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("گزارش", fontFamily = DanaFontFamily) },
                                onClick = { showMoreMenu = false },
                                leadingIcon = { Icon(Icons.Rounded.Flag, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Instagram-Style Profile Header
            ProfileHeader(
                institution = institution,
                isFollowing = state.isFollowing,
                isOwner = state.isOwner,
                calculatedScore = state.calculatedScore,
                calculatedRating = state.calculatedRating,
                onFollowClick = { viewModel.toggleFollow() },
                onMessageClick = { viewModel.startChatWithUser(institution.ownerId) },
                onEditProfileClick = { viewModel.navigateToEditProfile() },
                onChannelClick = { viewModel.navigateToOfficialChannel() }
            )

            // Description and Links
            ProfileBio(institution = institution)

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontFamily = DanaFontFamily,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> CoursesTabContent(courses = state.courses, onCourseClick = onNavigateToCourseDetail)
                    1 -> HonorsTabContent(honors = state.honors)
                    2 -> InstructorsTabContent(
                        instructors = state.instructors,
                        manualInstructors = state.manualInstructors,
                        onInstructorClick = { user -> viewModel.navigateToUserProfile(user.id) },
                        onFollowClick = { user -> viewModel.followUser(user.id) }
                    )
                    3 -> AdminsTabContent(
                        admins = state.admins,
                        onAdminClick = { user -> viewModel.navigateToUserProfile(user.id) },
                        onMessageClick = { user -> viewModel.startChatWithUser(user.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    institution: Institution,
    isFollowing: Boolean,
    isOwner: Boolean,
    calculatedScore: Double,
    calculatedRating: Double,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEditProfileClick: () -> Unit = {},
    onChannelClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (institution.verificationStatus == "VERIFIED") Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = com.Kelasor.app.util.UrlUtils.getFullUrl(institution.logoUrl) ?: "https://ui-avatars.com/api/?name=${institution.name}&background=random",
                    contentDescription = institution.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                // Popular Badge Overlay
                if (institution.averageRating >= 4.5) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .background(Color(0xFFFFD700), CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = "محبوب",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            // Stats
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(count = institution.courseCount.toString().toPersianNumbers(), label = "دوره‌ها")
                ProfileStat(count = institution.studentCount.toString().toPersianNumbers(), label = "دانشجو")
                ProfileStat(count = institution.followerCount.toString().toPersianNumbers(), label = "دنبال‌کننده")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Extended Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MiniStat(icon = Icons.Rounded.Star, value = String.format("%.1f", calculatedRating).toPersianNumbers(), label = "رتبه")
            MiniStat(icon = Icons.Rounded.People, value = institution.instructorIds.size.toString().toPersianNumbers(), label = "اساتید")
            MiniStat(icon = Icons.Rounded.EmojiEvents, value = String.format("%.1f", calculatedScore).toPersianNumbers(), label = "سطح")
            MiniStat(icon = Icons.Rounded.Reviews, value = institution.reviewCount.toString().toPersianNumbers(), label = "نظرات")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOwner) {
                Button(
                    onClick = { onEditProfileClick() },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text("ویرایش پروفایل", fontFamily = DanaFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                Button(
                    onClick = onFollowClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isFollowing) "دنبال شده" else "دنبال کردن",
                        fontFamily = DanaFontFamily,
                        fontSize = 13.sp,
                        color = if (isFollowing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                    )
                }

                if (!institution.channelId.isNullOrEmpty()) {
                    Button(
                        onClick = onChannelClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Campaign,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "کانال رسمی",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                IconButton(
                    onClick = onMessageClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = "پیام",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontFamily = DanaFontFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MiniStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            fontFamily = DanaFontFamily,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileBio(institution: Institution) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = institution.name,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (institution.verificationStatus == "VERIFIED") {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Verified,
                    contentDescription = "Verified",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        
        val typePersian = when(institution.type) {
            "CLUB" -> "کانون"
            "SCIENTIFIC_ASSOCIATION" -> "انجمن علمی"
            "INSTITUTE" -> "موسسه"
            "STUDENT_ORG" -> "تشکل دانشجویی"
            "RESEARCH_CENTER" -> "مرکز تحقیقاتی"
            "ASSOCIATION" -> "انجمن"
            "ACADEMY" -> "آکادمی"
            "COMMUNITY" -> "اجتماع"
            else -> institution.type
        }
        
        Text(
            text = typePersian,
            fontFamily = DanaFontFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        if (!institution.description.isNullOrEmpty()) {
            Text(
                text = institution.description,
                fontFamily = DanaFontFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CoursesTabContent(courses: List<Course>, onCourseClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val filterOptions = listOf(
        "همه" to "ALL",
        "فعال" to "APPROVED",
        "در انتظار بررسی" to "PENDING",
        "رد شده" to "REJECTED"
    )
    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }

    val filteredCourses = courses.filter { course ->
        val matchesSearch = course.title.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter.second) {
            "ALL" -> true
            "APPROVED" -> course.status == "APPROVED"
            "PENDING" -> course.status == "PENDING"
            "REJECTED" -> course.status == "REJECTED"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search and Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("جستجوی دوره...", fontFamily = DanaFontFamily, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = DanaFontFamily, fontSize = 14.sp)
            )
            
            // Filter Dropdown
            Box {
                Button(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Rounded.FilterList, contentDescription = "فیلتر", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedFilter.first, fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.first, fontFamily = DanaFontFamily, fontSize = 14.sp) },
                            onClick = {
                                selectedFilter = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (filteredCourses.isEmpty()) {
            EmptyTabState(icon = Icons.Rounded.School, message = "دوره‌ای یافت نشد")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredCourses) { course ->
                    AcademyProfileCourseItem(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
    }
}

@Composable
fun AcademyProfileCourseItem(
    course: Course,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
                contentDescription = course.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.title,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = course.organizerName ?: "ناشناس",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    val price = course.priceRials / 10
                    val discountPct = course.discountPercentage ?: 0
                    val discountedPrice = if (discountPct > 0) price - (price * discountPct / 100) else price
                    
                    Text(
                        text = if (course.isFree) "رایگان" else "${discountedPrice.toPersianPrice()} تومان",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Status Badge
                val (statusText, statusColor) = when (course.status) {
                    "APPROVED" -> "فعال" to Color(0xFF4CAF50)
                    "PENDING" -> "در انتظار بررسی" to Color(0xFFFF9800)
                    "REJECTED" -> "رد شده" to Color(0xFFF44336)
                    else -> "نامشخص" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontFamily = DanaFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HonorsTabContent(honors: List<com.Kelasor.app.domain.model.InstitutionHonor>) {
    if (honors.isEmpty()) {
        EmptyTabState(icon = Icons.Rounded.EmojiEvents, message = "افتخاری ثبت نشده است")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(honors) { honor ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (honor.imageUrl != null) {
                        AsyncImage(
                            model = com.Kelasor.app.util.UrlUtils.getFullUrl(honor.imageUrl),
                            contentDescription = honor.title,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = honor.title,
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (honor.description != null) {
                            Text(
                                text = honor.description,
                                fontFamily = DanaFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (honor.date != null) {
                        Text(
                            text = honor.date,
                            fontFamily = DanaFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminsTabContent(admins: List<com.Kelasor.app.domain.model.User>, onAdminClick: (com.Kelasor.app.domain.model.User) -> Unit, onMessageClick: (com.Kelasor.app.domain.model.User) -> Unit = {}) {
    if (admins.isEmpty()) {
        EmptyTabState(icon = Icons.Rounded.AdminPanelSettings, message = "ادمین یافت نشد")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(admins) { admin ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdminClick(admin) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(imageUrl = admin.avatarUrl, name = admin.displayName, size = AvatarSize.LARGE)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = admin.displayName,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ادمین آکادمی",
                        fontFamily = DanaFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { onMessageClick(admin) }) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun InstructorsTabContent(
    instructors: List<User>,
    manualInstructors: List<com.Kelasor.app.domain.model.ManualInstructor> = emptyList(),
    onInstructorClick: (User) -> Unit,
    onFollowClick: (User) -> Unit = {}
) {
    if (instructors.isEmpty() && manualInstructors.isEmpty()) {
        EmptyTabState(icon = Icons.Rounded.PeopleOutline, message = "مدرسی یافت نشد")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(instructors) { instructor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInstructorClick(instructor) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(imageUrl = instructor.avatarUrl, name = instructor.displayName, size = AvatarSize.LARGE)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = instructor.displayName,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مدرس",
                        fontFamily = DanaFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Button(
                    onClick = { onFollowClick(instructor) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("دنبال کردن", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        // Manual (guest) instructors
        items(manualInstructors) { manual ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(manual.avatarUrl)
                if (fullUrl != null) {
                    AsyncImage(
                        model = fullUrl,
                        contentDescription = manual.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                } else {
                    AvatarImage(imageUrl = null, name = manual.name, size = AvatarSize.LARGE)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = manual.name,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مدرس مهمان",
                        fontFamily = DanaFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!manual.resume.isNullOrEmpty()) {
                        Text(
                            text = manual.resume,
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AboutTabContent(institution: Institution) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AboutSectionCard(title = "اطلاعات تماس") {
                if (!institution.province.isNullOrEmpty()) {
                    AboutInfoRow(icon = Icons.Rounded.LocationCity, title = "استان", value = institution.province)
                }
                if (!institution.city.isNullOrEmpty()) {
                    AboutInfoRow(icon = Icons.Rounded.LocationOn, title = "شهر", value = institution.city)
                }
            }
        }
        
        if (institution.specialties.isNotEmpty() || institution.faculties.isNotEmpty()) {
            item {
                AboutSectionCard(title = "حوزه‌های فعالیت") {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (institution.specialties + institution.faculties).forEach { item ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item,
                                    fontFamily = DanaFontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        if (!institution.achievements.isNullOrEmpty()) {
            item {
                AboutSectionCard(title = "دستاوردها") {
                    Text(
                        text = institution.achievements,
                        fontFamily = DanaFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun AboutSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun AboutInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = DanaFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyTabState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontFamily = DanaFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CollaborationsTabContent(
    requests: List<com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyTabState(icon = Icons.Rounded.Handshake, message = "درخواست همکاری یافت نشد")
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(requests) { request ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "درخواست همکاری در دوره",
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.courseTitle,
                        fontFamily = DanaFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ارسال کننده: ${request.senderInstitutionName}",
                        fontFamily = DanaFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!request.message.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "پیام: ${request.message}",
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAccept(request.id) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("قبول", fontFamily = DanaFontFamily, fontSize = 13.sp, color = Color.White)
                        }
                        Button(
                            onClick = { onReject(request.id) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("رد کردن", fontFamily = DanaFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }
    }
}
