package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackState(
    val title: String = "",
    val description: String = "",
    val rating: Int = 0,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedbackState())
    val state: StateFlow<FeedbackState> = _state.asStateFlow()

    fun onTitleChange(title: String) {
        _state.value = _state.value.copy(title = title, error = null)
    }

    fun onDescriptionChange(description: String) {
        _state.value = _state.value.copy(description = description, error = null)
    }

    fun onRatingChange(rating: Int) {
        _state.value = _state.value.copy(rating = rating, error = null)
    }

    fun submitFeedback() {
        val currentState = _state.value
        if (currentState.title.isBlank() || currentState.description.isBlank() || currentState.rating == 0) {
            _state.value = currentState.copy(error = "لطفاً تمامی فیلدها و امتیاز را تکمیل کنید.")
            return
        }

        viewModelScope.launch {
            userRepository.submitFeedback(
                title = currentState.title,
                description = currentState.description,
                rating = currentState.rating
            ).collect { result ->
                when (result) {
                    is UserResult.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is UserResult.Success -> {
                        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    }
                    is UserResult.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }
    
    fun resetSuccess() {
        _state.value = _state.value.copy(isSuccess = false, title = "", description = "", rating = 0)
    }
}
