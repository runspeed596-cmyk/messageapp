package com.Kelasor.app.ui.screens.settings

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import com.Kelasor.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerSettingsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAccountClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDataStorageClick: () -> Unit,
    onDevicesClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val profileState by profileViewModel.state.collectAsState()
    val user = profileState.user

    val currentLayoutDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = onNavigateBack)
                                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                "پروفایل پیام رسان",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(start = 4.dp)
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
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 1. Messenger Profile ──────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MessengerProfileCircleWithEdit(
                            imageUrl = user?.avatarUrl,
                            name = user?.displayName ?: "",
                            label = "پروفایل من",
                            onClick = onEditProfileClick,
                            accentColor = extendedColors.accent
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user?.displayName ?: "نام کاربری",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = DanaFontFamily
                        )
                        
                        Text(
                            text = if (user != null) "${user.phoneNumber} • @${user.username}" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = DanaFontFamily
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ── 2. Settings Sections ────────────────────────────────
                item {
                    MessengerSettingsSection(
                        items = listOf(
                            MessengerSettingsItemData(
                                icon = Icons.Default.Person,
                                title = "حساب کاربری",
                                subtitle = "شماره، نام کاربری، بیوگرافی",
                                iconColor = Color(0xFF2196F3),
                                onClick = onAccountClick
                            ),
                            MessengerSettingsItemData(
                                icon = Icons.Default.Chat,
                                title = "تنظیمات گفتگو",
                                subtitle = "تصویر زمینه، حالت شب، انیمیشن‌ها",
                                iconColor = Color(0xFFFFA000),
                                onClick = onAppearanceClick
                            ),
                            MessengerSettingsItemData(
                                icon = Icons.Default.Lock,
                                title = "حریم خصوصی و امنیت",
                                subtitle = "تصویر پروفایل، آخرین بازدید، PIN",
                                iconColor = Color(0xFF4CAF50),
                                onClick = onPrivacyClick
                            ),
                            MessengerSettingsItemData(
                                icon = Icons.Default.Notifications,
                                title = "اعلان‌ها",
                                subtitle = "صداها، لرزش، پاپ‌آپ‌ها",
                                iconColor = Color(0xFFF44336),
                                onClick = onNotificationsClick
                            ),
                            MessengerSettingsItemData(
                                icon = Icons.Default.PieChart,
                                title = "داده‌ها و ذخیره‌سازی",
                                subtitle = "حافظه کش و مصرف اینترنت",
                                iconColor = Color(0xFF00BCD4),
                                onClick = onDataStorageClick
                            ),
                            MessengerSettingsItemData(
                                icon = Icons.Default.Devices,
                                title = "دستگاه‌ها",
                                subtitle = "مدیریت دستگاه‌های متصل",
                                iconColor = Color(0xFF009688),
                                onClick = onDevicesClick
                            )
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ── 3. Logout ───────────────────────────────────────────
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
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessengerProfileCircleWithEdit(
    imageUrl: String?,
    name: String,
    label: String,
    onClick: () -> Unit,
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
                    .clickable(onClick = onClick)
            ) {
                AvatarImage(
                    imageUrl = imageUrl,
                    name = name,
                    size = AvatarSize.XLARGE,
                    modifier = Modifier.size(110.dp)
                )
            }
            
            // Clean camera edit badge
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
            fontFamily = DanaFontFamily
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
                fontFamily = DanaFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

data class MessengerSettingsItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun MessengerSettingsSection(
    title: String? = null,
    items: List<MessengerSettingsItemData>
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
                fontFamily = DanaFontFamily,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        items.forEachIndexed { index, item ->
            MessengerSettingsRow(
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
private fun MessengerSettingsRow(
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
                fontFamily = DanaFontFamily
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontFamily = DanaFontFamily
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
