package com.Kelasor.app.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily

/**
 * Dialog for editing a message.
 * Shows original message content and allows user to modify it.
 */
@Composable
fun EditMessageDialog(
    originalMessage: String,
    onConfirm: (newContent: String) -> Unit,
    onDismiss: () -> Unit
) {
    var editedContent by remember { mutableStateOf(originalMessage) }
    val extendedColors = MessageAppTheme.extendedColors
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ویرایش پیام",
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "پیام جدید را وارد کنید:",
                    fontFamily = VazirFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShapes.inputField)
                        .padding(12.dp),
                    textStyle = TextStyle(
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(extendedColors.accent),
                    decorationBox = { innerTextField ->
                        if (editedContent.isEmpty()) {
                            Text(
                                text = "پیام...",
                                fontFamily = VazirFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (editedContent.isNotBlank() && editedContent != originalMessage) {
                        onConfirm(editedContent.trim())
                    }
                },
                enabled = editedContent.isNotBlank() && editedContent != originalMessage
            ) {
                Text(
                    text = "ذخیره",
                    fontFamily = VazirFontFamily,
                    color = if (editedContent.isNotBlank() && editedContent != originalMessage) 
                        extendedColors.accent 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "انصراف",
                    fontFamily = VazirFontFamily
                )
            }
        }
    )
}
