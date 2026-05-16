package com.Kelasor.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration Request Dialog
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun CollaborationRequestDialog(
    recipientName: String,
    onDismiss: () -> Unit,
    onSend: (title: String, message: String) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "درخواست همکاری",
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ارسال درخواست همکاری به $recipientName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { 
                        Text(
                            text = "عنوان درخواست",
                            fontFamily = DanaFontFamily
                        )
                    },
                    placeholder = {
                        Text(
                            text = "مثال: همکاری در پروژه...",
                            fontFamily = DanaFontFamily
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { 
                        Text(
                            text = "پیام",
                            fontFamily = DanaFontFamily
                        )
                    },
                    placeholder = {
                        Text(
                            text = "توضیحات درخواست خود را بنویسید...",
                            fontFamily = DanaFontFamily
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        isSubmitting = true
                        onSend(title, message)
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank() && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = extendedColors.accent
                )
            ) {
                Text(
                    text = "ارسال",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = "انصراف",
                    fontFamily = DanaFontFamily
                )
            }
        }
    )
}
