package com.Kelasor.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    // Expandable section states
    var isAccountExpanded by remember { mutableStateOf(false) }
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    var isPrivacyExpanded by remember { mutableStateOf(false) }
    val currentLayoutDirection = LocalLayoutDirection.current
    val fontFamily = VazirFontFamily

    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                    } else {
                        // Loading placeholder
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.5f)
                                        .height(16.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.3f)
                                        .height(12.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // ══════════════════════════════════════════════════════
                // 2. حساب کاربری (Account) — Expandable
                // ══════════════════════════════════════════════════════
                item {
                    ExpandableSectionHeader(
                        icon = Icons.Default.Person,
                        title = "حساب کاربری",
                        isExpanded = isAccountExpanded,
                        onClick = { isAccountExpanded = !isAccountExpanded },
                        fontFamily = fontFamily,
                        accentColor = extendedColors.accent
                    )
                    AnimatedVisibility(
                        visible = isAccountExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
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
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // ══════════════════════════════════════════════════════
                // 3. ظاهر (Appearance) — Expandable with inline theme + palette
                // ══════════════════════════════════════════════════════
                item {
                    ExpandableSectionHeader(
                        icon = Icons.Default.Palette,
                        title = "ظاهر",
                        isExpanded = isAppearanceExpanded,
                        onClick = { isAppearanceExpanded = !isAppearanceExpanded },
                        fontFamily = fontFamily,
                        accentColor = extendedColors.accent
                    )
                    AnimatedVisibility(
                        visible = isAppearanceExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
                            // Theme selection — inline radio buttons
                            Text(
                                text = "تم",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            val themeOptions = listOf(
                                SettingsRepository.THEME_MODE_SYSTEM to "سیستم",
                                SettingsRepository.THEME_MODE_LIGHT to "روشن",
                                SettingsRepository.THEME_MODE_DARK to "تاریک"
                            )
                            themeOptions.forEach { (key, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setThemeMode(key) }
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settingsState.themeMode == key,
                                        onClick = { viewModel.setThemeMode(key) },
                                        colors = RadioButtonDefaults.colors(selectedColor = extendedColors.accent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontFamily = fontFamily,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            // Color palette selection — inline radio buttons
                            Text(
                                text = "پالت رنگی",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            val paletteOptions = listOf(
                                SettingsRepository.PALETTE_DEFAULT to "پیش‌فرض",
                                SettingsRepository.PALETTE_OCEAN to "اقیانوس",
                                SettingsRepository.PALETTE_SUNSET to "غروب",
                                SettingsRepository.PALETTE_FOREST to "جنگل",
                                SettingsRepository.PALETTE_LAVENDER to "اسطوخودوس"
                            )
                            paletteOptions.forEach { (key, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setColorPalette(key) }
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settingsState.colorPalette == key,
                                        onClick = { viewModel.setColorPalette(key) },
                                        colors = RadioButtonDefaults.colors(selectedColor = extendedColors.accent)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = label,
                                        fontFamily = fontFamily,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // ══════════════════════════════════════════════════════
                // 4. حریم خصوصی و امنیت (Privacy & Security) — Expandable
                // ══════════════════════════════════════════════════════
                item {
                    ExpandableSectionHeader(
                        icon = Icons.Default.Lock,
                        title = "حریم خصوصی و امنیت",
                        isExpanded = isPrivacyExpanded,
                        onClick = { isPrivacyExpanded = !isPrivacyExpanded },
                        fontFamily = fontFamily,
                        accentColor = extendedColors.accent
                    )
                    AnimatedVisibility(
                        visible = isPrivacyExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
                            // Profile Visibility
                            PrivacyOptionRow(
                                title = stringResource(R.string.profile_visibility),
                                selectedOption = settingsState.profileVisibility,
                                options = listOf(
                                    "everyone" to stringResource(R.string.everyone),
                                    "contacts" to stringResource(R.string.my_contacts),
                                    "nobody" to stringResource(R.string.nobody)
                                ),
                                onOptionSelected = { viewModel.setProfileVisibility(it) },
                                fontFamily = fontFamily,
                                accentColor = extendedColors.accent
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            // Online Visibility
                            PrivacyOptionRow(
                                title = stringResource(R.string.online_visibility),
                                selectedOption = settingsState.onlineVisibility,
                                options = listOf(
                                    "everyone" to stringResource(R.string.everyone),
                                    "contacts" to stringResource(R.string.my_contacts),
                                    "nobody" to stringResource(R.string.nobody)
                                ),
                                onOptionSelected = { viewModel.setOnlineVisibility(it) },
                                fontFamily = fontFamily,
                                accentColor = extendedColors.accent
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            // Phone Visibility
                            PrivacyOptionRow(
                                title = stringResource(R.string.phone_visibility),
                                selectedOption = settingsState.phoneVisibility,
                                options = listOf(
                                    "everyone" to stringResource(R.string.everyone),
                                    "contacts" to stringResource(R.string.my_contacts)
                                ),
                                onOptionSelected = { viewModel.setPhoneVisibility(it) },
                                fontFamily = fontFamily,
                                accentColor = extendedColors.accent
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // ══════════════════════════════════════════════════════
                // 5. Logout
                // ══════════════════════════════════════════════════════
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

            com.Kelasor.app.ui.components.SystemBarBackdrop()
        }
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
}

// ═══════════════════════════════════════════════════════════════════════════════
// Expandable Section Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ExpandableSectionHeader(
    icon: ImageVector,
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MessageAppTypography.chatName.copy(fontFamily = fontFamily),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Privacy Option Row — inline radio buttons with title
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrivacyOptionRow(
    title: String,
    selectedOption: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit,
    fontFamily: androidx.compose.ui.text.font.FontFamily?,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        options.forEach { (key, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(key) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == key,
                    onClick = { onOptionSelected(key) },
                    colors = RadioButtonDefaults.colors(selectedColor = accentColor)
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
}

// ═══════════════════════════════════════════════════════════════════════════════
// Settings Item (reused for sub-items)
// ═══════════════════════════════════════════════════════════════════════════════

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
