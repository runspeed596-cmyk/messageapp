package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// ═══════════════════════════════════════════════════════════════════════════════
// 📐 Avatar Size Enum
// ═══════════════════════════════════════════════════════════════════════════════

enum class AvatarSize(val sizeDp: Dp, val fontSize: Int) {
    SMALL(32.dp, 12),
    MEDIUM(48.dp, 16),
    LARGE(64.dp, 20),
    XLARGE(96.dp, 28)
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
// 🖼️ Avatar Image Composable
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
    
    // Resolve URL: Prepend BASE_URL if relative
    val resolvedUrl = remember(imageUrl) {
        if (!imageUrl.isNullOrEmpty() && imageUrl.startsWith("/")) {
             "${com.Kelasor.app.util.Constants.BASE_URL.removeSuffix("/")}$imageUrl"
        } else {
            imageUrl
        }
    }
    
    Box(
        modifier = modifier
            .size(size.sizeDp)
            .then(
                if (hasBorder) {
                    Modifier.border(2.dp, extendedColors.accent, CircleShape)
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedUrl.isNullOrEmpty()) {
            // Load image with Coil 3
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier
                    .size(size.sizeDp)
                    .clip(CircleShape)
                    .background(getAvatarColor(name)), // Placeholder bg while loading
                contentScale = ContentScale.Crop
            )
        } else {
            // Placeholder with initials or icon
            Box(
                modifier = Modifier
                    .size(size.sizeDp)
                    .clip(CircleShape)
                    .background(getAvatarColor(name)),
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(size.sizeDp / 2)
                            )
                        }
                    }
                    AvatarType.GROUP -> {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(size.sizeDp / 2)
                        )
                    }
                    AvatarType.CHANNEL -> {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(size.sizeDp / 2)
                        )
                    }
                }
            }
        }
        
        // Online indicator
        if (isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset((-2).dp, (-2).dp)
                    .size(if (size == AvatarSize.SMALL) 8.dp else 12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4ADE80)) // Green for online
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
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
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF3B82F6), // Blue
        Color(0xFFEF4444), // Red
        Color(0xFF14B8A6)  // Teal
    )
    val index = name.hashCode().mod(colors.size).let { if (it < 0) it + colors.size else it }
    return colors[index]
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📖 Story Avatar (with gradient border)
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
                    Modifier.border(3.dp, extendedColors.accent, CircleShape)
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
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
