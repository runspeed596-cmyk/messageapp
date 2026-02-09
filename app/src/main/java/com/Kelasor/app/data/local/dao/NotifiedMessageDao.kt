package com.Kelasor.app.data.local.dao

import androidx.room.*
import com.Kelasor.app.data.local.entity.NotifiedMessageEntity

@Dao
interface NotifiedMessageDao {
    @Query("SELECT EXISTS(SELECT 1 FROM notified_messages WHERE messageId = :messageId)")
    suspend fun isMessageNotified(messageId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsNotified(message: NotifiedMessageEntity)
    
    @Query("DELETE FROM notified_messages WHERE notifiedAt < :before")
    suspend fun cleanupOldEntries(before: Long)
}
