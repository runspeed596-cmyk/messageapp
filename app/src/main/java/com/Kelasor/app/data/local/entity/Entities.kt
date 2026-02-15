package com.Kelasor.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Junction

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val phoneNumber: String,
    val avatarUrl: String?,
    val bio: String?,
    val isOnline: Boolean = false,
    val lastSeenAt: Long? = null,
    val contactName: String? = null,
    val isCurrentUser: Boolean = false,
    val isContact: Boolean = false, // Added security field
    
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
    
    // Privacy settings - stored to enforce privacy on cached data
    val profileVisibility: String = "EVERYONE",
    val onlineVisibility: String = "EVERYONE",
    val phoneVisibility: String = "CONTACTS"
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val avatarUrl: String?,
    val lastMessageId: String?,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val isLastMessageEdited: Boolean = false,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isDeletedLocally: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Entity
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "messages",
    indices = [Index("chatId"), Index("createdAt")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val type: String,
    val content: String,
    val mediaUrl: String?,
    val replyToMessageId: String?,
    val replyToMessage: String? = null,
    val forwardedFrom: String?,
    val status: String,
    val isEdited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val isSynced: Boolean = true,
    val reactions: String? = null,
    val myReaction: String? = null,
    val poll: String? = null,
    val amplitudes: String? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val scheduledAt: Long? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val memberCount: Int,
    val isPublic: Boolean,
    val inviteLink: String?,
    val isInviteLinkEnabled: Boolean,
    val allowMembersToSendMessages: Boolean,
    val allowMembersToEditInfo: Boolean,
    val myRole: String?,
    val lastMessageContent: String? = null,
    val lastMessageTime: Long? = null,
    val isLastMessageEdited: Boolean = false,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "group_members",
    indices = [Index("groupId"), Index("userId")]
)
data class GroupMemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val userId: String,
    val role: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val canEditInfo: Boolean = false,
    val canPostStory: Boolean = false,
    val canAddMembers: Boolean = false,
    val canRemoveMembers: Boolean = false
)

@Entity(
    tableName = "group_messages",
    indices = [Index("groupId"), Index("createdAt")]
)
data class GroupMessageEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String?,
    val type: String,
    val content: String,
    val mediaUrl: String?,
    val replyToMessageId: String?,
    val replyToMessage: String? = null,
    val isEdited: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val isSynced: Boolean = true,
    val reactions: String? = null,
    val myReaction: String? = null,
    val poll: String? = null,
    val amplitudes: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val subscriberCount: Int,
    val isPublic: Boolean,
    val publicId: String?, // Unique public identifier
    val inviteLink: String?,
    val ownerId: String?,
    val isSubscribed: Boolean = false,
    val isAdmin: Boolean = false,
    val lastPostContent: String? = null,
    val lastPostTime: Long? = null,
    val isLastPostEdited: Boolean = false,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "channel_posts",
    indices = [Index("channelId"), Index("createdAt")]
)
data class ChannelPostEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val type: String,
    val content: String,
    val mediaUrl: String?,
    val viewCount: Int = 0,
    val commentsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val poll: String? = null,
    val reactions: String? = null, // JSON map of reactions
    val amplitudes: String? = null,
    val isPinned: Boolean = false,
    val pinnedAt: Long? = null,
    val scheduledAt: Long? = null,
    val forwardedFrom: String? = null,
    val isEdited: Boolean = false,
    val myReaction: String? = null
)

@Entity(
    tableName = "channel_subscribers",
    indices = [Index("channelId"), Index("userId")]
)
data class ChannelSubscriberEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val userId: String,
    val isAdmin: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔗 Chat Participant Junction
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "chat_participants",
    primaryKeys = ["chatId", "userId"],
    indices = [Index("chatId"), Index("userId")]
)
data class ChatParticipantEntity(
    val chatId: String,
    val userId: String
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notified Message Entity (to track which messages have been notified)
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "notified_messages")
data class NotifiedMessageEntity(
    @PrimaryKey val messageId: String,
    val notifiedAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔗 Compound Entities (Relations)
// ═══════════════════════════════════════════════════════════════════════════════

// ChatWithParticipants is defined in its own file

data class GroupMessageWithReply(
    @Embedded val message: GroupMessageEntity,
    @Relation(
        parentColumn = "replyToMessageId",
        entityColumn = "id"
    )
    val replyToMessage: GroupMessageEntity?
)

data class SubscriberWithUser(
    @Embedded val subscriber: ChannelSubscriberEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)

// ═══════════════════════════════════════════════════════════════════════════════
// 📖 Story Entities
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // IMAGE or VIDEO
    val mediaUrl: String,
    val caption: String?,
    val duration: Int,
    val viewCount: Int,
    val isViewed: Boolean,
    val createdAt: Long,
    val expiresAt: Long
)

@Entity(tableName = "story_users")
data class StoryUserEntity(
    @PrimaryKey val id: String, // same as userId
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val isCurrentUser: Boolean,
    val hasUnviewedStories: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
