package com.iliyadev.springboot.netty

import com.iliyadev.springboot.netty.protocol.*
import com.iliyadev.springboot.services.cache.ChatListCacheService
import com.iliyadev.springboot.services.cache.MessageCacheService
import com.iliyadev.springboot.services.cache.RedisPubSubService
import com.iliyadev.springboot.services.cache.StoryCacheService
import com.iliyadev.springboot.services.cache.UnreadCountCacheService
import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 🚀 Message Pipeline Service - Async, non-blocking message delivery
// Flow: Client → Netty → Redis Pub/Sub → Target Client(s)
// ═══════════════════════════════════════════════════════════════════════════════

@Service
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class MessagePipelineService(
    private val connectionManager: ConnectionManager,
    private val chatListCache: ChatListCacheService,
    private val messageCache: MessageCacheService,
    private val storyCache: StoryCacheService,
    private val unreadCountCache: UnreadCountCacheService,
    private val redisPubSub: RedisPubSubService
) {
    private val logger = LoggerFactory.getLogger(MessagePipelineService::class.java)
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun processMessage(message: ChatMessageProto) {
        scope.launch {
            try {
                messageCache.cacheMessage(message)
                chatListCache.updateLastMessage(message)
                val members: Set<UUID> = redisPubSub.getChatMembers(message.chatId)
                val writer = BinaryWriter()
                message.serialize(writer)
                val payload: ByteArray = writer.toByteArray()
                val envelope = Envelope(OpCode.CHAT_MESSAGE, payload)
                val frameBytes: ByteArray = envelope.serialize()
                members.forEach { memberId ->
                    if (memberId != message.senderId) {
                        unreadCountCache.incrementUnread(memberId, message.chatId)
                        val buf = Unpooled.wrappedBuffer(frameBytes.copyOf())
                        connectionManager.sendToUser(memberId, BinaryWebSocketFrame(buf))
                    }
                }
                redisPubSub.publishMessage(message.chatId, frameBytes)
            } catch (e: Exception) {
                logger.error("Message pipeline error: ${e.message}", e)
            }
        }
    }

    fun processTypingEvent(event: TypingEventProto) {
        scope.launch {
            val writer = BinaryWriter()
            event.serialize(writer)
            val payload: ByteArray = writer.toByteArray()
            val envelope = Envelope(OpCode.TYPING_EVENT, payload)
            val frameBytes: ByteArray = envelope.serialize()
            val members: Set<UUID> = redisPubSub.getChatMembers(event.chatId)
            members.forEach { memberId ->
                if (memberId != event.userId) {
                    val buf = Unpooled.wrappedBuffer(frameBytes.copyOf())
                    connectionManager.sendToUser(memberId, BinaryWebSocketFrame(buf))
                }
            }
        }
    }

    fun processReadReceipt(receipt: ReadReceiptProto) {
        scope.launch {
            unreadCountCache.resetUnread(receipt.userId, receipt.chatId)
            val writer = BinaryWriter()
            receipt.serialize(writer)
            val payload: ByteArray = writer.toByteArray()
            val envelope = Envelope(OpCode.READ_RECEIPT, payload)
            val frameBytes: ByteArray = envelope.serialize()
            val members: Set<UUID> = redisPubSub.getChatMembers(receipt.chatId)
            members.forEach { memberId ->
                if (memberId != receipt.userId) {
                    val buf = Unpooled.wrappedBuffer(frameBytes.copyOf())
                    connectionManager.sendToUser(memberId, BinaryWebSocketFrame(buf))
                }
            }
        }
    }

    fun fetchChatList(userId: UUID, request: CursorPageRequest, callback: (ByteArray) -> Unit) {
        scope.launch {
            val items: List<ChatListItemProto> = chatListCache.getChatList(userId, request.cursorId, request.limit)
            val writer = BinaryWriter()
            writer.writeInt(items.size)
            items.forEach { it.serialize(writer) }
            callback(writer.toByteArray())
        }
    }

    fun fetchMessages(userId: UUID, request: CursorPageRequest, callback: (ByteArray) -> Unit) {
        scope.launch {
            val chatId: UUID = request.chatId ?: return@launch
            val messages: List<ChatMessageProto> = messageCache.getMessages(chatId, request.cursorId, request.limit)
            val writer = BinaryWriter()
            writer.writeInt(messages.size)
            messages.forEach { it.serialize(writer) }
            callback(writer.toByteArray())
        }
    }

    fun fetchStories(userId: UUID, request: CursorPageRequest, callback: (ByteArray) -> Unit) {
        scope.launch {
            val stories: List<StoryItemProto> = storyCache.getStories(userId, request.cursorId, request.limit)
            val writer = BinaryWriter()
            writer.writeInt(stories.size)
            stories.forEach { it.serialize(writer) }
            callback(writer.toByteArray())
        }
    }

    fun shutdown() {
        scope.cancel()
    }
}
