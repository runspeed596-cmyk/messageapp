package com.hasani.messageapp.data.session

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Manages user session data using DataStore.
 * Stores access token, refresh token, and user ID for authentication.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SessionManager"
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val PHONE_NUMBER_KEY = stringPreferencesKey("phone_number")
    }
    
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        val token = preferences[ACCESS_TOKEN_KEY]
        Log.d(TAG, "📖 Reading accessToken: ${if (token != null) "${token.take(20)}..." else "NULL"}")
        token
    }
    
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    val phoneNumber: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PHONE_NUMBER_KEY]
    }
    
    val isLoggedIn: Flow<Boolean> = accessToken.map { 
        val loggedIn = it != null && it.isNotEmpty()
        Log.d(TAG, "🔐 isLoggedIn check: $loggedIn")
        loggedIn
    }
    
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        phoneNumber: String? = null
    ) {
        Log.d(TAG, "💾 Saving session for user: $userId")
        try {
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
                preferences[USER_ID_KEY] = userId
                phoneNumber?.let { preferences[PHONE_NUMBER_KEY] = it }
            }
            Log.d(TAG, "✅ Session saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save session", e)
            throw e
        }
    }
    
    suspend fun updateAccessToken(accessToken: String) {
        Log.d(TAG, "🔄 Updating accessToken: ${accessToken.take(20)}...")
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }
    }
    
    suspend fun clearSession() {
        Log.d(TAG, "🗑️ Clearing session")
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(PHONE_NUMBER_KEY)
        }
        Log.d(TAG, "✅ Session cleared")
    }
}
