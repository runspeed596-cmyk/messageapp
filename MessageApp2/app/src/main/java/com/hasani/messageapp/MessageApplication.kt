package com.hasani.messageapp

import android.app.Application
import com.hasani.messageapp.data.sync.GlobalSyncManager
import com.hasani.messageapp.data.sync.MessageSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Message App Application Class
// ═══════════════════════════════════════════════════════════════════════════════

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder

@HiltAndroidApp
class MessageApplication : Application(), SingletonImageLoader.Factory, androidx.work.Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var messageSyncManager: MessageSyncManager
    
    @Inject
    lateinit var globalSyncManager: GlobalSyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        android.util.Log.d("MessageApplication", "🚀 Application starting...")
        
        // Explicitly initialize WorkManager to ensure HiltWorkerFactory is used
        // This is a safeguard against default initialization if manifest removal fails
        try {
            androidx.work.WorkManager.initialize(this, workManagerConfiguration)
            android.util.Log.d("MessageApplication", "✅ WorkManager initialized with HiltWorkerFactory")
        } catch (e: Exception) {
            android.util.Log.w("MessageApplication", "⚠️ WorkManager already initialized: ${e.message}")
        }
        
        // Initialize the message sync manager for offline/retry logic
        // Handles syncing pending messages when connectivity is restored
        android.util.Log.d("MessageApplication", "📤 Initializing MessageSyncManager...")
        messageSyncManager.initialize()
        
        // Initialize the global sync manager for real-time WebSocket events
        // This ensures data is persisted regardless of which screen is active
        // Key component of "Database as Single Source of Truth" architecture
        android.util.Log.d("MessageApplication", "🌐 Initializing GlobalSyncManager...")
        globalSyncManager.initialize()
        
        // Schedule periodic WebSocket sync for background notifications
        android.util.Log.d("MessageApplication", "📡 Scheduling WebSocket notification sync...")
        com.hasani.messageapp.data.notification.WebSocketNotificationWorker.schedulePeriodicSync(this)
        
        android.util.Log.d("MessageApplication", "✅ Application started successfully")
    }

    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
