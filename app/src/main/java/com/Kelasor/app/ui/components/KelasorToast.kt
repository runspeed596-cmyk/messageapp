package com.Kelasor.app.ui.components

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Kelasor.app.ui.theme.DanaFontFamily
import kotlinx.coroutines.delay

enum class ToastType {
    SUCCESS, ERROR, INFO
}

object KelasorToast {
    fun show(
        context: Context,
        message: String,
        type: ToastType = ToastType.INFO,
        durationMillis: Long = 5000
    ) {
        val activity = context as? Activity ?: return
        val decorView = activity.window.decorView as? ViewGroup ?: return
        
        val composeView = ComposeView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                bottomMargin = 250 // Push it slightly above bottom navigation if present
            }
        }
        
        decorView.addView(composeView)
        
        composeView.setContent {
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                visible = true
                delay(durationMillis)
                visible = false
                delay(300) // wait for exit animation
                decorView.removeView(composeView)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(tween(300))
                ) {
                    val backgroundColor = when (type) {
                        ToastType.SUCCESS -> Color(0xFF4CAF50)
                        ToastType.ERROR -> Color(0xFFE53935)
                        ToastType.INFO -> Color(0xFF2196F3)
                    }
                    val icon = when (type) {
                        ToastType.SUCCESS -> Icons.Default.CheckCircle
                        ToastType.ERROR -> Icons.Default.Warning
                        ToastType.INFO -> Icons.Default.Info
                    }
                    
                    Row(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = message,
                            color = Color.White,
                            fontFamily = DanaFontFamily,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
