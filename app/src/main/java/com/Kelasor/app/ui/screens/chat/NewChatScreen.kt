package com.Kelasor.app.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.User
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.components.AvatarSize
import com.Kelasor.app.ui.components.SearchBar
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.viewmodel.NewChatViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Premium New Chat Screen
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
    var hasContactPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
        if (isGranted) {
            viewModel.loadContacts()
        }
    }
    LaunchedEffect(Unit) {
        if (hasContactPermission) {
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    LaunchedEffect(state.createdChatId) {
        state.createdChatId?.let { chatId ->
            onStartChat(chatId)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Top App Bar ──────────────────────────────────────────────
        TopAppBar(
            title = {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.new_chat),
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
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
        // ── Create Options Row — Premium gradient cards ──────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PremiumCreateCard(
                icon = Icons.Default.People,
                label = "گروه جدید",
                gradientColors = listOf(Color(0xFF42A5F5), Color(0xFF1565C0)),
                onClick = onNavigateToCreateGroup,
                modifier = Modifier.weight(1f)
            )
            PremiumCreateCard(
                icon = Icons.Default.Campaign,
                label = "کانال جدید",
                gradientColors = listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2)),
                onClick = onNavigateToCreateChannel,
                modifier = Modifier.weight(1f)
            )
            PremiumCreateCard(
                icon = Icons.Default.School,
                label = "دوره جدید",
                gradientColors = listOf(Color(0xFF26C6DA), Color(0xFF00838F)),
                onClick = onNavigateToCreateCourse,
                modifier = Modifier.weight(1f)
            )
        }
        // ── Search ───────────────────────────────────────────────────
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { viewModel.searchUsers(it) },
            onSearch = { viewModel.searchUsers(it) },
            placeholder = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.search_hint),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        // ── Loading ──────────────────────────────────────────────────
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = extendedColors.accent,
                    strokeWidth = 2.5.dp
                )
            }
        }
        // ── Content ──────────────────────────────────────────────────
        when {
            !hasContactPermission -> {
                PremiumPermissionRequest(
                    accentColor = extendedColors.accent,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                )
            }
            state.searchQuery.length >= 2 -> {
                if (state.users.isEmpty() && !state.isLoading) {
                    PremiumEmptyState(
                        message = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.user_not_found),
                        accentColor = extendedColors.accent
                    )
                } else {
                    PremiumUsersList(
                        users = state.users,
                        title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.search_results),
                        onUserClick = { viewModel.createChat(it.id) },
                        sanitizedAvatarUrls = state.sanitizedAvatarUrls,
                        hideOnlineStatus = state.hideOnlineStatus
                    )
                }
            }
            state.contactUsers.isNotEmpty() -> {
                PremiumUsersList(
                    users = state.contactUsers,
                    title = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.your_contacts),
                    onUserClick = { viewModel.createChat(it.id) }
                )
            }
            state.hasLoadedContacts && state.contactUsers.isEmpty() && !state.isLoading -> {
                PremiumEmptyState(
                    message = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.no_contacts_found),
                    accentColor = extendedColors.accent
                )
            }
        }
        // ── Error ────────────────────────────────────────────────────
        state.error?.let { error ->
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎴 Premium Create Card — Gradient icon + label
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumCreateCard(
    icon: ImageVector,
    label: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cardScale"
    )
    Card(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔒 Permission Request — Premium
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumPermissionRequest(
    accentColor: Color,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(12.dp, CircleShape, ambientColor = accentColor.copy(alpha = 0.2f))
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(accentColor, accentColor.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.access_contacts_title),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.access_contacts_rationale),
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = DanaFontFamily,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.allow_access),
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📭 Empty State — Premium
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumEmptyState(
    message: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Users List — Staggered entrance animation
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumUsersList(
    users: List<User>,
    title: String,
    onUserClick: (User) -> Unit,
    sanitizedAvatarUrls: Map<String, String?> = emptyMap(),
    hideOnlineStatus: Map<String, Boolean> = emptyMap()
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MessageAppTheme.extendedColors.accent,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
        itemsIndexed(users, key = { _, user -> user.id }) { index, user ->
            val avatarUrl = sanitizedAvatarUrls[user.id] ?: user.avatarUrl
            val showOnline = !(hideOnlineStatus[user.id] ?: false)
            // Staggered reveal animation
            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 40L)
                isVisible = true
            }
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(300)) +
                        slideInVertically(
                            initialOffsetY = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
            ) {
                PremiumContactItem(
                    user = user,
                    avatarUrl = avatarUrl,
                    showOnline = showOnline,
                    onClick = { onUserClick(user) }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 Premium Contact Item
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PremiumContactItem(
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(
            imageUrl = avatarUrl,
            name = resolvedName,
            size = AvatarSize.MEDIUM,
            isOnline = if (showOnline) user.isOnline else false
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = resolvedName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DanaFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
