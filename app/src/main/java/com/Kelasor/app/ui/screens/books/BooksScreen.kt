package com.Kelasor.app.ui.screens.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.Kelasor.app.ui.theme.MessageAppTheme

@Composable
fun BooksScreen(
    onNavigateBack: () -> Unit = {}
) {
    val extendedColors = MessageAppTheme.extendedColors
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(extendedColors.navBarBackground)
    ) {
        Column(Modifier.fillMaxSize()) {
            BookHeader(onNavigateBack)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(12) {
                    BookItem()
                }
            }
        }
    }
}

@Composable
fun BookHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                "کتابخوانی",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFFE91E63), // Pink
                fontWeight = FontWeight.Bold
            )
            Text(
                "کتابخانه دیجیتال شما",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BookItem() {
    Column(
        modifier = Modifier.width(100.dp).clickable {  }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723))
                    )
                )
        ) {
            // Spine/Cover Detail
            Box(
                 Modifier
                     .fillMaxHeight()
                     .width(4.dp)
                     .background(Color.White.copy(alpha=0.2f))
                     .padding(start=4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "نام کتاب",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            maxLines = 1
        )
        Text(
            "نویسنده",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}
