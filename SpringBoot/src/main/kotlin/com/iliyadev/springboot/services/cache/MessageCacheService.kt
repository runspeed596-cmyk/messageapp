package com.iliyadev.springboot.services.cache

import com.iliyadev.springboot.netty.protocol.BinaryReader
import com.iliyadev.springboot.netty.protocol.BinaryWriter
import com.iliyadev.springboot.netty.protocol.ChatMessageProto
import io.lettuce.core.api.async.RedisAsyncCommands
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Message Cache Service
// Hot messages: msg:{chatId}:recent → last 50 messages per chat
// TTL: 10 minutes for active chats, 2 minutes for idle
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class MessageCacheService(
    private val redis: RedisAsyncCommands<String, String>
) {
    private val logger = LoggerFactory.getLogger(MessageCacheService::class.java)
    private val MAX_CACHED_MESSAGES: Long = 50L
    private val ACTIVE_TTL: Long = 600L
    private val IDLE_TTL: Long = 120L

    fun cacheMessage(message: ChatMessageProto) {
        val key = "msg:${message.chatId}:recent"
        val writer = BinaryWriter()
        message.serialize(writer)
        val encoded: String = Base64.getEncoder().encodeToString(writer.toByteArray())
        redis.zadd(key, message.messageId.toDouble(), encoded)
        redis.zremrangebyrank(key, 0, -(MAX_CACHED_MESSAGES + 1))
        redis.expire(key, ACTIVE_TTL)
    }

    fun getMessages(chatId: UUID, cursor: Long, limit: Int): List<ChatMessageProto> {
        val key = "msg:$chatId:recent"
        val maxScore = if (cursor > 0) "(${cursor}" else "+inf"
        val future = redis.zrevrangebyscoreWithScores(key, io.lettuce.core.Range.unbounded(), io.lettuce.core.Limit.create(0, limit.toLong()))
        return try {
            val results = future.get()
            results.mapNotNull { sv ->
                try {
                    val bytes: ByteArray = Base64.getDecoder().decode(sv.value)
                    ChatMessageProto.deserialize(BinaryReader(bytes))
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Cache miss for messages $chatId: ${e.message}")
            emptyList()
        }
    }

    fun invalidateChat(chatId: UUID) {
        redis.del("msg:$chatId:recent")
    }

    fun getLastMessage(chatId: UUID): ChatMessageProto? {
        val key = "msg:$chatId:recent"
        val future = redis.zrevrangebyscoreWithScores(key, io.lettuce.core.Range.unbounded(), io.lettuce.core.Limit.create(0, 1))
        return try {
            val results = future.get()
            results.firstOrNull()?.let { sv ->
                val bytes: ByteArray = Base64.getDecoder().decode(sv.value)
                ChatMessageProto.deserialize(BinaryReader(bytes))
            }
        } catch (e: Exception) {
            null
        }
    }
}
