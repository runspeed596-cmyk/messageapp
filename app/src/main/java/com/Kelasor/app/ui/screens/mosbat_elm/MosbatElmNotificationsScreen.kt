package com.Kelasor.app.ui.screens.mosbat_elm

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.CourseCollaborationRequestDto
import com.Kelasor.app.data.repository.UserRepository
import com.Kelasor.app.data.repository.UserResult
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.DanaFontFamily
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Mosbat Elm Notifications Screen — Separate from Messenger Notifications
// ═══════════════════════════════════════════════════════════════════════════════

data class MosbatElmNotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val courseTitle: String? = null,
    val senderName: String? = null,
    val requestId: String? = null,
    val status: String = "PENDING",
    val createdAt: String = "",
    val isRead: Boolean = false
)

data class MosbatElmNotificationsState(
    val notifications: List<MosbatElmNotificationItem> = emptyList(),
    val pendingCollaborations: List<CourseCollaborationRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val academyId: String? = null
)

@HiltViewModel
class MosbatElmNotificationsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MosbatElmNotificationsState())
    val state: StateFlow<MosbatElmNotificationsState> = _state.asStateFlow()
    init {
        loadData()
    }
    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                userRepository.getCurrentUser(forceRefresh = false).collect { result ->
                    if (result is UserResult.Success) {
                        val institutionId: String? = result.data.institutionId
                        _state.update { it.copy(academyId = institutionId) }
                        if (institutionId != null) {
                            loadCollaborationRequests(institutionId)
                        } else {
                            _state.update { it.copy(isLoading = false) }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    private suspend fun loadCollaborationRequests(academyId: String) {
        try {
            val response = apiService.getPendingCollaborations(academyId)
            if (response.isSuccessful && response.body()?.success == true) {
                val requests: List<CourseCollaborationRequestDto> = response.body()?.data?.content ?: emptyList()
                // Convert to notification items
                val notifItems: List<MosbatElmNotificationItem> = requests.map { req ->
                    MosbatElmNotificationItem(
                        id = req.id,
                        type = "COURSE_COLLABORATION_REQUEST",
                        title = "درخواست همکاری جدید",
                        body = "از ${req.senderInstitutionName} برای دوره «${req.courseTitle}»",
                        courseTitle = req.courseTitle,
                        senderName = req.senderInstitutionName,
                        requestId = req.id,
                        status = req.status,
                        createdAt = req.createdAt
                    )
                }
                _state.update { it.copy(
                    pendingCollaborations = requests,
                    notifications = notifItems,
                    isLoading = false
                ) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        } catch (e: Exception) {
            Log.e("MosbatElmNotifVM", "Error loading", e)
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }
    fun acceptCollaboration(requestId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.acceptCollaboration(requestId)
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notif ->
                                if (notif.requestId == requestId) notif.copy(status = "ACCEPTED") else notif
                            },
                            pendingCollaborations = state.pendingCollaborations.map {
                                if (it.id == requestId) it.copy(status = "ACCEPTED") else it
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MosbatElmNotifVM", "Error accepting", e)
            }
        }
    }
    fun rejectCollaboration(requestId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.rejectCollaboration(requestId)
                if (response.isSuccessful) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notif ->
                                if (notif.requestId == requestId) notif.copy(status = "REJECTED") else notif
                            },
                            pendingCollaborations = state.pendingCollaborations.map {
                                if (it.id == requestId) it.copy(status = "REJECTED") else it
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MosbatElmNotifVM", "Error rejecting", e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosbatElmNotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: MosbatElmNotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "اعلان‌های مثبت علم",
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
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.loadData() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.notifications.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "اعلان جدیدی ندارید",
                            fontFamily = DanaFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.notifications, key = { it.id }) { notif: MosbatElmNotificationItem ->
                        MosbatElmNotificationCard(
                            notification = notif,
                            onAccept = { notif.requestId?.let { viewModel.acceptCollaboration(it) } },
                            onReject = { notif.requestId?.let { viewModel.rejectCollaboration(it) } },
                            accentColor = extendedColors.accent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MosbatElmNotificationCard(
    notification: MosbatElmNotificationItem,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    accentColor: Color
) {
    val typeIcon = when (notification.type) {
        "COURSE_COLLABORATION_REQUEST" -> Icons.Default.Handshake
        "COURSE_COLLABORATION_ACCEPTED" -> Icons.Default.CheckCircle
        "COURSE_COLLABORATION_REJECTED" -> Icons.Default.Cancel
        else -> Icons.Default.Notifications
    }
    val typeColor = when (notification.type) {
        "COURSE_COLLABORATION_REQUEST" -> Color(0xFFFFA000)
        "COURSE_COLLABORATION_ACCEPTED" -> Color(0xFF4CAF50)
        "COURSE_COLLABORATION_REJECTED" -> Color(0xFFF44336)
        else -> accentColor
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.status == "PENDING")
                typeColor.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = notification.body,
                        fontFamily = DanaFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.createdAt,
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            // Accept/Reject buttons for pending collaboration requests
            if (notification.type == "COURSE_COLLABORATION_REQUEST" && notification.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("رد کردن", fontFamily = DanaFontFamily, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("قبول کردن", fontFamily = DanaFontFamily, fontSize = 13.sp)
                    }
                }
            }
            // Status badge for non-pending
            if (notification.status != "PENDING") {
                Spacer(modifier = Modifier.height(8.dp))
                val statusColor: Color = when (notification.status) {
                    "ACCEPTED" -> Color(0xFF4CAF50)
                    "REJECTED" -> Color(0xFFF44336)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (notification.status) {
                            "ACCEPTED" -> "✓ تایید شده"
                            "REJECTED" -> "✕ رد شده"
                            else -> notification.status
                        },
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
