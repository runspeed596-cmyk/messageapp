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

data class InstitutionRegisterRequest(
    val name: String,
    val type: String,
    val registrationNumber: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val logoUrl: String? = null
)

data class InstitutionReviewRequest(
    val status: VerificationStatus, // APPROVED or REJECTED
    val adminNote: String? = null
)

data class InstitutionResponse(
    val id: UUID,
    val name: String,
    val type: String,
    val registrationNumber: String?,
    val contactPhone: String?,
    val contactEmail: String?,
    val province: String?,
    val city: String?,
    val address: String?,
    val logoUrl: String?,
    val ownerId: UUID,
    val channelId: UUID?,
    val verificationStatus: VerificationStatus,
    val adminNote: String?,
    val isActive: Boolean,
    val createdAt: Instant
)

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository
) {
    @Transactional
    fun registerInstitution(ownerId: UUID, request: InstitutionRegisterRequest): InstitutionResponse {
        val owner: User = userRepository.findById(ownerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (request.registrationNumber != null && institutionRepository.existsByRegistrationNumber(request.registrationNumber)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Registration number already exists")
        }
        val institution = Institution(
            name = request.name,
            type = request.type,
            registrationNumber = request.registrationNumber,
            contactPhone = request.contactPhone,
            contactEmail = request.contactEmail,
            province = request.province,
            city = request.city,
            address = request.address,
            logoUrl = request.logoUrl,
            owner = owner,
            verificationStatus = VerificationStatus.PENDING_VERIFICATION
        )
        val saved: Institution = institutionRepository.save(institution)
        return mapToResponse(saved)
    }

    fun getMyInstitutions(ownerId: UUID): List<InstitutionResponse> {
        return institutionRepository.findByOwnerId(ownerId).map { mapToResponse(it) }
    }

    fun getInstitutionById(institutionId: UUID): InstitutionResponse {
        val entity: Institution = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        return mapToResponse(entity)
    }

    fun getActiveInstitutions(pageable: Pageable): Page<InstitutionResponse> {
        return institutionRepository.findByIsActiveTrue(pageable).map { mapToResponse(it) }
    }

    fun getPendingInstitutions(pageable: Pageable): Page<InstitutionResponse> {
        return institutionRepository.findByVerificationStatus(VerificationStatus.PENDING_VERIFICATION, pageable)
            .map { mapToResponse(it) }
    }

    @Transactional
    fun reviewInstitution(institutionId: UUID, adminId: UUID, review: InstitutionReviewRequest): InstitutionResponse {
        val entity: Institution = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        if (entity.verificationStatus != VerificationStatus.PENDING_VERIFICATION) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Institution has already been reviewed")
        }
        entity.verificationStatus = review.status
        entity.adminNote = review.adminNote
        entity.reviewedBy = adminId
        entity.reviewedAt = Instant.now()
        entity.updatedAt = Instant.now()
        if (review.status == VerificationStatus.APPROVED) {
            entity.isActive = true
            // Update user role to INSTITUTION
            val ownerUser: User = entity.owner!!
            if (ownerUser.role == UserRole.NORMAL) {
                ownerUser.role = UserRole.INSTITUTION
                userRepository.save(ownerUser)
            }
            if (entity.channel == null) {
                // Auto-create a channel for the institution
                val autoChannel = Channel(
                    name = entity.name,
                    description = "کانال رسمی ${entity.name}",
                    isPublic = true,
                    owner = ownerUser,
                    classification = ChannelClassification.ELM_CLUB_INSTITUTION,
                    institutionId = entity.id
                )
                val savedChannel: Channel = channelRepository.save(autoChannel)
                entity.channel = savedChannel
            } else {
                // Mark linked channel as institution channel
                val channel: Channel = entity.channel!!
                channel.classification = ChannelClassification.ELM_CLUB_INSTITUTION
                channel.institutionId = entity.id
                channelRepository.save(channel)
            }
        }
        val saved: Institution = institutionRepository.save(entity)
        return mapToResponse(saved)
    }

    @Transactional
    fun linkChannel(institutionId: UUID, channelId: UUID, ownerId: UUID): InstitutionResponse {
        val entity: Institution = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        if (entity.owner?.id != ownerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the institution owner can link a channel")
        }
        val channel: Channel = channelRepository.findById(channelId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found") }
        if (channel.owner?.id != ownerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You must own the channel to link it")
        }
        entity.channel = channel
        entity.updatedAt = Instant.now()
        if (entity.verificationStatus == VerificationStatus.APPROVED) {
            channel.classification = ChannelClassification.ELM_CLUB_INSTITUTION
            channel.institutionId = entity.id
            channelRepository.save(channel)
        }
        val saved: Institution = institutionRepository.save(entity)
        return mapToResponse(saved)
    }

    private fun mapToResponse(entity: Institution): InstitutionResponse {
        return InstitutionResponse(
            id = entity.id!!,
            name = entity.name,
            type = entity.type,
            registrationNumber = entity.registrationNumber,
            contactPhone = entity.contactPhone,
            contactEmail = entity.contactEmail,
            province = entity.province,
            city = entity.city,
            address = entity.address,
            logoUrl = entity.logoUrl,
            ownerId = entity.owner!!.id!!,
            channelId = entity.channel?.id,
            verificationStatus = entity.verificationStatus,
            adminNote = entity.adminNote,
            isActive = entity.isActive,
            createdAt = entity.createdAt
        )
    }
}
