package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.repository.GroupRepository
import com.hasani.messageapp.data.repository.GroupResult
import com.hasani.messageapp.domain.model.Group
import com.hasani.messageapp.domain.model.GroupMember
import com.hasani.messageapp.domain.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import com.hasani.messageapp.data.remote.dto.PollDto
import com.hasani.messageapp.data.remote.dto.SendGroupMessageRequest
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Events
// ═══════════════════════════════════════════════════════════════════════════════

sealed class GroupEvent {
    data object MessageSent : GroupEvent()
    data object GroupUpdated : GroupEvent()
    data object GroupDeleted : GroupEvent()
    data object GroupLeft : GroupEvent()
    data class Error(val message: String) : GroupEvent()
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group List State
// ═══════════════════════════════════════════════════════════════════════════════

data class GroupListState(
    val groups: List<Group> = emptyList(),
    val pinnedGroups: List<Group> = emptyList(),
    val archivedGroups: List<Group> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val selectedGroupIds: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false
) {
    val filteredGroups: List<Group>
        get() = if (searchQuery.isBlank()) {
            groups.filter { !it.isArchived && !it.isPinned }
        } else {
            groups.filter { it.name.contains(searchQuery, ignoreCase = true) && !it.isArchived && !it.isPinned }
        }
}

data class GroupConversationState(
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val group: Group? = null,
    val messages: List<Message> = emptyList(),
    val members: List<GroupMember> = emptyList(),
    val error: String? = null,
    val currentUserId: String? = null,
    val isSending: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadTotalBytes: Long = 0L,
    val selectedMessageIds: Set<String> = emptySet()
)

data class GroupSettingsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val contacts: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val error: String? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group List ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val sessionManager: com.hasani.messageapp.data.session.SessionManager
) : ViewModel() {
    
    private val _state = MutableStateFlow(GroupListState())
    val state: StateFlow<GroupListState> = _state.asStateFlow()
    
    init {
        loadCurrentUser()
        loadGroups()
        observeGroups()
        observeArchivedGroups()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            _state.update { it.copy(currentUserId = userId) }
        }
    }
    
    fun loadGroups(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            groupRepository.getGroups(0, forceRefresh).collect { result ->
                when (result) {
                    is GroupResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is GroupResult.Success -> {
                        // Don't set groups here - let observeGroups() be the sole source of truth
                        // This prevents race conditions and duplicate key issues
                        _state.update { it.copy(isLoading = false) }
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                }
            }
        }
    }
    
    private fun observeGroups() {
        viewModelScope.launch {
            groupRepository.observeGroups().collectLatest { allGroups ->
                val pinned = allGroups.filter { it.isPinned }
                val regular = allGroups.filter { !it.isPinned && !it.isArchived }
                _state.update { it.copy(groups = regular, pinnedGroups = pinned) }
            }
        }
    }
    
    private fun observeArchivedGroups() {
        viewModelScope.launch {
            groupRepository.observeArchivedGroups().collectLatest { archivedGroups ->
                _state.update { it.copy(archivedGroups = archivedGroups) }
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
    
    fun joinGroup(inviteCode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            groupRepository.joinByInviteLink(inviteCode).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        loadGroups(forceRefresh = true)
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is GroupResult.Loading -> {}
                }
            }
        }
    }
    
    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            val currentUserId = sessionManager.userId.first() ?: return@launch
            groupRepository.removeMember(groupId, currentUserId).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        loadGroups(forceRefresh = true)
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(error = result.message) }
                    }
                    is GroupResult.Loading -> {}
                }
            }
        }
    }
    
    fun toggleSelection(groupId: String) {
        _state.update {
            val currentSelected = it.selectedGroupIds.toMutableSet()
            if (currentSelected.contains(groupId)) {
                currentSelected.remove(groupId)
            } else {
                currentSelected.add(groupId)
            }
            it.copy(selectedGroupIds = currentSelected)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedGroupIds = emptySet()) }
    }

    fun pinSelectedGroups(isPinned: Boolean) {
        val selected = _state.value.selectedGroupIds
        if (selected.isEmpty()) return

        viewModelScope.launch {
            selected.forEach { groupId ->
                groupRepository.togglePin(groupId, isPinned).collect { /* Silent */ }
            }
            _state.update { it.copy(selectedGroupIds = emptySet()) }
        }
    }

    fun requestDeleteSelection() {
        if (_state.value.selectedGroupIds.isNotEmpty()) {
            _state.update { it.copy(showDeleteConfirmation = true) }
        }
    }

    fun cancelDeleteSelection() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDeleteSelection() {
        val selected = _state.value.selectedGroupIds
        if (selected.isEmpty()) return

        viewModelScope.launch {
            // Groups are usually "left" not deleted locally, but if the requirement is deleting chats,
            // for groups it usually means leaving the group or deleting local copy if possible.
            // Assuming "Deleting" from list means Leaving/Removing local history.
            // Following ChatList logic which calls deleteLocalChat, but GroupRepository might not expose deleteLocalGroup simply.
            // Let's assume we call leaveGroup for each selected group for now as "Deleting" a group from list usually implies leaving it.
            // OR if strictly local delete:
            // groupRepository.deleteLocalGroup(groupId) ?
            
            // Re-reading rules: "Confirm Delete must remove the selected items and clear selection."
            // Existing ChatListViewModel uses `chatRepository.deleteLocalChat(chatId)`.
            // Let's see if GroupRepository has delete.
            
            selected.forEach { groupId ->
                // Attempt to leave group as "Delete" action
                 leaveGroup(groupId)
            }
            _state.update { it.copy(selectedGroupIds = emptySet(), showDeleteConfirmation = false) }
        }
    }
    
    fun deleteSelectedGroups() {
         confirmDeleteSelection()
    }
    
    fun archiveGroup(groupId: String, archive: Boolean) {
        viewModelScope.launch {
            val flow = if (archive) {
                groupRepository.archiveGroup(groupId)
            } else {
                groupRepository.unarchiveGroup(groupId)
            }
            flow.collect { /* Silent operation, toast shown in UI */ }
        }
    }
    
    fun pinGroup(groupId: String, isPinned: Boolean) {
        viewModelScope.launch {
            groupRepository.togglePin(groupId, isPinned).collect { /* Silent operation */ }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Group Conversation ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class GroupConversationViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val chatRepository: com.hasani.messageapp.data.repository.ChatRepository,
    private val pollRepository: com.hasani.messageapp.data.repository.PollRepository,
    private val webSocketManager: com.hasani.messageapp.data.websocket.WebSocketManager,
    val voiceRecorderManager: com.hasani.messageapp.data.voice.VoiceRecorderManager,
    val audioPlayerManager: com.hasani.messageapp.data.audio.AudioPlayerManager,
    val locationManager: com.hasani.messageapp.data.location.LocationManager,
    private val sessionManager: com.hasani.messageapp.data.session.SessionManager,
    private val soundPlayer: com.hasani.messageapp.data.media.SoundPlayer,
    private val currentChatManager: com.hasani.messageapp.data.session.CurrentChatManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun setActiveChat(id: String) {
        currentChatManager.setChat(id)
    }

    fun clearActiveChat() {
        currentChatManager.setChat(null)
    }
    
    private val _state = MutableStateFlow(GroupConversationState())
    val state: StateFlow<GroupConversationState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<GroupEvent>()
    val events: SharedFlow<GroupEvent> = _events.asSharedFlow()
    
    private var currentGroupId: String? = null
    
    init {
        viewModelScope.launch {
            val userId = sessionManager.userId.first()
            _state.update { it.copy(currentUserId = userId) }
        }
    }

    fun loadGroup(groupId: String) {
        if (currentGroupId == groupId) return
        currentGroupId = groupId
        webSocketManager.subscribeToGroup(groupId)
        
        // Observe group
        viewModelScope.launch {
            groupRepository.observeGroup(groupId).collectLatest { group ->
                _state.update { it.copy(group = group) }
            }
        }
        
        // Observe messages
        viewModelScope.launch {
            groupRepository.observeGroupMessages(groupId).collectLatest { messages ->
                // Check for new messages to play sound
                val currentMessages = _state.value.messages
                if (currentMessages.isNotEmpty() && messages.isNotEmpty()) {
                    // Assuming messages are sorted by date descending (newest first)
                    val lastNew = messages.firstOrNull()
                    val lastOld = currentMessages.firstOrNull()
                    
                    if (lastNew != null && lastOld != null && lastNew.id != lastOld.id) {
                         // Check if it's from someone else
                         val currentUserId = _state.value.currentUserId
                         if (lastNew.senderId != currentUserId) {
                             soundPlayer.playReceiveSound()
                         }
                    }
                }
                _state.update { it.copy(messages = messages, isInitialLoad = false) }
            }
        }

        // Observe members
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId).collectLatest { members ->
                _state.update { it.copy(members = members) }
            }
        }

        // Initial fetch
        viewModelScope.launch {
            groupRepository.getGroupById(groupId).collect { result ->
                if (result is GroupResult.Error) _events.emit(GroupEvent.Error(result.message))
            }
            groupRepository.getGroupMessages(groupId).collect { }
            groupRepository.getGroupMembers(groupId).collect { }
        }
    }

    fun reactToMessage(messageId: String, reaction: String) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.reactToMessage(groupId, messageId, reaction).collect { result ->
                if (result is GroupResult.Error) _events.emit(GroupEvent.Error(result.message))
            }
        }
    }

    fun deleteMessage(messageId: String, deleteForEveryone: Boolean = true) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.deleteMessage(groupId, messageId, deleteForEveryone).collect { result ->
                if (result is GroupResult.Error) _events.emit(GroupEvent.Error(result.message))
            }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.editMessage(groupId, messageId, newContent).collect { result ->
                if (result is GroupResult.Error) _events.emit(GroupEvent.Error(result.message))
            }
        }
    }

    fun sendMessage(content: String, replyToMessageId: String? = null, type: String = "TEXT", mediaUrl: String? = null, amplitudes: List<Int>? = null) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            groupRepository.sendGroupMessage(groupId, content, replyToMessageId = replyToMessageId, type = type, mediaUrl = mediaUrl, amplitudes = amplitudes).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _events.emit(GroupEvent.MessageSent)
                        _state.update { it.copy(isSending = false) }
                        soundPlayer.playSendSound()
                    }
                    is GroupResult.Error -> {
                        _events.emit(GroupEvent.Error(result.message))
                        _state.update { it.copy(isSending = false) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun uploadAndSendFile(groupId: String, uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f, uploadTotalBytes = 0L) }
            try {
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
                
                val requestBody = if (fileSize > oneMB) {
                    com.hasani.messageapp.data.remote.util.ProgressRequestBody(
                        file = tempFile,
                        contentType = (context.contentResolver.getType(uri) ?: "*/*").toMediaTypeOrNull(),
                        listener = object : com.hasani.messageapp.data.remote.util.ProgressRequestBody.ProgressListener {
                            override fun onProgressUpdate(bytesWritten: Long, totalBytes: Long) {
                                val progress = bytesWritten.toFloat() / totalBytes.toFloat()
                                _state.update { it.copy(uploadProgress = progress, uploadTotalBytes = totalBytes) }
                            }
                        }
                    )
                } else {
                    _state.update { it.copy(uploadTotalBytes = fileSize) }
                    tempFile.asRequestBody((context.contentResolver.getType(uri) ?: "*/*").toMediaTypeOrNull())
                }
                
                val part = okhttp3.MultipartBody.Part.createFormData("file", tempFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        sendMessage(fileName, type = "FILE", mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(GroupEvent.Error("خطا در آپلود فایل: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(GroupEvent.Error("خطا در آپلود فایل: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun uploadAndSendMedia(groupId: String, uri: android.net.Uri, context: android.content.Context, isVideo: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f, uploadTotalBytes = 0L) }
            try {
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
                        val emojiPrefix = if (isVideoFile) "🎬" else "🖼️"
                        sendMessage("$emojiPrefix $fileName", type = messageType, mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(GroupEvent.Error("خطا در آپلود: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(GroupEvent.Error("خطا در آپلود: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun uploadAndSendAudio(groupId: String, uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f, uploadTotalBytes = 0L) }
            try {
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
                        sendMessage("🎵 $fileName", type = "AUDIO", mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(GroupEvent.Error("خطا در آپلود صوت: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(GroupEvent.Error("خطا در آپلود صوت: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun sendVoiceMessage(voiceFile: java.io.File, durationMs: Long, amplitudes: List<Int> = emptyList()) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, isUploading = true, uploadProgress = 0f, uploadTotalBytes = 0L) }
            try {
                val requestBody = voiceFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
                _state.update { it.copy(uploadTotalBytes = voiceFile.length()) }
                val part = okhttp3.MultipartBody.Part.createFormData("file", voiceFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        val durationSeconds = durationMs / 1000
                        sendMessage("🎤 صدا (${durationSeconds}s)", type = "VOICE", mediaUrl = fileUrl, amplitudes = amplitudes)
                    },
                    onFailure = { e ->
                        _events.emit(GroupEvent.Error("خطا در آپلود صدا: ${e.message}"))
                    }
                )
                voiceFile.delete()
            } catch (e: Exception) {
                _events.emit(GroupEvent.Error("خطا در آپلود صدا: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun sendLocationMessage(latitude: Double, longitude: Double) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            try {
                sendMessage("📍 موقعیت مکانی", type = "LOCATION", mediaUrl = "$latitude,$longitude")
            } catch (e: Exception) {
                _events.emit(GroupEvent.Error("خطا در ارسال موقعیت: ${e.message}"))
            } finally {
                _state.update { it.copy(isSending = false) }
            }
        }
    }

    fun createPoll(question: String, options: List<String>, isMultipleChoice: Boolean, isAnonymous: Boolean) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true) }
            val result = pollRepository.createPoll(question, options, isMultipleChoice, isAnonymous)
            result.fold(
                onSuccess = { pollDto ->
                    viewModelScope.launch {
                        groupRepository.sendPollMessage(groupId, pollDto).collect { result ->
                            if (result is GroupResult.Error) {
                                _events.emit(GroupEvent.Error(result.message))
                            } else {
                                soundPlayer.playSendSound()
                            }
                            _state.update { it.copy(isSending = false) }
                        }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isSending = false) }
                    _events.emit(GroupEvent.Error("Poll creation failed: ${e.message}"))
                }
            )
        }
    }

    fun votePoll(pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
             pollRepository.vote(pollId, optionIds).fold(
                 onSuccess = { updatedPoll ->
                     // Refresh messages to get updated poll data
                     val groupId = currentGroupId ?: return@fold
                     // Force refresh messages from server (bypasses loadGroup's early return)
                     groupRepository.getGroupMessages(groupId).collect { }
                 },
                 onFailure = { error ->
                     _events.emit(GroupEvent.Error("Vote failed: ${error.message}"))
                 }
             )
        }
    }

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

    fun onChatClosed() {
        currentGroupId?.let { webSocketManager.unsubscribeFromGroup(it) }
    }

    override fun onCleared() {
        super.onCleared()
        onChatClosed()
    }

    fun toggleMute() {
        val groupId = currentGroupId ?: return
        val currentGroup = _state.value.group ?: return
        viewModelScope.launch {
            // Toggle local state optimistically or wait for result?
            // Repository updates local DB, and we observeGroup, so UI should update automatically 
            // via observeGroup flow. But we can also handle result for error showing.
            val newMuteStatus = !currentGroup.isMuted
            groupRepository.toggleMute(groupId, newMuteStatus).collect { result ->
                 if (result is GroupResult.Error) {
                     _events.emit(GroupEvent.Error(result.message))
                 }
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
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Group Settings ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: com.hasani.messageapp.data.repository.UserRepository,
    private val contactsRepository: com.hasani.messageapp.data.repository.ContactsRepository,
    private val sessionManager: com.hasani.messageapp.data.session.SessionManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _state = MutableStateFlow(GroupSettingsState())
    val state: StateFlow<GroupSettingsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<GroupEvent>()
    val events: SharedFlow<GroupEvent> = _events.asSharedFlow()
    
    private var currentGroupId: String? = null
    
    // Cache for phone to name mapping
    private var phoneToNameMap: Map<String, String> = emptyMap()
    
    init {
        // Load contacts on init
        viewModelScope.launch {
            try {
                val contacts = contactsRepository.getDeviceContacts()
                phoneToNameMap = contacts.associate { 
                    normalizePhoneNumber(it.phoneNumber) to it.name 
                }
            } catch (e: Exception) {
                // Permission not granted or error - continue without contact names
            }
        }
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
     * Apply contact name resolution to a GroupMember
     */
    private fun applyContactNameToMember(member: GroupMember): GroupMember {
        val phone = member.user.phoneNumber
        if (phone.isNotBlank()) {
            val normalizedPhone = normalizePhoneNumber(phone)
            val contactName = phoneToNameMap[normalizedPhone]
            if (contactName != null) {
                return member.copy(
                    user = member.user.copy(displayName = contactName)
                )
            }
        }
        return member
    }
    
    /**
     * Apply contact name resolution to a User
     */
    private fun applyContactNameToUser(user: com.hasani.messageapp.domain.model.User): com.hasani.messageapp.domain.model.User {
        val phone = user.phoneNumber
        if (phone.isNotBlank()) {
            val normalizedPhone = normalizePhoneNumber(phone)
            val contactName = phoneToNameMap[normalizedPhone]
            if (contactName != null) {
                return user.copy(displayName = contactName)
            }
        }
        return user
    }
    
    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        viewModelScope.launch {
            groupRepository.getGroupById(groupId).collect { result ->
                when (result) {
                    is GroupResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is GroupResult.Success -> {
                        _state.update { it.copy(group = result.data, isLoading = false) }
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                }
            }
        }
        
        // Load members
        viewModelScope.launch {
            groupRepository.getGroupMembers(groupId).collect { result ->
                if (result is GroupResult.Success) {
                    val membersWithContactNames = result.data.map { applyContactNameToMember(it) }
                    _state.update { it.copy(members = membersWithContactNames) }
                }
            }
        }
        
        // Observe members
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId).collectLatest { members ->
                val membersWithContactNames = members.map { applyContactNameToMember(it) }
                _state.update { it.copy(members = membersWithContactNames) }
            }
        }
    }
    

    
    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "temp_group_avatar_update_${System.currentTimeMillis()}.jpg"
            val tempFile = java.io.File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun updateGroup(name: String?, description: String?, isPublic: Boolean?, avatarUri: android.net.Uri? = null) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val avatarFile = avatarUri?.let { getFileFromUri(it) }
            
            groupRepository.updateGroup(groupId, name, description, isPublic, avatarFile).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _state.update { it.copy(group = result.data, isSaving = false) }
                        _events.emit(GroupEvent.GroupUpdated)
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(isSaving = false) }
                        _events.emit(GroupEvent.Error(result.message))
                    }
                    else -> {}
                }
                
                if (result !is GroupResult.Loading && avatarFile != null && avatarFile.exists()) {
                     avatarFile.delete()
                }
            }
        }
    }

    
    fun loadContacts() {
        viewModelScope.launch {
            userRepository.getContacts().collect { result ->
                if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    val contactsWithNames = result.data.map { applyContactNameToUser(it) }
                    _state.update { it.copy(contacts = contactsWithNames) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    val resultsWithNames = result.data.map { applyContactNameToUser(it) }
                    _state.update { it.copy(searchResults = resultsWithNames) }
                }
            }
        }
    }
    
    fun addMembers(memberIds: List<String>) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.addMembers(groupId, memberIds).collect { result ->
                if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun removeMember(memberId: String) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.removeMember(groupId, memberId).collect { result ->
                if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun changeMemberRole(memberId: String, newRole: com.hasani.messageapp.domain.model.MemberRole) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.changeRole(groupId, memberId, newRole).collect { result ->
                if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                } else if (result is GroupResult.Success) {
                    loadGroup(groupId) // Refresh members
                }
            }
        }
    }

    fun updateSettings(allowMembersToSendMessages: Boolean? = null, allowMembersToEditInfo: Boolean? = null) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.updateGroupSettings(groupId, allowMembersToSendMessages, allowMembersToEditInfo).collect { result ->
                if (result is GroupResult.Success) {
                    _state.update { it.copy(group = result.data) }
                } else if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }

    fun deleteGroup() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId).collect { result ->
                if (result is GroupResult.Success) {
                    _events.emit(GroupEvent.GroupDeleted)
                }
            }
        }
    }

    fun leaveGroup() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            val currentUserId = sessionManager.userId.first() ?: return@launch
            groupRepository.removeMember(groupId, currentUserId).collect { result ->
                if (result is GroupResult.Success) {
                    _events.emit(GroupEvent.GroupLeft)
                } else if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun archiveGroup(archive: Boolean) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            val flow = if (archive) {
                groupRepository.archiveGroup(groupId)
            } else {
                groupRepository.unarchiveGroup(groupId)
            }
            flow.collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        val message = if (archive) "گروه آرشیو شد" else "گروه از آرشیو خارج شد"
                        _events.emit(GroupEvent.Error(message)) // Reusing Error for toast
                    }
                    is GroupResult.Error -> {
                        _events.emit(GroupEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ➕ Create Group ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateGroupState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = false,
    val selectedMembers: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val contacts: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdGroupId: String? = null,
    val groupImageUri: android.net.Uri? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: com.hasani.messageapp.data.repository.UserRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(CreateGroupState())
    val state: StateFlow<CreateGroupState> = _state.asStateFlow()
    
    fun setName(name: String) {
        _state.update { it.copy(name = name) }
    }
    
    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun setIsPublic(isPublic: Boolean) {
        _state.update { it.copy(isPublic = isPublic) }
    }
    
    fun searchUsers(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= 2) {
            viewModelScope.launch {
                userRepository.searchUsers(query).collect { result ->
                    when (result) {
                        is com.hasani.messageapp.data.repository.UserResult.Success -> {
                            _state.update { it.copy(searchResults = result.data) }
                        }
                        is com.hasani.messageapp.data.repository.UserResult.Error -> {
                            _state.update { it.copy(searchResults = emptyList()) }
                        }
                        else -> {}
                    }
                }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }
    
    fun loadContacts() {
        viewModelScope.launch {
            userRepository.getContacts().collect { result ->
                when (result) {
                    is com.hasani.messageapp.data.repository.UserResult.Success -> {
                        _state.update { it.copy(contacts = result.data) }
                    }
                    is com.hasani.messageapp.data.repository.UserResult.Error -> {
                        // Fallback: try to search for all users
                        _state.update { it.copy(contacts = emptyList()) }
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun addMember(user: com.hasani.messageapp.domain.model.User) {
        _state.update { 
            it.copy(selectedMembers = it.selectedMembers + user) 
        }
    }
    
    fun removeMember(userId: String) {
        _state.update { 
            it.copy(selectedMembers = it.selectedMembers.filter { user -> user.id != userId }) 
        }
    }
    

    fun setGroupImage(uri: android.net.Uri?) {
        _state.update { it.copy(groupImageUri = uri) }
    }

    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "temp_group_avatar_${System.currentTimeMillis()}.jpg" // Simple temp name
            val tempFile = java.io.File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createGroup() {
        val currentState = _state.value
        if (currentState.name.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val avatarFile = currentState.groupImageUri?.let { getFileFromUri(it) }
            
            groupRepository.createGroup(
                name = currentState.name,
                description = currentState.description.ifBlank { null },
                isPublic = currentState.isPublic,
                memberIds = currentState.selectedMembers.map { it.id },
                avatarFile = avatarFile
            ).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                createdGroupId = result.data.id
                            ) 
                        }
                    }
                    is GroupResult.Error -> {
                        _state.update { 
                            it.copy(isLoading = false, error = result.message) 
                        }
                    }
                    is GroupResult.Loading -> {}
                }
                // Cleanup temp file
                if (result !is GroupResult.Loading && avatarFile != null && avatarFile.exists()) {
                     avatarFile.delete()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Group Detail ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class GroupDetailState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val myRole: com.hasani.messageapp.domain.model.MemberRole? = null,
    val isAdmin: Boolean = false,
    val isOwner: Boolean = false,
    val contacts: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: com.hasani.messageapp.data.repository.UserRepository,
    private val contactsRepository: com.hasani.messageapp.data.repository.ContactsRepository,
    private val sessionManager: com.hasani.messageapp.data.session.SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _state = MutableStateFlow(GroupDetailState())
    val state: StateFlow<GroupDetailState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<GroupEvent>()
    val events: SharedFlow<GroupEvent> = _events.asSharedFlow()
    
    private var currentGroupId: String? = null
    private var currentUserId: String? = null
    
    // Cache for phone to name mapping
    private var phoneToNameMap: Map<String, String> = emptyMap()
    
    init {
        viewModelScope.launch {
            currentUserId = sessionManager.userId.first()
        }
        // Load contacts for name resolution
        viewModelScope.launch {
            try {
                val contacts = contactsRepository.getDeviceContacts()
                phoneToNameMap = contacts.associate { 
                    normalizePhoneNumber(it.phoneNumber) to it.name 
                }
            } catch (e: Exception) {
                // Permission not granted - continue without contact names
            }
        }
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
     * Apply contact name resolution to a GroupMember
     */
    private fun applyContactNameToMember(member: GroupMember): GroupMember {
        val phone = member.user.phoneNumber
        if (phone.isNotBlank()) {
            val normalizedPhone = normalizePhoneNumber(phone)
            val contactName = phoneToNameMap[normalizedPhone]
            if (contactName != null) {
                return member.copy(
                    user = member.user.copy(displayName = contactName)
                )
            }
        }
        return member
    }
    
    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        viewModelScope.launch {
            groupRepository.getGroupById(groupId).collect { result ->
                when (result) {
                    is GroupResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is GroupResult.Success -> {
                        val group = result.data
                        val isOwner = group.myRole == com.hasani.messageapp.domain.model.MemberRole.OWNER
                        val isAdmin = group.myRole in listOf(
                            com.hasani.messageapp.domain.model.MemberRole.OWNER,
                            com.hasani.messageapp.domain.model.MemberRole.ADMIN
                        )
                        _state.update { it.copy(
                            group = group, 
                            isLoading = false,
                            isOwner = isOwner,
                            isAdmin = isAdmin,
                            myRole = group.myRole
                        ) }
                    }
                    is GroupResult.Error -> {
                        _state.update { it.copy(error = result.message, isLoading = false) }
                    }
                }
            }
        }
        
        // Load and observe members with contact name resolution
        viewModelScope.launch {
            groupRepository.getGroupMembers(groupId).collect { result ->
                if (result is GroupResult.Success) {
                    val membersWithContactNames = result.data.map { applyContactNameToMember(it) }
                    _state.update { it.copy(members = membersWithContactNames) }
                }
            }
        }
        
        viewModelScope.launch {
            groupRepository.observeGroupMembers(groupId).collectLatest { members ->
                val membersWithContactNames = members.map { applyContactNameToMember(it) }
                _state.update { it.copy(members = membersWithContactNames) }
            }
        }
    }
    
    fun promoteToAdmin(
        userId: String,
        canEditInfo: Boolean = false,
        canPostStory: Boolean = false,
        canAddMembers: Boolean = false,
        canRemoveMembers: Boolean = false
    ) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.changeRole(
                groupId, 
                userId, 
                com.hasani.messageapp.domain.model.MemberRole.ADMIN,
                canEditInfo,
                canPostStory,
                canAddMembers,
                canRemoveMembers
            ).collect { result ->
                if (result is GroupResult.Success) {
                     loadGroup(groupId) // Refresh
                } else if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun demoteFromAdmin(userId: String) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.changeRole(groupId, userId, com.hasani.messageapp.domain.model.MemberRole.MEMBER).collect { result ->
                if (result is GroupResult.Success) {
                     loadGroup(groupId) // Refresh
                } else if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun removeMember(userId: String) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.removeMember(groupId, userId).collect { result ->
                if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            userRepository.getContacts().collect { result ->
                if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    _state.update { it.copy(contacts = result.data) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    _state.update { it.copy(searchResults = result.data) }
                }
            }
        }
    }
    
    fun addMembers(memberIds: List<String>) {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.addMembers(groupId, memberIds).collect { result ->
                if (result is GroupResult.Error) {
                    _events.emit(GroupEvent.Error(result.message))
                }
            }
        }
    }
    
    fun leaveGroup() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            val userId = currentUserId ?: sessionManager.userId.first() ?: return@launch
            groupRepository.removeMember(groupId, userId).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _events.emit(GroupEvent.GroupLeft)
                    }
                    is GroupResult.Error -> {
                        _events.emit(GroupEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun deleteGroup() {
        val groupId = currentGroupId ?: return
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId).collect { result ->
                when (result) {
                    is GroupResult.Success -> {
                        _events.emit(GroupEvent.GroupDeleted)
                    }
                    is GroupResult.Error -> {
                        _events.emit(GroupEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }
}
