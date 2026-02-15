package com.Kelasor.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Reusable animation modifier extensions for premium UI effects.
 */

/**
 * Staggered fade + slide-up entrance for list items.
 * [index] controls the delay offset.
 */
fun Modifier.staggeredFadeIn(index: Int, visible: Boolean = true): Modifier = composed {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { started = visible }
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = AppAnimations.staggerTween(index),
        label = "stagger_alpha_$index"
    )
    val slideY by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = AppAnimations.staggerTween(index),
        label = "stagger_slide_$index"
    )
    this.graphicsLayer {
        this.alpha = alpha
        translationY = slideY
    }
}

/**
 * Subtle depth press effect — scales down slightly when pressed.
 */
fun Modifier.pressDepth(onClick: () -> Unit): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = AppAnimations.quickSpring(),
        label = "press_scale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Breathing glow — gentle infinite pulse for FABs and highlights.
 */
fun Modifier.breathingGlow(color: Color): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    this.drawWithContent {
        drawContent()
        drawCircle(
            color = color.copy(alpha = glowAlpha),
            radius = size.maxDimension * 0.7f,
            center = Offset(size.width / 2, size.height / 2),
            blendMode = BlendMode.SrcOver
        )
    }
}

/**
 * Subtle shimmer sweep effect — horizontal light sweep across a surface.
 */
fun Modifier.shimmerSweep(): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    this.drawWithContent {
        drawContent()
        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0f),
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0f)
            ),
            start = Offset(size.width * offset, 0f),
            end = Offset(size.width * (offset + 0.5f), size.height)
        )
        drawRect(brush = shimmerBrush)
    }
}

/**
 * Gentle pulse scale — for online indicators, notification dots.
 */
fun Modifier.pulseScale(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Scale-in entrance animation — for badges, buttons appearing.
 */
fun Modifier.popIn(): Modifier = composed {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = AppAnimations.bouncySpring(),
        label = "pop_scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = AppAnimations.fastTween(),
        label = "pop_alpha"
    )
    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
