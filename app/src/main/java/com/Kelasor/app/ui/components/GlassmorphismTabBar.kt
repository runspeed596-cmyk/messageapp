package com.Kelasor.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.Kelasor.app.ui.theme.MessageAppTheme

/**
 * Telegram-style glassmorphism pill tab bar.
 * Frosted glass container with an animated sliding pill indicator.
 */
@Composable
fun GlassmorphismTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabCounts: List<Int> = emptyList()
) {
    val extendedColors = MessageAppTheme.extendedColors
    val containerShape = RoundedCornerShape(14.dp)
    val pillShape = RoundedCornerShape(11.dp)
    val tabCount = tabs.size

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Glass container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = containerShape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.05f)
                )
                .clip(containerShape)
                .background(extendedColors.tabBarGlass)
                .border(
                    width = 0.5.dp,
                    color = extendedColors.tabBarGlassBorder,
                    shape = containerShape
                )
        ) {
            val scrollState = rememberScrollState()
            
            // Smoothly auto-scroll to center target tab pill
            LaunchedEffect(selectedIndex) {
                if (selectedIndex >= 0 && selectedIndex < tabs.size) {
                    val targetScroll = (selectedIndex * 110) - 100
                    scrollState.animateScrollTo(maxOf(0, targetScroll))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 3.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = index == selectedIndex

                    // Animated text color
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) extendedColors.tabActiveText
                        else extendedColors.tabInactiveText,
                        animationSpec = tween(250),
                        label = "tabTextColor"
                    )

                    // Animated background for the selected pill
                    val pillAlpha by animateDpAsState(
                        targetValue = if (isSelected) 1.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "pillAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .height(42.dp)
                            .padding(horizontal = 4.dp)
                            .clip(pillShape)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = pillShape,
                                            ambientColor = extendedColors.accent.copy(alpha = 0.15f),
                                            spotColor = extendedColors.accent.copy(alpha = 0.1f)
                                        )
                                        .background(extendedColors.tabIndicator)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(index) }
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = title,
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                            // Unread count badge
                            val count = tabCounts.getOrElse(index) { 0 }
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            if (isSelected) extendedColors.accent
                                            else extendedColors.accent.copy(alpha = 0.7f)
                                        )
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (count > 999) "999+" else count.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    } // end CompositionLocalProvider
}
