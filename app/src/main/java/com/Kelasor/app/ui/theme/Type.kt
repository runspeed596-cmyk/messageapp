package com.Kelasor.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
import com.Kelasor.app.R

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Vazir Font Family - Persian Typography
// ═══════════════════════════════════════════════════════════════════════════════

val VazirFontFamily = FontFamily(
    Font(R.font.vazir_light, FontWeight.Light),
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_medium, FontWeight.Medium),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

val PinarFontFamily = FontFamily(
    Font(R.font.pinar, FontWeight.Bold)
)

val DanaFontFamily = FontFamily(
    Font(R.font.dana_regular, FontWeight.Normal),
    Font(R.font.dana_medium, FontWeight.Medium),
    Font(R.font.dana_bold, FontWeight.Bold)
)

val DoranFontFamily = FontFamily(
    Font(R.font.doran_vf, FontWeight.Bold)
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Premium Typography System
// ═══════════════════════════════════════════════════════════════════════════════

val Typography = Typography(
    // ─────────────────────────────────────────────────────────────────────────────
    // Display Styles - Large Headlines
    // ─────────────────────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,

    ),
    displayMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,

    ),
    displaySmall = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,

    ),
    // ─────────────────────────────────────────────────────────────────────────────
    // Headline Styles - Section Headers
    // ─────────────────────────────────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,

    ),
    headlineMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,

    ),
    headlineSmall = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,

    ),
    // ─────────────────────────────────────────────────────────────────────────────
    // Title Styles - App Bar & Cards
    // ─────────────────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,

    ),
    titleMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,

    ),
    titleSmall = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,

    ),
    // ─────────────────────────────────────────────────────────────────────────────
    // Body Styles - Main Content & Messages
    // ─────────────────────────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,

    ),
    bodyMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,

    ),
    bodySmall = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,

    ),
    // ─────────────────────────────────────────────────────────────────────────────
    // Label Styles - Buttons & Tags
    // ─────────────────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,

    ),
    labelMedium = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,

    ),
    labelSmall = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,

    )
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🔤 Custom Text Styles for Messaging App
// ═══════════════════════════════════════════════════════════════════════════════

object MessageAppTypography {
    val chatName = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,

    )
    val chatPreview = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,

    )
    val chatTime = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        textDirection = TextDirection.Ltr
    )
    val messageText = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,

    )
    val messageTime = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        textDirection = TextDirection.Ltr
    )
    val unreadCount = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        textDirection = TextDirection.Ltr
    )
    val appBarTitle = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,

    )
    val buttonText = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,

    )
    val inputHint = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,

    )
    val sectionTitle = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
    )
    val caption = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    val body = TextStyle(
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        textDirection = TextDirection.ContentOrLtr
    )
}
