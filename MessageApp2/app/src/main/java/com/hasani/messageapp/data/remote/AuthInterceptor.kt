package com.hasani.messageapp.data.remote

import com.hasani.messageapp.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor that adds JWT Bearer token to authenticated requests.
 * Automatically reads the token from SessionManager and adds it to the Authorization header.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    companion object {
        private val EXCLUDED_PATHS = listOf(
            "/api/auth/send-otp",
            "/api/auth/verify-otp",
            "/api/auth/refresh-token"
        )
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath
        // Skip authentication for excluded paths
        if (EXCLUDED_PATHS.any { requestPath.contains(it) }) {
            return chain.proceed(originalRequest)
        }
        // Get token from session manager
        val token = runBlocking { sessionManager.accessToken.first() }
        // If no token, proceed without auth header
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        // Add Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authenticatedRequest)
    }
}
