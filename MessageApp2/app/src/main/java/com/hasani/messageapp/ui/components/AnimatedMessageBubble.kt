package com.hasani.messageapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 🎬 iMessage-Style Elegant Message Bubble Animation
 * 
 * Smooth, subtle, premium animation effects:
 * - Gentle scale from 0.85 to 1.0 (subtle, not dramatic)
 * - Soft slide from sender side (only 30px, not 200px)
 * - No rotation (cleaner look)
 * - Smooth ease-out curve
 */
@Composable
fun AnimatedMessageBubble(
    isMyMessage: Boolean,
    messageId: String,
    isNewMessage: Boolean = true,
    content: @Composable () -> Unit
) {
    // Animation state - starts at 0 and animates to 1
    var animationStarted by remember(messageId) { mutableStateOf(false) }
    
    // Gentle scale animation - starts at 85% (subtle, not dramatic)
    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.85f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "scale_$messageId"
    )
    
    // Smooth alpha animation
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearEasing
        ),
        label = "alpha_$messageId"
    )
    
    // Subtle slide - only 30px, not 200px (smooth, not aggressive)
    val slideX by animateFloatAsState(
        targetValue = if (animationStarted) 0f else if (isMyMessage) 30f else -30f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "slide_$messageId"
    )
    
    // Trigger animation on first composition
    LaunchedEffect(messageId) {
        animationStarted = true
    }
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationX = slideX
                // Transform origin from the message sender side
                transformOrigin = if (isMyMessage) {
                    androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f)
                } else {
                    androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                }
            }
    ) {
        content()
    }
}

/**
 * 🎯 Ultra-Minimal Animation - Just fade + tiny scale
 * 
 * The most subtle option for premium feel
 */
@Composable
fun MinimalMessageAnimation(
    messageId: String,
    content: @Composable () -> Unit
) {
    var visible by remember(messageId) { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(120),
        label = "alpha"
    )
    
    LaunchedEffect(messageId) {
        visible = true
    }
    
    Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    ) {
        content()
    }
}
