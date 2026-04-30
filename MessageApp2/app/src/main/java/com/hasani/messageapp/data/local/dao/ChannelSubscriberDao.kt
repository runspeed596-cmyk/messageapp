package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.ChannelSubscriberEntity
import com.hasani.messageapp.data.local.entity.SubscriberWithUser
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelSubscriberDao {
    @Transaction
    @Query("SELECT * FROM channel_subscribers WHERE channelId = :channelId ORDER BY joinedAt DESC")
    fun observeSubscribersWithUsers(channelId: String): Flow<List<SubscriberWithUser>>

    @Query("SELECT * FROM channel_subscribers WHERE channelId = :channelId ORDER BY joinedAt DESC")
    fun observeSubscribersForChannel(channelId: String): Flow<List<ChannelSubscriberEntity>>
    
    @Query("SELECT * FROM channel_subscribers WHERE channelId = :channelId ORDER BY joinedAt DESC")
    suspend fun getSubscribersForChannel(channelId: String): List<ChannelSubscriberEntity>
    
    @Query("SELECT * FROM channel_subscribers WHERE channelId = :channelId AND userId = :userId")
    suspend fun getSubscriber(channelId: String, userId: String): ChannelSubscriberEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriber(subscriber: ChannelSubscriberEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscribers(subscribers: List<ChannelSubscriberEntity>)
    
    @Delete
    suspend fun deleteSubscriber(subscriber: ChannelSubscriberEntity)
    
    @Query("DELETE FROM channel_subscribers WHERE channelId = :channelId AND userId = :userId")
    suspend fun removeSubscriber(channelId: String, userId: String)
    
    @Query("UPDATE channel_subscribers SET isAdmin = :isAdmin WHERE channelId = :channelId AND userId = :userId")
    suspend fun updateAdminStatus(channelId: String, userId: String, isAdmin: Boolean)
}
