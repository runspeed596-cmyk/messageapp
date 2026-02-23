package com.Kelasor.app.ui.screens.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.ExamDto
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ExamViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Exam History Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamHistoryScreen(
    onNavigateBack: () -> Unit,
    onExamClick: (String) -> Unit = {},
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadMyExams()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("آزمون‌های من", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MessageAppTheme.extendedColors.accent)
                }
            }
            state.myExams.isEmpty() -> {
                EmptyExamHistory(modifier = Modifier.padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(state.myExams) { exam ->
                        ExamHistoryCard(exam = exam, onClick = { onExamClick(exam.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyExamHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Assignment,
            null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "هنوز آزمونی ایجاد نکرده‌اید",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "آزمون‌های ایجاد شده توسط شما اینجا نمایش داده می‌شود",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExamHistoryCard(exam: ExamDto, onClick: () -> Unit) {
    val statusData = getExamStatusData(exam.status)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    exam.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusData.color.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusData.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusData.color
                    )
                }
            }
            exam.description?.let { desc ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(icon = Icons.Default.Assignment, value = "${exam.questionCount}", label = "سوال")
                StatItem(icon = Icons.Default.People, value = "${exam.attemptCount}", label = "شرکت‌کننده")
                StatItem(icon = Icons.Default.Timer, value = "${exam.durationMinutes} دق", label = "مدت")
                StatItem(icon = Icons.Default.Grade, value = "${exam.totalScore}", label = "نمره کل")
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = MessageAppTheme.extendedColors.accent)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private data class ExamStatusInfo(val label: String, val color: Color)

private fun getExamStatusData(status: String): ExamStatusInfo {
    return when (status) {
        "DRAFT" -> ExamStatusInfo("پیش‌نویس", Color(0xFF9E9E9E))
        "SCHEDULED" -> ExamStatusInfo("برنامه‌ریزی شده", Color(0xFF2196F3))
        "ACTIVE" -> ExamStatusInfo("فعال", Color(0xFF4CAF50))
        "ENDED" -> ExamStatusInfo("پایان یافته", Color(0xFFFF9800))
        "GRADED" -> ExamStatusInfo("نمره‌گذاری شده", Color(0xFF9C27B0))
        else -> ExamStatusInfo(status, Color(0xFF9E9E9E))
    }
}
