package com.iliyadev.springboot.repositories

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📄 Cursor-Based Pagination Repository
// NEVER uses OFFSET — always cursor-based for O(1) page access
// Supports: messages, group messages, channel posts, stories, chat list
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
class CursorPaginationRepository {
    @PersistenceContext
    private lateinit var em: EntityManager
    private val logger = LoggerFactory.getLogger(CursorPaginationRepository::class.java)

    data class MessageRow(
        val id: Long,
        val chatId: UUID,
        val senderId: UUID,
        val content: String?,
        val messageType: String?,
        val mediaUrl: String?,
        val thumbnailUrl: String?,
        val replyToId: Long?,
        val createdAt: java.time.Instant,
        val editedAt: java.time.Instant?
    )

    fun fetchMessages(chatId: UUID, cursorId: Long, limit: Int): List<MessageRow> {
        val sql = if (cursorId > 0) {
            """SELECT m.id, m.chat_id, m.sender_id, m.content, m.message_type, 
                      m.media_url, m.thumbnail_url, m.reply_to_id, m.created_at, m.edited_at
               FROM messages m
               WHERE m.chat_id = :chatId AND m.id < :cursorId
               ORDER BY m.id DESC
               LIMIT :lim"""
        } else {
            """SELECT m.id, m.chat_id, m.sender_id, m.content, m.message_type,
                      m.media_url, m.thumbnail_url, m.reply_to_id, m.created_at, m.edited_at
               FROM messages m
               WHERE m.chat_id = :chatId
               ORDER BY m.id DESC
               LIMIT :lim"""
        }
        val query = em.createNativeQuery(sql)
            .setParameter("chatId", chatId)
            .setParameter("lim", limit)
        if (cursorId > 0) query.setParameter("cursorId", cursorId)
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any?>>
        return results.map { row -> mapMessageRow(row) }
    }

    fun fetchGroupMessages(groupId: UUID, cursorId: Long, limit: Int): List<MessageRow> {
        val sql = if (cursorId > 0) {
            """SELECT gm.id, gm.group_id, gm.sender_id, gm.content, gm.message_type,
                      gm.media_url, gm.thumbnail_url, gm.reply_to_id, gm.created_at, gm.edited_at
               FROM group_messages gm
               WHERE gm.group_id = :groupId AND gm.id < :cursorId
               ORDER BY gm.id DESC
               LIMIT :lim"""
        } else {
            """SELECT gm.id, gm.group_id, gm.sender_id, gm.content, gm.message_type,
                      gm.media_url, gm.thumbnail_url, gm.reply_to_id, gm.created_at, gm.edited_at
               FROM group_messages gm
               WHERE gm.group_id = :groupId
               ORDER BY gm.id DESC
               LIMIT :lim"""
        }
        val query = em.createNativeQuery(sql)
            .setParameter("groupId", groupId)
            .setParameter("lim", limit)
        if (cursorId > 0) query.setParameter("cursorId", cursorId)
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any?>>
        return results.map { row -> mapMessageRow(row) }
    }

    fun fetchChannelPosts(channelId: UUID, cursorId: Long, limit: Int): List<MessageRow> {
        val sql = if (cursorId > 0) {
            """SELECT cp.id, cp.channel_id, cp.author_id, cp.content, cp.content_type,
                      cp.media_url, cp.thumbnail_url, NULL, cp.created_at, cp.edited_at
               FROM channel_posts cp
               WHERE cp.channel_id = :channelId AND cp.id < :cursorId
               ORDER BY cp.id DESC
               LIMIT :lim"""
        } else {
            """SELECT cp.id, cp.channel_id, cp.author_id, cp.content, cp.content_type,
                      cp.media_url, cp.thumbnail_url, NULL, cp.created_at, cp.edited_at
               FROM channel_posts cp
               WHERE cp.channel_id = :channelId
               ORDER BY cp.id DESC
               LIMIT :lim"""
        }
        val query = em.createNativeQuery(sql)
            .setParameter("channelId", channelId)
            .setParameter("lim", limit)
        if (cursorId > 0) query.setParameter("cursorId", cursorId)
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any?>>
        return results.map { row -> mapMessageRow(row) }
    }

    data class ChatListRow(
        val chatId: UUID,
        val chatType: String?,
        val otherUserId: UUID?,
        val lastMessageContent: String?,
        val lastMessageTime: java.time.Instant?,
        val unreadCount: Long
    )

    fun fetchChatList(userId: UUID, cursorTime: Long, limit: Int): List<ChatListRow> {
        val sql = if (cursorTime > 0) {
            """SELECT c.id, c.chat_type, cp2.user_id,
                      (SELECT content FROM messages WHERE chat_id = c.id ORDER BY id DESC LIMIT 1),
                      c.last_message_time, 0
               FROM chats c
               JOIN chat_participants cp ON cp.chat_id = c.id AND cp.user_id = :userId
               LEFT JOIN chat_participants cp2 ON cp2.chat_id = c.id AND cp2.user_id != :userId
               WHERE c.last_message_time < :cursorTime
               ORDER BY c.last_message_time DESC
               LIMIT :lim"""
        } else {
            """SELECT c.id, c.chat_type, cp2.user_id,
                      (SELECT content FROM messages WHERE chat_id = c.id ORDER BY id DESC LIMIT 1),
                      c.last_message_time, 0
               FROM chats c
               JOIN chat_participants cp ON cp.chat_id = c.id AND cp.user_id = :userId
               LEFT JOIN chat_participants cp2 ON cp2.chat_id = c.id AND cp2.user_id != :userId
               ORDER BY c.last_message_time DESC
               LIMIT :lim"""
        }
        val query = em.createNativeQuery(sql)
            .setParameter("userId", userId)
            .setParameter("lim", limit)
        if (cursorTime > 0) query.setParameter("cursorTime", java.time.Instant.ofEpochMilli(cursorTime))
        @Suppress("UNCHECKED_CAST")
        val results = query.resultList as List<Array<Any?>>
        return results.map { row ->
            ChatListRow(
                chatId = row[0] as UUID,
                chatType = row[1] as? String,
                otherUserId = row[2] as? UUID,
                lastMessageContent = row[3] as? String,
                lastMessageTime = row[4] as? java.time.Instant,
                unreadCount = (row[5] as? Number)?.toLong() ?: 0
            )
        }
    }

    fun fetchStories(userId: UUID, cursorId: Long, limit: Int): List<Array<Any?>> {
        val sql = if (cursorId > 0) {
            """SELECT s.id, s.user_id, s.media_type, s.media_url, s.thumbnail_url,
                      s.caption, s.created_at, s.expires_at, s.view_count
               FROM stories s
               WHERE s.expires_at > NOW() AND s.id < :cursorId
               ORDER BY s.id DESC
               LIMIT :lim"""
        } else {
            """SELECT s.id, s.user_id, s.media_type, s.media_url, s.thumbnail_url,
                      s.caption, s.created_at, s.expires_at, s.view_count
               FROM stories s
               WHERE s.expires_at > NOW()
               ORDER BY s.id DESC
               LIMIT :lim"""
        }
        val query = em.createNativeQuery(sql)
            .setParameter("lim", limit)
        if (cursorId > 0) query.setParameter("cursorId", cursorId)
        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<Array<Any?>>
    }

    private fun mapMessageRow(row: Array<Any?>): MessageRow {
        return MessageRow(
            id = (row[0] as Number).toLong(),
            chatId = row[1] as UUID,
            senderId = row[2] as UUID,
            content = row[3] as? String,
            messageType = row[4] as? String,
            mediaUrl = row[5] as? String,
            thumbnailUrl = row[6] as? String,
            replyToId = (row[7] as? Number)?.toLong(),
            createdAt = row[8] as java.time.Instant,
            editedAt = row[9] as? java.time.Instant
        )
    }
}
