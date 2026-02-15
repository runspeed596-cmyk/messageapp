package com.Kelasor.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A component that provides a semi-transparent or blurred backdrop behind the 
 * system navigation bar (3-button or gesture bar).
 * This ensures that app content doesn't visually clash with system buttons.
 */
@Composable
fun SystemBarBackdrop(
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    
    // Telegram-style backdrop: Dark semi-transparent in both modes, 
    // but slightly different opacity for better contrast.
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.5f),
                Color.Black.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.1f),
                Color.Black.copy(alpha = 0.25f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navBarHeight)
            .background(backgroundBrush)
    )
}
