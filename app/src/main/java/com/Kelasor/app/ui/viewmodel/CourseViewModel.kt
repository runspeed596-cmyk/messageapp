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

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    val courses = courseRepository.observeAllCourses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _createCourseState = MutableStateFlow<CreateCourseState>(CreateCourseState.Idle)
    val createCourseState: StateFlow<CreateCourseState> = _createCourseState.asStateFlow()

    fun createCourse(title: String, description: String) {
        viewModelScope.launch {
            _createCourseState.value = CreateCourseState.Loading
            android.util.Log.d("CourseViewModel", "Creating course: $title")
            val result = courseRepository.createCourse(title, description)
            result.fold(
                onSuccess = {
                    android.util.Log.d("CourseViewModel", "Course created success")
                    _createCourseState.value = CreateCourseState.Success
                },
                onFailure = { e ->
                    android.util.Log.e("CourseViewModel", "Course creation failed", e)
                    _createCourseState.value = CreateCourseState.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    fun resetCreateState() {
        _createCourseState.value = CreateCourseState.Idle
    }
}

sealed class CreateCourseState {
    object Idle : CreateCourseState()
    object Loading : CreateCourseState()
    object Success : CreateCourseState()
    data class Error(val message: String) : CreateCourseState()
}
