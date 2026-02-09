package com.Kelasor.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.R
import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.SettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Settings Screen (Replaces Profile Tab)
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSavedMessagesClick: (String) -> Unit,
    onArchivedChatsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val settingsState by viewModel.state.collectAsState()
    val profileState by profileViewModel.state.collectAsState()
    val user = profileState.user
    
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showProfileVisibilityDialog by remember { mutableStateOf(false) }
    var showOnlineVisibilityDialog by remember { mutableStateOf(false) }
    var showPhoneVisibilityDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showColorPaletteDialog by remember { mutableStateOf(false) }
    
    val currentLayoutDirection = LocalLayoutDirection.current
    val fontFamily = VazirFontFamily
    
    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                
                // 1. User Profile Header
                item {
                    if (user != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onEditProfileClick)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                imageUrl = user.avatarUrl,
                                name = user.displayName,
                                size = AvatarSize.LARGE
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = fontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (user.username.isNotEmpty()) "@${user.username}" else user.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = fontFamily
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = extendedColors.accent
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                
                // 2. Account Actions
                item {
                    SectionTitle("حساب کاربری", fontFamily)
                    if (user != null) {
                        SettingsItem(
                            icon = Icons.Default.Bookmark,
                            title = "پیام‌های ذخیره شده",
                            subtitle = "باز کردن چت شخصی",
                            onClick = { onSavedMessagesClick(user.id) },
                            fontFamily = fontFamily
                        )
                    }
                    SettingsItem(
                        icon = Icons.Default.Archive,
                        title = "چت‌های آرشیو شده",
                        subtitle = "مشاهده آرشیو",
                        onClick = onArchivedChatsClick,
                        fontFamily = fontFamily
                    )
                }

                // 3. Appearance section
                item {
                    SectionTitle(stringResource(R.string.appearance), fontFamily)
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = "تم",
                        subtitle = when (settingsState.themeMode) {
                            SettingsRepository.THEME_MODE_LIGHT -> "روشن"
                            SettingsRepository.THEME_MODE_DARK -> "تاریک"
                            else -> "سیستم"
                        },
                        onClick = { showThemeDialog = true },
                        fontFamily = fontFamily
                    )
                }

                // 4. Privacy & Security section
                item {
                    SectionTitle(stringResource(R.string.privacy_security), fontFamily)
                    SettingsItem(
                        icon = Icons.Default.Person,
                        title = stringResource(R.string.profile_visibility),
                        subtitle = when (settingsState.profileVisibility) {
                            "everyone" -> stringResource(R.string.everyone)
                            "contacts" -> stringResource(R.string.my_contacts)
                            else -> stringResource(R.string.nobody)
                        },
                        onClick = { showProfileVisibilityDialog = true },
                        fontFamily = fontFamily
                    )
                    SettingsItem(
                        icon = Icons.Default.Circle,
                        title = stringResource(R.string.online_visibility),
                        subtitle = when (settingsState.onlineVisibility) {
                            "everyone" -> stringResource(R.string.everyone)
                            "contacts" -> stringResource(R.string.my_contacts)
                            else -> stringResource(R.string.nobody)
                        },
                        onClick = { showOnlineVisibilityDialog = true },
                        fontFamily = fontFamily
                    )
                    SettingsItem(
                        icon = Icons.Default.Phone,
                        title = stringResource(R.string.phone_visibility),
                        subtitle = when (settingsState.phoneVisibility) {
                            "everyone" -> stringResource(R.string.everyone)
                            else -> stringResource(R.string.my_contacts)
                        },
                        onClick = { showPhoneVisibilityDialog = true },
                        fontFamily = fontFamily
                    )
                    
                    // PIN Lock Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "قفل امنیتی (PIN)",
                                    fontFamily = fontFamily,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (settingsState.isPinLockEnabled) "فعال" else "غیرفعال",
                                    fontFamily = fontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settingsState.isPinLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinSetupDialog = true
                                } else {
                                    viewModel.setPinLockEnabled(false)
                                }
                            }
                        )
                    }
                }
                
                // 5. Appearance section (Color Palettes)
                item {
                    SectionTitle("ظاهر برنامه", fontFamily)
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "پالت رنگی",
                        subtitle = when (settingsState.colorPalette) {
                            SettingsRepository.PALETTE_OCEAN -> "اقیانوس"
                            SettingsRepository.PALETTE_SUNSET -> "غروب"
                            SettingsRepository.PALETTE_FOREST -> "جنگل"
                            SettingsRepository.PALETTE_LAVENDER -> "اسطوخودوس"
                            else -> "پیش‌فرض"
                        },
                        onClick = { showColorPaletteDialog = true },
                        fontFamily = fontFamily
                    )
                }
                
                // 6. About section
                item {
                    SectionTitle(stringResource(R.string.about), fontFamily)
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.about_app),
                        subtitle = stringResource(R.string.about_app_subtitle),
                        onClick = { showAboutDialog = true },
                        fontFamily = fontFamily
                    )
                }
                
                // 6. Logout
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "خروج از حساب کاربری",
                        subtitle = "",
                        onClick = {
                            authViewModel.logout()
                            onLogoutClick()
                        },
                        fontFamily = fontFamily,
                        iconColor = MaterialTheme.colorScheme.error,
                        textColor = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        VisibilitySelectionDialog(
            title = "تم",
            selectedOption = settingsState.themeMode,
            options = listOf(
                SettingsRepository.THEME_MODE_LIGHT to "روشن",
                SettingsRepository.THEME_MODE_DARK to "تاریک",
                SettingsRepository.THEME_MODE_SYSTEM to "سیستم"
            ),
            onOptionSelected = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
            fontFamily = fontFamily,
            accentColor = extendedColors.accent
        )
    }
    
    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.about_dialog_title),
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.about_app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.about_version),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = fontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.about_description),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = fontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(
                        text = stringResource(R.string.close),
                        fontFamily = fontFamily
                    )
                }
            }
        )
    }
    
    // Profile Visibility Dialog
    if (showProfileVisibilityDialog) {
        VisibilitySelectionDialog(
            title = stringResource(R.string.profile_visibility),
            selectedOption = settingsState.profileVisibility,
            options = listOf(
                "everyone" to stringResource(R.string.everyone),
                "contacts" to stringResource(R.string.my_contacts),
                "nobody" to stringResource(R.string.nobody)
            ),
            onOptionSelected = {
                viewModel.setProfileVisibility(it)
                showProfileVisibilityDialog = false
            },
            onDismiss = { showProfileVisibilityDialog = false },
            fontFamily = fontFamily,
            accentColor = extendedColors.accent
        )
    }
    
    // Online Visibility Dialog
    if (showOnlineVisibilityDialog) {
        VisibilitySelectionDialog(
            title = stringResource(R.string.online_visibility),
            selectedOption = settingsState.onlineVisibility,
            options = listOf(
                "everyone" to stringResource(R.string.everyone),
                "contacts" to stringResource(R.string.my_contacts),
                "nobody" to stringResource(R.string.nobody)
            ),
            onOptionSelected = {
                viewModel.setOnlineVisibility(it)
                showOnlineVisibilityDialog = false
            },
            onDismiss = { showOnlineVisibilityDialog = false },
            fontFamily = fontFamily,
            accentColor = extendedColors.accent
        )
    }
    
    // Phone Visibility Dialog
    if (showPhoneVisibilityDialog) {
        VisibilitySelectionDialog(
            title = stringResource(R.string.phone_visibility),
            selectedOption = settingsState.phoneVisibility,
            options = listOf(
                "everyone" to stringResource(R.string.everyone),
                "contacts" to stringResource(R.string.my_contacts)
            ),
            onOptionSelected = {
                viewModel.setPhoneVisibility(it)
                showPhoneVisibilityDialog = false
            },
            onDismiss = { showPhoneVisibilityDialog = false },
            fontFamily = fontFamily,
            accentColor = extendedColors.accent
        )
    }
    
    // PIN Setup Dialog
    if (showPinSetupDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinSetupDialog = false
                pinInput = ""
            },
            title = {
                Text(
                    text = "تنظیم رمز ۴ رقمی",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "یک رمز ۴ رقمی وارد کنید:",
                        fontFamily = fontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("رمز PIN", fontFamily = fontFamily) },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                        ),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.setPinLockEnabled(true, pinInput)
                            showPinSetupDialog = false
                            pinInput = ""
                        }
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("تایید", fontFamily = fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPinSetupDialog = false
                    pinInput = ""
                }) {
                    Text("انصراف", fontFamily = fontFamily)
                }
            }
        )
    }
    
    // Color Palette Dialog
    if (showColorPaletteDialog) {
        val paletteOptions = listOf(
            SettingsRepository.PALETTE_DEFAULT to "پیش‌فرض",
            SettingsRepository.PALETTE_OCEAN to "اقیانوس",
            SettingsRepository.PALETTE_SUNSET to "غروب",
            SettingsRepository.PALETTE_FOREST to "جنگل",
            SettingsRepository.PALETTE_LAVENDER to "اسطوخودوس"
        )
        AlertDialog(
            onDismissRequest = { showColorPaletteDialog = false },
            title = {
                Text(
                    text = "انتخاب پالت رنگی",
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    paletteOptions.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setColorPalette(key)
                                    showColorPaletteDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settingsState.colorPalette == key,
                                onClick = {
                                    viewModel.setColorPalette(key)
                                    showColorPaletteDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = extendedColors.accent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontFamily = fontFamily,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPaletteDialog = false }) {
                    Text("بستن", fontFamily = fontFamily)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String, fontFamily: androidx.compose.ui.text.font.FontFamily?) {
    Text(
        text = title,
        style = MessageAppTypography.sectionTitle.copy(fontFamily = fontFamily),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MessageAppTypography.chatName.copy(fontFamily = fontFamily),
                color = textColor
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MessageAppTypography.chatTime.copy(fontFamily = fontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun VisibilitySelectionDialog(
    title: String,
    selectedOption: String,
    options: List<Pair<String, String>>, // key to display label
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    accentColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                options.forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(key) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == key,
                            onClick = { onOptionSelected(key) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = accentColor
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = fontFamily
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.close),
                    fontFamily = fontFamily
                )
            }
        }
    )
}
