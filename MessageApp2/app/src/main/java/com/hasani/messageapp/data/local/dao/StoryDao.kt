package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.StoryEntity
import com.hasani.messageapp.data.local.entity.StoryUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM story_users ORDER BY hasUnviewedStories DESC, lastUpdated DESC")
    fun getAllStoryUsers(): Flow<List<StoryUserEntity>>

    @Query("SELECT * FROM stories WHERE userId = :userId AND expiresAt > :currentTime ORDER BY createdAt ASC")
    fun getStoriesForUser(userId: String, currentTime: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories WHERE expiresAt > :currentTime ORDER BY createdAt ASC")
    fun getAllStories(currentTime: Long = System.currentTimeMillis()): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryUsers(users: List<StoryUserEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryUser(user: StoryUserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)

    @Query("DELETE FROM story_users")
    suspend fun deleteAllStoryUsers()

    @Query("DELETE FROM stories WHERE userId = :userId")
    suspend fun deleteStoriesForUser(userId: String)
    
    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStory(storyId: String)
    
    @Query("UPDATE stories SET isViewed = 1 WHERE id = :storyId")
    suspend fun markStoryAsViewed(storyId: String)
    
    // Transaction to replace all stories for a user
    @Transaction
    suspend fun updateStoriesForUser(userId: String, stories: List<StoryEntity>) {
        deleteStoriesForUser(userId)
        insertStories(stories)
    }
}
