package com.Kelasor.app.ui.screens.exam

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ExamEvent
import com.Kelasor.app.ui.viewmodel.ExamViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Taking Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamTakingScreen(
    examId: String,
    onNavigateBack: () -> Unit,
    onExamSubmitted: () -> Unit = {},
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(mutableMapOf<String, String>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    // Start exam attempt and timer
    LaunchedEffect(examId) {
        viewModel.loadExam(examId)
        viewModel.startAttempt(examId)
    }
    LaunchedEffect(state.currentExam) {
        state.currentExam?.let { exam ->
            remainingSeconds = exam.durationMinutes * 60
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
            // Auto-submit when time is up
            state.currentAttempt?.id?.let { attemptId ->
                viewModel.submitExam(attemptId)
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExamEvent.ExamSubmitted -> {
                    showResultDialog = true
                }
                is ExamEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }
    // Confirm Dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("تأیید پایان آزمون") },
            text = {
                Text(
                    "آیا مطمئنید می‌خواهید آزمون را ارسال کنید؟\n" +
                    "تعداد سوالات پاسخ داده شده: ${answers.size} از ${state.questions.size}"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    state.currentAttempt?.id?.let { viewModel.submitExam(it) }
                }) { Text("تأیید و ارسال", color = Color(0xFF4CAF50)) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("بازگشت") }
            }
        )
    }
    // Result Dialog
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                onExamSubmitted()
            },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("آزمون ارسال شد!", textAlign = TextAlign.Center) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    state.currentAttempt?.let { attempt ->
                        attempt.finalScore?.let { score ->
                            Text("نمره شما: $score", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        attempt.passed?.let { passed ->
                            Text(
                                if (passed) "✅ قبول شدید!" else "❌ قبول نشدید",
                                color = if (passed) Color(0xFF4CAF50) else Color(0xFFF44336),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    onExamSubmitted()
                }) { Text("بستن") }
            }
        )
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.currentExam?.title ?: "آزمون", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "سوال ${currentQuestionIndex + 1} از ${state.questions.size}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Timer
                    TimerBadge(remainingSeconds = remainingSeconds)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            ExamBottomBar(
                currentQuestion = currentQuestionIndex,
                totalQuestions = state.questions.size,
                onPrevious = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                onNext = {
                    if (currentQuestionIndex < state.questions.size - 1) currentQuestionIndex++
                },
                onSubmit = { showConfirmDialog = true },
                isLastQuestion = currentQuestionIndex == state.questions.size - 1
            )
        }
    ) { padding ->
        if (state.isLoading && state.questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MessageAppTheme.extendedColors.accent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("در حال بارگذاری آزمون...")
                }
            }
        } else if (state.questions.isNotEmpty()) {
            val question = state.questions[currentQuestionIndex]
            QuestionContent(
                modifier = Modifier.padding(padding),
                question = question,
                questionNumber = currentQuestionIndex + 1,
                selectedAnswer = answers[question.id],
                onAnswerSelected = { answer ->
                    answers = answers.toMutableMap().apply { this[question.id] = answer }
                    // Submit answer to server
                    state.currentAttempt?.id?.let { attemptId ->
                        val request = if (question.questionType == "MULTIPLE_CHOICE") {
                            SubmitAnswerRequest(questionId = question.id, selectedOption = answer)
                        } else {
                            SubmitAnswerRequest(questionId = question.id, answerText = answer)
                        }
                        viewModel.submitAnswer(attemptId, request)
                    }
                }
            )
        }
    }
}

@Composable
private fun TimerBadge(remainingSeconds: Int) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val isUrgent = remainingSeconds < 300
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isUrgent) Color(0xFFF44336).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                null,
                modifier = Modifier.size(16.dp),
                tint = if (isUrgent) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                String.format("%02d:%02d", minutes, seconds),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isUrgent) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QuestionContent(
    modifier: Modifier = Modifier,
    question: ExamQuestionDto,
    questionNumber: Int,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit
) {
    val accent = MessageAppTheme.extendedColors.accent
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$questionNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "${question.points} نمره",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        question.questionText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp
                    )
                }
            }
        }
        // Options or text input
        when (question.questionType) {
            "MULTIPLE_CHOICE" -> {
                itemsIndexed(question.options) { _, option ->
                    val isSelected = selectedAnswer == option.optionLabel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAnswerSelected(option.optionLabel) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) accent.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) accent else MaterialTheme.colorScheme.outline, CircleShape)
                                    .then(if (isSelected) Modifier.background(accent) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    option.optionLabel,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                option.optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
            else -> {
                item {
                    var textAnswer by remember(question.id) { mutableStateOf(selectedAnswer ?: "") }
                    OutlinedTextField(
                        value = textAnswer,
                        onValueChange = {
                            textAnswer = it
                            onAnswerSelected(it)
                        },
                        label = {
                            Text(
                                when (question.questionType) {
                                    "DESCRIPTIVE" -> "پاسخ تشریحی"
                                    "FILL_BLANK" -> "جای خالی را پر کنید"
                                    else -> "پاسخ شما"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = if (question.questionType == "DESCRIPTIVE") 5 else 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamBottomBar(
    currentQuestion: Int,
    totalQuestions: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    isLastQuestion: Boolean
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = currentQuestion > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("قبلی")
            }
            // Progress indicator
            Text(
                "${currentQuestion + 1} / $totalQuestions",
                fontWeight = FontWeight.Bold,
                color = MessageAppTheme.extendedColors.accent
            )
            if (isLastQuestion) {
                Button(
                    onClick = onSubmit,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("ارسال آزمون")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                }
            } else {
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MessageAppTheme.extendedColors.accent)
                ) {
                    Text("بعدی")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
