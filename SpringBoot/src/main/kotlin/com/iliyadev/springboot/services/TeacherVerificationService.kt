package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class TeacherVerificationSubmitRequest(
    val fullName: String,
    val nationalCode: String,
    val teachingField: String,
    val institution: String? = null,
    val documentUrls: List<String> = emptyList()
)

data class TeacherVerificationReviewRequest(
    val status: VerificationStatus, // APPROVED or REJECTED
    val adminNote: String? = null
)

data class TeacherVerificationResponse(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val nationalCode: String,
    val teachingField: String,
    val institution: String?,
    val documentUrls: List<String>,
    val status: VerificationStatus,
    val adminNote: String?,
    val createdAt: Instant,
    val reviewedAt: Instant?
)

@Service
class TeacherVerificationService(
    private val verificationRepo: TeacherVerificationRequestRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository
) {
    @Transactional
    fun submitVerificationRequest(userId: UUID, request: TeacherVerificationSubmitRequest): TeacherVerificationResponse {
        val hasPending: Boolean = verificationRepo.existsByUserIdAndStatus(userId, VerificationStatus.PENDING_VERIFICATION)
        if (hasPending) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You already have a pending verification request")
        }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (user.role == UserRole.TEACHER) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User is already a verified teacher")
        }
        val entity = TeacherVerificationRequest(
            user = user,
            fullName = request.fullName,
            nationalCode = request.nationalCode,
            teachingField = request.teachingField,
            institution = request.institution,
            documentUrls = request.documentUrls.toMutableList(),
            status = VerificationStatus.PENDING_VERIFICATION
        )
        val saved: TeacherVerificationRequest = verificationRepo.save(entity)
        return mapToResponse(saved)
    }

    fun getMyVerificationStatus(userId: UUID): TeacherVerificationResponse? {
        val latest: TeacherVerificationRequest = verificationRepo.findTopByUserIdOrderByCreatedAtDesc(userId)
            ?: return null
        return mapToResponse(latest)
    }

    fun getPendingRequests(pageable: Pageable): Page<TeacherVerificationResponse> {
        return verificationRepo.findByStatus(VerificationStatus.PENDING_VERIFICATION, pageable)
            .map { mapToResponse(it) }
    }

    fun getAllRequests(pageable: Pageable): Page<TeacherVerificationResponse> {
        return verificationRepo.findAll(pageable)
            .map { mapToResponse(it) }
    }

    @Transactional
    fun reviewRequest(requestId: UUID, adminId: UUID, review: TeacherVerificationReviewRequest): TeacherVerificationResponse {
        val entity: TeacherVerificationRequest = verificationRepo.findById(requestId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found") }
        if (entity.status != VerificationStatus.PENDING_VERIFICATION) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Request has already been reviewed")
        }
        if (review.status != VerificationStatus.APPROVED && review.status != VerificationStatus.REJECTED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid review status")
        }
        entity.status = review.status
        entity.adminNote = review.adminNote
        entity.reviewedBy = adminId
        entity.reviewedAt = Instant.now()
        entity.updatedAt = Instant.now()
        if (review.status == VerificationStatus.APPROVED) {
            promoteUserToTeacher(entity.user!!.id!!)
        }
        val saved: TeacherVerificationRequest = verificationRepo.save(entity)
        return mapToResponse(saved)
    }

    @Transactional
    fun promoteUserToTeacher(userId: UUID) {
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        user.role = UserRole.TEACHER
        userRepository.save(user)
        // Mark all channels owned by this user as verified teacher channels
        var channels: List<Channel> = channelRepository.findByOwnerId(userId)
        if (channels.isEmpty()) {
            // Auto-create a default channel for the teacher
            val channel = Channel(
                name = "کانال ${user.displayName}",
                description = "کانال رسمی استاد ${user.displayName}",
                isPublic = true,
                owner = user,
                classification = ChannelClassification.VERIFIED_TEACHER,
                isVerifiedTeacher = true
            )
            val savedChannel: Channel = channelRepository.save(channel)
            // Link to user's bio channel slot
            if (user.bioChannelId1 == null) {
                user.bioChannelId1 = savedChannel.id
            } else if (user.bioChannelId2 == null) {
                user.bioChannelId2 = savedChannel.id
            }
            userRepository.save(user)
            channels = listOf(savedChannel)
        }
        channels.forEach { channel ->
            channel.isVerifiedTeacher = true
            channel.classification = ChannelClassification.VERIFIED_TEACHER
        }
        channelRepository.saveAll(channels)
    }

    private fun mapToResponse(entity: TeacherVerificationRequest): TeacherVerificationResponse {
        return TeacherVerificationResponse(
            id = entity.id!!,
            userId = entity.user!!.id!!,
            fullName = entity.fullName,
            nationalCode = entity.nationalCode,
            teachingField = entity.teachingField,
            institution = entity.institution,
            documentUrls = entity.documentUrls,
            status = entity.status,
            adminNote = entity.adminNote,
            createdAt = entity.createdAt,
            reviewedAt = entity.reviewedAt
        )
    }
}
