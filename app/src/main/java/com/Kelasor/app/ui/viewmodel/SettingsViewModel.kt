package com.Kelasor.app.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.CreateChatRequest
import com.Kelasor.app.data.remote.dto.UpdatePrivacyRequest
import com.Kelasor.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
    val bioVisibility: String = "everyone",
    val lastSeenVisibility: String = "everyone",
    // PIN Lock
    val isPinLockEnabled: Boolean = false,
    val pinCode: String? = null,
    // Color Palette
    val colorPalette: String = SettingsRepository.PALETTE_DEFAULT,
    // Chat Settings
    val chatWallpaperUri: String? = null,
    // Cache info
    val cacheSize: Long = 0L,
    val isClearingCache: Boolean = false,
    // Network usage
    val wifiSentData: Long = 0L,
    val wifiReceivedData: Long = 0L,
    val mobileSentData: Long = 0L,
    val mobileReceivedData: Long = 0L,
    // Auto-download settings
    val autoDownloadImages: String = "WiFi و دیتای موبایل",
    val autoDownloadVideos: String = "فقط WiFi",
    val autoDownloadFiles: String = "هیچ‌وقت"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()
    // Saved Messages navigation: emits the real chatId after resolving userId → self-chat
    private val _savedMessagesChatId = MutableSharedFlow<String>()
    val savedMessagesChatId: SharedFlow<String> = _savedMessagesChatId.asSharedFlow()
    /**
     * Resolve the saved messages (self-chat) real chatId by calling POST /api/chats.
     * This finds or creates the self-chat and emits the actual chatId for navigation.
     */
    fun resolveSavedMessagesChatId(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.createChat(CreateChatRequest(participantId = userId))
                if (response.isSuccessful && response.body()?.success == true) {
                    val realChatId: String = response.body()?.data?.id ?: userId
                    Log.d("SettingsVM", "Resolved self-chat: userId=$userId → chatId=$realChatId")
                    _savedMessagesChatId.emit(realChatId)
                } else {
                    Log.w("SettingsVM", "Failed to resolve self-chat, falling back to userId")
                    _savedMessagesChatId.emit(userId)
                }
            } catch (e: Exception) {
                Log.e("SettingsVM", "Error resolving self-chat", e)
                _savedMessagesChatId.emit(userId)
            }
        }
    }
    init {
        loadSettings()
        calculateCacheSize()
        calculateNetworkUsage()
    }
    
    private fun calculateNetworkUsage() {
        try {
            val uid = android.os.Process.myUid()
            val rxBytes = android.net.TrafficStats.getUidRxBytes(uid).coerceAtLeast(0)
            val txBytes = android.net.TrafficStats.getUidTxBytes(uid).coerceAtLeast(0)
            // TrafficStats without NetworkStatsManager doesn't reliably split Mobile/WiFi per UID.
            // We assign all current usage to WiFi for demonstration, or a split based on current network.
            // For now, we put the actual data usage into WiFi stats.
            _state.update { it.copy(
                wifiReceivedData = rxBytes,
                wifiSentData = txBytes,
                mobileReceivedData = 0L,
                mobileSentData = 0L
            ) }
        } catch (e: Exception) {
            Log.e("SettingsVM", "Error reading traffic stats", e)
        }
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
            launch {
                settingsRepository.bioVisibility.collect { visibility ->
                    _state.update { it.copy(bioVisibility = visibility) }
                }
            }
            launch {
                settingsRepository.lastSeenVisibility.collect { visibility ->
                    _state.update { it.copy(lastSeenVisibility = visibility) }
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
            // Chat Settings
            launch {
                settingsRepository.chatWallpaperUri.collect { uri ->
                    _state.update { it.copy(chatWallpaperUri = uri) }
                }
            }
            // Auto-Download Settings
            launch {
                settingsRepository.autoDownloadImages.collect { value ->
                    _state.update { it.copy(autoDownloadImages = value) }
                }
            }
            launch {
                settingsRepository.autoDownloadVideos.collect { value ->
                    _state.update { it.copy(autoDownloadVideos = value) }
                }
            }
            launch {
                settingsRepository.autoDownloadFiles.collect { value ->
                    _state.update { it.copy(autoDownloadFiles = value) }
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
        val localeList = androidx.core.os.LocaleListCompat.forLanguageTags(language)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
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
    fun setBioVisibility(visibility: String) {
        viewModelScope.launch {
            settingsRepository.setBioVisibility(visibility)
        }
    }
    fun setLastSeenVisibility(visibility: String) {
        viewModelScope.launch {
            settingsRepository.setLastSeenVisibility(visibility)
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
    fun setChatWallpaper(uri: String?) {
        viewModelScope.launch {
            settingsRepository.setChatWallpaperUri(uri)
        }
    }
    fun setNotifSound(channel: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifSound(channel, enabled)
        }
    }
    fun setNotifVibration(channel: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifVibration(channel, enabled)
        }
    }
    fun setNotifPopup(channel: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotifPopup(channel, enabled)
        }
    }
    fun calculateCacheSize() {
        viewModelScope.launch {
            try {
                val cacheDir: File = context.cacheDir
                val size: Long = calculateDirSize(cacheDir)
                _state.update { it.copy(cacheSize = size) }
            } catch (e: Exception) {
                Log.e("SettingsVM", "Error calculating cache", e)
            }
        }
    }
    fun clearCache() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingCache = true) }
            try {
                val cacheDir: File = context.cacheDir
                deleteDir(cacheDir)
                _state.update { it.copy(cacheSize = 0L, isClearingCache = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isClearingCache = false) }
            }
        }
    }
    private fun calculateDirSize(dir: File): Long {
        var size: Long = 0
        val files: Array<File>? = dir.listFiles()
        if (files != null) {
            for (file: File in files) {
                size += if (file.isDirectory) calculateDirSize(file) else file.length()
            }
        }
        return size
    }
    private fun deleteDir(dir: File) {
        val files: Array<File>? = dir.listFiles()
        if (files != null) {
            for (file: File in files) {
                if (file.isDirectory) deleteDir(file) else file.delete()
            }
        }
    }
    fun resetNetworkUsage() {
        _state.update { it.copy(
            wifiSentData = 0L, wifiReceivedData = 0L,
            mobileSentData = 0L, mobileReceivedData = 0L
        ) }
    }
    fun setAutoDownloadImages(value: String) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadImages(value)
        }
    }
    fun setAutoDownloadVideos(value: String) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadVideos(value)
        }
    }
    fun setAutoDownloadFiles(value: String) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadFiles(value)
        }
    }
}
