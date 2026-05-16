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
// 🤝 Collaborations Screen — Course Collaboration Management
// ═══════════════════════════════════════════════════════════════════════════════

data class CollaborationsState(
    val pendingRequests: List<CourseCollaborationRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val academyId: String? = null
)

@HiltViewModel
class CollaborationsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CollaborationsState())
    val state: StateFlow<CollaborationsState> = _state.asStateFlow()
    init {
        loadAcademyAndCollaborations()
    }
    private fun loadAcademyAndCollaborations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                userRepository.getCurrentUser(forceRefresh = false).collect { result ->
                    if (result is UserResult.Success) {
                        val institutionId: String? = result.data.institutionId
                        _state.update { it.copy(academyId = institutionId) }
                        if (institutionId != null) {
                            loadPendingCollaborations(institutionId)
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
    fun loadPendingCollaborations(academyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getPendingCollaborations(academyId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val requests: List<CourseCollaborationRequestDto> = response.body()?.data?.content ?: emptyList()
                    _state.update { it.copy(pendingRequests = requests, isLoading = false) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطا در دریافت درخواست‌ها") }
                }
            } catch (e: Exception) {
                Log.e("CollaborationsVM", "Error loading collaborations", e)
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
                            pendingRequests = state.pendingRequests.map {
                                if (it.id == requestId) it.copy(status = "ACCEPTED") else it
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CollaborationsVM", "Error accepting", e)
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
                            pendingRequests = state.pendingRequests.map {
                                if (it.id == requestId) it.copy(status = "REJECTED") else it
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CollaborationsVM", "Error rejecting", e)
            }
        }
    }
    fun refresh() {
        val academyId: String = _state.value.academyId ?: return
        loadPendingCollaborations(academyId)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CollaborationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "همکاری‌ها",
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
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.pendingRequests.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "درخواست همکاری فعالی وجود ندارد",
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
                    // Pending requests
                    val pending: List<CourseCollaborationRequestDto> = state.pendingRequests.filter { it.status == "PENDING" }
                    val accepted: List<CourseCollaborationRequestDto> = state.pendingRequests.filter { it.status == "ACCEPTED" }
                    val rejected: List<CourseCollaborationRequestDto> = state.pendingRequests.filter { it.status == "REJECTED" }
                    if (pending.isNotEmpty()) {
                        item {
                            Text(
                                text = "در انتظار تایید (${pending.size})",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFA000),
                                fontSize = 14.sp
                            )
                        }
                        items(pending, key = { it.id }) { request ->
                            CollaborationRequestCard(
                                request = request,
                                onAccept = { viewModel.acceptCollaboration(request.id) },
                                onReject = { viewModel.rejectCollaboration(request.id) },
                                accentColor = extendedColors.accent
                            )
                        }
                    }
                    if (accepted.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تایید شده (${accepted.size})",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp
                            )
                        }
                        items(accepted, key = { it.id }) { request ->
                            CollaborationRequestCard(
                                request = request,
                                onAccept = {},
                                onReject = {},
                                accentColor = extendedColors.accent
                            )
                        }
                    }
                    if (rejected.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "رد شده (${rejected.size})",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF44336),
                                fontSize = 14.sp
                            )
                        }
                        items(rejected, key = { it.id }) { request ->
                            CollaborationRequestCard(
                                request = request,
                                onAccept = {},
                                onReject = {},
                                accentColor = extendedColors.accent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollaborationRequestCard(
    request: CourseCollaborationRequestDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    accentColor: Color
) {
    val statusColor: Color = when (request.status) {
        "PENDING" -> Color(0xFFFFA000)
        "ACCEPTED" -> Color(0xFF4CAF50)
        "REJECTED" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Handshake,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.courseTitle,
                        fontFamily = DanaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "از: ${request.senderInstitutionName}",
                        fontFamily = DanaFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Status badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = when (request.status) {
                            "PENDING" -> "در انتظار"
                            "ACCEPTED" -> "تایید شده"
                            "REJECTED" -> "رد شده"
                            else -> request.status
                        },
                        fontFamily = DanaFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (request.message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = request.message,
                    fontFamily = DanaFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = request.createdAt,
                fontFamily = DanaFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            // Action buttons for PENDING
            if (request.status == "PENDING") {
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
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
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
        }
    }
}
