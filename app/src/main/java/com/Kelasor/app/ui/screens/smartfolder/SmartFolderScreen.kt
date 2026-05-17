package com.Kelasor.app.ui.screens.smartfolder

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.data.remote.dto.SmartFolderChannelDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.SmartFolderViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder Channel List Screen
// Reusable for Teachers, Elm Club, and Courses tabs
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SmartFolderTabContent(
    folderType: String,
    title: String,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    emptyMessage: String,
    accentColor: Color,
    onChannelClick: (String) -> Unit,
    onGroupClick: (String) -> Unit = {},
    onMyStoriesClick: () -> Unit = {},
    onNavigateToChannelStories: (String, String) -> Unit = { _, _ -> },
    viewModel: SmartFolderViewModel = hiltViewModel(),
    storyViewModel: com.Kelasor.app.ui.viewmodel.StoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val channels: List<SmartFolderChannelDto> = when (folderType) {
        "TEACHERS" -> state.teacherChannels
        "ELM_CLUB" -> state.elmClubChannels.filter { it.isSubscribed }
        "COURSES" -> state.courseChannels
        else -> emptyList()
    }
    val isLoading: Boolean = state.isLoading
    val error: String? = state.error
    // Story state
    val storyUiState by storyViewModel.channelUiState.collectAsState()
    var showChannelSelectionSheet by remember { mutableStateOf(false) }
    var showMyStoriesChannelSheet by remember { mutableStateOf(false) }
    var selectedChannelIdForStory by remember { mutableStateOf<String?>(null) }
    val subscribedChannels = remember(channels) { channels.filter { it.isSubscribed } }
    var showPremiumDialog by remember { mutableStateOf(false) }
    // Handle Story Error Events
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
    // Load Channel Stories on Enter
    LaunchedEffect(Unit) { storyViewModel.loadChannelStories() }
    val storyPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && selectedChannelIdForStory != null) {
            storyViewModel.uploadChannelStory(selectedChannelIdForStory!!, uri, "AUTO")
            selectedChannelIdForStory = null
        }
    }
    val chatListViewModel: com.Kelasor.app.ui.viewmodel.ChatListViewModel = hiltViewModel()
    val chatListState by chatListViewModel.state.collectAsState()
    val selectedChatIds = chatListState.selectedChatIds
    val allChats = chatListState.chats + chatListState.pinnedChats + chatListState.archivedChats

    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedChatIds.isNotEmpty()) {
            com.Kelasor.app.ui.screens.chat.SelectionTopBar(
                selectedCount = selectedChatIds.size,
                onClearSelection = { chatListViewModel.clearSelection() },
                onDeleteSelected = { chatListViewModel.requestDeleteSelection() },
                onPinSelected = {
                    val selectedChats = allChats.filter { it.id in selectedChatIds }
                    val newPinState = !selectedChats.any { it.isPinned }
                    selectedChatIds.forEach { chatId -> chatListViewModel.pinChat(chatId, newPinState) }
                    chatListViewModel.clearSelection()
                },
                onArchiveSelected = {
                    val selectedChats = allChats.filter { it.id in selectedChatIds }
                    val newArchiveState = !selectedChats.any { it.isArchived }
                    selectedChatIds.forEach { chatId -> chatListViewModel.archiveChat(chatId, newArchiveState) }
                    chatListViewModel.clearSelection()
                }
            )
        }

        if (chatListState.showDeleteConfirmation) {
            val selectedChats = allChats.filter { it.id in selectedChatIds }
            val selectedName = if (selectedChats.size == 1) selectedChats.first().title else "${selectedChatIds.size} گفتگو"
            AlertDialog(
                onDismissRequest = { chatListViewModel.cancelDeleteSelection() },
                title = { Text("حذف گفتگو", fontFamily = DanaFontFamily) },
                text = { Text("آیا مطمئن هستید که می‌خواهید $selectedName را حذف کنید؟ این عمل غیرقابل بازگشت است.", fontFamily = DanaFontFamily) },
                confirmButton = {
                    TextButton(onClick = { chatListViewModel.confirmDeleteSelection() }) {
                        Text("حذف", color = MaterialTheme.colorScheme.error, fontFamily = DanaFontFamily)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { chatListViewModel.cancelDeleteSelection() }) {
                        Text("انصراف", fontFamily = DanaFontFamily)
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = error ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            fontFamily = DanaFontFamily
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.loadSmartFolders() }) {
                            Text("تلاش مجدد", fontFamily = DanaFontFamily, color = accentColor)
                        }
                    }
                }
                channels.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = emptyIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            fontFamily = DanaFontFamily
                        )
                    }
                }
                else -> {
                    val sortedChannels: List<SmartFolderChannelDto> = remember(channels) {
                        channels.sortedByDescending { it.createdAt ?: "" }
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(key = "${folderType}_stories_row", contentType = "stories") {
                            val storyUsers = when (val uiState = storyUiState) {
                                is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success -> uiState.storyUsers
                                else -> emptyList()
                            }
                            val currentUser = storyUsers.firstOrNull { it.isCurrentUser }
                            com.Kelasor.app.ui.components.story.StoriesList(
                                currentUser = currentUser,
                                storyUsers = storyUsers,
                                onStoryClick = { su ->
                                    if (su.isCurrentUser) {
                                        if (subscribedChannels.size == 1) {
                                            onNavigateToChannelStories(subscribedChannels.first().id, subscribedChannels.first().name)
                                        } else if (subscribedChannels.isNotEmpty()) {
                                            showMyStoriesChannelSheet = true
                                        } else {
                                            onMyStoriesClick()
                                        }
                                    } else {
                                        storyViewModel.openStoryViewer(su)
                                    }
                                },
                                onAddStoryClick = { showChannelSelectionSheet = true }
                            )
                        }
                        items(sortedChannels, key = { it.id }) { channel ->
                            val mappedChat = allChats.find { it.id == channel.id } ?: com.Kelasor.app.domain.model.Chat(
                                id = channel.id,
                                type = if (channel.chatType == "GROUP") com.Kelasor.app.domain.model.ChatType.GROUP else com.Kelasor.app.domain.model.ChatType.CHANNEL,
                                title = channel.name,
                                avatarUrl = channel.avatarUrl,
                                lastMessage = null,
                                unreadCount = channel.unreadCount,
                                isPinned = false,
                                isMuted = false,
                                isArchived = false,
                                participants = emptyList(),
                                updatedAt = java.time.Instant.now()
                            )
                            com.Kelasor.app.ui.screens.chat.ChatItem(
                                chat = mappedChat,
                                isSelected = selectedChatIds.contains(channel.id),
                                inSelectionMode = selectedChatIds.isNotEmpty(),
                                onClick = {
                                    if (selectedChatIds.isNotEmpty()) {
                                        chatListViewModel.toggleChatSelection(channel.id)
                                    } else {
                                        if (channel.chatType == "GROUP") onGroupClick(channel.id) else onChannelClick(channel.id)
                                    }
                                },
                                onLongClick = {
                                    chatListViewModel.toggleChatSelection(channel.id)
                                },
                                onPin = {
                                    chatListViewModel.pinChat(channel.id, !mappedChat.isPinned)
                                },
                                onMute = {
                                    chatListViewModel.muteChat(channel.id, !mappedChat.isMuted)
                                },
                                onArchive = {
                                    chatListViewModel.archiveChat(channel.id, !mappedChat.isArchived)
                                },
                                onDelete = {
                                    chatListViewModel.deleteChat(channel.id)
                                }
                            )
                        }
                    }
                }
            }
        }
        // Channel Selection Bottom Sheet for Story Upload
        if (showChannelSelectionSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showChannelSelectionSheet = false },
                sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ارسال استوری به عنوان...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = DanaFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    if (channels.isEmpty()) {
                        Text(
                            text = "هیچ کانالی یافت نشد",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = DanaFontFamily
                        )
                    } else {
                        LazyColumn {
                            items(channels.filter { it.isSubscribed }) { channel ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedChannelIdForStory = channel.id
                                            showChannelSelectionSheet = false
                                            storyPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                                )
                                            )
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(accentColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (channel.avatarUrl != null) {
                                            coil3.compose.AsyncImage(
                                                model = channel.avatarUrl,
                                                contentDescription = channel.name,
                                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = channel.name.take(1),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor,
                                                fontFamily = DanaFontFamily
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = DanaFontFamily
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        // My Stories - Channel Selection Sheet (navigate to channel stories manager)
        if (showMyStoriesChannelSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showMyStoriesChannelSheet = false },
                sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "استوری‌های من - انتخاب کانال",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = DanaFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    LazyColumn {
                        items(subscribedChannels) { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showMyStoriesChannelSheet = false
                                        onNavigateToChannelStories(channel.id, channel.name)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(accentColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (channel.avatarUrl != null) {
                                        coil3.compose.AsyncImage(
                                            model = channel.avatarUrl,
                                            contentDescription = channel.name,
                                            modifier = Modifier.size(40.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = channel.name.take(1),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            fontFamily = DanaFontFamily
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = DanaFontFamily
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
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

// ═══════════════════════════════════════════════════════════════════════════════
// 📇 Channel Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SmartFolderChannelCard(
    channel: SmartFolderChannelDto,
    accentColor: Color,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            accentColor.copy(alpha = 0.2f)
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
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = channel.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = DanaFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = DanaFontFamily,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (channel.isVerifiedTeacher) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تایید شده",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.lastMessage ?: "${channel.subscriberCount} عضو",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (channel.unreadCount > 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = DanaFontFamily,
                    modifier = Modifier.weight(1f)
                )
                if (channel.unreadCount > 0) {
                    Spacer(Modifier.width(8.dp))
                    com.Kelasor.app.ui.components.UnreadBadge(count = channel.unreadCount)
                }
            }
        }

        // Subscription badge
        if (channel.isSubscribed) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "عضو",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontFamily = DanaFontFamily,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
