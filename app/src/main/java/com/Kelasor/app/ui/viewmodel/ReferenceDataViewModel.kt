package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.dto.EducationLevelDto
import com.Kelasor.app.data.remote.dto.EducationalRoleOptionDto
import com.Kelasor.app.data.remote.dto.FacultyDto
import com.Kelasor.app.data.remote.dto.FieldOfStudyDto
import com.Kelasor.app.data.repository.ReferenceDataRepository
import com.Kelasor.app.data.repository.ReferenceDataResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReferenceDataState(
    val isLoading: Boolean = false,
    val educationLevels: List<EducationLevelDto> = emptyList(),
    val fieldsOfStudy: List<FieldOfStudyDto> = emptyList(),
    val faculties: List<FacultyDto> = emptyList(),
    val educationalRoles: List<EducationalRoleOptionDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReferenceDataViewModel @Inject constructor(
    private val referenceDataRepository: ReferenceDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReferenceDataState())
    val state: StateFlow<ReferenceDataState> = _state.asStateFlow()

    init {
        loadReferenceData()
    }

    fun loadReferenceData() {
        viewModelScope.launch {
            referenceDataRepository.fetchReferenceData().collect { result ->
                when (result) {
                    is ReferenceDataResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is ReferenceDataResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                educationLevels = result.data.educationLevels,
                                fieldsOfStudy = result.data.fieldsOfStudy,
                                faculties = result.data.faculties,
                                educationalRoles = result.data.educationalRoles
                            )
                        }
                    }
                    is ReferenceDataResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
}
