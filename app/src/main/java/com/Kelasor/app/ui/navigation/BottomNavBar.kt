package com.Kelasor.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.Kelasor.app.R
import com.Kelasor.app.ui.components.UnreadBadge
import com.Kelasor.app.ui.theme.MessageAppTheme

// ═══════════════════════════════════════════════════════════════════════════════
// 🎯 Animated Bottom Navigation Bar
// ═══════════════════════════════════════════════════════════════════════════════

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: Int,   // Changed to Int for Drawable Resource ID
    val unselectedIcon: Int, // Changed to Int for Drawable Resource ID
    val badgeCount: Int = 0
) {
    data object Bazaar : BottomNavItem(
        route = Routes.Bazaar.route,
        title = "بازار",
        selectedIcon = R.drawable.ic_shop,
        unselectedIcon = R.drawable.ic_shop
    )

    data object Elm : BottomNavItem(
        route = Routes.Elm.route,
        title = "علم+",
        selectedIcon = R.drawable.ic_science,
        unselectedIcon = R.drawable.ic_science
    )

    data object Home : BottomNavItem(
        route = Routes.Home.route,
        title = "خانه",
        selectedIcon = R.drawable.ic_home,
        unselectedIcon = R.drawable.ic_home
    )

    class Messaging(count: Int) : BottomNavItem(
        route = Routes.Messaging.route,
        title = "پیام‌رسان",
        selectedIcon = R.drawable.ic_chat,
        unselectedIcon = R.drawable.ic_chat,
        badgeCount = count
    )

    data object Treasure : BottomNavItem(
        route = Routes.Treasure.route,
        title = "گنج",
        selectedIcon = R.drawable.ic_treasure,
        unselectedIcon = R.drawable.ic_treasure
    )
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemClick: (String) -> Unit,
    unreadMessageCount: Int,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Items in Visual Left-to-Right order: Bazaar, Elm, Home, Messaging, Treasure
    val items = listOf(
        BottomNavItem.Bazaar,
        BottomNavItem.Elm,
        BottomNavItem.Home,
        BottomNavItem.Messaging(unreadMessageCount),
        BottomNavItem.Treasure
    )

    // Force LTR for layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp) // Adjusted floating margin
                .height(72.dp) // Fixed height space for sleeker look
        ) {
            // Liquid Glass / Dark Gradient Background
            val isDark = isSystemInDarkTheme()
            val glassBackground = if (isDark) {
                 androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF24243E).copy(alpha = 0.95f),
                        Color(0xFF0F0C29).copy(alpha = 0.95f)
                    )
                )
            } else {
                 androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF3F4F6).copy(alpha = 0.98f), // Almost opaque light gray
                        Color(0xFFFFFFFF).copy(alpha = 0.95f)  // Almost opaque white
                    )
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(
                        elevation = if (isDark) 16.dp else 12.dp, // Increased elevation for light mode
                        shape = RoundedCornerShape(50.dp),
                        spotColor = if (isDark) extendedColors.accent.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.15f) // Darker shadow for light mode
                    )
                    .background(
                        brush = glassBackground,
                        shape = RoundedCornerShape(50.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = if (isDark) {
                                listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)
                            } else {
                                listOf(Color.White, Color(0xFFE5E7EB)) // Solid border for visibility
                            }
                        ),
                        shape = RoundedCornerShape(50.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                    // Removed SpaceEvenly to rely on weights for exact touch targets
                ) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val isMessaging = item is BottomNavItem.Messaging
                        val isMessagingActive = isMessaging && (currentRoute == Routes.ChatList.route || 
                                                               currentRoute == Routes.Groups.route || 
                                                               currentRoute == Routes.Channels.route ||
                                                               currentRoute == Routes.Messaging.route)
                        val selected = isSelected || isMessagingActive
                        
                        AnimatedBottomNavItem(
                            item = item,
                            isSelected = selected,
                            onClick = { 
                                // Intercept Elm tab — show "under development" toast
                                if (item is BottomNavItem.Elm) {
                                    android.widget.Toast.makeText(context, "در حال توسعه", android.widget.Toast.LENGTH_SHORT).show()
                                    return@AnimatedBottomNavItem
                                }
                                // Direct click handling
                                if (item is BottomNavItem.Messaging && !selected) {
                                     onItemClick(Routes.Messaging.route)
                                } else {
                                     onItemClick(item.route)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = MessageAppTheme.extendedColors
    val interactionSource = remember { MutableInteractionSource() }
    // Enhanced scale with bouncy spring
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    // Subtle rotation on selection
    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 0f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )
    // Badge pop-in
    val badgeScale by animateFloatAsState(
        targetValue = if (item.badgeCount > 0) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "badge_scale"
    )
    // Using Theme colors
    val indicatorColor = when (item) {
        is BottomNavItem.Messaging -> Color(0xFF00897B) // Teal/Green
        is BottomNavItem.Elm -> Color(0xFF1E88E5)       // Blue
        is BottomNavItem.Treasure -> Color(0xFF8E24AA)  // Purple
        else -> extendedColors.accent                   // Default Gold/Accent
    }
    val iconSelectedColor = Color.White
    val iconUnselectedColor = extendedColors.navItemInactive
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
         // Active Indicator - Solid Squircle with spring animation
         androidx.compose.animation.AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(150)),
            exit = scaleOut(
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(120))
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .shadow(8.dp, RoundedCornerShape(18.dp), spotColor = indicatorColor)
            )
        }
        // Icon with Painter
        Box(
            modifier = Modifier
                .scale(scale)
                .then(
                    Modifier.graphicsLayer {
                        rotationZ = rotation
                    }
                )
        ) {
            Icon(
                painter = painterResource(if (isSelected) item.selectedIcon else item.unselectedIcon),
                contentDescription = item.title,
                tint = if(isSelected) iconSelectedColor else iconUnselectedColor,
                modifier = Modifier.size(28.dp)
            )
            if (item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .graphicsLayer {
                            scaleX = badgeScale
                            scaleY = badgeScale
                        }
                ) {
                    UnreadBadge(
                        count = item.badgeCount
                    )
                }
            }
        }
    }
}
