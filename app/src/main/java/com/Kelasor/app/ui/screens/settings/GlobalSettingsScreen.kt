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
import coil3.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.theme.MessageAppTheme
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.util.UrlUtils
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import androidx.compose.ui.text.style.TextOverflow
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSettingsScreen(
    onNavigateBack: () -> Unit,
    onWalletClick: () -> Unit,
    onMessengerSettingsClick: () -> Unit,
    onMosbatElmSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onAboutUsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val profileState by profileViewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    val savedAccounts by authViewModel.savedAccounts.collectAsState(initial = emptyList())
    val user = profileState.user
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn == false) {
            onLogoutSuccess()
        }
    }

    val currentLayoutDirection = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides currentLayoutDirection) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { 
                        Text("تنظیمات کلی", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
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
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // ── 0. User Profile Header ─────────────────────────────────────────────
                item {
                    if (user != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditProfileClick() }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Profile Image (right side in RTL)
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarImage(
                                        imageUrl = user.avatarUrl,
                                        name = user.displayName.ifBlank { user.firstName ?: "کاربر" },
                                        size = AvatarSize.LARGE,
                                        hasBorder = false
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Text Details (left side in RTL)
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = DanaFontFamily,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = user.phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = DanaFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "آیدی: ${user.username}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = DanaFontFamily,
                                        color = Color(0xFF2196F3)
                                    )
                                    if (!user.bio.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = user.bio,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = DanaFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // --- Account Switcher UI ---
                            if (savedAccounts.size > 1) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                        savedAccounts.forEach { account ->
                                            val isActive = account.userId == user?.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { 
                                                        if (!isActive) authViewModel.switchAccount(account.userId) 
                                                    }
                                                    .background(if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AvatarImage(
                                                    imageUrl = account.avatarUrl,
                                                    name = if (account.displayName.isNotBlank()) account.displayName else account.phoneNumber,
                                                    modifier = Modifier.padding(end = 12.dp),
                                                    size = AvatarSize.SMALL,
                                                    hasBorder = false
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = if (account.displayName.isNotBlank()) account.displayName else account.phoneNumber,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontFamily = DanaFontFamily,
                                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                                        color = MaterialTheme.colorScheme.onBackground,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    if (account.displayName.isNotBlank()) {
                                                        Text(
                                                            text = account.phoneNumber,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontFamily = DanaFontFamily,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (isActive) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Active",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Add Account Button
                            OutlinedButton(
                                onClick = onAddAccountClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Account",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اضافه کردن حساب کاربری",
                                    fontFamily = DanaFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ── 1. Wallet Card (Slightly larger) ──────────────────────────────────────
                item {
                    GlobalWalletCard(
                        onClick = onWalletClick,
                        accentColor = extendedColors.accent
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ── 2. Two Grid Cards for Messenger & Mosbat Elm ──────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Messenger Settings Card
                        SettingsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "تنظیمات\nپیام رسان",
                            icon = Icons.Default.Chat,
                            iconColor = Color(0xFF2196F3),
                            onClick = onMessengerSettingsClick
                        )
                        
                        // Mosbat Elm Settings Card
                        SettingsGridCard(
                            modifier = Modifier.weight(1f),
                            title = "تنظیمات\nمثبت علم",
                            icon = Icons.Default.School,
                            iconColor = Color(0xFF4CAF50),
                            onClick = onMosbatElmSettingsClick
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // ── 3. Global Settings List Section ────────────────────────────────────────
                item {
                    GlobalSettingsSection(
                        items = listOf(
                            GlobalSettingsItemData(
                                icon = Icons.Default.Notifications,
                                title = "اعلان ها",
                                iconColor = Color(0xFFF44336),
                                onClick = onNotificationsClick
                            ),
                            GlobalSettingsItemData(
                                icon = Icons.Default.SupportAgent,
                                title = "پشتیبانی",
                                subtitle = "سوالات متداول و ارتباط با ما",
                                iconColor = Color(0xFF00BCD4),
                                onClick = onSupportClick
                            ),
                            GlobalSettingsItemData(
                                icon = Icons.Default.Feedback,
                                title = "انتقاد و پیشنهاد",
                                iconColor = Color(0xFFFF9800),
                                onClick = onFeedbackClick
                            ),
                            GlobalSettingsItemData(
                                icon = Icons.Default.Info,
                                title = "درباره ما",
                                iconColor = Color(0xFF9C27B0),
                                onClick = onAboutUsClick
                            )
                        )
                    )
                }
                
                // ── Common. App Version ──────────────────────────────────────
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
                            fontFamily = DanaFontFamily
                                    )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                item {
                    // Logout Button (At the bottom)
                    OutlinedButton(
                        onClick = { user?.id?.let { authViewModel.logout(it) } },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خروج از حساب کاربری",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            if (authState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) { },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
}

@Composable
private fun GlobalWalletCard(
    onClick: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF8E24AA).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color(0xFFAB47BC),
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "کیف پول من",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "مدیریت دارایی و تراکنش‌ها",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = DanaFontFamily
                )
            }
            
            Text(
                text = "۰ تومان",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontFamily = DanaFontFamily,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SettingsGridCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class GlobalSettingsItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String = "",
    val iconColor: Color,
    val onClick: () -> Unit
)

@Composable
private fun GlobalSettingsSection(
    title: String? = null,
    items: List<GlobalSettingsItemData>
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
            GlobalSettingsRow(
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
private fun GlobalSettingsRow(
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Medium
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
