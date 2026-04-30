package com.Kelasor.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthEvent
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import com.Kelasor.app.ui.viewmodel.ReferenceDataViewModel

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
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedRoleValueEn by remember { mutableStateOf("") }
    var selectedEducationLevel by remember { mutableStateOf("") }
    var isLevelExpanded by remember { mutableStateOf(false) }
    var selectedFieldOfStudy by remember { mutableStateOf("") }
    var isFieldExpanded by remember { mutableStateOf(false) }
    var selectedFaculty by remember { mutableStateOf("") }
    var isFacultyExpanded by remember { mutableStateOf(false) }

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
    
    val isFormValid = firstName.isNotBlank() && lastName.isNotBlank() && selectedRoleValueEn.isNotBlank()

    // ── Navigation logic ─────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> onNavigateToMain()
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
            // ── Telegram-Style Header (Profile Preview) ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(extendedColors.accent, extendedColors.accent.copy(alpha = 0.8f))
                        )
                    )
                    .padding(top = 40.dp, bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Initials Avatar (No image selection as requested)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (firstName.isNotEmpty()) firstName.take(1) else "?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = VazirFontFamily
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(20.dp))
                    
                    Column {
                        Text(
                            text = if (firstName.isBlank() && lastName.isBlank()) "نام شما" else "$firstName $lastName",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = selectedRoleLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = VazirFontFamily,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            // ── Form Section ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .animateContentSize()
            ) {
                SectionHeader("اطلاعات شناسایی")
                
                ProfileEditField(
                    label = "نام *",
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "نام خود را وارد کنید",
                    imeAction = ImeAction.Next
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileEditField(
                    label = "نام خانوادگی *",
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "نام خانوادگی خود را وارد کنید",
                    imeAction = ImeAction.Done
                )

                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader("نقش و تحصیلات")
                
                // Role Chips
                if (refState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = extendedColors.accent)
                } else {
                    Text(
                        text = "نقش من در آموزش *",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
                                    selectedEducationLevel = ""
                                    selectedFieldOfStudy = ""
                                },
                                modifier = Modifier.weight(1f)
                            )
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
                        val filteredLevels = refState.educationLevels.filter { it.roleValueEn == selectedRoleValueEn || it.roleValueEn.isNullOrBlank() }
                        
                        DropdownSelector(
                            label = "مقطع تحصیلی",
                            selected = selectedEducationLevel,
                            options = filteredLevels.map { it.name },
                            expanded = isLevelExpanded,
                            onExpand = { isLevelExpanded = !isLevelExpanded },
                            onDismiss = { isLevelExpanded = false },
                            onSelect = { 
                                selectedEducationLevel = it
                                selectedFaculty = ""
                                selectedFieldOfStudy = ""
                                isLevelExpanded = false
                            }
                        )

                        // Faculty Dropdown (Dynamic)
                        AnimatedVisibility(
                            visible = filteredFaculties.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                DropdownSelector(
                                    label = "دانشکده",
                                    selected = selectedFaculty,
                                    options = filteredFaculties,
                                    expanded = isFacultyExpanded,
                                    onExpand = { isFacultyExpanded = !isFacultyExpanded },
                                    onDismiss = { isFacultyExpanded = false },
                                    onSelect = {
                                        selectedFaculty = it
                                        isFacultyExpanded = false
                                    }
                                )
                            }
                        }
                        
                        // Major Dropdown (Dynamic)
                        AnimatedVisibility(
                            visible = filteredFields.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                DropdownSelector(
                                    label = "رشته تحصیلی",
                                    selected = selectedFieldOfStudy,
                                    options = filteredFields,
                                    expanded = isFieldExpanded,
                                    onExpand = { isFieldExpanded = !isFieldExpanded },
                                    onDismiss = { isFieldExpanded = false },
                                    onSelect = {
                                        selectedFieldOfStudy = it
                                        isFieldExpanded = false
                                    }
                                )
                        }
                    }
                }
            }

                Spacer(modifier = Modifier.height(48.dp))
                
                PrimaryButton(
                    text = "ذخیره و ادامه",
                    onClick = {
                        authViewModel.updateProfile(
                            firstName = firstName,
                            lastName = lastName,
                            educationalRole = selectedRoleValueEn,
                            gradeLevel = selectedEducationLevel,
                            major = selectedFieldOfStudy,
                            faculty = selectedFaculty,
                            nationalCode = null,
                            username = null,
                            bio = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !authState.isLoading,
                    isLoading = authState.isLoading
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontFamily = VazirFontFamily,
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
            fontFamily = VazirFontFamily,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontFamily = VazirFontFamily, color = Color.Gray) },
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
                fontFamily = VazirFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) extendedColors.accent else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpand() }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = VazirFontFamily) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MessageAppTheme.extendedColors.accent,
                focusedLabelColor = MessageAppTheme.extendedColors.accent
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, fontFamily = VazirFontFamily) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

