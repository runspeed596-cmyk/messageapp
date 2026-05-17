package com.Kelasor.app.ui.screens.course

import android.widget.Toast
import coil3.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.data.remote.dto.CreateCourseRequest
import com.Kelasor.app.domain.model.ManualInstructor
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.KelasorToast
import com.Kelasor.app.ui.components.ToastType
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.CourseViewModel
import com.Kelasor.app.ui.viewmodel.CreateCourseState
import com.Kelasor.app.ui.viewmodel.ReferenceDataViewModel
import com.Kelasor.app.util.toPersianDateTime
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    editCourseId: String? = null,
    onNavigateBack: () -> Unit,
    onCourseCreated: (String?) -> Unit,
    onNavigateToEditAcademyProfile: () -> Unit = {},
    viewModel: CourseViewModel = hiltViewModel(),
    refViewModel: ReferenceDataViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val createCourseState by viewModel.createCourseState.collectAsState()
    val refState by refViewModel.state.collectAsState()
    val loadedCourse by viewModel.loadedCourse.collectAsState()
    val academyInstructors by viewModel.academyInstructors.collectAsState()
    val academyAdmins by viewModel.academyAdmins.collectAsState()
    var showAdminDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        refViewModel.loadReferenceData()
    }
    
    LaunchedEffect(editCourseId) {
        if (editCourseId != null) {
            viewModel.loadCourseForEdit(editCourseId)
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("create_course_draft", android.content.Context.MODE_PRIVATE) }
    
    var title by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_title", "") ?: "" else "") }
    var validationAttempted by remember { mutableStateOf(false) }

    var showAddInstructorTypeDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var slogan by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_slogan", "") ?: "" else "") }
    var description by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_description", "") ?: "" else "") }
    var priceTomans by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_priceTomans", "") ?: "" else "") }
    var fieldOfStudyError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var posterError by remember { mutableStateOf<String?>(null) }
    var priceDisplay by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_priceDisplay", "") ?: "" else "") }
    var discountPercentage by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_discountPercentage", "") ?: "" else "") }
    var capacity by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_capacity", "") ?: "" else "") }
    var syllabusDurationHours by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_syllabusDurationHours", "") ?: "" else "") }
    var syllabusDurationMinutes by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getString("draft_syllabusDurationMinutes", "") ?: "" else "") }
    var isPublic by remember { mutableStateOf(if (editCourseId == null) sharedPrefs.getBoolean("draft_isPublic", true) else true) }
    var posterUrl by remember { mutableStateOf<String?>(if (editCourseId == null) sharedPrefs.getString("draft_posterUrl", null) else null) }
    var isUploadingPoster by remember { mutableStateOf(false) }
    var tagsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTagInput by remember { mutableStateOf("") }
    var selectedCollaborators by remember { mutableStateOf<List<com.Kelasor.app.domain.model.Institution>>(emptyList()) }
    var selectedInstructors by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedAdmins by remember { mutableStateOf<List<User>>(emptyList()) }
    var showUserSearchDialog by remember { mutableStateOf(false) }
    var showInstructorDialog by remember { mutableStateOf(false) }
    var showCollaboratorDialog by remember { mutableStateOf(false) }
    var showAdminSearchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(title, slogan, description, priceTomans, priceDisplay, discountPercentage, capacity, syllabusDurationHours, syllabusDurationMinutes, isPublic, posterUrl) {
        if (editCourseId == null) {
            sharedPrefs.edit().apply {
                putString("draft_title", title)
                putString("draft_slogan", slogan)
                putString("draft_description", description)
                putString("draft_priceTomans", priceTomans)
                putString("draft_priceDisplay", priceDisplay)
                putString("draft_discountPercentage", discountPercentage)
                putString("draft_capacity", capacity)
                putString("draft_syllabusDurationHours", syllabusDurationHours)
                putString("draft_syllabusDurationMinutes", syllabusDurationMinutes)
                putBoolean("draft_isPublic", isPublic)
                putString("draft_posterUrl", posterUrl)
            }.apply()
        }
    }
    var suitableForList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isVerticalPoster by remember { mutableStateOf(false) }
    var chaptersList by remember { mutableStateOf<List<com.Kelasor.app.data.remote.dto.CourseChapterDto>>(emptyList()) }
    var newChapterTitle by remember { mutableStateOf("") }
    var newChapterStartTimeIso by remember { mutableStateOf<String?>(null) }
    var newChapterEndTimeIso by remember { mutableStateOf<String?>(null) }
    var newChapterStartTimeDisplay by remember { mutableStateOf("") }
    var newChapterEndTimeDisplay by remember { mutableStateOf("") }
    var selectedManualInstructors by remember { mutableStateOf<List<com.Kelasor.app.domain.model.ManualInstructor>>(emptyList()) }
    var showManualInstructorDialog by remember { mutableStateOf(false) }
    var fieldSearchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(loadedCourse) {
        loadedCourse?.let { course ->
            title = course.title
            slogan = course.slogan ?: ""
            description = course.description ?: ""
            priceTomans = (course.priceRials / 10).toString()
            priceDisplay = String.format("%,d", course.priceRials / 10)
            discountPercentage = if (course.discountPercentage > 0) course.discountPercentage.toString() else ""
            capacity = course.capacity?.toString() ?: ""
            syllabusDurationHours = (course.durationMinutes / 60).toString()
            syllabusDurationMinutes = (course.durationMinutes % 60).toString()
            posterUrl = course.posterUrl
            tagsList = course.tags
            isVerticalPoster = course.isVerticalPoster
            
            // Load new fields
            selectedInstructors = course.instructors
            selectedAdmins = course.admins
            suitableForList = course.suitableFor
            chaptersList = course.chapters.map { com.Kelasor.app.data.remote.dto.CourseChapterDto(it.title, it.durationText, it.sessionStartTime?.toString(), it.sessionEndTime?.toString()) }
            selectedManualInstructors = course.manualInstructors
            
            // Dates removed as per user request
            // Only syllabus duration and chapters remain for scheduling
            
            // We can't directly load collaborators since they are a List<String> of names/IDs
            // but we can at least avoid overwriting them
        }
    }
    
    val scope = rememberCoroutineScope()
    
    val posterPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isUploadingPoster = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val tempFile = File.createTempFile("poster_", ".jpg", context.cacheDir)
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val result = viewModel.uploadPoster(tempFile)
                    if (result.isSuccess) {
                        posterUrl = result.getOrThrow()
                    }
                    isUploadingPoster = false
                    tempFile.delete()
                } catch (e: Exception) {
                    isUploadingPoster = false
                }
            }
        }
    }
    
    LaunchedEffect(createCourseState) {
        if (createCourseState is CreateCourseState.Success) {
            val courseId = (createCourseState as CreateCourseState.Success).courseId
            if (courseId != null) {
                selectedCollaborators.forEach { inst ->
                    viewModel.requestCollaboration(courseId, inst.id, "درخواست همکاری در دوره $title")
                }
            }
            if (editCourseId != null) {
                KelasorToast.show(
                    context = context,
                    message = "دوره با موفقیت ویرایش شد!",
                    type = ToastType.SUCCESS
                )
            } else {
                KelasorToast.show(
                    context = context,
                    message = "دوره با موفقیت ساخته شد! یک گروه اختصاصی برای دوره شما ساخته شد. به بخش پیام‌رسان مراجعه کنید.",
                    type = ToastType.SUCCESS
                )
                sharedPrefs.edit().clear().apply()
            }
            onCourseCreated(null) 
            viewModel.resetCreateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editCourseId != null) "ویرایش دوره" else "ایجاد دوره جدید",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    validationAttempted = true
                    
                    // Validation logic
                    titleError = if (title.isBlank()) "عنوان دوره الزامی است" else null
                    descriptionError = if (description.isBlank()) "توضیحات دوره الزامی است" else null
                    posterError = if (posterUrl == null) "انتخاب پوستر دوره الزامی است" else null
                    fieldOfStudyError = if (suitableForList.isEmpty()) "انتخاب حداقل یک رشته یا هشتگ الزامی است" else null

                    if (titleError != null || descriptionError != null || posterError != null || fieldOfStudyError != null) {
                        KelasorToast.show(context, "لطفاً فیلدهای الزامی را تکمیل کنید", ToastType.ERROR)
                        return@FloatingActionButton
                    }
                    if (title.isNotBlank()) {
                        val request = CreateCourseRequest(
                            title = title,
                            slogan = slogan.takeIf { it.isNotBlank() },
                            description = description.takeIf { it.isNotBlank() },
                            priceRials = (priceTomans.replace(",", "").toLongOrNull() ?: 0L) * 10,
                            discountPercentage = discountPercentage.toIntOrNull(),
                            capacity = capacity.toIntOrNull(),
                            syllabusDuration = "${syllabusDurationHours}h ${syllabusDurationMinutes}m",
                            startsAt = java.time.Instant.now().toString(), // Default to now
                            endsAt = java.time.Instant.now().plusSeconds(365 * 24 * 3600L).toString(), // Default to 1 year
                            isPublic = isPublic,
                            coverImageUrl = posterUrl,
                            tags = suitableForList,
                            suitableFor = suitableForList,
                            teacherIds = selectedInstructors.map { it.id },
                            adminIds = selectedAdmins.map { it.id },
                            collaborators = emptyList(), // Omitted; we use requestCollaboration instead
                            chapters = chaptersList,
                            manualInstructors = selectedManualInstructors.map { com.Kelasor.app.data.remote.dto.ManualInstructorDto(it.name, it.avatarUrl, it.resume) },
                            isVerticalPoster = isVerticalPoster
                        )
                        if (editCourseId != null) {
                            viewModel.updateCourse(editCourseId, request)
                        } else {
                            viewModel.createCourse(request)
                        }
                    }
                },
                containerColor = extendedColors.accent,
                contentColor = Color.White
            ) {
                if (createCourseState is CreateCourseState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Poster Section (Vertical/Portrait)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(320.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { posterPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (posterUrl != null) {
                    AsyncImage(
                        model = com.Kelasor.app.util.UrlUtils.getFullUrl(posterUrl),
                        contentDescription = "Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isUploadingPoster) {
                            CircularProgressIndicator()
                        } else {
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = if (posterError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("انتخاب پوستر دوره * (الزامی)", fontFamily = DanaFontFamily, color = if (posterError != null) MaterialTheme.colorScheme.error else Color.Unspecified)
                            if (posterError != null) {
                                Text(posterError!!, fontFamily = DanaFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; if (it.isNotBlank()) titleError = null },
                label = { Text("عنوان دوره * (الزامی)", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = titleError != null,
                supportingText = titleError?.let { { Text(it, fontFamily = DanaFontFamily) } }
            )

            OutlinedTextField(
                value = slogan,
                onValueChange = { slogan = it },
                label = { Text("شعار دوره (کوتاه)", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it; if (it.isNotBlank()) descriptionError = null },
                label = { Text("توضیحات کامل * (الزامی)", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                isError = descriptionError != null,
                supportingText = descriptionError?.let { { Text(it, fontFamily = DanaFontFamily) } }
            )
            
            Text("مدت زمان و ظرفیت", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            
            // Global date pickers removed as per user request
            // focus is now on syllabus duration and individual chapter times.
            
            var showDurationPicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = if (syllabusDurationHours.isBlank() && syllabusDurationMinutes.isBlank()) "" 
                        else "${if (syllabusDurationHours.isNotBlank()) syllabusDurationHours + " ساعت" else ""} ${if (syllabusDurationMinutes.isNotBlank()) syllabusDurationMinutes + " دقیقه" else ""}".trim(),
                onValueChange = {},
                readOnly = true,
                label = { Text("مجموع ساعت‌های دوره", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth().clickable { showDurationPicker = true },
                enabled = false,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = { Icon(Icons.Rounded.Timer, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )
            
            if (showDurationPicker) {
                AlertDialog(
                    onDismissRequest = { showDurationPicker = false },
                    title = { Text("مدت زمان دوره", fontFamily = DanaFontFamily) },
                    text = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ساعت", fontFamily = DanaFontFamily)
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { context ->
                                        android.widget.NumberPicker(context).apply {
                                            minValue = 0
                                            maxValue = 100
                                            value = syllabusDurationHours.toIntOrNull() ?: 0
                                            setOnValueChangedListener { _, _, newVal ->
                                                syllabusDurationHours = newVal.toString()
                                            }
                                        }
                                    }
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("دقیقه", fontFamily = DanaFontFamily)
                                androidx.compose.ui.viewinterop.AndroidView(
                                    factory = { context ->
                                        android.widget.NumberPicker(context).apply {
                                            minValue = 0
                                            maxValue = 59
                                            value = syllabusDurationMinutes.toIntOrNull() ?: 0
                                            setOnValueChangedListener { _, _, newVal ->
                                                syllabusDurationMinutes = newVal.toString()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDurationPicker = false }) {
                            Text("تایید", fontFamily = DanaFontFamily)
                        }
                    }
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("ظرفیت (نفر)", fontFamily = DanaFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Instructors Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("اساتید دوره", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
                TextButton(onClick = { showInstructorDialog = true }) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("افزودن مدرس", fontFamily = DanaFontFamily, fontSize = 12.sp)
                }
            }

            if (selectedInstructors.isEmpty() && selectedManualInstructors.isEmpty()) {
                Text(
                    "هیچ مدرسی اضافه نشده است",
                    fontFamily = DanaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(selectedInstructors) { user ->
                        Box {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                                AvatarImage(imageUrl = user.avatarUrl, name = user.displayName, size = AvatarSize.MEDIUM, modifier = Modifier.size(56.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                Text(user.displayName, fontFamily = DanaFontFamily, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                            }
                            Box(modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).background(MaterialTheme.colorScheme.error, CircleShape).border(1.dp, Color.White, CircleShape).clickable { selectedInstructors = selectedInstructors - user }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    items(selectedManualInstructors) { instructor ->
                        Box {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                                AvatarImage(
                                    imageUrl = instructor.avatarUrl,
                                    name = instructor.name,
                                    size = AvatarSize.MEDIUM,
                                    modifier = Modifier.size(56.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                )
                                Text(instructor.name, fontFamily = DanaFontFamily, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                                Text("(دستی)", fontFamily = DanaFontFamily, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Box(modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).background(MaterialTheme.colorScheme.error, CircleShape).border(1.dp, Color.White, CircleShape).clickable { selectedManualInstructors = selectedManualInstructors - instructor }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // Admins Section
            Text("ادمین‌های دوره", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            CollaboratorSelectionList(
                users = selectedAdmins,
                buttonLabel = "افزودن ادمین",
                emptyLabel = "هیچ ادمینی اضافه نشده است",
                onAddClick = { showAdminDialog = true },
                onRemove = { selectedAdmins = selectedAdmins - it }
            )

            // Collaborators (Academies) Section
            Text("همکاران / برگزارکنندگان (آکادمی‌ها)", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            AcademySelectionList(
                academies = selectedCollaborators,
                onAddClick = { showCollaboratorDialog = true },
                onRemove = { selectedCollaborators = selectedCollaborators - it }
            )

            Text("سرفصل‌ها و جلسات دوره", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            var showChapterStartPicker by remember { mutableStateOf(false) }
            var showChapterEndPicker by remember { mutableStateOf(false) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("تعریف جلسه جدید", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                
                OutlinedTextField(
                    value = newChapterTitle,
                    onValueChange = { newChapterTitle = it },
                    label = { Text("عنوان جلسه/سرفصل", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newChapterStartTimeDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("زمان شروع جلسه", fontFamily = DanaFontFamily, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).clickable { showChapterStartPicker = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = { Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    OutlinedTextField(
                        value = newChapterEndTimeDisplay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("زمان پایان", fontFamily = DanaFontFamily, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).clickable { showChapterEndPicker = true },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        trailingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                
                Button(
                    onClick = {
                        if (newChapterTitle.isNotBlank() && newChapterStartTimeIso != null && newChapterEndTimeIso != null) {
                            val durationMillis = java.time.Instant.parse(newChapterEndTimeIso).toEpochMilli() - java.time.Instant.parse(newChapterStartTimeIso).toEpochMilli()
                            val hours = durationMillis / (1000 * 60 * 60)
                            val minutes = (durationMillis / (1000 * 60)) % 60
                            val durationText = "${hours}h ${minutes}m"
                            
                            chaptersList = chaptersList + com.Kelasor.app.data.remote.dto.CourseChapterDto(
                                title = newChapterTitle.trim(),
                                durationText = durationText,
                                sessionStartTime = newChapterStartTimeIso,
                                sessionEndTime = newChapterEndTimeIso
                            )
                            newChapterTitle = ""
                            newChapterStartTimeDisplay = ""
                            newChapterEndTimeDisplay = ""
                            newChapterStartTimeIso = null
                            newChapterEndTimeIso = null
                        } else {
                            KelasorToast.show(context, "لطفاً عنوان و زمان شروع/پایان جلسه را وارد کنید", ToastType.ERROR)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("افزودن جلسه به لیست", fontFamily = DanaFontFamily)
                }
            }

            if (showChapterStartPicker) {
                com.Kelasor.app.ui.components.ShamsiDateTimePickerDialog(
                    initialDate = newChapterStartTimeDisplay,
                    onDateTimeSelected = { result ->
                        newChapterStartTimeDisplay = "${result.displayString} ${result.hour.toString().padStart(2,'0')}:${result.minute.toString().padStart(2,'0')}"
                        newChapterStartTimeIso = result.isoInstantString
                        showChapterStartPicker = false
                    },
                    onDismissRequest = { showChapterStartPicker = false }
                )
            }
            if (showChapterEndPicker) {
                com.Kelasor.app.ui.components.ShamsiDateTimePickerDialog(
                    initialDate = newChapterEndTimeDisplay,
                    onDateTimeSelected = { result ->
                        newChapterEndTimeDisplay = "${result.displayString} ${result.hour.toString().padStart(2,'0')}:${result.minute.toString().padStart(2,'0')}"
                        newChapterEndTimeIso = result.isoInstantString
                        showChapterEndPicker = false
                    },
                    onDismissRequest = { showChapterEndPicker = false }
                )
            }

            if (chaptersList.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    chaptersList.forEach { chapter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(chapter.title, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${chapter.sessionStartTime?.toPersianDateTime() ?: ""} تا ${chapter.sessionEndTime?.toPersianDateTime()?.split(" ")?.lastOrNull() ?: ""}",
                                        fontFamily = DanaFontFamily,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { chaptersList = chaptersList - chapter }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            
            Text("مالی و دسته‌بندی", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceDisplay,
                    onValueChange = { raw ->
                        val digits = raw.replace(",", "").filter { it.isDigit() }
                        priceTomans = digits
                        priceDisplay = if (digits.isEmpty()) "" else digits.reversed().chunked(3).joinToString(",").reversed()
                    },
                    label = { Text("هزینه (تومان)", fontFamily = DanaFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                var expandedDiscount by remember { mutableStateOf(false) }
                val discountOptions = listOf("بدون تخفیف") + (5..100 step 5).map { "$it" }

                ExposedDropdownMenuBox(
                    expanded = expandedDiscount,
                    onExpandedChange = { expandedDiscount = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = if (discountPercentage.isEmpty() || discountPercentage == "0") "بدون تخفیف" else "$discountPercentage%",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تخفیف (درصد)", fontFamily = DanaFontFamily) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDiscount) },
                        modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDiscount,
                        onDismissRequest = { expandedDiscount = false },
                        modifier = Modifier.heightIn(max = 250.dp)
                    ) {
                        discountOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(if (option == "بدون تخفیف") option else "$option%", fontFamily = DanaFontFamily) },
                                onClick = {
                                    discountPercentage = if (option == "بدون تخفیف") "" else option
                                    expandedDiscount = false
                                }
                            )
                        }
                    }
                }
            }
            // Suitable For / Tags combined - Searchable Dropdown
            Text("هشتگ و رشته‌های مناسب", fontWeight = FontWeight.Bold, fontFamily = DanaFontFamily)
            var expandedFields by remember { mutableStateOf(false) }
            val fieldsOfStudy = refState.fieldsOfStudy
            val filteredFields = fieldsOfStudy.filter { it.name.contains(fieldSearchQuery, ignoreCase = true) }
            
            @OptIn(ExperimentalMaterial3Api::class)
            ExposedDropdownMenuBox(
                expanded = expandedFields,
                onExpandedChange = { expandedFields = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fieldSearchQuery,
                    onValueChange = { 
                        fieldSearchQuery = it
                        expandedFields = true 
                    },
                    modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    label = { Text("جستجو و انتخاب رشته/هشتگ * (الزامی)", fontFamily = DanaFontFamily) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFields) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = fieldOfStudyError != null
                )
                ExposedDropdownMenu(
                    expanded = expandedFields,
                    onDismissRequest = { expandedFields = false },
                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
                ) {
                    if (fieldsOfStudy.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("در حال بارگذاری...", fontFamily = DanaFontFamily) },
                            onClick = { }
                        )
                    } else if (filteredFields.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("موردی یافت نشد", fontFamily = DanaFontFamily) },
                            onClick = { }
                        )
                    } else {
                        filteredFields.forEach { field ->
                            DropdownMenuItem(
                                text = { Text(field.name, fontFamily = DanaFontFamily) },
                                onClick = {
                                    if (field.name !in suitableForList) {
                                        suitableForList = suitableForList + field.name
                                    }
                                    fieldSearchQuery = ""
                                    expandedFields = false
                                }
                            )
                        }
                    }
                }
            }
            if (fieldOfStudyError != null) {
                Text(
                    text = fieldOfStudyError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            if (suitableForList.isNotEmpty()) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suitableForList.forEach { field ->
                        androidx.compose.material3.InputChip(
                            selected = false,
                            onClick = { suitableForList = suitableForList - field },
                            label = { Text(field, fontFamily = DanaFontFamily, fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("نمایش عمومی دوره", fontFamily = DanaFontFamily)
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
            }

            // Removed Vertical Poster Switch as requested
            
            if (createCourseState is CreateCourseState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (createCourseState as CreateCourseState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showManualInstructorDialog) {
        ManualInstructorDialog(
            onDismiss = { showManualInstructorDialog = false },
            onInstructorAdded = { instructor ->
                selectedManualInstructors = selectedManualInstructors + instructor
                showManualInstructorDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showUserSearchDialog) {
        UserSearchDialog(
            onDismiss = { showUserSearchDialog = false },
            onUserSelected = { user ->
                if (user !in selectedInstructors) {
                    selectedInstructors = selectedInstructors + user
                } else {
                    KelasorToast.show(context, "این مدرس قبلاً اضافه شده است", ToastType.ERROR)
                }
                showUserSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showCollaboratorDialog) {
        AcademySearchDialog(
            onDismiss = { showCollaboratorDialog = false },
            onAcademySelected = { academy ->
                if (selectedCollaborators.any { it.id == academy.id }) {
                    KelasorToast.show(context, "این آکادمی قبلاً اضافه شده است", ToastType.ERROR)
                } else {
                    selectedCollaborators = selectedCollaborators + academy
                    showCollaboratorDialog = false
                }
            },
            viewModel = viewModel
        )
    }

    if (showInstructorDialog) {
        AlertDialog(
            onDismissRequest = { showInstructorDialog = false },
            title = { Text("انتخاب اساتید آکادمی", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                if (academyInstructors.isEmpty()) {
                    Text("هیچ استادی در آکادمی شما ثبت نشده است.", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(academyInstructors) { instructor ->
                            val isAlreadySelected = instructor in selectedInstructors
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedInstructors.any { it.id == instructor.id }) {
                                            KelasorToast.show(context, "این مدرس قبلاً اضافه شده است", ToastType.ERROR)
                                        } else {
                                            selectedInstructors = selectedInstructors + instructor
                                            showInstructorDialog = false
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                                    .then(if (isAlreadySelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)) else Modifier),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.Kelasor.app.ui.components.AvatarImage(imageUrl = instructor.avatarUrl, name = instructor.displayName, size = com.Kelasor.app.ui.components.AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(instructor.displayName, fontFamily = DanaFontFamily, modifier = Modifier.weight(1f))
                                if (isAlreadySelected) {
                                    Icon(Icons.Default.Check, contentDescription = "انتخاب شده", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showInstructorDialog = false
                    showAddInstructorTypeDialog = true
                }) {
                    Text("افزودن مدرس جدید", fontFamily = DanaFontFamily, color = extendedColors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInstructorDialog = false }) {
                    Text("بستن", fontFamily = DanaFontFamily)
                }
            }
        )
    }

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("انتخاب ادمین‌های آکادمی", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                if (academyAdmins.isEmpty()) {
                    Text("هیچ ادمینی در آکادمی شما ثبت نشده است.", fontFamily = DanaFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(academyAdmins) { admin ->
                            val isAlreadySelected = admin in selectedAdmins
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (selectedAdmins.any { it.id == admin.id }) {
                                            KelasorToast.show(context, "این ادمین قبلاً اضافه شده است", ToastType.ERROR)
                                        } else {
                                            selectedAdmins = selectedAdmins + admin
                                            showAdminDialog = false
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                                    .then(if (isAlreadySelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)) else Modifier),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.Kelasor.app.ui.components.AvatarImage(imageUrl = admin.avatarUrl, name = admin.displayName, size = com.Kelasor.app.ui.components.AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(admin.displayName, fontFamily = DanaFontFamily, modifier = Modifier.weight(1f))
                                if (isAlreadySelected) {
                                    Icon(Icons.Default.Check, contentDescription = "انتخاب شده", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("بستن", fontFamily = DanaFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAdminDialog = false
                    onNavigateToEditAcademyProfile() 
                }) {
                    Text("افزودن ادمین جدید", fontFamily = DanaFontFamily, color = extendedColors.accent)
                }
            }
        )
    }
    if (showAddInstructorTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddInstructorTypeDialog = false },
            title = { Text("افزودن مدرس", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showAddInstructorTypeDialog = false
                            showUserSearchDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("جستجو در کاربران کلاسور (با آیدی)", fontFamily = DanaFontFamily)
                    }
                    TextButton(
                        onClick = {
                            showAddInstructorTypeDialog = false
                            showManualInstructorDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("افزودن مدرس مهمان (دستی)", fontFamily = DanaFontFamily)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddInstructorTypeDialog = false }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }

    if (showUserSearchDialog) {
        UserSearchDialogForCourse(
            onDismiss = { showUserSearchDialog = false },
            onUserSelected = { user ->
                if (selectedInstructors.none { it.id == user.id }) {
                    selectedInstructors = selectedInstructors + user
                }
                showUserSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showAdminSearchDialog) {
        UserSearchDialogForCourse(
            onDismiss = { showAdminSearchDialog = false },
            onUserSelected = { user ->
                if (selectedAdmins.none { it.id == user.id }) {
                    selectedAdmins = selectedAdmins + user
                }
                showAdminSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showManualInstructorDialog) {
        ManualInstructorDialog(
            onDismiss = { showManualInstructorDialog = false },
            onInstructorAdded = { instructor ->
                if (selectedManualInstructors.none { it.name == instructor.name }) {
                    selectedManualInstructors = selectedManualInstructors + instructor
                }
                showManualInstructorDialog = false
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun UserSearchDialogForCourse(
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    viewModel: CourseViewModel
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "جستجوی کاربر",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = query,
                    onValueChange = { 
                        query = it
                        viewModel.searchUsers(it)
                    },
                    label = { Text("شناسه یا نام کاربری", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (query.length < 3) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("حداقل ۳ حرف وارد کنید", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else if (searchResults.isEmpty() && !isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("کاربری یافت نشد", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        searchResults.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(imageUrl = user.avatarUrl, name = user.displayName, size = AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.displayName, fontFamily = DanaFontFamily, fontSize = 14.sp)
                                    if (!user.username.isNullOrEmpty()) {
                                        Text("@${user.username}", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
fun CollaboratorSelectionList(
    users: List<User>,
    buttonLabel: String = "افزودن",
    emptyLabel: String = "هیچ همکاری اضافه نشده است",
    onAddClick: () -> Unit,
    onRemove: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onAddClick) {
                Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(buttonLabel, fontFamily = DanaFontFamily, fontSize = 12.sp)
            }
        }
        
        if (users.isEmpty()) {
            Text(
                emptyLabel,
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(users) { user ->
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(70.dp)
                        ) {
                            AvatarImage(
                                imageUrl = user.avatarUrl,
                                name = user.displayName,
                                size = AvatarSize.MEDIUM,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            Text(
                                user.displayName,
                                fontFamily = DanaFontFamily,
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .clickable { onRemove(user) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchDialog(
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    viewModel: CourseViewModel
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "جستجوی همکار",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = query,
                    onValueChange = { 
                        query = it
                        viewModel.searchUsers(it)
                    },
                    label = { Text("شناسه یا نام کاربری", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (query.length < 3) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("حداقل ۳ حرف وارد کنید", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else if (searchResults.isEmpty() && !isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("کاربری یافت نشد", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        searchResults.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(user) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(
                                    imageUrl = user.avatarUrl,
                                    name = user.displayName,
                                    size = AvatarSize.SMALL,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.displayName, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("@${user.username}", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademySelectionList(
    academies: List<com.Kelasor.app.domain.model.Institution>,
    onAddClick: () -> Unit,
    onRemove: (com.Kelasor.app.domain.model.Institution) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onAddClick) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن آکادمی / موسسه", fontFamily = DanaFontFamily, fontSize = 12.sp)
            }
        }
        
        if (academies.isEmpty()) {
            Text(
                "هیچ آکادمی انتخاب نشده است",
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(academies) { academy ->
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(70.dp)
                        ) {
                            AvatarImage(
                                imageUrl = academy.logoUrl,
                                name = academy.name,
                                size = AvatarSize.MEDIUM,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            Text(
                                academy.name,
                                fontFamily = DanaFontFamily,
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .clickable { onRemove(academy) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademySearchDialog(
    onDismiss: () -> Unit,
    onAcademySelected: (com.Kelasor.app.domain.model.Institution) -> Unit,
    viewModel: CourseViewModel
) {
    var query by remember { mutableStateOf("") }
    val activeInstitutions by viewModel.activeInstitutions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.searchInstitutions("")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "انتخاب آکادمی",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = query,
                    onValueChange = { 
                        query = it
                        viewModel.searchInstitutions(it)
                    },
                    label = { Text("نام آکادمی...", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (activeInstitutions.isEmpty() && !isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("آکادمی یافت نشد", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        activeInstitutions.forEach { academy ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAcademySelected(academy) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(
                                    imageUrl = academy.logoUrl,
                                    name = academy.name,
                                    size = AvatarSize.SMALL,
                                    modifier = Modifier.size(40.dp).clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(academy.name, fontFamily = DanaFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
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
fun ManualInstructorSelectionList(
    instructors: List<com.Kelasor.app.domain.model.ManualInstructor>,
    onAddClick: () -> Unit,
    onRemove: (com.Kelasor.app.domain.model.ManualInstructor) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن مدرس مهمان", fontFamily = DanaFontFamily, fontSize = 12.sp)
            }
        }
        
        if (instructors.isEmpty()) {
            Text(
                "هیچ مدرس مهمانی اضافه نشده است",
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(instructors) { instructor ->
                    Box {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(70.dp)
                        ) {
                            AvatarImage(
                                imageUrl = instructor.avatarUrl,
                                name = instructor.name,
                                size = AvatarSize.MEDIUM,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            Text(
                                instructor.name,
                                fontFamily = DanaFontFamily,
                                fontSize = 10.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                .border(1.dp, Color.White, CircleShape)
                                .clickable { onRemove(instructor) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Remove, 
                                contentDescription = null, 
                                tint = Color.White, 
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualInstructorDialog(
    onDismiss: () -> Unit,
    onInstructorAdded: (com.Kelasor.app.domain.model.ManualInstructor) -> Unit,
    viewModel: CourseViewModel
) {
    var name by remember { mutableStateOf("") }
    var resume by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isUploading = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val result = viewModel.uploadPoster(tempFile)
                    if (result.isSuccess) {
                        avatarUrl = result.getOrThrow()
                    }
                    isUploading = false
                    tempFile.delete()
                } catch (e: Exception) {
                    isUploading = false
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "افزودن مدرس دستی",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp).align(Alignment.Start)
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = com.Kelasor.app.util.UrlUtils.getFullUrl(avatarUrl),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام مدرس", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = resume,
                    onValueChange = { resume = it },
                    label = { Text("رزومه کوتاه", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", fontFamily = DanaFontFamily)
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onInstructorAdded(com.Kelasor.app.domain.model.ManualInstructor(name, avatarUrl, resume.takeIf { it.isNotBlank() }))
                            }
                        },
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("افزودن", fontFamily = DanaFontFamily)
                    }
                }
            }
        }
    }
}
