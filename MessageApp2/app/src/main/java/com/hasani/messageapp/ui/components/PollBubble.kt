package com.hasani.messageapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.domain.model.Poll
import com.hasani.messageapp.domain.model.PollOption

@Composable
fun PollBubble(
    poll: Poll,
    onVote: (String, List<String>) -> Unit, // pollId, optionIds
    isFromMe: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFromMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = poll.question,
                style = MessageAppTypography.chatName,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (poll.isAnonymous) {
                Text(
                    text = "نظرسنجی ناشناس",
                    style = MessageAppTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            poll.options.forEach { option ->
                PollOptionItem(
                    option = option,
                    isSelected = poll.userVotedOptionIds.contains(option.id),
                    showResults = true,
                    isFromMe = isFromMe,
                    onClick = {
                        if (poll.isMultipleChoice) {
                            val currentVotes = poll.userVotedOptionIds.toMutableList()
                            if (currentVotes.contains(option.id)) {
                                currentVotes.remove(option.id)
                            } else {
                                currentVotes.add(option.id)
                            }
                            onVote(poll.id, currentVotes)
                        } else {
                            if (!poll.userVotedOptionIds.contains(option.id)) {
                                onVote(poll.id, listOf(option.id))
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (poll.isMultipleChoice) "امکان انتخاب چند گزینه" else "انتخاب یک گزینه",
                    style = MessageAppTypography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = "${poll.totalVotes} رأی",
                    style = MessageAppTypography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PollOptionItem(
    option: PollOption,
    isSelected: Boolean,
    showResults: Boolean,
    isFromMe: Boolean,
    onClick: () -> Unit
) {
    val percentage = option.votePercentage / 100f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isFromMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
            .clickable(onClick = onClick)
    ) {
        // Progress Bar Background
        if (showResults && percentage > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(
                        if (isFromMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    modifier = Modifier.size(20.dp),
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = option.text,
                    style = MessageAppTypography.body,
                    maxLines = 1
                )
            }
            
            if (showResults) {
                Text(
                    text = "${option.votePercentage.toInt()}%",
                    style = MessageAppTypography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
