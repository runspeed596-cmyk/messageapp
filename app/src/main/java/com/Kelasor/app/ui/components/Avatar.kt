package com.Kelasor.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// 📐 Avatar Size Enum — Refined sizes for premium layout
// ═══════════════════════════════════════════════════════════════════════════════

enum class AvatarSize(val sizeDp: Dp, val fontSize: Int) {
    EXTRA_SMALL(24.dp, 10),
    SMALL(36.dp, 13),
    MEDIUM(52.dp, 18),
    LARGE(68.dp, 22),
    XLARGE(120.dp, 36)
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎭 Avatar Type Enum
// ═══════════════════════════════════════════════════════════════════════════════

enum class AvatarType {
    USER,
    GROUP,
    CHANNEL
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🖼️ Premium Avatar Image — Gradient ring, shadow glow, pulsing online dot
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AvatarImage(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.MEDIUM,
    isOnline: Boolean = false,
    hasBorder: Boolean = false,
    avatarType: AvatarType = AvatarType.USER
) {
    val extendedColors = MessageAppTheme.extendedColors
    val context = LocalContext.current
    // Resolve URL
    val resolvedUrl = remember(imageUrl) {
        com.Kelasor.app.util.UrlUtils.getFullUrl(imageUrl)
    }
    // Pulsing animation for online indicator
    val infiniteTransition = rememberInfiniteTransition(label = "online_pulse")
    val onlinePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "onlinePulseScale"
    )
    val avatarColor = getAvatarColor(name)
    // Outer container with optional gradient ring
    Box(
        modifier = modifier
            .then(
                if (modifier == Modifier || !modifier.toString().contains("Size")) {
                    Modifier.size(size.sizeDp + if (hasBorder) 6.dp else 0.dp)
                } else Modifier
            )
            .then(
                if (hasBorder) {
                    Modifier
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        extendedColors.accent,
                                        extendedColors.accentSecondary,
                                        extendedColors.accent
                                    )
                                ),
                                style = Stroke(width = 2.5.dp.toPx())
                            )
                        }
                        .padding(3.dp)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedUrl.isNullOrEmpty()) {
            // Real image with shadow
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier
                    .size(size.sizeDp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        ambientColor = avatarColor.copy(alpha = 0.3f),
                        spotColor = avatarColor.copy(alpha = 0.2f)
                    )
                    .clip(CircleShape)
                    .background(avatarColor),
                contentScale = ContentScale.Crop
            )
        } else {
            // Premium placeholder with gradient background
            Box(
                modifier = Modifier
                    .size(size.sizeDp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        ambientColor = avatarColor.copy(alpha = 0.4f),
                        spotColor = avatarColor.copy(alpha = 0.3f)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                avatarColor,
                                avatarColor.copy(alpha = 0.75f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (avatarType) {
                    AvatarType.USER -> {
                        val initials = getInitials(name)
                        if (initials.isNotEmpty()) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontSize = size.fontSize.sp,
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(size.sizeDp * 0.48f)
                            )
                        }
                    }
                    AvatarType.GROUP -> {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(size.sizeDp * 0.48f)
                        )
                    }
                    AvatarType.CHANNEL -> {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(size.sizeDp * 0.48f)
                        )
                    }
                }
            }
        }
        // Premium online indicator — pulsing green dot with white ring
        if (isOnline) {
            val dotSize: Dp = when (size) {
                AvatarSize.EXTRA_SMALL -> 8.dp
                AvatarSize.SMALL -> 10.dp
                AvatarSize.MEDIUM -> 14.dp
                AvatarSize.LARGE -> 16.dp
                AvatarSize.XLARGE -> 18.dp
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(dotSize)
                    .drawBehind {
                        // Outer glow ring
                        drawCircle(
                            color = Color(0xFF34C759).copy(alpha = 0.35f),
                            radius = this.size.minDimension / 2f * onlinePulse
                        )
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759))
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Helper Functions
// ═══════════════════════════════════════════════════════════════════════════════

private fun getInitials(name: String): String {
    return name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF5B7FFF), // Soft Blue
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
        Color(0xFFEF4444), // Red
        Color(0xFF14B8A6), // Teal
        Color(0xFF6366F1), // Indigo
        Color(0xFFD946EF)  // Fuchsia
    )
    val index = name.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }
    return colors[index]
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📖 Story Avatar (with gradient segmented ring)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun StoryAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.LARGE,
    hasUnseenStory: Boolean = false
) {
    val extendedColors = MessageAppTheme.extendedColors
    Box(
        modifier = modifier
            .size(size.sizeDp + 8.dp)
            .then(
                if (hasUnseenStory) {
                    Modifier
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        extendedColors.storyRingStart,
                                        extendedColors.storyRingEnd,
                                        extendedColors.storyRingStart
                                    )
                                ),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                        .padding(4.dp)
                } else {
                    Modifier
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .padding(3.dp)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        AvatarImage(
            imageUrl = imageUrl,
            name = name,
            size = size,
            hasBorder = false
        )
    }
}
