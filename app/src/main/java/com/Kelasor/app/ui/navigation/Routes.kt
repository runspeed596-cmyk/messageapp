package com.Kelasor.app.ui.navigation

// ═══════════════════════════════════════════════════════════════════════════════
// 🗺️ Screen Routes Definition
// ═══════════════════════════════════════════════════════════════════════════════

sealed class Routes(val route: String) {
    // ─────────────────────────────────────────────────────────────────────────────
    // Splash & Authentication
    // ─────────────────────────────────────────────────────────────────────────────
    data object Splash : Routes("splash")
    data object Login : Routes("login")
    data object Register : Routes("register")
    data object Otp : Routes("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String): String = "otp/$phoneNumber"
    }
    data object UserInfo : Routes("userInfo")
    // ─────────────────────────────────────────────────────────────────────────────
    // Main Navigation
    // ─────────────────────────────────────────────────────────────────────────────
    data object Main : Routes("main")
    // ─────────────────────────────────────────────────────────────────────────────
    // Bottom Navigation Tabs
    // ─────────────────────────────────────────────────────────────────────────────
    data object Bazaar : Routes("bazaar")
    data object Elm : Routes("elm")
    data object Home : Routes("home")
    data object Messaging : Routes("messaging") // Was ChatList/Main logically
    data object Treasure : Routes("treasure")

    data object ChatList : Routes("chatList")
    data object Groups : Routes("groups")
    data object Channels : Routes("channels")
    data object Profile : Routes("profile")
    
    // ─────────────────────────────────────────────────────────────────────────────
    // Home Ecosystem Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object Fun : Routes("fun")
    data object Events : Routes("events")
    data object Books : Routes("books")
    data object AllMovies : Routes("allMovies")
    data object AllMusic : Routes("allMusic")
    data object AllRiddles : Routes("allRiddles")
    data object RiddleDetail : Routes("riddleDetail/{riddleId}") {
        fun createRoute(riddleId: String) = "riddleDetail/$riddleId"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Chat Screens
    // ─────────────────────────────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────────────────
    // Chat Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object Conversation : Routes("conversation/{chatId}") {
        fun createRoute(chatId: String, messageId: String? = null): String {
            return if (messageId != null) "conversation/$chatId?messageId=$messageId" else "conversation/$chatId"
        }
    }
    data object NewChat : Routes("newChat")
    data object ChatInfo : Routes("chatInfo/{chatId}") {
        fun createRoute(chatId: String): String = "chatInfo/$chatId"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Group Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object CreateGroup : Routes("createGroup")
    data object GroupDetail : Routes("groupDetail/{groupId}") {
        fun createRoute(groupId: String): String = "groupDetail/$groupId"
    }
    data object GroupConversation : Routes("groupConversation/{groupId}") {
        fun createRoute(groupId: String, messageId: String? = null): String {
             return if (messageId != null) "groupConversation/$groupId?messageId=$messageId" else "groupConversation/$groupId"
        }
    }
    data object GroupSettings : Routes("groupSettings/{groupId}") {
        fun createRoute(groupId: String): String = "groupSettings/$groupId"
    }
    data object AddGroupMembers : Routes("addGroupMembers/{groupId}") {
        fun createRoute(groupId: String): String = "addGroupMembers/$groupId"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Channel Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object CreateChannel : Routes("createChannel")
    data object CreateCourse : Routes("createCourse")
    data object ChannelView : Routes("channelView/{channelId}") {
        fun createRoute(channelId: String, messageId: String? = null): String {
            return if (messageId != null) "channelView/$channelId?messageId=$messageId" else "channelView/$channelId"
        }
    }
    data object ChannelSettings : Routes("channelSettings/{channelId}") {
        fun createRoute(channelId: String): String = "channelSettings/$channelId"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Profile Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object EditProfile : Routes("editProfile")
    data object Settings : Routes("settings")
    data object ArchivedChats : Routes("archivedChats")
    data    object UserProfile : Routes("user_profile") {
        fun createRoute(userId: String, fromChat: Boolean = false) = "$route/$userId?fromChat=$fromChat"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Media & Files
    // ─────────────────────────────────────────────────────────────────────────────
    data object MediaViewer : Routes("mediaViewer/{mediaUrl}") {
        fun createRoute(mediaUrl: String): String = "mediaViewer/${java.net.URLEncoder.encode(mediaUrl, "UTF-8")}"
    }
    data object SharedMedia : Routes("sharedMedia/{chatId}") {
        fun createRoute(chatId: String): String = "sharedMedia/$chatId"
    }
    
    // ─────────────────────────────────────────────────────────────────────────────
    // Stories
    // ─────────────────────────────────────────────────────────────────────────────
    data object MyStories : Routes("myStories")
    data object GroupStories : Routes("groupStories/{groupId}/{groupName}") {
        fun createRoute(groupId: String, groupName: String): String = 
            "groupStories/$groupId/${java.net.URLEncoder.encode(groupName, "UTF-8")}"
    }
    data object ChannelStories : Routes("channelStories/{channelId}/{channelName}") {
        fun createRoute(channelId: String, channelName: String): String = 
            "channelStories/$channelId/${java.net.URLEncoder.encode(channelName, "UTF-8")}"
    }
    data object CreateTextStory : Routes("createTextStory")
    
    // ─────────────────────────────────────────────────────────────────────────────
    // Player
    // ─────────────────────────────────────────────────────────────────────────────
    data object FullScreenVideo : Routes("fullScreenVideo/{videoUrl}") {
        fun createRoute(videoUrl: String): String = 
            "fullScreenVideo/${java.net.URLEncoder.encode(videoUrl, "UTF-8")}"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Notifications
    // ─────────────────────────────────────────────────────────────────────────────
    data object Notifications : Routes("notifications")
    // ─────────────────────────────────────────────────────────────────────────────
    // Social - Follow Lists
    // ─────────────────────────────────────────────────────────────────────────────
    data object FollowList : Routes("followList/{userId}/{tab}") {
        fun createRoute(userId: String, tab: Int = 0): String = "followList/$userId/$tab"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Forward Target Selection
    // ─────────────────────────────────────────────────────────────────────────────
    data object ForwardTarget : Routes("forwardTarget/{messageIds}/{sourceType}/{sourceId}") {
        fun createRoute(messageIds: String, sourceType: String = "CHAT", sourceId: String = ""): String =
            "forwardTarget/$messageIds/$sourceType/$sourceId"
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📍 Bottom Navigation Items (Moved to BottomNavBar.kt)
// ═══════════════════════════════════════════════════════════════════════════════

// Enum removed to avoid conflict with sealed class in BottomNavBar.kt
