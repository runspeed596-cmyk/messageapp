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

/**
 * DTO for story WebSocket events (new story, story deleted)
 */
data class WsStoryEvent(
    val event: String, // "STORY_CREATED", "STORY_DELETED"
    val storyId: UUID,
    val userId: UUID,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val mediaUrl: String? = null,
    val type: String? = null, // IMAGE, VIDEO, TEXT
    val caption: String? = null,
    val durationSeconds: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000
)

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
    val replyToContent: String? = null,
    val actionLabel: String? = null,  // For buttons/links
    val actionUrl: String? = null,     // For navigation
    val timerTargetAt: Instant? = null // For countdown timer
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

/**
 * DTO for real-time reaction updates on messages.
 * Broadcast to all chat participants when a reaction is added/changed/removed.
 */
data class WsReactionEvent(
    val messageId: UUID,
    val chatId: UUID,
    val userId: UUID,
    val userName: String,
    val reaction: String?,  // null = reaction removed
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO for real-time course capacity updates (Mosbat Elm)
 */
data class WsCourseCapacityUpdateEvent(
    val event: String, // "CAPACITY_UPDATED"
    val courseId: UUID,
    val currentEnrollment: Int,
    val capacity: Int,
    val timestamp: Long = System.currentTimeMillis()
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
        
        // Also send to individual user topics for redundancy
        subscriberIds.forEach { userId ->
            messagingTemplate.convertAndSend(
                "/topic/user/$userId/messages",
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
        messagingTemplate.convertAndSend(
            "/topic/user/$userId/messages",
            message
        )
        logger.info("✅ Private message sent to /topic/user/$userId/messages")
    }
    
    /**
     * Notify a user about a new chat being created with them.
     * Sends via /queue/messages with type=CHAT_CREATED so client's unified handler dispatches it.
     */
    fun notifyNewChat(userId: UUID, chatEvent: WsChatEvent) {
        logger.info("📤 Notifying new chat to user: $userId, chatId: ${chatEvent.id}")
        messagingTemplate.convertAndSend(
            "/topic/user/$userId/messages",
            mapOf(
                "type" to "CHAT_UPDATE",
                "event" to chatEvent.event,
                "id" to chatEvent.id.toString(),
                "chatType" to chatEvent.type,
                "title" to chatEvent.title,
                "avatarUrl" to chatEvent.avatarUrl,
                "participants" to chatEvent.participants,
                "lastMessageContent" to chatEvent.lastMessageContent,
                "lastMessageTime" to chatEvent.lastMessageTime,
                "unreadCount" to chatEvent.unreadCount,
                "isPinned" to chatEvent.isPinned,
                "isMuted" to chatEvent.isMuted,
                "isArchived" to chatEvent.isArchived,
                "updatedAt" to chatEvent.updatedAt
            )
        )
        logger.info("✅ New chat notification sent to /topic/user/$userId/messages")
    }
    
    /**
     * Notify a user about chat updates (new message arrived, unread count changed).
     * Sends via /queue/messages with type=CHAT_UPDATE so client's unified handler dispatches it.
     */
    fun notifyChatUpdate(userId: UUID, chatEvent: WsChatEvent) {
        logger.info("📤 Notifying chat update to user: $userId, chatId: ${chatEvent.id}, lastMessage: ${chatEvent.lastMessageContent?.take(20)}")
        messagingTemplate.convertAndSend(
        "/topic/user/$userId/messages",
        mapOf(
            "type" to "CHAT_UPDATE",
            "event" to chatEvent.event,
            "id" to chatEvent.id.toString(),
            "chatType" to chatEvent.type,
            "title" to chatEvent.title,
            "avatarUrl" to chatEvent.avatarUrl,
            "participants" to chatEvent.participants,
            "lastMessageContent" to chatEvent.lastMessageContent,
            "lastMessageTime" to chatEvent.lastMessageTime,
            "unreadCount" to chatEvent.unreadCount,
            "isPinned" to chatEvent.isPinned,
            "isMuted" to chatEvent.isMuted,
            "isArchived" to chatEvent.isArchived,
            "updatedAt" to chatEvent.updatedAt
        )
    )
    logger.info("✅ Chat update notification sent to /topic/user/$userId/messages")
    }
    
    /**
     * Broadcast a message to all participants in a chat except the sender.
     * Uses user queue for reliable, deduplicated delivery.
     */
    fun broadcastMessageToChat(chatId: UUID, message: WsMessage, recipientIds: List<UUID>) {
        logger.info("📤 Broadcasting message ${message.id} to ${recipientIds.size} recipients in chat $chatId")
        
        recipientIds.forEach { recipientId ->
            logger.info("   → Sending to topic: /topic/user/$recipientId/messages")
            messagingTemplate.convertAndSend(
                "/topic/user/$recipientId/messages",
                message
            )
        }
        
        logger.info("✅ Broadcast complete for message ${message.id}")
    }
    
    fun sendOnlineStatus(status: WsOnlineStatus) {
        messagingTemplate.convertAndSend("/topic/online-status", status)
    }
    
    /**
     * Broadcast a group message to all group members except the sender.
     * Uses BOTH user-level queues AND topic broadcast for maximum reliability.
     */
    fun broadcastGroupMessage(groupId: UUID, message: WsMessage, memberIds: List<UUID>) {
        logger.info("📤 Broadcasting group message ${message.id} to ${memberIds.size} members in group $groupId")
        
        memberIds.forEach { memberId ->
            logger.info("   → Sending group message to user: $memberId")
            messagingTemplate.convertAndSend(
                "/topic/user/$memberId/messages",
                message
            )
        }
        
        logger.info("✅ Group message broadcast complete for ${message.id}")
    }
    
    /**
     * Notify subscribers about a new channel post.
     * Broadcasts to all subscribers via user-level queues and Topic.
     */
    fun broadcastChannelPost(channelId: UUID, post: ChannelPostDto, subscriberIds: List<UUID>) {
        logger.info("📤 Broadcasting channel post ${post.id} to ${subscriberIds.size} subscribers in channel $channelId")
        
        subscriberIds.forEach { subscriberId ->
            logger.info("   → Sending channel post to user: $subscriberId")
            messagingTemplate.convertAndSend(
                "/topic/user/$subscriberId/messages",
                post
            )
        }
        
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
        messagingTemplate.convertAndSend(
            "/topic/user/$userId/messages",
            readEvent
        )
        logger.info("✅ Read receipt sent to /topic/user/$userId/messages")
    }

    /**
     * Broadcast a group member update event to all group members.
     * Used for real-time member list updates when members are added/removed.
     */
    fun broadcastGroupMemberUpdate(groupId: UUID, event: WsGroupMemberEvent, memberIds: List<UUID>) {
        logger.info("📤 Broadcasting group member update (${event.event}) to ${memberIds.size} members in group $groupId")
        memberIds.forEach { memberId ->
            logger.info("   → Sending member update to user: $memberId")
            messagingTemplate.convertAndSend(
                "/topic/user/$memberId/messages",
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
        // Direct topic routing — no convertAndSendToUser
        messagingTemplate.convertAndSend("/topic/user/$userId/notifications", notification)
        logger.info("✅ Notification sent to /topic/user/$userId/notifications")
    }
    
    /**
     * Broadcast a message deletion to all participants in a chat.
     * Enables real-time removal of deleted messages on all devices.
     */
    fun broadcastMessageDeletion(chatId: UUID, messageId: UUID, recipientIds: List<UUID>) {
        logger.info("🗑️ Broadcasting message deletion: messageId=$messageId in chat $chatId to ${recipientIds.size} recipients")
        val deleteEvent = mapOf(
            "type" to "MESSAGE_DELETED",
            "chatId" to chatId.toString(),
            "messageId" to messageId.toString(),
            "timestamp" to System.currentTimeMillis()
        )
        recipientIds.forEach { recipientId ->
            messagingTemplate.convertAndSend(
                "/topic/user/$recipientId/messages",
                deleteEvent
            )
        }
        logger.info("✅ Message deletion broadcast complete")
    }
    
    /**
     * Broadcast a new story event to all contacts/followers.
     * Enables real-time story updates without polling.
     */
    fun broadcastNewStory(storyEvent: WsStoryEvent, recipientIds: List<UUID>) {
        logger.info("📸 Broadcasting new story from ${storyEvent.userId} to ${recipientIds.size} recipients")
        val payload = mapOf(
            "type" to "STORY_CREATED",
            "data" to storyEvent
        )
        recipientIds.forEach { recipientId ->
            messagingTemplate.convertAndSend(
                "/topic/user/$recipientId/messages",
                payload
            )
        }
        logger.info("✅ Story broadcast complete")
    }
    
    /**
     * Notify contacts that a story was deleted.
     */
    fun notifyStoryDeleted(storyEvent: WsStoryEvent, recipientIds: List<UUID>) {
        logger.info("🗑️ Broadcasting story deletion: storyId=${storyEvent.storyId}")
        val payload = mapOf(
            "type" to "STORY_DELETED",
            "data" to storyEvent
        )
        recipientIds.forEach { recipientId ->
            messagingTemplate.convertAndSend(
                "/topic/user/$recipientId/messages",
                payload
            )
        }
        logger.info("✅ Story deletion broadcast complete")
    }
    
    /**
     * Broadcast a reaction update to all participants in a chat.
     * Enables real-time reaction display on all devices.
     */
    fun broadcastReactionUpdate(chatId: UUID, event: WsReactionEvent, recipientIds: List<UUID>) {
        logger.info("💬 Broadcasting reaction update: messageId=${event.messageId} in chat $chatId to ${recipientIds.size} recipients")
        val payload = mapOf(
            "type" to "REACTION_UPDATE",
            "data" to event
        )
        recipientIds.forEach { recipientId ->
            messagingTemplate.convertAndSend(
                "/topic/user/$recipientId/messages",
                payload
            )
        }
        logger.info("✅ Reaction update broadcast complete")
    }

    /**
     * Broadcast a course capacity update to all clients watching the course or mosbat elm home.
     * We'll broadcast it to public topics.
     */
    fun broadcastCourseCapacityUpdate(courseId: UUID, currentEnrollment: Int, capacity: Int) {
        val event = WsCourseCapacityUpdateEvent(
            event = "CAPACITY_UPDATED",
            courseId = courseId,
            currentEnrollment = currentEnrollment,
            capacity = capacity
        )
        logger.info("📈 Broadcasting course capacity update for $courseId: $currentEnrollment/$capacity")
        // Broadcast to specific course topic
        messagingTemplate.convertAndSend("/topic/courses/$courseId/capacity", event)
        // Broadcast to general mosbat-elm capacity topic
        messagingTemplate.convertAndSend("/topic/mosbat-elm/courses/capacity", event)
    }
}

