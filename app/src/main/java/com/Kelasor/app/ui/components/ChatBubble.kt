package com.Kelasor.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.Kelasor.app.domain.model.MessageStatus
import com.Kelasor.app.ui.theme.GlassBorderLight
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.MessageShapes
import com.Kelasor.app.ui.theme.VazirFontFamily
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import com.Kelasor.app.domain.model.Message
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import android.content.Intent
import android.net.Uri

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Premium Chat Bubble Component
// ═══════════════════════════════════════════════════════════════════════════════

// URL detection pattern
// URL detection pattern - Updated to support "google.com" style links
private val URL_PATTERN = Regex(
    """((https?://|www\.)[^\s]+|[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}[^\s]*)""",
    RegexOption.IGNORE_CASE
)

enum class BubblePosition {
    FIRST, MIDDLE, LAST, SINGLE
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatBubble(
    message: String,
    time: String,
    isMyMessage: Boolean,
    status: MessageStatus = MessageStatus.SENT,
    position: BubblePosition = BubblePosition.SINGLE,
    senderName: String? = null,
    reactions: Map<String, Int> = emptyMap(),
    myReaction: String? = null,
    replyToMessage: Message? = null,
    onReactionClick: ((String) -> Unit)? = null,
    onSenderClick: (() -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    isEdited: Boolean = false,
    isPinned: Boolean = false,
    forwardedFrom: String? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    val shape = when {
        isMyMessage -> when (position) {
            BubblePosition.FIRST -> MessageShapes.myBubbleFirst
            BubblePosition.MIDDLE -> MessageShapes.myBubbleMiddle
            BubblePosition.LAST -> MessageShapes.myBubbleLast
            BubblePosition.SINGLE -> MessageShapes.myBubbleSingle
        }
        else -> when (position) {
            BubblePosition.FIRST -> MessageShapes.otherBubbleFirst
            BubblePosition.MIDDLE -> MessageShapes.otherBubbleMiddle
            BubblePosition.LAST -> MessageShapes.otherBubbleLast
            BubblePosition.SINGLE -> MessageShapes.otherBubbleSingle
        }
    }
    
    // My bubble: Rich gradient (rose/magenta tones matching reference)
    // Other bubble: Dark glass effect
    val bubbleBackground = if (isMyMessage) {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.myBubble,
                extendedColors.myBubbleEnd
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.otherBubble,
                extendedColors.otherBubble
            )
        )
    }
    
    // Shadow for depth
    val shadowColor = if (isMyMessage) {
        extendedColors.myBubble.copy(alpha = 0.3f)
    } else {
        Color.Black.copy(alpha = 0.2f)
    }
    
    Box(
        modifier = modifier
            .widthIn(max = 300.dp)
            .shadow(
                elevation = if (isMyMessage) 8.dp else 4.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(bubbleBackground)
            .then(
                if (!isMyMessage) {
                    Modifier.drawBehind {
                        // Glass border effect for received messages
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GlassBorderLight,
                                    Color.Transparent
                                ),
                                start = Offset.Zero,
                                end = Offset(0f, size.height)
                            )
                        )
                    }
                } else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            // Reply Preview
            if (replyToMessage != null) {
                Row(
                   modifier = Modifier
                       .padding(bottom = 4.dp)
                       .clip(RoundedCornerShape(8.dp))
                       .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                       .clickable { onReplyClick?.invoke() }
                       .padding(6.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .size(36.dp)
                            .background(extendedColors.accent, RoundedCornerShape(2.dp))
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = replyToMessage.senderName,
                            style = MessageAppTypography.chatTime.copy(fontWeight = FontWeight.Bold, color = extendedColors.accent),
                            maxLines = 1
                        )
                        Text(
                            text = if (replyToMessage.type == com.Kelasor.app.domain.model.MessageType.TEXT) 
                                replyToMessage.content 
                            else "پیام رسانه",
                            style = MessageAppTypography.messageText.copy(fontSize = 12.sp),
                            color = if (isMyMessage) extendedColors.myBubbleText.copy(0.8f) else extendedColors.otherBubbleText.copy(0.8f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Pin Indicator
            if (isPinned) {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = extendedColors.accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "سنجاق شده",
                        style = MessageAppTypography.chatTime.copy(fontSize = 10.sp),
                        color = extendedColors.accent
                    )
                }
            }

            // Forward Indicator
            if (!forwardedFrom.isNullOrEmpty()) {
                Text(
                    text = "↗ ارسال مجدد از: $forwardedFrom",
                    style = MessageAppTypography.chatTime.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.7f) else extendedColors.otherBubbleText.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Sender Name (for Groups/Channels)
            if (!isMyMessage && !senderName.isNullOrEmpty()) {
                Text(
                    text = senderName,
                    style = MessageAppTypography.chatTime.copy(fontWeight = FontWeight.Bold),
                    color = extendedColors.accent,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .then(
                            if (onSenderClick != null) Modifier.clickable { onSenderClick() } else Modifier
                        )
                )
            }
            
            // Message text with clickable links
            val context = LocalContext.current
            val textColor = if (isMyMessage) extendedColors.myBubbleText else extendedColors.otherBubbleText
            val linkColor = Color(0xFF4FC3F7) // Light blue for links
            val annotatedMessage = buildAnnotatedString {
                var lastIndex = 0
                URL_PATTERN.findAll(message).forEach { matchResult ->
                    // Append text before the URL
                    if (matchResult.range.first > lastIndex) {
                        append(message.substring(lastIndex, matchResult.range.first))
                    }
                    // Append the URL with styling and annotation
                    pushStringAnnotation(tag = "URL", annotation = matchResult.value)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        append(matchResult.value)
                    }
                    pop()
                    lastIndex = matchResult.range.last + 1
                }
                // Append remaining text after last URL
                if (lastIndex < message.length) {
                    append(message.substring(lastIndex))
                }
            }
            // Build the inline time+status label to measure its width
            val timeLabel = buildString {
                if (isEdited) append("ویرایش شده ")
                append(time)
                if (isMyMessage) append("  ✓") // placeholder for status icon width
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            // Use Box to overlay time on the message text, Telegram-style
            Box {
                // Message text with invisible trailing spacer for time
                Text(
                    text = buildAnnotatedString {
                        append(annotatedMessage)
                        // Add invisible spacer to reserve room for the time overlay
                        withStyle(SpanStyle(fontSize = 10.sp, color = Color.Transparent)) {
                            append("  \u00A0\u00A0\u00A0\u00A0$timeLabel\u00A0\u00A0\u00A0")
                        }
                    },
                    style = MessageAppTypography.messageText.copy(
                        fontFamily = VazirFontFamily,
                        color = textColor
                    ),
                    onTextLayout = { layoutResult = it },
                    modifier = Modifier.pointerInput(onLongClick) {
                        detectTapGestures(
                            onLongPress = {
                                onLongClick?.invoke()
                            },
                            onTap = { offset ->
                                layoutResult?.let { layout ->
                                    val position = layout.getOffsetForPosition(offset)
                                    annotatedMessage.getStringAnnotations(tag = "URL", start = position, end = position)
                                        .firstOrNull()?.let { annotation ->
                                            val url = if (annotation.item.startsWith("http://") || annotation.item.startsWith("https://")) {
                                                annotation.item
                                            } else {
                                                "https://${annotation.item}"
                                            }
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                // Handle invalid URL gracefully
                                            }
                                        }
                                }
                            }
                        )
                    }
                )
                // Time + status overlay at bottom-end
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 4.dp, start = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Edited indicator
                    if (isEdited) {
                        Text(
                            text = "ویرایش شده",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = if (isMyMessage) {
                                extendedColors.myBubbleText.copy(alpha = 0.6f)
                            } else {
                                extendedColors.otherBubbleText.copy(alpha = 0.6f)
                            }
                        )
                    }
                    Text(
                        text = time,
                        style = MessageAppTypography.messageTime,
                        color = if (isMyMessage) {
                            extendedColors.myBubbleText.copy(alpha = 0.6f)
                        } else {
                            extendedColors.otherBubbleText.copy(alpha = 0.6f)
                        }
                    )
                    if (isMyMessage) {
                        MessageStatusIcon(
                            status = status,
                            tint = extendedColors.myBubbleText.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Reactions Row
             if (reactions.isNotEmpty()) {
                 FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                 ) {
                     reactions.forEach { (emoji, count) ->
                         val isSelected = emoji == myReaction
                         Box(
                             modifier = Modifier
                                 .clip(RoundedCornerShape(12.dp))
                                 .background(
                                     if (isSelected) extendedColors.accent.copy(alpha = 0.3f)
                                     else Color.Black.copy(alpha = 0.2f)
                                 )
                                 .clickable { onReactionClick?.invoke(emoji) }
                                 .padding(horizontal = 6.dp, vertical = 4.dp)
                         ) {
                             Text(
                                 text = "$emoji $count",
                                 style = MessageAppTypography.chatTime.copy(fontSize = 10.sp),
                                 color = if (isMyMessage) extendedColors.myBubbleText else extendedColors.otherBubbleText
                             )
                         }
                     }
                 }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(
    status: MessageStatus,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    val iconColor by animateColorAsState(
        targetValue = when (status) {
            MessageStatus.READ -> extendedColors.messageRead
            MessageStatus.DELIVERED -> extendedColors.messageSent
            else -> tint
        },
        animationSpec = tween(300),
        label = "statusColor"
    )
    
    when (status) {
        MessageStatus.SENDING -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Sending",
            tint = tint,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.SENT -> Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Sent",
            tint = iconColor,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.DELIVERED -> Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = "Delivered",
            tint = iconColor,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.READ -> Icon(
            imageVector = Icons.Default.DoneAll,
            contentDescription = "Read",
            tint = iconColor,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.FAILED -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Failed",
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.PENDING -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Pending",
            tint = tint,
            modifier = modifier.size(14.dp)
        )
        MessageStatus.SCHEDULED -> Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = "Scheduled",
            tint = Color(0xFFFFA726),
            modifier = modifier.size(14.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💭 Typing Indicator Bubble
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TypingIndicatorBubble(
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = MessageShapes.otherBubbleSingle,
                ambientColor = Color.Black.copy(alpha = 0.2f)
            )
            .clip(MessageShapes.otherBubbleSingle)
            .background(extendedColors.otherBubble)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ShimmerDots()
    }
}

@Composable
private fun ShimmerDots() {
    val extendedColors = MessageAppTheme.extendedColors
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(
                label = "dot$index"
            )
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "dotAlpha$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(extendedColors.typingIndicator.copy(alpha = alpha))
            )
        }
    }
}
