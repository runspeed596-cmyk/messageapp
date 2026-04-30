package com.hasani.messageapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasani.messageapp.domain.model.Chat
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.components.AvatarType
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.ui.viewmodel.ChatListViewModel
import com.hasani.messageapp.ui.viewmodel.GroupListViewModel
import com.hasani.messageapp.ui.viewmodel.ChannelListViewModel

/**
 * Screen to display archived chats, groups, and channels with selection mode for bulk unarchiving.
 * Accessible from Profile > آرشیو شده‌ها
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedChatsScreen(
    onNavigateBack: () -> Unit,
    onChatClick: (Chat) -> Unit,
    onGroupClick: (String) -> Unit = {},
    onChannelClick: (String) -> Unit = {},
    viewModel: ChatListViewModel = hiltViewModel(),
    groupViewModel: GroupListViewModel = hiltViewModel(),
    channelViewModel: ChannelListViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val groupState by groupViewModel.state.collectAsState()
    val channelState by channelViewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Local selection state for archived chats screen
    val selectedChatIds = remember { mutableStateListOf<String>() }
    val inSelectionMode = selectedChatIds.isNotEmpty()
    
    // Check if there's any archived content
    val hasArchivedContent = state.archivedChats.isNotEmpty() || 
        groupState.archivedGroups.isNotEmpty() || 
        channelState.archivedChannels.isNotEmpty()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Selection mode header or normal header
        if (inSelectionMode) {
            TopAppBar(
                title = {
                    Text(
                        text = "${selectedChatIds.size} انتخاب شده",
                        style = MessageAppTypography.chatName
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { selectedChatIds.clear() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "لغو انتخاب"
                        )
                    }
                },
                actions = {
                    // Unarchive selected
                    IconButton(onClick = {
                        val chatIdsToUnarchive = selectedChatIds.toList()
                        chatIdsToUnarchive.forEach { chatId ->
                            viewModel.archiveChat(chatId, false)
                        }
                        selectedChatIds.clear()
                        android.widget.Toast.makeText(
                            context,
                            "گفتگوها از آرشیو خارج شدند",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Unarchive,
                            contentDescription = "خارج کردن از آرشیو"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        } else {
            TopAppBar(
                title = {
                    Text(
                        text = "آرشیو شده‌ها",
                        style = MessageAppTypography.chatName
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        
        if (!hasArchivedContent) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "هیچ موردی آرشیو نشده است",
                        style = MessageAppTypography.chatName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "گفتگوها، گروه‌ها و کانال‌های آرشیو شده اینجا نمایش داده می‌شوند",
                        style = MessageAppTypography.chatPreview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Archived Chats Section
                if (state.archivedChats.isNotEmpty()) {
                    item {
                        Text(
                            text = "گفتگوها (${state.archivedChats.size})",
                            style = MessageAppTypography.sectionTitle,
                            color = extendedColors.accent,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(state.archivedChats, key = { "chat_${it.id}" }) { chat ->
                        val isSelected = chat.id in selectedChatIds
                        ChatItem(
                            chat = chat,
                            isSelected = isSelected,
                            inSelectionMode = inSelectionMode,
                            onClick = { 
                                if (inSelectionMode) {
                                    if (isSelected) selectedChatIds.remove(chat.id) else selectedChatIds.add(chat.id)
                                } else {
                                    onChatClick(chat) 
                                }
                            },
                            onLongClick = { 
                                if (isSelected) selectedChatIds.remove(chat.id) else selectedChatIds.add(chat.id)
                            },
                            onPin = { viewModel.pinChat(chat.id, !chat.isPinned) },
                            onMute = { viewModel.muteChat(chat.id, !chat.isMuted) },
                            onArchive = { 
                                viewModel.archiveChat(chat.id, false)
                                android.widget.Toast.makeText(context, "گفتگو از آرشیو خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteChat(chat.id) },
                            onUnarchiveClick = {
                                viewModel.archiveChat(chat.id, false)
                                android.widget.Toast.makeText(context, "گفتگو از آرشیو خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
                
                // Archived Groups Section
                if (groupState.archivedGroups.isNotEmpty()) {
                    item {
                        Text(
                            text = "گروه‌ها (${groupState.archivedGroups.size})",
                            style = MessageAppTypography.sectionTitle,
                            color = extendedColors.accent,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(groupState.archivedGroups, key = { "group_${it.id}" }) { group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGroupClick(group.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                imageUrl = group.avatarUrl,
                                name = group.name,
                                size = AvatarSize.MEDIUM,
                                avatarType = AvatarType.GROUP
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    style = MessageAppTypography.chatName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${group.memberCount} عضو",
                                    style = MessageAppTypography.chatPreview,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                groupViewModel.archiveGroup(group.id, false)
                                android.widget.Toast.makeText(context, "گروه از آرشیو خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Unarchive,
                                    contentDescription = "خارج کردن از آرشیو",
                                    tint = extendedColors.accent
                                )
                            }
                        }
                    }
                }
                
                // Archived Channels Section
                if (channelState.archivedChannels.isNotEmpty()) {
                    item {
                        Text(
                            text = "کانال‌ها (${channelState.archivedChannels.size})",
                            style = MessageAppTypography.sectionTitle,
                            color = extendedColors.accent,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    
                    items(channelState.archivedChannels, key = { "channel_${it.id}" }) { channel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChannelClick(channel.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(
                                imageUrl = channel.avatarUrl,
                                name = channel.name,
                                size = AvatarSize.MEDIUM,
                                avatarType = AvatarType.CHANNEL
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    style = MessageAppTypography.chatName,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${channel.subscriberCount} عضو",
                                    style = MessageAppTypography.chatPreview,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                channelViewModel.archiveChannel(channel.id, false)
                                android.widget.Toast.makeText(context, "کانال از آرشیو خارج شد", android.widget.Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Unarchive,
                                    contentDescription = "خارج کردن از آرشیو",
                                    tint = extendedColors.accent
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
