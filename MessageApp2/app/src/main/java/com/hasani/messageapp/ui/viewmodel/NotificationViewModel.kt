package com.hasani.messageapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasani.messageapp.data.notification.NotificationBadgeManager
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.remote.dto.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification ViewModel
// ═══════════════════════════════════════════════════════════════════════════════

data class NotificationState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = false,
    val unreadCount: Int = 0,
    val error: String? = null,
    val currentPage: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService,
    private val notificationBadgeManager: NotificationBadgeManager
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()
    private val pageSize = 20
    /**
     * Observable badge count from NotificationBadgeManager
     */
    val badgeCount: StateFlow<Int> = notificationBadgeManager.unreadCount
    init {
        // Observe badge count changes and update state
        viewModelScope.launch {
            notificationBadgeManager.unreadCount.collect { count ->
                _state.update { it.copy(unreadCount = it.unreadCount.coerceAtLeast(count)) }
            }
        }
    }
    fun loadNotifications() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getNotifications(page = 0, size = pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    _state.update {
                        it.copy(
                            notifications = body?.notifications ?: emptyList(),
                            hasMore = body?.hasMore ?: false,
                            unreadCount = body?.unreadCount ?: 0,
                            isLoading = false,
                            currentPage = 0
                        )
                    }
                    // Reset badge count when notifications are loaded (user is viewing)
                    notificationBadgeManager.resetCount()
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در دریافت اعلان‌ها") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun loadMore() {
        if (_state.value.isLoading || !_state.value.hasMore) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val nextPage = _state.value.currentPage + 1
                val response = apiService.getNotifications(page = nextPage, size = pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    _state.update {
                        it.copy(
                            notifications = it.notifications + (body?.notifications ?: emptyList()),
                            hasMore = body?.hasMore ?: false,
                            isLoading = false,
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
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.markNotificationAsRead(notificationId)
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map {
                                if (it.id == notificationId) it.copy(isRead = true) else it
                            },
                            unreadCount = (state.unreadCount - 1).coerceAtLeast(0)
                        )
                    }
                }
            } catch (e: Exception) {
                // Silent fail for mark as read
            }
        }
    }
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = apiService.markAllNotificationsAsRead()
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) },
                            unreadCount = 0
                        )
                    }
                    // Also reset badge manager
                    notificationBadgeManager.resetCount()
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    fun refreshUnreadCount() {
        viewModelScope.launch {
            try {
                val response = apiService.getUnreadNotificationCount()
                if (response.isSuccessful) {
                    val serverCount = response.body()?.unreadCount ?: 0
                    _state.update { it.copy(unreadCount = serverCount) }
                    // Sync badge manager with server count
                    notificationBadgeManager.setCount(serverCount)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }
    /**
     * Reset badge count when user views notifications
     */
    fun onNotificationsViewed() {
        notificationBadgeManager.resetCount()
    }
}

