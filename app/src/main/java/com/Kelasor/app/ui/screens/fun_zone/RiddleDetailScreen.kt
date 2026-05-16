package com.Kelasor.app.ui.screens.fun_zone

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.viewmodel.EntertainmentViewModel
import com.Kelasor.app.data.remote.dto.RiddleResultDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiddleDetailScreen(
    riddleId: String,
    onNavigateBack: () -> Unit,
    viewModel: EntertainmentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val extendedColors = MessageAppTheme.extendedColors
    
    val riddle = remember(state.challenges, riddleId) {
        state.challenges.find { it.id == riddleId }
    }

    if (riddle == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = extendedColors.accent)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Glow Background
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .background(Brush.radialGradient(listOf(extendedColors.accent.copy(alpha = 0.1f), Color.Transparent)))
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("باشگاه معما", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                            Text("چالش هوش و جایزه", style = MaterialTheme.typography.labelSmall, color = extendedColors.accent)
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .background(extendedColors.glass, CircleShape)
                                .border(1.dp, extendedColors.glassBorder, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(extendedColors.accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .border(1.dp, extendedColors.accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stars, null, tint = extendedColors.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(riddle.reward ?: "0", fontWeight = FontWeight.Bold, color = extendedColors.accent)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Riddle Question Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, extendedColors.glassBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(extendedColors.accent.copy(alpha = 0.15f))
                                .border(2.dp, extendedColors.accent.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = extendedColors.accent,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Text(
                            text = riddle.question,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 34.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Text(
                    "یک گزینه را انتخاب کنید:",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                // Options
                riddle.options.forEachIndexed { index, option ->
                    val isSelected = state.riddleResult != null // Check if answered
                    val isCorrect = state.riddleResult?.success == true && riddle.correctAnswerIndex == index
                    val isSelectedWrong = state.riddleResult?.success == false && state.riddleResult?.message?.contains("اشتباه") == true 
                                       // This is a bit hacky, but state doesn't track selected index for now
                    
                    Surface(
                        onClick = { if (state.riddleResult == null) viewModel.solveRiddle(riddle.id, index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = when {
                            isCorrect -> Color(0xFFE8F5E9).copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        },
                        border = BorderStroke(
                            2.dp, 
                            when {
                                isCorrect -> Color(0xFF4CAF50)
                                else -> extendedColors.glassBorder
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCorrect) Color(0xFF4CAF50) 
                                        else extendedColors.accent.copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = "${index + 1}", 
                                        color = extendedColors.accent, 
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                option.text, 
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Results Celebratory Section
                AnimatedVisibility(
                    visible = state.riddleResult != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val result = state.riddleResult!!
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(32.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (result.success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(32.dp),
                            border = BorderStroke(1.dp, if (result.success) Color(0xFF4CAF50).copy(alpha = 0.3f) else Color(0xFFE53935).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            if (result.success) Color(0xFF4CAF50).copy(alpha = 0.1f) 
                                            else Color(0xFFE53935).copy(alpha = 0.1f), 
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFE53935),
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                                
                                Spacer(Modifier.height(20.dp))
                                
                                Text(
                                    text = if (result.success) "تبریک! پاسخ صحیح بود" else "متأسفم، پاسخ اشتباه بود",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                    color = if (result.success) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                
                                Text(
                                    text = result.message,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (result.success) Color(0xFF2E7D32).copy(alpha = 0.8f) else Color(0xFFC62828).copy(alpha = 0.8f)
                                )
                                
                                if (result.success) {
                                    Spacer(Modifier.height(24.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                    ) {
                                        Icon(Icons.Default.Stars, null, tint = Color(0xFFFFA000), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            "شما ${riddle.reward} امتیاز دریافت کردید!",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                
                                if (!result.success) {
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = { viewModel.clearRiddleResult() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("تلاش دوباره", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Spacer(Modifier.height(24.dp))
                                    Button(
                                        onClick = onNavigateBack,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text("بازگشت به دنیای سرگرمی", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }
}
