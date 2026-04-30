package com.Kelasor.app.ui.screens.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.CourseViewModel
import com.Kelasor.app.ui.viewmodel.CreateCourseState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.Kelasor.app.data.remote.dto.CreateCourseRequest
import com.Kelasor.app.domain.model.User
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.ArrowDropDown

import com.Kelasor.app.ui.viewmodel.ReferenceDataViewModel

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

    
    var title by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceRials by remember { mutableStateOf("") }
    var priceDisplay by remember { mutableStateOf("") }
    var discountPercentage by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var syllabusDurationHours by remember { mutableStateOf("") }
    var syllabusDurationMinutes by remember { mutableStateOf("") }
    var startsAtDisplay by remember { mutableStateOf("") }
    var endsAtDisplay by remember { mutableStateOf("") }
    var startsAtIso by remember { mutableStateOf("") }
    var endsAtIso by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var posterUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingPoster by remember { mutableStateOf(false) }
    var tagsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentTagInput by remember { mutableStateOf("") }
    var selectedCollaborators by remember { mutableStateOf<List<com.Kelasor.app.domain.model.Institution>>(emptyList()) }
    var selectedInstructors by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedAdmins by remember { mutableStateOf<List<User>>(emptyList()) }
    var showUserSearchDialog by remember { mutableStateOf(false) }
    var showInstructorDialog by remember { mutableStateOf(false) }
    var showCollaboratorDialog by remember { mutableStateOf(false) }
    var activeSearchTarget by remember { mutableStateOf("ADMIN") } // ADMIN or INSTRUCTOR
    var suitableForList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isVerticalPoster by remember { mutableStateOf(false) }
    var chaptersList by remember { mutableStateOf<List<com.Kelasor.app.data.remote.dto.CourseChapterDto>>(emptyList()) }
    var fieldSearchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(loadedCourse) {
        loadedCourse?.let { course ->
            title = course.title
            slogan = course.slogan ?: ""
            description = course.description ?: ""
            priceRials = course.priceRials.toString()
            priceDisplay = String.format("%,d", course.priceRials)
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
            chaptersList = course.chapters.map { com.Kelasor.app.data.remote.dto.CourseChapterDto(it.title, it.durationText) }
            
            // Dates
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(java.time.ZoneId.systemDefault())
            startsAtIso = course.startsAt.toString()
            startsAtDisplay = formatter.format(course.startsAt)
            endsAtIso = course.endsAt.toString()
            endsAtDisplay = formatter.format(course.endsAt)
            
            // We can't directly load collaborators since they are a List<String> of names/IDs
            // but we can at least avoid overwriting them
        }
    }
    
    val context = LocalContext.current
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
                        fontFamily = VazirFontFamily,
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
                    if (title.isNotBlank()) {
                        val request = CreateCourseRequest(
                            title = title,
                            slogan = slogan.takeIf { it.isNotBlank() },
                            description = description.takeIf { it.isNotBlank() },
                            priceRials = priceRials.replace(",", "").toLongOrNull() ?: 0L,
                            discountPercentage = discountPercentage.toIntOrNull(),
                            capacity = capacity.toIntOrNull(),
                            syllabusDuration = "${syllabusDurationHours}h ${syllabusDurationMinutes}m",
                            startsAt = startsAtIso.takeIf { it.isNotBlank() } ?: java.time.Instant.now().toString(),
                            endsAt = endsAtIso.takeIf { it.isNotBlank() } ?: java.time.Instant.now().plusSeconds(30 * 24 * 3600L).toString(),
                            isPublic = isPublic,
                            coverImageUrl = posterUrl,
                            tags = tagsList,
                            suitableFor = suitableForList,
                            teacherIds = selectedInstructors.map { it.id },
                            adminIds = selectedAdmins.map { it.id },
                            collaborators = emptyList(), // Omitted; we use requestCollaboration instead
                            chapters = chaptersList,
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
                            Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp))
                            Text("انتخاب پوستر دوره", fontFamily = VazirFontFamily)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("عنوان دوره", fontFamily = VazirFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = slogan,
                onValueChange = { slogan = it },
                label = { Text("شعار دوره (کوتاه)", fontFamily = VazirFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات کامل", fontFamily = VazirFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
            
            Text("زمان‌بندی و ظرفیت", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            
            var showStartsAtPicker by remember { mutableStateOf(false) }
            var showEndsAtPicker by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startsAtDisplay,
                    onValueChange = {},
                    label = { Text("تاریخ شروع (شمسی)", fontFamily = VazirFontFamily) },
                    modifier = Modifier.weight(1f).clickable { showStartsAtPicker = true },
                    singleLine = true,
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = endsAtDisplay,
                    onValueChange = {},
                    label = { Text("تاریخ پایان", fontFamily = VazirFontFamily) },
                    modifier = Modifier.weight(1f).clickable { showEndsAtPicker = true },
                    singleLine = true,
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            if (showStartsAtPicker) {
                com.Kelasor.app.ui.components.ShamsiDateTimePickerDialog(
                    initialDate = startsAtDisplay,
                    onDateTimeSelected = { result ->
                        startsAtDisplay = "${result.displayString} ${result.hour.toString().padStart(2,'0')}:${result.minute.toString().padStart(2,'0')}"
                        startsAtIso = result.isoInstantString
                        showStartsAtPicker = false
                    },
                    onDismissRequest = { showStartsAtPicker = false }
                )
            }
            if (showEndsAtPicker) {
                com.Kelasor.app.ui.components.ShamsiDateTimePickerDialog(
                    initialDate = endsAtDisplay,
                    onDateTimeSelected = { result ->
                        endsAtDisplay = "${result.displayString} ${result.hour.toString().padStart(2,'0')}:${result.minute.toString().padStart(2,'0')}"
                        endsAtIso = result.isoInstantString
                        showEndsAtPicker = false
                    },
                    onDismissRequest = { showEndsAtPicker = false }
                )
            }
            
            var showDurationPicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = if (syllabusDurationHours.isBlank() && syllabusDurationMinutes.isBlank()) "" 
                        else "${if (syllabusDurationHours.isNotBlank()) syllabusDurationHours + " ساعت" else ""} ${if (syllabusDurationMinutes.isNotBlank()) syllabusDurationMinutes + " دقیقه" else ""}".trim(),
                onValueChange = {},
                readOnly = true,
                label = { Text("مدت زمان دوره", fontFamily = VazirFontFamily) },
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
                    title = { Text("مدت زمان دوره", fontFamily = VazirFontFamily) },
                    text = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ساعت", fontFamily = VazirFontFamily)
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
                                Text("دقیقه", fontFamily = VazirFontFamily)
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
                            Text("تایید", fontFamily = VazirFontFamily)
                        }
                    }
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("ظرفیت (نفر)", fontFamily = VazirFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Instructors Section
            Text("اساتید دوره", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            CollaboratorSelectionList(
                users = selectedInstructors,
                buttonLabel = "افزودن مدرس",
                emptyLabel = "هیچ مدرسی اضافه نشده است",
                onAddClick = { showInstructorDialog = true },
                onRemove = { selectedInstructors = selectedInstructors - it }
            )

            // Admins Section
            Text("ادمین‌های دوره", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            CollaboratorSelectionList(
                users = selectedAdmins,
                buttonLabel = "افزودن ادمین",
                emptyLabel = "هیچ ادمینی اضافه نشده است",
                onAddClick = { onNavigateToEditAcademyProfile() },
                onRemove = { selectedAdmins = selectedAdmins - it }
            )

            // Collaborators (Academies) Section
            Text("همکاران / برگزارکنندگان (آکادمی‌ها)", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            AcademySelectionList(
                academies = selectedCollaborators,
                onAddClick = { showCollaboratorDialog = true },
                onRemove = { selectedCollaborators = selectedCollaborators - it }
            )
            
            // Chapters Section
            Text("سرفصل‌های دوره", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            var newChapterTitle by remember { mutableStateOf("") }
            var newChapterDuration by remember { mutableStateOf("") }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newChapterTitle,
                    onValueChange = { newChapterTitle = it },
                    label = { Text("عنوان سرفصل", fontFamily = VazirFontFamily) },
                    modifier = Modifier.weight(2f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = newChapterDuration,
                    onValueChange = { newChapterDuration = it },
                    label = { Text("زمان (مثلا: ۲ ساعت)", fontFamily = VazirFontFamily, fontSize = 10.sp) },
                    modifier = Modifier.weight(1.5f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(
                    onClick = {
                        if (newChapterTitle.isNotBlank() && newChapterDuration.isNotBlank()) {
                            chaptersList = chaptersList + com.Kelasor.app.data.remote.dto.CourseChapterDto(newChapterTitle.trim(), newChapterDuration.trim())
                            newChapterTitle = ""
                            newChapterDuration = ""
                        }
                    },
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                }
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
                            Column {
                                Text(chapter.title, fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium)
                                Text(chapter.durationText, fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { chaptersList = chaptersList - chapter }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
            
            Text("مالی و دسته‌بندی", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceDisplay,
                    onValueChange = { raw ->
                        val digits = raw.replace(",", "").filter { it.isDigit() }
                        priceRials = digits
                        priceDisplay = if (digits.isEmpty()) "" else digits.reversed().chunked(3).joinToString(",").reversed()
                    },
                    label = { Text("هزینه (تومان)", fontFamily = VazirFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = discountPercentage,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }
                        val parsed = digits.toIntOrNull()
                        discountPercentage = when {
                            digits.isEmpty() -> ""
                            parsed != null && parsed > 100 -> "100"
                            else -> digits
                        }
                    },
                    label = { Text("تخفیف (درصد)", fontFamily = VazirFontFamily) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            // Tags with Enter/+ (chip-based)
            Text("هشتگ‌ها", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = currentTagInput,
                    onValueChange = { currentTagInput = it },
                    label = { Text("هشتگ جدید", fontFamily = VazirFontFamily) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            if (currentTagInput.isNotBlank() && currentTagInput !in tagsList) {
                                tagsList = tagsList + currentTagInput.trim()
                                currentTagInput = ""
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                IconButton(
                    onClick = {
                        if (currentTagInput.isNotBlank() && currentTagInput !in tagsList) {
                            tagsList = tagsList + currentTagInput.trim()
                            currentTagInput = ""
                        }
                    },
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            if (tagsList.isNotEmpty()) {
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tagsList.forEach { tag ->
                        androidx.compose.material3.InputChip(
                            selected = false,
                            onClick = { tagsList = tagsList - tag },
                            label = { Text(tag, fontFamily = VazirFontFamily, fontSize = 12.sp) },
                            trailingIcon = { Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
            // Suitable For - Searchable Dropdown
            Text("رشته‌های مناسب", fontWeight = FontWeight.Bold, fontFamily = VazirFontFamily)
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
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("جستجو و انتخاب رشته...", fontFamily = VazirFontFamily) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFields) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = expandedFields,
                    onDismissRequest = { expandedFields = false },
                    modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
                ) {
                    if (fieldsOfStudy.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("در حال بارگذاری...", fontFamily = VazirFontFamily) },
                            onClick = { }
                        )
                    } else if (filteredFields.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("رشته‌ای یافت نشد", fontFamily = VazirFontFamily) },
                            onClick = { }
                        )
                    } else {
                        filteredFields.forEach { field ->
                            DropdownMenuItem(
                                text = { Text(field.name, fontFamily = VazirFontFamily) },
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
                            label = { Text(field, fontFamily = VazirFontFamily, fontSize = 12.sp) },
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
                Text("نمایش عمومی دوره", fontFamily = VazirFontFamily)
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("پوستر عمودی (اینستاگرامی)", fontFamily = VazirFontFamily)
                Switch(
                    checked = isVerticalPoster,
                    onCheckedChange = { isVerticalPoster = it }
                )
            }
            
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

    if (showUserSearchDialog) {
        UserSearchDialog(
            onDismiss = { showUserSearchDialog = false },
            onUserSelected = { user ->
                if (activeSearchTarget == "ADMIN") {
                    if (user !in selectedAdmins) {
                        selectedAdmins = selectedAdmins + user
                    } else {
                        android.widget.Toast.makeText(context, "این ادمین قبلاً اضافه شده است", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else if (activeSearchTarget == "INSTRUCTOR") {
                    if (user !in selectedInstructors) {
                        selectedInstructors = selectedInstructors + user
                    } else {
                        android.widget.Toast.makeText(context, "این مدرس قبلاً اضافه شده است", android.widget.Toast.LENGTH_SHORT).show()
                    }
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
                if (academy !in selectedCollaborators) {
                    selectedCollaborators = selectedCollaborators + academy
                } else {
                    android.widget.Toast.makeText(context, "این آکادمی قبلاً اضافه شده است", android.widget.Toast.LENGTH_SHORT).show()
                }
                showCollaboratorDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showInstructorDialog) {
        AlertDialog(
            onDismissRequest = { showInstructorDialog = false },
            title = { Text("انتخاب اساتید آکادمی", fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                if (academyInstructors.isEmpty()) {
                    Text("هیچ استادی در آکادمی شما ثبت نشده است.", fontFamily = VazirFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(academyInstructors) { instructor ->
                            val isAlreadySelected = instructor in selectedInstructors
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isAlreadySelected) {
                                            selectedInstructors = selectedInstructors + instructor
                                            showInstructorDialog = false
                                        } else {
                                            android.widget.Toast.makeText(context, "این مدرس قبلاً اضافه شده است", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                                    .then(if (isAlreadySelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)) else Modifier),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.Kelasor.app.ui.components.AvatarImage(imageUrl = instructor.avatarUrl, name = instructor.displayName, size = com.Kelasor.app.ui.components.AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(instructor.displayName, fontFamily = VazirFontFamily, modifier = Modifier.weight(1f))
                                if (isAlreadySelected) {
                                    Icon(Icons.Default.Check, contentDescription = "انتخاب شده", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructorDialog = false }) {
                    Text("بستن", fontFamily = VazirFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showInstructorDialog = false
                    onNavigateToEditAcademyProfile() 
                }) {
                    Text("افزودن مدرس جدید", fontFamily = VazirFontFamily, color = extendedColors.accent)
                }
            }
        )
    }

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("انتخاب ادمین‌های آکادمی", fontFamily = VazirFontFamily, fontWeight = FontWeight.Bold) },
            text = {
                if (academyAdmins.isEmpty()) {
                    Text("هیچ ادمینی در آکادمی شما ثبت نشده است.", fontFamily = VazirFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn {
                        items(academyAdmins) { admin ->
                            val isAlreadySelected = admin in selectedAdmins
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!isAlreadySelected) {
                                            selectedAdmins = selectedAdmins + admin
                                            showAdminDialog = false
                                        } else {
                                            android.widget.Toast.makeText(context, "این ادمین قبلاً اضافه شده است", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(vertical = 8.dp)
                                    .then(if (isAlreadySelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp)) else Modifier),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.Kelasor.app.ui.components.AvatarImage(imageUrl = admin.avatarUrl, name = admin.displayName, size = com.Kelasor.app.ui.components.AvatarSize.SMALL)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(admin.displayName, fontFamily = VazirFontFamily, modifier = Modifier.weight(1f))
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
                    Text("بستن", fontFamily = VazirFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAdminDialog = false
                    onNavigateToEditAcademyProfile() 
                }) {
                    Text("افزودن ادمین جدید", fontFamily = VazirFontFamily, color = extendedColors.accent)
                }
            }
        )
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
                Text(buttonLabel, fontFamily = VazirFontFamily, fontSize = 12.sp)
            }
        }
        
        if (users.isEmpty()) {
            Text(
                emptyLabel,
                fontFamily = VazirFontFamily,
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
                                fontFamily = VazirFontFamily,
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
                    fontFamily = VazirFontFamily,
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
                    label = { Text("شناسه یا نام کاربری", fontFamily = VazirFontFamily) },
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
                        Text("حداقل ۳ حرف وارد کنید", fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else if (searchResults.isEmpty() && !isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("کاربری یافت نشد", fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
                                    Text(user.displayName, fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text("@${user.username}", fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("افزودن آکادمی / موسسه", fontFamily = VazirFontFamily, fontSize = 12.sp)
            }
        }
        
        if (academies.isEmpty()) {
            Text(
                "هیچ آکادمی انتخاب نشده است",
                fontFamily = VazirFontFamily,
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
                                fontFamily = VazirFontFamily,
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
                    fontFamily = VazirFontFamily,
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
                    label = { Text("نام آکادمی...", fontFamily = VazirFontFamily) },
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
                        Text("آکادمی یافت نشد", fontFamily = VazirFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
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
                                    Text(academy.name, fontFamily = VazirFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
