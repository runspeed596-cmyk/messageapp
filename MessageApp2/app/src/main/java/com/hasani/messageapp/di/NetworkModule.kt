package com.hasani.messageapp.di

import android.content.Context
import com.hasani.messageapp.data.local.dao.MessageDao
import com.hasani.messageapp.data.remote.AuthInterceptor
import com.hasani.messageapp.data.remote.api.ApiService
import com.hasani.messageapp.data.session.SessionManager
import com.hasani.messageapp.data.sync.MessageSyncManager
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
    private const val BASE_URL = "http://192.168.70.113:8080/"
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
    ): com.hasani.messageapp.data.remote.api.StoryApiService = retrofit.create(com.hasani.messageapp.data.remote.api.StoryApiService::class.java)
    
    @Provides
    @Singleton
    fun providePollApiService(
        retrofit: Retrofit
    ): com.hasani.messageapp.data.remote.api.PollApiService = retrofit.create(com.hasani.messageapp.data.remote.api.PollApiService::class.java)

    @Provides
    @Singleton
    fun provideEntertainmentApiService(
        retrofit: Retrofit
    ): com.hasani.messageapp.data.remote.api.EntertainmentApiService = retrofit.create(com.hasani.messageapp.data.remote.api.EntertainmentApiService::class.java)
    
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
        groupMessageDao: com.hasani.messageapp.data.local.dao.GroupMessageDao,
        channelPostDao: com.hasani.messageapp.data.local.dao.ChannelPostDao,
        userDao: com.hasani.messageapp.data.local.dao.UserDao,
        chatDao: com.hasani.messageapp.data.local.dao.ChatDao,
        groupDao: com.hasani.messageapp.data.local.dao.GroupDao,
        channelDao: com.hasani.messageapp.data.local.dao.ChannelDao,
        channelSubscriberDao: com.hasani.messageapp.data.local.dao.ChannelSubscriberDao,
        contactsRepository: com.hasani.messageapp.data.repository.ContactsRepository,
        sessionManager: SessionManager,
        notificationHelper: com.hasani.messageapp.data.notification.NotificationHelper,
        notificationBadgeManager: com.hasani.messageapp.data.notification.NotificationBadgeManager,
        soundPlayer: com.hasani.messageapp.data.media.SoundPlayer,
        currentChatManager: com.hasani.messageapp.data.session.CurrentChatManager,
        gson: com.google.gson.Gson
    ): com.hasani.messageapp.data.websocket.WebSocketManager = 
        com.hasani.messageapp.data.websocket.WebSocketManager(
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
        webSocketManager: com.hasani.messageapp.data.websocket.WebSocketManager,
        sessionManager: SessionManager,
        messageDao: MessageDao,
        groupMessageDao: com.hasani.messageapp.data.local.dao.GroupMessageDao,
        channelPostDao: com.hasani.messageapp.data.local.dao.ChannelPostDao,
        userDao: com.hasani.messageapp.data.local.dao.UserDao,
        groupDao: com.hasani.messageapp.data.local.dao.GroupDao,
        channelDao: com.hasani.messageapp.data.local.dao.ChannelDao
    ): com.hasani.messageapp.data.sync.GlobalSyncManager = 
        com.hasani.messageapp.data.sync.GlobalSyncManager(
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

