package com.iliyadev.springboot.netty

import io.netty.channel.Channel
import io.netty.channel.group.DefaultChannelGroup
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.util.concurrent.GlobalEventExecutor
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

// ═══════════════════════════════════════════════════════════════════════════════
// 🔗 Connection Manager - O(1) user→channel lookup, 100K connection target
// Memory budget: ~40KB per connection × 100K = ~4GB
// ═══════════════════════════════════════════════════════════════════════════════

@Component
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class ConnectionManager {
    private val logger = LoggerFactory.getLogger(ConnectionManager::class.java)
    private val userChannels: ConcurrentHashMap<UUID, Channel> = ConcurrentHashMap(16384, 0.75f, 16)
    private val channelUsers: ConcurrentHashMap<Channel, UUID> = ConcurrentHashMap(16384, 0.75f, 16)
    private val allChannels = DefaultChannelGroup("all-connections", GlobalEventExecutor.INSTANCE)

    fun registerConnection(userId: UUID, channel: Channel) {
        val oldChannel: Channel? = userChannels.put(userId, channel)
        channelUsers[channel] = userId
        allChannels.add(channel)
        oldChannel?.let { old ->
            if (old != channel && old.isActive) {
                channelUsers.remove(old)
                old.close()
            }
        }
        logger.info("✅ User $userId connected. Total connections: ${userChannels.size}")
    }

    fun removeConnection(channel: Channel) {
        val userId: UUID? = channelUsers.remove(channel)
        userId?.let { uid ->
            userChannels.remove(uid, channel)
            logger.info("🔌 User $uid disconnected. Total connections: ${userChannels.size}")
        }
        allChannels.remove(channel)
    }

    fun getChannel(userId: UUID): Channel? = userChannels[userId]

    fun getUserId(channel: Channel): UUID? = channelUsers[channel]

    fun isUserOnline(userId: UUID): Boolean = userChannels[userId]?.isActive == true

    fun sendToUser(userId: UUID, frame: BinaryWebSocketFrame): Boolean {
        val channel: Channel = userChannels[userId] ?: return false
        if (!channel.isActive) {
            removeConnection(channel)
            return false
        }
        channel.writeAndFlush(frame.retainedDuplicate())
        return true
    }

    fun sendToUsers(userIds: Collection<UUID>, frame: BinaryWebSocketFrame) {
        userIds.forEach { userId -> sendToUser(userId, frame) }
        frame.release()
    }

    fun getOnlineCount(): Int = userChannels.size

    fun getOnlineUsers(): Set<UUID> = userChannels.keys.toSet()
}
