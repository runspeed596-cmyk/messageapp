package com.hasani.messageapp.ui.screens.channel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import com.hasani.messageapp.domain.model.Channel
import com.hasani.messageapp.domain.model.User
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.components.ExpandableSearchHeader
import com.hasani.messageapp.ui.components.UnreadBadge
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import java.time.Instant

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel List Screen - With Selection Mode Support
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    onChannelClick: (String) -> Unit,
    onCreateChannelClick: () -> Unit,
    onNavigateToChannelStories: (String, String) -> Unit = { _, _ -> }, // channelId, channelName
    onMyStoriesClick: () -> Unit = {},
    onNavigateToCreateTextStory: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    searchQuery: String = "",
    viewModel: com.hasani.messageapp.ui.viewmodel.ChannelListViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    storyViewModel: com.hasani.messageapp.ui.viewmodel.StoryViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Use ViewModel selection state
    val inSelectionMode = state.selectedChannelIds.isNotEmpty()
    
    // Stories State (using channelUiState for channel stories)
    val storyUiState by storyViewModel.channelUiState.collectAsState()
    val selectedStoryUser by storyViewModel.selectedStoryUser.collectAsState()
    val currentUserProfile by storyViewModel.currentUser.collectAsState()
    
    // Story Media Picker
    var showAddStorySheet by remember { mutableStateOf(false) }

    
    // Filter channels for stories (Admin only)
    val adminChannels = remember(state.channels) {
        state.channels.filter { it.isAdmin }
    }
    
    // Channel Selection Logic
    var showChannelSelectionSheet by remember { mutableStateOf(false) }
    var selectedChannelIdForStory by remember { mutableStateOf<String?>(null) }
    
    // Premium Dialog State
    var showPremiumDialog by remember { mutableStateOf(false) }

    // Handle Story Error Events (separate from uiState so StoryRow stays visible)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        storyViewModel.errorEvent.collect { event ->
            when (event) {
                is com.hasani.messageapp.ui.viewmodel.StoryErrorEvent.PremiumRequired -> {
                    showPremiumDialog = true
                }
                is com.hasani.messageapp.ui.viewmodel.StoryErrorEvent.GenericError -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    // Load Channel Stories on Enter
    androidx.compose.runtime.LaunchedEffect(Unit) {
        storyViewModel.loadChannelStories()
    }

    val storyPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && selectedChannelIdForStory != null) {
            storyViewModel.uploadChannelStory(selectedChannelIdForStory!!, uri, "AUTO")
            selectedChannelIdForStory = null
        }
    }
    
    // Sync external search query with ViewModel
    androidx.compose.runtime.LaunchedEffect(searchQuery) {
        viewModel.searchChannels(searchQuery)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column {
            // ... (Selection Mode Header - Unchanged)
             AnimatedVisibility(
                visible = inSelectionMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ChannelSelectionTopBar(
                    selectedCount = state.selectedChannelIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onLeaveSelected = { viewModel.requestDeleteSelection() },
                    onPinSelected = {
                        viewModel.pinSelectedChannels(true)
                        android.widget.Toast.makeText(context, "کانال‌ها سنجاق شدند", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onArchiveSelected = {
                        state.selectedChannelIds.forEach { channelId ->
                            viewModel.archiveChannel(channelId, true)
                        }
                        android.widget.Toast.makeText(context, "کانال‌ها به آرشیو منتقل شدند", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.clearSelection()
                    }
                )
            }
            
            // ... (Loading/Empty logic - Unchanged)
            if (state.isLoading && state.channels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Story Row (Always visible if not searching and not in selection mode)
                    if (searchQuery.isEmpty() && !inSelectionMode) {
                        item(contentType = { "story_row" }) {
                            val storiesState = storyUiState
                            if (storiesState is com.hasani.messageapp.ui.viewmodel.StoriesUiState.Success || storiesState is com.hasani.messageapp.ui.viewmodel.StoriesUiState.Loading) {
                                val storyUsers = if (storiesState is com.hasani.messageapp.ui.viewmodel.StoriesUiState.Success) {
                                    storiesState.storyUsers
                                } else {
                                    emptyList()
                                }
                                com.hasani.messageapp.ui.components.story.StoriesList(
                                    currentUser = com.hasani.messageapp.domain.model.StoryUser(
                                        userId = "channel_add",
                                        username = "",
                                        displayName = "استوری کانال",
                                        avatarUrl = null,
                                        stories = emptyList(),
                                        isCurrentUser = true
                                    ),
                                    storyUsers = storyUsers,
                                    onStoryClick = { user ->
                                        if (user.isCurrentUser) {
                                            if (adminChannels.isNotEmpty()) {
                                                showChannelSelectionSheet = true
                                            } else {
                                                android.widget.Toast.makeText(context, "شما ادمین هیچ کانالی نیستید", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            storyViewModel.openStoryViewer(user)
                                        }
                                    },
                                    onAddStoryClick = {
                                        if (adminChannels.isNotEmpty()) {
                                            showChannelSelectionSheet = true
                                        } else {
                                            android.widget.Toast.makeText(context, "شما ادمین هیچ کانالی نیستید", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    excludeCurrentUserFromList = false
                                )
                            }
                        }
                    }

                    // Empty State
                    if (searchQuery.isEmpty() && state.channels.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("کانالی وجود ندارد", style = MessageAppTypography.chatName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("یک کانال جدید بسازید", style = MessageAppTypography.chatPreview, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }
                    } else {

                        // ... (Rest of lists)
                         if (state.archivedChannels.isNotEmpty() && !inSelectionMode) {
                              item {
                                var isExpanded by remember { mutableStateOf(false) }
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isExpanded = !isExpanded }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Archive, null, tint = extendedColors.accent, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("آرشیو شده (${state.archivedChannels.size})", style = MessageAppTypography.sectionTitle, color = extendedColors.accent)
                                        }
                                        Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isExpanded) {
                                        state.archivedChannels.forEach { channel ->
                                            ChannelListItem(
                                                channel = channel,
                                                isSelected = false,
                                                inSelectionMode = false,
                                                onClick = { onChannelClick(channel.id) },
                                                onLongClick = { viewModel.archiveChannel(channel.id, false) },
                                                onUnarchiveClick = { viewModel.archiveChannel(channel.id, false) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                         if (state.pinnedChannels.isNotEmpty()) {
                             item {
                                Text(stringResource(com.hasani.messageapp.R.string.pinned), style = MessageAppTypography.sectionTitle, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                            items(state.pinnedChannels, key = { it.id }) { channel ->
                                ChannelListItem(
                                    channel = channel,
                                    isSelected = state.selectedChannelIds.contains(channel.id),
                                    inSelectionMode = inSelectionMode,
                                    onClick = { if (inSelectionMode) viewModel.toggleSelection(channel.id) else onChannelClick(channel.id) },
                                    onLongClick = { viewModel.toggleSelection(channel.id) },
                                    onPin = { viewModel.pinChannel(channel.id, false) }
                                )
                            }
                        }
                        } // close else (archived/pinned/all)


                        if (state.pinnedChannels.isNotEmpty() && state.filteredChannels.isNotEmpty()) {
                            item { Text("همه کانال‌ها", style = MessageAppTypography.sectionTitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                        }
                        
                        items(state.filteredChannels, key = { it.id }) { channel ->
                             ChannelListItem(
                                channel = channel,
                                isSelected = state.selectedChannelIds.contains(channel.id),
                                inSelectionMode = inSelectionMode,
                                onClick = { if (inSelectionMode) viewModel.toggleSelection(channel.id) else onChannelClick(channel.id) },
                                onLongClick = { viewModel.toggleSelection(channel.id) },
                                onPin = { viewModel.pinChannel(channel.id, true) }
                            )
                        }
                    }

                }
            }

        
        // ... (Leave Dialog Unchanged)
        if (state.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteSelection() },
                title = { Text("خروج از کانال‌ها") },
                text = { Text("آیا از خروج از ${state.selectedChannelIds.size} کانال مطمئن هستید؟") },
                confirmButton = { TextButton(onClick = { viewModel.confirmDeleteSelection() }) { Text("خروج", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { viewModel.cancelDeleteSelection() }) { Text("انصراف") } }
            )
        }

        // Add Story - Channel Selection Bottom Sheet
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
                        style = MessageAppTypography.chatName,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn {
                        items(adminChannels) { channel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedChannelIdForStory = channel.id
                                        showChannelSelectionSheet = false
                                        onNavigateToChannelStories(channel.id, channel.name)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(
                                    imageUrl = channel.avatarUrl,
                                    name = channel.name,
                                    size = AvatarSize.SMALL
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = channel.name,
                                    style = MessageAppTypography.chatName
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
        
        // Story Viewer Overlay
        AnimatedVisibility(
            visible = selectedStoryUser != null,
            enter = androidx.compose.animation.scaleIn() + fadeIn(),
            exit = androidx.compose.animation.scaleOut() + fadeOut()
        ) {
            selectedStoryUser?.let { user ->
                com.hasani.messageapp.ui.screens.story.StoryViewerScreen(
                    viewModel = storyViewModel,
                    storyUser = user,
                    initialStoryIndex = 0,
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
            com.hasani.messageapp.ui.components.PremiumUpgradeDialog(
                onDismiss = { showPremiumDialog = false },
                onUpgrade = {
                    showPremiumDialog = false
                    android.widget.Toast.makeText(context, "Navigate to Premium Purchase", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}


@Composable
private fun ChannelSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onLeaveSelected: () -> Unit,
    onPinSelected: () -> Unit,
    onArchiveSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MessageAppTheme.extendedColors.accent.copy(alpha = 0.1f))
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClearSelection) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$selectedCount",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        IconButton(onClick = onArchiveSelected) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "Archive",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onPinSelected) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pin",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onLeaveSelected) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Leave",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelListItem(
    channel: Channel,
    isSelected: Boolean = false,
    inSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onUnarchiveClick: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) {
        MessageAppTheme.extendedColors.accent.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    
    val extendedColors = MessageAppTheme.extendedColors
    
    // Main Content (no swipe)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Avatar with selection indicator
            Box {
                AvatarImage(
                    imageUrl = channel.avatarUrl,
                    name = channel.name,
                    size = AvatarSize.MEDIUM
                )
                
                // Selection indicator overlay
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MessageAppTheme.extendedColors.accent.copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = channel.name,
                            style = MessageAppTypography.chatName,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                   if (channel.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MessageAppTheme.extendedColors.accent,
                            modifier = Modifier.size(16.dp).padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when {
                            !channel.publicId.isNullOrBlank() -> "@${channel.publicId}"
                            channel.lastPost != null -> channel.lastPost.content ?: ""
                            else -> stringResource(com.hasani.messageapp.R.string.subscribers_count, channel.subscriberCount)
                        },
                        style = MessageAppTypography.chatPreview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (channel.unreadCount > 0) {
                         Spacer(modifier = Modifier.width(8.dp))
                         UnreadBadge(count = channel.unreadCount)
                    }
                }
            }
            
            // Unarchive Button
            if (onUnarchiveClick != null) {
                IconButton(onClick = onUnarchiveClick) {
                    Icon(
                        imageVector = Icons.Default.Unarchive,
                        contentDescription = "Unarchive",
                        tint = MessageAppTheme.extendedColors.accent
                    )
                }
            }
    }
}
