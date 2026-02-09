package com.Kelasor.app.data.local.dao

import androidx.room.*
import com.Kelasor.app.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC")
    fun observeMessagesForChat(chatId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getMessagesForChat(chatId: String, limit: Int, offset: Int): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): MessageEntity?
    
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getPendingMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE isSynced = 0")
    suspend fun getUnsyncedMessages(): List<MessageEntity>
    
    @Query("SELECT * FROM messages WHERE chatId = :chatId AND content = :content AND isSynced = 0 LIMIT 1")
    suspend fun findPendingMessageByContent(chatId: String, content: String): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)
    
    @Delete
    suspend fun deleteMessage(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
    
    @Query("UPDATE messages SET status = :status, isSynced = :isSynced WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String, isSynced: Boolean)
    
    @Query("UPDATE messages SET reactions = :reactions, myReaction = :myReaction WHERE id = :messageId")
    suspend fun updateReactions(messageId: String, reactions: String?, myReaction: String?)

    @Transaction
    suspend fun replacePendingMessage(localId: String, serverMessage: MessageEntity) {
        // 1. Insert server message first (if it doesn't exist)
        insertMessage(serverMessage)
        // 2. Delete local message (if different ID)
        if (localId != serverMessage.id) {
            deleteMessageById(localId)
        }
    }
}
