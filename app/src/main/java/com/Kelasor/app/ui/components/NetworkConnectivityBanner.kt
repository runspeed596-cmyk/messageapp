package com.Kelasor.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.Kelasor.app.data.websocket.ConnectionState
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 App Connection State — Unified State for UI
// ═══════════════════════════════════════════════════════════════════════════════

enum class AppConnectionState {
    CONNECTED,
    CONNECTING,
    WAITING_FOR_NETWORK,
    UPDATING,
    DISCONNECTED
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📡 Telegram-Style Network Connectivity Banner
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun NetworkConnectivityBanner(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    // Backward compatibility: convert boolean to state
    val appState: AppConnectionState = if (isConnected) AppConnectionState.CONNECTED else AppConnectionState.WAITING_FOR_NETWORK
    TelegramStyleConnectionBanner(
        appConnectionState = appState,
        modifier = modifier
    )
}

@Composable
fun TelegramStyleConnectionBanner(
    appConnectionState: AppConnectionState,
    modifier: Modifier = Modifier
) {
    var showConnectedBriefly by remember { mutableStateOf(false) }
    var previousState by remember { mutableStateOf(appConnectionState) }
    // Track if we should show the "connected" success banner briefly
    LaunchedEffect(appConnectionState) {
        if (appConnectionState == AppConnectionState.CONNECTED && previousState != AppConnectionState.CONNECTED) {
            showConnectedBriefly = true
            delay(2000L)
            showConnectedBriefly = false
        }
        previousState = appConnectionState
    }
    val shouldShow: Boolean = appConnectionState != AppConnectionState.CONNECTED || showConnectedBriefly
    AnimatedVisibility(
        visible = shouldShow,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(200)),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(200)),
        modifier = modifier.zIndex(99f)
    ) {
        val bannerConfig: BannerConfig = when {
            showConnectedBriefly && appConnectionState == AppConnectionState.CONNECTED -> BannerConfig(
                gradient = Brush.horizontalGradient(listOf(Color(0xFF43A047), Color(0xFF66BB6A))),
                text = "متصل شد ✓",
                icon = BannerIcon.Static(Icons.Default.Check),
                textColor = Color.White
            )
            appConnectionState == AppConnectionState.CONNECTING -> BannerConfig(
                gradient = Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
                text = "در حال اتصال...",
                icon = BannerIcon.Spinning,
                textColor = Color.White
            )
            appConnectionState == AppConnectionState.UPDATING -> BannerConfig(
                gradient = Brush.horizontalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
                text = "به‌روزرسانی...",
                icon = BannerIcon.Spinning,
                textColor = Color.White
            )
            appConnectionState == AppConnectionState.WAITING_FOR_NETWORK -> BannerConfig(
                gradient = Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935))),
                text = "در انتظار اتصال به شبکه...",
                icon = BannerIcon.Static(Icons.Default.SignalWifiOff),
                textColor = Color.White
            )
            else -> BannerConfig(
                gradient = Brush.horizontalGradient(listOf(Color(0xFFB71C1C), Color(0xFFE53935))),
                text = "اتصال قطع شده",
                icon = BannerIcon.Static(Icons.Default.SignalWifiOff),
                textColor = Color.White
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerConfig.gradient)
                .heightIn(min = 24.dp)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                when (val icon = bannerConfig.icon) {
                    is BannerIcon.Static -> {
                        Icon(
                            imageVector = icon.vector,
                            contentDescription = null,
                            tint = bannerConfig.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    is BannerIcon.Spinning -> {
                        val infiniteTransition = rememberInfiniteTransition(label = "spin")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spinRotation"
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = bannerConfig.textColor,
                            modifier = Modifier
                                .size(16.dp)
                                .rotate(rotation)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = bannerConfig.text,
                    color = bannerConfig.textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Banner Config Models
// ═══════════════════════════════════════════════════════════════════════════════

private data class BannerConfig(
    val gradient: Brush,
    val text: String,
    val icon: BannerIcon,
    val textColor: Color
)

private sealed class BannerIcon {
    data class Static(val vector: androidx.compose.ui.graphics.vector.ImageVector) : BannerIcon()
    data object Spinning : BannerIcon()
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💀 Shimmer Loading Effect (Skeleton)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ShimmerLoadingBlock(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color(0x33FFFFFF),
        Color(0x66FFFFFF),
        Color(0x33FFFFFF)
    )
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, 0f)
    )
    Box(
        modifier = modifier
            .background(
                brush = brush,
                shape = RoundedCornerShape(8.dp)
            )
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// ❌ Error Overlay with Retry
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ConnectionErrorOverlay(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SignalWifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("تلاش مجدد")
            }
        }
    }
}
