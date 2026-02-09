package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.PollService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/polls")
class PollController(
    private val pollService: PollService
) {

    @PostMapping
    fun createPoll(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreatePollRequest
    ): ResponseEntity<ApiResponse<PollDto>> {
        return try {
            val poll = pollService.createPoll(userId, request)
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Poll created successfully",
                data = poll.toDto(userId)
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(
                success = false,
                message = "خطا در ایجاد نظرسنجی: ${e.message ?: "Unknown error"}",
                data = null
            ))
        }
    }

    @PostMapping("/{id}/vote")
    fun vote(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: VoteRequest
    ): ResponseEntity<ApiResponse<PollDto>> {
        return try {
            val updatedPoll = pollService.vote(userId, id, request.optionIds)
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Vote recorded successfully",
                data = updatedPoll.toDto(userId)
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(
                success = false,
                message = "خطا در ثبت رأی: ${e.message ?: "Unknown error"}",
                data = null
            ))
        }
    }
    
    @DeleteMapping("/{id}/vote")
    fun retractVote(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PollDto>> {
        return try {
            val updatedPoll = pollService.retractVote(userId, id)
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Vote retracted successfully",
                data = updatedPoll.toDto(userId)
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(
                success = false,
                message = "خطا در حذف رأی: ${e.message ?: "Unknown error"}",
                data = null
            ))
        }
    }
    
    @GetMapping("/{id}")
    fun getPoll(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<PollDto>> {
        return try {
            val poll = pollService.getPoll(id)
            ResponseEntity.ok(ApiResponse(
                success = true,
                message = "Poll retrieved successfully",
                data = poll.toDto(userId)
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(ApiResponse(
                success = false,
                message = "خطا در دریافت نظرسنجی: ${e.message ?: "Unknown error"}",
                data = null
            ))
        }
    }
}
