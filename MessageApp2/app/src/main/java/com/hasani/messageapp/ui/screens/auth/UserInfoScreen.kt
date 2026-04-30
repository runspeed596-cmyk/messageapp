package com.hasani.messageapp.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// import coil.compose.AsyncImage // Assuming coil is used, checking imports from other files if needed.
// Using AvatarImage component if available or standard Image.
// Checking ConversationScreen imports: import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarImage
import com.hasani.messageapp.ui.components.AvatarSize
import com.hasani.messageapp.ui.components.PrimaryButton
import com.hasani.messageapp.ui.theme.MessageAppTheme
import com.hasani.messageapp.ui.theme.VazirFontFamily
import com.hasani.messageapp.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.hasani.messageapp.ui.viewmodel.AuthEvent.LoginSuccess -> {
                    if (!event.isNewUser) {
                        onNavigateToMain()
                    }
                }
                is com.hasani.messageapp.ui.viewmodel.AuthEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }
    
    val extendedColors = MessageAppTheme.extendedColors
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // Removing hardcoded RTL provider
    // CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                extendedColors.gradientStart.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .imePadding()
                    .verticalScroll(rememberScrollState()) // Allow scrolling
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.complete_profile),
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Profile Image Picker
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        AvatarImage(
                            imageUrl = profileImageUri.toString(),
                            name = firstName,
                            size = AvatarSize.LARGE
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.select_photo),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.first_name), fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.last_name), fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.username_english), fontFamily = VazirFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.bio_optional), fontFamily = VazirFontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                PrimaryButton(
                    text = androidx.compose.ui.res.stringResource(com.hasani.messageapp.R.string.register_and_login),
                    onClick = {
                        val avatarFile = profileImageUri?.let { uri ->
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val file = java.io.File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
                                inputStream?.use { input ->
                                    file.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                file
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                        viewModel.updateProfile(
                            firstName, lastName, username, bio, avatarFile,
                            "", "", "", "", "", "", ""
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = firstName.isNotBlank() && lastName.isNotBlank() && username.isNotBlank(),
                    isLoading = state.isLoading
                )
                Spacer(modifier = Modifier.height(100.dp)) // Extra padding for scrolling
            }
        }
    // }
}
