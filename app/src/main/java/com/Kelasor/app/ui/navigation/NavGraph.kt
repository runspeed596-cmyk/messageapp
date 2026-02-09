package com.Kelasor.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.Kelasor.app.ui.screens.auth.LoginScreen
import com.Kelasor.app.ui.screens.auth.OtpScreen
import com.Kelasor.app.ui.screens.channel.ChannelListScreen
import com.Kelasor.app.ui.screens.channel.ChannelViewScreen
import com.Kelasor.app.ui.screens.channel.CreateChannelScreen
import com.Kelasor.app.ui.screens.chat.ChatListScreen
import com.Kelasor.app.ui.screens.chat.ConversationScreen
import com.Kelasor.app.ui.screens.chat.NewChatScreen
import com.Kelasor.app.ui.screens.group.CreateGroupScreen
import com.Kelasor.app.ui.screens.group.GroupDetailScreen
import com.Kelasor.app.ui.screens.group.GroupListScreen
import com.Kelasor.app.ui.screens.group.GroupSettingsScreen
import com.Kelasor.app.ui.screens.main.MainScreen
import com.Kelasor.app.ui.screens.profile.EditProfileScreen
import com.Kelasor.app.ui.screens.profile.ProfileScreen
import com.Kelasor.app.ui.screens.profile.SettingsScreen
import com.Kelasor.app.ui.screens.profile.UserProfileScreen
import com.Kelasor.app.ui.screens.splash.SplashScreen

// ═══════════════════════════════════════════════════════════════════════════════
// 🗺️ Main Navigation Graph
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeIn()
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeOut()
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeIn()
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeOut()
        }
    ) {
        // ─────────────────────────────────────────────────────────────────────────
        // Splash Screen
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Authentication Flow
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.Login.route) {
            LoginScreen(
                onNavigateToOtp = { phoneNumber ->
                    navController.navigate(Routes.Otp.createRoute(phoneNumber))
                }
            )
        }
        composable(
            route = Routes.Otp.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToUserInfo = {
                    navController.navigate(Routes.UserInfo.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.UserInfo.route) {
            com.Kelasor.app.ui.screens.auth.UserInfoScreen(
                onNavigateToMain = {
                     navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Main Screen with Bottom Navigation
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.Main.route) {
            MainScreen(
                onNavigateToConversation = { chatId -> navController.navigate("conversation/$chatId") },
                onNavigateToNewChat = { navController.navigate(Routes.NewChat.route) },
                onNavigateToCreateGroup = { navController.navigate(Routes.CreateGroup.route) },
                onNavigateToCreateChannel = { navController.navigate(Routes.CreateChannel.route) },
                onNavigateToMyStories = { navController.navigate(Routes.MyStories.route) },
                onNavigateToCreateTextStory = { navController.navigate(Routes.CreateTextStory.route) },
                onNavigateToGroupStories = { id, name -> 
                    navController.navigate("groupStories/$id/${java.net.URLEncoder.encode(name, "UTF-8")}")
                },
                onNavigateToChannelStories = { id, name -> 
                    navController.navigate("channelStories/$id/${java.net.URLEncoder.encode(name, "UTF-8")}")
                },
                onNavigateToGroupChat = { groupId -> navController.navigate("groupChat/$groupId") },
                onNavigateToChannelView = { channelId -> navController.navigate("channelView/$channelId") },
                onNavigateToProfile = { navController.navigate(Routes.Profile.route) },
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToUserProfile = { userId -> navController.navigate("userProfile/$userId") },
                onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                onNavigateToElm = { navController.navigate(Routes.Elm.route) },
                onNavigateToAllMovies = { navController.navigate(Routes.AllMovies.route) },
                onNavigateToAllMusic = { navController.navigate(Routes.AllMusic.route) },
                onNavigateToAllRiddles = { navController.navigate(Routes.AllRiddles.route) },
                onNavigateToRiddleDetail = { id -> navController.navigate(Routes.RiddleDetail.createRoute(id)) },
                onPlayVideo = { url -> 
                    navController.navigate(Routes.FullScreenVideo.createRoute(url))
                },
                onLogout = { 
                    // context.getSharedPreferences("auth", android.content.Context.MODE_PRIVATE)
                    //     .edit().clear().apply()
                    navController.navigate(Routes.Login.route) { // Changed from Routes.Auth.route to Routes.Login.route
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Notifications Screen
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.Notifications.route) {
            com.Kelasor.app.ui.screens.notification.NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Follow List Screen (Followers / Following)
        // ─────────────────────────────────────────────────────────────────────────
        composable(
            route = Routes.FollowList.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("tab") { 
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            com.Kelasor.app.ui.screens.social.FollowListScreen(
                userId = userId,
                initialTab = tab,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { targetUserId ->
                    navController.navigate(Routes.UserProfile.createRoute(targetUserId))
                }
            )
        }
        composable(Routes.Profile.route) {
            com.Kelasor.app.ui.screens.profile.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Routes.EditProfile.route) },
                onSavedMessagesClick = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId))
                },
                onArchivedChatsClick = { navController.navigate(Routes.ArchivedChats.route) },
                onLogoutClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Chat Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(
            route = Routes.Conversation.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ConversationScreen(
                chatId = chatId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId, fromChat = true))
                }
            )
        }
        composable(Routes.NewChat.route) {
            NewChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartChat = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId)) {
                        popUpTo(Routes.NewChat.route) { inclusive = true }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Group Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.CreateGroup.route) {
            CreateGroupScreen(
                onNavigateBack = { navController.popBackStack() },
                onGroupCreated = { groupId ->
                    navController.navigate(Routes.GroupConversation.createRoute(groupId)) {
                        popUpTo(Routes.CreateGroup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.GroupConversation.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            com.Kelasor.app.ui.screens.group.GroupConversationScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupDetail = { id ->
                    navController.navigate(Routes.GroupDetail.createRoute(id))
                },
                onNavigateToGroupSettings = { id ->
                    navController.navigate(Routes.GroupSettings.createRoute(id))
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                }
            )
        }
        composable(
            route = Routes.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupSettings = {
                    navController.navigate(Routes.GroupSettings.createRoute(groupId))
                },
                onGroupDeleted = {
                    navController.popBackStack(Routes.Main.route, inclusive = false)
                }
            )
        }
        composable(
            route = Routes.GroupSettings.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupSettingsScreen(
                groupId = groupId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId -> 
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                },
                onGroupDeleted = {
                    navController.popBackStack(Routes.Main.route, inclusive = false)
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Channel Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.CreateChannel.route) {
            CreateChannelScreen(
                onNavigateBack = { navController.popBackStack() },
                onChannelCreated = { channelId ->
                    navController.navigate(Routes.ChannelView.createRoute(channelId)) {
                        popUpTo(Routes.CreateChannel.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.ChannelView.route,
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            ChannelViewScreen(
                channelId = channelId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChannelSettings = {
                    navController.navigate(Routes.ChannelSettings.createRoute(channelId))
                }
            )
        }
        composable(
            route = Routes.ChannelSettings.route,
            arguments = listOf(navArgument("channelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            com.Kelasor.app.ui.screens.channel.ChannelSettingsScreen(
                channelId = channelId,
                onNavigateBack = { navController.popBackStack() },
                onChannelDeleted = {
                    navController.popBackStack(Routes.Main.route, inclusive = false)
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Profile Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Settings.route) {
            com.Kelasor.app.ui.screens.profile.SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Routes.EditProfile.route) },
                onSavedMessagesClick = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId))
                },
                onArchivedChatsClick = { navController.navigate(Routes.ArchivedChats.route) },
                onLogoutClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ArchivedChats.route) {
            com.Kelasor.app.ui.screens.chat.ArchivedChatsScreen(
                onNavigateBack = { navController.popBackStack() },
                onChatClick = { chat ->
                    navController.navigate(Routes.Conversation.createRoute(chat.id))
                }
            )
        }
        composable(
            route = Routes.UserProfile.route + "/{userId}?fromChat={fromChat}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("fromChat") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val fromChat = backStackEntry.arguments?.getBoolean("fromChat") ?: false
            UserProfileScreen(
                userId = userId,
                fromChat = fromChat,
                onNavigateBack = { navController.popBackStack() },
                onStartChat = { targetUserId ->
                    navController.navigate(Routes.Conversation.createRoute(targetUserId)) {
                        popUpTo(Routes.UserProfile.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToFollowList = { targetUserId, tab ->
                    navController.navigate(Routes.FollowList.createRoute(targetUserId, tab))
                },
                onNavigateToChannel = { channelId ->
                    navController.navigate(Routes.ChannelView.createRoute(channelId))
                }
            )
        }
        composable(
            route = "invite/{code}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "messageapp://invite/{code}" }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            // Explicitly specify the ViewModel type to ensure type inference works for state collection
            val viewModel: com.Kelasor.app.ui.viewmodel.GroupListViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            LaunchedEffect(code) {
                if (code.isNotEmpty()) {
                    viewModel.joinGroup(code)
                }
            }
            
            // Ephemeral UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.error != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error: ${state.error}", 
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { navController.navigate(Routes.Main.route) }) {
                            Text("Go to Home")
                        }
                    }
                } else {
                     LaunchedEffect(state.isLoading) {
                         if (!state.isLoading && state.error == null && code.isNotEmpty()) {
                             navController.navigate(Routes.Main.route) {
                                 popUpTo("invite/{code}") { inclusive = true }
                             }
                         }
                     }
                     Text("Joining Group...")
                }
            }
        }
        composable(Routes.MyStories.route) {
            com.Kelasor.app.ui.screens.story.MyStoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateTextStory = { navController.navigate(Routes.CreateTextStory.route) },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                }
            )
        }
        composable(Routes.CreateTextStory.route) {
            com.Kelasor.app.ui.screens.story.CreateTextStoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Group Stories Manager
        composable(
            route = Routes.GroupStories.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val groupName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("groupName") ?: "", "UTF-8"
            )
            com.Kelasor.app.ui.screens.story.GroupStoriesManagerScreen(
                groupId = groupId,
                groupName = groupName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateTextStory = { navController.navigate(Routes.CreateTextStory.route) }
            )
        }
        // Channel Stories Manager
        composable(Routes.ChannelStories.route,
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            val channelName = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("channelName") ?: "", "UTF-8"
            )
            com.Kelasor.app.ui.screens.story.ChannelStoriesManagerScreen(
                channelId = channelId,
                channelName = channelName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateTextStory = { navController.navigate(Routes.CreateTextStory.route) }
            )
        }

        // Jahan Elm (Standalone Route for High Performance)
        composable(Routes.Elm.route) {
            com.Kelasor.app.ui.screens.elm.ElmScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─────────────────────────────────────────────────────────────────────────
        // Entertainment View All Screens
        composable(Routes.AllMovies.route) {
            com.Kelasor.app.ui.screens.fun_zone.AllMoviesScreen(
                onNavigateBack = { navController.popBackStack() },
                onPlayVideo = { url -> 
                    navController.navigate(Routes.FullScreenVideo.createRoute(url))
                }
            )
        }
        composable(Routes.AllMusic.route) {
            com.Kelasor.app.ui.screens.fun_zone.AllMusicScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.AllRiddles.route) {
            com.Kelasor.app.ui.screens.fun_zone.AllRiddlesScreen(
                onRiddleClick = { riddle -> 
                    navController.navigate(Routes.RiddleDetail.createRoute(riddle.id))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.RiddleDetail.route,
            arguments = listOf(navArgument("riddleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val riddleId = backStackEntry.arguments?.getString("riddleId") ?: ""
            com.Kelasor.app.ui.screens.fun_zone.RiddleDetailScreen(
                riddleId = riddleId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Full Screen Video Player
        composable(
            route = Routes.FullScreenVideo.route,
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoUrl = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("videoUrl") ?: "", "UTF-8"
            )
            com.Kelasor.app.ui.screens.player.FullScreenVideoScreen(
                videoUrl = videoUrl,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
