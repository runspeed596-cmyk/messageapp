package com.Kelasor.app.ui.screens.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.Kelasor.app.domain.model.ChatType
import com.Kelasor.app.ui.navigation.BottomNavBar
import com.Kelasor.app.ui.navigation.Routes
import com.Kelasor.app.ui.screens.bazaar.BazaarScreen
import com.Kelasor.app.ui.screens.channel.ChannelListScreen
import com.Kelasor.app.ui.screens.chat.ChatListScreen
import com.Kelasor.app.ui.screens.course.CourseListScreen
import com.Kelasor.app.ui.screens.group.GroupListScreen
import com.Kelasor.app.ui.screens.home.HomeScreen
import com.Kelasor.app.ui.screens.treasure.TreasureScreen
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ChatListViewModel

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
    onNavigateToAllMovies: () -> Unit = {},
    onNavigateToAllMusic: () -> Unit = {},
    onNavigateToAllRiddles: () -> Unit = {},
    onNavigateToRiddleDetail: (String) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    onLogout: () -> Unit,
    chatListViewModel: ChatListViewModel = hiltViewModel(),
    // notificationViewModel: com.Kelasor.app.ui.viewmodel.NotificationViewModel = hiltViewModel()
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
            composable(Routes.Fun.route) { 
                com.Kelasor.app.ui.screens.fun_zone.FunScreen(
                    onNavigateBack = { bottomNavController.popBackStack() },
                    onNavigateToAllMovies = onNavigateToAllMovies,
                    onNavigateToAllMusic = onNavigateToAllMusic,
                    onNavigateToAllRiddles = onNavigateToAllRiddles,
                    onNavigateToRiddleDetail = onNavigateToRiddleDetail,
                    onPlayVideo = onPlayVideo
                ) 
            }
            composable(Routes.Events.route) { 
                com.Kelasor.app.ui.screens.events.EventsScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                ) 
            }
            composable(Routes.Books.route) { 
                com.Kelasor.app.ui.screens.books.BooksScreen(
                    onNavigateBack = { bottomNavController.popBackStack() }
                ) 
            }
        }
        
        // Floating Bottom Navigation with Backdrop - Hidden in Messaging section
        val showBottomNav = currentRoute != Routes.Messaging.route
        androidx.compose.animation.AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter),
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it }
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it }
            ) + androidx.compose.animation.fadeOut()
        ) {
            Column {
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
                    unreadMessageCount = 0
                )

                com.Kelasor.app.ui.components.SystemBarBackdrop()
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Telegram-Style Messaging Content
// ═══════════════════════════════════════════════════════════════════════════════

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
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("پیام‌ها", "گروه‌ها", "کانال‌ها", "دوره‌ها")
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val courseViewModel: com.Kelasor.app.ui.viewmodel.CourseViewModel = hiltViewModel()
    // Story row expand/collapse state — driven by search activity
    val isStoryRowExpanded = !isSearchActive && searchQuery.isEmpty()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size }
    )
    // Sync tab clicks → Pager (only when user taps a tab, not during swipe)
    LaunchedEffect(selectedTabIndex) {
        if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    // Sync Pager swipes → Tab (only after pager settles to avoid mid-swipe race)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (selectedTabIndex != page) {
                selectedTabIndex = page
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Custom Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = androidx.compose.foundation.layout.WindowInsets
                        .statusBars
                        .asPaddingValues()
                        .calculateTopPadding())
            ) {
                // Row 1: Profile + Title + Search Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "پیام رسان",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { isSearchActive = !isSearchActive }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Row 2: Inline Search Bar (animated)
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(
                        expandFrom = Alignment.Top
                    ),
                    exit = fadeOut() + androidx.compose.animation.shrinkVertically(
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                                        )
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "جستجو...",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        innerTextField()
                                    }
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                Icons.Default.Close,
                                                "Clear",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Row 3: Glassmorphism Tab Bar
                com.Kelasor.app.ui.components.GlassmorphismTabBar(
                    tabs = tabs,
                    selectedIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it }
                )
            }

            // ── Pager Content ──
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                key = { it }
            ) { page ->
                when (page) {
                    0 -> {
                        ChatListScreen(
                            onChatClick = { chat ->
                                when (chat.type) {
                                    ChatType.PRIVATE -> onNavigateToConversation(chat.id)
                                    ChatType.GROUP -> onNavigateToGroupChat(chat.id)
                                    ChatType.CHANNEL -> onNavigateToChannelView(chat.id)
                                }
                            },
                            onNewChatClick = onNavigateToNewChat,
                            onProfileClick = onNavigateToProfile,
                            onChatAvatarClick = { chat ->
                                when (chat.type) {
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

        // FAB
        if (!isSearchActive) {
            val navBarBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            androidx.compose.material3.FloatingActionButton(
                onClick = onNavigateToNewChat,
                containerColor = extendedColors.accent,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp + navBarBottomPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New"
                )
            }
        }
    }
}
