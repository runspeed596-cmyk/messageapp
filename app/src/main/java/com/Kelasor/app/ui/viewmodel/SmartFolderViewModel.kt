package com.Kelasor.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.SmartFolderChannelDto
import com.Kelasor.app.data.remote.dto.SmartFolderDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.Kelasor.app.data.websocket.WebSocketManager
import com.Kelasor.app.data.websocket.WebSocketMessage

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class SmartFolderState(
    val isLoading: Boolean = false,
    val folders: List<SmartFolderDto> = emptyList(),
    val teacherChannels: List<SmartFolderChannelDto> = emptyList(),
    val elmClubChannels: List<SmartFolderChannelDto> = emptyList(),
    val courseChannels: List<SmartFolderChannelDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SmartFolderViewModel @Inject constructor(
    private val apiService: ApiService,
    private val webSocketManager: WebSocketManager
) : ViewModel() {
    private val _state: MutableStateFlow<SmartFolderState> = MutableStateFlow(SmartFolderState())
    val state: StateFlow<SmartFolderState> = _state.asStateFlow()
    init {
        Log.d("SmartFolderVM", "🔥 ViewModel created, calling loadSmartFolders()")
        loadSmartFolders()
        observeWebSockets()
    }

    private fun observeWebSockets() {
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                when (message) {
                    is WebSocketMessage.ChannelCreated,
                    is WebSocketMessage.GroupCreated,
                    is WebSocketMessage.GroupMemberUpdate,
                    is WebSocketMessage.ChannelPost,
                    is WebSocketMessage.GroupMessage -> {
                        // Refresh folders when relevant real-time events occur
                        loadSmartFolders()
                    }
                    else -> {} // Ignore other events
                }
            }
        }
    }
    fun loadSmartFolders() {
        viewModelScope.launch {
            Log.d("SmartFolderVM", "🚀 loadSmartFolders() started")
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getSmartFolders()
                Log.d("SmartFolderVM", "📡 API response: code=${response.code()}, success=${response.isSuccessful}")
                if (response.isSuccessful) {
                    val folders: List<SmartFolderDto> = response.body()?.data ?: emptyList()
                    Log.d("SmartFolderVM", "✅ Received ${folders.size} folders")
                    val teacherChannels: List<SmartFolderChannelDto> = folders
                        .firstOrNull { it.folderType == "TEACHERS" }?.channels ?: emptyList()
                    val elmClubChannels: List<SmartFolderChannelDto> = folders
                        .firstOrNull { it.folderType == "ELM_CLUB" }?.channels ?: emptyList()
                    val courseChannels: List<SmartFolderChannelDto> = folders
                        .firstOrNull { it.folderType == "COURSES" }?.channels ?: emptyList()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            folders = folders,
                            teacherChannels = teacherChannels,
                            elmClubChannels = elmClubChannels,
                            courseChannels = courseChannels
                        )
                    }
                } else {
                    Log.e("SmartFolderVM", "❌ API error: ${response.code()}")
                    _state.update { it.copy(isLoading = false, error = "خطا: ${response.code()}") }
                }
            } catch (e: Exception) {
                Log.e("SmartFolderVM", "💥 Exception in loadSmartFolders", e)
                _state.update { it.copy(isLoading = false, error = "خطا در اتصال: ${e.message}") }
            }
        }
    }
}
