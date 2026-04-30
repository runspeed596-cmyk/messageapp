package com.Kelasor.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ═══════════════════════════════════════════════════════════════════════════════
//  ☀️ Premium Light Color Scheme — Clean iOS-inspired
// ═══════════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary = iOSBlue,
    onPrimary = Color.White,
    primaryContainer = iOSBlue.copy(alpha = 0.12f),
    onPrimaryContainer = iOSBlueVibrant,
    secondary = AccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = AccentIndigo.copy(alpha = 0.12f),
    onSecondaryContainer = AccentIndigo,
    tertiary = AccentTeal,
    onTertiary = Color.White,
    tertiaryContainer = AccentTeal.copy(alpha = 0.12f),
    onTertiaryContainer = AccentTeal,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightDivider,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedLight.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed
)

// ═══════════════════════════════════════════════════════════════════════════════
//  🌙 Premium Dark Color Scheme — True black OLED
// ═══════════════════════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = iOSBlueDark,
    onPrimary = Color.White,
    primaryContainer = iOSBlueDark.copy(alpha = 0.2f),
    onPrimaryContainer = iOSBlueLight,
    secondary = AccentIndigo,
    onSecondary = Color.White,
    secondaryContainer = AccentIndigo.copy(alpha = 0.15f),
    onSecondaryContainer = AccentTeal,
    tertiary = AccentTeal,
    onTertiary = Color.White,
    tertiaryContainer = AccentTeal.copy(alpha = 0.15f),
    onTertiaryContainer = AccentTeal,
    background = PremiumBlack,
    onBackground = TextPrimary,
    surface = PremiumSurface,
    onSurface = TextPrimary,
    surfaceVariant = PremiumSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    outlineVariant = DividerDark,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRedLight
)

// ═══════════════════════════════════════════════════════════════════════════════
//  🎯 Extended Colors for Premium Chat UI
// ═══════════════════════════════════════════════════════════════════════════════

data class ExtendedColors(
    // Chat Bubbles
    val myBubble: Color,
    val myBubbleEnd: Color,
    val otherBubble: Color,
    val myBubbleText: Color,
    val otherBubbleText: Color,
    // Status Indicators
    val onlineIndicator: Color,
    val typingIndicator: Color,
    // Badges
    val unreadBadge: Color,
    val unreadBadgeText: Color,
    val unreadBadgeGlow: Color,
    // Message Status
    val messageTime: Color,
    val messageRead: Color,
    val messageSent: Color,
    // Glass Effects
    val glass: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    // Shimmer Loading
    val shimmer: Color,
    val shimmerHighlight: Color,
    // Gradients
    val gradientStart: Color,
    val gradientMiddle: Color,
    val gradientEnd: Color,
    // Accent & Glow
    val accent: Color,
    val accentSecondary: Color,
    val accentGlow: Color,
    // Navigation
    val navBarBackground: Color,
    val navItemActive: Color,
    val navItemInactive: Color,
    // Input Fields
    val inputBackground: Color,
    val inputBorder: Color,
    val inputBorderFocused: Color,
    // Filter Chips
    val chipBackground: Color,
    val chipBackgroundSelected: Color,
    val chipText: Color,
    val chipTextSelected: Color,
    // Story Ring
    val storyRingStart: Color,
    val storyRingEnd: Color,
    // Tab Bar (Glassmorphism)
    val tabBarGlass: Color,
    val tabBarGlassBorder: Color,
    val tabIndicator: Color,
    val tabActiveText: Color,
    val tabInactiveText: Color,
    // Text (Fallback)
    val textSecondary: Color
)

// ═══════════════════════════════════════════════════════════════════════════════
//  ☀️ Light Extended Colors — Clean, bright iOS Messages
// ═══════════════════════════════════════════════════════════════════════════════

val LightExtendedColors = ExtendedColors(
    // Chat Bubbles — Blue sent, light gray received
    myBubble = iOSBlue,
    myBubbleEnd = iOSBlueLight,
    otherBubble = OtherBubbleLight,
    myBubbleText = Color.White,
    otherBubbleText = LightTextPrimary,
    // Status
    onlineIndicator = OnlineGreen,
    typingIndicator = iOSBlue,
    // Badges
    unreadBadge = iOSBlue,
    unreadBadgeText = Color.White,
    unreadBadgeGlow = GlowBlue,
    // Messages
    messageTime = LightTextTertiary,
    messageRead = iOSBlue,
    messageSent = LightTextTertiary,
    // Glass
    glass = Color.White.copy(alpha = 0.85f),
    glassBorder = Color.Black.copy(alpha = 0.06f),
    glassHighlight = Color.White.copy(alpha = 0.6f),
    // Shimmer
    shimmer = LightSurfaceElevated,
    shimmerHighlight = Color.White,
    // Gradients
    gradientStart = iOSBlue,
    gradientMiddle = iOSBlueDark,
    gradientEnd = iOSBlueSoft,
    // Accent
    accent = iOSBlue,
    accentSecondary = AccentIndigo,
    accentGlow = GlowBlue,
    // Navigation
    navBarBackground = LightSurface,
    navItemActive = iOSBlue,
    navItemInactive = LightTextTertiary,
    // Input
    inputBackground = LightSurfaceElevated,
    inputBorder = LightBorder,
    inputBorderFocused = iOSBlue,
    // Chips
    chipBackground = LightSurfaceElevated,
    chipBackgroundSelected = iOSBlue,
    chipText = LightTextSecondary,
    chipTextSelected = Color.White,
    // Story Ring
    storyRingStart = iOSBlue,
    storyRingEnd = iOSBlueSoft,
    // Tab Bar
    tabBarGlass = Color.White.copy(alpha = 0.92f),
    tabBarGlassBorder = Color.Black.copy(alpha = 0.04f),
    tabIndicator = Color.White,
    tabActiveText = iOSBlue,
    tabInactiveText = LightTextSecondary,
    // Text
    textSecondary = LightTextSecondary
)

// ═══════════════════════════════════════════════════════════════════════════════
//  🌙 Dark Extended Colors — OLED-optimized iOS-style
// ═══════════════════════════════════════════════════════════════════════════════

val DarkExtendedColors = ExtendedColors(
    // Chat Bubbles — Blue sent, dark gray received
    myBubble = iOSBlueDark,
    myBubbleEnd = iOSBlue,
    otherBubble = OtherBubbleDark,
    myBubbleText = Color.White,
    otherBubbleText = TextPrimary,
    // Status
    onlineIndicator = OnlineGreen,
    typingIndicator = iOSBlueDark,
    // Badges
    unreadBadge = iOSBlueDark,
    unreadBadgeText = Color.White,
    unreadBadgeGlow = GlowBlue,
    // Messages
    messageTime = TextTertiary,
    messageRead = iOSBlueDark,
    messageSent = TextMuted,
    // Glass
    glass = Color(0x501C1C1E),
    glassBorder = GlassBorderMedium,
    glassHighlight = GlassBorderLight,
    // Shimmer
    shimmer = ShimmerBase,
    shimmerHighlight = ShimmerHighlight,
    // Gradients
    gradientStart = iOSBlueDark,
    gradientMiddle = iOSBlue,
    gradientEnd = iOSBlueSoft,
    // Accent
    accent = iOSBlueDark,
    accentSecondary = AccentIndigo,
    accentGlow = GlowBlue,
    // Navigation
    navBarBackground = NavBarBackground,
    navItemActive = iOSBlueDark,
    navItemInactive = NavItemInactive,
    // Input
    inputBackground = InputBackground,
    inputBorder = InputBorder,
    inputBorderFocused = iOSBlueDark,
    // Chips
    chipBackground = ChipBackground,
    chipBackgroundSelected = iOSBlueDark,
    chipText = ChipText,
    chipTextSelected = ChipTextSelected,
    // Story Ring
    storyRingStart = iOSBlueDark,
    storyRingEnd = iOSBlueSoft,
    // Tab Bar
    tabBarGlass = Color.White.copy(alpha = 0.08f),
    tabBarGlassBorder = Color.White.copy(alpha = 0.10f),
    tabIndicator = Color.White.copy(alpha = 0.12f),
    tabActiveText = Color.White,
    tabInactiveText = TextSecondary,
    // Text
    textSecondary = TextSecondary
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

// ═══════════════════════════════════════════════════════════════════════════════
//  🌈 MessageApp Premium Theme Composable
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MessageAppTheme(
    darkTheme: Boolean? = null, // null = follow system theme
    colorPalette: String = "default", // Color palette selection
    content: @Composable () -> Unit
) {
    // If darkTheme is explicitly set, use that value; otherwise follow system
    val effectiveDarkTheme = darkTheme ?: isSystemInDarkTheme()
    val colorScheme = if (effectiveDarkTheme) DarkColorScheme else LightColorScheme
    // Get palette-specific accent colors
    val paletteAccent = when (colorPalette) {
        "ocean" -> Color(0xFF00BCD4)
        "sunset" -> Color(0xFFFF5722)
        "forest" -> Color(0xFF4CAF50)
        "lavender" -> Color(0xFF9C27B0)
        else -> if (effectiveDarkTheme) iOSBlueDark else iOSBlue
    }
    val paletteAccentSecondary = when (colorPalette) {
        "ocean" -> Color(0xFF0097A7)
        "sunset" -> Color(0xFFE64A19)
        "forest" -> Color(0xFF388E3C)
        "lavender" -> Color(0xFF7B1FA2)
        else -> AccentIndigo
    }
    val paletteGradientStart = when (colorPalette) {
        "ocean" -> Color(0xFF006064)
        "sunset" -> Color(0xFFBF360C)
        "forest" -> Color(0xFF1B5E20)
        "lavender" -> Color(0xFF4A148C)
        else -> if (effectiveDarkTheme) iOSBlueDark else iOSBlue
    }
    val paletteGradientEnd = when (colorPalette) {
        "ocean" -> Color(0xFF4DD0E1)
        "sunset" -> Color(0xFFFFAB91)
        "forest" -> Color(0xFFA5D6A7)
        "lavender" -> Color(0xFFCE93D8)
        else -> iOSBlueSoft
    }
    // Create palette-adjusted extended colors
    val baseExtendedColors = if (effectiveDarkTheme) DarkExtendedColors else LightExtendedColors
    val extendedColors = if (colorPalette == "default") {
        baseExtendedColors
    } else {
        baseExtendedColors.copy(
            accent = paletteAccent,
            accentSecondary = paletteAccentSecondary,
            gradientStart = paletteGradientStart,
            gradientEnd = paletteGradientEnd,
            myBubble = paletteGradientStart.copy(alpha = 0.95f),
            myBubbleEnd = paletteAccent.copy(alpha = 0.9f),
            storyRingStart = paletteGradientStart,
            storyRingEnd = paletteGradientEnd,
            navItemActive = paletteAccent,
            unreadBadge = paletteAccent,
            chipBackgroundSelected = paletteAccent,
            inputBorderFocused = paletteAccentSecondary
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !effectiveDarkTheme
                isAppearanceLightNavigationBars = !effectiveDarkTheme
            }
        }
    }
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  🔧 Theme Extensions
// ═══════════════════════════════════════════════════════════════════════════════

object MessageAppTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
