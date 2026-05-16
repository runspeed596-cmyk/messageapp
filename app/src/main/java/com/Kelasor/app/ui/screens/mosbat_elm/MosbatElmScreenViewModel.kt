package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.CourseRepository
import com.Kelasor.app.domain.model.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.Kelasor.app.domain.mapper.toDomain
import com.Kelasor.app.data.remote.dto.*

data class MosbatElmState(
    val banners: List<BannerDto> = emptyList(),
    val categories: List<String> = emptyList(),
    val publicCourses: List<Course> = emptyList(),
    val topInstitutions: List<com.Kelasor.app.domain.model.Institution> = emptyList(), // Featured
    val popularInstitutions: List<com.Kelasor.app.domain.model.Institution> = emptyList(), // Popular
    val featuredCourses: List<Course> = emptyList(),
    val newCourses: List<Course> = emptyList(),
    val topInstructors: List<com.Kelasor.app.domain.model.User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOrganizer: Boolean = false,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val discountedCourses: List<Course> = emptyList(),
    val instituteCourses: List<Course> = emptyList(),
    val clubCourses: List<Course> = emptyList(),
    val associationCourses: List<Course> = emptyList(),
    val studentOrgCourses: List<Course> = emptyList(),
    val researchCenterCourses: List<Course> = emptyList()
)

@HiltViewModel
class MosbatElmScreenViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val apiService: com.Kelasor.app.data.remote.api.ApiService,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository,
    private val webSocketManager: com.Kelasor.app.data.websocket.WebSocketManager
) : ViewModel() {

    private val _state = MutableStateFlow(MosbatElmState())
    val state: StateFlow<MosbatElmState> = _state.asStateFlow()

    init {
        loadData()
        webSocketManager.subscribeToMosbatElmCapacityUpdates()
        observeWebSocketMessages()
    }

    private fun observeWebSocketMessages() {
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                if (message is com.Kelasor.app.data.websocket.WebSocketMessage.CourseCapacityUpdate) {
                    val courseId = message.courseId
                    val currentCount = message.currentEnrollment
                    val capacity = message.capacity
                    
                    _state.update { currentState ->
                        currentState.copy(
                            publicCourses = updateCourseCapacity(currentState.publicCourses, courseId, currentCount, capacity),
                            featuredCourses = updateCourseCapacity(currentState.featuredCourses, courseId, currentCount, capacity),
                            newCourses = updateCourseCapacity(currentState.newCourses, courseId, currentCount, capacity),
                            discountedCourses = updateCourseCapacity(currentState.discountedCourses, courseId, currentCount, capacity),
                            instituteCourses = updateCourseCapacity(currentState.instituteCourses, courseId, currentCount, capacity),
                            clubCourses = updateCourseCapacity(currentState.clubCourses, courseId, currentCount, capacity),
                            associationCourses = updateCourseCapacity(currentState.associationCourses, courseId, currentCount, capacity),
                            studentOrgCourses = updateCourseCapacity(currentState.studentOrgCourses, courseId, currentCount, capacity),
                            researchCenterCourses = updateCourseCapacity(currentState.researchCenterCourses, courseId, currentCount, capacity)
                        )
                    }
                }
            }
        }
    }

    private fun updateCourseCapacity(courses: List<Course>, courseId: String, currentCount: Int, capacity: Int): List<Course> {
        return courses.map { course ->
            if (course.id == courseId) {
                course.copy(enrollmentLimit = capacity, enrolledCount = currentCount)
            } else {
                course
            }
        }
    }

    fun loadData() {
        if (_state.value.publicCourses.isNotEmpty() || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val homeResponse = apiService.getMosbatElmHomeData()
                if (homeResponse.isSuccessful && homeResponse.body()?.success == true) {
                    val data = homeResponse.body()!!.data!!
                    _state.update { it.copy(
                        banners = data.banners,
                        categories = data.categories,
                        topInstitutions = data.featuredInstitutions.map { inst: InstitutionDto -> inst.toDomain() },
                        popularInstitutions = data.popularInstitutions.map { inst: InstitutionDto -> inst.toDomain() },
                        featuredCourses = data.upcomingCourses.map { c: CourseDto -> c.toDomain() },
                        topInstructors = data.popularTeachers.map { it.toDomain() },
                        isLoading = false
                    ) }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
        loadPublicCourses()
        checkOrganizerStatus()
    }

    fun loadPublicCourses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val courses: List<Course> = courseRepository.getPublicCourses()
                    .filter { it.status == "APPROVED" || it.status == "ACTIVE" }
                
                val discounted = courses.filter { (it.discountPercentage ?: 0) > 0 }
                val institutes = courses.filter { 
                    it.organizerType == "ACADEMY" || it.organizerType == "INSTITUTE" ||
                    it.organizerName?.contains("موسسه", ignoreCase = true) == true || 
                    it.organizerName?.contains("آکادمی", ignoreCase = true) == true ||
                    it.organizerName?.contains("آموزشی", ignoreCase = true) == true
                }
                val clubs = courses.filter { 
                    it.organizerType == "CLUB" ||
                    it.organizerName?.contains("کانون", ignoreCase = true) == true || 
                    it.scientificAssociationName?.contains("کانون", ignoreCase = true) == true
                }
                val associations = courses.filter { 
                    it.organizerType == "SCIENTIFIC_ASSOCIATION" ||
                    it.scientificAssociationName?.contains("انجمن", ignoreCase = true) == true || 
                    it.organizerName?.contains("انجمن", ignoreCase = true) == true ||
                    it.organizerName?.contains("علمی", ignoreCase = true) == true
                }

                val studentOrgs = courses.filter { 
                    it.organizerType == "STUDENT_ORG" ||
                    it.organizerName?.contains("تشکل", ignoreCase = true) == true ||
                    it.organizerName?.contains("دانشجویی", ignoreCase = true) == true
                }
                val researchCenters = courses.filter { 
                    it.organizerType == "RESEARCH_CENTER" || it.organizerType == "UNIVERSITY" ||
                    it.organizerName?.contains("تحقیقات", ignoreCase = true) == true ||
                    it.organizerName?.contains("پژوهش", ignoreCase = true) == true ||
                    it.organizerName?.contains("دانشگاه", ignoreCase = true) == true
                }

                _state.update { it.copy(
                    publicCourses = courses, 
                    discountedCourses = discounted,
                    instituteCourses = institutes,
                    clubCourses = clubs,
                    associationCourses = associations,
                    studentOrgCourses = studentOrgs,
                    researchCenterCourses = researchCenters,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }



    private fun checkOrganizerStatus() {
        viewModelScope.launch {
            try {
                userRepository.observeCurrentUser().collect { user ->
                    if (user != null) {
                        val isOrganizer = user.institutionId != null
                        _state.update { it.copy(isOrganizer = isOrganizer) }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onToggleSearch(active: Boolean) {
        _state.update { it.copy(isSearchActive = active) }
    }
}
