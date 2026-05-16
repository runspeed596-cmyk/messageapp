package com.Kelasor.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Privacy Exceptions Screen
// ═══════════════════════════════════════════════════════════════════════════════

import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.viewmodel.ContactsSelectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyExceptionsScreen(
    type: String,
    onNavigateBack: () -> Unit,
    viewModel: ContactsSelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    
    val title = when(type) {
        "profile" -> "استثناهای پروفایل"
        "last_seen" -> "استثناهای بازدید"
        "bio" -> "استثناهای بیوگرافی"
        "phone" -> "استثناهای شماره تلفن"
        "online" -> "استثناهای آنلاین"
        "blocked_users" -> "افزودن کاربر مسدود"
        else -> "انتخاب مخاطبین"
    }

    LaunchedEffect(type) {
        viewModel.loadSelection(type)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                actions = {
                    if (state.selectedContacts.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.saveSelection(type)
                            onNavigateBack()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "تایید", tint = extendedColors.accent)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = "مخاطبینی که می‌خواهید استثنا قائل شوید را انتخاب کنید:",
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                }
            } else if (state.contacts.isEmpty()) {
                item {
                    Text(
                        text = "شما هنوز مخاطبی ندارید.",
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp)
                    )
                }
            } else {
                items(state.contacts) { contact ->
                    val isSelected = state.selectedContacts.contains(contact.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleContactSelection(contact.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(8.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = contact.displayName,
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = extendedColors.accent)
                        )
                    }
                }
            }
        }
    }
}
