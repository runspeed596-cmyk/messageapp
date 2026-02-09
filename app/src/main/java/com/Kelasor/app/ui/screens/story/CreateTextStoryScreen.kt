package com.Kelasor.app.ui.screens.story

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.Kelasor.app.ui.theme.MessageAppTypography
import com.Kelasor.app.ui.viewmodel.StoryViewModel
import java.io.File
import java.io.FileOutputStream

data class StoryBackground(
    val id: Int,
    val brush: Brush,
    // Helper for Bitmap generation (simplified to handling solid color or simple gradient logic manually if needed)
    val colors: List<Color> 
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTextStoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    // State for Premium Dialog
    var showPremiumDialog by remember { mutableStateOf(false) }
    
    // Handle UI State Changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success -> {
                 // Optimization: Only navigate back if we effectively just uploaded something? 
                 // Issue: uiState might be Success from initial loadStories().
                 // We need a way to distinguish "Load Success" from "Upload Success".
                 // Detailed fix: ViewModel should expose a separate 'uploadState' or generic 'actionEvent'.
                 // For now, let's assume if isUploading goes true -> false and we are in Success, we are good?
                 // Simpler: ViewModel clears isUploading.
                 // Let's rely on a separate side effect or assumes we just check if it WAS uploading.
            }
            is com.Kelasor.app.ui.viewmodel.StoriesUiState.Error -> {
                if (state.isPremiumRequired) {
                    showPremiumDialog = true
                } else {
                    android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
            else -> {}
        }
    }
    
    // Better approach for Navigation on Success: 
    // Use a specific 'uploadResult' flow or similar, OR check if we pressed send.
    // Let's add 'uploadSuccess' event to ViewModel?
    // Or for now, we just keep it simple: WE DO NOT NAVIGATE BACK on success automatically to simplify, 
    // OR we add a local state 'hasAttemptedUpload' to track it.
    
    var hasAttemptedUpload by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState, hasAttemptedUpload) {
        if (hasAttemptedUpload && uiState is com.Kelasor.app.ui.viewmodel.StoriesUiState.Success) {
            onNavigateBack()
        }
    }
    
    val backgrounds = remember {
        listOf(
            StoryBackground(1, SolidColor(Color(0xFFE91E63)), listOf(Color(0xFFE91E63))), // Pink
            StoryBackground(2, SolidColor(Color(0xFF9C27B0)), listOf(Color(0xFF9C27B0))), // Purple
            StoryBackground(3, SolidColor(Color(0xFF2196F3)), listOf(Color(0xFF2196F3))), // Blue
            StoryBackground(4, SolidColor(Color(0xFF009688)), listOf(Color(0xFF009688))), // Teal
            StoryBackground(5, SolidColor(Color(0xFFFF9800)), listOf(Color(0xFFFF9800))), // Orange
            StoryBackground(6, SolidColor(Color(0xFFF44336)), listOf(Color(0xFFF44336))), // Red
            StoryBackground(7, Brush.verticalGradient(listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))), listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))), // Purple Gradient
            StoryBackground(8, Brush.verticalGradient(listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))), listOf(Color(0xFFFF5F6D), Color(0xFFFFC371))), // Orange Gradient
            StoryBackground(9, Brush.verticalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d))), listOf(Color(0xFF11998e), Color(0xFF38ef7d))), // Green Gradient
             StoryBackground(10, SolidColor(Color.Black), listOf(Color.Black)) // Black
        )
    }
    
    var selectedBackground by remember { mutableStateOf(backgrounds[0]) }
    var isGenerating by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (text.isNotBlank() && !isGenerating) {
                        isGenerating = true
                        val uri = generateImageFromText(context, text, selectedBackground)
                        if (uri != null) {
                            hasAttemptedUpload = true
                            viewModel.uploadStory(uri, "IMAGE") // Upload as Image
                        }
                        isGenerating = false
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isGenerating) {
                     CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(selectedBackground.brush)
                .padding(padding)
        ) {
            // Close Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }

            // Text Input Area
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 300) text = it },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = "تایپ کنید...",
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )

            // Background Selector
            LazyRow(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // Space for FAB
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(backgrounds) { bg ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(bg.brush)
                            .border(
                                width = 2.dp,
                                color = if (selectedBackground == bg) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedBackground = bg }
                    ) {
                        if (selectedBackground == bg) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.align(Alignment.Center).size(24.dp)
                            )
                        }
                    }
                }
        }
    }

    if (showPremiumDialog) {
        com.Kelasor.app.ui.components.PremiumUpgradeDialog(
            onDismiss = { showPremiumDialog = false },
            onUpgrade = {
                showPremiumDialog = false
                // TODO: Navigate to Premium Purchase Screen
                android.widget.Toast.makeText(context, "Navigate to Purchase Screen", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
}
fun generateImageFromText(context: Context, text: String, background: StoryBackground): Uri? {
    try {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Background
        val paint = Paint()
        if (background.colors.size > 1) {
             val shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                background.colors.map { it.toArgb() }.toIntArray(),
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = shader
        } else {
            paint.color = background.colors.first().toArgb()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Draw Text
        val textPaint = TextPaint().apply {
            color = android.graphics.Color.WHITE
            textSize = 100f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        // Calculate Text Layout
        // Simple fitting: if too wide, reduce size? For now relying on wrapping.
        val textLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width - 200)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1f)
            .setIncludePad(false)
            .build()
        
        canvas.save()
        // Center text vertically
        val textHeight = textLayout.height
        val y = (height - textHeight) / 2f
        canvas.translate(width / 2f, y) // Translate horizontal to center, vertical to center
        textLayout.draw(canvas)
        canvas.restore()

        // Save to File
        val imagesFolder = File(context.cacheDir, "story_images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "story_${System.currentTimeMillis()}.jpg")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        stream.close()
        
        // Return URI using FileProvider if configured, or just Uri.fromFile for internal usage if we handle it right.
        // Since we are inside the app, passing file URI to our Repository (which uses ContentResolver) might be tricky if it expects content://.
        // Repository's uriToFile expects content:// usually from picker.
        // BUT, our modified StoryRepository.uriToFile implementation:
        // context.contentResolver.openInputStream(uri).
        // File URI (file://...) is also supported by ContentResolver if we are just reading.
        // Let's try Uri.fromFile(file).
        return Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
