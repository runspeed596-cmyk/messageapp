package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.GroupMessageEntity
import com.hasani.messageapp.data.local.entity.GroupMessageWithReply
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMessageDao {
    @Transaction
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun observeMessagesForGroup(groupId: String): Flow<List<GroupMessageWithReply>>
    
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesForGroup(groupId: String, limit: Int, offset: Int): List<GroupMessageEntity>
    
    @Query("SELECT * FROM group_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): GroupMessageEntity?
    
    @Query("SELECT * FROM group_messages WHERE isSynced = 0")
    suspend fun getPendingMessages(): List<GroupMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GroupMessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<GroupMessageEntity>)
    
    @Delete
    suspend fun deleteMessage(message: GroupMessageEntity)
    
    @Query("UPDATE group_messages SET reactions = :reactions, myReaction = :myReaction WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactions: String?, myReaction: String?)

    @Query("DELETE FROM group_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("SELECT * FROM group_messages WHERE groupId = :groupId AND content = :content AND isSynced = 0 LIMIT 1")
    suspend fun findPendingMessageByContent(groupId: String, content: String): GroupMessageEntity?
}
