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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
    onVoiceRecorded: ((File, Long, List<Int>) -> Unit)? = null
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
    val amplitudes = remember { mutableStateListOf<Int>() }

    // Permission state
    val audioPermission = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

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
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Attachment button
                IconButton(onClick = onAttachClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.attachment),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text input with microphone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CardShapes.inputField)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Microphone button (replaces emoji)
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "ضبط صدا",
                            tint = if (voiceRecorderManager != null) 
                                extendedColors.accent 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (voiceRecorderManager != null && onVoiceRecorded != null) {
                                                if (audioPermission.status.isGranted) {
                                                    voiceRecorderManager.startRecording()
                                                } else {
                                                    audioPermission.launchPermissionRequest()
                                                }
                                            }
                                        }
                                    )
                                }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        BasicTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.weight(1f),
                            textStyle = MessageAppTypography.messageText.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(extendedColors.accent),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (text.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.message_hint),
                                            style = MessageAppTypography.inputHint,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send button (changes to stop when recording)
                if (isRecording) {
                    // Stop recording button
                    IconButton(
                        onClick = { voiceRecorderManager?.stopRecording() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "توقف ضبط",
                            tint = Color.White
                        )
                    }
                } else {
                    // Normal send button
                    GlowingIconButton(
                        icon = Icons.AutoMirrored.Filled.Send,
                        onClick = onSendClick,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.scale(sendButtonScale)
                    )
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
                    .navigationBarsPadding()
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
