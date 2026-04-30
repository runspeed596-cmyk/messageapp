package com.iliyadev.springboot.services.media

import com.iliyadev.springboot.config.MinioConfig
import io.minio.GetObjectArgs
import io.minio.PutObjectArgs
import io.minio.MinioClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📤 Chunked Upload/Download Service - Never load full media into RAM
// Uses streaming I/O with 5MB buffer chunks
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ChunkedStorageService(
    private val minioClient: MinioClient
) {
    private val logger = LoggerFactory.getLogger(ChunkedStorageService::class.java)
    private val CHUNK_SIZE: Int = 5 * 1024 * 1024

    fun uploadStream(
        bucket: String,
        objectName: String,
        inputStream: InputStream,
        contentType: String,
        size: Long
    ): String {
        val mediaId: String = "${UUID.randomUUID()}_$objectName"
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(mediaId)
                .stream(inputStream, size, CHUNK_SIZE.toLong())
                .contentType(contentType)
                .build()
        )
        logger.info("📤 Uploaded $mediaId to $bucket (${size / 1024}KB)")
        return mediaId
    }

    fun downloadStream(bucket: String, objectName: String): InputStream {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(objectName)
                .build()
        )
    }

    fun uploadImage(fileName: String, inputStream: InputStream, size: Long): String {
        return uploadStream(MinioConfig.BUCKET_IMAGES, fileName, inputStream, "image/webp", size)
    }

    fun uploadThumbnail(fileName: String, inputStream: InputStream, size: Long): String {
        return uploadStream(MinioConfig.BUCKET_THUMBNAILS, fileName, inputStream, "image/webp", size)
    }

    fun uploadVoice(fileName: String, inputStream: InputStream, size: Long): String {
        return uploadStream(MinioConfig.BUCKET_VOICE, fileName, inputStream, "audio/ogg", size)
    }

    fun uploadVideo(fileName: String, inputStream: InputStream, size: Long): String {
        return uploadStream(MinioConfig.BUCKET_VIDEO, fileName, inputStream, "video/mp4", size)
    }

    fun uploadFile(fileName: String, inputStream: InputStream, size: Long, contentType: String): String {
        return uploadStream(MinioConfig.BUCKET_FILES, fileName, inputStream, contentType, size)
    }

    fun uploadStory(fileName: String, inputStream: InputStream, size: Long, contentType: String): String {
        return uploadStream(MinioConfig.BUCKET_STORIES, fileName, inputStream, contentType, size)
    }
}
