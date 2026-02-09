package com.Kelasor.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.dto.MovieDto
import com.Kelasor.app.data.remote.dto.MusicDto
import com.Kelasor.app.data.remote.dto.GameChallengeDto
import com.Kelasor.app.data.repository.EntertainmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.Kelasor.app.data.remote.dto.RiddleResultDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EntertainmentState(
    val movies: List<MovieDto> = emptyList(),
    val music: List<MusicDto> = emptyList(),
    val challenges: List<GameChallengeDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val riddleResult: RiddleResultDto? = null,
    val isSolvingRiddle: Boolean = false
)

@HiltViewModel
class EntertainmentViewModel @Inject constructor(
    private val repository: EntertainmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EntertainmentState())
    val state = _state.asStateFlow()

    init {
        loadEntertainment()
    }

    fun loadEntertainment() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getEntertainmentData()
                .onSuccess { data ->
                    _state.update { 
                        it.copy(
                            movies = data.movies,
                            music = data.music,
                            challenges = data.challenges,
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun refresh() {
        loadEntertainment()
    }

    fun solveRiddle(riddleId: String, answerIndex: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isSolvingRiddle = true, riddleResult = null) }
            repository.solveRiddle(riddleId, answerIndex)
                .onSuccess { result ->
                    _state.update { it.copy(isSolvingRiddle = false, riddleResult = result) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSolvingRiddle = false, error = error.message) }
                }
        }
    }

    fun clearRiddleResult() {
        _state.update { it.copy(riddleResult = null) }
    }
}
