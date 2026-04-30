package com.hasani.messageapp.ui.screens.story

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hasani.messageapp.domain.model.StoryType
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.ui.viewmodel.StoryViewModel
import com.hasani.messageapp.ui.viewmodel.StoriesUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateTextStory: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit = {}, // userId -> Navigate to profile
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    
    // Find current user's story user wrapper
    val myStoryUser = (uiState as? StoriesUiState.Success)?.storyUsers?.find { it.isCurrentUser }
    val stories = myStoryUser?.stories ?: emptyList()

    // Story Picker
    val storyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Auto detect type in ViewModel
            viewModel.uploadStory(uri, "AUTO")
        }
    }
    
    // Error Handling
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        if (uiState is StoriesUiState.Error) {
            android.widget.Toast.makeText(context, (uiState as StoriesUiState.Error).message, android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    // UI States
    var initialStoryIndex by remember { mutableStateOf(0) }
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("استوری‌های من", style = MessageAppTypography.chatName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptionsSheet = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("استوری جدید") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (stories.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "شما هنوز استوری نگذاشته‌اید",
                        style = MessageAppTypography.body,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showAddOptionsSheet = true }) {
                        Text("ارسال اولین استوری")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(stories.size) { index ->
                        val story = stories[index]
                        
                        ListItem(
                            modifier = Modifier.clickable {
                                initialStoryIndex = index
                                myStoryUser?.let { viewModel.openStoryViewer(it) }
                            },
                            headlineContent = {
                                Text(
                                    text = if (story.caption.isNullOrBlank()) "بدون متن" else story.caption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                val formatter = DateTimeFormatter.ofPattern("HH:mm - dd MMM")
                                    .withZone(ZoneId.systemDefault())
                                Text(formatter.format(story.createdAt))
                            },
                            leadingContent = {
                                if (story.type == StoryType.VIDEO) {
                                    // Video thumbnail placeholder or icon
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black),
                                        contentAlignment = Alignment.Center
                                    ) {
                                       Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                                            contentDescription = "Video",
                                            tint = Color.White
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = story.mediaUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Views",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${story.viewCount}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            if (isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                )
            }
            
            // Add Options Sheet
            if (showAddOptionsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showAddOptionsSheet = false },
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
                                showAddOptionsSheet = false
                                storyPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            }
                        )
                        
                        ListItem(
                            headlineContent = { Text("متن با پس‌زمینه") },
                            leadingContent = { 
                                Icon(androidx.compose.material.icons.Icons.Default.Create, contentDescription = null) 
                            },
                            modifier = Modifier.clickable {
                                showAddOptionsSheet = false
                                onNavigateToCreateTextStory()
                            }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            // Story Viewer Overlay
            val selectedStoryUser by viewModel.selectedStoryUser.collectAsState()
            
            if (selectedStoryUser != null && selectedStoryUser?.isCurrentUser == true) {
                com.hasani.messageapp.ui.screens.story.StoryViewerScreen(
                    viewModel = viewModel,
                    storyUser = selectedStoryUser!!,
                    initialStoryIndex = initialStoryIndex,
                    onClose = { viewModel.closeStoryViewer() },
                    onStoryViewed = { /* Already handled */ },
                    onNavigateToProfile = { userId ->
                        viewModel.closeStoryViewer()
                        onNavigateToUserProfile(userId)
                    }
                )
            }
        }
    }
}
