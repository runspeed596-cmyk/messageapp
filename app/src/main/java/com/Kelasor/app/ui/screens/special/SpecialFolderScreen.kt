package com.Kelasor.app.ui.screens.special

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.data.remote.dto.AiBotDto
import com.Kelasor.app.data.remote.dto.SpecialChannelDto
import com.Kelasor.app.data.remote.dto.SpecialGroupDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.SpecialFolderViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ⭐ Special Folder Screen — "ویژه" tab
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SpecialFolderScreen(
    onNavigateToChannel: (String) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToAiBotList: () -> Unit = {},
    viewModel: SpecialFolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading && state.aiBots.isEmpty()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = extendedColors.accent
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Channels Section ─────────────────────────────────────────
                if (state.channels.isNotEmpty()) {
                    item {
                        SectionHeader(title = "کانال‌ها")
                    }
                    items(state.channels) { channel: SpecialChannelDto ->
                        SpecialChannelItem(
                            channel = channel,
                            onClick = { onNavigateToChannel(channel.id) }
                        )
                    }
                }

                // ── Groups Section ───────────────────────────────────────────
                if (state.groups.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(title = "گروه‌ها")
                    }
                    items(state.groups) { group: SpecialGroupDto ->
                        SpecialGroupItem(
                            group = group,
                            onClick = { onNavigateToGroup(group.id) }
                        )
                    }
                }

                // ── Support Section (Expandable Button) ──────────────────────
                if (state.supportChannels.isNotEmpty() || state.supportGroups.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SupportExpandableButton(
                            supportChannels = state.supportChannels,
                            supportGroups = state.supportGroups,
                            onNavigateToChannel = onNavigateToChannel,
                            onNavigateToGroup = onNavigateToGroup
                        )
                    }
                } else {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SupportBanner(
                            onClick = {
                                val chatId: String? = state.supportChatId
                                if (chatId != null) {
                                    onNavigateToChat(chatId)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Error display
        if (state.error != null) {
            Text(
                text = state.error ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
    } // end CompositionLocalProvider
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔥 AI Bot Banner — Top banner showing the AI bot (like the schematic)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiBotBanner(
    aiBots: List<AiBotDto>,
    onBotClick: (String) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            extendedColors.gradientStart,
            extendedColors.gradientMiddle,
            extendedColors.gradientEnd
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = extendedColors.accentGlow.copy(alpha = 0.3f),
                spotColor = extendedColors.accentGlow.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(vertical = 16.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔥 هوش مصنوعی 🔥",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (aiBots.isNotEmpty()) onBotClick(aiBots.first().id)
                    }
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📌 Section Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = extendedColors.textSecondary,
                fontSize = 14.sp
            )
        )
    }
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = extendedColors.glassBorder.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Item Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SpecialChannelItem(
    channel: SpecialChannelDto,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            extendedColors.accent,
                            extendedColors.accentSecondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (channel.avatarUrl != null) {
                AsyncImage(
                    model = channel.avatarUrl,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Channel info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (channel.subscriberCount > 0) {
                Text(
                    text = "${channel.subscriberCount} عضو",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = extendedColors.textSecondary
                    )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Item Row
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SpecialGroupItem(
    group: SpecialGroupDto,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            extendedColors.accentSecondary,
                            extendedColors.accent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (group.avatarUrl != null) {
                AsyncImage(
                    model = group.avatarUrl,
                    contentDescription = group.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Group info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (group.memberCount > 0) {
                Text(
                    text = "${group.memberCount} عضو",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = extendedColors.textSecondary
                    )
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📞 Support Banner
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SupportBanner(onClick: () -> Unit) {
    val extendedColors = MessageAppTheme.extendedColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.glass.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = extendedColors.glassBorder.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(vertical = 14.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SupportAgent,
                contentDescription = null,
                tint = extendedColors.accent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ارتباط با پشتیبان اپلیکیشن کلاسور",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔽 Support Expandable Button
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SupportExpandableButton(
    supportChannels: List<SpecialChannelDto>,
    supportGroups: List<SpecialGroupDto>,
    onNavigateToChannel: (String) -> Unit,
    onNavigateToGroup: (String) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    var isExpanded: Boolean by remember { mutableStateOf(false) }
    val rotationAngle: Float by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "arrow_rotation"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
    ) {
        // ── Clickable Button ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = extendedColors.glass
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.SupportAgent,
                    contentDescription = null,
                    tint = extendedColors.accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ارتباط با پشتیبان",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = rotationAngle }
                )
            }
        }
        // ── Expanded Content ──
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                supportChannels.forEach { channel: SpecialChannelDto ->
                    SpecialChannelItem(
                        channel = channel,
                        onClick = { onNavigateToChannel(channel.id) }
                    )
                }
                supportGroups.forEach { group: SpecialGroupDto ->
                    SpecialGroupItem(
                        group = group,
                        onClick = { onNavigateToGroup(group.id) }
                    )
                }
            }
        }
    }
}


@Composable
private fun ProfileCompletionBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "پروفایل خود را تکمیل کنید!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "دانشگاه، رشته و مقطع تحصیلی خود را وارد کنید",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFBF360C)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
