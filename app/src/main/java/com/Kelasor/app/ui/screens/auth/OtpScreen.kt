package com.Kelasor.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.components.TextButton
import com.Kelasor.app.ui.theme.AppAnimations
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthEvent
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
//  🔐 Premium OTP Verification Screen — iOS-inspired staggered entrance
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phoneNumber: String,
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToUserInfo: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var otpValue by remember { mutableStateOf("") }
    var resendTimer by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val isOtpComplete: Boolean = otpValue.length == 6
    // ── Staggered entrance animations ───────────────────────────────────────
    val headerAlpha = remember { Animatable(0f) }
    val headerSlideY = remember { Animatable(20f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val subtitleSlideY = remember { Animatable(15f) }
    val otpAlpha = remember { Animatable(0f) }
    val otpSlideY = remember { Animatable(20f) }
    val buttonAlpha = remember { Animatable(0f) }
    val buttonSlideY = remember { Animatable(15f) }
    val resendAlpha = remember { Animatable(0f) }
    // ── Orchestrated entrance sequence ──────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(100)
        // Step 1: Header
        launch { headerAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { headerSlideY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f)) }
        delay(150)
        // Step 2: Subtitle
        launch { subtitleAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { subtitleSlideY.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f)) }
        delay(150)
        // Step 3: OTP boxes
        launch { otpAlpha.animateTo(1f, tween(400, easing = AppAnimations.FluidEasing)) }
        launch { otpSlideY.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 180f)) }
        delay(120)
        // Step 4: Button
        launch { buttonAlpha.animateTo(1f, tween(350, easing = AppAnimations.FluidEasing)) }
        launch { buttonSlideY.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 180f)) }
        delay(150)
        // Step 5: Resend
        resendAlpha.animateTo(1f, tween(500, easing = AppAnimations.FluidEasing))
    }
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> {
                    if (event.isNewUser) onNavigateToUserInfo() else onNavigateToMain()
                }
                is AuthEvent.Error -> { /* Error already in state */ }
                else -> {}
            }
        }
    }
    // Timer countdown
    LaunchedEffect(resendTimer) {
        if (resendTimer > 0) {
            delay(1000)
            resendTimer--
        } else {
            canResend = true
        }
    }
    // Auto focus
    LaunchedEffect(Unit) {
        delay(500)
        focusRequester.requestFocus()
    }
    // ── UI Layout ───────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.verify_number),
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back),
                        tint = extendedColors.accent
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            // ── Instructions ────────────────────────────────────────────────
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.verification_code_sent),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = headerAlpha.value
                    translationY = headerSlideY.value
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.enter_verification_code, phoneNumber),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = subtitleAlpha.value
                    translationY = subtitleSlideY.value
                }
            )
            Spacer(modifier = Modifier.height(48.dp))
            // ── OTP Input ───────────────────────────────────────────────────
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = otpAlpha.value
                    translationY = otpSlideY.value
                }
            ) {
                BasicTextField(
                    value = otpValue,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            otpValue = it
                            viewModel.clearError()
                            if (it.length == 6) {
                                viewModel.verifyOtp(phoneNumber, it)
                            }
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(Color.Transparent),
                    decorationBox = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) { index ->
                                OtpDigitBox(
                                    digit = otpValue.getOrNull(5 - index)?.toString() ?: "",
                                    isFocused = index == 5 - otpValue.length,
                                    hasError = state.error != null,
                                    index = index
                                )
                            }
                        }
                    }
                )
            }
            // Error message
            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                Text(
                    text = state.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            // ── Verify Button ───────────────────────────────────────────────
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = buttonAlpha.value
                    translationY = buttonSlideY.value
                }
            ) {
                PrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.verify),
                    onClick = {
                        if (isOtpComplete) {
                            viewModel.verifyOtp(phoneNumber, otpValue)
                        }
                    },
                    enabled = isOtpComplete && !state.isLoading,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            // ── Resend section ──────────────────────────────────────────────
            Box(
                modifier = Modifier.graphicsLayer { alpha = resendAlpha.value }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (canResend) {
                        TextButton(
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.resend_code),
                            onClick = {
                                resendTimer = 60
                                canResend = false
                                viewModel.sendOtp(phoneNumber)
                            }
                        )
                    } else {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.resend_code_timer, resendTimer),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = VazirFontFamily,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  🔢 Premium OTP Digit Box — Bouncy spring with accent glow
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun RowScope.OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    hasError: Boolean,
    index: Int
) {
    val extendedColors = MessageAppTheme.extendedColors
    // Bouncy scale when digit is entered
    val scale by animateFloatAsState(
        targetValue = if (digit.isNotEmpty()) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "digitScale_$index"
    )
    // Animated border color
    val borderColor: Color = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> extendedColors.accent
        digit.isNotEmpty() -> extendedColors.accent.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    // Background color
    val backgroundColor: Color = when {
        digit.isNotEmpty() -> extendedColors.accent.copy(alpha = 0.08f)
        isFocused -> extendedColors.accent.copy(alpha = 0.04f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .scale(scale)
            .clip(CardShapes.button)
            .background(backgroundColor)
            .border(
                width = if (isFocused || digit.isNotEmpty()) 2.dp else 1.dp,
                color = borderColor,
                shape = CardShapes.button
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            color = if (hasError) {
                MaterialTheme.colorScheme.error
            } else {
                extendedColors.accent
            }
        )
        // Cursor-like blinking indicator when focused and empty
        if (isFocused && digit.isEmpty()) {
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 24.dp)
                    .background(extendedColors.accent.copy(alpha = 0.6f))
            )
        }
    }
}
