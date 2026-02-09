package com.Kelasor.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.Kelasor.app.data.notification.WebSocketNotificationWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "📱 Device booted, scheduling WebSocket worker...")
            val request = OneTimeWorkRequestBuilder<WebSocketNotificationWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
