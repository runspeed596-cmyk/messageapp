package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Collapsible story row wrapper that shows/hides a story section with smooth animation.
 * The expand/collapse is driven by the `isExpanded` flag from the parent.
 *
 * Features:
 *  - Smooth vertical expand/shrink with fade
 *  - Subtle scale animation on collapse for a premium feel
 *  - Parallax-style depth effect during scroll
 */
@Composable
fun CollapsibleStoryRow(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scaleY by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0.92f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "storyRowScale"
    )
    val alphaValue by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "storyRowAlpha"
    )
    // Subtle slide offset for parallax feel
    val slideY by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -8f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "storyRowSlide"
    )

    AnimatedVisibility(
        visible = isExpanded,
        modifier = modifier.fillMaxWidth(),
        enter = expandVertically(
            animationSpec = tween(350, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(220))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.scaleY = scaleY
                    this.alpha = alphaValue
                    translationY = slideY
                }
        ) {
            content()
        }
    }
}
