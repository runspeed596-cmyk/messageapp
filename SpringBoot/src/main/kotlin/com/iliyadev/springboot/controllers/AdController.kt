package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.AdService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Ad Controller — User endpoints
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/ads")
class AdController(
    private val adService: AdService
) {
    @PostMapping
    fun submitAdRequest(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreateAdRequest
    ): ResponseEntity<ApiResponse<AdRequestDto>> {
        val result = adService.createAdRequest(userId, request)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, "درخواست تبلیغ ثبت شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ثبت درخواست"))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Ad Admin Controller — Admin endpoints
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/admin/ads")
class AdAdminController(
    private val adService: AdService
) {
    @GetMapping
    fun getAdRequests(
        @RequestParam(required = false) status: String?
    ): ResponseEntity<ApiResponse<AdRequestListResponse>> {
        val requests = adService.getAdRequests(status)
        val response = AdRequestListResponse(
            adRequests = requests,
            totalCount = requests.size
        )
        return ResponseEntity.ok(ApiResponse(true, "موفق", response))
    }

    @PutMapping("/{id}/approve")
    fun approveAd(
        @RequestAttribute("userId") adminId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = adService.approveAd(id, adminId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "تبلیغ تایید و در کانال منتشر شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در تایید تبلیغ"))
        }
    }

    @PutMapping("/{id}/reject")
    fun rejectAd(
        @RequestAttribute("userId") adminId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = adService.rejectAd(id, adminId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "تبلیغ رد شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در رد تبلیغ"))
        }
    }
}
