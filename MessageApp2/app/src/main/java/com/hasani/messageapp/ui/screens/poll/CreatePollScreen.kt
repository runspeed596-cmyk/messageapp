package com.hasani.messageapp.ui.screens.poll

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePollScreen(
    onBack: () -> Unit,
    onSendPoll: (String, List<String>, Boolean, Boolean) -> Unit // question, options, isMultiple, isAnonymous
) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }
    var isMultipleChoice by remember { mutableStateOf(false) }
    var isAnonymous by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نظرسنجی جدید", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "انصراف")
                    }
                }
            )
        },
        bottomBar = {
             val isValid = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
             Surface(
                 tonalElevation = 8.dp,
                 shadowElevation = 8.dp,
                 modifier = Modifier.fillMaxWidth()
             ) {
                 Button(
                     onClick = {
                         val validOptions = options.filter { it.isNotBlank() }
                         onSendPoll(question, validOptions, isMultipleChoice, isAnonymous)
                         onBack()
                     },
                     enabled = isValid,
                     modifier = Modifier
                         .fillMaxWidth()
                         .padding(16.dp)
                         .height(56.dp),
                     shape = RoundedCornerShape(16.dp),
                     colors = ButtonDefaults.buttonColors(
                         containerColor = MaterialTheme.colorScheme.primary
                     )
                 ) {
                     Text(
                         text = "ارسال نظرسنجی",
                         style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatName,
                         color = Color.White
                     )
                 }
             }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("سوال خود را بپرسید", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }
            
            item {
                Text(
                    text = "گزینه‌ها (حداقل ۲ گزینه)",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (options.count { it.isNotBlank() } < 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            itemsIndexed(options) { index, option ->
                OutlinedTextField(
                    value = option,
                    onValueChange = { newText ->
                        val newOptions = options.toMutableList()
                        newOptions[index] = newText
                        options = newOptions
                    },
                    label = { Text("گزینه ${index + 1}", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = if (options.size > 2) {
                        {
                            IconButton(onClick = {
                                val newOptions = options.toMutableList()
                                newOptions.removeAt(index)
                                options = newOptions
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "حذف گزینه")
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (options.size < 10) {
                item {
                    TextButton(
                        onClick = { options = options + "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("افزودن گزینه", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime)
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("چند گزینه برای پاسخ", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime)
                    Switch(
                        checked = isMultipleChoice,
                        onCheckedChange = { isMultipleChoice = it }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نظرسنجی بی نام", style = com.hasani.messageapp.ui.theme.MessageAppTypography.chatTime)
                    Switch(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it }
                    )
                }
            }
            
            item {
                 Text(
                    text = "در صورت بی نام بودن، هیچکس متوجه نخواهد شد چه کسی به چه گزینه‌ای رای داده است.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
