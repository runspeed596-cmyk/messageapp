package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE isSubscribed = 1 AND isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun observeAllChannels(): Flow<List<ChannelEntity>>
    
    @Query("SELECT * FROM channels WHERE isSubscribed = 1 ORDER BY isPinned DESC, createdAt DESC")
    fun observeSubscribedChannels(): Flow<List<ChannelEntity>>
    
    @Query("SELECT * FROM channels WHERE isSubscribed = 1 AND isArchived = 1 ORDER BY createdAt DESC")
    fun observeArchivedChannels(): Flow<List<ChannelEntity>>
    
    @Query("SELECT * FROM channels WHERE id = :channelId")
    fun observeChannelById(channelId: String): Flow<ChannelEntity?>
    
    @Query("SELECT * FROM channels WHERE id = :channelId")
    suspend fun getChannelById(channelId: String): ChannelEntity?
    
    @Query("SELECT * FROM channels WHERE publicId = :publicId COLLATE NOCASE LIMIT 1")
    suspend fun getChannelByPublicId(publicId: String): ChannelEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)
    
    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)
    
    @Query("DELETE FROM channels WHERE id = :channelId")
    suspend fun deleteChannelById(channelId: String)
    
    @Query("UPDATE channels SET isSubscribed = :isSubscribed WHERE id = :channelId")
    suspend fun updateSubscriptionStatus(channelId: String, isSubscribed: Boolean)
    
    @Query("UPDATE channels SET isPinned = :isPinned WHERE id = :channelId")
    suspend fun updatePinStatus(channelId: String, isPinned: Boolean)
    
    @Query("UPDATE channels SET isArchived = :isArchived WHERE id = :channelId")
    suspend fun updateArchiveStatus(channelId: String, isArchived: Boolean)

    @Query("UPDATE channels SET isMuted = :isMuted WHERE id = :channelId")
    suspend fun updateMuteStatus(channelId: String, isMuted: Boolean)

    @Query("UPDATE channels SET unreadCount = :count WHERE id = :channelId")
    suspend fun updateUnreadCount(channelId: String, count: Int)

    @Query("UPDATE channels SET lastPostContent = :lastPostContent, lastPostTime = :lastPostTime WHERE id = :channelId")
    suspend fun updateLastPost(channelId: String, lastPostContent: String?, lastPostTime: Long?)
}
