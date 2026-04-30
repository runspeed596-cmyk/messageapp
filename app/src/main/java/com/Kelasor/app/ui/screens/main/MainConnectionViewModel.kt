package com.Kelasor.app.ui.screens.main

import androidx.lifecycle.ViewModel
import com.Kelasor.app.data.websocket.ConnectionState
import com.Kelasor.app.data.websocket.WebSocketManager
import com.Kelasor.app.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 Main Connection ViewModel — Exposes WebSocket + Network state for UI
// ═══════════════════════════════════════════════════════════════════════════════

@HiltViewModel
class MainConnectionViewModel @Inject constructor(
    webSocketManager: WebSocketManager,
    networkMonitor: NetworkMonitor
) : ViewModel() {
    val connectionState: StateFlow<ConnectionState> = webSocketManager.connectionState
    val isNetworkAvailable: StateFlow<Boolean> = networkMonitor.isNetworkAvailable
}
