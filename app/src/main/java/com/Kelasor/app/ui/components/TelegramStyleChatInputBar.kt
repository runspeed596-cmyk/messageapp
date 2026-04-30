package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TelegramStyleChatInputBar(
    modifier: Modifier = Modifier,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAttachClick: () -> Unit,
    onEmojiClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // دکمه ایموجی سمت چپ
        IconButton(
            onClick = onEmojiClick,
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EmojiEmotions,
                contentDescription = "Emoji",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // باکس متنی با گوشه‌های گرد و پس‌زمینه محو
        Row(
            modifier = Modifier
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 16.dp, end = 4.dp), // Start padding for text, end for attachment icon
            verticalAlignment = Alignment.Bottom
        ) {
            BasicTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
                    .heightIn(min = 20.dp, max = 150.dp), // افزایش ارتفاع به صورت خودکار تا 150dp
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    textDirection = TextDirection.ContentOrRtl // پشتیبانی از تایپ راست‌چین
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "پیام...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                style = TextStyle(textDirection = TextDirection.Rtl)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // دکمه گیره (فایل ضمیمه) در داخل باکس متنی
            IconButton(
                onClick = onAttachClick,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // دکمه ارسال / میکروفون با انیمیشن تغییر
        Box(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = messageText.isNotBlank(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "SendVoiceToggle"
            ) { isTyping ->
                if (isTyping) {
                    IconButton(onClick = onSendClick) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    IconButton(onClick = onVoiceClick) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Record",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
