package com.Kelasor.app.ui.screens.profile

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.Channel
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.ChannelListViewModel
import com.Kelasor.app.ui.viewmodel.ProfileEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.data.ProvincesData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    channelViewModel: ChannelListViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.state.collectAsState()
    val channelState by channelViewModel.state.collectAsState()
    val user = profileState.user
    val extendedColors = MessageAppTheme.extendedColors
    val context = LocalContext.current

    // Local URI for immediate preview after picking
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }

    // Avatar picker launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedAvatarUri = it
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = java.io.File.createTempFile("avatar_", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                profileViewModel.uploadAvatar(tempFile)
            } catch (e: Exception) {
                selectedAvatarUri = null
                android.widget.Toast.makeText(context, "خطا در بارگذاری تصویر", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load channels & reference data on mount
    LaunchedEffect(Unit) {
        channelViewModel.loadChannels()
        profileViewModel.loadReferenceData()
    }

    // Observe avatar upload events
    LaunchedEffect(Unit) {
        profileViewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.AvatarUploaded -> {
                    android.widget.Toast.makeText(context, "تصویر پروفایل بروزرسانی شد", android.widget.Toast.LENGTH_SHORT).show()
                }
                is ProfileEvent.Error -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
    
    // Admin Channels for Bio Selection
    val adminChannels = remember(channelState.channels) {
        channelState.channels.filter { it.isAdmin }
    }

    // Form State
    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    
    var university by remember(user) { mutableStateOf(user?.university ?: "") }
    var fieldOfStudy by remember(user) { mutableStateOf(user?.fieldOfStudy ?: "") }
    var education by remember(user) { mutableStateOf(user?.education ?: "") }
    var skills by remember(user) { mutableStateOf(user?.skills ?: "") }
    var interests by remember(user) { mutableStateOf(user?.interests ?: "") }
    var workExperience by remember(user) { mutableStateOf(user?.workExperience ?: "") }
    var achievements by remember(user) { mutableStateOf(user?.achievements ?: "") }
    
    // Teacher fields
    var isTeacher by remember(user) { mutableStateOf(user?.isTeacher ?: false) }
    var teachingField by remember(user) { mutableStateOf(user?.teachingField ?: "") }
    var teachingUniversity by remember(user) { mutableStateOf(user?.teachingUniversity ?: "") }
    // Location fields
    var province by remember(user) { mutableStateOf(user?.province ?: "") }
    var city by remember(user) { mutableStateOf(user?.city ?: "") }
    // Pre-load cities if user already has a province set
    LaunchedEffect(user?.province) {
        val existingProvince: String? = user?.province
        if (!existingProvince.isNullOrBlank()) {
            profileViewModel.loadCities(existingProvince)
        }
    }
    
    var bioChannelId1 by remember(user) { mutableStateOf(user?.bioChannelId1) }
    var bioChannelId2 by remember(user) { mutableStateOf(user?.bioChannelId2) }

    // Dropdown expanded states
    var universityExpanded by remember { mutableStateOf(false) }
    var fieldOfStudyExpanded by remember { mutableStateOf(false) }
    var educationExpanded by remember { mutableStateOf(false) }
    var teachingFieldExpanded by remember { mutableStateOf(false) }
    var teachingUniversityExpanded by remember { mutableStateOf(false) }
    var provinceExpanded by remember { mutableStateOf(false) }
    var cityExpanded by remember { mutableStateOf(false) }

    // Channel Selection Sheets
    var showChannelSheet1 by remember { mutableStateOf(false) }
    var showChannelSheet2 by remember { mutableStateOf(false) }

    // Profile completion calculation
    val profileCompletionPercent = remember(user, university, fieldOfStudy, education, isTeacher) {
        var filled = 0
        var total = 6
        if (!user?.displayName.isNullOrBlank()) filled++
        if (!user?.bio.isNullOrBlank()) filled++
        if (university.isNotBlank()) filled++
        if (fieldOfStudy.isNotBlank()) filled++
        if (education.isNotBlank()) filled++
        if (!user?.avatarUrl.isNullOrBlank()) filled++
        (filled * 100) / total
    }

    // Handle Save Success
    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            onNavigateBack()
            profileViewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش پروفایل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp).size(24.dp))
                    } else {
                        IconButton(onClick = {
                            profileViewModel.updateProfile(
                                username = username,
                                displayName = displayName,
                                bio = bio,
                                university = university,
                                fieldOfStudy = fieldOfStudy,
                                education = education,
                                skills = skills,
                                interests = interests,
                                workExperience = workExperience,
                                achievements = achievements,
                                bioChannelId1 = bioChannelId1,
                                bioChannelId2 = bioChannelId2,
                                isTeacher = isTeacher,
                                teachingField = teachingField.ifBlank { null },
                                teachingUniversity = teachingUniversity.ifBlank { null },
                                province = province.ifBlank { null },
                                city = city.ifBlank { null }
                            )
                        }) {
                            Icon(Icons.Default.Check, "Save", tint = extendedColors.accent)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Completion Banner
            if (profileCompletionPercent < 100) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF6C63FF),
                                            Color(0xFF4ECDC4)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        "پروفایل خود را تکمیل کنید!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { profileCompletionPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "%$profileCompletionPercent تکمیل شده",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // Avatar Upload Section
            item {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    val displayImageUri: Any? = selectedAvatarUri
                        ?: user?.avatarUrl?.let { com.Kelasor.app.util.UrlUtils.getFullUrl(it) }
                    if (displayImageUri != null && (displayImageUri is Uri || (displayImageUri is String && displayImageUri.isNotBlank()))) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(displayImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "پروفایل",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable {
                                    avatarPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AvatarImage(
                            imageUrl = null,
                            name = user?.displayName ?: "?",
                            size = com.Kelasor.app.ui.components.AvatarSize.LARGE,
                            modifier = Modifier
                                .size(130.dp)
                                .clickable {
                                    avatarPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        color = extendedColors.accent,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "تغییر تصویر",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "تغییر تصویر پروفایل",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.accent
                )
            }

            // Basic Info Section
            item {
                SectionHeader("اطلاعات پایه", Icons.Default.Person)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("نام نمایشی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '.' }
                    },
                    label = { Text("نام کاربری (@)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = username.isNotEmpty() && username.length < 3,
                    supportingText = {
                        when {
                            username.isEmpty() -> Text("حداقل ۳ کاراکتر، فقط حروف انگلیسی و عدد")
                            username.length < 3 -> Text("نام کاربری باید حداقل ۳ کاراکتر باشد", color = MaterialTheme.colorScheme.error)
                            else -> Text("✓ فرمت صحیح", color = Color(0xFF4CAF50))
                        }
                    },
                    leadingIcon = {
                        Text("@", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (username.length >= 3) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (username.length >= 3) Color(0xFF4CAF50).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("بیوگرافی") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Education Section with Dropdowns
            item {
                SectionHeader("تحصیلات و مهارت‌ها", Icons.Default.School)
                Spacer(modifier = Modifier.height(8.dp))
                
                // University Dropdown
                ExposedDropdownMenuBox(
                    expanded = universityExpanded,
                    onExpandedChange = { universityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = { Text("دانشگاه") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = universityExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    val filteredUniversities = remember(university, profileState.universities) {
                        if (university.isBlank()) profileState.universities
                        else profileState.universities.filter { it.name.contains(university, ignoreCase = true) }
                    }
                    if (filteredUniversities.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = universityExpanded,
                            onDismissRequest = { universityExpanded = false }
                        ) {
                            filteredUniversities.take(15).forEach { uni ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(uni.name, fontWeight = FontWeight.Medium)
                                            if (uni.city != null) {
                                                Text(
                                                    uni.city,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        university = uni.name
                                        universityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Education Level Dropdown
                ExposedDropdownMenuBox(
                    expanded = educationExpanded,
                    onExpandedChange = { educationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = { Text("مقطع تحصیلی") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        readOnly = profileState.educationLevels.isNotEmpty(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = educationExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    if (profileState.educationLevels.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = educationExpanded,
                            onDismissRequest = { educationExpanded = false }
                        ) {
                            profileState.educationLevels.forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name) },
                                    onClick = {
                                        education = level.name
                                        educationExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Field of Study Dropdown
                ExposedDropdownMenuBox(
                    expanded = fieldOfStudyExpanded,
                    onExpandedChange = { fieldOfStudyExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fieldOfStudy,
                        onValueChange = { fieldOfStudy = it },
                        label = { Text("رشته تحصیلی") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldOfStudyExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    val filteredFields = remember(fieldOfStudy, profileState.fieldsOfStudy) {
                        if (fieldOfStudy.isBlank()) profileState.fieldsOfStudy
                        else profileState.fieldsOfStudy.filter { it.name.contains(fieldOfStudy, ignoreCase = true) }
                    }
                    if (filteredFields.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = fieldOfStudyExpanded,
                            onDismissRequest = { fieldOfStudyExpanded = false }
                        ) {
                            filteredFields.take(15).forEach { field ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(field.name, fontWeight = FontWeight.Medium)
                                        }
                                    },
                                    onClick = {
                                        fieldOfStudy = field.name
                                        fieldOfStudyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // ── Location Section ──
                SectionHeader("موقعیت مکانی", Icons.Default.LocationOn)
                Spacer(modifier = Modifier.height(8.dp))

                // Province Dropdown
                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = { },
                        label = { Text("استان") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false }
                    ) {
                        profileState.provinces.forEach { provinceName ->
                            DropdownMenuItem(
                                text = { Text(provinceName) },
                                onClick = {
                                    province = provinceName
                                    city = "" // Reset city when province changes
                                    provinceExpanded = false
                                    profileViewModel.loadCities(provinceName)
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // City Dropdown (filtered by selected province)
                val citiesForProvince: List<String> = profileState.cities
                ExposedDropdownMenuBox(
                    expanded = cityExpanded,
                    onExpandedChange = { if (citiesForProvince.isNotEmpty()) cityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { },
                        label = { Text("شهر") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        readOnly = true,
                        enabled = citiesForProvince.isNotEmpty(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    if (citiesForProvince.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = cityExpanded,
                            onDismissRequest = { cityExpanded = false }
                        ) {
                            citiesForProvince.forEach { cityName ->
                                DropdownMenuItem(
                                    text = { Text(cityName) },
                                    onClick = {
                                        city = cityName
                                        cityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("مهارت‌ها (با کاما جدا کنید)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("علاقه‌مندی‌ها") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Teacher Role Section
            item {
                SectionHeader("نقش استاد", Icons.Default.School)
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTeacher) Color(0xFF6C63FF).copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "من استاد هستم",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "با فعال کردن این گزینه، می‌توانید به عنوان استاد شناخته شوید",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isTeacher,
                                onCheckedChange = { isTeacher = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF6C63FF)
                                )
                            )
                        }
                        
                        AnimatedVisibility(visible = isTeacher) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ExposedDropdownMenuBox(
                                    expanded = teachingFieldExpanded,
                                    onExpandedChange = { teachingFieldExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = teachingField,
                                        onValueChange = { teachingField = it },
                                        label = { Text("رشته تدریس") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teachingFieldExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )
                                    val filteredTeachFields = remember(teachingField, profileState.fieldsOfStudy) {
                                        if (teachingField.isBlank()) profileState.fieldsOfStudy
                                        else profileState.fieldsOfStudy.filter { it.name.contains(teachingField, ignoreCase = true) }
                                    }
                                    if (filteredTeachFields.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = teachingFieldExpanded,
                                            onDismissRequest = { teachingFieldExpanded = false }
                                        ) {
                                            filteredTeachFields.take(15).forEach { field ->
                                                DropdownMenuItem(
                                                    text = { Text(field.name, fontWeight = FontWeight.Medium) },
                                                    onClick = {
                                                        teachingField = field.name
                                                        teachingFieldExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                // Teaching University Dropdown
                                ExposedDropdownMenuBox(
                                    expanded = teachingUniversityExpanded,
                                    onExpandedChange = { teachingUniversityExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = teachingUniversity,
                                        onValueChange = { teachingUniversity = it },
                                        label = { Text("دانشگاه محل تدریس") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teachingUniversityExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                    )
                                    val filteredTeachUniversities = remember(teachingUniversity, profileState.universities) {
                                        if (teachingUniversity.isBlank()) profileState.universities
                                        else profileState.universities.filter { it.name.contains(teachingUniversity, ignoreCase = true) }
                                    }
                                    if (filteredTeachUniversities.isNotEmpty()) {
                                        ExposedDropdownMenu(
                                            expanded = teachingUniversityExpanded,
                                            onDismissRequest = { teachingUniversityExpanded = false }
                                        ) {
                                            filteredTeachUniversities.take(15).forEach { uni ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(uni.name, fontWeight = FontWeight.Medium)
                                                            if (uni.city != null) {
                                                                Text(
                                                                    uni.city,
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        teachingUniversity = uni.name
                                                        teachingUniversityExpanded = false
                                                    }
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

            // Experience Section
            item {
                SectionHeader("سوابق کاری و افتخارات", Icons.Default.Work)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = workExperience,
                    onValueChange = { workExperience = it },
                    label = { Text("تجربه کاری") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = achievements,
                    onValueChange = { achievements = it },
                    label = { Text("افتخارات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Bio Channels Section
            item {
                SectionHeader("کانال‌های بایو", Icons.Default.Campaign)
                Text(
                    "می‌توانید تا ۲ کانال که مدیریت می‌کنید را در پروفایل خود نمایش دهید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                BioChannelSelector(
                    label = "کانال اول",
                    selectedChannelId = bioChannelId1,
                    channels = adminChannels,
                    onSelectValues = { showChannelSheet1 = true },
                    onRemove = { bioChannelId1 = null }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                BioChannelSelector(
                    label = "کانال دوم",
                    selectedChannelId = bioChannelId2,
                    channels = adminChannels,
                    onSelectValues = { showChannelSheet2 = true },
                    onRemove = { bioChannelId2 = null }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Channel Selection Sheets
    if (showChannelSheet1) {
        ChannelSelectionSheet(
            channels = adminChannels,
            onDismiss = { showChannelSheet1 = false },
            onChannelSelected = { 
                bioChannelId1 = it.id 
                showChannelSheet1 = false
            }
        )
    }
    
    if (showChannelSheet2) {
        ChannelSelectionSheet(
            channels = adminChannels,
            onDismiss = { showChannelSheet2 = false },
            onChannelSelected = { 
                bioChannelId2 = it.id
                showChannelSheet2 = false
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MessageAppTheme.extendedColors.accent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BioChannelSelector(
    label: String,
    selectedChannelId: String?,
    channels: List<Channel>,
    onSelectValues: () -> Unit,
    onRemove: () -> Unit
) {
    val selectedChannel = remember(selectedChannelId, channels) {
        channels.find { it.id == selectedChannelId }
    }
    
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectValues() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selectedChannel != null) {
                    Text(selectedChannel.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                } else {
                    Text("انتخاب کنید...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (selectedChannelId != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSelectionSheet(
    channels: List<Channel>,
    onDismiss: () -> Unit,
    onChannelSelected: (Channel) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            "انتخاب کانال",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (channels.isEmpty()) {
            Text(
                "شما مدیر هیچ کانالی نیستید.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            channels.forEach { channel ->
                ListItem(
                    headlineContent = { Text(channel.name) },
                    supportingContent = { Text("${channel.subscriberCount} عضو") },
                    leadingContent = {
                        AvatarImage(
                            imageUrl = channel.avatarUrl?.let { com.Kelasor.app.util.UrlUtils.getFullUrl(it) },
                            name = channel.name,
                            size = com.Kelasor.app.ui.components.AvatarSize.SMALL
                        )
                    },
                    modifier = Modifier.clickable { onChannelSelected(channel) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
