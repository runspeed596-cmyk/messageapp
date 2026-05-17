package com.Kelasor.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.AiBotDto
import com.Kelasor.app.data.remote.dto.SpecialChannelDto
import com.Kelasor.app.data.remote.dto.SpecialFolderDto
import com.Kelasor.app.data.remote.dto.SpecialGroupDto
import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// ⭐ Special Folder ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class SpecialFolderState(
    val isLoading: Boolean = false,
    val aiBots: List<AiBotDto> = emptyList(),
    val channels: List<SpecialChannelDto> = emptyList(),
    val groups: List<SpecialGroupDto> = emptyList(),
    val supportChannels: List<SpecialChannelDto> = emptyList(),
    val supportGroups: List<SpecialGroupDto> = emptyList(),
    val supportChatId: String? = null,
    val isProfileComplete: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SpecialFolderViewModel @Inject constructor(
    private val apiService: ApiService,
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _state: MutableStateFlow<SpecialFolderState> = MutableStateFlow(SpecialFolderState())
    val state: StateFlow<SpecialFolderState> = _state.asStateFlow()
    
    val isProfileBannerDismissed: StateFlow<Boolean> = settingsRepository.isProfileBannerDismissed
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun startChatWithBot(botUserId: String, onChatResolved: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = chatRepository.createChat(botUserId)
            _state.update { it.copy(isLoading = false) }
            result.fold(
                onSuccess = { chat ->
                    onChatResolved(chat.id)
                },
                onFailure = { e ->
                    _state.update { it.copy(error = "خطا در ارتباط با ربات: ${e.message}") }
                }
            )
        }
    }

    init {
        Log.d("SpecialFolderVM", "🔥 ViewModel created, calling loadSpecialFolder()")
        loadSpecialFolder()
    }
    fun loadSpecialFolder() {
        viewModelScope.launch {
            Log.d("SpecialFolderVM", "🚀 loadSpecialFolder() started")
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getSpecialFolder()
                Log.d("SpecialFolderVM", "📡 API response: code=${response.code()}, success=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val folderData: SpecialFolderDto? = response.body()?.data
                    if (folderData != null) {
                        Log.d("SpecialFolderVM", "✅ Data received: bots=${folderData.aiBots.size}, channels=${folderData.channels.size}, groups=${folderData.groups.size}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                aiBots = folderData.aiBots,
                                channels = folderData.channels,
                                groups = folderData.groups,
                                supportChannels = folderData.supportChannels,
                                supportGroups = folderData.supportGroups,
                                supportChatId = folderData.supportChatId,
                                isProfileComplete = folderData.isProfileComplete
                            )
                        }
                    } else {
                        Log.w("SpecialFolderVM", "⚠️ Response body data was null")
                        _state.update { it.copy(isLoading = false, error = "داده‌ای دریافت نشد") }
                    }
                } else {
                    Log.e("SpecialFolderVM", "❌ API error: ${response.code()} - ${response.errorBody()?.string()}")
                    _state.update { it.copy(isLoading = false, error = "خطا: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e("SpecialFolderVM", "💥 Exception in loadSpecialFolder", e)
                _state.update { it.copy(isLoading = false, error = "خطا در اتصال: ${e.message}") }
            }
        }
    }

    fun dismissProfileBanner() {
        viewModelScope.launch {
            settingsRepository.setProfileBannerDismissed(true)
        }
    }
}

