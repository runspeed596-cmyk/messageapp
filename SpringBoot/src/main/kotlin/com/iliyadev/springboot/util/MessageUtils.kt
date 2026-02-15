package com.iliyadev.springboot.util

import com.iliyadev.springboot.models.MessageType

object MessageUtils {
    fun isLink(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        val urlPattern = Regex("""((https?://|www\.)[^\s]+|[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}[^\s]*)""", RegexOption.IGNORE_CASE)
        return urlPattern.containsMatchIn(text)
    }
}
