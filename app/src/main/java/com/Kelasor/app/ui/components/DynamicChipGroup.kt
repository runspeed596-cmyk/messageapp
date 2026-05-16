package com.Kelasor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.Kelasor.app.ui.theme.DanaFontFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DynamicChipGroup(
    label: String,
    items: List<String>,
    suggestions: List<String> = emptyList(),
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    placeholder: String,
    allowManualAdd: Boolean = false
) {
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val filteredSuggestions = remember(text, suggestions, items) {
        if (text.isBlank()) emptyList()
        else suggestions.filter { it.contains(text, ignoreCase = true) && it !in items }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box {
            OutlinedTextField(
                value = text,
                onValueChange = { 
                    text = it
                    expanded = filteredSuggestions.isNotEmpty()
                },
                label = { Text(label, fontFamily = DanaFontFamily) },
                placeholder = { Text(placeholder, fontFamily = DanaFontFamily, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = if (allowManualAdd) ImeAction.Done else ImeAction.Default),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (allowManualAdd && text.isNotBlank() && text !in items) {
                            onAdd(text.trim())
                            text = ""
                            expanded = false
                        }
                    }
                ),
                trailingIcon = if (allowManualAdd) {
                    {
                        IconButton(onClick = {
                            if (text.isNotBlank() && text !in items) {
                                onAdd(text.trim())
                                text = ""
                                expanded = false
                            }
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "افزودن")
                        }
                    }
                } else null
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f),
                properties = PopupProperties(focusable = false)
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion, fontFamily = DanaFontFamily) },
                        onClick = {
                            onAdd(suggestion)
                            text = ""
                            expanded = false
                        }
                    )
                }
            }
        }
        
        if (items.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    AssistChip(
                        onClick = { },
                        label = { Text(item, fontFamily = DanaFontFamily, fontSize = 12.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onRemove(item) },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        }
                    )
                }
            }
        }
    }
}
