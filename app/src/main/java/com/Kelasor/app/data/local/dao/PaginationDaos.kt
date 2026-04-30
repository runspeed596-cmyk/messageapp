package com.Kelasor.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.Kelasor.app.data.local.entity.CachedChatListEntity
import com.Kelasor.app.data.local.entity.CachedMessageEntity
import com.Kelasor.app.data.local.entity.CachedStoryEntity
import com.Kelasor.app.data.local.entity.PaginationRemoteKey

// ═══════════════════════════════════════════════════════════════════════════════
// 💾 Room DAOs for Cursor Pagination + Paging 3
// All queries use cursor-based (id-based) ordering, NEVER OFFSET
// ═══════════════════════════════════════════════════════════════════════════════

@Dao
interface CachedMessageDao {
    @Query("SELECT * FROM cached_messages WHERE chat_id = :chatId ORDER BY id DESC")
    fun getMessagesPagingSource(chatId: String): PagingSource<Int, CachedMessageEntity>

    @Query("SELECT * FROM cached_messages WHERE chat_id = :chatId ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentMessages(chatId: String, limit: Int): List<CachedMessageEntity>

    @Query("SELECT * FROM cached_messages WHERE chat_id = :chatId AND id < :cursorId ORDER BY id DESC LIMIT :limit")
    suspend fun getMessagesBefore(chatId: String, cursorId: Long, limit: Int): List<CachedMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<CachedMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CachedMessageEntity)

    @Query("DELETE FROM cached_messages WHERE chat_id = :chatId")
    suspend fun clearChatMessages(chatId: String)

    @Query("DELETE FROM cached_messages")
    suspend fun clearAll()

    @Query("SELECT MIN(id) FROM cached_messages WHERE chat_id = :chatId")
    suspend fun getOldestMessageId(chatId: String): Long?
}

@Dao
interface CachedChatListDao {
    @Query("SELECT * FROM cached_chat_list ORDER BY last_message_time DESC")
    fun getChatListPagingSource(): PagingSource<Int, CachedChatListEntity>

    @Query("SELECT * FROM cached_chat_list ORDER BY last_message_time DESC LIMIT :limit")
    suspend fun getRecentChats(limit: Int): List<CachedChatListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<CachedChatListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: CachedChatListEntity)

    @Query("DELETE FROM cached_chat_list")
    suspend fun clearAll()
}

@Dao
interface CachedStoryDao {
    @Query("SELECT * FROM cached_stories WHERE expires_at > :now ORDER BY timestamp DESC")
    fun getStoriesPagingSource(now: Long = System.currentTimeMillis()): PagingSource<Int, CachedStoryEntity>

    @Query("SELECT * FROM cached_stories WHERE user_id = :userId AND expires_at > :now ORDER BY timestamp DESC")
    suspend fun getUserStories(userId: String, now: Long = System.currentTimeMillis()): List<CachedStoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<CachedStoryEntity>)

    @Query("DELETE FROM cached_stories WHERE expires_at < :now")
    suspend fun deleteExpired(now: Long = System.currentTimeMillis())

    @Query("DELETE FROM cached_stories")
    suspend fun clearAll()
}

@Dao
interface PaginationRemoteKeyDao {
    @Query("SELECT * FROM pagination_remote_keys WHERE entity_type = :type AND entity_id = :entityId")
    suspend fun getRemoteKey(type: String, entityId: String): PaginationRemoteKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKey(key: PaginationRemoteKey)

    @Query("DELETE FROM pagination_remote_keys WHERE entity_type = :type AND entity_id = :entityId")
    suspend fun deleteRemoteKey(type: String, entityId: String)

    @Query("DELETE FROM pagination_remote_keys")
    suspend fun clearAll()
}
