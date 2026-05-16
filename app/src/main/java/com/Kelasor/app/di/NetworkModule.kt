package com.Kelasor.app.di

import android.content.Context
import com.Kelasor.app.data.local.dao.MessageDao
import com.Kelasor.app.data.remote.AuthInterceptor
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.session.SessionManager
import com.Kelasor.app.data.sync.MessageSyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private val BASE_URL = com.Kelasor.app.util.Constants.BASE_URL
    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager = SessionManager(context)
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        sessionManager: SessionManager
    ): AuthInterceptor = AuthInterceptor(sessionManager)
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideStoryApiService(
        retrofit: Retrofit
    ): com.Kelasor.app.data.remote.api.StoryApiService = retrofit.create(com.Kelasor.app.data.remote.api.StoryApiService::class.java)
    
    @Provides
    @Singleton
    fun providePollApiService(
        retrofit: Retrofit
    ): com.Kelasor.app.data.remote.api.PollApiService = retrofit.create(com.Kelasor.app.data.remote.api.PollApiService::class.java)

    @Provides
    @Singleton
    fun provideEntertainmentApiService(
        retrofit: Retrofit
    ): com.Kelasor.app.data.remote.api.EntertainmentApiService = retrofit.create(com.Kelasor.app.data.remote.api.EntertainmentApiService::class.java)

    @Provides
    @Singleton
    fun provideElmApiService(
        retrofit: Retrofit
    ): com.Kelasor.app.data.remote.api.ElmApiService = retrofit.create(com.Kelasor.app.data.remote.api.ElmApiService::class.java)
    
    @Provides
    @Singleton
    fun provideMessageSyncManager(
        @ApplicationContext context: Context,
        messageDao: MessageDao,
        apiService: ApiService
    ): MessageSyncManager = MessageSyncManager(context, messageDao, apiService)
    
    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson = com.google.gson.Gson()
    
    @Provides
    @Singleton
    fun provideWebSocketManager(
        messageDao: MessageDao,
        groupMessageDao: com.Kelasor.app.data.local.dao.GroupMessageDao,
        channelPostDao: com.Kelasor.app.data.local.dao.ChannelPostDao,
        userDao: com.Kelasor.app.data.local.dao.UserDao,
        chatDao: com.Kelasor.app.data.local.dao.ChatDao,
        groupDao: com.Kelasor.app.data.local.dao.GroupDao,
        channelDao: com.Kelasor.app.data.local.dao.ChannelDao,
        channelSubscriberDao: com.Kelasor.app.data.local.dao.ChannelSubscriberDao,
        contactsRepository: com.Kelasor.app.data.repository.ContactsRepository,
        sessionManager: SessionManager,
        notificationHelper: com.Kelasor.app.data.notification.NotificationHelper,
        notificationBadgeManager: com.Kelasor.app.data.notification.NotificationBadgeManager,
        soundPlayer: com.Kelasor.app.data.media.SoundPlayer,
        currentChatManager: com.Kelasor.app.data.session.CurrentChatManager,
        gson: com.google.gson.Gson
    ): com.Kelasor.app.data.websocket.WebSocketManager = 
        com.Kelasor.app.data.websocket.WebSocketManager(
            messageDao, 
            groupMessageDao, 
            channelPostDao, 
            userDao, 
            chatDao, 
            groupDao, 
            channelDao, 
            channelSubscriberDao,
            contactsRepository, 
            sessionManager, 
            notificationHelper, 
            notificationBadgeManager, 
            soundPlayer,
            currentChatManager,
            gson
        )

    
    @Provides
    @Singleton
    fun provideGlobalSyncManager(
        webSocketManager: com.Kelasor.app.data.websocket.WebSocketManager,
        sessionManager: SessionManager,
        messageDao: MessageDao,
        groupMessageDao: com.Kelasor.app.data.local.dao.GroupMessageDao,
        channelPostDao: com.Kelasor.app.data.local.dao.ChannelPostDao,
        userDao: com.Kelasor.app.data.local.dao.UserDao,
        groupDao: com.Kelasor.app.data.local.dao.GroupDao,
        channelDao: com.Kelasor.app.data.local.dao.ChannelDao
    ): com.Kelasor.app.data.sync.GlobalSyncManager = 
        com.Kelasor.app.data.sync.GlobalSyncManager(
            webSocketManager, 
            sessionManager, 
            messageDao, 
            groupMessageDao, 
            channelPostDao, 
            userDao,
            groupDao,
            channelDao
        )
}

