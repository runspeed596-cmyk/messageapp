package com.Kelasor.app.ui.screens.treasure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.MessageAppTheme

@Composable
fun TreasureScreen() {
    val extendedColors = MessageAppTheme.extendedColors
    
    // Mock Data
    val treasures = listOf(
        TreasureItem("کد تخفیف ۵۰٪", "دیجی‌کالا", Color(0xFFFFD700)),
        TreasureItem("اشتراک رایگان", "فیلیمو", Color(0xFFE0E0E0)), // Silver
        TreasureItem("تخفیف ۳۰٪", "اسنپ", Color(0xFFCD7F32)), // Bronze
        TreasureItem("بن خرید", "شهروند", Color(0xFFFFD700)),
        TreasureItem("هدیه ویژه", "کتابخانه", Color(0xFFE0E0E0)),
        TreasureItem("سفر رایگان", "علی‌بابا", Color(0xFFFFD700)),
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.navBarBackground)
    ) {
        TreasureBackground()
        
        Column(Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "دنیای گنج",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black
                )
                Text(
                    "تخفیف‌های طلایی شما",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(treasures) { item ->
                    TreasureCard(item)
                }
            }
        }
    }
}

data class TreasureItem(val title: String, val provider: String, val glowColor: Color)

@Composable
fun TreasureBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF263238),
                    Color(0xFF000000)
                ),
                radius = size.maxDimension
            )
        )
    }
}

@Composable
fun TreasureCard(item: TreasureItem) {
    Box(
        modifier = Modifier
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF37474F),
                        Color(0xFF263238)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(item.glowColor, Color.Transparent)
                ),
                RoundedCornerShape(16.dp)
            )
    ) {
        // Shine Effect
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(item.glowColor.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "🎁", 
                fontSize = 32.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                item.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Text(
                item.provider,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
