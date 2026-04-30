package com.iliyadev.springboot.services.media

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Image

// ═══════════════════════════════════════════════════════════════════════════════
// 🗜️ Media Compression Service
// Images: WebP (fallback PNG), resize max 1920px, 200px thumbnail
// Voice: Opus via FFmpeg subprocess (32kbps mono)
// Video: H.265 via FFmpeg subprocess (720p, CRF 28)
// Files: NEVER compressed (passthrough)
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class MediaCompressionService(
    private val storageService: ChunkedStorageService
) {
    private val logger = LoggerFactory.getLogger(MediaCompressionService::class.java)
    private val MAX_IMAGE_SIZE: Int = 1920
    private val THUMBNAIL_SIZE: Int = 200
    private val VIDEO_MAX_HEIGHT: Int = 720
    private val VOICE_BITRATE: String = "32k"
    private val VIDEO_CRF: String = "28"

    data class MediaUploadResult(
        val mediaId: String,
        val thumbnailId: String?,
        val originalSize: Long,
        val compressedSize: Long,
        val mediaType: String
    )

    fun processAndUploadImage(fileName: String, inputStream: InputStream, originalSize: Long): MediaUploadResult {
        val imageBytes: ByteArray = inputStream.readBytes()
        val image: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: run {
                val mediaId: String = storageService.uploadImage(fileName, ByteArrayInputStream(imageBytes), originalSize)
                return MediaUploadResult(mediaId, null, originalSize, originalSize, "image")
            }
        val resized: BufferedImage = resizeImage(image, MAX_IMAGE_SIZE)
        val compressedBytes: ByteArray = compressToWebP(resized)
        val thumbnailImage: BufferedImage = resizeImage(image, THUMBNAIL_SIZE)
        val thumbnailBytes: ByteArray = compressToWebP(thumbnailImage)
        val mediaId: String = storageService.uploadImage(
            "$fileName.webp",
            ByteArrayInputStream(compressedBytes),
            compressedBytes.size.toLong()
        )
        val thumbnailId: String = storageService.uploadThumbnail(
            "thumb_$fileName.webp",
            ByteArrayInputStream(thumbnailBytes),
            thumbnailBytes.size.toLong()
        )
        logger.info("🖼️ Image compressed: ${originalSize / 1024}KB → ${compressedBytes.size / 1024}KB")
        return MediaUploadResult(mediaId, thumbnailId, originalSize, compressedBytes.size.toLong(), "image")
    }

    fun processAndUploadVoice(fileName: String, inputStream: InputStream, originalSize: Long): MediaUploadResult {
        val tempInput = createTempFile("voice_in_", ".ogg")
        val tempOutput = createTempFile("voice_out_", ".ogg")
        try {
            tempInput.outputStream().use { inputStream.copyTo(it) }
            val process = ProcessBuilder(
                "ffmpeg", "-y", "-i", tempInput.absolutePath,
                "-c:a", "libopus", "-b:a", VOICE_BITRATE, "-ac", "1",
                tempOutput.absolutePath
            ).redirectErrorStream(true).start()
            process.waitFor()
            val compressedSize: Long = tempOutput.length()
            val outputStream = tempOutput.inputStream()
            val mediaId: String = storageService.uploadVoice(
                "$fileName.ogg", outputStream, compressedSize
            )
            logger.info("🎤 Voice compressed: ${originalSize / 1024}KB → ${compressedSize / 1024}KB")
            return MediaUploadResult(mediaId, null, originalSize, compressedSize, "voice")
        } catch (e: Exception) {
            logger.warn("FFmpeg not available, uploading raw voice: ${e.message}")
            tempInput.inputStream().use { raw ->
                val mediaId: String = storageService.uploadVoice(fileName, raw, originalSize)
                return MediaUploadResult(mediaId, null, originalSize, originalSize, "voice")
            }
        } finally {
            tempInput.delete()
            tempOutput.delete()
        }
    }

    fun processAndUploadVideo(fileName: String, inputStream: InputStream, originalSize: Long): MediaUploadResult {
        val tempInput = createTempFile("video_in_", ".mp4")
        val tempOutput = createTempFile("video_out_", ".mp4")
        val tempThumb = createTempFile("thumb_", ".webp")
        try {
            tempInput.outputStream().use { inputStream.copyTo(it) }
            val videoProcess = ProcessBuilder(
                "ffmpeg", "-y", "-i", tempInput.absolutePath,
                "-c:v", "libx265", "-preset", "fast", "-crf", VIDEO_CRF,
                "-vf", "scale=-2:${VIDEO_MAX_HEIGHT}",
                "-c:a", "aac", "-b:a", "64k",
                tempOutput.absolutePath
            ).redirectErrorStream(true).start()
            videoProcess.waitFor()
            val thumbProcess = ProcessBuilder(
                "ffmpeg", "-y", "-i", tempInput.absolutePath,
                "-vframes", "1", "-vf", "scale=${THUMBNAIL_SIZE}:-2",
                tempThumb.absolutePath
            ).redirectErrorStream(true).start()
            thumbProcess.waitFor()
            val compressedSize: Long = tempOutput.length()
            val mediaId: String = storageService.uploadVideo(
                "$fileName.mp4", tempOutput.inputStream(), compressedSize
            )
            var thumbnailId: String? = null
            if (tempThumb.exists() && tempThumb.length() > 0) {
                thumbnailId = storageService.uploadThumbnail(
                    "thumb_$fileName.webp", tempThumb.inputStream(), tempThumb.length()
                )
            }
            logger.info("🎬 Video compressed: ${originalSize / 1024}KB → ${compressedSize / 1024}KB")
            return MediaUploadResult(mediaId, thumbnailId, originalSize, compressedSize, "video")
        } catch (e: Exception) {
            logger.warn("FFmpeg not available, uploading raw video: ${e.message}")
            tempInput.inputStream().use { raw ->
                val mediaId: String = storageService.uploadVideo(fileName, raw, originalSize)
                return MediaUploadResult(mediaId, null, originalSize, originalSize, "video")
            }
        } finally {
            tempInput.delete()
            tempOutput.delete()
            tempThumb.delete()
        }
    }

    fun uploadFile(fileName: String, inputStream: InputStream, size: Long, contentType: String): MediaUploadResult {
        val mediaId: String = storageService.uploadFile(fileName, inputStream, size, contentType)
        logger.info("📎 File uploaded (NO compression): $fileName (${size / 1024}KB)")
        return MediaUploadResult(mediaId, null, size, size, "file")
    }

    private fun resizeImage(image: BufferedImage, maxSize: Int): BufferedImage {
        val width: Int = image.width
        val height: Int = image.height
        if (width <= maxSize && height <= maxSize) return image
        val ratio: Double = minOf(maxSize.toDouble() / width, maxSize.toDouble() / height)
        val newWidth: Int = (width * ratio).toInt()
        val newHeight: Int = (height * ratio).toInt()
        val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        val g = resized.createGraphics()
        g.drawImage(image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null)
        g.dispose()
        return resized
    }

    private fun compressToWebP(image: BufferedImage): ByteArray {
        val baos = ByteArrayOutputStream()
        val writers = ImageIO.getImageWritersByMIMEType("image/webp")
        if (writers.hasNext()) {
            val writer = writers.next()
            val ios = ImageIO.createImageOutputStream(baos)
            writer.output = ios
            writer.write(image)
            ios.flush()
            writer.dispose()
        } else {
            ImageIO.write(image, "png", baos)
        }
        return baos.toByteArray()
    }

    private fun createTempFile(prefix: String, suffix: String): java.io.File {
        return java.io.File.createTempFile(prefix, suffix).apply { deleteOnExit() }
    }
}
