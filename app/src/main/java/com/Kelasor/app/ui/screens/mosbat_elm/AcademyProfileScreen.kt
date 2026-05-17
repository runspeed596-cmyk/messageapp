package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
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
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.ExtendedColors
import com.Kelasor.app.ui.theme.DanaFontFamily
import kotlinx.coroutines.flow.collectLatest
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyProfileScreen(
    institutionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit = {},
    onNavigateToEditAcademyProfile: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToEditCourse: (String) -> Unit = {},
    onNavigateToChannel: (String) -> Unit = {},
    viewModel: AcademyPublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(institutionId) { viewModel.loadAcademyProfile(institutionId) }
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AcademyPublicProfileEvent.NavigateToChat -> onNavigateToChat(event.chatId)
                is AcademyPublicProfileEvent.NavigateToUserProfile -> onNavigateToUserProfile(event.userId)
                is AcademyPublicProfileEvent.NavigateToEditProfile -> onNavigateToEditAcademyProfile()
                is AcademyPublicProfileEvent.NavigateToChannel -> onNavigateToChannel(event.channelId)
                is AcademyPublicProfileEvent.ShowToast -> {
                    com.Kelasor.app.ui.components.KelasorToast.show(
                        context = context,
                        message = event.message,
                        type = event.type
                    )
                }
            }
        }
    }
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(state.institution?.name ?: "پروفایل آکادمی", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت") } },
                    actions = {
                        if (state.isOwner) {
                            IconButton(onClick = onNavigateToEditAcademyProfile) { Icon(Icons.Default.Edit, "ویرایش", tint = extendedColors.accent) }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = extendedColors.accent)
            }
        } else if (state.error != null && state.institution == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error, fontFamily = DanaFontFamily)
            }
        } else {
            val institution = state.institution ?: return@Scaffold
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var showAdminsChatSheet by remember { mutableStateOf(false) }
            val tabs = listOf("دوره‌ها", "نشان ها", "مدرسین", "ادمین‌ها")
            var searchQuery by remember { mutableStateOf("") }
            var selectedFilter by remember { mutableStateOf("همه") }
            val filterList = listOf("همه", "دوره‌های پیش‌رو", "به اتمام رسیده", "پرطرفدار", "تخفیف‌دار", "همکاری شده")

            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
                // Profile Header
                item { ProfileHeader(institution = institution, extendedColors = extendedColors) }
                // Institution Type Badge

                // Stats
                item { StatsRow(institution = institution, calculatedRating = state.calculatedRating, courses = state.courses) }
                if (state.isOwner) {
                    item { ExtendedStatsRow(institution = institution) }
                }
                
                // Actions Row (Follow / Channel / Message)
                item {
                    val isFollowing = state.isFollowing
                    val isFollowLoading = state.isFollowLoading

                    if (!state.isOwner && !state.isSelf) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Follow Button
                            Button(
                                onClick = { viewModel.toggleFollow() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else extendedColors.accent,
                                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White
                                ),
                                enabled = !isFollowLoading
                            ) {
                                Text(
                                    text = if (isFollowing) "دنبال شده" else "دنبال کردن",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Channel Button
                            if (!institution.channelId.isNullOrEmpty()) {
                                Button(
                                    onClick = { viewModel.navigateToOfficialChannel() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "کانال رسمی",
                                        fontFamily = DanaFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Message Button
                            IconButton(
                                onClick = {
                                    if (state.admins.isNotEmpty()) {
                                        showAdminsChatSheet = true
                                    } else {
                                        onNavigateToChat(institution.ownerId)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "پیام",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // If owner/self, we still show the Official Channel button at the top/center of profile content
                        if (!institution.channelId.isNullOrEmpty()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { viewModel.navigateToOfficialChannel() },
                                    modifier = Modifier
                                        .height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "کانال رسمی",
                                        fontFamily = DanaFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                // Description & Affiliations
                item {
                    if (!institution.description.isNullOrEmpty() || institution.associatedClubIds.isNotEmpty() || institution.associatedFieldOfStudyIds.isNotEmpty() || institution.associatedStudentOrgIds.isNotEmpty()) {
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                if (!institution.description.isNullOrEmpty()) {
                                    Text("درباره آکادمی", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Spacer(Modifier.height(8.dp))
                                    Text(institution.description, style = MaterialTheme.typography.bodyMedium, fontFamily = DanaFontFamily)
                                    Spacer(Modifier.height(16.dp))
                                }
                                
                                // Affiliations
                                if (institution.associatedFieldOfStudyIds.isNotEmpty()) {
                                    Text("رشته‌های مرتبط", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Text(institution.associatedFieldOfStudyIds.joinToString(" • "), style = MaterialTheme.typography.bodySmall, fontFamily = DanaFontFamily)
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (institution.associatedClubIds.isNotEmpty()) {
                                    Text("کانون‌های مرتبط", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Text(institution.associatedClubIds.joinToString(" • "), style = MaterialTheme.typography.bodySmall, fontFamily = DanaFontFamily)
                                    Spacer(Modifier.height(8.dp))
                                }
                                if (institution.associatedStudentOrgIds.isNotEmpty()) {
                                    Text("تشکل‌های مرتبط", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Text(institution.associatedStudentOrgIds.joinToString(" • "), style = MaterialTheme.typography.bodySmall, fontFamily = DanaFontFamily)
                                }
                            }
                        }
                    }
                }

                // TabBar
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).clip(RoundedCornerShape(16.dp)),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                Box(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(extendedColors.accent)
                                )
                            }
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        fontFamily = DanaFontFamily,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == index) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> { // Courses
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("جستجوی دوره...", fontFamily = DanaFontFamily, fontSize = 14.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "جستجو") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    items(filterList) { filter ->
                                        val isSelected = selectedFilter == filter
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(if (isSelected) extendedColors.accent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                                                .clickable { selectedFilter = filter }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = filter,
                                                fontFamily = DanaFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        val filteredCourses = state.courses.filter { course ->
                            val matchSearch = course.title.contains(searchQuery, ignoreCase = true) || course.slogan?.contains(searchQuery, ignoreCase = true) == true
                            val matchFilter = when (selectedFilter) {
                                "دوره‌های پیش‌رو" -> course.startsAt?.isAfter(java.time.Instant.now()) == true
                                "به اتمام رسیده" -> course.endsAt?.isBefore(java.time.Instant.now()) == true
                                "پرطرفدار" -> course.rating >= 4.5 || course.favoritesCount >= 10
                                "تخفیف‌دار" -> (course.discountPercentage ?: 0) > 0
                                "همکاری شده" -> !course.scientificAssociationName.isNullOrEmpty()
                                else -> true // "همه"
                            }
                            matchSearch && matchFilter
                        }
                        
                        if (filteredCourses.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("دوره‌ای یافت نشد", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(filteredCourses, key = { it.id }) { course ->
                                AcademyCourseCard(
                                    course = course,
                                    isOwner = state.isOwner,
                                    onClick = { onNavigateToCourseDetail(course.id) },
                                    onEditClick = { onNavigateToEditCourse(course.id) },
                                    onDeleteClick = { viewModel.deleteCourse(course.id) }
                                )
                            }
                        }
                    }
                    1 -> { // Honors
                        item {
                            if (institution.honors.isEmpty()) {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("بجی ثبت نشده است", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                            } else {
                                institution.honors.forEach { honor ->
                                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(12.dp)) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(honor.title, fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
                                            if (!honor.description.isNullOrEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(honor.description, fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Teachers
                        item {
                            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("مدرسین", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Spacer(Modifier.height(8.dp))
                                    if (state.instructors.isEmpty() && state.manualInstructors.isEmpty()) {
                                        Text("مدرسی ثبت نشده است", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                                    } else {
                                        state.instructors.forEach { instructor ->
                                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.startChatWithUser(instructor.id) }, verticalAlignment = Alignment.CenterVertically) {
                                                AvatarImage(imageUrl = instructor.displayAvatarUrl, name = instructor.displayName, size = AvatarSize.SMALL)
                                                Spacer(Modifier.width(10.dp))
                                                Text(instructor.displayName, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                                IconButton(onClick = { viewModel.startChatWithUser(instructor.id) }) {
                                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "پیام دادن", tint = extendedColors.accent)
                                                }
                                            }
                                        }
                                        state.manualInstructors.forEach { manualInstructor ->
                                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                AvatarImage(imageUrl = manualInstructor.avatarUrl, name = manualInstructor.name, size = AvatarSize.SMALL)
                                                Spacer(Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(manualInstructor.name, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium)
                                                        Spacer(Modifier.width(6.dp))
                                                        Text("(دستی)", fontFamily = DanaFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                    if (!manualInstructor.resume.isNullOrEmpty()) {
                                                        Text(manualInstructor.resume, fontFamily = DanaFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // Admins
                        item {
                            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("ادمین‌ها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                    Spacer(Modifier.height(8.dp))
                                    if (state.admins.isEmpty()) {
                                        Text("ادمینی ثبت نشده است", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                                    } else {
                                        state.admins.forEach { admin ->
                                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { viewModel.startChatWithUser(admin.id) }, verticalAlignment = Alignment.CenterVertically) {
                                                AvatarImage(imageUrl = admin.displayAvatarUrl, name = admin.displayName, size = AvatarSize.SMALL)
                                                Spacer(Modifier.width(10.dp))
                                                Text(admin.displayName, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                                IconButton(onClick = { viewModel.startChatWithUser(admin.id) }) {
                                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "پیام دادن", tint = extendedColors.accent)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showAdminsChatSheet && state.admins.isNotEmpty()) {
                ModalBottomSheet(
                    onDismissRequest = { showAdminsChatSheet = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "گفتگو با ادمین‌های آکادمی",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.admins) { admin ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                        .clickable {
                                            showAdminsChatSheet = false
                                            viewModel.startChatWithUser(admin.id)
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AvatarImage(
                                        imageUrl = admin.displayAvatarUrl,
                                        name = admin.displayName,
                                        size = AvatarSize.MEDIUM
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = admin.displayName,
                                            fontFamily = DanaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "ادمین آکادمی",
                                            fontFamily = DanaFontFamily,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "چت",
                                        tint = extendedColors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

private fun calculateRegisterDuration(createdAt: java.time.Instant?): String {
    if (createdAt == null) return "عضو جدید"
    val days = java.time.Duration.between(createdAt, java.time.Instant.now()).toDays()
    return when {
        days <= 0 -> "امروز به کلاسور پیوست"
        days < 30 -> "$days روز حضور در کلاسور"
        days < 365 -> "${days / 30} ماه حضور در کلاسور"
        else -> "${days / 365} سال حضور در کلاسور"
    }.toPersianNumbers()
}

@Composable
private fun ProfileHeader(institution: Institution, extendedColors: ExtendedColors) {
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val fullLogoUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(institution.logoUrl)
        if (!fullLogoUrl.isNullOrEmpty()) {
            AsyncImage(
                model = fullLogoUrl, contentDescription = institution.name,
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(Modifier.size(100.dp).clip(CircleShape).background(Brush.linearGradient(listOf(extendedColors.accent, extendedColors.accent.copy(alpha = 0.5f)))),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.School, null, Modifier.size(48.dp), tint = Color.White)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(institution.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, textAlign = TextAlign.Center)
        val typePersian = when(institution.type?.uppercase()) {
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
        if (typePersian.isNotBlank()) {
            Text(typePersian, style = MaterialTheme.typography.labelMedium, fontFamily = DanaFontFamily, color = extendedColors.accent, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
        }
        if (institution.universities.isNotEmpty()) {
            Text(institution.universities.joinToString(" • "), style = MaterialTheme.typography.bodySmall, fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
        }
        val registerDuration = calculateRegisterDuration(institution.createdAt)
        Text(
            text = registerDuration,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = DanaFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatsRow(institution: Institution, calculatedRating: Double, courses: List<Course> = emptyList()) {
    // Use proper rounding instead of integer division (90min / 60 = 1 bug → 2 with rounding)
    val totalDurationMinutes: Int = courses.sumOf { it.durationMinutes }
    val totalDurationHours: Int = (totalDurationMinutes + 30) / 60 // Round to nearest hour
    val totalStudents: Int = courses.sumOf { it.enrolledCount }
    // Person-hours: only calculate when we have actual duration data, otherwise 0
    val totalPersonHours: Int = if (totalDurationHours > 0) totalStudents * totalDurationHours else 0
    val totalTeachers = courses.flatMap { it.instructors }.distinctBy { it.id }.size + courses.flatMap { it.manualInstructors }.distinctBy { it.name }.size
    val totalCollaborations = courses.count { it.collaborators.isNotEmpty() }
    // Prefer backend-provided values over client-calculated ones
    val courseCount = if (institution.courseCount > 0) institution.courseCount else courses.size
    val studentCount = if (institution.studentCount > 0) institution.studentCount else totalStudents
    val teachersCount = if (institution.totalTeachersCount > 0) institution.totalTeachersCount else totalTeachers
    val durationHours = if (institution.totalTrainingHours > 0) institution.totalTrainingHours else totalDurationHours
    val personHours = if (institution.totalPersonHours > 0) institution.totalPersonHours else totalPersonHours
    val collaborationsCount = if (institution.totalCollaborations > 0) institution.totalCollaborations else totalCollaborations

    val finalRating = if (calculatedRating > 0.0) calculatedRating else institution.averageRating
    val ratingText = if (finalRating > 0.0) {
        String.format(java.util.Locale.US, "%.1f", finalRating).toPersianNumbers()
    } else {
        "جدید"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "دوره‌ها", value = courseCount.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "دانشجو", value = studentCount.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "دنبال‌کننده", value = institution.followerCount.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "رتبه مجموعه", value = ratingText, modifier = Modifier.weight(1f))
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = "تعداد اساتید", value = teachersCount.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "ساعت آموزش", value = durationHours.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "نفر-ساعت", value = personHours.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
                StatItem(label = "همکاری‌ها", value = collaborationsCount.toString().toPersianNumbers(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = MessageAppTheme.extendedColors.accent, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ExtendedStatsRow(institution: Institution) {
    val extendedColors = MessageAppTheme.extendedColors
    val totalRevenue = institution.totalRevenue ?: 0L
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("گزارش مالی آکادمی (فقط شما می‌بینید)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("درآمد کل دوره‌ها:", style = MaterialTheme.typography.labelMedium, fontFamily = DanaFontFamily)
                Spacer(Modifier.width(8.dp))
                if (totalRevenue > 0) {
                    Text((totalRevenue / 10).toPersianPrice() + " تومان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = Color(0xFF2E7D32))
                } else {
                    Text("بدون درآمد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AcademyCourseCard(course: Course, isOwner: Boolean, onClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    val discountPct = course.discountPercentage ?: 0
    val priceRials = course.priceRials
    val priceToman = priceRials / 10
    val hasDiscount = !course.isFree && discountPct > 0 && priceToman > 0
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // Right Side: Blurred & Stretched Backdrop + Fit Portrait Poster
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp))
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
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 160f - 60f
                            )
                        )
                )
                
                // Popular Badge (Top End)
                if (course.enrolledCount > 30) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFE67E22), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, Modifier.size(8.dp), tint = Color.White)
                            Spacer(Modifier.width(2.dp))
                            Text("محبوب", color = Color.White, fontFamily = DanaFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Capacity Badge (Top Start)
                if (course.capacity != null && course.capacity > 0) {
                    val isFull = course.enrolledCount >= course.capacity
                    val capacityText = if (isFull) "تکمیل" else "ظرفیت: ${course.capacity.toString().toPersianNumbers()}"
                    val bgColor = if (isFull) MaterialTheme.colorScheme.error else Color(0xFFFF66B2)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopStart)
                            .background(bgColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
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
                
                // Status Badge (Bottom Center)
                val (statusText, statusColor) = when (course.status) {
                    "APPROVED" -> "تأیید شده" to Color(0xFF4CAF50)
                    "PENDING" -> "در انتظار تأیید" to Color(0xFFFF9800)
                    "REJECTED" -> "رد شده" to Color(0xFFF44336)
                    else -> "نامشخص" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.BottomCenter)
                        .background(color = statusColor.copy(alpha = 0.9f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = statusText, color = Color.White, fontFamily = DanaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Left Side: Content Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Title
                    Text(
                        text = course.title.toPersianNumbers(), 
                        fontFamily = DanaFontFamily, 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 15.sp, 
                        maxLines = 2, 
                        overflow = TextOverflow.Ellipsis, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Instructor (Individual)
                    val instructor = course.instructors.firstOrNull()
                    val displayName = instructor?.displayName 
                        ?: course.manualInstructors.firstOrNull()?.name 
                        ?: course.organizerName 
                        ?: "ناشناس"
                    val displayAvatar = instructor?.displayAvatarUrl 
                        ?: course.manualInstructors.firstOrNull()?.avatarUrl 
                        ?: course.organizerAvatarUrl
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(imageUrl = displayAvatar, name = displayName, size = AvatarSize.EXTRA_SMALL)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مدرس: ${displayName.toPersianNumbers()}", 
                            fontFamily = DanaFontFamily, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            maxLines = 1, 
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Organizer (Academy/Institution)
                    if (!course.organizerName.isNullOrEmpty() && course.organizerName != displayName) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarImage(imageUrl = course.organizerAvatarUrl, name = course.organizerName, size = AvatarSize.EXTRA_SMALL)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "برگزارکننده: ${course.organizerName.toPersianNumbers()}", 
                                fontFamily = DanaFontFamily, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary, 
                                maxLines = 1, 
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Time / Date info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Event, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            val now = java.time.Instant.now()
                            val daysUntil = java.time.Duration.between(now, course.startsAt).toDays()
                            val dateText = when {
                                daysUntil > 0 -> "$daysUntil روز تا برگزاری"
                                daysUntil == 0L -> "امروز برگزاری"
                                else -> "برگزار شده"
                            }
                            Text(text = dateText.toPersianNumbers(), fontFamily = DanaFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (course.durationMinutes > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${course.durationMinutes / 60} ساعت".toPersianNumbers(), fontFamily = DanaFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    
                    // Metadata tags Row
                    val fieldsToDisplay = mutableListOf<String>()
                    if (course.suitableFor.isNotEmpty()) {
                        fieldsToDisplay.addAll(course.suitableFor)
                    } else {
                        if (!course.fieldOfStudy.isNullOrEmpty()) fieldsToDisplay.add(course.fieldOfStudy)
                        fieldsToDisplay.addAll(course.tags)
                    }
                    val displayFields = fieldsToDisplay.map { it.replace("مخصوص", "").replace("(", "").replace(")", "").trim() }.filter { it.isNotEmpty() }.distinct().take(2)
                    
                    if (displayFields.isNotEmpty()) {
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            displayFields.forEach { fieldText ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFFD54F), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = fieldText,
                                        color = Color.Black,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Price Section (Aligned to the start/right side under RTL layout direction)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                        if (course.isFree || priceRials == 0L) {
                            Text(text = "رایگان", color = Color(0xFF4CAF50), fontFamily = DanaFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        } else if (hasDiscount) {
                            val discountedPriceToman = priceToman - (priceToman * discountPct / 100)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.error)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${discountPct.toString().toPersianNumbers()}%",
                                        color = Color.White,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = discountedPriceToman.toPersianPrice(),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = DanaFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "تومان",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontFamily = DanaFontFamily,
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = priceToman.toPersianPrice(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontFamily = DanaFontFamily,
                                    fontSize = 12.sp,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = priceToman.toPersianPrice(), color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "تومان", color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // Owner actions (Below the row)
        if (isOwner) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ویرایش", fontFamily = DanaFontFamily, fontSize = 12.sp)
                }
                TextButton(onClick = onDeleteClick, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف", fontFamily = DanaFontFamily, fontSize = 12.sp)
                }
            }
        }
    }
}
