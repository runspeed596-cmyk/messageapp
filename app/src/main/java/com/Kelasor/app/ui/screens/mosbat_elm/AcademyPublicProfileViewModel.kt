package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.domain.model.Institution
import com.Kelasor.app.data.repository.InstitutionRepository
import com.Kelasor.app.data.repository.CourseRepository
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.data.repository.UserResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import com.Kelasor.app.domain.mapper.toDomain
import javax.inject.Inject

data class AcademyProfileState(
    val institution: Institution? = null,
    val courses: List<Course> = emptyList(),
    val instructors: List<User> = emptyList(),
    val admins: List<User> = emptyList(),
    val honors: List<com.Kelasor.app.domain.model.InstitutionHonor> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFollowing: Boolean = false,
    val isOwner: Boolean = false,
    val pendingCollaborations: List<com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto> = emptyList(),
    val calculatedScore: Double = 0.0,
    val calculatedRating: Double = 0.0
)

sealed class AcademyPublicProfileEvent {
    data class NavigateToChat(val chatId: String) : AcademyPublicProfileEvent()
    data class NavigateToUserProfile(val userId: String) : AcademyPublicProfileEvent()
    data class NavigateToEditProfile(val institutionId: String) : AcademyPublicProfileEvent()
}

@HiltViewModel
class AcademyPublicProfileViewModel @Inject constructor(
    private val institutionRepository: InstitutionRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository,
    private val chatRepository: com.Kelasor.app.data.repository.ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AcademyProfileState())
    val state: StateFlow<AcademyProfileState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AcademyPublicProfileEvent>()
    val events: SharedFlow<AcademyPublicProfileEvent> = _events.asSharedFlow()

    fun loadAcademyProfile(institutionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch Current User for ownership check
                var currentUserId: String? = null
                var currentUserInstitutionId: String? = null
                try {
                    val currentUserResult = userRepository.getCurrentUser()
                        .filter { it !is UserResult.Loading }
                        .first()
                    if (currentUserResult is UserResult.Success) {
                        currentUserId = currentUserResult.data.id
                        currentUserInstitutionId = currentUserResult.data.institutionId
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AcademyVM", "Failed to get current user", e)
                }

                // First try to fetch institution by institutionId directly
                var institution: Institution? = institutionRepository.getInstitution(institutionId)
                
                // If not found, maybe user navigated with ownerId - try fetching by owner
                if (institution == null && currentUserInstitutionId != null && institutionId == currentUserId) {
                    institution = institutionRepository.getInstitution(currentUserInstitutionId)
                }

                val isOwner = currentUserId != null && (
                    institution?.ownerId == currentUserId || 
                    currentUserInstitutionId == institutionId ||
                    currentUserInstitutionId == institution?.id
                )
                
                if (institution == null) {
                    _state.update { it.copy(isLoading = false, isOwner = isOwner, error = "اطلاعات آکادمی یافت نشد") }
                    return@launch
                }

                // Fetch Courses for this institution
                val coursesResponse: List<Course> = try {
                    if (isOwner) {
                        // Get all courses of the owner, filter for this institution
                        val myCourses = courseRepository.getMyCourses()
                        myCourses.filter { it.institutionId == institution.id || it.creatorId == institution.ownerId }
                    } else {
                        courseRepository.getCoursesByInstitution(institution.id, 0, 50)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AcademyVM", "Failed to load courses", e)
                    emptyList()
                }

                // Fetch Honors, Teachers, and Admins from new endpoints
                val honors = institutionRepository.getHonors(institution.id)
                val academyTeachers = institutionRepository.getTeachers(institution.id)
                val academyAdmins = institutionRepository.getAdmins(institution.id)
                
                // Merge with course instructors to ensure no one is missed
                val courseInstructors = coursesResponse.flatMap { it.instructors }.distinctBy { it.id }
                val finalInstructors = (courseInstructors + academyTeachers).distinctBy { it.id }
                
                // Fetch pending collaborations if owner
                var pendingCollabs: List<com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto> = emptyList()
                if (isOwner) {
                    try {
                        val result = courseRepository.getPendingCollaborations(institution.id)
                        if (result.isSuccess) {
                            pendingCollabs = result.getOrNull() ?: emptyList()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AcademyVM", "Failed to load pending collaborations", e)
                    }
                }
                
                // Calculate Dynamic Metrics
                val computedScore = (coursesResponse.size / 3.0) * 0.25
                val finalScore = if (computedScore > 5.0) 5.0 else computedScore
                
                val avgRating = if (coursesResponse.isNotEmpty()) {
                    coursesResponse.map { it.rating }.average()
                } else 0.0

                // Fetch following status
                var currentlyFollowing = false
                if (!isOwner) {
                    try {
                        val followResult = userRepository.isFollowing(institution.ownerId)
                            .filter { it !is UserResult.Loading }
                            .first()
                        if (followResult is UserResult.Success) {
                            currentlyFollowing = followResult.data
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("AcademyVM", "Failed to get follow status", e)
                    }
                }

                _state.update { 
                    it.copy(
                        institution = institution,
                        courses = coursesResponse,
                        instructors = finalInstructors,
                        admins = academyAdmins,
                        honors = honors.map { h -> h.toDomain() },
                        isOwner = isOwner,
                        pendingCollaborations = pendingCollabs,
                        calculatedScore = finalScore,
                        calculatedRating = avgRating,
                        isFollowing = currentlyFollowing,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademyVM", "Failed to load academy profile", e)
                _state.update { it.copy(isLoading = false, error = "خطا در اتصال: ${e.message}") }
            }
        }
    }

    fun toggleFollow() {
        val institution = _state.value.institution ?: return
        val currentlyFollowing = _state.value.isFollowing
        
        // Optimistic update
        _state.update { 
            it.copy(
                isFollowing = !currentlyFollowing,
                institution = it.institution?.copy(
                    followerCount = if (currentlyFollowing) maxOf(0, it.institution.followerCount - 1) else it.institution.followerCount + 1
                )
            )
        }

        viewModelScope.launch {
            try {
                if (currentlyFollowing) {
                    userRepository.unfollowUser(institution.ownerId)
                        .filter { it !is UserResult.Loading }
                        .first()
                        .let { result ->
                            if (result !is UserResult.Success) {
                                // Revert on failure
                                _state.update { 
                                    it.copy(
                                        isFollowing = true,
                                        institution = it.institution?.copy(followerCount = it.institution.followerCount + 1)
                                    ) 
                                }
                            }
                        }
                } else {
                    userRepository.followUser(institution.ownerId)
                        .filter { it !is UserResult.Loading }
                        .first()
                        .let { result ->
                            if (result !is UserResult.Success) {
                                // Revert on failure
                                _state.update { 
                                    it.copy(
                                        isFollowing = false,
                                        institution = it.institution?.copy(followerCount = maxOf(0, it.institution.followerCount - 1))
                                    ) 
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                // Revert on error
                _state.update { 
                    it.copy(
                        isFollowing = currentlyFollowing,
                        institution = it.institution?.copy(
                            followerCount = if (currentlyFollowing) it.institution.followerCount + 1 else maxOf(0, it.institution.followerCount - 1)
                        )
                    ) 
                }
            }
        }
    }

    fun acceptCollaboration(requestId: String, academyId: String) {
        viewModelScope.launch {
            try {
                val result = courseRepository.acceptCollaboration(requestId)
                if (result.isSuccess) {
                    loadAcademyProfile(academyId) // Reload
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademyVM", "Failed to accept collaboration", e)
            }
        }
    }

    fun rejectCollaboration(requestId: String, academyId: String) {
        viewModelScope.launch {
            try {
                val result = courseRepository.rejectCollaboration(requestId)
                if (result.isSuccess) {
                    loadAcademyProfile(academyId) // Reload
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademyVM", "Failed to reject collaboration", e)
            }
        }
    }
    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            try {
                val result: Result<Unit> = courseRepository.deleteCourse(courseId)
                if (result.isSuccess) {
                    _state.update { currentState ->
                        currentState.copy(courses = currentState.courses.filter { it.id != courseId })
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AcademyVM", "Failed to delete course", e)
            }
        }
    }
    
    fun startChatWithUser(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = chatRepository.createChat(userId)
            result.fold(
                onSuccess = { chat ->
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(AcademyPublicProfileEvent.NavigateToChat(chat.id))
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = "خطا در ایجاد گفتگو: ${e.message}") }
                }
            )
        }
    }
    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                userRepository.followUser(userId)
                    .filter { it !is UserResult.Loading }
                    .first()
                    .let { result ->
                        if (result is UserResult.Success) {
                            // Reload to reflect updated follow state
                            _state.value.institution?.id?.let { loadAcademyProfile(it) }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("AcademyVM", "Failed to follow user", e)
            }
        }
    }
    fun navigateToUserProfile(userId: String) {
        viewModelScope.launch {
            _events.emit(AcademyPublicProfileEvent.NavigateToUserProfile(userId))
        }
    }
    fun navigateToEditProfile() {
        val institutionId: String = _state.value.institution?.id ?: return
        viewModelScope.launch {
            _events.emit(AcademyPublicProfileEvent.NavigateToEditProfile(institutionId))
        }
    }
}
