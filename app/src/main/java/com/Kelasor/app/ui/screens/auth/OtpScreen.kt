package com.Kelasor.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.components.TextButton
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.AuthEvent
import com.Kelasor.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 OTP Verification Screen
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
    val isOtpComplete = otpValue.length == 6
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> {
                    if (event.isNewUser) {
                        onNavigateToUserInfo()
                    } else {
                        onNavigateToMain()
                    }
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
        focusRequester.requestFocus()
    }
    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            extendedColors.gradientStart.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .imePadding()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.verify_number),
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back)
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                // Instructions
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.verification_code_sent),
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.enter_verification_code, phoneNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                // OTP Input
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(6) { index ->
                                OtpDigitBox(
                                    digit = otpValue.getOrNull(5 - index)?.toString() ?: "",
                                    isFocused = index == 5 - otpValue.length,
                                    hasError = state.error != null
                                )
                            }
                        }
                    }
                )
                // Error message
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(),
                    exit = fadeOut()
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
                // Verify Button
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
                Spacer(modifier = Modifier.height(24.dp))
                // Resend section
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
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    // }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    hasError: Boolean
) {
    val extendedColors = MessageAppTheme.extendedColors
    val scale by animateFloatAsState(
        targetValue = if (digit.isNotEmpty()) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "digitScale"
    )
    val borderColor = when {
        hasError -> MaterialTheme.colorScheme.error
        isFocused -> extendedColors.accent
        digit.isNotEmpty() -> extendedColors.gradientStart
        else -> MaterialTheme.colorScheme.outline
    }
    Box(
        modifier = Modifier
            .size(45.dp)
            .scale(scale)
            .clip(CardShapes.button)
            .background(
                if (digit.isNotEmpty()) {
                    extendedColors.gradientStart.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
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
                extendedColors.gradientStart
            }
        )
    }
}
