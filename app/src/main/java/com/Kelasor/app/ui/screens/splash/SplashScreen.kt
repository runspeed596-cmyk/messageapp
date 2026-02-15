package com.Kelasor.app.ui.screens.splash

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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    // Expanding ring animation
    val ringScale = remember { Animatable(0.5f) }
    val ringAlpha = remember { Animatable(0f) }
    // Second expanding ring (delayed)
    val ring2Scale = remember { Animatable(0.5f) }
    val ring2Alpha = remember { Animatable(0f) }
    // Exit animation
    var isExiting by remember { mutableStateOf(false) }
    val exitScale by animateFloatAsState(
        targetValue = if (isExiting) 1.15f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "exitScale"
    )
    val exitAlpha by animateFloatAsState(
        targetValue = if (isExiting) 0f else 1f,
        animationSpec = tween(350),
        label = "exitAlpha"
    )
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
    // Background gradient shift
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )
    // Start animations
    LaunchedEffect(Unit) {
        isAnimationStarted = true
        // Logo scale with spring overshoot
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.55f,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    // Expanding rings sequence
    LaunchedEffect(isAnimationStarted) {
        if (isAnimationStarted) {
            // Logo fade in
            logoAlpha.animateTo(1f, animationSpec = tween(200))
            // First ring expands outward
            launch {
                ringAlpha.animateTo(0.5f, animationSpec = tween(200))
                ringScale.animateTo(2.5f, animationSpec = tween(800, easing = FastOutSlowInEasing))
                ringAlpha.animateTo(0f, animationSpec = tween(300))
            }
            delay(250)
            // Second ring expands (staggered)
            launch {
                ring2Alpha.animateTo(0.35f, animationSpec = tween(200))
                ring2Scale.animateTo(2.5f, animationSpec = tween(800, easing = FastOutSlowInEasing))
                ring2Alpha.animateTo(0f, animationSpec = tween(300))
            }
            delay(150)
            // Text fades in
            textAlpha.animateTo(1f, animationSpec = tween(300))
            delay(300)
        }
    }
    var isAnimationFinished by remember { mutableStateOf(false) }
    LaunchedEffect(textAlpha.value) {
        if (textAlpha.value >= 0.95f) {
            isAnimationFinished = true
        }
    }
    LaunchedEffect(isAnimationFinished, authState.isLoggedIn) {
        if (isAnimationFinished && authState.isLoggedIn != null && !hasNavigated) {
            hasNavigated = true
            // Cinematic exit
            isExiting = true
            delay(350)
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
                        extendedColors.gradientStart.copy(alpha = 0.05f + gradientOffset * 0.08f),
                        extendedColors.gradientEnd.copy(alpha = 0.03f + gradientOffset * 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .alpha(exitAlpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(exitScale)
        ) {
            // Logo with glow effect and expanding rings
            Box(contentAlignment = Alignment.Center) {
                // Expanding ring 1
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ringScale.value)
                        .alpha(ringAlpha.value)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .then(
                            Modifier.drawWithContent {
                                drawContent()
                                drawCircle(
                                    color = extendedColors.accent,
                                    radius = size.minDimension / 2,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                                )
                            }
                        )
                )
                // Expanding ring 2
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring2Scale.value)
                        .alpha(ring2Alpha.value)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .then(
                            Modifier.drawWithContent {
                                drawContent()
                                drawCircle(
                                    color = extendedColors.gradientEnd,
                                    radius = size.minDimension / 2,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                                )
                            }
                        )
                )
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(glowScale)
                        .alpha(glowAlpha * logoAlpha.value)
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
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.splash_app_name),
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(textAlpha.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Tagline
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.splash_tagline),
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
                modifier = Modifier.alpha(textAlpha.value * exitAlpha)
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
