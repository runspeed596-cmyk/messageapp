package com.Kelasor.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Campaign
import com.Kelasor.app.domain.model.Channel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.CollaborationRequestDialog
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.UserProfileViewModel
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Profile Screen - View another user's profile
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    fromChat: Boolean = false,
    onNavigateBack: () -> Unit,
    onStartChat: (String, String?) -> Unit, // Updated signature
    onNavigateToFollowList: (userId: String, tab: Int) -> Unit = { _, _ -> },
    onNavigateToChannel: (String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val user = state.user
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // Shared Media Action State
    var selectedSharedContent by remember { mutableStateOf<com.Kelasor.app.domain.model.SharedContent?>(null) }
    
    // Media Preview State
    var previewMediaUrl by remember { mutableStateOf<String?>(null) }
    var previewMediaType by remember { mutableStateOf(com.Kelasor.app.ui.components.MediaType.IMAGE) }

    // Media Preview Dialog
    previewMediaUrl?.let { url ->
        com.Kelasor.app.ui.components.MediaPreviewDialog(
            mediaUrl = url,
            mediaType = previewMediaType,
            onDismiss = { previewMediaUrl = null }
        )
    }
    
    // Shared Media Action Sheet
    com.Kelasor.app.ui.components.SharedMediaActionSheet(
        content = selectedSharedContent,
        onDismissRequest = { selectedSharedContent = null },
        onView = {
            selectedSharedContent?.let { item ->
                when (item.type) {
                    com.Kelasor.app.domain.model.MessageType.IMAGE,
                    com.Kelasor.app.domain.model.MessageType.VIDEO -> {
                        previewMediaUrl = item.url
                        previewMediaType = if (item.type == com.Kelasor.app.domain.model.MessageType.VIDEO) 
                            com.Kelasor.app.ui.components.MediaType.VIDEO 
                        else 
                            com.Kelasor.app.ui.components.MediaType.IMAGE
                    }
                    com.Kelasor.app.domain.model.MessageType.LINK -> {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "خطا در باز کردن لینک", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        android.widget.Toast.makeText(context, "قابلیت نمایش این فایل هنوز پیاده‌سازی نشده است", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onShowInChat = {
            selectedSharedContent?.let { item ->
                // Use actual chatId if available, fallback to userId for new chat resolution
                val targetChatId: String = item.chatId ?: userId
                onStartChat(targetChatId, item.messageId)
            }
        }
    )

    // Collaboration Request Dialog
    if (state.showCollaborationDialog) {
        CollaborationRequestDialog(
            recipientName = user?.displayName ?: "کاربر",
            onDismiss = { viewModel.hideCollaborationDialog() },
            onSend = { title, message ->
                viewModel.sendCollaborationRequest(
                    userId = userId,
                    title = title,
                    message = message,
                    onSuccess = {
                        // Show success toast or feedback
                        android.widget.Toast.makeText(context, "درخواست ارسال شد", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = user?.contactName ?: user?.displayName ?: androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.profile_title_default),
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            if (state.isLoading && user == null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            } else if (user != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Profile Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            extendedColors.gradientStart.copy(alpha = 0.15f),
                                            extendedColors.gradientEnd.copy(alpha = 0.05f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CardShapes.profileHeader
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar - use privacy-sanitized URL
                            AvatarImage(
                                imageUrl = state.displayAvatarUrl,
                                name = user.contactName ?: user.displayName,
                                size = AvatarSize.XLARGE,
                                isOnline = if (state.canSeeOnlineStatus) user.isOnline else false,
                                hasBorder = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            // Name - show contactName if saved, otherwise displayName
                            Text(
                                text = user.contactName ?: user.displayName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Username
                            Text(
                                text = "@${user.username}",
                                style = MessageAppTypography.chatPreview,
                                color = extendedColors.accent
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Bio
                            if (!user.bio.isNullOrEmpty()) {
                                Text(
                                    text = user.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = DanaFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Online status - only show if allowed by privacy
                            if (state.canSeeOnlineStatus) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (user.isOnline) androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.online) else androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.last_seen_recently),
                                    style = MessageAppTypography.chatTime,
                                    color = if (user.isOnline) extendedColors.onlineIndicator
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Educational Role Chip
                            if (!user.educationalRole.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                val roleLabel = when(user.educationalRole) {
                                    "SCHOOL_STUDENT" -> "🎒 دانش‌آموز"
                                    "STUDENT" -> "🎒 دانش‌آموز"
                                    "UNI_STUDENT" -> "🎓 دانشجو"
                                    "TEACHER" -> "👨‍🏫 استاد/معلم"
                                    "FREELANCER" -> "💼 آزاد"
                                    "GRADUATE" -> "🎓 فارغ‌التحصیل"
                                    else -> "📚 ${user.educationalRole}"
                                }
                                Surface(
                                    color = extendedColors.accent.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.accent.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = roleLabel,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = DanaFontFamily,
                                        color = extendedColors.accent
                                    )
                                }
                            }
                        }
                    }
                    // Action Buttons
                    item {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            UserProfileActionButton(
                                icon = Icons.AutoMirrored.Filled.Message,
                                label = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.action_message),
                                onClick = { 
                                    if (fromChat) {
                                        onNavigateBack()
                                    } else {
                                        onStartChat(userId, null)
                                    }
                                },
                                // Take full width if call button is hidden
                                modifier = if (state.canSeePhoneNumber) Modifier.weight(1f) else Modifier.fillMaxWidth()
                            )
                            // Only show call button if phone number is visible (not hidden by privacy)
                            if (state.canSeePhoneNumber) {
                                UserProfileActionButton(
                                    icon = Icons.Default.Call,
                                    label = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.action_call),
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                            data = android.net.Uri.parse("tel:${user.phoneNumber}")
                                        }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    // Collaboration Request Button
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(CardShapes.button)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .clickable { viewModel.showCollaborationDialog() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Handshake,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "درخواست همکاری",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = DanaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    // Media Filter
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        com.Kelasor.app.ui.components.ContentFilter(
                            selectedType = state.selectedContentType,
                            onTypeSelected = { viewModel.onFilterSelected(it) }
                        )
                        
                        if (state.selectedContentType != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            com.Kelasor.app.ui.components.SharedMediaGrid(
                                content = state.sharedContent,
                                isLoading = state.isMediaLoading,
                                onItemClick = { item ->
                                    selectedSharedContent = item
                                }
                            )
                        }
                    }
                    // Info section
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    shape = CardShapes.glassCard
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.information),
                                style = MessageAppTypography.sectionTitle,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            // Phone number row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.mobile_number),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = DanaFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = state.displayPhoneNumber,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = DanaFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            // Location Row
                            if (!user.province.isNullOrBlank() || !user.city.isNullOrBlank()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = extendedColors.accent, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "موقعیت",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = DanaFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = listOfNotNull(user.province, user.city).joinToString("، "),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = DanaFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Divider
                            if (!user.bio.isNullOrEmpty()) {
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                // Bio row
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.bio_short),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = DanaFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = user.bio,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = DanaFontFamily,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } // End of Bio check
                            
                            // Bio Channels
                            if (state.bioChannels.isNotEmpty()) {
                                androidx.compose.material3.HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                BioChannelsSection(
                                    channels = state.bioChannels,
                                    onChannelClick = onNavigateToChannel
                                )
                            }
                            
                            // Extended Fields
                            if (!user.gradeLevel.isNullOrEmpty() || !user.major.isNullOrEmpty() || !user.faculty.isNullOrEmpty() || !user.education.isNullOrEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                ProfileInfoSection(
                                    title = "هویت آموزشی",
                                    icon = Icons.Default.School,
                                    items = buildList {
                                        user.gradeLevel?.takeIf { it.isNotEmpty() }?.let { add("🎓 مقطع: $it") }
                                        user.faculty?.takeIf { it.isNotEmpty() }?.let { add("🏛️ دانشکده: $it") }
                                        user.major?.takeIf { it.isNotEmpty() }?.let { add("📚 رشته: $it") }
                                        user.education?.takeIf { it.isNotEmpty() }?.let { add("📖 مدارک: $it") }
                                    }
                                )
                            }
                            
                            if (!user.skills.isNullOrEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                SkillsChipsSection(
                                    title = "مهارت‌ها",
                                    icon = Icons.Default.Stars,
                                    skills = user.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )
                            }
                            
                            if (!user.achievements.isNullOrEmpty()) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                ProfileInfoSection(
                                    title = "افتخارات",
                                    icon = Icons.Default.EmojiEvents,
                                    items = listOf(user.achievements),
                                    accentColor = Color(0xFFFFD700)
                                )
                            }
                            
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } else if (state.error != null) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.error_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    // }
}

@Composable
private fun UserProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(CardShapes.button)
            .background(extendedColors.accent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BioChannelsSection(
    channels: List<Channel>,
    onChannelClick: (String) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = extendedColors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "کانال‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                color = extendedColors.accent
            )
        }
        
        channels.forEach { channel ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShapes.glassCardSmall)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
                    .clickable { onChannelClick(channel.id) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    imageUrl = channel.avatarUrl,
                    name = channel.name,
                    size = AvatarSize.MEDIUM
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (!channel.publicId.isNullOrBlank()) "@${channel.publicId}" else "${channel.subscriberCount} مشترک",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


