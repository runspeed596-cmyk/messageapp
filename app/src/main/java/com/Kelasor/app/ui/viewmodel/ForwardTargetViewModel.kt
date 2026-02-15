package com.Kelasor.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.local.dao.ChatDao
import com.Kelasor.app.data.local.dao.ChannelDao
import com.Kelasor.app.data.local.dao.GroupDao
import com.Kelasor.app.data.local.dao.UserDao
import com.Kelasor.app.data.local.entity.ChannelEntity
import com.Kelasor.app.data.local.entity.ChatWithParticipants
import com.Kelasor.app.data.local.entity.GroupEntity
import com.Kelasor.app.data.local.entity.UserEntity
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.CreateChatRequest
import com.Kelasor.app.data.remote.dto.ForwardMessageRequest
import com.Kelasor.app.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 🔀 Forward Target Types
// ═══════════════════════════════════════════════════════════════════════════════

sealed class ForwardTarget {
    data class ChatTarget(val chatId: String, val title: String, val avatarUrl: String?) : ForwardTarget()
    data class GroupTarget(val groupId: String, val name: String, val avatarUrl: String?) : ForwardTarget()
    data class ChannelTarget(val channelId: String, val name: String, val avatarUrl: String?) : ForwardTarget()
    data class ContactTarget(val userId: String, val name: String, val avatarUrl: String?) : ForwardTarget()
    val id: String get() = when (this) {
        is ChatTarget -> "chat_$chatId"
        is GroupTarget -> "group_$groupId"
        is ChannelTarget -> "channel_$channelId"
        is ContactTarget -> "contact_$userId"
    }
    val displayName: String get() = when (this) {
        is ChatTarget -> title
        is GroupTarget -> name
        is ChannelTarget -> name
        is ContactTarget -> name
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Forward Target State
// ═══════════════════════════════════════════════════════════════════════════════

data class ForwardTargetState(
    val chats: List<ChatWithParticipants> = emptyList(),
    val groups: List<GroupEntity> = emptyList(),
    val channels: List<ChannelEntity> = emptyList(),
    val contacts: List<UserEntity> = emptyList(),
    val selectedTargets: Set<ForwardTarget> = emptySet(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isForwarding: Boolean = false,
    val messageIds: List<String> = emptyList(),
    val sourceType: String = "CHAT" // CHAT, GROUP, CHANNEL
)

sealed class ForwardEvent {
    data object ForwardComplete : ForwardEvent()
    data class Error(val message: String) : ForwardEvent()
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎯 Forward Target ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class ForwardTargetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatDao: ChatDao,
    private val groupDao: GroupDao,
    private val channelDao: ChannelDao,
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : ViewModel() {
    companion object {
        private const val TAG = "ForwardTargetVM"
    }
    private val _state = MutableStateFlow(ForwardTargetState())
    val state: StateFlow<ForwardTargetState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<ForwardEvent>()
    val events: SharedFlow<ForwardEvent> = _events.asSharedFlow()
    init {
        val messageIdsRaw: String = savedStateHandle["messageIds"] ?: ""
        val sourceType: String = savedStateHandle["sourceType"] ?: "CHAT"
        val sourceId: String = savedStateHandle["sourceId"] ?: ""
        val parsedIds: List<String> = messageIdsRaw.split(",").filter { it.isNotBlank() }
        _state.update { it.copy(messageIds = parsedIds, sourceType = sourceType) }
        Log.d(TAG, "Init: messageIds=$parsedIds, sourceType=$sourceType, sourceId=$sourceId")
        loadTargets()
    }
    private fun loadTargets() {
        // Launch each flow independently - no blocking on sessionManager
        viewModelScope.launch {
            chatDao.observeAllChats().collect { chats ->
                Log.d(TAG, "Chats loaded: ${chats.size}")
                _state.update { it.copy(chats = chats, isLoading = false) }
            }
        }
        viewModelScope.launch {
            groupDao.observeAllGroups().collect { groups ->
                Log.d(TAG, "Groups loaded: ${groups.size}")
                _state.update { it.copy(groups = groups, isLoading = false) }
            }
        }
        viewModelScope.launch {
            channelDao.observeSubscribedChannels().collect { channels ->
                Log.d(TAG, "Channels loaded: ${channels.size}")
                _state.update { it.copy(channels = channels, isLoading = false) }
            }
        }
        viewModelScope.launch {
            userDao.observeAllUsers().collect { users ->
                val contacts = users.filter { !it.isCurrentUser }
                Log.d(TAG, "Users loaded: ${users.size}, contacts: ${contacts.size}")
                _state.update { it.copy(contacts = contacts, isLoading = false) }
            }
        }
    }
    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
    fun toggleTarget(target: ForwardTarget) {
        _state.update { currentState ->
            val current = currentState.selectedTargets.toMutableSet()
            val existing = current.find { it.id == target.id }
            if (existing != null) {
                current.remove(existing)
            } else {
                current.add(target)
            }
            currentState.copy(selectedTargets = current)
        }
    }
    fun isTargetSelected(targetId: String): Boolean {
        return _state.value.selectedTargets.any { it.id == targetId }
    }
    fun forwardMessages() {
        val currentState = _state.value
        if (currentState.selectedTargets.isEmpty() || currentState.messageIds.isEmpty()) return
        _state.update { it.copy(isForwarding = true) }
        viewModelScope.launch {
            try {
                var successCount = 0
                var failCount = 0
                for (target in currentState.selectedTargets) {
                    try {
                        val result: Boolean = forwardToTarget(currentState.messageIds, target)
                        if (result) successCount++ else failCount++
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to forward to ${target.id}", e)
                        failCount++
                    }
                }
                _state.update { it.copy(isForwarding = false) }
                if (failCount == 0) {
                    _events.emit(ForwardEvent.ForwardComplete)
                } else {
                    _events.emit(ForwardEvent.Error("$successCount ارسال شد، $failCount خطا"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "forwardMessages failed", e)
                _state.update { it.copy(isForwarding = false) }
                _events.emit(ForwardEvent.Error("خطا در ارسال مجدد پیام"))
            }
        }
    }
    private suspend fun forwardToTarget(
        messageIds: List<String>,
        target: ForwardTarget
    ): Boolean {
        val request: ForwardMessageRequest = when (target) {
            is ForwardTarget.ChatTarget -> ForwardMessageRequest(
                messageIds = messageIds,
                targetChatId = target.chatId,
                targetType = "CHAT"
            )
            is ForwardTarget.GroupTarget -> ForwardMessageRequest(
                messageIds = messageIds,
                targetGroupId = target.groupId,
                targetType = "GROUP"
            )
            is ForwardTarget.ChannelTarget -> ForwardMessageRequest(
                messageIds = messageIds,
                targetChannelId = target.channelId,
                targetType = "CHANNEL"
            )
            is ForwardTarget.ContactTarget -> {
                // Create chat first, then forward to it
                val chatResult = apiService.createChat(CreateChatRequest(target.userId))
                if (chatResult.isSuccessful && chatResult.body()?.data != null) {
                    val chatId = chatResult.body()!!.data!!.id
                    ForwardMessageRequest(
                        messageIds = messageIds,
                        targetChatId = chatId,
                        targetType = "CHAT"
                    )
                } else {
                    Log.e(TAG, "Failed to create chat for contact ${target.userId}")
                    return false
                }
            }
        }
        val response = apiService.forwardMessages(request)
        return response.isSuccessful && response.body()?.success == true
    }
    // ─── Filtered lists for search ─────────────────────────────────────────────
    fun filteredChats(): List<ChatWithParticipants> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) return _state.value.chats
        return _state.value.chats.filter {
            it.chat.title.lowercase().contains(query)
        }
    }
    fun filteredGroups(): List<GroupEntity> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) return _state.value.groups
        return _state.value.groups.filter {
            it.name.lowercase().contains(query)
        }
    }
    fun filteredChannels(): List<ChannelEntity> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) return _state.value.channels
        return _state.value.channels.filter {
            it.name.lowercase().contains(query)
        }
    }
    fun filteredContacts(): List<UserEntity> {
        val query = _state.value.searchQuery.lowercase()
        if (query.isBlank()) return _state.value.contacts
        return _state.value.contacts.filter {
            it.displayName.lowercase().contains(query) ||
            it.username.lowercase().contains(query)
        }
    }
}
