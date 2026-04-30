package com.hasani.messageapp.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hasani.messageapp.domain.model.ChatType
import com.hasani.messageapp.ui.navigation.BottomNavBar
import com.hasani.messageapp.ui.navigation.Routes
import com.hasani.messageapp.ui.screens.bazaar.BazaarScreen
import com.hasani.messageapp.ui.screens.channel.ChannelListScreen
import com.hasani.messageapp.ui.screens.chat.ChatListScreen
import com.hasani.messageapp.ui.screens.course.CourseListScreen
import com.hasani.messageapp.ui.screens.elm.ElmScreen
import com.hasani.messageapp.ui.screens.group.GroupListScreen
import com.hasani.messageapp.ui.screens.home.HomeScreen
import com.hasani.messageapp.ui.screens.treasure.TreasureScreen
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.viewmodel.ChatListViewModel
import com.hasani.messageapp.ui.viewmodel.CreateCourseState

// ═══════════════════════════════════════════════════════════════════════════════
// 🏠 Main Screen with Bottom Navigation
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MainScreen(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onNavigateToMyStories: () -> Unit,
    onNavigateToCreateTextStory: () -> Unit,
    onNavigateToGroupStories: (String, String) -> Unit = { _, _ -> },
    onNavigateToChannelStories: (String, String) -> Unit = { _, _ -> },
    onNavigateToGroupChat: (String) -> Unit,
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToChannelView: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchivedChats: () -> Unit = {},
    onNavigateToUserProfile: (String) -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToElm: () -> Unit = {},
    onLogout: () -> Unit,
    chatListViewModel: ChatListViewModel = hiltViewModel(),
    // notificationViewModel: com.hasani.messageapp.ui.viewmodel.NotificationViewModel = hiltViewModel()
) {
    // Nested NavController for Bottom Navigation
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home.route

    // Handle Back Press to return to Home Tab if on other tabs
    BackHandler(enabled = currentRoute != Routes.Home.route) {
        bottomNavController.navigate(Routes.Home.route) {
            popUpTo(bottomNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Root Container - No Scaffold here to avoid double padding/insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.Bazaar.route) { BazaarScreen() }
            composable(Routes.Home.route) { 
                HomeScreen(
                    onNavigate = { route ->
                        if (route == Routes.Elm.route) {
                            onNavigateToElm()
                        } else {
                            bottomNavController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                ) 
            }
            composable(Routes.Messaging.route) {
                MessagingContent(
                    onNavigateToConversation = onNavigateToConversation,
                    onNavigateToNewChat = onNavigateToNewChat,
                    onNavigateToCreateGroup = onNavigateToCreateGroup,
                    onNavigateToCreateChannel = onNavigateToCreateChannel,
                    onNavigateToMyStories = onNavigateToMyStories,
                    onNavigateToCreateTextStory = onNavigateToCreateTextStory,
                    onNavigateToGroupStories = onNavigateToGroupStories,
                    onNavigateToChannelStories = onNavigateToChannelStories,
                    onNavigateToGroupChat = onNavigateToGroupChat,
                    onNavigateToGroupDetail = onNavigateToGroupDetail,
                    onNavigateToChannelView = onNavigateToChannelView,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToArchivedChats = onNavigateToArchivedChats,
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    onNavigateToNotifications = onNavigateToNotifications,
                    chatListViewModel = chatListViewModel
                )
            }
            composable(Routes.Treasure.route) { TreasureScreen() }
            
            // New Linked Sections
            composable(Routes.Fun.route) { com.hasani.messageapp.ui.screens.fun_zone.FunScreen() }
            composable(Routes.Events.route) { com.hasani.messageapp.ui.screens.events.EventsScreen() }
            composable(Routes.Books.route) { com.hasani.messageapp.ui.screens.books.BooksScreen() }
        }
        
        // Floating Bottom Navigation
        BottomNavBar(
            currentRoute = currentRoute,
            onItemClick = { route ->
                if (route == Routes.Elm.route) {
                    onNavigateToElm()
                } else {
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            unreadMessageCount = 0,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}

// ... NetworkingContent logic remains the same below ...
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingContent(
    onNavigateToConversation: (String) -> Unit,
    onNavigateToNewChat: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onNavigateToMyStories: () -> Unit,
    onNavigateToCreateTextStory: () -> Unit,
    onNavigateToGroupStories: (String, String) -> Unit,
    onNavigateToChannelStories: (String, String) -> Unit,
    onNavigateToGroupChat: (String) -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToChannelView: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArchivedChats: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToNotifications: () -> Unit,
    chatListViewModel: ChatListViewModel
) {
    val extendedColors = MessageAppTheme.extendedColors
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // Use rememberSaveable for tab state
    val tabs = listOf("پیام‌ها", "گروه‌ها", "کانال‌ها", "دوره‌ها")
    
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Course Dialog State
    var showCreateCourseDialog by remember { mutableStateOf(false) }
    var newCourseTitle by remember { mutableStateOf("") }
    var newCourseDescription by remember { mutableStateOf("") }
    val courseViewModel: com.hasani.messageapp.ui.viewmodel.CourseViewModel = hiltViewModel()
    val createCourseState by courseViewModel.createCourseState.collectAsState()

    LaunchedEffect(createCourseState) {
        if (createCourseState is CreateCourseState.Success) {
            showCreateCourseDialog = false
            newCourseTitle = ""
            newCourseDescription = ""
            courseViewModel.resetCreateState()
        }
    }

    // Permission Logic
    val context = LocalContext.current

    Scaffold(
        topBar = {
             Column {
                if (isSearchActive) {
                     TopAppBar(
                        title = {
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("جستجو...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxSize(),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                        },
                        navigationIcon = {
                             IconButton(onClick = { 
                                isSearchActive = false
                                searchQuery = ""
                             }) {
                                 Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
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
                } else {
                    TopAppBar(
                        title = { 
                            Text(
                                text = "پیام رسان", 
                                fontWeight = FontWeight.Bold, 
                                style = MaterialTheme.typography.titleLarge
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateToProfile) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            }
                            if (selectedTabIndex == 1 || selectedTabIndex == 2 || selectedTabIndex == 3) {
                                IconButton(onClick = {
                                    when (selectedTabIndex) {
                                        1 -> onNavigateToCreateGroup()
                                        2 -> onNavigateToCreateChannel()
                                        3 -> showCreateCourseDialog = true
                                    }
                                }) {
                                    Icon(Icons.Default.Add, "New")
                                }
                            } else {
                                IconButton(onClick = onNavigateToNewChat) {
                                    Icon(Icons.Default.Add, "New Chat")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        )
                    )
                }

                 TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = extendedColors.accent,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                             TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = extendedColors.accent
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                         Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title, 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
             }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTabIndex) {
                0 -> {
                    ChatListScreen(
                        onChatClick = { chat ->
                            when(chat.type) {
                                ChatType.PRIVATE -> onNavigateToConversation(chat.id)
                                ChatType.GROUP -> onNavigateToGroupChat(chat.id)
                                ChatType.CHANNEL -> onNavigateToChannelView(chat.id)
                            }
                        },
                        onNewChatClick = onNavigateToNewChat,
                        onProfileClick = onNavigateToProfile,
                        onChatAvatarClick = { chat ->
                             when(chat.type) {
                                ChatType.GROUP -> onNavigateToGroupDetail(chat.id)
                                ChatType.CHANNEL -> onNavigateToChannelView(chat.id)
                                ChatType.PRIVATE -> {
                                    val otherUserId = chat.participants.firstOrNull { it.id != chatListViewModel.state.value.currentUserId }?.id
                                    if (otherUserId != null) onNavigateToUserProfile(otherUserId)
                                }
                            }
                        },
                        onMyStoriesClick = onNavigateToMyStories,
                        onNavigateToCreateTextStory = onNavigateToCreateTextStory,
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        viewModel = chatListViewModel,
                        searchQuery = searchQuery
                    )
                }
                1 -> {
                    GroupListScreen(
                        onGroupClick = onNavigateToGroupChat,
                        onCreateGroupClick = onNavigateToCreateGroup,
                        onNavigateToUserProfile = onNavigateToUserProfile,
                        onNavigateToGroupDetail = onNavigateToGroupDetail,
                        onNavigateToGroupStories = onNavigateToGroupStories,
                        searchQuery = searchQuery
                    )
                }
                2 -> {
                    ChannelListScreen(
                        onChannelClick = onNavigateToChannelView,
                        onCreateChannelClick = onNavigateToCreateChannel,
                        onNavigateToChannelStories = onNavigateToChannelStories,
                        searchQuery = searchQuery
                    )
                }
                3 -> {
                    CourseListScreen(
                        onNavigateToCourse = onNavigateToChannelView,
                        viewModel = courseViewModel
                    )
                }
            }
        }
    }

    if (showCreateCourseDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCourseDialog = false },
            title = { Text("ساخت دوره جدید") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCourseTitle,
                        onValueChange = { newCourseTitle = it },
                        label = { Text("عنوان دوره") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCourseDescription,
                        onValueChange = { newCourseDescription = it },
                        label = { Text("توضیحات") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCourseTitle.isNotBlank()) {
                            courseViewModel.createCourse(newCourseTitle, newCourseDescription)
                        }
                    },
                    enabled = createCourseState !is CreateCourseState.Loading
                ) {
                    if (createCourseState is CreateCourseState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("ساخت دوره")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCourseDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
