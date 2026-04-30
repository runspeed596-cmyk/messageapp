package com.hasani.messageapp.data.local.dao

import androidx.room.*
import com.hasani.messageapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUserById(userId: String): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?
    
    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchUsers(query: String): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users")
    fun observeAllUsers(): Flow<List<UserEntity>>
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("UPDATE users SET isOnline = :isOnline, lastSeenAt = :lastSeenAt WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, lastSeenAt: Long?)
}
