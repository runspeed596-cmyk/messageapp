package com.Kelasor.app.data.local.dao

import androidx.room.*
import com.Kelasor.app.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun observeMembersForGroup(groupId: String): Flow<List<GroupMemberEntity>>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getMembersForGroup(groupId: String): List<GroupMemberEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<GroupMemberEntity>)
    
    @Delete
    suspend fun deleteMember(member: GroupMemberEntity)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeMember(groupId: String, userId: String)
    
    @Query("UPDATE group_members SET role = :role WHERE groupId = :groupId AND userId = :userId")
    suspend fun updateMemberRole(groupId: String, userId: String, role: String)
}
