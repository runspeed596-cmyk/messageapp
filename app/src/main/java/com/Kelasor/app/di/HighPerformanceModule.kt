package com.Kelasor.app.di

import com.Kelasor.app.data.cache.RamCacheManager
import com.Kelasor.app.data.websocket.BinaryWsClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 💉 High-Performance Module - Hilt DI for WebSocket + Cache
// ═══════════════════════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object HighPerformanceModule {
    @Provides
    @Singleton
    fun provideBinaryWsClient(): BinaryWsClient = BinaryWsClient()

    @Provides
    @Singleton
    fun provideRamCacheManager(): RamCacheManager = RamCacheManager()
}
