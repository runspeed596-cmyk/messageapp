package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
//   Premium Unread Badge with Glow Effect
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier,
    isMuted: Boolean = false
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val badgeColor = if (isMuted) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        extendedColors.unreadBadge
    }
    
    val textColor = if (isMuted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        extendedColors.unreadBadgeText
    }
    
    AnimatedVisibility(
        visible = count > 0,
        enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .scale(if (!isMuted) pulseScale else 1f)
                .shadow(
                    elevation = if (!isMuted) 8.dp else 0.dp,
                    shape = CardShapes.badge,
                    ambientColor = extendedColors.unreadBadgeGlow,
                    spotColor = extendedColors.unreadBadgeGlow
                )
                .clip(CardShapes.badge)
                .background(
                    if (!isMuted) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                badgeColor,
                                badgeColor.copy(alpha = 0.8f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(badgeColor, badgeColor)
                        )
                    }
                )
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = textColor,
                fontSize = 11.sp,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📌 Pin Badge
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PinBadge(
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Box(
        modifier = modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(extendedColors.accentSecondary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = "Pinned",
            tint = extendedColors.accentSecondary,
            modifier = Modifier.size(12.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔇 Mute Badge
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MuteBadge(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = Icons.Default.VolumeOff,
        contentDescription = "Muted",
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.size(16.dp)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⌨️ Premium Typing Indicator Animation
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 7.dp,
    dotSpacing: Dp = 5.dp
) {
    val extendedColors = MessageAppTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    Row(
        modifier = modifier
            .clip(CardShapes.chip)
            .background(extendedColors.inputBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypingDot(alpha = dot1Alpha, size = dotSize, color = extendedColors.typingIndicator)
        TypingDot(alpha = dot2Alpha, size = dotSize, color = extendedColors.typingIndicator)
        TypingDot(alpha = dot3Alpha, size = dotSize, color = extendedColors.typingIndicator)
    }
}

@Composable
private fun TypingDot(
    alpha: Float,
    size: Dp,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// ✓✓ Message Status Indicators
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MessageStatusIcon(
    isSent: Boolean,
    isDelivered: Boolean,
    isRead: Boolean,
    isPending: Boolean = false,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    val color = when {
        isPending -> extendedColors.messageSent.copy(alpha = 0.5f)
        isRead -> extendedColors.messageRead
        isDelivered -> extendedColors.messageSent.copy(alpha = 0.8f)
        isSent -> extendedColors.messageSent.copy(alpha = 0.6f)
        else -> extendedColors.messageSent.copy(alpha = 0.4f)
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (isRead) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "statusScale"
    )
    
    Row(
        modifier = modifier.scale(animatedScale),
        horizontalArrangement = Arrangement.spacedBy((-3).dp)
    ) {
        when {
            isPending -> {
                Text(
                    text = "⏱",
                    fontSize = 12.sp,
                    modifier = Modifier.size(14.dp)
                )
            }
            isSent -> {
                CheckIcon(color = color, size = 14.dp)
                if (isDelivered || isRead) {
                    CheckIcon(color = color, size = 14.dp)
                }
            }
        }
    }
}

@Composable
private fun CheckIcon(
    color: Color,
    size: Dp
) {
    Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(size)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏷️ Status Text Badge (for online status display)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun StatusTextBadge(
    text: String,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(extendedColors.onlineIndicator)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isOnline) extendedColors.onlineIndicator else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontFamily = DanaFontFamily
        )
    }
}
