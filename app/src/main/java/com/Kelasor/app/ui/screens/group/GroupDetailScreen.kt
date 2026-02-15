package com.Kelasor.app.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.R
import com.Kelasor.app.domain.model.GroupMember
import com.Kelasor.app.domain.model.MemberRole
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.GroupDetailViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Detail Screen (Profile View)
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToGroupSettings: () -> Unit,
    onGroupDeleted: () -> Unit = {},
    onNavigateToMessage: (String, String) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showLeaveDeleteDialog by remember { mutableStateOf(false) }
    var showPromoteDialog by remember { mutableStateOf<String?>(null) }
    
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

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }
    
    // Handle events for navigation
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.Kelasor.app.ui.viewmodel.GroupEvent.GroupDeleted,
                is com.Kelasor.app.ui.viewmodel.GroupEvent.GroupLeft -> {
                    onGroupDeleted()
                }
                is com.Kelasor.app.ui.viewmodel.GroupEvent.Error -> {
                    // Show error toast if needed
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.group_info),
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                // Only show settings for admins
                if (state.isAdmin || state.isOwner || state.group?.allowMembersToEditInfo == true) {
                    IconButton(onClick = onNavigateToGroupSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (state.isLoading && state.group == null) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(32.dp)
            )
        } else if (state.group != null) {
            val group = state.group!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        extendedColors.gradientStart.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CardShapes.profileHeader
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AvatarImage(
                            imageUrl = group.avatarUrl,
                            name = group.name,
                            size = AvatarSize.XLARGE
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = VazirFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " " + stringResource(R.string.members_count, group.memberCount),
                                style = MessageAppTypography.chatPreview,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Show role badge
                        group.myRole?.let { role ->
                            Spacer(modifier = Modifier.height(8.dp))
                            RoleBadge(role = role)
                        }
                    }
                }

                // Description section
                if (!group.description.isNullOrBlank()) {
                    item {
                        SectionCard(
                            title = stringResource(R.string.description),
                            content = group.description
                        )
                    }
                }

                // Members section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.members_section, state.members.size),
                            style = MessageAppTypography.sectionTitle,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (state.isAdmin || state.isOwner) {
                            IconButton(onClick = { showAddMemberDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Add Member",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                items(state.members, key = { it.user.id }) { member ->
                    MemberItem(
                        member = member,
                        isCurrentUserOwner = state.isOwner,
                        onPromote = { showPromoteDialog = member.user.id },
                        onDemote = { viewModel.demoteFromAdmin(member.user.id) },
                        onRemove = { viewModel.removeMember(member.user.id) }
                    )
                }

                // Shared Media Header & Filter
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "رسانه‌های اشتراکی",
                        style = MessageAppTypography.sectionTitle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                
                // Danger Zone - Leave or Delete Group
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { showLeaveDeleteDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isOwner) Icons.Default.Delete else Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (state.isOwner) "حذف گروه" else "ترک گروه",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = VazirFontFamily,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Leave/Delete Confirmation Dialog
    if (showLeaveDeleteDialog) {
        val title = if (state.isOwner) "حذف گروه" else "ترک گروه"
        val message = if (state.isOwner) 
            "آیا مطمئن هستید که می‌خواهید این گروه را حذف کنید؟ این عمل قابل برگشت نیست."
        else 
            "آیا مطمئن هستید که می‌خواهید این گروه را ترک کنید؟"
        
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLeaveDeleteDialog = false },
            title = { Text(title, fontFamily = VazirFontFamily) },
            text = { Text(message, fontFamily = VazirFontFamily) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDeleteDialog = false
                        if (state.isOwner) viewModel.deleteGroup() else viewModel.leaveGroup()
                    }
                ) {
                    Text(
                        if (state.isOwner) "حذف" else "ترک گروه",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = VazirFontFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel), fontFamily = VazirFontFamily)
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
            onAddMembers = { memberIds ->
                viewModel.addMembers(memberIds)
            },
            contacts = state.contacts,
            searchResults = state.searchResults,
            searchQuery = state.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            currentMembers = state.members
        )
    }

    if (showPromoteDialog != null) {
        PromoteAdminDialog(
            onDismiss = { showPromoteDialog = null },
            onConfirm = { canEditInfo, canPostStory, canAddMembers, canRemoveMembers ->
                val userId = showPromoteDialog
                if (userId != null) {
                    viewModel.promoteToAdmin(
                        userId,
                        canEditInfo,
                        canPostStory,
                        canAddMembers,
                        canRemoveMembers
                    )
                }
                showPromoteDialog = null
            }
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
                onNavigateToMessage(groupId, item.messageId)
            }
        }
    )
}

@Composable
private fun SectionCard(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                CardShapes.glassCard
            )
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MessageAppTypography.sectionTitle,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = VazirFontFamily,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RoleBadge(role: MemberRole) {
    val (backgroundColor, text) = when (role) {
        MemberRole.OWNER -> Pair(MaterialTheme.colorScheme.primary, "مالک")
        MemberRole.ADMIN -> Pair(MaterialTheme.colorScheme.secondary, "ادمین")
        MemberRole.MEMBER -> Pair(MaterialTheme.colorScheme.surfaceVariant, "عضو")
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .background(backgroundColor, CardShapes.chip)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    )
}

@Composable
private fun MemberItem(
    member: GroupMember,
    isCurrentUserOwner: Boolean,
    onPromote: () -> Unit,
    onDemote: () -> Unit,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            imageUrl = member.user.displayAvatarUrl,
            name = member.user.displayName,
            size = AvatarSize.SMALL,
            isOnline = member.user.isOnline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = member.user.displayName,
                    style = MessageAppTypography.chatName,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (member.role == MemberRole.OWNER || member.role == MemberRole.ADMIN) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (member.role == MemberRole.OWNER) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = "@${member.user.username}",
                style = MessageAppTypography.chatPreview,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Role badge
        RoleBadge(role = member.role)
        
        // Menu Actions (Only if Owner, and not self)
        if (isCurrentUserOwner && member.role != MemberRole.OWNER) {
             androidx.compose.foundation.layout.Box {
                 IconButton(onClick = { showMenu = true }) {
                     Icon(
                         imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                         contentDescription = "Options",
                         tint = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                 }
                 androidx.compose.material3.DropdownMenu(
                     expanded = showMenu,
                     onDismissRequest = { showMenu = false }
                 ) {
                     if (member.role == MemberRole.MEMBER) {
                         androidx.compose.material3.DropdownMenuItem(
                             text = { Text("ارتقا به مدیر") },
                             onClick = {
                                 onPromote()
                                 showMenu = false
                             }
                         )
                     } else if (member.role == MemberRole.ADMIN) {
                         androidx.compose.material3.DropdownMenuItem(
                             text = { Text("تنزل به عضو") },
                             onClick = {
                                 onDemote()
                                 showMenu = false
                             }
                         )
                     }
                     androidx.compose.material3.DropdownMenuItem(
                         text = { Text("حذف از گروه", color = MaterialTheme.colorScheme.error) },
                         onClick = {
                             onRemove()
                             showMenu = false
                         }
                     )
                 }
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
    currentMembers: List<GroupMember>
) {
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val extendedColors = MessageAppTheme.extendedColors
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = CardShapes.dialog,
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
                            text = stringResource(R.string.search_hint),
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
                        val isAlreadyMember = currentMembers.any { it.user.id == user.id }
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
                        Text("لغو", fontFamily = VazirFontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAddMembers(selectedUserIds.toList())
                            onDismiss()
                        },
                        enabled = selectedUserIds.isNotEmpty()
                    ) {
                        Text("افزودن (${selectedUserIds.size})", fontFamily = VazirFontFamily)
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
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun PromoteAdminDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var canEditInfo by remember { mutableStateOf(false) }
    var canPostStory by remember { mutableStateOf(false) }
    var canAddMembers by remember { mutableStateOf(false) }
    var canRemoveMembers by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = CardShapes.dialog,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "تنظیم دسترسی‌های مدیر",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = VazirFontFamily
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                PermissionItem(
                    text = "ویرایش اطلاعات گروه (نام، عکس، توضیحات)",
                    checked = canEditInfo,
                    onCheckedChange = { canEditInfo = it }
                )
                PermissionItem(
                    text = "ارسال استوری برای گروه",
                    checked = canPostStory,
                    onCheckedChange = { canPostStory = it }
                )
                PermissionItem(
                    text = "افزودن عضو جدید",
                    checked = canAddMembers,
                    onCheckedChange = { canAddMembers = it }
                )
                PermissionItem(
                    text = "حذف اعضا",
                    checked = canRemoveMembers,
                    onCheckedChange = { canRemoveMembers = it }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("لغو", fontFamily = VazirFontFamily)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(canEditInfo, canPostStory, canAddMembers, canRemoveMembers)
                        }
                    ) {
                        Text("تایید و ارتقا", fontFamily = VazirFontFamily)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = VazirFontFamily
        )
    }
}
