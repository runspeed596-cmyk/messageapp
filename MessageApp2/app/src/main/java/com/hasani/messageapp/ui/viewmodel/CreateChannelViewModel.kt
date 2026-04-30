package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.repository.ChannelRepository
import com.hasani.messageapp.data.repository.ChannelResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateChannelUiState(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdChannelId: String? = null,
    val channelImageUri: android.net.Uri? = null,
    val publicId: String = "",
    val publicIdError: String? = null,
    val contacts: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.hasani.messageapp.domain.model.User> = emptyList(),
    val selectedMembers: Set<com.hasani.messageapp.domain.model.User> = emptySet()
)

@HiltViewModel
class CreateChannelViewModel @Inject constructor(
    private val channelRepository: com.hasani.messageapp.data.repository.ChannelRepository,
    private val userRepository: com.hasani.messageapp.data.repository.UserRepository,
    private val contactsRepository: com.hasani.messageapp.data.repository.ContactsRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _state = MutableStateFlow(CreateChannelUiState())
    val state: StateFlow<CreateChannelUiState> = _state.asStateFlow()

    private var phoneToNameMap: Map<String, String> = emptyMap()

    init {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val contacts = contactsRepository.getDeviceContacts()
                phoneToNameMap = contacts.associate { it.phoneNumber to it.name }
                loadContacts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun normalizePhoneNumber(number: String): String {
        var normalized = number.replace(Regex("[^0-9+]"), "")
        if (normalized.startsWith("+98")) {
            normalized = "0" + normalized.substring(3)
        }
        normalized = normalized.removePrefix("+")
        return normalized
    }

    private fun applyContactName(user: com.hasani.messageapp.domain.model.User): com.hasani.messageapp.domain.model.User {
        val normalized = normalizePhoneNumber(user.phoneNumber)
        val contactName = phoneToNameMap[normalized]
        return if (contactName != null) {
            user.copy(contactName = contactName)
        } else {
            user
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            userRepository.getContacts().collect { result ->
                if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    val mapped = result.data.map { applyContactName(it) }
                    _state.update { it.copy(contacts = mapped) }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            userRepository.searchUsers(query).collect { result ->
                 if (result is com.hasani.messageapp.data.repository.UserResult.Success) {
                    val mapped = result.data.map { applyContactName(it) }
                    _state.update { it.copy(searchResults = mapped) }
                }
            }
        }
    }

    fun toggleMemberSelection(user: com.hasani.messageapp.domain.model.User) {
        _state.update { 
            val currentSelected = it.selectedMembers.toMutableSet()
            if (currentSelected.any { u -> u.id == user.id }) {
                currentSelected.removeAll { u -> u.id == user.id }
            } else {
                currentSelected.add(user)
            }
            it.copy(selectedMembers = currentSelected)
        }
    }


    fun setName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun setDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun setIsPublic(isPublic: Boolean) {
        _state.update { it.copy(isPublic = isPublic) }
    }
    
    fun setPublicId(id: String) {
        // Basic validation: 5-32 chars, only a-z, 0-9, underscore
        val isValid = id.isEmpty() || id.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]{4,31}$"))
        val error = if (isValid) null else "شناسه باید ۵ تا ۳۲ کاراکتر و شامل حروف و اعداد باشد"
        _state.update { it.copy(publicId = id, publicIdError = error) }
    }

    fun setChannelImage(uri: android.net.Uri?) {
        _state.update { it.copy(channelImageUri = uri) }
    }

    private fun getFileFromUri(uri: android.net.Uri): java.io.File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = "temp_channel_avatar_${System.currentTimeMillis()}.jpg"
            val tempFile = java.io.File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                java.io.FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createChannel() {
        val currentState = _state.value
        if (currentState.name.isBlank()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val avatarFile = currentState.channelImageUri?.let { getFileFromUri(it) }
            
            channelRepository.createChannel(
                name = currentState.name,
                description = currentState.description.ifBlank { null },
                isPublic = currentState.isPublic,
                publicId = if (currentState.isPublic) currentState.publicId.ifBlank { null } else null,
                memberIds = currentState.selectedMembers.map { it.id },
                avatarFile = avatarFile
            ).collect { result ->
                when (result) {
                    is ChannelResult.Success -> {
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                createdChannelId = result.data.id
                            ) 
                        }
                    }
                    is ChannelResult.Error -> {
                        _state.update { 
                            it.copy(isLoading = false, error = result.message) 
                        }
                    }
                    is ChannelResult.Loading -> {}
                }
                
                if (result !is ChannelResult.Loading && avatarFile != null && avatarFile.exists()) {
                     avatarFile.delete()
                }
            }
        }
    }

    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
