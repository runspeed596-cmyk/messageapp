package com.Kelasor.app.ui.components

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.Kelasor.app.R
import com.Kelasor.app.data.voice.RecordingState
import com.Kelasor.app.data.voice.VoiceRecorderManager
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.util.DateUtils
import com.Kelasor.app.ui.theme.MessageAppTypography
import kotlinx.coroutines.delay
import java.io.File

/**
 * MessageInputBar with voice recording support.
 * - When text is empty: tap mic to start recording, tap again to stop.
 * - When text has content: shows send button.
 */

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
    voiceRecorderManager: VoiceRecorderManager? = null,
    onVoiceRecorded: ((File, Long, List<Int>) -> Unit)? = null,
    onScheduleSendClick: ((Long) -> Unit)? = null,
    onVideoNoteClick: (() -> Unit)? = null
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sendButtonScale"
    )

    // Voice recording state
    val recordingInfo = voiceRecorderManager?.recordingInfo?.collectAsState()
    val isRecording = recordingInfo?.value?.state == RecordingState.RECORDING
    var recordingDuration by remember { mutableLongStateOf(0L) }
    var amplitude by remember { mutableStateOf(0) }
    var amplitudes = remember { mutableStateListOf<Int>() }
    var showEmojiPicker: Boolean by remember { mutableStateOf(false) }

    // Permission state
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    // Update duration and amplitude while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            amplitudes.clear()
            while (true) {
                recordingDuration = voiceRecorderManager?.getCurrentDuration() ?: 0L
                amplitude = voiceRecorderManager?.getAmplitude() ?: 0
                amplitudes.add(amplitude)
                delay(100)
            }
        } else {
            recordingDuration = 0L
            amplitude = 0
            // Do not clear amplitudes here immediately, we need them for onVoiceRecorded
        }
    }

    // Handle recording completion
    LaunchedEffect(recordingInfo?.value?.state) {
        if (recordingInfo?.value?.state == RecordingState.COMPLETED) {
            val filePath = recordingInfo.value.filePath
            val duration = recordingInfo.value.durationMs
            if (filePath != null && duration > 500) { // Minimum 500ms
                onVoiceRecorded?.invoke(File(filePath), duration, amplitudes.toList())
            }
            voiceRecorderManager?.reset()
        }
    }

    Column {
        // Recording overlay
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            VoiceRecordingOverlay(
                durationMs = recordingDuration,
                amplitude = amplitude,
                onCancel = { voiceRecorderManager?.cancelRecording() }
            )
        }

        // Main input bar (hidden during recording)
        AnimatedVisibility(
            visible = !isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(32.dp))
                    .border(
                        width = 0.8.dp,
                        color = extendedColors.glassBorder,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
                    )
                    .background(extendedColors.glass)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji toggle button
                IconButton(
                    onClick = { showEmojiPicker = !showEmojiPicker },
                    modifier = Modifier.size(42.dp)
                ) {
                    if (showEmojiPicker) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "صفحه‌کلید",
                            tint = extendedColors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text("\uD83D\uDE0A", style = MaterialTheme.typography.titleMedium)
                    }
                }
                // Text field
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = com.Kelasor.app.ui.theme.VazirFontFamily,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        textDirection = androidx.compose.ui.text.style.TextDirection.Content
                    ),
                    cursorBrush = SolidColor(extendedColors.accent),
                    decorationBox = { innerTextField ->
                        Box {
                            if (text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.message_hint),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = com.Kelasor.app.ui.theme.VazirFontFamily
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Attachment button
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.attachment),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Schedule button (only when text is present)
                if (onScheduleSendClick != null && text.isNotEmpty()) {
                    var showScheduleDialog by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "ارسال زمان‌بندی شده",
                            tint = extendedColors.accent.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (showScheduleDialog) {
                        SchedulePickerDialog(
                            onDismiss = { showScheduleDialog = false },
                            onSchedule = { timeMs ->
                                onScheduleSendClick(timeMs)
                                showScheduleDialog = false
                            }
                        )
                    }
                }

                // Mic / Video / Send button area
                if (text.isNotEmpty()) {
                    // Send button when text is present
                    GlowingIconButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = onSendClick,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.scale(sendButtonScale)
                    )
                } else if (isRecording) {
                    // Stop recording button
                    IconButton(
                        onClick = { voiceRecorderManager?.stopRecording() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "توقف ضبط",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    // Mic button
                    IconButton(
                        onClick = {
                            if (voiceRecorderManager != null && onVoiceRecorded != null) {
                                if (audioPermission.status.isGranted) {
                                    voiceRecorderManager.startRecording()
                                } else {
                                    audioPermission.launchPermissionRequest()
                                }
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ضبط صدا",
                            tint = if (voiceRecorderManager != null) extendedColors.accent else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Video note button
                    if (onVideoNoteClick != null) {
                        IconButton(
                            onClick = {
                                if (audioPermission.status.isGranted && cameraPermission.status.isGranted) {
                                    onVideoNoteClick()
                                } else {
                                    if (!cameraPermission.status.isGranted) {
                                        cameraPermission.launchPermissionRequest()
                                    } else if (!audioPermission.status.isGranted) {
                                        audioPermission.launchPermissionRequest()
                                    }
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "ویدیو نوت",
                                tint = extendedColors.accent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Recording mode bar (shown during recording)
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Stop recording button
                IconButton(
                    onClick = { voiceRecorderManager?.stopRecording() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(extendedColors.accent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "ارسال صدا",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Emoji Picker Grid (shown below input bar)
    AnimatedVisibility(
        visible = showEmojiPicker && !isRecording,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        EmojiPickerGrid(
            onEmojiClick = { emoji ->
                onTextChange(text + emoji)
            }
        )
    }
}

// Keep old signature for backward compatibility
@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit
) {
    MessageInputBar(
        text = text,
        onTextChange = onTextChange,
        onSendClick = onSendClick,
        onAttachClick = onAttachClick,
        voiceRecorderManager = null,
        onVoiceRecorded = null
    )
}


/**
 * Enhanced Schedule Picker Dialog with Shamsi (Jalali) calendar date picker + time picker.
 * Persian week layout: Saturday → Friday.
 * Includes quick presets at top for convenience.
 */
@Composable
fun SchedulePickerDialog(
    onDismiss: () -> Unit,
    onSchedule: (Long) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val now = System.currentTimeMillis()
    // Convert today to Jalali
    val todayCal = remember { java.util.Calendar.getInstance() }
    val todayJalali = remember {
        DateUtils.gregorianToJalali(
            todayCal.get(java.util.Calendar.YEAR),
            todayCal.get(java.util.Calendar.MONTH) + 1,
            todayCal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    // State: selected Jalali date
    var selectedYear by remember { mutableStateOf(todayJalali.first) }
    var selectedMonth by remember { mutableStateOf(todayJalali.second) }
    var selectedDay by remember { mutableStateOf(todayJalali.third) }
    var selectedHour by remember { mutableStateOf(todayCal.get(java.util.Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(todayCal.get(java.util.Calendar.MINUTE) + 5) }
    // Viewing month for calendar navigation (Jalali)
    var viewYear by remember { mutableStateOf(todayJalali.first) }
    var viewMonth by remember { mutableStateOf(todayJalali.second) }
    // Fix minute overflow
    if (selectedMinute >= 60) {
        selectedMinute = selectedMinute % 60
        selectedHour = (selectedHour + 1) % 24
    }
    // Persian weekday headers: Saturday first → Friday last
    val persianDayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    // Shamsi month names (1-based index)
    val shamsiMonthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    // Quick presets
    val presets = remember {
        val cal = java.util.Calendar.getInstance()
        listOf(
            "۱ ساعت بعد" to run {
                cal.timeInMillis = now
                cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
                cal.timeInMillis
            },
            "۳ ساعت بعد" to run {
                cal.timeInMillis = now
                cal.add(java.util.Calendar.HOUR_OF_DAY, 3)
                cal.timeInMillis
            },
            "فردا ۹:۰۰" to run {
                cal.timeInMillis = now
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 9)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.timeInMillis
            },
            "فردا ۱۸:۰۰" to run {
                cal.timeInMillis = now
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 18)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.timeInMillis
            }
        )
    }
    // Build result timestamp from selected Jalali date + time
    // Convert Jalali back to Gregorian, then build Calendar timestamp
    fun buildTimestamp(): Long {
        val (gYear, gMonth, gDay) = DateUtils.jalaliToGregorian(selectedYear, selectedMonth, selectedDay)
        val cal = java.util.Calendar.getInstance()
        cal.set(gYear, gMonth - 1, gDay, selectedHour, selectedMinute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    // Calculate days for the Jalali calendar grid
    // Returns list of nullable Ints (null = empty cell, Int = day number)
    fun getCalendarDays(jYear: Int, jMonth: Int): List<Int?> {
        val daysInMonth = DateUtils.jalaliMonthDays(jYear, jMonth)
        // Find the day of week for the 1st of this Jalali month
        val (gY, gM, gD) = DateUtils.jalaliToGregorian(jYear, jMonth, 1)
        val cal = java.util.Calendar.getInstance()
        cal.set(gY, gM - 1, gD)
        // Java Calendar: SATURDAY=7, SUNDAY=1, MONDAY=2, ...
        // We want Saturday=0, Sunday=1, Monday=2, ..., Friday=6
        val firstDowJava = cal.get(java.util.Calendar.DAY_OF_WEEK)
        val offset = when (firstDowJava) {
            java.util.Calendar.SATURDAY -> 0
            java.util.Calendar.SUNDAY -> 1
            java.util.Calendar.MONDAY -> 2
            java.util.Calendar.TUESDAY -> 3
            java.util.Calendar.WEDNESDAY -> 4
            java.util.Calendar.THURSDAY -> 5
            java.util.Calendar.FRIDAY -> 6
            else -> 0
        }
        val cells = mutableListOf<Int?>()
        repeat(offset) { cells.add(null) }
        for (d in 1..daysInMonth) { cells.add(d) }
        // Pad to fill last row
        while (cells.size % 7 != 0) { cells.add(null) }
        return cells
    }
    // Is Jalali date in the past?
    fun isDayInPast(jYear: Int, jMonth: Int, jDay: Int): Boolean {
        val (gY, gM, gD) = DateUtils.jalaliToGregorian(jYear, jMonth, jDay)
        val cal = java.util.Calendar.getInstance()
        cal.set(gY, gM - 1, gD, 23, 59, 59)
        return cal.timeInMillis < now
    }
    // Persian number formatter
    fun toPersianDigits(number: Int): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return number.toString().map { persianDigits[it - '0'] }.joinToString("")
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "زمان‌بندی ارسال",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                // ── Quick Presets ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (label, timeMs) ->
                        androidx.compose.material3.FilterChip(
                            selected = false,
                            onClick = { onSchedule(timeMs) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = extendedColors.accent.copy(alpha = 0.1f),
                                labelColor = extendedColors.accent
                            )
                        )
                    }
                }
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                // ── Calendar Navigation (Shamsi) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (viewMonth == 1) { viewMonth = 12; viewYear-- }
                        else viewMonth--
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "ماه قبل",
                            tint = extendedColors.accent
                        )
                    }
                    Text(
                        text = "${shamsiMonthNames[viewMonth - 1]} ${DateUtils.toPersianDigits(viewYear)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = {
                        if (viewMonth == 12) { viewMonth = 1; viewYear++ }
                        else viewMonth++
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "ماه بعد",
                            tint = extendedColors.accent
                        )
                    }
                }
                // ── Day-of-week headers ──
                Row(modifier = Modifier.fillMaxWidth()) {
                    persianDayHeaders.forEach { header ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = header,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // ── Calendar Grid (Shamsi) ──
                val days = remember(viewYear, viewMonth) { getCalendarDays(viewYear, viewMonth) }
                val rows = days.chunked(7)
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp)) {
                    rows.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { day ->
                                val isToday = day != null && day == todayJalali.third && viewMonth == todayJalali.second && viewYear == todayJalali.first
                                val isSelected = day != null && day == selectedDay && viewMonth == selectedMonth && viewYear == selectedYear
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(extendedColors.accent, CircleShape)
                                            } else if (isToday) {
                                                Modifier.background(extendedColors.accent.copy(alpha = 0.15f), CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .then(
                                            if (day != null && !isDayInPast(viewYear, viewMonth, day)) {
                                                Modifier.clickable {
                                                    selectedYear = viewYear
                                                    selectedMonth = viewMonth
                                                    selectedDay = day
                                                }
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (day != null) {
                                        val isPast = isDayInPast(viewYear, viewMonth, day)
                                        Text(
                                            text = DateUtils.toPersianDigits(day),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = when {
                                                isSelected -> Color.White
                                                isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                // ── Time Picker ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ساعت:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    // Hour selector
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedHour = if (selectedHour > 0) selectedHour - 1 else 23 }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = DateUtils.toPersianDigits(selectedHour).padStart(2, '۰'),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = extendedColors.accent
                            )
                            IconButton(onClick = { selectedHour = (selectedHour + 1) % 24 }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text(
                        ":",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    // Minute selector
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedMinute = if (selectedMinute > 0) selectedMinute - 1 else 59 }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = DateUtils.toPersianDigits(selectedMinute).padStart(2, '۰'),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = extendedColors.accent
                            )
                            IconButton(onClick = { selectedMinute = (selectedMinute + 1) % 60 }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row {
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("انصراف")
                }
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        val ts = buildTimestamp()
                        if (ts > now) onSchedule(ts)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = extendedColors.accent
                    )
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ارسال زمان‌بندی شده")
                }
            }
        }
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 😊 Emoji Picker Grid
// ═══════════════════════════════════════════════════════════════════════════════

private val EMOJI_CATEGORIES: List<Pair<String, List<String>>> = listOf(
    "😊" to listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂",
        "🙂", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗",
        "😋", "😛", "😜", "🤪", "😎", "🤗", "🤔", "🤫",
        "😏", "😒", "😞", "😔", "😟", "😢", "😭", "😤",
        "😡", "🤬", "😱", "😨", "😰", "😥", "😳", "🤯",
        "🤢", "🤮", "🤧", "😷", "🤕", "🤒", "😴", "🥱",
        "😪", "🤤", "😵", "🫠", "🥴", "🥸", "🫡", "🫣",
        "🫢", "🤭", "🫥", "🤐", "🫤", "😶", "😑", "😐",
        "🙄", "😬", "😮", "😯", "😲", "🥹", "😧", "😦",
        "😈", "👿", "💀", "☠️", "👻", "👽", "🤖", "🎃",
        "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿"
    ),
    "👍" to listOf(
        "👍", "👎", "👏", "🙌", "🤲", "🤝", "🙏", "✌️",
        "🤞", "🤟", "🤘", "👌", "🤌", "👈", "👉", "👆",
        "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "💪",
        "🦾", "🦿", "🖕", "✊", "👊", "🤛", "🤜", "🫰",
        "🫵", "🫱", "🫲", "🫳", "🫴", "👐", "🤲", "🫶",
        "🙏", "✍️", "💅", "🤳", "🦵", "🦶", "👂", "🦻",
        "👃", "👀", "👁️", "🫀", "🫁", "🧠", "🦷", "🦴"
    ),
    "❤️" to listOf(
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍",
        "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖",
        "💘", "💝", "💟", "🫶", "🥹", "❤️‍🔥", "❤️‍🩹",
        "💋", "💌", "💐", "🌹", "🥀", "🌺", "🌻", "🌼",
        "🌷", "🌸", "💮", "🏵️", "🪻", "🪷", "🪹", "🪺"
    ),
    "🎉" to listOf(
        "⭐", "🌟", "✨", "⚡", "🔥", "💯", "🎉", "🎊",
        "🎈", "🎁", "🏆", "🥇", "🥈", "🥉", "🏅", "🎖️",
        "🎯", "🚀", "💎", "🌈", "☀️", "🌙", "⏰", "📌",
        "🎵", "🎶", "🎤", "🎧", "🎼", "🎹", "🥁", "🎷",
        "🎺", "🎸", "🪗", "🎻", "🎲", "🎮", "🎰", "🧩"
    ),
    "🐶" to listOf(
        "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
        "🐻‍❄️", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵",
        "🙈", "🙉", "🙊", "🐔", "🐧", "🐦", "🦅", "🦆",
        "🦇", "🐺", "🐗", "🐴", "🦄", "🐝", "🪱", "🐛",
        "🦋", "🐌", "🐞", "🐜", "🪳", "🦂", "🐢", "🐍",
        "🦎", "🐙", "🦑", "🦐", "🦀", "🐡", "🐠", "🐟",
        "🐬", "🐳", "🐋", "🦈", "🐊", "🐅", "🐆", "🦓"
    ),
    "🍎" to listOf(
        "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓",
        "🫐", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝",
        "🍅", "🍆", "🥑", "🥦", "🥬", "🌽", "🌶️", "🫑",
        "🥕", "🧄", "🧅", "🥔", "🍠", "🥐", "🍞", "🥖",
        "🥨", "🧀", "🥚", "🍳", "🧇", "🥞", "🧈", "🍕",
        "🍔", "🍟", "🌭", "🥪", "🌮", "🌯", "🫔", "🥙",
        "🍝", "🍜", "🍲", "🍛", "🍣", "🍱", "🥟", "🍩",
        "🍰", "🎂", "🧁", "🍫", "🍬", "🍭", "🍮", "🍦"
    ),
    "🚗" to listOf(
        "🚗", "🚕", "🚙", "🏎️", "🚓", "🚑", "🚒", "🚐",
        "🛻", "🚚", "🚛", "🚜", "🏍️", "🛵", "🚲", "🛴",
        "✈️", "🛩️", "🚀", "🛸", "🚁", "🛥️", "🚢", "⛵",
        "🏠", "🏡", "🏢", "🏣", "🏤", "🏥", "🏦", "🏨",
        "🏩", "🏪", "🏫", "🏬", "🏰", "🏯", "🗼", "🗽",
        "⛪", "🕌", "🕍", "🛕", "⛩️", "🗿", "🌍", "🌎"
    ),
    "💡" to listOf(
        "💡", "🔦", "🕯️", "📱", "💻", "⌨️", "🖥️", "🖨️",
        "📷", "📸", "📹", "🎥", "📺", "📻", "🎙️", "🎚️",
        "⌚", "📡", "🔋", "🔌", "💰", "💵", "💴", "💶",
        "💷", "💳", "🪙", "⚽", "🏀", "🏈", "⚾", "🎾",
        "🏐", "🎳", "🏓", "🏸", "🥊", "🥋", "⛳", "⛸️",
        "🎿", "🛷", "🏂", "🏋️", "🤸", "🤺", "🧘", "🏊",
        "📝", "📖", "📚", "🔒", "🔑", "🛡️", "⚔️", "🏹"
    )
)

@Composable
private fun EmojiPickerGrid(
    onEmojiClick: (String) -> Unit
) {
    val extendedColors: com.Kelasor.app.ui.theme.ExtendedColors = MessageAppTheme.extendedColors
    var selectedCategory: Int by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Category tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EMOJI_CATEGORIES.forEachIndexed { index: Int, (icon: String, _) ->
                val isSelected: Boolean = index == selectedCategory
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) extendedColors.accent.copy(alpha = 0.2f)
                            else Color.Transparent
                        )
                        .clickable { selectedCategory = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        // Emoji grid
        val emojis: List<String> = EMOJI_CATEGORIES[selectedCategory].second
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(emojis) { emoji: String ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEmojiClick(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}


