package com.Kelasor.app.ui.screens.home

import android.graphics.Color.parseColor
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Ultra-Luxury Home Screen
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    val isDarkTheme = isSystemInDarkTheme()
    
    // Using Scaffold to provide the TopAppBar
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "کلاسور", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Match background
                    titleContentColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background, // Match Messaging background
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // Background Setup
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
        // LuxuryBackground removed for uniform look

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp), // Space for BottomBar
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Audience Count (Top)
            item {
                AudienceCounterSection(count = state.userCount, isDarkTheme = isDarkTheme)
            }

            // 2. Advertisement (Carousel)
            item {
                AdCarouselSection(banners = state.banners)
            }

            // 3. Mosaic Dashboard (Science + Fun + Events)
            item {
                SectionHeader(title = "ویترین", subtitle = "دسترسی سریع", isDarkTheme = isDarkTheme)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp) // Fixed height for mosaic
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: World of Science (Tall)
                    LuxuryGlassCard(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight(),
                        gradientStart = Color(0xFF1E88E5),
                        gradientEnd = Color(0xFF1565C0),
                        onClick = { onNavigate(Routes.Elm.route) }
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            // Globe Icon Background
                            Icon(
                                imageVector = Icons.Outlined.Public,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(160.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 40.dp, y = 40.dp)
                            )
                            
                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "جهان علم",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "کاوش در دنیای دانش\nو دانشگاه‌ها",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            
                            // Action Button styled
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("ورود", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // Right Column: Fun (Top) & Events (Bottom)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Fun Card
                        LuxuryGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            gradientStart = Color(0xFFFDD835),
                            gradientEnd = Color(0xFFF57F17),
                            onClick = { onNavigate(Routes.Fun.route) }
                        ) {
                            Box(Modifier.fillMaxSize().padding(16.dp)) {
                                Column(Modifier.align(Alignment.TopStart)) {
                                    Text(
                                        "سرگرمی",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "بازی کن!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Black.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.VideogameAsset,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier.size(32.dp).align(Alignment.BottomEnd)
                                )
                            }
                        }
                        
                        // Events Card (Mini)
                        LuxuryGlassCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            gradientStart = Color(0xFF9C27B0),
                            gradientEnd = Color(0xFF673AB7),
                            onClick = { onNavigate(Routes.Events.route) }
                        ) {
                            Box(Modifier.fillMaxSize().padding(16.dp)) {
                                Column(Modifier.align(Alignment.TopStart)) {
                                    Text(
                                        "قله علم",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "مسابقات، کنگره های علمی و استارتاپ ویکندها",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                                Text(
                                    "📅", 
                                    fontSize = 24.sp,
                                    modifier = Modifier.align(Alignment.BottomEnd)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Discount World
            item {
                SectionHeader(title = "دنیای تخفیف", subtitle = "پیشنهادهای طلایی", isDarkTheme = isDarkTheme)
                DiscountGrid(discounts = state.discounts, onNavigate = onNavigate)
            }

            // 5. University List
            if (state.universities.isNotEmpty()) {
                item { Spacer(Modifier.height(32.dp)) }

                // Featured Movies Header
            }
        }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Components
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun LuxuryBackground(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val colors = if (isDarkTheme) {
        listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E))
    } else {
        listOf(Color(0xFFF5F7FA), Color(0xFFC3CFE2), Color(0xFFE2EBF5)) // Platinum/Silver
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
        // Add animated orbs/glows if needed here
    }
}

@Composable
fun AudienceCounterSection(count: Long, isDarkTheme: Boolean) {
    val textColor = if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.8f)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "تعداد مخاطبان تا این لحظه",
            style = MaterialTheme.typography.labelLarge,
            color = MessageAppTheme.extendedColors.accent.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Animated Ticker Style Text
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            }, label = "counter"
        ) { targetCount ->
             Text(
                text = "%,d".format(targetCount),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = textColor,
                modifier = Modifier.graphicsLayer {
                    shadowElevation = if (isDarkTheme) 20f else 0f
                }
            )
        }
    }
}

@Composable
fun AdCarouselSection(banners: List<BannerItem>) {
    if (banners.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Auto-scroll
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
            contentPadding = PaddingValues(horizontal = 48.dp),
            pageSpacing = 16.dp
        ) { page ->
             val item = banners[page]
             val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
             
             Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(160.dp)
                     .graphicsLayer {
                         // Parallax/Scale Effect
                         lerp(
                             start = 0.85f,
                             stop = 1f,
                             fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                         ).also { scale ->
                             scaleX = scale
                             scaleY = scale
                         }
                         alpha = lerp(
                             start = 0.5f,
                             stop = 1f,
                             fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                         )
                     }
                     .clip(RoundedCornerShape(24.dp))
             ) {
                 // Display actual image from backend
                 if (item.imageUrl.isNotBlank()) {
                     val fullImageUrl = if (item.imageUrl.startsWith("http")) {
                         item.imageUrl
                     } else {
                         Constants.BASE_URL.removeSuffix("/") + "/" + item.imageUrl.removePrefix("/")
                     }
                     
                     AsyncImage(
                         model = ImageRequest.Builder(context)
                             .data(fullImageUrl)
                             .crossfade(true)
                             .build(),
                         contentDescription = item.title,
                         modifier = Modifier.fillMaxSize(),
                         contentScale = androidx.compose.ui.layout.ContentScale.Crop
                     )
                 } else {
                     // Fallback to gradient if no image
                     Box(
                         modifier = Modifier
                             .fillMaxSize()
                             .background(
                                 Brush.linearGradient(
                                     colors = listOf(Color(item.colorStart), Color(item.colorEnd))
                                 )
                             )
                     ) {
                         Text(
                             text = item.title,
                             color = Color.White,
                             style = MaterialTheme.typography.titleLarge,
                             fontWeight = FontWeight.Bold,
                             modifier = Modifier.align(Alignment.Center)
                         )
                     }
                 }
                 
                 // Title overlay at bottom
                 Box(
                     modifier = Modifier
                         .fillMaxWidth()
                         .align(Alignment.BottomCenter)
                         .background(
                             Brush.verticalGradient(
                                 colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                             )
                         )
                         .padding(16.dp)
                 ) {
                     Text(
                         text = item.title,
                         color = Color.White,
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold
                     )
                 }
             }
        }
    }
}

@Composable
fun LuxuryGlassCard(
    modifier: Modifier = Modifier,
    gradientStart: Color = Color.White.copy(alpha = 0.1f),
    gradientEnd: Color = Color.White.copy(alpha = 0.05f),
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradientStart, gradientEnd)
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(32.dp)
            )
            .clickable(onClick = onClick)
    ) {
        content()
    }
}

@Composable
fun EventCard(event: EventItem, onNavigate: (String) -> Unit) {
    LuxuryGlassCard(
        modifier = Modifier
            .width(140.dp) // Smaller width
            .height(160.dp), // Smaller height
        gradientStart = Color(0xFF9C27B0).copy(alpha = 0.8f),
        gradientEnd = Color(0xFF673AB7).copy(alpha = 0.8f),
        onClick = { onNavigate(Routes.Events.route) } // Navigate to Events
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                 Text("📅", fontSize = 20.sp)
            }
            
            Column {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    event.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun DiscountGrid(discounts: List<DiscountItem>, onNavigate: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var copiedCode by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(copiedCode) {
        if (copiedCode != null) {
            delay(2000)
            copiedCode = null
        }
    }
    
    // Simple 2x2 Grid
    Column(Modifier.padding(horizontal = 24.dp)) {
        discounts.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { item ->
                    LuxuryGlassCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
                        gradientStart = Color(0xFF263238),
                        gradientEnd = Color(0xFF37474F),
                        onClick = { onNavigate(Routes.Treasure.route) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    item.title,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    "${item.percent}%",
                                    color = Color(0xFFFFD700), // Gold
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            
                            // Discount Code with Copy Button
                            if (item.code.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            val clip = android.content.ClipData.newPlainText("discount_code", item.code)
                                            clipboardManager.setPrimaryClip(clip)
                                            copiedCode = item.code
                                            android.widget.Toast.makeText(context, "کد تخفیف کپی شد!", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.code,
                                        color = Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = if (copiedCode == item.code) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = "کپی کد",
                                        tint = if (copiedCode == item.code) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Fill empty space if odd number
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun UniversityList(universities: List<UniversityItem>, onNavigate: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(universities) { uni ->
            LuxuryGlassCard(
                modifier = Modifier
                    .width(160.dp)
                    .height(100.dp),
                gradientStart = Color(0xFF1B5E20),
                gradientEnd = Color(0xFF2E7D32),
                onClick = { onNavigate(Routes.Elm.route) }
            ) {
                Box(Modifier.fillMaxSize().padding(12.dp)) {
                    Column(Modifier.align(Alignment.Center)) {
                        Text(
                            uni.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String, isDarkTheme: Boolean) {
    val titleColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground
    val subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Indicator
        Box(
            modifier = Modifier
                .size(4.dp, 24.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MessageAppTheme.extendedColors.accent)
                .shadow(8.dp, spotColor = MessageAppTheme.extendedColors.accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = titleColor,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = subtitleColor
            )
        }
    }
}
