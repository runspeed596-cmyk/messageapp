package com.Kelasor.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.ChatRepository
import com.Kelasor.app.data.repository.ChannelRepository
import com.Kelasor.app.data.repository.ContactsRepository
import com.Kelasor.app.data.repository.GroupRepository
import com.Kelasor.app.data.repository.MessageRepository
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.data.sync.MessageSyncManager
import com.Kelasor.app.data.websocket.WebSocketManager
import com.Kelasor.app.domain.model.Chat
import com.Kelasor.app.domain.model.ChatType
import com.Kelasor.app.domain.model.Channel
import com.Kelasor.app.domain.model.Group
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import com.Kelasor.app.data.voice.VoiceRecorderManager
import com.Kelasor.app.data.audio.AudioPlayerManager
import com.Kelasor.app.data.location.LocationManager
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat List ViewModel - Observable Architecture
// ═══════════════════════════════════════════════════════════════════════════════

enum class SearchFilter {
    ALL, PEOPLE, GROUP, CHANNEL
}

data class ChatListState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val pinnedChats: List<Chat> = emptyList(),
    val archivedChats: List<Chat> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val activeFilter: SearchFilter = SearchFilter.ALL,
    val searchResults: List<User> = emptyList(), // Keep for legacy/people
    val channelSearchResults: List<Channel> = emptyList(), // Add for channels
    val localGroups: List<Chat> = emptyList(), // Store joined groups
    val localChannels: List<Chat> = emptyList(), // Store subscribed channels
    val selectedChatIds: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val currentUserId: String = ""
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val groupRepository: GroupRepository,
    private val channelRepository: ChannelRepository,
    private val contactsRepository: ContactsRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _state = MutableStateFlow(ChatListState())
    val state: StateFlow<ChatListState> = _state.asStateFlow()
    
    // Reactive map of phone number to contact name
    private val phoneToName = MutableStateFlow<Map<String, String>>(emptyMap())
    
    val events = MutableSharedFlow<ChatListEvent>()

    init {
        // ARCHITECTURE: Observe chats from Room database - this is the SINGLE SOURCE OF TRUTH
        observeChats()
        observeSyncStatus()
        
        // Trigger initial sync from server (results go to DB, then to UI via Flow)
        chatRepository.requestChatSync()
        // Also sync groups and channels? 
        // groupRepository.refreshGroups() 
        // channelRepository.refreshChannels() 
        // Assume repositories have auto-refresh or called elsewhere, or add calls here if needed.
        viewModelScope.launch {
            // channelRepository.getChannels() // trigger refresh
        }
    }
    
    fun onUserSelected(userId: String) {
        viewModelScope.launch {
            // 1. Check if we already have a chat with this user locally
            val existingChat = _state.value.chats.find { chat ->
                chat.participants.any { it.id == userId }
            } ?: _state.value.pinnedChats.find { chat ->
                chat.participants.any { it.id == userId }
            } ?: _state.value.archivedChats.find { chat ->
                chat.participants.any { it.id == userId }
            }
            
            if (existingChat != null) {
                events.emit(ChatListEvent.NavigateToChat(existingChat))
                return@launch
            }
            
            // 2. If not found locally, create via API
            _state.update { it.copy(isLoading = true) }
            val result = chatRepository.createChat(userId)
            result.fold(
                onSuccess = { chat ->
                    _state.update { it.copy(isLoading = false) }
                    events.emit(ChatListEvent.NavigateToChat(chat))
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                val deviceContacts = contactsRepository.getDeviceContacts()
                val contactMap = deviceContacts.associate { 
                    normalizePhoneNumber(it.phoneNumber) to it.name 
                }
                phoneToName.value = contactMap
                
                // Persist device names to local DB by updating User entities
                if (contactMap.isNotEmpty()) {
                    userRepository.matchContacts(contactMap).collect {
                         // Users updated in DB with contactName
                    }
                }
            } catch (e: SecurityException) {
                // Permission not granted yet
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error loading contacts: ${e.message}") }
            }
        }
    }
    
    /**
     * Observe chats from Room database.
     * This is the SINGLE SOURCE OF TRUTH for chat data.
     * Any changes in the database will automatically update the UI.
     */
    /**
     * Observe chats, groups, and channels from Room database.
     * Combines all sources into a single list of [Chat].
     */
    private fun observeChats() {
        // Combine active chats, groups, channels, contact names, and userId
        viewModelScope.launch {
            Log.d("ChatListViewModel", "🔄 Starting to observe chats...")
            combine(
                chatRepository.observeChats().distinctUntilChanged(),
                groupRepository.observeGroups().distinctUntilChanged(),
                channelRepository.observeSubscribedChannels().distinctUntilChanged(),
                phoneToName,
                sessionManager.userId
            ) { chats, groups, channels, contactMap, userId ->
                val groupChats = groups.map { group -> group.toChat() }
                val channelChats = channels.map { channel -> channel.toChat() }
                val allChats = chats + groupChats + channelChats
                val sortedChats = allChats.sortedByDescending { chat ->
                    chat.lastMessage?.createdAt ?: chat.updatedAt
                }
                Triple(sortedChats, contactMap, userId)
            }.flowOn(kotlinx.coroutines.Dispatchers.Default).collect { (chats, contactMap, userId) ->
                Log.d("ChatListViewModel", "📥 Received ${chats.size} chats from Flow, first chat lastMessage='${chats.firstOrNull()?.lastMessage?.content}'")
                val chatsWithContactNames = chats.map { chat -> 
                    // Only apply contact name logic to PRIVATE chats
                    if (chat.type == ChatType.PRIVATE) {
                        applyPhoneContactName(chat, contactMap, userId) 
                    } else {
                        chat
                    }
                }
                
                // Separate by type
                val privateChatsList = chatsWithContactNames.filter { it.type == ChatType.PRIVATE }
                val groupsList = chatsWithContactNames.filter { it.type == ChatType.GROUP }
                val channelsList = chatsWithContactNames.filter { it.type == ChatType.CHANNEL }
                
                _state.update {
                    it.copy(
                        // Default view shows only Private chats
                        chats = privateChatsList.filter { chat -> !chat.isPinned && !chat.isArchived },
                        pinnedChats = privateChatsList.filter { chat -> chat.isPinned },
                        // Store others for search
                        localGroups = groupsList,
                        localChannels = channelsList,
                        isLoading = false,
                        currentUserId = userId ?: ""
                    )
                }
                Log.d("ChatListViewModel", "✅ State updated with ${privateChatsList.size} chats, ${groupsList.size} groups, ${channelsList.size} channels")
            }
        }
        
        // Observe archived chats separately
        viewModelScope.launch {
            combine(
                chatRepository.observeArchivedChats(),
                phoneToName,
                sessionManager.userId
            ) { archived, contactMap, userId ->
                Triple(archived, contactMap, userId)
            }.collect { (archived, contactMap, userId) ->
                val archivedWithContactNames = archived.map { chat -> 
                    applyPhoneContactName(chat, contactMap, userId) 
                }
                _state.update { it.copy(archivedChats = archivedWithContactNames) }
            }
        }
    }
    
    /**
     * Observe sync status from repository.
     */
    private fun observeSyncStatus() {
        viewModelScope.launch {
            chatRepository.isSyncingChats.collect { isSyncing ->
                _state.update { it.copy(isSyncing = isSyncing) }
            }
        }
        
        viewModelScope.launch {
            chatRepository.lastSyncError.collect { error ->
                if (error != null) {
                    _state.update { it.copy(error = error) }
                }
            }
        }
    }
    
    /**
     * Apply phone contact name to chat title if the other participant is in phone contacts
     */
    private fun applyPhoneContactName(chat: Chat, contactMap: Map<String, String>, currentUserId: String?): Chat {
        val otherParticipant = chat.participants.find { it.id != currentUserId }
        if (otherParticipant != null && otherParticipant.phoneNumber != null) {
            val normalizedPhone = normalizePhoneNumber(otherParticipant.phoneNumber)
            val phoneContactName = contactMap[normalizedPhone]
            if (phoneContactName != null) {
                return chat.copy(title = phoneContactName)
            }
        }
        return chat
    }
    
    private fun normalizePhoneNumber(number: String): String {
        var normalized = number.replace(Regex("[^0-9+]"), "")
        if (normalized.startsWith("+98")) {
            normalized = "0" + normalized.substring(3)
        }
        normalized = normalized.removePrefix("+")
        return normalized
    }
    
    /**
     * Refresh chats from server.
     * Called on pull-to-refresh.
     */
    fun refreshChats() {
        chatRepository.requestChatSync()
    }
    
    fun pinChat(chatId: String, pinned: Boolean) {
        viewModelScope.launch {
            chatRepository.pinChat(chatId, pinned)
        }
    }
    
    fun muteChat(chatId: String, muted: Boolean) {
        viewModelScope.launch {
            chatRepository.muteChat(chatId, muted)
        }
    }
    
    fun archiveChat(chatId: String, archived: Boolean) {
        viewModelScope.launch {
            chatRepository.archiveChat(chatId, archived)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            chatRepository.deleteLocalChat(chatId)
        }
    }
    
    fun setFilter(filter: SearchFilter) {
        _state.update { it.copy(activeFilter = filter) }
        // Re-trigger search with current query
        setSearchQuery(_state.value.searchQuery)
    }

    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        val filter = _state.value.activeFilter
        
        if (query.length >= 2) {
            viewModelScope.launch {
                // 1. Search Users if ALL or PEOPLE
                if (filter == SearchFilter.ALL || filter == SearchFilter.PEOPLE) {
                    userRepository.searchUsers(query).collect { result ->
                        if (result is UserResult.Success) {
                            _state.update { it.copy(searchResults = result.data) }
                        } else if (result is UserResult.Error) {
                            _state.update { it.copy(searchResults = emptyList()) }
                        }
                    }
                } else {
                    _state.update { it.copy(searchResults = emptyList()) }
                }

                // 2. Search Channels if ALL or CHANNEL
                if (filter == SearchFilter.ALL || filter == SearchFilter.CHANNEL) {
                    channelRepository.searchChannels(query).collect { result ->
                        if (result is com.Kelasor.app.data.repository.ChannelResult.Success) {
                            _state.update { it.copy(channelSearchResults = result.data) }
                        } else {
                            _state.update { it.copy(channelSearchResults = emptyList()) }
                        }
                    }
                } else {
                    _state.update { it.copy(channelSearchResults = emptyList()) }
                }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList(), channelSearchResults = emptyList()) }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun Group.toChat(): Chat {
        return Chat(
            id = this.id,
            type = ChatType.GROUP,
            title = this.name,
            avatarUrl = this.avatarUrl,
            lastMessage = this.lastMessage,
            unreadCount = 0, // Groups don't track unread count in GroupEntity easily yet?
            // Actually GroupEntity DOES NOT have unread count. Need to be added.
            // For now 0.
            isPinned = false, // GroupEntity doesn't have isPinned/Archived/Muted?
            isMuted = false,
            isArchived = false,
            participants = this.members.map { it.user },
            updatedAt = this.lastMessage?.createdAt ?: this.createdAt
        )
    }

    fun toggleChatSelection(chatId: String) {
        _state.update { 
            val currentSelected = it.selectedChatIds.toMutableSet()
            if (currentSelected.contains(chatId)) {
                currentSelected.remove(chatId)
            } else {
                currentSelected.add(chatId)
            }
            it.copy(selectedChatIds = currentSelected)
        }
    }
    
    fun clearSelection() {
        _state.update { it.copy(selectedChatIds = emptySet()) }
    }
    
    fun requestDeleteSelection() {
        if (_state.value.selectedChatIds.isNotEmpty()) {
            _state.update { it.copy(showDeleteConfirmation = true) }
        }
    }
    
    fun cancelDeleteSelection() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }
    
    fun confirmDeleteSelection() {
        val selected = _state.value.selectedChatIds
        if (selected.isEmpty()) return
        
        viewModelScope.launch {
            selected.forEach { chatId ->
                chatRepository.deleteLocalChat(chatId)
            }
            _state.update { it.copy(selectedChatIds = emptySet(), showDeleteConfirmation = false) }
        }
    }
    
    // Deprecated: Access via request/confirm logic
    private fun deleteSelectedChats() {
        confirmDeleteSelection() 
    }

    private fun Channel.toChat(): Chat {
        return Chat(
            id = this.id,
            type = ChatType.CHANNEL,
            title = this.name,
            avatarUrl = this.avatarUrl,
            lastMessage = null, // ChannelEntity doesn't have lastMessage yet.
            // Requirement was "including last post previews". 
            // Since we can't easily get it without DB change, defaulting to null.
            unreadCount = 0,
            isPinned = false,
            isMuted = false,
            isArchived = false, // ChannelEntity doesn't have
            participants = emptyList(), // Channels don't have participants list for Chat view
            updatedAt = this.createdAt // Should use last post time if available
        )
    }
}

sealed class ChatListEvent {
    data class NavigateToChat(val chat: Chat) : ChatListEvent()
}


// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Conversation ViewModel - FIXED for stable message display
// ═══════════════════════════════════════════════════════════════════════════════

data class ConversationState(
    val isLoading: Boolean = true,
    val isInitialLoad: Boolean = true,
    val isSyncing: Boolean = false,
    val isSending: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f, // 0.0 to 1.0
    val chat: Chat? = null,
    val messages: List<Message> = emptyList(),
    val error: String? = null,
    val currentUserId: String = "",
    val isOtherUserTyping: Boolean = false,
    val pendingMessageCount: Int = 0,
    val selectedMessageIds: Set<String> = emptySet(),
    val replyToMessage: Message? = null
)

sealed class ConversationEvent {
    data object MessageSent : ConversationEvent()
    data class Error(val message: String) : ConversationEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val messageSyncManager: MessageSyncManager,
    private val sessionManager: SessionManager,
    private val webSocketManager: WebSocketManager,
    private val contactsRepository: ContactsRepository,
    val voiceRecorderManager: VoiceRecorderManager,
    val audioPlayerManager: AudioPlayerManager,
    val locationManager: LocationManager,
    private val pollRepository: com.Kelasor.app.data.repository.PollRepository,
    private val soundPlayer: com.Kelasor.app.data.media.SoundPlayer,
    private val currentChatManager: com.Kelasor.app.data.session.CurrentChatManager,
    val videoNoteRecorderManager: com.Kelasor.app.data.video.VideoNoteRecorderManager
) : ViewModel() {

    fun setActiveChat(id: String) {
        currentChatManager.setChat(id)
    }

    fun clearActiveChat() {
        currentChatManager.setChat(null)
    }
    
    companion object {
        private const val TAG = "ConversationViewModel"
    }
    
    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<ConversationEvent>()
    val events: SharedFlow<ConversationEvent> = _events.asSharedFlow()
    
    // Map of UserID -> Contact Name
    private var userIdToNameMap: Map<String, String> = emptyMap()
    
    // Current chat ID being viewed
    private var currentChatId: String? = null
    private var currentUserName: String = ""
    
    // Jobs for cancellation
    private var messageObserverJob: Job? = null
    private var chatObserverJob: Job? = null
    
    init {
        // Observe current user ID from session
        viewModelScope.launch {
            sessionManager.userId.collect { userId ->
                _state.update { 
                    it.copy(currentUserId = userId ?: "") 
                }
            }
        }
        
        // TODO: Observe typing status when implemented in WebSocketManager
        // viewModelScope.launch {
        //     webSocketManager.typingStatus.collect { status ->
        //         if (status.chatId == currentChatId && status.userId != _state.value.currentUserId) {
        //             _state.update { it.copy(isOtherUserTyping = status.isTyping) }
        //         }
        //     }
        // }
        
        // Observe sync status
        viewModelScope.launch {
            messageSyncManager.pendingCount.collect { count ->
                _state.update { it.copy(pendingMessageCount = count) }
            }
        }
    }
    
    /**
     * Load a chat and start observing its messages.
     * Messages come from Room database via observable Flow.
     * 
     * CRITICAL FIX: We cancel previous observers and create new ones.
     * This prevents multiple collectors from competing and causing state conflicts.
     */
    /**
     * Load a chat and start observing its messages.
     * Messages come from Room database via observable Flow.
     * 
     * CRITICAL FIX: We cancel previous observers and create new ones.
     * This prevents multiple collectors from competing and causing state conflicts.
     */
    fun loadChat(chatId: String) {
        // If same chat, don't reload
        if (currentChatId == chatId && _state.value.messages.isNotEmpty()) {
            Log.d(TAG, "Chat $chatId already loaded with messages, skipping reload")
            // Ensure WebSocket subscription remains active
            webSocketManager.subscribeToChat(chatId)
            return
        }
        
        Log.d(TAG, "Loading chat: $chatId")
        currentChatId = chatId
        
        // LOGIC FIX: Only show loading if we really don't have messages for this chat.
        // If we are switching chats, clear old messages temporarily but keep it brief.
        // We set isInitialLoad = true, but carefully manage isLoading.
        if (_state.value.messages.isEmpty() || currentChatId != chatId) {
             _state.update { it.copy(
                isInitialLoad = true,
                isLoading = true,
                error = null
            )}
        }
        
        // Subscribe to chat via WebSocket for real-time message updates
        webSocketManager.subscribeToChat(chatId)
        Log.d(TAG, "Subscribed to WebSocket topic for chat: $chatId")
        
        // Clear unread count since user is now viewing this chat
        viewModelScope.launch {
            chatRepository.clearUnreadCount(chatId)
        }
        
        // Cancel previous observers to prevent stale data
        messageObserverJob?.cancel()
        chatObserverJob?.cancel()
        
        // Start observing messages from database
        messageObserverJob = viewModelScope.launch {
            Log.d(TAG, "Starting message observation for chat: $chatId")
            
            // Track previous message count to detect when list unexpectedly becomes empty
            var previousMessageCount = 0
            
            messageRepository.observeMessages(chatId).collect { messages ->
                Log.d(TAG, "Received ${messages.size} messages from database for chatId=[$chatId]")
                
                // DEBUG: Log each message's chatId to find mismatches
                if (messages.isNotEmpty()) {
                    messages.take(3).forEach { msg ->
                        Log.d(TAG, "   - Message id=${msg.id}, chatId=[${msg.chatId}]")
                    }
                }
                
                // FIX: Skip empty emissions if we previously had messages
                // This prevents the UI from briefly showing empty state during sync race conditions
                if (messages.isEmpty() && previousMessageCount > 0) {
                    Log.w(TAG, "⚠️ Skipping empty emission - likely race condition (had $previousMessageCount messages)")
                    return@collect
                }
                
                previousMessageCount = messages.size
                
                // Only update if we're still viewing this chat
                if (currentChatId == chatId) {
                    // Update user name mapping before displaying
                    val displayMessages = messages.map { msg ->
                        userIdToNameMap[msg.senderId]?.let { name ->
                            msg.copy(senderName = name)
                        } ?: msg
                    }

                    _state.update { state ->
                        state.copy(
                            messages = displayMessages.sortedByDescending { it.createdAt },
                            isLoading = false,
                            isInitialLoad = false
                        )
                    }
                    
                    // Mark messages as read AND clear unread count for the chat
                    if (messages.isNotEmpty()) {
                        markMessagesAsRead(messages)
                        // CRITICAL FIX: Ensure unread count is cleared when new messages arrive while viewing
                        viewModelScope.launch {
                            chatRepository.clearUnreadCount(chatId)
                        }
                    }
                }
            }
        }
        
        // Start observing chat details from database
        chatObserverJob = viewModelScope.launch {
            loadChatDetails(chatId)
        }
        
        // Trigger message sync from server (results go to DB, then to UI via Flow)
        messageRepository.requestMessageSync(chatId)
    }
    
    /**
     * Called when user leaves the chat screen
     */
    fun onChatClosed() {
        Log.d(TAG, "Chat closed: $currentChatId")
        // Don't unsubscribe! We want to keep receiving messages for this chat 
        // even when we're not on the screen, to update the badge/snippet.
        // currentChatId?.let { webSocketManager.unsubscribeFromChat(it) }
        
        // Cancel observers
        messageObserverJob?.cancel()
        chatObserverJob?.cancel()
        messageObserverJob = null
        chatObserverJob = null
    }
    
    private fun normalizePhoneNumber(number: String): String {
        var normalized = number.replace(Regex("[^0-9+]"), "")
        if (normalized.startsWith("+98")) {
            normalized = "0" + normalized.substring(3)
        }
        normalized = normalized.removePrefix("+")
        return normalized
    }

    private suspend fun loadChatDetails(chatId: String) {
        // Load contacts logic integrated here
        val deviceContacts = try {
            contactsRepository.getDeviceContacts()
        } catch (e: Exception) {
            emptyList()
        }
        val phoneToName = deviceContacts.associate { normalizePhoneNumber(it.phoneNumber) to it.name }
        
        // Use first() to get initial value, then collect for updates
        chatRepository.observeChatById(chatId).collect { chat ->
            if (chat != null && currentChatId == chatId) {
                var displayChat = chat
                
                // 1. Build UserID -> Name Map for this chat
                val newMap = userIdToNameMap.toMutableMap()
                chat.participants.forEach { user ->
                    user.phoneNumber?.let { phone ->
                        val normalized = normalizePhoneNumber(phone)
                        val contactName = phoneToName[normalized]
                        if (contactName != null) {
                            newMap[user.id] = contactName
                        }
                    }
                }
                userIdToNameMap = newMap
                
                // 2. Apply phone contact name to Chat Title
                val otherParticipant = chat.participants.find { 
                    it.id != _state.value.currentUserId 
                }
                if (otherParticipant?.phoneNumber != null) {
                    val normalized = normalizePhoneNumber(otherParticipant.phoneNumber)
                    val phoneContactName = phoneToName[normalized]
                    if (phoneContactName != null) {
                        displayChat = chat.copy(title = phoneContactName)
                    }
                }
                
                _state.update { it.copy(chat = displayChat) }
                
                currentUserName = chat.participants.find { 
                    it.id == _state.value.currentUserId 
                }?.displayName ?: "User"
            }
        }
        
        // Also request sync of chat details from server
        chatRepository.requestChatDetailsSync(chatId)
    }
    
    /**
     * Mark unread messages as read when viewing the chat.
     * FIX: Process sequentially to avoid race conditions that skip messages.
     */
    private fun markMessagesAsRead(messages: List<Message>) {
        val currentUserId = _state.value.currentUserId
        val chatId = currentChatId ?: return
        
        val unreadMessages = messages.filter { 
            it.senderId != currentUserId && it.status != MessageStatus.READ 
        }.take(10)
        
        Log.d(TAG, "📖 markMessagesAsRead: Found ${unreadMessages.size} unread messages from ${messages.size} total")
        
        if (unreadMessages.isEmpty()) return
        
        // Single coroutine to process all messages sequentially
        viewModelScope.launch {
            unreadMessages.forEach { message ->
                try {
                    Log.d(TAG, "📖 Sending read receipt for message ${message.id}")
                    // Send read receipt via WebSocket
                    webSocketManager.sendReadReceipt(chatId, message.id)
                    messageRepository.markAsRead(message.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to mark message ${message.id} as read", e)
                }
            }
        }
    }
    /**
     * Send a message.
     * 
     * ARCHITECTURE:
     * 1. Write immediately to database with PENDING status
     * 2. UI updates automatically via observable Flow
     * 3. MessageSyncManager handles sending to server in background
     * 4. Server saves to DB and broadcasts via WebSocket to other participants
     * 5. Server response updates local DB status, UI updates automatically
     * 
     * NOTE: We no longer send via WebSocket directly from the client.
     * The server handles broadcasting after persisting the message.
     * This prevents duplicate messages and ensures message ordering consistency.
     */
    fun setReplyMessage(message: Message?) {
        _state.update { it.copy(replyToMessage = message) }
    }

    fun sendMessage(content: String, type: String = "TEXT", mediaUrl: String? = null, amplitudes: List<Int>? = null) {
        val chatId = currentChatId ?: return
        val currentUserId = _state.value.currentUserId
        val replyToId = _state.value.replyToMessage?.id
        
        _state.update { it.copy(isSending = true) }
        
        viewModelScope.launch {
            val messageId = messageRepository.queueMessage(
                chatId = chatId,
                content = content,
                type = type,
                senderId = currentUserId,
                senderName = currentUserName,
                mediaUrl = mediaUrl,
                replyToMessageId = replyToId,
                replyToMessage = _state.value.replyToMessage,
                amplitudes = amplitudes
            )
            
            Log.d(TAG, "Message queued: $messageId")
            
            // 🔊 Play send sound
            soundPlayer.playSendSound()
            
            // Trigger sync to send to server
            // Server will broadcast via WebSocket to other participants after saving
            messageSyncManager.syncPendingMessages()
            
            _state.update { it.copy(isSending = false, replyToMessage = null) }
            _events.emit(ConversationEvent.MessageSent)
        }
    }
    
    /**
     * Send typing indicator to other user
     */
    fun sendTypingIndicator(isTyping: Boolean) {
        val chatId = currentChatId ?: return
        webSocketManager.sendTypingStatus(chatId, isTyping)
    }
    
    /**
     * Retry a failed message
     */
    fun retryMessage(messageId: String) {
        messageSyncManager.retryMessage(messageId)
    }
    
    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            messageRepository.editMessage(messageId, newContent)
        }
    }
    
    fun reactToMessage(messageId: String, reaction: String) {
        viewModelScope.launch {
            Log.d(TAG, "Reacting to message $messageId with $reaction")
            val chat = _state.value.chat ?: return@launch
            val isGroup = chat.type == ChatType.GROUP
            val isChannel = chat.type == ChatType.CHANNEL
            
            // For now, handling Private and Group. Channel might need separate API/Logic.
            // If channel messages are stored in 'messages' table and accessed via same IDs, maybe it works?
            // But usually Channels have 'posts'. 
            // Assuming for now generic reaction works for what we have.
            
            if (isChannel) {
                // Todo: Channel reaction logic
                Log.w(TAG, "Channel reactions not yet fully implemented in Repo")
                return@launch
            }

            messageRepository.reactToMessage(
                messageId = messageId, 
                reaction = reaction, 
                isGroup = isGroup, 
                groupId = if (isGroup) chat.id else null
            )
        }
    }

    fun reactToMultipleMessages(messageIds: Set<String>, reaction: String) {
        viewModelScope.launch {
            messageIds.forEach { messageId ->
                val chat = _state.value.chat ?: return@launch
                val isGroup = chat.type == ChatType.GROUP
                if (chat.type == ChatType.CHANNEL) return@launch
                messageRepository.reactToMessage(
                    messageId = messageId,
                    reaction = reaction,
                    isGroup = isGroup,
                    groupId = if (isGroup) chat.id else null
                )
            }
            clearSelection()
        }
    }

    fun pinMessage(messageId: String, isPinned: Boolean) {
        viewModelScope.launch {
            try {
                val response = chatRepository.pinMessage(messageId, isPinned)
                response.fold(
                    onSuccess = {
                        Log.d(TAG, "Message $messageId pin state set to $isPinned")
                        _state.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages.map { msg ->
                                    if (msg.id == messageId) msg.copy(isPinned = isPinned, pinnedAt = if (isPinned) java.time.Instant.now() else null) else msg
                                }
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to pin message", e)
                        _events.emit(ConversationEvent.Error("خطا در سنجاق کردن پیام"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "pinMessage failed", e)
                _events.emit(ConversationEvent.Error("خطا در سنجاق کردن پیام"))
            }
        }
    }

    fun forwardMessage(messageId: String, targetChatId: String?, targetGroupId: String?, targetChannelId: String?) {
        viewModelScope.launch {
            try {
                val response = chatRepository.forwardMessage(messageId, targetChatId, targetGroupId, targetChannelId)
                response.fold(
                    onSuccess = {
                        Log.d(TAG, "Message $messageId forwarded successfully")
                        _events.emit(ConversationEvent.MessageSent)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to forward message", e)
                        _events.emit(ConversationEvent.Error("خطا در ارسال مجدد پیام"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "forwardMessage failed", e)
                _events.emit(ConversationEvent.Error("خطا در ارسال مجدد پیام"))
            }
        }
    }

    fun scheduleMessage(content: String, scheduledAt: String, type: String = "TEXT", mediaUrl: String? = null) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            try {
                val response = chatRepository.scheduleMessage(chatId, content, type, mediaUrl, scheduledAt)
                response.fold(
                    onSuccess = {
                        Log.d(TAG, "Message scheduled for $scheduledAt")
                        _events.emit(ConversationEvent.MessageSent)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to schedule message", e)
                        _events.emit(ConversationEvent.Error("خطا در زمانبندی پیام"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "scheduleMessage failed", e)
                _events.emit(ConversationEvent.Error("خطا در زمانبندی پیام"))
            }
        }
    }

    fun updateScheduledTime(messageId: String, newScheduledAt: String) {
        viewModelScope.launch {
            try {
                // Find the message in current state
                val msg = _state.value.messages.find { it.id == messageId }
                if (msg == null) {
                    Log.e(TAG, "updateScheduledTime: message $messageId not found")
                    return@launch
                }
                val chatId = currentChatId ?: return@launch
                val response = chatRepository.scheduleMessage(
                    chatId = chatId,
                    content = msg.content,
                    type = msg.type.name,
                    mediaUrl = msg.mediaUrl,
                    scheduledAt = newScheduledAt
                )
                response.fold(
                    onSuccess = {
                        Log.d(TAG, "Message $messageId rescheduled to $newScheduledAt")
                        // Delete old scheduled message
                        messageRepository.deleteMessage(messageId)
                        // Refresh messages from server
                        messageRepository.requestMessageSync(chatId)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Failed to reschedule message", e)
                        _events.emit(ConversationEvent.Error("خطا در ویرایش زمان"))
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "updateScheduledTime failed", e)
                _events.emit(ConversationEvent.Error("خطا در ویرایش زمان"))
            }
        }
    }
    
    fun toggleMessageSelection(messageId: String) {
        _state.update { 
            val currentSelected = it.selectedMessageIds.toMutableSet()
            if (currentSelected.contains(messageId)) {
                currentSelected.remove(messageId)
            } else {
                currentSelected.add(messageId)
            }
            it.copy(selectedMessageIds = currentSelected)
        }
    }
    
    fun clearSelection() {
        _state.update { it.copy(selectedMessageIds = emptySet()) }
    }
    
    fun deleteSelectedMessages(deleteForEveryone: Boolean = false) {
        val selected = _state.value.selectedMessageIds
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { messageId ->
                messageRepository.deleteMessage(messageId)
            }
            clearSelection()
        }
    }
    
    fun createPoll(question: String, options: List<String>, isMultipleChoice: Boolean, isAnonymous: Boolean) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = pollRepository.createPoll(question, options, isMultipleChoice, isAnonymous)
            result.fold(
                onSuccess = { pollDto ->
                    // Now send message with this poll linked
                    // We need to update existing sendMessage or use a new method that accepts pollId
                    // For now, I'll update messageRepository.queueMessage (or call it directly if I update it)
                    // But wait, QueueMessage is local first.
                    // Local MessageEntity needs 'poll' field (String JSON).
                    // I need to save the Poll locally? Or just rely on Network for now?
                    
                    // Let's call messageRepository.queueMessage with pollId if possible.
                    // Or call messageRepository.sendPollMessage(chatId, pollDto)
                    
                    // Since I updated Backend to accept pollId in SendMessageRequest,
                    // I should prioritize sending it via API.
                    // But 'queueMessage' implies offline support.
                    
                    // For now, let's just trigger the sync manager or repository to send it.
                    // I will add 'sendPollMessage' to Repository.
                    messageRepository.sendPollMessage(chatId, pollDto)
                    _state.update { it.copy(isSending = false) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isSending = false, error = e.message) }
                }
            )
        }
    }
    
    fun votePoll(pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
             pollRepository.vote(pollId, optionIds).fold(
                 onSuccess = { updatedPoll ->
                     // Update local message state if possible, or wait for socket update
                 },
                 onFailure = { error ->
                     _state.update { it.copy(error = "Vote failed: ${error.message}") }
                 }
             )
        }
    }

    fun deleteMessage(messageId: String, deleteForEveryone: Boolean = true) {
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
        }
    }

    /**
     * Delete the current chat locally (only from user's device, not server).
     */
    fun deleteChat() {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            chatRepository.deleteLocalChat(chatId)
        }
    }
    
    /**
     * Toggle mute state for the current chat.
     */
    fun toggleMute() {
        val chatId = currentChatId ?: return
        val currentMuted = _state.value.chat?.isMuted ?: false
        viewModelScope.launch {
            chatRepository.muteChat(chatId, !currentMuted)
        }
    }
    
    /**
     * Load more messages (pagination)
     */
    fun loadMoreMessages() {
        val chatId = currentChatId ?: return
        val currentPage = _state.value.messages.size / 50
        messageRepository.requestMessageSync(chatId, currentPage)
    }
    
    /**
     * Refresh messages from server
     */
    fun refreshMessages() {
        val chatId = currentChatId ?: return
        messageRepository.requestMessageSync(chatId)
    }

    /**
     * Upload a file and send it as a message.
     * Shows progress bar for files > 1MB.
     */
    fun uploadAndSendFile(chatId: String, uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f) }
            try {
                // Get file from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = getFileNameFromUri(context, uri) ?: "file_${System.currentTimeMillis()}"
                val tempFile = java.io.File(context.cacheDir, fileName)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val fileSize = tempFile.length()
                val oneMB = 1024 * 1024L
                
                // Use ProgressRequestBody for files > 1MB
                val requestBody = if (fileSize > oneMB) {
                    com.Kelasor.app.data.remote.util.ProgressRequestBody(
                        file = tempFile,
                        contentType = "*/*".toMediaTypeOrNull(),
                        listener = object : com.Kelasor.app.data.remote.util.ProgressRequestBody.ProgressListener {
                            override fun onProgressUpdate(bytesWritten: Long, totalBytes: Long) {
                                val progress = bytesWritten.toFloat() / totalBytes.toFloat()
                                _state.update { it.copy(uploadProgress = progress) }
                            }
                        }
                    )
                } else {
                    tempFile.asRequestBody("*/*".toMediaTypeOrNull())
                }
                
                val part = okhttp3.MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        // Send message with file URL
                        Log.d(TAG, "File uploaded successfully: $fileUrl. Calling sendMessage...")
                        sendMessage(fileName, "FILE", fileUrl)
                        Log.d(TAG, "File uploaded and message sent: $fileUrl")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "File upload failed: ${e.message}")
                        _events.emit(ConversationEvent.Error("خطا در آپلود فایل: ${e.message}"))
                    }
                )
                
                // Clean up temp file
                tempFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading file: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در آپلود فایل: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }
    
    /**
     * Send a voice message.
     * Uploads the recorded voice file and sends as a VOICE message.
     */
    fun sendVoiceMessage(voiceFile: java.io.File, durationMs: Long, amplitudes: List<Int> = emptyList()) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f) }
            try {
                val requestBody = voiceFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("file", voiceFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        // Send message with voice URL
                        // Content contains duration in seconds for display
                        val durationSeconds = durationMs / 1000
                        sendMessage("🎤 صدا (${durationSeconds}s)", "VOICE", fileUrl, amplitudes)
                        Log.d(TAG, "Voice message uploaded and sent: $fileUrl")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Voice upload failed: ${e.message}")
                        _events.emit(ConversationEvent.Error("خطا در آپلود صدا: ${e.message}"))
                    }
                )
                
                // Clean up temp file
                voiceFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading voice: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در آپلود صدا: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    /**
     * Send a circular video note message.
     * Uploads the recorded video file and sends as a VIDEO_NOTE message.
     */
    fun sendVideoNote(videoFile: java.io.File, durationMs: Long) {
        val chatId = currentChatId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f) }
            try {
                val requestBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("file", videoFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                response.fold(
                    onSuccess = { fileUrl ->
                        val durationSeconds = durationMs / 1000
                        sendMessage("🎥 ویدیو نوت (${durationSeconds}s)", "VIDEO_NOTE", fileUrl)
                        Log.d(TAG, "Video note uploaded and sent: $fileUrl")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Video note upload failed: ${e.message}")
                        _events.emit(ConversationEvent.Error("خطا در آپلود ویدیو نوت: ${e.message}"))
                    }
                )
                videoFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading video note: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در آپلود ویدیو نوت: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }
    
    /**
     * Send a location message.
     * Location is stored as "lat,lng" in mediaUrl for easy parsing.
     */
    fun sendLocationMessage(latitude: Double, longitude: Double) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            try {
                // Store location coordinates in content for display
                val content = "📍 موقعیت مکانی"
                val locationUrl = "$latitude,$longitude"
                
                sendMessage(content, "LOCATION", locationUrl)
                Log.d(TAG, "Location message sent: $latitude, $longitude")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending location: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در ارسال موقعیت: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false) }
            }
        }
    }
    
    /**
     * Extract file name from content URI.
     */
    private fun getFileNameFromUri(context: android.content.Context, uri: android.net.Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }
    
    /**
     * Upload and send media (image/video) from gallery.
     * Sends as IMAGE or VIDEO message type.
     */
    fun uploadAndSendMedia(chatId: String, uri: android.net.Uri, context: android.content.Context, isVideo: Boolean, caption: String? = null) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f) }
            try {
                // Get file from URI
                val fileName = getFileNameFromUri(context, uri) ?: "media_${System.currentTimeMillis()}"
                val mimeType = context.contentResolver.getType(uri) ?: if (isVideo) "video/mp4" else "image/jpeg"
                val isVideoFile = mimeType.startsWith("video/")
                
                val tempFile = java.io.File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        val messageType = if (isVideoFile) "VIDEO" else "IMAGE"
                        val content = if (!caption.isNullOrBlank()) {
                            caption
                        } else {
                            val emojiPrefix = if (isVideoFile) "🎬" else "🖼️"
                            "$emojiPrefix $fileName"
                        }
                        sendMessage(content, messageType, fileUrl)
                        Log.d(TAG, "Media uploaded and sent: $fileUrl (type: $messageType)")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Media upload failed: ${e.message}")
                        _events.emit(ConversationEvent.Error("خطا در آپلود: ${e.message}"))
                    }
                )
                
                // Clean up temp file
                tempFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading media: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در آپلود: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }
    
    /**
     * Upload and send audio file.
     * Sends as AUDIO message type (different from VOICE).
     */
    fun uploadAndSendAudio(chatId: String, uri: android.net.Uri, context: android.content.Context) {
        val chatId = currentChatId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f) }
            try {
                // Get file from URI
                val fileName = getFileNameFromUri(context, uri) ?: "audio_${System.currentTimeMillis()}.mp3"
                val mimeType = context.contentResolver.getType(uri) ?: "audio/mpeg"
                
                val tempFile = java.io.File(context.cacheDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        // Send as AUDIO type with file name in content
                        sendMessage("🎵 $fileName", "AUDIO", fileUrl)
                        Log.d(TAG, "Audio file uploaded and sent: $fileUrl")
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Audio upload failed: ${e.message}")
                        _events.emit(ConversationEvent.Error("خطا در آپلود صوت: ${e.message}"))
                    }
                )
                
                // Clean up temp file
                tempFile.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading audio: ${e.message}")
                _events.emit(ConversationEvent.Error("خطا در آپلود صوت: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        onChatClosed()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🆕 New Chat ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class NewChatState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val contactUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val createdChatId: String? = null,
    val hasLoadedContacts: Boolean = false,
    // Privacy-sanitized avatar URLs - maps userId to sanitized avatarUrl (null if hidden)
    val sanitizedAvatarUrls: Map<String, String?> = emptyMap(),
    val hideOnlineStatus: Map<String, Boolean> = emptyMap()
)

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val contactsRepository: ContactsRepository,
    private val privacyManager: com.Kelasor.app.domain.privacy.PrivacyManager
) : ViewModel() {
    private val _state = MutableStateFlow(NewChatState())
    val state: StateFlow<NewChatState> = _state.asStateFlow()
    
    /**
     * Load device contacts and match them with registered users.
     */
    fun loadContacts() {
        if (_state.value.hasLoadedContacts) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val deviceContacts = contactsRepository.getDeviceContacts()
                if (deviceContacts.isNotEmpty()) {
                    val phoneToName = deviceContacts.associate { it.phoneNumber to it.name }
                    
                    userRepository.matchContacts(phoneToName).collect { result ->
                        when (result) {
                            is UserResult.Loading -> {
                                _state.update { it.copy(isLoading = true) }
                            }
                            is UserResult.Success -> {
                                // Apply privacy to contact users - contacts are trusted so show all
                                _state.update { 
                                    it.copy(
                                        isLoading = false, 
                                        contactUsers = result.data,
                                        hasLoadedContacts = true
                                    )
                                }
                            }
                            is UserResult.Error -> {
                                _state.update { 
                                    it.copy(
                                        isLoading = false, 
                                        error = result.message,
                                        hasLoadedContacts = true
                                    )
                                }
                            }
                        }
                    }
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            hasLoadedContacts = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = "خطا در خواندن مخاطبین: ${e.message}",
                        hasLoadedContacts = true
                    )
                }
            }
        }
    }
    
    fun searchUsers(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 2) {
            _state.update { it.copy(users = emptyList(), sanitizedAvatarUrls = emptyMap(), hideOnlineStatus = emptyMap()) }
            return
        }
        
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is UserResult.Success -> {
                        // Apply privacy filtering to search results
                        // For non-contacts, respect user's own privacy settings
                        val users = result.data
                        val sanitizedAvatars = mutableMapOf<String, String?>()
                        val hideOnline = mutableMapOf<String, Boolean>()
                        
                        // Get current privacy settings (proxy for target user's settings)
                        val privacySettings = privacyManager.getMyPrivacySettings().first()
                        
                        users.forEach { user ->
                            // Check if this user is a contact (for privacy purposes)
                            val isContact = try { contactsRepository.isContact(user.id) } catch (e: Exception) { false }
                            val userPrivacy = privacyManager.canISeeUserData(privacySettings, isContact)
                            
                            sanitizedAvatars[user.id] = if (userPrivacy.canSeeProfilePhoto) user.avatarUrl else null
                            hideOnline[user.id] = !userPrivacy.canSeeOnlineStatus
                        }
                        
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                users = users,
                                sanitizedAvatarUrls = sanitizedAvatars,
                                hideOnlineStatus = hideOnline
                            )
                        }
                    }
                    is UserResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
    
    fun createChat(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val result = chatRepository.createChat(userId)
            result.fold(
                onSuccess = { chat ->
                    _state.update { it.copy(isLoading = false, createdChatId = chat.id) }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
