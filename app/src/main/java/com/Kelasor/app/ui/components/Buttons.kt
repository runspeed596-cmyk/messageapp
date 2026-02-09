package com.Kelasor.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.GlassBorderLight
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.theme.VazirFontFamily

// ═══════════════════════════════════════════════════════════════════════════════
// 🔘 Premium Primary Button - Gradient Background with Glow
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    val backgroundBrush = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.gradientStart,
                extendedColors.gradientMiddle,
                extendedColors.gradientEnd
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = CardShapes.button,
                ambientColor = extendedColors.accentGlow,
                spotColor = extendedColors.accentGlow
            )
            .clip(CardShapes.button)
            .background(backgroundBrush)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MessageAppTypography.buttonText,
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔲 Premium Secondary Button - Glass Effect
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(56.dp)
            .clip(CardShapes.button)
            .background(extendedColors.glass)
            .drawBehind {
                // Glass border
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassBorderLight,
                            Color.Transparent
                        )
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) extendedColors.accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MessageAppTypography.buttonText,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Medium,
                color = if (enabled) extendedColors.accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⚪ Premium Text Button
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "textButtonScale"
    )
    
    Text(
        text = text,
        style = MessageAppTypography.buttonText,
        fontFamily = VazirFontFamily,
        fontWeight = FontWeight.Medium,
        color = if (enabled) {
            if (isPressed) extendedColors.accent.copy(alpha = 0.7f)
            else extendedColors.accent
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💊 Premium Filter Chip / Pill Button (like "همه", "گروه‌ها", etc.)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "chipScale"
    )
    
    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.gradientStart,
                extendedColors.accent
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                extendedColors.chipBackground,
                extendedColors.chipBackground
            )
        )
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .height(40.dp)
            .shadow(
                elevation = if (isSelected) 8.dp else 0.dp,
                shape = CardShapes.chip,
                ambientColor = if (isSelected) extendedColors.accentGlow else Color.Transparent
            )
            .clip(CardShapes.chip)
            .background(backgroundBrush)
            .then(
                if (!isSelected) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = extendedColors.inputBorder,
                            cornerRadius = CornerRadius(20.dp.toPx()),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MessageAppTypography.chatTime.copy(fontWeight = FontWeight.Medium),
            fontFamily = VazirFontFamily,
            color = if (isSelected) extendedColors.chipTextSelected else extendedColors.chipText
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ⭕ Premium Icon Button
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp,
    hasBackground: Boolean = false
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CardShapes.buttonPill)
            .then(
                if (hasBackground) {
                    Modifier.background(extendedColors.inputBackground)
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📤 Send Button with Gradient
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sendButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = CardShapes.sendButton,
                ambientColor = extendedColors.accentGlow
            )
            .clip(CardShapes.sendButton)
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            extendedColors.gradientStart,
                            extendedColors.accent
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Send",
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(22.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Glowing Icon Button (for Send button with gradient glow)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GlowingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glowingButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = CardShapes.sendButton,
                ambientColor = extendedColors.accentGlow,
                spotColor = extendedColors.accentGlow
            )
            .clip(CardShapes.sendButton)
            .background(
                brush = if (enabled) {
                    Brush.linearGradient(
                        colors = listOf(
                            extendedColors.gradientStart,
                            extendedColors.accent
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(22.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📅 Date Separator for Chat
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun DateSeparator(
    dateText: String,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CardShapes.chip)
                .background(extendedColors.inputBackground)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = dateText,
                style = MessageAppTypography.chatTime,
                fontFamily = VazirFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔙 Back Button with Glass Effect
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun GlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val extendedColors = MessageAppTheme.extendedColors
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "backButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(CardShapes.buttonPill)
            .background(extendedColors.glass)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}
