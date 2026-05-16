package com.Kelasor.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.SettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 🔒 Privacy & Security Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToPrivacyExceptions: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    // PIN dialog state
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "حریم خصوصی و امنیت",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Profile Photo Visibility ──────────────────────────────
            item {
                PrivacySettingCard(
                    title = "دیدن تصویر پروفایل",
                    icon = Icons.Default.AccountCircle,
                    currentValue = state.profileVisibility,
                    onValueChange = { viewModel.setProfileVisibility(it) },
                    onNavigateToExceptions = { onNavigateToPrivacyExceptions("profile") },
                    accentColor = extendedColors.accent
                )
            }
            // ── Last Seen Visibility ──────────────────────────────────
            item {
                PrivacySettingCard(
                    title = "آخرین بازدید",
                    icon = Icons.Default.AccessTime,
                    currentValue = state.lastSeenVisibility,
                    onValueChange = { viewModel.setLastSeenVisibility(it) },
                    onNavigateToExceptions = { onNavigateToPrivacyExceptions("last_seen") },
                    accentColor = extendedColors.accent
                )
            }
            // ── Bio Visibility ──────────────────────────────────────
            item {
                PrivacySettingCard(
                    title = "بیوگرافی",
                    icon = Icons.Default.Info,
                    currentValue = state.bioVisibility,
                    onValueChange = { viewModel.setBioVisibility(it) },
                    onNavigateToExceptions = { onNavigateToPrivacyExceptions("bio") },
                    accentColor = extendedColors.accent
                )
            }
            // ── Phone Number Visibility ──────────────────────────────
            item {
                PrivacySettingCard(
                    title = "شماره تلفن",
                    icon = Icons.Default.Phone,
                    currentValue = state.phoneVisibility,
                    onValueChange = { viewModel.setPhoneVisibility(it) },
                    onNavigateToExceptions = { onNavigateToPrivacyExceptions("phone") },
                    accentColor = extendedColors.accent
                )
            }
            // ── Online Status Visibility ──────────────────────────────
            item {
                PrivacySettingCard(
                    title = "وضعیت آنلاین",
                    icon = Icons.Default.Circle,
                    currentValue = state.onlineVisibility,
                    onValueChange = { viewModel.setOnlineVisibility(it) },
                    onNavigateToExceptions = { onNavigateToPrivacyExceptions("online") },
                    accentColor = extendedColors.accent
                )
            }
            // ── PIN Lock ──────────────────────────────────────────────
            item {
                SettingsCard(title = "رمز PIN", icon = Icons.Default.Lock) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "قفل با رمز ۴ رقمی",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.isPinLockEnabled) "فعال" else "غیرفعال",
                                fontFamily = DanaFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isPinLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    showPinDialog = true
                                } else {
                                    viewModel.setPinLockEnabled(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = extendedColors.accent
                            )
                        )
                    }
                }
            }
            // ── Blocked Users ──────────────────────────────────────────
            item {
                SettingsCard(title = "افراد مسدود شده", icon = Icons.Default.Block) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToBlockedUsers() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "مشاهده لیست افراد مسدود شده",
                            fontFamily = DanaFontFamily,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    // ── PIN Dialog ──────────────────────────────────────────────
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false; pinInput = "" },
            title = {
                Text("تنظیم رمز PIN", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "یک رمز ۴ رقمی وارد کنید:",
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        placeholder = { Text("• • • •", fontFamily = DanaFontFamily) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.setPinLockEnabled(true, pinInput)
                            showPinDialog = false
                            pinInput = ""
                        }
                    },
                    enabled = pinInput.length == 4
                ) {
                    Text("تایید", fontFamily = DanaFontFamily, color = extendedColors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false; pinInput = "" }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 Privacy Setting Card with 4 visibility options
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrivacySettingCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentValue: String,
    onValueChange: (String) -> Unit,
    onNavigateToExceptions: () -> Unit,
    accentColor: Color
) {
    val options: List<Pair<String, String>> = listOf(
        SettingsRepository.VISIBILITY_EVERYONE to "همه",
        SettingsRepository.VISIBILITY_CONTACTS to "فقط مخاطبین",
        SettingsRepository.VISIBILITY_CONTACTS_EXCEPT to "مخاطبین به‌جز",
        SettingsRepository.VISIBILITY_NOBODY to "هیچ‌کس"
    )
    var isExpanded by remember { mutableStateOf(false) }
    val displayLabel: String = options.find { it.first == currentValue }?.second ?: "همه"
    SettingsCard(title = title, icon = icon) {
        // Current selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayLabel,
                fontFamily = DanaFontFamily,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Expandable options
        if (isExpanded) {
            options.forEach { (value: String, label: String) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onValueChange(value)
                            if (value != SettingsRepository.VISIBILITY_CONTACTS_EXCEPT) {
                                isExpanded = false
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = DanaFontFamily,
                            color = if (currentValue == value) accentColor else MaterialTheme.colorScheme.onSurface
                        )
                        // "Contacts Except" button
                        if (value == SettingsRepository.VISIBILITY_CONTACTS_EXCEPT && currentValue == value) {
                            FilledTonalButton(
                                onClick = onNavigateToExceptions,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "انتخاب مخاطبین",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    RadioButton(
                        selected = currentValue == value,
                        onClick = { onValueChange(value) },
                        colors = RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                }
            }
        }
    }
}
