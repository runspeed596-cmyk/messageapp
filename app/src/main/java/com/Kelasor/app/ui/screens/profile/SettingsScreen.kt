package com.Kelasor.app.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.R
import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.SettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Ultra-Premium Settings Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onEditAcademyProfileClick: () -> Unit,
    onAcademyProfileClick: (String) -> Unit,
    onAccountClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDataStorageClick: () -> Unit,
    onFoldersClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onWalletClick: () -> Unit,

    onLogoutClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val profileState by profileViewModel.state.collectAsState()
    val user = profileState.user
    val context = LocalContext.current

    val currentLayoutDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        // Removed Search and More menu as requested
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 1. Dual Profile Header ──────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Messenger Profile
                            ProfileCircleWithEdit(
                                imageUrl = user?.avatarUrl,
                                name = user?.displayName ?: "",
                                label = "پیام‌رسان",
                                onClick = onEditProfileClick,
                                accentColor = extendedColors.accent
                            )

                             // Mosbat Elm Profile
                            if (user?.institutionId != null) {
                                ProfileCircleWithEdit(
                                    imageUrl = user.institutionLogoUrl,
                                    name = user.institutionName ?: "آکادمی",
                                    label = "مثبت علم",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { onAcademyProfileClick(user.institutionId) },
                                    accentColor = Color(0xFF2196F3)
                                )
                            } else if (user?.isTeacher == true) {
                                ProfileCircleWithEdit(
                                    imageUrl = user.avatarUrl,
                                    name = user.displayName,
                                    label = "مثبت علم",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { /* Maybe go to teacher personal profile? */ },
                                    accentColor = Color(0xFF2196F3)
                                )
                            } else {
                                AddAcademyProfileCircle(
                                    onClick = onEditAcademyProfileClick,
                                    label = "مثبت علم",
                                    accentColor = Color(0xFF4CAF50)
                                )
                            }
                        }

                        Text(
                            text = user?.displayName ?: "نام کاربری",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = VazirFontFamily
                        )
                        
                        Text(
                            text = if (user != null) "${user.phoneNumber} • @${user.username}" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }

                // ── 2. Wallet Card ──────────────────────────────────────
                item {
                    WalletCard(
                        onClick = onWalletClick,
                        accentColor = extendedColors.accent
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }

                // ── 3. Settings Sections ────────────────────────────────
                item {
                    SettingsSection(
                        items = listOf(
                            SettingsItemData(
                                icon = Icons.Default.Person,
                                title = "حساب کاربری",
                                subtitle = "شماره، نام کاربری، بیوگرافی",
                                iconColor = Color(0xFF2196F3),
                                onClick = onAccountClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.Chat,
                                title = "تنظیمات گفتگو",
                                subtitle = "تصویر زمینه، حالت شب، انیمیشن‌ها",
                                iconColor = Color(0xFFFFA000),
                                onClick = onAppearanceClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.Lock,
                                title = "حریم خصوصی و امنیت",
                                subtitle = "آخرین بازدید، دستگاه‌ها، گذرواژه‌ها",
                                iconColor = Color(0xFF4CAF50),
                                onClick = onPrivacyClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.Notifications,
                                title = "اعلان‌ها",
                                subtitle = "صداها، تماس‌ها، نشان‌ها",
                                iconColor = Color(0xFFF44336),
                                onClick = onNotificationsClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.PieChart,
                                title = "داده‌ها و ذخیره‌سازی",
                                subtitle = "تنظیمات دانلود خودکار رسانه‌ها",
                                iconColor = Color(0xFF00BCD4),
                                onClick = onDataStorageClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.Folder,
                                title = "پوشه‌های گفتگو",
                                subtitle = "مرتب‌سازی گفتگوها در پوشه‌ها",
                                iconColor = Color(0xFF2196F3),
                                onClick = onFoldersClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.Devices,
                                title = "دستگاه‌ها",
                                subtitle = "مدیریت دستگاه‌های متصل",
                                iconColor = Color(0xFF009688),
                                onClick = onDevicesClick
                            ),
                            SettingsItemData(
                                icon = Icons.Default.BatteryChargingFull,
                                title = "صرفه‌جویی در باتری",
                                subtitle = "کاهش مصرف انرژی",
                                iconColor = Color(0xFFFF9800),
                                onClick = { /* Power Saving */ }
                            )
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }


                // ── 5. Logout ───────────────────────────────────────────
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            onLogoutClick()
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "خروج از حساب",
                            color = Color.Red,
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ── 6. App Version ──────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "کلاسور برای اندروید نسخه ۱.۰.۰",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCircleWithEdit(
    imageUrl: String?,
    name: String,
    label: String,
    onClick: () -> Unit,
    onAvatarClick: (() -> Unit)? = null,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            // Main Avatar
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable(onClick = onAvatarClick ?: onClick)
            ) {
                AvatarImage(
                    imageUrl = imageUrl,
                    name = name,
                    size = AvatarSize.XLARGE,
                    modifier = Modifier.size(110.dp)
                )
            }
            
            // Frosted glass "مشاهده" overlay for Mosbat Elm circle - CENTERED & BLURRED
            if (onAvatarClick != null) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp, 30.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .blur(20.dp) // Glass effect
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(15.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.size(70.dp, 30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "مشاهده",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = VazirFontFamily
                            )
                        }
                    }
                }
            }
            
            // Clean camera edit badge — single layer, no stacked shadows
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit",
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = VazirFontFamily
        )
        
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.1f),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "ویرایش",
                color = accentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AddAcademyProfileCircle(
    onClick: () -> Unit,
    label: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f))
                .border(2.dp, accentColor.copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = accentColor,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = VazirFontFamily
        )
        
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = accentColor.copy(alpha = 0.1f),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "برگزارکننده شو",
                color = accentColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun WalletCard(
    onClick: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8E24AA).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "کیف پول من",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "مدیریت دارایی و تراکنش‌ها",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily
                )
            }
            
            Text(
                text = "۰ تومان",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily
            )
        }
    }
}

data class SettingsItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun SettingsSection(
    title: String? = null,
    items: List<SettingsItemData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(vertical = 8.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                color = Color(0xFF2196F3),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        items.forEachIndexed { index, item ->
            TelegramSettingsRow(
                icon = item.icon,
                title = item.title,
                subtitle = item.subtitle,
                iconColor = item.iconColor,
                onClick = item.onClick
            )
            
            if (index < items.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun TelegramSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = VazirFontFamily
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontFamily = VazirFontFamily
                )
            }
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎴 Premium Profile Card — Hero header with gradient accent
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumProfileCard(
    user: com.Kelasor.app.domain.model.User?,
    onEditClick: () -> Unit,
    extendedColors: com.Kelasor.app.ui.theme.ExtendedColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = extendedColors.accent.copy(alpha = 0.15f),
                spotColor = extendedColors.accent.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (user != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditClick)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    imageUrl = user.avatarUrl,
                    name = user.displayName,
                    size = AvatarSize.LARGE,
                    hasBorder = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = VazirFontFamily
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (user.username.isNotEmpty()) "@${user.username}" else user.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = VazirFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(extendedColors.accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = extendedColors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Shimmer placeholder
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🃏 Elevated Settings Card Container
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumSettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Premium Section Header — Gradient icon badge + animated chevron
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumSectionHeader(
    icon: ImageVector,
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    gradientColors: List<Color>
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chevron"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(brush = Brush.linearGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontFamily = VazirFontFamily,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .rotate(chevronRotation)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📄 Premium Settings Row — Icon badge + title/subtitle + chevron
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String = "",
    iconColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontFamily = VazirFontFamily,
                color = textColor
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔘 Premium Radio Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumRadioRow(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "radioScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = accentColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontFamily = VazirFontFamily,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔒 Privacy Option Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PrivacyOptionRow(
    title: String,
    selectedOption: String,
    options: List<Pair<String, String>>,
    onOptionSelected: (String) -> Unit,
    accentColor: Color
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        options.forEach { (key, label) ->
            PremiumRadioRow(
                label = label,
                isSelected = selectedOption == key,
                accentColor = accentColor,
                onClick = { onOptionSelected(key) }
            )
        }
    }
}
