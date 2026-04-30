package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE isArchived = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun observeAllGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun observeArchivedGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun observeGroupById(groupId: String): Flow<GroupEntity?>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: String)
    
    @Query("UPDATE groups SET memberCount = :memberCount WHERE id = :groupId")
    suspend fun updateMemberCount(groupId: String, memberCount: Int)
    
    @Query("UPDATE groups SET isPinned = :isPinned WHERE id = :groupId")
    suspend fun updatePinStatus(groupId: String, isPinned: Boolean)
    
    @Query("UPDATE groups SET isArchived = :isArchived WHERE id = :groupId")
    suspend fun updateArchiveStatus(groupId: String, isArchived: Boolean)

    @Query("UPDATE groups SET isMuted = :isMuted WHERE id = :groupId")
    suspend fun updateMuteStatus(groupId: String, isMuted: Boolean)

    @Query("UPDATE groups SET unreadCount = :count WHERE id = :groupId")
    suspend fun updateUnreadCount(groupId: String, count: Int)

    @Query("UPDATE groups SET lastMessageContent = :lastMessage, lastMessageTime = :lastMessageTime WHERE id = :groupId")
    suspend fun updateLastMessage(groupId: String, lastMessage: String?, lastMessageTime: Long?)
}
