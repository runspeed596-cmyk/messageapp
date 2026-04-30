package com.Kelasor.app.data.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.data.websocket.WebSocketManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 WebSocket Notification Worker
// Maintains WebSocket connection in background for real-time notifications
// ═══════════════════════════════════════════════════════════════════════════════

@HiltWorker
class WebSocketNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val webSocketManager: WebSocketManager,
    private val sessionManager: SessionManager
) : CoroutineWorker(context, workerParams) {
    companion object {
        private const val TAG = "WebSocketNotificationWorker"
        private const val WORK_NAME = "websocket_notification_sync"
        private const val NOTIFICATION_ID = 999
        /**
         * Start the persistent WebSocket sync service.
         * Using OneTimeWorkRequest with Expedited/LongRunning policy.
         */
        fun startService(context: Context) {
             val workRequest = androidx.work.OneTimeWorkRequest.Builder(WebSocketNotificationWorker::class.java)
                .setExpedited(androidx.work.OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                androidx.work.ExistingWorkPolicy.KEEP, // KEEP existing worker — never cancel a running worker
                workRequest
            )
            // Also schedule periodic fallback to restart if the foreground worker gets killed
            schedulePeriodicFallback(context)
            Log.i(TAG, "🚀 Started WebSocket foreground service + periodic fallback")
        }

        /**
         * Schedule periodic fallback every 15 minutes.
         * If the main foreground worker is killed by OS battery optimization,
         * this periodic worker will restart it.
         */
        fun schedulePeriodicFallback(context: Context) {
            val periodicWork = PeriodicWorkRequestBuilder<WebSocketNotificationWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${WORK_NAME}_periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
        }

        fun schedulePeriodicSync(context: Context) {
             startService(context)
        }
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "🔄 WebSocketNotificationWorker starting...")
        
        try {
            // Promote to foreground service
            setForeground(createForegroundInfo())
            Log.i(TAG, "✅ WebSocketNotificationWorker is now in FOREGROUND")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to set foreground: ${e.message}")
            return Result.failure()
        }

        return try {
            // Get access token
            val token = sessionManager.accessToken.first()
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ No access token available, finishing worker")
                return Result.success()
            }
            // NOTE: Do NOT call webSocketManager.connect() here.
            // GlobalSyncManager owns the WebSocket connection lifecycle.
            // This worker only keeps the process alive via foreground service.
            Log.i(TAG, "✅ WebSocket foreground service keeping process alive")
            // Keep the worker running indefinitely
            try {
                kotlinx.coroutines.awaitCancellation()
            } catch (e: kotlinx.coroutines.CancellationException) {
                // IMPORTANT: Do NOT call webSocketManager.disconnect() here!
                // GlobalSyncManager owns the WebSocket connection.
                // If this worker is cancelled, the connection should stay alive
                // as long as GlobalSyncManager is still running.
                Log.i(TAG, "🛑 Worker cancelled (process still alive, WebSocket untouched)")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ WebSocket sync error: ${e.message}", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(): androidx.work.ForegroundInfo {
        // Use silent background channel - this notification should be invisible
        val notification = androidx.core.app.NotificationCompat.Builder(applicationContext, com.Kelasor.app.data.notification.NotificationHelper.BACKGROUND_CHANNEL_ID)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(com.Kelasor.app.R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
            
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            androidx.work.ForegroundInfo(
                NOTIFICATION_ID, 
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            androidx.work.ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
