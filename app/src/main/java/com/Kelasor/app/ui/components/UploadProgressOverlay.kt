package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography

/**
 * Upload progress overlay shown during file upload.
 * Displays circular progress, percentage, and cancel button.
 */

@Composable
fun UploadProgressOverlay(
    visible: Boolean,
    progress: Float, // 0.0 to 1.0
    fileName: String = "در حال آپلود...",
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "uploadProgress"
    )
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Progress indicator with percentage
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(120.dp)
                ) {
                    // Background circle
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(100.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    
                    // Progress circle
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(100.dp),
                        color = extendedColors.accent,
                        strokeWidth = 8.dp,
                        strokeCap = StrokeCap.Round
                    )
                    
                    // Percentage text
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MessageAppTypography.chatName,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // File name
                Text(
                    text = fileName,
                    style = MessageAppTypography.chatTime,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Cancel button
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "لغو آپلود",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
