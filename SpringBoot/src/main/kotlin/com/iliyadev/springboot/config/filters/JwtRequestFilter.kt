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
                if (jwtTokenUtils.validateToken(token)) {
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
                }
            } catch (e: Exception) {
                // Just log it. Spring Security will block unauthorized requests based on config.
                println("JWT_FILTER_ERROR: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
