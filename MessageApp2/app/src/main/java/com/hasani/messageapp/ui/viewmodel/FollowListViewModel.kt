package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.remote.dto.FollowDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Follow List ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class FollowListState(
    val isLoading: Boolean = false,
    val users: List<FollowDto> = emptyList(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val hasMore: Boolean = false,
    val currentPage: Int = 0,
    val error: String? = null,
    val listType: ListType = ListType.FOLLOWERS
)

enum class ListType {
    FOLLOWERS, FOLLOWING
}

@HiltViewModel
class FollowListViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(FollowListState())
    val state: StateFlow<FollowListState> = _state.asStateFlow()
    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, listType = ListType.FOLLOWERS, currentPage = 0) }
            try {
                // Load counts first
                val countsResponse = apiService.getFollowCounts(userId)
                if (countsResponse.isSuccessful) {
                    val counts = countsResponse.body()
                    _state.update { 
                        it.copy(
                            followerCount = counts?.followerCount ?: 0,
                            followingCount = counts?.followingCount ?: 0
                        )
                    }
                }
                // Load followers list
                val response = apiService.getFollowers(userId, 0, 20)
                if (response.isSuccessful) {
                    val data = response.body()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            users = data?.users ?: emptyList(),
                            hasMore = data?.hasMore ?: false,
                            currentPage = 0
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, listType = ListType.FOLLOWING, currentPage = 0) }
            try {
                // Load counts first
                val countsResponse = apiService.getFollowCounts(userId)
                if (countsResponse.isSuccessful) {
                    val counts = countsResponse.body()
                    _state.update { 
                        it.copy(
                            followerCount = counts?.followerCount ?: 0,
                            followingCount = counts?.followingCount ?: 0
                        )
                    }
                }
                // Load following list
                val response = apiService.getFollowing(userId, 0, 20)
                if (response.isSuccessful) {
                    val data = response.body()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            users = data?.users ?: emptyList(),
                            hasMore = data?.hasMore ?: false,
                            currentPage = 0
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در بارگذاری") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun loadMoreFollowers(userId: String) {
        if (_state.value.isLoading || !_state.value.hasMore) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val nextPage = _state.value.currentPage + 1
                val response = apiService.getFollowers(userId, nextPage, 20)
                if (response.isSuccessful) {
                    val data = response.body()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            users = it.users + (data?.users ?: emptyList()),
                            hasMore = data?.hasMore ?: false,
                            currentPage = nextPage
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    fun loadMoreFollowing(userId: String) {
        if (_state.value.isLoading || !_state.value.hasMore) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val nextPage = _state.value.currentPage + 1
                val response = apiService.getFollowing(userId, nextPage, 20)
                if (response.isSuccessful) {
                    val data = response.body()
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            users = it.users + (data?.users ?: emptyList()),
                            hasMore = data?.hasMore ?: false,
                            currentPage = nextPage
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
