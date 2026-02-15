package com.Kelasor.app.domain.model

import java.time.Instant

data class SharedContent(
    val id: String,
    val type: MessageType,
    val url: String, // Media URL or Link URL
    val thumbnail: String? = null,
    val name: String? = null, // Filename or Link Title
    val caption: String? = null,
    val createdAt: Instant,
    val messageId: String,
    val chatId: String? = null
)
