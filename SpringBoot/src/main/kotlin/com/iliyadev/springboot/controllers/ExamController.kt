package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📝 Exam Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/exams")
class ExamController(
    private val examService: ExamService
) {
    // ── Exam CRUD ──

    @PostMapping
    fun createExam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateExamRequest
    ): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.createExam(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam created", data = result))
    }

    @PutMapping("/{id}")
    fun updateExam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: UpdateExamRequest
    ): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.updateExam(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam updated", data = result))
    }

    @PostMapping("/{id}/activate")
    fun activateExam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.activateExam(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam activated", data = result))
    }

    @PostMapping("/{id}/end")
    fun endExam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.endExam(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam ended", data = result))
    }

    @PostMapping("/{id}/graded")
    fun markAsGraded(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.markExamAsGraded(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam marked as graded", data = result))
    }

    @GetMapping("/{id}")
    fun getExam(@PathVariable id: UUID): ResponseEntity<ApiResponse<ExamResponse>> {
        val result: ExamResponse = examService.getExamById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/my")
    fun getMyExams(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ExamResponse>>> {
        val result: Page<ExamResponse> = examService.getMyExams(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/course/{courseId}")
    fun getCourseExams(@PathVariable courseId: UUID): ResponseEntity<ApiResponse<List<ExamResponse>>> {
        val result: List<ExamResponse> = examService.getCourseExams(courseId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/channel/{channelId}")
    fun getChannelExams(
        @PathVariable channelId: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ExamResponse>>> {
        val result: Page<ExamResponse> = examService.getChannelExams(channelId, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── Questions ──

    @PostMapping("/{id}/questions")
    fun addQuestion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: AddQuestionRequest
    ): ResponseEntity<ApiResponse<ExamQuestionResponse>> {
        val result: ExamQuestionResponse = examService.addQuestion(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Question added", data = result))
    }

    @DeleteMapping("/questions/{questionId}")
    fun deleteQuestion(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable questionId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        examService.deleteQuestion(questionId, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Question deleted", data = Unit))
    }

    @GetMapping("/{id}/questions")
    fun getQuestions(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<ExamQuestionResponse>>> {
        val result: List<ExamQuestionResponse> = examService.getExamQuestions(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── Attempt Lifecycle ──

    @PostMapping("/{id}/start")
    fun startAttempt(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ExamAttemptResponse>> {
        val result: ExamAttemptResponse = examService.startAttempt(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Attempt started", data = result))
    }

    @PostMapping("/attempts/{attemptId}/answer")
    fun submitAnswer(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable attemptId: UUID,
        @RequestBody request: SubmitAnswerRequest
    ): ResponseEntity<ApiResponse<ExamAnswerResponse>> {
        val result: ExamAnswerResponse = examService.submitAnswer(attemptId, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Answer saved", data = result))
    }

    @PostMapping("/attempts/{attemptId}/submit")
    fun submitExam(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable attemptId: UUID
    ): ResponseEntity<ApiResponse<ExamAttemptResponse>> {
        val result: ExamAttemptResponse = examService.submitExam(attemptId, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Exam submitted", data = result))
    }

    // ── Results & Grading ──

    @GetMapping("/attempts/{attemptId}/results")
    fun getResults(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable attemptId: UUID
    ): ResponseEntity<ApiResponse<ExamResultResponse>> {
        val result: ExamResultResponse = examService.getExamResult(attemptId, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{id}/attempts")
    fun getAttempts(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<ExamAttemptResponse>>> {
        val result: Page<ExamAttemptResponse> = examService.getExamAttempts(id, principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/answers/{answerId}/grade")
    fun gradeAnswer(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable answerId: UUID,
        @RequestParam score: BigDecimal
    ): ResponseEntity<ApiResponse<ExamAnswerResponse>> {
        val result: ExamAnswerResponse = examService.gradeAnswer(answerId, principal.id, score)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Answer graded", data = result))
    }
}
