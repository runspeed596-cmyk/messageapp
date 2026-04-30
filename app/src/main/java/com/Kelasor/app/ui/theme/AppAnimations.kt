package com.Kelasor.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Central animation specifications for the entire app.
 * Single source of truth for all timing, easing, and spring configs.
 *
 * Design philosophy: iOS-inspired fluid, buttery-smooth animations.
 * Longer durations + softer springs = premium feel.
 */
object AppAnimations {
    // ── Durations ──────────────────────────────────────────────────────────────
    const val INSTANT: Int = 120
    const val FAST: Int = 250
    const val MEDIUM: Int = 400
    const val SLOW: Int = 600
    const val EXTRA_SLOW: Int = 800
    const val STAGGER_DELAY: Int = 50
    const val SCREEN_TRANSITION: Int = 500
    // ── iOS-Style Easings ──────────────────────────────────────────────────────
    /** iOS default ease-out — smooth deceleration for entering elements */
    val EmphasizedDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    /** iOS ease-in — for elements leaving the screen */
    val EmphasizedAccelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    /** iOS standard ease-in-out — balanced for most transitions */
    val StandardEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    /** Soft overshoot — subtle bounce for playful elements */
    val OvershootEasing = CubicBezierEasing(0.34f, 1.4f, 0.64f, 1.0f)
    /** iOS spring-like ease — long deceleration tail */
    val FluidEasing = CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
    /** Premium ease — ultra-smooth for hero animations */
    val PremiumEasing = CubicBezierEasing(0.16f, 1.0f, 0.3f, 1.0f)
    // ── Spring Specs ───────────────────────────────────────────────────────────
    /** Snappy spring — responsive UI elements like toggles, chips */
    fun <T> snappySpring() = spring<T>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMediumLow
    )
    /** Bouncy spring — FABs, badges, playful elements */
    fun <T> bouncySpring() = spring<T>(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessLow
    )
    /** Gentle spring — lists, cards, subtle motion */
    fun <T> gentleSpring() = spring<T>(
        dampingRatio = 0.92f,
        stiffness = Spring.StiffnessVeryLow
    )
    /** Quick spring — button press, micro-interactions */
    fun <T> quickSpring() = spring<T>(
        dampingRatio = 0.88f,
        stiffness = 350f
    )
    /** Fluid spring — iOS-style smooth motion for screen transitions */
    fun <T> fluidSpring() = spring<T>(
        dampingRatio = 0.86f,
        stiffness = 200f
    )
    /** Elastic spring — playful overshoot for reactions, emojis */
    fun <T> elasticSpring() = spring<T>(
        dampingRatio = 0.45f,
        stiffness = Spring.StiffnessLow
    )
    /** Smooth spring — ultra-smooth for large-scale transforms */
    fun <T> smoothSpring() = spring<T>(
        dampingRatio = 0.95f,
        stiffness = 150f
    )
    // ── Tween Presets ──────────────────────────────────────────────────────────
    fun <T> fastTween() = tween<T>(FAST, easing = EmphasizedDecelerate)
    fun <T> mediumTween() = tween<T>(MEDIUM, easing = FluidEasing)
    fun <T> slowTween() = tween<T>(SLOW, easing = PremiumEasing)
    fun <T> smoothTween() = tween<T>(EXTRA_SLOW, easing = FluidEasing)
    fun <T> screenTween() = tween<T>(SCREEN_TRANSITION, easing = PremiumEasing)
    fun <T> staggerTween(index: Int) = tween<T>(
        durationMillis = MEDIUM,
        delayMillis = index * STAGGER_DELAY,
        easing = FluidEasing
    )
}
