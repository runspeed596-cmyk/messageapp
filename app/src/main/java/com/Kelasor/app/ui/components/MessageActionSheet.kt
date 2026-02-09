package com.Kelasor.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.domain.model.Message
import com.Kelasor.app.ui.theme.MessageAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageActionSheet(
    message: Message? = null,
    isOwner: Boolean,
    showDeleteForEveryone: Boolean = true, // Show checkbox for admins/owners
    replyLabel: String = "پاسخ", // Can be "نظر" for channels
    onDismissRequest: () -> Unit,
    onReactionClick: (String) -> Unit,
    onReplyClick: (() -> Unit)? = null,
    onCopyClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: ((deleteForEveryone: Boolean) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState()
    val extendedColors = MessageAppTheme.extendedColors
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteForEveryone by remember { mutableStateOf(true) }

    // Delete Confirmation Dialog
    if (showDeleteDialog && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("حذف پیام") },
            text = {
                Column {
                    Text("آیا از حذف این پیام اطمینان دارید؟")
                    if (showDeleteForEveryone) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { deleteForEveryone = !deleteForEveryone }
                        ) {
                            Checkbox(
                                checked = deleteForEveryone,
                                onCheckedChange = { deleteForEveryone = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حذف برای همه")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick(deleteForEveryone)
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Reactions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val reactions = listOf("👍", "❤️", "😂", "😮", "😢", "😡")
                reactions.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier
                            .clickable { onReactionClick(emoji) }
                            .padding(8.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Actions
            if (onReplyClick != null) {
                ActionItem(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    text = replyLabel,
                    onClick = onReplyClick
                )
            }
            
            if (onCopyClick != null) {
                ActionItem(
                    icon = Icons.Default.ContentCopy,
                    text = "کپی",
                    onClick = onCopyClick
                )
            }

            if (onEditClick != null) {
                ActionItem(
                    icon = Icons.Default.Edit,
                    text = "ویرایش",
                    onClick = onEditClick
                )
            }
            
            if (onDeleteClick != null) {
                ActionItem(
                    icon = Icons.Default.Delete,
                    text = "حذف",
                    color = MaterialTheme.colorScheme.error,
                    onClick = { showDeleteDialog = true }
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
