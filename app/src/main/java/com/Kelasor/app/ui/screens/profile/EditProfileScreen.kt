package com.Kelasor.app.ui.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.ReferenceDataViewModel
import com.Kelasor.app.ui.viewmodel.ProfileEvent
import com.Kelasor.app.util.DateUtils
import com.Kelasor.app.util.UrlUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    channelViewModel: com.Kelasor.app.ui.viewmodel.ChannelListViewModel = hiltViewModel(),
    refViewModel: ReferenceDataViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.state.collectAsState()
    val channelState by channelViewModel.state.collectAsState()
    val refState by refViewModel.state.collectAsState()
    val user = profileState.user
    val extendedColors = MessageAppTheme.extendedColors
    val context = LocalContext.current

    // Local URI for immediate preview
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    
    // Photo Editor State
    var editingAvatarUri by remember { mutableStateOf<Uri?>(null) }

    // Avatar picker launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            editingAvatarUri = it
        }
    }

    // Load reference data and channels
    LaunchedEffect(Unit) {
        refViewModel.loadReferenceData()
        profileViewModel.loadProvinces()
        channelViewModel.loadChannels()
    }

    // Observe events
    LaunchedEffect(Unit) {
        profileViewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.SaveSuccess -> {
                    Toast.makeText(context, "تغییرات با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
                is ProfileEvent.AvatarUploaded -> {
                    Toast.makeText(context, "تصویر پروفایل بروزرسانی شد", Toast.LENGTH_SHORT).show()
                }
                is ProfileEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ── Form State ───────────────────────────────────────────────────────────
    var fullName by remember(user) { mutableStateOf(user?.let { 
        if (it.lastName.isNullOrBlank()) it.firstName ?: "" 
        else "${it.firstName} ${it.lastName}" 
    } ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var nationalCode by remember(user) { mutableStateOf(user?.nationalCode ?: "") }
    var birthDate by remember(user) { mutableStateOf(user?.birthDate ?: "") }
    
    // Educational State
    var selectedRoleValueEn by remember(user) { mutableStateOf(user?.educationalRole ?: "") }
    var selectedLevelName by remember(user) { mutableStateOf(user?.gradeLevel ?: "") }
    var selectedFaculty by remember(user) { mutableStateOf(user?.faculty ?: "") }
    var selectedMajor by remember(user) { mutableStateOf(user?.major ?: "") }
    
    // Detailed Profile Fields
    var achievements by remember(user) { mutableStateOf(user?.achievements ?: "") }
    var skills by remember(user) { mutableStateOf(user?.skills ?: "") }
    var certificates by remember(user) { mutableStateOf(user?.education ?: "") }
    
    // Location State
    var province by remember(user) { mutableStateOf(user?.province ?: "") }
    var city by remember(user) { mutableStateOf(user?.city ?: "") }

    // Bio Channels State
    var bioChannelId1 by remember(user) { mutableStateOf(user?.bioChannelId1) }
    var bioChannelId2 by remember(user) { mutableStateOf(user?.bioChannelId2) }

    // Admin Channels
    val adminChannels = remember(channelState.channels) {
        channelState.channels.filter { it.isAdmin }
    }

    // Validation
    val isFormValid = username.isNotBlank() && fullName.isNotBlank()
    
    // UI state for showing validation errors
    var showErrors by remember { mutableStateOf(false) }

    val filteredLevels = remember(selectedRoleValueEn, refState.educationLevels) {
        refState.educationLevels.filter { it.roleValueEn == selectedRoleValueEn || it.roleValueEn.isNullOrBlank() }
    }

    val filteredFields = remember(selectedLevelName, refState.fieldsOfStudy) {
        if (selectedLevelName.isBlank()) emptyList()
        else refState.fieldsOfStudy.filter { it.educationLevel.equals(selectedLevelName, ignoreCase = true) }.map { it.name }
    }

    val filteredFaculties = remember(selectedLevelName, refState.faculties) {
        if (selectedLevelName.isBlank()) emptyList()
        else refState.faculties.filter { it.educationLevel.equals(selectedLevelName, ignoreCase = true) }.map { it.name }
    }

    // Load cities if province changes
    LaunchedEffect(province) {
        if (province.isNotBlank()) {
            profileViewModel.loadCities(province)
        }
    }

    // Jalali Date Picker State
    var showJalaliPicker by remember { mutableStateOf(false) }

    if (showJalaliPicker) {
        JalaliDatePickerDialog(
            initialDate = birthDate,
            onDismiss = { showJalaliPicker = false },
            onConfirm = { selectedDate ->
                birthDate = selectedDate
                showJalaliPicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش پروفایل", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "برگشت")
                    }
                },
                actions = {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp).size(24.dp),
                            strokeWidth = 2.dp,
                            color = extendedColors.accent
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (isFormValid) {
                                    profileViewModel.updateProfile(
                                        username = username.trim(),
                                        displayName = fullName.trim(),
                                        firstName = fullName.trim(),
                                        lastName = "",
                                        nationalCode = if (nationalCode.isBlank()) null else nationalCode.trim(),
                                        bio = if (bio.isBlank()) null else bio.trim(),
                                        educationalRole = if (selectedRoleValueEn.isBlank()) null else selectedRoleValueEn,
                                        gradeLevel = if (selectedLevelName.isBlank()) null else selectedLevelName,
                                        major = if (selectedMajor.isBlank()) null else selectedMajor,
                                        faculty = if (selectedFaculty.isBlank()) null else selectedFaculty,
                                        birthDate = if (birthDate.isBlank()) null else birthDate,
                                        skills = if (skills.isBlank()) null else skills.trim(),
                                        achievements = if (achievements.isBlank()) null else achievements.trim(),
                                        education = if (certificates.isBlank()) null else certificates.trim(),
                                        province = if (province.isBlank()) null else province,
                                        city = if (city.isBlank()) null else city,
                                        bioChannelId1 = bioChannelId1,
                                        bioChannelId2 = bioChannelId2
                                    )
                                } else {
                                    showErrors = true
                                    Toast.makeText(context, "لطفاً تمامی فیلدهای اجباری را پر کنید", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Check, 
                                contentDescription = "ذخیره", 
                                tint = if (isFormValid) extendedColors.accent else extendedColors.accent.copy(alpha = 0.3f)
                            )
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
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Top Avatar Section ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(120.dp)) {
                        val displayImageUri: Any? = selectedAvatarUri
                            ?: user?.avatarUrl?.let { UrlUtils.getFullUrl(it) }

                        if (displayImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(displayImageUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "پروفایل",
                                modifier = Modifier
                                    .fillMaxSize()
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
                                name = if (fullName.isNotBlank()) fullName else "?",
                                size = com.Kelasor.app.ui.components.AvatarSize.XLARGE,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable {
                                        avatarPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                            )
                        }
                        
                        // Camera overlay
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
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
                            tonalElevation = 4.dp
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "تغییر تصویر",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // ── Identity Section ──
            item {
                EditSectionHeader("اطلاعات هویتی", Icons.Default.Badge)
                EditField(
                    label = "نام کامل (اجباری)",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "نام و نام خانوادگی خود را وارد کنید",
                    isError = showErrors && fullName.isBlank()
                )
                EditField(
                    label = "نام کاربری / ID (اجباری)",
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "یک شناسه‌ی کاربری انتخاب کنید",
                    isError = showErrors && username.isBlank()
                )
                EditField(
                    label = "کد ملی",
                    value = nationalCode,
                    onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) nationalCode = it },
                    placeholder = "کد ملی ۱۰ رقمی",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Birth Date Field (Clickable TextField)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "تاریخ تولد",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = DanaFontFamily,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showJalaliPicker = true }
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = if (birthDate.isBlank()) "انتخاب تاریخ تولد" else DateUtils.formatGregorianToJalali(birthDate),
                            fontFamily = DanaFontFamily,
                            color = if (birthDate.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                }
                
                EditField(
                    label = "بیوگرافی",
                    value = bio,
                    onValueChange = { bio = it },
                    placeholder = "درباره خودتان بنویسید...",
                    singleLine = false,
                    maxLines = 4
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            // ── Educational Identity Section ──
            item {
                Column(modifier = Modifier.animateContentSize()) {
                    EditSectionHeader("هویت آموزشی", Icons.Default.School)
                    
                    // Role Selection
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            "نقش من در آموزش",
                            style = MaterialTheme.typography.labelMedium,
                            color = extendedColors.accent,
                            fontFamily = DanaFontFamily
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            refState.educationalRoles.forEach { role ->
                                RoleChip(
                                    emoji = role.emoji,
                                    label = role.labelFa,
                                    isSelected = selectedRoleValueEn == role.valueEn,
                                    onClick = { 
                                        selectedRoleValueEn = role.valueEn
                                        selectedLevelName = ""
                                        selectedFaculty = ""
                                        selectedMajor = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Education Level Dropdown
                    AnimatedVisibility(visible = selectedRoleValueEn.isNotBlank()) {
                        EditDropdownField(
                            label = "مقطع تحصیلی",
                            value = selectedLevelName,
                            options = filteredLevels.map { it.name },
                            onOptionSelected = { 
                                selectedLevelName = it
                                selectedFaculty = ""
                                selectedMajor = ""
                            }
                        )
                    }

                    // Faculty Dropdown
                    AnimatedVisibility(visible = filteredFaculties.isNotEmpty()) {
                        EditDropdownField(
                            label = "دانشکده",
                            value = selectedFaculty,
                            options = filteredFaculties,
                            onOptionSelected = { selectedFaculty = it }
                        )
                    }

                    // Major Dropdown
                    AnimatedVisibility(visible = filteredFields.isNotEmpty()) {
                        EditDropdownField(
                            label = "رشته تحصیلی",
                            value = selectedMajor,
                            options = filteredFields,
                            onOptionSelected = { selectedMajor = it }
                        )
                    }
                    
                    // New Fields: Skills, Achievements, Certificates
                    EditField(
                        label = "مهارت‌ها",
                        value = skills,
                        onValueChange = { skills = it },
                        placeholder = "مهارت‌های خود را وارد کنید (مثلا: فتوشاپ، پایتون)",
                        singleLine = false,
                        maxLines = 3
                    )
                    
                    EditField(
                        label = "افتخارات",
                        value = achievements,
                        onValueChange = { achievements = it },
                        placeholder = "افتخارات و جوایز خود را بنویسید",
                        singleLine = false,
                        maxLines = 3
                    )
                    
                    EditField(
                        label = "مدارک و گواهینامه‌ها",
                        value = certificates,
                        onValueChange = { certificates = it },
                        placeholder = "مدارک تحصیلی یا گواهینامه‌ها",
                        singleLine = false,
                        maxLines = 3
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            // ── Bio Channels Section ──
            item {
                EditSectionHeader("کانال‌های بایو", Icons.Default.Campaign)
                Text(
                    "می‌توانید تا ۲ کانال را در پروفایل خود نمایش دهید",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = DanaFontFamily,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                ChannelSelectorItem(
                    label = "کانال اول",
                    selectedChannelId = bioChannelId1,
                    availableChannels = adminChannels,
                    onChannelSelected = { bioChannelId1 = it }
                )
                
                ChannelSelectorItem(
                    label = "کانال دوم",
                    selectedChannelId = bioChannelId2,
                    availableChannels = adminChannels,
                    onChannelSelected = { bioChannelId2 = it }
                )
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }

            // ── Location Section ──
            item {
                EditSectionHeader("موقعیت مکانی", Icons.Default.LocationOn)
                
                EditDropdownField(
                    label = "استان",
                    value = province,
                    options = profileState.provinces,
                    onOptionSelected = { 
                        province = it
                        city = ""
                    }
                )
                
                EditDropdownField(
                    label = "شهر",
                    value = city,
                    options = profileState.cities,
                    onOptionSelected = { city = it },
                    enabled = province.isNotBlank()
                )
            }
        }
    }

    // ── Profile Photo Editor Overlay ──
    AnimatedVisibility(
        visible = editingAvatarUri != null,
        enter = fadeIn() + scaleIn(initialScale = 0.8f) + expandIn(expandFrom = Alignment.Center),
        exit = fadeOut() + scaleOut(targetScale = 0.8f) + shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        editingAvatarUri?.let { uri ->
            com.Kelasor.app.ui.components.ProfilePhotoEditorScreen(
                imageUri = uri,
                onSave = { editedUri ->
                    selectedAvatarUri = editedUri
                    editingAvatarUri = null
                    try {
                        val file = File(editedUri.path!!)
                        profileViewModel.uploadAvatar(file)
                    } catch (e: Exception) {
                        Toast.makeText(context, "خطا در آپلود تصویر", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = { editingAvatarUri = null }
            )
        }
    }
}

@Composable
fun EditSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MessageAppTheme.extendedColors.accent
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MessageAppTheme.extendedColors.accent,
            fontFamily = DanaFontFamily
        )
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = DanaFontFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontFamily = DanaFontFamily, fontSize = 14.sp) },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = DanaFontFamily),
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = if (isError) MaterialTheme.colorScheme.error else MessageAppTheme.extendedColors.accent,
                unfocusedIndicatorColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
                errorIndicatorColor = MaterialTheme.colorScheme.error
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = DanaFontFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = it }
        ) {
            TextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                placeholder = { Text("انتخاب کنید...", fontFamily = DanaFontFamily, fontSize = 14.sp) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = DanaFontFamily),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MessageAppTheme.extendedColors.accent,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            
            if (options.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontFamily = DanaFontFamily) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleChip(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) extendedColors.accent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, extendedColors.accent) else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = DanaFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) extendedColors.accent else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ChannelSelectorItem(
    label: String,
    selectedChannelId: String?,
    availableChannels: List<com.Kelasor.app.domain.model.Channel>,
    onChannelSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChannel = availableChannels.firstOrNull { it.id == selectedChannelId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = DanaFontFamily,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedChannel != null) {
                    AvatarImage(
                        imageUrl = selectedChannel.avatarUrl,
                        name = selectedChannel.name,
                        size = com.Kelasor.app.ui.components.AvatarSize.SMALL,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        selectedChannel.name,
                        fontFamily = DanaFontFamily,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        "انتخاب کانال",
                        fontFamily = DanaFontFamily,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (selectedChannelId != null) {
                    IconButton(onClick = { onChannelSelected(null) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "حذف", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (availableChannels.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("کانالی یافت نشد", fontFamily = DanaFontFamily) },
                    onClick = { expanded = false },
                    enabled = false
                )
            } else {
                availableChannels.forEach { channel ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AvatarImage(imageUrl = channel.avatarUrl, name = channel.name, size = com.Kelasor.app.ui.components.AvatarSize.SMALL, modifier = Modifier.size(24.dp))
                                Text(channel.name, fontFamily = DanaFontFamily)
                            }
                        },
                        onClick = {
                            onChannelSelected(channel.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
enum class PickerMode { DAYS, MONTHS, YEARS }

@Composable
fun JalaliDatePickerDialog(
    initialDate: String, // yyyy-MM-dd
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit // yyyy-MM-dd
) {
    val extendedColors = MessageAppTheme.extendedColors
    val now = Calendar.getInstance()
    
    // Parse initial date
    val initJalali = if (initialDate.isNotBlank()) {
        try {
            val parts = initialDate.split("-")
            DateUtils.gregorianToJalali(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } catch (e: Exception) {
            DateUtils.gregorianToJalali(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
        }
    } else {
        DateUtils.gregorianToJalali(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
    }

    var selectedYear by remember { mutableStateOf(initJalali.first) }
    var selectedMonth by remember { mutableStateOf(initJalali.second) }
    var selectedDay by remember { mutableStateOf(initJalali.third) }

    var viewYear by remember { mutableStateOf(selectedYear) }
    var viewMonth by remember { mutableStateOf(selectedMonth) }
    
    var pickerMode by remember { mutableStateOf(PickerMode.DAYS) }

    val shamsiMonthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val persianDayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "انتخاب تاریخ تولد",
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(extendedColors.accent.copy(alpha = 0.1f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${DateUtils.toPersianDigits(selectedDay)} ${shamsiMonthNames[selectedMonth - 1]} ${DateUtils.toPersianDigits(selectedYear)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = extendedColors.accent,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Mode Selection Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shamsiMonthNames[viewMonth - 1],
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { pickerMode = if (pickerMode == PickerMode.MONTHS) PickerMode.DAYS else PickerMode.MONTHS }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (pickerMode == PickerMode.MONTHS) extendedColors.accent else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateUtils.toPersianDigits(viewYear),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { pickerMode = if (pickerMode == PickerMode.YEARS) PickerMode.DAYS else PickerMode.YEARS }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (pickerMode == PickerMode.YEARS) extendedColors.accent else MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                    when (pickerMode) {
                        PickerMode.DAYS -> {
                            Column {
                                // Month Navigation Arrows
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = {
                                        if (viewMonth == 1) { viewMonth = 12; viewYear-- }
                                        else viewMonth--
                                    }) {
                                        Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowRight, contentDescription = "قبلی", tint = extendedColors.accent)
                                    }
                                    
                                    Text(
                                        text = "${shamsiMonthNames[viewMonth - 1]} ${DateUtils.toPersianDigits(viewYear)}",
                                        fontFamily = DanaFontFamily,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    IconButton(onClick = {
                                        if (viewMonth == 12) { viewMonth = 1; viewYear++ }
                                        else viewMonth++
                                    }) {
                                        Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowLeft, contentDescription = "بعدی", tint = extendedColors.accent)
                                    }
                                }

                                // Weekday Headers
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    persianDayHeaders.forEach { header ->
                                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                            Text(header, fontFamily = DanaFontFamily, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                // Calendar Grid
                                val days = remember(viewYear, viewMonth) { getCalendarDays(viewYear, viewMonth) }
                                val rows = days.chunked(7)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    rows.forEach { week ->
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            week.forEach { day ->
                                                val isSelected = day != null && day == selectedDay && viewMonth == selectedMonth && viewYear == selectedYear
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) extendedColors.accent else Color.Transparent)
                                                        .clickable(enabled = day != null) {
                                                            selectedDay = day!!
                                                            selectedMonth = viewMonth
                                                            selectedYear = viewYear
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (day != null) {
                                                        Text(
                                                            text = DateUtils.toPersianDigits(day),
                                                            fontFamily = DanaFontFamily,
                                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        PickerMode.MONTHS -> {
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(12) { index ->
                                    val month = index + 1
                                    val isSelected = month == viewMonth
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) extendedColors.accent else Color.Transparent)
                                            .clickable {
                                                viewMonth = month
                                                pickerMode = PickerMode.DAYS
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shamsiMonthNames[index],
                                            fontFamily = DanaFontFamily,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        PickerMode.YEARS -> {
                            val years = (1300..1405).toList().reversed()
                            val listState = androidx.compose.foundation.lazy.grid.rememberLazyGridState(
                                initialFirstVisibleItemIndex = years.indexOf(viewYear).coerceAtLeast(0)
                            )
                            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(years.size) { index ->
                                    val year = years[index]
                                    val isSelected = year == viewYear
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) extendedColors.accent else Color.Transparent)
                                            .clickable {
                                                viewYear = year
                                                pickerMode = PickerMode.DAYS
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = DateUtils.toPersianDigits(year),
                                            fontFamily = DanaFontFamily,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val (gy, gm, gd) = DateUtils.jalaliToGregorian(selectedYear, selectedMonth, selectedDay)
                val formatted = "$gy-${gm.toString().padStart(2, '0')}-${gd.toString().padStart(2, '0')}"
                onConfirm(formatted)
            }) {
                Text("تایید", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لغو", fontFamily = DanaFontFamily)
            }
        }
    )
}

private fun getCalendarDays(jYear: Int, jMonth: Int): List<Int?> {
    val daysInMonth = DateUtils.jalaliMonthDays(jYear, jMonth)
    val (gY, gM, gD) = DateUtils.jalaliToGregorian(jYear, jMonth, 1)
    val cal = Calendar.getInstance()
    cal.set(gY, gM - 1, gD)
    val firstDowJava = cal.get(Calendar.DAY_OF_WEEK)
    val offset = when (firstDowJava) {
        Calendar.SATURDAY -> 0
        Calendar.SUNDAY -> 1
        Calendar.MONDAY -> 2
        Calendar.TUESDAY -> 3
        Calendar.WEDNESDAY -> 4
        Calendar.THURSDAY -> 5
        Calendar.FRIDAY -> 6
        else -> 0
    }
    val cells = mutableListOf<Int?>()
    repeat(offset) { cells.add(null) }
    for (d in 1..daysInMonth) { cells.add(d) }
    while (cells.size % 7 != 0) { cells.add(null) }
    return cells
}
