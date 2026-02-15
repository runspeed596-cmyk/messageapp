package com.Kelasor.app.ui.screens.channel

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.ChannelSubscriber
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.AvatarType
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.viewmodel.ChannelEvent
import com.Kelasor.app.ui.viewmodel.ChannelSettingsEvent
import com.Kelasor.app.ui.viewmodel.ChannelSettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Channel Settings Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ChannelSettingsScreen(
    channelId: String,
    onNavigateBack: () -> Unit,
    onChannelDeleted: () -> Unit = {},
    onNavigateToMessage: (String, String) -> Unit,
    viewModel: ChannelSettingsViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var publicId by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    
    // Shared Media Action State
    var selectedSharedContent by remember { mutableStateOf<com.Kelasor.app.domain.model.SharedContent?>(null) }
    
    // Media Preview State
    var previewMediaUrl by remember { mutableStateOf<String?>(null) }
    var previewMediaType by remember { mutableStateOf(com.Kelasor.app.ui.components.MediaType.IMAGE) }

    // Media Preview Dialog
    previewMediaUrl?.let { url ->
        com.Kelasor.app.ui.components.MediaPreviewDialog(
            mediaUrl = url,
            mediaType = previewMediaType,
            onDismiss = { previewMediaUrl = null }
        )
    }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            viewModel.updateChannel(name, description, isPublic, publicId, uri)
        }
    }
    
    // Initialize fields when channel loads
    LaunchedEffect(state.channel) {
        state.channel?.let { channel ->
            name = channel.name
            description = channel.description ?: ""
            publicId = channel.publicId ?: ""
            isPublic = channel.isPublic
        }
    }
    
    // Load channel
    LaunchedEffect(channelId) {
        viewModel.loadChannel(channelId)
    }
    
    val context = LocalContext.current
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChannelSettingsEvent.ChannelUpdated -> {
                    android.widget.Toast.makeText(context, "تغییرات ذخیره شد", android.widget.Toast.LENGTH_SHORT).show()
                }
                is ChannelSettingsEvent.ChannelDeleted, is ChannelSettingsEvent.ChannelLeft -> onChannelDeleted()
                is ChannelSettingsEvent.Error -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                    showDeleteDialog = false // Close dialog if error occurred during delete
                }
                else -> {}
            }
        }
    }

    val canEdit = state.isOwner || state.channel?.isAdmin == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "تنظیمات کانال",
                style = MessageAppTypography.chatName,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (canEdit) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = extendedColors.accent
                    )
                } else {
                    IconButton(onClick = {
                        viewModel.updateChannel(name, description, isPublic, publicId, selectedImageUri)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            tint = extendedColors.accent
                        )
                    }
                }
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Section
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        AvatarImage(
                            imageUrl = if (selectedImageUri != null) selectedImageUri.toString() else state.channel?.avatarUrl,
                            name = state.channel?.name ?: "",
                            size = AvatarSize.XLARGE,
                            avatarType = AvatarType.CHANNEL
                        )
                        
                        if (canEdit) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(extendedColors.accent)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Channel Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "اطلاعات کانال",
                            style = MessageAppTypography.chatName,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("نام کانال") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = canEdit,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.accent,
                                cursorColor = extendedColors.accent
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("توضیحات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            enabled = canEdit,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.accent,
                                cursorColor = extendedColors.accent
                            )
                        )
                        
                        // Public ID Field (Only if public)
                        if (isPublic) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = publicId,
                                onValueChange = { publicId = it },
                                label = { Text("آیدی عمومی (لینک اختصاصی)") },
                                placeholder = { Text("example_channel") },
                                prefix = { Text("@") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = canEdit,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = extendedColors.accent,
                                    cursorColor = extendedColors.accent
                                )
                            )
                            Text(
                                text = "در صورت تنظیم، کاربران می‌توانند کانال شما را با @$publicId پیدا کنند.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "کانال عمومی",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "همه می‌توانند این کانال را پیدا کنند",
                                    style = MessageAppTypography.chatTime,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { isPublic = it },
                                enabled = canEdit,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = extendedColors.accent
                                )
                            )
                        }
                    }
                }
            }
            
            // Invite Link Card
            item {
                val channel = state.channel
                if (channel != null) {
                    // Generate link based on publicId if available, otherwise use inviteLink
                    val displayLink = if (!channel.publicId.isNullOrBlank()) {
                        "https://t.me/${channel.publicId}"
                    } else {
                        channel.inviteLink
                    }
                    
                    if (displayLink != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "لینک دعوت",
                                    style = MessageAppTypography.chatName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayLink,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(displayLink))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = extendedColors.accent
                                        )
                                    }
                                    
                                    if (canEdit && channel.publicId.isNullOrBlank()) {
                                        // Only show regenerate for invite code links, not publicId links
                                        IconButton(onClick = {
                                            viewModel.regenerateInviteLink()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Regenerate",
                                                tint = extendedColors.accent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            


            // Shared Media Filter
            item {
                Text(
                    text = "رسانه‌های اشتراکی",
                    style = MessageAppTypography.sectionTitle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
                
                com.Kelasor.app.ui.components.ContentFilter(
                    selectedType = state.selectedContentType,
                    onTypeSelected = { viewModel.onFilterSelected(it) }
                )
                
                if (state.selectedContentType != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    com.Kelasor.app.ui.components.SharedMediaGrid(
                        content = state.sharedContent,
                        isLoading = state.isMediaLoading,
                        onItemClick = { item ->
                            selectedSharedContent = item
                        }
                    )
                }
            }
            
            // Danger Zone - Positioned prominently after Invite Link
            item {
                val isOwner = state.isOwner
                if (state.channel != null) { // Safe check, everyone can either Leave or Delete (if owner)
                    // Actually, logic:
                    // If owner -> Delete Channel
                    // If not owner -> Leave Channel
                    // This logic is already handled, but we need to ensure admin sees "Leave", not "Delete".
                    val actionText = if (isOwner) "حذف کانال" else "ترک کانال"
                    val actionIcon = if (isOwner) Icons.Default.Delete else Icons.AutoMirrored.Filled.ArrowBack
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { showDeleteDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = actionIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = actionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (isOwner) "این عمل قابل برگشت نیست" else "می‌توانید دوباره عضو شوید",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مشترکین (${state.subscribers.size})",
                        style = MessageAppTypography.chatName,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (canEdit) {
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Member",
                                tint = extendedColors.accent
                            )
                        }
                    }
                }
            }
            
            items(state.subscribers, key = { it.user.id }) { subscriber ->
                SubscriberItem(
                    subscriber = subscriber,
                    onToggleAdmin = {
                        if (subscriber.isAdmin) {
                            viewModel.removeAdmin(subscriber.user.id)
                        } else {
                            viewModel.addAdmin(subscriber.user.id)
                        }
                    }
                )
            }
        }
    }
    
    // Delete/Leave Confirmation Dialog
    if (showDeleteDialog) {
        val isOwner = state.isOwner
        val title = if (isOwner) "حذف کانال" else "ترک کانال"
        val message = if (isOwner) 
            "آیا مطمئن هستید که می‌خواهید این کانال را حذف کنید؟ این عمل قابل برگشت نیست." 
        else 
            "آیا مطمئن هستید که می‌خواهید کانال را ترک کنید؟"
        val confirmText = if (isOwner) "حذف" else "ترک کانال"

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        if (isOwner) viewModel.deleteChannel() else viewModel.unsubscribe()
                    }
                ) {
                    Text(confirmText, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("لغو")
                }
            }
        )
    }

    if (showAddMemberDialog) {
        LaunchedEffect(Unit) {
            viewModel.loadContacts()
        }
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAddMembers = { userIds ->
                viewModel.addMembers(userIds)
            },
            contacts = state.contacts,
            searchResults = state.searchResults,
            searchQuery = state.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            currentMemberIds = state.subscribers.map { it.user.id }.toSet()
        )
    }
    
    // Shared Media Action Sheet
    com.Kelasor.app.ui.components.SharedMediaActionSheet(
        content = selectedSharedContent,
        onDismissRequest = { selectedSharedContent = null },
        onView = {
            selectedSharedContent?.let { item ->
                when (item.type) {
                    com.Kelasor.app.domain.model.MessageType.IMAGE,
                    com.Kelasor.app.domain.model.MessageType.VIDEO -> {
                        previewMediaUrl = item.url
                        previewMediaType = if (item.type == com.Kelasor.app.domain.model.MessageType.VIDEO) 
                            com.Kelasor.app.ui.components.MediaType.VIDEO 
                        else 
                            com.Kelasor.app.ui.components.MediaType.IMAGE
                    }
                    com.Kelasor.app.domain.model.MessageType.LINK -> {
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "خطا در باز کردن لینک", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        android.widget.Toast.makeText(context, "قابلیت نمایش این فایل هنوز پیاده‌سازی نشده است", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        },
        onShowInChat = {
            selectedSharedContent?.let { item ->
                onNavigateToMessage(channelId, item.messageId)
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 Subscriber Item
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SubscriberItem(
    subscriber: ChannelSubscriber,
    onToggleAdmin: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                imageUrl = subscriber.user.displayAvatarUrl,
                name = subscriber.user.displayName,
                size = AvatarSize.MEDIUM
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscriber.user.displayName,
                    style = MessageAppTypography.chatName,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (subscriber.isAdmin) "مدیر" else "مشترک",
                    style = MessageAppTypography.chatTime,
                    color = if (subscriber.isAdmin) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            TextButton(onClick = onToggleAdmin) {
                Text(
                    text = if (subscriber.isAdmin) "حذف مدیریت" else "تبدیل به مدیر",
                    color = extendedColors.accent
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ➕ Add Member Dialog
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMembers: (List<String>) -> Unit,
    contacts: List<com.Kelasor.app.domain.model.User>,
    searchResults: List<com.Kelasor.app.domain.model.User>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentMemberIds: Set<String>
) {
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val extendedColors = MessageAppTheme.extendedColors
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "افزودن عضو جدید",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            text = "جستجو...",
                            style = MaterialTheme.typography.bodyLarge
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val usersToShow = if (searchQuery.isNotBlank()) {
                        searchResults
                    } else {
                        contacts
                    }
                    
                    items(items = usersToShow, key = { it.id }) { user ->
                        val isAlreadyMember = currentMemberIds.contains(user.id)
                        if (!isAlreadyMember) {
                            UserSelectionItem(
                                user = user,
                                isSelected = selectedUserIds.contains(user.id),
                                onToggle = {
                                    selectedUserIds = if (selectedUserIds.contains(user.id)) {
                                        selectedUserIds - user.id
                                    } else {
                                        selectedUserIds + user.id
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("لغو")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Button(
                        onClick = {
                            onAddMembers(selectedUserIds.toList())
                            onDismiss()
                        },
                        enabled = selectedUserIds.isNotEmpty()
                    ) {
                        Text("افزودن (${selectedUserIds.size})")
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSelectionItem(
    user: com.Kelasor.app.domain.model.User,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            imageUrl = user.displayAvatarUrl,
            name = user.contactName ?: user.displayName,
            size = AvatarSize.SMALL,
            isOnline = user.isOnline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.contactName ?: user.displayName,
                style = MessageAppTypography.chatName,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${user.username}",
                style = MessageAppTypography.chatTime,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}
