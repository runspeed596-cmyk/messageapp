package com.iliyadev.springboot.config

import com.iliyadev.springboot.config.security.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 🔌 WebSocket Configuration - STOMP over WebSocket
// ═══════════════════════════════════════════════════════════════════════════════

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtTokenUtils: JwtTokenUtils
) : WebSocketMessageBrokerConfigurer {
    
    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)
    
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        logger.info("🔧 Configuring message broker...")
        registry.enableSimpleBroker("/topic", "/queue")
            .setHeartbeatValue(longArrayOf(10000, 10000))
            .setTaskScheduler(org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler().apply {
                poolSize = 1
                setThreadNamePrefix("ws-heartbeat-")
                initialize()
            })
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
        logger.info("✅ Message broker configured")
    }
    
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        logger.info("🔧 Registering STOMP endpoints...")
        registry.addEndpoint("/ws").setAllowedOrigins("*")
        registry.addEndpoint("/ws-sockjs").setAllowedOrigins("*").withSockJS()
        logger.info("✅ WebSocket endpoints registered")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        logger.info("🔧 Configuring client inbound channel with auth interceptor...")
        registration.interceptors(StompAuthInterceptor(jwtTokenUtils))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 STOMP Authentication Interceptor
// ═══════════════════════════════════════════════════════════════════════════════

class StompAuthInterceptor(
    private val jwtTokenUtils: JwtTokenUtils
) : ChannelInterceptor {
    
    private val logger = LoggerFactory.getLogger(StompAuthInterceptor::class.java)
    
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)
        
        when (accessor.command) {
            StompCommand.CONNECT -> {
                val newMessage = handleConnect(accessor, message)
                if (newMessage != null) return newMessage
            }
            StompCommand.SUBSCRIBE -> handleSubscribe(accessor)
            StompCommand.SEND -> handleSend(accessor)
            StompCommand.DISCONNECT -> handleDisconnect(accessor)
            else -> {}
        }
        
        return message
    }
    
    private fun handleConnect(accessor: StompHeaderAccessor, originalMessage: Message<*>): Message<*>? {
        logger.info("🔌 STOMP CONNECT received")
        val authHeader = accessor.getFirstNativeHeader("Authorization")
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                if (jwtTokenUtils.validateToken(token)) {
                    val userId = jwtTokenUtils.getUserIdFromToken(token)
                    accessor.user = UserPrincipal(UUID.fromString(userId), userId)
                    logger.info("✅ STOMP CONNECT authenticated: userId=$userId")
                    return org.springframework.messaging.support.MessageBuilder.createMessage(
                        originalMessage.payload,
                        accessor.messageHeaders
                    )
                }
            } catch (e: Exception) {
                logger.error("❌ STOMP CONNECT error: ${e.message}")
            }
        }
        return null
    }
    
    private fun handleSubscribe(accessor: StompHeaderAccessor) {
        logger.info("📡 STOMP SUBSCRIBE: destination=${accessor.destination}, user=${accessor.user?.name ?: "anonymous"}")
    }
    
    private fun handleSend(accessor: StompHeaderAccessor) {
        logger.info("📨 STOMP SEND: destination=${accessor.destination}, user=${accessor.user?.name ?: "anonymous"}")
    }
    
    private fun handleDisconnect(accessor: StompHeaderAccessor) {
        logger.info("🔌 STOMP DISCONNECT: user=${accessor.user?.name ?: "anonymous"}")
    }
}
