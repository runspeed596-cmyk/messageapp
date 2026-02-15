package com.Kelasor.app.ui.components.story

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.domain.model.StoryUser
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography

/**
 * Horizontal list of stories
 */
@Composable
fun StoriesList(
    currentUser: StoryUser?, // Pass current user specifically for the "Add Story" button
    storyUsers: List<StoryUser>,
    onStoryClick: (StoryUser) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier,
    excludeCurrentUserFromList: Boolean = true
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. "Add Story" item (Current User)
        item {
            if (currentUser != null && currentUser.stories.isNotEmpty()) {
                 StoryItem(
                     storyUser = currentUser,
                     onClick = { onStoryClick(currentUser) }
                 )
            } else {
                AddStoryItem(
                    currentUser = currentUser,
                    onClick = onAddStoryClick
                )
            }
        }

        // 2. Friends' stories
        items(storyUsers) { storyUser ->
            if (!excludeCurrentUserFromList || !storyUser.isCurrentUser) { 
                StoryItem(
                    storyUser = storyUser,
                    onClick = { onStoryClick(storyUser) }
                )
            }
        }
    }
}

/**
 * Single story item with avatar ring
 */
@Composable
fun StoryItem(
    storyUser: StoryUser,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Segmented Ring
            val numStories = storyUser.stories.size
            if (numStories > 0) {
                val hasUnviewed = !storyUser.allViewed
                val isMyActiveStory = storyUser.isCurrentUser
                Canvas(modifier = Modifier.size(68.dp)) {
                    val strokeWidth = 2.5.dp.toPx()
                    val gapAngle = if (numStories > 1) 5f else 0f
                    val sweepAngle = (360f - (numStories * gapAngle)) / numStories
                    for (i in 0 until numStories) {
                        val startAngle = -90f + (i * (sweepAngle + gapAngle))
                        val isStoryViewed = storyUser.stories[i].isViewed
                        val segmentColor = if (isStoryViewed && !isMyActiveStory) Color.Gray.copy(alpha = 0.5f) else extendedColors.accent
                        drawArc(
                            color = segmentColor,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            // Avatar
            StoryAvatarImage(
                model = storyUser.avatarUrl,
                displayName = storyUser.displayName,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = storyUser.displayName,
            style = MessageAppTypography.caption,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddStoryItem(
    currentUser: StoryUser?,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            StoryAvatarImage(
                model = currentUser?.avatarUrl,
                displayName = currentUser?.displayName ?: "شما",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            // Plus Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(extendedColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Story",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currentUser?.displayName ?: "استوری شما",
            style = MessageAppTypography.caption,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Generates initials from a display name.
 * "Dad 1" -> "D1", "علی رضایی" -> "عر", "John" -> "J"
 */
private fun generateInitials(displayName: String): String {
    val parts = displayName.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}"
        parts.isNotEmpty() && parts.first().isNotEmpty() -> parts.first().first().toString()
        else -> "?"
    }
}

/**
 * Deterministic avatar background colors based on name hash.
 */
private val avatarColors: List<Color> = listOf(
    Color(0xFF6C5CE7), // Purple
    Color(0xFF00B894), // Green
    Color(0xFFE17055), // Coral
    Color(0xFF0984E3), // Blue
    Color(0xFFFDAB3D), // Amber
    Color(0xFFE84393), // Pink
    Color(0xFF00CEC9), // Teal
    Color(0xFFFF7675), // Red
)

@Composable
fun StoryAvatarImage(
    model: Any?,
    displayName: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Resolve URL: Prepend BASE_URL if relative path
    val resolvedModel = remember(model) {
        when (model) {
            is String -> com.Kelasor.app.util.UrlUtils.getFullUrl(model)
            else -> model
        }
    }
    if (resolvedModel != null && resolvedModel.toString().isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resolvedModel)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        // Initials fallback with colored background
        val initials = generateInitials(displayName)
        val bgColor = avatarColors[kotlin.math.abs(displayName.hashCode()) % avatarColors.size]
        Box(
            modifier = modifier.background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                style = MessageAppTypography.chatName,
                textAlign = TextAlign.Center
            )
        }
    }
}

