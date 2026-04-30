package com.hasani.messageapp.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageShapes

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Shimmer Loading Effect
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun shimmerBrush(showShimmer: Boolean = true): Brush {
    val extendedColors = MessageAppTheme.extendedColors
    return if (showShimmer) {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerTranslate"
        )
        Brush.linearGradient(
            colors = listOf(
                extendedColors.shimmer,
                extendedColors.shimmerHighlight,
                extendedColors.shimmer
            ),
            start = Offset(translateAnimation - 500f, 0f),
            end = Offset(translateAnimation, 0f)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                extendedColors.shimmer,
                extendedColors.shimmer
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Item Shimmer Placeholder
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ChatItemShimmer(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(CardShapes.button)
                    .background(brush)
            )
            // Message preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(12.dp)
                    .clip(CardShapes.button)
                    .background(brush)
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time placeholder
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(10.dp)
                    .clip(CardShapes.button)
                    .background(brush)
            )
            // Badge placeholder
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Message Shimmer Placeholder
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MessageShimmer(
    isFromMe: Boolean,
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    val shape = if (isFromMe) MessageShapes.myBubbleSingle else MessageShapes.otherBubbleSingle
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromMe) 80.dp else 16.dp,
                end = if (isFromMe) 16.dp else 80.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
                .clip(shape)
                .background(brush)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 Profile Shimmer Placeholder
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProfileShimmer(
    modifier: Modifier = Modifier
) {
    val brush = shimmerBrush()
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(brush)
        )
        // Name
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(CardShapes.button)
                .background(brush)
        )
        // Username
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .clip(CardShapes.button)
                .background(brush)
        )
        // Bio
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(40.dp)
                .clip(CardShapes.button)
                .background(brush)
        )
    }
}
