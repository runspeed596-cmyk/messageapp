package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.local.dao.UserDao
import com.Kelasor.app.data.local.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.Kelasor.app.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

data class ContactsSelectionState(
    val contacts: List<UserEntity> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedContacts: Set<String> = emptySet()
)

@HiltViewModel
class ContactsSelectionViewModel @Inject constructor(
    private val userDao: UserDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ContactsSelectionState())
    val state: StateFlow<ContactsSelectionState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            userDao.observeAllUsers().collect { users ->
                // Filter out the current user, keep only actual contacts
                val contacts = users.filter { !it.isCurrentUser }
                _state.update { it.copy(contacts = contacts, isLoading = false) }
            }
        }
    }

    fun loadSelection(type: String) {
        viewModelScope.launch {
            val savedSelection = if (type == "blocked_users") {
                settingsRepository.getBlockedUsers().first()
            } else {
                settingsRepository.getPrivacyExceptions(type).first()
            }
            _state.update { it.copy(selectedContacts = savedSelection) }
        }
    }

    fun saveSelection(type: String) {
        viewModelScope.launch {
            val currentSelected = _state.value.selectedContacts
            if (type == "blocked_users") {
                settingsRepository.setBlockedUsers(currentSelected)
            } else {
                settingsRepository.setPrivacyExceptions(type, currentSelected)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleContactSelection(userId: String) {
        _state.update { currentState ->
            val currentSelected = currentState.selectedContacts.toMutableSet()
            if (currentSelected.contains(userId)) {
                currentSelected.remove(userId)
            } else {
                currentSelected.add(userId)
            }
            currentState.copy(selectedContacts = currentSelected)
        }
    }

    fun removeContact(userId: String) {
        _state.update { currentState ->
            val currentSelected = currentState.selectedContacts.toMutableSet()
            currentSelected.remove(userId)
            currentState.copy(selectedContacts = currentSelected)
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
