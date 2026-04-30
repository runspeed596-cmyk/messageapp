package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.models.InstitutionResponse
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
    private val subscriptionService: SubscriptionService,
    private val courseService: CourseService
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

    // ── Course (Elm Club) Management ──

    @GetMapping("/courses/pending")
    fun getPendingCourses(pageable: Pageable): ResponseEntity<ApiResponse<Page<CourseResponse>>> {
        val result: Page<CourseResponse> = courseService.getPendingCourses(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/courses/{id}/review")
    fun reviewCourse(
        @PathVariable id: UUID,
        @RequestParam adminId: UUID,
        @RequestBody request: CourseReviewRequest
    ): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.reviewCourse(id, adminId, request.status, request.adminNote)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course reviewed", data = result))
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

data class CourseReviewRequest(
    val status: com.iliyadev.springboot.models.CourseStatus, // APPROVED or REJECTED
    val adminNote: String? = null
)
