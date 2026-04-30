package com.hasani.messageapp.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * File message bubble with download progress, save to device, and open with external app.
 */

enum class FileDownloadState {
    NOT_DOWNLOADED,
    DOWNLOADING,
    DOWNLOADED
}

@Composable
fun FileMessageBubble(
    mediaUrl: String,
    fileName: String,
    fileSize: String = "",
    isMyMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extendedColors = MessageAppTheme.extendedColors
    
    // Resolve full URL
    val fullMediaUrl = if (mediaUrl.startsWith("http://") || mediaUrl.startsWith("https://")) {
        mediaUrl
    } else {
        "${Constants.BASE_URL.removeSuffix("/")}$mediaUrl"
    }
    
    var downloadState by remember { mutableStateOf(FileDownloadState.NOT_DOWNLOADED) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadedFile by remember { mutableStateOf<File?>(null) }
    
    // Check if already downloaded
    val cacheFile = File(context.cacheDir, "downloads/$fileName")
    if (cacheFile.exists() && downloadState == FileDownloadState.NOT_DOWNLOADED) {
        downloadState = FileDownloadState.DOWNLOADED
        downloadedFile = cacheFile
    }
    
    val backgroundColor = if (isMyMessage) {
        extendedColors.accent.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val accentColor = if (isMyMessage) extendedColors.accent else MaterialTheme.colorScheme.primary
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable {
                when (downloadState) {
                    FileDownloadState.NOT_DOWNLOADED -> {
                        // Start download
                        downloadState = FileDownloadState.DOWNLOADING
                        scope.launch {
                            val file = downloadFile(context, fullMediaUrl, fileName) { progress ->
                                downloadProgress = progress
                            }
                            if (file != null) {
                                downloadedFile = file
                                downloadState = FileDownloadState.DOWNLOADED
                            } else {
                                downloadState = FileDownloadState.NOT_DOWNLOADED
                                Toast.makeText(context, "خطا در دانلود", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    FileDownloadState.DOWNLOADED -> {
                        // Open file with external app
                        downloadedFile?.let { file ->
                            openFileWithExternalApp(context, file, fileName)
                        }
                    }
                    FileDownloadState.DOWNLOADING -> {
                        // Already downloading, do nothing
                    }
                }
            }
            .padding(12.dp)
            .width(250.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File icon with state indicator
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                when (downloadState) {
                    FileDownloadState.NOT_DOWNLOADED -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "دانلود",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    FileDownloadState.DOWNLOADING -> {
                        CircularProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.size(32.dp),
                            color = accentColor,
                            strokeWidth = 3.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                    FileDownloadState.DOWNLOADED -> {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "باز کردن",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // File info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = fileName.take(30) + if (fileName.length > 30) "..." else "",
                style = MessageAppTypography.chatTime,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = when (downloadState) {
                    FileDownloadState.NOT_DOWNLOADED -> "برای دانلود کلیک کنید"
                    FileDownloadState.DOWNLOADING -> "در حال دانلود... ${(downloadProgress * 100).toInt()}%"
                    FileDownloadState.DOWNLOADED -> "برای باز کردن کلیک کنید"
                },
                style = MessageAppTypography.messageTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Save button (only when downloaded)
        AnimatedVisibility(
            visible = downloadState == FileDownloadState.DOWNLOADED,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = {
                    downloadedFile?.let { file ->
                        scope.launch {
                            val saved = saveFileToDevice(context, file, fileName)
                            val message = if (saved) "ذخیره شد" else "خطا در ذخیره"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = "ذخیره در دستگاه",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Download file from URL with progress callback.
 */
private suspend fun downloadFile(
    context: Context,
    url: String,
    fileName: String,
    onProgress: (Float) -> Unit
): File? = withContext(Dispatchers.IO) {
    try {
        val downloadDir = File(context.cacheDir, "downloads")
        if (!downloadDir.exists()) downloadDir.mkdirs()
        
        val outputFile = File(downloadDir, fileName)
        
        val connection = URL(url).openConnection()
        connection.connect()
        
        val totalSize = connection.contentLength
        var downloadedSize = 0
        
        connection.getInputStream().use { input ->
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead
                    if (totalSize > 0) {
                        withContext(Dispatchers.Main) {
                            onProgress(downloadedSize.toFloat() / totalSize.toFloat())
                        }
                    }
                }
            }
        }
        
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Open file with external app using FileProvider.
 */
private fun openFileWithExternalApp(context: Context, file: File, fileName: String) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        
        val mimeType = getMimeType(fileName) ?: "*/*"
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "برنامه‌ای برای باز کردن این فایل یافت نشد", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "خطا در باز کردن فایل", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Save file to device Downloads folder.
 */
private suspend fun saveFileToDevice(context: Context, file: File, fileName: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val mimeType = getMimeType(fileName) ?: "application/octet-stream"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(it, contentValues, null, null)
                true
            } ?: false
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val destFile = File(downloadsDir, fileName)
            file.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Get MIME type from file name.
 */
private fun getMimeType(fileName: String): String? {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}
