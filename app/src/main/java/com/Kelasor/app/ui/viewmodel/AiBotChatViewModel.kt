package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.AiBotMessageDto
import com.Kelasor.app.data.remote.dto.SendAiBotMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiBotChatState(
    val messages: List<AiBotMessageDto> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiBotChatViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _state: MutableStateFlow<AiBotChatState> = MutableStateFlow(AiBotChatState())
    val state: StateFlow<AiBotChatState> = _state.asStateFlow()

    fun loadMessages(botId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getAiBotMessages(botId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val messages: List<AiBotMessageDto> = response.body()?.data ?: emptyList()
                    _state.update { it.copy(messages = messages, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری پیام‌ها") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "خطا در اتصال") }
            }
        }
    }

    fun sendMessage(botId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            try {
                val response = apiService.sendAiBotMessage(botId, SendAiBotMessageRequest(content))
                if (response.isSuccessful && response.body()?.success == true) {
                    val newMessages: List<AiBotMessageDto> = response.body()?.data ?: emptyList()
                    _state.update { currentState ->
                        currentState.copy(
                            messages = currentState.messages + newMessages,
                            isSending = false
                        )
                    }
                } else {
                    _state.update { it.copy(isSending = false, error = "خطا در ارسال پیام") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSending = false, error = "خطا در اتصال") }
            }
        }
    }
}
