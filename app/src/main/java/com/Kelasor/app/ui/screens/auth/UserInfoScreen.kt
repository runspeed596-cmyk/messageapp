package com.Kelasor.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.ExperimentalComposeUiApi
import android.widget.Toast
import java.io.File
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.AuthEvent
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import com.Kelasor.app.ui.viewmodel.ReferenceDataViewModel
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onNavigateToMain: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    refViewModel: ReferenceDataViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    val refState by refViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val extendedColors = MessageAppTheme.extendedColors
    
    // ── Form state ───────────────────────────────────────────────────────────
    var fullName by remember { mutableStateOf("") }
    var selectedRoleValueEn by remember { mutableStateOf("") }
    var selectedEducationLevel by remember { mutableStateOf("") }
    var selectedFieldOfStudy by remember { mutableStateOf("") }
    var selectedFaculty by remember { mutableStateOf("") }
    
    // New Fields
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var editingAvatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isGraduated by remember { mutableStateOf(false) }
    var universities by remember { mutableStateOf(listOf<String>()) }
    var fieldsOfStudy by remember { mutableStateOf(listOf<String>()) }
    var selectedUniversity by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Handle back button when editing photo or in signup
    androidx.activity.compose.BackHandler(enabled = true) {
        if (editingAvatarUri != null) {
            editingAvatarUri = null
        } else {
            // If in UserInfo screen, maybe show a toast or just let it exit if they really want
            // but the user complained about accidental exit during cropping.
            // For now, if not cropping, we let the default behavior happen (exit app since it's root)
            // unless we want to force them to stay.
            // The user said: "مشکل وقتی که یک بار Back میزنیم با اندروید کامل از اپلیکیشن میوفته بیرون(در صفحه ای که عکس پروفایل کراپ میکنیم)"
            // So the primary fix is for the cropping state.
            (context as? android.app.Activity)?.finish()
        }
    }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { editingAvatarUri = it }
    }

    // ── Derived values ───────────────────────────────────────────────────────
    val selectedRole = refState.educationalRoles.firstOrNull { it.valueEn == selectedRoleValueEn }
    val selectedRoleLabel = selectedRole?.let { "${it.emoji} ${it.labelFa}" } ?: "نقش خود را انتخاب کنید"
    
    val filteredFields = remember(selectedEducationLevel, refState.fieldsOfStudy) {
        if (selectedEducationLevel.isBlank()) emptyList()
        else refState.fieldsOfStudy.filter { it.educationLevel.equals(selectedEducationLevel, ignoreCase = true) }.map { it.name }
    }

    val filteredFaculties = remember(selectedEducationLevel, refState.faculties) {
        if (selectedEducationLevel.isBlank()) emptyList()
        else refState.faculties.filter { it.educationLevel.equals(selectedEducationLevel, ignoreCase = true) }.map { it.name }
    }
    
    val allUniversities = remember(refState.universities) { refState.universities.map { it.name } }
    val allFieldsOfStudy = remember(refState.fieldsOfStudy) { refState.fieldsOfStudy.map { it.name } }
    
    // Mandatory fields based on selected role
    val isFormValid: Boolean = fullName.isNotBlank() && selectedRoleValueEn.isNotBlank() && when (selectedRoleValueEn) {
        "UNI_STUDENT" -> selectedUniversity.isNotBlank() && selectedEducationLevel.isNotBlank()
        "SCHOOL_STUDENT" -> selectedEducationLevel.isNotBlank()
        "TEACHER" -> universities.isNotEmpty()
        else -> true // FREELANCER etc.
    }

    // Ensure data is loaded if it was missed or failed before
    LaunchedEffect(refState.educationalRoles) {
        if (refState.educationalRoles.isEmpty() && !refState.isLoading && refState.error == null) {
            refViewModel.loadReferenceData()
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> {
                    authViewModel.setOnboardingComplete(true)
                    onNavigateToMain()
                }
                is AuthEvent.Error -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // ── Avatar Upload Section ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Profile Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Upload Avatar",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "آپلود تصویر پروفایل (اختیاری)",
                    fontFamily = DanaFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            // ── Form Section ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .animateContentSize()
            ) {
                SectionHeader("اطلاعات شناسایی")
                
                ProfileEditField(
                    label = "نام کامل *",
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "نام و نام خانوادگی خود را وارد کنید",
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader("نقش و تحصیلات")
                
                // Role Selection Section
                Text(
                    text = "به عنوان کدام یک از نقش های زیر وارد میشوید؟ *",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (refState.isLoading && refState.educationalRoles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = extendedColors.accent, strokeWidth = 2.dp)
                    }
                } else if (refState.error != null && refState.educationalRoles.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "خطا در بارگذاری نقش‌ها",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = DanaFontFamily,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = { refViewModel.loadReferenceData() }) {
                            Text("تلاش مجدد", fontFamily = DanaFontFamily, color = extendedColors.accent)
                        }
                    }
                } else if (refState.educationalRoles.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "در حال بارگذاری...",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = DanaFontFamily,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // Using a simple Column + Rows instead of FlowRow for better stability on different screens
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val roles = refState.educationalRoles
                        val chunks = roles.chunked(2)
                        chunks.forEach { rowRoles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowRoles.forEach { role ->
                                    RoleChip(
                                        emoji = role.emoji,
                                        label = role.labelFa,
                                        isSelected = selectedRoleValueEn == role.valueEn,
                                        onClick = { 
                                            selectedRoleValueEn = role.valueEn
                                            selectedEducationLevel = ""
                                            selectedFieldOfStudy = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // If row has only one item, add a spacer to keep width consistent
                                if (rowRoles.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Education Details
                AnimatedVisibility(
                    visible = selectedRoleValueEn.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (selectedRoleValueEn == "TEACHER") {
                            // Teacher fields
                            com.Kelasor.app.ui.components.DynamicChipGroup(
                                label = "دانشگاه‌های محل تحصیل/تدریس",
                                placeholder = "دانشگاه را جستجو کنید...",
                                items = universities,
                                suggestions = allUniversities,
                                onAdd = { if (it !in universities) universities = universities + it },
                                onRemove = { universities = universities - it },
                                allowManualAdd = false
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            com.Kelasor.app.ui.components.DynamicChipGroup(
                                label = "رشته‌های تخصصی",
                                placeholder = "رشته را جستجو کنید...",
                                items = fieldsOfStudy,
                                suggestions = allFieldsOfStudy,
                                onAdd = { if (it !in fieldsOfStudy) fieldsOfStudy = fieldsOfStudy + it },
                                onRemove = { fieldsOfStudy = fieldsOfStudy - it },
                                allowManualAdd = false
                            )
                        } else if (selectedRoleValueEn == "SCHOOL_STUDENT") {
                            // School student fields: مقطع + رشته (if configured)
                            val schoolLevels = refState.educationLevels.filter { it.roleValueEn == "SCHOOL_STUDENT" }
                            
                            SearchableSelector(
                                label = "مقطع تحصیلی",
                                selected = selectedEducationLevel,
                                options = schoolLevels.map { it.name },
                                placeholder = "جستجوی مقطع تحصیلی...",
                                onSelect = { 
                                    selectedEducationLevel = it
                                    selectedFieldOfStudy = ""
                                }
                            )
                            
                            // Show field of study if admin has enabled it for this level
                            val selectedLevelObj = schoolLevels.firstOrNull { it.name == selectedEducationLevel }
                            val showSchoolField = selectedLevelObj?.hasFieldOfStudy == true
                            
                            AnimatedVisibility(
                                visible = showSchoolField && filteredFields.isNotEmpty(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SearchableSelector(
                                        label = "رشته تحصیلی",
                                        selected = selectedFieldOfStudy,
                                        options = filteredFields,
                                        placeholder = "جستجوی رشته تحصیلی...",
                                        onSelect = {
                                            selectedFieldOfStudy = it
                                        }
                                    )
                                }
                            }
                        } else if (selectedRoleValueEn == "UNI_STUDENT") {
                            // University student fields
                            val filteredLevels = refState.educationLevels.filter { it.roleValueEn == selectedRoleValueEn || it.roleValueEn.isNullOrBlank() }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isGraduated,
                                    onCheckedChange = { isGraduated = it },
                                    colors = CheckboxDefaults.colors(checkedColor = extendedColors.accent)
                                )
                                Text(
                                    text = "فارغ التحصیل شده‌ام",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 14.sp,
                                    modifier = Modifier.clickable { isGraduated = !isGraduated }
                                )
                            }
                            
                            if (isGraduated) {
                                // Graduated student: دانشگاه → آخرین مقطع → رشته تحصیلی
                                Spacer(modifier = Modifier.height(8.dp))
                                SearchableSelector(
                                    label = "دانشگاه محل تحصیل",
                                    selected = selectedUniversity,
                                    options = allUniversities,
                                    placeholder = "جستجوی دانشگاه...",
                                    onSelect = {
                                        selectedUniversity = it
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchableSelector(
                                    label = "آخرین مقطع تحصیلی",
                                    selected = selectedEducationLevel,
                                    options = filteredLevels.map { it.name },
                                    placeholder = "جستجوی مقطع تحصیلی...",
                                    onSelect = {
                                        selectedEducationLevel = it
                                        if (selectedFieldOfStudy !in filteredFields) {
                                            selectedFieldOfStudy = ""
                                        }
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchableSelector(
                                    label = "رشته تحصیلی",
                                    selected = selectedFieldOfStudy,
                                    options = filteredFields,
                                    placeholder = "جستجوی رشته تحصیلی...",
                                    onSelect = {
                                        selectedFieldOfStudy = it
                                    }
                                )
                            } else {
                                // Active student: دانشگاه → مقطع → دانشکده → رشته
                                Spacer(modifier = Modifier.height(8.dp))
                                SearchableSelector(
                                    label = "دانشگاه محل تحصیل",
                                    selected = selectedUniversity,
                                    options = allUniversities,
                                    placeholder = "جستجوی دانشگاه...",
                                    onSelect = {
                                        selectedUniversity = it
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                SearchableSelector(
                                    label = "مقطع تحصیلی",
                                    selected = selectedEducationLevel,
                                    options = filteredLevels.map { it.name },
                                    placeholder = "جستجوی مقطع تحصیلی...",
                                    onSelect = { 
                                        selectedEducationLevel = it
                                        selectedFaculty = ""
                                        selectedFieldOfStudy = ""
                                    }
                                )
                                
                                AnimatedVisibility(
                                    visible = filteredFaculties.isNotEmpty(),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SearchableSelector(
                                            label = "دانشکده",
                                            selected = selectedFaculty,
                                            options = filteredFaculties,
                                            placeholder = "جستجوی دانشکده...",
                                            onSelect = {
                                                selectedFaculty = it
                                            }
                                        )
                                    }
                                }
                                
                                AnimatedVisibility(
                                    visible = filteredFields.isNotEmpty(),
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SearchableSelector(
                                            label = "رشته تحصیلی",
                                            selected = selectedFieldOfStudy,
                                            options = filteredFields,
                                            placeholder = "جستجوی رشته تحصیلی...",
                                            onSelect = {
                                                selectedFieldOfStudy = it
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        // FREELANCER: No additional fields needed
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                
                PrimaryButton(
                    text = "ذخیره و ادامه",
                    onClick = {
                        if (fullName.isBlank()) {
                            com.Kelasor.app.ui.components.KelasorToast.show(
                                context = context,
                                message = "لطفاً نام و نام خانوادگی را وارد کنید",
                                type = com.Kelasor.app.ui.components.ToastType.ERROR
                            )
                            return@PrimaryButton
                        }
                        if (selectedRoleValueEn.isBlank()) {
                            com.Kelasor.app.ui.components.KelasorToast.show(
                                context = context,
                                message = "لطفاً نقش خود را انتخاب کنید",
                                type = com.Kelasor.app.ui.components.ToastType.ERROR
                            )
                            return@PrimaryButton
                        }
                        // Role-specific mandatory field checks
                        when (selectedRoleValueEn) {
                            "UNI_STUDENT" -> {
                                if (selectedUniversity.isBlank()) {
                                    com.Kelasor.app.ui.components.KelasorToast.show(context = context, message = "لطفاً دانشگاه خود را انتخاب کنید", type = com.Kelasor.app.ui.components.ToastType.ERROR)
                                    return@PrimaryButton
                                }
                                if (selectedEducationLevel.isBlank()) {
                                    com.Kelasor.app.ui.components.KelasorToast.show(context = context, message = "لطفاً مقطع تحصیلی خود را انتخاب کنید", type = com.Kelasor.app.ui.components.ToastType.ERROR)
                                    return@PrimaryButton
                                }
                            }
                            "SCHOOL_STUDENT" -> {
                                if (selectedEducationLevel.isBlank()) {
                                    com.Kelasor.app.ui.components.KelasorToast.show(context = context, message = "لطفاً مقطع تحصیلی خود را انتخاب کنید", type = com.Kelasor.app.ui.components.ToastType.ERROR)
                                    return@PrimaryButton
                                }
                            }
                            "TEACHER" -> {
                                if (universities.isEmpty()) {
                                    com.Kelasor.app.ui.components.KelasorToast.show(context = context, message = "لطفاً حداقل یک دانشگاه اضافه کنید", type = com.Kelasor.app.ui.components.ToastType.ERROR)
                                    return@PrimaryButton
                                }
                            }
                        }
                        val resolvedAvatarFile: java.io.File? = avatarUri?.let { uri ->
                            try {
                                val inputStream: java.io.InputStream = context.contentResolver.openInputStream(uri)
                                    ?: return@let null
                                val tempFile: java.io.File = java.io.File(context.cacheDir, "avatar_temp_${System.currentTimeMillis()}.jpg")
                                tempFile.outputStream().use { output -> inputStream.copyTo(output) }
                                inputStream.close()
                                tempFile
                            } catch (e: Exception) {
                                null
                            }
                        }
                        authViewModel.updateProfile(
                            firstName = fullName,
                            lastName = "",
                            educationalRole = selectedRoleValueEn,
                            gradeLevel = if (selectedRoleValueEn in listOf("SCHOOL_STUDENT", "UNI_STUDENT")) selectedEducationLevel.ifBlank { null } else null,
                            major = if (selectedRoleValueEn in listOf("SCHOOL_STUDENT", "UNI_STUDENT")) selectedFieldOfStudy.ifBlank { null } else null,
                            faculty = if (selectedRoleValueEn == "UNI_STUDENT" && !isGraduated) selectedFaculty.ifBlank { null } else null,
                            university = if (selectedRoleValueEn == "UNI_STUDENT" && isGraduated) selectedUniversity.ifBlank { null } else null,
                            universities = if (selectedRoleValueEn == "TEACHER") universities else null,
                            fieldsOfStudy = if (selectedRoleValueEn == "TEACHER") fieldsOfStudy else null,
                            isGraduated = if (selectedRoleValueEn == "UNI_STUDENT") isGraduated else null,
                            nationalCode = null,
                            username = null,
                            bio = null,
                            avatarFile = resolvedAvatarFile
                        )
                        
                        if (selectedRoleValueEn == "TEACHER") {
                            com.Kelasor.app.ui.components.KelasorToast.show(
                                context = context,
                                message = "کانال اختصاصی شما در بخش اساتید پیام‌رسان ساخته شد",
                                type = com.Kelasor.app.ui.components.ToastType.SUCCESS
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !authState.isLoading,
                    isLoading = authState.isLoading
                )
                
                Spacer(modifier = Modifier.height(40.dp))
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
                        avatarUri = editedUri
                        editingAvatarUri = null
                    },
                    onDismiss = { editingAvatarUri = null }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontFamily = DanaFontFamily,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun ProfileEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = DanaFontFamily,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontFamily = DanaFontFamily, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            ),
            keyboardOptions = KeyboardOptions(imeAction = imeAction)
        )
    }
}

@Composable
private fun RoleChip(
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
        border = if (isSelected) BorderStroke(1.dp, extendedColors.accent) else null,
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
private fun SearchableSelector(
    label: String,
    selected: String,
    options: List<String>,
    placeholder: String = "جستجو...",
    onSelect: (String) -> Unit
) {
    var isSearchOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = DanaFontFamily) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isSearchOpen = true },
            enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { isSearchOpen = true }
        )
    }

    if (isSearchOpen) {
        SearchSelectionDialog(
            title = label,
            options = options,
            placeholder = placeholder,
            onSelect = {
                onSelect(it)
                isSearchOpen = false
            },
            onDismiss = { isSearchOpen = false }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SearchSelectionDialog(
    title: String,
    options: List<String>,
    placeholder: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    val filteredOptions = remember(searchQuery, options) {
        if (searchQuery.isBlank()) {
            options
        } else {
            options.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "انتخاب $title",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = placeholder, fontFamily = DanaFontFamily, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "پاک کردن")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MessageAppTheme.extendedColors.accent,
                        focusedLabelColor = MessageAppTheme.extendedColors.accent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (filteredOptions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "موردی یافت نشد",
                            fontFamily = DanaFontFamily,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredOptions) { option ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        keyboardController?.hide()
                                        onSelect(option)
                                    },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontFamily = DanaFontFamily,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}
