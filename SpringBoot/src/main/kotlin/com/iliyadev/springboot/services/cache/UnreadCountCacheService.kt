package com.iliyadev.springboot.services.cache

import io.lettuce.core.api.async.RedisAsyncCommands
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 🔢 Unread Count Cache Service
// Key: unread:{userId}:{chatId} → atomic counter
// Updated on message receive + read receipt
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class UnreadCountCacheService(
    private val redis: RedisAsyncCommands<String, String>
) {
    private val logger = LoggerFactory.getLogger(UnreadCountCacheService::class.java)

    fun incrementUnread(userId: UUID, chatId: UUID) {
        val key = "unread:$userId:$chatId"
        redis.incr(key)
    }

    fun resetUnread(userId: UUID, chatId: UUID) {
        val key = "unread:$userId:$chatId"
        redis.set(key, "0")
    }

    fun getUnreadCount(userId: UUID, chatId: UUID): Int {
        val key = "unread:$userId:$chatId"
        return try {
            val future = redis.get(key)
            future.get()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getAllUnreadCounts(userId: UUID, chatIds: List<UUID>): Map<UUID, Int> {
        val result = mutableMapOf<UUID, Int>()
        chatIds.forEach { chatId ->
            result[chatId] = getUnreadCount(userId, chatId)
        }
        return result
    }

    fun getTotalUnread(userId: UUID): Int {
        return try {
            val future = redis.keys("unread:$userId:*")
            val keys = future.get()
            if (keys.isNullOrEmpty()) return 0
            var total = 0
            keys.forEach { key ->
                val count = redis.get(key).get()?.toIntOrNull() ?: 0
                total += count
            }
            total
        } catch (e: Exception) {
            0
        }
    }
}
