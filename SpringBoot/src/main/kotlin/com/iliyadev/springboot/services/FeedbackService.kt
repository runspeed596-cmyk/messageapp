package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.FeedbackRepository
import com.iliyadev.springboot.repositories.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val userRepository: UserRepository
) {

    fun submitFeedback(userId: UUID, request: CreateFeedbackRequest): FeedbackResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        val feedback = Feedback(
            user = user,
            title = request.title,
            description = request.description,
            rating = request.rating,
            status = FeedbackStatus.OPEN
        )

        val saved = feedbackRepository.save(feedback)
        return mapToResponse(saved)
    }

    fun getAllFeedbacks(pageable: Pageable): Page<FeedbackResponse> {
        return feedbackRepository.findAll(pageable).map { mapToResponse(it) }
    }

    fun updateFeedbackStatus(id: UUID, request: UpdateFeedbackStatusRequest): FeedbackResponse {
        val feedback = feedbackRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Feedback not found") }

        feedback.status = FeedbackStatus.valueOf(request.status)
        request.adminNote?.let { feedback.adminNote = it }

        val saved = feedbackRepository.save(feedback)
        return mapToResponse(saved)
    }

    private fun mapToResponse(entity: Feedback): FeedbackResponse {
        return FeedbackResponse(
            id = entity.id!!,
            userId = entity.user?.id,
            userDisplayName = entity.user?.displayName,
            title = entity.title,
            description = entity.description,
            rating = entity.rating,
            status = entity.status.name,
            createdAt = entity.createdAt,
            adminNote = entity.adminNote
        )
    }
}
