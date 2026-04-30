package com.Kelasor.app.ui.screens.channel

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.Kelasor.app.domain.model.ChannelPost
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.components.BubblePosition
import com.Kelasor.app.ui.components.ChatBubble
import com.Kelasor.app.ui.components.MediaType
import com.Kelasor.app.data.remote.dto.ChannelPostCommentDto
import com.Kelasor.app.ui.components.MessageActionSheet
import com.Kelasor.app.ui.components.PinnedMessageBanner
import kotlinx.coroutines.launch
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.viewmodel.ChannelViewViewModel
import com.Kelasor.app.ui.screens.chat.EditMessageDialog
import com.Kelasor.app.ui.screens.chat.MessageSelectionTopBar
import com.Kelasor.app.ui.components.VideoNoteBubble
import com.Kelasor.app.ui.components.CircularVideoRecorder
import com.Kelasor.app.data.video.VideoNoteRecordingState
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChannelViewScreen(
    channelId: String,
    initialMessageId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToChannelSettings: () -> Unit = {},
    onNavigateToExamCreation: () -> Unit = {},
    viewModel: ChannelViewViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var postForActions by remember { mutableStateOf<ChannelPost?>(null) }
    val scope = rememberCoroutineScope()
    
    // Auto-scroll to initial message
    val initialScrollDone = remember { mutableStateOf(false) }
    LaunchedEffect(state.posts, initialMessageId) {
        if (!initialScrollDone.value && initialMessageId != null && state.posts.isNotEmpty()) {
            val index = state.posts.indexOfFirst { it.id == initialMessageId }
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
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()) }

    // Media preview state
    var mediaPreviewUrl by remember { mutableStateOf<String?>(null) }
    var mediaPreviewType by remember { mutableStateOf(MediaType.UNKNOWN) }

    // Attachment menu state
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    
    // Edit dialog state
    var showEditPostDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<ChannelPost?>(null) }
    // Video note recording state
    var showVideoNoteRecorder by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadAndSendFile(channelId, it, context) }
    }
    
    // Gallery picker launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it)
            val isVideo = mimeType?.startsWith("video/") == true
            viewModel.uploadAndSendMedia(channelId, it, context, isVideo = isVideo)
        }
    }
    
    // Audio picker launcher
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadAndSendAudio(channelId, it, context) }
    }

    // Back Handler
    BackHandler(enabled = state.isCreatePostDialogVisible) {
        viewModel.hideCreatePostDialog()
    }

    // Load Channel Data
    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
    }

    DisposableEffect(channelId) {
        viewModel.setActiveChat(channelId)
        onDispose {
            viewModel.clearActiveChat()
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.ChannelEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    val isInSelectionMode = state.selectedPostIds.isNotEmpty()
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (isInSelectionMode) {
                MessageSelectionTopBar(
                    selectedCount = state.selectedPostIds.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onDeleteSelected = { deleteForEveryone ->
                        state.selectedPostIds.forEach { postId ->
                            viewModel.deletePost(postId, deleteForEveryone)
                        }
                        viewModel.clearSelection()
                        Toast.makeText(
                            context,
                            if (deleteForEveryone) "پست‌ها برای همه حذف شد" else "پست‌ها حذف شد",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onEditSelected = {
                        val postId = state.selectedPostIds.firstOrNull()
                        val post = state.posts.find { it.id == postId }
                        if (post != null) {
                            postToEdit = post
                            showEditPostDialog = true
                        }
                        viewModel.clearSelection()
                    },
                    onCopySelected = {
                        val selectedText = state.posts.filter { it.id in state.selectedPostIds }
                            .joinToString("\n") { it.content }
                        clipboardManager.setText(AnnotatedString(selectedText))
                        viewModel.clearSelection()
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.channel?.name ?: "کانال")
                            Text(
                                text = "${state.channel?.subscriberCount ?: 0} عضو",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                         IconButton(onClick = onNavigateBack) {
                             Icon(
                                 imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                 contentDescription = "Back"
                             )
                         }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleMute() }) {
                            val isMuted = state.channel?.isMuted == true
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.NotificationsOff else Icons.Filled.Notifications,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = if (isMuted) extendedColors.accent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = onNavigateToChannelSettings) {
                            Icon(
                                 imageVector = Icons.Filled.Settings,
                                 contentDescription = "Settings"
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                if (state.canPost) {
                    com.Kelasor.app.ui.components.MessageInputBar(
                        text = state.newPostContent,
                        onTextChange = { viewModel.setNewPostContent(it) },
                        onSendClick = {
                            if (state.newPostContent.isNotBlank()) {
                                viewModel.createOrEditPost()
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
                } else if (!state.isMember) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { viewModel.subscribe() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = extendedColors.accent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "عضویت در کانال",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
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
                            text = "فقط مدیران می‌توانند پست ارسال کنند",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        ) {
            // Pinned post banner
            val pinnedPost = remember(state.posts) {
                state.posts.filter { it.isPinned }.maxByOrNull { it.pinnedAt ?: java.time.Instant.EPOCH }
            }
            if (pinnedPost != null) {
                PinnedMessageBanner(
                    content = pinnedPost.content,
                    senderName = state.channel?.name,
                    messageType = pinnedPost.type,
                    onClick = {
                        val sortedPosts = state.posts.sortedByDescending { it.createdAt }
                        val index = sortedPosts.indexOfFirst { it.id == pinnedPost.id }
                        if (index != -1) {
                            scope.launch {
                                listState.animateScrollToItem(index)
                                highlightedMessageId = pinnedPost.id
                            }
                        }
                    },
                    onUnpin = {
                        viewModel.pinPost(pinnedPost.id, false)
                    }
                )
            }
            // Posts List
            Box(modifier = Modifier.weight(1f)) {
                 if (state.isLoading && state.posts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                } else if (state.posts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "هنوز پستی وجود ندارد",
                                style = MessageAppTypography.chatName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        reverseLayout = true
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        
                        items(
                             items = state.posts.sortedByDescending { it.createdAt },
                             key = { it.id }
                        ) { post ->
                            val isSelected = state.selectedPostIds.contains(post.id)
                            ChannelPostItemWrapper(
                                post = post,
                                viewModel = viewModel,
                                isSelected = isSelected,
                                isHighlighted = post.id == highlightedMessageId,
                                isInSelectionMode = isInSelectionMode,
                                onClick = {
                                    if (isInSelectionMode) {
                                        viewModel.togglePostSelection(post.id)
                                    } else {
                                        postForActions = post
                                    }
                                },
                                onLongClick = { viewModel.togglePostSelection(post.id) },
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

            // Post Actions Overlay (Telegram-style single click)
            val selectedPost = postForActions
            com.Kelasor.app.ui.components.MessageActionsOverlay(
                visible = selectedPost != null,
                message = if (selectedPost != null) Message(
                    id = selectedPost.id,
                    chatId = selectedPost.channelId,
                    senderId = "", 
                    senderName = state.channel?.name ?: "Channel",
                    senderAvatar = null,
                    type = MessageType.TEXT,
                    content = selectedPost.content,
                    mediaUrl = selectedPost.mediaUrl,
                    replyToMessageId = null,
                    replyToMessage = null,
                    forwardedFrom = null,
                    status = MessageStatus.READ,
                    isEdited = false,
                    createdAt = selectedPost.createdAt,
                    editedAt = null
                ) else null,
                messageContent = {
                    if (selectedPost != null) {
                        ChatBubble(
                            message = selectedPost.content,
                            time = try { 
                                val timeStr = timeFormatter.format(selectedPost.createdAt)
                                "$timeStr • ${selectedPost.viewCount} بازدید"
                            } catch (e: Exception) { "" },
                            isMyMessage = false,
                            status = MessageStatus.SENT,
                            position = BubblePosition.SINGLE,
                            reactions = selectedPost.reactions
                        )
                    }
                },
                isOwner = state.canPost,
                reactionCount = selectedPost?.reactions?.values?.sum() ?: 0,
                onDismiss = { postForActions = null },
                onReactionClick = { emoji ->
                    selectedPost?.let { viewModel.reactToPost(it.id, emoji) }
                },
                onReplyClick = {
                    selectedPost?.let { viewModel.openComments(it.id) }
                },
                onCopyClick = {
                    selectedPost?.let {
                        clipboardManager.setText(AnnotatedString(it.content))
                        Toast.makeText(context, "کپی شد", Toast.LENGTH_SHORT).show()
                    }
                },
                onEditClick = if (state.canPost) {{
                    selectedPost?.let {
                        postToEdit = it
                        showEditPostDialog = true
                    }
                }} else null,
                onDeleteClick = if (state.canPost) {{ deleteForEveryone ->
                    selectedPost?.let { viewModel.deletePost(it.id, deleteForEveryone) }
                }} else null,
                replyLabel = "نظر",
                onSelectClick = {
                    selectedPost?.let { viewModel.togglePostSelection(it.id) }
                },
                onLockContentClick = {
                    Toast.makeText(context, "به زودی!", Toast.LENGTH_SHORT).show()
                },
                onAdClick = {
                    selectedPost?.let { post ->
                        Toast.makeText(context, "تبلیغات: ${post.content.take(30)}...", Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            // Edit Post Dialog
            if (showEditPostDialog && postToEdit != null) {
                // For media posts, extract caption (strip emoji prefix + file name)
                val editableContent = remember(postToEdit) {
                    val p = postToEdit!!
                    if (p.type in listOf(MessageType.IMAGE, MessageType.VIDEO)) {
                        val stripped = p.content
                            .removePrefix("🖼️ ")
                            .removePrefix("🎬 ")
                            .trim()
                        if (stripped.startsWith("media_") || stripped.startsWith("edited_")) "" else stripped
                    } else {
                        p.content
                    }
                }
                EditMessageDialog(
                    originalMessage = editableContent,
                    onConfirm = { newContent ->
                        viewModel.editPost(postToEdit!!.id, newContent)
                        Toast.makeText(context, "پست ویرایش شد", Toast.LENGTH_SHORT).show()
                        showEditPostDialog = false
                        postToEdit = null
                    },
                    onDismiss = {
                        showEditPostDialog = false
                        postToEdit = null
                    }
                )
            }
            
            // Comments Bottom Sheet
            if (state.activePostForComments != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.closeComments() },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                        Text(
                            text = "نظرات (${state.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
                        )
                        Divider()
                        
                        // Comment List
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            if (state.isLoadingComments) {
                                item { CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally)) }
                            } else if (state.comments.isEmpty()) {
                                item { Text("هنوز نظری ثبت نشده است.", modifier = Modifier.padding(16.dp)) }
                            } else {
                                items(state.comments) { comment ->
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text(text = comment.user.displayName, style = MaterialTheme.typography.labelMedium, color = extendedColors.accent)
                                        Text(text = comment.content, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = comment.createdAt, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                }
                            }
                        }
                        
                        // Comment Input
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = state.newCommentContent,
                                onValueChange = { viewModel.updateNewCommentContent(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("نظر خود را بنویسید...") },
                                maxLines = 3
                            )
                            IconButton(onClick = { viewModel.sendComment() }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = extendedColors.accent)
                            }
                        }
                    }
                }
            }
            
            // Input Bar (Only for admins or channel owner)
            if (state.canPost) {
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

                }
            }
            

        }

        // Overlay Menus
        com.Kelasor.app.ui.components.AttachmentMenu(
            visible = showAttachmentMenu,
            onDismiss = { showAttachmentMenu = false },
            onFileClick = { filePickerLauncher.launch("*/*") },
            onGalleryClick = { galleryPickerLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageAndVideo
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
            },
            onExamClick = {
                showAttachmentMenu = false
                onNavigateToExamCreation()
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
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelPostItemWrapper(
    post: ChannelPost,
    viewModel: ChannelViewViewModel,
    isSelected: Boolean = false,
    isHighlighted: Boolean = false,
    isInSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPreviewMedia: (String, MediaType) -> Unit,
    timeFormatter: DateTimeFormatter
) {
    val extendedColors = MessageAppTheme.extendedColors
    val timeString = try {
        val dateStr = timeFormatter.format(post.createdAt)
        "$dateStr • ${post.viewCount} بازدید"
    } catch (e: Exception) { "" }

    // Entire row is clickable for better UX
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            com.Kelasor.app.ui.components.AnimatedMessageBubble(
                isMyMessage = false,
                messageId = post.id
            ) {
                // Special handling for polls - allow voting
                if (post.poll != null) {
                    com.Kelasor.app.ui.components.PollBubble(
                        poll = post.poll!!,
                        onVote = { pollId, optionIds ->
                            viewModel.votePoll(pollId, optionIds)
                        },
                        isFromMe = false
                    )
                } else {
                    when {
                        post.type == MessageType.VOICE && post.mediaUrl != null -> {
                            val durationSeconds = try {
                                val regex = """\((\d+)s\)""".toRegex()
                                regex.find(post.content)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                            } catch (e: Exception) { 0L }
                            com.Kelasor.app.ui.components.VoiceMessageBubble(
                                mediaUrl = post.mediaUrl!!,
                                durationMs = durationSeconds * 1000L,
                                isMyMessage = false,
                                audioPlayerManager = viewModel.audioPlayerManager,
                                amplitudes = post.amplitudes,
                                time = timeString,
                                reactions = post.reactions,
                                myReaction = post.myReaction,
                                onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                            )
                        }
                        post.type == MessageType.IMAGE && post.mediaUrl != null -> {
                            val imageCaption = post.content
                                .removePrefix("🖼️ ")
                                .trim()
                                .let { if (it.startsWith("media_") || it.startsWith("edited_") || it.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|mp4|mov|avi|mkv|bmp|svg|heic|heif|3gp|webm)$", RegexOption.IGNORE_CASE))) null else it }
                            com.Kelasor.app.ui.components.ImageMessageBubble(
                                mediaUrl = post.mediaUrl!!,
                                isVideo = false,
                                isMyMessage = false,
                                time = timeString,
                                onPreviewClick = { url, type -> onPreviewMedia(url, type) },
                                caption = imageCaption,
                                reactions = post.reactions,
                                myReaction = post.myReaction,
                                onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                            )
                        }
                        post.type == MessageType.VIDEO && post.mediaUrl != null -> {
                            val videoCaption = post.content
                                .removePrefix("🎬 ")
                                .trim()
                                .let { if (it.startsWith("media_") || it.startsWith("edited_") || it.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|mp4|mov|avi|mkv|bmp|svg|heic|heif|3gp|webm)$", RegexOption.IGNORE_CASE))) null else it }
                            com.Kelasor.app.ui.components.ImageMessageBubble(
                                mediaUrl = post.mediaUrl!!,
                                isVideo = true,
                                isMyMessage = false,
                                time = timeString,
                                onPreviewClick = { url, type -> onPreviewMedia(url, type) },
                                caption = videoCaption,
                                reactions = post.reactions,
                                myReaction = post.myReaction,
                                onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                            )
                        }

                        // Video note (circular video)
                        post.type == MessageType.VIDEO_NOTE && post.mediaUrl != null -> {
                            val durationText = post.content
                                .substringAfter("(", "")
                                .substringBefore("s)", "")
                                .let { if (it.isNotBlank()) "${it}s" else null }
                            VideoNoteBubble(
                                mediaUrl = post.mediaUrl!!,
                                isMyMessage = false,
                                time = timeString,
                                durationText = durationText,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                reactions = post.reactions,
                                myReaction = post.myReaction,
                                onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                            )
                        }

                        post.type == MessageType.AUDIO && post.mediaUrl != null -> {
                            val fileName = post.content.removePrefix("🎵 ").trim()
                            com.Kelasor.app.ui.components.AudioFileBubble(
                                mediaUrl = post.mediaUrl!!,
                                fileName = fileName,
                                durationMs = 0L,
                                isMyMessage = false,
                                audioPlayerManager = viewModel.audioPlayerManager,
                                time = timeString
                            )
                        }
                        post.type == MessageType.LOCATION && post.mediaUrl != null -> {
                            val parts = post.mediaUrl!!.split(",")
                            if (parts.size == 2) {
                                val lat = parts[0].toDoubleOrNull() ?: 0.0
                                val lng = parts[1].toDoubleOrNull() ?: 0.0
                                com.Kelasor.app.ui.components.LocationMessageBubble(
                                    latitude = lat,
                                    longitude = lng,
                                    isMyMessage = false,
                                    time = timeString,
                                    reactions = post.reactions,
                                    myReaction = post.myReaction,
                                    onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                                )
                            }
                        }
                        post.type == MessageType.FILE && post.mediaUrl != null -> {
                            val fileName = post.content
                                .removePrefix("📎 ")
                                .removePrefix("📄 ")
                                .trim()
                                .ifEmpty { "file_${post.id}" }
                            com.Kelasor.app.ui.components.FileMessageBubble(
                                mediaUrl = post.mediaUrl!!,
                                fileName = fileName,
                                isMyMessage = false,
                                time = timeString,
                                reactions = post.reactions,
                                myReaction = post.myReaction,
                                onReactionClick = { emoji -> viewModel.reactToPost(post.id, emoji) }
                            )
                        }
                        else -> {
                            ChatBubble(
                                message = post.content,
                                time = timeString,
                                isMyMessage = false,
                                status = MessageStatus.SENT,
                                position = BubblePosition.SINGLE,
                                senderName = null,
                                reactions = post.reactions
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelPostItem(
    post: ChannelPost,
    timeFormatter: DateTimeFormatter,
    onLongClick: () -> Unit
) {
    val timeString = try {
        val dateStr = timeFormatter.format(post.createdAt)
        "$dateStr • ${post.viewCount} بازدید"
    } catch(e: Exception) {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        ChatBubble(
            message = post.content,
            time = timeString,
            isMyMessage = false,
            status = MessageStatus.SENT,
            position = BubblePosition.SINGLE,
            senderName = null,
            reactions = post.reactions
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
