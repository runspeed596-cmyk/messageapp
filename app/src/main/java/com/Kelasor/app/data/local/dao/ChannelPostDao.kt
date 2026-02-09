package com.Kelasor.app.data.local.dao

import androidx.room.*
import com.Kelasor.app.data.local.entity.ChannelPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelPostDao {
    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY createdAt DESC")
    fun observePostsForChannel(channelId: String): Flow<List<ChannelPostEntity>>
    
    @Query("SELECT * FROM channel_posts WHERE channelId = :channelId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPostsForChannel(channelId: String, limit: Int, offset: Int): List<ChannelPostEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: ChannelPostEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<ChannelPostEntity>)
    
    @Query("SELECT * FROM channel_posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: String): ChannelPostEntity?
    
    @Delete
    suspend fun deletePost(post: ChannelPostEntity)

    @Query("DELETE FROM channel_posts WHERE id = :postId")
    suspend fun deletePostById(postId: String)
}
