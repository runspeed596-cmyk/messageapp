package com.Kelasor.app.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.SettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 🌙 Chat Settings Screen — Night Mode, Animations, Wallpaper
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "تنظیمات گفتگو",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
            // ── Night Mode Section ──────────────────────────────────────
            item {
                SettingsCard(title = "حالت شب", icon = Icons.Default.DarkMode) {
                    val themeOptions: List<Pair<String, String>> = listOf(
                        SettingsRepository.THEME_MODE_LIGHT to "روشن",
                        SettingsRepository.THEME_MODE_DARK to "تاریک",
                        SettingsRepository.THEME_MODE_SYSTEM to "خودکار (سیستم)"
                    )
                    themeOptions.forEach { (mode: String, label: String) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontFamily = DanaFontFamily,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            RadioButton(
                                selected = state.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = extendedColors.accent
                                )
                            )
                        }
                    }
                }
            }

            // ── Color Palette Section ──────────────────────────────────────
            item {
                SettingsCard(title = "پالت رنگ", icon = Icons.Default.Palette) {
                    val palettes: List<Pair<String, Color>> = listOf(
                        SettingsRepository.PALETTE_DEFAULT to Color(0xFF6366F1),
                        SettingsRepository.PALETTE_OCEAN to Color(0xFF0EA5E9),
                        SettingsRepository.PALETTE_SUNSET to Color(0xFFF97316),
                        SettingsRepository.PALETTE_FOREST to Color(0xFF22C55E),
                        SettingsRepository.PALETTE_LAVENDER to Color(0xFFA855F7)
                    )
                    val paletteLabels: Map<String, String> = mapOf(
                        SettingsRepository.PALETTE_DEFAULT to "پیش‌فرض",
                        SettingsRepository.PALETTE_OCEAN to "اقیانوس",
                        SettingsRepository.PALETTE_SUNSET to "غروب",
                        SettingsRepository.PALETTE_FOREST to "جنگل",
                        SettingsRepository.PALETTE_LAVENDER to "بنفش"
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(palettes) { (palette: String, color: Color) ->
                            val isSelected: Boolean = state.colorPalette == palette
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) color else color.copy(alpha = 0.15f),
                                label = "paletteColor"
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { viewModel.setColorPalette(palette) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, color, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = paletteLabels[palette] ?: "",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 11.sp,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Reusable Settings Card Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MessageAppTheme.extendedColors.accent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}
