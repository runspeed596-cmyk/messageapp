package com.Kelasor.app.ui.screens.group

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.R
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.PrimaryButton
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.CreateGroupViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// ➕ Create Group Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onGroupCreated: (String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val isFormValid = state.name.isNotBlank()
    
    // Image picker launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setGroupImage(uri)
    }
    
    // Navigate when group is created
    LaunchedEffect(state.createdGroupId) {
        state.createdGroupId?.let { groupId ->
            onGroupCreated(groupId)
        }
    }
    
    // Load contacts when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadContacts()
    }
    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.create_group),
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                // Group avatar section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            // Profile Image (Clipped)
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.groupImageUri != null) {
                                    // Show selected image
                                    AsyncImage(
                                        model = state.groupImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Show default group icon
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            // Camera button overlay (Not clipped, on top)
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
                                    contentDescription = androidx.compose.ui.res.stringResource(R.string.select_image),
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                // Group name input
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.setName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.group_name), fontFamily = VazirFontFamily) },
                        singleLine = true,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                // Description input
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.setDescription(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.description_optional), fontFamily = VazirFontFamily) },
                        minLines = 3,
                        maxLines = 5,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                // Public toggle removed - groups are always private
                // Selected members
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = extendedColors.accent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.group_members),
                            style = MessageAppTypography.sectionTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.members_count, state.selectedMembers.size),
                            style = MessageAppTypography.chatTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Selected members chips
                if (state.selectedMembers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.selectedMembers.forEach { user ->
                                Card(
                                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = extendedColors.accent.copy(alpha = 0.1f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = user.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = VazirFontFamily,
                                            color = extendedColors.accent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.delete),
                                            tint = extendedColors.accent,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeMember(user.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Search for members
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.searchUsers(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(androidx.compose.ui.res.stringResource(R.string.search_by_id_hint), fontFamily = VazirFontFamily) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                
                // Contacts section title (only show when not searching)
                if (state.searchQuery.isBlank() && state.contacts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.your_contacts_list),
                            style = MessageAppTypography.sectionTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Show contacts
                    items(state.contacts, key = { "contact_${it.id}" }) { user ->
                        val isSelected = state.selectedMembers.any { it.id == user.id }
                        UserSelectionItem(
                            user = user,
                            isSelected = isSelected,
                            extendedColors = extendedColors,
                            onToggle = {
                                if (isSelected) viewModel.removeMember(user.id)
                                else viewModel.addMember(user)
                            }
                        )
                    }
                }
                
                // Search results
                if (state.searchResults.isNotEmpty()) {
                    items(state.searchResults, key = { "search_${it.id}" }) { user ->
                        val isSelected = state.selectedMembers.any { it.id == user.id }
                        UserSelectionItem(
                            user = user,
                            isSelected = isSelected,
                            extendedColors = extendedColors,
                            onToggle = {
                                if (isSelected) viewModel.removeMember(user.id)
                                else viewModel.addMember(user)
                            }
                        )
                    }
                }
                // Error message
                item {
                    AnimatedVisibility(visible = state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = VazirFontFamily,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                // Create button
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(
                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.create_group),
                        onClick = { viewModel.createGroup() },
                        enabled = isFormValid && !state.isLoading,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    // }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Selection Item
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun UserSelectionItem(
    user: com.Kelasor.app.domain.model.User,
    isSelected: Boolean,
    extendedColors: com.Kelasor.app.ui.theme.ExtendedColors,
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
                checkedColor = extendedColors.accent
            )
        )
    }
}
