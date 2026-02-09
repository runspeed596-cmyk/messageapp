package com.Kelasor.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Register Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToOtp: (String) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val isFormValid = fullName.isNotBlank() && username.isNotBlank() && phoneNumber.length >= 10
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
                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.register_title),
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.add_profile_picture),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    color = extendedColors.accent
                )
                Spacer(modifier = Modifier.height(32.dp))
                // Full name input
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.full_name),
                            fontFamily = VazirFontFamily
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = extendedColors.accent
                        )
                    },
                    singleLine = true,
                    shape = CardShapes.inputField,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = extendedColors.accent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = extendedColors.accent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Username input
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { 
                            if (it.all { char -> char.isLetterOrDigit() || char == '_' }) {
                                username = it.lowercase()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.username),
                                fontFamily = VazirFontFamily
                            )
                        },
                        prefix = { Text("@") },
                        singleLine = true,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Phone input
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                                phoneNumber = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.mobile_number),
                                fontFamily = VazirFontFamily
                            )
                        },
                        placeholder = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.phone_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = extendedColors.accent
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                // Register Button
                PrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.register_title),
                    onClick = {
                        isLoading = true
                        onNavigateToOtp(phoneNumber)
                    },
                    enabled = isFormValid,
                    isLoading = isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Terms
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.register_terms),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    // }
}
