package com.Kelasor.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.SearchBar
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily
import com.Kelasor.app.ui.viewmodel.NewChatViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 New Chat Screen - Contact Selection with Permission Handling
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onNavigateBack: () -> Unit,
    onStartChat: (String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToCreateChannel: () -> Unit,
    onNavigateToCreateCourse: () -> Unit,
    viewModel: NewChatViewModel = hiltViewModel()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // Permission state
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
        if (isGranted) {
            viewModel.loadContacts()
        }
    }
    // Request permission and load contacts on first launch
    LaunchedEffect(Unit) {
        if (hasContactPermission) {
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    // Navigate when chat is created
    LaunchedEffect(state.createdChatId) {
        state.createdChatId?.let { chatId ->
            onStartChat(chatId)
        }
    }
    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.new_chat),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            // Create Options Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
            ) {
                CreateOptionItem(
                    icon = Icons.Default.People,
                    label = "گروه جدید",
                    onClick = onNavigateToCreateGroup
                )
                CreateOptionItem(
                    icon = Icons.Default.Campaign,
                    label = "کانال جدید",
                    onClick = onNavigateToCreateChannel
                )
                CreateOptionItem(
                    icon = Icons.Default.School,
                    label = "دوره جدید",
                    onClick = onNavigateToCreateCourse
                )
            }
            
            // Search
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.searchUsers(it) },
                onSearch = { viewModel.searchUsers(it) },
                placeholder = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.search_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
            }
            // Content
            when {
                // Permission not granted - show request UI
                !hasContactPermission -> {
                    PermissionRequestContent(
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    )
                }
                // Show search results if searching
                state.searchQuery.length >= 2 -> {
                    if (state.users.isEmpty() && !state.isLoading) {
                        EmptyStateContent(message = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.user_not_found))
                    } else {
                        UsersList(
                            users = state.users,
                            title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.search_results),
                            onUserClick = { viewModel.createChat(it.id) },
                            sanitizedAvatarUrls = state.sanitizedAvatarUrls,
                            hideOnlineStatus = state.hideOnlineStatus
                        )
                    }
                }
                // Show contacts from device
                state.contactUsers.isNotEmpty() -> {
                    UsersList(
                        users = state.contactUsers,
                        title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.your_contacts),
                        onUserClick = { viewModel.createChat(it.id) }
                    )
                }
                // No contacts found
                state.hasLoadedContacts && state.contactUsers.isEmpty() && !state.isLoading -> {
                    EmptyStateContent(
                        message = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.no_contacts_found)
                    )
                }
            }
            // Error message
            state.error?.let { error ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = VazirFontFamily,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    // }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(extendedColors.accent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = null,
                tint = extendedColors.accent,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.access_contacts_title),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = VazirFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.access_contacts_rationale),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = VazirFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = extendedColors.accent
            )
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.allow_access),
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyStateContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = VazirFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun UsersList(
    users: List<User>,
    title: String,
    onUserClick: (User) -> Unit,
    sanitizedAvatarUrls: Map<String, String?> = emptyMap(),
    hideOnlineStatus: Map<String, Boolean> = emptyMap()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = title,
                style = MessageAppTypography.sectionTitle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(users, key = { it.id }) { user ->
            val avatarUrl = sanitizedAvatarUrls[user.id] ?: user.avatarUrl
            val showOnline = !(hideOnlineStatus[user.id] ?: false)
            ContactItem(
                user = user,
                avatarUrl = avatarUrl,
                showOnline = showOnline,
                onClick = { onUserClick(user) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ContactItem(
    user: User,
    avatarUrl: String? = user.displayAvatarUrl,
    showOnline: Boolean = true,
    onClick: () -> Unit
) {
    val resolvedName: String = user.contactName ?: user.displayName
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            imageUrl = avatarUrl,
            name = resolvedName,
            size = AvatarSize.MEDIUM,
            isOnline = if (showOnline) user.isOnline else false
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resolvedName,
                style = MessageAppTypography.chatName,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "@${user.username}",
                style = MessageAppTypography.chatPreview,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
