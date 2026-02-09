package com.Kelasor.app.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ListItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.Chat
import com.Kelasor.app.domain.model.ChatType
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.ExpandableSearchHeader
import com.Kelasor.app.ui.components.FilterChip
import com.Kelasor.app.ui.components.StoryAvatar
import com.Kelasor.app.ui.components.UnreadBadge
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.viewmodel.ChatListViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import com.Kelasor.app.ui.screens.chat.SelectionTopBar

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat List Screen
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
    onNavigateToUserProfile: (String) -> Unit = {}, // userId -> Navigate to profile
    searchQuery: String = "",
    viewModel: ChatListViewModel = hiltViewModel(),
    storyViewModel: com.Kelasor.app.ui.viewmodel.StoryViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    
    // Stories State
    val storyUiState by storyViewModel.uiState.collectAsState()
    val selectedStoryUser by storyViewModel.selectedStoryUser.collectAsState()
    val isUploadingStory by storyViewModel.isUploading.collectAsState()
    
    // Story Media Picker
    val storyPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
             storyViewModel.uploadStory(uri, "AUTO")
        }
    }
    
    // Story Sheet State
    var showAddStorySheet by remember { mutableStateOf(false) }
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
    
    // Premium Dialog State
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    // Handle Story Error Events (separate from uiState so StoryRow stays visible)
    LaunchedEffect(Unit) {
        storyViewModel.errorEvent.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.StoryErrorEvent.PremiumRequired -> {
                    showPremiumDialog = true
                }
                is com.Kelasor.app.ui.viewmodel.StoryErrorEvent.GenericError -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Local search state removed in favor of passed searchQuery
    // Sync external search query with ViewModel
    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }
    
    var isArchivedExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()


    // Permission handling

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadContacts()
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        }
    }
    
    // Handle Navigation Events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.ChatListEvent.NavigateToChat -> {
                    onChatClick(event.chat)
                }
            }
        }
    }

    // Combine and filter logic
    val filteredChats = remember(state.chats, state.localGroups, state.localChannels, state.pinnedChats, searchQuery, state.activeFilter) {
        val filter = state.activeFilter
        val query = searchQuery.trim()
        
        // 1. Determine base list based on filter
        val candidates: List<Chat> = when (filter) {
            com.Kelasor.app.ui.viewmodel.SearchFilter.ALL -> {
                // If searching or filtering, include everything. If default view, just private.
                if (query.isNotEmpty()) {
                    state.chats + state.localGroups + state.localChannels
                } else {
                    state.pinnedChats + state.chats // Default Home View
                }
            }
            com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE -> state.chats
            com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP -> state.localGroups
            com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL -> state.localChannels
        }

        // 2. Filter by query
        if (query.isEmpty()) candidates
        else candidates.filter { it.title.contains(query, ignoreCase = true) }
    }
    
    // Removing hardcoded RTL provider to follow global app language
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column {
                // Selection Mode Header vs Search Header
                if (state.selectedChatIds.isNotEmpty()) {
                    SelectionTopBar(
                        selectedCount = state.selectedChatIds.size,
                        onClearSelection = { viewModel.clearSelection() },
                        onDeleteSelected = { viewModel.requestDeleteSelection() },
                        onPinSelected = { 
                            // ... (Logic remains same)
                            // Get all selected chats to check if any are pinned
                            val allChats = state.chats + state.pinnedChats + state.archivedChats
                            val selectedChats = allChats.filter { it.id in state.selectedChatIds }
                            val anyPinned = selectedChats.any { it.isPinned }
                            
                            // Toggle: if any are pinned, unpin all; else pin all
                            val newPinState = !anyPinned
                            state.selectedChatIds.forEach { chatId ->
                                viewModel.pinChat(chatId, newPinState)
                            }
                            val toastMessage = if (newPinState) "گفتگو سنجاق شد" else "گفتگو از سنجاق خارج شد"
                            android.widget.Toast.makeText(context, toastMessage, android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.clearSelection()
                        },
                        onArchiveSelected = { 
                             // ... (Logic remains same)
                            val allChats = state.chats + state.pinnedChats + state.archivedChats
                            val selectedChats = allChats.filter { it.id in state.selectedChatIds }
                            val anyArchived = selectedChats.any { it.isArchived }
                            
                            val newArchiveState = !anyArchived
                            state.selectedChatIds.forEach { chatId ->
                                viewModel.archiveChat(chatId, newArchiveState)
                            }
                            val toastMessage = if (newArchiveState) {
                                "گفتگو به آرشیو منتقل شد. برای مشاهده به پروفایل > آرشیو شده‌ها بروید"
                            } else {
                                "گفتگو از آرشیو خارج شد"
                            }
                            android.widget.Toast.makeText(
                                context, 
                                toastMessage, 
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearSelection()
                        }
                    )

                if (state.showDeleteConfirmation) {
                    AlertDialog(
                        onDismissRequest = { viewModel.cancelDeleteSelection() },
                        title = { Text(text = "حذف گفتگوها", style = MessageAppTypography.chatName) },
                        text = { Text(text = "آیا از حذف ${state.selectedChatIds.size} مورد انتخاب شده اطمینان دارید؟ این عملیات غیرقابل بازگشت است.") },
                        confirmButton = {
                            TextButton(
                                onClick = { viewModel.confirmDeleteSelection() }
                            ) {
                                Text(text = "حذف", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.cancelDeleteSelection() }
                            ) {
                                Text(text = "لغو")
                            }
                        }
                    )
                }
                } else {
                    // Search Header Removed - Handled by MainScreen
                    
                    // Filter Chips (Visible when searching or if active filter is not ALL)
                    // We check viewModel state.searchQuery instead of local var
                    AnimatedVisibility(visible = state.searchQuery.isNotEmpty() || state.activeFilter != com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL,
                                    onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) },
                                    label = "همه"
                                )
                            }
                            item {
                                FilterChip(
                                    selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE,
                                    onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE) },
                                    label = "افراد"
                                )
                            }
                            item {
                                FilterChip(
                                    selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP,
                                    onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.GROUP) },
                                    label = "گروه‌ها"
                                )
                            }
                            item {
                                FilterChip(
                                    selected = state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL,
                                    onClick = { viewModel.setFilter(com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL) },
                                    label = "کانال‌ها"
                                )
                            }
                        }
                    }
                }
            }
                // Chat list
                if (state.isLoading && filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Story Row (Always visible if not searching AND not selecting)
                        if (state.searchQuery.isEmpty() && state.selectedChatIds.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                            item(contentType = { "story_row" }) {
                                val storiesState = storyUiState
                                if (storiesState is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success || storiesState is com.Kelasor.app.ui.viewmodel.StoriesUiState.Loading) {
                                    val storyUsers = if (storiesState is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success) {
                                        storiesState.storyUsers
                                    } else {
                                        emptyList()
                                    }
                                    val currentUserStory = storyUsers.find { it.isCurrentUser }
                                    val currentUserProfile by storyViewModel.currentUser.collectAsState()
                                    
                                    com.Kelasor.app.ui.components.story.StoriesList(
                                        currentUser = currentUserStory ?: currentUserProfile?.let { user ->
                                            com.Kelasor.app.domain.model.StoryUser(
                                                userId = user.id,
                                                username = user.username ?: "",
                                                displayName = user.displayName ?: "You",
                                                avatarUrl = user.avatarUrl,
                                                stories = emptyList(),
                                                isCurrentUser = true
                                            )
                                        },
                                        storyUsers = storyUsers,
                                        onStoryClick = { user ->
                                            if (user.isCurrentUser) {
                                                onMyStoriesClick()
                                            } else {
                                                storyViewModel.openStoryViewer(user)
                                            }
                                        },
                                        onAddStoryClick = {
                                            showAddStorySheet = true
                                        }
                                    )
                                }
                            }
                        }

                        // Empty State Item
                        if (filteredChats.isEmpty() && state.searchQuery.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "گفتگویی وجود ندارد",
                                            style = MessageAppTypography.chatName,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "یک گفتگوی جدید آغاز کنید",
                                            style = MessageAppTypography.chatPreview,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        } else {
                            // ... Regular Content (will be rendered below)
                        
                        // Global User Search Results
                        if (state.searchResults.isNotEmpty() && 
                           (state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL || state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.PEOPLE)) {
                            item(contentType = { "header" }) {
                                SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.global_search))
                            }
                            items(
                                items = state.searchResults,
                                key = { "user_${it.id}" },
                                contentType = { "search_user" }
                            ) { user ->
                                SearchUserItem(
                                   user = user,
                                   onClick = { viewModel.onUserSelected(user.id) }
                                )
                            }
                        }
                        
                        // Global Channel Search Results
                        if (state.channelSearchResults.isNotEmpty() &&
                           (state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL || state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.CHANNEL)) {
                             item(contentType = { "header" }) {
                                SectionHeader(title = "کانال‌های جهانی")
                            }
                            items(
                                items = state.channelSearchResults,
                                key = { "channel_${it.id}" },
                                contentType = { "chat_item" }
                            ) { channel ->
                                val chat = Chat(
                                    id = channel.id,
                                    type = ChatType.CHANNEL,
                                    title = channel.name,
                                    avatarUrl = channel.avatarUrl,
                                    lastMessage = if (!channel.publicId.isNullOrBlank()) {
                                        Message(
                                            id = "",
                                            chatId = channel.id,
                                            senderId = "",
                                            senderName = "",
                                            senderAvatar = null,
                                            type = MessageType.TEXT,
                                            content = "@${channel.publicId}",
                                            mediaUrl = null,
                                            replyToMessageId = null,
                                            replyToMessage = null,
                                            forwardedFrom = null,
                                            status = MessageStatus.SENT,
                                            isEdited = false,
                                            createdAt = channel.createdAt,
                                            editedAt = null,
                                            reactions = emptyMap()
                                        )
                                    } else null,
                                    unreadCount = 0,
                                    isPinned = false,
                                    isMuted = false,
                                    isArchived = false,
                                    participants = emptyList(),
                                    updatedAt = channel.createdAt
                                )
                                ChatItem(
                                    chat = chat,
                                    onClick = { onChatAvatarClick(chat) }, 
                                    onAvatarClick = { onChatAvatarClick(chat) },
                                    currentUserId = state.currentUserId
                                )
                            }
                        }

                        // Local Chats
                        if (filteredChats.isNotEmpty()) {
                            if (state.searchResults.isNotEmpty()) {
                                item(contentType = { "header" }) {
                                    SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.your_chats))
                                }
                            }
                        }
                        
                        
                        // If not searching and ALL, show Pinned/Archived/All logic. 
                        // If searching or filtering, just show filtered list directly
                        if (state.searchQuery.isEmpty() && state.activeFilter == com.Kelasor.app.ui.viewmodel.SearchFilter.ALL) {
                            // Archived chats section (clickable to expand/collapse)
                            if (state.archivedChats.isNotEmpty()) {
                                item(contentType = { "archive_header" }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isArchivedExpanded = !isArchivedExpanded }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Archive,
                                                contentDescription = null,
                                                tint = extendedColors.accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "آرشیو شده (${state.archivedChats.size})",
                                                style = MessageAppTypography.sectionTitle,
                                                color = extendedColors.accent
                                            )
                                        }
                                        Icon(
                                            imageVector = if (isArchivedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                if (isArchivedExpanded) {
                                    items(
                                        items = state.archivedChats,
                                        key = { "archived_${it.id}" },
                                        contentType = { "chat_item" }
                                    ) { chat ->
                                        ChatItem(
                                            chat = chat,
                                            isSelected = state.selectedChatIds.contains(chat.id),
                                            inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                            onClick = { 
                                                if (state.selectedChatIds.isNotEmpty()) {
                                                    viewModel.toggleChatSelection(chat.id)
                                                } else {
                                                    onChatClick(chat) 
                                                }
                                            },
                                            onLongClick = { viewModel.toggleChatSelection(chat.id) },
                                            onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                            onMute = { viewModel.muteChat(chat.id, !chat.isMuted) },
                                            onArchive = { viewModel.archiveChat(chat.id, false) }, // Unarchive
                                            onDelete = { viewModel.deleteChat(chat.id) },
                                            currentUserId = state.currentUserId,
                                            onUnarchiveClick = { viewModel.archiveChat(chat.id, false) }
                                        )
                                    }
                                }
                            }
                            
                            // Pinned chats section
                            if (state.pinnedChats.isNotEmpty()) {
                                item(contentType = { "header" }) {
                                    SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.pinned))
                                }
                                items(
                                    items = state.pinnedChats,
                                    key = { it.id },
                                    contentType = { "chat_item" }
                                ) { chat ->
                                    ChatItem(
                                        chat = chat,
                                        isSelected = state.selectedChatIds.contains(chat.id),
                                        inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                        onClick = { 
                                            if (state.selectedChatIds.isNotEmpty()) {
                                                viewModel.toggleChatSelection(chat.id)
                                            } else {
                                                onChatClick(chat) 
                                            }
                                        },
                                        onLongClick = { viewModel.toggleChatSelection(chat.id) },
                                        onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                        onMute = { viewModel.muteChat(chat.id, !chat.isMuted) },
                                        onArchive = { viewModel.archiveChat(chat.id, true) },
                                        onDelete = { viewModel.deleteChat(chat.id) },
                                        currentUserId = state.currentUserId
                                    )
                                }
                            }
                             // Regular chats
                            if (state.pinnedChats.isNotEmpty() && state.chats.isNotEmpty()) {
                                item(contentType = { "header" }) {
                                    SectionHeader(title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.all_chats))
                                }
                            }
                            items(
                                items = state.chats,
                                key = { it.id },
                                contentType = { "chat_item" }
                            ) { chat ->
                                ChatItem(
                                    chat = chat,
                                    isSelected = state.selectedChatIds.contains(chat.id),
                                    inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                    onClick = { 
                                        if (state.selectedChatIds.isNotEmpty()) {
                                            viewModel.toggleChatSelection(chat.id)
                                        } else {
                                            onChatClick(chat) 
                                        }
                                    },
                                    onLongClick = { viewModel.toggleChatSelection(chat.id) },
                                    onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                                    onMute = { viewModel.muteChat(chat.id, !chat.isMuted) },
                                    onArchive = { viewModel.archiveChat(chat.id, true) },
                                    onDelete = { viewModel.deleteChat(chat.id) },
                                    onAvatarClick = { onChatAvatarClick(chat) },
                                    currentUserId = state.currentUserId
                                )
                            }
                        } else {
                             // Search Results Mode (Local)
                             items(
                                 items = filteredChats,
                                 key = { it.id },
                                 contentType = { "chat_item" }
                             ) { chat ->
                                ChatItem(
                                    chat = chat,
                                    isSelected = state.selectedChatIds.contains(chat.id),
                                    inSelectionMode = state.selectedChatIds.isNotEmpty(),
                                    onClick = { 
                                        if (state.selectedChatIds.isNotEmpty()) {
                                            viewModel.toggleChatSelection(chat.id)
                                        } else {
                                            onChatClick(chat) 
                                        }
                                    },
                                    onLongClick = { viewModel.toggleChatSelection(chat.id) },
                                    onPin = { viewModel.pinChat(chat.id, !chat.isPinned) }, // Use !chat.isPinned for toggle
                                    onMute = { viewModel.muteChat(chat.id, !chat.isMuted) },
                                    onArchive = { viewModel.archiveChat(chat.id, true) },
                                    onDelete = { viewModel.deleteChat(chat.id) }
                                )
                        } // Close Search Results else
                    } // Close LazyColumn
                } // Close else for (isLoading) Check
                }
            // FAB REMOVED

            // Upload Loading Indicator
            if (isUploadingStory) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uploading Story...", color = Color.White)
                    }
                }
            }
            
            // Add Story Sheet
            if (showAddStorySheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showAddStorySheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "افزودن استوری",
                            style = MessageAppTypography.chatName,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        ListItem(
                            headlineContent = { Text("عکس یا ویدیو") },
                            leadingContent = { 
                                Icon(androidx.compose.material.icons.Icons.Default.Image, contentDescription = null) 
                            },
                            modifier = Modifier.clickable {
                                showAddStorySheet = false
                                storyPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text("متن با پس‌زمینه") },
                            leadingContent = { 
                                Icon(androidx.compose.material.icons.Icons.Default.Create, contentDescription = null) 
                            },
                            modifier = Modifier.clickable {
                                showAddStorySheet = false
                                onNavigateToCreateTextStory()
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            // Story Viewer Overlay
            AnimatedVisibility(
                visible = selectedStoryUser != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                selectedStoryUser?.let { user ->
                    com.Kelasor.app.ui.screens.story.StoryViewerScreen(
                        viewModel = storyViewModel,
                        storyUser = user,
                        initialStoryIndex = 0, // Or find unviewed index
                        onClose = { storyViewModel.closeStoryViewer() },
                        onStoryViewed = { story ->
                            storyViewModel.markStoryAsViewed(story.id)
                        },
                        onNavigateToProfile = { userId ->
                            storyViewModel.closeStoryViewer()
                            onNavigateToUserProfile(userId)
                        }
                    )
                }
            }
    
    if (showPremiumDialog) {
        com.Kelasor.app.ui.components.PremiumUpgradeDialog(
            onDismiss = { showPremiumDialog = false },
            onUpgrade = {
                showPremiumDialog = false
                android.widget.Toast.makeText(context, "Navigate to Premium Purchase", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
}
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MessageAppTypography.sectionTitle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔍 Search User Item
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SearchUserItem(
    user: com.Kelasor.app.domain.model.User,
    onClick: () -> Unit
) {
     Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            imageUrl = user.displayAvatarUrl,
            name = user.displayName,
            size = AvatarSize.MEDIUM,
            isOnline = user.isOnline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = user.displayName,
                style = MessageAppTypography.chatName,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Chat Item with Swipe Actions
// ═══════════════════════════════════════════════════════════════════════════════

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
    onUnarchiveClick: (() -> Unit)? = null // Optional unarchive action
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())

    // Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_title), style = MessageAppTypography.chatName) },
            text = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.cancel))
                }
            }
        )
    }

    // Main content (no swipe)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isSelected) extendedColors.accent.copy(alpha = 0.15f) 
                else MaterialTheme.colorScheme.background
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Avatar
            val isOnline = if (chat.type == ChatType.PRIVATE) {
                 chat.participants.find { it.id != currentUserId }?.isOnline == true
            } else {
                 false
            }
            
            AvatarImage(
                imageUrl = chat.avatarUrl,
                name = chat.title,
                size = AvatarSize.MEDIUM,
                isOnline = isOnline,
                modifier = Modifier.clickable { onAvatarClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.title,
                        style = MessageAppTypography.chatName,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = chat.lastMessage?.let { timeFormatter.format(it.createdAt) } ?: "",
                        style = MessageAppTypography.chatTime,
                        color = if (chat.unreadCount > 0) {
                            extendedColors.accent
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage?.content ?: androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.no_messages),
                        style = MessageAppTypography.chatPreview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (chat.unreadCount > 0) {
                        UnreadBadge(
                            count = chat.unreadCount,
                            isMuted = chat.isMuted
                        )
                    }
                }
            }

            // Unarchive Button
            if (onUnarchiveClick != null) {
                IconButton(onClick = onUnarchiveClick) {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = "Unarchive",
                        tint = extendedColors.accent
                    )
                }
            }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Mock Data
// ═══════════════════════════════════════════════════════════════════════════════

private fun getMockChats(): List<Chat> {
    val now = Instant.now()
    return listOf(
        Chat(
            id = "1",
            type = ChatType.PRIVATE,
            title = "علی محمدی",
            avatarUrl = null,
            lastMessage = Message(
                id = "m1",
                chatId = "1",
                senderId = "other",
                senderName = "علی محمدی",
                senderAvatar = null,
                type = MessageType.TEXT,
                content = "سلام! خوبی؟ چه خبر؟",
                mediaUrl = null,
                replyToMessageId = null,
                replyToMessage = null,
                forwardedFrom = null,
                status = MessageStatus.DELIVERED,
                isEdited = false,
                createdAt = now.minusSeconds(300),
                editedAt = null
            ),
            unreadCount = 2,
            isPinned = true,
            isMuted = false,
            isArchived = false,
            participants = emptyList(),
            updatedAt = now.minusSeconds(300)
        ),
        Chat(
            id = "2",
            type = ChatType.PRIVATE,
            title = "سارا احمدی",
            avatarUrl = null,
            lastMessage = Message(
                id = "m2",
                chatId = "2",
                senderId = "current_user_id",
                senderName = "من",
                senderAvatar = null,
                type = MessageType.TEXT,
                content = "فردا جلسه داریم، یادت نره!",
                mediaUrl = null,
                replyToMessageId = null,
                replyToMessage = null,
                forwardedFrom = null,
                status = MessageStatus.READ,
                isEdited = false,
                createdAt = now.minusSeconds(3600),
                editedAt = null
            ),
            unreadCount = 0,
            isPinned = true,
            isMuted = false,
            isArchived = false,
            participants = emptyList(),
            updatedAt = now.minusSeconds(3600)
        ),
        Chat(
            id = "3",
            type = ChatType.PRIVATE,
            title = "حسین رضایی",
            avatarUrl = null,
            lastMessage = Message(
                id = "m3",
                chatId = "3",
                senderId = "other",
                senderName = "حسین رضایی",
                senderAvatar = null,
                type = MessageType.TEXT,
                content = "پروژه رو فرستادم، چک کن",
                mediaUrl = null,
                replyToMessageId = null,
                replyToMessage = null,
                forwardedFrom = null,
                status = MessageStatus.DELIVERED,
                isEdited = false,
                createdAt = now.minusSeconds(7200),
                editedAt = null
            ),
            unreadCount = 5,
            isPinned = false,
            isMuted = false,
            isArchived = false,
            participants = emptyList(),
            updatedAt = now.minusSeconds(7200)
        ),
        Chat(
            id = "4",
            type = ChatType.PRIVATE,
            title = "مریم کریمی",
            avatarUrl = null,
            lastMessage = Message(
                id = "m4",
                chatId = "4",
                senderId = "current_user_id",
                senderName = "من",
                senderAvatar = null,
                type = MessageType.TEXT,
                content = "ممنون از کمکت 🙏",
                mediaUrl = null,
                replyToMessageId = null,
                replyToMessage = null,
                forwardedFrom = null,
                status = MessageStatus.DELIVERED,
                isEdited = false,
                createdAt = now.minusSeconds(86400),
                editedAt = null
            ),
            unreadCount = 0,
            isPinned = false,
            isMuted = true,
            isArchived = false,
            participants = emptyList(),
            updatedAt = now.minusSeconds(86400)
        ),
        Chat(
            id = "5",
            type = ChatType.PRIVATE,
            title = "امیر حسینی",
            avatarUrl = null,
            lastMessage = Message(
                id = "m5",
                chatId = "5",
                senderId = "other",
                senderName = "امیر حسینی",
                senderAvatar = null,
                type = MessageType.TEXT,
                content = "عالی بود! دمت گرم 👍",
                mediaUrl = null,
                replyToMessageId = null,
                replyToMessage = null,
                forwardedFrom = null,
                status = MessageStatus.READ,
                isEdited = false,
                createdAt = now.minusSeconds(172800),
                editedAt = null
            ),
            unreadCount = 1,
            isPinned = false,
            isMuted = false,
            isArchived = false,
            participants = emptyList(),
            updatedAt = now.minusSeconds(172800)
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔵 Story Row Component
// ═══════════════════════════════════════════════════════════════════════════════



