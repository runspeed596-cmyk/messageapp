package com.Kelasor.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.data.remote.dto.ChannelDto
import com.Kelasor.app.data.remote.dto.CreateAdRequestDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdChannelPickerDialog(
    isVisible: Boolean,
    messageContent: String,
    messageMediaUrl: String?,
    messageType: String,
    sourceType: String,
    sourceId: String?,
    sourceMessageId: String,
    channels: List<ChannelDto>,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onSearchQuery: (String) -> Unit,
    onSubmit: (CreateAdRequestDto) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    var selectedChannelId: String? by remember { mutableStateOf(null) }
    var searchQuery: String by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val goldGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFFFD54F), Color(0xFFFFA726))
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(goldGradient)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = Color.White
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "تبلیغ در کانال",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        // Placeholder for symmetry
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        coroutineScope.launch {
                            delay(300)
                            onSearchQuery(query)
                        }
                    },
                    placeholder = { Text("جستجوی کانال...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                // Channel list
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFA726))
                    }
                } else if (channels.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "کانالی یافت نشد",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(channels) { channel ->
                            val isSelected = selectedChannelId == channel.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedChannelId = channel.id }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                2.dp,
                                                Color(0xFFFFA726),
                                                RoundedCornerShape(16.dp)
                                            )
                                        } else Modifier
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        Color(0xFFFFA726).copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFA726).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (channel.avatarUrl != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(channel.avatarUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = channel.name,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = channel.name.take(1),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color(0xFFFFA726)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    // Channel info
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = channel.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${channel.subscriberCount} عضو",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    // Selection indicator
                                    AnimatedVisibility(visible = isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFA726)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // Submit button
                Button(
                    onClick = {
                        val channelId = selectedChannelId ?: return@Button
                        onSubmit(
                            CreateAdRequestDto(
                                messageContent = messageContent,
                                targetChannelId = channelId,
                                sourceType = sourceType,
                                sourceId = sourceId,
                                sourceMessageId = sourceMessageId,
                                messageMediaUrl = messageMediaUrl,
                                messageType = messageType
                            )
                        )
                    },
                    enabled = selectedChannelId != null && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA726),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFFFA726).copy(alpha = 0.3f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ارسال درخواست تبلیغ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
