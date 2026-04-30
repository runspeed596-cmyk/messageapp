package com.iliyadev.springboot.config

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 MinIO Object Storage Configuration
// Buckets: media-images, media-voice, media-video, media-files,
//          media-stories, media-thumbnails
// ═══════════════════════════════════════════════════════════════════════════════

@Configuration
class MinioConfig(
    @Value("\${minio.endpoint:http://localhost:9000}") private val endpoint: String,
    @Value("\${minio.access-key:minioadmin}") private val accessKey: String,
    @Value("\${minio.secret-key:minioadmin123}") private val secretKey: String
) {
    private val logger = LoggerFactory.getLogger(MinioConfig::class.java)

    companion object {
        const val BUCKET_IMAGES: String = "media-images"
        const val BUCKET_VOICE: String = "media-voice"
        const val BUCKET_VIDEO: String = "media-video"
        const val BUCKET_FILES: String = "media-files"
        const val BUCKET_STORIES: String = "media-stories"
        const val BUCKET_THUMBNAILS: String = "media-thumbnails"
        val ALL_BUCKETS: List<String> = listOf(BUCKET_IMAGES, BUCKET_VOICE, BUCKET_VIDEO, BUCKET_FILES, BUCKET_STORIES, BUCKET_THUMBNAILS)
    }

    @Bean
    fun minioClient(): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }

    @PostConstruct
    fun initBuckets() {
        try {
            val client: MinioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build()
            ALL_BUCKETS.forEach { bucket ->
                val exists: Boolean = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
                if (!exists) {
                    client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
                    logger.info("✅ Created MinIO bucket: $bucket")
                } else {
                    logger.info("📦 MinIO bucket exists: $bucket")
                }
            }
            logger.info("✅ MinIO initialized successfully at $endpoint")
        } catch (e: Exception) {
            logger.warn("⚠️ MinIO not available at $endpoint: ${e.message}. Media features will be unavailable.")
        }
    }
}
