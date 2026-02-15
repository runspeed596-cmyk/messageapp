package com.Kelasor.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Telegram-style send animation — only for outgoing new messages.
 * Also animates incoming messages with a subtle slide from left.
 * Animates ONLY local (pending) messages on first send.
 * Server-confirmed messages (ID replacement) render instantly.
 */
@Composable
fun AnimatedMessageBubble(
    isMyMessage: Boolean,
    messageId: String,
    isNewMessage: Boolean = true,
    content: @Composable () -> Unit
) {
    // Only animate local pending messages (outgoing) or new incoming
    val isLocalPending = messageId.startsWith("local_")
    // For outgoing: only animate local pending
    if (isMyMessage && (!isNewMessage || !isLocalPending)) {
        Box { content() }
        return
    }
    // For incoming: only animate truly new messages
    if (!isMyMessage && !isNewMessage) {
        Box { content() }
        return
    }
    var started by remember(messageId) { mutableStateOf(false) }
    if (isMyMessage) {
        // Outgoing: slide up + scale + fade
        val scale by animateFloatAsState(
            targetValue = if (started) 1f else 0.96f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "send_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (started) 1f else 0f,
            animationSpec = tween(150, easing = FastOutSlowInEasing),
            label = "send_alpha"
        )
        val slideY by animateFloatAsState(
            targetValue = if (started) 0f else 24f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "send_slide"
        )
        LaunchedEffect(messageId) { started = true }
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationY = slideY
            }
        ) {
            content()
        }
    } else {
        // Incoming: subtle slide from left + fade
        val alpha by animateFloatAsState(
            targetValue = if (started) 1f else 0f,
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            label = "incoming_alpha"
        )
        val slideX by animateFloatAsState(
            targetValue = if (started) 0f else -20f,
            animationSpec = tween(250, easing = FastOutSlowInEasing),
            label = "incoming_slide"
        )
        val scale by animateFloatAsState(
            targetValue = if (started) 1f else 0.97f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "incoming_scale"
        )
        LaunchedEffect(messageId) { started = true }
        Box(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                translationX = slideX
                scaleX = scale
                scaleY = scale
            }
        ) {
            content()
        }
    }
}

/**
 * No-op wrapper — instant render.
 */
@Composable
fun MinimalMessageAnimation(
    messageId: String,
    content: @Composable () -> Unit
) {
    Box { content() }
}
