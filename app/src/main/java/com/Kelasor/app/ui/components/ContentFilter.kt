package com.Kelasor.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme

enum class ContentType(val label: String) {
    Photo("عکس"),
    Video("ویدئو"),
    Link("لینک"),
    File("فایل"),
    Music("موزیک")
}

@Composable
fun ContentFilter(
    selectedType: ContentType?,
    onTypeSelected: (ContentType?) -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContentType.values().forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { 
                    if (selectedType == type) onTypeSelected(null) else onTypeSelected(type)
                },
                label = { Text(type.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = extendedColors.accent.copy(alpha = 0.2f),
                    selectedLabelColor = extendedColors.accent,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedType == type,
                    borderColor = if (selectedType == type) extendedColors.accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
        }
    }
}
