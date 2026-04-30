package com.iliyadev.springboot.config

import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

// ═══════════════════════════════════════════════════════════════════════════════
// 🔴 Redis Configuration - Lettuce async client (Optional)
// Gracefully skips Redis if not available — app runs without caching
// ═══════════════════════════════════════════════════════════════════════════════

@Configuration
@ConditionalOnProperty(name = ["redis.enabled"], havingValue = "true", matchIfMissing = false)
class RedisConfig(
    @Value("\${redis.host:localhost}") private val host: String,
    @Value("\${redis.port:6379}") private val port: Int
) {
    private val logger = LoggerFactory.getLogger(RedisConfig::class.java)
    private lateinit var _redisClient: RedisClient

    @PostConstruct
    fun init() {
        logger.info("🔴 Redis config loaded — will connect to $host:$port")
    }

    @Bean
    fun redisClient(): RedisClient {
        val uri = RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .build()
        _redisClient = RedisClient.create(uri)
        return _redisClient
    }

    @Bean
    @Primary
    fun redisConnection(redisClient: RedisClient): StatefulRedisConnection<String, String> {
        val connection: StatefulRedisConnection<String, String> = redisClient.connect()
        logger.info("✅ Redis connected successfully")
        return connection
    }

    @Bean
    fun redisCommands(connection: StatefulRedisConnection<String, String>): RedisCommands<String, String> {
        return connection.sync()
    }

    @Bean
    fun redisAsyncCommands(connection: StatefulRedisConnection<String, String>): RedisAsyncCommands<String, String> {
        return connection.async()
    }

    @Bean
    fun redisPubSubConnection(redisClient: RedisClient): StatefulRedisPubSubConnection<String, String> {
        return redisClient.connectPubSub()
    }

    @PreDestroy
    fun shutdown() {
        if (::_redisClient.isInitialized) {
            _redisClient.shutdown()
            logger.info("🔴 Redis connection closed")
        }
    }
}
