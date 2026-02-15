package com.iliyadev.springboot.config.filters

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.config.security.UserPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtRequestFilter(
    private val jwtTokenUtils: JwtTokenUtils
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val isValid = jwtTokenUtils.validateToken(token)
                println("JWT_FILTER: Token validation result = $isValid for ${request.method} ${request.requestURI}")
                if (isValid) {
                    val userId = jwtTokenUtils.getUserIdFromToken(token)
                    val uuid = UUID.fromString(userId)
                    
                    // Create UserPrincipal matching the one used in STOMP
                    val principal = UserPrincipal(uuid, userId)
                    
                    val authentication = UsernamePasswordAuthenticationToken(
                        principal, null, emptyList()
                    )
                    
                    SecurityContextHolder.getContext().authentication = authentication
                    
                    // Keep compatibility for any legacy code using request attribute
                    request.setAttribute("userId", uuid)
                    println("JWT_FILTER: Authentication set for user $userId")
                } else {
                    println("JWT_FILTER: Token is INVALID or EXPIRED")
                }
            } catch (e: Exception) {
                println("JWT_FILTER_ERROR: ${e.javaClass.simpleName}: ${e.message}")
            }
        } else {
            println("JWT_FILTER: No Bearer token for ${request.method} ${request.requestURI}")
        }

        filterChain.doFilter(request, response)
    }
}
