package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.CreateFeedbackRequest
import com.iliyadev.springboot.models.FeedbackResponse
import com.iliyadev.springboot.models.UpdateFeedbackStatusRequest
import com.iliyadev.springboot.services.FeedbackService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID
import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.ApiResponse

@RestController
@RequestMapping("/api")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @PostMapping("/feedback/submit")
    fun submitFeedback(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateFeedbackRequest
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val result = feedbackService.submitFeedback(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Feedback submitted", data = result))
    }

    @GetMapping("/admin/feedbacks")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun getAllFeedbacks(pageable: Pageable): ResponseEntity<ApiResponse<Page<FeedbackResponse>>> {
        val result = feedbackService.getAllFeedbacks(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PutMapping("/admin/feedbacks/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    fun updateFeedbackStatus(
        @PathVariable id: UUID,
        @RequestBody request: UpdateFeedbackStatusRequest
    ): ResponseEntity<ApiResponse<FeedbackResponse>> {
        val result = feedbackService.updateFeedbackStatus(id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Status updated", data = result))
    }
}
