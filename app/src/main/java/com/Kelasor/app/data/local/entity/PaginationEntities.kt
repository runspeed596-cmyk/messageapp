package com.Kelasor.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Room Entities for Cursor-Based Pagination
// Indexes: composite on (chatId, id DESC) for O(1) cursor lookup
// ═══════════════════════════════════════════════════════════════════════════════

@Entity(
    tableName = "cached_messages",
    indices = [
        Index(value = ["chat_id", "id"], orders = [Index.Order.ASC, Index.Order.DESC]),
        Index(value = ["chat_id", "timestamp"], orders = [Index.Order.ASC, Index.Order.DESC])
    ]
)
data class CachedMessageEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "chat_id") val chatId: String,
    @ColumnInfo(name = "sender_id") val senderId: String,
    @ColumnInfo(name = "message_type") val messageType: Int,
    @ColumnInfo(name = "content") val content: String?,
    @ColumnInfo(name = "media_id") val mediaId: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "reply_to_id") val replyToId: Long?,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "edited_at") val editedAt: Long?,
    @ColumnInfo(name = "is_synced") val isSynced: Boolean = true
)

@Entity(
    tableName = "cached_chat_list",
    indices = [
        Index(value = ["last_message_time"], orders = [Index.Order.DESC])
    ]
)
data class CachedChatListEntity(
    @PrimaryKey @ColumnInfo(name = "chat_id") val chatId: String,
    @ColumnInfo(name = "chat_type") val chatType: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "avatar_url") val avatarUrl: String?,
    @ColumnInfo(name = "last_message_content") val lastMessageContent: String?,
    @ColumnInfo(name = "last_message_sender_id") val lastMessageSenderId: String?,
    @ColumnInfo(name = "last_message_time") val lastMessageTime: Long,
    @ColumnInfo(name = "unread_count") val unreadCount: Int,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "is_muted") val isMuted: Boolean
)

@Entity(
    tableName = "cached_stories",
    indices = [
        Index(value = ["user_id", "timestamp"], orders = [Index.Order.ASC, Index.Order.DESC])
    ]
)
data class CachedStoryEntity(
    @PrimaryKey @ColumnInfo(name = "story_id") val storyId: Long,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "media_type") val mediaType: Int,
    @ColumnInfo(name = "media_url") val mediaUrl: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "caption") val caption: String?,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "expires_at") val expiresAt: Long,
    @ColumnInfo(name = "view_count") val viewCount: Int
)

@Entity(
    tableName = "pagination_remote_keys",
    indices = [
        Index(value = ["entity_type", "entity_id"])
    ]
)
data class PaginationRemoteKey(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "next_cursor") val nextCursor: Long?,
    @ColumnInfo(name = "prev_cursor") val prevCursor: Long?,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
