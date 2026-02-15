package com.Kelasor.app.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Full-screen media preview dialog.
 * Supports images and videos with zoom, playback controls, and save-to-gallery.
 */

enum class MediaType {
    IMAGE, VIDEO, UNKNOWN
}

@Composable
fun MediaPreviewDialog(
    mediaUrl: String,
    mediaType: MediaType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val extendedColors = MessageAppTheme.extendedColors
    
    // Resolve full URL
    val fullUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(mediaUrl) ?: ""
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Zoom state for images
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (mediaType) {
                MediaType.IMAGE -> {
                    // Image with zoom and pan
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fullUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "تصویر",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            )
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    if (scale > 1f) {
                                        offsetX += pan.x
                                        offsetY += pan.y
                                    } else {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (scale > 1f) {
                                            scale = 1f
                                            offsetX = 0f
                                            offsetY = 0f
                                        } else {
                                            scale = 2.5f
                                        }
                                    }
                                )
                            },
                        onSuccess = { isLoading = false },
                        onError = { isLoading = false }
                    )
                }
                
                MediaType.VIDEO -> {
                    VideoPlayer(
                        videoUrl = fullUrl,
                        modifier = Modifier.fillMaxSize(),
                        onLoaded = { isLoading = false }
                    )
                }
                
                MediaType.UNKNOWN -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "فرمت پشتیبانی نمی‌شود",
                            color = Color.White
                        )
                    }
                    isLoading = false
                }
            }
            
            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            }
            
            // Top bar with close and download buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "بستن",
                        tint = Color.White
                    )
                }
                
                // Save to gallery button
                IconButton(
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            scope.launch {
                                val success = saveMediaToGallery(context, fullUrl, mediaType)
                                isSaving = false
                                val message = if (success) "ذخیره شد" else "خطا در ذخیره"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(extendedColors.accent.copy(alpha = 0.8f))
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "ذخیره در گالری",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
private fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onLoaded: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Get the cached DataSource.Factory from Hilt EntryPoint
    val dataSourceFactory = remember {
        dagger.hilt.android.EntryPointAccessors.fromApplication(
            context,
            com.Kelasor.app.di.VideoModule.VideoCacheEntryPoint::class.java
        ).getDataSourceFactory()
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
            )
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            onLoaded()
                        }
                    }
                })
            }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Save media file to device gallery.
 */
private suspend fun saveMediaToGallery(
    context: Context,
    mediaUrl: String,
    mediaType: MediaType
): Boolean = withContext(Dispatchers.IO) {
    try {
        // Download file
        val url = URL(mediaUrl)
        val connection = url.openConnection()
        connection.connect()
        val inputStream = connection.getInputStream()
        
        val fileName = "MessageApp_${System.currentTimeMillis()}"
        val extension = when (mediaType) {
            MediaType.IMAGE -> ".jpg"
            MediaType.VIDEO -> ".mp4"
            MediaType.UNKNOWN -> ""
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + extension)
                put(MediaStore.MediaColumns.MIME_TYPE, 
                    if (mediaType == MediaType.IMAGE) "image/jpeg" else "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, 
                    if (mediaType == MediaType.IMAGE) Environment.DIRECTORY_PICTURES 
                    else Environment.DIRECTORY_MOVIES)
            }
            
            val uri = context.contentResolver.insert(
                if (mediaType == MediaType.IMAGE) MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                true
            } ?: false
        } else {
            // Legacy storage for older devices
            val directory = if (mediaType == MediaType.IMAGE) {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            } else {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            }
            
            val file = File(directory, fileName + extension)
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Detect media type from URL or MIME type.
 */
fun detectMediaType(url: String, mimeType: String? = null): MediaType {
    val urlLower = url.lowercase()
    val mimeLower = mimeType?.lowercase() ?: ""
    
    return when {
        mimeLower.startsWith("image/") || 
        urlLower.endsWith(".jpg") || 
        urlLower.endsWith(".jpeg") || 
        urlLower.endsWith(".png") || 
        urlLower.endsWith(".gif") || 
        urlLower.endsWith(".webp") -> MediaType.IMAGE
        
        mimeLower.startsWith("video/") || 
        urlLower.endsWith(".mp4") || 
        urlLower.endsWith(".mov") || 
        urlLower.endsWith(".avi") || 
        urlLower.endsWith(".webm") -> MediaType.VIDEO
        
        else -> MediaType.UNKNOWN
    }
}
