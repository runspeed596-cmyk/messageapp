package com.Kelasor.app.ui.screens.settings

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.ApiResponse
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.util.toPersianDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.*
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 📱 Devices Screen — Session Management
// ═══════════════════════════════════════════════════════════════════════════════

import com.Kelasor.app.data.remote.dto.DeviceSessionDto

data class DevicesState(
    val sessions: List<DeviceSessionDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _state = MutableStateFlow(DevicesState())
    val state: StateFlow<DevicesState> = _state.asStateFlow()
    init {
        loadSessions()
    }
    fun loadSessions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getActiveSessions()
                if (response.isSuccessful && response.body()?.success == true) {
                    val rawSessions = response.body()?.data ?: emptyList()
                    val sortedSessions = rawSessions.sortedByDescending { it.isCurrent }
                    _state.update { it.copy(sessions = sortedSessions, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در دریافت لیست دستگاه‌ها") }
                }
            } catch (e: Exception) {
                Log.e("DevicesVM", "Error loading sessions", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun terminateSession(sessionId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.terminateSession(sessionId)
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(sessions = state.sessions.filter { it.id != sessionId })
                    }
                }
            } catch (e: Exception) {
                Log.e("DevicesVM", "Error terminating session", e)
            }
        }
    }
    fun terminateAllOtherSessions() {
        viewModelScope.launch {
            try {
                val response = apiService.terminateOtherSessions()
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(sessions = state.sessions.filter { it.isCurrent })
                    }
                }
            } catch (e: Exception) {
                Log.e("DevicesVM", "Error terminating all sessions", e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    var showLogoutAllDialog by remember { mutableStateOf(false) }
    var sessionToLogout by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "دستگاه‌ها",
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Current Device ──────────────────────────────────────
            val currentDevice: DeviceSessionDto? = state.sessions.find { it.isCurrent }
            if (currentDevice != null) {
                item {
                    SettingsCard(title = "دستگاه فعلی", icon = Icons.Default.PhoneAndroid) {
                        DeviceRow(
                            session = currentDevice,
                            isCurrent = true,
                            onTerminate = {},
                            accentColor = extendedColors.accent
                        )
                    }
                }
            }
            // ── Other Devices ──────────────────────────────────────
            val otherDevices: List<DeviceSessionDto> = state.sessions.filter { !it.isCurrent }
            if (otherDevices.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "سایر دستگاه‌ها",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showLogoutAllDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "خروج از همه",
                                fontFamily = DanaFontFamily,
                                color = Color(0xFFF44336),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
                items(otherDevices, key = { it.id }) { session: DeviceSessionDto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        DeviceRow(
                            session = session,
                            isCurrent = false,
                            onTerminate = { sessionToLogout = session.id },
                            accentColor = extendedColors.accent
                        )
                    }
                }
            }
            // ── Loading State ──────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = extendedColors.accent)
                    }
                }
            }
            // ── Error State ──────────────────────────────────────
            if (state.error != null && !state.isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error ?: "خطا در بارگذاری",
                                fontFamily = DanaFontFamily,
                                color = Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.loadSessions() }) {
                                Text("تلاش مجدد", fontFamily = DanaFontFamily)
                            }
                        }
                    }
                }
            }
            // ── Empty State ──────────────────────────────────────
            if (state.sessions.isEmpty() && !state.isLoading && state.error == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Devices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "هیچ دستگاه فعالی یافت نشد",
                                fontFamily = DanaFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    // ── Logout All Dialog ──────────────────────────────────
    if (showLogoutAllDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutAllDialog = false },
            title = {
                Text("خروج از همه دستگاه‌ها", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "آیا می‌خواهید از تمام دستگاه‌ها به‌جز دستگاه فعلی خارج شوید؟",
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.terminateAllOtherSessions()
                    showLogoutAllDialog = false
                }) {
                    Text("خروج", fontFamily = DanaFontFamily, color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutAllDialog = false }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }

    // ── Logout Individual Dialog ───────────────────────────────
    if (sessionToLogout != null) {
        AlertDialog(
            onDismissRequest = { sessionToLogout = null },
            title = {
                Text("خروج از دستگاه", fontFamily = DanaFontFamily, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "آیا مطمئن هستید که می‌خواهید اتصال این دستگاه را قطع کنید؟",
                    fontFamily = DanaFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    sessionToLogout?.let { viewModel.terminateSession(it) }
                    sessionToLogout = null
                }) {
                    Text("خروج", fontFamily = DanaFontFamily, color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToLogout = null }) {
                    Text("انصراف", fontFamily = DanaFontFamily)
                }
            }
        )
    }
}

@Composable
private fun DeviceRow(
    session: DeviceSessionDto,
    isCurrent: Boolean,
    onTerminate: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrent) accentColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when {
                    session.platform?.contains("Android", ignoreCase = true) == true -> Icons.Default.PhoneAndroid
                    session.platform?.contains("iOS", ignoreCase = true) == true -> Icons.Default.PhoneIphone
                    session.platform?.contains("Web", ignoreCase = true) == true -> Icons.Default.Language
                    session.platform?.contains("Desktop", ignoreCase = true) == true -> Icons.Default.DesktopWindows
                    else -> Icons.Default.Devices
                },
                contentDescription = null,
                tint = if (isCurrent) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.deviceName ?: "دستگاه ناشناخته",
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
                if (isCurrent) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "فعلی",
                            fontFamily = DanaFontFamily,
                            fontSize = 10.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = "${session.platform ?: "نامشخص"} • ${session.lastActiveAt?.toPersianDateTime() ?: "نامشخص"}",
                fontFamily = DanaFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // IP address not in DTO yet, can add if needed
        }
        if (!isCurrent) {
            IconButton(onClick = onTerminate) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "خروج",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
