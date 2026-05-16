package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ElmApiService
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.data.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ElmViewState(
    val isLoading: Boolean = false,
    val featuredEvents: List<ElmEventDto> = emptyList(),
    val competitions: List<ElmEventDto> = emptyList(),
    val startups: List<ElmEventDto> = emptyList(),
    val congresses: List<ElmEventDto> = emptyList(),
    val universities: List<com.Kelasor.app.data.University> = emptyList(),
    val publicCourses: List<com.Kelasor.app.domain.model.Course> = emptyList(),
    val error: String? = null,
    val submissionMessage: String? = null
)

@HiltViewModel
class ElmViewModel @Inject constructor(
    private val apiService: ElmApiService,
    private val courseRepository: com.Kelasor.app.data.repository.CourseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ElmViewState())
    val state = _state.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch events
                val response = apiService.getElmPeakData()
                
                // Fetch universities
                val unisResponse = apiService.getUniversities()
                val domainsUnis = unisResponse.map { it.toDomain() }
                
                _state.update {
                    it.copy(
                        isLoading = false,
                        featuredEvents = response.featuredEvents,
                        competitions = response.competitions,
                        startups = response.startups,
                        congresses = response.congresses,
                        universities = domainsUnis,
                        publicCourses = try { courseRepository.getPublicCourses() } catch (e: Exception) { emptyList() }
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun submitIdea(title: String, description: String, contactInfo: String) {
        viewModelScope.launch {
            try {
                val request = IdeaSubmissionRequest(title, description, contactInfo)
                val response = apiService.submitIdea(request)
                _state.update { it.copy(submissionMessage = response["message"]) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun reportEvent(title: String, description: String, date: String, location: String, link: String, type: ElmEventType) {
        viewModelScope.launch {
            try {
                val request = EventReportRequest(title, description, date, location, link, type)
                val response = apiService.reportEvent(request)
                _state.update { it.copy(submissionMessage = response["message"]) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun clearSubmissionMessage() {
        _state.update { it.copy(submissionMessage = null) }
    }
}
