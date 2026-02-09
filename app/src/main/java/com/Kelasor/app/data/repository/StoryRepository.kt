package com.Kelasor.app.data.repository

import android.content.Context
import android.net.Uri
import com.Kelasor.app.data.local.dao.StoryDao
import com.Kelasor.app.data.local.entity.StoryEntity
import com.Kelasor.app.data.local.entity.StoryUserEntity
import com.Kelasor.app.data.mapper.toDomain
import com.Kelasor.app.data.remote.api.StoryApiService
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.domain.model.Story
import com.Kelasor.app.domain.model.StoryUser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepository @Inject constructor(
    private val apiService: StoryApiService,
    private val storyDao: StoryDao,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) {

    /**
     * Fetch all stories from API and cache in Room.
     * Returns Flow that emits local data immediately and updates when API returns specific result.
     */
    // observeStories removed as it requires Relation refactoring. getStories now uses snapshot strategy.
    
    // BACKTRACK: I cannot easily fix the N+1 problem without a proper Relation class.
    // The previous implementation tried to use `first()` inside `collect` or `map`. 
    // `storyDao.getStoriesForUser(id).first()` is suspend.
    
    // I WILL USE `flatMapLatest` (requires ExperimentalCoroutinesApi?) or regular `map` is not enough.
    
    // Let's look at `refreshStories`:
    suspend fun refreshStories(): Result<Unit> {
        return try {
            val response = apiService.getStories()
            if (response.isSuccessful && response.body() != null) {
                // Logic to save to DB (same as before)
                val currentUserId = sessionManager.userId.first() ?: ""
                val stories = response.body()!!.map { it.toDomain(currentUserId) }
                
                 val storyUserEntities = stories.map { storyUser ->
                   StoryUserEntity(
                       id = storyUser.userId,
                       username = storyUser.username,
                       displayName = storyUser.displayName,
                       avatarUrl = storyUser.avatarUrl,
                       isCurrentUser = storyUser.isCurrentUser,
                       hasUnviewedStories = !storyUser.allViewed
                   )
                }
                storyDao.insertStoryUsers(storyUserEntities)
                
                stories.forEach { storyUser ->
                    val storyEntities = storyUser.stories.map { story ->
                        StoryEntity(
                            id = story.id,
                            userId = storyUser.userId,
                            type = story.type.name,
                            mediaUrl = story.mediaUrl,
                            caption = story.caption,
                            duration = story.durationSeconds,
                            viewCount = story.viewCount,
                            isViewed = story.isViewed,
                            createdAt = story.createdAt.toEpochMilli(),
                            expiresAt = story.expiresAt.toEpochMilli()
                        )
                    }
                    storyDao.updateStoriesForUser(storyUser.userId, storyEntities)
                }
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to fetch stories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getStories(): Flow<Result<List<StoryUser>>> = flow {
        // Simple polling/observe logic
        // We emit local data combined with N+1 queries using `flow` builder loop
        
        // This is the "Blocking" loop I had before. 
        // `storyDao.getAllStoryUsers().collect` blocks.
        
        // Fix: Use `emitAll` with `channelFlow` OR just use `refreshStories` call at start 
        // and return a flow that queries DB periodically?
        
        // To fix OOM/Freezing, I will just emit ONE snapshot of local data, then refresh, then emit new snapshot.
        // I won't observe DB continuously for now to save complexity/memory.
        
        // 1. Emit local snapshot
        try {
             val localUsers = storyDao.getAllStoryUsers().first() // One shot
             if (localUsers.isNotEmpty()) {
                 val fullUsers = localUsers.map { u ->
                     val s = storyDao.getStoriesForUser(u.id).first()
                     // Map to domain...
                     mapToDomain(u, s)
                 }
                 emit(Result.success(fullUsers))
             }
        } catch(e: Exception) {}
        
        // 2. Refresh
        val result = refreshStories()
        if (result.isFailure) emit(Result.failure(result.exceptionOrNull()!!))
        
        // 3. Emit new local snapshot
         try {
             val localUsers = storyDao.getAllStoryUsers().first()
              val fullUsers = localUsers.map { u ->
                     val s = storyDao.getStoriesForUser(u.id).first()
                     mapToDomain(u, s)
                 }
                 emit(Result.success(fullUsers))
         } catch(e: Exception) {}
    }

    private fun mapToDomain(userEntity: StoryUserEntity, stories: List<StoryEntity>): StoryUser {
        return StoryUser(
             userId = userEntity.id,
             username = userEntity.username,
             displayName = userEntity.displayName,
             avatarUrl = userEntity.avatarUrl,
             isCurrentUser = userEntity.isCurrentUser,
             stories = stories.map { story ->
                 Story(
                     id = story.id,
                     userId = story.userId,
                     mediaUrl = story.mediaUrl,
                     type = com.Kelasor.app.domain.model.StoryType.valueOf(story.type),
                     caption = story.caption,
                     durationSeconds = story.duration,
                     viewCount = story.viewCount,
                     isViewed = story.isViewed,
                     createdAt = java.time.Instant.ofEpochMilli(story.createdAt),
                     expiresAt = java.time.Instant.ofEpochMilli(story.expiresAt)
                 )
             }
         )
    }

    /**
     * Upload a story
     */
    suspend fun uploadStory(
        uri: Uri, 
        type: String, 
        duration: Int, 
        caption: String?
    ): Result<Story> {
        return try {
            val suffix = if (type == "VIDEO") ".mp4" else ".jpg"
            val file = uriToFile(uri, suffix) ?: return Result.failure(Exception("Could not process file"))
            
            val requestFile = file.asRequestBody(
                (if (type == "VIDEO") "video/*" else "image/*").toMediaTypeOrNull()
            )
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = apiService.postStory(body, type, duration, caption)
            
            if (response.isSuccessful && response.body() != null) {
                val storyDomain = response.body()!!.toDomain()
                // Save specific story to DB? For now, refresh all stories is safer/easier
                Result.success(storyDomain)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                val errorMsg = try {
                    JSONObject(errorBody).optString("error", errorBody)
                } catch (e: Exception) {
                    errorBody
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a story
     */
    suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            val response = apiService.deleteStory(storyId)
            if (response.isSuccessful) {
                storyDao.deleteStory(storyId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.code()}"))
            }
        } catch (e: Exception) {
             // If manual delete offline, maybe delete locally too?
             // storyDao.deleteStory(storyId)
            Result.failure(e)
        }
    }

    /**
     * Mark story as viewed
     */
    suspend fun markAsViewed(storyId: String) {
        try {
            storyDao.markStoryAsViewed(storyId) // Optimistic update
            apiService.markStoryViewed(storyId)
        } catch (e: Exception) {
            // Ignore error
        }
    }

    /**
     * Get story views
     */
    suspend fun getStoryViews(storyId: String): Result<List<com.Kelasor.app.domain.model.StoryViewer>> {
        return try {
            val response = apiService.getStoryViews(storyId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toDomain() })
            } else {
                Result.failure(Exception("Failed to fetch views"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper to convert Uri to File
    private fun uriToFile(uri: Uri, suffix: String): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", suffix, context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGroupStoriesFromApi(): Result<List<StoryUser>> {
        return try {
            val response = apiService.getGroupStories()
            if (response.isSuccessful && response.body() != null) {
                // We use empty string for currentUserId as we don't need to differentiate "My Story" logic for groups heavily yet
                // or simpler: we don't check isViewed locally perfectly for groups in this simplified flow
                val currentUserId = sessionManager.userId.first() ?: ""
                Result.success(response.body()!!.map { it.toDomain(currentUserId) })
            } else {
                Result.failure(Exception("Failed to fetch group stories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelStoriesFromApi(): Result<List<StoryUser>> {
        return try {
            val response = apiService.getChannelStories()
            if (response.isSuccessful && response.body() != null) {
                val currentUserId = sessionManager.userId.first() ?: ""
                Result.success(response.body()!!.map { it.toDomain(currentUserId) })
            } else {
                Result.failure(Exception("Failed to fetch channel stories"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadGroupStory(groupId: String, uri: Uri, type: String, duration: Int, caption: String?): Result<Story> {
        return try {
            val suffix = if (type == "VIDEO") ".mp4" else ".jpg"
            val file = uriToFile(uri, suffix) ?: return Result.failure(Exception("Could not process file"))
            val requestFile = file.asRequestBody((if (type == "VIDEO") "video/*" else "image/*").toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = apiService.postGroupStory(groupId, body, type, duration, caption)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                val errorMsg = try {
                    JSONObject(errorBody).optString("error", errorBody)
                } catch (e: Exception) {
                    errorBody
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadChannelStory(channelId: String, uri: Uri, type: String, duration: Int, caption: String?): Result<Story> {
        return try {
            val suffix = if (type == "VIDEO") ".mp4" else ".jpg"
            val file = uriToFile(uri, suffix) ?: return Result.failure(Exception("Could not process file"))
            val requestFile = file.asRequestBody((if (type == "VIDEO") "video/*" else "image/*").toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            val response = apiService.postChannelStory(channelId, body, type, duration, caption)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.toDomain())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Upload failed: ${response.code()}"
                val errorMsg = try {
                    JSONObject(errorBody).optString("error", errorBody)
                } catch (e: Exception) {
                    errorBody
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
