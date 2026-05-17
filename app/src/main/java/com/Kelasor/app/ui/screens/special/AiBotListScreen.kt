package com.Kelasor.app.ui.screens.special

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.AiBotDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.SpecialFolderViewModel
import com.Kelasor.app.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

// ═══════════════════════════════════════════════════════════════════════════════
// 🤖 AI Bot List Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiBotListScreen(
    onNavigateBack: () -> Unit,
    onBotClick: (botId: String, botName: String, botType: String) -> Unit,
    onNavigateToChat: (String) -> Unit = {},
    viewModel: SpecialFolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors

    val generalBots: List<AiBotDto> = state.aiBots.filter { it.category == "GENERAL" }
    val specialistBots: List<AiBotDto> = state.aiBots.filter { it.category == "SPECIALIST" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "هوش مصنوعی کلاسور",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
        }
    ) { paddingValues ->
        if (state.isLoading && state.aiBots.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = extendedColors.accent)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── General Bots Section ──
                if (generalBots.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        BotSectionHeader(
                            title = "مدل‌های عمومی",
                            icon = Icons.Default.SmartToy
                        )
                    }
                    items(generalBots, key = { it.id }) { bot ->
                        AiBotCard(
                            bot = bot,
                            onClick = {
                                if (bot.botType == "mosbat_elm" || bot.botType == "mosbat_elm_bot") {
                                    viewModel.startChatWithBot("00000000-0000-0000-0000-000000000001") { chatId ->
                                        onNavigateToChat(chatId)
                                    }
                                } else {
                                    onBotClick(bot.id, bot.name, bot.botType)
                                }
                            }
                        )
                    }
                }

                // ── Specialist Bots Section ──
                if (specialistBots.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(4.dp))
                        BotSectionHeader(
                            title = "دستیارهای تخصصی",
                            icon = Icons.Default.Psychology
                        )
                    }
                    items(specialistBots, key = { it.id }) { bot ->
                        AiBotCard(
                            bot = bot,
                            onClick = {
                                if (bot.botType == "mosbat_elm" || bot.botType == "mosbat_elm_bot") {
                                    viewModel.startChatWithBot("00000000-0000-0000-0000-000000000001") { chatId ->
                                        onNavigateToChat(chatId)
                                    }
                                } else {
                                    onBotClick(bot.id, bot.name, bot.botType)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📌 Bot Section Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun BotSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = extendedColors.accent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎴 AI Bot Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiBotCard(
    bot: AiBotDto,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val isPremium: Boolean = bot.description?.contains("⭐") == true

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = extendedColors.accentGlow.copy(alpha = 0.15f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bot Avatar
            val drawableResId: Int? = getBotDrawableRes(bot.botType)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (drawableResId == null) {
                            Brush.linearGradient(
                                colors = if (bot.category == "GENERAL") {
                                    listOf(
                                        extendedColors.gradientStart,
                                        extendedColors.gradientEnd
                                    )
                                } else {
                                    listOf(
                                        Color(0xFF7C3AED),
                                        Color(0xFFDB2777)
                                    )
                                }
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color.Transparent, Color.Transparent)
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (drawableResId != null) {
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = bot.name,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = getBotEmoji(bot.botType),
                        fontSize = 24.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Bot Name
            Text(
                text = bot.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Premium Badge
            if (isPremium) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "پولی ⭐",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEAB308),
                    fontWeight = FontWeight.Medium
                )
            }
            // Description
            if (!bot.description.isNullOrBlank()) {
                val cleanDescription: String = bot.description.replace("⭐", "").trim()
                if (cleanDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cleanDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun getBotEmoji(botType: String): String {
    return when (botType) {
        "chatgpt" -> "🧠"
        "gemini" -> "✨"
        "deepseek" -> "🔍"
        "grok" -> "⚡"
        "copilot" -> "🤖"
        "exam_assistant" -> "📝"
        "translation_assistant" -> "🌐"
        "article_assistant" -> "📄"
        "file_analysis" -> "📊"
        "image_generation" -> "🎨"
        "powerpoint_assistant" -> "📑"
        "clip_assistant" -> "🎬"
        "paper_search" -> "🔬"
        else -> "🤖"
    }
}

private fun getBotDrawableRes(botType: String): Int? {
    return when (botType) {
        "chatgpt" -> R.drawable.chatgpt
        "gemini" -> R.drawable.gemeni
        "grok" -> R.drawable.grok
        "copilot" -> R.drawable.copilot
        "deepseek" -> R.drawable.deepseek
        else -> null
    }
}
