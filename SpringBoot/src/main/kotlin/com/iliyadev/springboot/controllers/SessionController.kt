package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/sessions")
class SessionController(
    private val authService: AuthService
) {
    @GetMapping("/active")
    fun getActiveSessions(
        @RequestAttribute("userId") userId: UUID,
        @RequestAttribute("sessionId", required = false) sessionId: UUID?
    ): ResponseEntity<ApiResponse<List<DeviceSessionDto>>> {
        val sessions = authService.getActiveSessions(userId, sessionId)
        return ResponseEntity.ok(ApiResponse(true, "موفق", sessions))
    }

    @DeleteMapping("/{sessionId}")
    fun terminateSession(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable sessionId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = authService.terminateSession(userId, sessionId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "نشست با موفقیت پایان یافت"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در پایان نشست"))
        }
    }

    @DeleteMapping("/terminate-others")
    fun terminateOtherSessions(
        @RequestAttribute("userId") userId: UUID,
        @RequestAttribute("sessionId", required = false) sessionId: UUID?
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = authService.terminateAllOtherSessions(userId, sessionId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "سایر نشست‌ها پایان یافتند"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا"))
        }
    }
    
    @GetMapping("/test")
    fun test(): String = "Session Controller is Active"
}
