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
// 🔒 Content DRM Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/content")
class ContentDrmController(
    private val contentDrmService: ContentDrmService
) {
    // ── Content Management ──

    @PostMapping
    fun uploadContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: UploadLockedContentRequest
    ): ResponseEntity<ApiResponse<LockedContentResponse>> {
        val result: LockedContentResponse = contentDrmService.uploadContent(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Content uploaded", data = result))
    }

    @PutMapping("/{id}")
    fun updateContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: UpdateLockedContentRequest
    ): ResponseEntity<ApiResponse<LockedContentResponse>> {
        val result: LockedContentResponse = contentDrmService.updateContent(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Content updated", data = result))
    }

    @DeleteMapping("/{id}")
    fun deleteContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        contentDrmService.deleteContent(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Content deleted", data = Unit))
    }

    @GetMapping("/{id}")
    fun getContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<LockedContentResponse>> {
        val result: LockedContentResponse = contentDrmService.getContentById(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/channel/{channelId}")
    fun getChannelContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable channelId: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LockedContentResponse>>> {
        val result: Page<LockedContentResponse> = contentDrmService.getChannelContent(channelId, principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/my")
    fun getMyUploads(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<LockedContentResponse>>> {
        val result: Page<LockedContentResponse> = contentDrmService.getMyUploadedContent(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── Purchase ──

    @PostMapping("/{id}/purchase")
    fun purchaseContent(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ContentPurchaseResponse>> {
        val result: ContentPurchaseResponse = contentDrmService.purchaseContent(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Content purchased", data = result))
    }

    @GetMapping("/purchases")
    fun getMyPurchases(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ContentPurchaseResponse>>> {
        val result: Page<ContentPurchaseResponse> = contentDrmService.getMyPurchases(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{id}/purchased")
    fun hasPurchased(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val result: Boolean = contentDrmService.hasPurchased(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── Access (Streaming) ──

    @GetMapping("/{id}/access")
    fun getContentAccess(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ContentAccessResponse>> {
        val result: ContentAccessResponse = contentDrmService.getContentAccess(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}
