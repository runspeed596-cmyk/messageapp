package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.ChannelRepository
import com.Kelasor.app.data.repository.ChannelResult
import com.Kelasor.app.domain.model.Channel
import com.Kelasor.app.domain.model.ChannelPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.Kelasor.app.data.voice.VoiceRecorderManager
import com.Kelasor.app.data.audio.AudioPlayerManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import javax.inject.Inject
import com.Kelasor.app.data.remote.dto.ChannelPostCommentDto
import com.Kelasor.app.domain.mapper.toDomain

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel List ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class ChannelListState(
    val isLoading: Boolean = false,
    val channels: List<Channel> = emptyList(),
    val pinnedChannels: List<Channel> = emptyList(),
    val archivedChannels: List<Channel> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<Channel> = emptyList(),
    val selectedChannelIds: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false
) {
    val filteredChannels: List<Channel>
        get() = if (searchQuery.isBlank()) {
            channels.filter { !it.isArchived && !it.isPinned }
        } else {
            channels.filter { it.name.contains(searchQuery, ignoreCase = true) && !it.isArchived && !it.isPinned }
        }
}

@HiltViewModel
class ChannelListViewModel @Inject constructor(
    private val channelRepository: ChannelRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ChannelListState())
    val state: StateFlow<ChannelListState> = _state.asStateFlow()
    init {
        observeChannels()
        observeArchivedChannels()
        loadChannels()
    }
    private fun observeChannels() {
        viewModelScope.launch {
            channelRepository.observeSubscribedChannels().collect { allChannels ->
                val pinned = allChannels.filter { it.isPinned }
                val regular = allChannels.filter { !it.isPinned && !it.isArchived }
                _state.update { it.copy(channels = regular, pinnedChannels = pinned) }
            }
        }
    }
    private fun observeArchivedChannels() {
        viewModelScope.launch {
            channelRepository.observeArchivedChannels().collect { archivedChannels ->
                _state.update { it.copy(archivedChannels = archivedChannels) }
            }
        }
    }
    fun loadChannels(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            channelRepository.getChannels(forceRefresh = forceRefresh).collect { result ->
                when (result) {
                    is ChannelResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is ChannelResult.Success -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
    fun searchChannels(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 2) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            channelRepository.searchChannels(query).collect { result ->
                if (result is ChannelResult.Success) {
                    _state.update { it.copy(searchResults = result.data) }
                }
            }
        }
    }
    fun subscribe(channelId: String) {
        viewModelScope.launch {
            channelRepository.subscribe(channelId).collect { }
        }
    }
    fun unsubscribe(channelId: String) {
        viewModelScope.launch {
            channelRepository.unsubscribe(channelId).collect { }
        }
    }
    
    fun leaveChannel(channelId: String) {
        viewModelScope.launch {
            channelRepository.unsubscribe(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    loadChannels(forceRefresh = true)
                }
            }
        }
    }

    fun toggleSelection(channelId: String) {
        _state.update {
            val currentSelected = it.selectedChannelIds.toMutableSet()
            if (currentSelected.contains(channelId)) {
                currentSelected.remove(channelId)
            } else {
                currentSelected.add(channelId)
            }
            it.copy(selectedChannelIds = currentSelected)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedChannelIds = emptySet()) }
    }

    fun requestDeleteSelection() {
        if (_state.value.selectedChannelIds.isNotEmpty()) {
            _state.update { it.copy(showDeleteConfirmation = true) }
        }
    }

    fun cancelDeleteSelection() {
        _state.update { it.copy(showDeleteConfirmation = false) }
    }

    fun confirmDeleteSelection() {
        val selected = _state.value.selectedChannelIds
        if (selected.isEmpty()) return

        viewModelScope.launch {
            selected.forEach { channelId ->
                channelRepository.unsubscribe(channelId).collect {}
            }
            loadChannels(forceRefresh = true)
            _state.update { it.copy(selectedChannelIds = emptySet(), showDeleteConfirmation = false) }
        }
    }
    
    fun pinSelectedChannels(isPinned: Boolean) {
        val selected = _state.value.selectedChannelIds
        if (selected.isEmpty()) return

        viewModelScope.launch {
            selected.forEach { channelId ->
                channelRepository.togglePin(channelId, isPinned).collect { /* Silent */ }
            }
            _state.update { it.copy(selectedChannelIds = emptySet()) }
        }
    }

    fun archiveChannel(channelId: String, archive: Boolean) {
        viewModelScope.launch {
            val flow = if (archive) {
                channelRepository.archiveChannel(channelId)
            } else {
                channelRepository.unarchiveChannel(channelId)
            }
            flow.collect { /* Silent operation, toast shown in UI */ }
        }
    }

    fun pinChannel(channelId: String, isPinned: Boolean) {
        viewModelScope.launch {
            channelRepository.togglePin(channelId, isPinned).collect { /* Silent operation */ }
        }
    }

    fun toggleMute(channelId: String, isMuted: Boolean) {
        viewModelScope.launch {
            channelRepository.toggleMute(channelId, isMuted).collect { /* Silent operation */ }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel View ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class ChannelViewState(
    val isLoading: Boolean = false,
    val isInitialLoad: Boolean = true,
    val channel: Channel? = null,
    val posts: List<ChannelPost> = emptyList(),
    val error: String? = null,
    val isCreatePostDialogVisible: Boolean = false,
    val newPostContent: String = "",
    val currentUserId: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadTotalBytes: Long = 0L,
    val activePostForComments: String? = null,
    val comments: List<ChannelPostCommentDto> = emptyList(),
    val isLoadingComments: Boolean = false,
    val newCommentContent: String = "",
    val editingPost: ChannelPost? = null,
    val selectedPostIds: Set<String> = emptySet()
) {
    // Admin check with owner fallback
    val canPost: Boolean
        get() {
            if (channel == null) return false
            val isOwner = currentUserId != null && channel.owner?.id == currentUserId
            return channel.isAdmin || isOwner
        }
    
    // Membership check
    val isMember: Boolean
        get() = channel?.isSubscribed == true
}

sealed class ChannelEvent {
    data object PostCreated : ChannelEvent()
    data object ChannelDeleted : ChannelEvent()
    data object Subscribed : ChannelEvent()
    data object Unsubscribed : ChannelEvent()
    data class Error(val message: String) : ChannelEvent()
}

@HiltViewModel
class ChannelViewViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val chatRepository: com.Kelasor.app.data.repository.ChatRepository,
    private val pollRepository: com.Kelasor.app.data.repository.PollRepository,
    private val webSocketManager: com.Kelasor.app.data.websocket.WebSocketManager,
    private val sessionManager: com.Kelasor.app.data.session.SessionManager,
    val voiceRecorderManager: VoiceRecorderManager,
    val audioPlayerManager: AudioPlayerManager,
    val locationManager: com.Kelasor.app.data.location.LocationManager,
    private val currentChatManager: com.Kelasor.app.data.session.CurrentChatManager
) : ViewModel() {

    fun setActiveChat(id: String) {
        currentChatManager.setChat(id)
    }

    fun clearActiveChat() {
        currentChatManager.setChat(null)
    }
    private val _state = MutableStateFlow(ChannelViewState())
    val state: StateFlow<ChannelViewState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<ChannelEvent>()
    val events: SharedFlow<ChannelEvent> = _events.asSharedFlow()
    private var currentChannelId: String? = null
    
    init {
        loadCurrentUserId()
    }
    
    private fun loadCurrentUserId() {
        viewModelScope.launch {
            sessionManager.userId.collect { userId ->
                _state.update { it.copy(currentUserId = userId) }
            }
        }
    }
    fun loadChannel(channelId: String) {
        if (currentChannelId == channelId) return
        currentChannelId = channelId
        webSocketManager.subscribeToChannel(channelId)
        observeChannel(channelId)
        observePosts(channelId)
        loadChannelDetails(channelId)
        loadPosts(channelId)
    }
    private fun observeChannel(channelId: String) {
        viewModelScope.launch {
            channelRepository.observeChannel(channelId).collect { channel ->
                if (channel != null) {
                    android.util.Log.d("ChannelDebug", "Observed channel: id=${channel.id}, isAdmin=${channel.isAdmin}, owner=${channel.owner?.id}, current=${state.value.currentUserId}")
                    _state.update { it.copy(channel = channel) }
                }
            }
        }
    }
    private fun observePosts(channelId: String) {
        viewModelScope.launch {
            channelRepository.observePosts(channelId).collect { posts ->
                _state.update { it.copy(posts = posts, isInitialLoad = false) }
            }
        }
    }
    private fun loadChannelDetails(channelId: String) {
        viewModelScope.launch {
            channelRepository.getChannelById(channelId).collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        _state.update { it.copy(channel = result.data) }
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(error = result.message) }
                    }
                    is ChannelResult.Loading -> {}
                }
            }
        }
    }
    private fun loadPosts(channelId: String, page: Int = 0) {
        viewModelScope.launch {
            channelRepository.getPosts(channelId, page).collect { result ->
                when (result) {
                    is ChannelResult.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is ChannelResult.Success -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
    fun subscribe() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.subscribe(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    _events.emit(ChannelEvent.Subscribed)
                }
            }
        }
    }
    fun unsubscribe() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.unsubscribe(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    _events.emit(ChannelEvent.Unsubscribed)
                }
            }
        }
    }

    fun toggleMute() {
        val currentChannel = _state.value.channel ?: return
        viewModelScope.launch {
            val newMuteStatus = !currentChannel.isMuted
            channelRepository.toggleMute(currentChannel.id, newMuteStatus).collect { result ->
                if (result is ChannelResult.Success) {
                    _state.update { 
                        it.copy(channel = currentChannel.copy(isMuted = newMuteStatus)) 
                    }
                } else if (result is ChannelResult.Error) {
                    _events.emit(ChannelEvent.Error(result.message))
                }
            }
        }
    }
    fun setNewPostContent(content: String) {
        _state.update { it.copy(newPostContent = content) }
    }
    fun showCreatePostDialog() {
        _state.update { it.copy(isCreatePostDialogVisible = true) }
    }
    fun hideCreatePostDialog() {
        _state.update { it.copy(isCreatePostDialogVisible = false, newPostContent = "") }
    }
    fun createPost(content: String? = null, replyToPostId: String? = null, type: String = "TEXT", mediaUrl: String? = null, amplitudes: List<Int>? = null) {
        val channelId = currentChannelId ?: return
        val finalContent = content ?: _state.value.newPostContent
        if (finalContent.isBlank() && mediaUrl == null) return
        
        viewModelScope.launch {
            channelRepository.createPost(channelId, finalContent, type = type, mediaUrl = mediaUrl, amplitudes = amplitudes).collect { result ->
                when (result) {
                    is ChannelResult.Loading -> {
                         if (content == null) _state.update { it.copy(isLoading = true) }
                    }
                    is ChannelResult.Success -> {
                        _state.update {
                            it.copy(isLoading = false, isCreatePostDialogVisible = false, newPostContent = "")
                        }
                        _events.emit(ChannelEvent.PostCreated)
                        loadPosts(channelId)
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(ChannelEvent.Error(result.message))
                    }
                }
            }
        }
    }

    fun uploadAndSendFile(channelId: String, uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isUploading = true, uploadProgress = 0f) }
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
                    com.Kelasor.app.data.remote.util.ProgressRequestBody(
                        file = tempFile,
                        contentType = (context.contentResolver.getType(uri) ?: "*/*").toMediaTypeOrNull(),
                        listener = object : com.Kelasor.app.data.remote.util.ProgressRequestBody.ProgressListener {
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
                        createPost(content = fileName, type = "FILE", mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(ChannelEvent.Error("خطا در آپلود فایل: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(ChannelEvent.Error("خطا در آپلود فایل: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun uploadAndSendMedia(channelId: String, uri: android.net.Uri, context: android.content.Context, isVideo: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isUploading = true, uploadProgress = 0f) }
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
                        createPost(content = "$emojiPrefix $fileName", type = messageType, mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(ChannelEvent.Error("خطا در آپلود: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(ChannelEvent.Error("خطا در آپلود: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun uploadAndSendAudio(channelId: String, uri: android.net.Uri, context: android.content.Context) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isUploading = true, uploadProgress = 0f) }
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
                        createPost(content = "🎵 $fileName", type = "AUDIO", mediaUrl = fileUrl)
                    },
                    onFailure = { e ->
                        _events.emit(ChannelEvent.Error("خطا در آپلود صوت: ${e.message}"))
                    }
                )
                tempFile.delete()
            } catch (e: Exception) {
                _events.emit(ChannelEvent.Error("خطا در آپلود صوت: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun sendVoiceMessage(voiceFile: java.io.File, durationMs: Long, amplitudes: List<Int> = emptyList()) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isUploading = true, uploadProgress = 0f) }
            try {
                val requestBody = voiceFile.asRequestBody("audio/mp4".toMediaTypeOrNull())
                _state.update { it.copy(uploadTotalBytes = voiceFile.length()) }
                val part = okhttp3.MultipartBody.Part.createFormData("file", voiceFile.name, requestBody)
                val response = chatRepository.uploadFile(part)
                
                response.fold(
                    onSuccess = { fileUrl ->
                        val durationSeconds = durationMs / 1000
                        createPost(content = "🎤 صدا (${durationSeconds}s)", type = "VOICE", mediaUrl = fileUrl, amplitudes = amplitudes)
                    },
                    onFailure = { e ->
                        _events.emit(ChannelEvent.Error("خطا در آپلود صدا: ${e.message}"))
                    }
                )
                voiceFile.delete()
            } catch (e: Exception) {
                _events.emit(ChannelEvent.Error("خطا در آپلود صدا: ${e.message}"))
            } finally {
                _state.update { it.copy(isLoading = false, isUploading = false, uploadProgress = 0f) }
            }
        }
    }

    fun sendLocationMessage(latitude: Double, longitude: Double) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            try {
                createPost(content = "📍 موقعیت مکانی", type = "LOCATION", mediaUrl = "$latitude,$longitude")
            } catch (e: Exception) {
                _events.emit(ChannelEvent.Error("خطا در ارسال موقعیت: ${e.message}"))
            }
        }
    }

    fun createPoll(question: String, options: List<String>, isMultipleChoice: Boolean, isAnonymous: Boolean) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = pollRepository.createPoll(question, options, isMultipleChoice, isAnonymous)
            result.fold(
                onSuccess = { pollDto ->
                    viewModelScope.launch {
                        channelRepository.sendPollMessage(channelId, pollDto).collect { result ->
                            if (result is ChannelResult.Error) {
                                _events.emit(ChannelEvent.Error(result.message))
                            }
                            _state.update { it.copy(isLoading = false) }
                        }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(ChannelEvent.Error("Poll creation failed: ${e.message}"))
                }
            )
        }
    }

    fun votePoll(pollId: String, optionIds: List<String>) {
        viewModelScope.launch {
             pollRepository.vote(pollId, optionIds).fold(
                 onSuccess = { updatedPoll ->
                     _state.update { currentState ->
                         val updatedPosts = currentState.posts.map { post ->
                             if (post.poll?.id == pollId) {
                                 post.copy(poll = updatedPoll.toDomain())
                             } else {
                                 post
                             }
                         }
                         currentState.copy(posts = updatedPosts)
                     }
                 },
                 onFailure = { error ->
                     _events.emit(ChannelEvent.Error("Vote failed: ${error.message}"))
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
    fun deleteChannel() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.deleteChannel(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    _events.emit(ChannelEvent.ChannelDeleted)
                }
            }
        }
    }


    fun startEditingPost(post: ChannelPost) {
        _state.update { it.copy(
            editingPost = post,
            newPostContent = post.content
        )}
    }

    fun cancelEditingPost() {
        _state.update { it.copy(
            editingPost = null,
            newPostContent = ""
        )}
    }

    fun editPost(postId: String, content: String) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.editPost(channelId, postId, content).collect { result ->
                when (result) {
                    is ChannelResult.Loading -> {
                         _state.update { it.copy(isLoading = true) }
                    }
                    is ChannelResult.Success -> {
                        _state.update {
                            it.copy(isLoading = false, newPostContent = "", editingPost = null)
                        }
                        currentChannelId?.let { loadPosts(it) }
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(ChannelEvent.Error(result.message))
                    }
                }
            }
        }
    }

    fun deletePost(postId: String, deleteForEveryone: Boolean = true) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.deletePost(channelId, postId, deleteForEveryone).collect { result ->
                 if (result is ChannelResult.Error) {
                     _events.emit(ChannelEvent.Error(result.message))
                 }
            }
        }
    }

    fun reactToPost(postId: String, reaction: String) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.reactToPost(channelId, postId, reaction).collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        // Refresh posts to see updated reactions
                        currentChannelId?.let { loadPosts(it) }
                    }
                    is ChannelResult.Error -> {
                        _events.emit(ChannelEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }

    fun openComments(postId: String) {
        _state.update { it.copy(activePostForComments = postId, comments = emptyList()) }
        loadComments(postId)
    }

    fun closeComments() {
        _state.update { it.copy(activePostForComments = null, comments = emptyList()) }
    }

    fun updateNewCommentContent(content: String) {
        _state.update { it.copy(newCommentContent = content) }
    }

    fun loadComments(postId: String) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.getComments(channelId, postId).collect { result ->
                when (result) {
                    is ChannelResult.Loading -> _state.update { it.copy(isLoadingComments = true) }
                    is ChannelResult.Success -> _state.update { it.copy(isLoadingComments = false, comments = result.data.comments) }
                    is ChannelResult.Error -> _state.update { it.copy(isLoadingComments = false) } // Handle error silently or show toast
                }
            }
        }
    }

    fun sendComment() {
        val channelId = currentChannelId ?: return
        val postId = state.value.activePostForComments ?: return
        val content = state.value.newCommentContent
        if (content.isBlank()) return

        viewModelScope.launch {
            channelRepository.sendComment(channelId, postId, content).collect { result ->
                if (result is ChannelResult.Success) {
                    _state.update { it.copy(newCommentContent = "") }
                    loadComments(postId) // Refresh
                } else if (result is ChannelResult.Error) {
                    _events.emit(ChannelEvent.Error(result.message))
                }
            }
        }
    }

    // Overriding createPost to handle edit mode logic update
    fun createOrEditPost(content: String? = null) {
        val currentEditingPost = _state.value.editingPost
        if (currentEditingPost != null) {
            editPost(currentEditingPost.id, content ?: _state.value.newPostContent)
        } else {
            createPost(content)
        }
    }

    fun togglePostSelection(postId: String) {
        _state.update {
            val currentSelected = it.selectedPostIds.toMutableSet()
            if (currentSelected.contains(postId)) {
                currentSelected.remove(postId)
            } else {
                currentSelected.add(postId)
            }
            it.copy(selectedPostIds = currentSelected)
        }
    }

    fun clearSelection() {
        _state.update { it.copy(selectedPostIds = emptySet()) }
    }

    override fun onCleared() {
        super.onCleared()
        currentChannelId?.let { webSocketManager.unsubscribeFromChannel(it) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Channel Settings ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class ChannelSettingsState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val channel: Channel? = null,
    val subscribers: List<com.Kelasor.app.domain.model.ChannelSubscriber> = emptyList(),
    val isOwner: Boolean = false,
    val error: String? = null,
    val contacts: List<com.Kelasor.app.domain.model.User> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.Kelasor.app.domain.model.User> = emptyList()
)

sealed class ChannelSettingsEvent {
    data object ChannelUpdated : ChannelSettingsEvent()
    data object ChannelDeleted : ChannelSettingsEvent()
    data object ChannelLeft : ChannelSettingsEvent()
    data class Error(val message: String) : ChannelSettingsEvent()
}

@HiltViewModel
class ChannelSettingsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository,
    private val contactsRepository: com.Kelasor.app.data.repository.ContactsRepository,
    private val sessionManager: com.Kelasor.app.data.session.SessionManager,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    private val _state = MutableStateFlow(ChannelSettingsState())
    val state: StateFlow<ChannelSettingsState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<ChannelSettingsEvent>()
    val events: SharedFlow<ChannelSettingsEvent> = _events.asSharedFlow()
    
    private var currentChannelId: String? = null
    
    private var phoneToNameMap: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val contacts = contactsRepository.getDeviceContacts()
                phoneToNameMap = contacts.associate { it.phoneNumber to it.name }
            } catch (e: Exception) {
                e.printStackTrace()
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

    private fun applyContactName(user: com.Kelasor.app.domain.model.User): com.Kelasor.app.domain.model.User {
        val normalized = normalizePhoneNumber(user.phoneNumber)
        val contactName = phoneToNameMap[normalized]
        return if (contactName != null) {
            user.copy(contactName = contactName)
        } else {
            user
        }
    }

    
    fun loadChannel(channelId: String) {
        currentChannelId = channelId
        observeChannel(channelId)
        loadSubscribers(channelId)
    }
    
    private fun observeChannel(channelId: String) {
        viewModelScope.launch {
            val currentUserId = sessionManager.userId.first()
            channelRepository.observeChannel(channelId).collect { channel ->
                if (channel != null) {
                    val isOwner = channel.owner?.id == currentUserId
                    _state.update { it.copy(channel = channel, isOwner = isOwner) }
                }
            }
        }
        // Also trigger sync
        viewModelScope.launch {
            channelRepository.getChannelById(channelId).collect {
                // handle result if needed, mostly for error
            }
        }
    }
    
    private fun loadSubscribers(channelId: String) {
        viewModelScope.launch {
            channelRepository.getSubscribers(channelId).collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        val mappedSubscribers = result.data.map { subscriber ->
                            subscriber.copy(user = applyContactName(subscriber.user))
                        }
                        _state.update { it.copy(subscribers = mappedSubscribers) }
                    }
                    is ChannelResult.Error -> {
                        // _state.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            userRepository.getContacts().collect { result ->
                if (result is com.Kelasor.app.data.repository.UserResult.Success) {
                    val mappedContacts = result.data.map { applyContactName(it) }
                    _state.update { it.copy(contacts = mappedContacts) }
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
                if (result is com.Kelasor.app.data.repository.UserResult.Success) {
                    val mappedResults = result.data.map { applyContactName(it) }
                    _state.update { it.copy(searchResults = mappedResults) }
                }
            }
        }
    }

    fun addMembers(userIds: List<String>) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.addMembers(channelId, userIds).collect { result ->
                if (result is ChannelResult.Error) {
                    _events.emit(ChannelSettingsEvent.Error(result.message))
                }
            }
            // Refresh subscribers
            loadSubscribers(channelId)
        }
    }

    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "temp_channel_avatar_${System.currentTimeMillis()}.jpg"
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

    fun updateChannel(name: String?, description: String?, isPublic: Boolean?, publicId: String? = null, avatarUri: android.net.Uri? = null) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val avatarFile = avatarUri?.let { getFileFromUri(it) }

            channelRepository.updateChannel(channelId, name, description, isPublic, publicId, avatarFile).collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        _state.update { it.copy(channel = result.data, isSaving = false) }
                        _events.emit(ChannelSettingsEvent.ChannelUpdated)
                    }
                    is ChannelResult.Error -> {
                        _state.update { it.copy(isSaving = false) }
                        _events.emit(ChannelSettingsEvent.Error(result.message))
                    }
                    else -> {}
                }
                
                 if (result !is ChannelResult.Loading && avatarFile != null && avatarFile.exists()) {
                     avatarFile.delete()
                }
            }
        }
    }
    
    fun toggleInviteLink(enabled: Boolean) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.toggleInviteLink(channelId, enabled).collect { }
        }
    }
    
    fun regenerateInviteLink() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.regenerateInviteLink(channelId).collect { }
        }
    }
    
    fun addAdmin(userId: String) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.addAdmin(channelId, userId).collect { }
        }
    }
    
    fun removeAdmin(userId: String) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.removeAdmin(channelId, userId).collect { }
        }
    }

    fun deleteChannel() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.deleteChannel(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    _events.emit(ChannelSettingsEvent.ChannelDeleted)
                } else if (result is ChannelResult.Error) {
                    _events.emit(ChannelSettingsEvent.Error(result.message))
                }
            }
        }
    }

    fun unsubscribe() {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            channelRepository.unsubscribe(channelId).collect { result ->
                if (result is ChannelResult.Success) {
                    _events.emit(ChannelSettingsEvent.ChannelLeft)
                } else if (result is ChannelResult.Error) {
                    _events.emit(ChannelSettingsEvent.Error(result.message))
                }
            }
        }
    }
    
    fun archiveChannel(archive: Boolean) {
        val channelId = currentChannelId ?: return
        viewModelScope.launch {
            val flow = if (archive) {
                channelRepository.archiveChannel(channelId)
            } else {
                channelRepository.unarchiveChannel(channelId)
            }
            flow.collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        val message = if (archive) "کانال آرشیو شد" else "کانال از آرشیو خارج شد"
                        _events.emit(ChannelSettingsEvent.Error(message))
                    }
                    is ChannelResult.Error -> {
                        _events.emit(ChannelSettingsEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }
}




