package com.Kelasor.app.ui.screens.mosbat_elm

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.dto.InstitutionRegisterRequestDto
import com.Kelasor.app.data.repository.InstitutionRepository
import com.Kelasor.app.data.repository.ReferenceDataRepository
import com.Kelasor.app.data.repository.ReferenceDataResult
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.domain.model.ManualInstructor
import com.Kelasor.app.data.remote.dto.ManualInstructorDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class AcademyProfileSetupState(
    val name: String = "",
    val description: String = "",
    val logoUrl: String? = null,
    val localLogoUri: String? = null, // Temporary local URI for preview while uploading
    val isUploadingLogo: Boolean = false,
    val type: String = "ACADEMY",
    val isSubsidiary: Boolean = false,
    val dependencyDescription: String = "",
    val universities: List<String> = emptyList(),
    val specialties: List<String> = emptyList(),
    val associatedClubIds: List<String> = emptyList(),
    val associatedFieldOfStudyIds: List<String> = emptyList(),
    val associatedStudentOrgIds: List<String> = emptyList(),
    val instructors: List<User> = emptyList(),
    val manualInstructors: List<ManualInstructor> = emptyList(),
    val admins: List<User> = emptyList(),
    val allUniversities: List<String> = emptyList(),
    val allClubs: List<String> = emptyList(),
    val allStudentOrgs: List<String> = emptyList(),
    val allFieldsOfStudy: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false
)

sealed class AcademyProfileEvent {
    data object Success : AcademyProfileEvent()
    data class Error(val message: String) : AcademyProfileEvent()
}

@HiltViewModel
class AcademyProfileViewModel @Inject constructor(
    private val application: Application,
    private val institutionRepository: InstitutionRepository,
    private val userRepository: UserRepository,
    private val referenceDataRepository: ReferenceDataRepository
) : ViewModel() {

    var state by mutableStateOf(AcademyProfileSetupState())
        private set

    private val _events = MutableSharedFlow<AcademyProfileEvent>()
    val events = _events.asSharedFlow()

    private var existingInstitutionId: String? = null

    init {
        loadReferenceData()
        loadExistingInstitution()
    }

    private fun loadExistingInstitution() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().firstOrNull()?.institutionId?.let { id ->
                existingInstitutionId = id
                state = state.copy(isLoading = true)
                val inst = institutionRepository.getInstitution(id)
                if (inst != null) {
                    state = state.copy(
                        name = inst.name,
                        description = inst.description ?: "",
                        logoUrl = inst.logoUrl,
                        type = inst.type,
                        isSubsidiary = inst.isSubsidiary,
                        dependencyDescription = inst.dependencyDescription ?: "",
                        universities = inst.universities,
                        specialties = inst.specialties,
                        associatedClubIds = inst.associatedClubIds,
                        associatedFieldOfStudyIds = inst.associatedFieldOfStudyIds,
                        associatedStudentOrgIds = inst.associatedStudentOrgIds,
                        isLoading = false
                    )
                    // Optionally load instructors and admins profiles
                    if (inst.instructorIds.isNotEmpty()) {
                        userRepository.getUsersByIds(inst.instructorIds).collect { result ->
                            if (result is UserResult.Success) {
                                state = state.copy(instructors = result.data)
                            }
                        }
                    }
                    if (inst.manualInstructors.isNotEmpty()) {
                        state = state.copy(manualInstructors = inst.manualInstructors)
                    }
                    if (inst.adminIds.isNotEmpty()) {
                        userRepository.getUsersByIds(inst.adminIds).collect { result ->
                            if (result is UserResult.Success) {
                                state = state.copy(admins = result.data)
                            }
                        }
                    }
                } else {
                    state = state.copy(isLoading = false)
                }
            }
        }
    }

    private fun loadReferenceData() {
        viewModelScope.launch {
            referenceDataRepository.fetchReferenceData().collect { result ->
                if (result is ReferenceDataResult.Success) {
                    state = state.copy(
                        allUniversities = result.data.universities.map { it.name },
                        allFieldsOfStudy = result.data.fieldsOfStudy.map { it.name },
                        allClubs = result.data.clubs.map { it.name },
                        allStudentOrgs = result.data.studentOrgs.map { it.name }
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) { state = state.copy(name = name) }
    fun onDescriptionChange(desc: String) { state = state.copy(description = desc) }
    fun onTypeChange(type: String) { state = state.copy(type = type) }
    fun onIsSubsidiaryChange(isSubsidiary: Boolean) { state = state.copy(isSubsidiary = isSubsidiary) }
    fun onDependencyDescriptionChange(desc: String) { state = state.copy(dependencyDescription = desc) }

    /**
     * Handles logo selection: immediately uploads the image to the server
     * and stores the server URL. Shows a local preview during upload.
     */
    fun onLogoChange(contentUri: String) {
        // Show local preview immediately
        state = state.copy(localLogoUri = contentUri, isUploadingLogo = true)
        viewModelScope.launch {
            try {
                val uri: Uri = Uri.parse(contentUri)
                val file: File = copyUriToTempFile(uri) ?: run {
                    state = state.copy(isUploadingLogo = false, error = "خطا در خواندن فایل")
                    return@launch
                }
                val result: Result<String> = institutionRepository.uploadLogo(file)
                if (result.isSuccess) {
                    val serverUrl: String = result.getOrThrow()
                    state = state.copy(
                        logoUrl = serverUrl,
                        localLogoUri = null,
                        isUploadingLogo = false
                    )
                } else {
                    state = state.copy(
                        isUploadingLogo = false,
                        error = "خطا در آپلود لوگو: ${result.exceptionOrNull()?.message}"
                    )
                }
                // Clean up temp file
                file.delete()
            } catch (e: Exception) {
                state = state.copy(isUploadingLogo = false, error = "خطا: ${e.message}")
            }
        }
    }

    suspend fun uploadAvatar(file: File): Result<String> {
        return institutionRepository.uploadLogo(file)
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            val inputStream = application.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("institution_logo_", ".jpg", application.cacheDir)
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    fun addUniversity(uni: String) {
        if (uni !in state.universities) state = state.copy(universities = state.universities + uni)
    }
    fun removeUniversity(uni: String) {
        state = state.copy(universities = state.universities - uni)
    }

    fun addClub(club: String) {
        if (club !in state.associatedClubIds) state = state.copy(associatedClubIds = state.associatedClubIds + club)
    }
    fun removeClub(club: String) {
        state = state.copy(associatedClubIds = state.associatedClubIds - club)
    }

    fun addFieldOfStudy(field: String) {
        if (field !in state.associatedFieldOfStudyIds) state = state.copy(associatedFieldOfStudyIds = state.associatedFieldOfStudyIds + field)
    }
    fun removeFieldOfStudy(field: String) {
        state = state.copy(associatedFieldOfStudyIds = state.associatedFieldOfStudyIds - field)
    }

    fun addStudentOrg(org: String) {
        if (org !in state.associatedStudentOrgIds) state = state.copy(associatedStudentOrgIds = state.associatedStudentOrgIds + org)
    }
    fun removeStudentOrg(org: String) {
        state = state.copy(associatedStudentOrgIds = state.associatedStudentOrgIds - org)
    }

    fun addSpecialty(spec: String) {
        if (spec !in state.specialties) state = state.copy(specialties = state.specialties + spec)
    }
    fun removeSpecialty(spec: String) {
        state = state.copy(specialties = state.specialties - spec)
    }

    fun addInstructor(user: User) {
        if (user !in state.instructors) state = state.copy(instructors = state.instructors + user)
    }
    fun removeInstructor(user: User) {
        state = state.copy(instructors = state.instructors - user)
    }

    fun addManualInstructor(instructor: ManualInstructor) {
        if (instructor !in state.manualInstructors) state = state.copy(manualInstructors = state.manualInstructors + instructor)
    }
    fun removeManualInstructor(instructor: ManualInstructor) {
        state = state.copy(manualInstructors = state.manualInstructors - instructor)
    }

    fun addAdmin(user: User) {
        if (user !in state.admins) state = state.copy(admins = state.admins + user)
    }
    fun removeAdmin(user: User) {
        state = state.copy(admins = state.admins - user)
    }

    fun searchUsers(query: String) {
        if (query.length < 3) {
            state = state.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                when (result) {
                    is UserResult.Loading -> state = state.copy(isSearching = true)
                    is UserResult.Success -> state = state.copy(isSearching = false, searchResults = result.data)
                    is UserResult.Error -> state = state.copy(isSearching = false)
                }
            }
        }
    }

    fun submit() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val request = InstitutionRegisterRequestDto(
                name = state.name,
                type = state.type,
                logoUrl = state.logoUrl,
                description = state.description,
                isSubsidiary = state.isSubsidiary,
                dependencyDescription = state.dependencyDescription,
                universities = state.universities,
                specialties = state.specialties,
                associatedClubIds = state.associatedClubIds,
                associatedFieldOfStudyIds = state.associatedFieldOfStudyIds,
                associatedStudentOrgIds = state.associatedStudentOrgIds,
                instructorIds = state.instructors.map { it.id },
                manualInstructors = state.manualInstructors.map { 
                    ManualInstructorDto(
                        name = it.name,
                        resume = it.resume,
                        avatarUrl = it.avatarUrl
                    )
                },
                adminIds = state.admins.map { it.id }
            )
            val result: Result<*> = if (existingInstitutionId != null) {
                institutionRepository.updateInstitution(existingInstitutionId!!, request)
            } else {
                institutionRepository.registerInstitution(request)
            }
            if (result.isSuccess) {
                state = state.copy(isLoading = false)
                _events.emit(AcademyProfileEvent.Success)
                // Refresh current user to get the updated institutionLogoUrl
                userRepository.getCurrentUser(forceRefresh = true).collect()
            } else {
                state = state.copy(isLoading = false, error = result.exceptionOrNull()?.message)
                _events.emit(AcademyProfileEvent.Error(state.error ?: "خطا در ثبت اطلاعات"))
            }
        }
    }
}
