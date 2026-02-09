package com.Kelasor.app.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * API Service for managing stories (status updates).
 */
interface StoryApiService {

    /**
     * Get all active stories for the user (friends + self).
     * Returns a list of users, each containing their active stories.
     */
    @GET("api/stories")
    suspend fun getStories(): Response<List<StoryUserDto>>

    /**
     * Post a new story.
     */
    @Multipart
    @POST("api/stories")
    suspend fun postStory(
        @Part file: MultipartBody.Part,
        @Part("type") type: String, // "IMAGE" or "VIDEO"
        @Part("duration") duration: Int,
        @Part("caption") caption: String?
    ): Response<StoryDto>

    /**
     * Mark a story as viewed.
     */
    @POST("api/stories/{id}/view")
    suspend fun markStoryViewed(@Path("id") storyId: String): Response<Unit>

    /**
     * Get inactive/active views for a story (for the owner).
     */
    @GET("api/stories/{id}/views")
    suspend fun getStoryViews(@Path("id") storyId: String): Response<List<StoryViewDto>>

    /**
     * Delete a story.
     */
    @retrofit2.http.DELETE("api/stories/{id}")
    suspend fun deleteStory(@Path("id") storyId: String): Response<Unit>

    @GET("api/stories/groups")
    suspend fun getGroupStories(): Response<List<StoryUserDto>>

    @GET("api/stories/channels")
    suspend fun getChannelStories(): Response<List<StoryUserDto>>

    @Multipart
    @POST("api/stories/group/{groupId}")
    suspend fun postGroupStory(
        @Path("groupId") groupId: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String,
        @Part("duration") duration: Int,
        @Part("caption") caption: String?
    ): Response<StoryDto>

    @Multipart
    @POST("api/stories/channel/{channelId}")
    suspend fun postChannelStory(
        @Path("channelId") channelId: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: String,
        @Part("duration") duration: Int,
        @Part("caption") caption: String?
    ): Response<StoryDto>
}

// DTOs (Data Transfer Objects) matching the API response structure

data class StoryUserDto(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val stories: List<StoryDto>,
    val isCurrentUser: Boolean = false
)

data class StoryDto(
    val id: String,
    val userId: String,
    val mediaUrl: String,
    val type: String,
    val caption: String?,
    val duration: Int,
    val createdAt: String, // ISO timestamp
    val expiresAt: String, // ISO timestamp
    val isViewed: Boolean?,
    val viewCount: Int
)

data class StoryViewDto(
    val userId: String,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    val viewedAt: String // ISO timestamp
)
