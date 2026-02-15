package com.Kelasor.app.ui.screens.group

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.MemberRole
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.AvatarType
import com.Kelasor.app.ui.components.BubblePosition
import com.Kelasor.app.ui.components.ChatBubble
import com.Kelasor.app.ui.components.MediaType
import com.Kelasor.app.ui.components.MessageActionSheet
import com.Kelasor.app.ui.components.MessageInputBar
import com.Kelasor.app.ui.components.PinnedMessageBanner
import kotlinx.coroutines.launch
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.viewmodel.GroupConversationViewModel
import com.Kelasor.app.ui.viewmodel.GroupEvent
import com.Kelasor.app.ui.screens.chat.EditMessageDialog
import com.Kelasor.app.ui.screens.chat.MessageSelectionTopBar
import com.Kelasor.app.ui.components.VideoNoteBubble
import com.Kelasor.app.ui.components.CircularVideoRecorder
import com.Kelasor.app.data.video.VideoNoteRecordingState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Conversation Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroupConversationScreen(
    groupId: String,
    initialMessageId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToGroupSettings: (String) -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToForward: (messageIds: String, sourceType: String, sourceId: String) -> Unit = { _, _, _ -> },
    viewModel: GroupConversationViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }
    var messageInput by remember { mutableStateOf("") }
    // Media preview state
    var mediaPreviewUrl by remember { mutableStateOf<String?>(null) }
    var mediaPreviewType by remember { mutableStateOf(MediaType.UNKNOWN) }
    
    // Auto-scroll to initial message
    val initialScrollDone = remember { mutableStateOf(false) }
    LaunchedEffect(state.messages, initialMessageId) {
        if (!initialScrollDone.value && initialMessageId != null && state.messages.isNotEmpty()) {
            val index = state.messages.indexOfFirst { it.id == initialMessageId }
            if (index != -1) {
                listState.scrollToItem(index)
                initialScrollDone.value = true
            }
        }
    }
    
    // Message Highlighting
    var highlightedMessageId by remember { mutableStateOf(initialMessageId) }
    LaunchedEffect(highlightedMessageId) {
        if (highlightedMessageId != null) {
            kotlinx.coroutines.delay(2000)
            highlightedMessageId = null
        }
    }

    // Attachment menu state
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    // Media editing state
    var pendingMediaUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var pendingMediaIsVideo by remember { mutableStateOf(false) }
    // Video note recording state
    var showVideoNoteRecorder by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadAndSendFile(groupId, it, context) }
    }
    
    // Gallery picker launcher → opens media editor
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            pendingMediaIsVideo = mimeType?.startsWith("video/") == true
            pendingMediaUri = it
        }
    }
    
    // Audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadAndSendAudio(groupId, it, context) }
    }
    var messageForActions by remember { mutableStateOf<Message?>(null) }
    var showEditMessageDialog by remember { mutableStateOf(false) }
    var messageToEdit by remember { mutableStateOf<Message?>(null) }
    var replyingToMessage by remember { mutableStateOf<Message?>(null) }
    
    // Load group on first composition
    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    DisposableEffect(groupId) {
        viewModel.setActiveChat(groupId)
        onDispose {
            viewModel.clearActiveChat()
        }
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GroupEvent.MessageSent -> {
                    messageInput = ""
                }
                is GroupEvent.GroupDeleted -> {
                    onNavigateBack()
                }
                is GroupEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    // Scroll to bottom when new messages arrive (check for initial load too)
    LaunchedEffect(state.messages.size, state.isLoading) {
        if (state.messages.isNotEmpty()) {
            // Scroll to the last message (newest at bottom, index 0 in reverse layout)
            listState.animateScrollToItem(0)
        }
    }
    
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault())
    }

    val isInSelectionMode = state.selectedMessageIds.isNotEmpty()
    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            if (isInSelectionMode) {
                MessageSelectionTopBar(
                    selectedCount = state.selectedMessageIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelected = { deleteForEveryone ->
                        state.selectedMessageIds.forEach { msgId ->
                            viewModel.deleteMessage(msgId, deleteForEveryone)
                        }
                        viewModel.clearSelection()
                        Toast.makeText(
                            context,
                            if (deleteForEveryone) "پیام‌ها برای همه حذف شد" else "پیام‌ها حذف شد",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onEditSelected = {
                        val messageId = state.selectedMessageIds.firstOrNull()
                        val message = state.messages.find { it.id == messageId }
                        if (message != null) {
                            messageToEdit = message
                            showEditMessageDialog = true
                        }
                        viewModel.clearSelection()
                    },
                    onCopySelected = {
                        val selectedText = state.messages.filter { it.id in state.selectedMessageIds }
                            .joinToString("\n") { it.content }
                        clipboardManager.setText(AnnotatedString(selectedText))
                        viewModel.clearSelection()
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onNavigateToGroupDetail(groupId) }
                        ) {
                            AvatarImage(
                                imageUrl = state.group?.avatarUrl,
                                name = state.group?.name ?: "",
                                size = AvatarSize.SMALL,
                                avatarType = AvatarType.GROUP
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.group?.name ?: "گروه",
                                    style = MessageAppTypography.chatName,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${state.group?.memberCount ?: 0} عضو",
                                    style = MessageAppTypography.chatTime,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        val isAdminOrOwner = state.group?.myRole in listOf(MemberRole.OWNER, MemberRole.ADMIN)
                        // Mute/Unmute bell icon - All users
                        IconButton(
                            onClick = {
                                val currentMute = state.group?.isMuted ?: false
                                viewModel.toggleMute()
                                Toast.makeText(
                                    context,
                                    if (currentMute) "صدای گروه فعال شد" else "گروه بی‌صدا شد", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (state.group?.isMuted == true) 
                                    Icons.Default.NotificationsOff 
                                else 
                                    Icons.Default.Notifications,
                                contentDescription = "Mute",
                                tint = if (state.group?.isMuted == true)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Settings icon - Admin/Owner only (goes to settings for editing)
                        if (isAdminOrOwner) {
                            IconButton(onClick = { onNavigateToGroupSettings(groupId) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        // Info icon - All users (navigates to group info where leave/delete is available)
                        IconButton(onClick = { onNavigateToGroupDetail(groupId) }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            extendedColors.gradientStart.copy(alpha = 0.02f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .imePadding()
        ) {
            // Pinned message banner
            val pinnedMessage = remember(state.messages) {
                state.messages.filter { it.isPinned }.maxByOrNull { it.pinnedAt ?: java.time.Instant.EPOCH }
            }
            if (pinnedMessage != null) {
                PinnedMessageBanner(
                    content = pinnedMessage.content,
                    senderName = pinnedMessage.senderName,
                    messageType = pinnedMessage.type,
                    onClick = {
                        val index = state.messages.indexOfFirst { it.id == pinnedMessage.id }
                        if (index != -1) {
                            scope.launch {
                                listState.animateScrollToItem(index)
                                highlightedMessageId = pinnedMessage.id
                            }
                        }
                    },
                    onUnpin = {
                        viewModel.pinMessage(pinnedMessage.id, false)
                    }
                )
            }
            // Messages List
            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                } else if (state.messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Text(
                            text = "هنوز پیامی وجود ندارد",
                            style = MessageAppTypography.chatName,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {

// Messages list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    reverseLayout = true
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    items(
                        items = state.messages,
                        key = { it.id }
                    ) { message ->
                        val isFromMe = message.senderId == state.currentUserId
                        val isSelected = state.selectedMessageIds.contains(message.id)
                        
                        // 🎬 iMessage-style animation wrapper
                        com.Kelasor.app.ui.components.AnimatedMessageBubble(
                            isMyMessage = isFromMe,
                            messageId = message.id
                        ) {
                            ChatBubbleWrapper(
                                message = message,
                                isFromMe = isFromMe,
                                isSelected = isSelected,
                                isHighlighted = message.id == highlightedMessageId,
                                isInSelectionMode = isInSelectionMode,
                                viewModel = viewModel,
                                onNavigateToUserProfile = onNavigateToUserProfile,
                                onClick = {
                                    if (isInSelectionMode) {
                                        viewModel.toggleMessageSelection(message.id)
                                    } else {
                                        messageForActions = message
                                    }
                                },
                                onLongClick = { viewModel.toggleMessageSelection(message.id) },
                                onPreviewMedia = { url, type ->
                                    mediaPreviewUrl = url
                                    mediaPreviewType = type
                                },
                                timeFormatter = timeFormatter
                            )
                        }
                    }
                }
                }
            }
            
            // Message Actions Overlay (Telegram-style single click)
            val selectedMsgForOverlay = messageForActions
            com.Kelasor.app.ui.components.MessageActionsOverlay(
                visible = selectedMsgForOverlay != null,
                message = selectedMsgForOverlay,
                messageContent = {
                    if (selectedMsgForOverlay != null) {
                        val isFromMe = selectedMsgForOverlay.senderId == state.currentUserId
                        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                        ChatBubble(
                            message = selectedMsgForOverlay.content,
                            time = try { timeFormatter.format(selectedMsgForOverlay.createdAt) } catch (e: Exception) { "" },
                            isMyMessage = isFromMe,
                            status = selectedMsgForOverlay.status,
                            position = BubblePosition.SINGLE,
                            senderName = if (!isFromMe) selectedMsgForOverlay.senderName else null,
                            reactions = selectedMsgForOverlay.reactions,
                            myReaction = selectedMsgForOverlay.myReaction,
                            replyToMessage = selectedMsgForOverlay.replyToMessage,
                            isPinned = selectedMsgForOverlay.isPinned,
                            forwardedFrom = selectedMsgForOverlay.forwardedFrom
                        )
                    }
                },
                isOwner = selectedMsgForOverlay?.senderId == state.currentUserId,
                reactionCount = selectedMsgForOverlay?.reactions?.values?.sum() ?: 0,
                onDismiss = { messageForActions = null },
                onReactionClick = { emoji ->
                    selectedMsgForOverlay?.let { viewModel.reactToMessage(it.id, emoji) }
                },
                onReplyClick = if (state.group?.allowMembersToSendMessages == true || 
                    state.group?.myRole in listOf(MemberRole.OWNER, MemberRole.ADMIN)) {{
                    selectedMsgForOverlay?.let { replyingToMessage = it }
                }} else null,
                onCopyClick = {
                    selectedMsgForOverlay?.let {
                        clipboardManager.setText(AnnotatedString(it.content))
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    }
                },
                onEditClick = if (selectedMsgForOverlay?.senderId == state.currentUserId) {{
                    selectedMsgForOverlay?.let {
                        messageToEdit = it
                        showEditMessageDialog = true
                    }
                }} else null,
                onDeleteClick = if (selectedMsgForOverlay?.senderId == state.currentUserId || 
                    state.group?.myRole in listOf(MemberRole.OWNER, MemberRole.ADMIN)) {{ deleteForEveryone ->
                    selectedMsgForOverlay?.let { viewModel.deleteMessage(it.id, deleteForEveryone) }
                }} else null,
                onSelectClick = {
                    selectedMsgForOverlay?.let { viewModel.toggleMessageSelection(it.id) }
                },
                onPinClick = {
                    selectedMsgForOverlay?.let {
                        viewModel.pinMessage(it.id, !it.isPinned)
                        android.widget.Toast.makeText(
                            context,
                            if (it.isPinned) "پین برداشته شد" else "پیام سنجاق شد",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onForwardClick = {
                    selectedMsgForOverlay?.let {
                        onNavigateToForward(it.id, "GROUP", groupId)
                    }
                },
                onLockContentClick = {
                    Toast.makeText(context, "به زودی!", Toast.LENGTH_SHORT).show()
                }
            )
            
            // Edit Message Dialog
            if (showEditMessageDialog && messageToEdit != null) {
                // For media messages, extract caption (strip emoji prefix + file name)
                val editableContent = remember(messageToEdit) {
                    val msg = messageToEdit!!
                    if (msg.type in listOf(MessageType.IMAGE, MessageType.VIDEO)) {
                        val stripped = msg.content
                            .removePrefix("🖼️ ")
                            .removePrefix("🎬 ")
                            .trim()
                        if (stripped.startsWith("media_") || stripped.startsWith("edited_")) "" else stripped
                    } else {
                        msg.content
                    }
                }
                EditMessageDialog(
                    originalMessage = editableContent,
                    onConfirm = { newContent ->
                        viewModel.editMessage(messageToEdit!!.id, newContent)
                        Toast.makeText(context, "پیام ویرایش شد", Toast.LENGTH_SHORT).show()
                        showEditMessageDialog = false
                        messageToEdit = null
                    },
                    onDismiss = {
                        showEditMessageDialog = false
                        messageToEdit = null
                    }
                )
            }

            // Input Bar
            val canSendMessages = state.group?.allowMembersToSendMessages == true || 
                state.group?.myRole in listOf(MemberRole.OWNER, MemberRole.ADMIN)
            
            if (canSendMessages) {
                // Reply Preview Bar
                if (replyingToMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(40.dp)
                                .background(extendedColors.accent, shape = RoundedCornerShape(2.dp))
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = replyingToMessage!!.senderName,
                                style = MaterialTheme.typography.labelSmall,
                                color = extendedColors.accent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = replyingToMessage!!.content,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { replyingToMessage = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Upload progress indicator
                if (state.isUploading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (state.uploadTotalBytes > 0) {
                                    val current = (state.uploadProgress * state.uploadTotalBytes).toLong()
                                    "${formatFileSize(current)} / ${formatFileSize(state.uploadTotalBytes)}"
                                } else "در حال آپلود...",
                                style = MessageAppTypography.chatTime,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.uploadProgress > 0f) {
                                Text(
                                    text = "${(state.uploadProgress * 100).toInt()}%",
                                    style = MessageAppTypography.chatTime,
                                    color = extendedColors.accent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        if (state.uploadProgress > 0f) {
                            LinearProgressIndicator(
                                progress = { state.uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = extendedColors.accent,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = extendedColors.accent,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }

                // Input bar or Multi-Select Action Panel
                if (isInSelectionMode) {
                    com.Kelasor.app.ui.components.MultiSelectActionPanel(
                        selectedCount = state.selectedMessageIds.size,
                        onForwardClick = {
                        if (state.selectedMessageIds.isNotEmpty()) {
                            val ids = state.selectedMessageIds.joinToString(",")
                            onNavigateToForward(ids, "GROUP", groupId)
                        }
                        viewModel.clearSelection()
                    },
                        onReplyClick = if (state.selectedMessageIds.size == 1) {{
                            val messageId = state.selectedMessageIds.first()
                            val message = state.messages.find { it.id == messageId }
                            if (message != null) {
                                replyingToMessage = message
                            }
                            viewModel.clearSelection()
                        }} else null,
                        onCopyClick = {
                            val selectedText = state.messages.filter { it.id in state.selectedMessageIds }
                                .joinToString("\n") { it.content }
                            clipboardManager.setText(AnnotatedString(selectedText))
                            viewModel.clearSelection()
                            Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = {
                            state.selectedMessageIds.forEach { msgId ->
                                viewModel.deleteMessage(msgId, true)
                            }
                            viewModel.clearSelection()
                            Toast.makeText(context, "پیام‌ها حذف شد", Toast.LENGTH_SHORT).show()
                        },
                        onPinClick = if (state.selectedMessageIds.size == 1) {{
                            val messageId = state.selectedMessageIds.first()
                            val message = state.messages.find { it.id == messageId }
                            if (message != null) {
                                viewModel.pinMessage(messageId, !message.isPinned)
                                Toast.makeText(
                                    context,
                                    if (message.isPinned) "پین برداشته شد" else "پیام سنجاق شد",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            viewModel.clearSelection()
                        }} else null
                    )
                } else {
                    MessageInputBar(
                        text = messageInput,
                        onTextChange = { messageInput = it },
                        onSendClick = {
                            if (messageInput.isNotBlank()) {
                                viewModel.sendMessage(messageInput, replyingToMessage?.id)
                                messageInput = ""
                                replyingToMessage = null
                            }
                        },
                        onAttachClick = { showAttachmentMenu = true },
                        voiceRecorderManager = viewModel.voiceRecorderManager,
                        onVoiceRecorded = { file, duration, amplitudes ->
                            viewModel.sendVoiceMessage(file, duration, amplitudes)
                        },
                        onVideoNoteClick = {
                            showVideoNoteRecorder = true
                        }
                    )
                }
            } else {
                 Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "فقط مدیران می‌توانند پیام ارسال کنند",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Overlay Menus
        com.Kelasor.app.ui.components.AttachmentMenu(
            visible = showAttachmentMenu,
            onDismiss = { showAttachmentMenu = false },
            onFileClick = { filePickerLauncher.launch("*/*") },
            onGalleryClick = { galleryPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            ) },
            onAudioClick = { audioPickerLauncher.launch("audio/*") },
            onLocationClick = { 
                showAttachmentMenu = false
                showLocationPicker = true
            },
            onPollClick = {
                showAttachmentMenu = false
                showPollCreator = true
            }
        )
        
        if (showPollCreator) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                com.Kelasor.app.ui.screens.poll.CreatePollScreen(
                    onBack = { showPollCreator = false },
                    onSendPoll = { question, options, isMultiple, isAnonymous ->
                        viewModel.createPoll(question, options, isMultiple, isAnonymous)
                        showPollCreator = false
                    }
                )
            }
        }
        
        com.Kelasor.app.ui.components.LocationPickerDialog(
            visible = showLocationPicker,
            locationManager = viewModel.locationManager,
            onDismiss = { showLocationPicker = false },
            onSendLocation = { lat, lng ->
                viewModel.sendLocationMessage(lat, lng)
            }
        )
        
        mediaPreviewUrl?.let { url ->
            com.Kelasor.app.ui.components.MediaPreviewDialog(
                mediaUrl = url,
                mediaType = mediaPreviewType,
                onDismiss = { 
                    mediaPreviewUrl = null
                }
            )
        }
    }

    // ── Media Editor Overlay (outside Scaffold, covers full screen) ──
    pendingMediaUri?.let { uri ->
        com.Kelasor.app.ui.components.MediaEditScreen(
            mediaUri = uri,
            isVideo = pendingMediaIsVideo,
            onSend = { editedUri, captionText ->
                viewModel.uploadAndSendMedia(
                    groupId, editedUri, context,
                    isVideo = pendingMediaIsVideo,
                    caption = captionText.ifBlank { null }
                )
                pendingMediaUri = null
            },
            onDismiss = { pendingMediaUri = null }
        )
    }

    // Circular Video Note Recorder Overlay
    if (showVideoNoteRecorder) {
        CircularVideoRecorder(
            videoNoteRecorderManager = viewModel.videoNoteRecorderManager,
            onRecordComplete = {
                showVideoNoteRecorder = false
                val info = viewModel.videoNoteRecorderManager.recordingInfo.value
                if (info.state == VideoNoteRecordingState.COMPLETED && info.filePath != null) {
                    viewModel.sendVideoNote(
                        java.io.File(info.filePath!!),
                        info.durationMs
                    )
                    viewModel.videoNoteRecorderManager.reset()
                }
            },
            onCancel = {
                showVideoNoteRecorder = false
                viewModel.videoNoteRecorderManager.reset()
            }
        )
    }
} // end Box
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubbleWrapper(
    message: Message,
    isFromMe: Boolean,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isInSelectionMode: Boolean = false,
    viewModel: GroupConversationViewModel,
    onNavigateToUserProfile: (String) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPreviewMedia: (String, com.Kelasor.app.ui.components.MediaType) -> Unit,
    timeFormatter: DateTimeFormatter
) {
    val extendedColors = MessageAppTheme.extendedColors
    val time = try { timeFormatter.format(message.createdAt) } catch(e: Exception) { "" }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .background(
                when {
                    isSelected -> extendedColors.accent.copy(alpha = 0.15f)
                    isHighlighted -> extendedColors.accent.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
        if (!isFromMe) {
            Box(modifier = Modifier.clickable { onNavigateToUserProfile(message.senderId) }) {
                AvatarImage(
                    imageUrl = message.senderAvatar,
                    name = message.senderName,
                    size = AvatarSize.SMALL
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box {
            when {
                message.poll != null -> {
                    com.Kelasor.app.ui.components.PollBubble(
                        poll = message.poll!!,
                        onVote = { pollId, optionIds ->
                            viewModel.votePoll(pollId, optionIds)
                        },
                        isFromMe = isFromMe
                    )
                }

                message.type == MessageType.VOICE && message.mediaUrl != null -> {
                    val durationSeconds = try {
                        val regex = """\((\d+)s\)""".toRegex()
                        regex.find(message.content)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    } catch (e: Exception) { 0L }

                    com.Kelasor.app.ui.components.VoiceMessageBubble(
                        mediaUrl = message.mediaUrl!!,
                        durationMs = durationSeconds * 1000L,
                        isMyMessage = isFromMe,
                        audioPlayerManager = viewModel.audioPlayerManager,
                        amplitudes = message.amplitudes,
                        time = time,
                        status = message.status
                    )
                }

                message.type == MessageType.IMAGE && message.mediaUrl != null -> {
                    val imageCaption = message.content
                        .removePrefix("🖼️ ")
                        .trim()
                        .let { if (it.startsWith("media_") || it.startsWith("edited_") || it.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|mp4|mov|avi|mkv|bmp|svg|heic|heif|3gp|webm)$", RegexOption.IGNORE_CASE))) null else it }
                    com.Kelasor.app.ui.components.ImageMessageBubble(
                        mediaUrl = message.mediaUrl!!,
                        isVideo = false,
                        isMyMessage = isFromMe,
                        time = time,
                        onPreviewClick = { url, type -> onPreviewMedia(url, type) },
                        caption = imageCaption,
                        status = message.status
                    )
                }

                message.type == MessageType.VIDEO && message.mediaUrl != null -> {
                    val videoCaption = message.content
                        .removePrefix("🎬 ")
                        .trim()
                        .let { if (it.startsWith("media_") || it.startsWith("edited_") || it.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|mp4|mov|avi|mkv|bmp|svg|heic|heif|3gp|webm)$", RegexOption.IGNORE_CASE))) null else it }
                    com.Kelasor.app.ui.components.ImageMessageBubble(
                        mediaUrl = message.mediaUrl!!,
                        isVideo = true,
                        isMyMessage = isFromMe,
                        time = time,
                        onPreviewClick = { url, type -> onPreviewMedia(url, type) },
                        caption = videoCaption,
                        status = message.status
                    )
                }

                // Video note (circular video)
                message.type == MessageType.VIDEO_NOTE && message.mediaUrl != null -> {
                    val durationText = message.content
                        .substringAfter("(", "")
                        .substringBefore("s)", "")
                        .let { if (it.isNotBlank()) "${it}s" else null }
                    VideoNoteBubble(
                        mediaUrl = message.mediaUrl!!,
                        isMyMessage = isFromMe,
                        time = time,
                        durationText = durationText,
                        status = message.status,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                message.type == MessageType.AUDIO && message.mediaUrl != null -> {
                    val fileName = message.content.removePrefix("🎵 ").trim()
                    com.Kelasor.app.ui.components.AudioFileBubble(
                        mediaUrl = message.mediaUrl!!,
                        fileName = fileName,
                        durationMs = 0L,
                        isMyMessage = isFromMe,
                        audioPlayerManager = viewModel.audioPlayerManager,
                        time = time,
                        status = message.status
                    )
                }

                message.type == MessageType.LOCATION && message.mediaUrl != null -> {
                    val parts = message.mediaUrl!!.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull() ?: 0.0
                        val lng = parts[1].toDoubleOrNull() ?: 0.0
                        com.Kelasor.app.ui.components.LocationMessageBubble(
                            latitude = lat,
                            longitude = lng,
                            isMyMessage = isFromMe,
                            time = time,
                            status = message.status
                        )
                    }
                }

                message.type == MessageType.FILE && message.mediaUrl != null -> {
                    val fileName = message.content
                        .removePrefix("📎 ")
                        .removePrefix("📄 ")
                        .trim()
                        .ifEmpty { "file_${message.id}" }
                    com.Kelasor.app.ui.components.FileMessageBubble(
                        mediaUrl = message.mediaUrl!!,
                        fileName = fileName,
                        isMyMessage = isFromMe,
                        time = time,
                        status = message.status
                    )
                }

                else -> {
                    ChatBubble(
                        message = message.content,
                        time = time,
                        isMyMessage = isFromMe,
                        status = message.status,
                        position = BubblePosition.SINGLE,
                        senderName = if (!isFromMe) message.senderName else null,
                        replyToMessage = message.replyToMessage,
                        reactions = message.reactions,
                        myReaction = message.myReaction,
                        onSenderClick = { onNavigateToUserProfile(message.senderId) },
                        isEdited = message.isEdited,
                        isPinned = message.isPinned,
                        forwardedFrom = message.forwardedFrom
                    )
                }
            }
        }
        }
        }
    }
}

@Composable
private fun GroupMessageItem(
    message: Message,
    currentUserId: String?,
    timeFormatter: DateTimeFormatter,
    onAvatarClick: () -> Unit = {}
) {
    val isFromMe = message.senderId == currentUserId
    
    // Wrapper for Row layout (Avatar + Bubble)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe) {
            Box(modifier = Modifier.clickable { onAvatarClick() }) {
                AvatarImage(
                    imageUrl = message.senderAvatar,
                    name = message.senderName,
                    size = AvatarSize.SMALL
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        ChatBubble(
            message = message.content,
            time = try { timeFormatter.format(message.createdAt) } catch(e:Exception){ "" },
            isMyMessage = isFromMe,
            status = message.status,
            position = BubblePosition.SINGLE,
            senderName = if(!isFromMe) message.senderName else null
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}
