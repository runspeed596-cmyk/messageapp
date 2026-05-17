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
    data object MosbatElm : Routes("mosbat_elm")
    data object CourseDetail : Routes("course_detail/{courseId}") {
        fun createRoute(courseId: String): String = "course_detail/$courseId"
    }
    data object Home : Routes("home")
    data object Messaging : Routes("messaging") // Was ChatList/Main logically
    data object Treasure : Routes("treasure")

    data object ChatList : Routes("chatList")
    data object Groups : Routes("groups")
    data object Channels : Routes("channels")
    data object Profile : Routes("profile?initialTab={initialTab}") {
        fun createRoute(initialTab: Int = 0): String = "profile?initialTab=$initialTab"
    }
    
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
    data object EditCourse : Routes("editCourse/{courseId}") {
        fun createRoute(courseId: String): String = "editCourse/$courseId"
    }
    data object OrganizerSetup : Routes("organizerSetup")
    data object AcademyProfileSetup : Routes("academyProfileSetup")
    data object AcademyProfile : Routes("academyProfile/{institutionId}") {
        fun createRoute(institutionId: String): String = "academyProfile/$institutionId"
    }
    data object AcademyAnalytics : Routes("academyAnalytics/{institutionId}") {
        fun createRoute(institutionId: String): String = "academyAnalytics/$institutionId"
    }
    data object TeacherPublicProfile : Routes("teacherPublicProfile/{teacherId}") {
        fun createRoute(teacherId: String): String = "teacherPublicProfile/$teacherId"
    }
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
    data object GlobalSettings : Routes("global_settings")
    data object Feedback : Routes("feedback")
    data object MessengerSettings : Routes("messenger_settings")
    data object MosbatElmSettings : Routes("mosbat_elm_settings")
    data object MyCourses : Routes("myCourses")
    data object LikedPosts : Routes("likedPosts")
    data object BlockedUsers : Routes("blocked_users")
    data object PrivacyExceptions : Routes("privacy_exceptions/{type}") {
        fun createRoute(type: String) = "privacy_exceptions/$type"
    }

    data object SettingsChat : Routes("settings_chat")
    data object SettingsPrivacy : Routes("settings_privacy")
    data object SettingsNotifications : Routes("settings_notifications")
    data object SettingsDataStorage : Routes("settings_data_storage")
    data object SettingsDevices : Routes("settings_devices")
    data object SettingsLanguage : Routes("settings_language")
    data object Wallet : Routes("wallet")
    data object ArchivedChats : Routes("archivedChats")
    data object MosbatElmNotifications : Routes("mosbat_elm_notifications")
    data object Collaborations : Routes("collaborations")
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
    data object ComingSoon : Routes("coming_soon/{feature}") {
        fun createRoute(feature: String) = "coming_soon/${java.net.URLEncoder.encode(feature, "UTF-8")}"
    }
    data object ForwardTarget : Routes("forwardTarget/{messageIds}/{sourceType}/{sourceId}") {
        fun createRoute(messageIds: String, sourceType: String = "CHAT", sourceId: String = ""): String =
            "forwardTarget/$messageIds/$sourceType/$sourceId"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // AI Bot Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object AiBotList : Routes("aiBotList")
    data object AiBotChat : Routes("aiBotChat/{botId}/{botName}/{botType}") {
        fun createRoute(botId: String, botName: String, botType: String): String =
            "aiBotChat/$botId/${java.net.URLEncoder.encode(botName, "UTF-8")}/${java.net.URLEncoder.encode(botType, "UTF-8")}"
    }
    // ─────────────────────────────────────────────────────────────────────────────
    // Exam Screens
    // ─────────────────────────────────────────────────────────────────────────────
    data object ExamCreation : Routes("examCreation?channelId={channelId}") {
        fun createRoute(channelId: String? = null): String =
            if (channelId != null) "examCreation?channelId=$channelId" else "examCreation"
    }
    data object ExamTaking : Routes("examTaking/{examId}") {
        fun createRoute(examId: String): String = "examTaking/$examId"
    }
    data object ExamHistory : Routes("examHistory")
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📍 Bottom Navigation Items (Moved to BottomNavBar.kt)
// ═══════════════════════════════════════════════════════════════════════════════

// Enum removed to avoid conflict with sealed class in BottomNavBar.kt
