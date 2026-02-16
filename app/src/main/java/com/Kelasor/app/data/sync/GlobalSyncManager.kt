package com.Kelasor.app.data.sync

import android.util.Log
import com.Kelasor.app.data.local.dao.*
import com.Kelasor.app.data.websocket.ConnectionState
import com.Kelasor.app.data.websocket.WebSocketManager
import com.Kelasor.app.data.websocket.WebSocketMessage
import com.Kelasor.app.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 🌐 Global Sync Manager
// Responsible for maintaining WebSocket connection and syncing data in background
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class GlobalSyncManager @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val sessionManager: SessionManager,
    private val messageDao: MessageDao,
    private val groupMessageDao: GroupMessageDao,
    private val channelPostDao: ChannelPostDao,
    private val userDao: UserDao,
    private val groupDao: GroupDao,
    private val channelDao: ChannelDao
) {
    companion object {
        private const val TAG = "GlobalSyncManager"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    
    fun initialize() {
        if (isInitialized) {
            Log.e(TAG, "Already initialized, skipping...")
            return
        }
        isInitialized = true
        
        Log.e(TAG, "🌐 Initializing GlobalSyncManager...")
        
        // Observe session changes and connect/disconnect WebSocket accordingly
        scope.launch {
            Log.e(TAG, "📡 Starting to observe accessToken...")
            sessionManager.accessToken.collectLatest { token ->
                Log.e(TAG, "🔑 Token state changed: hasToken=${token != null}, tokenLength=${token?.length ?: 0}")
                if (token != null) {
                    Log.e(TAG, "✅ Token available (${token.take(20)}...), connecting WebSocket...")
                    try {
                        webSocketManager.connect(token)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to call webSocketManager.connect: ${e.message}", e)
                    }
                } else {
                    Log.e(TAG, "⚠️ No token, disconnecting WebSocket...")
                    webSocketManager.disconnect()
                }
            }
        }
        
        // Observe WebSocket connection state
        scope.launch {
            webSocketManager.connectionState.collectLatest { state ->
                when (state) {
                    ConnectionState.CONNECTED -> {
                        Log.e(TAG, "✅ WebSocket connected - ready for real-time updates")
                        // Re-subscribe to all groups/channels upon (re)connection
                        startSubscriptionSync()
                    }
                    ConnectionState.DISCONNECTED -> {
                        Log.e(TAG, "⚠️ WebSocket disconnected")
                        // Auto-reconnect after a short delay if we have a token
                        scope.launch {
                            kotlinx.coroutines.delay(3000)
                            val currentToken = sessionManager.accessToken.first()
                            if (currentToken != null) {
                                Log.e(TAG, "🔄 Auto-reconnecting WebSocket after disconnect...")
                                try {
                                    webSocketManager.connect(currentToken)
                                } catch (e: Exception) {
                                    Log.e(TAG, "❌ Auto-reconnect failed: ${e.message}")
                                }
                            }
                        }
                    }
                    ConnectionState.CONNECTING -> {
                        Log.e(TAG, "🔄 WebSocket connecting...")
                    }
                    ConnectionState.ERROR -> {
                        Log.e(TAG, "❌ WebSocket connection error")
                        // Also auto-reconnect on error
                        scope.launch {
                            kotlinx.coroutines.delay(5000)
                            val currentToken = sessionManager.accessToken.first()
                            if (currentToken != null) {
                                Log.e(TAG, "🔄 Auto-reconnecting WebSocket after error...")
                                try {
                                    webSocketManager.connect(currentToken)
                                } catch (e: Exception) {
                                    Log.e(TAG, "❌ Auto-reconnect failed: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Observe incoming WebSocket messages and persist to Room
        scope.launch {
            webSocketManager.messages.collectLatest { message ->
                handleWebSocketMessage(message)
            }
        }
        
        Log.e(TAG, "✅ GlobalSyncManager initialized successfully")
    }

    private fun startSubscriptionSync() {
        // Observe all joined groups and subscribe to their topics
        scope.launch {
            // Assuming we have a DAO method to get all groups the user is part of. 
            // If not, we might need to rely on what is locally available or a specific query.
            // Using observeAllGroups() or similar if available, or just getting them once + updates.
            // Here we assume observeSubscribedChannels works for channels.
            // For groups, we should have something like observeAllGroups().
            
            // NOTE: We should ideally only subscribe once per connection, or when the list changes.
            // WebSocketManager manages idempotent subscriptions (checks activeSubscriptions set).
            
            launch {
                groupDao.observeAllGroups().collectLatest { groups ->
                    groups.forEach { group ->
                        // Log.d(TAG, "🔌 Auto-subscribing to group: ${group.id}")
                        webSocketManager.subscribeToGroup(group.id)
                    }
                }
            }
            
            launch {
                channelDao.observeSubscribedChannels().collectLatest { channels ->
                    channels.forEach { channel ->
                        // Log.d(TAG, "🔌 Auto-subscribing to channel: ${channel.id}")
                        webSocketManager.subscribeToChannel(channel.id)
                    }
                }
            }
        }
    }
    
    private suspend fun handleWebSocketMessage(message: WebSocketMessage) {
        try {
            when (message) {
                is WebSocketMessage.ChatMessage -> {
                    // Already saved by WebSocketManager.handleChatMessage() - just log
                    Log.d(TAG, "📨 Chat message event received: ${message.message.id}")
                }
                is WebSocketMessage.GroupMessage -> {
                    // Already saved by WebSocketManager.handleGroupMessage() - just log
                    Log.d(TAG, "👥 Group message event received: ${message.message.id}")
                }
                is WebSocketMessage.ChannelPost -> {
                    // Already saved by WebSocketManager.handleChannelPost() - just log
                    Log.d(TAG, "📢 Channel post event received: ${message.post.id}")
                }
                is WebSocketMessage.UserOnline -> {
                    Log.d(TAG, "👤 User online status: ${message.userId} -> ${message.isOnline}")
                    userDao.updateOnlineStatus(message.userId, message.isOnline, message.lastSeen)
                }
                is WebSocketMessage.Typing -> {
                    // Typing events are handled in the UI, not persisted
                    Log.d(TAG, "⌨️ Typing: ${message.userName} in ${message.chatId}")
                }
                is WebSocketMessage.MessageRead -> {
                    Log.d(TAG, "✓ Message read: ${message.messageId}")
                    messageDao.updateMessageStatus(message.messageId, "READ", true)
                }
                is WebSocketMessage.GroupMemberUpdate -> {
                    // Group member updates are handled in WebSocketManager - just log
                    Log.d(TAG, "👥 Group member update: ${message.event} in ${message.groupId}")
                }
                is WebSocketMessage.ChatCreated -> {
                    Log.d(TAG, "🆕 Chat created/updated: ${message.chat.id}")
                }
                is WebSocketMessage.GroupCreated -> {
                    Log.d(TAG, "🆕 Group created/updated: ${message.group.id}")
                }
                is WebSocketMessage.ChannelCreated -> {
                    Log.d(TAG, "🆕 Channel created/updated: ${message.channel.id}")
                }
                is WebSocketMessage.MessageDeleted -> {
                    Log.d(TAG, "🗑️ Message deleted: ${message.messageId} in chat ${message.chatId}")
                }
                is WebSocketMessage.StoryEvent -> {
                    Log.d(TAG, "📸 Story event: ${message.event} by ${message.userId}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling WebSocket message: ${e.message}", e)
        }
    }
    
    fun subscribeToGroup(groupId: String) {
        webSocketManager.subscribeToGroup(groupId)
    }
    
    fun subscribeToChannel(channelId: String) {
        webSocketManager.subscribeToChannel(channelId)
    }
    
    fun sendTypingStatus(chatId: String, isTyping: Boolean) {
        webSocketManager.sendTypingStatus(chatId, isTyping)
    }
}
