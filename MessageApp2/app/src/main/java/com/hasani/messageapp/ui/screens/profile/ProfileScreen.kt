package com.hasani.messageapp.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily
import com.hasani.messageapp.ui.viewmodel.AuthViewModel
import com.hasani.messageapp.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import java.io.File

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Premium Profile Screen - Dribbble-Style Design
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToArchivedChats: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToSavedMessages: (String) -> Unit,
    onNavigateToFollowList: (userId: String, tab: Int) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val profileState by profileViewModel.state.collectAsState()
    val user = profileState.user
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    // Image picker for avatar
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            // Convert URI to File and upload
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                profileViewModel.uploadAvatar(file)
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "❌ Failed to upload avatar: ${e.message}")
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Background gradient effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            extendedColors.gradientStart.copy(alpha = 0.15f),
                            extendedColors.gradientEnd.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        if (profileState.isLoading && user == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = extendedColors.accent)
            }
        } else if (user != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Header with Avatar and Stats
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically { -50 }
                    ) {
                        ProfileHeader(
                            avatarUrl = user.avatarUrl,
                            displayName = user.displayName,
                            username = user.username,
                            bio = user.bio,
                            followerCount = profileState.followerCount,
                            followingCount = profileState.followingCount,
                            onEditClick = onEditProfileClick,
                            onAvatarChangeClick = { imagePickerLauncher.launch("image/*") },
                            onFollowersClick = { onNavigateToFollowList(user.id, 0) },
                            onFollowingClick = { onNavigateToFollowList(user.id, 1) }
                        )
                    }
                }
                // Quick Actions Row
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically { 30 }
                    ) {
                        QuickActionsRow(
                            onSavedClick = { onNavigateToSavedMessages(user.id) },
                            onArchivedClick = onNavigateToArchivedChats,
                            onSettingsClick = onSettingsClick
                        )
                    }
                }
                // Profile Sections
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically { 50 }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            // Education Section
                            if (!user.university.isNullOrEmpty() || !user.fieldOfStudy.isNullOrEmpty() || !user.education.isNullOrEmpty()) {
                                ProfileInfoSection(
                                    title = "تحصیلات",
                                    icon = Icons.Default.School,
                                    items = buildList {
                                        user.university?.takeIf { it.isNotEmpty() }?.let { add("🎓 $it") }
                                        user.fieldOfStudy?.takeIf { it.isNotEmpty() }?.let { add("📚 $it") }
                                        user.education?.takeIf { it.isNotEmpty() }?.let { add("📖 $it") }
                                    }
                                )
                            }
                            // Skills Section
                            if (!user.skills.isNullOrEmpty()) {
                                SkillsChipsSection(
                                    title = "مهارت‌ها",
                                    icon = Icons.Default.Stars,
                                    skills = user.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                            }
                            // Interests Section
                            if (!user.interests.isNullOrEmpty()) {
                                SkillsChipsSection(
                                    title = "علاقه‌مندی‌ها",
                                    icon = Icons.Default.Interests,
                                    skills = user.interests.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    chipColor = extendedColors.gradientEnd
                                )
                            }
                            // Work Experience Section
                            if (!user.workExperience.isNullOrEmpty()) {
                                ProfileInfoSection(
                                    title = "تجربه کاری",
                                    icon = Icons.Default.Work,
                                    items = listOf(user.workExperience)
                                )
                            }
                            // Achievements Section
                            if (!user.achievements.isNullOrEmpty()) {
                                ProfileInfoSection(
                                    title = "افتخارات",
                                    icon = Icons.Default.EmojiEvents,
                                    items = listOf(user.achievements),
                                    accentColor = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
                // Menu Items
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn() + slideInVertically { 80 }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            ProfileMenuCard {
                                ProfileMenuItem(
                                    icon = Icons.Default.Edit,
                                    title = "ویرایش پروفایل",
                                    subtitle = "اطلاعات شخصی، عکس، بیو",
                                    onClick = onEditProfileClick
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                                ProfileMenuItem(
                                    icon = Icons.Default.Settings,
                                    title = "تنظیمات",
                                    subtitle = "حریم خصوصی، اعلان‌ها، تم",
                                    onClick = onSettingsClick
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileMenuCard {
                                ProfileMenuItem(
                                    icon = Icons.AutoMirrored.Filled.Logout,
                                    title = "خروج از حساب",
                                    subtitle = "خروج از این دستگاه",
                                    iconColor = MaterialTheme.colorScheme.error,
                                    onClick = {
                                        authViewModel.logout()
                                        onLogoutClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "کاربر یافت نشد",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Profile Header Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileHeader(
    avatarUrl: String?,
    displayName: String,
    username: String,
    bio: String?,
    followerCount: Int,
    followingCount: Int,
    onEditClick: () -> Unit,
    onAvatarChangeClick: () -> Unit,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with glow effect
        Box(contentAlignment = Alignment.Center) {
            // Glow background
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .blur(20.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                extendedColors.accent.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Avatar border
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                extendedColors.gradientStart,
                                extendedColors.gradientEnd
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                AvatarImage(
                    imageUrl = avatarUrl,
                    name = displayName,
                    size = AvatarSize.XLARGE,
                    hasBorder = false
                )
            }
            // Camera button for changing profile photo
            IconButton(
                onClick = onAvatarChangeClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(36.dp)
                    .background(
                        color = extendedColors.accent,
                        shape = CircleShape
                    )
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "تغییر عکس",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Name
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        // Username
        Text(
            text = "@$username",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = VazirFontFamily,
            color = extendedColors.accent
        )
        // Bio
        if (!bio.isNullOrEmpty()) {
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                count = followerCount,
                label = "دنبال‌کننده",
                onClick = onFollowersClick
            )
            StatItem(
                count = followingCount,
                label = "دنبال‌شده",
                onClick = onFollowingClick
            )
        }
    }
}


// StatItem moved to ProfileComponents.kt

// ═══════════════════════════════════════════════════════════════════════════════
// ⚡ Quick Actions Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionsRow(
    onSavedClick: () -> Unit,
    onArchivedClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Default.Bookmark,
            title = "ذخیره‌شده",
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
            modifier = Modifier.weight(1f),
            onClick = onSavedClick
        )
        QuickActionCard(
            icon = Icons.Default.Archive,
            title = "آرشیو",
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF14B8A6)),
            modifier = Modifier.weight(1f),
            onClick = onArchivedClick
        )
        QuickActionCard(
            icon = Icons.Default.Settings,
            title = "تنظیمات",
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFF97316)),
            modifier = Modifier.weight(1f),
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Profile Info Section
// ═══════════════════════════════════════════════════════════════════════════════

// ProfileInfoSection, SkillsChipsSection, SkillChip moved to ProfileComponents.kt

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Profile Menu Components
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileMenuCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = MessageAppTheme.extendedColors.accent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = iconColor.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier.size(24.dp)
        )
    }
}
