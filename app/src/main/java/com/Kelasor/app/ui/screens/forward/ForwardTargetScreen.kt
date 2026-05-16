package com.Kelasor.app.ui.screens.forward

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.ForwardEvent
import com.Kelasor.app.ui.viewmodel.ForwardTarget
import com.Kelasor.app.ui.viewmodel.ForwardTargetViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 🔀 Forward Target Selection Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ForwardTargetScreen(
    onBackPress: () -> Unit,
    viewModel: ForwardTargetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForwardEvent.ForwardComplete -> {
                    Toast.makeText(context, "پیام با موفقیت ارسال شد ✓", Toast.LENGTH_SHORT).show()
                    onBackPress()
                }
                is ForwardEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Derive participant info for private chats - show other user's name/avatar
    val chatParticipantUserIds = state.chats.flatMap { cwp ->
        cwp.participants.filter { !it.isCurrentUser }.map { it.id }
    }.toSet()
    // Derive filtered lists from Compose-observed state to avoid desync
    val query = state.searchQuery.lowercase()
    val filteredChats = state.chats.filter { cwp ->
        if (query.isBlank()) true
        else {
            val otherUser = cwp.participants.firstOrNull { !it.isCurrentUser }
            val name = otherUser?.contactName ?: otherUser?.displayName ?: cwp.chat.title
            name.lowercase().contains(query) || cwp.chat.title.lowercase().contains(query)
        }
    }
    val filteredGroups = if (query.isBlank()) state.groups
        else state.groups.filter { it.name.lowercase().contains(query) }
    val filteredChannels = if (query.isBlank()) state.channels
        else state.channels.filter { it.name.lowercase().contains(query) }
    // Exclude contacts that already have a private chat
    val dedupedContacts = state.contacts.filter { it.id !in chatParticipantUserIds }
    val filteredContacts = if (query.isBlank()) dedupedContacts
        else dedupedContacts.filter {
            it.displayName.lowercase().contains(query) ||
            (it.contactName?.lowercase()?.contains(query) == true) ||
            it.username.lowercase().contains(query)
        }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ارسال مجدد",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (state.selectedTargets.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.forwardMessages() },
                    containerColor = extendedColors.accent,
                    contentColor = Color.White
                ) {
                    if (state.isForwarding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال"
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Search Bar ──────────────────────────────────────────────────
            TextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = "جستجو...",
                        fontFamily = DanaFontFamily,
                        color = extendedColors.textSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = extendedColors.textSecondary
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "پاک کردن",
                                tint = extendedColors.textSecondary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )
            // ── Selected Chips ─────────────────────────────────────────────
            if (state.selectedTargets.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.selectedTargets.forEach { target ->
                        SelectedTargetChip(
                            name = target.displayName,
                            accentColor = extendedColors.accent,
                            onRemove = { viewModel.toggleTarget(target) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            // ── Loading ─────────────────────────────────────────────────────
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
                return@Column
            }
            // ── Target Lists ────────────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Private Chats
                if (filteredChats.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "چت\u200Cهای خصوصی",
                            textColor = extendedColors.accent
                        )
                    }
                    items(filteredChats, key = { "chat_${it.chat.id}" }) { chatWithParticipants ->
                        val otherUser = chatWithParticipants.participants.firstOrNull { !it.isCurrentUser }
                        val chatDisplayName = otherUser?.contactName ?: otherUser?.displayName ?: chatWithParticipants.chat.title
                        val chatAvatarUrl = otherUser?.avatarUrl ?: chatWithParticipants.chat.avatarUrl
                        val targetId = "chat_${chatWithParticipants.chat.id}"
                        val isSelected = viewModel.isTargetSelected(targetId)
                        ForwardTargetItem(
                            title = chatDisplayName,
                            subtitle = chatWithParticipants.chat.lastMessage ?: "",
                            avatarUrl = chatAvatarUrl,
                            isSelected = isSelected,
                            accentColor = extendedColors.accent,
                            cardBackground = MaterialTheme.colorScheme.surfaceVariant,
                            textPrimary = MaterialTheme.colorScheme.onBackground,
                            textSecondary = extendedColors.textSecondary,
                            onClick = {
                                viewModel.toggleTarget(
                                    ForwardTarget.ChatTarget(
                                        chatId = chatWithParticipants.chat.id,
                                        title = chatDisplayName,
                                        avatarUrl = chatAvatarUrl
                                    )
                                )
                            }
                        )
                    }
                }
                // 2. Groups
                if (filteredGroups.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "گروه\u200Cها",
                            textColor = extendedColors.accent
                        )
                    }
                    items(filteredGroups, key = { "group_${it.id}" }) { group ->
                        val targetId = "group_${group.id}"
                        val isSelected = viewModel.isTargetSelected(targetId)
                        ForwardTargetItem(
                            title = group.name,
                            subtitle = "${group.memberCount} عضو",
                            avatarUrl = group.avatarUrl,
                            isSelected = isSelected,
                            accentColor = extendedColors.accent,
                            cardBackground = MaterialTheme.colorScheme.surfaceVariant,
                            textPrimary = MaterialTheme.colorScheme.onBackground,
                            textSecondary = extendedColors.textSecondary,
                            onClick = {
                                viewModel.toggleTarget(
                                    ForwardTarget.GroupTarget(
                                        groupId = group.id,
                                        name = group.name,
                                        avatarUrl = group.avatarUrl
                                    )
                                )
                            }
                        )
                    }
                }
                // 3. Channels
                if (filteredChannels.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "کانال\u200Cها",
                            textColor = extendedColors.accent
                        )
                    }
                    items(filteredChannels, key = { "channel_${it.id}" }) { channel ->
                        val targetId = "channel_${channel.id}"
                        val isSelected = viewModel.isTargetSelected(targetId)
                        ForwardTargetItem(
                            title = channel.name,
                            subtitle = "${channel.subscriberCount} دنبال\u200Cکننده",
                            avatarUrl = channel.avatarUrl,
                            isSelected = isSelected,
                            accentColor = extendedColors.accent,
                            cardBackground = MaterialTheme.colorScheme.surfaceVariant,
                            textPrimary = MaterialTheme.colorScheme.onBackground,
                            textSecondary = extendedColors.textSecondary,
                            onClick = {
                                viewModel.toggleTarget(
                                    ForwardTarget.ChannelTarget(
                                        channelId = channel.id,
                                        name = channel.name,
                                        avatarUrl = channel.avatarUrl
                                    )
                                )
                            }
                        )
                    }
                }
                // 4. Contacts
                if (filteredContacts.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "مخاطبین",
                            textColor = extendedColors.accent
                        )
                    }
                    items(filteredContacts, key = { "contact_${it.id}" }) { user ->
                        val targetId = "contact_${user.id}"
                        val isSelected = viewModel.isTargetSelected(targetId)
                        ForwardTargetItem(
                            title = user.contactName ?: user.displayName,
                            subtitle = if (user.username.isNotBlank()) "@${user.username}" else "",
                            avatarUrl = user.avatarUrl,
                            isSelected = isSelected,
                            accentColor = extendedColors.accent,
                            cardBackground = MaterialTheme.colorScheme.surfaceVariant,
                            textPrimary = MaterialTheme.colorScheme.onBackground,
                            textSecondary = extendedColors.textSecondary,
                            onClick = {
                                viewModel.toggleTarget(
                                    ForwardTarget.ContactTarget(
                                        userId = user.id,
                                        name = user.contactName ?: user.displayName,
                                        avatarUrl = user.avatarUrl
                                    )
                                )
                            }
                        )
                    }
                }
                // Empty state
                if (filteredChats.isEmpty() && filteredGroups.isEmpty() &&
                    filteredChannels.isEmpty() && filteredContacts.isEmpty()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (state.searchQuery.isNotBlank()) "نتیجه\u200Cای یافت نشد"
                                       else "هیچ مقصدی موجود نیست",
                                fontFamily = DanaFontFamily,
                                color = extendedColors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
        // ── Forwarding Overlay ──────────────────────────────────────────
        if (state.isForwarding) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = extendedColors.accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "در حال ارسال...",
                        fontFamily = DanaFontFamily,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🧩 Sub-Components
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    textColor: Color
) {
    Text(
        text = title,
        fontFamily = DanaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = textColor,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun ForwardTargetItem(
    title: String,
    subtitle: String,
    avatarUrl: String?,
    isSelected: Boolean,
    accentColor: Color,
    cardBackground: Color,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = accentColor,
                uncheckedColor = textSecondary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Avatar
        val fullAvatarUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(avatarUrl ?: "")
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(cardBackground),
            contentAlignment = Alignment.Center
        ) {
            if (!fullAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = fullAvatarUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = title.take(1).uppercase(),
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = accentColor
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        // Name + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SelectedTargetChip(
    name: String,
    accentColor: Color,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onRemove)
            .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontFamily = DanaFontFamily,
            fontSize = 12.sp,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "حذف",
            modifier = Modifier.size(16.dp),
            tint = accentColor
        )
    }
}
