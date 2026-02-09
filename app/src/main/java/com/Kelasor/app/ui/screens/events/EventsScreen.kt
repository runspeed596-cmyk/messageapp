package com.Kelasor.app.ui.screens.events

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.data.remote.dto.ElmEventType
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.ElmViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ElmViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val extendedColors = MessageAppTheme.extendedColors
    var activeTab by remember { mutableStateOf(0) } // 0: Competitions, 1: Startups, 2: Congresses
    
    // Sub-states for forms
    var showIdeaForm by remember { mutableStateOf(false) }
    var showReportForm by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Luxury Background Glows
        BackgroundGlows(extendedColors.accent)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // Premium Header
            ElmHeader(onNavigateBack)

            // Dynamic Content Tabs
            ElmTabs(
                selectedTab = activeTab,
                onTabSelected = { activeTab = it }
            )

            Spacer(Modifier.height(24.dp))

            // Main Content based on activeTab
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(500))
                },
                label = "MainContent"
            ) { tab ->
                when (tab) {
                    0 -> CompetitionsSection(state.competitions)
                    1 -> StartupsSection(state.startups, onPitchIdea = { showIdeaForm = true })
                    2 -> CongressesSection(state.congresses, onReportEvent = { showReportForm = true })
                }
            }

            Spacer(Modifier.height(120.dp))
        }

        // Floating Action Button at BOTTOM-RIGHT (above navbar)
        // In lines 97-108: Changed BottomEnd to BottomStart for RTL support (Start = Right in RTL)
        SmallAddButton(
            modifier = Modifier
                .align(Alignment.BottomStart) // Start = Right in RTL
                .padding(start = 24.dp, bottom = 100.dp), // Above navbar
            onClick = {
                when (activeTab) {
                    0 -> showReportForm = true // Competitions - report a competition
                    1 -> showIdeaForm = true   // Startups - pitch an idea
                    else -> showReportForm = true // Congresses - report an event
                }
            }
        )
        
        // Error Snackbar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.onError)
            }
        }
        
        // Success Snackbar
        if (state.submissionMessage != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp, start = 16.dp, end = 16.dp), // Above navbar + FAB
                containerColor = Color(0xFF00C853).copy(alpha = 0.9f)
            ) {
                Text(state.submissionMessage!!, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal Sheets for Forms
    if (showIdeaForm) {
        IdeaSubmissionSheet(
            onDismiss = { showIdeaForm = false },
            onSubmit = { title, desc, contact ->
                viewModel.submitIdea(title, desc, contact)
                showIdeaForm = false
            }
        )
    }

    if (showReportForm) {
        ReportEventSheet(
            onDismiss = { showReportForm = false },
            onSubmit = { title, desc, date, loc, link ->
                val type = if (activeTab == 2) ElmEventType.CONGRESS else ElmEventType.COMPETITION
                viewModel.reportEvent(title, desc, date, loc, link, type)
                showReportForm = false
            }
        )
    }

    // Clear submission message after a while
    LaunchedEffect(state.submissionMessage) {
        if (state.submissionMessage != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearSubmissionMessage()
        }
    }
}

@Composable
fun BackgroundGlows(accent: Color) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = if (isDark) 0.1f else 0.05f,
        targetValue = if (isDark) 0.25f else 0.15f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "GlowAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-150).dp, y = (-150).dp)
                .blur(120.dp)
                .background(accent.copy(alpha = glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .blur(100.dp)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha * 0.4f), CircleShape)
        )
    }
}

@Composable
fun ElmHeader(onBack: () -> Unit) {
    val extendedColors = MessageAppTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(extendedColors.glass, CircleShape)
                .border(1.dp, extendedColors.glassBorder, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
        }

        Text(
            "قله علم",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(extendedColors.glass, CircleShape)
                .border(1.dp, extendedColors.glassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ElmTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        "مسابقات" to Icons.Default.EmojiEvents,
        "استارتاپ‌ها" to Icons.Default.RocketLaunch,
        "کنگره‌ها" to Icons.Default.CastForEducation
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tabs.size) { index ->
            val isSelected = selectedTab == index
            val extendedColors = MessageAppTheme.extendedColors
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) extendedColors.accent.copy(alpha = 0.15f)
                        else extendedColors.glass
                    )
                    .border(
                        1.dp,
                        if (isSelected) extendedColors.accent.copy(alpha = 0.4f)
                        else extendedColors.glassBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        tabs[index].second,
                        null,
                        tint = if (isSelected) extendedColors.accent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tabs[index].first,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun CompetitionsSection(competitions: List<ElmEventDto>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle("مسابقات علمی روز", "برترین چالش‌های کشوری و بین‌المللی")
        
        competitions.forEach { event ->
            ElmEventCard(event)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun StartupsSection(startups: List<ElmEventDto>, onPitchIdea: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle("اکوسیستم استارتاپی", "ایده‌پردازی، جذب سرمایه و رویدادهای دانشگاهی")
        
        // Pitch Idea CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
                    )
                )
                .clickable { onPitchIdea() }
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("ایده‌ای داری؟", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 22.sp)
                    Text("ایده‌ات رو مطرح کن و از حامیان علم حمایت مالی بگیر", color = Color.Black.copy(alpha = 0.7f), fontSize = 13.sp)
                }
                Icon(Icons.Default.Lightbulb, null, tint = Color.Black, modifier = Modifier.size(40.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        
        Text(
            "استارتاپ ویکندها", 
            color = MaterialTheme.colorScheme.onBackground, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        startups.forEach { event ->
            ElmEventCard(event)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun CongressesSection(congresses: List<ElmEventDto>, onReportEvent: () -> Unit) {
    val extendedColors = MessageAppTheme.extendedColors
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle("کنگره‌های علمی", "تازه‌ترین یافته‌های پژوهشی در ایران و جهان")
        
        // Report Event Reward Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = extendedColors.glass),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFFFD600).copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.clickable { onReportEvent() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Color(0xFFFFD600).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddBusiness, null, tint = Color(0xFFFFD600))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("ثبت کنگره یا رویداد علمی", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Text("اطلاع‌رسانی کن و سکه هدیه بگیر (کیف پول)", color = Color(0xFFFF9100), fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowForwardIos, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
            }
        }

        Spacer(Modifier.height(24.dp))
        
        congresses.forEach { event ->
            val isInternational = event.isExternal
            ElmEventCard(event, badgeText = if (isInternational) "بین‌المللی" else "ملی", badgeColor = if (isInternational) Color(0xFFE91E63) else Color(0xFF00E5FF))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ElmEventCard(event: ElmEventDto, badgeText: String? = null, badgeColor: Color = Color(0xFF00E5FF)) {
    val extendedColors = MessageAppTheme.extendedColors
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = extendedColors.glass),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            event.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (badgeText != null) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = badgeColor.copy(alpha = 0.15f),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f))
                            ) {
                                Text(badgeText, color = badgeColor, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        event.organizer ?: "برگزارکننده نامشخص",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                if (event.reward != null) {
                    Icon(Icons.Default.Redeem, null, tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text(event.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), fontSize = 13.sp, maxLines = 2)
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(event.date, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(event.location, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            
            if (event.reward != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().background(extendedColors.accent.copy(alpha = 0.05f), RoundedCornerShape(12.dp)).padding(10.dp)
                ) {
                    Text("جایزه/حمایت: ${event.reward}", color = if (isSystemInDarkTheme()) Color(0xFFFFD600) else Color(0xFFF57C00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(subtitle, color = MessageAppTheme.extendedColors.accent, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun SmallAddButton(modifier: Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = MessageAppTheme.extendedColors.accent,
        contentColor = Color.White,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeaSubmissionSheet(onDismiss: () -> Unit, onSubmit: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)) }
    ) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding()) {
            Text("ثبت ایده استارتاپی", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("ایده شما محرمانه باقی می‌ماند و تنها برای حامیان ارسال می‌شود", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
            
            ElmTextField(value = title, onValueChange = { title = it }, label = "عنوان طرح/ایده")
            Spacer(Modifier.height(16.dp))
            ElmTextField(value = desc, onValueChange = { desc = it }, label = "توضیحات مختصر و مزیت رقابتی", singleLine = false)
            Spacer(Modifier.height(16.dp))
            ElmTextField(value = contact, onValueChange = { contact = it }, label = "شماره تماس یا آیدی تلگرام")
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { if (title.isNotEmpty()) onSubmit(title, desc, contact) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MessageAppTheme.extendedColors.accent,
                    contentColor = Color.White
                )
            ) {
                Text("ارائه ایده برای داوری و حمایت", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportEventSheet(onDismiss: () -> Unit, onSubmit: (String, String, String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

    val extendedColors = MessageAppTheme.extendedColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(24.dp).navigationBarsPadding().verticalScroll(rememberScrollState())) {
            Text("گزارش/ثبت رویداد علمی", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("با معرفی رویدادهای معتبر، به پویایی فضای علمی کمک کنید و امتیاز بگیرید", color = if (isSystemInDarkTheme()) Color(0xFFFFD600) else Color(0xFFF57C00), fontSize = 12.sp)
            Spacer(Modifier.height(24.dp))
            
            ElmTextField(value = title, onValueChange = { title = it }, label = "نام کنگره/همایش")
            Spacer(Modifier.height(12.dp))
            ElmTextField(value = date, onValueChange = { date = it }, label = "تاریخ برگزاری")
            Spacer(Modifier.height(12.dp))
            ElmTextField(value = loc, onValueChange = { loc = it }, label = "مکان (دانشگاه/شهر)")
            Spacer(Modifier.height(12.dp))
            ElmTextField(value = link, onValueChange = { link = it }, label = "لینک وب‌سایت یا اطلاعیه رسمی")
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = { if (title.isNotEmpty()) onSubmit(title, "", date, loc, link) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black)
            ) {
                Text("ثبت برای بررسی و دریافت امتیاز", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ElmTextField(value: String, onValueChange: (String) -> Unit, label: String, singleLine: Boolean = true) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 5,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedIndicatorColor = MessageAppTheme.extendedColors.accent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
