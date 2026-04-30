package com.Kelasor.app.data.sync

import android.util.Log
import com.Kelasor.app.data.local.dao.ChatDao
import com.Kelasor.app.data.local.dao.MessageDao
import com.Kelasor.app.data.local.dao.UserDao
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.domain.mapper.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncCoordinator is the central orchestrator for all data synchronization in the app.
 * 
 * PRINCIPLE: The database is the ONLY source of truth. All syncs write to DB,
 * and the UI observes DB changes via Room's reactive Flows.
 * 
 * This coordinator handles:
 * - Chat list sync (from API to DB)
 * - Message sync per chat (from API to DB)
 * - User/participant sync (from API to DB)
 * 
 * It exposes sync status for UI loading indicators but NEVER exposes data directly.
 * Data flows: API → SyncCoordinator → DB → (Room Flow) → UI
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val apiService: ApiService,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Sync status for UI loading indicators
    private val _isSyncingChats = MutableStateFlow(false)
    val isSyncingChats: StateFlow<Boolean> = _isSyncingChats.asStateFlow()
    
    private val _isSyncingMessages = MutableStateFlow<Set<String>>(emptySet())
    val isSyncingMessages: StateFlow<Set<String>> = _isSyncingMessages.asStateFlow()
    
    private val _lastSyncError = MutableStateFlow<String?>(null)
    val lastSyncError: StateFlow<String?> = _lastSyncError.asStateFlow()
    
    // Mutex to prevent concurrent syncs for the same resource
    private val chatSyncMutex = Mutex()
    private val messageSyncMutexes = mutableMapOf<String, Mutex>()
    
    companion object {
        private const val TAG = "SyncCoordinator"
    }
    
    /**
     * Sync all chats from the server.
     * Results are written to Room database; UI observes via Flow.
     * 
     * @param page Page number for pagination (default 0)
     */
    fun syncChats(page: Int = 0) {
        scope.launch {
            chatSyncMutex.withLock {
                _isSyncingChats.value = true
                _lastSyncError.value = null
                
                try {
                    val response = apiService.getChats(page)
                    if (response.isSuccessful) {
                        val chatDtos = response.body()?.chats ?: emptyList()
                        
                        chatDtos.forEach { chatDto ->
                            // Check if chat is deleted locally
                            val existingChat = chatDao.getChatById(chatDto.id)
                            if (existingChat?.isDeletedLocally == true) {
                                // Skip restoration of locally deleted chat
                                return@forEach
                            }
                            
                            // Save users first (participants)
                            val users = chatDto.participants.map { it.toEntity() }
                            userDao.insertUsers(users)
                            
                            // Save chat
                            chatDao.insertChat(chatDto.toEntity())
                            
                            // Save participant relations
                            val participantEntities = chatDto.participants.map { user ->
                                com.Kelasor.app.data.local.entity.ChatParticipantEntity(
                                    chatId = chatDto.id,
                                    userId = user.id
                                )
                            }
                            chatDao.insertChatParticipants(participantEntities)
                        }
                        
                        Log.d(TAG, "Synced ${chatDtos.size} chats to database")
                    } else {
                        _lastSyncError.value = "Chat sync failed: ${response.code()}"
                        Log.e(TAG, "Chat sync failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    _lastSyncError.value = "Chat sync error: ${e.message}"
                    Log.e(TAG, "Chat sync error", e)
                } finally {
                    _isSyncingChats.value = false
                }
            }
        }
    }
    
    /**
     * Sync messages for a specific chat from the server.
     * Results are written to Room database; UI observes via Flow.
     * 
     * @param chatId The chat to sync messages for
     * @param page Page number for pagination (default 0)
     */
    fun syncMessages(chatId: String, page: Int = 0) {
        scope.launch {
            val mutex = messageSyncMutexes.getOrPut(chatId) { Mutex() }
            
            mutex.withLock {
                _isSyncingMessages.value = _isSyncingMessages.value + chatId
                
                try {
                    val response = apiService.getMessages(chatId, page)
                    if (response.isSuccessful) {
                        val messageDtos = response.body()?.messages ?: emptyList()
                        
                        if (messageDtos.isNotEmpty()) {
                            // IMPORTANT: Preserve local replyToMessage JSON for messages
                            // Server may not return full replyToMessage object, only replyToMessageId
                            val serverEntities = messageDtos.map { it.toEntity() }
                            val entitiesToInsert = serverEntities.map { serverEntity ->
                                val localMessage = messageDao.getMessageById(serverEntity.id)
                                if (localMessage != null && 
                                    localMessage.replyToMessage != null && 
                                    serverEntity.replyToMessage == null) {
                                    // Preserve local replyToMessage JSON
                                    serverEntity.copy(replyToMessage = localMessage.replyToMessage)
                                } else {
                                    serverEntity
                                }
                            }
                            
                            // Insert messages to database
                            // OnConflictStrategy.REPLACE ensures existing messages are updated
                            messageDao.insertMessages(entitiesToInsert)
                            Log.d(TAG, "Synced ${messageDtos.size} messages for chat $chatId")
                        }
                    } else {
                        Log.e(TAG, "Message sync failed for $chatId: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Message sync error for $chatId", e)
                } finally {
                    _isSyncingMessages.value = _isSyncingMessages.value - chatId
                }
            }
        }
    }
    
    /**
     * Sync a specific chat's details from the server.
     * Used when entering a chat to ensure we have latest metadata.
     * 
     * @param chatId The chat to sync
     */
    fun syncChatDetails(chatId: String) {
        scope.launch {
            try {
                val response = apiService.getChatById(chatId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val chatDto = response.body()?.data
                    if (chatDto != null) {
                        // Check if chat is deleted locally
                        val existingChat = chatDao.getChatById(chatDto.id)
                        if (existingChat?.isDeletedLocally == true) {
                            return@launch
                        }
                        // Save users first
                        val users = chatDto.participants.map { it.toEntity() }
                        userDao.insertUsers(users)
                        // Save chat
                        chatDao.insertChat(chatDto.toEntity())
                        // Save participant relations
                        val participantEntities = chatDto.participants.map { user ->
                            com.Kelasor.app.data.local.entity.ChatParticipantEntity(
                                chatId = chatDto.id,
                                userId = user.id
                            )
                        }
                        chatDao.insertChatParticipants(participantEntities)
                        // Handle chatId redirect: if returned chatId differs from requested,
                        // the requested ID was likely a userId. Sync messages for the real chatId.
                        if (chatDto.id != chatId) {
                            Log.d(TAG, "Chat ID redirect: $chatId → ${chatDto.id} (syncing messages)")
                            syncMessages(chatDto.id)
                        }
                        Log.d(TAG, "Synced chat details for $chatId (resolved: ${chatDto.id})")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Chat details sync error for $chatId", e)
            }
        }
    }
    
    /**
     * Perform a full sync of all data.
     * Called on app startup and when network connectivity is restored.
     */
    fun performFullSync() {
        syncChats()
        // Messages are synced on-demand when entering a chat
    }
    
    /**
     * Clear the last sync error.
     */
    fun clearError() {
        _lastSyncError.value = null
    }
}
