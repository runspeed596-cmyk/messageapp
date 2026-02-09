package com.Kelasor.app.data.local.dao

import androidx.room.*
import com.Kelasor.app.data.local.entity.ChatEntity
import com.Kelasor.app.data.local.entity.ChatParticipantEntity
import com.Kelasor.app.data.local.entity.ChatWithParticipants
import com.Kelasor.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Transaction
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllChats(): Flow<List<ChatWithParticipants>>
    
    @Transaction
    @Query("SELECT * FROM chats WHERE isArchived = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun observeActiveChats(): Flow<List<ChatWithParticipants>>
    
    @Transaction
    @Query("SELECT * FROM chats WHERE isArchived = 1 ORDER BY updatedAt DESC")
    fun observeArchivedChats(): Flow<List<ChatWithParticipants>>
    
    @Transaction
    @Query("SELECT * FROM chats WHERE isPinned = 1 ORDER BY updatedAt DESC")
    fun observePinnedChats(): Flow<List<ChatWithParticipants>>
    
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: String): ChatEntity?
    
    @Transaction
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChatById(chatId: String): Flow<ChatWithParticipants?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    
    @Delete
    suspend fun deleteChat(chat: ChatEntity)
    
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
    
    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :chatId")
    suspend fun updatePinStatus(chatId: String, isPinned: Boolean)
    
    @Query("UPDATE chats SET isMuted = :isMuted WHERE id = :chatId")
    suspend fun updateMuteStatus(chatId: String, isMuted: Boolean)
    
    @Query("UPDATE chats SET isArchived = :isArchived WHERE id = :chatId")
    suspend fun updateArchiveStatus(chatId: String, isArchived: Boolean)
    
    @Query("UPDATE chats SET unreadCount = :count WHERE id = :chatId")
    suspend fun updateUnreadCount(chatId: String, count: Int)
    
    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun clearUnreadCount(chatId: String)
    
    @Query("UPDATE chats SET isDeletedLocally = 1 WHERE id = :chatId")
    suspend fun markAsDeletedLocally(chatId: String)
    
    @Query("UPDATE chats SET lastMessage = :lastMessage, lastMessageTime = :lastMessageTime, updatedAt = :lastMessageTime WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, lastMessage: String?, lastMessageTime: Long?)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatParticipants(participants: List<ChatParticipantEntity>)
    
    @Query("""
        SELECT u.* FROM users u 
        INNER JOIN chat_participants cp ON u.id = cp.userId 
        WHERE cp.chatId = :chatId
    """)
    suspend fun getParticipantsForChat(chatId: String): List<UserEntity>
    
    @Query("SELECT userId FROM chat_participants WHERE chatId = :chatId")
    suspend fun getParticipantIdsForChat(chatId: String): List<String>
}
