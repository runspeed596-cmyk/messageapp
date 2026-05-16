package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.AppDatabase
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.domain.mapper.toDomain
import com.Kelasor.app.domain.mapper.toEntity
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.domain.model.SavedAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val user: User, val isNewUser: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Loading : AuthResult()
}

sealed class OtpResult {
    data class Success(val expiresInSeconds: Int) : OtpResult()
    data class Error(val message: String) : OtpResult()
    data object Loading : OtpResult()
}

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val database: AppDatabase
) {
    val isLoggedIn: Flow<Boolean> = sessionManager.isLoggedIn
    val isOnboardingComplete: Flow<Boolean> = sessionManager.isOnboardingComplete
    val currentUserId: Flow<String?> = sessionManager.userId
    val savedAccounts: Flow<List<SavedAccount>> = sessionManager.savedAccounts

    suspend fun sendOtp(phoneNumber: String): Flow<OtpResult> = flow {
        emit(OtpResult.Loading)
        try {
            val response = apiService.sendOtp(SendOtpRequest(phoneNumber))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(OtpResult.Success(response.body()?.expiresInSeconds ?: 60))
            } else {
                emit(OtpResult.Error(response.body()?.message ?: "خطا در ارسال کد تأیید"))
            }
        } catch (e: Exception) {
            emit(OtpResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun verifyOtp(
        phoneNumber: String,
        code: String,
        deviceName: String? = null,
        platform: String? = null,
        osVersion: String? = null,
        appVersion: String? = null
    ): Flow<AuthResult> = flow {
        emit(AuthResult.Loading)
        try {
            val response = apiService.verifyOtp(VerifyOtpRequest(
                phoneNumber = phoneNumber,
                code = code,
                deviceName = deviceName,
                platform = platform,
                osVersion = osVersion,
                appVersion = appVersion
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val accessToken = body.accessToken
                val refreshToken = body.refreshToken
                val user = body.user
                if (accessToken != null && refreshToken != null && user != null) {
                    // ALWAYS clear old data before login to ensure no stale data
                    // This handles: switching accounts, reinstalls, and any cached data issues
                    val previousUserId = sessionManager.userId.first()
                    if (previousUserId == null || previousUserId != user.id) {
                        android.util.Log.d("AuthRepository", "🗑️ Clearing old data before login (previousUserId=$previousUserId, newUserId=${user.id})")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            database.clearAllData()
                        }
                    }
                    sessionManager.saveSession(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        userId = user.id,
                        phoneNumber = phoneNumber,
                        onboardingComplete = !body.isNewUser,
                        displayName = user.displayName ?: user.firstName ?: "",
                        avatarUrl = user.avatarUrl
                    )
                    emit(AuthResult.Success(user.toDomain(), body.isNewUser))
                } else {
                    emit(AuthResult.Error("پاسخ نامعتبر از سرور"))
                }
            } else {
                emit(AuthResult.Error(response.body()?.message ?: "کد تأیید نادرست است"))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun refreshToken(): Boolean {
        return try {
            val currentRefreshToken = sessionManager.refreshToken.first()
            if (currentRefreshToken.isNullOrEmpty()) return false
            val response = apiService.refreshToken(RefreshTokenRequest(currentRefreshToken))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                body.accessToken?.let { sessionManager.updateAccessToken(it) }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun setOnboardingComplete(complete: Boolean) {
        sessionManager.setOnboardingComplete(complete)
    }

    suspend fun switchAccount(userId: String): Boolean {
        val previousUserId = sessionManager.userId.first()
        if (previousUserId == userId) return true
        val success = sessionManager.switchAccount(userId)
        if (success) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                database.clearAllData()
            }
        }
        return success
    }
    
    /**
     * Logout user: Clear all local data (database + session)
     * This ensures no old chats remain when a new user logs in
     */
    suspend fun logout(userId: String? = null): Boolean {
        return try {
            val targetUserId = userId ?: sessionManager.userId.first()
            if (targetUserId == null) return false
            
            val isActiveAccount = targetUserId == sessionManager.userId.first()
            if (isActiveAccount) {
                try { apiService.logout() } catch (e: Exception) {}
            }
            
            val hasOtherAccounts = sessionManager.removeAccount(targetUserId)
            if (!hasOtherAccounts) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    database.clearAllData()
                }
            } else if (isActiveAccount) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    database.clearAllData()
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
