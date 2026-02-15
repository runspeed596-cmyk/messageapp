package com.iliyadev.springboot.services

import org.springframework.stereotype.Service
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * Service for generating and caching image thumbnails on-demand.
 * Resizes images to the requested width while maintaining aspect ratio.
 * Caches generated thumbnails on the file system to avoid re-processing.
 */
@Service
class ThumbnailService {
    private val uploadDir: String = "uploads"
    private val thumbDir: String = "uploads/thumbnails"

    init {
        val dir = File(thumbDir)
        if (!dir.exists()) dir.mkdirs()
    }

    /**
     * Get or generate a thumbnail for the given filename.
     * Returns the thumbnail file bytes, or null if the source is not a valid image.
     */
    fun getThumbnail(filename: String, width: Int = 200, quality: Int = 60): ByteArray? {
        val sourceFile = File(uploadDir, filename)
        if (!sourceFile.exists()) return null
        val safeWidth = width.coerceIn(50, 800)
        val thumbFileName = "thumb_${safeWidth}_${filename}"
        val thumbFile = File(thumbDir, thumbFileName)
        // Return cached thumbnail if it exists and source hasn't changed
        if (thumbFile.exists() && thumbFile.lastModified() >= sourceFile.lastModified()) {
            return thumbFile.readBytes()
        }
        // Generate thumbnail
        return try {
            val originalImage: BufferedImage = ImageIO.read(sourceFile) ?: return null
            val originalWidth: Int = originalImage.width
            val originalHeight: Int = originalImage.height
            if (originalWidth <= safeWidth) {
                // Image is already small enough, return original
                return sourceFile.readBytes()
            }
            val scaledHeight: Int = (originalHeight.toDouble() * safeWidth / originalWidth).toInt()
            val scaledImage: Image = originalImage.getScaledInstance(safeWidth, scaledHeight, Image.SCALE_SMOOTH)
            val outputImage = BufferedImage(safeWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
            val graphics = outputImage.createGraphics()
            graphics.drawImage(scaledImage, 0, 0, null)
            graphics.dispose()
            val outputStream = ByteArrayOutputStream()
            val writers = ImageIO.getImageWritersByFormatName("jpeg")
            if (writers.hasNext()) {
                val writer = writers.next()
                val param = writer.defaultWriteParam
                if (param.canWriteCompressed()) {
                    param.compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
                    param.compressionQuality = quality / 100f
                }
                writer.output = ImageIO.createImageOutputStream(outputStream)
                writer.write(null, javax.imageio.IIOImage(outputImage, null, null), param)
                writer.dispose()
            } else {
                ImageIO.write(outputImage, "jpeg", outputStream)
            }
            val thumbBytes = outputStream.toByteArray()
            // Cache to disk
            thumbFile.writeBytes(thumbBytes)
            thumbBytes
        } catch (e: Exception) {
            null
        }
    }
}
