package com.Kelasor.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.CornerRadius
import coil3.request.crossfade
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.Kelasor.app.ui.theme.MessageAppTheme
import com.Kelasor.app.ui.theme.VazirFontFamily
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ═══════════════════════════════════════════════════════════════════════════════
// Data classes
// ═══════════════════════════════════════════════════════════════════════════════

data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)

data class WatermarkText(
    val text: String,
    val offsetX: Float,
    val offsetY: Float,
    val color: Color,
    val fontSize: Float = 24f,
    val scale: Float = 1f,
    val bgStyle: WatermarkBgStyle = WatermarkBgStyle.BLACK
)

enum class WatermarkBgStyle {
    NONE, BLACK, WHITE
}

enum class EditMode {
    NONE, CROP, DRAW, WATERMARK, ADJUST
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎨 Media Edit Screen
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun MediaEditScreen(
    mediaUri: Uri,
    isVideo: Boolean,
    onSend: (editedUri: Uri, caption: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val densityValue = LocalDensity.current.density
    val extendedColors = MessageAppTheme.extendedColors
    var caption by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(EditMode.NONE) }
    // Draw state
    val drawPaths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var drawColor by remember { mutableStateOf(Color.Red) }
    var drawStrokeWidth by remember { mutableFloatStateOf(6f) }
    // Watermark state
    val watermarks = remember { mutableStateListOf<WatermarkText>() }
    var watermarkInput by remember { mutableStateOf("") }
    var watermarkColor by remember { mutableStateOf(Color.White) }
    var selectedWatermarkIndex by remember { mutableIntStateOf(-1) }
    // Adjustment state
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    // Crop state
    var cropTopLeft by remember { mutableStateOf(Offset(0f, 0f)) }
    var cropBottomRight by remember { mutableStateOf(Offset(1f, 1f)) }
    var isCropApplied by remember { mutableStateOf(false) }
    var croppedPreviewBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    // Track image container size for watermark centering
    var imageContainerSize by remember { mutableStateOf(IntSize(400, 600)) }
    var activeCropCorner by remember { mutableIntStateOf(-1) }
    LaunchedEffect(mediaUri) {
        if (!isVideo) {
            try {
                context.contentResolver.openInputStream(mediaUri)?.use { stream ->
                    originalBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {}
        }
    }

    // ── ROOT: Box overlay layout ──
    // The image area is COMPLETELY INDEPENDENT from the bottom controls.
    // This prevents ANY layout shift when keyboard opens.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                enabled = true,
                onClick = { /* Block clicks from reaching background */ },
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // ═══════════════════════════════════════════════════════════════════
        // LAYER 1: Image — fills the ENTIRE available space, never moves
        // ═══════════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 52.dp, bottom = 56.dp) // space for top bar + caption
                .padding(horizontal = 8.dp)
                .onSizeChanged { imageContainerSize = it },
            contentAlignment = Alignment.Center
        ) {
            if (isVideo) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "ویدیو",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    // Color filter
                    val colorFilter = if (brightness != 0f || contrast != 1f || saturation != 1f) {
                        val cm = ColorMatrix()
                        val b = brightness * 255f
                        cm.set(floatArrayOf(
                            contrast, 0f, 0f, 0f, b,
                            0f, contrast, 0f, 0f, b,
                            0f, 0f, contrast, 0f, b,
                            0f, 0f, 0f, 1f, 0f
                        ))
                        val sat = ColorMatrix()
                        sat.setSaturation(saturation)
                        cm.postConcat(sat)
                        androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                            androidx.compose.ui.graphics.ColorMatrix(cm.array)
                        )
                    } else null

                    // Base image
                    if (croppedPreviewBitmap != null && isCropApplied && editMode != EditMode.CROP) {
                        androidx.compose.foundation.Image(
                            bitmap = croppedPreviewBitmap!!,
                            contentDescription = "تصویر برش‌شده",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            colorFilter = colorFilter
                        )
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(mediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "تصویر",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            colorFilter = colorFilter
                        )
                    }

                    // Draw overlay
                    if (editMode == EditMode.DRAW || drawPaths.isNotEmpty()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (editMode == EditMode.DRAW) {
                                        Modifier.pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    currentPath?.lineTo(change.position.x, change.position.y)
                                                    currentPath = currentPath?.let {
                                                        Path().apply { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
                                                    currentPath?.let {
                                                        drawPaths.add(DrawPath(it, drawColor, drawStrokeWidth))
                                                    }
                                                    currentPath = null
                                                }
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
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

                    // Watermark overlays — drag + pinch to scale
                    // IMPORTANT: Force LTR layout inside a fillMaxSize Box so that:
                    // 1) fillMaxSize Box fills parent → no RTL offset from parent
                    // 2) Box content uses LTR → absoluteOffset positions from top-LEFT
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            watermarks.forEachIndexed { index, wm ->
                                val isSelected = selectedWatermarkIndex == index && editMode == EditMode.WATERMARK
                                Text(
                                    text = wm.text,
                                    color = wm.color,
                                    fontSize = (wm.fontSize * wm.scale).sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = VazirFontFamily,
                                    modifier = Modifier
                                        .absoluteOffset { IntOffset(wm.offsetX.roundToInt(), wm.offsetY.roundToInt()) }
                                        .then(
                                            if (editMode == EditMode.WATERMARK) {
                                                Modifier
                                                    .pointerInput(index) {
                                                        detectTapGestures {
                                                            selectedWatermarkIndex = if (selectedWatermarkIndex == index) -1 else index
                                                        }
                                                    }
                                                    .pointerInput(index, selectedWatermarkIndex) {
                                                        detectTransformGestures { _, pan, _, _ ->
                                                            val cur = watermarks[index]
                                                            val maxX = (imageContainerSize.width - 40f).coerceAtLeast(0f)
                                                            val maxY = (imageContainerSize.height - 20f).coerceAtLeast(0f)
                                                            watermarks[index] = cur.copy(
                                                                offsetX = (cur.offsetX + pan.x).coerceIn(0f, maxX),
                                                                offsetY = (cur.offsetY + pan.y).coerceIn(0f, maxY)
                                                            )
                                                            selectedWatermarkIndex = index
                                                        }
                                                    }
                                            } else Modifier
                                        )
                                        .then(
                                            if (isSelected) Modifier.border(2.dp, Color(0xFF00BFFF), RoundedCornerShape(6.dp))
                                            else Modifier
                                        )
                                        .background(
                                            when (wm.bgStyle) {
                                                WatermarkBgStyle.NONE -> Color.Transparent
                                                WatermarkBgStyle.BLACK -> Color.Black.copy(alpha = 0.55f)
                                                WatermarkBgStyle.WHITE -> Color.White.copy(alpha = 0.7f)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Crop overlay
                    if (editMode == EditMode.CROP) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { pos ->
                                            val normX = pos.x / size.width
                                            val normY = pos.y / size.height
                                            val corners = listOf(
                                                Offset(cropTopLeft.x, cropTopLeft.y),
                                                Offset(cropBottomRight.x, cropTopLeft.y),
                                                Offset(cropTopLeft.x, cropBottomRight.y),
                                                Offset(cropBottomRight.x, cropBottomRight.y)
                                            )
                                            val distances = corners.map { c ->
                                                sqrt((normX - c.x) * (normX - c.x) + (normY - c.y) * (normY - c.y))
                                            }
                                            val minIdx = distances.indices.minBy { distances[it] }
                                            activeCropCorner = if (distances[minIdx] < 0.25f) minIdx else -1
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val dx = dragAmount.x / size.width
                                            val dy = dragAmount.y / size.height
                                            when (activeCropCorner) {
                                                0 -> cropTopLeft = Offset(
                                                    (cropTopLeft.x + dx).coerceIn(0f, cropBottomRight.x - 0.08f),
                                                    (cropTopLeft.y + dy).coerceIn(0f, cropBottomRight.y - 0.08f)
                                                )
                                                1 -> {
                                                    cropBottomRight = cropBottomRight.copy(
                                                        x = (cropBottomRight.x + dx).coerceIn(cropTopLeft.x + 0.08f, 1f)
                                                    )
                                                    cropTopLeft = cropTopLeft.copy(
                                                        y = (cropTopLeft.y + dy).coerceIn(0f, cropBottomRight.y - 0.08f)
                                                    )
                                                }
                                                2 -> {
                                                    cropTopLeft = cropTopLeft.copy(
                                                        x = (cropTopLeft.x + dx).coerceIn(0f, cropBottomRight.x - 0.08f)
                                                    )
                                                    cropBottomRight = cropBottomRight.copy(
                                                        y = (cropBottomRight.y + dy).coerceIn(cropTopLeft.y + 0.08f, 1f)
                                                    )
                                                }
                                                3 -> cropBottomRight = Offset(
                                                    (cropBottomRight.x + dx).coerceIn(cropTopLeft.x + 0.08f, 1f),
                                                    (cropBottomRight.y + dy).coerceIn(cropTopLeft.y + 0.08f, 1f)
                                                )
                                            }
                                        },
                                        onDragEnd = { activeCropCorner = -1 }
                                    )
                                }
                        ) {
                            val left = size.width * cropTopLeft.x
                            val top = size.height * cropTopLeft.y
                            val right = size.width * cropBottomRight.x
                            val bottom = size.height * cropBottomRight.y
                            val dimColor = Color.Black.copy(alpha = 0.55f)
                            drawRect(dimColor, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(size.width, top))
                            drawRect(dimColor, topLeft = Offset(0f, bottom), size = androidx.compose.ui.geometry.Size(size.width, size.height - bottom))
                            drawRect(dimColor, topLeft = Offset(0f, top), size = androidx.compose.ui.geometry.Size(left, bottom - top))
                            drawRect(dimColor, topLeft = Offset(right, top), size = androidx.compose.ui.geometry.Size(size.width - right, bottom - top))
                            drawRect(Color.White, topLeft = Offset(left, top), size = androidx.compose.ui.geometry.Size(right - left, bottom - top), style = Stroke(width = 2f))
                            // Rule of thirds
                            val thirdW = (right - left) / 3f
                            val thirdH = (bottom - top) / 3f
                            for (i in 1..2) {
                                drawLine(Color.White.copy(alpha = 0.25f), Offset(left + thirdW * i, top), Offset(left + thirdW * i, bottom), strokeWidth = 0.8f)
                                drawLine(Color.White.copy(alpha = 0.25f), Offset(left, top + thirdH * i), Offset(right, top + thirdH * i), strokeWidth = 0.8f)
                            }
                            // Large corner handles
                            val handleLen = 36f
                            val handleW = 5f
                            val activeColor = Color(0xFF00BFFF)
                            val tlC = if (activeCropCorner == 0) activeColor else Color.White
                            drawLine(tlC, Offset(left, top), Offset(left + handleLen, top), handleW)
                            drawLine(tlC, Offset(left, top), Offset(left, top + handleLen), handleW)
                            drawCircle(tlC, radius = 10f, center = Offset(left, top))
                            val trC = if (activeCropCorner == 1) activeColor else Color.White
                            drawLine(trC, Offset(right, top), Offset(right - handleLen, top), handleW)
                            drawLine(trC, Offset(right, top), Offset(right, top + handleLen), handleW)
                            drawCircle(trC, radius = 10f, center = Offset(right, top))
                            val blC = if (activeCropCorner == 2) activeColor else Color.White
                            drawLine(blC, Offset(left, bottom), Offset(left + handleLen, bottom), handleW)
                            drawLine(blC, Offset(left, bottom), Offset(left, bottom - handleLen), handleW)
                            drawCircle(blC, radius = 10f, center = Offset(left, bottom))
                            val brC = if (activeCropCorner == 3) activeColor else Color.White
                            drawLine(brC, Offset(right, bottom), Offset(right - handleLen, bottom), handleW)
                            drawLine(brC, Offset(right, bottom), Offset(right, bottom - handleLen), handleW)
                            drawCircle(brC, radius = 10f, center = Offset(right, bottom))
                        }
                        // Crop hint
                        Text(
                            text = "گوشه‌ها را بکشید تا برش دهید",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontFamily = VazirFontFamily,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 2: Top bar — overlaid on top
        // ═══════════════════════════════════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .height(52.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (editMode != EditMode.NONE) {
                    editMode = EditMode.NONE
                    selectedWatermarkIndex = -1
                } else {
                    onDismiss()
                }
            }) {
                Icon(
                    if (editMode != EditMode.NONE) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (editMode != EditMode.NONE) "بستن ابزار" else "بازگشت",
                    tint = Color.White
                )
            }
            Spacer(Modifier.weight(1f))
            if (!isVideo && editMode != EditMode.NONE) {
                IconButton(onClick = {
                    if (editMode == EditMode.CROP) {
                        isCropApplied = true
                        originalBitmap?.let { bmp ->
                            try {
                                val x = (cropTopLeft.x * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                                val y = (cropTopLeft.y * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                                val w = ((cropBottomRight.x - cropTopLeft.x) * bmp.width).toInt().coerceIn(1, bmp.width - x)
                                val h = ((cropBottomRight.y - cropTopLeft.y) * bmp.height).toInt().coerceIn(1, bmp.height - y)
                                croppedPreviewBitmap = Bitmap.createBitmap(bmp, x, y, w, h).asImageBitmap()
                            } catch (_: Exception) {}
                        }
                    }
                    editMode = EditMode.NONE
                    selectedWatermarkIndex = -1
                }) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "تایید",
                        tint = extendedColors.accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // LAYER 3: Bottom controls — overlaid on bottom, shifts with keyboard
        // ═══════════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
        ) {
            // Tool buttons (only when no tool is active)
            if (!isVideo) {
                AnimatedVisibility(
                    visible = editMode == EditMode.NONE,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ToolButton(icon = Icons.Default.Crop, label = "برش", onClick = { editMode = EditMode.CROP })
                        ToolButton(icon = Icons.Default.Brush, label = "نقاشی", onClick = { editMode = EditMode.DRAW })
                        ToolButton(icon = Icons.Default.TextFields, label = "واترمارک", onClick = { editMode = EditMode.WATERMARK })
                        ToolButton(icon = Icons.Default.Tune, label = "تنظیمات", onClick = { editMode = EditMode.ADJUST })
                    }
                }
                // Tool panels
                AnimatedVisibility(visible = editMode == EditMode.DRAW, enter = fadeIn(), exit = fadeOut()) {
                    DrawToolPanel(
                        selectedColor = drawColor,
                        onColorSelected = { drawColor = it },
                        strokeWidth = drawStrokeWidth,
                        onStrokeWidthChange = { drawStrokeWidth = it },
                        onUndo = { if (drawPaths.isNotEmpty()) drawPaths.removeAt(drawPaths.lastIndex) }
                    )
                }
                AnimatedVisibility(visible = editMode == EditMode.WATERMARK && selectedWatermarkIndex !in watermarks.indices, enter = fadeIn(), exit = fadeOut()) {
                    WatermarkToolPanel(
                        text = watermarkInput,
                        onTextChange = { watermarkInput = it },
                        selectedColor = watermarkColor,
                        onColorSelected = { watermarkColor = it },
                        onAddWatermark = {
                            if (watermarkInput.isNotBlank()) {
                                // Place at ~30% of container (safe zone for Fit images)
                                val cx = (imageContainerSize.width * 0.3f).coerceAtLeast(20f)
                                val cy = (imageContainerSize.height * 0.3f).coerceAtLeast(20f)
                                android.util.Log.d("MediaEdit", "Adding watermark: text='$watermarkInput' at ($cx, $cy) containerSize=$imageContainerSize")
                                watermarks.add(
                                    WatermarkText(
                                        text = watermarkInput,
                                        offsetX = cx,
                                        offsetY = cy,
                                        color = watermarkColor
                                    )
                                )
                                watermarkInput = ""
                                // Dismiss keyboard so user sees the watermark right away
                                focusManager.clearFocus()
                            }
                        }
                    )
                }
                AnimatedVisibility(visible = editMode == EditMode.ADJUST, enter = fadeIn(), exit = fadeOut()) {
                    AdjustToolPanel(
                        brightness = brightness,
                        onBrightnessChange = { brightness = it },
                        contrast = contrast,
                        onContrastChange = { contrast = it },
                        saturation = saturation,
                        onSaturationChange = { saturation = it }
                    )
                }
                // Selected watermark editing panel (color + size + delete)
                AnimatedVisibility(
                    visible = editMode == EditMode.WATERMARK && selectedWatermarkIndex in watermarks.indices,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (selectedWatermarkIndex in watermarks.indices) {
                        WatermarkSelectedPanel(
                            watermark = watermarks[selectedWatermarkIndex],
                            onUpdate = { watermarks[selectedWatermarkIndex] = it },
                            onDelete = {
                                watermarks.removeAt(selectedWatermarkIndex)
                                selectedWatermarkIndex = -1
                            }
                        )
                    }
                }
            }
            // Caption + Send
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (caption.isEmpty()) {
                        Text(
                            text = "توضیحات را بنویسید...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp,
                            fontFamily = VazirFontFamily
                        )
                    }
                    BasicTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = VazirFontFamily),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3
                    )
                }
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        val hasEdits = !isVideo && (drawPaths.isNotEmpty() || watermarks.isNotEmpty() || brightness != 0f || contrast != 1f || saturation != 1f || isCropApplied)
                        if (hasEdits) {
                            val editedUri = applyImageEdits(
                                context = context,
                                originalUri = mediaUri,
                                drawPaths = drawPaths,
                                watermarks = watermarks,
                                brightness = brightness,
                                contrast = contrast,
                                saturation = saturation,
                                cropTopLeft = if (isCropApplied) cropTopLeft else null,
                                cropBottomRight = if (isCropApplied) cropBottomRight else null,
                                containerSize = imageContainerSize,
                                density = densityValue
                            )
                            onSend(editedUri ?: mediaUri, caption)
                        } else {
                            onSend(mediaUri, caption)
                        }
                    },
                    containerColor = extendedColors.accent,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "ارسال", modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Tool button
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontFamily = VazirFontFamily)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Draw tool panel
// ═══════════════════════════════════════════════════════════════════════════════

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
            .background(Color(0xFF1A1A2E))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (color in drawColors) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (color == selectedColor) Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onColorSelected(color) }
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(onClick = onUndo),
                contentAlignment = Alignment.Center
            ) {
                Text("↩", color = Color.White, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ضخامت", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontFamily = VazirFontFamily)
            Spacer(Modifier.width(8.dp))
            DraggableSlider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..20f,
                modifier = Modifier.weight(1f),
                activeTrackColor = Color.White,
                thumbColor = Color.White
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Watermark tool panel (no inline editing — pinch to resize on image)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WatermarkToolPanel(
    text: String,
    onTextChange: (String) -> Unit,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onAddWatermark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A2E))
            .padding(12.dp)
    ) {
        Text(
            text = "متن بنویسید و «+» بزنید. با انگشت بکشید تا جابه‌جا شود. دو انگشت برای بزرگ/کوچک.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontFamily = VazirFontFamily,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "متن واترمارک...",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontFamily = VazirFontFamily
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp, fontFamily = VazirFontFamily),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MessageAppTheme.extendedColors.accent)
                    .clickable(onClick = onAddWatermark),
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (color in drawColors) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (color == selectedColor) Modifier.border(2.dp, Color.White, CircleShape)
                            else Modifier
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Watermark selected panel (inline editing: color, size, delete)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WatermarkSelectedPanel(
    watermark: WatermarkText,
    onUpdate: (WatermarkText) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D1F))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Font size slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "اندازه",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = VazirFontFamily,
                modifier = Modifier.width(48.dp)
            )
            DraggableSlider(
                value = watermark.fontSize,
                onValueChange = { onUpdate(watermark.copy(fontSize = it)) },
                valueRange = 12f..60f,
                modifier = Modifier.weight(1f),
                activeTrackColor = Color.White,
                thumbColor = Color.White
            )
            Spacer(Modifier.width(8.dp))
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف واترمارک",
                    tint = Color(0xFFFF5252)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Color picker
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (color in drawColors) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (color == watermark.color) Modifier.border(2.dp, Color(0xFF00BFFF), CircleShape)
                            else Modifier
                        )
                        .clickable { onUpdate(watermark.copy(color = color)) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Background style picker
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "پس\u200Cزمینه",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = VazirFontFamily
            )
            // No background
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (watermark.bgStyle == WatermarkBgStyle.NONE) Color(0xFF00BFFF).copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .then(
                        if (watermark.bgStyle == WatermarkBgStyle.NONE) Modifier.border(1.5.dp, Color(0xFF00BFFF), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable { onUpdate(watermark.copy(bgStyle = WatermarkBgStyle.NONE)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("بدون", color = Color.White, fontSize = 11.sp, fontFamily = VazirFontFamily)
            }
            // Black background
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (watermark.bgStyle == WatermarkBgStyle.BLACK) Color(0xFF00BFFF).copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .then(
                        if (watermark.bgStyle == WatermarkBgStyle.BLACK) Modifier.border(1.5.dp, Color(0xFF00BFFF), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable { onUpdate(watermark.copy(bgStyle = WatermarkBgStyle.BLACK)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("مشکی", color = Color.White, fontSize = 11.sp, fontFamily = VazirFontFamily)
            }
            // White background
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (watermark.bgStyle == WatermarkBgStyle.WHITE) Color(0xFF00BFFF).copy(alpha = 0.25f)
                        else Color.White.copy(alpha = 0.1f)
                    )
                    .then(
                        if (watermark.bgStyle == WatermarkBgStyle.WHITE) Modifier.border(1.5.dp, Color(0xFF00BFFF), RoundedCornerShape(8.dp))
                        else Modifier
                    )
                    .clickable { onUpdate(watermark.copy(bgStyle = WatermarkBgStyle.WHITE)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("سفید", color = Color.Black, fontSize = 11.sp, fontFamily = VazirFontFamily)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Adjust tool panel
// ═══════════════════════════════════════════════════════════════════════════════

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
            .background(Color(0xFF1A1A2E))
            .padding(12.dp)
    ) {
        AdjustSliderRow(label = "روشنایی", value = brightness, onValueChange = onBrightnessChange, valueRange = -0.5f..0.5f)
        AdjustSliderRow(label = "کنتراست", value = contrast, onValueChange = onContrastChange, valueRange = 0.5f..2f)
        AdjustSliderRow(label = "اشباع", value = saturation, onValueChange = onSaturationChange, valueRange = 0f..2f)
    }
}

@Composable
private fun AdjustSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontFamily = VazirFontFamily,
            modifier = Modifier.width(56.dp)
        )
        DraggableSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f),
            activeTrackColor = Color.White.copy(alpha = 0.7f),
            thumbColor = Color.White
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Custom draggable slider — smooth grab & drag instead of click-to-set
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DraggableSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    activeTrackColor: Color = Color.White,
    inactiveTrackColor: Color = Color.White.copy(alpha = 0.2f),
    thumbColor: Color = Color.White
) {
    val density = LocalDensity.current
    val thumbRadiusPx = with(density) { 10.dp.toPx() }
    val trackHeightPx = with(density) { 4.dp.toPx() }
    val fraction = remember(value, valueRange) {
        ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .pointerInput(valueRange) {
                val totalWidth = size.width.toFloat()
                val usableWidth = (totalWidth - thumbRadiusPx * 2).coerceAtLeast(1f)
                fun xToValue(x: Float): Float {
                    val clampedX = (x - thumbRadiusPx).coerceIn(0f, usableWidth)
                    val frac = clampedX / usableWidth
                    return valueRange.start + frac * (valueRange.endInclusive - valueRange.start)
                }
                detectHorizontalDragGestures(
                    onDragStart = { startOffset ->
                        onValueChange(xToValue(startOffset.x))
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        onValueChange(xToValue(change.position.x))
                    }
                )
            }
            .pointerInput(valueRange) {
                detectTapGestures { offset ->
                    val totalWidth = size.width.toFloat()
                    val usableWidth = (totalWidth - thumbRadiusPx * 2).coerceAtLeast(1f)
                    val clampedX = (offset.x - thumbRadiusPx).coerceIn(0f, usableWidth)
                    val frac = clampedX / usableWidth
                    val newValue = valueRange.start + frac * (valueRange.endInclusive - valueRange.start)
                    onValueChange(newValue)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f
            val usableWidth = canvasWidth - thumbRadiusPx * 2
            val thumbCenterX = thumbRadiusPx + usableWidth * fraction
            // Inactive track
            drawRoundRect(
                color = inactiveTrackColor,
                topLeft = Offset(thumbRadiusPx, centerY - trackHeightPx / 2),
                size = androidx.compose.ui.geometry.Size(usableWidth, trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )
            // Active track
            drawRoundRect(
                color = activeTrackColor,
                topLeft = Offset(thumbRadiusPx, centerY - trackHeightPx / 2),
                size = androidx.compose.ui.geometry.Size((thumbCenterX - thumbRadiusPx).coerceAtLeast(0f), trackHeightPx),
                cornerRadius = CornerRadius(trackHeightPx / 2, trackHeightPx / 2)
            )
            // Thumb (matches Material 3 — 20dp diameter)
            drawCircle(
                color = thumbColor,
                radius = thumbRadiusPx,
                center = Offset(thumbCenterX, centerY)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Apply image edits
// ═══════════════════════════════════════════════════════════════════════════════

private fun applyImageEdits(
    context: Context,
    originalUri: Uri,
    drawPaths: List<DrawPath>,
    watermarks: List<WatermarkText>,
    brightness: Float,
    contrast: Float,
    saturation: Float,
    cropTopLeft: Offset?,
    cropBottomRight: Offset?,
    containerSize: IntSize,
    density: Float
): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(originalUri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        val mutable = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        // 1. Color adjustments
        if (brightness != 0f || contrast != 1f || saturation != 1f) {
            val paint = Paint()
            val cm = ColorMatrix()
            val b = brightness * 255f
            cm.set(floatArrayOf(
                contrast, 0f, 0f, 0f, b,
                0f, contrast, 0f, 0f, b,
                0f, 0f, contrast, 0f, b,
                0f, 0f, 0f, 1f, 0f
            ))
            val sat = ColorMatrix()
            sat.setSaturation(saturation)
            cm.postConcat(sat)
            paint.colorFilter = ColorMatrixColorFilter(cm)
            // Draw original onto mutable using the filter paint
            canvas.drawBitmap(original, 0f, 0f, paint)
        }

        // 2. Coordinate mapping (UI space to Bitmap space)
        // Simulate ContentScale.Fit logic
        val cw = containerSize.width.toFloat().coerceAtLeast(1f)
        val ch = containerSize.height.toFloat().coerceAtLeast(1f)
        val bw = mutable.width.toFloat()
        val bh = mutable.height.toFloat()
        val scale = (cw / bw).coerceAtMost(ch / bh)
        val offsetX = (cw - bw * scale) / 2f
        val offsetY = (ch - bh * scale) / 2f

        // 3. Render drawings
        for (dp in drawPaths) {
            val paint = Paint().apply {
                color = dp.color.toArgb()
                strokeWidth = dp.strokeWidth / scale
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                isAntiAlias = true
            }
            val matrix = android.graphics.Matrix()
            matrix.postTranslate(-offsetX, -offsetY)
            matrix.postScale(1f / scale, 1f / scale)
            val androidPath = dp.path.asAndroidPath()
            val nativePath = android.graphics.Path(androidPath)
            nativePath.transform(matrix)
            canvas.drawPath(nativePath, paint)
        }

        // 4. Render watermarks
        for (wm in watermarks) {
            // First, background if any
            val textPaint = Paint().apply {
                color = wm.color.toArgb()
                // Apply density to font size to match UI scale
                textSize = (wm.fontSize * wm.scale * density) / scale
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val x = (wm.offsetX - offsetX) / scale
            val y = (wm.offsetY - offsetY) / scale

            // In Compose Text is rendered from top-left, but Canvas.drawText is from baseline.
            val fontMetrics = textPaint.fontMetrics
            val baselineY = y - fontMetrics.top
            
            // Draw background
            if (wm.bgStyle != WatermarkBgStyle.NONE) {
                val textWidth = textPaint.measureText(wm.text)
                val bgPadding = (8f * density) / scale
                val bgPaint = Paint().apply {
                    color = when(wm.bgStyle) {
                        WatermarkBgStyle.BLACK -> Color.Black.copy(alpha = 0.55f).toArgb()
                        WatermarkBgStyle.WHITE -> Color.White.copy(alpha = 0.7f).toArgb()
                        else -> 0
                    }
                }
                canvas.drawRoundRect(
                    x - bgPadding,
                    y,
                    x + textWidth + bgPadding,
                    y - fontMetrics.top + fontMetrics.bottom,
                    (6f * density) / scale, (6f * density) / scale,
                    bgPaint
                )
            }
            
            canvas.drawText(wm.text, x, baselineY, textPaint)
        }

        // 5. Apply crop
        val result = if (cropTopLeft != null && cropBottomRight != null) {
            val x = (cropTopLeft.x * mutable.width).toInt().coerceIn(0, mutable.width - 1)
            val y = (cropTopLeft.y * mutable.height).toInt().coerceIn(0, mutable.height - 1)
            val w = ((cropBottomRight.x - cropTopLeft.x) * mutable.width).toInt().coerceIn(1, mutable.width - x)
            val h = ((cropBottomRight.y - cropTopLeft.y) * mutable.height).toInt().coerceIn(1, mutable.height - y)
            Bitmap.createBitmap(mutable, x, y, w, h)
        } else {
            mutable
        }

        val outputFile = File(context.cacheDir, "edited_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { fos ->
            result.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        }
        if (result !== mutable) result.recycle()
        mutable.recycle()
        original.recycle()
        Uri.fromFile(outputFile)
    } catch (e: Exception) {
        android.util.Log.e("MediaEditScreen", "Failed to apply edits: ${e.message}", e)
        null
    }
}
