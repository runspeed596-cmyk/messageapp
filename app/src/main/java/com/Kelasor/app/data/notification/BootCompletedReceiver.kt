package com.Kelasor.app.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Restarts the WebSocket notification worker after device reboot.
 * Ensures push notifications keep working even after a full device restart.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i(TAG, "📱 Device booted — restarting WebSocket notification service")
            WebSocketNotificationWorker.startService(context)
        }
    }
}
