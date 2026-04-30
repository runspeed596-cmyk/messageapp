package com.hasani.messageapp.data.mapper

import com.hasani.messageapp.data.remote.api.StoryDto
import com.hasani.messageapp.data.remote.api.StoryUserDto
import com.hasani.messageapp.domain.model.Story
import com.hasani.messageapp.domain.model.StoryType
import com.hasani.messageapp.domain.model.StoryUser
import java.time.Instant

// Hardcoding BASE_URL matching NetworkModule for quick fix. Ideally inject it.
private const val BASE_URL = "http://192.168.70.113:8080"

fun StoryDto.toDomain(): Story {
    return Story(
        id = id,
        userId = userId,
        mediaUrl = if (mediaUrl.startsWith("/")) "$BASE_URL$mediaUrl" else mediaUrl,
        type = try {
            val typeStr = type.trim().uppercase()
            StoryType.valueOf(typeStr)
        } catch (e: Exception) {
            android.util.Log.e("StoryMapper", "Error parsing story type: '${type}', defaulting to IMAGE")
            StoryType.IMAGE
        },
        caption = caption,
        durationSeconds = duration,
        createdAt = try {
            Instant.parse(createdAt)
        } catch (e: Exception) {
            Instant.now()
        },
        expiresAt = try {
            Instant.parse(expiresAt)
        } catch (e: Exception) {
            Instant.now().plusSeconds(24 * 3600)
        },
        isViewed = isViewed ?: false,
        viewCount = viewCount
    )
}

fun StoryUserDto.toDomain(currentUserId: String): StoryUser {
    return StoryUser(
        userId = userId,
        username = username,
        displayName = displayName,
        avatarUrl = avatarUrl?.let { if (it.startsWith("/")) "$BASE_URL$it" else it },
        stories = stories.map { it.toDomain() },
        isCurrentUser = isCurrentUser
    )
}

fun com.hasani.messageapp.data.remote.api.StoryViewDto.toDomain(): com.hasani.messageapp.domain.model.StoryViewer {
    return com.hasani.messageapp.domain.model.StoryViewer(
        userId = userId,
        displayName = userDisplayName,
        avatarUrl = userAvatarUrl?.let { if (it.startsWith("/")) "$BASE_URL$it" else it },
        viewedAt = try {
            Instant.parse(viewedAt)
        } catch (e: Exception) {
            Instant.now()
        }
    )
}
