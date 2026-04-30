package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.Kelasor.app.data.remote.dto.CourseCommentDto
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.domain.model.CourseChapter
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit,
    onInstructorClick: (String) -> Unit = {},
    onOrganizerClick: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToEditCourse: (String) -> Unit = {},
    onNavigateToCourseDetail: (String) -> Unit = {},
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CourseDetailEvent.NavigateToChat -> onNavigateToChat(event.chatId)
            }
        }
    }
    
    LaunchedEffect(state.error) {
        if (state.error == "DELETED_SUCCESS") onBack()
    }

    if (state.isLoading && state.course == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val course = state.course ?: return

    var showAdminsDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            CourseBottomBar(
                course = course,
                isEnrolled = state.isEnrolled,
                isEnrolling = state.isEnrolling,
                onEnrollClick = { viewModel.enrollInCourse() },
                isFavorite = state.isFavorite,
                onFavoriteClick = { viewModel.toggleFavorite() },
                onAdminsClick = { showAdminsDialog = true },
                onOrganizerClick = {
                    course.institutionId?.let { onOrganizerClick(it) }
                }
            )
        }
    ) { paddingValues ->
        if (showAdminsDialog) {
            AlertDialog(
                onDismissRequest = { showAdminsDialog = false },
                title = { Text("ادمین‌های دوره", fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn {
                        items(course.admins) { admin ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showAdminsDialog = false
                                        onNavigateToChat(admin.id)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(imageUrl = admin.displayAvatarUrl, name = admin.displayName, size = AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(admin.displayName, fontFamily = VazirFontFamily, modifier = Modifier.weight(1f))
                                Icon(androidx.compose.material.icons.Icons.Rounded.Chat, contentDescription = "پیام", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAdminsDialog = false }) {
                        Text("بستن", fontFamily = VazirFontFamily)
                    }
                }
            )
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Static Header
                item {
                    CourseParallaxHeader(
                        course = course,
                        scrollState = scrollState,
                        onBack = onBack,
                        isOwner = state.isOwner,
                        onEditClick = { onNavigateToEditCourse(course.id) },
                        onDeleteClick = { showDeleteDialog = true }
                    )
                }

                // Course Main Info
                item {
                    CourseMainInfo(course = course)
                }

                // 1. Instructors Section
                if (course.instructors.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        InstructorsSection(
                            instructors = course.instructors,
                            onInstructorClick = { onInstructorClick(it.id) }
                        )
                    }
                }

                // 2. Chapters (Syllabus)
                if (course.chapters.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        ChaptersSection(chapters = course.chapters)
                    }
                }

                // 3. Tags (Suitable Fields)
                if (course.tags.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        TagsSection(tags = course.tags)
                    }
                }

                // 4. Description
                if (!course.description.isNullOrEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        ExpandableDescriptionSection(description = course.description)
                    }
                }

                // 5. Organizer & Manager Sections
                item {
                    if (!course.scientificAssociationName.isNullOrEmpty() && course.institutionId != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        OrganizersSection(
                            name = course.scientificAssociationName ?: "",
                            logoUrl = course.organizerAvatarUrl,
                            institutionId = course.institutionId,
                            collaborators = course.collaborators,
                            onOrganizerClick = { onOrganizerClick(course.institutionId!!) }
                        )
                    }
                    
                    if (!course.organizerName.isNullOrEmpty() && course.creatorId != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        AcademyManagerSection(
                            managerName = course.organizerName ?: "",
                            managerAvatarUrl = course.organizerAvatarUrl,
                            managerId = course.creatorId!!,
                            onMessageClick = { managerId -> onNavigateToChat(managerId) }
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    StudentsSection(
                        students = emptyList(), // Placeholder since there is no enrolledStudents in state
                        onStudentClick = { userId -> onInstructorClick(userId) }
                    )
                }

                // 6. Rating
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    CourseRatingSection(
                        averageRating = course.rating,
                        totalRatings = course.favoritesCount + 5,
                        enrolledCount = course.enrolledCount
                    )
                }

                // 7. Comments
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    var replyToCommentId by remember { mutableStateOf<String?>(null) }
                    var replyToUserName by remember { mutableStateOf<String?>(null) }
                    CommentsSection(
                        comments = state.comments,
                        isSubmitting = state.isSubmittingComment,
                        replyToUserName = replyToUserName,
                        onCancelReply = { replyToCommentId = null; replyToUserName = null },
                        onReplyClick = { comment ->
                            replyToCommentId = comment.id
                            replyToUserName = comment.userDisplayName
                        },
                        onSubmitComment = { content, rating ->
                            viewModel.addComment(content, rating, replyToCommentId)
                            replyToCommentId = null
                            replyToUserName = null
                        }
                    )
                }

                // 8. Institution Courses (Real data from API)
                if (state.institutionCourses.isNotEmpty()) {
                    item {
                        SimilarCoursesSection(
                            title = "دیگر دوره‌های این مجموعه",
                            courses = state.institutionCourses,
                            onCourseClick = { courseId -> onNavigateToCourseDetail(courseId) }
                        )
                    }
                }

                // 9. Similar Courses (Real data from API)
                if (state.similarCourses.isNotEmpty()) {
                    item {
                        SimilarCoursesSection(
                            title = "دوره‌های مشابه",
                            courses = state.similarCourses,
                            onCourseClick = { courseId -> onNavigateToCourseDetail(courseId) }
                        )
                    }
                }
            }
            
            // Top Bar Overlay (changes color based on scroll)
            val topBarAlpha by remember {
                derivedStateOf {
                    val firstVisible = scrollState.firstVisibleItemIndex
                    val offset = scrollState.firstVisibleItemScrollOffset
                    if (firstVisible > 0) 1f else (offset / 500f).coerceIn(0f, 1f)
                }
            }
            
            TopAppBarOverlay(
                alpha = topBarAlpha,
                title = course.title,
                onBack = onBack,
                isOwner = state.isOwner,
                onEditClick = { onNavigateToEditCourse(course.id) },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف دوره", fontFamily = VazirFontFamily) },
            text = { Text("آیا از حذف این دوره اطمینان دارید؟", fontFamily = VazirFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCourse()
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontFamily = VazirFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف", fontFamily = VazirFontFamily)
                }
            }
        )
    }
}

@Composable
fun CourseParallaxHeader(
    course: Course,
    scrollState: LazyListState,
    onBack: () -> Unit,
    isOwner: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var height by remember { mutableStateOf(350.dp) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Blurred background
        AsyncImage(
            model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp)
        )
        
        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        // Vertical Poster
        AsyncImage(
            model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "https://ui-avatars.com/api/?name=${course.title}&background=random",
            contentDescription = course.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp)
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .shadow(8.dp, RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun TopAppBarOverlay(
    alpha: Float,
    title: String,
    onBack: () -> Unit,
    isOwner: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = alpha)
    val contentColor = if (alpha > 0.5f) MaterialTheme.colorScheme.onBackground else Color.White
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = if (alpha > 0.5f) 0f else 0.3f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = contentColor
                )
            }
            if (alpha > 0.8f) {
                Text(
                    text = title,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .widthIn(max = 200.dp)
                )
            }
        }
        
        Row {
            if (isOwner) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (alpha > 0.5f) 0f else 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "ویرایش",
                        tint = contentColor
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = if (alpha > 0.5f) 0f else 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun CourseStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = Color.White, fontFamily = VazirFontFamily, fontSize = 13.sp)
    }
}

@Composable
fun CourseMainInfo(course: Course) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Status Badge
        if (course.status != "APPROVED") {
            Box(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "وضعیت: " + if (course.status == "PENDING") "در انتظار بررسی" else "رد شده",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = course.title,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (!course.slogan.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.slogan,
                fontFamily = VazirFontFamily,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // "X days until start" badge
        val now = java.time.Instant.now()
        val duration = java.time.Duration.between(now, course.startsAt)
        val daysUntilStart = duration.toDays()
        val hoursUntilStart = duration.toHours()
        val minutesUntilStart = duration.toMinutes()
        val daysUntilEnd = java.time.Duration.between(now, course.endsAt).toDays()
        val timeText = when {
            daysUntilStart > 1 -> "$daysUntilStart روز تا شروع"
            daysUntilStart == 1L -> "فردا شروع می‌شود"
            hoursUntilStart in 1..23 -> "$hoursUntilStart ساعت دیگر شروع می‌شود"
            minutesUntilStart in 1..59 -> "$minutesUntilStart دقیقه دیگر شروع می‌شود"
            minutesUntilStart in 0..0 -> "الان شروع شده!"
            daysUntilEnd > 0 -> "در حال برگزاری"
            else -> "پایان یافته"
        }
        val timeColor = when {
            daysUntilStart > 1 -> Color(0xFF2E7D32)
            daysUntilStart == 1L -> Color(0xFF2E7D32)
            hoursUntilStart in 1..23 -> Color(0xFFF57C00)
            minutesUntilStart in 0..59 -> Color(0xFFF57C00)
            daysUntilEnd > 0 -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.error
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(timeColor.copy(alpha = 0.15f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, null, modifier = Modifier.size(18.dp), tint = timeColor)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = timeText, fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = timeColor)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (course.capacity != null) {
                    InfoRow(icon = Icons.Rounded.Groups, label = "ظرفیت:", value = "${course.capacity} نفر")
                }
                if (!course.fieldOfStudy.isNullOrEmpty()) {
                    InfoRow(icon = Icons.Rounded.School, label = "رشته تحصیلی:", value = course.fieldOfStudy)
                }
                if (course.durationMinutes > 0) {
                    InfoRow(icon = Icons.Rounded.Timer, label = "مدت دوره:", value = "${course.durationMinutes} دقیقه")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Price Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "هزینه دوره:",
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (course.isFree || course.priceRials == 0L) {
                Text(
                    text = "رایگان",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    if (course.discountPercentage > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.error)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "%${course.discountPercentage}",
                                    color = Color.White,
                                    fontFamily = VazirFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%,d", course.priceRials / 10),
                                fontFamily = VazirFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }
                    
                    val finalPrice = if (course.discountPercentage > 0) {
                        course.priceRials - (course.priceRials * course.discountPercentage / 100)
                    } else {
                        course.priceRials
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%,d", finalPrice / 10),
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تومان",
                            fontFamily = VazirFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontFamily = VazirFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = value, fontFamily = VazirFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun OrganizersSection(
    name: String,
    logoUrl: String?,
    institutionId: String?,
    collaborators: List<String> = emptyList(),
    onOrganizerClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "برگزارکنندگان",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main institution card
            item {
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOrganizerClick),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarImage(
                            imageUrl = logoUrl,
                            name = name,
                            size = AvatarSize.MEDIUM
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "آکادمی برگزارکننده",
                                fontFamily = VazirFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            // Collaborators
            items(collaborators) { collaboratorName ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarImage(imageUrl = null, name = collaboratorName, size = AvatarSize.SMALL)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = collaboratorName,
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "همکار",
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
}

@Composable
fun AcademyManagerSection(
    managerName: String,
    managerAvatarUrl: String?,
    managerId: String,
    onMessageClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "مدیر آکادمی",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(imageUrl = managerAvatarUrl, name = managerName, size = AvatarSize.MEDIUM)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = managerName,
                    fontFamily = VazirFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ارتباط با مدیر",
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { onMessageClick(managerId) }) {
                Text("ارسال پیام", fontFamily = VazirFontFamily, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StudentsSection(
    students: List<User>,
    onStudentClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (students.isEmpty()) {
            Text(
                text = "لیست دانشجویان در حال حاضر خالی است.",
                fontFamily = VazirFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(students) { student ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onStudentClick(student.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(imageUrl = student.avatarUrl, name = student.displayName, size = AvatarSize.MEDIUM)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.displayName,
                                    fontFamily = VazirFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "دانشجو",
                                    fontFamily = VazirFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun InstructorsSection(
    instructors: List<User>,
    onInstructorClick: (User) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "مدرسین دوره",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(instructors) { instructor ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onInstructorClick(instructor) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarImage(imageUrl = instructor.avatarUrl, name = instructor.displayName, size = AvatarSize.MEDIUM)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = instructor.displayName,
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "مدرس",
                                fontFamily = VazirFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminsSection(
    admins: List<User>,
    onMessageClick: (User) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "پشتیبانی و ادمین‌ها",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            admins.forEach { admin ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(imageUrl = admin.avatarUrl, name = admin.displayName, size = AvatarSize.SMALL)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = admin.displayName,
                        fontFamily = VazirFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onMessageClick(admin) }) {
                        Text("ارسال پیام", fontFamily = VazirFontFamily, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableDescriptionSection(description: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "توضیحات دوره",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontFamily = VazirFontFamily,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        
        if (description.length > 200) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (expanded) "بستن" else "مشاهده بیشتر",
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ChaptersSection(chapters: List<CourseChapter>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "سرفصل‌ها",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chapters.forEachIndexed { index, chapter ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = chapter.title,
                        fontFamily = VazirFontFamily,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = chapter.durationText,
                        fontFamily = VazirFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TagsSection(tags: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "برچسب‌ها",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "#$tag",
                        fontFamily = VazirFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CourseRatingSection(
    averageRating: Double,
    totalRatings: Int,
    enrolledCount: Int
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "نظرات و امتیازات",
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%.1f".format(averageRating),
                    fontFamily = VazirFontFamily,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                StarRatingBar(rating = averageRating, size = 18.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalRatings امتیاز",
                    fontFamily = VazirFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RatingProgressRow(stars = 5, progress = 0.8f)
                RatingProgressRow(stars = 4, progress = 0.15f)
                RatingProgressRow(stars = 3, progress = 0.05f)
                RatingProgressRow(stars = 2, progress = 0f)
                RatingProgressRow(stars = 1, progress = 0f)
            }
        }
    }
}

@Composable
fun RatingProgressRow(stars: Int, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$stars", fontFamily = VazirFontFamily, fontSize = 12.sp, modifier = Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StarRatingBar(rating: Double, size: androidx.compose.ui.unit.Dp = 16.dp) {
    Row {
        repeat(5) { index ->
            val starIndex = index + 1
            val icon = when {
                rating >= starIndex -> Icons.Rounded.Star
                rating >= starIndex - 0.5 -> Icons.Rounded.StarHalf
                else -> Icons.Rounded.StarOutline
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (rating >= starIndex - 0.5) Color(0xFFFFC107) else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
fun CommentsSection(
    comments: List<CourseCommentDto>,
    isSubmitting: Boolean,
    replyToUserName: String? = null,
    onCancelReply: () -> Unit = {},
    onReplyClick: (CourseCommentDto) -> Unit = {},
    onSubmitComment: (String, Int) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(5) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        // Add Comment Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "نظر شما چیست؟",
                    fontFamily = VazirFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Reply indicator
                if (replyToUserName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "پاسخ به $replyToUserName",
                            fontFamily = VazirFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onCancelReply,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "لغو",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Clickable Stars
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        IconButton(
                            onClick = { selectedRating = starIndex },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedRating >= starIndex) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = null,
                                tint = if (selectedRating >= starIndex) Color(0xFFFFC107) else MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("تجربه خود را بنویسید...", fontFamily = VazirFontFamily, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            onSubmitComment(commentText, selectedRating)
                            commentText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !isSubmitting && commentText.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("ثبت نظر", fontFamily = VazirFontFamily)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Comments List
        if (comments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("هنوز نظری ثبت نشده است. اولین نفر باشید!", fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.forEach { comment ->
                    CommentItem(comment = comment, onReplyClick = { onReplyClick(comment) })
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: CourseCommentDto, onReplyClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    imageUrl = comment.userAvatarUrl,
                    name = comment.userDisplayName,
                    size = AvatarSize.SMALL
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.userDisplayName,
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    StarRatingBar(rating = comment.rating.toDouble(), size = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment.content,
                fontFamily = VazirFontFamily,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onReplyClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "پاسخ",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("پاسخ", fontFamily = VazirFontFamily, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SimilarCoursesSection(
    title: String,
    courses: List<Course>,
    onCourseClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionTitle(title = title, icon = Icons.Rounded.LibraryBooks)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(courses) { course ->
                CourseCard(
                    course = course,
                    onClick = { onCourseClick(course.id) },
                    modifier = Modifier.width(260.dp)
                )
            }
        }
    }
}

@Composable
fun CourseBottomBar(
    course: Course,
    isEnrolled: Boolean,
    isEnrolling: Boolean,
    onEnrollClick: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onAdminsClick: () -> Unit = {},
    onOrganizerClick: () -> Unit = {}
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: Organizer Logo, Admin button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Organizer Logo
                AvatarImage(
                    imageUrl = course.organizerAvatarUrl,
                    name = course.organizerName ?: "M",
                    size = AvatarSize.SMALL,
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant).clickable { onOrganizerClick() }
                )
                
                // Admins Button
                if (course.admins.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onAdminsClick() }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SupportAgent,
                            contentDescription = "ارتباط با ادمین",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ادمین‌ها",
                            fontFamily = VazirFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Right Side: Like Button & Enroll Button
            val context = androidx.compose.ui.platform.LocalContext.current
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Like Button
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onFavoriteClick() }.padding(4.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "${course.favoritesCount}",
                        fontFamily = VazirFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { 
                        if (course.status != "APPROVED") {
                            android.widget.Toast.makeText(context, "این دوره در حال حاضر قابل ثبت‌نام نیست", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (!isEnrolled && !isEnrolling) {
                            onEnrollClick()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isEnrolled -> MaterialTheme.colorScheme.secondary
                            course.status != "APPROVED" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                    enabled = course.status == "APPROVED" || isEnrolled
                ) {
                    if (isEnrolling) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else if (isEnrolled) {
                        Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت‌نام شده", fontFamily = VazirFontFamily, fontSize = 14.sp)
                    } else if (course.status != "APPROVED") {
                        Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("غیرقابل ثبت‌نام", fontFamily = VazirFontFamily, fontSize = 14.sp)
                    } else {
                        Icon(imageVector = Icons.Rounded.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت‌نام در دوره", fontFamily = VazirFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

