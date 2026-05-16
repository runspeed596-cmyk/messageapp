package com.Kelasor.app.util

/**
 * Environment switcher for Kelasor app.
 *
 * HOW TO SWITCH:
 *   - LOCAL development → set [isLocalMode] = true
 *   - SERVER production → set [isLocalMode] = false
 */
object Constants {
    // ═══════════════════════════════════════════════════════════════
    // 🔧 SWITCH: Change this ONE value to toggle Local ↔ Server
    // ═══════════════════════════════════════════════════════════════
    private const val isLocalMode: Boolean = false

    // ─── Endpoints ────────────────────────────────────────────────
    private const val LOCAL_IP: String = "192.168.70.113"
    private const val LOCAL_BASE: String = "http://$LOCAL_IP:8080/"
    private const val LOCAL_WS: String = "ws://$LOCAL_IP:8080/ws"

    private const val SERVER_DOMAIN: String = "kelasorapp.ir"
    private const val SERVER_BASE: String = "https://$SERVER_DOMAIN/"
    private const val SERVER_WS: String = "wss://$SERVER_DOMAIN/ws"

    // ─── Public Constants (auto-switch) ───────────────────────────
    val BASE_URL: String = if (isLocalMode) LOCAL_BASE else SERVER_BASE
    val WS_URL: String = if (isLocalMode) LOCAL_WS else SERVER_WS
}
