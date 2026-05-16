package com.Kelasor.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
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
// 💾 Data & Storage Screen — Cache & Network Usage
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    var showClearCacheDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.calculateCacheSize()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "داده‌ها و ذخیره‌سازی",
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
            // ── Cache Storage ──────────────────────────────────────────
            item {
                SettingsCard(title = "حافظه کش", icon = Icons.Default.Storage) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "حجم فایل‌های کش",
                                    fontFamily = DanaFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = formatFileSize(state.cacheSize),
                                    fontFamily = DanaFontFamily,
                                    fontSize = 14.sp,
                                    color = extendedColors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Cache size visual bar
                        val cachePercent: Float = (state.cacheSize.toFloat() / (500 * 1024 * 1024)).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { cachePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = extendedColors.accent,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showClearCacheDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            enabled = !state.isClearingCache && state.cacheSize > 0
                        ) {
                            if (state.isClearingCache) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isClearingCache) "در حال پاک‌سازی..." else "پاک‌سازی کش",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            // ── Network Usage ──────────────────────────────────────────
            item {
                SettingsCard(title = "مصرف اینترنت", icon = Icons.Default.DataUsage) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "مصرف داده از زمان نصب",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // WiFi usage
                        NetworkUsageRow(
                            label = "WiFi",
                            icon = Icons.Default.Wifi,
                            sent = formatFileSize(state.wifiSentData),
                            received = formatFileSize(state.wifiReceivedData),
                            iconColor = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Mobile usage
                        NetworkUsageRow(
                            label = "دیتای موبایل",
                            icon = Icons.Default.SignalCellularAlt,
                            sent = formatFileSize(state.mobileSentData),
                            received = formatFileSize(state.mobileReceivedData),
                            iconColor = Color(0xFF2196F3)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetNetworkUsage() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("بازنشانی آمار مصرف", fontFamily = DanaFontFamily)
                        }
                    }
                }
            }
            // ── Auto-Download Settings ────────────────────────────────
            item {
                SettingsCard(title = "دانلود خودکار رسانه‌ها", icon = Icons.Default.Download) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        AutoDownloadRow(
                            label = "تصاویر", 
                            description = state.autoDownloadImages,
                            options = listOf("هیچ‌وقت", "فقط WiFi", "WiFi و دیتای موبایل"),
                            onSelect = { viewModel.setAutoDownloadImages(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                        AutoDownloadRow(
                            label = "ویدیوها", 
                            description = state.autoDownloadVideos,
                            options = listOf("هیچ‌وقت", "فقط WiFi", "WiFi و دیتای موبایل"),
                            onSelect = { viewModel.setAutoDownloadVideos(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                        )
                        AutoDownloadRow(
                            label = "فایل‌ها", 
                            description = state.autoDownloadFiles,
                            options = listOf("هیچ‌وقت", "فقط WiFi", "WiFi و دیتای موبایل"),
                            onSelect = { viewModel.setAutoDownloadFiles(it) }
                        )
                    }
                }
            }
        }
    }
    // ── Clear Cache Confirmation Dialog ──────────────────────────
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = {
                Text("پاک‌سازی حافظه کش", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "آیا از پاک‌سازی ${formatFileSize(state.cacheSize)} حافظه کش اطمینان دارید؟\nاین عمل قابل بازگشت نیست.",
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCache()
                    showClearCacheDialog = false
                }) {
                    Text("پاک‌سازی", fontFamily = DanaFontFamily, color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }
}

@Composable
private fun NetworkUsageRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    sent: String,
    received: String,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "ارسال: $sent",
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "دریافت: $received",
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AutoDownloadRow(
    label: String, 
    description: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = DanaFontFamily) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
