package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.animation.*
import coil3.compose.AsyncImage
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import android.net.Uri
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.components.DynamicChipGroup
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.theme.MessageAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyProfileSetupScreen(
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit,
    viewModel: AcademyProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    val logoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onLogoChange(it.toString()) }
    }

    var showUserSearchDialog by remember { mutableStateOf(false) }
    var searchTarget by remember { mutableStateOf(SearchTarget.INSTRUCTOR) }
    var showManualInstructorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AcademyProfileEvent.Success -> onFinish()
                is AcademyProfileEvent.Error -> { /* Show snackbar if needed */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پروفایل آکادمی", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (state.name.isBlank()) {
                                    com.Kelasor.app.ui.components.KelasorToast.show(
                                        context,
                                        "لطفاً فیلدهای الزامی را تکمیل کنید",
                                        com.Kelasor.app.ui.components.ToastType.ERROR
                                    )
                                } else {
                                    viewModel.submit()
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "ذخیره",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { logoPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                // Priority: localLogoUri (immediate preview) > logoUrl (server URL)
                val displayUrl: String? = state.localLogoUri
                    ?: com.Kelasor.app.util.UrlUtils.getFullUrl(state.logoUrl)
                
                if (displayUrl != null) {
                    AsyncImage(
                        model = displayUrl,
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = "Upload Logo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    )
                }
                // Upload progress overlay
                if (state.isUploadingLogo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
            Text(
                "لوگوی مجموعه",
                fontFamily = DanaFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Name
            SectionHeader(title = "اطلاعات پایه", icon = Icons.Default.Info)
            
            // Type Selector
            var expandedType by remember { mutableStateOf(false) }
            val types = mapOf(
                "CLUB" to "کانون",
                "SCIENTIFIC_ASSOCIATION" to "انجمن علمی",
                "ACADEMY" to "آکادمی و موسسات",
                "STUDENT_ORG" to "تشکل دانشجویی",
                "RESEARCH_CENTER" to "مرکز تحقیقاتی"
            )
            val currentTypeLabel = types[state.type] ?: "آکادمی و موسسات"

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = currentTypeLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("نوع مجموعه", fontFamily = DanaFontFamily) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    types.forEach { (key, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontFamily = DanaFontFamily) },
                            onClick = {
                                viewModel.onTypeChange(key)
                                expandedType = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("نام مجموعه برگزارکننده", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("توضیحات و آدرس (اختیاری)", fontFamily = DanaFontFamily) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            // Subsidiary for Academy and Research Center
            if (state.type in listOf("ACADEMY", "RESEARCH_CENTER")) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.onIsSubsidiaryChange(!state.isSubsidiary) }
                ) {
                    Checkbox(
                        checked = state.isSubsidiary,
                        onCheckedChange = { viewModel.onIsSubsidiaryChange(it) }
                    )
                    Text("زیرمجموعه هستم", fontFamily = DanaFontFamily, fontSize = 14.sp)
                }
                if (state.isSubsidiary) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.dependencyDescription,
                        onValueChange = { viewModel.onDependencyDescriptionChange(it) },
                        label = { Text("توضیح دهید وابسته به چه چیز هستید؟", fontFamily = DanaFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Dynamic Affiliations — hidden when INDEPENDENT, ACADEMY, RESEARCH_CENTER
            if (state.type !in listOf("INDEPENDENT", "ACADEMY", "RESEARCH_CENTER")) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(title = "وابستگی‌های سازمانی", icon = Icons.Default.AccountBalance)
                
                // University — for all non-INDEPENDENT
                DynamicChipGroup(
                    label = "نام دانشگاه(ها)",
                    items = state.universities,
                    suggestions = state.allUniversities,
                    onAdd = { viewModel.addUniversity(it) },
                    onRemove = { viewModel.removeUniversity(it) },
                    placeholder = "جستجو یا وارد کردن نام دانشگاه"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Fields of Study — for SCIENTIFIC_ASSOCIATION
                if (state.type in listOf("SCIENTIFIC_ASSOCIATION")) {
                    DynamicChipGroup(
                        label = "رشته(های) مرتبط",
                        items = state.associatedFieldOfStudyIds,
                        suggestions = state.allFieldsOfStudy,
                        onAdd = { viewModel.addFieldOfStudy(it) },
                        onRemove = { viewModel.removeFieldOfStudy(it) },
                        placeholder = "مثلاً: مهندسی کامپیوتر"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Clubs — for CLUB
                if (state.type in listOf("CLUB")) {
                    DynamicChipGroup(
                        label = "کانون(های) مرتبط",
                        items = state.associatedClubIds,
                        suggestions = state.allClubs,
                        onAdd = { viewModel.addClub(it) },
                        onRemove = { viewModel.removeClub(it) },
                        placeholder = "انتخاب از لیست کانون‌ها"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Student Orgs — for STUDENT_ORG
                if (state.type in listOf("STUDENT_ORG")) {
                    DynamicChipGroup(
                        label = "نام تشکل(ها)",
                        items = state.associatedStudentOrgIds,
                        suggestions = state.allStudentOrgs,
                        onAdd = { viewModel.addStudentOrg(it) },
                        onRemove = { viewModel.removeStudentOrg(it) },
                        placeholder = "انتخاب از لیست تشکل‌ها"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Specialties (Hashtags)
            SectionHeader(title = "تخصص‌ها و هشتگ‌ها", icon = Icons.Default.Tag)
            DynamicChipGroup(
                label = "تخصص‌ها",
                items = state.specialties,
                onAdd = { viewModel.addSpecialty(it) },
                onRemove = { viewModel.removeSpecialty(it) },
                placeholder = "مثلاً: هوش مصنوعی",
                allowManualAdd = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Instructors Section
            var showInstructorAddMenu by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("مدرسین", fontFamily = DanaFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { 
                        searchTarget = SearchTarget.INSTRUCTOR
                        showUserSearchDialog = true 
                    }) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("جستجو کاربر", fontFamily = DanaFontFamily, fontSize = 12.sp)
                    }
                    TextButton(onClick = { showManualInstructorDialog = true }, colors = ButtonDefaults.textButtonColors(contentColor = MessageAppTheme.extendedColors.accent)) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("افزودن مدرس مهمان", fontFamily = DanaFontFamily, fontSize = 12.sp)
                    }
                }
            }

            if (state.instructors.isEmpty() && state.manualInstructors.isEmpty()) {
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
                    items(state.instructors) { user ->
                        Box {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                                AvatarImage(imageUrl = user.avatarUrl, name = user.displayName, size = AvatarSize.MEDIUM, modifier = Modifier.size(56.dp).clip(CircleShape).border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                Text(user.displayName, fontFamily = DanaFontFamily, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
                            }
                            Box(modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).background(MaterialTheme.colorScheme.error, CircleShape).border(1.dp, Color.White, CircleShape).clickable { viewModel.removeInstructor(user) }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    items(state.manualInstructors) { instructor ->
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
                            Box(modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).background(MaterialTheme.colorScheme.error, CircleShape).border(1.dp, Color.White, CircleShape).clickable { viewModel.removeManualInstructor(instructor) }, contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            UserSelectionList(
                title = "ادمین‌ها",
                users = state.admins,
                onAddClick = {
                    searchTarget = SearchTarget.ADMIN
                    showUserSearchDialog = true
                },
                onRemove = { viewModel.removeAdmin(it) }
            )

            Spacer(modifier = Modifier.height(40.dp))

            PrimaryButton(
                text = "تایید و اتمام راه‌اندازی",
                onClick = { viewModel.submit() },
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isLoading,
                enabled = state.name.isNotBlank()
            )
            
            if (state.error != null) {
                Text(
                    state.error,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    if (showUserSearchDialog) {
        UserSearchDialog(
            onDismiss = { showUserSearchDialog = false },
            onUserSelected = { user ->
                if (searchTarget == SearchTarget.INSTRUCTOR) {
                    viewModel.addInstructor(user)
                } else {
                    viewModel.addAdmin(user)
                }
                showUserSearchDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showManualInstructorDialog) {
        ManualInstructorDialog(
            onDismiss = { showManualInstructorDialog = false },
            onInstructorAdded = { instructor ->
                viewModel.addManualInstructor(instructor)
                showManualInstructorDialog = false
            },
            viewModel = viewModel
        )
    }
}

enum class SearchTarget { INSTRUCTOR, ADMIN }

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UserSelectionList(
    title: String,
    users: List<User>,
    onAddClick: () -> Unit,
    onRemove: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontFamily = DanaFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onAddClick) {
                Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("افزودن بر اساس ID", fontFamily = DanaFontFamily, fontSize = 12.sp)
            }
        }
        
        if (users.isEmpty()) {
            Text(
                "هیچ موردی اضافه نشده است",
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
    viewModel: AcademyProfileViewModel
) {
    var query by remember { mutableStateOf("") }
    
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
                        if (viewModel.state.isSearching) {
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
                } else if (viewModel.state.searchResults.isEmpty() && !viewModel.state.isSearching) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("کاربری یافت نشد", fontFamily = DanaFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        viewModel.state.searchResults.forEach { user ->
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

@Composable
fun ManualInstructorDialog(
    onDismiss: () -> Unit,
    onInstructorAdded: (com.Kelasor.app.domain.model.ManualInstructor) -> Unit,
    viewModel: AcademyProfileViewModel
) {
    var name by remember { mutableStateOf("") }
    var resume by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    val avatarPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            isUploading = true
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val tempFile = java.io.File.createTempFile("avatar_", ".jpg", context.cacheDir)
                    inputStream?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    val result = viewModel.uploadAvatar(tempFile)
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

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
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
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "آپلود تصویر", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام و نام خانوادگی", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = resume,
                    onValueChange = { resume = it },
                    label = { Text("رزومه / تخصص", fontFamily = DanaFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", fontFamily = DanaFontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onInstructorAdded(com.Kelasor.app.domain.model.ManualInstructor(name, resume, avatarUrl))
                            }
                        },
                        enabled = name.isNotBlank() && !isUploading
                    ) {
                        Text("افزودن", fontFamily = DanaFontFamily)
                    }
                }
            }
        }
    }
}
