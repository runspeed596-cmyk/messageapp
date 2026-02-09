package com.Kelasor.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ═══════════════════════════════════════════════════════════════════════════════
// 🔷 Premium Shape System - com.Kelasor.app
// ═══════════════════════════════════════════════════════════════════════════════

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Premium Chat Bubble Shapes
// ═══════════════════════════════════════════════════════════════════════════════

object MessageShapes {
    // My message bubble (sender) - RTL: right side - More rounded premium feel
    val myBubbleFirst = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 6.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
    val myBubbleMiddle = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 6.dp,
        bottomStart = 24.dp,
        bottomEnd = 6.dp
    )
    val myBubbleLast = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp,
        bottomEnd = 6.dp
    )
    val myBubbleSingle = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 6.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
    
    // Other message bubble (receiver) - RTL: left side
    val otherBubbleFirst = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
    val otherBubbleMiddle = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 24.dp,
        bottomStart = 6.dp,
        bottomEnd = 24.dp
    )
    val otherBubbleLast = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 6.dp,
        bottomEnd = 24.dp
    )
    val otherBubbleSingle = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 24.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎴 Premium Card & Container Shapes
// ═══════════════════════════════════════════════════════════════════════════════

object CardShapes {
    // Chat & List Items
    val chatItem = RoundedCornerShape(0.dp)
    val chatItemHover = RoundedCornerShape(16.dp)
    val chatBubble = RoundedCornerShape(20.dp)
    
    // Glass Cards
    val glassCard = RoundedCornerShape(24.dp)
    val glassCardLarge = RoundedCornerShape(32.dp)
    val glassCardSmall = RoundedCornerShape(16.dp)
    
    // Input Fields
    val inputField = RoundedCornerShape(28.dp)
    val inputFieldSmall = RoundedCornerShape(20.dp)
    val searchBar = RoundedCornerShape(32.dp)
    
    // Avatar
    val avatar = RoundedCornerShape(50)
    
    // Buttons
    val button = RoundedCornerShape(16.dp)
    val buttonSmall = RoundedCornerShape(12.dp)
    val buttonPill = RoundedCornerShape(50)
    
    // FAB
    val fab = RoundedCornerShape(20.dp)
    val fabMini = RoundedCornerShape(16.dp)
    
    // Sheets & Dialogs
    val bottomSheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    val dialog = RoundedCornerShape(28.dp)
    val dialogSmall = RoundedCornerShape(20.dp)
    
    // Media
    val imagePreview = RoundedCornerShape(16.dp)
    val imageLarge = RoundedCornerShape(20.dp)
    val videoThumbnail = RoundedCornerShape(12.dp)
    
    // Badges & Tags
    val badge = RoundedCornerShape(50)
    val tag = RoundedCornerShape(8.dp)
    
    // Chips
    val chip = RoundedCornerShape(20.dp)
    val chipSmall = RoundedCornerShape(16.dp)
    
    // Navigation
    val navBar = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val navPill = RoundedCornerShape(50)
    
    // Profile
    val profileHeader = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
    val profileCard = RoundedCornerShape(24.dp)
    
    // Story Ring
    val storyRing = RoundedCornerShape(50)
    
    // Message Input
    val messageInput = RoundedCornerShape(28.dp)
    val sendButton = RoundedCornerShape(50)
    val attachmentButton = RoundedCornerShape(16.dp)
}

// ═══════════════════════════════════════════════════════════════════════════════
// ✨ Animation & Effect Shapes
// ═══════════════════════════════════════════════════════════════════════════════

object EffectShapes {
    val glow = RoundedCornerShape(50)
    val ripple = RoundedCornerShape(50)
    val blur = RoundedCornerShape(24.dp)
    val skeleton = RoundedCornerShape(12.dp)
}
