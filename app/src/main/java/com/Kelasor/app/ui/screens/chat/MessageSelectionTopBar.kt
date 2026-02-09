package com.Kelasor.app.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.Kelasor.app.ui.theme.VazirFontFamily

/**
 * Selection top bar specifically for message selection.
 * Shows Edit (single selection), Copy, and Delete buttons.
 */
@Composable
fun MessageSelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: (deleteForEveryone: Boolean) -> Unit,
    onEditSelected: () -> Unit,
    onCopySelected: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteForEveryone by remember { mutableStateOf(false) }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                deleteForEveryone = false
            },
            title = { 
                Text(
                    text = "حذف پیام",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column {
                    Text(
                        text = "آیا مطمئن هستید که می‌خواهید ${selectedCount} پیام را حذف کنید؟",
                        fontFamily = VazirFontFamily
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = deleteForEveryone,
                            onCheckedChange = { deleteForEveryone = it }
                        )
                        Text(
                            text = "برای همه حذف شود",
                            fontFamily = VazirFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelected(deleteForEveryone)
                        showDeleteDialog = false
                        deleteForEveryone = false
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = VazirFontFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    deleteForEveryone = false
                }) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily
                    )
                }
            }
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MessageAppTheme.extendedColors.accent.copy(alpha = 0.1f))
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClearSelection) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$selectedCount",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Edit button (only when single message selected)
        if (selectedCount == 1) {
            IconButton(onClick = onEditSelected) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Copy button
        IconButton(onClick = onCopySelected) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Delete button
        IconButton(onClick = { showDeleteDialog = true }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
