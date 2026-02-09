package com.Kelasor.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.CardShapes
import com.Kelasor.app.ui.theme.GlassBorderLight
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.MessageAppTypography

// ═══════════════════════════════════════════════════════════════════════════════
// 🔍 Premium Floating Search Bar Component
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.search_placeholder),
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val extendedColors = MessageAppTheme.extendedColors
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.01f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "searchScale"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = if (isFocused) 12.dp else 6.dp,
                shape = CardShapes.searchBar,
                ambientColor = if (isFocused) extendedColors.accentGlow else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.2f)
            )
            .clip(CardShapes.searchBar)
            .background(extendedColors.inputBackground)
            .drawBehind {
                if (isFocused) {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                extendedColors.inputBorderFocused.copy(alpha = 0.5f),
                                extendedColors.accent.copy(alpha = 0.3f)
                            )
                        ),
                        cornerRadius = CornerRadius(32.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                } else {
                    drawRoundRect(
                        color = extendedColors.inputBorder,
                        cornerRadius = CornerRadius(32.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        AnimatedVisibility(
            visible = showBackButton,
            enter = slideInHorizontally { -it } + fadeIn(),
            exit = slideOutHorizontally { -it } + fadeOut()
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Search icon
        if (!showBackButton) {
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isFocused) extendedColors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // Text field
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = MessageAppTypography.inputHint.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(extendedColors.accent),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MessageAppTypography.inputHint,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )
        
        // Clear button
        AnimatedVisibility(
            visible = query.isNotEmpty(),
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = androidx.compose.ui.res.stringResource(com.Kelasor.app.R.string.clear_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Premium Header with Search Toggle
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumHeader(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: (() -> Unit)? = null
) {
    val extendedColors = MessageAppTheme.extendedColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        AnimatedVisibility(
            visible = !isSearchActive,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title
                Text(
                    text = title,
                    style = MessageAppTypography.appBarTitle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                
                // Search icon button
                PremiumIconButton(
                    icon = Icons.Default.Search,
                    onClick = onSearchToggle,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onBackground,
                    size = 44.dp,
                    iconSize = 24.dp
                )
                
                // Notification icon button
                if (onNotificationClick != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    PremiumIconButton(
                        icon = Icons.Default.Notifications,
                        onClick = onNotificationClick,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onBackground,
                        size = 44.dp,
                        iconSize = 24.dp
                    )
                }
            }
        }
        
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn() + slideInHorizontally { -it },
            exit = fadeOut() + slideOutHorizontally { -it }
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onSearch = onSearch,
                showBackButton = true,
                onBackClick = onSearchToggle
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Legacy Expandable Search Header (for compatibility)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ExpandableSearchHeader(
    title: String,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    showMenu: Boolean = false,
    onProfileClick: (() -> Unit)? = null
) {
    PremiumHeader(
        title = title,
        isSearchActive = isSearchActive,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onSearchToggle = onSearchToggle,
        onSearch = onSearch,
        modifier = modifier,
        onNotificationClick = null
    )
}
