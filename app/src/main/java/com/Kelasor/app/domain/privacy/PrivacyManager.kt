package com.Kelasor.app.domain.privacy

import com.Kelasor.app.data.repository.SettingsRepository
import com.Kelasor.app.data.repository.SettingsRepository.Companion.VISIBILITY_CONTACTS
import com.Kelasor.app.data.repository.SettingsRepository.Companion.VISIBILITY_EVERYONE
import com.Kelasor.app.data.repository.SettingsRepository.Companion.VISIBILITY_NOBODY
import com.Kelasor.app.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Privacy enforcement manager - centralizes privacy logic
 * Enforces "Nobody" settings strictly across all screens
 */
@Singleton
class PrivacyManager @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) {
    data class PrivacySettings(
        val profilePhotoVisibility: String,
        val onlineStatusVisibility: String,
        val phoneNumberVisibility: String
    )
    
    data class UserPrivacy(
        val userId: String,
        val canSeeProfilePhoto: Boolean,
        val canSeeOnlineStatus: Boolean,
        val canSeePhoneNumber: Boolean
    )
    
    /**
     * Get current user's privacy settings
     */
    fun getMyPrivacySettings(): Flow<PrivacySettings> = combine(
        settingsRepository.profileVisibility,
        settingsRepository.onlineVisibility,
        settingsRepository.phoneVisibility
    ) { photo: String, online: String, phone: String ->
        PrivacySettings(photo, online, phone)
    }
    
    /**
     * Check if target user can see my data based on privacy settings
     */
    suspend fun canUserSeeMyData(
        targetUserId: String,
        isContact: Boolean
    ): UserPrivacy {
        val currentUserId = sessionManager.userId.first()
        
        // User can always see their own data
        if (targetUserId == currentUserId) {
            return UserPrivacy(
                userId = targetUserId,
                canSeeProfilePhoto = true,
                canSeeOnlineStatus = true,
                canSeePhoneNumber = true
            )
        }
        
        val privacySettings = combine(
            settingsRepository.profileVisibility,
            settingsRepository.onlineVisibility,
            settingsRepository.phoneVisibility
        ) { photo: String, online: String, phone: String ->
            PrivacySettings(photo, online, phone)
        }.first()
        
        return UserPrivacy(
            userId = targetUserId,
            canSeeProfilePhoto = checkVisibility(privacySettings.profilePhotoVisibility, isContact),
            canSeeOnlineStatus = checkVisibility(privacySettings.onlineStatusVisibility, isContact),
            canSeePhoneNumber = checkVisibility(privacySettings.phoneNumberVisibility, isContact)
        )
    }
    
    /**
     * Check if other user's data should be visible to me
     * This assumes backend sends proper privacy settings with user data
     */
    fun canISeeUserData(
        userPrivacySettings: PrivacySettings?,
        isContact: Boolean
    ): UserPrivacy {
        // If no privacy settings from backend, default to restrictive (nobody)
        val settings = userPrivacySettings ?: PrivacySettings(
            VISIBILITY_NOBODY,
            VISIBILITY_NOBODY,
            VISIBILITY_NOBODY
        )
        
        return UserPrivacy(
            userId = "",
            canSeeProfilePhoto = checkVisibility(settings.profilePhotoVisibility, isContact),
            canSeeOnlineStatus = checkVisibility(settings.onlineStatusVisibility, isContact),
            canSeePhoneNumber = checkVisibility(settings.phoneNumberVisibility, isContact)
        )
    }
    
    private fun checkVisibility(setting: String, isContact: Boolean): Boolean {
        return when (setting) {
            VISIBILITY_EVERYONE -> true
            VISIBILITY_CONTACTS -> isContact
            VISIBILITY_NOBODY -> false
            else -> false // Default to strictest
        }
    }
    
    /**
     * Sanitize user entity based on privacy - returns null for restricted fields
     */
    fun sanitizeUserAvatarUrl(
        avatarUrl: String?,
        canSeeProfilePhoto: Boolean
    ): String? {
        return if (canSeeProfilePhoto) avatarUrl else null
    }
    
    fun sanitizePhoneNumber(
        phoneNumber: String,
        canSeePhoneNumber: Boolean
    ): String {
        return if (canSeePhoneNumber) phoneNumber else "مخفی"
    }
    
    fun sanitizeOnlineStatus(
        isOnline: Boolean,
        lastSeen: Long?,
        canSeeOnlineStatus: Boolean
    ): Pair<Boolean, Long?> {
        return if (canSeeOnlineStatus) {
            Pair(isOnline, lastSeen)
        } else {
            Pair(false, null)
        }
    }
}
