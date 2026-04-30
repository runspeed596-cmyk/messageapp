package com.Kelasor.app.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.Chat
import com.Kelasor.app.domain.model.ChatType
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.FilterChip
import com.Kelasor.app.ui.components.UnreadBadge
import com.Kelasor.app.ui.theme.AppAnimations
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.ChatListViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Ultra-Premium Chat List Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (Chat) -> Unit,
    onNewChatClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onChatAvatarClick: (Chat) -> Unit = {},
    onMyStoriesClick: () -> Unit = {},
    onNavigateToCreateTextStory: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    searchQuery: String = "",
    viewModel: ChatListViewModel = hiltViewModel(),
    storyViewModel: com.Kelasor.app.ui.viewmodel.StoryViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val storyUiState by storyViewModel.uiState.collectAsState()
    val selectedStoryUser by storyViewModel.selectedStoryUser.collectAsState()
    val isUploadingStory by storyViewModel.isUploading.collectAsState()
    val storyPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) { storyViewModel.uploadStory(uri, "AUTO") }
    }
    var showAddStorySheet by remember { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    var showPremiumDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        storyViewModel.errorEvent.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.StoryErrorEvent.PremiumRequired -> { showPremiumDialog = true }
                is com.Kelasor.app.ui.viewmodel.StoryErrorEvent.GenericError -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    LaunchedEffect(searchQuery) { viewModel.setSearchQuery(searchQuery) }
    var isArchivedExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) { viewModel.loadContacts(); storyViewModel.loadStories() } }
    LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.loadContacts()
        } else { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) { is com.Kelasor.app.ui.viewmodel.ChatListEvent.NavigateToChat -> { onChatClick(event.chat) } }
        }
    }
    val filteredChats = remember(state.chats, state.localGroups, state.localChannels, state.pinnedChats, searchQuery, state.activeFilter) {
        val filter = state.activeFilter
        val query = searchQuery.trim()
        val candidates: List<Chat> = when (filter) {
            com.Kelasor.app.ui.viewmodel.SearchFilter.ALL -> {
                if (query.isNotEmpty()) state.chats + state.localGroups + state.localChannels
                else state.pinnedChats + state.chats
            }
            com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE -> state.chats
            com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP -> state.localGroups
            com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL -> state.localChannels
        }
        if (query.isEmpty()) candidates else candidates.filter { it.title.contains(query, ignoreCase = true) }
    }
    var deleteForAll by remember { mutableStateOf(false) }
    // ── UI Layout ────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column {
            // Selection Mode or Filter Chips
            if (state.selectedChatIds.isNotEmpty()) {
                SelectionTopBar(
                    selectedCount = state.selectedChatIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelected = { viewModel.requestDeleteSelection() },
                    onPinSelected = {
                        val allChats = state.chats + state.pinnedChats + state.archivedChats
                        val selectedChats = allChats.filter { it.id in state.selectedChatIds }
                        val newPinState = !selectedChats.any { it.isPinned }
                        state.selectedChatIds.forEach { chatId -> viewModel.pinChat(chatId, newPinState) }
                        android.widget.Toast.makeText(context, if (newPinState) "سنجاق شد" else "از سنجاق خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.clearSelection()
                    },
                    onArchiveSelected = {
                        val allChats = state.chats + state.pinnedChats + state.archivedChats
                        val selectedChats = allChats.filter { it.id in state.selectedChatIds }
                        val newArchiveState = !selectedChats.any { it.isArchived }
                        state.selectedChatIds.forEach { chatId -> viewModel.archiveChat(chatId, newArchiveState) }
                        android.widget.Toast.makeText(context, if (newArchiveState) "آرشیو شد" else "از آرشیو خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.clearSelection()
                    }
                )
            } else {
                AnimatedVisibility(
                    visible = state.searchQuery.isNotEmpty() || state.activeFilter != com.Kelasor.app.ui.viewmodel.SearchFilter.ALL,
                    enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 300f)) + fadeIn(tween(200)),
                    exit = shrinkVertically(spring(dampingRatio = 0.8f, stiffness = 300f)) + fadeOut(tween(150))
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { FilterChip(selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL, onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) }, label = "همه") }
                        item { FilterChip(selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE, onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE) }, label = "افراد") }
                        item { FilterChip(selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP, onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP) }, label = "گروه‌ها") }
                        item { FilterChip(selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL, onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL) }, label = "کانال‌ها") }
                    }
                }
            }
            // Content
            if (state.isLoading && filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    // Stories
                    if (state.searchQuery.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                        item(key = "stories_row", contentType = "stories") {
                            val storyUsers = when (val uiState = storyUiState) { is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success -> uiState.storyUsers; else -> emptyList() }
                            val currentUser = storyUsers.firstOrNull { it.isCurrentUser }
                            com.Kelasor.app.ui.components.story.StoriesList(
                                currentUser = currentUser, storyUsers = storyUsers,
                                onStoryClick = { su -> if (su.isCurrentUser) onMyStoriesClick() else storyViewModel.openStoryViewer(su) },
                                onAddStoryClick = { showAddStorySheet = true }
                            )
                            // Thin divider after stories
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                    // Empty
                    if (filteredChats.isEmpty() && state.searchQuery.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                        item { PremiumEmptyState() }
                    } else {
                        // Global Search Results
                        if (state.searchResults.isNotEmpty() && (state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL || state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE)) {
                            item(contentType = { "header" }) { SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.global_search)) }
                            items(items = state.searchResults, key = { "user_${it.id}" }, contentType = { "search_user" }) { user -> SearchUserItem(user = user, onClick = { viewModel.onUserSelected(user.id) }) }
                        }
                        // Channel Search Results
                        if (state.channelSearchResults.isNotEmpty() && (state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL || state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL)) {
                            item(contentType = { "header" }) { SectionHeader(title = "کانال‌های جهانی") }
                            items(items = state.channelSearchResults, key = { "channel_${it.id}" }, contentType = { "chat_item" }) { channel ->
                                val chat = Chat(id = channel.id, type = ChatType.CHANNEL, title = channel.name, avatarUrl = channel.avatarUrl,
                                    lastMessage = if (!channel.publicId.isNullOrBlank()) Message(id = "", chatId = channel.id, senderId = "", senderName = "", senderAvatar = null, type = MessageType.TEXT, content = "@${channel.publicId}", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.SENT, isEdited = false, createdAt = channel.createdAt, editedAt = null, reactions = emptyMap()) else null,
                                    unreadCount = 0, isPinned = false, isMuted = false, isArchived = false, participants = emptyList(), updatedAt = channel.createdAt)
                                ChatItem(chat = chat, onClick = { onChatAvatarClick(chat) }, onAvatarClick = { onChatAvatarClick(chat) }, currentUserId = state.currentUserId)
                            }
                        }
                        if (filteredChats.isNotEmpty() && state.searchResults.isNotEmpty()) {
                            item(contentType = { "header" }) { SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.your_chats)) }
                        }
                        // Default view
                        if (state.searchQuery.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                            // Archived
                            if (state.archivedChats.isNotEmpty()) {
                                item(contentType = { "archive_header" }) { PremiumArchiveHeader(count = state.archivedChats.size, isExpanded = isArchivedExpanded, onClick = { isArchivedExpanded = !isArchivedExpanded }) }
                                if (isArchivedExpanded) {
                                    items(items = state.archivedChats, key = { "archived_${it.id}" }, contentType = { "chat_item" }) { chat ->
                                        ChatItem(chat = chat, isSelected = state.selectedChatIds.contains(chat.id), inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                            onClick = { if (state.selectedChatIds.isNotEmpty()) viewModel.toggleChatSelection(chat.id) else onChatClick(chat) },
                                            onLongClick = { viewModel.toggleChatSelection(chat.id) }, onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                            onMute = { viewModel.muteChat(chat.id, !chat.isMuted) }, onArchive = { viewModel.archiveChat(chat.id, false) },
                                            onDelete = { viewModel.deleteChat(chat.id) }, currentUserId = state.currentUserId, onUnarchiveClick = { viewModel.archiveChat(chat.id, false) })
                                    }
                                }
                            }
                            // Pinned
                            if (state.pinnedChats.isNotEmpty()) {
                                item(contentType = { "header" }) { SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.pinned)) }
                                itemsIndexed(items = state.pinnedChats, key = { _, chat -> chat.id }, contentType = { _, _ -> "chat_item" }) { index, chat ->
                                    ChatItem(chat = chat, isSelected = state.selectedChatIds.contains(chat.id), inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                        onClick = { if (state.selectedChatIds.isNotEmpty()) viewModel.toggleChatSelection(chat.id) else onChatClick(chat) },
                                        onLongClick = { viewModel.toggleChatSelection(chat.id) }, onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                        onMute = { viewModel.muteChat(chat.id, !chat.isMuted) }, onArchive = { viewModel.archiveChat(chat.id, true) },
                                        onDelete = { viewModel.deleteChat(chat.id) }, currentUserId = state.currentUserId, animationDelay = index * 50)
                                }
                            }
                            // Regular
                            if (state.pinnedChats.isNotEmpty() && state.chats.isNotEmpty()) {
                                item(contentType = { "header" }) { SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.all_chats)) }
                            }
                            itemsIndexed(items = state.chats, key = { _, chat -> chat.id }, contentType = { _, _ -> "chat_item" }) { index, chat ->
                                ChatItem(chat = chat, isSelected = state.selectedChatIds.contains(chat.id), inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                    onClick = { if (state.selectedChatIds.isNotEmpty()) viewModel.toggleChatSelection(chat.id) else onChatClick(chat) },
                                    onLongClick = { viewModel.toggleChatSelection(chat.id) }, onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                    onMute = { viewModel.muteChat(chat.id, !chat.isMuted) }, onArchive = { viewModel.archiveChat(chat.id, true) },
                                    onDelete = { viewModel.deleteChat(chat.id) }, onAvatarClick = { onChatAvatarClick(chat) },
                                    currentUserId = state.currentUserId, animationDelay = index * 50)
                            }
                        } else {
                            itemsIndexed(items = filteredChats, key = { _, chat -> chat.id }, contentType = { _, _ -> "chat_item" }) { index, chat ->
                                ChatItem(chat = chat, isSelected = state.selectedChatIds.contains(chat.id), inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                    onClick = { if (state.selectedChatIds.isNotEmpty()) viewModel.toggleChatSelection(chat.id) else onChatClick(chat) },
                                    onLongClick = { viewModel.toggleChatSelection(chat.id) }, onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                    onMute = { viewModel.muteChat(chat.id, !chat.isMuted) }, onArchive = { viewModel.archiveChat(chat.id, true) },
                                    onDelete = { viewModel.deleteChat(chat.id) }, currentUserId = state.currentUserId, animationDelay = index * 50)
                            }
                        }
                    }
                }
            }
        }
        // Dialogs & Overlays
        if (state.showDeleteConfirmation) {
            val allChats = state.chats + state.pinnedChats + state.archivedChats
            val selectedChats = allChats.filter { it.id in state.selectedChatIds }
            val selectedName = if (selectedChats.size == 1) selectedChats.first().title else "${state.selectedChatIds.size} گفتگو"
            AlertDialog(
                onDismissRequest = { deleteForAll = false; viewModel.cancelDeleteSelection() },
                title = { Text("حذف گفتگو", style = MessageAppTypography.chatName) },
                text = { Column { Text("آیا میخواهید $selectedName را حذف کنید؟"); Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Checkbox(checked = deleteForAll, onCheckedChange = { deleteForAll = it }); Spacer(Modifier.width(4.dp)); Text("حذف برای همه", style = MaterialTheme.typography.bodyMedium) } } },
                confirmButton = { TextButton(onClick = { viewModel.confirmDeleteSelection(); deleteForAll = false }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { deleteForAll = false; viewModel.cancelDeleteSelection() }) { Text("لغو") } }
            )
        }
        if (isUploadingStory) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = Color.White); Spacer(Modifier.height(8.dp)); Text("در حال آپلود استوری...", color = Color.White) }
            }
        }
        if (showAddStorySheet) {
            androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showAddStorySheet = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("افزودن استوری", style = MessageAppTypography.chatName, modifier = Modifier.padding(bottom = 16.dp))
                    ListItem(headlineContent = { Text("عکس یا ویدیو") }, leadingContent = { Icon(Icons.Default.Image, contentDescription = null, tint = extendedColors.accent) },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { showAddStorySheet = false; storyPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo)) })
                    ListItem(headlineContent = { Text("متن با پس‌زمینه") }, leadingContent = { Icon(Icons.Default.Create, contentDescription = null, tint = extendedColors.accent) },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable { showAddStorySheet = false; onNavigateToCreateTextStory() })
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
        // Story Viewer — REMOVED: centralized in MessagingContent to prevent duplicate ExoPlayer instances across pager tabs.
        if (showPremiumDialog) {
            com.Kelasor.app.ui.components.PremiumUpgradeDialog(onDismiss = { showPremiumDialog = false }, onUpgrade = { showPremiumDialog = false; android.widget.Toast.makeText(context, "Navigate to Premium Purchase", android.widget.Toast.LENGTH_SHORT).show() })
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏷️ Premium Section Header — Accent bar + subtle glass background
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(title: String) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(extendedColors.accent, extendedColors.accentSecondary)
                    )
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = extendedColors.accent,
            fontWeight = FontWeight.Bold,
            fontFamily = VazirFontFamily,
            letterSpacing = 0.5.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📂 Premium Archive Header — Pill-style with icon
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumArchiveHeader(count: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val extendedColors = MessageAppTheme.extendedColors
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f), label = "archiveRot"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(extendedColors.accent.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(extendedColors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Archive, contentDescription = null, tint = extendedColors.accent, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(12.dp))
            Text("آرشیو شده ($count)", style = MaterialTheme.typography.titleSmall, color = extendedColors.accent, fontWeight = FontWeight.SemiBold, fontFamily = VazirFontFamily)
        }
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = extendedColors.accent.copy(alpha = 0.7f), modifier = Modifier.graphicsLayer { rotationZ = rotationAngle })
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 😶 Premium Empty State
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumEmptyState() {
    val extendedColors = MessageAppTheme.extendedColors
    val alpha = remember { Animatable(0f) }
    val slideY = remember { Animatable(40f) }
    LaunchedEffect(Unit) {
        delay(200)
        launch { alpha.animateTo(1f, tween(600, easing = AppAnimations.FluidEasing)) }
        slideY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 200f))
    }
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp).graphicsLayer { this.alpha = alpha.value; translationY = slideY.value },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Layered icon circles
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(extendedColors.accent.copy(alpha = 0.05f)))
                Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(extendedColors.accent.copy(alpha = 0.08f)))
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = extendedColors.accent.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("گفتگویی وجود ندارد", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = VazirFontFamily, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("یک گفتگوی جدید آغاز کنید", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontFamily = VazirFontFamily)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔍 Search User Item
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SearchUserItem(user: com.Kelasor.app.domain.model.User, onClick: () -> Unit) {
    val resolvedName: String = user.contactName ?: user.displayName
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(imageUrl = user.displayAvatarUrl, name = resolvedName, size = AvatarSize.MEDIUM, isOnline = user.isOnline)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(resolvedName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground, fontFamily = VazirFontFamily, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = VazirFontFamily)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Ultra-Premium ChatItem — Card-style with visual depth
// ═══════════════════════════════════════════════════════════════════════════════

private val CHAT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItem(
    chat: Chat,
    isSelected: Boolean = false,
    inSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onPin: () -> Unit = {},
    onMute: () -> Unit = {},
    onArchive: () -> Unit = {},
    onDelete: () -> Unit = {},
    currentUserId: String = "",
    modifier: Modifier = Modifier,
    onUnarchiveClick: (() -> Unit)? = null,
    animationDelay: Int = 0
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Staggered entrance animation
    val itemAlpha = remember { Animatable(0f) }
    val itemSlideX = remember { Animatable(24f) }
    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        launch { itemAlpha.animateTo(1f, tween(350, easing = AppAnimations.FluidEasing)) }
        itemSlideX.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = 220f))
    }
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium), label = "selScale"
    )
    val selectedBgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.14f else 0f,
        animationSpec = tween(250), label = "selBg"
    )
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_title), style = MessageAppTypography.chatName) },
            text = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_message)) },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.cancel)) } }
        )
    }
    Column(
        modifier = modifier
            .graphicsLayer { alpha = itemAlpha.value; translationX = itemSlideX.value; scaleX = selectionScale; scaleY = selectionScale }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (selectedBgAlpha > 0f) {
                        drawRoundRect(color = extendedColors.accent.copy(alpha = selectedBgAlpha), cornerRadius = CornerRadius(20f, 20f))
                    }
                }
                .combinedClickable(interactionSource = interactionSource, indication = null, onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status
            val hasAllSameParticipants = chat.participants.isNotEmpty() && chat.participants.map { it.id }.distinct().size == 1
            val isSelfChat = chat.type == ChatType.PRIVATE &&
                chat.participants.size >= 2 &&
                (hasAllSameParticipants || chat.participants.all { it.id == currentUserId })
            val isOnline = if (isSelfChat) false else if (chat.type == ChatType.PRIVATE) chat.participants.find { it.id != currentUserId }?.isOnline == true else false
            val displayTitle = if (isSelfChat) "پیام\u200Cهای ذخیره شده" else chat.title
            if (isSelfChat) {
                // Bookmark icon for saved messages
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xFF7C4DFF), Color(0xFF536DFE))
                            )
                        )
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Saved Messages",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                AvatarImage(
                    imageUrl = chat.avatarUrl, name = displayTitle,
                    size = AvatarSize.MEDIUM, isOnline = isOnline,
                    modifier = Modifier.clickable { onAvatarClick() }
                )
            }
            Spacer(Modifier.width(14.dp))
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Chat title — bold if unread
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = VazirFontFamily,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Time — accent if unread
                    Text(
                        text = chat.lastMessage?.let { CHAT_TIME_FORMATTER.format(it.createdAt) } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = VazirFontFamily,
                        color = if (chat.unreadCount > 0) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(5.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    // Message preview — clean up story reply tags
                    val rawContent = chat.lastMessage?.content ?: androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.no_messages)
                    val previewContent = if (rawContent.startsWith("[STORY_REPLY:")) {
                        val closingBracket = rawContent.indexOf(']')
                        if (closingBracket != -1) "📸 پاسخ به استوری: ${rawContent.substring(closingBracket + 1)}" else rawContent
                    } else rawContent
                    Text(
                        text = androidx.compose.ui.text.buildAnnotatedString {
                            append(previewContent)
                            if (chat.lastMessage?.isEdited == true) {
                                append(" ")
                                pushStyle(androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), fontSize = 10.sp))
                                append("(ویرایش\u200Cشده)")
                                pop()
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = VazirFontFamily,
                        color = if (chat.unreadCount > 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    if (chat.unreadCount > 0) {
                        UnreadBadge(count = chat.unreadCount, isMuted = chat.isMuted)
                    }
                }
            }
            if (onUnarchiveClick != null) {
                IconButton(onClick = onUnarchiveClick) {
                    Icon(Icons.Default.Unarchive, contentDescription = "Unarchive", tint = extendedColors.accent)
                }
            }
        }
        // Elegant thin divider — indented to align with text
        HorizontalDivider(
            modifier = Modifier.padding(start = 86.dp, end = 20.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Mock Data
// ═══════════════════════════════════════════════════════════════════════════════

private fun getMockChats(): List<Chat> {
    val now = Instant.now()
    return listOf(
        Chat(id = "1", type = ChatType.PRIVATE, title = "علی محمدی", avatarUrl = null, lastMessage = Message(id = "m1", chatId = "1", senderId = "other", senderName = "علی محمدی", senderAvatar = null, type = MessageType.TEXT, content = "سلام! خوبی؟ چه خبر؟", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.DELIVERED, isEdited = false, createdAt = now.minusSeconds(300), editedAt = null), unreadCount = 2, isPinned = true, isMuted = false, isArchived = false, participants = emptyList(), updatedAt = now.minusSeconds(300)),
        Chat(id = "2", type = ChatType.PRIVATE, title = "سارا احمدی", avatarUrl = null, lastMessage = Message(id = "m2", chatId = "2", senderId = "current_user_id", senderName = "من", senderAvatar = null, type = MessageType.TEXT, content = "فردا جلسه داریم، یادت نره!", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.READ, isEdited = false, createdAt = now.minusSeconds(3600), editedAt = null), unreadCount = 0, isPinned = true, isMuted = false, isArchived = false, participants = emptyList(), updatedAt = now.minusSeconds(3600)),
        Chat(id = "3", type = ChatType.PRIVATE, title = "حسین رضایی", avatarUrl = null, lastMessage = Message(id = "m3", chatId = "3", senderId = "other", senderName = "حسین رضایی", senderAvatar = null, type = MessageType.TEXT, content = "پروژه رو فرستادم، چک کن", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.DELIVERED, isEdited = false, createdAt = now.minusSeconds(7200), editedAt = null), unreadCount = 5, isPinned = false, isMuted = false, isArchived = false, participants = emptyList(), updatedAt = now.minusSeconds(7200)),
        Chat(id = "4", type = ChatType.PRIVATE, title = "مریم کریمی", avatarUrl = null, lastMessage = Message(id = "m4", chatId = "4", senderId = "current_user_id", senderName = "من", senderAvatar = null, type = MessageType.TEXT, content = "ممنون از کمکت 🙏", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.DELIVERED, isEdited = false, createdAt = now.minusSeconds(86400), editedAt = null), unreadCount = 0, isPinned = false, isMuted = true, isArchived = false, participants = emptyList(), updatedAt = now.minusSeconds(86400)),
        Chat(id = "5", type = ChatType.PRIVATE, title = "امیر حسینی", avatarUrl = null, lastMessage = Message(id = "m5", chatId = "5", senderId = "other", senderName = "امیر حسینی", senderAvatar = null, type = MessageType.TEXT, content = "عالی بود! دمت گرم 👍", mediaUrl = null, replyToMessageId = null, replyToMessage = null, forwardedFrom = null, status = MessageStatus.READ, isEdited = false, createdAt = now.minusSeconds(172800), editedAt = null), unreadCount = 1, isPinned = false, isMuted = false, isArchived = false, participants = emptyList(), updatedAt = now.minusSeconds(172800))
    )
}
