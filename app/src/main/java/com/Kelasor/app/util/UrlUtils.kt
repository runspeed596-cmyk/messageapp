package com.Kelasor.app.util

/**
 * Utility for handling and normalizing URLs in the application.
 */
object UrlUtils {
    /**
     * Resolves a full URL from a potentially relative media path.
     * Prepends BASE_URL if the path is relative.
     */
    fun getFullUrl(mediaUrl: String?): String? {
        if (mediaUrl == null) return null
        if (mediaUrl.isBlank()) return ""
        
        return if (mediaUrl.startsWith("http://") || 
            mediaUrl.startsWith("https://") ||
            mediaUrl.startsWith("content://") ||
            mediaUrl.startsWith("file://")
        ) {
            mediaUrl
        } else {
            val baseUrl = Constants.BASE_URL.removeSuffix("/")
            val normalizedPath = if (mediaUrl.startsWith("/")) mediaUrl else "/$mediaUrl"
            "$baseUrl$normalizedPath"
        }
    }
}
