package com.Kelasor.app.ui.screens.mosbat_elm

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import com.Kelasor.app.util.toPersianDateTime
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
    val isRead: Boolean = false,
    val relatedEntityId: String? = null
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
                        
                        // Load user invitations (Mosbat Elm notifications)
                        val userNotifsResponse = apiService.getMosbatElmNotifications(0, 50)
                        val userNotifs = if (userNotifsResponse.isSuccessful) {
                            userNotifsResponse.body()?.notifications ?: emptyList()
                        } else {
                            emptyList()
                        }
                        
                        val inviteItems = userNotifs.map { req ->
                            MosbatElmNotificationItem(
                                id = req.id,
                                type = req.type,
                                title = req.title,
                                body = req.body,
                                status = req.status,
                                createdAt = req.createdAt,
                                relatedEntityId = req.relatedId
                            )
                        }

                        // Load collaborations if user is an academy owner
                        val collabItems = if (institutionId != null) {
                            val response = apiService.getPendingCollaborations(institutionId)
                            if (response.isSuccessful && response.body()?.success == true) {
                                val requests = response.body()?.data?.content ?: emptyList()
                                requests.map { req ->
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
                            } else {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }

                        // Merge and update state
                        _state.update { it.copy(
                            notifications = (inviteItems + collabItems).sortedByDescending { item -> item.createdAt },
                            isLoading = false
                        ) }
                    }
                }
            } catch (e: Exception) {
                Log.e("MosbatElmNotifVM", "Error loading", e)
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
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

    fun acceptInvite(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.acceptInvite(notificationId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notif ->
                                if (notif.id == notificationId) notif.copy(status = "ACCEPTED") else notif
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MosbatElmNotifVM", "Error accepting invite", e)
            }
        }
    }

    fun rejectInvite(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.rejectInvite(notificationId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notif ->
                                if (notif.id == notificationId) notif.copy(status = "REJECTED") else notif
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MosbatElmNotifVM", "Error rejecting invite", e)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosbatElmNotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAcademyProfile: (String) -> Unit = {},
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
                            onAccept = {
                                if (notif.type == "TEACHER_INVITE" || notif.type == "ADMIN_INVITE") {
                                    viewModel.acceptInvite(notif.id)
                                } else {
                                    notif.requestId?.let { viewModel.acceptCollaboration(it) }
                                }
                            },
                            onReject = {
                                if (notif.type == "TEACHER_INVITE" || notif.type == "ADMIN_INVITE") {
                                    viewModel.rejectInvite(notif.id)
                                } else {
                                    notif.requestId?.let { viewModel.rejectCollaboration(it) }
                                }
                            },
                            onAcademyClick = onNavigateToAcademyProfile,
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
    onAcademyClick: (String) -> Unit,
    accentColor: Color
) {
    val typeIcon = when (notification.type) {
        "COURSE_COLLABORATION_REQUEST" -> Icons.Default.Handshake
        "COURSE_COLLABORATION_ACCEPTED" -> Icons.Default.CheckCircle
        "COURSE_COLLABORATION_REJECTED" -> Icons.Default.Cancel
        "TEACHER_INVITE" -> Icons.Default.School
        "ADMIN_INVITE" -> Icons.Default.SupervisorAccount
        else -> Icons.Default.Notifications
    }
    val typeColor = when (notification.type) {
        "COURSE_COLLABORATION_REQUEST" -> Color(0xFFFFA000)
        "COURSE_COLLABORATION_ACCEPTED" -> Color(0xFF4CAF50)
        "COURSE_COLLABORATION_REJECTED" -> Color(0xFFF44336)
        "TEACHER_INVITE" -> Color(0xFF2196F3)
        "ADMIN_INVITE" -> Color(0xFF9C27B0)
        else -> accentColor
    }
    val hasAcademyLink = notification.relatedEntityId != null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(
                if (hasAcademyLink) {
                    Modifier.clickable { onAcademyClick(notification.relatedEntityId!!) }
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.status == "PENDING")
                typeColor.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
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
                    Column {
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
                
                if (hasAcademyLink) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "مشاهده آکادمی",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = notification.createdAt.toPersianDateTime(),
                    fontFamily = DanaFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                if (hasAcademyLink) {
                    Text(
                        text = "مشاهده آکادمی ❯",
                        fontFamily = DanaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Accept/Reject buttons for pending requests/invitations
            if ((notification.type == "COURSE_COLLABORATION_REQUEST" || notification.type == "TEACHER_INVITE" || notification.type == "ADMIN_INVITE") && notification.status == "PENDING") {
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
