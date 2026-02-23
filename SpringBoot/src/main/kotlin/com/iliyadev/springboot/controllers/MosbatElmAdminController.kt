package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 🛡️ Mosbat Elm Admin Controller
//    Manages teacher verification reviews, institution approvals,
//    subscription plan CRUD, and smart folder rule management.
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/admin/mosbat-elm")
class MosbatElmAdminController(
    private val teacherService: TeacherVerificationService,
    private val institutionService: InstitutionService,
    private val subscriptionService: SubscriptionService
) {
    // ── Teacher Verification ──

    @GetMapping("/teacher-requests")
    fun getPendingTeacherRequests(pageable: Pageable): ResponseEntity<ApiResponse<Page<TeacherVerificationResponse>>> {
        val result: Page<TeacherVerificationResponse> = teacherService.getPendingRequests(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/teacher-requests/all")
    fun getAllTeacherRequests(pageable: Pageable): ResponseEntity<ApiResponse<Page<TeacherVerificationResponse>>> {
        val result: Page<TeacherVerificationResponse> = teacherService.getAllRequests(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/teacher-requests/{id}/review")
    fun reviewTeacherRequest(
        @PathVariable id: UUID,
        @RequestParam adminId: UUID,
        @RequestBody request: TeacherVerificationReviewRequest
    ): ResponseEntity<ApiResponse<TeacherVerificationResponse>> {
        val result: TeacherVerificationResponse = teacherService.reviewRequest(id, adminId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Request reviewed", data = result))
    }

    // ── Institution (Elm Club) Management ──

    @GetMapping("/institutions/pending")
    fun getPendingInstitutions(pageable: Pageable): ResponseEntity<ApiResponse<Page<InstitutionResponse>>> {
        val result: Page<InstitutionResponse> = institutionService.getPendingInstitutions(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/institutions/{id}/review")
    fun reviewInstitution(
        @PathVariable id: UUID,
        @RequestParam adminId: UUID,
        @RequestBody request: InstitutionReviewRequest
    ): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = institutionService.reviewInstitution(id, adminId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Institution reviewed", data = result))
    }

    // ── Subscription Plans CRUD ──

    @PostMapping("/subscription-plans")
    fun createPlan(@RequestBody request: CreateSubscriptionPlanRequest): ResponseEntity<ApiResponse<SubscriptionPlanResponse>> {
        val result: SubscriptionPlanResponse = subscriptionService.createPlan(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Plan created", data = result))
    }

    @PostMapping("/subscription-plans/{id}/deactivate")
    fun deactivatePlan(@PathVariable id: UUID): ResponseEntity<ApiResponse<SubscriptionPlanResponse>> {
        val result: SubscriptionPlanResponse = subscriptionService.deactivatePlan(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Plan deactivated", data = result))
    }
}
