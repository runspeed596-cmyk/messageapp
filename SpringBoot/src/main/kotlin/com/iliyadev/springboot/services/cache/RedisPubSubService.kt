package com.iliyadev.springboot.services.cache

import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 Redis Pub/Sub Service - Real-time message delivery between server instances
// Channel: chat:{chatId} for message events
// Also manages chat membership cache for fan-out
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class RedisPubSubService(
    private val redis: RedisAsyncCommands<String, String>,
    private val pubSubConnection: StatefulRedisPubSubConnection<String, String>
) {
    private val logger = LoggerFactory.getLogger(RedisPubSubService::class.java)
    private val chatMembersCache: ConcurrentHashMap<UUID, MutableSet<UUID>> = ConcurrentHashMap()
    private val MEMBER_TTL: Long = 3600L

    fun publishMessage(chatId: UUID, messageBytes: ByteArray) {
        val channel = "chat:$chatId"
        val encoded: String = java.util.Base64.getEncoder().encodeToString(messageBytes)
        redis.publish(channel, encoded)
    }

    fun subscribeToChatChannel(chatId: UUID, listener: (ByteArray) -> Unit) {
        pubSubConnection.addListener(object : RedisPubSubAdapter<String, String>() {
            override fun message(channel: String, message: String) {
                if (channel == "chat:$chatId") {
                    val bytes: ByteArray = java.util.Base64.getDecoder().decode(message)
                    listener(bytes)
                }
            }
        })
        pubSubConnection.async().subscribe("chat:$chatId")
    }

    fun getChatMembers(chatId: UUID): Set<UUID> {
        val cached: MutableSet<UUID>? = chatMembersCache[chatId]
        if (cached != null) return cached
        return loadChatMembers(chatId)
    }

    fun addChatMember(chatId: UUID, userId: UUID) {
        val key = "chatmembers:$chatId"
        redis.sadd(key, userId.toString())
        redis.expire(key, MEMBER_TTL)
        chatMembersCache.getOrPut(chatId) { ConcurrentHashMap.newKeySet() }.add(userId)
    }

    fun removeChatMember(chatId: UUID, userId: UUID) {
        val key = "chatmembers:$chatId"
        redis.srem(key, userId.toString())
        chatMembersCache[chatId]?.remove(userId)
    }

    fun setChatMembers(chatId: UUID, members: Set<UUID>) {
        val key = "chatmembers:$chatId"
        redis.del(key)
        members.forEach { member -> redis.sadd(key, member.toString()) }
        redis.expire(key, MEMBER_TTL)
        chatMembersCache[chatId] = ConcurrentHashMap.newKeySet<UUID>().apply { addAll(members) }
    }

    private fun loadChatMembers(chatId: UUID): Set<UUID> {
        val key = "chatmembers:$chatId"
        return try {
            val future = redis.smembers(key)
            val members = future.get()
            val result: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
            members.forEach { m -> result.add(UUID.fromString(m)) }
            chatMembersCache[chatId] = result
            result
        } catch (e: Exception) {
            logger.warn("Error loading members for chat $chatId: ${e.message}")
            emptySet()
        }
    }
}
