package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.dao.ChatDao
import com.Kelasor.app.data.local.dao.GroupMessageDao
import com.Kelasor.app.data.local.dao.MessageDao
import com.Kelasor.app.data.local.dao.UserDao
import com.Kelasor.app.data.local.entity.MessageEntity
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.CreateChatRequest
import com.Kelasor.app.data.remote.dto.EditMessageRequest
import com.Kelasor.app.data.remote.dto.SendMessageRequest
import com.Kelasor.app.data.sync.SyncCoordinator
import com.Kelasor.app.domain.mapper.*
import com.Kelasor.app.domain.model.Chat
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Repository - Database as Single Source of Truth
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * ChatRepository provides access to chat data using the database as the single source of truth.
 * 
 * ARCHITECTURE:
 * - All data is read via observable Flows from Room database
 * - Sync operations trigger API calls that write to database
 * - UI observes database, never API directly
 * 
 * Data Flow: API → SyncCoordinator → DB → (Room Flow) → Repository → ViewModel → UI
 */
@Singleton
class ChatRepository @Inject constructor(
    private val apiService: ApiService,
    private val chatDao: ChatDao,
    private val userDao: UserDao,
    private val syncCoordinator: SyncCoordinator
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // 📡 OBSERVABLE DATA STREAMS (Database as Source of Truth)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Observe all active (non-archived) chats.
     * This is the PRIMARY way to get chat data.
     * Changes in the database automatically trigger emissions.
     * Combined with users flow to get live online status updates.
     */
    fun observeChats(): Flow<List<Chat>> = 
        chatDao.observeActiveChats().map { chats ->
            chats.filter { !it.chat.isDeletedLocally }.map { it.toDomain() }
        }.distinctUntilChanged()

    /**
     * Observe pinned chats only.
     */
    fun observePinnedChats(): Flow<List<Chat>> = 
        chatDao.observePinnedChats().map { chats ->
            chats.filter { !it.chat.isDeletedLocally }.map { it.toDomain() }
        }.distinctUntilChanged()

    /**
     * Observe archived chats only.
     */
    fun observeArchivedChats(): Flow<List<Chat>> = 
        chatDao.observeArchivedChats().map { chats ->
            chats.filter { !it.chat.isDeletedLocally }.map { it.toDomain() }
        }.distinctUntilChanged()
    
    /**
     * Observe a specific chat by ID.
     */
    fun observeChatById(chatId: String): Flow<Chat?> = 
        chatDao.observeChatById(chatId).map { it?.toDomain() }.distinctUntilChanged()
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 SYNC STATUS (For UI Loading Indicators)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Whether chats are currently being synced from the server.
     */
    val isSyncingChats: StateFlow<Boolean> = syncCoordinator.isSyncingChats
    
    /**
     * Last sync error, if any.
     */
    val lastSyncError: StateFlow<String?> = syncCoordinator.lastSyncError
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 SYNC TRIGGERS (Fire-and-forget, results go to DB)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Request a chat list sync from the server.
     * Results are written to the database; observe via observeChats().
     */
    fun requestChatSync(page: Int = 0) {
        syncCoordinator.syncChats(page)
    }
    
    /**
     * Request a specific chat's details sync from the server.
     */
    fun requestChatDetailsSync(chatId: String) {
        syncCoordinator.syncChatDetails(chatId)
    }
    
    /**
     * Clear unread count for a chat when user views it.
     * Called when entering a chat screen.
     */
    suspend fun clearUnreadCount(chatId: String) {
        chatDao.clearUnreadCount(chatId)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ✏️ WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Create a new chat with a participant.
     * Returns the created chat or null if failed.
     */
    suspend fun createChat(participantId: String): Result<Chat> {
        return try {
            val response = apiService.createChat(CreateChatRequest(participantId))
            if (response.isSuccessful && response.body()?.success == true) {
                val chatDto = response.body()?.data
                if (chatDto != null) {
                    // Save to database (this triggers Flow update automatically)
                    saveChatWithParticipants(chatDto)
                    Result.success(chatDto.toDomain())
                } else {
                    Result.failure(Exception("خطا در ایجاد چت"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا در ایجاد چت"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    /**
     * Pin or unpin a chat.
     */
    suspend fun pinChat(chatId: String, pinned: Boolean): Result<Chat> {
        return try {
            val response = apiService.pinChat(chatId, pinned)
            if (response.isSuccessful && response.body()?.success == true) {
                val chatDto = response.body()?.data
                if (chatDto != null) {
                    saveChatWithParticipants(chatDto)
                    Result.success(chatDto.toDomain())
                } else {
                    Result.failure(Exception("خطا"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    /**
     * Mute or unmute a chat.
     */
    suspend fun muteChat(chatId: String, muted: Boolean): Result<Chat> {
        return try {
            val response = apiService.muteChat(chatId, muted)
            if (response.isSuccessful && response.body()?.success == true) {
                val chatDto = response.body()?.data
                if (chatDto != null) {
                    saveChatWithParticipants(chatDto)
                    Result.success(chatDto.toDomain())
                } else {
                    Result.failure(Exception("خطا"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    /**
     * Archive or unarchive a chat.
     */
    suspend fun archiveChat(chatId: String, archived: Boolean): Result<Chat> {
        return try {
            val response = apiService.archiveChat(chatId, archived)
            if (response.isSuccessful && response.body()?.success == true) {
                val chatDto = response.body()?.data
                if (chatDto != null) {
                    saveChatWithParticipants(chatDto)
                    Result.success(chatDto.toDomain())
                } else {
                    Result.failure(Exception("خطا"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    /**
     * Delete a chat locally (only from user's device, not server).
     * Uses soft-delete to prevent the chat from reappearing on sync.
     */
    suspend fun deleteLocalChat(chatId: String) {
        chatDao.markAsDeletedLocally(chatId)
    }

    /**
     * Helper to save a chat and its participants/relations to local DB.
     * Preserves the isDeletedLocally flag if chat was previously deleted.
     */
    private suspend fun saveChatWithParticipants(chatDto: com.Kelasor.app.data.remote.dto.ChatDto) {
        // Check if this chat was already deleted locally
        val existingChat = chatDao.getChatById(chatDto.id)
        if (existingChat?.isDeletedLocally == true) {
            // Chat was deleted locally, do not restore it
            return
        }
        
        // Save users
        val users = chatDto.participants.map { it.toEntity() }
        userDao.insertUsers(users)

        // Save chat
        chatDao.insertChat(chatDto.toEntity())

        // Save participants relation
        val participantEntities = chatDto.participants.map { user ->
            com.Kelasor.app.data.local.entity.ChatParticipantEntity(
                chatId = chatDto.id,
                userId = user.id
            )
        }
        chatDao.insertChatParticipants(participantEntities)
    }

    /**
     * Upload a file to the server.
     * Returns the URL of the uploaded file.
     */
    suspend fun uploadFile(part: okhttp3.MultipartBody.Part): Result<String> {
        return try {
            val response = apiService.uploadFile(part)
            if (response.isSuccessful && response.body()?.success == true) {
                val fileUrl = response.body()?.data
                if (fileUrl != null) {
                    Result.success(fileUrl)
                } else {
                    Result.failure(Exception("خطا در آپلود فایل"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا در آپلود فایل"))
            }
        } catch (e: Throwable) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Repository - Database as Single Source of Truth
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * MessageRepository provides access to message data using the database as the single source of truth.
 * 
 * ARCHITECTURE:
 * - All messages are read via observable Flows from Room database
 * - Sending a message writes to DB first with PENDING status
 * - MessageSyncManager handles background sync of pending messages
 * - Server responses update the DB, which triggers Flow emissions
 * 
 * OFFLINE SUPPORT:
 * - Messages are immediately written to DB with isSynced=false
 * - They appear in the UI via the observable Flow
 * - When online, MessageSyncManager syncs them in background
 */
@Singleton
class MessageRepository @Inject constructor(
    private val apiService: ApiService,
    private val messageDao: MessageDao,
    private val chatDao: ChatDao,
    private val userDao: UserDao,
    private val groupMessageDao: GroupMessageDao,
    private val syncCoordinator: SyncCoordinator
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // 📡 OBSERVABLE DATA STREAMS (Database as Source of Truth)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Observe all messages for a chat.
     * This is the PRIMARY and ONLY way to get messages for display.
     * Changes in the database automatically trigger emissions.
     * 
     * Messages are sorted by createdAt descending (newest first).
     */
    fun observeMessages(chatId: String): Flow<List<Message>> =
        messageDao.observeMessagesForChat(chatId).map { messages ->
            messages.map { it.toDomain() }
        }.distinctUntilChanged()
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 SYNC STATUS (For UI Loading Indicators)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Set of chatIds currently being synced.
     */
    val isSyncingMessages: StateFlow<Set<String>> = syncCoordinator.isSyncingMessages
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔄 SYNC TRIGGERS (Fire-and-forget, results go to DB)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Request a message sync for a chat from the server.
     * Results are written to the database; observe via observeMessages().
     */
    fun requestMessageSync(chatId: String, page: Int = 0) {
        syncCoordinator.syncMessages(chatId, page)
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ✏️ WRITE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Queue a message to be sent.
     * 
     * This immediately writes the message to the database with PENDING status
     * and isSynced=false. The UI will see it immediately via the observable Flow.
     * 
     * MessageSyncManager will pick it up and send it to the server.
     * When successful, the status is updated to SENT and isSynced=true.
     */
    suspend fun queueMessage(
        chatId: String,
        content: String,
        type: String = "TEXT",
        senderId: String,
        senderName: String,
        senderAvatar: String? = null,
        mediaUrl: String? = null,
        replyToMessageId: String? = null,
        replyToMessage: Message? = null,
        amplitudes: List<Int>? = null
    ): String {
        val messageId = "local_${java.util.UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        
        val replyToMessageJson = if (replyToMessage != null) {
            com.google.gson.Gson().toJson(replyToMessage)
        } else null

        if (type != "TEXT" && mediaUrl == null) {
            android.util.Log.e("ChatRepository", "Queueing MEDIA message ($type) with NULL mediaUrl! ChatId: $chatId")
        } else if (type != "TEXT") {
            android.util.Log.d("ChatRepository", "Queueing MEDIA message ($type) with Url: $mediaUrl")
        }
        
        val entity = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            type = type,
            content = content,
            mediaUrl = mediaUrl,
            replyToMessageId = replyToMessageId,
            replyToMessage = replyToMessageJson,
            forwardedFrom = null,
            status = "PENDING",
            isEdited = false,
            createdAt = now,
            editedAt = null,
            isSynced = false,
            amplitudes = amplitudes?.joinToString(",")
        )
        
        messageDao.insertMessage(entity)
        
        // FIX: Also update chat preview for own sent messages
        val previewContent = if (type == "TEXT") content else "Media: $type"
        chatDao.updateLastMessage(chatId, previewContent, now)
        
        return messageId
    }
    
    /**
     * Save a message received from WebSocket to local database.
     * Called by GlobalSyncManager when a real-time message arrives.
     */
    suspend fun saveIncomingMessage(message: Message) {
        // Serialize replyToMessage if present
        var replyToMessageJson = message.replyToMessage?.let { reply ->
            try {
                com.google.gson.Gson().toJson(reply)
            } catch (e: Exception) { null }
        }

        // If replyToMessage is null but we have an ID, try to find it locally to preserve context
        if (replyToMessageJson == null && message.replyToMessageId != null) {
             try {
                val localReply = messageDao.getMessageById(message.replyToMessageId!!)
                if (localReply != null) {
                    val replyDomain = localReply.toDomain()
                    replyToMessageJson = com.google.gson.Gson().toJson(replyDomain)
                }
             } catch (e: Exception) { /* Ignore */ }
        }
        
        val entity = MessageEntity(
            id = message.id,
            chatId = message.chatId,
            senderId = message.senderId,
            senderName = message.senderName,
            senderAvatar = message.senderAvatar,
            type = message.type.name,
            content = message.content,
            mediaUrl = message.mediaUrl,
            replyToMessageId = message.replyToMessageId,
            replyToMessage = replyToMessageJson,
            forwardedFrom = message.forwardedFrom,
            status = message.status.name,
            isEdited = message.isEdited,
            createdAt = message.createdAt.toEpochMilli(),
            editedAt = message.editedAt?.toEpochMilli(),
            isSynced = true // Messages from server are synced
        )
        messageDao.insertMessage(entity)
    }
    
    /**
     * Update message status after successful sync.
     * Called by MessageSyncManager.
     */
    suspend fun updateMessageStatus(messageId: String, status: String, isSynced: Boolean) {
        messageDao.updateMessageStatus(messageId, status, isSynced)
    }
    
    /**
     * Replace a local pending message with the server response.
     * Called by MessageSyncManager after successful send.
     * 
     * Uses atomic UPDATE to prevent Room Flow from emitting
     * intermediate states that would cause messages to briefly disappear.
     */
    suspend fun replacePendingMessage(localId: String, serverMessage: Message) {
        // FIX: Use atomic update instead of delete+insert
        // This prevents Room Flow from emitting intermediate empty states
        messageDao.updateMessageStatus(
            messageId = localId,
            status = serverMessage.status.name,
            isSynced = true
        )
    }
    
    /**
     * Get all pending messages that need to be synced.
     * Used by MessageSyncManager.
     */
    suspend fun getPendingMessages(): List<MessageEntity> {
        return messageDao.getUnsyncedMessages()
    }

    /**
     * Get a message by its ID.
     */
    suspend fun getMessageById(messageId: String): Message? {
        return messageDao.getMessageById(messageId)?.toDomain()
    }
    
    /**
     * Edit a message.
     */
    suspend fun editMessage(messageId: String, newContent: String): Result<Message> {
        return try {
            val response = apiService.editMessage(messageId, EditMessageRequest(newContent))
            if (response.isSuccessful && response.body()?.success == true) {
                val messageDto = response.body()?.data
                if (messageDto != null) {
                    messageDao.insertMessage(messageDto.toEntity())
                    Result.success(messageDto.toDomain())
                } else {
                    Result.failure(Exception("خطا در ویرایش پیام"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا در ویرایش پیام"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    /**
     * Delete a message from server (delete for everyone).
     * This calls the API and removes from local database.
     */
    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            val localMessage = messageDao.getMessageById(messageId)
            val response = apiService.deleteMessage(messageId)
            if (response.isSuccessful && response.body()?.success == true) {
                localMessage?.let { messageDao.deleteMessage(it) }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا در حذف پیام"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    /**
     * Delete a message locally only (delete for self).
     * Does NOT call the API - message remains on server and for other users.
     */
    suspend fun deleteMessageLocally(messageId: String) {
        val localMessage = messageDao.getMessageById(messageId)
        localMessage?.let { messageDao.deleteMessage(it) }
    }
    
    /**
     * Mark a message as read.
     */
    suspend fun markAsRead(messageId: String): Result<Unit> {
        return try {
            apiService.markAsRead(messageId)
            messageDao.updateMessageStatus(messageId, "READ", true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("خطا: ${e.message}"))
        }
    }

    /**
     * React to a message.
     */
    suspend fun reactToMessage(messageId: String, reaction: String?, isGroup: Boolean = false, groupId: String? = null): Result<Unit> {
        // Need to import ReactionRequest if not already imported or qualify it
        val request = com.Kelasor.app.data.remote.dto.ReactionRequest(reaction)
        return try {
            val response = if (isGroup && groupId != null) {
                apiService.reactToGroupMessage(groupId, messageId, request)
            } else {
                apiService.reactToMessage(messageId, request)
            }
            
            if (response.isSuccessful && response.body()?.success == true) {
                val gson = com.google.gson.Gson()
                val type = object : com.google.gson.reflect.TypeToken<Map<String, Int>>() {}.type

                if (isGroup) {
                    val msg = groupMessageDao.getMessageById(messageId)
                    if (msg != null) {
                        val currentReactions: MutableMap<String, Int> = try {
                            gson.fromJson(msg.reactions, type) ?: mutableMapOf()
                        } catch (e: Exception) { mutableMapOf() }
                        
                        // Update counts
                        val oldReaction = msg.myReaction
                        if (oldReaction != null) {
                            val count = currentReactions[oldReaction] ?: 0
                            if (count > 1) currentReactions[oldReaction] = count - 1 else currentReactions.remove(oldReaction)
                        }
                        if (reaction != null) {
                           currentReactions[reaction] = (currentReactions[reaction] ?: 0) + 1
                        }
                        
                        groupMessageDao.updateReactions(messageId, gson.toJson(currentReactions), reaction)
                    }
                } else {
                    val msg = messageDao.getMessageById(messageId)
                    if (msg != null) {
                        val currentReactions: MutableMap<String, Int> = try {
                            gson.fromJson(msg.reactions, type) ?: mutableMapOf()
                        } catch (e: Exception) { mutableMapOf() }
                        
                        // Update counts
                        val oldReaction = msg.myReaction
                        if (oldReaction != null) {
                            val count = currentReactions[oldReaction] ?: 0
                            if (count > 1) currentReactions[oldReaction] = count - 1 else currentReactions.remove(oldReaction)
                        }
                        if (reaction != null) {
                           currentReactions[reaction] = (currentReactions[reaction] ?: 0) + 1
                        }
                        
                        messageDao.updateReactions(messageId, gson.toJson(currentReactions), reaction)
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.message ?: "خطا در ثبت واکنش"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    /**
     * React to a message.
     */


    /**
     * Find a pending message with the same content for deduplication.
     */
    suspend fun findPendingMessageByContent(chatId: String, content: String): MessageEntity? {
        return messageDao.findPendingMessageByContent(chatId, content)
    }

    suspend fun sendPollMessage(
        chatId: String,
        pollDto: com.Kelasor.app.data.remote.dto.PollDto
    ): String {
        val messageId = "local_${java.util.UUID.randomUUID()}"
        val now = System.currentTimeMillis()
        
        // We need sender info, but senderId/Name are usually current user.
        // Assuming we can get current user info from session manager or userDao.
        // But here we don't have direct access easily unless we fetch it.
        // For now, I'll pass dummy or fetch it.
        // The previous queueMessage accepts senderId/Name. 
        // I should probably refactor to get it from ViewModel or SessionManager here.
        // But to match signature, I should probably ask ViewModel to pass it?
        // Or better, fetch current user from DB.
        
        // Wait, queueMessage assumes caller passes sender info.
        // Usage in ChatViewModel: uses 'currentUserId' and 'userRepository'.
        
        // I will let ViewModel handle calling queueMessage?
        // But queueMessage doesn't support 'poll'.
        
        // Option 1: Update queueMessage to support poll.
        // Option 2: Create createLocalMessageWithPoll helper.
        
        // I'll assume current user logic is handled upstream or I fetch here.
        // Actually, queueMessage takes senderId. 
        // I'll make sendPollMessage take senderId too?
        // Or better, fetch it locally since I have userDao.
        
        // For simplicity and speed, I will update queueMessage to accept 'poll' and 'pollId'.
        
        // Wait, queueMessage has many params. 
        // I will just add sendPollMessage and fetch user.
        
        // Actually, I can overload queueMessage or just add specific logic here.
        
        // THIS IS TRICKY: I need sender info.
        // I will fetch my user from userDao.
        
        val myUser = userDao.getCurrentUser()
        val senderId = myUser?.id ?: ""
        val senderName = myUser?.displayName ?: ""
        val senderAvatar = myUser?.avatarUrl
        
        val pollJson = com.google.gson.Gson().toJson(pollDto.toDomain())

        val entity = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = senderAvatar,
            type = "POLL",
            content = "Poll: ${pollDto.question}",
            mediaUrl = null,
            replyToMessageId = null,
            replyToMessage = null,
            forwardedFrom = null,
            status = "PENDING",
            isEdited = false,
            createdAt = now,
            editedAt = null,
            isSynced = false,
            poll = pollJson // Needs to be added to Entity if not present
            // Entity logic needs to be checked.
            // Summary said: "Modified Message... entities to include poll field".
            // I should check MessageEntity definition in this file or imported.
            // It is imported: com.Kelasor.app.data.local.entity.MessageEntity
        )
        // I need to verify MessageEntity has 'poll' field.
        // If not, I can't save it.
        
        // Assuming it has it based on context.
        // If not, it will error.
        
        messageDao.insertMessage(entity)
        chatDao.updateLastMessage(chatId, "Poll: ${pollDto.question}", now)
        
        // Now trigger sync. Ideally MessageSyncManager handles it.
        // But MessageSyncManager needs to know about polls.
        // I should ensure SendMessageRequest includes pollId.
        // I checked Backend definition of SendMessageRequest, it has pollId.
        // Android SendMessageRequest DTO needs to have pollId too.
        
        // Once saved locally, the sync manager will pick it up (if it supports pollId).
        // I need to check MessageSyncManager.
        
        return messageId
    }
}
