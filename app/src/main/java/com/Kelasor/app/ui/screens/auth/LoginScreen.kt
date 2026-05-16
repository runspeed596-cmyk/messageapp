package com.Kelasor.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.theme.AppAnimations
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.AuthEvent
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════════
//  📱 Premium Login Screen — iOS-style staggered entrance
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    val isPhoneValid: Boolean = phoneNumber.length >= 10
    // ── Staggered entrance animations ───────────────────────────────────────
    val logoScale = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleSlideY = remember { Animatable(30f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleSlideY = remember { Animatable(25f) }
    val inputAlpha = remember { Animatable(0f) }
    val inputSlideY = remember { Animatable(20f) }
    val buttonAlpha = remember { Animatable(0f) }
    val buttonSlideY = remember { Animatable(15f) }
    val termsAlpha = remember { Animatable(0f) }
    // ── Ambient background animation ────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "login_ambient")
    val ambientPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientPhase"
    )
    // ── Logo glow pulse ─────────────────────────────────────────────────────
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    // ── Orchestrated entrance sequence ──────────────────────────────────────
    LaunchedEffect(Unit) {
        // Step 1: Logo springs in
        launch { logoAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { logoScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)) }
        delay(250)
        // Step 2: Title slides up
        launch { titleAlpha.animateTo(1f, tween(450, easing = AppAnimations.FluidEasing)) }
        launch { titleSlideY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f)) }
        delay(120)
        // Step 3: Subtitle
        launch { subtitleAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { subtitleSlideY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f)) }
        delay(120)
        // Step 4: Input field
        launch { inputAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { inputSlideY.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 180f)) }
        delay(100)
        // Step 5: Button
        launch { buttonAlpha.animateTo(1f, tween(350, easing = AppAnimations.FluidEasing)) }
        launch { buttonSlideY.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 180f)) }
        delay(200)
        // Step 6: Terms fade in gently
        termsAlpha.animateTo(1f, tween(600, easing = AppAnimations.FluidEasing))
    }
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.OtpSent -> onNavigateToOtp(phoneNumber)
                is AuthEvent.Error -> { /* Error already in state */ }
                else -> {}
            }
        }
    }
    // ── UI Layout ───────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Floating ambient particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height * 0.3f
            for (i in 0 until 6) {
                val angle = ambientPhase + (i * PI.toFloat() / 3f)
                val radius = 100f + (i * 20f)
                val x = centerX + cos(angle) * radius
                val y = centerY + sin(angle) * radius * 0.6f
                val alphaVal = (0.04f + (sin(angle) * 0.03f)).coerceIn(0f, 1f)
                drawCircle(
                    color = extendedColors.accent.copy(alpha = alphaVal * logoAlpha.value),
                    radius = 2.5f + (i * 0.4f),
                    center = Offset(x, y)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            // ── Logo with glow ──────────────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Outer glow aura
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            alpha = glowPulse * logoAlpha.value
                            scaleX = logoScale.value * 1.1f
                            scaleY = logoScale.value * 1.1f
                        }
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    extendedColors.accent.copy(alpha = 0.25f),
                                    extendedColors.accent.copy(alpha = 0f)
                                )
                            )
                        )
                )
                // Logo circle
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                            alpha = logoAlpha.value
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
            Spacer(modifier = Modifier.height(32.dp))
            // ── Title ───────────────────────────────────────────────────────
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.welcome),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleSlideY.value
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            // ── Subtitle ────────────────────────────────────────────────────
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.login_title),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                    translationY = subtitleSlideY.value
                }
            )
            Spacer(modifier = Modifier.height(48.dp))
            // ── Phone Input Field ───────────────────────────────────────────
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = inputAlpha.value
                    translationY = inputSlideY.value
                }
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                                phoneNumber = it
                                viewModel.clearError()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.mobile_number),
                                fontFamily = DanaFontFamily
                            )
                        },
                        placeholder = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.phone_placeholder),
                                fontFamily = DanaFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = extendedColors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = state.error != null,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedLabelColor = extendedColors.accent,
                            cursorColor = extendedColors.accent,
                            focusedLeadingIconColor = extendedColors.accent,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
            // ── Error message ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = state.error != null,
                enter = slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(150)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            // ── Login Button ────────────────────────────────────────────────
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = buttonAlpha.value
                    translationY = buttonSlideY.value
                }
            ) {
                PrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.login_button),
                    onClick = {
                        if (isPhoneValid) {
                            viewModel.sendOtp(phoneNumber)
                        }
                    },
                    enabled = isPhoneValid && !state.isLoading,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            // ── Terms ───────────────────────────────────────────────────────
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.login_terms),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 28.dp)
                    .graphicsLayer { alpha = termsAlpha.value }
            )
        }
    }
}
