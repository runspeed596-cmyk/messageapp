package com.Kelasor.app.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.AppAnimations
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════════
//  ✨ Cinematic Splash Screen — iOS-inspired fluid animation
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToUserInfo: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val authState by viewModel.state.collectAsState()
    var hasNavigated by remember { mutableStateOf(false) }
    // ── Phase 1: Logo entrance ──────────────────────────────────────────────
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-15f) }
    // ── Phase 2: Text entrance ──────────────────────────────────────────────
    val titleAlpha = remember { Animatable(0f) }
    val titleSlideY = remember { Animatable(20f) }
    val taglineAlpha = remember { Animatable(0f) }
    val taglineSlideY = remember { Animatable(15f) }
    // ── Phase 3: Ripple rings ───────────────────────────────────────────────
    val ring1Scale = remember { Animatable(0.6f) }
    val ring1Alpha = remember { Animatable(0f) }
    val ring2Scale = remember { Animatable(0.6f) }
    val ring2Alpha = remember { Animatable(0f) }
    val ring3Scale = remember { Animatable(0.6f) }
    val ring3Alpha = remember { Animatable(0f) }
    // ── Exit animation ──────────────────────────────────────────────────────
    var isExiting by remember { mutableStateOf(false) }
    val exitScale by animateFloatAsState(
        targetValue = if (isExiting) 1.08f else 1f,
        animationSpec = tween(500, easing = AppAnimations.PremiumEasing),
        label = "exitScale"
    )
    val exitAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(450, easing = AppAnimations.FluidEasing),
        label = "exitAlpha"
    )
    // ── Ambient floating particles ──────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash_ambient")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particlePhase"
    )
    // ── Subtle glow pulse ───────────────────────────────────────────────────
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    // ── Orchestrated entry sequence ─────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Logo springs in with rotation
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(300, easing = AppAnimations.FluidEasing))
        }
        launch {
            logoScale.animateTo(
                1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            logoRotation.animateTo(
                0f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow)
            )
        }
        delay(200)
        // Ring 1 — smooth outward expansion
        launch {
            ring1Alpha.animateTo(0.5f, animationSpec = tween(300, easing = AppAnimations.FluidEasing))
            ring1Scale.animateTo(2.8f, animationSpec = tween(1000, easing = AppAnimations.PremiumEasing))
            ring1Alpha.animateTo(0f, animationSpec = tween(400, easing = AppAnimations.FluidEasing))
        }
        delay(300)
        // Ring 2 — staggered
        launch {
            ring2Alpha.animateTo(0.35f, animationSpec = tween(300, easing = AppAnimations.FluidEasing))
            ring2Scale.animateTo(2.8f, animationSpec = tween(1000, easing = AppAnimations.PremiumEasing))
            ring2Alpha.animateTo(0f, animationSpec = tween(400, easing = AppAnimations.FluidEasing))
        }
        delay(200)
        // Ring 3 — lightest
        launch {
            ring3Alpha.animateTo(0.2f, animationSpec = tween(300, easing = AppAnimations.FluidEasing))
            ring3Scale.animateTo(2.8f, animationSpec = tween(1000, easing = AppAnimations.PremiumEasing))
            ring3Alpha.animateTo(0f, animationSpec = tween(400, easing = AppAnimations.FluidEasing))
        }
        delay(200)
        // Title slides up smoothly
        launch {
            titleAlpha.animateTo(1f, animationSpec = tween(500, easing = AppAnimations.FluidEasing))
        }
        launch {
            titleSlideY.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f))
        }
        delay(150)
        // Tagline slides up
        launch {
            taglineAlpha.animateTo(1f, animationSpec = tween(500, easing = AppAnimations.FluidEasing))
        }
        launch {
            taglineSlideY.animateTo(0f, animationSpec = spring(dampingRatio = 0.8f, stiffness = 200f))
        }
    }
    // ── Navigation after animation finishes ─────────────────────────────────
    var isAnimationFinished by remember { mutableStateOf(false) }
    LaunchedEffect(taglineAlpha.value) {
        if (taglineAlpha.value >= 0.9f) {
            isAnimationFinished = true
        }
    }
    LaunchedEffect(isAnimationFinished, authState.isLoggedIn) {
        if (isAnimationFinished && authState.isLoggedIn != null && !hasNavigated) {
            hasNavigated = true
            delay(400) // Brief pause to admire the animation
            isExiting = true
            delay(500)
            if (authState.isLoggedIn == true) {
                if (authState.isOnboardingComplete) {
                    onNavigateToMain()
                } else {
                    onNavigateToUserInfo()
                }
            } else {
                onNavigateToLogin()
            }
        }
    }
    // ── UI Layout ───────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer {
                alpha = exitAlpha
                scaleX = exitScale
                scaleY = exitScale
            },
        contentAlignment = Alignment.Center
    ) {
        // Floating particle background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val particleCount = 8
            for (i in 0 until particleCount) {
                val angle = particlePhase + (i * 2f * PI.toFloat() / particleCount)
                val radius = 120f + (i * 25f)
                val x = centerX + cos(angle) * radius
                val y = centerY + sin(angle) * radius
                val alphaVal = (0.06f + (sin(angle + particlePhase) * 0.04f)).coerceIn(0f, 1f)
                drawCircle(
                    color = extendedColors.accent.copy(alpha = alphaVal * logoAlpha.value),
                    radius = 3f + (i * 0.5f),
                    center = Offset(x, y)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with glow + ripple rings
            Box(contentAlignment = Alignment.Center) {
                // Ripple ring 1
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring1Scale.value)
                        .alpha(ring1Alpha.value)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = extendedColors.accent,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
                // Ripple ring 2
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring2Scale.value)
                        .alpha(ring2Alpha.value)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = extendedColors.gradientEnd,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.8f)
                        )
                    }
                }
                // Ripple ring 3
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring3Scale.value)
                        .alpha(ring3Alpha.value)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = extendedColors.accent.copy(alpha = 0.5f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.2f)
                        )
                    }
                }
                // Outer glow aura
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .alpha(glowPulse * logoAlpha.value)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extendedColors.accent.copy(alpha = 0.3f),
                                    extendedColors.accent.copy(alpha = 0f)
                                )
                            )
                        )
                )
                // Inner soft glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value * 0.4f
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extendedColors.gradientEnd,
                                    extendedColors.gradientStart.copy(alpha = 0f)
                                )
                            )
                        )
                )
                // Main logo circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
                            rotationZ = logoRotation.value
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    extendedColors.gradientStart,
                                    extendedColors.gradientEnd
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
            // App name — slides up with fade
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.splash_app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleSlideY.value
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Tagline — slides up with slight delay
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.graphicsLayer {
                    alpha = taglineAlpha.value
                    translationY = taglineSlideY.value
                }
            )
        }
        // Loading dots at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
        ) {
            PulsingLoadingDots(
                modifier = Modifier.alpha(taglineAlpha.value * exitAlpha)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  Pulsing Loading Dots — iOS-style smooth wave
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PulsingLoadingDots(
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1a"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2a"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3a"
    )
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot1Scale)
                .alpha(dot1Alpha)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot2Scale)
                .alpha(dot2Alpha)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .scale(dot3Scale)
                .alpha(dot3Alpha)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
    }
}
