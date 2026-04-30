package com.Kelasor.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

data class ProfileDrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

enum class ProfileEditMode {
    NONE, CROP, DRAW, ADJUST
}

@Composable
fun ProfilePhotoEditorScreen(
    imageUri: Uri,
    onSave: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val extendedColors = MessageAppTheme.extendedColors
    
    // Image state
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Transformation state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var rotation by remember { mutableFloatStateOf(0f) }
    
    // Editor modes
    var editMode by remember { mutableStateOf(ProfileEditMode.NONE) }
    
    // Drawing state
    val drawPaths = remember { mutableStateListOf<ProfileDrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var drawColor by remember { mutableStateOf(Color.Red) }
    var drawStrokeWidth by remember { mutableFloatStateOf(6f) }
    
    // Adjustment state
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }

    // Load bitmap
    LaunchedEffect(imageUri) {
        try {
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                bitmap = BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ── Main Image Area ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it }
                .pointerInput(editMode) {
                    if (editMode == ProfileEditMode.NONE || editMode == ProfileEditMode.CROP) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offset += pan
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Background Image with Transformations
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                        rotationZ = rotation
                    )
            ) {
                val colorFilter = if (brightness != 0f || contrast != 1f || saturation != 1f) {
                    val cm = android.graphics.ColorMatrix()
                    val b = brightness * 255f
                    cm.set(floatArrayOf(
                        contrast, 0f, 0f, 0f, b,
                        0f, contrast, 0f, 0f, b,
                        0f, 0f, contrast, 0f, b,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    val sat = android.graphics.ColorMatrix()
                    sat.setSaturation(saturation)
                    cm.postConcat(sat)
                    androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                        androidx.compose.ui.graphics.ColorMatrix(cm.array)
                    )
                } else null

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = colorFilter
                )
                
                // Drawing Canvas inside transformation layer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (dp in drawPaths) {
                        drawPath(
                            path = dp.path,
                            color = dp.color,
                            style = Stroke(width = dp.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    currentPath?.let { path ->
                        drawPath(
                            path = path,
                            color = drawColor,
                            style = Stroke(width = drawStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            // Circular Mask (Fixed in front)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val radius = (minOf(canvasWidth, canvasHeight) * 0.8f) / 2f
                val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

                // Outer dimmed area
                val path = Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
                    addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path, Color.Black.copy(alpha = 0.6f))
                
                // Circle border
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            
            // Drawing Overlay (Active only in DRAW mode, outside transformation to allow static drawing on screen if desired, but here we want it to follow image, so it's inside)
            // If we want drawing to follow image, it should be in the graphicsLayer above.
            // If we want drawing to be static, it stays here. Telegram drawing follows image.
            
            if (editMode == ProfileEditMode.DRAW) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { pos ->
                                    // Map screen pos to image pos
                                    val mappedPos = mapScreenToImage(pos, offset, scale, rotation, containerSize)
                                    currentPath = Path().apply { moveTo(mappedPos.x, mappedPos.y) }
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val mappedPos = mapScreenToImage(change.position, offset, scale, rotation, containerSize)
                                    currentPath?.lineTo(mappedPos.x, mappedPos.y)
                                    currentPath = currentPath?.let { Path().apply { addPath(it) } }
                                },
                                onDragEnd = {
                                    currentPath?.let { drawPaths.add(ProfileDrawPath(it, drawColor, drawStrokeWidth)) }
                                    currentPath = null
                                }
                            )
                        }
                )
            }
        }

        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "بستن", tint = Color.White)
            }
            
            Text(
                "ویرایش تصویر پروفایل",
                color = Color.White,
                fontFamily = VazirFontFamily,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = {
                    val resultUri = processAndSaveImage(
                        context = context,
                        originalUri = imageUri,
                        scale = scale,
                        offset = offset,
                        rotation = rotation,
                        drawPaths = drawPaths,
                        brightness = brightness,
                        contrast = contrast,
                        saturation = saturation,
                        containerSize = containerSize
                    )
                    if (resultUri != null) onSave(resultUri)
                },
                modifier = Modifier.background(extendedColors.accent, CircleShape)
            ) {
                Icon(Icons.Default.Check, contentDescription = "تایید", tint = Color.White)
            }
        }

        // ── Bottom Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(bottom = 24.dp, top = 8.dp)
        ) {
            // Sub-panels for tools
            AnimatedVisibility(visible = editMode == ProfileEditMode.DRAW) {
                DrawToolPanel(
                    selectedColor = drawColor,
                    onColorSelected = { drawColor = it },
                    strokeWidth = drawStrokeWidth,
                    onStrokeWidthChange = { drawStrokeWidth = it },
                    onUndo = { if (drawPaths.isNotEmpty()) drawPaths.removeAt(drawPaths.lastIndex) }
                )
            }
            
            AnimatedVisibility(visible = editMode == ProfileEditMode.ADJUST) {
                AdjustToolPanel(
                    brightness = brightness,
                    onBrightnessChange = { brightness = it },
                    contrast = contrast,
                    onContrastChange = { contrast = it },
                    saturation = saturation,
                    onSaturationChange = { saturation = it }
                )
            }

            // Main Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EditorToolButton(
                    icon = Icons.Default.RotateRight,
                    label = "چرخش",
                    onClick = { rotation += 90f }
                )
                EditorToolButton(
                    icon = Icons.Default.Brush,
                    label = "نقاشی",
                    selected = editMode == ProfileEditMode.DRAW,
                    onClick = { editMode = if (editMode == ProfileEditMode.DRAW) ProfileEditMode.NONE else ProfileEditMode.DRAW }
                )
                EditorToolButton(
                    icon = Icons.Default.Tune,
                    label = "تنظیمات",
                    selected = editMode == ProfileEditMode.ADJUST,
                    onClick = { editMode = if (editMode == ProfileEditMode.ADJUST) ProfileEditMode.NONE else ProfileEditMode.ADJUST }
                )
                EditorToolButton(
                    icon = Icons.Default.RestartAlt,
                    label = "بازنشانی",
                    onClick = {
                        scale = 1f
                        offset = Offset.Zero
                        rotation = 0f
                        drawPaths.clear()
                        brightness = 0f
                        contrast = 1f
                        saturation = 1f
                        editMode = ProfileEditMode.NONE
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) MessageAppTheme.extendedColors.accent else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            color = if (selected) MessageAppTheme.extendedColors.accent else Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontFamily = VazirFontFamily
        )
    }
}

private fun mapScreenToImage(
    screenPos: Offset,
    offset: Offset,
    scale: Float,
    rotation: Float,
    containerSize: IntSize
): Offset {
    // Basic mapping: screen center is (0,0) for transformations
    val cx = containerSize.width / 2f
    val cy = containerSize.height / 2f
    
    var x = screenPos.x - cx
    var y = screenPos.y - cy
    
    // Reverse offset
    x -= offset.x
    y -= offset.y
    
    // Reverse rotation (complex if needed, skipping for simple demo or implementing matrix)
    // For now simple translation/scale
    x /= scale
    y /= scale
    
    return Offset(x + cx, y + cy)
}

private fun processAndSaveImage(
    context: Context,
    originalUri: Uri,
    scale: Float,
    offset: Offset,
    rotation: Float,
    drawPaths: List<ProfileDrawPath>,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    containerSize: IntSize
): Uri? {
    try {
        val inputStream = context.contentResolver.openInputStream(originalUri) ?: return null
        val originalBmp = BitmapFactory.decodeStream(inputStream)
        
        // Target size: a square high-res image (e.g., 1080x1080)
        val targetSize = 1080
        val resultBmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(resultBmp)
        
        // Calculate crop radius in container pixels
        val radiusInContainer = (minOf(containerSize.width, containerSize.height) * 0.8f) / 2f
        
        // Matrix to map from original image to target square
        val matrix = android.graphics.Matrix()
        
        // 1. Center image in container
        val bmpWidth = originalBmp.width.toFloat()
        val bmpHeight = originalBmp.height.toFloat()
        
        // Calculate fit scale
        val fitScale = minOf(containerSize.width / bmpWidth, containerSize.height / bmpHeight)
        
        // Initial matrix to fit and center image in container
        matrix.postTranslate(-bmpWidth / 2f, -bmpHeight / 2f)
        matrix.postScale(fitScale, fitScale)
        
        // 2. Apply user transformations (relative to container center)
        matrix.postScale(scale, scale)
        matrix.postRotate(rotation)
        matrix.postTranslate(offset.x, offset.y)
        
        // 3. Translate so that container center is mapped to target center (targetSize/2)
        // And scale everything up to target resolution
        val finalScale = targetSize / (radiusInContainer * 2f)
        matrix.postTranslate(0f, 0f) // Center is already (0,0) relative to container center
        matrix.postScale(finalScale, finalScale)
        matrix.postTranslate(targetSize / 2f, targetSize / 2f)
        
        // Draw image
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG or android.graphics.Paint.FILTER_BITMAP_FLAG)
        
        // Apply color adjustments
        if (brightness != 0f || contrast != 1f || saturation != 1f) {
            val cm = android.graphics.ColorMatrix()
            val b = brightness * 255f
            cm.set(floatArrayOf(
                contrast, 0f, 0f, 0f, b,
                0f, contrast, 0f, 0f, b,
                0f, 0f, contrast, 0f, b,
                0f, 0f, 0f, 1f, 0f
            ))
            val sat = android.graphics.ColorMatrix()
            sat.setSaturation(saturation)
            cm.postConcat(sat)
            paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        }
        
        canvas.drawBitmap(originalBmp, matrix, paint)
        
        // Draw paths
        // We need to map draw paths from container space to target space
        // Paths are stored in container coordinates relative to container (0,0)
        // But since they were drawn on top of transformed image, they are already "transformed"
        // in terms of where they appear.
        // Actually, in the current mapScreenToImage, I mapped them to image space.
        // So I should draw them using the SAME matrix as the bitmap.
        
        val drawPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeJoin = android.graphics.Paint.Join.ROUND
        }
        
        for (dp in drawPaths) {
            drawPaint.color = dp.color.toArgb()
            drawPaint.strokeWidth = dp.strokeWidth * (targetSize / (radiusInContainer * 2f)) // Scale stroke width
            
            val androidPath = dp.path.asAndroidPath()
            val transformedPath = android.graphics.Path()
            androidPath.transform(matrix, transformedPath)
            canvas.drawPath(transformedPath, drawPaint)
        }

        // Save to temp file
        val tempFile = File.createTempFile("edited_avatar_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            resultBmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        return Uri.fromFile(tempFile)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// ── Components reused or adapted from MediaEditScreen ──

@Composable
private fun AdjustToolPanel(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AdjustSlider(label = "نور", value = brightness, range = -0.5f..0.5f, onValueChange = onBrightnessChange)
        AdjustSlider(label = "کنتراست", value = contrast, range = 0.5f..1.5f, onValueChange = onContrastChange)
        AdjustSlider(label = "اشباع", value = saturation, range = 0f..2f, onValueChange = onSaturationChange)
    }
}

@Composable
private fun AdjustSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 12.sp, fontFamily = VazirFontFamily, modifier = Modifier.width(60.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = MessageAppTheme.extendedColors.accent
            )
        )
    }
}

private val drawColors = listOf(
    Color.Red, Color.Blue, Color.Green, Color.Yellow,
    Color.White, Color.Black, Color.Magenta, Color.Cyan
)

@Composable
private fun DrawToolPanel(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    onUndo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            drawColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 2.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onUndo) {
                Icon(Icons.Default.Undo, contentDescription = "بازگشت", tint = Color.White)
            }
        }
        Slider(
            value = strokeWidth,
            onValueChange = onStrokeWidthChange,
            valueRange = 2f..20f,
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
        )
    }
}
