package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.CourseCollaborationRequestRepository
import com.iliyadev.springboot.repositories.CourseRepository
import com.iliyadev.springboot.repositories.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class CourseCollaborationService(
    private val collaborationRepository: CourseCollaborationRequestRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createRequest(userId: UUID, courseId: UUID, request: CreateCollaborationRequest): CourseCollaborationRequestDto {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (course.organizer?.id != userId && course.institutionId != user.institutionId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the course organizer or their institution can invite collaborators")
        }

        val senderInstitutionId = course.institutionId ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Course must belong to an institution to collaborate")

        if (senderInstitutionId == request.targetInstitutionId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot collaborate with your own institution")
        }

        val collaborationRequest = CourseCollaborationRequest(
            course = course,
            senderInstitutionId = senderInstitutionId,
            targetInstitutionId = request.targetInstitutionId,
            status = CollaborationStatus.PENDING,
            message = request.message
        )

        val saved = collaborationRepository.save(collaborationRequest)
        return mapToDto(saved)
    }

    fun getPendingRequests(userId: UUID, academyId: UUID, pageable: Pageable): Page<CourseCollaborationRequestDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (user.institutionId != academyId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not belong to this academy")
        }

        return collaborationRepository.findByTargetInstitutionIdAndStatusOrderByCreatedAtDesc(
            academyId,
            CollaborationStatus.PENDING,
            pageable
        ).map { mapToDto(it) }
    }

    @Transactional
    fun acceptRequest(userId: UUID, requestId: UUID): CourseCollaborationRequestDto {
        val request = collaborationRepository.findById(requestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (user.institutionId != request.targetInstitutionId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not belong to the target academy")
        }

        if (request.status != CollaborationStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is already processed")
        }

        request.status = CollaborationStatus.ACCEPTED
        val saved = collaborationRepository.save(request)

        // Add target institution to course collaborators
        val course = request.course!!
        val collaborators = course.collaborators.toMutableList()
        collaborators.add(request.targetInstitutionId.toString())
        course.collaborators = collaborators
        courseRepository.save(course)

        return mapToDto(saved)
    }

    @Transactional
    fun rejectRequest(userId: UUID, requestId: UUID): CourseCollaborationRequestDto {
        val request = collaborationRepository.findById(requestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (user.institutionId != request.targetInstitutionId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not belong to the target academy")
        }

        if (request.status != CollaborationStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is already processed")
        }

        request.status = CollaborationStatus.REJECTED
        val saved = collaborationRepository.save(request)

        return mapToDto(saved)
    }

    private fun mapToDto(request: CourseCollaborationRequest): CourseCollaborationRequestDto {
        return CourseCollaborationRequestDto(
            id = request.id!!,
            courseId = request.course!!.id!!,
            courseTitle = request.course!!.title,
            senderInstitutionId = request.senderInstitutionId!!,
            senderInstitutionName = "Academy", // We don't have Institution repository directly accessible, placeholder for now
            targetInstitutionId = request.targetInstitutionId!!,
            status = request.status.name,
            message = request.message,
            createdAt = request.createdAt
        )
    }
}
