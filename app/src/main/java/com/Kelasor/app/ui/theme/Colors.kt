package com.Kelasor.app.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════════════════════════════════════════════════════════════════════════
//  Premium iOS Messages-Inspired Color Palette — Kelasor Messenger
// ═══════════════════════════════════════════════════════════════════════════════

// ─────────────────────────────────────────────────────────────────────────────────
//  Base Dark Colors — True OLED-friendly dark tones
// ─────────────────────────────────────────────────────────────────────────────────

val PremiumBlack = Color(0xFF000000)
val PremiumBlackLight = Color(0xFF0A0A0A)
val PremiumSurface = Color(0xFF1C1C1E)
val PremiumSurfaceElevated = Color(0xFF2C2C2E)
val PremiumSurfaceHighlight = Color(0xFF3A3A3C)

// ─────────────────────────────────────────────────────────────────────────────────
//  Primary Blue Palette — iOS Messages Blue
// ─────────────────────────────────────────────────────────────────────────────────

val iOSBlue = Color(0xFF007AFF)
val iOSBlueDark = Color(0xFF0A84FF)
val iOSBlueLight = Color(0xFF409CFF)
val iOSBlueSoft = Color(0xFF5AC8FA)
val iOSBlueVibrant = Color(0xFF0055D4)

// ─────────────────────────────────────────────────────────────────────────────────
//  Gradient Colors — Blue spectrum
// ─────────────────────────────────────────────────────────────────────────────────

val GradientBlueStart = Color(0xFF007AFF)
val GradientBlueMid = Color(0xFF0A84FF)
val GradientBlueEnd = Color(0xFF5AC8FA)
val GradientBlueDeep = Color(0xFF0055D4)

// Legacy aliases kept for compatibility
val GradientPurpleStart = GradientBlueDeep
val GradientPurple = GradientBlueStart
val GradientMagenta = GradientBlueMid
val GradientPink = iOSBlueLight
val GradientCoral = GradientBlueEnd
val GradientOrange = Color(0xFF32ADE6)

// ─────────────────────────────────────────────────────────────────────────────────
//  💬 Chat Bubble Colors — iMessage-inspired
// ─────────────────────────────────────────────────────────────────────────────────

// My bubbles — Bold iOS blue
val MyBubblePrimary = Color(0xFF007AFF)
val MyBubbleSecondary = Color(0xFF0A84FF)
val MyBubbleDarkStart = Color(0xFF0A84FF)
val MyBubbleDarkEnd = Color(0xFF007AFF)
val MyBubbleLightStart = Color(0xFF007AFF)
val MyBubbleLightEnd = Color(0xFF409CFF)

// Other bubbles — Neutral gray
val OtherBubbleDark = Color(0xFF2C2C2E)
val OtherBubbleGlass = Color(0xFF3A3A3C)
val OtherBubbleLight = Color(0xFFE9E9EB)

// ─────────────────────────────────────────────────────────────────────────────────
//  ✨ Accent & Highlight Colors
// ─────────────────────────────────────────────────────────────────────────────────

val AccentBlue = Color(0xFF007AFF)
val AccentBlueLight = Color(0xFF5AC8FA)
val AccentTeal = Color(0xFF64D2FF)
val AccentIndigo = Color(0xFF5856D6)

// Legacy aliases
val AccentPink = AccentBlue
val AccentPinkLight = AccentBlueLight
val AccentPurple = AccentIndigo
val AccentCoral = AccentTeal

// Glow Colors (for shadows and effects)
val GlowBlue = Color(0x40007AFF)
val GlowBlueSoft = Color(0x300A84FF)
val GlowTeal = Color(0x405AC8FA)

// Legacy aliases
val GlowPink = GlowBlue
val GlowPurple = GlowBlueSoft
val GlowMagenta = GlowTeal

// ─────────────────────────────────────────────────────────────────────────────────
//  🟢 Status Colors
// ─────────────────────────────────────────────────────────────────────────────────

val OnlineGreen = Color(0xFF34C759)
val OnlineGreenLight = Color(0xFF30D158)
val TypingBlue = Color(0xFF007AFF)
val AwayYellow = Color(0xFFFFD60A)
val OfflineGray = Color(0xFF8E8E93)

// ─────────────────────────────────────────────────────────────────────────────────
//  🔴 Notification & Badge Colors
// ─────────────────────────────────────────────────────────────────────────────────

val UnreadBadgeBlue = Color(0xFF007AFF)
val UnreadBadgeRed = Color(0xFFFF3B30)
val UnreadBadgeGlow = Color(0x60007AFF)
val MutedBadge = Color(0xFF8E8E93)
val PinnedBadge = Color(0xFF007AFF)

// ─────────────────────────────────────────────────────────────────────────────────
//  📝 Text Colors
// ─────────────────────────────────────────────────────────────────────────────────

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFEBEBF5)
val TextTertiary = Color(0xFF8E8E93)
val TextMuted = Color(0xFF636366)
val TextOnAccent = Color(0xFFFFFFFF)

// Light theme text
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF3C3C43)
val LightTextTertiary = Color(0xFF8E8E93)

// ─────────────────────────────────────────────────────────────────────────────────
//  🔲 Border & Divider Colors
// ─────────────────────────────────────────────────────────────────────────────────

val BorderDark = Color(0xFF38383A)
val BorderLight = Color(0xFF48484A)
val DividerDark = Color(0xFF38383A)
val DividerLight = Color(0xFFC6C6C8)
val GlassBorderLight = Color(0x30FFFFFF)
val GlassBorderMedium = Color(0x20FFFFFF)

// ─────────────────────────────────────────────────────────────────────────────────
//  🔮 Glass & Overlay Colors
// ─────────────────────────────────────────────────────────────────────────────────

val GlassDark = Color(0x501C1C1E)
val GlassMedium = Color(0x702C2C2E)
val GlassLight = Color(0x20FFFFFF)
val OverlayDark = Color(0xCC000000)
val OverlayMedium = Color(0x80000000)

// ─────────────────────────────────────────────────────────────────────────────────
//  🎭 Shimmer & Loading Colors
// ─────────────────────────────────────────────────────────────────────────────────

val ShimmerBase = Color(0xFF2C2C2E)
val ShimmerHighlight = Color(0xFF3A3A3C)
val ShimmerAccent = Color(0x20007AFF)

// ─────────────────────────────────────────────────────────────────────────────────
//  ⚠️ Semantic Colors — iOS System
// ─────────────────────────────────────────────────────────────────────────────────

val ErrorRed = Color(0xFFFF3B30)
val ErrorRedLight = Color(0xFFFF6961)
val SuccessGreen = Color(0xFF34C759)
val SuccessGreenLight = Color(0xFF30D158)
val WarningYellow = Color(0xFFFFD60A)
val WarningYellowLight = Color(0xFFFFE620)
val InfoBlue = Color(0xFF007AFF)
val InfoBlueLight = Color(0xFF5AC8FA)

// ─────────────────────────────────────────────────────────────────────────────────
//  🏠 Bottom Navigation Colors
// ─────────────────────────────────────────────────────────────────────────────────

val NavBarBackground = Color(0xFF1C1C1E)
val NavBarSurface = Color(0xFF2C2C2E)
val NavItemInactive = Color(0xFF8E8E93)
val NavItemActive = Color(0xFF007AFF)

// ─────────────────────────────────────────────────────────────────────────────────
//  📱 Input Field Colors
// ─────────────────────────────────────────────────────────────────────────────────

val InputBackground = Color(0xFF2C2C2E)
val InputBackgroundFocused = Color(0xFF3A3A3C)
val InputBorder = Color(0xFF38383A)
val InputBorderFocused = Color(0xFF007AFF)
val InputPlaceholder = Color(0xFF636366)

// ─────────────────────────────────────────────────────────────────────────────────
//  🏷️ Filter Chip Colors
// ─────────────────────────────────────────────────────────────────────────────────

val ChipBackground = Color(0xFF2C2C2E)
val ChipBackgroundSelected = Color(0xFF007AFF)
val ChipBorder = Color(0xFF38383A)
val ChipText = Color(0xFFEBEBF5)
val ChipTextSelected = Color(0xFFFFFFFF)

// ═══════════════════════════════════════════════════════════════════════════════
//  ☀️ Light Theme Colors
// ═══════════════════════════════════════════════════════════════════════════════

val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF2F2F7)
val LightSurfaceSecondary = Color(0xFFE5E5EA)
val LightBorder = Color(0xFFC6C6C8)
val LightDivider = Color(0xFFC6C6C8)
