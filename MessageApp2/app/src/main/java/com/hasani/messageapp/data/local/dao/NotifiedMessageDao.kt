package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.NotifiedMessageEntity

@Dao
interface NotifiedMessageDao {
    @Query("SELECT EXISTS(SELECT 1 FROM notified_messages WHERE messageId = :messageId)")
    suspend fun isMessageNotified(messageId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsNotified(message: NotifiedMessageEntity)
    
    @Query("DELETE FROM notified_messages WHERE notifiedAt < :before")
    suspend fun cleanupOldEntries(before: Long)
}
