package com.Kelasor.app.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.Kelasor.app.data.local.dao.MessageDao
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.SendMessageRequest
import com.Kelasor.app.domain.mapper.toEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MessageSyncManager handles synchronization of pending messages that were created
 * offline or failed to send. It observes network connectivity and retries pending
 * messages when connectivity is restored.
 * 
 * ARCHITECTURE:
 * - Queries database for messages with isSynced=false
 * - Attempts to send each via API
 * - On success: replaces local message with server response
 * - On failure: leaves message for retry, marks as FAILED after max attempts
 * 
 * This ensures messages are never lost on app restart or connectivity issues.
 * 
 * FIX: Added Mutex lock to prevent duplicate syncs that were causing triple sends.
 */
@Singleton
class MessageSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao,
    private val apiService: ApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false
    
    // Mutex to ensure only one sync runs at a time
    private val syncMutex = Mutex()
    
    // Track which message IDs are currently being synced to prevent duplicates
    private val syncingMessageIds = mutableSetOf<String>()
    
    // Observable sync status for UI
    private val _isSyncing = MutableStateFlow(false)
    val isSyncingStatus: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()
    
    // Debounce tracking
    private var lastSyncTime = 0L
    private val minSyncIntervalMs = 2000L // Minimum 2 seconds between syncs
    
    private val connectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Network available, scheduling sync")
            // Use debounced sync to prevent multiple triggers
            scheduleDebouncedSync()
        }
        
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            // Don't trigger sync here - onAvailable is sufficient
            // This was causing duplicate syncs
        }
    }
    
    companion object {
        private const val TAG = "MessageSyncManager"
        private const val MAX_RETRY_AGE_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val SYNC_DELAY_BETWEEN_MESSAGES_MS = 100L
    }
    
    /**
     * Initialize the sync manager. Should be called once on app startup.
     * Registers for network callbacks and syncs any pending messages.
     */
    fun initialize() {
        if (isInitialized) return
        isInitialized = true
        
        // Register for network changes
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
        
        // Sync any pending messages from previous sessions (only once on startup)
        scheduleDebouncedSync()
        
        // Update pending count periodically
        scope.launch {
            while (true) {
                updatePendingCount()
                delay(5000) // Check every 5 seconds
            }
        }
        
        Log.d(TAG, "MessageSyncManager initialized")
    }
    
    /**
     * Schedule a debounced sync.
     * Prevents multiple syncs from running in quick succession.
     */
    private fun scheduleDebouncedSync() {
        val now = System.currentTimeMillis()
        if (now - lastSyncTime < minSyncIntervalMs) {
            Log.d(TAG, "Sync debounced, last sync was ${now - lastSyncTime}ms ago")
            return
        }
        
        scope.launch {
            // Small delay to batch rapid calls
            delay(500)
            syncPendingMessages()
        }
    }
    
    /**
     * Update the count of pending messages.
     */
    private suspend fun updatePendingCount() {
        try {
            val count = messageDao.getUnsyncedMessages().size
            _pendingCount.value = count
        } catch (e: Exception) {
            Log.e(TAG, "Error counting pending messages", e)
        }
    }
    
    /**
     * Sync all pending messages that haven't been sent to the server.
     * Uses database as the source of truth for pending messages.
     * 
     * FIX: Uses Mutex to ensure only one sync runs at a time,
     * preventing the duplicate/triple send issue.
     */
    fun syncPendingMessages() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network not available, skipping sync")
            return
        }
        
        scope.launch {
            // Use mutex to ensure only one sync runs at a time
            if (!syncMutex.tryLock()) {
                Log.d(TAG, "Sync already in progress (mutex locked), skipping")
                return@launch
            }
            
            try {
                lastSyncTime = System.currentTimeMillis()
                _isSyncing.value = true
                
                val pendingMessages = messageDao.getUnsyncedMessages()
                Log.d(TAG, "Found ${pendingMessages.size} pending messages to sync")
                
                if (pendingMessages.isEmpty()) {
                    return@launch
                }
                
                for (message in pendingMessages) {
                    // Skip if already marked as failed
                    if (message.status == "FAILED") {
                        continue
                    }
                    
                    // Skip if this message is already being synced (shouldn't happen with mutex, but extra safety)
                    if (syncingMessageIds.contains(message.id)) {
                        Log.d(TAG, "Message ${message.id} already being synced, skipping")
                        continue
                    }
                    
                    try {
                        syncingMessageIds.add(message.id)
                        Log.d(TAG, "Syncing message ${message.id} to chat ${message.chatId}")
                        
                            val pollId = if (message.type == "POLL" && message.poll != null) {
                                try {
                                    val poll = com.google.gson.Gson().fromJson(message.poll, com.Kelasor.app.data.remote.dto.PollDto::class.java)
                                    poll?.id
                                } catch (e: Exception) { null }
                            } else null
                            
                            // Parse amplitudes from string if present
                            val amplitudesList = message.amplitudes?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
    
                            val response = apiService.sendMessage(
                                message.chatId,
                                SendMessageRequest(
                                    type = message.type,
                                    content = message.content,
                                    mediaUrl = message.mediaUrl,
                                    replyToMessageId = message.replyToMessageId,
                                    pollId = pollId,
                                    amplitudes = amplitudesList
                                )
                            )
                        
                        if (response.isSuccessful && response.body()?.success == true) {
                            val serverMessage = response.body()?.data
                            if (serverMessage != null) {
                                // FIX: Delete local message and insert server message
                                // This prevents duplicate messages (local + server versions)
                                // Note: We first insert the server message, then delete the local one
                                // to minimize the chance of the message disappearing from UI
                                
                                // IMPORTANT: Preserve the local replyToMessage JSON since the server
                                // response may not include the full replyToMessage object
                                val serverEntity = serverMessage.toEntity()
                                
                                // FIX: If server returns null/TEXT for media messages, preserve local type and mediaUrl
                                // This is critical to prevent media appearing as text
                                val isMediaType = message.type in listOf("IMAGE", "VIDEO", "AUDIO", "VOICE", "FILE", "LOCATION")
                                val finalServerEntity = if (isMediaType && (serverEntity.mediaUrl == null || serverEntity.type == "TEXT")) {
                                    serverEntity.copy(
                                        mediaUrl = message.mediaUrl ?: serverEntity.mediaUrl,
                                        type = message.type,
                                        amplitudes = message.amplitudes ?: serverEntity.amplitudes
                                    )
                                } else if (serverEntity.mediaUrl == null && message.mediaUrl != null) {
                                    serverEntity.copy(mediaUrl = message.mediaUrl, type = message.type)
                                } else {
                                    serverEntity
                                }

                                val entityWithReply = if (finalServerEntity.replyToMessage == null && message.replyToMessage != null) {
                                    finalServerEntity.copy(replyToMessage = message.replyToMessage)
                                } else {
                                    finalServerEntity
                                }
                                
                                // FIX: Use atomic replacement to prevent UI flicker/race conditions
                                messageDao.replacePendingMessage(message.id, entityWithReply)
                                Log.d(TAG, "Message ${message.id} replaced with server id: ${serverMessage.id} (type: ${entityWithReply.type})")
                            }
                        } else {
                            handleSyncFailure(message)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing message ${message.id}: ${e.message}")
                        handleSyncFailure(message)
                    } finally {
                        syncingMessageIds.remove(message.id)
                    }
                    
                    // Small delay between messages to avoid overwhelming server
                    delay(SYNC_DELAY_BETWEEN_MESSAGES_MS)
                }
                
                updatePendingCount()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync: ${e.message}", e)
            } finally {
                _isSyncing.value = false
                syncMutex.unlock()
            }
        }
    }
    
    /**
     * Handle sync failure for a message.
     * Marks as FAILED if too old.
     */
    private suspend fun handleSyncFailure(message: com.Kelasor.app.data.local.entity.MessageEntity) {
        val ageMillis = System.currentTimeMillis() - message.createdAt
        if (ageMillis > MAX_RETRY_AGE_MS) {
            Log.d(TAG, "Message ${message.id} is too old (${ageMillis}ms), marking as FAILED")
            messageDao.updateMessageStatus(message.id, "FAILED", false)
        }
        // Otherwise, leave for retry on next sync
    }
    
    /**
     * Retry a specific failed message.
     */
    fun retryMessage(messageId: String) {
        scope.launch {
            try {
                // Reset status to PENDING so it will be picked up by next sync
                messageDao.updateMessageStatus(messageId, "PENDING", false)
                // Trigger sync
                syncPendingMessages()
            } catch (e: Exception) {
                Log.e(TAG, "Error retrying message $messageId", e)
            }
        }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
