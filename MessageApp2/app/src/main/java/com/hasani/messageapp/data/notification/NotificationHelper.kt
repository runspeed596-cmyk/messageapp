package com.hasani.messageapp.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hasani.messageapp.MainActivity
import com.hasani.messageapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // Message notification channel
        const val MESSAGE_CHANNEL_ID = "message_channel"
        const val MESSAGE_CHANNEL_NAME = "پیام‌ها"
        const val MESSAGE_CHANNEL_DESCRIPTION = "اعلان پیام‌های جدید"
        // Social notification channel (follow, collaboration, etc.)
        const val SOCIAL_CHANNEL_ID = "social_channel"
        const val SOCIAL_CHANNEL_NAME = "فعالیت اجتماعی"
        const val SOCIAL_CHANNEL_DESCRIPTION = "اعلان فالو، درخواست همکاری و..."
        // Background service channel (silent, hidden)
        const val BACKGROUND_CHANNEL_ID = "background_service_channel"
        const val BACKGROUND_CHANNEL_NAME = "سرویس پس‌زمینه"
        const val BACKGROUND_CHANNEL_DESCRIPTION = "اتصال به سرور برای دریافت پیام"
        // Legacy channel ID for backward compatibility
        const val CHANNEL_ID = MESSAGE_CHANNEL_ID
        const val CHANNEL_NAME = MESSAGE_CHANNEL_NAME
        const val CHANNEL_DESCRIPTION = MESSAGE_CHANNEL_DESCRIPTION
    }
    init {
        createNotificationChannels()
    }
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Message channel - high priority for instant messages
            val messageChannel = NotificationChannel(
                MESSAGE_CHANNEL_ID,
                MESSAGE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = MESSAGE_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(messageChannel)
            // Social channel - default priority for social activities
            val socialChannel = NotificationChannel(
                SOCIAL_CHANNEL_ID,
                SOCIAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = SOCIAL_CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(socialChannel)
            // Background service channel - completely silent and hidden
            val backgroundChannel = NotificationChannel(
                BACKGROUND_CHANNEL_ID,
                BACKGROUND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN // Lowest priority, no sound, no popup
            ).apply {
                description = BACKGROUND_CHANNEL_DESCRIPTION
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(backgroundChannel)
        }
    }
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not needed for older versions
        }
    }
    /**
     * Show message notification (private chat, group, channel)
     */
    fun showNotification(
        id: Int,
        title: String,
        message: String,
        chatId: String? = null
    ) {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (chatId != null) {
                putExtra("chat_id", chatId)
            }
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
    /**
     * Show follow notification
     */
    fun showFollowNotification(
        id: Int,
        followerName: String,
        followerAvatarUrl: String? = null,
        userId: String? = null
    ) {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "notifications")
            userId?.let { putExtra("user_id", it) }
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, SOCIAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("دنبال‌کننده جدید")
            .setContentText("$followerName شما را دنبال کرد")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
    /**
     * Show collaboration request notification
     */
    fun showCollaborationNotification(
        id: Int,
        senderName: String,
        requestTitle: String,
        requestId: String? = null
    ) {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "notifications")
            requestId?.let { putExtra("request_id", it) }
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, SOCIAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("درخواست همکاری")
            .setContentText("$senderName: $requestTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
    /**
     * Show generic social notification
     */
    fun showSocialNotification(
        id: Int,
        title: String,
        message: String,
        navigateTo: String = "notifications"
    ) {
        if (!hasNotificationPermission()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", navigateTo)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = NotificationCompat.Builder(context, SOCIAL_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }
    }
}

