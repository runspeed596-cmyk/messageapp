package com.Kelasor.app.ui.screens.profile

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.domain.model.Channel
import com.Kelasor.app.ui.components.AvatarImage
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ProfileViewModel
import com.Kelasor.app.ui.viewmodel.ChannelListViewModel
import com.Kelasor.app.ui.viewmodel.ProfileEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    channelViewModel: ChannelListViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.state.collectAsState()
    val channelState by channelViewModel.state.collectAsState()
    val user = profileState.user
    val extendedColors = MessageAppTheme.extendedColors
    val context = LocalContext.current

    // Local URI for immediate preview after picking
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }

    // Avatar picker launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedAvatarUri = it
            // Copy to temp file and upload
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val tempFile = java.io.File.createTempFile("avatar_", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                profileViewModel.uploadAvatar(tempFile)
            } catch (e: Exception) {
                selectedAvatarUri = null
                android.widget.Toast.makeText(context, "خطا در بارگذاری تصویر", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load channels on mount
    LaunchedEffect(Unit) {
        channelViewModel.loadChannels()
    }

    // Observe avatar upload events
    LaunchedEffect(Unit) {
        profileViewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.AvatarUploaded -> {
                    android.widget.Toast.makeText(context, "تصویر پروفایل بروزرسانی شد", android.widget.Toast.LENGTH_SHORT).show()
                }
                is ProfileEvent.Error -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
    
    // Admin Channels for Bio Selection
    val adminChannels = remember(channelState.channels) {
        channelState.channels.filter { it.isAdmin }
    }

    // Form State
    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    
    var university by remember(user) { mutableStateOf(user?.university ?: "") }
    var fieldOfStudy by remember(user) { mutableStateOf(user?.fieldOfStudy ?: "") }
    var education by remember(user) { mutableStateOf(user?.education ?: "") }
    var skills by remember(user) { mutableStateOf(user?.skills ?: "") }
    var interests by remember(user) { mutableStateOf(user?.interests ?: "") }
    var workExperience by remember(user) { mutableStateOf(user?.workExperience ?: "") }
    var achievements by remember(user) { mutableStateOf(user?.achievements ?: "") }
    
    var bioChannelId1 by remember(user) { mutableStateOf(user?.bioChannelId1) }
    var bioChannelId2 by remember(user) { mutableStateOf(user?.bioChannelId2) }

    // Channel Selection Sheets
    var showChannelSheet1 by remember { mutableStateOf(false) }
    var showChannelSheet2 by remember { mutableStateOf(false) }

    // Handle Save Success
    LaunchedEffect(profileState.saveSuccess) {
        if (profileState.saveSuccess) {
            onNavigateBack()
            profileViewModel.resetSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ویرایش پروفایل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 16.dp).size(24.dp))
                    } else {
                        IconButton(onClick = {
                            profileViewModel.updateProfile(
                                username = username,
                                displayName = displayName,
                                bio = bio,
                                university = university,
                                fieldOfStudy = fieldOfStudy,
                                education = education,
                                skills = skills,
                                interests = interests,
                                workExperience = workExperience,
                                achievements = achievements,
                                bioChannelId1 = bioChannelId1,
                                bioChannelId2 = bioChannelId2
                            )
                        }) {
                            Icon(Icons.Default.Check, "Save", tint = extendedColors.accent)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Upload Section
            item {
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    // Use local URI first (immediate preview), fallback to server URL
                    val displayImageUri: Any? = selectedAvatarUri
                        ?: user?.avatarUrl?.let { com.Kelasor.app.util.UrlUtils.getFullUrl(it) }
                    if (displayImageUri != null && (displayImageUri is Uri || (displayImageUri is String && displayImageUri.isNotBlank()))) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(displayImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "پروفایل",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .clickable {
                                    avatarPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AvatarImage(
                            imageUrl = null,
                            name = user?.displayName ?: "?",
                            size = com.Kelasor.app.ui.components.AvatarSize.LARGE,
                            modifier = Modifier
                                .size(130.dp)
                                .clickable {
                                    avatarPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                        )
                    }
                    // Camera icon overlay
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                avatarPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        color = extendedColors.accent,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "تغییر تصویر",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "تغییر تصویر پروفایل",
                    style = MaterialTheme.typography.bodySmall,
                    color = extendedColors.accent
                )
            }

            // Basic Info Section
            item {
                SectionHeader("اطلاعات پایه", Icons.Default.Person)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("نام نمایشی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '.' }
                    },
                    label = { Text("نام کاربری (@)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = username.isNotEmpty() && username.length < 3,
                    supportingText = {
                        when {
                            username.isEmpty() -> Text("حداقل ۳ کاراکتر، فقط حروف انگلیسی و عدد")
                            username.length < 3 -> Text("نام کاربری باید حداقل ۳ کاراکتر باشد", color = MaterialTheme.colorScheme.error)
                            else -> Text("✓ فرمت صحیح", color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                        }
                    },
                    leadingIcon = {
                        Text("@", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (username.length >= 3) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (username.length >= 3) androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("بیوگرافی") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // Professional Info Section
            item {
                SectionHeader("تحصیلات و مهارت‌ها", Icons.Default.School)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it },
                        label = { Text("دانشگاه") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = education,
                        onValueChange = { education = it },
                        label = { Text("مقطع") }, // e.g. Bachelor
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = fieldOfStudy,
                    onValueChange = { fieldOfStudy = it },
                    label = { Text("رشته تحصیلی") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("مهارت‌ها (با کاما جدا کنید)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("علاقه‌مندی‌ها") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Experience Section
            item {
                SectionHeader("سوابق کاری و افتخارات", Icons.Default.Work)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = workExperience,
                    onValueChange = { workExperience = it },
                    label = { Text("تجربه کاری") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = achievements,
                    onValueChange = { achievements = it },
                    label = { Text("افتخارات") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Bio Channels Section
            item {
                SectionHeader("کانال‌های بایو", Icons.Default.Campaign)
                Text(
                    "می‌توانید تا ۲ کانال که مدیریت می‌کنید را در پروفایل خود نمایش دهید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                BioChannelSelector(
                    label = "کانال اول",
                    selectedChannelId = bioChannelId1,
                    channels = adminChannels,
                    onSelectValues = { showChannelSheet1 = true },
                    onRemove = { bioChannelId1 = null }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                BioChannelSelector(
                    label = "کانال دوم",
                    selectedChannelId = bioChannelId2,
                    channels = adminChannels,
                    onSelectValues = { showChannelSheet2 = true },
                    onRemove = { bioChannelId2 = null }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Channel Selection Sheets
    if (showChannelSheet1) {
        ChannelSelectionSheet(
            channels = adminChannels,
            onDismiss = { showChannelSheet1 = false },
            onChannelSelected = { 
                bioChannelId1 = it.id 
                showChannelSheet1 = false
            }
        )
    }
    
    if (showChannelSheet2) {
        ChannelSelectionSheet(
            channels = adminChannels,
            onDismiss = { showChannelSheet2 = false },
            onChannelSelected = { 
                bioChannelId2 = it.id
                showChannelSheet2 = false
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
fun BioChannelSelector(
    label: String,
    selectedChannelId: String?,
    channels: List<Channel>,
    onSelectValues: () -> Unit,
    onRemove: () -> Unit
) {
    val selectedChannel = channels.find { it.id == selectedChannelId }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp),
        onClick = onSelectValues
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                if (selectedChannel != null) {
                    Text(selectedChannel.name, style = MaterialTheme.typography.bodyMedium)
                } else if (selectedChannelId != null) {
                     Text("بارگیری کانال...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("انتخاب کنید", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (selectedChannelId != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, "Remove")
                }
            } else {
                Icon(Icons.Default.KeyboardArrowDown, null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelSelectionSheet(
    channels: List<Channel>,
    onDismiss: () -> Unit,
    onChannelSelected: (Channel) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("انتخاب کانال", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            if (channels.isEmpty()) {
                 Text("شما ادمین هیچ کانالی نیستید.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn {
                    items(channels.size) { index ->
                        val channel = channels[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChannelSelected(channel) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AvatarImage(channel.avatarUrl, channel.name, size = com.Kelasor.app.ui.components.AvatarSize.SMALL)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(channel.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
