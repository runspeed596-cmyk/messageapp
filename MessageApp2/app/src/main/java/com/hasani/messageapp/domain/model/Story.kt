package com.hasani.messageapp.domain.model

import java.time.Instant

/**
 * Type of story content
 */
enum class StoryType {
    IMAGE,
    VIDEO
}

/**
 * Represents a single story item (status update).
 */
data class Story(
    val id: String,
    val userId: String,
    val mediaUrl: String,
    val type: StoryType,
    val caption: String? = null,
    val durationSeconds: Int = 5, // Default 5s for images, video duration for videos
    val createdAt: Instant,
    val expiresAt: Instant,
    var isViewed: Boolean = false,
    val viewCount: Int = 0
)

data class StoryViewer(
    val userId: String,
    val displayName: String,
    val avatarUrl: String?,
    val viewedAt: Instant
)

/**
 * Represents a user in the stories list, holding their active stories.
 */
data class StoryUser(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val stories: List<Story>,
    val isCurrentUser: Boolean = false
) {
    /**
     * True if all stories have been viewed
     */
    val allViewed: Boolean
        get() = stories.all { it.isViewed }
        
    /**
     * Returns the index of the first unviewed story, or 0 if all viewed
     */
    val firstUnviewedIndex: Int
        get() {
            val index = stories.indexOfFirst { !it.isViewed }
            return if (index == -1) 0 else index
        }
}
