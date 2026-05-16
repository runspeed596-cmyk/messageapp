package com.Kelasor.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing app settings using DataStore.
 * Handles dark mode, language, notifications, and other user preferences.
 */
@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        
        // Privacy settings keys
        private val PROFILE_VISIBILITY_KEY = stringPreferencesKey("profile_visibility")
        private val ONLINE_VISIBILITY_KEY = stringPreferencesKey("online_visibility")
        private val PHONE_VISIBILITY_KEY = stringPreferencesKey("phone_visibility")
        private val BIO_VISIBILITY_KEY = stringPreferencesKey("bio_visibility")
        private val LAST_SEEN_VISIBILITY_KEY = stringPreferencesKey("last_seen_visibility")
        
        // PIN Lock settings
        private val PIN_LOCK_ENABLED_KEY = booleanPreferencesKey("pin_lock_enabled")
        private val PIN_CODE_KEY = stringPreferencesKey("pin_code")
        // Auto-Download settings
        private val AUTO_DOWNLOAD_IMAGES_KEY = stringPreferencesKey("auto_download_images")
        private val AUTO_DOWNLOAD_VIDEOS_KEY = stringPreferencesKey("auto_download_videos")
        private val AUTO_DOWNLOAD_FILES_KEY = stringPreferencesKey("auto_download_files")
        
        // Profile Banner State
        private val IS_PROFILE_BANNER_DISMISSED_KEY = booleanPreferencesKey("is_profile_banner_dismissed")
        
        // Blocked & Privacy Exceptions
        private val BLOCKED_USERS_KEY = stringSetPreferencesKey("blocked_users")
        private fun privacyExceptionKey(type: String) = stringSetPreferencesKey("privacy_exceptions_$type")
        
        // Constants for default valuestte settings
        private val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
        
        // Chat Settings
        private val CHAT_WALLPAPER_URI_KEY = stringPreferencesKey("chat_wallpaper_uri")
        
        // Notification channel settings
        private val NOTIF_PERSONAL_SOUND_KEY = booleanPreferencesKey("notif_personal_sound")
        private val NOTIF_PERSONAL_VIBRATION_KEY = booleanPreferencesKey("notif_personal_vibration")
        private val NOTIF_PERSONAL_POPUP_KEY = booleanPreferencesKey("notif_personal_popup")
        private val NOTIF_GROUP_SOUND_KEY = booleanPreferencesKey("notif_group_sound")
        private val NOTIF_GROUP_VIBRATION_KEY = booleanPreferencesKey("notif_group_vibration")
        private val NOTIF_GROUP_POPUP_KEY = booleanPreferencesKey("notif_group_popup")
        private val NOTIF_CHANNEL_SOUND_KEY = booleanPreferencesKey("notif_channel_sound")
        private val NOTIF_CHANNEL_VIBRATION_KEY = booleanPreferencesKey("notif_channel_vibration")
        private val NOTIF_CHANNEL_POPUP_KEY = booleanPreferencesKey("notif_channel_popup")
        private val NOTIF_BOT_SOUND_KEY = booleanPreferencesKey("notif_bot_sound")
        private val NOTIF_BOT_VIBRATION_KEY = booleanPreferencesKey("notif_bot_vibration")
        private val NOTIF_BOT_POPUP_KEY = booleanPreferencesKey("notif_bot_popup")
        
        // Language constants
        const val LANGUAGE_PERSIAN = "fa"
        const val LANGUAGE_ENGLISH = "en"
        
        // Theme mode constants
        const val THEME_MODE_LIGHT = "light"
        const val THEME_MODE_DARK = "dark"
        const val THEME_MODE_SYSTEM = "system"
        
        // Privacy visibility options
        const val VISIBILITY_EVERYONE = "everyone"
        const val VISIBILITY_CONTACTS = "contacts"
        const val VISIBILITY_CONTACTS_EXCEPT = "contacts_except"
        const val VISIBILITY_NOBODY = "nobody"
        
        // Color palette options
        const val PALETTE_DEFAULT = "default"
        const val PALETTE_OCEAN = "ocean"
        const val PALETTE_SUNSET = "sunset"
        const val PALETTE_FOREST = "forest"
        const val PALETTE_LAVENDER = "lavender"
    }
    
    val themeMode: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: THEME_MODE_SYSTEM // Default to system
    }
    
    val language: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: LANGUAGE_PERSIAN // Default to Persian
    }
    
    val isNotificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }
    
    val isSoundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[SOUND_ENABLED_KEY] ?: true
    }
    
    val isVibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED_KEY] ?: true
    }
    
    // Privacy settings flows
    val profileVisibility: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[PROFILE_VISIBILITY_KEY] ?: VISIBILITY_EVERYONE
    }
    
    val onlineVisibility: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[ONLINE_VISIBILITY_KEY] ?: VISIBILITY_EVERYONE
    }
    
    val phoneVisibility: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[PHONE_VISIBILITY_KEY] ?: VISIBILITY_CONTACTS
    }
    
    val isPinLockEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[PIN_LOCK_ENABLED_KEY] ?: false
    }
    
    val pinCode: Flow<String?> = context.settingsDataStore.data.map { preferences ->
        preferences[PIN_CODE_KEY]
    }
    
    val colorPalette: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[COLOR_PALETTE_KEY] ?: PALETTE_DEFAULT
    }
    val chatWallpaperUri: Flow<String?> = context.settingsDataStore.data.map { preferences ->
        preferences[CHAT_WALLPAPER_URI_KEY]
    }
    val bioVisibility: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[BIO_VISIBILITY_KEY] ?: VISIBILITY_EVERYONE
    }
    val lastSeenVisibility: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[LAST_SEEN_VISIBILITY_KEY] ?: VISIBILITY_EVERYONE
    }
    
    val isProfileBannerDismissed: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[IS_PROFILE_BANNER_DISMISSED_KEY] ?: false
    }
    // Notification channel flows
    fun notifSound(channel: String): Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        when (channel) {
            "personal" -> prefs[NOTIF_PERSONAL_SOUND_KEY] ?: true
            "group" -> prefs[NOTIF_GROUP_SOUND_KEY] ?: true
            "channel" -> prefs[NOTIF_CHANNEL_SOUND_KEY] ?: true
            "bot" -> prefs[NOTIF_BOT_SOUND_KEY] ?: true
            else -> true
        }
    }
    fun notifVibration(channel: String): Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        when (channel) {
            "personal" -> prefs[NOTIF_PERSONAL_VIBRATION_KEY] ?: true
            "group" -> prefs[NOTIF_GROUP_VIBRATION_KEY] ?: true
            "channel" -> prefs[NOTIF_CHANNEL_VIBRATION_KEY] ?: true
            "bot" -> prefs[NOTIF_BOT_VIBRATION_KEY] ?: true
            else -> true
        }
    }
    fun notifPopup(channel: String): Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        when (channel) {
            "personal" -> prefs[NOTIF_PERSONAL_POPUP_KEY] ?: true
            "group" -> prefs[NOTIF_GROUP_POPUP_KEY] ?: true
            "channel" -> prefs[NOTIF_CHANNEL_POPUP_KEY] ?: true
            "bot" -> prefs[NOTIF_BOT_POPUP_KEY] ?: true
            else -> true
        }
    }
    val autoDownloadImages: Flow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[AUTO_DOWNLOAD_IMAGES_KEY] ?: "WiFi و دیتای موبایل" }
        
    val autoDownloadVideos: Flow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[AUTO_DOWNLOAD_VIDEOS_KEY] ?: "فقط WiFi" }
        
    val autoDownloadFiles: Flow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[AUTO_DOWNLOAD_FILES_KEY] ?: "هیچ‌وقت" }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }
    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }
    suspend fun setProfileVisibility(visibility: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PROFILE_VISIBILITY_KEY] = visibility
        }
    }
    suspend fun setOnlineVisibility(visibility: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[ONLINE_VISIBILITY_KEY] = visibility
        }
    }
    suspend fun setPhoneVisibility(visibility: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[PHONE_VISIBILITY_KEY] = visibility
        }
    }
    suspend fun setBioVisibility(visibility: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[BIO_VISIBILITY_KEY] = visibility
        }
    }
    suspend fun setLastSeenVisibility(visibility: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[LAST_SEEN_VISIBILITY_KEY] = visibility
        }
    }
    suspend fun setProfileBannerDismissed(dismissed: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[IS_PROFILE_BANNER_DISMISSED_KEY] = dismissed
        }
    }
    suspend fun setPinLockEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[PIN_LOCK_ENABLED_KEY] = enabled
        }
    }
    suspend fun setPinCode(pin: String?) {
        context.settingsDataStore.edit { preferences ->
            if (pin != null) {
                preferences[PIN_CODE_KEY] = pin
            } else {
                preferences.remove(PIN_CODE_KEY)
            }
        }
    }
    suspend fun setColorPalette(palette: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[COLOR_PALETTE_KEY] = palette
        }
    }
    suspend fun setChatWallpaperUri(uri: String?) {
        context.settingsDataStore.edit { preferences ->
            if (uri != null) {
                preferences[CHAT_WALLPAPER_URI_KEY] = uri
            } else {
                preferences.remove(CHAT_WALLPAPER_URI_KEY)
            }
        }
    }
    suspend fun setNotifSound(channel: String, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            when (channel) {
                "personal" -> prefs[NOTIF_PERSONAL_SOUND_KEY] = enabled
                "group" -> prefs[NOTIF_GROUP_SOUND_KEY] = enabled
                "channel" -> prefs[NOTIF_CHANNEL_SOUND_KEY] = enabled
                "bot" -> prefs[NOTIF_BOT_SOUND_KEY] = enabled
            }
        }
    }
    suspend fun setNotifVibration(channel: String, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            when (channel) {
                "personal" -> prefs[NOTIF_PERSONAL_VIBRATION_KEY] = enabled
                "group" -> prefs[NOTIF_GROUP_VIBRATION_KEY] = enabled
                "channel" -> prefs[NOTIF_CHANNEL_VIBRATION_KEY] = enabled
                "bot" -> prefs[NOTIF_BOT_VIBRATION_KEY] = enabled
            }
        }
    }
    suspend fun setNotifPopup(channel: String, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            when (channel) {
                "personal" -> prefs[NOTIF_PERSONAL_POPUP_KEY] = enabled
                "group" -> prefs[NOTIF_GROUP_POPUP_KEY] = enabled
                "channel" -> prefs[NOTIF_CHANNEL_POPUP_KEY] = enabled
                "bot" -> prefs[NOTIF_BOT_POPUP_KEY] = enabled
            }
        }
    }
    /**
     * Clear all settings - used during logout
     */
    suspend fun setAutoDownloadImages(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_IMAGES_KEY] = value
        }
    }

    suspend fun setAutoDownloadVideos(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_VIDEOS_KEY] = value
        }
    }

    suspend fun setAutoDownloadFiles(value: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[AUTO_DOWNLOAD_FILES_KEY] = value
        }
    }

    /**
     * Clear all settings - used during logout
     */
    suspend fun clearAllSettings() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    // ── Contacts Selection (Exceptions / Blocked) ──────────────

    fun getBlockedUsers(): Flow<Set<String>> = context.settingsDataStore.data.map { preferences ->
        preferences[BLOCKED_USERS_KEY] ?: emptySet()
    }

    suspend fun setBlockedUsers(userIds: Set<String>) {
        context.settingsDataStore.edit { preferences ->
            preferences[BLOCKED_USERS_KEY] = userIds
        }
    }

    fun getPrivacyExceptions(type: String): Flow<Set<String>> = context.settingsDataStore.data.map { preferences ->
        preferences[privacyExceptionKey(type)] ?: emptySet()
    }

    suspend fun setPrivacyExceptions(type: String, userIds: Set<String>) {
        context.settingsDataStore.edit { preferences ->
            preferences[privacyExceptionKey(type)] = userIds
        }
    }
}
