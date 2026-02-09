package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.UpdatePrivacyRequest
import com.Kelasor.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: String = SettingsRepository.THEME_MODE_SYSTEM,
    val language: String = SettingsRepository.LANGUAGE_PERSIAN,
    val isNotificationsEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isLoading: Boolean = true,
    // Privacy settings
    val profileVisibility: String = "everyone",
    val onlineVisibility: String = "everyone",
    val phoneVisibility: String = "contacts",
    // PIN Lock
    val isPinLockEnabled: Boolean = false,
    val pinCode: String? = null,
    // Color Palette
    val colorPalette: String = SettingsRepository.PALETTE_DEFAULT
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val apiService: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            launch {
                settingsRepository.themeMode.collect { themeMode ->
                    _state.update { it.copy(themeMode = themeMode, isLoading = false) }
                }
            }
            launch {
                settingsRepository.language.collect { language ->
                    _state.update { it.copy(language = language) }
                }
            }
            launch {
                settingsRepository.isNotificationsEnabled.collect { enabled ->
                    _state.update { it.copy(isNotificationsEnabled = enabled) }
                }
            }
            launch {
                settingsRepository.isSoundEnabled.collect { enabled ->
                    _state.update { it.copy(isSoundEnabled = enabled) }
                }
            }
            launch {
                settingsRepository.isVibrationEnabled.collect { enabled ->
                    _state.update { it.copy(isVibrationEnabled = enabled) }
                }
            }
            // Privacy settings
            launch {
                settingsRepository.profileVisibility.collect { visibility ->
                    _state.update { it.copy(profileVisibility = visibility) }
                }
            }
            launch {
                settingsRepository.onlineVisibility.collect { visibility ->
                    _state.update { it.copy(onlineVisibility = visibility) }
                }
            }
            launch {
                settingsRepository.phoneVisibility.collect { visibility ->
                    _state.update { it.copy(phoneVisibility = visibility) }
                }
            }
            // PIN Lock
            launch {
                settingsRepository.isPinLockEnabled.collect { enabled ->
                    _state.update { it.copy(isPinLockEnabled = enabled) }
                }
            }
            launch {
                settingsRepository.pinCode.collect { pin ->
                    _state.update { it.copy(pinCode = pin) }
                }
            }
            // Color Palette
            launch {
                settingsRepository.colorPalette.collect { palette ->
                    _state.update { it.copy(colorPalette = palette) }
                }
            }
        }
    }
    
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }
    
    fun setLanguage(language: String) {
        // Update App Locale directly using AppCompatDelegate
        // This handles persistence automatically and recreates the activity
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(language)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
        
        // Also update repository for local state consistency if needed, 
        // but rely on AppCompatDelegate for the actual switch.
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }
    
    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setVibrationEnabled(enabled)
        }
    }
    
    fun setProfileVisibility(visibility: String) {
        viewModelScope.launch {
            settingsRepository.setProfileVisibility(visibility)
            // Sync with backend
            try {
                apiService.updatePrivacy(UpdatePrivacyRequest(
                    profileVisibility = visibility.uppercase(),
                    onlineVisibility = null,
                    phoneVisibility = null
                ))
            } catch (e: Exception) {
                // Ignore network errors, local setting is still saved
            }
        }
    }
    
    fun setOnlineVisibility(visibility: String) {
        viewModelScope.launch {
            settingsRepository.setOnlineVisibility(visibility)
            // Sync with backend
            try {
                apiService.updatePrivacy(UpdatePrivacyRequest(
                    profileVisibility = null,
                    onlineVisibility = visibility.uppercase(),
                    phoneVisibility = null
                ))
            } catch (e: Exception) {
                // Ignore network errors, local setting is still saved
            }
        }
    }
    
    fun setPhoneVisibility(visibility: String) {
        viewModelScope.launch {
            settingsRepository.setPhoneVisibility(visibility)
            // Sync with backend
            try {
                apiService.updatePrivacy(UpdatePrivacyRequest(
                    profileVisibility = null,
                    onlineVisibility = null,
                    phoneVisibility = visibility.uppercase()
                ))
            } catch (e: Exception) {
                // Ignore network errors, local setting is still saved
            }
        }
    }
    
    fun setPinLockEnabled(enabled: Boolean, pin: String? = null) {
        viewModelScope.launch {
            if (enabled && pin != null && pin.length == 4) {
                settingsRepository.setPinCode(pin)
                settingsRepository.setPinLockEnabled(true)
            } else if (!enabled) {
                settingsRepository.setPinLockEnabled(false)
                settingsRepository.setPinCode(null)
            }
        }
    }
    
    fun setColorPalette(palette: String) {
        viewModelScope.launch {
            settingsRepository.setColorPalette(palette)
        }
    }
}
