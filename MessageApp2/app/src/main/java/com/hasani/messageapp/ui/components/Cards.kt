package com.hasani.messageapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hasani.messageapp.ui.theme.CardShapes
import com.hasani.messageapp.ui.theme.GlassBorderLight
import com.hasani.messageapp.ui.theme.GlassBorderMedium
import com.hasani.messageapp.ui.theme.MessageAppTheme

// ═══════════════════════════════════════════════════════════════════════════════
// 🔮 Premium Glassmorphism Card Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    hasGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .then(
                if (hasGlow) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = CardShapes.glassCard,
                        ambientColor = extendedColors.accentGlow,
                        spotColor = extendedColors.accentGlow
                    )
                } else {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CardShapes.glassCard,
                        ambientColor = Color.Black.copy(alpha = 0.3f)
                    )
                }
            )
            .clip(CardShapes.glassCard)
            .background(extendedColors.glass)
            .drawBehind {
                // Glass border effect
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassBorderLight,
                            GlassBorderMedium
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Premium Gradient Card Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val colors = gradientColors ?: listOf(
        extendedColors.gradientStart,
        extendedColors.gradientMiddle,
        extendedColors.gradientEnd
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = CardShapes.glassCard,
                ambientColor = extendedColors.accentGlow,
                spotColor = extendedColors.accentGlow
            )
            .clip(CardShapes.glassCard)
            .background(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📦 Premium Surface Card Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "backgroundColor"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.99f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (elevated) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CardShapes.glassCard,
                        ambientColor = Color.Black.copy(alpha = 0.2f)
                    )
                } else Modifier
            )
            .clip(CardShapes.glassCard)
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🌟 Premium Chat Item Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ChatItemCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> extendedColors.accent.copy(alpha = 0.15f)
            isPressed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> Color.Transparent
        },
        label = "chatItemBackground"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.995f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "chatItemScale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .background(backgroundColor)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick ?: {}
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏷️ Premium Settings Item Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SettingsItemCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            extendedColors.inputBackground
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "settingsItemBackground"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShapes.glassCardSmall)
            .background(backgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}
