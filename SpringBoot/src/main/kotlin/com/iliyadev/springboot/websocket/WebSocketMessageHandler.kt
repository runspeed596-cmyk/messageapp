package com.iliyadev.springboot.websocket

import com.iliyadev.springboot.models.*
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 WebSocket Message DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class WsMessage(
    val id: UUID = UUID.randomUUID(),
    val chatId: UUID,
    val senderId: UUID,
    val senderName: String,
    val senderAvatar: String? = null,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,  // CRITICAL: Required for AUDIO, VIDEO, IMAGE, FILE types
    val poll: PollDto? = null,     // CRITICAL: Required for POLL type
    val amplitudes: List<Int>? = null, // For VOICE/AUDIO waveform
    val timestamp: Instant = Instant.now(),
    val replyToMessageId: UUID? = null,
    val replyToSenderName: String? = null,
    val replyToContent: String? = null
)

data class WsTypingStatus(
    val chatId: UUID,
    val userId: UUID,
    val userName: String,
    val isTyping: Boolean
)

data class WsOnlineStatus(
    val userId: UUID,
    val isOnline: Boolean,
    val lastSeen: Instant?
)

data class WsReadReceipt(
    val chatId: UUID,
    val messageId: UUID,
    val userId: UUID
)

/**
 * DTO for chat-level WebSocket events (new chat created, chat updated, etc.)
 */
data class WsChatEvent(
    val event: String, // "CHAT_CREATED", "CHAT_UPDATED", "CHAT_DELETED"
    val id: UUID,
    val type: String, // "PRIVATE", "GROUP", etc.
    val title: String,
    val avatarUrl: String? = null,
    val participants: List<WsChatParticipant> = emptyList(),
    val lastMessageContent: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

data class WsChatParticipant(
    val id: UUID,
    val username: String,
    val displayName: String,
    val phoneNumber: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false
)

/**
 * DTO for group member update WebSocket events (member added, removed, role changed)
 */
data class WsGroupMemberEvent(
    val event: String, // "MEMBER_ADDED", "MEMBER_REMOVED", "ROLE_CHANGED"
    val groupId: UUID,
    val memberId: UUID,
    val memberName: String,
    val memberAvatar: String? = null,
    val role: String? = null, // For ROLE_CHANGED
    val newMemberCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO for channel subscriber update WebSocket events (admin added/removed)
 */
data class WsChannelSubscriberEvent(
    val event: String, // "ADMIN_ADDED", "ADMIN_REMOVED"
    val channelId: UUID,
    val userId: UUID,
    val isAdmin: Boolean
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔌 WebSocket Message Handler
// ═══════════════════════════════════════════════════════════════════════════════

@Controller
class WebSocketMessageHandler(
    private val messagingTemplate: SimpMessagingTemplate
) {
    // ... (existing code) ...

    /**
     * Broadcast a channel subscriber update event to all subscribers.
     */
    fun broadcastChannelSubscriberUpdate(channelId: UUID, event: WsChannelSubscriberEvent, subscriberIds: List<UUID>) {
        // We broadcast to the channel topic
        messagingTemplate.convertAndSend("/topic/channel/$channelId/subscribers", event)
        
        // Also send to individual user queues for redundancy if needed
        subscriberIds.forEach { userId ->
             messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                mapOf(
                    "type" to "CHANNEL_SUBSCRIBER_UPDATE",
                    "data" to event
                )
            )
        }
    }
    
    // ... (existing code) ...
    private val logger = org.slf4j.LoggerFactory.getLogger(WebSocketMessageHandler::class.java)
    @MessageMapping("/chat/{chatId}/message")
    @SendTo("/topic/chat/{chatId}/messages")
    fun handleChatMessage(
        @DestinationVariable chatId: UUID,
        @Payload message: WsMessage
    ): WsMessage {
        return message.copy(timestamp = Instant.now())
    }
    @MessageMapping("/chat/{chatId}/typing")
    @SendTo("/topic/chat/{chatId}/typing")
    fun handleTypingStatus(
        @DestinationVariable chatId: UUID,
        @Payload typingStatus: WsTypingStatus
    ): WsTypingStatus {
        return typingStatus
    }
    @MessageMapping("/chat/{chatId}/read")
    @SendTo("/topic/chat/{chatId}/read")
    fun handleReadReceipt(
        @DestinationVariable chatId: UUID,
        @Payload readReceipt: WsReadReceipt
    ): WsReadReceipt {
        return readReceipt
    }
    @MessageMapping("/group/{groupId}/message")
    @SendTo("/topic/group/{groupId}/messages")
    fun handleGroupMessage(
        @DestinationVariable groupId: UUID,
        @Payload message: WsMessage
    ): WsMessage {
        return message.copy(timestamp = Instant.now())
    }
    @MessageMapping("/group/{groupId}/typing")
    @SendTo("/topic/group/{groupId}/typing")
    fun handleGroupTypingStatus(
        @DestinationVariable groupId: UUID,
        @Payload typingStatus: WsTypingStatus
    ): WsTypingStatus {
        return typingStatus
    }
    
    /**
     * Send a private message to a specific user
     */
    fun sendPrivateMessage(userId: UUID, message: WsMessage) {
        logger.info("📤 Sending private message to user: $userId, messageId: ${message.id}, chatId: ${message.chatId}")
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages",
            message
        )
        logger.info("✅ Private message sent to /user/$userId/queue/messages")
    }
    
    /**
     * Notify a user about a new chat being created with them.
     * Called when another user initiates a private chat.
     */
    fun notifyNewChat(userId: UUID, chatEvent: WsChatEvent) {
        logger.info("📤 Notifying new chat to user: $userId, chatId: ${chatEvent.id}")
        
        // Primary: User-specific queue
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/chats",
            chatEvent
        )
        
        // Fallback: Direct topic broadcast (in case user queue doesn't work)
        messagingTemplate.convertAndSend("/topic/user/$userId/chats", chatEvent)
        
        logger.info("✅ New chat notification sent via both user queue and topic fallback")
    }
    
    /**
     * Notify a user about chat updates (new message arrived, unread count changed).
     */
    fun notifyChatUpdate(userId: UUID, chatEvent: WsChatEvent) {
        logger.info("📤 Notifying chat update to user: $userId, chatId: ${chatEvent.id}, lastMessage: ${chatEvent.lastMessageContent?.take(20)}")
        
        // Primary: User-specific queue
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/chats",
            chatEvent
        )
        
        // Fallback: Direct topic broadcast (in case user queue doesn't work)
        messagingTemplate.convertAndSend("/topic/user/$userId/chats", chatEvent)
        
        logger.info("✅ Chat update notification sent via both user queue and topic fallback")
    }
    
    /**
     * Broadcast a message to all participants in a chat except the sender.
     * Uses MULTIPLE delivery methods for maximum reliability:
     * 
     * 1. User queue: /user/{userId}/queue/messages (direct delivery)
     * 2. User topic: /topic/user/{userId}/messages (fallback for user queue issues)
     * 3. Chat topic: /topic/chat/{chatId}/messages (for users viewing the chat)
     */
    fun broadcastMessageToChat(chatId: UUID, message: WsMessage, recipientIds: List<UUID>) {
        logger.info("📤 Broadcasting message ${message.id} to ${recipientIds.size} recipients in chat $chatId")
        
        // Method 1: User Queue (primary) - works even when not viewing the chat
        recipientIds.forEach { recipientId ->
            logger.info("   → Sending to user queue: /user/$recipientId/queue/messages")
            messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                message
            )
            
            // Method 2: User Topic (fallback) - more reliable for some STOMP implementations
            logger.info("   → Sending to user topic fallback: /topic/user/$recipientId/messages")
            messagingTemplate.convertAndSend("/topic/user/$recipientId/messages", message)
        }
        
        // Method 3: Chat Topic Broadcast - for users actively viewing the chat
        logger.info("   → Also broadcasting to chat topic: /topic/chat/$chatId/messages")
        messagingTemplate.convertAndSend("/topic/chat/$chatId/messages", message)
        
        logger.info("✅ Broadcast complete for message ${message.id}")
    }
    
    fun sendOnlineStatus(status: WsOnlineStatus) {
        messagingTemplate.convertAndSend("/topic/online", status)
    }
    
    /**
     * Broadcast a group message to all group members except the sender.
     * Uses BOTH user-level queues AND topic broadcast for maximum reliability.
     */
    fun broadcastGroupMessage(groupId: UUID, message: WsMessage, memberIds: List<UUID>) {
        logger.info("📤 Broadcasting group message ${message.id} to ${memberIds.size} members in group $groupId")
        
        // Method 1: User Queue (primary)
        memberIds.forEach { memberId ->
            logger.info("   → Sending group message to user: $memberId")
            messagingTemplate.convertAndSendToUser(
                memberId.toString(),
                "/queue/messages",
                message
            )
        }
        
        // Method 2: Topic Broadcast (fallback/primary for active viewers)
        logger.info("   → Also broadcasting to topic: /topic/group/$groupId/messages")
        messagingTemplate.convertAndSend("/topic/group/$groupId/messages", message)
        
        logger.info("✅ Group message broadcast complete for ${message.id}")
    }
    
    /**
     * Notify subscribers about a new channel post.
     * Broadcasts to all subscribers via user-level queues and Topic.
     */
    fun broadcastChannelPost(channelId: UUID, post: ChannelPostDto, subscriberIds: List<UUID>) {
        logger.info("📤 Broadcasting channel post ${post.id} to ${subscriberIds.size} subscribers in channel $channelId")
        
        // Method 1: User Queue
        subscriberIds.forEach { subscriberId ->
            logger.info("   → Sending channel post to user: $subscriberId")
            messagingTemplate.convertAndSendToUser(
                subscriberId.toString(),
                "/queue/messages",
                post
            )
        }
        
        // Method 2: Topic Broadcast
        logger.info("   → Also broadcasting to topic: /topic/channel/$channelId/posts")
        messagingTemplate.convertAndSend("/topic/channel/$channelId/posts", post)
        
        logger.info("✅ Channel post broadcast complete for ${post.id}")
    }
    
    fun notifyNewChannelPost(channelId: UUID, post: ChannelPostDto) {
        // This might be redundant if broadcastChannelPost calls this or vice versa, but keeping for safety
        messagingTemplate.convertAndSend("/topic/channel/$channelId/posts", post)
    }
    
    /**
     * Send a read receipt notification to a specific user.
     * This notifies the original message sender that their message was read,
     * enabling real-time "blue tick" updates.
     */
    fun sendReadReceiptToUser(userId: UUID, readEvent: Map<String, Any>) {
        logger.info("👁️ Sending read receipt to user: $userId, event: $readEvent")
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/messages",
            readEvent
        )
        logger.info("✅ Read receipt sent to /user/$userId/queue/messages")
    }

    /**
     * Broadcast a group member update event to all group members.
     * Used for real-time member list updates when members are added/removed.
     */
    fun broadcastGroupMemberUpdate(groupId: UUID, event: WsGroupMemberEvent, memberIds: List<UUID>) {
        logger.info("📤 Broadcasting group member update (${event.event}) to ${memberIds.size} members in group $groupId")
        memberIds.forEach { memberId ->
            logger.info("   → Sending member update to user: $memberId")
            messagingTemplate.convertAndSendToUser(
                memberId.toString(),
                "/queue/messages",
                mapOf(
                    "type" to "GROUP_MEMBER_UPDATE",
                    "data" to event
                )
            )
        }
        // Also broadcast to group topic for any subscribers watching
        messagingTemplate.convertAndSend("/topic/group/$groupId/members", event)
        logger.info("✅ Group member update broadcast complete for ${event.event}")
    }
    /**
     * Send a notification to a specific user.
     * Used for real-time notification delivery (follow, collaboration, etc.)
     */
    fun sendNotification(userId: UUID, notification: Any) {
        logger.info("🔔 Sending notification to user: $userId")
        
        // Method 1: User Queue
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        )
        
        // Method 2: Topic Broadcast (Fallback)
        messagingTemplate.convertAndSend("/topic/user/$userId/notifications", notification)
        
        logger.info("✅ Notification sent to /user/$userId/queue/notifications AND /topic/user/$userId/notifications")
    }
}

