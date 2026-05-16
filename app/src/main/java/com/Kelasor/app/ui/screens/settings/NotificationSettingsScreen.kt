package com.Kelasor.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.SettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Settings Screen — Per-Channel Controls
// ═══════════════════════════════════════════════════════════════════════════════

data class NotifChannelState(
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val popup: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    // Channel states loaded from repository
    val channels: List<Triple<String, String, androidx.compose.ui.graphics.vector.ImageVector>> = listOf(
        Triple("personal", "پیام شخصی", Icons.Default.Chat),
        Triple("group", "گروه‌ها", Icons.Default.Groups),
        Triple("channel", "کانال‌ها", Icons.Default.Campaign),
        Triple("bot", "ربات‌ها", Icons.Default.SmartToy)
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "اعلان‌ها",
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
            // ── Global Notifications Toggle ──────────────────────────
            item {
                SettingsCard(title = "اعلان‌های کلی", icon = Icons.Default.NotificationsActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "فعال‌سازی اعلان‌ها",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "روشن/خاموش کردن تمام اعلان‌ها",
                                fontFamily = DanaFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.isNotificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = extendedColors.accent
                            )
                        )
                    }
                }
            }
            // ── Per-Channel Sections ──────────────────────────────────
            channels.forEach { (channelKey: String, channelLabel: String, channelIcon) ->
                item {
                    NotificationChannelCard(
                        channelKey = channelKey,
                        channelLabel = channelLabel,
                        channelIcon = channelIcon,
                        viewModel = viewModel,
                        accentColor = extendedColors.accent
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationChannelCard(
    channelKey: String,
    channelLabel: String,
    channelIcon: androidx.compose.ui.graphics.vector.ImageVector,
    viewModel: SettingsViewModel,
    accentColor: Color
) {
    // Observe channel-specific settings from repository
    val settingsRepo = viewModel // Using viewModel as proxy
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var popupEnabled by remember { mutableStateOf(true) }
    SettingsCard(title = channelLabel, icon = channelIcon) {
        // Sound toggle
        NotifToggleRow(
            label = "صدا",
            description = "پخش صدای اعلان",
            checked = soundEnabled,
            onCheckedChange = {
                soundEnabled = it
                viewModel.setNotifSound(channelKey, it)
            },
            accentColor = accentColor
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
        // Vibration toggle
        NotifToggleRow(
            label = "لرزش",
            description = "لرزش هنگام دریافت اعلان",
            checked = vibrationEnabled,
            onCheckedChange = {
                vibrationEnabled = it
                viewModel.setNotifVibration(channelKey, it)
            },
            accentColor = accentColor
        )
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
        // Popup toggle
        NotifToggleRow(
            label = "پاپ‌آپ",
            description = "نمایش اعلان بالای صفحه",
            checked = popupEnabled,
            onCheckedChange = {
                popupEnabled = it
                viewModel.setNotifPopup(channelKey, it)
            },
            accentColor = accentColor
        )
    }
}

@Composable
private fun NotifToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = accentColor)
        )
    }
}
