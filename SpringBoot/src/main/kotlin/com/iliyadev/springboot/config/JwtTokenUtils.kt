package com.iliyadev.springboot.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*
import java.util.function.Function

@Component
class JwtTokenUtils : Serializable {
    companion object {
        private const val JWT_TOKEN_VALIDITY: Long = 24 * 60 * 60 // 24 hours
    }
    @Value("\${jwt.secret}")
    private lateinit var secret: String
    fun getUserIdFromToken(token: String): String {
        return getClaimFromToken(token, Function { it.subject })
    }
    fun getSessionIdFromToken(token: String): String? {
        return getClaimFromToken(token, Function { it["sessionId"] as? String })
    }
    fun getExpirationDateFromToken(token: String): Date {
        return getClaimFromToken(token, Function { it.expiration })
    }
    fun <T> getClaimFromToken(token: String, claimsResolver: Function<Claims, T>): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver.apply(claims)
    }
    private fun getAllClaimsFromToken(token: String?): Claims {
        val keyBytes = Base64.getDecoder().decode(secret)
        val key = Keys.hmacShaKeyFor(keyBytes)
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
    fun isTokenExpired(token: String): Boolean {
        return try {
            val expiration = getExpirationDateFromToken(token)
            expiration.before(Date())
        } catch (e: Exception) {
            true
        }
    }
    fun generateToken(userId: String, sessionId: String? = null): String {
        val claims: MutableMap<String, Any> = HashMap()
        if (sessionId != null) {
            claims["sessionId"] = sessionId
        }
        return doGenerateToken(claims, userId)
    }
    private fun doGenerateToken(claims: Map<String, Any>, subject: String): String {
        val keyBytes = Base64.getDecoder().decode(secret)
        val key = Keys.hmacShaKeyFor(keyBytes)
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }
    fun validateToken(token: String): Boolean {
        return try {
            !isTokenExpired(token) && getUserIdFromToken(token).isNotBlank()
        } catch (e: Exception) {
            false
        }
    }
}