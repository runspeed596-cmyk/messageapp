package com.hasani.messageapp.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily
import com.hasani.messageapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Animated Splash Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val authState by viewModel.state.collectAsState()
    var isAnimationStarted by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    // Pulse animation for glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    // Start animations
    LaunchedEffect(Unit) {
        isAnimationStarted = true
        // Logo scale animation
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    var isAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isAnimationStarted) {
        if (isAnimationStarted) {
            logoAlpha.animateTo(1f, animationSpec = tween(200))
            delay(100)
            textAlpha.animateTo(1f, animationSpec = tween(200))
            delay(200)
            isAnimationFinished = true
        }
    }

    LaunchedEffect(isAnimationFinished, authState.isLoggedIn) {
        if (isAnimationFinished && authState.isLoggedIn != null && !hasNavigated) {
            hasNavigated = true
            if (authState.isLoggedIn == true) {
                onNavigateToMain()
            } else {
                onNavigateToLogin()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        extendedColors.gradientStart.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with glow effect
            Box(contentAlignment = Alignment.Center) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extendedColors.accent,
                                    extendedColors.accent.copy(alpha = 0f)
                                )
                            )
                        )
                )
                // Middle glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value * 0.5f)
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
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
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
            Spacer(modifier = Modifier.height(32.dp))
            // App name
            Text(
                text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.splash_app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Tagline
            Text(
                text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
        // Loading indicator at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            LoadingDots(
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

@Composable
private fun LoadingDots(
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val infiniteTransition = rememberInfiniteTransition(label = "loading_dots")
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot1)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot2)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dot3)
                .clip(CircleShape)
                .background(extendedColors.accent)
        )
    }
}
