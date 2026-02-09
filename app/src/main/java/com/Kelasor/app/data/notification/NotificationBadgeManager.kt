package com.Kelasor.app.data.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Badge Manager
// Singleton for tracking unread social notification count (follow, collaboration, etc.)
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class NotificationBadgeManager @Inject constructor() {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    /**
     * Increment unread notification count.
     * Called when new social notification arrives via WebSocket.
     */
    fun incrementCount() {
        _unreadCount.value++
    }
    /**
     * Add to unread notification count.
     * Used when loading initial count from server.
     */
    fun addCount(count: Int) {
        _unreadCount.value += count
    }
    /**
     * Set the unread notification count directly.
     * Used when loading initial count from server.
     */
    fun setCount(count: Int) {
        _unreadCount.value = count
    }
    /**
     * Reset unread notification count to 0.
     * Called when user views notification screen.
     */
    fun resetCount() {
        _unreadCount.value = 0
    }
    /**
     * Get current unread count synchronously.
     */
    fun getCurrentCount(): Int = _unreadCount.value
}
