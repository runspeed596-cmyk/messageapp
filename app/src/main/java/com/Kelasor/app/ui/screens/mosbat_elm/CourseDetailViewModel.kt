package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.dto.CourseCommentDto
import com.Kelasor.app.data.repository.CourseRepository
import com.Kelasor.app.domain.model.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.Kelasor.app.data.repository.ChatRepository

data class CourseDetailState(
    val course: Course? = null,
    val similarCourses: List<Course> = emptyList(),
    val institutionCourses: List<Course> = emptyList(),
    val comments: List<CourseCommentDto> = emptyList(),
    val isEnrolled: Boolean = false,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isEnrolling: Boolean = false,
    val isSubmittingComment: Boolean = false,
    val isOwner: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val joinClassUrl: String? = null,
    val isJoiningClass: Boolean = false,
    val isCreatingKelasor: Boolean = false
)

sealed class CourseDetailEvent {
    data class NavigateToChat(val chatId: String) : CourseDetailEvent()
    data class OpenUrl(val url: String) : CourseDetailEvent()
}

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository,
    private val chatRepository: ChatRepository,
    private val webSocketManager: com.Kelasor.app.data.websocket.WebSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(CourseDetailState())
    val state: StateFlow<CourseDetailState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CourseDetailEvent>()
    val events: SharedFlow<CourseDetailEvent> = _events.asSharedFlow()

    private var currentCourseId: String? = null

    init {
        val courseId: String? = savedStateHandle["courseId"]
        if (!courseId.isNullOrEmpty()) {
            currentCourseId = courseId
            loadCourse(courseId)
            webSocketManager.subscribeToCourseCapacity(courseId)
            observeWebSocketMessages(courseId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentCourseId?.let {
            webSocketManager.unsubscribeFromCourseCapacity(it)
        }
    }

    private fun observeWebSocketMessages(courseId: String) {
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                if (message is com.Kelasor.app.data.websocket.WebSocketMessage.CourseCapacityUpdate && message.courseId == courseId) {
                    _state.update { currentState ->
                        val updatedCourse = currentState.course?.copy(
                            enrollmentLimit = message.capacity,
                            enrolledCount = message.currentEnrollment
                        )
                        currentState.copy(course = updatedCourse)
                    }
                }
            }
        }
    }

    private fun incrementCourseViewCount(courseId: String) {
        viewModelScope.launch {
            try {
                courseRepository.incrementViewCount(courseId)
            } catch (_: Exception) { }
        }
    }

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                java.util.UUID.fromString(courseId)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "شناسه دوره نامعتبر است (دوره آزمایشی یا ساختگی)") }
                return@launch
            }
            
            try {
                val courseResult = courseRepository.getCourseById(courseId)
                courseResult.fold(
                    onSuccess = { course ->
                        _state.update { it.copy(course = course, isLoading = false) }
                        checkOwnership(course)
                        loadSupplementaryData(course)
                        incrementCourseViewCount(course.id)
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isLoading = false, error = e.message ?: "خطا در بارگذاری دوره") }
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "خطا در اتصال: ${e.message}") }
            }
        }
    }

    private fun loadSupplementaryData(course: Course) {
        viewModelScope.launch {
            try {
                val isEnrolled: Boolean = courseRepository.isEnrolled(course.id)
                _state.update { it.copy(isEnrolled = isEnrolled) }
            } catch (_: Exception) { }
        }
        viewModelScope.launch {
            try {
                val isFavorite: Boolean = courseRepository.isFavorite(course.id)
                _state.update { it.copy(isFavorite = isFavorite) }
            } catch (_: Exception) { }
        }
        viewModelScope.launch {
            try {
                val similar: List<Course> = courseRepository.getSimilarCourses(course.id)
                _state.update { it.copy(similarCourses = similar) }
            } catch (_: Exception) { }
        }
        viewModelScope.launch {
            try {
                val comments: List<CourseCommentDto> = courseRepository.getComments(course.id)
                _state.update { it.copy(comments = comments) }
            } catch (_: Exception) { }
        }
        if (!course.institutionId.isNullOrEmpty()) {
            viewModelScope.launch {
                try {
                    val instCourses: List<Course> = courseRepository.getCoursesByInstitution(course.institutionId, 0, 10)
                    _state.update { it.copy(institutionCourses = instCourses.filter { c -> c.id != course.id }) }
                } catch (_: Exception) { }
            }
        }
    }

    fun toggleFavorite() {
        val courseId: String = _state.value.course?.id ?: return
        viewModelScope.launch {
            try {
                val result: Result<Boolean> = courseRepository.toggleFavorite(courseId)
                result.onSuccess { isFav ->
                    _state.update { state ->
                        val updatedCourse: Course? = state.course?.copy(
                            favoritesCount = if (isFav) state.course.favoritesCount + 1 else maxOf(0, state.course.favoritesCount - 1)
                        )
                        state.copy(isFavorite = isFav, course = updatedCourse)
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun enrollInCourse(paymentType: String) {
        val courseId: String = _state.value.course?.id ?: return
        if (_state.value.isEnrolled || _state.value.isEnrolling) return
        viewModelScope.launch {
            _state.update { it.copy(isEnrolling = true) }
            // National code check before enrollment
            try {
                val userResult = userRepository.getCurrentUser()
                    .filter { it !is com.Kelasor.app.data.repository.UserResult.Loading }
                    .first()
                if (userResult is com.Kelasor.app.data.repository.UserResult.Success) {
                    val nationalCode: String? = userResult.data.nationalCode
                    if (nationalCode.isNullOrBlank()) {
                        _state.update { it.copy(
                            isEnrolling = false,
                            error = "برای حفظ محتوای مجموعه برگزارکننده، اصالت‌سنجی افراد، جلوگیری از سوءاستفاده‌های احتمالی و صدور گواهی، ابتدا باید کد ملی خود را در پروفایل پیام‌رسان تکمیل نمایید."
                        ) }
                        return@launch
                    }
                }
            } catch (_: Exception) { }
            try {
                courseRepository.incrementClickCount(courseId)
            } catch (_: Exception) { }
            try {
                val result: Result<Unit> = courseRepository.enrollInCourse(courseId, paymentType)
                result.fold(
                    onSuccess = {
                        _state.update { state ->
                            val updatedCourse: Course? = state.course?.copy(enrolledCount = state.course.enrolledCount + 1)
                            state.copy(isEnrolled = true, isEnrolling = false, course = updatedCourse, error = "ENROLL_SUCCESS")
                        }
                        // Re-fetch course details from server to get authoritative enrolledCount/capacity
                        try {
                            val refreshResult = courseRepository.getCourseById(courseId)
                            refreshResult.onSuccess { freshCourse ->
                                _state.update { it.copy(course = freshCourse) }
                            }
                        } catch (_: Exception) { }
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isEnrolling = false, error = e.message) }
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(isEnrolling = false, error = e.message) }
            }
        }
    }

    fun addComment(content: String, rating: Int, replyToId: String? = null) {
        val courseId: String = _state.value.course?.id ?: return
        if (_state.value.isSubmittingComment) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmittingComment = true) }
            try {
                // Pass replyToId to the repository or backend if supported. For now we will assume the repository can handle it.
                // Assuming courseRepository.addComment does not have replyToId yet. Let's see if we need to modify CourseRepository.
                // We'll call the standard addComment if replyToId is missing, else call an overloaded one if available.
                // Since I don't know if repository supports it yet, I will use a placeholder or modify it.
                // For now let's just pass replyToId if the repository is modified, but I need to modify the repository first.
                // I will temporarily leave the repository call unchanged and fix it right after.
                val result = courseRepository.addComment(courseId, content, rating, replyToId)
                result.onSuccess { newComment ->
                    _state.update { state ->
                        val currentCourse = state.course ?: return@update state
                        val newReviewCount = currentCourse.reviewCount + 1
                        val newRating = if (currentCourse.reviewCount == 0) {
                            rating.toDouble()
                        } else {
                            (currentCourse.rating * currentCourse.reviewCount + rating) / newReviewCount
                        }
                        
                        state.copy(
                            course = currentCourse.copy(
                                rating = newRating,
                                reviewCount = newReviewCount
                            ),
                            comments = listOf(newComment) + state.comments,
                            isSubmittingComment = false
                        )
                    }
                }
                result.onFailure { e ->
                    _state.update { it.copy(isSubmittingComment = false, error = e.message) }
                }
            } catch (_: Exception) {
                _state.update { it.copy(isSubmittingComment = false) }
            }
        }
    }

    fun deleteCourse() {
        val courseId = _state.value.course?.id ?: return
        if (_state.value.isDeleting) return
        
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            val result = courseRepository.deleteCourse(courseId)
            result.onSuccess {
                _state.update { it.copy(isDeleting = false, error = "DELETED_SUCCESS") }
            }
            result.onFailure { e ->
                _state.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }

    fun startChatWithAdmin(adminId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = chatRepository.createChat(adminId)
            result.fold(
                onSuccess = { chat ->
                    _state.update { it.copy(isLoading = false) }
                    _events.emit(CourseDetailEvent.NavigateToChat(chat.id))
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = "خطا در ایجاد گفتگو: ${e.message}") }
                }
            )
        }
    }

    private fun checkOwnership(course: Course) {
        viewModelScope.launch {
            try {
                val userResult = userRepository.getCurrentUser()
                    .filter { it !is com.Kelasor.app.data.repository.UserResult.Loading }
                    .first()
                if (userResult is com.Kelasor.app.data.repository.UserResult.Success) {
                    val user = userResult.data
                    // A user is an owner if:
                    // 1. They are the creator of the course
                    // 2. They belong to the institution that organized the course AND are not a regular user
                    // 3. They are an admin of the platform
                    val isOwner = (course.managerId == user.id || 
                                  (course.institutionId != null && course.institutionId == user.institutionId && user.role != "NORMAL") ||
                                  user.role == "ADMIN")
                                  
                    _state.update { it.copy(isOwner = isOwner) }
                }
            } catch (_: Exception) { }
        }
    }

    fun joinOnlineClass() {
        val courseId = _state.value.course?.id ?: return
        if (_state.value.isJoiningClass) return
        
        viewModelScope.launch {
            _state.update { it.copy(isJoiningClass = true, error = null) }
            val result = courseRepository.getJoinClassUrl(courseId)
            result.fold(
                onSuccess = { url ->
                    _state.update { it.copy(isJoiningClass = false, joinClassUrl = url) }
                    _events.emit(CourseDetailEvent.OpenUrl(url))
                },
                onFailure = { e ->
                    _state.update { it.copy(isJoiningClass = false, error = e.message ?: "خطا در ورود به کلاس آنلاین") }
                }
            )
        }
    }

    fun createKelasorOnline() {
        val courseId = _state.value.course?.id ?: return
        if (_state.value.isCreatingKelasor) return
        viewModelScope.launch {
            _state.update { it.copy(isCreatingKelasor = true, error = null) }
            val result = courseRepository.createKelasorOnline(courseId)
            result.fold(
                onSuccess = { updatedCourse ->
                    _state.update { it.copy(isCreatingKelasor = false, course = updatedCourse, error = "KELASOR_CREATED") }
                },
                onFailure = { e ->
                    _state.update { it.copy(isCreatingKelasor = false, error = e.message ?: "خطا در ساخت کلاسور آنلاین") }
                }
            )
        }
    }
}

