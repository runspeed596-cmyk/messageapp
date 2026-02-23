package com.Kelasor.app.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.Kelasor.app.data.local.dao.*
import com.Kelasor.app.data.local.entity.ChannelPostEntity
import com.Kelasor.app.data.local.entity.ChatEntity
import com.Kelasor.app.data.local.entity.GroupMessageEntity
import com.Kelasor.app.data.local.entity.MessageEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 WebSocket Connection State
// ═══════════════════════════════════════════════════════════════════════════════

enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    ERROR
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 WebSocket Message Types
// ═══════════════════════════════════════════════════════════════════════════════

sealed class WebSocketMessage {
    data class ChatMessage(val message: MessageEntity) : WebSocketMessage()
    data class GroupMessage(val message: GroupMessageEntity) : WebSocketMessage()
    data class ChannelPost(val post: ChannelPostEntity) : WebSocketMessage()
    data class UserOnline(val userId: String, val isOnline: Boolean, val lastSeen: Long?) : WebSocketMessage()
    data class Typing(val chatId: String, val userId: String, val userName: String, val isTyping: Boolean) : WebSocketMessage()
    data class MessageRead(val chatId: String, val messageId: String, val userId: String) : WebSocketMessage()
    data class GroupMemberUpdate(val groupId: String, val event: String, val memberId: String, val memberName: String, val newMemberCount: Int) : WebSocketMessage()
    
    // New Entity Events
    data class ChatCreated(val chat: ChatEntity) : WebSocketMessage()
    data class GroupCreated(val group: com.Kelasor.app.data.local.entity.GroupEntity) : WebSocketMessage()
    data class ChannelCreated(val channel: com.Kelasor.app.data.local.entity.ChannelEntity) : WebSocketMessage()
    data class MessageDeleted(val chatId: String, val messageId: String) : WebSocketMessage()
    data class StoryEvent(val event: String, val storyId: String, val userId: String) : WebSocketMessage()
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 WebSocket Manager - STOMP Protocol
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class WebSocketManager @Inject constructor(
    private val messageDao: MessageDao,
    private val groupMessageDao: GroupMessageDao,
    private val channelPostDao: ChannelPostDao,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val groupDao: GroupDao,
    private val channelDao: ChannelDao,
    private val channelSubscriberDao: ChannelSubscriberDao,
    private val contactsRepository: com.Kelasor.app.data.repository.ContactsRepository,
    private val sessionManager: com.Kelasor.app.data.session.SessionManager,
    private val notificationHelper: com.Kelasor.app.data.notification.NotificationHelper,
    private val notificationBadgeManager: com.Kelasor.app.data.notification.NotificationBadgeManager,

    private val soundPlayer: com.Kelasor.app.data.media.SoundPlayer,
    private val currentChatManager: com.Kelasor.app.data.session.CurrentChatManager,
    private val gson: Gson
) {
    companion object {
        private const val TAG = "WebSocketManager"
        private const val WS_BASE_URL = "ws://192.168.70.113:8080/ws"
        private const val RECONNECT_DELAY_MS = 2000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }
    
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "🔥 Uncaught exception in WebSocketManager scope: ${throwable.message}", throwable)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    private var stompSession: StompSession? = null
    private var stompClient: StompClient? = null
    private var accessToken: String? = null
    private var reconnectAttempts = 0
    private var shouldReconnect = true
    private var connectionJob: Job? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _messages = MutableSharedFlow<WebSocketMessage>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<WebSocketMessage> = _messages.asSharedFlow()
    
    // Track active subscriptions to avoid duplicates
    private val activeSubscriptions = mutableSetOf<String>()
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔌 Connection Management - STOMP Protocol
    // ═══════════════════════════════════════════════════════════════════════════
    
    fun connect(token: String) {
        Log.i(TAG, "📞 connect() called with token length: ${token.length}")
        
        if (_connectionState.value == ConnectionState.CONNECTED || 
            _connectionState.value == ConnectionState.CONNECTING) {
            Log.d(TAG, "Already connected or connecting, skipping")
            return
        }
        
        accessToken = token
        shouldReconnect = true
        reconnectAttempts = 0
        
        Log.i(TAG, "🚀 Starting STOMP connection job...")
        connectionJob?.cancel()
        connectionJob = scope.launch {
            try {
                connectStomp(token)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception in connectStomp coroutine: ${e.message}", e)
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }
    
    private suspend fun connectStomp(token: String) {
        _connectionState.value = ConnectionState.CONNECTING
        Log.i(TAG, "🔌 Connecting to STOMP WebSocket at $WS_BASE_URL...")
        Log.i(TAG, "📋 Token: ${token.take(30)}...")
        
        try {
            Log.d(TAG, "Creating OkHttpClient...")
            // Create OkHttp client for WebSocket
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(10, TimeUnit.SECONDS)
                .build()
            
            Log.d(TAG, "Creating StompClient...")
            // Create STOMP client using OkHttp WebSocket
            stompClient = StompClient(OkHttpWebSocketClient(okHttpClient))
            
            Log.d(TAG, "Calling stompClient.connect()...")
            // Connect with STOMP protocol and Authorization header
            stompSession = stompClient!!.connect(
                url = WS_BASE_URL,
                customStompConnectHeaders = mapOf("Authorization" to "Bearer $token")
            )
            
            Log.i(TAG, "✅ STOMP WebSocket connected successfully!")
            _connectionState.value = ConnectionState.CONNECTED
            reconnectAttempts = 0
            
            // Clear previous subscriptions state as we are starting fresh
            activeSubscriptions.clear()
            
            // Subscribe to user-specific queues
            Log.d(TAG, "Subscribing to user queues...")
            subscribeToUserQueues()
            
        } catch (e: Exception) {
            Log.e(TAG, "🔥 STOMP connection failed: ${e.javaClass.simpleName}: ${e.message}", e)
            _connectionState.value = ConnectionState.ERROR
            
            if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnect()
            }
        }
    }
    
    private suspend fun subscribeToUserQueues() {
        val session = stompSession ?: return
        val userId = sessionManager.userId.firstOrNull()
        
        if (userId == null) {
            Log.e(TAG, "❌ Cannot subscribe: userId is null")
            return
        }
        
        try {
            // Subscribe to personal message topic — direct topic routing (no STOMP principal needed)
            Log.i(TAG, "📡 Subscribing to /topic/user/$userId/messages")
            scope.launch {
                try {
                    session.subscribeText("/topic/user/$userId/messages").collect { frame ->
                        Log.d(TAG, "📨 Received message from topic: $frame")
                        handleMessage(frame)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in /topic/user/$userId/messages subscription", e)
                }
            }
            
            // Subscribe to online status updates (broadcast topic — works for everyone)
            Log.i(TAG, "📡 Subscribing to /topic/online-status")
            scope.launch {
                try {
                    session.subscribeText("/topic/online-status").collect { frame ->
                        Log.d(TAG, "📨 Received online status: $frame")
                        handleOnlineStatusFrame(frame)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in /topic/online-status subscription", e)
                }
            }
            
            // Subscribe to social notifications (topic-based)
            Log.i(TAG, "📡 Subscribing to /topic/user/$userId/notifications")
            scope.launch {
                try {
                    session.subscribeText("/topic/user/$userId/notifications").collect { frame ->
                        Log.d(TAG, "🔔 Received social notification: $frame")
                        handleSocialNotification(frame)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in /topic/user/$userId/notifications subscription", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error subscribing to user queues", e)
        }
    }
    
    fun disconnect() {
        shouldReconnect = false
        connectionJob?.cancel()
        scope.launch {
            try {
                stompSession?.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting STOMP session", e)
            }
            stompSession = null
            stompClient = null
            activeSubscriptions.clear()
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    private fun reconnect() {
        reconnectAttempts++
        Log.d(TAG, "🔄 Reconnecting... attempt $reconnectAttempts")
        
        scope.launch {
            delay(RECONNECT_DELAY_MS)
            accessToken?.let { connectStomp(it) }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 📤 Send Messages via STOMP
    // ═══════════════════════════════════════════════════════════════════════════
    
    fun sendTypingStatus(chatId: String, isTyping: Boolean) {
        scope.launch {
            try {
                val payload = gson.toJson(mapOf(
                    "chatId" to chatId,
                    "isTyping" to isTyping
                ))
                stompSession?.sendText("/app/typing", payload)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending typing status", e)
            }
        }
    }
    
    fun sendReadReceipt(chatId: String, messageId: String) {
        scope.launch {
            try {
                Log.i(TAG, "👁️ Sending read receipt for message: $messageId in chat: $chatId")
                val payload = gson.toJson(mapOf(
                    "chatId" to chatId,
                    "messageId" to messageId
                ))
                stompSession?.sendText("/app/read-receipt", payload)
                Log.d(TAG, "✅ Read receipt sent")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending read receipt", e)
            }
        }
    }
    
    // Chat-specific topic subscription removed — messages arrive via /user/queue/messages
    fun subscribeToChat(chatId: String) {
        // No-op: messages now delivered exclusively via user queue
        Log.d(TAG, "subscribeToChat($chatId) is no-op — using user queue delivery")
    }
    
    // Group topic subscription removed — messages arrive via /user/queue/messages
    fun subscribeToGroup(groupId: String) {
        // No-op: messages now delivered exclusively via user queue
        Log.d(TAG, "subscribeToGroup($groupId) is no-op — using user queue delivery")
    }
    
    // Channel topic subscription removed — messages arrive via /user/queue/messages
    fun subscribeToChannel(channelId: String) {
        // No-op: messages now delivered exclusively via user queue
        Log.d(TAG, "subscribeToChannel($channelId) is no-op — using user queue delivery")
    }
    
    fun unsubscribeFromGroup(groupId: String) {
        activeSubscriptions.remove("group_$groupId")
    }
    
    fun unsubscribeFromChannel(channelId: String) {
        activeSubscriptions.remove("channel_$channelId")
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 📥 Handle Incoming Messages
    // ═══════════════════════════════════════════════════════════════════════════
    
    private suspend fun handleMessage(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            val type = json.get("type")?.asString
            
            when (type) {
                "CHAT_MESSAGE", "TEXT", "IMAGE", "VIDEO", "VIDEO_NOTE", "VOICE", "AUDIO", "FILE", "POLL" -> 
                    handleChatMessage(json)
                "GROUP_MESSAGE" -> handleGroupMessage(json)
                "CHANNEL_POST" -> handleChannelPost(json)
                "USER_ONLINE" -> handleUserOnline(json)
                "TYPING" -> handleTyping(json)
                "MESSAGE_READ" -> handleMessageRead(json)
                "MESSAGE_DELETED" -> handleMessageDeleted(json)
                "CHAT_UPDATE" -> handleChatEvent(json)
                "GROUP_MEMBER_UPDATE" -> handleGroupMemberUpdate(json)
                "CHANNEL_SUBSCRIBER_UPDATE" -> handleChannelSubscriberUpdate(json)
                "STORY_CREATED", "STORY_DELETED" -> handleStoryEvent(json)
                else -> {
                    // Check for event field (CHAT_CREATED, etc)
                    val event = json.get("event")?.safeString()
                    if (event != null && (event == "CHAT_CREATED" || event == "CHAT_UPDATED" || event == "CHAT_DELETED")) {
                         handleChatEvent(json)
                     } else {
                        // Try to parse as direct message format (without type wrapper)
                        if (json.has("chatId") && json.has("content") && json.has("senderId")) {
                            val chatId = json.get("chatId").asString
                            
                            // Check if this is actually a Group or Channel message delivered via user queue
                            val isGroup = groupDao.getGroupById(chatId) != null
                            val isChannel = if (!isGroup) channelDao.getChannelById(chatId) != null else false
                            
                            if (isGroup) {
                                Log.d(TAG, "🔄 Routing user queue message to Group handler: $chatId")
                                // Ensure groupId property exists (backend sends chatId)
                                if (!json.has("groupId")) {
                                    json.addProperty("groupId", chatId)
                                }
                                handleGroupMessage(json)
                            } else if (isChannel) {
                                Log.d(TAG, "🔄 Routing user queue message to Channel handler: $chatId")
                                 // Ensure channelId property exists
                                if (!json.has("channelId")) {
                                    json.addProperty("channelId", chatId)
                                }
                                handleChannelPost(json)
                            } else {
                                handleDirectChatMessage(json)
                            }
                        } else {
                            Log.w(TAG, "Unknown message type: $type, raw: $text")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WebSocket message: ${e.message}", e)
        }
    }
    
    private suspend fun handleDirectChatMessage(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val messageId = data.get("id")?.safeString() ?: java.util.UUID.randomUUID().toString()
        val senderId = data.get("senderId")?.safeString() ?: ""
        val chatId = data.get("chatId")?.safeString() ?: ""
        val content = data.get("content")?.safeString() ?: ""
        
        // Check if message exists locally (matched by ID)
        val existingMessage = messageDao.getMessageById(messageId)
        var localReplyToMessage = existingMessage?.replyToMessage
        
        // FIX: If message is from ME, and not found by ID (UUID mismatch?), 
        // try to find a matching PENDING message to preserve reply context.
        // This handles race condition where WebSocket arrives before API response overwrites local ID.
        if (localReplyToMessage == null && senderId == sessionManager.userId.firstOrNull()) {
             val contentValue = data.get("content")?.safeString() ?: ""
             val chatIdValue = data.get("chatId")?.safeString() ?: ""
             val pendingMessage = messageDao.findPendingMessageByContent(chatIdValue, contentValue)
             if (pendingMessage != null) {
                 localReplyToMessage = pendingMessage.replyToMessage
                 Log.d(TAG, "📎 Found matching pending message for reply context: ${pendingMessage.id}")
             }
        }

        // ADDITIONAL FIX: If still null, but we have an ID, look up the original message context
        val replyToMessageId = data.get("replyToMessageId")?.safeString()
        if (localReplyToMessage == null && replyToMessageId != null) {
            try {
                val quotedMessage = messageDao.getMessageById(replyToMessageId)
                if (quotedMessage != null) {
                     // Create a map that matches MessageDto structure so standard mappers can parse it
                     val replyMap = mapOf(
                        "id" to quotedMessage.id,
                        "chatId" to quotedMessage.chatId,
                        "senderId" to quotedMessage.senderId,
                        "senderName" to quotedMessage.senderName,
                        "senderAvatar" to quotedMessage.senderAvatar,
                        "type" to quotedMessage.type,
                        "content" to quotedMessage.content,
                        "mediaUrl" to quotedMessage.mediaUrl,
                        "replyToMessageId" to quotedMessage.replyToMessageId,
                        // We don't recursively serialize replyToMessage of the reply to avoid depth issues/loops
                        "replyToMessage" to null, 
                        "forwardedFrom" to quotedMessage.forwardedFrom,
                        "status" to quotedMessage.status,
                        "isEdited" to quotedMessage.isEdited,
                        "createdAt" to quotedMessage.createdAt.toString(), // Entity stores Long, parseInstant can handle "12345"
                        "editedAt" to quotedMessage.editedAt?.toString()
                     )
                     localReplyToMessage = gson.toJson(replyMap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error looking up reply message context", e)
            }
        }
        
        // FIX: Extract timestamp from server if available, fallback to receipt time
        val createdAt = data.get("timestamp")?.safeString()?.let { parseTimestamp(it) }
            ?: data.get("createdAt")?.safeString()?.let { parseTimestamp(it) }
            ?: System.currentTimeMillis()

        // FIX: Preserve local entity's type/mediaUrl/poll/amplitudes if WebSocket would override with defaults
        val networkType = data.get("type")?.safeString() ?: "TEXT"
        val networkMediaUrl = data.get("mediaUrl")?.safeString()
        val networkPoll = data.get("poll")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) }
        val networkAmplitudes = data.get("amplitudes")?.takeIf { !it.isJsonNull && it.isJsonArray }?.asJsonArray?.joinToString(",") { it.asString }

        val finalType = if (existingMessage != null && existingMessage.type != "TEXT" && networkType == "TEXT") {
            Log.d(TAG, "📧 Preserving local type=${existingMessage.type} instead of network type=$networkType")
            existingMessage.type
        } else networkType

        val finalMediaUrl = if (existingMessage != null && existingMessage.mediaUrl != null && networkMediaUrl == null) {
            Log.d(TAG, "📧 Preserving local mediaUrl instead of null from network")
            existingMessage.mediaUrl
        } else networkMediaUrl

        val finalPoll = if (existingMessage != null && existingMessage.poll != null && networkPoll == null) {
            existingMessage.poll
        } else networkPoll

        val finalAmplitudes = if (existingMessage != null && existingMessage.amplitudes != null && networkAmplitudes == null) {
            existingMessage.amplitudes
        } else networkAmplitudes

        val messageEntity = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = data.get("senderName")?.safeString() ?: "",
            senderAvatar = data.get("senderAvatar")?.safeString(),
            type = finalType,
            content = content,
            mediaUrl = finalMediaUrl,
            replyToMessageId = data.get("replyToMessageId")?.safeString(),
            replyToMessage = localReplyToMessage,
            forwardedFrom = data.get("forwardedFrom")?.safeString(),
            status = data.get("status")?.safeString() ?: "DELIVERED",
            isEdited = data.get("isEdited")?.safeBoolean() ?: false,
            createdAt = createdAt,
            editedAt = data.get("editedAt")?.safeString()?.let { parseTimestamp(it) },
            isSynced = true,
            reactions = data.get("reactions")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) },
            myReaction = data.get("myReaction")?.safeString(),
            poll = finalPoll,
            amplitudes = finalAmplitudes
        )
        
        Log.d(TAG, "📨 Private message: id=${messageEntity.id}, type=${messageEntity.type}, mediaUrl=${messageEntity.mediaUrl != null}, poll=${messageEntity.poll != null}, amplitudes=${messageEntity.amplitudes != null}")
        
        // FIX: Deduplication - Skip if message already exists and is synced (prevents double insertion)
        // This handles the case where API response already inserted the message before WebSocket arrived
        if (existingMessage != null && existingMessage.isSynced) {
            Log.d(TAG, "⏭️ Skipping duplicate private message: id=$messageId (already synced)")
            // Still emit for UI updates but don't re-insert
            _messages.emit(WebSocketMessage.ChatMessage(existingMessage))
            return
        }
        
        // FIX: For messages from current user, also check if there's a pending message that was just synced
        // This handles the race condition where sync completes AFTER WebSocket arrives
        val currentUserId = sessionManager.userId.firstOrNull()
        if (senderId == currentUserId) {
            val pendingMessage = messageDao.findPendingMessageByContent(messageEntity.chatId, messageEntity.content)
            if (pendingMessage != null && pendingMessage.id != messageId) {
                Log.d(TAG, "⏭️ Found pending message ${pendingMessage.id} for WebSocket message $messageId - deleting pending")
                // Delete the pending message since we now have the server version
                messageDao.deleteMessageById(pendingMessage.id)
            }
        }
        
        // Save to local database
        messageDao.insertMessage(messageEntity)
        
        // Emit to subscribers
        _messages.emit(WebSocketMessage.ChatMessage(messageEntity))
        
        // Update Chat metadata
        updateChatMetadata(messageEntity)
        
        // 🔊 Play receive sound ONLY if chat represents the active conversation
        if (messageEntity.senderId != currentUserId && currentChatManager.isChatOpen(messageEntity.chatId)) {
            soundPlayer.playReceiveSound()
        }
        
        // Show notification
        showMessageNotification(messageEntity)
    }
    
    private suspend fun handleChatMessage(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val messageId = data.get("id")?.safeString() ?: java.util.UUID.randomUUID().toString()
        val senderId = data.get("senderId")?.safeString() ?: ""
        val chatId = data.get("chatId")?.safeString() ?: ""
        val content = data.get("content")?.safeString() ?: ""
        
        if (chatId.isEmpty()) {
            Log.e(TAG, "❌ Unified message missing chatId. Skipping.")
            return
        }

        // Check if message exists locally (matched by ID)
        val existingMessage = messageDao.getMessageById(messageId)
        var localReplyToMessage = existingMessage?.replyToMessage
        
        val currentUserId = sessionManager.userId.firstOrNull()

        // FIX: If message is from ME, and not found by ID, try to find matching PENDING message
        if (localReplyToMessage == null && senderId == currentUserId) {
             val pendingMessage = messageDao.findPendingMessageByContent(chatId, content)
             if (pendingMessage != null) {
                 localReplyToMessage = pendingMessage.replyToMessage
                 Log.d(TAG, "📎 Found matching pending message for unified reply context: ${pendingMessage.id}")
             }
        }

        // ADDITIONAL FIX: If still null, look up original message context
        val replyToMessageId = data.get("replyToMessageId")?.safeString()
        if (localReplyToMessage == null && replyToMessageId != null) {
            try {
                val quotedMessage = messageDao.getMessageById(replyToMessageId)
                if (quotedMessage != null) {
                      val replyMap = mapOf(
                        "id" to quotedMessage.id,
                        "chatId" to quotedMessage.chatId,
                        "senderId" to quotedMessage.senderId,
                        "senderName" to quotedMessage.senderName,
                        "senderAvatar" to quotedMessage.senderAvatar,
                        "type" to quotedMessage.type,
                        "content" to quotedMessage.content,
                        "mediaUrl" to quotedMessage.mediaUrl,
                        "replyToMessageId" to quotedMessage.replyToMessageId,
                        "replyToMessage" to null,
                        "forwardedFrom" to quotedMessage.forwardedFrom,
                        "status" to quotedMessage.status,
                        "isEdited" to quotedMessage.isEdited,
                        "createdAt" to quotedMessage.createdAt.toString(),
                        "editedAt" to quotedMessage.editedAt?.toString()
                      )
                      localReplyToMessage = gson.toJson(replyMap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error looking up reply context in handleChatMessage", e)
            }
        }

        // Extract timestamp from server if available, fallback to receipt time
        val createdAt = data.get("timestamp")?.safeString()?.let { parseTimestamp(it) }
            ?: data.get("createdAt")?.safeString()?.let { parseTimestamp(it) }
            ?: System.currentTimeMillis()

        // Preserve local entity's type/mediaUrl/poll/amplitudes if WebSocket would override with defaults
        val networkType = data.get("type")?.safeString() ?: "TEXT"
        val networkMediaUrl = data.get("mediaUrl")?.safeString()
        val networkPoll = data.get("poll")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) }
        val networkAmplitudes = data.get("amplitudes")?.takeIf { !it.isJsonNull && it.isJsonArray }?.asJsonArray?.joinToString(",") { it.asString }

        val finalType = if (existingMessage != null && existingMessage.type != "TEXT" && networkType == "TEXT") {
            Log.d(TAG, "📧 Preserving local type=${existingMessage.type} instead of network type=$networkType")
            existingMessage.type
        } else networkType

        val finalMediaUrl = if (existingMessage != null && existingMessage.mediaUrl != null && networkMediaUrl == null) {
            Log.d(TAG, "📧 Preserving local mediaUrl instead of null from network")
            existingMessage.mediaUrl
        } else networkMediaUrl

        val finalPoll = if (existingMessage != null && existingMessage.poll != null && networkPoll == null) {
            existingMessage.poll
        } else networkPoll

        val finalAmplitudes = if (existingMessage != null && existingMessage.amplitudes != null && networkAmplitudes == null) {
            existingMessage.amplitudes
        } else networkAmplitudes

        val messageEntity = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = data.get("senderName")?.safeString() ?: "",
            senderAvatar = data.get("senderAvatar")?.safeString(),
            type = finalType,
            content = content,
            mediaUrl = finalMediaUrl,
            replyToMessageId = data.get("replyToMessageId")?.safeString(),
            replyToMessage = localReplyToMessage,
            forwardedFrom = data.get("forwardedFrom")?.safeString(),
            status = data.get("status")?.safeString() ?: "DELIVERED",
            isEdited = data.get("isEdited")?.safeBoolean() ?: false,
            createdAt = createdAt,
            editedAt = data.get("editedAt")?.safeString()?.let { parseTimestamp(it) },
            isSynced = true,
            reactions = data.get("reactions")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) },
            myReaction = data.get("myReaction")?.safeString(),
            poll = finalPoll,
            amplitudes = finalAmplitudes
        )
        
        Log.d(TAG, "📨 Unwrapped message handled: id=${messageEntity.id}, type=${messageEntity.type}")

        // Deduplication
        if (existingMessage != null && existingMessage.isSynced) {
            Log.d(TAG, "⏭️ Skipping duplicate unwrapped message: id=$messageId")
            _messages.emit(WebSocketMessage.ChatMessage(existingMessage))
            return
        }

        // Save to local database
        messageDao.insertMessage(messageEntity)
        _messages.emit(WebSocketMessage.ChatMessage(messageEntity))
        updateChatMetadata(messageEntity)

        if (senderId != currentUserId && currentChatManager.isChatOpen(messageEntity.chatId)) {
            soundPlayer.playReceiveSound()
        }
        showMessageNotification(messageEntity)
    }
    
    private suspend fun updateChatMetadata(messageEntity: MessageEntity) {
        try {
            Log.i(TAG, "📝 Updating chat metadata for ${messageEntity.chatId}: lastMessage='${messageEntity.content.take(20)}...'")
            chatDao.updateLastMessage(messageEntity.chatId, messageEntity.content, messageEntity.createdAt)
            if (!isCurrentUser(messageEntity.senderId)) {
                val chat = chatDao.getChatById(messageEntity.chatId)
                chat?.let {
                    val newCount = it.unreadCount + 1
                    chatDao.updateUnreadCount(it.id, newCount)
                    Log.i(TAG, "🔔 Updated unread count to $newCount for chat ${it.id}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating chat metadata", e)
        }
    }
    
    private suspend fun resolveSenderName(senderId: String, currentName: String): String {
        return try {
            val user = userDao.getUserById(senderId)
            contactsRepository.resolveDisplayName(user?.phoneNumber, currentName)
        } catch (e: Exception) {
            currentName
        }
    }

    private suspend fun showMessageNotification(messageEntity: MessageEntity) {
        if (!isCurrentUser(messageEntity.senderId) && !currentChatManager.isChatOpen(messageEntity.chatId)) {
            try {
                // Check if chat is muted
                val chat = chatDao.getChatById(messageEntity.chatId)
                if (chat?.isMuted == true) {
                    Log.d(TAG, "🔕 Notification suppressed: Chat ${messageEntity.chatId} is muted")
                    return
                }

                // Resolve sender name from contacts
                val displayName = resolveSenderName(messageEntity.senderId, messageEntity.senderName)
                
                notificationHelper.showNotification(
                    id = messageEntity.id.hashCode(),
                    title = displayName,
                    message = messageEntity.content,
                    chatId = messageEntity.chatId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error showing notification", e)
            }
        }
    }
    
    private suspend fun handleGroupMessageFrame(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            handleGroupMessage(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing group message", e)
        }
    }
    
    private fun com.google.gson.JsonElement?.safeString(): String? {
        return if (this != null && !this.isJsonNull) this.asString else null
    }

    private fun com.google.gson.JsonElement?.safeBoolean(default: Boolean = false): Boolean {
        return if (this != null && !this.isJsonNull) this.asBoolean else default
    }

    private fun com.google.gson.JsonElement?.safeInt(default: Int = 0): Int {
        return if (this != null && !this.isJsonNull) this.asInt else default
    }

    private fun com.google.gson.JsonElement?.safeLong(default: Long = 0L): Long {
        return if (this != null && !this.isJsonNull) this.asLong else default
    }

    private suspend fun handleGroupMessage(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        
        // Robust field extraction
        val id = data.get("id")?.safeString() ?: java.util.UUID.randomUUID().toString()
        // Fallback: use chatId if groupId is missing
        val groupId = data.get("groupId")?.safeString() ?: data.get("chatId")?.safeString() ?: ""
        val senderId = data.get("senderId")?.safeString() ?: ""
        val content = data.get("content")?.safeString() ?: ""
        
        if (groupId.isEmpty()) {
            Log.e(TAG, "❌ Group message missing groupId and chatId. Skipping.")
            return
        }
        
        // FIX: Check if message already exists to prevent duplicates
        val existingMessage = groupMessageDao.getMessageById(id)
        if (existingMessage != null && existingMessage.isSynced) {
            Log.d(TAG, "⏭️ Skipping duplicate group message: id=$id (already synced)")
            // Still emit for UI updates but don't re-insert
            _messages.emit(WebSocketMessage.GroupMessage(existingMessage))
            return
        }

        // FIX: For messages from current user, check if there's a pending message (Right Aligned)
        // and delete it since we now have the server message (Left/Right Aligned)
        val currentUserId = sessionManager.userId.firstOrNull()
        if (senderId == currentUserId) {
             val pendingMessage = groupMessageDao.findPendingMessageByContent(groupId, content)
             if (pendingMessage != null && pendingMessage.id != id) {
                 Log.d(TAG, "⏭️ Found pending group message ${pendingMessage.id} for WebSocket message $id - deleting pending")
                 groupMessageDao.deleteMessageById(pendingMessage.id)
             }
        }
        
        // FIX: Preserve local entity's type/mediaUrl/poll/amplitudes if WebSocket would override with defaults
        val networkType = data.get("type")?.safeString() ?: "TEXT"
        val networkMediaUrl = data.get("mediaUrl")?.safeString()
        val networkPoll = data.get("poll")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) }
        val networkAmplitudes = data.get("amplitudes")?.takeIf { !it.isJsonNull && it.isJsonArray }?.asJsonArray?.joinToString(",") { it.asString }
        
        val finalType = if (existingMessage != null && existingMessage.type != "TEXT" && networkType == "TEXT") {
            Log.d(TAG, "👥 Preserving local type=${existingMessage.type} instead of network type=$networkType")
            existingMessage.type
        } else networkType
        
        val finalMediaUrl = if (existingMessage != null && existingMessage.mediaUrl != null && networkMediaUrl == null) {
            Log.d(TAG, "👥 Preserving local mediaUrl instead of null from network")
            existingMessage.mediaUrl
        } else networkMediaUrl
        
        val finalPoll = if (existingMessage != null && existingMessage.poll != null && networkPoll == null) {
            Log.d(TAG, "👥 Preserving local poll instead of null from network")
            existingMessage.poll
        } else networkPoll
        
        val finalAmplitudes = if (existingMessage != null && existingMessage.amplitudes != null && networkAmplitudes == null) {
            Log.d(TAG, "👥 Preserving local amplitudes instead of null from network")
            existingMessage.amplitudes
        } else networkAmplitudes

        // FIX: Extract timestamp from server if available, fallback to receipt time
        val createdAt = data.get("timestamp")?.safeString()?.let { parseTimestamp(it) }
            ?: data.get("createdAt")?.safeString()?.let { parseTimestamp(it) }
            ?: System.currentTimeMillis()

        val messageEntity = GroupMessageEntity(
            id = id,
            groupId = groupId,
            senderId = senderId,
            senderName = data.get("senderName")?.safeString() ?: "",
            senderAvatar = data.get("senderAvatar")?.safeString(),
            type = finalType,
            content = content,
            mediaUrl = finalMediaUrl,
            replyToMessageId = data.get("replyToMessageId")?.safeString(),
            replyToMessage = data.get("replyToMessage")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) },
            isEdited = data.get("isEdited")?.safeBoolean(false) ?: false,
            createdAt = createdAt,
            editedAt = data.get("editedAt")?.safeString()?.let { parseTimestamp(it) },
            isSynced = true,
            reactions = data.get("reactions")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) },
            myReaction = data.get("myReaction")?.safeString(),
            poll = finalPoll,
            amplitudes = finalAmplitudes
        )
        
        Log.d(TAG, "👥 Group message: id=${messageEntity.id}, type=${messageEntity.type}, mediaUrl=${messageEntity.mediaUrl != null}, poll=${messageEntity.poll != null}, amplitudes=${messageEntity.amplitudes != null}")
        
        groupMessageDao.insertMessage(messageEntity)
        _messages.emit(WebSocketMessage.GroupMessage(messageEntity))

        // 🔊 Play receive sound ONLY if group represents the active conversation
        if (!isCurrentUser(messageEntity.senderId) && currentChatManager.isChatOpen(messageEntity.groupId)) {
            soundPlayer.playReceiveSound()
        }

        // Update Group metadata
        try {
            val group = groupDao.getGroupById(messageEntity.groupId)
            if (group != null) {
                if (!isCurrentUser(messageEntity.senderId)) {
                     val newCount = group.unreadCount + 1
                     groupDao.updateUnreadCount(group.id, newCount)
                }
                groupDao.updateLastMessage(group.id, messageEntity.content, messageEntity.createdAt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating group metadata", e)
        }

        if (!isCurrentUser(messageEntity.senderId) && !currentChatManager.isChatOpen(messageEntity.groupId)) {
            try {
                // Resolve sender name from contacts
                val displayName = resolveSenderName(messageEntity.senderId, messageEntity.senderName)
                
                val group = groupDao.getGroupById(messageEntity.groupId)
                if (group != null && !group.isMuted) {
                    val title = "${group.name} ($displayName)"
                    
                    notificationHelper.showNotification(
                        id = messageEntity.id.hashCode(),
                        title = title,
                        message = messageEntity.content,
                        chatId = messageEntity.groupId
                    )
                } else if (group == null) {
                    // Fallback if group not found - Check if we should notify? 
                    // To be safe, let's notify but ideally we should fetch group status.
                    // For now, allow notification as fallback but maybe generic.
                     notificationHelper.showNotification(
                        id = messageEntity.id.hashCode(),
                        title = displayName,
                        message = messageEntity.content,
                        chatId = messageEntity.groupId
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error showing group notification", e)
            }
        }
    }
    
    private suspend fun handleChannelPostFrame(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            handleChannelPost(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing channel post", e)
        }
    }
    
    private suspend fun handleChannelPost(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        
        val id = data.get("id")?.safeString() ?: java.util.UUID.randomUUID().toString()
        val channelId = data.get("channelId")?.safeString() ?: ""
        val content = data.get("content")?.safeString() ?: ""
        
        if (channelId.isEmpty()) {
            Log.e(TAG, "❌ Invalid channel post: missing channelId")
            return
        }

        // CRITICAL FIX: Check for existing local entity and preserve its type/poll/mediaUrl/amplitudes
        // This prevents WebSocket from overwriting correctly patched posts with wrong backend values
        val existingPost = channelPostDao.getPostById(id)
        
        val networkType = data.get("type")?.safeString() ?: "TEXT"
        val networkMediaUrl = data.get("mediaUrl")?.safeString()
        val networkPoll = data.get("poll")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) }
        val networkAmplitudes = data.get("amplitudes")?.takeIf { !it.isJsonNull && it.isJsonArray }?.asJsonArray?.joinToString(",") { it.asString }
        
        // Preserve local entity's values if they are non-default (meaning they were correctly patched)
        val finalType = if (existingPost != null && existingPost.type != "TEXT" && networkType == "TEXT") {
            Log.d(TAG, "📢 Preserving local type=${existingPost.type} instead of network type=$networkType")
            existingPost.type
        } else networkType
        
        val finalMediaUrl = if (existingPost != null && existingPost.mediaUrl != null && networkMediaUrl == null) {
            Log.d(TAG, "📢 Preserving local mediaUrl instead of null network value")
            existingPost.mediaUrl
        } else networkMediaUrl
        
        val finalPoll = if (existingPost != null && existingPost.poll != null && networkPoll == null) {
            Log.d(TAG, "📢 Preserving local poll data instead of null network value")
            existingPost.poll
        } else networkPoll
        
        val finalAmplitudes = if (existingPost != null && existingPost.amplitudes != null && networkAmplitudes == null) {
            existingPost.amplitudes
        } else networkAmplitudes

        val postEntity = ChannelPostEntity(
            id = id,
            channelId = channelId,
            type = finalType,
            content = content,
            mediaUrl = finalMediaUrl,
            viewCount = data.get("viewCount")?.safeInt(0) ?: 0,
            commentsEnabled = data.get("commentsEnabled")?.safeBoolean(true) ?: true,
            createdAt = existingPost?.createdAt ?: System.currentTimeMillis(),
            editedAt = null,
            poll = finalPoll,
            reactions = data.get("reactions")?.takeIf { !it.isJsonNull }?.let { gson.toJson(it) },
            amplitudes = finalAmplitudes
        )
        
        Log.d(TAG, "📢 Channel post: id=${postEntity.id}, type=${postEntity.type}, mediaUrl=${postEntity.mediaUrl != null}, poll=${postEntity.poll != null}, amplitudes=${postEntity.amplitudes != null}")
        
        
        channelPostDao.insertPost(postEntity)
        _messages.emit(WebSocketMessage.ChannelPost(postEntity))

        try {
            val channel = channelDao.getChannelById(postEntity.channelId)
            if (channel != null) {
                 val newCount = channel.unreadCount + 1
                 channelDao.updateUnreadCount(channel.id, newCount)
                 channelDao.updateLastPost(channel.id, postEntity.content, postEntity.createdAt)
                 
                 if (!channel.isMuted && !currentChatManager.isChatOpen(postEntity.channelId)) {
                     val title = channel.name
                     notificationHelper.showNotification(
                         id = postEntity.id.hashCode(),
                         title = title,
                         message = postEntity.content,
                         chatId = postEntity.channelId
                     )
                 }
            } else {
                 if (!currentChatManager.isChatOpen(postEntity.channelId)) {
                     val title = "پست جدید"
                     notificationHelper.showNotification(
                         id = postEntity.id.hashCode(),
                         title = title,
                         message = postEntity.content,
                         chatId = postEntity.channelId
                     )
                 }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing channel notification", e)
        }
    }
    
    private suspend fun handleOnlineStatusFrame(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            handleUserOnline(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing online status", e)
        }
    }
    
    private suspend fun handleUserOnline(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val userId = data.get("userId")?.safeString() ?: return
        val isOnline = data.get("isOnline")?.safeBoolean(false) ?: false
        val lastSeen = data.get("lastSeen")?.takeIf { !it.isJsonNull }?.asLong
        
        Log.d(TAG, "👤 User online status: $userId -> $isOnline")
        
        userDao.updateOnlineStatus(userId, isOnline, lastSeen)
        _messages.emit(WebSocketMessage.UserOnline(userId, isOnline, lastSeen))
    }
    
    private suspend fun handleTyping(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val chatId = data.get("chatId")?.safeString() ?: return
        val userId = data.get("userId")?.safeString() ?: return
        val userName = data.get("userName")?.safeString() ?: ""
        val isTyping = data.get("isTyping")?.safeBoolean(false) ?: false
        
        _messages.emit(WebSocketMessage.Typing(chatId, userId, userName, isTyping))
    }
    
    private suspend fun handleMessageRead(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val chatId = data.get("chatId")?.safeString() ?: return
        val messageId = data.get("messageId")?.safeString() ?: return
        // Support both "readerId" (new) and "userId" (old) field names
        val readerId = data.get("readerId")?.safeString() ?: data.get("userId")?.safeString() ?: ""
        
        Log.d(TAG, "👁️ MESSAGE_READ received: messageId=$messageId, chatId=$chatId, readerId=$readerId")
        
        // Update the message status in local database
        messageDao.updateMessageStatus(messageId, "READ", true)
        Log.d(TAG, "✅ Message status updated to READ in database")
        
        _messages.emit(WebSocketMessage.MessageRead(chatId, messageId, readerId))
    }
    
    private suspend fun handleMessageDeleted(json: com.google.gson.JsonObject) {
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val chatId = data.get("chatId")?.safeString() ?: return
        val messageId = data.get("messageId")?.safeString() ?: return
        Log.d(TAG, "🗑️ MESSAGE_DELETED received: messageId=$messageId, chatId=$chatId")
        // Remove from local database
        messageDao.deleteMessageById(messageId)
        // Also try group and channel tables
        groupMessageDao.deleteMessageById(messageId)
        Log.d(TAG, "✅ Message deleted from local database")
        _messages.emit(WebSocketMessage.MessageDeleted(chatId, messageId))
    }
    
    private suspend fun handleStoryEvent(json: com.google.gson.JsonObject) {
        val type = json.get("type")?.safeString() ?: return
        val data = if (json.has("data")) json.getAsJsonObject("data") else json
        val storyId = data.get("storyId")?.safeString() ?: return
        val userId = data.get("userId")?.safeString() ?: return
        Log.d(TAG, "📸 Story event: $type, storyId=$storyId, userId=$userId")
        _messages.emit(WebSocketMessage.StoryEvent(type, storyId, userId))
    }
    
    private suspend fun handleChatEvent(json: com.google.gson.JsonObject) {
        try {
            val data = if (json.has("data")) json.getAsJsonObject("data") else json
            val event = data.get("event")?.safeString() ?: return
            val id = data.get("id")?.safeString() ?: return
            // Backend sends chatType for entity type (PRIVATE/GROUP/CHANNEL) since type=CHAT_UPDATE is used for routing
            val type = data.get("chatType")?.safeString() ?: data.get("type")?.safeString() ?: "PRIVATE"
            
            Log.i(TAG, "🔔 Chat Event: $event, Type: $type, ID: $id")
             
            if (event == "CHAT_CREATED" || event == "CHAT_UPDATED") {
                when (type) {
                    "PRIVATE" -> {
                        val title = data.get("title")?.safeString() ?: "Chat"
                        val avatarUrl = data.get("avatarUrl")?.safeString()
                        
                        val chat = ChatEntity(
                            id = id,
                            type = "PRIVATE",
                            title = title,
                            avatarUrl = avatarUrl,
                            lastMessageId = null,
                            lastMessage = data.get("lastMessageContent")?.safeString() ?: "",
                            lastMessageTime = data.get("lastMessageTime")?.safeLong() ?: System.currentTimeMillis(),
                            unreadCount = data.get("unreadCount")?.safeInt(0) ?: 0,
                            isPinned = data.get("isPinned")?.safeBoolean(false) ?: false,
                            isMuted = data.get("isMuted")?.safeBoolean(false) ?: false,
                            isArchived = data.get("isArchived")?.safeBoolean(false) ?: false
                        )
                        chatDao.insertChat(chat)
                        Log.d(TAG, "✅ Private chat inserted/updated: $id")
                        _messages.emit(WebSocketMessage.ChatCreated(chat))
                    }
                    "GROUP" -> {
                         val title = data.get("title")?.safeString() ?: "Group"
                         val avatarUrl = data.get("avatarUrl")?.safeString()
                         val description = data.get("description")?.safeString() ?: ""
                         
                         val group = com.Kelasor.app.data.local.entity.GroupEntity(
                             id = id,
                             name = title,
                             description = description,
                             avatarUrl = avatarUrl,
                             memberCount = data.get("participants")?.asJsonArray?.size() ?: 0,
                             isPublic = data.get("isPublic")?.safeBoolean(false) ?: false,
                             inviteLink = data.get("inviteLink")?.safeString(),
                             isInviteLinkEnabled = data.get("isInviteLinkEnabled")?.safeBoolean(false) ?: false,
                             allowMembersToSendMessages = data.get("allowMembersToSendMessages")?.safeBoolean(true) ?: true,
                             allowMembersToEditInfo = data.get("allowMembersToEditInfo")?.safeBoolean(false) ?: false,
                             myRole = data.get("myRole")?.safeString() ?: "MEMBER",
                             lastMessageContent = data.get("lastMessageContent")?.safeString() ?: "",
                             lastMessageTime = data.get("lastMessageTime")?.safeLong() ?: System.currentTimeMillis(),
                             unreadCount = data.get("unreadCount")?.safeInt(0) ?: 0
                         )
                         groupDao.insertGroup(group)
                         Log.d(TAG, "✅ Group inserted/updated: $id")
                         _messages.emit(WebSocketMessage.GroupCreated(group))
                         
                         // Subscribe to group topic
                         subscribeToGroup(id)
                    }
                    "CHANNEL" -> {
                         val title = data.get("title")?.safeString() ?: "Channel"
                         val avatarUrl = data.get("avatarUrl")?.safeString()
                         val description = data.get("description")?.safeString() ?: ""
                         val ownerId = data.get("ownerId")?.safeString() ?: ""
                         
                         val channel = com.Kelasor.app.data.local.entity.ChannelEntity(
                             id = id,
                             name = title,
                             description = description,
                             avatarUrl = avatarUrl,
                             subscriberCount = data.get("participants")?.asJsonArray?.size() ?: 0,
                             isPublic = data.get("isPublic")?.safeBoolean(false) ?: false,
                             publicId = data.get("publicId")?.safeString(),
                             inviteLink = data.get("inviteLink")?.safeString(),
                             ownerId = ownerId,
                             lastPostContent = data.get("lastMessageContent")?.safeString() ?: "",
                             lastPostTime = data.get("lastMessageTime")?.safeLong() ?: System.currentTimeMillis(),
                             unreadCount = data.get("unreadCount")?.safeInt(0) ?: 0
                         )
                         channelDao.insertChannel(channel)
                         Log.d(TAG, "✅ Channel inserted/updated: $id")
                         _messages.emit(WebSocketMessage.ChannelCreated(channel))
                         
                         // Subscribe to channel topic
                         subscribeToChannel(id)
                    }
                }
            } else if (event == "CHAT_DELETED") {
                // Handle deletion
                when (type) {
                    "PRIVATE" -> chatDao.deleteChatById(id)
                    "GROUP" -> groupDao.deleteGroupById(id)
                    "CHANNEL" -> channelDao.deleteChannelById(id)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Chat Event", e)
        }
    }
    
    private suspend fun handleChatUpdateFrame(text: String) {
        try {
            val json = JsonParser.parseString(text).asJsonObject
            handleChatUpdate(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing chat update", e)
        }
    }

    private suspend fun handleChatUpdate(json: com.google.gson.JsonObject) {
        try {
            val data = if (json.has("data")) json.getAsJsonObject("data") else json
            val id = data.get("id")?.safeString() ?: data.get("chatId")?.safeString() ?: return
            
            Log.d(TAG, "📋 Chat update: $id")
            
            if (data.has("unreadCount")) {
                val count = data.get("unreadCount").safeInt(0)
                chatDao.updateUnreadCount(id, count)
            }
            if (data.has("lastMessage")) {
                val lastMsg = data.get("lastMessage").safeString() ?: ""
                val time = if (data.has("updatedAt")) data.get("updatedAt").safeLong(System.currentTimeMillis()) else System.currentTimeMillis()
                chatDao.updateLastMessage(id, lastMsg, time)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling CHAT_UPDATE", e)
        }
    }

    private suspend fun handleGroupMemberUpdate(json: com.google.gson.JsonObject) {
        try {
            val data = if (json.has("data")) json.getAsJsonObject("data") else json
            val groupId = data.get("groupId")?.safeString() ?: return
            val event = data.get("event")?.safeString() ?: return
            val memberId = data.get("memberId")?.safeString() ?: return
            val memberName = data.get("memberName")?.safeString() ?: ""
            val newMemberCount = data.get("newMemberCount")?.safeInt(0) ?: 0
            
            Log.d(TAG, "👥 Group member update: $event in group $groupId, member: $memberName, count: $newMemberCount")
            
            // Update local group member count
            val group = groupDao.getGroupById(groupId)
            if (group != null) {
                groupDao.updateMemberCount(groupId, newMemberCount)
                Log.d(TAG, "✅ Updated group $groupId member count to $newMemberCount")
            }
            
            // Emit event for UI components to react
            _messages.emit(WebSocketMessage.GroupMemberUpdate(groupId, event, memberId, memberName, newMemberCount))
        } catch (e: Exception) {
            Log.e(TAG, "Error handling GROUP_MEMBER_UPDATE", e)
        }
    }

    private suspend fun handleChannelSubscriberUpdate(json: com.google.gson.JsonObject) {
        try {
            val data = if (json.has("data")) json.getAsJsonObject("data") else json
            val channelId = data.get("channelId")?.safeString() ?: return
            val event = data.get("event")?.safeString() ?: return
            val userId = data.get("userId")?.safeString() ?: return
            // isAdmin might be implicitly true for ADMIN_ADDED, but let's check field
            val isAdmin = data.get("isAdmin")?.safeBoolean(false) ?: false
            
            Log.d(TAG, "📢 Channel subscriber update: $event for user $userId in channel $channelId (isAdmin=$isAdmin)")
            
            when (event) {
                "ADMIN_ADDED" -> {
                    channelSubscriberDao.updateAdminStatus(channelId, userId, true)
                }
                "ADMIN_REMOVED" -> {
                    channelSubscriberDao.updateAdminStatus(channelId, userId, false)
                }
                "SUBSCRIBER_LEFT" -> {
                    channelSubscriberDao.removeSubscriber(channelId, userId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling CHANNEL_SUBSCRIBER_UPDATE", e)
        }
    }

    private suspend fun isCurrentUser(userId: String): Boolean {
        // First try SessionManager (most reliable)
        val sessionUserId = sessionManager.userId.first()
        if (sessionUserId != null) {
            return sessionUserId == userId
        }
        // Fallback to database
        val currentUser = userDao.getCurrentUser()
        return currentUser?.id == userId
    }
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔔 Social Notification Handling
    // ═══════════════════════════════════════════════════════════════════════════
    private suspend fun handleSocialNotification(text: String) {
        try {
            val json = com.google.gson.JsonParser.parseString(text).asJsonObject
            val type = json.get("type")?.safeString() ?: ""
            val title = json.get("title")?.safeString() ?: ""
            val body = json.get("body")?.safeString() ?: ""
            val actorId = json.get("actorId")?.safeString()
            val actorName = json.get("actorName")?.safeString() ?: ""
            val notificationId = json.get("id")?.safeString() ?: System.currentTimeMillis().toString()
            Log.i(TAG, "🔔 Social notification: type=$type, actor=$actorName")
            // Increment badge count
            notificationBadgeManager.incrementCount()
            // Show appropriate notification based on type
            when (type) {
                "FOLLOW", "FOLLOW_REQUEST" -> {
                    // Resolve actor name
                    val displayName = if (actorId != null) resolveSenderName(actorId, actorName) else actorName
                    
                    notificationHelper.showFollowNotification(
                        id = notificationId.hashCode(),
                        followerName = displayName,
                        userId = actorId
                    )
                }
                "COLLABORATION_REQUEST" -> {
                    val requestTitle = json.get("relatedData")?.asJsonObject?.get("title")?.safeString() ?: title
                    // Resolve actor name
                    val displayName = if (actorId != null) resolveSenderName(actorId, actorName) else actorName

                    notificationHelper.showCollaborationNotification(
                        id = notificationId.hashCode(),
                        senderName = displayName,
                        requestTitle = requestTitle,
                        requestId = json.get("relatedEntityId")?.safeString()
                    )
                }
                "COLLABORATION_ACCEPTED", "COLLABORATION_REJECTED" -> {
                    notificationHelper.showSocialNotification(
                        id = notificationId.hashCode(),
                        title = title,
                        message = body
                    )
                }
                else -> {
                    // Generic social notification
                    if (title.isNotEmpty() || body.isNotEmpty()) {
                        notificationHelper.showSocialNotification(
                            id = notificationId.hashCode(),
                            title = title.ifEmpty { "اعلان" },
                            message = body
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling social notification", e)
        }
    }
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            java.time.Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            timestamp.toLongOrNull() ?: System.currentTimeMillis()
        }
    }
}
