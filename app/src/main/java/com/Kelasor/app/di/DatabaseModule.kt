package com.Kelasor.app.di

import android.content.Context
import androidx.room.Room
import com.Kelasor.app.data.local.AppDatabase
import com.Kelasor.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "messageapp.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao = database.chatDao()
    
    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()
    
    @Provides
    fun provideGroupDao(database: AppDatabase): GroupDao = database.groupDao()
    
    @Provides
    fun provideGroupMemberDao(database: AppDatabase): GroupMemberDao = database.groupMemberDao()
    
    @Provides
    fun provideGroupMessageDao(database: AppDatabase): GroupMessageDao = database.groupMessageDao()
    
    @Provides
    fun provideChannelDao(database: AppDatabase): ChannelDao = database.channelDao()
    
    @Provides
    fun provideChannelPostDao(database: AppDatabase): ChannelPostDao = database.channelPostDao()
    
    @Provides
    fun provideNotifiedMessageDao(database: AppDatabase): NotifiedMessageDao = database.notifiedMessageDao()

    @Provides
    fun provideChannelSubscriberDao(database: AppDatabase): ChannelSubscriberDao = database.channelSubscriberDao()

    @Provides
    fun provideStoryDao(database: AppDatabase): StoryDao = database.storyDao()

    @Provides
    fun provideCourseDao(database: AppDatabase): com.Kelasor.app.data.local.dao.CourseDao = database.courseDao()
}
