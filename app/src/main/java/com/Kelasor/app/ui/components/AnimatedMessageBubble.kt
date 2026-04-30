package com.Kelasor.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Premium iMessage-style send/receive animation.
 *
 * OUTGOING (my message):
 *   • Spring-based scale from 0.82 → 1.0 with medium-bouncy overshoot
 *   • Slide up from +40px (input bar direction)
 *   • Quick 120ms fade-in
 *   • Haptic feedback on first appearance
 *
 * INCOMING (other's message):
 *   • Spring-based scale from 0.90 → 1.0 with gentle bounce
 *   • Slide-in from left (−32px) with slight upward pop (−12px)
 *   • 150ms fade-in
 *
 * Only animates truly new messages (local_ prefix for outgoing, isNewMessage for incoming).
 * Server-confirmed messages (ID replacement) render instantly — no double animation.
 */
@Composable
fun AnimatedMessageBubble(
    isMyMessage: Boolean,
    messageId: String,
    isNewMessage: Boolean = true,
    content: @Composable () -> Unit
) {
    val isLocalPending: Boolean = messageId.startsWith("local_")
    // Outgoing: only animate local pending messages
    if (isMyMessage && (!isNewMessage || !isLocalPending)) {
        Box { content() }
        return
    }
    // Incoming: only animate truly new messages
    if (!isMyMessage && !isNewMessage) {
        Box { content() }
        return
    }
    var started: Boolean by remember(messageId) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    if (isMyMessage) {
        // ── Outgoing: spring bounce up from input bar ─────────────
        val scale: Float by animateFloatAsState(
            targetValue = if (started) 1f else 0.82f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "send_scale"
        )
        val alpha: Float by animateFloatAsState(
            targetValue = if (started) 1f else 0f,
            animationSpec = tween(120),
            label = "send_alpha"
        )
        val slideY: Float by animateFloatAsState(
            targetValue = if (started) 0f else 40f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "send_slide"
        )
        LaunchedEffect(messageId) {
            started = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
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
        // ── Incoming: gentle spring pop from left ─────────────────
        val alpha: Float by animateFloatAsState(
            targetValue = if (started) 1f else 0f,
            animationSpec = tween(150),
            label = "incoming_alpha"
        )
        val slideX: Float by animateFloatAsState(
            targetValue = if (started) 0f else -32f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "incoming_slideX"
        )
        val slideY: Float by animateFloatAsState(
            targetValue = if (started) 0f else -12f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "incoming_slideY"
        )
        val scale: Float by animateFloatAsState(
            targetValue = if (started) 1f else 0.90f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "incoming_scale"
        )
        LaunchedEffect(messageId) { started = true }
        Box(
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                translationX = slideX
                translationY = slideY
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
