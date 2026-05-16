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
    val error: String? = null
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

                _state.update { 
                    it.copy(
                        teacher = teacher,
                        courses = courses,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                Log.e("TeacherPublicProfile", "Error loading teacher", e)
                _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری اطلاعات: ${e.message}") }
            }
        }
    }
}
