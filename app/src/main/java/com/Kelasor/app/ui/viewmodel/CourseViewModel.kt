package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.local.entity.CourseEntity
import com.Kelasor.app.data.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.File
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import com.Kelasor.app.domain.model.User

import com.Kelasor.app.data.repository.InstitutionRepository
import kotlinx.coroutines.flow.firstOrNull

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val institutionRepository: InstitutionRepository
) : ViewModel() {

    val courses = courseRepository.observeAllCourses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _createCourseState = MutableStateFlow<CreateCourseState>(CreateCourseState.Idle)
    val createCourseState: StateFlow<CreateCourseState> = _createCourseState.asStateFlow()

    fun createCourse(request: com.Kelasor.app.data.remote.dto.CreateCourseRequest) {
        viewModelScope.launch {
            _createCourseState.value = CreateCourseState.Loading
            android.util.Log.d("CourseViewModel", "Creating course: ${request.title}")
            val result = courseRepository.registerCourse(request)
            result.fold(
                onSuccess = { course ->
                    android.util.Log.d("CourseViewModel", "Course created success: ${course.id}")
                    _createCourseState.value = CreateCourseState.Success(course.id)
                },
                onFailure = { e: Throwable ->
                    android.util.Log.e("CourseViewModel", "Course creation failed", e)
                    _createCourseState.value = CreateCourseState.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    fun updateCourse(courseId: String, request: com.Kelasor.app.data.remote.dto.CreateCourseRequest) {
        viewModelScope.launch {
            _createCourseState.value = CreateCourseState.Loading
            android.util.Log.d("CourseViewModel", "Updating course: ${request.title}")
            val result = courseRepository.updateCourse(courseId, request)
            result.fold(
                onSuccess = { course ->
                    android.util.Log.d("CourseViewModel", "Course updated success")
                    _createCourseState.value = CreateCourseState.Success(course.id)
                },
                onFailure = { e: Throwable ->
                    android.util.Log.e("CourseViewModel", "Course update failed", e)
                    _createCourseState.value = CreateCourseState.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    private val _loadedCourse = MutableStateFlow<com.Kelasor.app.domain.model.Course?>(null)
    val loadedCourse = _loadedCourse.asStateFlow()

    fun loadCourseForEdit(courseId: String) {
        viewModelScope.launch {
            val result = courseRepository.getCourseById(courseId)
            result.fold(
                onSuccess = { course ->
                    _loadedCourse.value = course
                },
                onFailure = {
                    android.util.Log.e("CourseViewModel", "Failed to load course for edit")
                }
            )
        }
    }

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _academyInstructors = MutableStateFlow<List<User>>(emptyList())
    val academyInstructors = _academyInstructors.asStateFlow()

    private val _academyAdmins = MutableStateFlow<List<User>>(emptyList())
    val academyAdmins = _academyAdmins.asStateFlow()

    init {
        loadAcademyInstructors()
        loadAcademyAdmins()
    }

    private fun loadAcademyInstructors() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().firstOrNull()?.institutionId?.let { instId ->
                val inst = institutionRepository.getInstitution(instId)
                if (inst != null && inst.instructorIds.isNotEmpty()) {
                    userRepository.getUsersByIds(inst.instructorIds).collect { result ->
                        if (result is UserResult.Success) {
                            _academyInstructors.value = result.data
                        }
                    }
                }
            }
        }
    }

    private fun loadAcademyAdmins() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().firstOrNull()?.institutionId?.let { instId ->
                val inst = institutionRepository.getInstitution(instId)
                if (inst != null && inst.adminIds.isNotEmpty()) {
                    userRepository.getUsersByIds(inst.adminIds).collect { result ->
                        if (result is UserResult.Success) {
                            _academyAdmins.value = result.data
                        }
                    }
                }
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.length < 3) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                when (result) {
                    is UserResult.Loading -> _isSearching.value = true
                    is UserResult.Success -> {
                        _isSearching.value = false
                        _searchResults.value = result.data
                    }
                    is UserResult.Error -> _isSearching.value = false
                }
            }
        }
    }

    private val _activeInstitutions = MutableStateFlow<List<com.Kelasor.app.domain.model.Institution>>(emptyList())
    val activeInstitutions = _activeInstitutions.asStateFlow()

    fun searchInstitutions(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val result = institutionRepository.getActiveInstitutions(0, 100)
            result.fold(
                onSuccess = { institutions ->
                    val filtered = if (query.isBlank()) institutions else institutions.filter { it.name.contains(query, ignoreCase = true) }
                    _activeInstitutions.value = filtered
                    _isSearching.value = false
                },
                onFailure = {
                    _activeInstitutions.value = emptyList()
                    _isSearching.value = false
                }
            )
        }
    }

    fun requestCollaboration(courseId: String, institutionId: String, message: String? = null) {
        viewModelScope.launch {
            courseRepository.requestCollaboration(courseId, institutionId, message)
        }
    }

    suspend fun uploadPoster(file: File): Result<String> {
        return courseRepository.uploadPoster(file)
    }

    suspend fun uploadAvatar(file: File): Result<String> {
        return institutionRepository.uploadLogo(file)
    }

    fun resetCreateState() {
        _createCourseState.value = CreateCourseState.Idle
    }
}

sealed class CreateCourseState {
    object Idle : CreateCourseState()
    object Loading : CreateCourseState()
    data class Success(val courseId: String? = null) : CreateCourseState()
    data class Error(val message: String) : CreateCourseState()
}
