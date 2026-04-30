package com.Kelasor.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext


// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Ultra-Premium Chat Bubble Component
// ═══════════════════════════════════════════════════════════════════════════════

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
    onStoryReplyClick: ((storyId: String) -> Unit)? = null,
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
    // ── Premium Bubble Background ────────────────────────────────────────
    val bubbleBackground = if (isMyMessage) {
        Brush.linearGradient(
            colors = listOf(
                extendedColors.myBubble,
                extendedColors.myBubbleEnd
            ),
            start = Offset.Zero,
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                extendedColors.otherBubble,
                extendedColors.otherBubble.copy(alpha = 0.95f)
            )
        )
    }
    val shadowColor = if (isMyMessage) {
        extendedColors.myBubble.copy(alpha = 0.25f)
    } else {
        Color.Black.copy(alpha = 0.15f)
    }
    Box(
        modifier = modifier
            .widthIn(max = 310.dp)
            .shadow(
                elevation = if (isMyMessage) 6.dp else 3.dp,
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(bubbleBackground)
            .then(
                if (!isMyMessage) {
                    Modifier.drawBehind {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    GlassBorderLight.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset.Zero,
                                end = Offset(0f, size.height)
                            )
                        )
                    }
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            // ── Reply Preview ────────────────────────────────────────────
            if (replyToMessage != null) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isMyMessage) Color.White.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        )
                        .clickable { onReplyClick?.invoke() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(extendedColors.accent, extendedColors.accentSecondary)
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            text = replyToMessage.senderName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.accent,
                                fontFamily = VazirFontFamily
                            ),
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (replyToMessage.type == com.Kelasor.app.domain.model.MessageType.TEXT)
                                replyToMessage.content
                            else "پیام رسانه",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontFamily = VazirFontFamily
                            ),
                            color = if (isMyMessage) extendedColors.myBubbleText.copy(0.7f) else extendedColors.otherBubbleText.copy(0.7f),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // ── Pin Indicator ────────────────────────────────────────────
            if (isPinned) {
                Row(
                    modifier = Modifier.padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.7f) else extendedColors.accent,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "سنجاق شده",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = VazirFontFamily
                        ),
                        color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.7f) else extendedColors.accent
                    )
                }
            }
            // ── Forward Indicator ────────────────────────────────────────
            if (!forwardedFrom.isNullOrEmpty()) {
                Text(
                    text = "↗ ارسال مجدد از: $forwardedFrom",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = VazirFontFamily
                    ),
                    color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.65f) else extendedColors.otherBubbleText.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            // ── Sender Name (Groups/Channels) ────────────────────────────
            if (!isMyMessage && !senderName.isNullOrEmpty()) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = VazirFontFamily,
                        letterSpacing = 0.3.sp
                    ),
                    color = extendedColors.accent,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .then(
                            if (onSenderClick != null) Modifier.clickable { onSenderClick() } else Modifier
                        )
                )
            }
            // ── Story Reply Tag Detection ────────────────────────────────
            val storyReplyPattern = remember { Regex("""\[STORY_REPLY:([^:]+):([^:]*):([^\]]+)](.*)""", RegexOption.DOT_MATCHES_ALL) }
            val storyMatch = storyReplyPattern.matchEntire(message)
            val displayMessage = storyMatch?.groupValues?.getOrNull(4) ?: message
            val storyId = storyMatch?.groupValues?.getOrNull(1)
            val storyMediaUrl = storyMatch?.groupValues?.getOrNull(2)
            val storyType = storyMatch?.groupValues?.getOrNull(3)
            // Show story preview card if this is a story reply
            if (storyMatch != null && !storyMediaUrl.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isMyMessage) Color.White.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                        )
                        .clickable(enabled = onStoryReplyClick != null && !storyId.isNullOrBlank()) {
                            storyId?.let { onStoryReplyClick?.invoke(it) }
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53))
                                )
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    // Story thumbnail
                    val fullStoryUrl = com.Kelasor.app.util.UrlUtils.getFullUrl(storyMediaUrl) ?: ""
                    if (storyType == "VIDEO") {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "ویدیو استوری",
                            tint = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.7f) else extendedColors.accent,
                            modifier = Modifier.size(36.dp)
                        )
                    } else if (fullStoryUrl.isNotBlank()) {
                        coil3.compose.AsyncImage(
                            model = fullStoryUrl,
                            contentDescription = "استوری",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "📸 پاسخ به استوری",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = VazirFontFamily
                        ),
                        color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.8f) else extendedColors.accent
                    )
                }
            }
            // ── Message Text with Clickable Links ────────────────────────
            val context = LocalContext.current
            val textColor = if (isMyMessage) extendedColors.myBubbleText else extendedColors.otherBubbleText
            val linkColor = if (isMyMessage) Color(0xFFB3E5FC) else Color(0xFF4FC3F7)
            val annotatedMessage = buildAnnotatedString {
                var lastIndex = 0
                URL_PATTERN.findAll(displayMessage).forEach { matchResult ->
                    if (matchResult.range.first > lastIndex) {
                        append(displayMessage.substring(lastIndex, matchResult.range.first))
                    }
                    pushStringAnnotation(tag = "URL", annotation = matchResult.value)
                    withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                        append(matchResult.value)
                    }
                    pop()
                    lastIndex = matchResult.range.last + 1
                }
                if (lastIndex < displayMessage.length) {
                    append(displayMessage.substring(lastIndex))
                }
            }
            val timeLabel = buildString {
                if (isEdited) append("ویرایش شده ")
                append(time)
                if (isMyMessage) append("  ✓")
            }
            var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
            // ── Message + Time Overlay (Telegram-style) ──────────────────
            Box {
                Text(
                    text = buildAnnotatedString {
                        append(annotatedMessage)
                        withStyle(SpanStyle(fontSize = 10.sp, color = Color.Transparent)) {
                            append("\u00A0\u00A0\u00A0\u00A0\u00A0$timeLabel\u00A0\u00A0\u00A0\u00A0\u00A0")
                        }
                    },
                    style = MessageAppTypography.messageText.copy(
                        fontFamily = VazirFontFamily,
                        color = textColor,
                        lineHeight = 22.sp
                    ),
                    onTextLayout = { layoutResult = it },
                    modifier = Modifier.pointerInput(onLongClick) {
                        detectTapGestures(
                            onLongPress = { onLongClick?.invoke() },
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
                                            } catch (_: Exception) {}
                                        }
                                }
                            }
                        )
                    }
                )
                // Time + Status overlay at bottom-end
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEdited) {
                        Text(
                            text = "ویرایش شده",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontFamily = VazirFontFamily
                            ),
                            color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.55f) else extendedColors.otherBubbleText.copy(alpha = 0.55f)
                        )
                    }
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontFamily = VazirFontFamily
                        ),
                        color = if (isMyMessage) extendedColors.myBubbleText.copy(alpha = 0.55f) else extendedColors.otherBubbleText.copy(alpha = 0.55f)
                    )
                    if (isMyMessage) {
                        MessageStatusIcon(
                            status = status,
                            tint = extendedColors.myBubbleText.copy(alpha = 0.55f)
                        )
                    }
                }
            }
            // ── Reactions Row — Premium Pill Style ────────────────────────
            if (reactions.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reactions.forEach { (emoji, count) ->
                        val isSelected = emoji == myReaction
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) extendedColors.accent.copy(alpha = 0.25f)
                                    else if (isMyMessage) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.15f)
                                )
                                .clickable { onReactionClick?.invoke(emoji) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$emoji $count",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontFamily = VazirFontFamily
                                ),
                                color = if (isMyMessage) extendedColors.myBubbleText else extendedColors.otherBubbleText
                            )
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ✓ Message Status Icon — Animated color transitions
// ═══════════════════════════════════════════════════════════════════════════════

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
            MessageStatus.FAILED -> MaterialTheme.colorScheme.error
            MessageStatus.SCHEDULED -> Color(0xFFFFA726)
            else -> tint
        },
        animationSpec = tween(350),
        label = "statusColor"
    )
    // Scale bounce on status change — celebratory pulse for READ
    val statusScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = if (status == MessageStatus.READ) {
                Spring.DampingRatioLowBouncy
            } else {
                Spring.DampingRatioMediumBouncy
            },
            stiffness = Spring.StiffnessMedium
        ),
        label = "statusScale"
    )
    // Trigger scale pulse by keying on status
    var scaleTarget: Float by remember { mutableStateOf(1f) }
    LaunchedEffect(status) {
        scaleTarget = when (status) {
            MessageStatus.READ -> 1.2f
            MessageStatus.DELIVERED -> 1.12f
            MessageStatus.SENT -> 1.1f
            else -> 1f
        }
    }
    val animatedScale: Float by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pulseScale",
        finishedListener = { scaleTarget = 1f }
    )
    // Shake animation for FAILED
    val shakeX by animateFloatAsState(
        targetValue = if (status == MessageStatus.FAILED) 0f else 0f,
        animationSpec = if (status == MessageStatus.FAILED) {
            spring(dampingRatio = 0.3f, stiffness = Spring.StiffnessHigh)
        } else {
            tween(0)
        },
        label = "shakeX"
    )
    var shakeOffset: Float by remember { mutableStateOf(0f) }
    LaunchedEffect(status) {
        if (status == MessageStatus.FAILED) {
            repeat(3) {
                shakeOffset = 4f
                kotlinx.coroutines.delay(60)
                shakeOffset = -4f
                kotlinx.coroutines.delay(60)
            }
            shakeOffset = 0f
        }
    }
    val animatedShake: Float by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "shakeAnim"
    )
    // Animated icon transition using Crossfade
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                translationX = animatedShake
            }
    ) {
        androidx.compose.animation.Crossfade(
            targetState = status,
            animationSpec = tween(250),
            label = "statusTransition"
        ) { targetStatus ->
            when (targetStatus) {
                MessageStatus.SENDING -> Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Sending",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.SENT -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sent",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.DELIVERED -> Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Delivered",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.READ -> Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Read",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.FAILED -> Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = "Failed",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.PENDING -> Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pending",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                MessageStatus.SCHEDULED -> Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Scheduled",
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
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
                elevation = 3.dp,
                shape = MessageShapes.otherBubbleSingle,
                ambientColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(MessageShapes.otherBubbleSingle)
            .background(extendedColors.otherBubble)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        ShimmerDots()
    }
}

@Composable
private fun ShimmerDots() {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
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
