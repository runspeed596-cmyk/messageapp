package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// #️⃣ Hashtag Promotion Controller — Phase 5
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/hashtags")
class HashtagPromotionController(
    private val hashtagPromotionService: HashtagPromotionService
) {
    // ── Public Hashtag Listings ──

    @GetMapping
    fun getActiveHashtags(): ResponseEntity<ApiResponse<List<HashtagResponse>>> {
        val result: List<HashtagResponse> = hashtagPromotionService.getActiveHashtags()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/category/{category}")
    fun getByCategory(@PathVariable category: String): ResponseEntity<ApiResponse<List<HashtagResponse>>> {
        val result: List<HashtagResponse> = hashtagPromotionService.getHashtagsByCategory(category)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{hashtagId}/promotions")
    fun getApprovedPromotions(
        @PathVariable hashtagId: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<PromotionResponse>>> {
        val result: Page<PromotionResponse> = hashtagPromotionService.getApprovedPromotions(hashtagId, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── User Promotions ──

    @PostMapping("/promotions")
    fun submitPromotion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SubmitPromotionRequest
    ): ResponseEntity<ApiResponse<PromotionResponse>> {
        val result: PromotionResponse = hashtagPromotionService.submitPromotion(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Promotion submitted for review", data = result))
    }

    @GetMapping("/promotions/my")
    fun getMyPromotions(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<PromotionResponse>>> {
        val result: Page<PromotionResponse> = hashtagPromotionService.getMyPromotions(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// #️⃣ Hashtag Admin Controller — Phase 5 + 6
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/admin/hashtags")
class HashtagAdminController(
    private val hashtagPromotionService: HashtagPromotionService
) {
    @PostMapping
    fun createHashtag(@RequestBody request: CreateHashtagRequest): ResponseEntity<ApiResponse<HashtagResponse>> {
        val result: HashtagResponse = hashtagPromotionService.createHashtag(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Hashtag created", data = result))
    }

    @GetMapping("/all")
    fun getAllHashtags(): ResponseEntity<ApiResponse<List<HashtagResponse>>> {
        val result: List<HashtagResponse> = hashtagPromotionService.getAllHashtags()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/{id}/toggle")
    fun toggleHashtag(
        @PathVariable id: UUID,
        @RequestParam isActive: Boolean
    ): ResponseEntity<ApiResponse<HashtagResponse>> {
        val result: HashtagResponse = hashtagPromotionService.toggleHashtag(id, isActive)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Hashtag toggled", data = result))
    }

    @GetMapping("/promotions/pending")
    fun getPendingPromotions(pageable: Pageable): ResponseEntity<ApiResponse<Page<PromotionResponse>>> {
        val result: Page<PromotionResponse> = hashtagPromotionService.getPendingPromotions(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/promotions/{promotionId}/moderate")
    fun moderatePromotion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable promotionId: UUID,
        @RequestBody request: ModeratePromotionRequest
    ): ResponseEntity<ApiResponse<PromotionResponse>> {
        val result: PromotionResponse = hashtagPromotionService.moderatePromotion(promotionId, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Promotion moderated", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📊 Platform Health Controller — Phase 7
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/admin/platform")
class PlatformHealthController(
    private val platformHealthService: PlatformHealthService
) {
    @GetMapping("/stats")
    fun getPlatformStats(): ResponseEntity<ApiResponse<PlatformStatsResponse>> {
        val result: PlatformStatsResponse = platformHealthService.getPlatformStats()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/revenue")
    fun getRevenueStats(): ResponseEntity<ApiResponse<RevenueStatsResponse>> {
        val result: RevenueStatsResponse = platformHealthService.getRevenueStats()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}
