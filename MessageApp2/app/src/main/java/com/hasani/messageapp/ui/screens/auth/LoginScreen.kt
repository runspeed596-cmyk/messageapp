package com.hasani.messageapp.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasani.messageapp.ui.components.PrimaryButton
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily
import com.hasani.messageapp.ui.viewmodel.AuthEvent
import com.hasani.messageapp.ui.viewmodel.AuthViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Login Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoginScreen(
    onNavigateToOtp: (String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    val isPhoneValid = phoneNumber.length >= 10
    // Handle events
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
    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                // Logo
                Box(
                    modifier = Modifier
                        .size(100.dp)
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
                Spacer(modifier = Modifier.height(24.dp))
                // Title
                Text(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.login_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                // Phone input
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
                                text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.mobile_number),
                                fontFamily = VazirFontFamily
                            )
                        },
                        placeholder = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.phone_placeholder),
                                fontFamily = VazirFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = extendedColors.accent
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = state.error != null,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                // Error message
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                // Login Button
                PrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.login_button),
                    onClick = {
                        if (isPhoneValid) {
                            viewModel.sendOtp(phoneNumber)
                        }
                    },
                    enabled = isPhoneValid && !state.isLoading,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                // Terms
                Text(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.login_terms),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    // }
}
