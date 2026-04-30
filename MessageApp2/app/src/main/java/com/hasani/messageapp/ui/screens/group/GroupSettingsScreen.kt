package com.hasani.messageapp.ui.screens.group

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.foundation.layout.fillMaxHeight
import com.hasani.messageapp.ui.theme.VazirFontFamily
import com.hasani.messageapp.domain.model.GroupMember
import com.hasani.messageapp.domain.model.MemberRole
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.components.AvatarType
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.MessageAppTypography
import com.hasani.messageapp.ui.viewmodel.GroupEvent
import com.hasani.messageapp.ui.viewmodel.GroupSettingsViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ⚙️ Group Settings Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GroupSettingsScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onGroupDeleted: () -> Unit = {},
    viewModel: GroupSettingsViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        // TODO: Upload image to server
    }
    
    // Initialize fields when group loads
    LaunchedEffect(state.group) {
        state.group?.let { group ->
            name = group.name
            description = group.description ?: ""
            isPublic = group.isPublic
        }
    }
    
    // Load group
    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }
    
    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GroupEvent.GroupUpdated -> {
                    android.widget.Toast.makeText(context, "تغییرات ذخیره شد", android.widget.Toast.LENGTH_SHORT).show()
                }
                is GroupEvent.GroupDeleted, is GroupEvent.GroupLeft -> onGroupDeleted()
                is GroupEvent.Error -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

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
                text = "تنظیمات گروه",
                style = MessageAppTypography.chatName,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = extendedColors.accent
                )
            } else if (state.group?.myRole == MemberRole.OWNER || state.group?.myRole == MemberRole.ADMIN || state.group?.allowMembersToEditInfo == true) {
                IconButton(onClick = {
                    viewModel.updateGroup(name, description, isPublic, selectedImageUri)
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        tint = extendedColors.accent
                    )
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
                            imageUrl = state.group?.avatarUrl,
                            name = state.group?.name ?: "",
                            size = AvatarSize.XLARGE,
                            avatarType = AvatarType.GROUP
                        )
                        
                        if (state.group?.myRole == MemberRole.OWNER || state.group?.myRole == MemberRole.ADMIN || state.group?.allowMembersToEditInfo == true) {
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
            
            // Group Info Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "اطلاعات گروه",
                            style = MessageAppTypography.chatName,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val canEdit = state.group?.myRole == MemberRole.OWNER || state.group?.myRole == MemberRole.ADMIN || state.group?.allowMembersToEditInfo == true
                        
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (canEdit) name = it },
                            label = { Text("نام گروه") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = canEdit,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.accent,
                                cursorColor = extendedColors.accent,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha=0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (canEdit) description = it },
                            label = { Text("توضیحات") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            enabled = canEdit,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.accent,
                                cursorColor = extendedColors.accent,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha=0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "گروه عمومی",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "همه می‌توانند این گروه را پیدا کنند",
                                    style = MessageAppTypography.chatTime,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPublic,
                                onCheckedChange = { if (canEdit) isPublic = it },
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
            
            // Permissions Section
             if (state.group?.myRole == MemberRole.OWNER || state.group?.myRole == MemberRole.ADMIN) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "دسترسی‌ها",
                            style = MessageAppTypography.chatName,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Send Messages
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ارسال پیام توسط اعضا",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = state.group?.allowMembersToSendMessages ?: true,
                                onCheckedChange = { 
                                    viewModel.updateSettings(allowMembersToSendMessages = it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = extendedColors.accent
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Edit Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ویرایش اطلاعات گروه",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Switch(
                                checked = state.group?.allowMembersToEditInfo ?: false,
                                onCheckedChange = { 
                                    viewModel.updateSettings(allowMembersToEditInfo = it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = extendedColors.accent
                                )
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
            
            // Members Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اعضا (${state.members.size})",
                        style = MessageAppTypography.chatName,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (state.group?.myRole == MemberRole.OWNER || state.group?.myRole == MemberRole.ADMIN) {
                        TextButton(
                            onClick = { showAddMemberDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "افزودن",
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            items(state.members, key = { it.user.id }) { member ->
                MemberItem(
                    member = member,
                    myRole = state.group?.myRole,
                    onRemove = {
                        viewModel.removeMember(member.user.id)
                    },
                    onRoleChange = { newRole ->
                        viewModel.changeMemberRole(member.user.id, newRole)
                    },
                    onClick = { onNavigateToUserProfile(member.user.id) }
                )
            }
            
            // Danger Zone (Delete/Leave)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                val isOwner = state.group?.myRole == MemberRole.OWNER
                val actionText = if (isOwner) "حذف گروه" else "ترک گروه"
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
                        Text(
                            text = actionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    
    // Delete/Leave Confirmation Dialog
    if (showDeleteDialog) {
        val isOwner = state.group?.myRole == MemberRole.OWNER
        val title = if (isOwner) "حذف گروه" else "ترک گروه"
        val message = if (isOwner) 
            "آیا مطمئن هستید که می‌خواهید این گروه را حذف کنید؟ این عمل قابل برگشت نیست." 
        else 
            "آیا مطمئن هستید که می‌خواهید گروه را ترک کنید؟"
        val confirmText = if (isOwner) "حذف" else "ترک گروه"

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        if (isOwner) viewModel.deleteGroup() else viewModel.leaveGroup()
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
            onAddMembers = { memberIds ->
                viewModel.addMembers(memberIds)
            },
            viewModel = viewModel
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MemberItem(
    member: GroupMember,
    myRole: MemberRole?,
    onRemove: () -> Unit,
    onRoleChange: (MemberRole) -> Unit,
    onClick: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    var showMenu by remember { mutableStateOf(false) }
    
    val canManage = when (myRole) {
        MemberRole.OWNER -> member.role != MemberRole.OWNER
        MemberRole.ADMIN -> member.role == MemberRole.MEMBER
        else -> false
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                imageUrl = member.user.displayAvatarUrl,
                name = member.user.displayName,
                size = AvatarSize.MEDIUM
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.user.displayName,
                    style = MessageAppTypography.chatName,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = when (member.role) {
                        MemberRole.OWNER -> "مالک"
                        MemberRole.ADMIN -> "مدیر"
                        MemberRole.MEMBER -> "عضو"
                    },
                    style = MessageAppTypography.chatTime,
                    color = when (member.role) {
                        MemberRole.OWNER -> extendedColors.accent
                        MemberRole.ADMIN -> extendedColors.gradientEnd
                        MemberRole.MEMBER -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (canManage) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (myRole == MemberRole.OWNER) {
                            if (member.role == MemberRole.MEMBER) {
                                DropdownMenuItem(
                                    text = { Text("ارتقا به مدیر") },
                                    onClick = {
                                        onRoleChange(MemberRole.ADMIN)
                                        showMenu = false
                                    }
                                )
                            } else if (member.role == MemberRole.ADMIN) {
                                DropdownMenuItem(
                                    text = { Text("تنزل به عضو") },
                                    onClick = {
                                        onRoleChange(MemberRole.MEMBER)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                        
                        DropdownMenuItem(
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
}

// ═══════════════════════════════════════════════════════════════════════════════
// ➕ Add Member Dialog
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMembers: (List<String>) -> Unit,
    viewModel: com.hasani.messageapp.ui.viewmodel.GroupSettingsViewModel
) {
    val state by viewModel.state.collectAsState()
    var selectedUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val extendedColors = MessageAppTheme.extendedColors
    
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }

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
                    value = state.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.search_hint),
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
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val usersToShow = if (state.searchQuery.isNotBlank()) {
                        state.searchResults
                    } else {
                        state.contacts
                    }
                    
                    items(items = usersToShow, key = { it.id }) { user ->
                        // Filter out already existing members
                        // We need to check against state.members
                        // But members in state are GroupMember, user is User.
                        val isAlreadyMember = state.members.any { it.user.id == user.id }
                        if (!isAlreadyMember) {
                            UserSelectionItem(
                                user = user,
                                isSelected = selectedUserIds.contains(user.id),
                                extendedColors = extendedColors,
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
                        enabled = selectedUserIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
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
    user: com.hasani.messageapp.domain.model.User,
    isSelected: Boolean,
    extendedColors: com.hasani.messageapp.ui.theme.ExtendedColors,
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
            name = user.displayName,
            size = AvatarSize.SMALL,
            isOnline = user.isOnline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName,
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
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = Color.White
            )
        )
    }
}
