package com.Kelasor.app.ui.screens.settings

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
import com.Kelasor.app.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosbatElmSettingsScreen(
    onNavigateBack: () -> Unit,
    onEditAcademyProfileClick: () -> Unit,
    onAcademyProfileClick: (String) -> Unit,
    onAcademyAnalyticsClick: (String) -> Unit = {},
    onMyCoursesClick: () -> Unit = {},
    onCollaborationsClick: () -> Unit = {},
    onMosbatElmNotificationsClick: () -> Unit = {},
    onPurchasedCoursesClick: () -> Unit = {},
    onCertificatesClick: () -> Unit = {},
    onLikedPostsClick: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
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
                                "پروفایل مثبت علم",
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
            val isOrganizer = user?.institutionId != null || user?.isTeacher == true

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── 1. Mosbat Elm Profile ──────────────────────────────
                if (isOrganizer) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (user?.institutionId != null) {
                                MosbatElmProfileCircleWithEdit(
                                    imageUrl = user.institutionLogoUrl,
                                    name = user.institutionName ?: "آکادمی",
                                    label = "پروفایل موسسه",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { onAcademyProfileClick(user.institutionId) },
                                    accentColor = Color(0xFF2196F3)
                                )
                            } else if (user?.isTeacher == true) {
                                MosbatElmProfileCircleWithEdit(
                                    imageUrl = user.avatarUrl,
                                    name = user.displayName,
                                    label = "پروفایل مدرس",
                                    onClick = onEditAcademyProfileClick,
                                    onAvatarClick = { /* Maybe go to teacher personal profile? */ },
                                    accentColor = Color(0xFF2196F3)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // ── 2. Mosbat Elm Menu ──────────────────────────────────────
                item {
                    val menuItems = if (isOrganizer) {
                        listOf(
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.QueryStats,
                                title = "آمار و درآمد",
                                subtitle = "مشاهده عملکرد و گزارش مالی",
                                iconColor = extendedColors.accent,
                                onClick = { user?.institutionId?.let { onAcademyAnalyticsClick(it) } }
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.School,
                                title = "دوره‌های من",
                                subtitle = "مدیریت و ایجاد دوره‌های آموزشی",
                                iconColor = Color(0xFF4CAF50),
                                onClick = onMyCoursesClick
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.Handshake,
                                title = "همکاری‌ها",
                                subtitle = "مدیریت شرکا و مدرسین همکار",
                                iconColor = Color(0xFF2196F3),
                                onClick = onCollaborationsClick
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.NotificationsActive,
                                title = "اعلان‌های مثبت علم",
                                subtitle = "درخواست‌های همکاری و اطلاعیه‌ها",
                                iconColor = Color(0xFFFF5722),
                                onClick = onMosbatElmNotificationsClick
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.School,
                                title = "دوره‌های خریداری شده",
                                subtitle = "مشاهده دوره‌های فعال شما",
                                iconColor = Color(0xFF4CAF50),
                                onClick = onPurchasedCoursesClick
                            )
                        )
                    } else {
                        listOf(
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.School,
                                title = "دوره‌های خریداری شده",
                                subtitle = "مشاهده دوره‌های فعال شما",
                                iconColor = Color(0xFF4CAF50),
                                onClick = onPurchasedCoursesClick
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.EmojiEvents,
                                title = "مدرک‌های دریافت شده",
                                subtitle = "گواهینامه‌های دوره‌های گذرانده شده",
                                iconColor = Color(0xFFFFC107),
                                onClick = onCertificatesClick
                            ),
                            MosbatElmSettingsItemData(
                                icon = Icons.Default.Stars,
                                title = "پست‌های لایک شده",
                                subtitle = "محتواهایی که پسندیده‌اید",
                                iconColor = Color(0xFFE91E63),
                                onClick = onLikedPostsClick
                            )
                        )
                    }
                    
                    MosbatElmSettingsSection(items = menuItems)
                }
            }
        }
    }
}

@Composable
private fun MosbatElmProfileCircleWithEdit(
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
            
            // Frosted glass "مشاهده" overlay for Mosbat Elm circle
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
                            .blur(20.dp)
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
                                fontFamily = DanaFontFamily
                            )
                        }
                    }
                }
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

data class MosbatElmSettingsItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun MosbatElmSettingsSection(
    title: String? = null,
    items: List<MosbatElmSettingsItemData>
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
            MosbatElmSettingsRow(
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
private fun MosbatElmSettingsRow(
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
