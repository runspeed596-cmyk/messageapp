package com.iliyadev.springboot.websocket

import com.iliyadev.springboot.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * WebSocket Event Listener for tracking user online/offline status.
 * 
 * Listens to WebSocket connection events and broadcasts online status changes
 * to all connected clients via /topic/online.
 */
@Component
class WebSocketEventListener(
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)
    
    // Track userId for each session (sessionId -> userId)
    private val sessionUserMap = ConcurrentHashMap<String, UUID>()
    
    /**
     * Called when a WebSocket session is established.
     * We extract the userId from the session attributes or the first subscription.
     */
    @EventListener
    fun handleSessionConnected(event: SessionConnectedEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId
        
        logger.info("🔌 WebSocket session connected: $sessionId")
        
        // Note: We'll set the userId when the user subscribes to their queue
        // since the userId is not available in the connect event for anonymous users
    }
    
    /**
     * Called when a user subscribes to a destination.
     * We use this to extract the userId and update online status.
     */
    @EventListener
    fun handleSessionSubscribe(event: SessionSubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val destination = accessor.destination ?: return
        
        // Extract userId from user queue subscription
        // Format: /user/{userId}/queue/messages or /topic/user/{userId}/chats
        val userIdMatch = Regex("/(?:user|topic/user)/([a-f0-9-]{36})/").find(destination)
        
        if (userIdMatch != null && !sessionUserMap.containsKey(sessionId)) {
            try {
                val userId = UUID.fromString(userIdMatch.groupValues[1])
                sessionUserMap[sessionId] = userId
                
                logger.info("👤 User $userId connected (session: $sessionId)")
                
                // Update user online status in database
                updateUserOnlineStatus(userId, true)
                
                // Broadcast online status to all clients
                broadcastOnlineStatus(userId, true)
            } catch (e: Exception) {
                logger.error("Error parsing userId from destination: $destination", e)
            }
        }
    }
    
    /**
     * Called when a WebSocket session is disconnected.
     * Updates the user's online status and broadcasts to all clients.
     */
    @EventListener
    fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        
        val userId = sessionUserMap.remove(sessionId)
        if (userId != null) {
            logger.info("👋 User $userId disconnected (session: $sessionId)")
            
            // Check if user has other active sessions
            val hasOtherSessions = sessionUserMap.values.contains(userId)
            
            if (!hasOtherSessions) {
                // Update user offline status in database
                updateUserOnlineStatus(userId, false)
                
                // Broadcast offline status to all clients
                broadcastOnlineStatus(userId, false)
            } else {
                logger.info("   User $userId still has other active sessions")
            }
        }
    }
    
    /**
     * Update user's online status in the database.
     */
    private fun updateUserOnlineStatus(userId: UUID, isOnline: Boolean) {
        try {
            val user = userRepository.findById(userId).orElse(null) ?: return
            
            user.isOnline = isOnline
            if (!isOnline) {
                user.lastSeen = Instant.now()
            }
            
            userRepository.save(user)
            logger.info("   💾 Updated user $userId: isOnline=$isOnline")
        } catch (e: Exception) {
            logger.error("Error updating user online status", e)
        }
    }
    
    /**
     * Broadcast user's online status to all connected clients.
     */
    private fun broadcastOnlineStatus(userId: UUID, isOnline: Boolean) {
        val status = WsOnlineStatus(
            userId = userId,
            isOnline = isOnline,
            lastSeen = if (!isOnline) Instant.now() else null
        )
        
        messagingTemplate.convertAndSend("/topic/online", status)
        logger.info("   📡 Broadcasted online status: $userId isOnline=$isOnline")
    }
}
