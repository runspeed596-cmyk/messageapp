package com.Kelasor.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        modifier = modifier.padding(end = 4.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = extendedColors.accent.copy(alpha = 0.2f),
            selectedLabelColor = extendedColors.accent,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            selectedBorderColor = extendedColors.accent,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderWidth = 1.dp,
            borderWidth = 1.dp,
            enabled = true,
            selected = selected
        )
    )
}
