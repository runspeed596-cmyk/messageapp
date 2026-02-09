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
// 🎨 Premium Light Color Scheme
// ═══════════════════════════════════════════════════════════════════════════════

private val LightColorScheme = lightColorScheme(
    primary = AccentPurple,
    onPrimary = Color.White,
    primaryContainer = GradientPink.copy(alpha = 0.2f),
    onPrimaryContainer = GradientPurple,
    secondary = AccentPink,
    onSecondary = Color.White,
    secondaryContainer = AccentPinkLight.copy(alpha = 0.2f),
    onSecondaryContainer = AccentPink,
    tertiary = AccentCoral,
    onTertiary = Color.White,
    tertiaryContainer = AccentCoral.copy(alpha = 0.2f),
    onTertiaryContainer = AccentCoral,
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
    errorContainer = ErrorRedLight.copy(alpha = 0.3f),
    onErrorContainer = ErrorRed
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🌙 Premium Dark Color Scheme
// ═══════════════════════════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = AccentPink,
    onPrimary = Color.White,
    primaryContainer = GradientPurple.copy(alpha = 0.3f),
    onPrimaryContainer = AccentPinkLight,
    secondary = AccentPurple,
    onSecondary = Color.White,
    secondaryContainer = AccentPurple.copy(alpha = 0.2f),
    onSecondaryContainer = GradientPink,
    tertiary = AccentCoral,
    onTertiary = Color.White,
    tertiaryContainer = AccentCoral.copy(alpha = 0.2f),
    onTertiaryContainer = AccentCoral,
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
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRedLight
)

// ═══════════════════════════════════════════════════════════════════════════════
// 🎯 Extended Colors for Premium Chat UI
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

    // Text (Fallback)
    val textSecondary: Color
)

val LightExtendedColors = ExtendedColors(
    // Chat Bubbles - Light theme uses softer colors
    myBubble = GradientPurple.copy(alpha = 0.9f),
    myBubbleEnd = AccentPink.copy(alpha = 0.85f),
    otherBubble = LightSurfaceElevated,
    myBubbleText = Color.White,
    otherBubbleText = LightTextPrimary,
    
    // Status
    onlineIndicator = OnlineGreen,
    typingIndicator = TypingBlue,
    
    // Badges
    unreadBadge = UnreadBadgeRed,
    unreadBadgeText = Color.White,
    unreadBadgeGlow = UnreadBadgeGlow,
    
    // Messages
    messageTime = LightTextTertiary,
    messageRead = TypingBlue,
    messageSent = LightTextTertiary,
    
    // Glass
    glass = Color.White.copy(alpha = 0.8f),
    glassBorder = Color.White.copy(alpha = 0.3f),
    glassHighlight = Color.White.copy(alpha = 0.5f),
    
    // Shimmer
    shimmer = LightSurfaceElevated,
    shimmerHighlight = Color.White,
    
    // Gradients
    gradientStart = GradientPurple,
    gradientMiddle = GradientMagenta,
    gradientEnd = GradientCoral,
    
    // Accent
    accent = AccentPink,
    accentSecondary = AccentPurple,
    accentGlow = GlowPink,
    
    // Navigation
    navBarBackground = LightSurface,
    navItemActive = AccentPink,
    navItemInactive = LightTextTertiary,
    
    // Input
    inputBackground = LightSurfaceElevated,
    inputBorder = LightBorder,
    inputBorderFocused = AccentPurple,
    
    // Chips
    chipBackground = LightSurfaceElevated,
    chipBackgroundSelected = AccentPink,
    chipText = LightTextSecondary,
    chipTextSelected = Color.White,
    
    // Story Ring
    storyRingStart = GradientPurple,
    storyRingEnd = GradientCoral,
    
    // Text
    textSecondary = LightTextSecondary
)

val DarkExtendedColors = ExtendedColors(
    // Chat Bubbles - Matching reference design
    myBubble = MyBubbleDarkStart,
    myBubbleEnd = MyBubbleDarkEnd,
    otherBubble = OtherBubbleGlass,
    myBubbleText = TextPrimary,
    otherBubbleText = TextPrimary,
    
    // Status
    onlineIndicator = OnlineGreen,
    typingIndicator = TypingBlue,
    
    // Badges
    unreadBadge = UnreadBadgeRed,
    unreadBadgeText = Color.White,
    unreadBadgeGlow = UnreadBadgeGlow,
    
    // Messages
    messageTime = TextTertiary,
    messageRead = TypingBlue,
    messageSent = TextMuted,
    
    // Glass
    glass = GlassDark,
    glassBorder = GlassBorderMedium,
    glassHighlight = GlassBorderLight,
    
    // Shimmer
    shimmer = ShimmerBase,
    shimmerHighlight = ShimmerHighlight,
    
    // Gradients - Rich purple to pink
    gradientStart = GradientPurple,
    gradientMiddle = GradientMagenta,
    gradientEnd = GradientCoral,
    
    // Accent
    accent = AccentPink,
    accentSecondary = AccentPurple,
    accentGlow = GlowPink,
    
    // Navigation
    navBarBackground = NavBarBackground,
    navItemActive = NavItemActive,
    navItemInactive = NavItemInactive,
    
    // Input
    inputBackground = InputBackground,
    inputBorder = InputBorder,
    inputBorderFocused = InputBorderFocused,
    
    // Chips
    chipBackground = ChipBackground,
    chipBackgroundSelected = ChipBackgroundSelected,
    chipText = ChipText,
    chipTextSelected = ChipTextSelected,
    
    // Story Ring
    storyRingStart = GradientPurple,
    storyRingEnd = AccentCoral,
    
    // Text
    textSecondary = TextSecondary
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

// ═══════════════════════════════════════════════════════════════════════════════
// 🌈 MessageApp Premium Theme Composable
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
        "ocean" -> Color(0xFF00BCD4)    // Cyan/Ocean
        "sunset" -> Color(0xFFFF5722)   // Deep Orange/Sunset
        "forest" -> Color(0xFF4CAF50)   // Green/Forest
        "lavender" -> Color(0xFF9C27B0) // Purple/Lavender
        else -> AccentPink              // Default pink
    }
    val paletteAccentSecondary = when (colorPalette) {
        "ocean" -> Color(0xFF0097A7)
        "sunset" -> Color(0xFFE64A19)
        "forest" -> Color(0xFF388E3C)
        "lavender" -> Color(0xFF7B1FA2)
        else -> AccentPurple
    }
    val paletteGradientStart = when (colorPalette) {
        "ocean" -> Color(0xFF006064)
        "sunset" -> Color(0xFFBF360C)
        "forest" -> Color(0xFF1B5E20)
        "lavender" -> Color(0xFF4A148C)
        else -> GradientPurple
    }
    val paletteGradientEnd = when (colorPalette) {
        "ocean" -> Color(0xFF4DD0E1)
        "sunset" -> Color(0xFFFFAB91)
        "forest" -> Color(0xFFA5D6A7)
        "lavender" -> Color(0xFFCE93D8)
        else -> GradientCoral
    }
    
    // Create palette-adjusted extended colors
    val baseExtendedColors = if (effectiveDarkTheme) DarkExtendedColors else LightExtendedColors
    val extendedColors = baseExtendedColors.copy(
        accent = paletteAccent,
        accentSecondary = paletteAccentSecondary,
        gradientStart = paletteGradientStart,
        gradientEnd = paletteGradientEnd,
        myBubble = paletteGradientStart.copy(alpha = 0.9f),
        myBubbleEnd = paletteAccent.copy(alpha = 0.85f),
        storyRingStart = paletteGradientStart,
        storyRingEnd = paletteGradientEnd,
        navItemActive = paletteAccent,
        unreadBadge = paletteAccent,
        chipBackgroundSelected = paletteAccent,
        inputBorderFocused = paletteAccentSecondary
    )
    
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
// 🔧 Theme Extensions
// ═══════════════════════════════════════════════════════════════════════════════

object MessageAppTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
