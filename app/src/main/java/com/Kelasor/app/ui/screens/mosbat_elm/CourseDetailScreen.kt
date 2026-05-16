package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.*
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
import com.Kelasor.app.domain.model.ManualInstructor
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice
import java.time.Instant
import java.time.Duration
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
    onNavigateToCategory: (String) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showWalletSheet by remember { mutableStateOf(false) }
    var currentTime: java.time.Instant by remember { mutableStateOf(java.time.Instant.now()) }
    
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = java.time.Instant.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CourseDetailEvent.NavigateToChat -> onNavigateToChat(event.chatId)
                is CourseDetailEvent.OpenUrl -> {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(event.url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "خطا در باز کردن لینک", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    var showEnrollmentSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(state.error) {
        if (state.error == "DELETED_SUCCESS") onBack()
        else if (state.error == "ENROLL_SUCCESS") {
            showEnrollmentSuccess = true
            viewModel.clearError()
        }
        else if (state.error == "KELASOR_CREATED") {
            android.widget.Toast.makeText(context, "کلاسور آنلاین با موفقیت ایجاد شد ✅", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
        else if (state.error != null) {
            android.widget.Toast.makeText(context, state.error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    if (showEnrollmentSuccess) {
        MosbatElmEnrollmentSuccessDialog(
            courseTitle = state.course?.title ?: "",
            onDismiss = { showEnrollmentSuccess = false }
        )
    }

    if (showWalletSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWalletSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "انتخاب روش پرداخت",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Option 1: Wallet
                Surface(
                    onClick = { 
                        showWalletSheet = false
                        viewModel.enrollInCourse("WALLET") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("پرداخت از کیف پول", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("موجودی: ۱,۰۰۰,۰۰۰,۰۰۰ تومان", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Option 2: Online
                Surface(
                    onClick = { 
                        showWalletSheet = false
                        viewModel.enrollInCourse("ONLINE") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Public, null, tint = MaterialTheme.colorScheme.secondary)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text("پرداخت آنلاین", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("اتصال به درگاه بانکی شتاب", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "با خرید این دوره، دسترسی شما به محتوا و گروه اختصاصی فعال خواهد شد.",
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
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
                isOwner = state.isOwner,
                onEnrollClick = { viewModel.enrollInCourse("WALLET") },
                isFavorite = state.isFavorite,
                onFavoriteClick = { viewModel.toggleFavorite() },
                onAdminsClick = { showAdminsDialog = true },
                onOrganizerClick = {
                    course.institutionId?.let { onOrganizerClick(it) }
                },
                onShowWalletSheet = { showWalletSheet = true },
                isJoiningClass = state.isJoiningClass,
                onJoinOnlineClass = { viewModel.joinOnlineClass() }
            )
        }
    ) { paddingValues ->
        if (showAdminsDialog) {
            AlertDialog(
                onDismissRequest = { showAdminsDialog = false },
                title = { Text("ادمین‌های دوره", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
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
                                Text(admin.displayName, fontFamily = DanaFontFamily, modifier = Modifier.weight(1f))
                                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.Chat, contentDescription = "پیام", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAdminsDialog = false }) {
                        Text("بستن", fontFamily = DanaFontFamily)
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
                        onDeleteClick = { showDeleteDialog = true },
                        currentTime = currentTime
                    )
                }

                // Course Main Info
                item {
                    CourseMainInfo(course = course, currentTime = currentTime)
                }

                // 1. Instructors Section
                if (course.instructors.isNotEmpty() || course.manualInstructors.isNotEmpty()) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        InstructorsSection(
                            officialInstructors = course.instructors,
                            manualInstructors = course.manualInstructors,
                            onInstructorClick = { instructor ->
                                if (instructor.officialChannelId != null) {
                                    onChannelClick(instructor.officialChannelId)
                                } else {
                                    onInstructorClick(instructor.id)
                                }
                            },
                            onChannelClick = { onChannelClick(it) }
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
                    if (!course.organizerName.isNullOrEmpty() && course.institutionId != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        OrganizersSection(
                            name = course.organizerName,
                            logoUrl = course.organizerAvatarUrl,
                            institutionId = course.institutionId,
                            collaborators = course.collaborators,
                            onOrganizerClick = { onOrganizerClick(course.institutionId) },
                            onChannelClick = { onChannelClick(it) }
                        )
                    }
                    
                    if (course.managerId.isNotEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        AcademyManagerSection(
                            managerName = course.managerName,
                            managerAvatarUrl = course.managerAvatarUrl,
                            managerId = course.managerId,
                            onMessageClick = { onNavigateToChat(course.managerId) },
                            onManagerClick = { managerId -> onInstructorClick(managerId) }
                        )
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    StudentsSection(
                        students = emptyList(), // Placeholder since there is no enrolledStudents in state
                        onStudentClick = { userId -> onInstructorClick(userId) }
                    )
                }

                // 5.5 Kelasor Online Management (BBB)
                if (state.isOwner || (state.isEnrolled && course.hasOnlineClass)) {
                    item {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                        KelasorOnlineSection(
                            hasOnlineClass = course.hasOnlineClass,
                            isOwner = state.isOwner,
                            isCreatingKelasor = state.isCreatingKelasor,
                            isJoiningClass = state.isJoiningClass,
                            onCreateKelasor = { viewModel.createKelasorOnline() },
                            onJoinClass = { viewModel.joinOnlineClass() },
                            onCopyInviteLink = {
                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("لینک دوره", "https://mosbatelm.ir/course/${course.id}")
                                clipboardManager.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "لینک دعوت کپی شد ✅", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                // 6. Rating
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                    CourseRatingSection(
                        averageRating = course.rating,
                        totalRatings = course.reviewCount,
                        comments = state.comments
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
                            onCourseClick = { courseId -> onNavigateToCourseDetail(courseId) },
                            onViewAllClick = { onOrganizerClick(course.institutionId ?: "") }
                        )
                    }
                }

                // 9. Similar Courses (Real data from API)
                if (state.similarCourses.isNotEmpty()) {
                    item {
                        SimilarCoursesSection(
                            title = "دوره‌های مشابه",
                            courses = state.similarCourses,
                            onCourseClick = { courseId -> onNavigateToCourseDetail(courseId) },
                            onViewAllClick = { onNavigateToCategory("همه دسته‌ها") }
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
            title = { Text("حذف دوره", fontFamily = DanaFontFamily) },
            text = { Text("آیا از حذف این دوره اطمینان دارید؟", fontFamily = DanaFontFamily) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCourse()
                }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontFamily = DanaFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
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
    onDeleteClick: () -> Unit,
    currentTime: java.time.Instant = java.time.Instant.now()
) {
    var height: androidx.compose.ui.unit.Dp by remember { mutableStateOf(350.dp) }
    
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
                    fontFamily = DanaFontFamily,
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
        Text(text = text, color = Color.White, fontFamily = DanaFontFamily, fontSize = 13.sp)
    }
}

@Composable
fun CourseMainInfo(course: Course, currentTime: java.time.Instant = java.time.Instant.now()) {
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
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = course.title,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            
            // Session Countdown Timer exactly like MyCoursesScreen
            val nextChapter = course.chapters?.firstOrNull { chapter ->
                val start = chapter.sessionStartTime
                start != null && currentTime.isBefore(start.plus(java.time.Duration.ofHours(2)))
            }
            
            if (nextChapter != null) {
                val startTime = nextChapter.sessionStartTime
                val endTime = nextChapter.sessionEndTime
                
                if (startTime != null) {
                    val (timerText, timerColor, timerIcon) = remember(currentTime, startTime, endTime) {
                        when {
                            currentTime.isBefore(startTime) -> {
                                val diff = java.time.Duration.between(currentTime, startTime)
                                val h = diff.toHours()
                                val m = (diff.toMinutes() % 60).toInt()
                                val s = (diff.seconds % 60).toInt()
                                Triple("شروع جلسه در: ${h}:${m.toString().padStart(2,'0')}:${s.toString().padStart(2,'0')}".toPersianNumbers(), Color(0xFF2196F3), Icons.Rounded.Timer)
                            }
                            endTime != null && currentTime.isBefore(endTime) -> Triple("در حال برگزاری", Color(0xFFE91E63), Icons.Rounded.PlayCircleFilled)
                            endTime == null && currentTime.isBefore(startTime.plus(java.time.Duration.ofHours(2))) -> Triple("در حال برگزاری", Color(0xFFE91E63), Icons.Rounded.PlayCircleFilled)
                            else -> Triple("", Color.Transparent, Icons.Rounded.Timer)
                        }
                    }
                    
                    if (timerText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(timerColor.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(timerIcon, contentDescription = null, tint = timerColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timerText,
                                    color = timerColor,
                                    fontFamily = DanaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            
            if (course.rating >= 4.5) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, modifier = Modifier.size(14.dp), tint = Color(0xFFD4AF37))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "محبوب",
                            fontFamily = DanaFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4AF37)
                        )
                    }
                }
            }
        }
        if (!course.slogan.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.slogan,
                fontFamily = DanaFontFamily,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // ── Premium Countdown Timer Card ──
        if (course.chapters.isNotEmpty()) {
            val now = currentTime
            val duration = java.time.Duration.between(now, course.startsAt)
            val daysUntilStart = duration.toDays()
            val hoursUntilStart = duration.toHours()
            val daysUntilEnd = java.time.Duration.between(now, course.endsAt).toDays()
            val firstChapter = course.chapters.firstOrNull()
            val isFinished: Boolean = daysUntilEnd <= 0 && daysUntilStart < 0
            val isBefore: Boolean = duration.toMillis() > 0
            if (isBefore) {
                // Countdown Timer Card
                val days = duration.toDays()
                val hours = (duration.toHours() % 24).toInt()
                val minutes = (duration.toMinutes() % 60).toInt()
                val seconds = (duration.seconds % 60).toInt()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "شروع دوره:",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Timer boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (days > 0) {
                                TimerUnit(value = days.toInt(), label = "روز")
                                TimerSeparator()
                            }
                            TimerUnit(value = hours, label = "ساعت")
                            TimerSeparator()
                            TimerUnit(value = minutes, label = "دقیقه")
                            TimerSeparator()
                            TimerUnit(value = seconds, label = "ثانیه")
                        }
                        if (firstChapter != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Rounded.MenuBook, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "اولین سرفصل: ${firstChapter.title}",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (isFinished) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "پایان یافته", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        // Info Card — only show if there's at least one field to display
        val hasCapacity: Boolean = course.capacity != null
        val hasField: Boolean = !course.fieldOfStudy.isNullOrEmpty()
        val hasDuration: Boolean = course.durationMinutes > 0
        if (hasCapacity || hasField || hasDuration) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (hasCapacity) {
                        InfoRow(icon = Icons.Rounded.Groups, label = "ظرفیت:", value = "${course.capacity} نفر")
                    }
                    if (hasField) {
                        InfoRow(icon = Icons.Rounded.School, label = "رشته تحصیلی:", value = course.fieldOfStudy!!)
                    }
                    if (hasDuration) {
                        InfoRow(icon = Icons.Rounded.Timer, label = "مدت دوره:", value = "${course.durationMinutes} دقیقه")
                    }
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
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (course.isFree || course.priceRials == 0L) {
                Text(
                    text = "رایگان",
                    fontFamily = DanaFontFamily,
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
                                    fontFamily = DanaFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format("%,d", course.priceRials / 10).toPersianNumbers(),
                                fontFamily = DanaFontFamily,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }
                    }
                    
                    val price = course.priceRials / 10
                    val finalPrice = if (course.discountPercentage > 0) {
                        price - (price * course.discountPercentage / 100)
                    } else {
                        price
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%,d", finalPrice).toPersianNumbers(),
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تومان",
                            fontFamily = DanaFontFamily,
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
        Text(text = label, fontFamily = DanaFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(text = value, fontFamily = DanaFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun OrganizersSection(
    name: String,
    logoUrl: String?,
    institutionId: String?,
    collaborators: List<String> = emptyList(),
    onOrganizerClick: () -> Unit,
    onChannelClick: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "برگزارکنندگان",
            fontFamily = DanaFontFamily,
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
                        .width(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOrganizerClick),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    fontFamily = DanaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "آکادمی برگزارکننده",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onOrganizerClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.AccountCircle, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("پروفایل", fontFamily = DanaFontFamily, fontSize = 12.sp)
                            }
                            if (!institutionId.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = { onChannelClick(institutionId) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.Campaign, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("کانال", fontFamily = DanaFontFamily, fontSize = 12.sp)
                                }
                            }
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
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "همکار",
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
}

@Composable
fun AcademyManagerSection(
    managerName: String,
    managerAvatarUrl: String?,
    managerId: String,
    onMessageClick: (String) -> Unit,
    onManagerClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "مدیر دوره",
            fontFamily = DanaFontFamily,
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
                .clickable { onManagerClick(managerId) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(imageUrl = managerAvatarUrl, name = managerName, size = AvatarSize.MEDIUM)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = managerName,
                    fontFamily = DanaFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ارتباط با مدیر",
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { onMessageClick(managerId) }) {
                Text("ارسال پیام", fontFamily = DanaFontFamily, fontSize = 13.sp)
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
                fontFamily = DanaFontFamily,
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
                                    fontFamily = DanaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "دانشجو",
                                    fontFamily = DanaFontFamily,
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
    officialInstructors: List<User>,
    manualInstructors: List<ManualInstructor>,
    onInstructorClick: (User) -> Unit,
    onChannelClick: (String) -> Unit = {}
) {
    if (officialInstructors.isEmpty() && manualInstructors.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = "مدرسین دوره",
            fontFamily = DanaFontFamily,
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
            // Official Instructors
            items(officialInstructors) { instructor ->
                InstructorCard(
                    name = instructor.displayName,
                    avatarUrl = instructor.avatarUrl,
                    label = "مدرس رسمی",
                    officialChannelId = instructor.officialChannelId ?: instructor.bioChannelId1,
                    onCardClick = { if (instructor.officialChannelId != null) onChannelClick(instructor.officialChannelId) else onInstructorClick(instructor) },
                    onChannelClick = onChannelClick
                )
            }
            
            // Manual Instructors
            items(manualInstructors) { manual ->
                InstructorCard(
                    name = manual.name,
                    avatarUrl = manual.avatarUrl,
                    label = "مدرس مهمان",
                    resume = manual.resume,
                    onCardClick = { /* No specific action for manual instructors yet */ }
                )
            }
        }
    }
}

@Composable
private fun InstructorCard(
    name: String,
    avatarUrl: String?,
    label: String,
    resume: String? = null,
    officialChannelId: String? = null,
    onCardClick: () -> Unit,
    onChannelClick: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(imageUrl = avatarUrl, name = name, size = AvatarSize.MEDIUM)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = label,
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (!resume.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = resume,
                    fontFamily = DanaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            if (!officialChannelId.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onChannelClick(officialChannelId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.Campaign, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کانال مدرس",
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
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
            fontFamily = DanaFontFamily,
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
                        fontFamily = DanaFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onMessageClick(admin) }) {
                        Text("ارسال پیام", fontFamily = DanaFontFamily, fontSize = 12.sp)
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
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontFamily = DanaFontFamily,
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
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ChaptersSection(chapters: List<CourseChapter>) {
    var currentTime by remember { mutableStateOf(java.time.Instant.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(1000)
            currentTime = java.time.Instant.now()
        }
    }
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "سرفصل‌ها و جلسات",
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chapters.forEachIndexed { index, chapter ->
                ChapterItem(index = index, chapter = chapter, currentTime = currentTime)
            }
        }
    }
}

@Composable
fun ChapterItem(index: Int, chapter: CourseChapter, currentTime: java.time.Instant) {
    val startTime: java.time.Instant? = chapter.sessionStartTime
    val statusColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}".toPersianNumbers(),
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.title,
                    fontFamily = DanaFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                if (startTime != null) {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                        .withZone(java.time.ZoneId.of("Asia/Tehran"))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatter.format(startTime).toPersianNumbers(),
                            fontFamily = DanaFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MosbatElmEnrollmentSuccessDialog(courseTitle: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("متوجه شدم", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "ثبت‌نام با موفقیت انجام شد!",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "تبریک! شما با موفقیت در دوره «$courseTitle» ثبت‌نام کردید. هم‌اکنون می‌توانید از بخش پروفایل -> دوره‌های من به محتوای دوره دسترسی داشته باشید.",
                    fontFamily = DanaFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "یک پیام تایید نیز توسط ربات مثبت علم برای شما ارسال شد.",
                        fontFamily = DanaFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
fun TagsSection(tags: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "برچسب‌ها",
            fontFamily = DanaFontFamily,
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
                        fontFamily = DanaFontFamily,
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
    comments: List<CourseCommentDto>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "نظرات و امتیازات",
            fontFamily = DanaFontFamily,
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
                    text = if (averageRating > 0) "%.1f".format(averageRating) else "۰.۰",
                    fontFamily = DanaFontFamily,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                StarRatingBar(rating = averageRating, size = 18.dp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${totalRatings.toString().toPersianNumbers()} نظر",
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val total = comments.size.coerceAtLeast(1)
                for (i in 5 downTo 1) {
                    val count = comments.count { it.rating == i }
                    RatingProgressRow(stars = i, progress = count.toFloat() / total)
                }
            }
        }
    }
}

@Composable
fun RatingProgressRow(stars: Int, progress: Float) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$stars", fontFamily = DanaFontFamily, fontSize = 12.sp, modifier = Modifier.width(12.dp))
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
                rating >= starIndex - 0.5 -> Icons.AutoMirrored.Rounded.StarHalf
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
                    fontFamily = DanaFontFamily,
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
                            fontFamily = DanaFontFamily,
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
                    placeholder = { Text("تجربه خود را بنویسید...", fontFamily = DanaFontFamily, fontSize = 13.sp) },
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
                        Text("ثبت نظر", fontFamily = DanaFontFamily)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Comments List
        if (comments.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("هنوز نظری ثبت نشده است. اولین نفر باشید!", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Grouping comments by parent
                val parentComments = comments.filter { it.replyToCommentId == null }
                val replies = comments.filter { it.replyToCommentId != null }
                
                parentComments.forEach { parent ->
                    Column {
                        CommentItem(comment = parent, onReplyClick = { onReplyClick(parent) })
                        
                        // Render replies for this parent
                        val commentReplies = replies.filter { it.replyToCommentId == parent.id }
                        if (commentReplies.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .padding(start = 32.dp, top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                commentReplies.forEach { reply ->
                                    CommentItem(
                                        comment = reply, 
                                        isReply = true,
                                        onReplyClick = { onReplyClick(reply) }
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

@Composable
fun CommentItem(
    comment: CourseCommentDto, 
    isReply: Boolean = false,
    onReplyClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isReply) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isReply) 0.dp else 1.dp),
        border = if (isReply) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    imageUrl = comment.userAvatarUrl,
                    name = comment.userDisplayName,
                    size = if (isReply) AvatarSize.EXTRA_SMALL else AvatarSize.SMALL
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.userDisplayName,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isReply) 12.sp else 13.sp
                    )
                    if (!isReply) {
                        StarRatingBar(rating = comment.rating.toDouble(), size = 14.dp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comment.content,
                fontFamily = DanaFontFamily,
                fontSize = if (isReply) 12.sp else 13.sp,
                lineHeight = if (isReply) 20.sp else 22.sp,
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
                    Text("پاسخ", fontFamily = DanaFontFamily, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SimilarCoursesSection(
    title: String,
    courses: List<Course>,
    onCourseClick: (String) -> Unit,
    onViewAllClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        SectionTitle(
            title = title, 
            icon = Icons.AutoMirrored.Rounded.LibraryBooks,
            onViewAllClick = onViewAllClick
        )
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

// ═══════════════════════════════════════════════════════════════════════════════
// 🎥 Kelasor Online Management Section (BBB)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun KelasorOnlineSection(
    hasOnlineClass: Boolean,
    isOwner: Boolean,
    isCreatingKelasor: Boolean,
    isJoiningClass: Boolean,
    onCreateKelasor: () -> Unit,
    onJoinClass: () -> Unit,
    onCopyInviteLink: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "kelasor_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6C63FF), Color(0xFF3F51B5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.VideoCameraFront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "کلاسور آنلاین",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (hasOnlineClass) "اتاق آماده است" else "اتاق هنوز ساخته نشده",
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = if (hasOnlineClass) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            border = BorderStroke(
                1.dp,
                if (hasOnlineClass) Color(0xFF6C63FF).copy(alpha = glowAlpha) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isOwner && !hasOnlineClass) {
                    // Owner: Create Kelasor Button
                    Button(
                        onClick = onCreateKelasor,
                        enabled = !isCreatingKelasor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        )
                    ) {
                        if (isCreatingKelasor) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "در حال ایجاد...",
                                fontFamily = DanaFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "ساخت کلاسور آنلاین",
                                fontFamily = DanaFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "با ساخت کلاسور آنلاین، یک اتاق جلسه مجازی برای دوره شما ایجاد می‌شود.",
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (hasOnlineClass) {
                    // Online Class exists — show action buttons
                    if (isOwner) {
                        // Owner: Enter Room (Start Class) button
                        Button(
                            onClick = onJoinClass,
                            enabled = !isJoiningClass,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            if (isJoiningClass) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Rounded.PlayCircleFilled, null, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ورود به اتاق (شروع کلاس)",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Copy Invite Link button
                        OutlinedButton(
                            onClick = onCopyInviteLink,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF6C63FF).copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = null,
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "کپی لینک دعوت",
                                fontFamily = DanaFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6C63FF)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "پس از ورود و فشردن دکمه شروع، دانشجویان می‌توانند وارد کلاس شوند. ضبط کلاس فعال است.",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Student: Join Class button
                        Button(
                            onClick = onJoinClass,
                            enabled = !isJoiningClass,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isJoiningClass) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Rounded.VideoCameraFront, null, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ورود به کلاس آنلاین",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "اگر برگزارکننده هنوز کلاس را شروع نکرده باشد، امکان ورود وجود ندارد.",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseBottomBar(
    course: Course,
    isEnrolled: Boolean,
    isEnrolling: Boolean,
    isOwner: Boolean = false,
    onEnrollClick: () -> Unit,
    isJoiningClass: Boolean = false,
    onJoinOnlineClass: () -> Unit = {},
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onAdminsClick: () -> Unit = {},
    onOrganizerClick: () -> Unit = {},
    onShowWalletSheet: () -> Unit = {}
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
            // Left Side: Organizer (Academy) Logo, Admin button
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Organizer (Academy) Logo + Name — NOT the personal messenger profile
                val hasInstitution: Boolean = !course.institutionId.isNullOrEmpty()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(enabled = hasInstitution) { onOrganizerClick() }
                        .padding(end = 4.dp)
                        .widthIn(max = 60.dp)
                ) {
                    AvatarImage(
                        imageUrl = course.organizerAvatarUrl,
                        name = course.organizerName ?: course.title.firstOrNull()?.toString() ?: "M",
                        size = AvatarSize.SMALL,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (!course.organizerName.isNullOrEmpty()) {
                        Text(
                            text = course.organizerName,
                            fontFamily = DanaFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
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
                            fontFamily = DanaFontFamily,
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
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isFull = course.capacity != null && course.enrolledCount >= course.capacity

                if ((isEnrolled || isOwner) && course.hasOnlineClass) {
                    Button(
                        onClick = onJoinOnlineClass,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isJoiningClass
                    ) {
                        if (isJoiningClass) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.VideoCameraFront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ورود به کلاس", fontFamily = DanaFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { 
                            if (isOwner) {
                                // Do nothing
                            } else if (course.status != "APPROVED") {
                                android.widget.Toast.makeText(context, "این دوره در حال حاضر قابل ثبت‌نام نیست", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (isFull && !isEnrolled) {
                                android.widget.Toast.makeText(context, "ظرفیت این دوره تکمیل شده است", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (!isEnrolled && !isEnrolling) {
                                onShowWalletSheet()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isOwner -> MaterialTheme.colorScheme.surfaceVariant
                                isEnrolled -> MaterialTheme.colorScheme.secondary
                                course.status != "APPROVED" || (isFull && !isEnrolled) -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        enabled = !isOwner && ((course.status == "APPROVED" && (!isFull || isEnrolled)) || isEnrolled)
                    ) {
                        if (isOwner) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("شما سازنده دوره هستید", fontFamily = DanaFontFamily, fontSize = 14.sp)
                        } else if (isEnrolling) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        } else if (isEnrolled) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت‌نام شده", fontFamily = DanaFontFamily, fontSize = 14.sp)
                        } else if (course.status != "APPROVED") {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("غیرقابل ثبت‌نام", fontFamily = DanaFontFamily, fontSize = 14.sp)
                        } else if (isFull) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تکمیل ظرفیت", fontFamily = DanaFontFamily, fontSize = 14.sp)
                        } else {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Rounded.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ثبت‌نام در دوره", fontFamily = DanaFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TimerUnit(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = DanaFontFamily,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimerSeparator() {
    Text(
        text = ":",
        fontFamily = DanaFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)
    )
}
