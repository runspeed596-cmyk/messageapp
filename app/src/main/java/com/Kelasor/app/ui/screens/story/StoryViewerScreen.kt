package com.Kelasor.app.ui.screens.story

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.domain.model.Story
import com.Kelasor.app.domain.model.StoryType
import com.Kelasor.app.domain.model.StoryUser
import com.Kelasor.app.ui.theme.MessageAppTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Duration

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.input.TextFieldValue
import android.widget.Toast
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * Computes a Persian relative time string from an Instant.
 */
private fun getRelativeTime(instant: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(instant, now)
    val seconds = duration.seconds
    return when {
        seconds < 60 -> "الان"
        seconds < 3600 -> "${seconds / 60} دقیقه پیش"
        seconds < 86400 -> "${seconds / 3600} ساعت پیش"
        else -> "${seconds / 86400} روز پیش"
    }
}

/**
 * Full screen story viewer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    viewModel: com.Kelasor.app.ui.viewmodel.StoryViewModel,
    storyUser: StoryUser,
    initialStoryIndex: Int = 0,
    onClose: () -> Unit,
    onStoryViewed: (Story) -> Unit,
    onNavigateToProfile: (String) -> Unit = {} // userId -> Navigate to user profile
) {
    var currentStoryIndex by remember { mutableStateOf(initialStoryIndex) }
    var isPaused by remember { mutableStateOf(false) }
    var showViewersSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isReplyFocused by remember { mutableStateOf(false) }
    val replyFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current

    // Listen for reply sent events
    LaunchedEffect(Unit) {
        viewModel.replySentEvent.collect { success ->
            if (success) {
                replyText = ""
                Toast.makeText(context, "پاسخ ارسال شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "خطا در ارسال پاسخ", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Ensure index is valid
    val safeIndex = currentStoryIndex.coerceIn(storyUser.stories.indices)
    val currentStory = storyUser.stories[safeIndex]
    
    // Mark as viewed when story changes
    LaunchedEffect(currentStory.id) {
        onStoryViewed(currentStory)
    }

    // Timer Logic
    var progress by remember(currentStory.id) { mutableStateOf(0f) }
    
    LaunchedEffect(currentStory.id, isPaused, showViewersSheet, isReplyFocused) {
        if (isPaused || showViewersSheet || isReplyFocused) return@LaunchedEffect
        
        val durationMs = currentStory.durationSeconds * 1000L
        val updateInterval = 50L
        val step = updateInterval.toFloat() / durationMs
        
        while (progress < 1f) {
            delay(updateInterval)
            progress += step
        }
        
        // Auto-advance
        if (currentStoryIndex < storyUser.stories.lastIndex) {
            currentStoryIndex++
        } else {
            onClose() // Finished all stories
        }
    }

    // Immersive entry animation
    val entryAlpha = remember { Animatable(0f) }
    val entryScale = remember { Animatable(1.05f) }
    LaunchedEffect(Unit) {
        launch {
            entryAlpha.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }
        launch {
            entryScale.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .graphicsLayer {
                alpha = entryAlpha.value
                scaleX = entryScale.value
                scaleY = entryScale.value
            }
            // Gesture handling
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPaused = true
                        tryAwaitRelease()
                        isPaused = false
                    },
                    onTap = { offset ->
                        val screenWidth = size.width
                        // REVERSED LOGIC FOR RTL:
                        // Left side (start) -> Next
                        // Right side (end) -> Previous
                        // (Usually Right is Next in LTR, so we swap)
                        
                        if (offset.x > screenWidth * 2 / 3) {
                             // Right side -> Previous
                            if (currentStoryIndex > 0) {
                                currentStoryIndex--
                            }
                        } else {
                            // Left side / Center -> Next
                            if (currentStoryIndex < storyUser.stories.lastIndex) {
                                currentStoryIndex++
                            } else {
                                onClose()
                            }
                        }
                    }
                )
            }
    ) {
        // CONTENT
        StoryContent(
            story = currentStory,
            isPaused = isPaused
        )
        
        // Dark gradient overlay at top so icons/progress bars are visible on white stories
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
        // OVERLAYS (Progress, User Info, Caption)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 1. Progress Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                storyUser.stories.forEachIndexed { index, story ->
                    val barProgress = when {
                        index < currentStoryIndex -> 1f // Completed
                        index == currentStoryIndex -> progress // Current
                        else -> 0f // Future
                    }
                    // Smooth animated progress
                    val animatedBarProgress by animateFloatAsState(
                        targetValue = barProgress,
                        animationSpec = tween(
                            durationMillis = if (index == currentStoryIndex) 80 else 200,
                            easing = LinearEasing
                        ),
                        label = "progressBar_$index"
                    )
                    LinearProgressIndicator(
                        progress = { animatedBarProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 2. User Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initials fallback
                com.Kelasor.app.ui.components.story.StoryAvatarImage(
                    model = storyUser.avatarUrl,
                    displayName = storyUser.displayName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = storyUser.displayName,
                        style = MessageAppTypography.chatName,
                        color = Color.White
                    )
                    Text(
                        text = getRelativeTime(currentStory.createdAt),
                        style = MessageAppTypography.caption,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Delete Button (Only for own stories)
                if (storyUser.isCurrentUser) {
                    IconButton(onClick = { 
                        isPaused = true // Pause story while dialog is open
                        showDeleteDialog = true 
                    }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                IconButton(onClick = onClose) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Delete Dialog
            if (showDeleteDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { 
                        showDeleteDialog = false
                        isPaused = false
                    },
                    title = { Text("حذف استوری") },
                    text = { Text("آیا از حذف این استوری اطمینان دارید؟") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                viewModel.deleteStory(currentStory.id)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("حذف", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                showDeleteDialog = false
                                isPaused = false
                            }
                        ) {
                            Text("لغو")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 3. Caption
            if (!currentStory.caption.isNullOrBlank()) {
                Text(
                    text = currentStory.caption,
                    style = MessageAppTypography.body,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            // 4. Viewers (Only for current user) — positioned above caption
            if (storyUser.isCurrentUser) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .clickable {
                             viewModel.loadStoryViews(currentStory.id)
                             showViewersSheet = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Visibility,
                        contentDescription = "Viewers",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${currentStory.viewCount} بازدید",
                        color = Color.White,
                        style = MessageAppTypography.body
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Reply Input (for other users' stories)
            if (!storyUser.isCurrentUser) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                "پاسخ بدهید...",
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.95f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.4f),
                                RoundedCornerShape(24.dp)
                            )
                            .focusRequester(replyFocusRequester)
                            .onFocusChanged { focusState ->
                                isReplyFocused = focusState.isFocused
                            }
                    )
                    if (replyText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.replyToStory(currentStory.id, replyText.trim())
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "ارسال",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        if (showViewersSheet) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = { showViewersSheet = false },
                sheetState = androidx.compose.material3.rememberModalBottomSheetState()
            ) {
                val viewers by viewModel.viewersState.collectAsState()
                
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text("بازدیدکنندگان", style = MessageAppTypography.chatName)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (viewers.isEmpty()) {
                        Text("هنوز کسی ندیده است.", style = MessageAppTypography.body, color = Color.Gray)
                    } else {
                        // Deduplicate viewers by userId, keeping the latest viewedAt
                        val uniqueViewers = remember(viewers) {
                            viewers.groupBy { it.userId }
                                .map { (_, group) -> group.maxByOrNull { it.viewedAt } ?: group.first() }
                                .sortedByDescending { it.viewedAt }
                        }
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(uniqueViewers) { viewer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showViewersSheet = false
                                            onNavigateToProfile(viewer.userId)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar with initials fallback
                                    com.Kelasor.app.ui.components.story.StoryAvatarImage(
                                        model = viewer.avatarUrl,
                                        displayName = viewer.displayName,
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(viewer.displayName, style = MessageAppTypography.body)
                                        Text(
                                            text = getRelativeTime(viewer.viewedAt),
                                            style = MessageAppTypography.caption,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "View Profile",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun StoryContent(
    story: Story,
    isPaused: Boolean
) {
    val context = LocalContext.current
    
    val isVideo = story.type == StoryType.VIDEO || 
                  story.mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                  story.mediaUrl.endsWith(".mov", ignoreCase = true) ||
                  story.mediaUrl.endsWith(".mkv", ignoreCase = true)

    if (!isVideo) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(story.mediaUrl)
                .crossfade(true)
                .listener(
                    onError = { _, result ->
                        android.util.Log.e("StoryViewer", "Error loading image: ${result.throwable.message} URL: ${story.mediaUrl}")
                    },
                    onSuccess = { _, _ ->
                         android.util.Log.d("StoryViewer", "Image loaded successfully: ${story.mediaUrl}")
                    }
                )
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
            contentScale = ContentScale.Fit,
            error = androidx.compose.ui.graphics.painter.ColorPainter(Color.Red) // Show RED if error
        )
    } else {
        // Video Player — keyed on story.id to prevent echo/reuse across stories
        val exoPlayer = remember(story.id) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(story.mediaUrl)
                setMediaItem(mediaItem)
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
                prepare()
                playWhenReady = true
            }
        }
        // Handle Pause/Resume
        LaunchedEffect(isPaused) {
            if (isPaused) exoPlayer.pause() else exoPlayer.play()
        }
        // Clean up when story changes or composable leaves
        DisposableEffect(story.id) {
            onDispose { exoPlayer.release() }
        }
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
