package com.hasani.messageapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography

/**
 * WhatsApp-style animated attachment menu.
 * Shows when user taps the + button with options for File, Gallery, Audio.
 */

data class AttachmentOption(
    val icon: ImageVector,
    val label: String,
    val backgroundColor: Color,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

@Composable
fun AttachmentMenu(
    visible: Boolean,
    onDismiss: () -> Unit,
    onFileClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onAudioClick: () -> Unit,
    onLocationClick: () -> Unit = {},
    onPollClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    // Options with beautiful gradient colors
    val options = listOfNotNull(
        AttachmentOption(
            icon = Icons.Default.Folder,
            label = "فایل",
            backgroundColor = Color(0xFF7C4DFF),
            onClick = onFileClick
        ),
        AttachmentOption(
            icon = Icons.Default.Image,
            label = "گالری",
            backgroundColor = Color(0xFF00BFA5),
            onClick = onGalleryClick
        ),
        AttachmentOption(
            icon = Icons.Default.AudioFile,
            label = "صوتی",
            backgroundColor = Color(0xFFFF6D00),
            onClick = onAudioClick
        ),
        AttachmentOption(
            icon = Icons.Default.LocationOn,
            label = "موقعیت",
            backgroundColor = Color(0xFF9E9E9E),
            enabled = true,
            onClick = onLocationClick
        ),
        if (onPollClick != null) AttachmentOption(
            icon = Icons.Default.Poll,
            label = "نظرسنجی",
            backgroundColor = Color(0xFFE91E63),
            onClick = onPollClick
        ) else null
    )

    // Backdrop + Menu
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() }
        ) {
            // Menu container
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(150)
                ) + scaleOut(targetScale = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 80.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* Consume click to prevent dismissal */ },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 16.dp,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title
                        Text(
                            text = "انتخاب نوع پیوست",
                            style = MessageAppTypography.chatName,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Options grid - using aligned columns
                        val chunkedOptions = options.chunked(3)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            chunkedOptions.forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    rowOptions.forEach { option ->
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AttachmentOptionItem(
                                                option = option,
                                                onDismiss = onDismiss
                                            )
                                        }
                                    }
                                    // Fill empty slots to maintain alignment
                                    val emptySlots = 3 - rowOptions.size
                                    repeat(emptySlots) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentOptionItem(
    option: AttachmentOption,
    onDismiss: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (option.enabled) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "optionScale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                enabled = option.enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
                option.onClick()
            }
            .padding(8.dp)
    ) {
        // Circular icon button
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (option.enabled) option.backgroundColor 
                    else option.backgroundColor.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Text(
            text = option.label,
            style = MessageAppTypography.chatTime,
            color = if (option.enabled) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
