package com.hasani.messageapp.ui.screens.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hasani.messageapp.ui.components.PrimaryButton
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// ➕ Create Channel Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    onNavigateBack: () -> Unit,
    onChannelCreated: (String) -> Unit,
    viewModel: com.hasani.messageapp.ui.viewmodel.CreateChannelViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val isFormValid = state.name.isNotBlank() && !state.isLoading && (!state.isPublic || (state.publicId.isNotBlank() && state.publicIdError == null))

    val imagePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.setChannelImage(uri)
    }

    androidx.compose.runtime.LaunchedEffect(state.createdChannelId) {
        state.createdChannelId?.let { id ->
            onChannelCreated(id)
        }
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
                        text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.create_channel),
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Channel avatar
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.channelImageUri != null) {
                            com.hasani.messageapp.ui.components.AvatarImage(
                                imageUrl = state.channelImageUri.toString(),
                                name = state.name,
                                size = com.hasani.messageapp.ui.components.AvatarSize.XLARGE,
                                avatarType = com.hasani.messageapp.ui.components.AvatarType.CHANNEL
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.select_image),
                                tint = extendedColors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Name Field
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.setName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.channel_name), fontFamily = VazirFontFamily) },
                        singleLine = true,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                
                // Description Field
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.setDescription(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.description_optional), fontFamily = VazirFontFamily) },
                        minLines = 3,
                        maxLines = 5,
                        shape = CardShapes.inputField,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = extendedColors.accent,
                            focusedLabelColor = extendedColors.accent
                        )
                    )
                }
                
                // Public Toggle
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { viewModel.setIsPublic(!state.isPublic) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isPublic) androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.public_channel) else androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.private_channel),
                                style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatName,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (state.isPublic) androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.public_channel_desc) else androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.private_channel_desc),
                                style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = state.isPublic,
                            onCheckedChange = { viewModel.setIsPublic(it) },
                            colors = androidx.compose.material3.SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = extendedColors.accent
                            )
                        )
                    }
                }
                
                // Public ID Field
                if (state.isPublic) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.publicId,
                            onValueChange = { viewModel.setPublicId(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("شناسه عمومی (لینک)", fontFamily = VazirFontFamily) },
                            placeholder = { Text("my_channel", fontFamily = VazirFontFamily) },
                            leadingIcon = { Text("@", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp)) },
                            singleLine = true,
                            isError = state.publicIdError != null,
                            supportingText = {
                                if (state.publicIdError != null) {
                                    Text(state.publicIdError!!, color = MaterialTheme.colorScheme.error, fontFamily = VazirFontFamily)
                                } else {
                                    Text("لینک اختصاصی کانال شما", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = VazirFontFamily)
                                }
                            },
                            shape = CardShapes.inputField,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = extendedColors.accent,
                                focusedLabelColor = extendedColors.accent
                            )
                        )
                    }
                }
                
                // Member Selection Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = extendedColors.accent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "افزودن اعضا",
                            style = com.hasani.messageapp.ui.theme.MessageAppTypography.sectionTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${state.selectedMembers.size} عضو",
                            style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Search for members
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("جستجوی نام کاربری...", fontFamily = VazirFontFamily) },
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

                // Selected Members Chips
                if (state.selectedMembers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.foundation.layout.FlowRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            state.selectedMembers.forEach { user ->
                                androidx.compose.material3.Card(
                                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = extendedColors.accent.copy(alpha = 0.1f)
                                    )
                                ) {
                                    androidx.compose.foundation.layout.Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = user.contactName ?: user.displayName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = VazirFontFamily,
                                            color = extendedColors.accent
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف",
                                            tint = extendedColors.accent,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.toggleMemberSelection(user) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Contacts List
                if (state.searchQuery.isBlank() && state.contacts.isNotEmpty()) {
                     item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.your_contacts_list),
                            style = com.hasani.messageapp.ui.theme.MessageAppTypography.sectionTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(state.contacts, key = { "contact_${it.id}" }) { user ->
                        val isSelected = state.selectedMembers.any { it.id == user.id }
                        UserSelectionItem(
                            user = user,
                            isSelected = isSelected,
                            extendedColors = extendedColors,
                            onToggle = { viewModel.toggleMemberSelection(user) }
                        )
                    }
                }
                
                // Search Results
                if (state.searchResults.isNotEmpty()) {
                     items(state.searchResults, key = { "search_${it.id}" }) { user ->
                        val isSelected = state.selectedMembers.any { it.id == user.id }
                        UserSelectionItem(
                            user = user,
                            isSelected = isSelected,
                            extendedColors = extendedColors,
                            onToggle = { viewModel.toggleMemberSelection(user) }
                        )
                    }
                }

                // Error Message
                 if (state.error != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }
                }

                // Create Button
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    PrimaryButton(
                        text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.create_channel),
                        onClick = {
                            viewModel.createChannel()
                        },
                        enabled = isFormValid,
                        isLoading = state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    // }
}

@Composable
private fun UserSelectionItem(
    user: com.hasani.messageapp.domain.model.User,
    isSelected: Boolean,
    extendedColors: com.hasani.messageapp.ui.theme.ExtendedColors,
    onToggle: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.hasani.messageapp.ui.components.AvatarImage(
            imageUrl = user.displayAvatarUrl,
            name = user.contactName ?: user.displayName,
            size = com.hasani.messageapp.ui.components.AvatarSize.SMALL,
            isOnline = user.isOnline
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.contactName ?: user.displayName,
                style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatName,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (user.contactName != null) {
                 Text(
                    text = user.displayName, // Show original name if contact name exists
                    style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                 Text(
                    text = "@${user.username}",
                    style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() },
            colors = androidx.compose.material3.CheckboxDefaults.colors(
                checkedColor = extendedColors.accent
            )
        )
    }
}
