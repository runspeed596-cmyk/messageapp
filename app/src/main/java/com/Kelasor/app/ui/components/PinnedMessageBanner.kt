package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.Kelasor.app.domain.model.MessageType
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography

/**
 * Banner that shows pinned message(s) below the TopAppBar.
 * Clicking the banner scrolls to the pinned message in the list.
 * The close (X) button unpins the message.
 */
@Composable
fun PinnedMessageBanner(
    content: String,
    senderName: String?,
    messageType: MessageType,
    onClick: () -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Pin accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(extendedColors.accent)
        )
        // Pin icon
        Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = null,
            tint = extendedColors.accent,
            modifier = Modifier.size(18.dp)
        )
        // Content preview
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "پیام سنجاق‌شده",
                style = MessageAppTypography.chatTime,
                fontWeight = FontWeight.Bold,
                color = extendedColors.accent
            )
            Text(
                text = getPreviewText(content, messageType),
                style = MessageAppTypography.messageTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        // Close / Unpin button
        IconButton(
            onClick = onUnpin,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "برداشتن سنجاق",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Returns a user-friendly preview string based on message type.
 */
private fun getPreviewText(content: String, type: MessageType): String {
    return when (type) {
        MessageType.IMAGE -> "🖼️ تصویر"
        MessageType.VIDEO -> "🎬 ویدیو"
        MessageType.VOICE -> "🎤 پیام صوتی"
        MessageType.AUDIO -> "🎵 فایل صوتی"
        MessageType.FILE -> "📎 فایل"
        MessageType.LOCATION -> "📍 موقعیت مکانی"
        MessageType.VIDEO_NOTE -> "🎥 ویدیو پیام"
        MessageType.POLL -> "📊 نظرسنجی"
        else -> content
    }
}
