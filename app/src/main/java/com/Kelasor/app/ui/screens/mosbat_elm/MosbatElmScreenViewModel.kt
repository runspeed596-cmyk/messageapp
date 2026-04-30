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
    val myCourses: List<Course> = emptyList(),
    val topInstitutions: List<com.Kelasor.app.domain.model.Institution> = emptyList(),
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
    val associationCourses: List<Course> = emptyList()
)

@HiltViewModel
class MosbatElmScreenViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val apiService: com.Kelasor.app.data.remote.api.ApiService,
    private val userRepository: com.Kelasor.app.data.repository.UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MosbatElmState())
    val state: StateFlow<MosbatElmState> = _state.asStateFlow()

    init {
        loadData()
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
                        topInstitutions = data.featuredInstitutions.map { inst: InstitutionDto -> inst.toDomain() }.sortedByDescending { it.rating },
                        featuredCourses = data.upcomingCourses.map { c: CourseDto -> c.toDomain() },
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
        loadMyCourses()
        checkOrganizerStatus()
    }

    fun loadPublicCourses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val courses: List<Course> = courseRepository.getPublicCourses()
                
                val discounted = courses.filter { (it.discountPercentage ?: 0) > 0 }
                val institutes = courses.filter { 
                    it.organizerName?.contains("موسسه") == true || 
                    it.organizerName?.contains("آکادمی") == true ||
                    it.tags.any { tag -> tag.contains("موسسه") || tag.contains("آکادمی") }
                }
                val clubs = courses.filter { 
                    it.organizerName?.contains("کانون") == true || 
                    it.scientificAssociationName?.contains("کانون") == true ||
                    it.tags.any { tag -> tag.contains("کانون") }
                }
                val associations = courses.filter { 
                    it.scientificAssociationName?.contains("انجمن") == true || 
                    it.organizerName?.contains("انجمن") == true ||
                    it.tags.any { tag -> tag.contains("انجمن") }
                }

                _state.update { it.copy(
                    publicCourses = courses, 
                    discountedCourses = discounted,
                    instituteCourses = institutes,
                    clubCourses = clubs,
                    associationCourses = associations,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadMyCourses() {
        viewModelScope.launch {
            try {
                val courses: List<Course> = courseRepository.getMyCourses()
                _state.update { it.copy(myCourses = courses) }
            } catch (_: Exception) { }
        }
    }

    private fun checkOrganizerStatus() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser(forceRefresh = true).collect { result ->
                    if (result is com.Kelasor.app.data.repository.UserResult.Success) {
                        val user = result.data
                        val isOrganizer = user.institutionId != null
                        _state.update { it.copy(isOrganizer = isOrganizer) }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadTopInstitutions() {
        viewModelScope.launch {
            try {
                val response = apiService.getActiveInstitutions(0, 10)
                if (response.isSuccessful && response.body()?.success == true) {
                    val institutions: List<com.Kelasor.app.domain.model.Institution> = response.body()?.data?.content?.map { dto: InstitutionDto -> dto.toDomain() } ?: emptyList()
                    _state.update { it.copy(topInstitutions = institutions) }
                }
            } catch (e: Exception) {
                // Ignore error, keep existing data
            }
        }
    }

    private fun loadFeaturedCourses() {
        viewModelScope.launch {
            try {
                val courses = courseRepository.getPublicCourses(0, 10)
                val featured = courses.sortedByDescending { it.favoritesCount }.take(5)
                val newCourses = courses.sortedByDescending { it.createdAt }.take(5)
                _state.update { it.copy(featuredCourses = featured, newCourses = newCourses) }
            } catch (e: Exception) {
                // Ignore error
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
    
    fun onToggleSearch(active: Boolean) {
        _state.update { it.copy(isSearchActive = active) }
    }
}
