package com.Kelasor.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Central animation specifications for the entire app.
 * Single source of truth for all timing, easing, and spring configs.
 */
object AppAnimations {
    // ── Durations ──────────────────────────────────────────────────────────────
    const val INSTANT: Int = 100
    const val FAST: Int = 200
    const val MEDIUM: Int = 300
    const val SLOW: Int = 450
    const val STAGGER_DELAY: Int = 40
    // ── Easings ────────────────────────────────────────────────────────────────
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val OvershootEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)
    // ── Spring Specs ───────────────────────────────────────────────────────────
    fun <T> snappySpring() = spring<T>(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessMediumLow
    )
    fun <T> bouncySpring() = spring<T>(
        dampingRatio = 0.55f,
        stiffness = Spring.StiffnessLow
    )
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = 0.9f,
        stiffness = Spring.StiffnessVeryLow
    )
    fun <T> quickSpring() = spring<T>(
        dampingRatio = 0.85f,
        stiffness = 400f
    )
    // ── Tween Presets ──────────────────────────────────────────────────────────
    fun <T> fastTween() = tween<T>(FAST, easing = EmphasizedDecelerate)
    fun <T> mediumTween() = tween<T>(MEDIUM, easing = EmphasizedDecelerate)
    fun <T> slowTween() = tween<T>(SLOW, easing = StandardEasing)
    fun <T> staggerTween(index: Int) = tween<T>(
        durationMillis = MEDIUM,
        delayMillis = index * STAGGER_DELAY,
        easing = EmphasizedDecelerate
    )
}
