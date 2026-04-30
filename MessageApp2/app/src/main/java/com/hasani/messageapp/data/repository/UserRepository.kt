package com.hasani.messageapp.data.repository

import com.hasani.messageapp.data.local.dao.UserDao
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.remote.dto.UpdateUserRequest
import com.hasani.messageapp.data.session.SessionManager
import com.hasani.messageapp.domain.mapper.toDomain
import com.hasani.messageapp.domain.mapper.toEntity
import com.hasani.messageapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class UserResult<out T> {
    data class Success<T>(val data: T) : UserResult<T>()
    data class Error(val message: String) : UserResult<Nothing>()
    data object Loading : UserResult<Nothing>()
}

@Singleton
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    fun observeCurrentUser(): Flow<User?> = userDao.observeCurrentUser().map { it?.toDomain() }
    suspend fun getCurrentUser(forceRefresh: Boolean = false): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        // Try to get cached user first
        if (!forceRefresh) {
            val cachedUser = userDao.getCurrentUser()
            if (cachedUser != null) {
                emit(UserResult.Success(cachedUser.toDomain()))
            }
        }
        // Fetch from API
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    val userEntity = userDto.toEntity(isCurrentUser = true)
                    userDao.insertUser(userEntity)
                    emit(UserResult.Success(userDto.toDomain()))
                } else {
                    emit(UserResult.Error("کاربر یافت نشد"))
                }
            } else {
                emit(UserResult.Error(response.body()?.message ?: "خطا در دریافت اطلاعات"))
            }
        } catch (e: Exception) {
            val cachedUser = userDao.getCurrentUser()
            if (cachedUser != null) {
                emit(UserResult.Success(cachedUser.toDomain()))
            } else {
                emit(UserResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    suspend fun updateProfile(
        username: String?,
        displayName: String?,
        bio: String?,
        university: String? = null,
        fieldOfStudy: String? = null,
        education: String? = null,
        skills: String? = null,
        interests: String? = null,
        workExperience: String? = null,
        achievements: String? = null,
        bioChannelId1: String? = null,
        bioChannelId2: String? = null
    ): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        try {
            val response = apiService.updateUser(UpdateUserRequest(
                username, displayName, bio,
                university, fieldOfStudy, education, skills, interests, workExperience, achievements,
                null, // avatarUrl
                bioChannelId1, bioChannelId2
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    val userEntity = userDto.toEntity(isCurrentUser = true)
                    userDao.insertUser(userEntity)
                    emit(UserResult.Success(userDto.toDomain()))
                } else {
                    emit(UserResult.Error("خطا در بروزرسانی پروفایل"))
                }
            } else {
                emit(UserResult.Error(response.body()?.message ?: "خطا در بروزرسانی پروفایل"))
            }
        } catch (e: Exception) {
            emit(UserResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun uploadAvatar(file: File): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
            val response = apiService.uploadAvatar(part)
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    val userEntity = userDto.toEntity(isCurrentUser = true)
                    userDao.insertUser(userEntity)
                    emit(UserResult.Success(userDto.toDomain()))
                } else {
                    emit(UserResult.Error("خطا در آپلود آواتار"))
                }
            } else {
                emit(UserResult.Error(response.body()?.message ?: "خطا در آپلود آواتار"))
            }
        } catch (e: Exception) {
            emit(UserResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    suspend fun getUserById(userId: String): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        // Try cache first
        val cachedUser = userDao.getUserById(userId)
        if (cachedUser != null) {
            emit(UserResult.Success(cachedUser.toDomain()))
        }
        // Fetch from API
        try {
            val response = apiService.getUserById(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val userDto = response.body()?.data
                if (userDto != null) {
                    // Preserve existing contactName from cache when updating from API
                    val existingContactName = cachedUser?.contactName
                    userDao.insertUser(userDto.toEntity(contactName = existingContactName))
                    // Return domain with contactName preserved
                    val updatedUser = userDto.toDomain().copy(contactName = existingContactName)
                    emit(UserResult.Success(updatedUser))
                } else {
                    if (cachedUser == null) {
                        emit(UserResult.Error("کاربر یافت نشد"))
                    }
                }
            } else {
                if (cachedUser == null) {
                    emit(UserResult.Error(response.body()?.message ?: "کاربر یافت نشد"))
                }
            }
        } catch (e: Exception) {
            if (cachedUser == null) {
                emit(UserResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    suspend fun searchUsers(query: String): Flow<UserResult<List<User>>> = flow {
        emit(UserResult.Loading)
        try {
            val response = apiService.searchUsers(query)
            if (response.isSuccessful) {
                val users = response.body()?.users?.map { it.toDomain() } ?: emptyList()
                // Cache the users
                response.body()?.users?.forEach { userDto ->
                    userDao.insertUser(userDto.toEntity())
                }
                emit(UserResult.Success(users))
            } else {
                // Fall back to local search
                val localUsers = userDao.searchUsers(query).first().map { it.toDomain() }
                emit(UserResult.Success(localUsers))
            }
        } catch (e: Exception) {
            val localUsers = userDao.searchUsers(query).first().map { it.toDomain() }
            if (localUsers.isNotEmpty()) {
                emit(UserResult.Success(localUsers))
            } else {
                emit(UserResult.Error("خطا در جستجو: ${e.message}"))
            }
        }
    }
    /**
     * Match phone numbers from device contacts with registered users.
     * Returns users that are registered in the app and have matching phone numbers.
     */
    /**
     * Match phone numbers from device contacts with registered users.
     * Returns users that are registered in the app and have matching phone numbers.
     * @param phoneContacts Map of PhoneNumber -> LocalContactName
     */
    suspend fun matchContacts(phoneContacts: Map<String, String>): Flow<UserResult<List<User>>> = flow {
        emit(UserResult.Loading)
        try {
            val phoneNumbers = phoneContacts.keys.toList()
            val response = apiService.matchContacts(phoneNumbers)
            if (response.isSuccessful) {
                // Map to domain keeping the local name
                val users = response.body()?.users?.map { dto ->
                     val localName = phoneContacts[dto.phoneNumber]
                     // We return domain object with local name
                     dto.toDomain().copy(contactName = localName)
                } ?: emptyList()
                
                // Cache the users with local name
                response.body()?.users?.forEach { userDto ->
                    val localName = phoneContacts[userDto.phoneNumber]
                    userDao.insertUser(userDto.toEntity(contactName = localName))
                }
                emit(UserResult.Success(users))
            } else {
                emit(UserResult.Error("خطا در بررسی مخاطبین"))
            }
        } catch (e: Exception) {
            emit(UserResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    /**
     * Get all contacts (all cached users except current user).
     * Falls back to searching with empty query if available.
     */
    suspend fun getContacts(): Flow<UserResult<List<User>>> = flow {
        emit(UserResult.Loading)
        try {
            // Privacy fix: Do NOT fetch all users via searchUsers("").
            // Return locally cached users (which should be populated by matchContacts or other interactions).
            val allUsers = userDao.getAllUsers().first().map { it.toDomain() }
            val currentUserId = sessionManager.userId.first()
            val filteredUsers = allUsers.filter { it.id != currentUserId }
            emit(UserResult.Success(filteredUsers))
        } catch (e: Exception) {
            emit(UserResult.Error("خطا در دریافت مخاطبین: ${e.message}"))
        }
    }
}

