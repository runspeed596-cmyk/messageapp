package com.Kelasor.app.data.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentChatManager @Inject constructor() {
    private val _currentChatId = MutableStateFlow<String?>(null)
    val currentChatId: StateFlow<String?> = _currentChatId.asStateFlow()

    fun setChat(chatId: String?) {
        _currentChatId.value = chatId
    }

    fun isChatOpen(chatId: String): Boolean {
        return _currentChatId.value == chatId
    }
}
