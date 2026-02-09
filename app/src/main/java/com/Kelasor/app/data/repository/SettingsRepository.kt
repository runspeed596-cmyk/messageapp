package com.Kelasor.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
    @ApplicationContext private val context: Context
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
        
        // PIN Lock settings
        private val PIN_LOCK_ENABLED_KEY = booleanPreferencesKey("pin_lock_enabled")
        private val PIN_CODE_KEY = stringPreferencesKey("pin_code")
        
        // Color Palette settings
        private val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
        
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
    
    /**
     * Clear all settings - used during logout
     */
    suspend fun clearAllSettings() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
