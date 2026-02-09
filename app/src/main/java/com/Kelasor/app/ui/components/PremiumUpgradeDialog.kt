package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.Kelasor.app.ui.theme.MessageAppTypography

@Composable
fun PremiumUpgradeDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), // Dark background
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000)) // Gold Gradient
                            ),
                            shape = RoundedCornerShape(100)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "ارتقا به نسخه پریمیوم",
                    style = MessageAppTypography.chatName.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "کاربران عادی فقط می‌توانند یک استوری فعال داشته باشند. برای ارسال استوری‌های نامحدود و دسترسی به امکانات ویژه، حساب خود را ارتقا دهید.",
                    style = MessageAppTypography.body.copy(
                        lineHeight = 24.sp
                    ),
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Upgrade Button
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues() // Remove default padding for gradient
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ارتقا به پریمیوم",
                            style = MessageAppTypography.buttonText,
                            color = Color.Black // Contrast with Gold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "متوجه شدم",
                        style = MessageAppTypography.buttonText,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
