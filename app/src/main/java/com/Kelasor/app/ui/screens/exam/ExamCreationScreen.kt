package com.Kelasor.app.ui.screens.exam

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ExamEvent
import com.Kelasor.app.ui.viewmodel.ExamViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Creation Screen - Multi-step wizard
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCreationScreen(
    channelId: String? = null,
    onNavigateBack: () -> Unit,
    onExamCreated: (String) -> Unit = {},
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var currentStep by remember { mutableIntStateOf(0) }
    // Step 0: Exam Info
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf("60") }
    var totalScore by remember { mutableStateOf("20") }
    var passScore by remember { mutableStateOf("10") }
    var isPublic by remember { mutableStateOf(false) }
    var shuffleQuestions by remember { mutableStateOf(false) }
    var shuffleOptions by remember { mutableStateOf(false) }
    var showResultsAfter by remember { mutableStateOf(true) }
    var maxAttempts by remember { mutableStateOf("1") }
    // Step 1: Questions
    var questionText by remember { mutableStateOf("") }
    var questionType by remember { mutableStateOf("MULTIPLE_CHOICE") }
    var questionPoints by remember { mutableStateOf("1") }
    var optionTexts by remember { mutableStateOf(listOf("", "", "", "")) }
    var correctOptionIndex by remember { mutableIntStateOf(0) }
    var shortAnswerCorrect by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val steps = listOf("اطلاعات آزمون", "سوالات", "تأیید و فعال‌سازی")
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExamEvent.ExamCreated -> {
                    currentStep = 1
                    snackbarHostState.showSnackbar("آزمون ایجاد شد! حالا سوالات را اضافه کنید")
                }
                is ExamEvent.QuestionAdded -> {
                    questionText = ""
                    optionTexts = listOf("", "", "", "")
                    correctOptionIndex = 0
                    shortAnswerCorrect = ""
                    snackbarHostState.showSnackbar("سوال اضافه شد")
                }
                is ExamEvent.ExamActivated -> {
                    snackbarHostState.showSnackbar("آزمون فعال شد!")
                    onNavigateBack()
                }
                is ExamEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("ایجاد آزمون", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Step indicator
            StepIndicator(steps = steps, currentStep = currentStep)
            Spacer(modifier = Modifier.height(16.dp))
            when (currentStep) {
                0 -> ExamInfoStep(
                    title = title,
                    onTitleChange = { title = it },
                    description = description,
                    onDescriptionChange = { description = it },
                    durationMinutes = durationMinutes,
                    onDurationChange = { durationMinutes = it },
                    totalScore = totalScore,
                    onTotalScoreChange = { totalScore = it },
                    passScore = passScore,
                    onPassScoreChange = { passScore = it },
                    isPublic = isPublic,
                    onIsPublicChange = { isPublic = it },
                    shuffleQuestions = shuffleQuestions,
                    onShuffleQuestionsChange = { shuffleQuestions = it },
                    shuffleOptions = shuffleOptions,
                    onShuffleOptionsChange = { shuffleOptions = it },
                    showResultsAfter = showResultsAfter,
                    onShowResultsChange = { showResultsAfter = it },
                    maxAttempts = maxAttempts,
                    onMaxAttemptsChange = { maxAttempts = it },
                    isLoading = state.isLoading,
                    onNext = {
                        if (title.isBlank()) return@ExamInfoStep
                        val now = Instant.now()
                        val request = CreateExamRequest(
                            title = title,
                            description = description.ifBlank { null },
                            channelId = channelId,
                            startsAt = now.toString(),
                            endsAt = now.plusSeconds(86400L * 7).toString(),
                            durationMinutes = durationMinutes.toIntOrNull() ?: 60,
                            totalScore = totalScore.toDoubleOrNull() ?: 20.0,
                            passScore = passScore.toDoubleOrNull(),
                            isPublic = isPublic,
                            shuffleQuestions = shuffleQuestions,
                            shuffleOptions = shuffleOptions,
                            showResultsAfter = showResultsAfter,
                            maxAttempts = maxAttempts.toIntOrNull() ?: 1
                        )
                        viewModel.createExam(request)
                    }
                )
                1 -> QuestionsStep(
                    questions = state.questions,
                    questionText = questionText,
                    onQuestionTextChange = { questionText = it },
                    questionType = questionType,
                    onQuestionTypeChange = { questionType = it },
                    questionPoints = questionPoints,
                    onQuestionPointsChange = { questionPoints = it },
                    optionTexts = optionTexts,
                    onOptionTextChange = { index, text ->
                        optionTexts = optionTexts.toMutableList().apply { this[index] = text }
                    },
                    correctOptionIndex = correctOptionIndex,
                    onCorrectOptionChange = { correctOptionIndex = it },
                    shortAnswerCorrect = shortAnswerCorrect,
                    onShortAnswerChange = { shortAnswerCorrect = it },
                    isLoading = state.isLoading,
                    onAddQuestion = {
                        val examId = state.currentExam?.id ?: return@QuestionsStep
                        val labels = listOf("A", "B", "C", "D")
                        val options = if (questionType == "MULTIPLE_CHOICE") {
                            optionTexts.mapIndexed { i, text ->
                                QuestionOptionRequest(
                                    optionText = text,
                                    optionLabel = labels[i],
                                    isCorrect = i == correctOptionIndex,
                                    sortOrder = i
                                )
                            }.filter { it.optionText.isNotBlank() }
                        } else emptyList()
                        val request = AddQuestionRequest(
                            questionType = questionType,
                            questionText = questionText,
                            points = questionPoints.toDoubleOrNull() ?: 1.0,
                            sortOrder = state.questions.size,
                            correctAnswer = if (questionType != "MULTIPLE_CHOICE") shortAnswerCorrect.ifBlank { null } else null,
                            options = options
                        )
                        viewModel.addQuestion(examId, request)
                    },
                    onDeleteQuestion = { viewModel.deleteQuestion(it) },
                    onNext = { currentStep = 2 }
                )
                2 -> ConfirmStep(
                    exam = state.currentExam,
                    questionCount = state.questions.size,
                    isLoading = state.isLoading,
                    onActivate = {
                        state.currentExam?.id?.let { viewModel.activateExam(it) }
                    }
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(steps: List<String>, currentStep: Int) {
    val accent = MessageAppTheme.extendedColors.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentStep
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isActive) accent else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            "${index + 1}",
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    label,
                    fontSize = 11.sp,
                    color = if (isActive) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ExamInfoStep(
    title: String, onTitleChange: (String) -> Unit,
    description: String, onDescriptionChange: (String) -> Unit,
    durationMinutes: String, onDurationChange: (String) -> Unit,
    totalScore: String, onTotalScoreChange: (String) -> Unit,
    passScore: String, onPassScoreChange: (String) -> Unit,
    isPublic: Boolean, onIsPublicChange: (Boolean) -> Unit,
    shuffleQuestions: Boolean, onShuffleQuestionsChange: (Boolean) -> Unit,
    shuffleOptions: Boolean, onShuffleOptionsChange: (Boolean) -> Unit,
    showResultsAfter: Boolean, onShowResultsChange: (Boolean) -> Unit,
    maxAttempts: String, onMaxAttemptsChange: (String) -> Unit,
    isLoading: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("عنوان آزمون *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("توضیحات") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Duration & Scores
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = onDurationChange,
                label = { Text("مدت (دقیقه)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = totalScore,
                onValueChange = onTotalScoreChange,
                label = { Text("نمره کل") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = passScore,
                onValueChange = onPassScoreChange,
                label = { Text("نمره قبولی") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = maxAttempts,
                onValueChange = onMaxAttemptsChange,
                label = { Text("تعداد شرکت") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Toggle settings
        SwitchRow("آزمون عمومی", isPublic, onIsPublicChange)
        SwitchRow("مخلوط کردن سوالات", shuffleQuestions, onShuffleQuestionsChange)
        SwitchRow("مخلوط کردن گزینه‌ها", shuffleOptions, onShuffleOptionsChange)
        SwitchRow("نمایش نتایج بعد از آزمون", showResultsAfter, onShowResultsChange)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = title.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MessageAppTheme.extendedColors.accent
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("مرحله بعد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun QuestionsStep(
    questions: List<ExamQuestionDto>,
    questionText: String, onQuestionTextChange: (String) -> Unit,
    questionType: String, onQuestionTypeChange: (String) -> Unit,
    questionPoints: String, onQuestionPointsChange: (String) -> Unit,
    optionTexts: List<String>,
    onOptionTextChange: (Int, String) -> Unit,
    correctOptionIndex: Int, onCorrectOptionChange: (Int) -> Unit,
    shortAnswerCorrect: String, onShortAnswerChange: (String) -> Unit,
    isLoading: Boolean,
    onAddQuestion: () -> Unit,
    onDeleteQuestion: (String) -> Unit,
    onNext: () -> Unit
) {
    val questionTypes = mapOf(
        "MULTIPLE_CHOICE" to "تستی",
        "SHORT_ANSWER" to "کوتاه پاسخ",
        "DESCRIPTIVE" to "تشریحی",
        "FILL_BLANK" to "جای خالی",
        "IMAGE_BASED" to "تصویری"
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Existing questions
        item {
            if (questions.isNotEmpty()) {
                Text(
                    "سوالات اضافه شده (${questions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MessageAppTheme.extendedColors.accent
                )
            }
        }
        itemsIndexed(questions) { index, question ->
            QuestionCard(index + 1, question, onDelete = { onDeleteQuestion(question.id) })
        }
        // Add question form
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("افزودن سوال جدید", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    // Question type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        questionTypes.forEach { (type, label) ->
                            val isSelected = questionType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { onQuestionTypeChange(type) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MessageAppTheme.extendedColors.accent,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = onQuestionTextChange,
                        label = { Text("متن سوال *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = questionPoints,
                        onValueChange = onQuestionPointsChange,
                        label = { Text("نمره") },
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Type-specific fields
                    AnimatedVisibility(visible = questionType == "MULTIPLE_CHOICE") {
                        Column {
                            val labels = listOf("الف", "ب", "ج", "د")
                            optionTexts.forEachIndexed { i, text ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = correctOptionIndex == i,
                                        onClick = { onCorrectOptionChange(i) },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MessageAppTheme.extendedColors.accent
                                        )
                                    )
                                    OutlinedTextField(
                                        value = text,
                                        onValueChange = { onOptionTextChange(i, it) },
                                        label = { Text("گزینه ${labels[i]}") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 4.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    AnimatedVisibility(visible = questionType == "SHORT_ANSWER" || questionType == "FILL_BLANK") {
                        OutlinedTextField(
                            value = shortAnswerCorrect,
                            onValueChange = onShortAnswerChange,
                            label = { Text("پاسخ صحیح") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddQuestion,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = questionText.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MessageAppTheme.extendedColors.accent
                        )
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("افزودن سوال")
                    }
                }
            }
        }
        // Next button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = questions.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("مرحله بعد - تأیید نهایی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun QuestionCard(number: Int, question: ExamQuestionDto, onDelete: () -> Unit) {
    val typeLabel = when (question.questionType) {
        "MULTIPLE_CHOICE" -> "تستی"
        "SHORT_ANSWER" -> "کوتاه پاسخ"
        "DESCRIPTIVE" -> "تشریحی"
        "FILL_BLANK" -> "جای خالی"
        "IMAGE_BASED" -> "تصویری"
        else -> question.questionType
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MessageAppTheme.extendedColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Text("$number", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(question.questionText, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    AssistChip(
                        onClick = {},
                        label = { Text(typeLabel, fontSize = 11.sp) },
                        modifier = Modifier.height(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${question.points} نمره", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    exam: ExamDto?,
    questionCount: Int,
    isLoading: Boolean,
    onActivate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "آزمون آماده فعال‌سازی است!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (exam != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("عنوان", exam.title)
                    InfoRow("تعداد سوالات", "$questionCount")
                    InfoRow("مدت زمان", "${exam.durationMinutes} دقیقه")
                    InfoRow("نمره کل", "${exam.totalScore}")
                    exam.passScore?.let { InfoRow("نمره قبولی", "$it") }
                    InfoRow("نوع", if (exam.isPublic) "عمومی" else "خصوصی")
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onActivate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("فعال‌سازی آزمون", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}
