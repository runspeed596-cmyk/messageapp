package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.repository.UserRepository
import com.hasani.messageapp.data.repository.UserResult
import com.hasani.messageapp.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

data class UserProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    // Privacy-sanitized fields - derived from User's displayXxx fields
    val canSeeProfilePhoto: Boolean = true,
    val canSeeOnlineStatus: Boolean = true,
    val canSeePhoneNumber: Boolean = true,
    val displayPhoneNumber: String = "",
    val displayAvatarUrl: String? = null,
    // Social features
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val showCollaborationDialog: Boolean = false,
    val bioChannels: List<com.hasani.messageapp.domain.model.Channel> = emptyList()
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val apiService: ApiService,
    private val channelRepository: com.hasani.messageapp.data.repository.ChannelRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UserProfileState())
    val state: StateFlow<UserProfileState> = _state.asStateFlow()
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            userRepository.getUserById(userId).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        // Already set loading
                    }
                    is UserResult.Success -> {
                        val user = result.data
                        // Use the already-sanitized fields from the User domain model
                        // These fields are filtered in the toDomain() mapper based on
                        // the target user's own privacy settings (profileVisibility, etc.)
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                user = user,
                                canSeeProfilePhoto = user.displayAvatarUrl != null,
                                canSeeOnlineStatus = user.displayOnlineStatus,
                                canSeePhoneNumber = user.displayPhoneNumber != "مخفی",
                                displayPhoneNumber = user.displayPhoneNumber,
                                displayAvatarUrl = user.displayAvatarUrl
                            )
                        }
                        // Load social data
                        loadFollowCounts(userId)
                        checkFollowStatus(userId)
                        
                        // Load Bio Channels
                        if (user.bioChannelId1 != null || user.bioChannelId2 != null) {
                            loadBioChannels(listOfNotNull(user.bioChannelId1, user.bioChannelId2))
                        }
                    }
                    is UserResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
    private fun loadFollowCounts(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getFollowCounts(userId)
                if (response.isSuccessful) {
                    val counts = response.body()
                    _state.update { 
                        it.copy(
                            followerCount = counts?.followerCount ?: 0,
                            followingCount = counts?.followingCount ?: 0
                        )
                    }
                }
            } catch (e: Exception) {
                // Silent fail for social data
            }
        }
    }
    private fun checkFollowStatus(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.isFollowing(userId)
                if (response.isSuccessful) {
                    _state.update { it.copy(isFollowing = response.body()?.data ?: false) }
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    fun toggleFollow(userId: String) {
        if (_state.value.isFollowLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isFollowLoading = true) }
            try {
                val isCurrentlyFollowing = _state.value.isFollowing
                val response = if (isCurrentlyFollowing) {
                    apiService.unfollowUser(userId)
                } else {
                    apiService.followUser(userId)
                }
                if (response.isSuccessful) {
                    _state.update { 
                        it.copy(
                            isFollowing = !isCurrentlyFollowing,
                            followerCount = if (isCurrentlyFollowing) 
                                (it.followerCount - 1).coerceAtLeast(0) 
                            else 
                                it.followerCount + 1,
                            isFollowLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isFollowLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isFollowLoading = false) }
            }
        }
    }
    fun showCollaborationDialog() {
        _state.update { it.copy(showCollaborationDialog = true) }
    }
    fun hideCollaborationDialog() {
        _state.update { it.copy(showCollaborationDialog = false) }
    }
    fun sendCollaborationRequest(userId: String, title: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = com.hasani.messageapp.data.remote.dto.SendCollaborationRequest(
                    receiverId = userId,
                    title = title,
                    message = message
                )
                val response = apiService.sendCollaborationRequest(request)
                if (response.isSuccessful) {
                    _state.update { it.copy(showCollaborationDialog = false, error = null) }
                    onSuccess()
                } else {
                    // unexpected error or 400
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val gson = com.google.gson.Gson()
                            val apiResponse = gson.fromJson(errorBody, com.hasani.messageapp.data.remote.dto.ApiResponse::class.java)
                            apiResponse.message ?: "Error sending request"
                        } catch (e: Exception) {
                            "Error: ${response.code()}"
                        }
                    } else {
                        "Error: ${response.code()}"
                    }
                    _state.update { it.copy(error = errorMessage) }
                }
            } catch (e: Exception) {
                 _state.update { it.copy(error = e.message ?: "Unknown error") }
            }
        }
    }
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    private fun loadBioChannels(ids: List<String>) {
        viewModelScope.launch {
            val deferredChannels = ids.map { id ->
                async {
                    var channel: com.hasani.messageapp.domain.model.Channel? = null
                    try {
                        channelRepository.getChannelById(id).collect { res ->
                             if (res is com.hasani.messageapp.data.repository.ChannelResult.Success) {
                                 channel = res.data
                             }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    channel
                }
            }
            val channels = deferredChannels.awaitAll().filterNotNull()
            _state.update { it.copy(bioChannels = channels) }
        }
    }
}
