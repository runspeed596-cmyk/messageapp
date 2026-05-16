package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.EducationLevelDto
import com.Kelasor.app.data.remote.dto.FieldOfStudyDto
import com.Kelasor.app.data.remote.dto.UniversitySimpleDto
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import com.Kelasor.app.domain.model.User
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
    val followingCount: Int = 0,
    val universities: List<UniversitySimpleDto> = emptyList(),
    val fieldsOfStudy: List<FieldOfStudyDto> = emptyList(),
    val educationLevels: List<EducationLevelDto> = emptyList(),
    val isReferenceDataLoading: Boolean = false,
    val provinces: List<String> = emptyList(),
    val cities: List<String> = emptyList()
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
        loadProvinces()
        viewModelScope.launch {
            userRepository.currentUserId.collect { newUserId ->
                if (newUserId != null && _state.value.user?.id != newUserId && _state.value.user != null) {
                    loadCurrentUser(forceRefresh = true)
                }
            }
        }
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
    fun loadReferenceData() {
        viewModelScope.launch {
            _state.update { it.copy(isReferenceDataLoading = true) }
            try {
                val response = apiService.getReferenceData()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        _state.update {
                            it.copy(
                                universities = data.universities,
                                fieldsOfStudy = data.fieldsOfStudy,
                                educationLevels = data.educationLevels,
                                isReferenceDataLoading = false
                            )
                        }
                    }
                } else {
                    _state.update { it.copy(isReferenceDataLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isReferenceDataLoading = false) }
            }
        }
    }
    fun updateProfile(
        username: String?,
        displayName: String?,
        firstName: String? = null,
        lastName: String? = null,
        nationalCode: String? = null,
        educationalRole: String? = null,
        gradeLevel: String? = null,
        major: String? = null,
        bio: String?,
        university: String? = null,
        fieldOfStudy: String? = null,
        education: String? = null,
        skills: String? = null,
        interests: String? = null,
        workExperience: String? = null,
        achievements: String? = null,
        bioChannelId1: String? = null,
        bioChannelId2: String? = null,
        isTeacher: Boolean? = null,
        teachingField: String? = null,
        teachingUniversity: String? = null,
        province: String? = null,
        city: String? = null,
        faculty: String? = null,
        birthDate: String? = null
    ) {
        viewModelScope.launch {
            userRepository.updateProfile(
                username = username,
                displayName = displayName,
                firstName = firstName,
                lastName = lastName,
                nationalCode = nationalCode,
                educationalRole = educationalRole,
                gradeLevel = gradeLevel,
                major = major,
                bio = bio,
                university = university, 
                fieldOfStudy = fieldOfStudy, 
                education = education, 
                skills = skills, 
                interests = interests, 
                workExperience = workExperience, 
                achievements = achievements,
                bioChannelId1 = bioChannelId1, 
                bioChannelId2 = bioChannelId2,
                isTeacher = isTeacher, 
                teachingField = teachingField, 
                teachingUniversity = teachingUniversity,
                province = province, 
                city = city,
                faculty = faculty,
                birthDate = birthDate
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
    fun loadProvinces() {
        viewModelScope.launch {
            try {
                val response = apiService.getProvinces("ایران")
                if (response.isSuccessful && response.body()?.success == true) {
                    val data: List<String> = response.body()?.data ?: emptyList()
                    _state.update { it.copy(provinces = data) }
                }
            } catch (_: Exception) {
                // Silently fail, provinces will remain empty
            }
        }
    }
    fun loadCities(province: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getCities(province)
                if (response.isSuccessful && response.body()?.success == true) {
                    val data: List<String> = response.body()?.data ?: emptyList()
                    _state.update { it.copy(cities = data) }
                }
            } catch (_: Exception) {
                // Silently fail, cities will remain empty
            }
        }
    }
}
