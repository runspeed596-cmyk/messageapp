package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.VazirFontFamily

/**
 * Bottom action panel for multi-select message mode.
 * Shows action icons (forward, reply, copy, delete, pin).
 * Does NOT cover the entire screen — sits at the bottom replacing the input bar.
 */
@Composable
fun MultiSelectActionPanel(
    selectedCount: Int,
    onForwardClick: () -> Unit,
    onReplyClick: (() -> Unit)? = null,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPinClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                    )
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(top = 12.dp, bottom = 8.dp)
            .navigationBarsPadding()
    ) {
        // ── Action Icons Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply — only when single message selected
            if (onReplyClick != null && selectedCount == 1) {
                ActionIconButton(
                    icon = Icons.AutoMirrored.Filled.Reply,
                    label = "پاسخ",
                    onClick = onReplyClick
                )
            }
            // Forward
            ActionIconButton(
                icon = Icons.Default.Share,
                label = "فوروارد",
                onClick = onForwardClick
            )
            // Copy
            ActionIconButton(
                icon = Icons.Default.ContentCopy,
                label = "کپی",
                onClick = onCopyClick
            )
            // Pin
            if (onPinClick != null) {
                ActionIconButton(
                    icon = Icons.Default.PushPin,
                    label = "پین",
                    onClick = onPinClick
                )
            }
            // Delete
            ActionIconButton(
                icon = Icons.Default.Delete,
                label = "حذف",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
        }
    }
}

/**
 * Action icon button with label for the bottom action row.
 */
@Composable
private fun ActionIconButton(
    icon: ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Medium,
            color = tint,
            fontSize = 10.sp
        )
    }
}
