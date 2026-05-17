package com.Kelasor.app.ui.screens.mosbat_elm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.Kelasor.app.domain.model.Course
import com.Kelasor.app.ui.theme.DanaFontFamily
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.util.toPersianNumbers
import com.Kelasor.app.util.toPersianPrice
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademyAnalyticsScreen(
    institutionId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCourseDetail: (String) -> Unit,
    viewModel: AcademyPublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    var selectedCourseForDetail by remember { mutableStateOf<Course?>(null) }

    LaunchedEffect(institutionId) {
        viewModel.loadAcademyProfile(institutionId)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "آمار و درآمد آکادمی",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = extendedColors.accent)
                }
                return@Scaffold
            }

            val institution = state.institution
            if (institution == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "آکادمی یافت نشد",
                        fontFamily = DanaFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Scaffold
            }

            val courses = state.courses
            val totalViews = institution.totalViews
            val totalClicks = institution.totalClicks
            val totalRevenue = institution.totalRevenue ?: courses.sumOf { ((it.priceRials * (100 - it.discountPercentage)) / 100) * it.studentCount }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Private lock notice
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(extendedColors.accent.copy(alpha = 0.08f))
                            .border(1.dp, extendedColors.accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "خصوصی",
                            tint = extendedColors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "این صفحه گزارشات مالی و عملکرد آکادمی کاملاً خصوصی است و فقط برای شما به عنوان مالک موسسه نمایش داده می‌شود.",
                            fontFamily = DanaFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Grid of 4 main metrics
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "درآمد کل",
                                value = if (totalRevenue > 0) "${totalRevenue.toPersianPrice()} ریال" else "رایگان",
                                icon = Icons.Rounded.TrendingUp,
                                gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "کل دانشجویان",
                                value = institution.studentCount.toString().toPersianNumbers(),
                                icon = Icons.Rounded.People,
                                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "بازدید کل",
                                value = totalViews.toString().toPersianNumbers(),
                                icon = Icons.Rounded.Visibility,
                                gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFBA68C8))
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "کلیک کل",
                                value = totalClicks.toString().toPersianNumbers(),
                                icon = Icons.Rounded.TouchApp,
                                gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                            )
                        }
                    }
                }

                // Title for Course List
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = extendedColors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عملکرد به تفکیک دوره‌ها",
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                if (courses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز دوره‌ای در این آکادمی منتشر نشده است.",
                                fontFamily = DanaFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(courses) { course ->
                        CourseAnalyticsRow(
                            course = course,
                            onClick = { selectedCourseForDetail = course }
                        )
                    }
                }
            }

            // Interactive detailed BottomSheet
            selectedCourseForDetail?.let { course ->
                val courseStudents = course.studentCount
                val courseViews = course.viewCount
                val courseClicks = course.clickCount
                val courseRevenue = ((course.priceRials * (100 - course.discountPercentage)) / 100) * courseStudents

                ModalBottomSheet(
                    onDismissRequest = { selectedCourseForDetail = null },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Course Header
                        AsyncImage(
                            model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "",
                            contentDescription = course.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Text(
                            text = course.title,
                            fontFamily = DanaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "گزارش آماری و درآمد اختصاصی دوره",
                            fontFamily = DanaFontFamily,
                            fontSize = 12.sp,
                            color = extendedColors.accent,
                            fontWeight = FontWeight.Medium
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // 4 detailed rows
                        DetailedStatRow(
                            label = "درآمد حاصل از ثبت‌نام",
                            value = if (courseRevenue > 0) "${courseRevenue.toPersianPrice()} ریال" else "رایگان",
                            icon = Icons.Rounded.MonetizationOn,
                            tint = Color(0xFF4CAF50)
                        )
                        DetailedStatRow(
                            label = "تعداد کل ثبت‌نامی‌ها",
                            value = "${courseStudents.toString().toPersianNumbers()} دانشجو",
                            icon = Icons.Rounded.School,
                            tint = Color(0xFF2196F3)
                        )
                        DetailedStatRow(
                            label = "تعداد کل بازدیدها",
                            value = "${courseViews.toString().toPersianNumbers()} بار",
                            icon = Icons.Rounded.Visibility,
                            tint = Color(0xFF9C27B0)
                        )
                        DetailedStatRow(
                            label = "تعداد کلیک‌ها روی لینک ثبت‌نام",
                            value = "${courseClicks.toString().toPersianNumbers()} کلیک",
                            icon = Icons.Rounded.TouchApp,
                            tint = Color(0xFFFF9800)
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Visual Conversion Rate
                        val conversionRate = if (courseViews > 0) (courseStudents.toFloat() / courseViews.toFloat() * 100) else 0f
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "نرخ تبدیل بازدید به ثبت‌نام",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${String.format("%.1f", conversionRate).toPersianNumbers()}%",
                                    fontFamily = DanaFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.accent
                                )
                            }
                            LinearProgressIndicator(
                                progress = { conversionRate / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = extendedColors.accent,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                selectedCourseForDetail = null
                                onNavigateToCourseDetail(course.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "مشاهده صفحه دوره",
                                fontFamily = DanaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = DanaFontFamily,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                fontFamily = DanaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CourseAnalyticsRow(
    course: Course,
    onClick: () -> Unit
) {
    val courseRevenue = ((course.priceRials * (100 - course.discountPercentage)) / 100) * course.studentCount
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = com.Kelasor.app.util.UrlUtils.getFullUrl(course.posterUrl) ?: "",
                contentDescription = course.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = course.title,
                    fontFamily = DanaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ثبت‌نامی: ${course.studentCount.toString().toPersianNumbers()}",
                        fontFamily = DanaFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Text(
                        text = if (courseRevenue > 0) "${courseRevenue.toPersianPrice()} ریال" else "رایگان",
                        fontFamily = DanaFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowBackIosNew,
                contentDescription = "جزئیات",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun DetailedStatRow(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontFamily = DanaFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            fontFamily = DanaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = tint
        )
    }
}
