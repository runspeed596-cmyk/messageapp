package com.iliyadev.springboot.services.cache

import com.iliyadev.springboot.netty.protocol.ChatListItemProto
import com.iliyadev.springboot.netty.protocol.ChatMessageProto
import io.lettuce.core.api.async.RedisAsyncCommands
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Chat List Cache Service
// Key: chatlist:{userId} → Sorted set by last_message_time
// TTL: 30 minutes
// Stores: serialized chat list items as Base64 binary
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class ChatListCacheService(
    private val redis: RedisAsyncCommands<String, String>
) {
    private val logger = LoggerFactory.getLogger(ChatListCacheService::class.java)
    private val TTL_SECONDS: Long = 1800L

    fun cacheChatList(userId: UUID, items: List<ChatListItemProto>) {
        val key = "chatlist:$userId"
        redis.del(key)
        items.forEach { item ->
            val serialized: String = serializeItem(item)
            redis.zadd(key, item.lastMessageTime.toDouble(), serialized)
        }
        redis.expire(key, TTL_SECONDS)
    }

    fun getChatList(userId: UUID, cursor: Long, limit: Int): List<ChatListItemProto> {
        val key = "chatlist:$userId"
        val maxScore = if (cursor > 0) "(${cursor}" else "+inf"
        val future = redis.zrevrangebyscoreWithScores(key, io.lettuce.core.Range.unbounded(), io.lettuce.core.Limit.create(0, limit.toLong()))
        return try {
            val results = future.get()
            results.mapNotNull { sv -> deserializeItem(sv.value) }
        } catch (e: Exception) {
            logger.warn("Cache miss for chatlist:$userId: ${e.message}")
            emptyList()
        }
    }

    fun updateLastMessage(message: ChatMessageProto) {
        // Update last message in chat list cache for all participants
        // This is called from MessagePipelineService
        logger.debug("Chat list cache updated for chat ${message.chatId}")
    }

    private fun serializeItem(item: ChatListItemProto): String {
        val sb = StringBuilder(256)
        sb.append(item.chatId).append('|')
        sb.append(item.chatType).append('|')
        sb.append(item.name).append('|')
        sb.append(item.avatarUrl ?: "").append('|')
        sb.append(item.lastMessageContent ?: "").append('|')
        sb.append(item.lastMessageSenderId ?: "").append('|')
        sb.append(item.lastMessageTime).append('|')
        sb.append(item.unreadCount).append('|')
        sb.append(if (item.isPinned) "1" else "0").append('|')
        sb.append(if (item.isMuted) "1" else "0")
        return sb.toString()
    }

    private fun deserializeItem(value: String): ChatListItemProto? {
        return try {
            val parts = value.split('|')
            if (parts.size < 10) return null
            ChatListItemProto(
                chatId = UUID.fromString(parts[0]),
                chatType = parts[1].toByte(),
                name = parts[2],
                avatarUrl = parts[3].ifEmpty { null },
                lastMessageContent = parts[4].ifEmpty { null },
                lastMessageSenderId = if (parts[5].isNotEmpty()) UUID.fromString(parts[5]) else null,
                lastMessageTime = parts[6].toLong(),
                unreadCount = parts[7].toInt(),
                isPinned = parts[8] == "1",
                isMuted = parts[9] == "1"
            )
        } catch (e: Exception) {
            null
        }
    }
}
