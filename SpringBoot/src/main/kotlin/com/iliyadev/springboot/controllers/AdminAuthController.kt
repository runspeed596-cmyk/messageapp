package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.models.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(
    private val jwtTokenUtils: JwtTokenUtils
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AdminLoginRequest): ResponseEntity<ApiResponse<AdminLoginResponse>> {
        // Simple secure password for Super Admin (In production, use BCrypt and DB)
        if (request.username == "admin" && request.password == "Admin@123") {
            // Use a specific UUID for Super Admin to avoid parsing errors in JwtRequestFilter
            val token = jwtTokenUtils.generateToken("00000000-0000-0000-0000-000000000000")
            return ResponseEntity.ok(ApiResponse(true, "ورود موفق", AdminLoginResponse(token)))
        }
        return ResponseEntity.status(401).body(ApiResponse(false, "نام کاربری یا رمز عبور اشتباه است"))
    }
}

data class AdminLoginRequest(val username: String, val password: String)
data class AdminLoginResponse(val token: String)
