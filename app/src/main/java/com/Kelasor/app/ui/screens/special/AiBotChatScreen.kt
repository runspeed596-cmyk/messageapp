package com.Kelasor.app.ui.screens.special

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.AiBotMessageDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.AiBotChatViewModel
import com.Kelasor.app.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

// ═══════════════════════════════════════════════════════════════════════════════
// 🤖 AI Bot Chat Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiBotChatScreen(
    botId: String,
    botName: String,
    botType: String = "",
    onNavigateBack: () -> Unit,
    viewModel: AiBotChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    val listState = rememberLazyListState()
    var messageText: String by remember { mutableStateOf("") }
    val botIconRes: Int? = getBotIconRes(botType)

    // Load messages on first composition
    LaunchedEffect(botId) {
        viewModel.loadMessages(botId)
    }

    // Scroll to bottom on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (botIconRes == null) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                extendedColors.gradientStart,
                                                extendedColors.gradientEnd
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(Color.Transparent, Color.Transparent)
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (botIconRes != null) {
                                Image(
                                    painter = painterResource(id = botIconRes),
                                    contentDescription = botName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("🤖", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = botName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "آنلاین",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "بازگشت",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatInputBar(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    val text: String = messageText.trim()
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(botId, text)
                        messageText = ""
                    }
                },
                isSending = state.isSending
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                }
                state.messages.isEmpty() -> {
                    EmptyBotChat(botName = botName)
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            ChatBubble(message = message)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Bubble
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ChatBubble(message: AiBotMessageDto) {
    val extendedColors = MessageAppTheme.extendedColors
    val isUser: Boolean = message.role == "USER"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (isUser) {
                        extendedColors.accent
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⌨️ Chat Input Bar
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    val extendedColors = MessageAppTheme.extendedColors
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .padding(bottom = navBarPadding)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Input
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 4,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(extendedColors.accent),
                decorationBox = { innerTextField ->
                    Box {
                        if (messageText.isEmpty()) {
                            Text(
                                "پیام خود را بنویسید...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        // Send Button
        IconButton(
            onClick = onSend,
            enabled = messageText.isNotBlank() && !isSending,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (messageText.isNotBlank() && !isSending)
                        extendedColors.accent
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "ارسال",
                    tint = if (messageText.isNotBlank()) Color.White
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🫥 Empty Bot Chat State
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyBotChat(botName: String) {
    val extendedColors = MessageAppTheme.extendedColors

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                extendedColors.gradientStart.copy(alpha = 0.2f),
                                extendedColors.gradientEnd.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 40.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "سلام! من $botName هستم",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "هر سوالی دارید بپرسید، اینجا هستم تا کمکتان کنم!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getBotIconRes(botType: String): Int? {
    return when (botType) {
        "chatgpt" -> R.drawable.chatgpt
        "gemini" -> R.drawable.gemeni
        "grok" -> R.drawable.grok
        "copilot" -> R.drawable.copilot
        else -> null
    }
}
