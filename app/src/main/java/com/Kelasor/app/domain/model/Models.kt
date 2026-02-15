package com.Kelasor.app.domain.model

import java.time.Instant

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val phoneNumber: String,
    val avatarUrl: String?,
    val bio: String?,
    val isOnline: Boolean,
    val lastSeen: Instant?,
    val contactName: String? = null,
    
    // Profile Enhancements
    val university: String? = null,
    val fieldOfStudy: String? = null,
    val education: String? = null,
    val skills: String? = null,
    val interests: String? = null,
    val workExperience: String? = null,
    val achievements: String? = null,
    
    // Bio Channels
    val bioChannelId1: String? = null,
    val bioChannelId2: String? = null,
    
    val createdAt: Instant,
    // Privacy settings - these control what OTHER users can see
    val profileVisibility: String = "EVERYONE", // EVERYONE, CONTACTS, NOBODY
    val onlineVisibility: String = "EVERYONE",
    val phoneVisibility: String = "CONTACTS",
    // Privacy-sanitized display fields (computed based on viewer's relationship)
    // These will be null/hidden when the user has restricted visibility
    val displayAvatarUrl: String? = avatarUrl, // Avatar for display (may be hidden)
    val displayOnlineStatus: Boolean = isOnline, // Online status for display (may be hidden)
    val displayPhoneNumber: String = phoneNumber // Phone for display (may be "مخفی")
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat/Conversation Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

enum class ChatType {
    PRIVATE,
    GROUP,
    CHANNEL
}

data class Chat(
    val id: String,
    val type: ChatType,
    val title: String,
    val avatarUrl: String?,
    val lastMessage: Message?,
    val unreadCount: Int,
    val isPinned: Boolean,
    val isMuted: Boolean,
    val isArchived: Boolean,
    val participants: List<User>,
    val updatedAt: Instant
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VIDEO_NOTE,  // Circular video message (like Telegram video notes)
    VOICE,
    AUDIO,
    FILE,
    LOCATION,
    CONTACT,
    STICKER,
    POLL,
    LINK
}

enum class MessageStatus {
    PENDING,   // Message saved locally, waiting to sync (shows clock icon)
    SENDING,   // Message is being sent
    SENT,      // Message sent to server
    DELIVERED, // Message delivered to recipient
    READ,      // Message read by recipient
    FAILED,    // Message failed to send
    SCHEDULED  // Message scheduled for later
}

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val type: MessageType,
    val content: String,
    val mediaUrl: String?,
    val replyToMessageId: String?,
    val replyToMessage: Message?,
    val forwardedFrom: String?,
    val status: MessageStatus,
    val isEdited: Boolean,
    val createdAt: Instant,
    val editedAt: Instant?,
    val reactions: Map<String, Int> = emptyMap(),
    val myReaction: String? = null,
    val poll: Poll? = null,
    val amplitudes: List<Int>? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Instant? = null,
    val scheduledAt: Instant? = null
) {
    val isFromMe: Boolean
        get() = senderId == "current_user_id"
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER
}

data class GroupMember(
    val user: User,
    val role: MemberRole,
    val joinedAt: Instant,
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

data class Group(
    val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val members: List<GroupMember>,
    val memberCount: Int,
    val isPublic: Boolean,
    val inviteLink: String?,
    val isInviteLinkEnabled: Boolean,
    val allowMembersToSendMessages: Boolean,
    val allowMembersToEditInfo: Boolean,
    val myRole: MemberRole?,
    val lastMessage: Message?,
    val unreadCount: Int,
    val createdAt: Instant,
    val createdBy: User?,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

data class Channel(
    val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val subscriberCount: Int,
    val isPublic: Boolean,
    val publicId: String?, // Unique public identifier (e.g., "mychannel")
    val inviteLink: String?,
    val owner: User?,
    val isSubscribed: Boolean,
    val isAdmin: Boolean,
    val lastPost: ChannelPost?,
    val unreadCount: Int,
    val createdAt: Instant,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false
)

data class ChannelSubscriber(
    val user: User,
    val isAdmin: Boolean,
    val joinedAt: Instant
)

data class ChannelPost(
    val id: String,
    val channelId: String,
    val type: MessageType,
    val content: String,
    val mediaUrl: String?,
    val viewCount: Int,
    val reactions: Map<String, Int>,
    val commentsEnabled: Boolean,
    val commentCount: Int,
    val createdAt: Instant,
    val editedAt: Instant?,
    val poll: Poll? = null,
    val amplitudes: List<Int>? = null,
    val myReaction: String? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Instant? = null,
    val scheduledAt: Instant? = null,
    val forwardedFrom: String? = null,
    val isEdited: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Typing Status Model
// ═══════════════════════════════════════════════════════════════════════════════

data class TypingStatus(
    val chatId: String,
    val userId: String,
    val userName: String,
    val isTyping: Boolean
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Contact Model
// ═══════════════════════════════════════════════════════════════════════════════

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val user: User?,
    val isRegistered: Boolean
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Poll Domain Model
// ═══════════════════════════════════════════════════════════════════════════════

data class Poll(
    val id: String,
    val question: String,
    val isMultipleChoice: Boolean,
    val isAnonymous: Boolean,
    val options: List<PollOption>,
    val totalVotes: Int,
    val userVotedOptionIds: List<String>,
    val createdAt: Instant
)

data class PollOption(
    val id: String,
    val text: String,
    val voteCount: Int,
    val votePercentage: Float
)
