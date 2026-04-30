package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.repository.UserRepository
import com.hasani.messageapp.data.repository.UserResult
import com.hasani.messageapp.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0
)

sealed class ProfileEvent {
    data object SaveSuccess : ProfileEvent()
    data object AvatarUploaded : ProfileEvent()
    data class Error(val message: String) : ProfileEvent()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val apiService: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()
    init {
        loadCurrentUser()
        observeCurrentUser()
    }
    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                if (user != null) {
                    _state.update { it.copy(user = user) }
                    loadFollowCounts(user.id)
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
                // Silently fail, counts will show 0
            }
        }
    }
    fun loadCurrentUser(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            userRepository.getCurrentUser(forceRefresh).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is UserResult.Success -> {
                        _state.update { it.copy(isLoading = false, user = result.data) }
                    }
                    is UserResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(ProfileEvent.Error(result.message))
                    }
                }
            }
        }
    }
    fun updateProfile(
        username: String?,
        displayName: String?,
        bio: String?,
        university: String? = null,
        fieldOfStudy: String? = null,
        education: String? = null,
        skills: String? = null,
        interests: String? = null,
        workExperience: String? = null,
        achievements: String? = null,
        bioChannelId1: String? = null,
        bioChannelId2: String? = null
    ) {
        viewModelScope.launch {
            userRepository.updateProfile(
                username, displayName, bio,
                university, fieldOfStudy, education, skills, interests, workExperience, achievements,
                bioChannelId1, bioChannelId2
            ).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        _state.update { it.copy(isSaving = true, error = null) }
                    }
                    is UserResult.Success -> {
                        _state.update {
                            it.copy(isSaving = false, user = result.data, saveSuccess = true)
                        }
                        _events.emit(ProfileEvent.SaveSuccess)
                    }
                    is UserResult.Error -> {
                        _state.update { it.copy(isSaving = false, error = result.message) }
                        _events.emit(ProfileEvent.Error(result.message))
                    }
                }
            }
        }
    }
    fun uploadAvatar(file: File) {
        viewModelScope.launch {
            userRepository.uploadAvatar(file).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        _state.update { it.copy(isSaving = true, error = null) }
                    }
                    is UserResult.Success -> {
                        _state.update { it.copy(isSaving = false, user = result.data) }
                        // Force refresh to ensure UI gets the latest avatar URL
                        _events.emit(ProfileEvent.AvatarUploaded)
                    }
                    is UserResult.Error -> {
                        _state.update { it.copy(isSaving = false, error = result.message) }
                        _events.emit(ProfileEvent.Error(result.message))
                    }
                }
            }
        }
    }
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    fun resetSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }
}
