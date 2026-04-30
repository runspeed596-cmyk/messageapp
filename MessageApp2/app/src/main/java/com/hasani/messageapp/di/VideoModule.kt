package com.hasani.messageapp.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VideoModule {

    @Provides
    @Singleton
    fun provideVideoCache(@ApplicationContext context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, "video_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val evictor = LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024) // 200MB Cache
        val databaseProvider = StandaloneDatabaseProvider(context)
        
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }

    @Provides
    @Singleton
    fun provideDataSourceFactory(
        @ApplicationContext context: Context,
        cache: SimpleCache,
        okHttpClient: OkHttpClient
    ): DataSource.Factory {
        val upstreamFactory = OkHttpDataSource.Factory(okHttpClient)
        
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @dagger.hilt.EntryPoint
    @InstallIn(SingletonComponent::class)
    interface VideoCacheEntryPoint {
        fun getDataSourceFactory(): DataSource.Factory
    }
}
