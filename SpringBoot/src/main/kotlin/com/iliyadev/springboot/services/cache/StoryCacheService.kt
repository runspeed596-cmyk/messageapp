package com.iliyadev.springboot.services.cache

import com.iliyadev.springboot.netty.protocol.BinaryReader
import com.iliyadev.springboot.netty.protocol.BinaryWriter
import com.iliyadev.springboot.netty.protocol.StoryItemProto
import io.lettuce.core.api.async.RedisAsyncCommands
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📖 Story Cache Service
// Key: stories:{userId} → sorted set of active stories
// TTL: 24 hours (matches story expiration)
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class StoryCacheService(
    private val redis: RedisAsyncCommands<String, String>
) {
    private val logger = LoggerFactory.getLogger(StoryCacheService::class.java)
    private val STORY_TTL: Long = 86400L

    fun cacheStory(story: StoryItemProto) {
        val key = "stories:${story.userId}"
        val writer = BinaryWriter()
        story.serialize(writer)
        val encoded: String = Base64.getEncoder().encodeToString(writer.toByteArray())
        redis.zadd(key, story.timestamp.toDouble(), encoded)
        redis.expire(key, STORY_TTL)
    }

    fun getStories(userId: UUID, cursor: Long, limit: Int): List<StoryItemProto> {
        val key = "stories:$userId"
        val now: Long = System.currentTimeMillis()
        val future = redis.zrevrangebyscoreWithScores(key, io.lettuce.core.Range.unbounded(), io.lettuce.core.Limit.create(0, limit.toLong()))
        return try {
            val results = future.get()
            results.mapNotNull { sv ->
                try {
                    val bytes: ByteArray = Base64.getDecoder().decode(sv.value)
                    val story: StoryItemProto = StoryItemProto.deserialize(BinaryReader(bytes))
                    if (story.expiresAt > now) story else null
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Cache miss for stories $userId: ${e.message}")
            emptyList()
        }
    }

    fun removeExpiredStories(userId: UUID) {
        val key = "stories:$userId"
        val now: Double = System.currentTimeMillis().toDouble()
        redis.zremrangebyscore(key, io.lettuce.core.Range.create(0.0, now - (STORY_TTL * 1000)))
    }

    fun removeStory(userId: UUID, storyId: Long) {
        val key = "stories:$userId"
        val future = redis.zrangebyscoreWithScores(key, io.lettuce.core.Range.unbounded())
        try {
            val results = future.get()
            results.forEach { sv ->
                val bytes: ByteArray = Base64.getDecoder().decode(sv.value)
                val story: StoryItemProto = StoryItemProto.deserialize(BinaryReader(bytes))
                if (story.storyId == storyId) {
                    redis.zrem(key, sv.value)
                }
            }
        } catch (e: Exception) {
            logger.warn("Error removing story $storyId for user $userId")
        }
    }
}
