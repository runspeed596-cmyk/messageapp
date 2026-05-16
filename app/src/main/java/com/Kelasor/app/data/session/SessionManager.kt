package com.Kelasor.app.data.session

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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.Kelasor.app.domain.model.SavedAccount
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
        private val IS_ONBOARDING_COMPLETE_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_onboarding_complete")
        private val SAVED_ACCOUNTS_KEY = stringPreferencesKey("saved_accounts")
    }

    private val gson = Gson()
    
    val savedAccounts: Flow<List<SavedAccount>> = context.dataStore.data.map { preferences ->
        val json = preferences[SAVED_ACCOUNTS_KEY]
        val accounts = if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<SavedAccount>>() {}.type
            try {
                gson.fromJson<List<SavedAccount>>(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        val currentUserId = preferences[USER_ID_KEY]
        val currentAccessToken = preferences[ACCESS_TOKEN_KEY]
        
        if (accounts.isEmpty() && currentUserId != null && currentAccessToken != null) {
            listOf(
                SavedAccount(
                    userId = currentUserId,
                    phoneNumber = preferences[PHONE_NUMBER_KEY] ?: "",
                    accessToken = currentAccessToken,
                    refreshToken = preferences[REFRESH_TOKEN_KEY] ?: "",
                    displayName = "",
                    avatarUrl = null
                )
            )
        } else {
            accounts
        }
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
    
    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ONBOARDING_COMPLETE_KEY] ?: false
    }
    
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        phoneNumber: String? = null,
        onboardingComplete: Boolean = true,
        displayName: String = "",
        avatarUrl: String? = null
    ) {
        Log.d(TAG, "💾 Saving session for user: $userId (onboardingComplete=$onboardingComplete)")
        try {
            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
                preferences[USER_ID_KEY] = userId
                preferences[IS_ONBOARDING_COMPLETE_KEY] = onboardingComplete
                phoneNumber?.let { preferences[PHONE_NUMBER_KEY] = it }
                
                // Multi-Account logic
                val savedJson = preferences[SAVED_ACCOUNTS_KEY]
                val type = object : TypeToken<List<SavedAccount>>() {}.type
                val accounts = if (savedJson.isNullOrEmpty()) {
                    mutableListOf<SavedAccount>()
                } else {
                    try { gson.fromJson<List<SavedAccount>>(savedJson, type).toMutableList() } 
                    catch (e: Exception) { mutableListOf<SavedAccount>() }
                }

                val existingIndex = accounts.indexOfFirst { it.userId == userId }
                val newAccount = SavedAccount(
                    userId = userId,
                    phoneNumber = phoneNumber ?: "",
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    displayName = if (displayName.isNotBlank()) displayName else (if (existingIndex >= 0) accounts[existingIndex].displayName else ""),
                    avatarUrl = avatarUrl ?: (if (existingIndex >= 0) accounts[existingIndex].avatarUrl else null)
                )
                
                if (existingIndex >= 0) {
                    accounts[existingIndex] = newAccount
                } else {
                    accounts.add(newAccount)
                }
                preferences[SAVED_ACCOUNTS_KEY] = gson.toJson(accounts)
            }
            Log.d(TAG, "✅ Session saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save session", e)
            throw e
        }
    }
    
    suspend fun setOnboardingComplete(complete: Boolean) {
        Log.d(TAG, "👤 Setting onboardingComplete to: $complete")
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDING_COMPLETE_KEY] = complete
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
            preferences.remove(IS_ONBOARDING_COMPLETE_KEY)
        }
        Log.d(TAG, "✅ Session cleared")
    }

    suspend fun updateSavedAccountProfile(userId: String, displayName: String, avatarUrl: String?) {
        context.dataStore.edit { preferences ->
            val savedJson = preferences[SAVED_ACCOUNTS_KEY]
            val type = object : TypeToken<List<SavedAccount>>() {}.type
            val accounts = if (!savedJson.isNullOrEmpty()) {
                try { gson.fromJson<List<SavedAccount>>(savedJson, type).toMutableList() }
                catch (e: Exception) { mutableListOf<SavedAccount>() }
            } else {
                // Migration for legacy active user
                val currentUserId = preferences[USER_ID_KEY]
                if (currentUserId == userId) {
                    mutableListOf(
                        SavedAccount(
                            userId = currentUserId,
                            phoneNumber = preferences[PHONE_NUMBER_KEY] ?: "",
                            accessToken = preferences[ACCESS_TOKEN_KEY] ?: "",
                            refreshToken = preferences[REFRESH_TOKEN_KEY] ?: "",
                            displayName = displayName,
                            avatarUrl = avatarUrl
                        )
                    )
                } else {
                    mutableListOf<SavedAccount>()
                }
            }

            val existingIndex = accounts.indexOfFirst { it.userId == userId }
            if (existingIndex >= 0) {
                accounts[existingIndex] = accounts[existingIndex].copy(
                    displayName = displayName,
                    avatarUrl = avatarUrl
                )
                preferences[SAVED_ACCOUNTS_KEY] = gson.toJson(accounts)
            } else if (accounts.isNotEmpty()) { // if we migrated above, existingIndex is 0
                preferences[SAVED_ACCOUNTS_KEY] = gson.toJson(accounts)
            }
        }
    }

    suspend fun switchAccount(userId: String): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val savedJson = preferences[SAVED_ACCOUNTS_KEY]
            if (!savedJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<SavedAccount>>() {}.type
                try {
                    val accounts = gson.fromJson<List<SavedAccount>>(savedJson, type)
                    val account = accounts.find { it.userId == userId }
                    if (account != null) {
                        preferences[ACCESS_TOKEN_KEY] = account.accessToken
                        preferences[REFRESH_TOKEN_KEY] = account.refreshToken
                        preferences[USER_ID_KEY] = account.userId
                        preferences[PHONE_NUMBER_KEY] = account.phoneNumber
                        success = true
                        Log.d(TAG, "✅ Switched to account: $userId")
                    }
                } catch (e: Exception) {}
            }
        }
        return success
    }

    suspend fun removeAccount(userId: String): Boolean {
        var hasOtherAccounts = false
        context.dataStore.edit { preferences ->
            val savedJson = preferences[SAVED_ACCOUNTS_KEY]
            if (!savedJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<SavedAccount>>() {}.type
                try {
                    val accounts = gson.fromJson<List<SavedAccount>>(savedJson, type).toMutableList()
                    accounts.removeAll { it.userId == userId }
                    preferences[SAVED_ACCOUNTS_KEY] = gson.toJson(accounts)
                    
                    val currentActiveUserId = preferences[USER_ID_KEY]
                    if (currentActiveUserId == userId) {
                        if (accounts.isNotEmpty()) {
                            val nextAccount = accounts.first()
                            preferences[ACCESS_TOKEN_KEY] = nextAccount.accessToken
                            preferences[REFRESH_TOKEN_KEY] = nextAccount.refreshToken
                            preferences[USER_ID_KEY] = nextAccount.userId
                            preferences[PHONE_NUMBER_KEY] = nextAccount.phoneNumber
                            hasOtherAccounts = true
                        } else {
                            preferences.remove(ACCESS_TOKEN_KEY)
                            preferences.remove(REFRESH_TOKEN_KEY)
                            preferences.remove(USER_ID_KEY)
                            preferences.remove(PHONE_NUMBER_KEY)
                            preferences.remove(IS_ONBOARDING_COMPLETE_KEY)
                        }
                    } else {
                        hasOtherAccounts = accounts.isNotEmpty()
                    }
                } catch (e: Exception) {
                    preferences.remove(ACCESS_TOKEN_KEY)
                    preferences.remove(REFRESH_TOKEN_KEY)
                    preferences.remove(USER_ID_KEY)
                }
            }
        }
        return hasOtherAccounts
    }
}
