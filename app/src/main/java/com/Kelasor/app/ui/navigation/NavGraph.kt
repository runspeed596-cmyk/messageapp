package com.Kelasor.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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

import com.Kelasor.app.ui.screens.profile.UserProfileScreen
import com.Kelasor.app.ui.screens.profile.ComingSoonScreen
import com.Kelasor.app.ui.screens.splash.SplashScreen
import com.Kelasor.app.ui.screens.forward.ForwardTargetScreen
import com.Kelasor.app.ui.screens.mosbat_elm.OrganizerSetupScreen
import com.Kelasor.app.ui.screens.mosbat_elm.AcademyProfileSetupScreen

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
                animationSpec = tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                targetOffset = { it / 4 } // Parallax effect
            ) + fadeOut(animationSpec = tween(300), targetAlpha = 0.8f)
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                ),
                initialOffset = { it / 4 } // Parallax effect
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                )
            ) + fadeOut(animationSpec = tween(300), targetAlpha = 0.8f)
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
                onNavigateToUserInfo = {
                    navController.navigate(Routes.UserInfo.route) {
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
                onNavigateToOrganizerSetup = { navController.navigate(Routes.OrganizerSetup.route) },
                onNavigateToCreateCourse = { navController.navigate(Routes.CreateCourse.route) },
                onNavigateToEditCourse = { courseId -> navController.navigate(Routes.EditCourse.createRoute(courseId)) },
                onNavigateToAcademyProfile = { id -> navController.navigate(Routes.AcademyProfile.createRoute(id)) },
                onNavigateToMyStories = { navController.navigate(Routes.MyStories.route) },
                onNavigateToCreateTextStory = { navController.navigate(Routes.CreateTextStory.route) },
                onNavigateToGroupStories = { id, name -> 
                    navController.navigate("groupStories/$id/${java.net.URLEncoder.encode(name, "UTF-8")}")
                },
                onNavigateToChannelStories = { id, name -> 
                    navController.navigate("channelStories/$id/${java.net.URLEncoder.encode(name, "UTF-8")}")
                },
                onNavigateToGroupChat = { groupId -> navController.navigate(Routes.GroupConversation.createRoute(groupId)) },
                onNavigateToGroupDetail = { groupId -> navController.navigate(Routes.GroupDetail.createRoute(groupId)) },
                onNavigateToChannelView = { channelId -> navController.navigate("channelView/$channelId") },
                onNavigateToProfile = { tab -> 
                    if (tab == 0) {
                        navController.navigate(Routes.MessengerSettings.route)
                    } else {
                        navController.navigate(Routes.MosbatElmSettings.route)
                    }
                },
                onNavigateToSettings = { navController.navigate(Routes.GlobalSettings.route) },
                onNavigateToUserProfile = { userId -> navController.navigate(Routes.UserProfile.createRoute(userId)) },
                onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) },
                onNavigateToMosbatElmNotifications = { navController.navigate(Routes.MosbatElmNotifications.route) },
                onNavigateToElm = { navController.navigate(Routes.Elm.route) },
                onNavigateToAllMovies = { navController.navigate(Routes.AllMovies.route) },
                onNavigateToAllMusic = { navController.navigate(Routes.AllMusic.route) },
                onNavigateToAllRiddles = { navController.navigate(Routes.AllRiddles.route) },
                onNavigateToRiddleDetail = { id -> navController.navigate(Routes.RiddleDetail.createRoute(id)) },
                onPlayVideo = { url -> 
                    navController.navigate(Routes.FullScreenVideo.createRoute(url))
                },
                onNavigateToAiBotList = { navController.navigate(Routes.AiBotList.route) },
                onNavigateToEditProfile = { navController.navigate(Routes.EditProfile.route) },
                onNavigateToTeacherProfile = { teacherId -> navController.navigate(Routes.TeacherPublicProfile.createRoute(teacherId)) },
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

        // ─────────────────────────────────────────────────────────────────────────
        // Chat Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(
            route = Routes.Conversation.route + "?messageId={messageId}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("messageId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null 
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(durationMillis = 300),
                    targetOffset = { fullOffset -> fullOffset / 4 }
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 300),
                    initialOffset = { fullOffset -> fullOffset / 4 }
                ) + fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val messageId = backStackEntry.arguments?.getString("messageId")
            ConversationScreen(
                chatId = chatId,
                initialMessageId = messageId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId, fromChat = true))
                },
                onNavigateToForward = { messageIds, sourceType, sourceId ->
                    navController.navigate(Routes.ForwardTarget.createRoute(messageIds, sourceType, sourceId))
                },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
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
                },
                onNavigateToCreateGroup = { navController.navigate(Routes.CreateGroup.route) },
                onNavigateToCreateChannel = { navController.navigate(Routes.CreateChannel.route) },
                onNavigateToCreateCourse = { navController.navigate(Routes.CreateCourse.route) }
            )
        }
        composable(Routes.CreateCourse.route) {
            com.Kelasor.app.ui.screens.course.CreateCourseScreen(
                onNavigateBack = { navController.popBackStack() },
                onCourseCreated = { _ ->
                    navController.navigate(Routes.MyCourses.route) {
                        popUpTo(Routes.CreateCourse.route) { inclusive = true }
                    }
                },
                onNavigateToEditAcademyProfile = {
                    navController.navigate(Routes.OrganizerSetup.route)
                }
            )
        }
        composable(
            route = Routes.EditCourse.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            com.Kelasor.app.ui.screens.course.CreateCourseScreen(
                editCourseId = courseId,
                onNavigateBack = { navController.popBackStack() },
                onCourseCreated = { _ ->
                    navController.popBackStack()
                },
                onNavigateToEditAcademyProfile = {
                    navController.navigate(Routes.OrganizerSetup.route)
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
            route = Routes.GroupConversation.route + "?messageId={messageId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("messageId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(280), initialAlpha = 0.3f)
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = 0.86f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    targetOffset = { fullOffset -> fullOffset / 5 }
                ) + fadeOut(animationSpec = tween(150), targetAlpha = 0.5f)
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffset = { fullOffset -> fullOffset / 5 }
                ) + fadeIn(animationSpec = tween(180))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(animationSpec = tween(150), targetAlpha = 0.5f)
            }
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            val messageId = backStackEntry.arguments?.getString("messageId")
            com.Kelasor.app.ui.screens.group.GroupConversationScreen(
                groupId = groupId,
                initialMessageId = messageId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupDetail = { id ->
                    navController.navigate(Routes.GroupDetail.createRoute(id))
                },
                onNavigateToGroupSettings = { id ->
                    navController.navigate(Routes.GroupSettings.createRoute(id))
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                },
                onNavigateToForward = { messageIds, sourceType, sourceId ->
                    navController.navigate(Routes.ForwardTarget.createRoute(messageIds, sourceType, sourceId))
                },
                onNavigateToExamCreation = {
                    navController.navigate(Routes.ExamCreation.createRoute(null))
                },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
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
                },
                onNavigateToMessage = { gId, msgId ->
                    navController.navigate(Routes.GroupConversation.createRoute(gId, msgId)) {
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
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
        composable(Routes.OrganizerSetup.route) {
            OrganizerSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAcademyProfileSetup = {
                    navController.navigate(Routes.AcademyProfileSetup.route)
                }
            )
        }
        composable(Routes.AcademyProfileSetup.route) {
            AcademyProfileSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onFinish = {
                    navController.popBackStack(Routes.Main.route, inclusive = false)
                }
            )
        }
        composable(
            route = Routes.ChannelView.route + "?messageId={messageId}",
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType },
                navArgument("messageId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(280), initialAlpha = 0.3f)
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = spring(
                        dampingRatio = 0.86f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    targetOffset = { fullOffset -> fullOffset / 5 }
                ) + fadeOut(animationSpec = tween(150), targetAlpha = 0.5f)
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialOffset = { fullOffset -> fullOffset / 5 }
                ) + fadeIn(animationSpec = tween(180))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = spring(
                        dampingRatio = 0.82f,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeOut(animationSpec = tween(150), targetAlpha = 0.5f)
            }
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            val messageId = backStackEntry.arguments?.getString("messageId")
            ChannelViewScreen(
                channelId = channelId,
                initialMessageId = messageId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChannelSettings = {
                    navController.navigate(Routes.ChannelSettings.createRoute(channelId))
                },
                onNavigateToExamCreation = {
                    navController.navigate(Routes.ExamCreation.createRoute(channelId))
                },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
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
                },
                onNavigateToMessage = { cId, msgId ->
                    navController.navigate(Routes.ChannelView.createRoute(cId, msgId)) {
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Forward Target Selection Screen
        // ─────────────────────────────────────────────────────────────────────────
        composable(
            route = Routes.ForwardTarget.route,
            arguments = listOf(
                navArgument("messageIds") { type = NavType.StringType },
                navArgument("sourceType") { type = NavType.StringType; defaultValue = "CHAT" },
                navArgument("sourceId") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            ForwardTargetScreen(
                onBackPress = { navController.popBackStack() }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // AI Bot Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.AiBotList.route) {
            com.Kelasor.app.ui.screens.special.AiBotListScreen(
                onNavigateBack = { navController.popBackStack() },
                onBotClick = { botId, botName, botType ->
                    navController.navigate(Routes.AiBotChat.createRoute(botId, botName, botType))
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Routes.Conversation.createRoute(chatId))
                }
            )
        }
        composable(
            route = Routes.AiBotChat.route,
            arguments = listOf(
                navArgument("botId") { type = NavType.StringType },
                navArgument("botName") { type = NavType.StringType },
                navArgument("botType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val botId: String = backStackEntry.arguments?.getString("botId") ?: ""
            val botName: String = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("botName") ?: "", "UTF-8"
            )
            val botType: String = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("botType") ?: "", "UTF-8"
            )
            com.Kelasor.app.ui.screens.special.AiBotChatScreen(
                botId = botId,
                botName = botName,
                botType = botType,
                onNavigateBack = { navController.popBackStack() },
                onActionClick = { actionUrl ->
                    if (actionUrl.startsWith("course_details/")) {
                        val cId = actionUrl.substringAfter("course_details/")
                        navController.navigate(com.Kelasor.app.ui.navigation.Routes.CourseDetail.createRoute(cId))
                    }
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
        composable(Routes.GlobalSettings.route) {
            com.Kelasor.app.ui.screens.settings.GlobalSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onWalletClick = { navController.navigate(Routes.Wallet.route) },
                onMessengerSettingsClick = { navController.navigate(Routes.MessengerSettings.route) },
                onMosbatElmSettingsClick = { navController.navigate(Routes.MosbatElmSettings.route) },
                onNotificationsClick = { navController.navigate(Routes.SettingsNotifications.route) },
                onSupportClick = { navController.navigate(Routes.AiBotList.route) },
                onFeedbackClick = { navController.navigate(Routes.Feedback.route) },
                onAboutUsClick = { navController.navigate(Routes.ComingSoon.createRoute("درباره ما")) },
                onEditProfileClick = { navController.navigate(Routes.EditProfile.route) },
                onAddAccountClick = { navController.navigate(Routes.Login.route) },
                onLogoutSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Feedback.route) {
            com.Kelasor.app.ui.screens.settings.FeedbackScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MessengerSettings.route) {
            com.Kelasor.app.ui.screens.settings.MessengerSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditProfileClick = { navController.navigate(Routes.EditProfile.route) },
                onAccountClick = { navController.navigate(Routes.EditProfile.route) },
                onAppearanceClick = { navController.navigate(Routes.SettingsChat.route) },
                onPrivacyClick = { navController.navigate(Routes.SettingsPrivacy.route) },
                onNotificationsClick = { navController.navigate(Routes.SettingsNotifications.route) },
                onDataStorageClick = { navController.navigate(Routes.SettingsDataStorage.route) },
                onDevicesClick = { navController.navigate(Routes.SettingsDevices.route) },
                onLogoutClick = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MosbatElmSettings.route) {
            com.Kelasor.app.ui.screens.settings.MosbatElmSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditAcademyProfileClick = { navController.navigate(Routes.OrganizerSetup.route) },
                onAcademyProfileClick = { institutionId -> 
                    navController.navigate(Routes.AcademyProfile.createRoute(institutionId))
                },
                onAcademyAnalyticsClick = { institutionId -> 
                    navController.navigate(Routes.AcademyAnalytics.createRoute(institutionId))
                },
                onMyCoursesClick = { navController.navigate(Routes.MyCourses.route) },
                onCollaborationsClick = { navController.navigate(Routes.Collaborations.route) },
                onMosbatElmNotificationsClick = { navController.navigate(Routes.MosbatElmNotifications.route) },
                onPurchasedCoursesClick = { navController.navigate(Routes.MyCourses.route) },
                onCertificatesClick = { navController.navigate(Routes.ComingSoon.createRoute("مدرک‌های دریافت شده")) },
                onLikedPostsClick = { navController.navigate(Routes.LikedPosts.route) }
            )
        }
        composable(
            route = Routes.AcademyAnalytics.route,
            arguments = listOf(navArgument("institutionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.Kelasor.app.ui.screens.mosbat_elm.AcademyAnalyticsScreen(
                institutionId = institutionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
                }
            )
        }
        composable(
            route = Routes.AcademyProfile.route,
            arguments = listOf(navArgument("institutionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val institutionId = backStackEntry.arguments?.getString("institutionId") ?: ""
            com.Kelasor.app.ui.screens.mosbat_elm.AcademyProfileScreen(
                institutionId = institutionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
                },
                onNavigateToEditAcademyProfile = {
                    navController.navigate(Routes.OrganizerSetup.route)
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId))
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                },
                onNavigateToEditCourse = { courseId ->
                    navController.navigate(Routes.EditCourse.createRoute(courseId))
                },
                onNavigateToChannel = { channelId ->
                    navController.navigate(Routes.ChannelView.createRoute(channelId))
                }
            )
        }
        composable(
            route = Routes.TeacherPublicProfile.route,
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""
            com.Kelasor.app.ui.screens.mosbat_elm.TeacherPublicProfileScreen(
                teacherId = teacherId,
                onBack = { navController.popBackStack() },
                onNavigateToCourseDetail = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId))
                },
                onNavigateToChannel = { channelId ->
                    navController.navigate(Routes.ChannelView.createRoute(channelId))
                }
            )
        }
        composable(
            route = Routes.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            com.Kelasor.app.ui.screens.mosbat_elm.CourseDetailScreen(
                courseId = courseId,
                onBack = { navController.popBackStack() },
                onInstructorClick = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                },
                onOrganizerClick = { institutionId ->
                    navController.navigate(Routes.AcademyProfile.createRoute(institutionId))
                },
                onNavigateToChat = { userId ->
                    navController.navigate(Routes.Conversation.createRoute(userId))
                },
                onNavigateToEditCourse = { cId ->
                    navController.navigate(Routes.EditCourse.createRoute(cId))
                },
                onNavigateToCourseDetail = { cId ->
                    navController.navigate(Routes.CourseDetail.createRoute(cId))
                },
                onChannelClick = { channelId ->
                    navController.navigate(Routes.ChannelView.createRoute(channelId))
                }
            )
        }
        // EditCourse Route
        composable(
            route = Routes.EditCourse.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val editCourseId: String = backStackEntry.arguments?.getString("courseId") ?: ""
            com.Kelasor.app.ui.screens.course.CreateCourseScreen(
                editCourseId = editCourseId,
                onNavigateBack = { navController.popBackStack() },
                onCourseCreated = { _ ->
                    navController.popBackStack()
                },
                onNavigateToEditAcademyProfile = {
                    navController.navigate(Routes.OrganizerSetup.route)
                }
            )
        }
        
        // ── Settings Sub-Pages ───────────────────────────────────────────


        composable(Routes.SettingsLanguage.route) {
            ComingSoonScreen(title = "Language", onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.Wallet.route) {
            com.Kelasor.app.ui.screens.wallet.WalletScreen(onNavigateBack = { navController.popBackStack() })
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
                onStartChat = { targetUserId, messageId ->
                    navController.navigate(Routes.Conversation.createRoute(targetUserId, messageId)) {
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
        // ─────────────────────────────────────────────────────────────────────────
        // Exam Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(
            route = Routes.ExamCreation.route,
            arguments = listOf(
                navArgument("channelId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId")
            com.Kelasor.app.ui.screens.exam.ExamCreationScreen(
                channelId = channelId,
                onNavigateBack = { navController.popBackStack() },
                onExamCreated = { examId ->
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Routes.ExamTaking.route,
            arguments = listOf(navArgument("examId") { type = NavType.StringType })
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            com.Kelasor.app.ui.screens.exam.ExamTakingScreen(
                examId = examId,
                onNavigateBack = { navController.popBackStack() },
                onExamSubmitted = { navController.popBackStack() }
            )
        }
        composable(Routes.ExamHistory.route) {
            com.Kelasor.app.ui.screens.exam.ExamHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onExamClick = { examId ->
                    navController.navigate(Routes.ExamTaking.createRoute(examId))
                }
            )
        }
        composable(Routes.LikedPosts.route) {
            com.Kelasor.app.ui.screens.mosbat_elm.LikedPostsScreen(
                onNavigateBack = { navController.popBackStack() },
                onCourseClick = { id -> navController.navigate(Routes.CourseDetail.createRoute(id)) }
            )
        }
        composable(Routes.MyCourses.route) {
            com.Kelasor.app.ui.screens.mosbat_elm.MyCoursesScreen(
                onNavigateBack = { navController.popBackStack() },
                onCourseClick = { courseId ->
                    navController.navigate(Routes.CourseDetail.createRoute(courseId))
                },
                onEditCourseClick = { courseId ->
                    navController.navigate(Routes.EditCourse.createRoute(courseId))
                },
                onCreateCourseClick = {
                    navController.navigate(Routes.CreateCourse.route)
                }
            )
        }
        // ─────────────────────────────────────────────────────────────────────────
        // Settings Sub-Screens
        // ─────────────────────────────────────────────────────────────────────────
        composable(Routes.SettingsChat.route) {
            com.Kelasor.app.ui.screens.settings.ChatSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SettingsPrivacy.route) {
            com.Kelasor.app.ui.screens.settings.PrivacySecurityScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBlockedUsers = { navController.navigate(Routes.BlockedUsers.route) },
                onNavigateToPrivacyExceptions = { type ->
                    navController.navigate(Routes.PrivacyExceptions.createRoute(type))
                }
            )
        }
        composable(Routes.BlockedUsers.route) {
            com.Kelasor.app.ui.screens.settings.BlockedUsersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddBlock = { navController.navigate(Routes.PrivacyExceptions.createRoute("blocked_users")) }
            )
        }
        composable(
            route = Routes.PrivacyExceptions.route,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            com.Kelasor.app.ui.screens.settings.PrivacyExceptionsScreen(
                type = type,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.ComingSoon.route,
            arguments = listOf(navArgument("feature") { type = NavType.StringType })
        ) { backStackEntry ->
            val feature = backStackEntry.arguments?.getString("feature") ?: ""
            com.Kelasor.app.ui.screens.profile.ComingSoonScreen(
                title = feature,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SettingsNotifications.route) {
            com.Kelasor.app.ui.screens.settings.NotificationSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SettingsDataStorage.route) {
            com.Kelasor.app.ui.screens.settings.DataStorageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SettingsDevices.route) {
            com.Kelasor.app.ui.screens.settings.DevicesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.MosbatElmNotifications.route) {
            com.Kelasor.app.ui.screens.mosbat_elm.MosbatElmNotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAcademyProfile = { academyId ->
                    navController.navigate(Routes.AcademyProfile.createRoute(academyId))
                }
            )
        }
        composable(Routes.Collaborations.route) {
            com.Kelasor.app.ui.screens.mosbat_elm.CollaborationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
