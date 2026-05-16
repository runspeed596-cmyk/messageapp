package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.animation.animateColorAsState

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    LaunchedEffect(institutionId) { viewModel.loadAcademyProfile(institutionId) }
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AcademyPublicProfileEvent.NavigateToChat -> onNavigateToChat(event.chatId)
                is AcademyPublicProfileEvent.NavigateToUserProfile -> onNavigateToUserProfile(event.userId)
                is AcademyPublicProfileEvent.NavigateToEditProfile -> onNavigateToEditAcademyProfile()
                is AcademyPublicProfileEvent.NavigateToChannel -> onNavigateToChannel(event.channelId)
            }
        }
    }
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
            val tabs = listOf("دوره‌ها", "نشان ها", "مدرسین", "ادمین‌ها")
            var searchQuery by remember { mutableStateOf("") }
            var selectedFilter by remember { mutableStateOf("همه") }
            val filterList = listOf("همه", "دوره‌های پیش‌رو", "به اتمام رسیده", "پرطرفدار", "تخفیف‌دار", "همکاری شده")

            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 80.dp)) {
                // Profile Header
                item { ProfileHeader(institution = institution, extendedColors = extendedColors) }
                // Institution Type Badge

                // Stats
                item { StatsRow(institution = institution, calculatedRating = state.calculatedRating) }
                if (state.isOwner) {
                    item { ExtendedStatsRow(institution = institution) }
                }
                // Actions - Only follow, no message
                item {
                    val isFollowing = state.isFollowing
                    val isFollowLoading = state.isFollowLoading
                    val followBackgroundColor by animateColorAsState(
                        targetValue = if (isFollowing) Color.Transparent else extendedColors.accent,
                        label = "followBg"
                    )
                    val followContentColor by animateColorAsState(
                        targetValue = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White,
                        label = "followContent"
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!state.isOwner && !state.isSelf) {
                            Button(
                                onClick = { viewModel.toggleFollow() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isFollowing) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = followBackgroundColor,
                                    contentColor = followContentColor,
                                    disabledContainerColor = followBackgroundColor.copy(alpha = 0.5f)
                                ),
                                enabled = !isFollowLoading
                            ) {
                                AnimatedContent(
                                    targetState = isFollowing to isFollowLoading,
                                    transitionSpec = {
                                        fadeIn() togetherWith fadeOut()
                                    },
                                    label = "followContentAnim"
                                ) { (following, loading) ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (loading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = followContentColor,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (following) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (following) "دنبال شده" else "دنبال کردن",
                                                fontFamily = DanaFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
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
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("ادمین‌ها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = extendedColors.accent)
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("میانگین پاسخگویی: زیر ۱۰ دقیقه", fontFamily = DanaFontFamily, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            Text("شاخص فعالیت: بالا", fontFamily = DanaFontFamily, fontSize = 10.sp, color = Color(0xFF2E7D32))
                                        }
                                    }
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
        }
    }
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
        }
    }
}

@Composable
private fun StatsRow(institution: Institution, calculatedRating: Double) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        StatItem("دوره‌ها", institution.courseCount.toString().toPersianNumbers())
        StatItem("دانشجو", institution.studentCount.toString().toPersianNumbers())
        StatItem("دنبال‌کننده", institution.followerCount.toString().toPersianNumbers())
        val finalRating = if (calculatedRating > 0.0) calculatedRating else institution.averageRating
        if (finalRating > 0.0) {
            StatItem("رتبه", String.format(java.util.Locale.US, "%.1f", finalRating).toPersianNumbers())
        } else {
            StatItem("رتبه", "جدید")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = MessageAppTheme.extendedColors.accent)
        Text(label, style = MaterialTheme.typography.labelSmall, fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ExtendedStatsRow(institution: Institution) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("آمار اختصاصی آکادمی (فقط شما می‌بینید)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = MessageAppTheme.extendedColors.accent)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("نفر-ساعت", institution.totalPersonHours.toString().toPersianNumbers())
                StatItem("ساعت آموزش", institution.totalTrainingHours.toString().toPersianNumbers())
                StatItem("همکاری‌ها", institution.totalCollaborations.toString().toPersianNumbers())
                StatItem("تعداد اساتید", institution.totalTeachersCount.toString().toPersianNumbers())
            }
            if (institution.totalRevenue != null) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("درآمد کل:", style = MaterialTheme.typography.labelMedium, fontFamily = DanaFontFamily)
                    Spacer(Modifier.width(8.dp))
                    Text("%,d".format(institution.totalRevenue).toPersianNumbers() + " ریال", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily, color = Color(0xFF2E7D32))
                }
            }
        }
    }
}

@Composable
private fun AcademyCourseCard(course: Course, isOwner: Boolean, onClick: () -> Unit, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(140.dp)) {
            // Right Side: Poster & Status
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
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
                        .padding(4.dp)
                        .align(Alignment.TopEnd)
                        .background(color = statusColor.copy(alpha = 0.9f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = statusText, color = Color.White, fontFamily = DanaFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // Left Side: Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = course.title, 
                        fontFamily = DanaFontFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 15.sp, 
                        maxLines = 2, 
                        overflow = TextOverflow.Ellipsis, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Instructor / Organizer
                    val displayName = course.instructors.firstOrNull()?.displayName 
                        ?: course.manualInstructors.firstOrNull()?.name 
                        ?: course.organizerName 
                        ?: "ناشناس"
                    val displayAvatar = course.instructors.firstOrNull()?.displayAvatarUrl 
                        ?: course.manualInstructors.firstOrNull()?.avatarUrl 
                        ?: course.organizerAvatarUrl
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(imageUrl = displayAvatar, name = displayName, size = AvatarSize.SMALL, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = displayName, fontFamily = DanaFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                
                Column {
                    // Time / Date info
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Price / Discount badge
                    val priceRials = course.priceRials
                    val priceToman = priceRials / 10
                    val formattedPrice = if (priceToman > 0) priceToman.toPersianPrice() else "۰"
                    val discountPct = course.discountPercentage ?: 0
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Bottom) {
                        CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
                            if (course.isFree || priceRials == 0L) {
                                Text(text = "رایگان", color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            } else if (discountPct > 0) {
                                val discountedPriceToman = priceToman - (priceToman * discountPct / 100)
                                val formattedDiscounted = discountedPriceToman.toPersianPrice()
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.error).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                        Text(text = "${discountPct.toString().toPersianNumbers()}%", color = Color.White, fontFamily = DanaFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = formattedPrice, 
                                            color = MaterialTheme.colorScheme.error, // Red for crossed out price
                                            fontFamily = DanaFontFamily, 
                                            fontSize = 10.sp, 
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = formattedDiscounted, color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(text = "تومان", color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 9.sp)
                                        }
                                    }
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = formattedPrice, color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "تومان", color = MaterialTheme.colorScheme.primary, fontFamily = DanaFontFamily, fontSize = 9.sp)
                                }
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
