package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.services.PanelAdminService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(
    private val jwtTokenUtils: JwtTokenUtils,
    private val panelAdminService: PanelAdminService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AdminLoginRequest): ResponseEntity<ApiResponse<AdminLoginResponse>> {
        val admin = panelAdminService.authenticateAdmin(request.username, request.password)
            ?: return ResponseEntity.status(401)
                .body(ApiResponse(false, "نام کاربری یا رمز عبور اشتباه است"))
        val token = jwtTokenUtils.generateToken(admin.id.toString())
        return ResponseEntity.ok(
            ApiResponse(true, "ورود موفق", AdminLoginResponse(token, admin.isSuperAdmin, admin.id.toString(), admin.permissions))
        )
    }
}

data class AdminLoginRequest(val username: String, val password: String)
data class AdminLoginResponse(val token: String, val isSuperAdmin: Boolean = false, val adminId: String = "", val permissions: List<String> = emptyList())
