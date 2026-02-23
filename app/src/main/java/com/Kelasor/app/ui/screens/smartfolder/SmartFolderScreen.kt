package com.Kelasor.app.ui.screens.smartfolder

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.data.remote.dto.SmartFolderChannelDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.SmartFolderViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder Channel List Screen
// Reusable for Teachers, Elm Club, and Courses tabs
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SmartFolderTabContent(
    folderType: String,
    title: String,
    emptyIcon: androidx.compose.ui.graphics.vector.ImageVector,
    emptyMessage: String,
    accentColor: Color,
    onChannelClick: (String) -> Unit,
    viewModel: SmartFolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val channels: List<SmartFolderChannelDto> = when (folderType) {
        "TEACHERS" -> state.teacherChannels
        "ELM_CLUB" -> state.elmClubChannels
        "COURSES" -> state.courseChannels
        else -> emptyList()
    }
    val isLoading: Boolean = state.isLoading
    val error: String? = state.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = accentColor
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirFontFamily
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.loadSmartFolders() }) {
                        Text("تلاش مجدد", fontFamily = VazirFontFamily, color = accentColor)
                    }
                }
            }
            channels.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = emptyIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        fontFamily = VazirFontFamily
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels, key = { it.id }) { channel ->
                        SmartFolderChannelCard(
                            channel = channel,
                            accentColor = accentColor,
                            onClick = { onChannelClick(channel.id) }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📇 Channel Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SmartFolderChannelCard(
    channel: SmartFolderChannelDto,
    accentColor: Color,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            accentColor.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (channel.avatarUrl != null) {
                AsyncImage(
                    model = channel.avatarUrl,
                    contentDescription = channel.name,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = channel.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = VazirFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = VazirFontFamily,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (channel.isVerifiedTeacher) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تایید شده",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${channel.subscriberCount} عضو",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = VazirFontFamily
            )
        }

        // Subscription badge
        if (channel.isSubscribed) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "عضو",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontFamily = VazirFontFamily,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
