package com.hasani.messageapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hasani.messageapp.domain.model.Message
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily
import kotlinx.coroutines.delay

/**
 * Telegram-style full-screen message actions overlay.
 * Opens with single tap, shows blurred background, animated emoji reactions,
 * and action menu.
 */
@Composable
fun MessageActionsOverlay(
    visible: Boolean,
    message: Message?,
    messageContent: @Composable () -> Unit,
    isOwner: Boolean,
    showDeleteForEveryone: Boolean = true,
    replyLabel: String = "پاسخ",
    reactionCount: Int = 0,
    onDismiss: () -> Unit,
    onReactionClick: (String) -> Unit,
    onReplyClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: ((deleteForEveryone: Boolean) -> Unit)? = null,
    onPinClick: (() -> Unit)? = null,
    onForwardClick: (() -> Unit)? = null,
    onCopyLinkClick: (() -> Unit)? = null,
    onSelectClick: (() -> Unit)? = null,
    onLockContentClick: (() -> Unit)? = null // Feature 2: Lock content (UI only)
) {
    val extendedColors = MessageAppTheme.extendedColors
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteForEveryone by remember { mutableStateOf(true) }
    // Animation states
    var isAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(50)
            isAnimated = true
        } else {
            isAnimated = false
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (isAnimated) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    // Delete confirmation dialog
    if (showDeleteDialog && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف پیام", fontFamily = VazirFontFamily) },
            text = {
                Column {
                    Text("آیا از حذف این پیام اطمینان دارید؟", fontFamily = VazirFontFamily)
                    if (showDeleteForEveryone) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { deleteForEveryone = !deleteForEveryone }
                        ) {
                            Checkbox(
                                checked = deleteForEveryone,
                                onCheckedChange = { deleteForEveryone = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حذف برای همه", fontFamily = VazirFontFamily)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(deleteForEveryone)
                        onDismiss()
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontFamily = VazirFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف", fontFamily = VazirFontFamily)
                }
            }
        )
    }
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures { onDismiss() }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { /* Consume taps to prevent dismiss */ }
                        }
                ) {
                    // Emoji Reaction Bar with bounce animation
                    AnimatedVisibility(
                        visible = isAnimated,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        EmojiReactionBar(
                            onReactionClick = { emoji ->
                                onReactionClick(emoji)
                                onDismiss()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Message bubble with scale animation
                    Box(
                        modifier = Modifier
                            .scale(scale)
                            .widthIn(max = LocalConfiguration.current.screenWidthDp.dp - 32.dp)
                    ) {
                        messageContent()
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Reaction count indicator
                    if (reactionCount > 0) {
                        AnimatedVisibility(
                            visible = isAnimated,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("👏", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$reactionCount واکنش",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = VazirFontFamily,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    // Action menu card
                    AnimatedVisibility(
                        visible = isAnimated,
                        enter = expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            expandFrom = Alignment.Top
                        ) + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ActionMenuCard(
                            isOwner = isOwner,
                            replyLabel = replyLabel,
                            onReplyClick = {
                                onReplyClick?.invoke()
                                onDismiss()
                            },
                            onCopyClick = {
                                onCopyClick?.invoke()
                                onDismiss()
                            },
                            onPinClick = onPinClick?.let { {
                                it.invoke()
                                onDismiss()
                            } },
                            onCopyLinkClick = onCopyLinkClick?.let { {
                                it.invoke()
                                onDismiss()
                            } },
                            onForwardClick = onForwardClick?.let { {
                                it.invoke()
                                onDismiss()
                            } },
                            onLockContentClick = onLockContentClick?.let { {
                                it.invoke()
                                onDismiss()
                            } },
                            onEditClick = if (isOwner) onEditClick?.let { {
                                it.invoke()
                                onDismiss()
                            } } else null,
                            onDeleteClick = if (isOwner) { { showDeleteDialog = true } } else null,
                            onSelectClick = {
                                onSelectClick?.invoke()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Emoji reaction bar with animated emojis
 */
@Composable
private fun EmojiReactionBar(
    onReactionClick: (String) -> Unit
) {
    val reactions = listOf("❤️", "👍", "👎", "😂", "🔥", "😱")
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEachIndexed { index, emoji ->
                var isPressed by remember { mutableStateOf(false) }
                val emojiScale by animateFloatAsState(
                    targetValue = if (isPressed) 1.3f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "emoji_scale_$index"
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            isPressed = true
                            onReactionClick(emoji)
                        }
                        .scale(emojiScale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

/**
 * Action menu card with Telegram-style actions
 */
@Composable
private fun ActionMenuCard(
    isOwner: Boolean,
    replyLabel: String,
    onReplyClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPinClick: (() -> Unit)? = null,
    onCopyLinkClick: (() -> Unit)? = null,
    onForwardClick: (() -> Unit)? = null,
    onLockContentClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onSelectClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .widthIn(min = 200.dp, max = 280.dp)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Reply
            ActionMenuItem(
                icon = Icons.AutoMirrored.Filled.Reply,
                text = replyLabel,
                onClick = onReplyClick
            )
            // Copy
            ActionMenuItem(
                icon = Icons.Default.ContentCopy,
                text = "کپی",
                onClick = onCopyClick
            )
            // Pin (optional)
            if (onPinClick != null) {
                ActionMenuItem(
                    icon = Icons.Default.PushPin,
                    text = "پین کردن",
                    onClick = onPinClick
                )
            }
            // Copy Link (optional)
            if (onCopyLinkClick != null) {
                ActionMenuItem(
                    icon = Icons.Default.Link,
                    text = "کپی لینک",
                    onClick = onCopyLinkClick
                )
            }
            // Forward (optional)
            if (onForwardClick != null) {
                ActionMenuItem(
                    icon = Icons.Default.Share,
                    text = "فوروارد",
                    onClick = onForwardClick
                )
            }
            // Lock Content (Feature 2 - UI only)
            if (onLockContentClick != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ActionMenuItem(
                    icon = Icons.Default.Lock,
                    text = "قفل گذاری محتوا",
                    iconTint = extendedColors.accent,
                    onClick = onLockContentClick
                )
            }
            // Edit (owner only)
            if (onEditClick != null) {
                ActionMenuItem(
                    icon = Icons.Default.Edit,
                    text = "ویرایش",
                    onClick = onEditClick
                )
            }
            // Delete (owner only)
            if (onDeleteClick != null) {
                ActionMenuItem(
                    icon = Icons.Default.Delete,
                    text = "حذف",
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = onDeleteClick
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            // Select
            ActionMenuItem(
                icon = Icons.Default.CheckCircle,
                text = "انتخاب",
                onClick = onSelectClick
            )
        }
    }
}

@Composable
private fun ActionMenuItem(
    icon: ImageVector,
    text: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontFamily = VazirFontFamily,
            color = textColor
        )
    }
}
