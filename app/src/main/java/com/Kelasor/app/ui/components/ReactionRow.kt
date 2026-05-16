package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

/**
 * Reusable reaction row for all bubble types (voice, image, video, file, location, poll).
 * Shows emoji pills with count, highlights the user's own reaction.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionRow(
    reactions: Map<String, Int>,
    myReaction: String? = null,
    isMyMessage: Boolean,
    onReactionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return
    val extendedColors = MessageAppTheme.extendedColors
    Spacer(Modifier.height(2.dp))
    FlowRow(
        modifier = modifier.padding(top = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        reactions.forEach { (emoji, count) ->
            val isSelected: Boolean = emoji == myReaction
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) extendedColors.accent.copy(alpha = 0.25f)
                        else if (isMyMessage) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.15f)
                    )
                    .clickable { onReactionClick?.invoke(emoji) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$emoji $count",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontFamily = DanaFontFamily
                    ),
                    color = if (isMyMessage) extendedColors.myBubbleText else extendedColors.otherBubbleText
                )
            }
        }
    }
}
