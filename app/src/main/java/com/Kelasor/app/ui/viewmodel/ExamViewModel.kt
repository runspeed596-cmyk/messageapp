package com.Kelasor.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// State Classes
// ═══════════════════════════════════════════════════════════════════════════════

data class ExamState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val exams: List<ExamDto> = emptyList(),
    val currentExam: ExamDto? = null,
    val questions: List<ExamQuestionDto> = emptyList(),
    val currentAttempt: ExamAttemptDto? = null,
    val examResult: ExamResultDto? = null,
    val myExams: List<ExamDto> = emptyList(),
    val attempts: List<ExamAttemptDto> = emptyList(),
    val createSuccess: Boolean = false,
    val activateSuccess: Boolean = false,
    val submitSuccess: Boolean = false,
    val remainingSeconds: Int = 0
)

sealed class ExamEvent {
    data class ShowError(val message: String) : ExamEvent()
    data object ExamCreated : ExamEvent()
    data object ExamActivated : ExamEvent()
    data object ExamSubmitted : ExamEvent()
    data object QuestionAdded : ExamEvent()
}

// ═══════════════════════════════════════════════════════════════════════════════
// ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ExamState())
    val state: StateFlow<ExamState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ExamEvent>()
    val events = _events.asSharedFlow()

    // ── Exam CRUD ──

    fun createExam(request: CreateExamRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.createExam(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val exam: ExamDto = response.body()!!.data!!
                    _state.update { it.copy(isLoading = false, currentExam = exam, createSuccess = true) }
                    _events.emit(ExamEvent.ExamCreated)
                } else {
                    val errorMsg: String = response.body()?.message ?: "خطا در ایجاد آزمون"
                    _state.update { it.copy(isLoading = false, error = errorMsg) }
                    _events.emit(ExamEvent.ShowError(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("ExamVM", "createExam error", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(ExamEvent.ShowError(e.message ?: "خطای شبکه"))
            }
        }
    }

    fun activateExam(examId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.activateExam(examId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { it.copy(isLoading = false, currentExam = response.body()!!.data, activateSuccess = true) }
                    _events.emit(ExamEvent.ExamActivated)
                } else {
                    _state.update { it.copy(isLoading = false, error = response.body()?.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadExam(examId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getExam(examId)
                if (response.isSuccessful) {
                    _state.update { it.copy(isLoading = false, currentExam = response.body()?.data) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Questions ──

    fun addQuestion(examId: String, request: AddQuestionRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.addExamQuestion(examId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val newQuestion: ExamQuestionDto = response.body()!!.data!!
                    _state.update { it.copy(isLoading = false, questions = it.questions + newQuestion) }
                    _events.emit(ExamEvent.QuestionAdded)
                } else {
                    _state.update { it.copy(isLoading = false, error = response.body()?.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteExamQuestion(questionId)
                if (response.isSuccessful) {
                    _state.update { it.copy(questions = it.questions.filter { q -> q.id != questionId }) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadQuestions(examId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getExamQuestions(examId)
                if (response.isSuccessful) {
                    _state.update { it.copy(questions = response.body()?.data ?: emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // ── Attempt Lifecycle ──

    fun startAttempt(examId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.startExamAttempt(examId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val attempt: ExamAttemptDto = response.body()!!.data!!
                    _state.update { it.copy(isLoading = false, currentAttempt = attempt) }
                    // Load questions
                    loadQuestions(examId)
                } else {
                    _state.update { it.copy(isLoading = false, error = response.body()?.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun submitAnswer(attemptId: String, request: SubmitAnswerRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.submitExamAnswer(attemptId, request)
                if (!response.isSuccessful) {
                    _state.update { it.copy(error = response.body()?.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun submitExam(attemptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.submitExam(attemptId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { it.copy(isLoading = false, submitSuccess = true, currentAttempt = response.body()!!.data) }
                    _events.emit(ExamEvent.ExamSubmitted)
                } else {
                    _state.update { it.copy(isLoading = false, error = response.body()?.message) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // ── Results & History ──

    fun loadExamResult(attemptId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getExamResults(attemptId)
                if (response.isSuccessful) {
                    _state.update { it.copy(isLoading = false, examResult = response.body()?.data) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadMyExams() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getMyExams()
                if (response.isSuccessful) {
                    val page = response.body()?.data
                    _state.update { it.copy(isLoading = false, myExams = page?.content ?: emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadChannelExams(channelId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getChannelExams(channelId)
                if (response.isSuccessful) {
                    val page = response.body()?.data
                    _state.update { it.copy(isLoading = false, exams = page?.content ?: emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun loadExamAttempts(examId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getExamAttempts(examId)
                if (response.isSuccessful) {
                    val page = response.body()?.data
                    _state.update { it.copy(attempts = page?.content ?: emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun resetState() {
        _state.update { ExamState() }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
