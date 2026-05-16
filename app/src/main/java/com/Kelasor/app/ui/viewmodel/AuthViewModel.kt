package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.AuthRepository
import com.Kelasor.app.data.repository.AuthResult
import com.Kelasor.app.data.repository.OtpResult
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean? = null, // Null means checking, False means not logged in, True means logged in
    val isAuthCheckComplete: Boolean = false, // True when DataStore has been read
    val isOnboardingComplete: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val otpSent: Boolean = false,
    val otpExpiresInSeconds: Int = 0,
    val isNewUser: Boolean = false
)

sealed class AuthEvent {
    data object OtpSent : AuthEvent()
    data class LoginSuccess(val isNewUser: Boolean) : AuthEvent()
    data object LogoutSuccess : AuthEvent()
    data class Error(val message: String) : AuthEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()
    
    val savedAccounts = authRepository.savedAccounts
    
    init {
        android.util.Log.d("AuthViewModel", "🚀 AuthViewModel initialized")
        viewModelScope.launch {
            authRepository.isLoggedIn.collect { isLoggedIn ->
                android.util.Log.d("AuthViewModel", "🔄 isLoggedIn changed to: $isLoggedIn")
                _state.update { it.copy(isLoggedIn = isLoggedIn, isAuthCheckComplete = true) }
            }
        }
        viewModelScope.launch {
            authRepository.isOnboardingComplete.collect { complete ->
                android.util.Log.d("AuthViewModel", "🔄 isOnboardingComplete changed to: $complete")
                _state.update { it.copy(isOnboardingComplete = complete) }
            }
        }
    }

    fun sendOtp(phoneNumber: String) {
        viewModelScope.launch {
            authRepository.sendOtp(phoneNumber).collect { result ->
                when (result) {
                    is OtpResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is OtpResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                otpSent = true,
                                otpExpiresInSeconds = result.expiresInSeconds
                            )
                        }
                        _events.emit(AuthEvent.OtpSent)
                    }
                    is OtpResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(AuthEvent.Error(result.message))
                    }
                }
            }
        }
    }

    fun verifyOtp(phoneNumber: String, code: String) {
        viewModelScope.launch {
            val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
            val osVersion = "Android ${android.os.Build.VERSION.RELEASE}"
            authRepository.verifyOtp(
                phoneNumber = phoneNumber,
                code = code,
                deviceName = deviceName,
                platform = "Android",
                osVersion = osVersion,
                appVersion = "1.0.0"
            ).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _state.update { it.copy(isLoading = true, error = null) }
                    }
                    is AuthResult.Success -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                currentUser = result.user,
                                isNewUser = result.isNewUser
                            )
                        }
                        _events.emit(AuthEvent.LoginSuccess(result.isNewUser))
                    }
                    is AuthResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(AuthEvent.Error(result.message))
                    }
                }
            }
        }
    }

    fun switchAccount(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val success = authRepository.switchAccount(userId)
            if (success) {
                _state.update { it.copy(isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false, error = "خطا در جابجایی حساب کاربری") }
            }
        }
    }

    fun logout(userId: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val success = authRepository.logout(userId)
            if (userId == null || userId == state.value.currentUser?.id) {
                // We logged out the active account. Let state reset based on flow.
                // The flow will emit isLoggedIn = false if no accounts are left.
                _state.update { AuthState(isLoggedIn = false, isAuthCheckComplete = true) }
                _events.emit(AuthEvent.LogoutSuccess)
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun setOnboardingComplete(complete: Boolean) {
        viewModelScope.launch {
            authRepository.setOnboardingComplete(complete)
        }
    }

    fun updateProfile(
        firstName: String, 
        lastName: String, 
        nationalCode: String? = null,
        educationalRole: String? = null,
        gradeLevel: String? = null,
        major: String? = null,
        username: String? = null, 
        bio: String? = null, 
        avatarFile: java.io.File? = null,
        university: String? = null,
        fieldOfStudy: String? = null,
        universities: List<String>? = null,
        fieldsOfStudy: List<String>? = null,
        isGraduated: Boolean? = null,
        education: String? = null,
        skills: String? = null,
        interests: String? = null,
        workExperience: String? = null,
        achievements: String? = null,
        faculty: String? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Upload avatar if exists
            if (avatarFile != null) {
                 userRepository.uploadAvatar(avatarFile).collect { result ->
                     if (result is com.Kelasor.app.data.repository.UserResult.Error) {
                         // We continue even if avatar upload fails
                     }
                 }
            }

            val displayName = if (lastName.isBlank()) firstName else "$firstName $lastName"
            userRepository.updateProfile(
                username = username,
                displayName = displayName,
                firstName = firstName,
                lastName = lastName,
                nationalCode = nationalCode,
                educationalRole = educationalRole,
                gradeLevel = gradeLevel,
                major = major,
                bio = bio,
                university = university, 
                fieldOfStudy = fieldOfStudy, 
                universities = universities,
                fieldsOfStudy = fieldsOfStudy,
                isGraduated = isGraduated,
                education = education, 
                skills = skills, 
                interests = interests, 
                workExperience = workExperience, 
                achievements = achievements,
                faculty = faculty
            ).collect { result ->
                when (result) {
                    is com.Kelasor.app.data.repository.UserResult.Success -> {
                        _state.update { it.copy(isLoading = false, currentUser = result.data) }
                        _events.emit(AuthEvent.LoginSuccess(isNewUser = false))
                    }
                    is com.Kelasor.app.data.repository.UserResult.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _events.emit(AuthEvent.Error(result.message))
                    }
                    else -> {}
                }
            }
        }
    }
}
