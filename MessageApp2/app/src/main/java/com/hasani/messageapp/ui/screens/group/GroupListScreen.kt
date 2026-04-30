package com.hasani.messageapp.ui.screens.group

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.offset
import com.hasani.messageapp.domain.model.Group
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.components.ExpandableSearchHeader
import com.hasani.messageapp.ui.components.UnreadBadge
import com.hasani.messageapp.ui.components.MessageStatusIcon
import com.hasani.messageapp.domain.model.MessageStatus
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.ui.viewmodel.GroupListViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group List Screen - With Selection Mode Support
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToGroupStories: (String, String) -> Unit = { _, _ -> }, // groupId, groupName
    onMyStoriesClick: () -> Unit = {},
    onNavigateToCreateTextStory: () -> Unit = {},
    searchQuery: String = "",
    viewModel: GroupListViewModel = hiltViewModel(),
    storyViewModel: com.hasani.messageapp.ui.viewmodel.StoryViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Use ViewModel state for selection
    val inSelectionMode = state.selectedGroupIds.isNotEmpty()

    // Stories State (using groupUiState for group stories)
    val storyUiState by storyViewModel.groupUiState.collectAsState()
    val selectedStoryUser by storyViewModel.selectedStoryUser.collectAsState()
    val currentUserProfile by storyViewModel.currentUser.collectAsState()
    
    // Story Media Picker
    var showAddStorySheet by remember { mutableStateOf(false) }

    // Filter groups for stories (Admin/Owner only)
    val adminGroups = remember(state.groups) {
        state.groups.filter { 
            it.myRole == com.hasani.messageapp.domain.model.MemberRole.ADMIN || 
            it.myRole == com.hasani.messageapp.domain.model.MemberRole.OWNER 
        }
    }
    
    // Group Selection Logic
    var showGroupSelectionSheet by remember { mutableStateOf(false) }
    var selectedGroupIdForStory by remember { mutableStateOf<String?>(null) }
    
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
    
    // Load Group Stories on Enter
    androidx.compose.runtime.LaunchedEffect(Unit) {
        storyViewModel.loadGroupStories()
    }

    val storyPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && selectedGroupIdForStory != null) {
            storyViewModel.uploadGroupStory(selectedGroupIdForStory!!, uri, "AUTO")
            selectedGroupIdForStory = null
        }
    }

    // Filter groups for list
    val filteredGroups = remember(state.groups, state.pinnedGroups, searchQuery) {
        val baseList = state.groups.filter { !it.isPinned && !it.isArchived }
        if (searchQuery.isEmpty()) baseList
        else baseList.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                GroupSelectionTopBar(
                    selectedCount = state.selectedGroupIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelected = { viewModel.requestDeleteSelection() },
                    onPinSelected = {
                        viewModel.pinSelectedGroups(true)
                        android.widget.Toast.makeText(context, "گروه‌ها سنجاق شدند", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onArchiveSelected = {
                        state.selectedGroupIds.forEach { groupId ->
                            viewModel.archiveGroup(groupId, true)
                        }
                        android.widget.Toast.makeText(context, "گروه‌ها به آرشیو منتقل شدند", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.clearSelection()
                    }
                )
            }

            // ... (Loading/Empty logic - Unchanged) we can skip replace of this chunk if unchanged, but for safety in large block
            if (state.isLoading && state.groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                                        userId = "group_add", 
                                        username = "", 
                                        displayName = "استوری گروه", 
                                        avatarUrl = null, 
                                        stories = emptyList(), 
                                        isCurrentUser = true 
                                    ),
                                    storyUsers = storyUsers,
                                    onStoryClick = { user ->
                                         if (user.isCurrentUser) {
                                             if (adminGroups.isNotEmpty()) {
                                                 showGroupSelectionSheet = true
                                             } else {
                                                 android.widget.Toast.makeText(context, "شما ادمین هیچ گروهی نیستید", android.widget.Toast.LENGTH_SHORT).show()
                                             }
                                         } else {
                                             storyViewModel.openStoryViewer(user)
                                         }
                                    },
                                    onAddStoryClick = { 
                                         if (adminGroups.isNotEmpty()) {
                                             showGroupSelectionSheet = true
                                         } else {
                                             android.widget.Toast.makeText(context, "شما ادمین هیچ گروهی نیستید", android.widget.Toast.LENGTH_SHORT).show()
                                         }
                                    },
                                    excludeCurrentUserFromList = false
                                )
                            }
                        }
                    }

                    // Empty State
                    if (state.groups.isEmpty()) {
                        item {
                             Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("گروهی وجود ندارد", style = MessageAppTypography.chatName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("یک گروه جدید بسازید", style = MessageAppTypography.chatPreview, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }
                    } else {
                    
                    // ... (Rest of lists)
                     if (state.archivedGroups.isNotEmpty() && !inSelectionMode) {
                        item {
                           // ... Archived logic
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
                                        Text("آرشیو شده (${state.archivedGroups.size})", style = MessageAppTypography.sectionTitle, color = extendedColors.accent)
                                    }
                                    Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (isExpanded) {
                                    state.archivedGroups.forEach { group ->
                                        GroupListItem(
                                            group = group,
                                            currentUserId = state.currentUserId,
                                            isSelected = false,
                                            inSelectionMode = false,
                                            onClick = { onGroupClick(group.id) },
                                            onLongClick = { viewModel.archiveGroup(group.id, false) },
                                            onAvatarClick = onNavigateToGroupDetail,
                                            onUnarchiveClick = { viewModel.archiveGroup(group.id, false) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.pinnedGroups.isNotEmpty()) {
                         item {
                            Text(stringResource(com.hasani.messageapp.R.string.pinned), style = MessageAppTypography.sectionTitle, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                         items(state.pinnedGroups, key = { it.id }) { group ->
                            GroupListItem(
                                group = group,
                                currentUserId = state.currentUserId,
                                isSelected = state.selectedGroupIds.contains(group.id),
                                inSelectionMode = inSelectionMode,
                                onClick = { if (inSelectionMode) viewModel.toggleSelection(group.id) else onGroupClick(group.id) },
                                onLongClick = { viewModel.toggleSelection(group.id) },
                                onAvatarClick = onNavigateToGroupDetail,
                                onPin = { viewModel.pinGroup(group.id, false) }
                            )
                        }
                    }
                    
                    if (state.pinnedGroups.isNotEmpty() && filteredGroups.isNotEmpty()) {
                        item { Text("همه گروه‌ها", style = MessageAppTypography.sectionTitle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) }
                    }
                    
                    items(filteredGroups, key = { it.id }) { group ->
                         GroupListItem(
                            group = group,
                            currentUserId = state.currentUserId,
                            isSelected = state.selectedGroupIds.contains(group.id),
                            inSelectionMode = inSelectionMode,
                            onClick = { if (inSelectionMode) viewModel.toggleSelection(group.id) else onGroupClick(group.id) },
                            onLongClick = { viewModel.toggleSelection(group.id) },
                            onAvatarClick = onNavigateToGroupDetail
                        )
                    }
                     item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
        
        // ... (Delete Dialog Unchanged)
        if (state.showDeleteConfirmation) {
              AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteSelection() },
                title = { Text("حذف گروه‌ها") },
                text = { Text("آیا از حذف ${state.selectedGroupIds.size} گروه انتخاب شده اطمینان دارید؟ این عمل قابل برگشت نیست.") },
                confirmButton = { TextButton(onClick = { viewModel.confirmDeleteSelection() }) { Text("حذف", color = MaterialTheme.colorScheme.error) } },
                dismissButton = { TextButton(onClick = { viewModel.cancelDeleteSelection() }) { Text("انصراف") } }
            )
        }

        // Add Story - Group Selection Bottom Sheet
        if (showGroupSelectionSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showGroupSelectionSheet = false },
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
                        items(adminGroups) { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGroupIdForStory = group.id
                                        showGroupSelectionSheet = false
                                        onNavigateToGroupStories(group.id, group.name)
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(
                                    imageUrl = group.avatarUrl,
                                    name = group.name,
                                    size = AvatarSize.SMALL
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = group.name,
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

@Composable
private fun GroupSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
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
        IconButton(onClick = onDeleteSelected) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupListItem(
    group: Group,
    currentUserId: String?,
    isSelected: Boolean,
    inSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAvatarClick: (String) -> Unit,
    onUnarchiveClick: (() -> Unit)? = null, // Optional unarchive action
    onPin: (() -> Unit)? = null
) {
    val timeFormatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
    }
    
    val extendedColors = MessageAppTheme.extendedColors
    
    // Main Content (no swipe)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MessageAppTheme.extendedColors.accent.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.background
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Selection checkbox or Avatar
            Box {
                AvatarImage(
                    imageUrl = group.avatarUrl,
                    name = group.name,
                    size = AvatarSize.MEDIUM,
                    modifier = Modifier.clickable { 
                        if (!inSelectionMode) onAvatarClick(group.id) 
                    }
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
                Text(
                    text = group.name,
                    style = MessageAppTypography.chatName,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Time
                group.lastMessage?.let { msg ->
                    Text(
                        text = try { timeFormatter.format(msg.createdAt) } catch(e: Exception) { "" },
                        style = MessageAppTypography.chatTime,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                group.lastMessage?.let { msg ->
                    val isFromMe = msg.senderId == currentUserId
                    
                    if (isFromMe) {
                         // Message Status (Check/Double Check)
                         MessageStatusIcon(
                             status = msg.status,
                             tint = MaterialTheme.colorScheme.onSurfaceVariant,
                             modifier = Modifier.padding(end = 4.dp).size(16.dp)
                         )
                    } else if (msg.senderAvatar != null) {
                        // Sender Avatar (Small) for RECEIVED messages
                         Box(modifier = Modifier.clickable { onAvatarClick(msg.senderId) }) {
                             AvatarImage(
                                imageUrl = msg.senderAvatar,
                                name = msg.senderName,
                                size = AvatarSize.SMALL // 32dp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    
                    // Sender Name : Content
                    val displayText = if (isFromMe) {
                        "شما: ${msg.content}"
                    } else {
                        "${msg.senderName}: ${msg.content}"
                    }
                    
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } ?: Text(
                    text = stringResource(com.hasani.messageapp.R.string.members_count, group.memberCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (group.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    UnreadBadge(count = group.unreadCount)
                }
            }
        }
    }
}
