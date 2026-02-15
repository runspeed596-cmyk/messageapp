package com.Kelasor.app.data.media

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * Singleton provider for ExoPlayer media cache.
 * Uses SimpleCache with LRU eviction (250 MB) to persist voice and video
 * messages to local disk, preventing repeated server downloads.
 */
@UnstableApi
object MediaCacheProvider {
    private const val CACHE_DIR_NAME = "media_cache"
    private const val MAX_CACHE_SIZE_BYTES: Long = 250L * 1024L * 1024L // 250 MB
    @Volatile
    private var cache: SimpleCache? = null
    fun getCache(context: Context): SimpleCache {
        return cache ?: synchronized(this) {
            cache ?: createCache(context).also { cache = it }
        }
    }
    private fun createCache(context: Context): SimpleCache {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE_BYTES)
        val databaseProvider = StandaloneDatabaseProvider(context)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }
    fun release() {
        synchronized(this) {
            cache?.release()
            cache = null
        }
    }
}
