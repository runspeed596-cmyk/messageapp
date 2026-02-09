package com.Kelasor.app.ui.screens.elm

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp



@Composable
fun Globe3D(
    modifier: Modifier = Modifier
) {
    // Just the visual overlay (Atmosphere/Glow)
    // Multiple layers for a professional holographic depth
    Box(
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension * 0.48f // Globe radius
            
            // 1. Outer Glow (Large, Faint Blue)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00B0FF).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.5f
                ),
                radius = radius * 1.5f
            )

            // 2. Atmospheric Layer (Cyan Halo)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.25f),
                        Color(0xFF00E5FF).copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius * 1.1f
                ),
                radius = radius * 1.1f
            )

            // 3. Inner Rim Highlight (Cyber Edge)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF00E5FF).copy(alpha = 0.1f),
                        Color(0xFF00E5FF).copy(alpha = 0.4f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
            )
            
            // 4. Subtle Scanlines (Simulated with thin lines)
            val scanlineCount = 20
            for (i in 0 until scanlineCount) {
                val y = centerY - radius + (radius * 2 * i / scanlineCount)
                val width = Math.sqrt(Math.pow(radius.toDouble(), 2.0) - Math.pow((y - centerY).toDouble(), 2.0)).toFloat()
                if (!width.isNaN()) {
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.05f),
                        start = Offset(centerX - width, y),
                        end = Offset(centerX + width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
    }
}



