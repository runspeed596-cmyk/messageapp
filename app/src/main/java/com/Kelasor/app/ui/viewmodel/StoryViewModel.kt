package com.Kelasor.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.StoryRepository
import com.Kelasor.app.domain.model.Story
import com.Kelasor.app.domain.model.StoryUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StoriesUiState {
    object Loading : StoriesUiState()
    data class Success(val storyUsers: List<StoryUser>) : StoriesUiState()
    data class Error(val message: String, val isPremiumRequired: Boolean = false) : StoriesUiState()
}

// Separate error event for one-time error notifications (does not change uiState)
sealed class StoryErrorEvent {
    data class PremiumRequired(val message: String) : StoryErrorEvent()
    data class GenericError(val message: String) : StoryErrorEvent()
}

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository,
    @ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<StoriesUiState>(StoriesUiState.Loading)
    val uiState: StateFlow<StoriesUiState> = _uiState.asStateFlow()

    // Separate state for group stories
    private val _groupUiState = MutableStateFlow<StoriesUiState>(StoriesUiState.Loading)
    val groupUiState: StateFlow<StoriesUiState> = _groupUiState.asStateFlow()

    // Separate state for channel stories
    private val _channelUiState = MutableStateFlow<StoriesUiState>(StoriesUiState.Loading)
    val channelUiState: StateFlow<StoriesUiState> = _channelUiState.asStateFlow()

    private val _currentUser = MutableStateFlow<com.Kelasor.app.domain.model.User?>(null)
    val currentUser: StateFlow<com.Kelasor.app.domain.model.User?> = _currentUser.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Error events - use SharedFlow for one-time events (StoryRow stays visible)
    private val _errorEvent = MutableSharedFlow<StoryErrorEvent>()
    val errorEvent: SharedFlow<StoryErrorEvent> = _errorEvent.asSharedFlow()

    // For navigation to viewer
    private val _selectedStoryUser = MutableStateFlow<StoryUser?>(null)
    val selectedStoryUser: StateFlow<StoryUser?>  = _selectedStoryUser.asStateFlow()

    init {
        loadStories()
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun loadStories() {
        viewModelScope.launch {
            if (_uiState.value !is StoriesUiState.Success) {
                _uiState.value = StoriesUiState.Loading
            }
            storyRepository.getStories().collect { result ->
                result.fold(
                    onSuccess = { users ->
                        // Sort: Users with unviewed stories first, then by latest story time
                        val sorted = users.sortedWith(
                            compareByDescending<StoryUser> { !it.allViewed }
                                .thenByDescending { user ->
                                    val latestStory = user.stories.maxByOrNull { it.createdAt }
                                    latestStory?.createdAt ?: java.time.Instant.MIN
                                }
                        )
                        _uiState.value = StoriesUiState.Success(sorted)
                    },
                    onFailure = { e ->
                        // Even if fetch fails (e.g. 404), we want to show the list so user can post their own story.
                        _uiState.value = StoriesUiState.Success(emptyList())
                    }
                )
            }
        }
    }

    fun uploadStory(uri: Uri, type: String? = null, duration: Int = 5, caption: String? = null) {
        viewModelScope.launch {
            _isUploading.value = true
            val (fileType, finalDuration) = prepareUpload(uri, type, duration)
            if (fileType == "VIDEO" && finalDuration > 60) {
                _errorEvent.emit(StoryErrorEvent.GenericError("Video must be less than 1 minute"))
                _isUploading.value = false
                return@launch
            }
            val result = storyRepository.uploadStory(uri, fileType, finalDuration, caption)
            result.fold(
                onSuccess = { loadStories() },
                onFailure = { e ->
                    val msg = e.message ?: "Upload failed"
                    val isPremium = msg.contains("limit", ignoreCase = true) || msg.contains("Premium", ignoreCase = true)
                    if (isPremium) {
                        _errorEvent.emit(StoryErrorEvent.PremiumRequired(msg))
                    } else {
                        _errorEvent.emit(StoryErrorEvent.GenericError(msg))
                    }
                }
            )
            _isUploading.value = false
        }
    }

    fun loadGroupStories() {
        viewModelScope.launch {
            if (_groupUiState.value !is StoriesUiState.Success) {
                _groupUiState.value = StoriesUiState.Loading
            }
             storyRepository.getGroupStoriesFromApi().fold(
                onSuccess = { users ->
                    val sorted = sortStories(users)
                    _groupUiState.value = StoriesUiState.Success(sorted)
                },
                onFailure = {
                    _groupUiState.value = StoriesUiState.Success(emptyList())
                }
            )
        }
    }

    fun loadChannelStories() {
        viewModelScope.launch {
            if (_channelUiState.value !is StoriesUiState.Success) {
                _channelUiState.value = StoriesUiState.Loading
            }
            storyRepository.getChannelStoriesFromApi().fold(
                onSuccess = { users ->
                    val sorted = sortStories(users)
                    _channelUiState.value = StoriesUiState.Success(sorted)
                },
                onFailure = {
                     _channelUiState.value = StoriesUiState.Success(emptyList())
                }
            )
        }
    }

    fun uploadGroupStory(groupId: String, uri: Uri, type: String? = null, duration: Int = 5, caption: String? = null) {
        viewModelScope.launch {
            _isUploading.value = true
            val (fileType, finalDuration) = prepareUpload(uri, type, duration)
            if (fileType == "VIDEO" && finalDuration > 60) {
                _errorEvent.emit(StoryErrorEvent.GenericError("Video must be less than 1 minute"))
                _isUploading.value = false
                return@launch
            }
            storyRepository.uploadGroupStory(groupId, uri, fileType, finalDuration, caption).fold(
                onSuccess = { loadGroupStories() },
                onFailure = { e ->
                    val msg = e.message ?: "Upload failed"
                    val isPremium = msg.contains("limit", ignoreCase = true) || msg.contains("Premium", ignoreCase = true)
                    if (isPremium) {
                        _errorEvent.emit(StoryErrorEvent.PremiumRequired(msg))
                    } else {
                        _errorEvent.emit(StoryErrorEvent.GenericError(msg))
                    }
                }
            )
            _isUploading.value = false
        }
    }

    fun uploadChannelStory(channelId: String, uri: Uri, type: String? = null, duration: Int = 5, caption: String? = null) {
        viewModelScope.launch {
            _isUploading.value = true
            val (fileType, finalDuration) = prepareUpload(uri, type, duration)
            if (fileType == "VIDEO" && finalDuration > 60) {
                _errorEvent.emit(StoryErrorEvent.GenericError("Video must be less than 1 minute"))
                _isUploading.value = false
                return@launch
            }
            storyRepository.uploadChannelStory(channelId, uri, fileType, finalDuration, caption).fold(
                onSuccess = { loadChannelStories() },
                onFailure = { e ->
                    val msg = e.message ?: "Upload failed"
                    val isPremium = msg.contains("limit", ignoreCase = true) || msg.contains("Premium", ignoreCase = true)
                    if (isPremium) {
                        _errorEvent.emit(StoryErrorEvent.PremiumRequired(msg))
                    } else {
                        _errorEvent.emit(StoryErrorEvent.GenericError(msg))
                    }
                }
            )
            _isUploading.value = false
        }
    }

    private fun sortStories(users: List<StoryUser>): List<StoryUser> {
         return users.sortedWith(
            compareByDescending<StoryUser> { !it.allViewed }
                .thenByDescending { user ->
                    val latestStory = user.stories.maxByOrNull { it.createdAt }
                    latestStory?.createdAt ?: java.time.Instant.MIN
                }
        )
    }

    private fun prepareUpload(uri: Uri, type: String?, duration: Int): Pair<String, Int> {
        // Auto-detect type if not specified or "AUTO"
        var fileType = if (type == null || type == "AUTO") {
            val mimeType = context.contentResolver.getType(uri)
            var extension: String? = null
            if (uri.scheme == "content") {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            val name = cursor.getString(nameIndex)
                            extension = name?.substringAfterLast('.', "")?.lowercase()
                        }
                    }
                }
            } else {
                extension = uri.path?.substringAfterLast('.', "")?.lowercase()
            }

            if (mimeType?.startsWith("video/") == true || 
                extension == "mp4" || 
                extension == "mov" || 
                extension == "mkv") {
                "VIDEO" 
            } else {
                "IMAGE"
            }
        } else {
            type
        }

        // Validate Video Duration
        var finalDuration = duration
        if (fileType == "VIDEO") {
            try {
                val retriever = android.media.MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                val time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                val timeInMillis = time?.toLong() ?: 0L
                retriever.release()

                // Update duration in seconds for the backend
                finalDuration = (timeInMillis / 1000).toInt()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return Pair(fileType, finalDuration)
    }

    fun markStoryAsViewed(storyId: String) {
        viewModelScope.launch {
            storyRepository.markAsViewed(storyId)
        }
    }

    fun openStoryViewer(user: StoryUser) {
        _selectedStoryUser.value = user
    }

    fun closeStoryViewer() {
        _selectedStoryUser.value = null
        _viewersState.value = emptyList() // Reset viewers
    }

    // Viewers Logic
    private val _viewersState =
        MutableStateFlow<List<com.Kelasor.app.domain.model.StoryViewer>>(emptyList())
    val viewersState: StateFlow<List<com.Kelasor.app.domain.model.StoryViewer>> =
        _viewersState.asStateFlow()

    fun loadStoryViews(storyId: String) {
        viewModelScope.launch {
            storyRepository.getStoryViews(storyId).fold(
                onSuccess = { viewers ->
                    _viewersState.value = viewers
                },
                onFailure = {
                    _viewersState.value = emptyList()
                }
            )
        }
    }
    
    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            android.util.Log.d("StoryViewModel", "Requesting delete for storyId: $storyId")
            _isUploading.value = true 
            storyRepository.deleteStory(storyId).fold(
                onSuccess = {
                    android.util.Log.d("StoryViewModel", "Delete success")
                    // We don't know if we are in group/channel mode here unless we track it.
                    // Ideally we should reload based on context, but loadStories() is a safe default for now
                    // or simple refresh current list?
                    // Let's reload everything or just let the user pull to refresh.
                    // Ideally I should track "Current Mode".
                    loadStories() 
                    closeStoryViewer() 
                },
                onFailure = { e ->
                     android.util.Log.e("StoryViewModel", "Delete failed", e)
                     _uiState.value = StoriesUiState.Error("خطا در حذف استوری: ${e.message}")
                }
            )
            _isUploading.value = false
        }
    }
}
