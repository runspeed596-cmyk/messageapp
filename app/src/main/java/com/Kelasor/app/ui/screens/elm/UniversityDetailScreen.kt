package com.Kelasor.app.ui.screens.elm

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.data.University

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityDetailScreen(
    university: University,
    onBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF000000)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFF90CAF9)))
    }
    
    val textColor = if (isDark) Color.White else Color(0xFF0D47A1)
    val cardColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(university.name, color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Header Info
            DetailCard(backgroundColor = cardColor) {
               Row(verticalAlignment = Alignment.CenterVertically) {
                   Box(Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF00E5FF))) {
                       Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.fillMaxSize().padding(12.dp), tint = Color.Black)
                   }
                   Spacer(Modifier.width(16.dp))
                   Column {
                       Text(university.name, color = textColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                       Text(university.type, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                   }
               }
            }
            
            Spacer(Modifier.height(16.dp))

            // Main Statistics Grid
            Row(Modifier.fillMaxWidth()) {
                StatBox(modifier = Modifier.weight(1f), label = "رتبه ایران", value = university.iranRank.toString(), icon = Icons.Default.Info, color = Color(0xFFFFD600), textColor = textColor, cardColor = cardColor)
                Spacer(Modifier.width(16.dp))
                StatBox(modifier = Modifier.weight(1f), label = "رتبه جهان", value = university.worldRank.toString(), icon = Icons.Default.Public, color = Color(0xFF00E676), textColor = textColor, cardColor = cardColor)
            }
            
            Spacer(Modifier.height(16.dp))

            // More Stats
            DetailCard(backgroundColor = cardColor) {
                InfoRow(label = "وزارت مربوطه", value = university.ministry, textColor = textColor)
                HorizontalDivider(color = textColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(label = "سال تأسیس", value = university.establishmentYear, textColor = textColor)
                HorizontalDivider(color = textColor.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))
                InfoRow(label = "تعداد دانشجو", value = "${university.studentCount} نفر", textColor = textColor)
            }

            Spacer(Modifier.height(16.dp))
            
            // Research Stats
            Row(Modifier.fillMaxWidth()) {
                StatBox(modifier = Modifier.weight(1f), label = "تعداد مقالات", value = university.paperCount.toString(), icon = Icons.Default.Article, color = Color(0xFF2979FF), textColor = textColor, cardColor = cardColor)
                Spacer(Modifier.width(16.dp))
                StatBox(modifier = Modifier.weight(1f), label = "تعداد مجلات", value = university.journalCount.toString(), icon = Icons.Default.Article, color = Color(0xFFE91E63), textColor = textColor, cardColor = cardColor)
            }

            Spacer(Modifier.height(16.dp))

            // Faculties & Facilities
            SectionHeader(title = "دانشکده‌ها", textColor = textColor)
            DetailCard(backgroundColor = cardColor) {
                FlowRow(items = university.faculties, textColor = textColor)
            }

            Spacer(Modifier.height(16.dp))
            
            SectionHeader(title = "رشته‌ها", textColor = textColor)
            DetailCard(backgroundColor = cardColor) {
                FlowRow(items = university.majors, textColor = textColor)
            }

            Spacer(Modifier.height(16.dp))
            
            SectionHeader(title = "امکانات", textColor = textColor)
            DetailCard(backgroundColor = cardColor) {
                FlowRow(items = university.facilities, textColor = textColor)
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailCard(backgroundColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color, textColor: Color, cardColor: Color) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, color = textColor.copy(alpha = 0.6f), fontSize = 12.sp)
            Text(value, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, textColor: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = textColor.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
        Text(value, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SectionHeader(title: String, textColor: Color) {
    Text(
        text = title,
        color = textColor,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(items: List<String>, textColor: Color) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor.copy(alpha = 0.05f))
                    .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(item, color = textColor, fontSize = 12.sp)
            }
        }
    }
}
