package com.Kelasor.app.ui.screens.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.BubblePosition
import com.Kelasor.app.ui.components.ChatBubble
import com.Kelasor.app.ui.components.DateSeparator
import com.Kelasor.app.ui.components.GlowingIconButton
import com.Kelasor.app.ui.components.TypingIndicator
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.ConversationViewModel
import com.Kelasor.app.ui.screens.chat.MessageSelectionTopBar
import com.Kelasor.app.ui.components.MessageInputBar
import com.Kelasor.app.ui.components.VoiceMessageBubble
import com.Kelasor.app.ui.components.MessageActionSheet
import com.Kelasor.app.ui.components.AttachmentMenu
import com.Kelasor.app.ui.components.MediaPreviewDialog
import com.Kelasor.app.ui.components.ImageMessageBubble
import com.Kelasor.app.ui.components.AudioFileBubble
import com.Kelasor.app.ui.components.FileMessageBubble
import com.Kelasor.app.ui.components.LocationPickerDialog
import com.Kelasor.app.ui.components.AnimatedMessageBubble
import com.Kelasor.app.ui.components.LocationMessageBubble
import com.Kelasor.app.ui.components.MediaType
import com.Kelasor.app.ui.components.detectMediaType
import com.Kelasor.app.ui.components.PollBubble
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Search
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Conversation Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var isMenuExpanded by remember { mutableStateOf(false) }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    var showEditMessageDialog by remember { mutableStateOf(false) }
    var messageToEdit by remember { mutableStateOf<com.Kelasor.app.domain.model.Message?>(null) }
    var messageForActions by remember { mutableStateOf<com.Kelasor.app.domain.model.Message?>(null) }
    
    // Attachment menu state
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    
    // Search State
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // 0: All, 1: Media, 2: Links, 3: Files
    var searchFilterIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // Media preview state
    var mediaPreviewUrl by remember { mutableStateOf<String?>(null) }
    var mediaPreviewType by remember { mutableStateOf(MediaType.UNKNOWN) }

    val listState = rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    // Filtered messages logic
    val filteredMessages = remember(state.messages, searchQuery, isSearchActive, searchFilterIndex) {
        if (!isSearchActive || (searchQuery.isEmpty() && searchFilterIndex == 0)) {
            state.messages
        } else {
            state.messages.filter { message ->
                val matchesQuery = if (searchQuery.isNotEmpty()) {
                    message.content.contains(searchQuery, ignoreCase = true) || 
                    (message.senderName?.contains(searchQuery, ignoreCase = true) == true)
                } else true
                
                val matchesType = when (searchFilterIndex) {
                    0 -> true // All
                    1 -> message.type == MessageType.IMAGE || message.type == MessageType.VIDEO // Media
                    2 -> android.util.Patterns.WEB_URL.matcher(message.content).find() // Links
                    3 -> message.type == MessageType.FILE || message.type == MessageType.AUDIO // Files
                    else -> true
                }
                
                matchesQuery && matchesType
            }
        }
    }

    // File picker launcher (all files)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadAndSendFile(chatId, it, context)
        }
    }
    
    // Gallery picker launcher (images and videos)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            // Check if it's a video based on MIME type
            val mimeType = context.contentResolver.getType(it)
            val isVideo = mimeType?.startsWith("video/") == true
            viewModel.uploadAndSendMedia(chatId, it, context, isVideo = isVideo)
        }
    }
    
    // Audio picker launcher (audio files only)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadAndSendAudio(chatId, it, context)
        }
    }

    // Observe error events and show Toast
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.ConversationEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> { /* Handle other events */ }
            }
        }
    }

    // Load chat data
    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }
    // Clean up when leaving the screen
    // Clean up when leaving the screen
    androidx.compose.runtime.DisposableEffect(chatId) {
        viewModel.setActiveChat(chatId)
        onDispose {
            viewModel.clearActiveChat()
            viewModel.onChatClosed()
        }
    }
    // Get chat info from state
    val chat = state.chat
    val chatName = chat?.title ?: "چت"
    // FIX: Filter out current user to get OTHER participant
    val otherParticipant = chat?.participants?.find { it.id != state.currentUserId }
    // Use privacy-sanitized display fields
    val isOnline = otherParticipant?.displayOnlineStatus ?: false
    // For private chats, ALWAYS use participant's privacy-sanitized avatar (ignore cached chat?.avatarUrl)
    val avatarUrl = if (chat?.type == com.Kelasor.app.domain.model.ChatType.PRIVATE) {
        otherParticipant?.displayAvatarUrl
    } else {
        chat?.avatarUrl
    }
    val messages = state.messages
    val isTyping = state.isOtherUserTyping

    // Delete Chat Confirmation Dialog
    if (showDeleteChatDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteChatDialog = false },
            title = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_title), style = MessageAppTypography.chatName) },
            text = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_desc)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteChat()
                        showDeleteChatDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteChatDialog = false }) {
                    Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.cancel))
                }
            }
        )
    }
    
    // Edit Message Dialog
    if (showEditMessageDialog && messageToEdit != null) {
        EditMessageDialog(
            originalMessage = messageToEdit!!.content,
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
    
    // Message Actions Overlay (Telegram-style single click)
    val selectedMsgForOverlay = messageForActions
    com.Kelasor.app.ui.components.MessageActionsOverlay(
        visible = selectedMsgForOverlay != null,
        message = selectedMsgForOverlay,
        messageContent = {
            if (selectedMsgForOverlay != null) {
                val isFromMe = selectedMsgForOverlay.senderId == state.currentUserId
                ChatBubble(
                    message = selectedMsgForOverlay.content,
                    time = selectedMsgForOverlay.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                    isMyMessage = isFromMe,
                    status = selectedMsgForOverlay.status,
                    position = BubblePosition.SINGLE,
                    reactions = selectedMsgForOverlay.reactions,
                    myReaction = selectedMsgForOverlay.myReaction,
                    replyToMessage = selectedMsgForOverlay.replyToMessage
                )
            }
        },
        isOwner = selectedMsgForOverlay?.senderId == state.currentUserId,
        reactionCount = selectedMsgForOverlay?.reactions?.values?.sum() ?: 0,
        onDismiss = { messageForActions = null },
        onReactionClick = { emoji ->
            selectedMsgForOverlay?.let { viewModel.reactToMessage(it.id, emoji) }
        },
        onReplyClick = {
            selectedMsgForOverlay?.let { viewModel.setReplyMessage(it) }
        },
        onCopyClick = {
            selectedMsgForOverlay?.let {
                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(it.content))
                Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
            }
        },
        onEditClick = if (selectedMsgForOverlay?.senderId == state.currentUserId) {{
            selectedMsgForOverlay?.let {
                messageToEdit = it
                showEditMessageDialog = true
            }
        }} else null,
        onDeleteClick = if (selectedMsgForOverlay?.senderId == state.currentUserId) {{ deleteForEveryone ->
            selectedMsgForOverlay?.let { viewModel.deleteMessage(it.id) }
        }} else null,
        onSelectClick = {
            selectedMsgForOverlay?.let { viewModel.toggleMessageSelection(it.id) }
        },
        onLockContentClick = {
            Toast.makeText(context, "به زودی!", Toast.LENGTH_SHORT).show()
        }
    )

    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            // Top App Bar or Selection Bar
            if (state.selectedMessageIds.isNotEmpty()) {
                MessageSelectionTopBar(
                    selectedCount = state.selectedMessageIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelected = { deleteForEveryone ->
                        viewModel.deleteSelectedMessages(deleteForEveryone)
                        val toastMessage = if (deleteForEveryone) "پیام برای همه حذف شد" else "پیام حذف شد"
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    },
                    onEditSelected = {
                        // Only works for single selection
                        val messageId = state.selectedMessageIds.firstOrNull()
                        val message = messages.find { it.id == messageId }
                        if (message != null) {
                            messageToEdit = message
                            showEditMessageDialog = true
                        }
                        viewModel.clearSelection()
                    },
                    onCopySelected = { 
                        val selectedText = messages.filter { it.id in state.selectedMessageIds }
                            .joinToString("\n") { it.content }
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selectedText))
                        viewModel.clearSelection()
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    }
                )
            } else if (isSearchActive) {
                 Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
                    androidx.compose.material3.TopAppBar(
                        title = {
                            androidx.compose.material3.TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("جستجو...", style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = androidx.compose.material3.TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false 
                                searchQuery = ""
                                searchFilterIndex = 0
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        }
                    )
                    // Filter Chips
                     androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("همه", "رسانه", "لینک", "فایل")
                        items(filters.size) { index ->
                            androidx.compose.material3.FilterChip(
                                selected = searchFilterIndex == index,
                                onClick = { searchFilterIndex = index },
                                label = { Text(filters[index], fontFamily = VazirFontFamily) },
                                trailingIcon = if (searchFilterIndex == index) {
                                    { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { 
                                if (chat?.type == com.Kelasor.app.domain.model.ChatType.PRIVATE) {
                                    otherParticipant?.id?.let { onNavigateToProfile(it) }
                                }
                            }
                        ) {
                            AvatarImage(
                                imageUrl = avatarUrl,
                                name = chatName,
                                size = AvatarSize.SMALL,
                                isOnline = isOnline
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = chatName,
                                    style = MessageAppTypography.chatName,
                                    fontWeight = FontWeight.Bold
                                )
                                AnimatedVisibility(
                                    visible = isTyping,
                                    enter = fadeIn() + slideInVertically { -it },
                                    exit = fadeOut() + slideOutVertically { -it }
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.typing),
                                        style = MessageAppTypography.chatTime,
                                        color = extendedColors.accent
                                    )
                                }
                                AnimatedVisibility(
                                    visible = !isTyping && isOnline,
                                    enter = fadeIn() + slideInVertically { -it },
                                    exit = fadeOut() + slideOutVertically { -it }
                                ) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.online),
                                        style = MessageAppTypography.chatTime,
                                        color = extendedColors.onlineIndicator
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back)
                            )
                        }
                    },
                    actions = {
                        // Search Action
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        
                        // Mute/Unmute bell icon
                        IconButton(
                            onClick = { 
                                val currentMute = chat?.isMuted ?: false
                                viewModel.toggleMute()
                                Toast.makeText(
                                    context, 
                                    if (currentMute) "صدای چت فعال شد" else "چت بی‌صدا شد", 
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (chat?.isMuted == true) 
                                    Icons.Default.NotificationsOff 
                                else 
                                    Icons.Default.Notifications,
                                contentDescription = "Mute",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Box {
                            IconButton(onClick = { isMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.more)
                                )
                            }
                            androidx.compose.material3.DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                // View profile option
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.view_profile)) },
                                    onClick = {
                                        isMenuExpanded = false
                                        if (chat?.type == com.Kelasor.app.domain.model.ChatType.PRIVATE) {
                                            otherParticipant?.id?.let { onNavigateToProfile(it) }
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null
                                        )
                                    }
                                )
                                // Delete chat option
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete_chat_title), color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        isMenuExpanded = false
                                        showDeleteChatDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
            // Loading indicator
            Box(modifier = Modifier.weight(1f)) {
            // Messages list - Always present
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    // Typing indicator
                    if (isTyping) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 48.dp, bottom = 8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                TypingIndicator()
                            }
                        }
                    }
                    // Messages
                    items(
                        items = filteredMessages,
                        key = { it.id },
                        contentType = { "message" }
                    ) { message ->
                        val isFromMe = message.senderId == state.currentUserId
                        val isSelected = state.selectedMessageIds.contains(message.id)
                        val inSelectionMode = state.selectedMessageIds.isNotEmpty()
                        
                        // DEBUG: Log message details to find why media shows as text
                        if (message.type != MessageType.TEXT) {
                            android.util.Log.d("ConversationScreen", "Rendering msg ${message.id}: Type=${message.type}, Url=${message.mediaUrl}")
                        }
                        
                        // 🎬 iMessage-style animation wrapper
                        AnimatedMessageBubble(
                            isMyMessage = isFromMe,
                            messageId = message.id
                        ) {
                            // Wrap bubble with selection logic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { 
                                            if (inSelectionMode) {
                                                viewModel.toggleMessageSelection(message.id)
                                            } else {
                                                // Single click opens Telegram-style actions overlay
                                                messageForActions = message
                                            }
                                        },
                                        onLongClick = { 
                                            // Long click enters selection mode
                                            viewModel.toggleMessageSelection(message.id)
                                        }
                                    )
                                    .background(
                                        if (isSelected) extendedColors.accent.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                            // Render based on message type
                            when {
                                // Poll message
                                message.poll != null -> {
                                    PollBubble(
                                        poll = message.poll!!,
                                        onVote = { pollId, optionIds ->
                                            viewModel.votePoll(pollId, optionIds)
                                        },
                                        isFromMe = isFromMe,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Voice message (recorded in-app)
                                message.type == MessageType.VOICE && message.mediaUrl != null -> {
                                    val durationSeconds = try {
                                        val regex = """\((\d+)s\)""".toRegex()
                                        regex.find(message.content)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                                    } catch (e: Exception) { 0L }
                                    
                                VoiceMessageBubble(
                                        mediaUrl = message.mediaUrl!!,
                                        durationMs = durationSeconds * 1000L,
                                        isMyMessage = isFromMe,
                                        audioPlayerManager = viewModel.audioPlayerManager,
                                        amplitudes = message.amplitudes,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Image message
                                message.type == MessageType.IMAGE && message.mediaUrl != null -> {
                                    ImageMessageBubble(
                                        mediaUrl = message.mediaUrl!!,
                                        isVideo = false,
                                        isMyMessage = isFromMe,
                                        time = message.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                                        onPreviewClick = { url, type ->
                                            mediaPreviewUrl = url
                                            mediaPreviewType = type
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Video message
                                message.type == MessageType.VIDEO && message.mediaUrl != null -> {
                                    ImageMessageBubble(
                                        mediaUrl = message.mediaUrl!!,
                                        isVideo = true,
                                        isMyMessage = isFromMe,
                                        time = message.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                                        onPreviewClick = { url, type ->
                                            mediaPreviewUrl = url
                                            mediaPreviewType = type
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Audio file (from file picker)
                                message.type == MessageType.AUDIO && message.mediaUrl != null -> {
                                    // Extract file name from content (format: "🎵 filename.mp3")
                                    val fileName = message.content.removePrefix("🎵 ").trim()
                                    AudioFileBubble(
                                        mediaUrl = message.mediaUrl!!,
                                        fileName = fileName,
                                        durationMs = 0L, // Duration will be fetched from player
                                        isMyMessage = isFromMe,
                                        audioPlayerManager = viewModel.audioPlayerManager,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Location message
                                message.type == MessageType.LOCATION && message.mediaUrl != null -> {
                                    // Parse location from mediaUrl (format: "lat,lng")
                                    val parts = message.mediaUrl!!.split(",")
                                    if (parts.size == 2) {
                                        val lat = parts[0].toDoubleOrNull() ?: 0.0
                                        val lng = parts[1].toDoubleOrNull() ?: 0.0
                                        LocationMessageBubble(
                                            latitude = lat,
                                            longitude = lng,
                                            isMyMessage = isFromMe,
                                            time = message.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }
                                
                                // File message (from file picker)
                                message.type == MessageType.FILE && message.mediaUrl != null -> {
                                    // Extract file name from content (format: "📎 filename.ext" or just the file content)
                                    val fileName = message.content
                                        .removePrefix("📎 ")
                                        .removePrefix("📄 ")
                                        .trim()
                                        .ifEmpty { "file_${message.id}" }
                                    FileMessageBubble(
                                        mediaUrl = message.mediaUrl!!,
                                        fileName = fileName,
                                        isMyMessage = isFromMe,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                                
                                // Text or other message types
                                else -> {
                                    ChatBubble(
                                        message = message.content,
                                        time = message.createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
                                        isMyMessage = isFromMe,
                                        status = message.status,
                                        position = BubblePosition.SINGLE,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        reactions = message.reactions,
                                        myReaction = message.myReaction,
                                        replyToMessage = message.replyToMessage,
                                        onReactionClick = { emoji ->
                                            viewModel.reactToMessage(message.id, emoji)
                                        },
                                        onSenderClick = if (chat?.type != com.Kelasor.app.domain.model.ChatType.PRIVATE) {
                                            { onNavigateToProfile(message.senderId) }
                                        } else null,
                                        senderName = if (chat?.type != com.Kelasor.app.domain.model.ChatType.PRIVATE) message.senderName else null,
                                        onReplyClick = {
                                            message.replyToMessageId?.let { replyId ->
                                                val index = messages.indexOfFirst { it.id == replyId }
                                                if (index != -1) {
                                                    scope.launch { listState.animateScrollToItem(index) }
                                                }
                                            }
                                        },
                                        onLongClick = { 
                                            viewModel.toggleMessageSelection(message.id)
                                        }
                                    )
                                }
                            }
                        }
                        }
                    }
                    // Date separator
                    item {
                        DateSeparator(dateText = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.today))
                    }
                }

                // Loading indicator overlay - Show during initial load
                if (state.isInitialLoad && messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = extendedColors.accent)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.loading_messages),
                                style = MessageAppTypography.chatTime,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.uploading),
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
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { state.uploadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CardShapes.button),
                            color = extendedColors.accent,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    } else {
                        androidx.compose.material3.LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CardShapes.button),
                            color = extendedColors.accent,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
            
            // Reply Indicator
            state.replyToMessage?.let { replyMsg ->
                ReplyIndicator(
                    message = replyMsg,
                    onCancel = { viewModel.setReplyMessage(null) }
                )
            }

            // Input bar
            MessageInputBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    } else {
                        Toast.makeText(context, context.getString(com.Kelasor.app.R.string.enter_message_error), Toast.LENGTH_SHORT).show()
                    }
                },
                onAttachClick = { showAttachmentMenu = true },
                voiceRecorderManager = viewModel.voiceRecorderManager,
                onVoiceRecorded = { file, duration, amplitudes ->
                    viewModel.sendVoiceMessage(file, duration, amplitudes)
                }
            )
        }
        
        // Attachment Menu overlay
        AttachmentMenu(
            visible = showAttachmentMenu,
            onDismiss = { showAttachmentMenu = false },
            onFileClick = { filePickerLauncher.launch("*/*") },
            onGalleryClick = { galleryPickerLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
                )
            ) },  // Use PickVisualMedia to allow both images and videos
            onAudioClick = { audioPickerLauncher.launch("audio/*") },
            onLocationClick = { 
                showAttachmentMenu = false
                showLocationPicker = true
            },
            onPollClick = null // No polls in private chat
        )
        
        // Poll Creator removed from private chat
        
        // Location Picker Dialog
        LocationPickerDialog(
            visible = showLocationPicker,
            locationManager = viewModel.locationManager,
            onDismiss = { showLocationPicker = false },
            onSendLocation = { lat, lng ->
                viewModel.sendLocationMessage(lat, lng)
            }
        )
        
        // Media Preview Dialog
        mediaPreviewUrl?.let { url ->
            MediaPreviewDialog(
                mediaUrl = url,
                mediaType = mediaPreviewType,
                onDismiss = { 
                    mediaPreviewUrl = null
                    mediaPreviewType = MediaType.UNKNOWN
                }
            )
        }
    // }
}

@Composable
fun ReplyIndicator(
    message: Message,
    onCancel: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
         Box(
             modifier = Modifier
                 .width(4.dp)
                 .height(36.dp)
                 .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                 .background(extendedColors.accent)
         )
         Spacer(modifier = Modifier.width(8.dp))
         Column(modifier = Modifier.weight(1f)) {
             Text(
                 text = message.senderName,
                 style = MessageAppTypography.chatTime.copy(fontWeight = FontWeight.Bold),
                 color = extendedColors.accent
             )
             Text(
                 text = message.content,
                 style = MessageAppTypography.chatTime,
                 maxLines = 1,
                 overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                 color = MaterialTheme.colorScheme.onSurfaceVariant
             )
         }
         IconButton(onClick = onCancel) {
             Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
         }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⌨️ Message Input Bar
// ═══════════════════════════════════════════════════════════════════════════════

// MessageInputBar is now imported from ui.components

@Composable
private fun scaleIn() = androidx.compose.animation.scaleIn(
    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)

@Composable
private fun scaleOut() = androidx.compose.animation.scaleOut()

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Mock Data
// ═══════════════════════════════════════════════════════════════════════════════

private fun getMockMessages(): List<Message> {
    val now = Instant.now()
    return listOf(
        Message(
            id = "m1",
            chatId = "1",
            senderId = "other",
            senderName = "علی محمدی",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "سلام! خوبی؟",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.READ,
            isEdited = false,
            createdAt = now.minusSeconds(60),
            editedAt = null
        ),
        Message(
            id = "m2",
            chatId = "1",
            senderId = "current_user_id",
            senderName = "من",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "سلام! ممنون، تو خوبی؟",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.READ,
            isEdited = false,
            createdAt = now.minusSeconds(55),
            editedAt = null
        ),
        Message(
            id = "m3",
            chatId = "1",
            senderId = "other",
            senderName = "علی محمدی",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "آره، ممنون! داشتم فکر می‌کردم یه پروژه جدید شروع کنیم",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.READ,
            isEdited = false,
            createdAt = now.minusSeconds(45),
            editedAt = null
        ),
        Message(
            id = "m4",
            chatId = "1",
            senderId = "current_user_id",
            senderName = "من",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "عالیه! چه پروژه‌ای؟ 🤔",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.READ,
            isEdited = false,
            createdAt = now.minusSeconds(30),
            editedAt = null
        ),
        Message(
            id = "m5",
            chatId = "1",
            senderId = "other",
            senderName = "علی محمدی",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "یه اپلیکیشن پیام‌رسان! مثل تلگرام ولی بهتر 😎",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.READ,
            isEdited = false,
            createdAt = now.minusSeconds(20),
            editedAt = null
        ),
        Message(
            id = "m6",
            chatId = "1",
            senderId = "current_user_id",
            senderName = "من",
            senderAvatar = null,
            type = MessageType.TEXT,
            content = "واو! خیلی جالبه! من هم می‌خوام کمک کنم 🙌",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = MessageStatus.DELIVERED,
            isEdited = false,
            createdAt = now.minusSeconds(10),
            editedAt = null
        )
    ).reversed()
}

/**
 * Extract duration in milliseconds from voice message content.
 * Content format: "🎤 صدا (Xs)" where X is seconds.
 */
private fun extractDurationFromContent(content: String): Long {
    return try {
        // Match pattern like "(6s)" or "(12s)"
        val regex = """\((\d+)s\)""".toRegex()
        val match = regex.find(content)
        val seconds = match?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        seconds * 1000L
    } catch (e: Exception) {
        0L
    }
}
