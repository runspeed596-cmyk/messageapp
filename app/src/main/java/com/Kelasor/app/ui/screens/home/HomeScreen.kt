package com.Kelasor.app.ui.screens.home

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.R
import com.Kelasor.app.util.Constants
import com.Kelasor.app.ui.navigation.Routes
import com.Kelasor.app.ui.theme.MessageAppTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Ultra-Premium Home Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Bell Icon on left
                IconButton(
                    onClick = { onNavigate(Routes.Notifications.route) },
                    modifier = Modifier.align(Alignment.CenterStart).size(40.dp)
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "اعلان‌ها", tint = if (isDarkTheme) Color.White else Color(0xFF1A1A2E))
                }
                
                Box(modifier = Modifier.align(Alignment.Center)) {
                    ShimmerText(text = "کلاسور", isDarkTheme = isDarkTheme)
                }
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedMeshBackground(isDarkTheme = isDarkTheme)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. Hero: Audience Counter (BIG, top of page)
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { -80 }
                    ) {
                        HeroAudienceSection(count = state.userCount, isDarkTheme = isDarkTheme)
                    }
                }
                // 2. Ad Carousel
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(tween(700, delayMillis = 200)) { 80 }
                    ) {
                        PremiumAdCarousel(banners = state.banners)
                    }
                }
                // 3. Mosaic Dashboard
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(700, delayMillis = 400)) + slideInVertically(tween(700, delayMillis = 400)) { 80 }
                    ) {
                        Column {
                            AnimatedSectionHeader(title = "ویترین", subtitle = "دسترسی سریع", isDarkTheme = isDarkTheme)
                            Spacer(Modifier.height(4.dp))
                            PremiumMosaicDashboard(onNavigate = onNavigate, isDarkTheme = isDarkTheme)
                        }
                    }
                }
                // 4. Discounts
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(700, delayMillis = 600)) + slideInVertically(tween(700, delayMillis = 600)) { 80 }
                    ) {
                        Column {
                            AnimatedSectionHeader(title = "دنیای تخفیف", subtitle = "پیشنهادهای طلایی", isDarkTheme = isDarkTheme)
                            Spacer(Modifier.height(4.dp))
                            PremiumDiscountGrid(discounts = state.discounts, onNavigate = onNavigate)
                        }
                    }
                }
                if (state.universities.isNotEmpty()) {
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🌟 Animated Mesh Gradient Background
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedMeshBackground(isDarkTheme: Boolean) {
    val inf = rememberInfiniteTransition(label = "meshBg")
    val t1 by inf.animateFloat(0f, 6.2832f, infiniteRepeatable(tween(25000, easing = LinearEasing), RepeatMode.Restart), label = "t1")
    val t2 by inf.animateFloat(6.2832f, 0f, infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Restart), label = "t2")
    val t3 by inf.animateFloat(0f, 6.2832f, infiniteRepeatable(tween(22000, easing = LinearEasing), RepeatMode.Restart), label = "t3")
    val t4 by inf.animateFloat(3.14f, 9.42f, infiniteRepeatable(tween(30000, easing = LinearEasing), RepeatMode.Restart), label = "t4")
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            brush = if (isDarkTheme) {
                Brush.verticalGradient(listOf(Color(0xFF060610), Color(0xFF0A0A18), Color(0xFF050510)))
            } else {
                Brush.verticalGradient(listOf(Color(0xFFF5F7FF), Color(0xFFEBEFF8), Color(0xFFF0F3FB)))
            }
        )
        if (isDarkTheme) {
            // Orb 1 — electric blue
            val o1x = w * 0.25f + w * 0.2f * cos(t1)
            val o1y = h * 0.15f + h * 0.1f * sin(t1 * 0.7f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF3B82F6).copy(alpha = 0.15f), Color.Transparent), center = Offset(o1x, o1y), radius = w * 0.5f), radius = w * 0.5f, center = Offset(o1x, o1y))
            // Orb 2 — violet
            val o2x = w * 0.75f + w * 0.15f * cos(t2)
            val o2y = h * 0.4f + h * 0.12f * sin(t2 * 0.8f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF8B5CF6).copy(alpha = 0.12f), Color.Transparent), center = Offset(o2x, o2y), radius = w * 0.45f), radius = w * 0.45f, center = Offset(o2x, o2y))
            // Orb 3 — teal
            val o3x = w * 0.5f + w * 0.18f * sin(t3)
            val o3y = h * 0.7f + h * 0.08f * cos(t3 * 0.6f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF06B6D4).copy(alpha = 0.10f), Color.Transparent), center = Offset(o3x, o3y), radius = w * 0.4f), radius = w * 0.4f, center = Offset(o3x, o3y))
            // Orb 4 — rose
            val o4x = w * 0.15f + w * 0.12f * sin(t4)
            val o4y = h * 0.55f + h * 0.06f * cos(t4 * 0.5f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFF43F5E).copy(alpha = 0.06f), Color.Transparent), center = Offset(o4x, o4y), radius = w * 0.3f), radius = w * 0.3f, center = Offset(o4x, o4y))
        } else {
            val o1x = w * 0.25f + w * 0.15f * cos(t1)
            val o1y = h * 0.15f + h * 0.08f * sin(t1 * 0.7f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFF93C5FD).copy(alpha = 0.22f), Color.Transparent), center = Offset(o1x, o1y), radius = w * 0.45f), radius = w * 0.45f, center = Offset(o1x, o1y))
            val o2x = w * 0.7f + w * 0.12f * cos(t2)
            val o2y = h * 0.45f + h * 0.1f * sin(t2)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFC4B5FD).copy(alpha = 0.20f), Color.Transparent), center = Offset(o2x, o2y), radius = w * 0.38f), radius = w * 0.38f, center = Offset(o2x, o2y))
            val o3x = w * 0.4f + w * 0.1f * sin(t3)
            val o3y = h * 0.8f + h * 0.05f * cos(t3)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFBCFE8).copy(alpha = 0.18f), Color.Transparent), center = Offset(o3x, o3y), radius = w * 0.3f), radius = w * 0.3f, center = Offset(o3x, o3y))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Shimmer Text
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ShimmerText(text: String, isDarkTheme: Boolean) {
    val inf = rememberInfiniteTransition(label = "shimmerText")
    val offset by inf.animateFloat(-300f, 600f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "so")
    val base = if (isDarkTheme) Color.White else Color(0xFF1A1A2E)
    val shimmer = if (isDarkTheme) Color(0xFF60A5FA) else Color(0xFF3B82F6)
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            brush = Brush.linearGradient(listOf(base, shimmer, base), start = Offset(offset, 0f), end = Offset(offset + 300f, 0f))
        )
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔢 Hero Audience Section (BIG)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun HeroAudienceSection(count: Long, isDarkTheme: Boolean) {
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A2E)
    val accentColor = MessageAppTheme.extendedColors.accent
    val inf = rememberInfiniteTransition(label = "heroGlow")
    val glowAlpha by inf.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ga")
    val ringScale by inf.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "rs")
    val ringAlpha by inf.animateFloat(0.15f, 0.35f, infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ra")
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "تعداد مخاطبان تا این لحظه",
                style = MaterialTheme.typography.titleSmall,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor)
            )
        }
        Spacer(Modifier.height(20.dp))
        // Big counter with glow
        Box(contentAlignment = Alignment.Center) {
            // Multi-ring glow
            if (isDarkTheme) {
                Canvas(modifier = Modifier.size(280.dp, 120.dp)) {
                    // Outer ring
                    drawOval(
                        brush = Brush.radialGradient(listOf(accentColor.copy(alpha = ringAlpha * 0.5f), Color.Transparent), center = Offset(size.width / 2, size.height / 2), radius = size.width * 0.7f),
                        size = Size(size.width, size.height),
                        topLeft = Offset(0f, 0f)
                    )
                    // Inner glow
                    drawOval(
                        brush = Brush.radialGradient(listOf(accentColor.copy(alpha = glowAlpha * 0.4f), Color.Transparent), center = Offset(size.width / 2, size.height / 2), radius = size.width * 0.4f),
                        size = Size(size.width * 0.7f, size.height * 0.7f),
                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f)
                    )
                }
            }
            // Counter number
            AnimatedContent(
                targetState = count,
                transitionSpec = {
                    slideInVertically { h -> h } + fadeIn() togetherWith slideOutVertically { h -> -h } + fadeOut()
                }, label = "counter"
            ) { targetCount ->
                Text(
                    text = "%,d".format(targetCount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    ),
                    color = textColor
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Decorative line
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, accentColor.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎠 Premium Ad Carousel
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumAdCarousel(banners: List<BannerItem>) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }
    Column(Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 36.dp),
            pageSpacing = 14.dp
        ) { page ->
            val item = banners[page]
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .graphicsLayer {
                        lerp(0.88f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)).also { s -> scaleX = s; scaleY = s }
                        alpha = lerp(0.5f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                    },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(Modifier.fillMaxSize()) {
                    if (item.imageUrl.isNotBlank()) {
                        val fullImageUrl = if (item.imageUrl.startsWith("http")) item.imageUrl
                        else Constants.BASE_URL.removeSuffix("/") + "/" + item.imageUrl.removePrefix("/")
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(fullImageUrl).crossfade(true).build(),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(item.colorStart), Color(item.colorEnd))))
                        ) {
                            Text(item.title, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                            .padding(16.dp)
                    ) {
                        Text(item.title, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        // Animated page dots
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            repeat(banners.size) { idx ->
                val isActive = pagerState.currentPage == idx
                val targetWidth = if (isActive) 24.dp else 8.dp
                val targetColor = if (isActive) MessageAppTheme.extendedColors.accent else Color.White.copy(alpha = 0.3f)
                val animWidth by animateFloatAsState(
                    targetValue = if (isActive) 24f else 8f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
                    label = "dw$idx"
                )
                val animColor by animateColorAsState(targetValue = targetColor, animationSpec = tween(300), label = "dc$idx")
                Box(
                    modifier = Modifier.padding(horizontal = 3.dp).height(8.dp).width(animWidth.dp).clip(RoundedCornerShape(4.dp)).background(animColor)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🧩 Premium Mosaic Dashboard
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumMosaicDashboard(onNavigate: (String) -> Unit, isDarkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(360.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left: World of Science (Tall)
        ShineCard(
            modifier = Modifier.weight(1.4f).fillMaxHeight(),
            gradientColors = listOf(Color(0xFF0D6EFD), Color(0xFF3B82F6), Color(0xFF1D4ED8)),
            onClick = { onNavigate(Routes.Elm.route) }
        ) {
            Box(Modifier.fillMaxSize()) {
                // Decorative circles
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.6f, center = Offset(size.width * 0.8f, size.height * 0.9f))
                    drawCircle(Color.White.copy(alpha = 0.03f), radius = size.width * 0.4f, center = Offset(size.width * 0.1f, size.height * 0.3f))
                }
                Icon(
                    imageVector = Icons.Outlined.Public,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(160.dp).align(Alignment.BottomEnd).offset(x = 25.dp, y = 25.dp)
                )
                Column(modifier = Modifier.align(Alignment.TopStart).padding(22.dp)) {
                    // Mini badge
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("🌍 جهان", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("جهان علم", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(6.dp))
                    Text("کاوش در دنیای دانش\nو دانشگاه‌ها", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f), lineHeight = 22.sp)
                }
                // Action button
                Box(
                    modifier = Modifier.align(Alignment.BottomStart).padding(22.dp).clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.2f)).border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ورود", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        // Right column: Fun + Events
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Fun Card
            ShineCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                gradientColors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706)),
                onClick = { onNavigate(Routes.Fun.route) }
            ) {
                Box(Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.08f), radius = size.minDimension * 0.5f, center = Offset(size.width * 0.85f, size.height * 0.15f))
                    }
                    Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF1C1917).copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("🎮", fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("سرگرمی", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1C1917), fontWeight = FontWeight.Bold)
                        Text("بازی کن!", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1C1917).copy(alpha = 0.65f))
                    }
                    Icon(Icons.Outlined.VideogameAsset, null, tint = Color(0xFF1C1917).copy(alpha = 0.6f), modifier = Modifier.size(40.dp).align(Alignment.BottomEnd).padding(12.dp))
                }
            }
            // Events Card
            ShineCard(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED), Color(0xFF6D28D9)),
                onClick = { onNavigate(Routes.Events.route) }
            ) {
                Box(Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(alpha = 0.06f), radius = size.minDimension * 0.55f, center = Offset(size.width * 0.9f, size.height * 0.8f))
                    }
                    Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                            Text("🏆", fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("قله علم", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("مسابقات و استارتاپ‌ها", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    }
                    Text("📅", fontSize = 28.sp, modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💎 Shine Card
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ShineCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    val inf = rememberInfiniteTransition(label = "shine")
    val shineOffset by inf.animateFloat(-500f, 1500f, infiniteRepeatable(tween(4000, easing = LinearEasing, delayMillis = 2000), RepeatMode.Restart), label = "so")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(gradientColors, start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)))
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f), Color.Transparent), start = Offset(0f, 0f), end = Offset(500f, 500f)), RoundedCornerShape(26.dp))
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.10f), Color.Transparent),
                        start = Offset(shineOffset, shineOffset * 0.6f),
                        end = Offset(shineOffset + 300f, shineOffset * 0.6f + 300f)
                    ),
                    size = size
                )
            }
            .clickable(onClick = onClick)
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏷️ Premium Discount Grid
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun PremiumDiscountGrid(discounts: List<DiscountItem>, onNavigate: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var copiedCode by remember { mutableStateOf<String?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    LaunchedEffect(copiedCode) {
        if (copiedCode != null) { delay(2000); copiedCode = null }
    }
    Column(Modifier.padding(horizontal = 20.dp)) {
        discounts.chunked(2).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    val cardGradient = if (isDarkTheme) listOf(Color(0xFF16162A), Color(0xFF1E1E36), Color(0xFF16162A))
                    else listOf(Color(0xFFFFFFFF), Color(0xFFF8F9FF), Color(0xFFFFFFFF))
                    ShineCard(
                        modifier = Modifier.weight(1f).height(155.dp),
                        gradientColors = cardGradient,
                        onClick = { onNavigate(Routes.Treasure.route) }
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(item.title, color = if (isDarkTheme) Color.White else Color(0xFF1A1A2E), style = MaterialTheme.typography.bodyMedium, maxLines = 1, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                // Golden percentage with glow
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDarkTheme) {
                                        Canvas(modifier = Modifier.size(60.dp, 40.dp)) {
                                            drawOval(Brush.radialGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.25f), Color.Transparent), center = Offset(size.width / 2, size.height / 2), radius = size.width * 0.8f), size = size)
                                        }
                                    }
                                    Text("${item.percent}%", color = Color(0xFFFFD700), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                                }
                            }
                            if (item.code.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                        .background(if (isDarkTheme) Color.White.copy(alpha = 0.07f) else Color(0xFF1A1A2E).copy(alpha = 0.05f))
                                        .border(0.5.dp, if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color(0xFF1A1A2E).copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            val clip = android.content.ClipData.newPlainText("discount_code", item.code)
                                            clipboardManager.setPrimaryClip(clip)
                                            copiedCode = item.code
                                            android.widget.Toast.makeText(context, "کد تخفیف کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.code, color = Color(0xFF10B981), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = if (copiedCode == item.code) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = "کپی",
                                        tint = if (copiedCode == item.code) Color(0xFF10B981) else if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF1A1A2E).copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                if (rowItems.size == 1) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Animated Section Header
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AnimatedSectionHeader(title: String, subtitle: String, isDarkTheme: Boolean) {
    val titleColor = if (isDarkTheme) Color.White else Color(0xFF1A1A2E)
    val subtitleColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B)
    val accentColor = MessageAppTheme.extendedColors.accent
    var isBarVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isBarVisible = true }
    val barHeight by animateFloatAsState(
        targetValue = if (isBarVisible) 28f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "bh"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(4.dp).height(barHeight.dp).clip(RoundedCornerShape(2.dp)).background(accentColor).shadow(12.dp, spotColor = accentColor))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = titleColor, fontWeight = FontWeight.Black)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = subtitleColor)
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ University List
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun UniversityList(universities: List<UniversityItem>, onNavigate: (String) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(universities) { uni ->
            ShineCard(
                modifier = Modifier.width(160.dp).height(100.dp),
                gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF047857)),
                onClick = { onNavigate(Routes.Elm.route) }
            ) {
                Box(Modifier.fillMaxSize().padding(12.dp)) {
                    Text(uni.name, color = Color.White, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
