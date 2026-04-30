package com.Kelasor.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Client-side media compression to speed up uploads.
 * Compresses images before upload to reduce file size significantly.
 */
object MediaCompressor {
    private const val TAG = "MediaCompressor"
    private const val MAX_IMAGE_DIMENSION = 1920
    private const val JPEG_QUALITY = 80
    private const val WEBP_QUALITY = 75
    /**
     * Compress an image URI to a smaller JPEG file.
     * Resizes to max 1920px on longest side and compresses to 80% quality.
     * Typical result: 5-15MB → 200-500KB
     */
    fun compressImage(context: Context, uri: Uri, outputFile: File): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val rotation = try {
                val tempExif = File.createTempFile("exif_check", ".tmp", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { exifInput ->
                    tempExif.outputStream().use { out -> exifInput.copyTo(out) }
                }
                val r = getRotationFromExif(ExifInterface(tempExif.absolutePath))
                tempExif.delete()
                r
            } catch (e: Exception) { 0 }
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            val origWidth = options.outWidth
            val origHeight = options.outHeight
            val sampleSize = calculateSampleSize(origWidth, origHeight, MAX_IMAGE_DIMENSION)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decodeStream = context.contentResolver.openInputStream(uri) ?: return false
            var bitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()
            if (bitmap == null) return false
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_DIMENSION)
            if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (rotated != bitmap) bitmap.recycle()
                bitmap = rotated
            }
            FileOutputStream(outputFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)
                fos.flush()
            }
            bitmap.recycle()
            val originalSize = getFileSize(context, uri)
            val compressedSize = outputFile.length()
            Log.i(TAG, "✅ Compressed image: ${originalSize / 1024}KB → ${compressedSize / 1024}KB (${(compressedSize * 100 / maxOf(originalSize, 1))}%)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Image compression failed: ${e.message}")
            false
        }
    }
    private fun calculateSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var sampleSize = 1
        val longerSide = maxOf(width, height)
        if (longerSide > maxDim) {
            while (longerSide / sampleSize > maxDim * 2) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }
    private fun resizeBitmap(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val scale = maxDim.toFloat() / maxOf(width, height).toFloat()
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        if (scaled != bitmap) bitmap.recycle()
        return scaled
    }
    private fun getRotationFromExif(exif: ExifInterface?): Int {
        if (exif == null) return 0
        return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.available().toLong() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
