package com.hasani.messageapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hasani.messageapp.data.local.dao.*
import com.hasani.messageapp.data.local.entity.*

import com.hasani.messageapp.data.local.entity.CourseEntity
import com.hasani.messageapp.data.local.dao.CourseDao
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        GroupMessageEntity::class,
        ChannelEntity::class,
        ChannelPostEntity::class,
        ChannelSubscriberEntity::class,
        ChatParticipantEntity::class,
        NotifiedMessageEntity::class,
        StoryEntity::class,
        StoryUserEntity::class,
        CourseEntity::class
    ],
    version = 2, // Increment version if needed, or stick to 1 if dev
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun channelDao(): ChannelDao
    abstract fun channelPostDao(): ChannelPostDao
    abstract fun channelSubscriberDao(): ChannelSubscriberDao
    abstract fun notifiedMessageDao(): NotifiedMessageDao
    abstract fun storyDao(): StoryDao
    abstract fun courseDao(): CourseDao
    
    /**
     * Clear all data from the database.
     * Used during logout to ensure no old data remains.
     */
    suspend fun clearAllData() {
        clearAllTables()
    }
}
