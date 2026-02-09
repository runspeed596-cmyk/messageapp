package com.Kelasor.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily

/**
 * PIN Lock Screen - Shows when app starts if PIN lock is enabled
 */
@Composable
fun PinLockScreen(
    onPinVerified: () -> Unit,
    storedPin: String
) {
    val extendedColors = MessageAppTheme.extendedColors
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
            modifier = Modifier.padding(32.dp)
        ) {
            // Lock Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(extendedColors.accent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    modifier = Modifier.size(40.dp),
                    tint = extendedColors.accent
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "رمز امنیتی را وارد کنید",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "برای دسترسی به اپلیکیشن رمز ۴ رقمی را وارد کنید",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // PIN Input
            OutlinedTextField(
                value = enteredPin,
                onValueChange = { 
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        enteredPin = it
                        isError = false
                        
                        // Auto-verify when 4 digits entered
                        if (it.length == 4) {
                            if (it == storedPin) {
                                onPinVerified()
                            } else {
                                isError = true
                                errorMessage = "رمز اشتباه است"
                                enteredPin = ""
                            }
                        }
                    }
                },
                label = { Text("رمز PIN", fontFamily = VazirFontFamily) },
                singleLine = true,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.width(200.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontFamily = VazirFontFamily
                )
            )
            
            if (isError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = VazirFontFamily
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // PIN dots indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (enteredPin.length > index) 
                                    extendedColors.accent 
                                else 
                                    MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}
