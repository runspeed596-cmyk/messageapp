package com.Kelasor.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

/**
 * Reusable delete confirmation dialog with "delete for everyone" option.
 * Follows the app's Persian RTL design language.
 */
@Composable
fun DeleteConfirmationDialog(
    messageCount: Int,
    onConfirm: (deleteForEveryone: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    var isDeleteForEveryone: Boolean by remember { mutableStateOf(false) }
    val titleText: String = if (messageCount == 1) "حذف پیام" else "حذف ${messageCount} پیام"
    val bodyText: String = if (messageCount == 1) {
        "آیا از حذف این پیام مطمئن هستید؟"
    } else {
        "آیا از حذف ${messageCount} پیام مطمئن هستید؟"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = DanaFontFamily
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = bodyText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isDeleteForEveryone,
                        onCheckedChange = { isDeleteForEveryone = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = extendedColors.accent,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "حذف برای همه",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(isDeleteForEveryone) }) {
                Text(
                    text = "حذف",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontFamily = DanaFontFamily
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "انصراف",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = DanaFontFamily
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface
    )
}
