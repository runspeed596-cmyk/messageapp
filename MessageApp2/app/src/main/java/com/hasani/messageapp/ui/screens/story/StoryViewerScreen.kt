package com.hasani.messageapp.ui.screens.story

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.hasani.messageapp.domain.model.Story
import com.hasani.messageapp.domain.model.StoryType
import com.hasani.messageapp.domain.model.StoryUser
import com.hasani.messageapp.ui.theme.MessageAppTypography
import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Full screen story viewer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerScreen(
    viewModel: com.hasani.messageapp.ui.viewmodel.StoryViewModel,
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
    
    // Ensure index is valid
    val safeIndex = currentStoryIndex.coerceIn(storyUser.stories.indices)
    val currentStory = storyUser.stories[safeIndex]
    
    // Mark as viewed when story changes
    LaunchedEffect(currentStory.id) {
        onStoryViewed(currentStory)
    }

    // Timer Logic
    var progress by remember(currentStory.id) { mutableStateOf(0f) }
    
    LaunchedEffect(currentStory.id, isPaused, showViewersSheet) {
        if (isPaused || showViewersSheet) return@LaunchedEffect
        
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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
        
        // OVERLAYS (Progress, User Info, Caption)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 32.dp)
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
                    
                    LinearProgressIndicator(
                        progress = { barProgress },
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
                // Avatar
                AsyncImage(
                    model = storyUser.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = storyUser.displayName,
                        style = MessageAppTypography.chatName,
                        color = Color.White
                    )
                    Text(
                        text = "12 ساعت پیش", // TODO: Real relative time
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
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
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
            
            // 4. Viewers (Only for current user)
            if (storyUser.isCurrentUser) {
                Spacer(modifier = Modifier.height(8.dp))
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
                        text = "${currentStory.viewCount}",
                        color = Color.White,
                        style = MessageAppTypography.body
                    )
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
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(viewers) { viewer ->
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
                                    AsyncImage(
                                        model = viewer.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(viewer.displayName, style = MessageAppTypography.body)
                                        Text(
                                            text = "مشاهده پروفایل",
                                            style = MessageAppTypography.caption,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    androidx.compose.material3.Icon(
                                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.KeyboardArrowRight, // Use Right for "Go To" in LTR, flips in RTL
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
        // Video Player
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(story.mediaUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
        }
        
        // Handle Pause/Resume
        LaunchedEffect(isPaused) {
            if (isPaused) exoPlayer.pause() else exoPlayer.play()
        }
        
        // Clean up
        DisposableEffect(Unit) {
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
