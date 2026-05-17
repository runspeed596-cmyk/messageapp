package com.Kelasor.app.ui.screens.mosbat_elm

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.CourseDto
import com.Kelasor.app.data.remote.dto.UserDto
import com.Kelasor.app.domain.mapper.toDomain
import com.Kelasor.app.domain.model.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherPublicProfileState(
    val teacher: UserDto? = null,
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val followerCount: Int = 0
)

@HiltViewModel
class TeacherPublicProfileViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val teacherId: String = checkNotNull(savedStateHandle["teacherId"])

    private val _state = MutableStateFlow(TeacherPublicProfileState())
    val state: StateFlow<TeacherPublicProfileState> = _state.asStateFlow()

    init {
        loadProfileAndCourses()
    }

    private fun loadProfileAndCourses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch Teacher Profile
                val userResponse = apiService.getUserById(teacherId)
                if (!userResponse.isSuccessful || userResponse.body()?.success != true) {
                    throw Exception(userResponse.body()?.message ?: "Failed to fetch teacher profile")
                }
                val teacher = userResponse.body()?.data

                // Fetch Teacher Courses
                val coursesResponse = apiService.getTeacherCourses(teacherId, 0, 50)
                if (!coursesResponse.isSuccessful || coursesResponse.body()?.success != true) {
                    throw Exception(coursesResponse.body()?.message ?: "Failed to fetch courses")
                }
                val courses = coursesResponse.body()?.data?.content?.map { it.toDomain() } ?: emptyList()

                // Fetch follow counts
                var followerCount = 0
                try {
                    val countsResponse = apiService.getFollowCounts(teacherId)
                    if (countsResponse.isSuccessful) {
                        followerCount = countsResponse.body()?.followerCount ?: 0
                    }
                } catch (e: Exception) {
                    Log.e("TeacherPublicProfile", "Failed to fetch follow counts", e)
                }

                // Fetch is following status
                var isFollowing = false
                try {
                    val followResponse = apiService.isFollowing(teacherId)
                    if (followResponse.isSuccessful && followResponse.body()?.success == true) {
                        isFollowing = followResponse.body()?.data ?: false
                    }
                } catch (e: Exception) {
                    Log.e("TeacherPublicProfile", "Failed to check is following", e)
                }

                _state.update { 
                    it.copy(
                        teacher = teacher,
                        courses = courses,
                        isFollowing = isFollowing,
                        followerCount = followerCount,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                Log.e("TeacherPublicProfile", "Error loading teacher", e)
                _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری اطلاعات: ${e.message}") }
            }
        }
    }

    fun toggleFollow() {
        if (_state.value.isFollowLoading) return
        val currentlyFollowing = _state.value.isFollowing
        val currentFollowerCount = _state.value.followerCount
        
        // Optimistic UI Update: instantly flip the follow state & modify counter
        _state.update { currentState ->
            currentState.copy(
                isFollowing = !currentlyFollowing,
                followerCount = if (currentlyFollowing) maxOf(0, currentFollowerCount - 1) else currentFollowerCount + 1,
                isFollowLoading = true
            )
        }
        
        viewModelScope.launch {
            try {
                val response = if (currentlyFollowing) {
                    apiService.unfollowUser(teacherId)
                } else {
                    apiService.followUser(teacherId)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { it.copy(isFollowLoading = false) }
                } else {
                    // Revert on API response failure
                    _state.update { currentState ->
                        currentState.copy(
                            isFollowing = currentlyFollowing,
                            followerCount = currentFollowerCount,
                            isFollowLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // Revert on Exception
                _state.update { currentState ->
                    currentState.copy(
                        isFollowing = currentlyFollowing,
                        followerCount = currentFollowerCount,
                        isFollowLoading = false
                    )
                }
            }
        }
    }
}
