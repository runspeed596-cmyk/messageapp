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
import org.springframework.security.core.context.SecurityContextHolder
import com.iliyadev.springboot.config.security.UserPrincipal

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class InstitutionRegisterRequest(
    val name: String,
    val type: InstitutionType,
    val registrationNumber: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val province: String? = null,
    val city: String? = null,
    val address: String? = null,
    val logoUrl: String? = null,
    val description: String? = null,
    val isSubsidiary: Boolean = false,
    val dependencyDescription: String? = null,
    val universities: List<String> = emptyList(),
    val specialties: List<String> = emptyList(),
    val associatedClubIds: List<String> = emptyList(),
    val associatedFieldOfStudyIds: List<String> = emptyList(),
    val associatedStudentOrgIds: List<String> = emptyList(),
    val instructorIds: List<UUID> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val adminIds: List<UUID> = emptyList()
)

data class InstitutionReviewRequest(
    val status: VerificationStatus, // APPROVED or REJECTED
    val adminNote: String? = null
)

@Service
class InstitutionService(
    private val institutionRepository: InstitutionRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val courseRepository: CourseRepository,
    private val userFollowRepository: UserFollowRepository,
    private val enrollmentRepository: CourseEnrollmentRepository
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
            description = request.description,
            isSubsidiary = request.isSubsidiary,
            dependencyDescription = request.dependencyDescription,
            universities = request.universities.toMutableList(),
            specialties = request.specialties.toMutableList(),
            associatedClubIds = request.associatedClubIds.toMutableList(),
            associatedFieldOfStudyIds = request.associatedFieldOfStudyIds.toMutableList(),
            associatedStudentOrgIds = request.associatedStudentOrgIds.toMutableList(),
            instructorIds = request.instructorIds.toMutableList(),
            manualInstructors = request.manualInstructors.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) }.toMutableList(),
            adminIds = request.adminIds.toMutableList(),
            owner = owner,
            verificationStatus = VerificationStatus.PENDING_VERIFICATION
        )
        val saved: Institution = institutionRepository.save(institution)
        
        // Link to user (owner)
        owner.institutionId = saved.id
        owner.institutionLogoUrl = saved.logoUrl
        owner.institutionName = saved.name
        userRepository.save(owner)
        
        return mapToResponse(saved)
    }

    @Transactional
    fun updateInstitution(institutionId: UUID, ownerId: UUID, request: InstitutionRegisterRequest): InstitutionResponse {
        val entity: Institution = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        if (entity.owner?.id != ownerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the institution owner can update it")
        }
        entity.name = request.name
        entity.type = request.type
        if (request.logoUrl != null) entity.logoUrl = request.logoUrl
        if (request.description != null) entity.description = request.description
        entity.isSubsidiary = request.isSubsidiary
        entity.dependencyDescription = request.dependencyDescription
        if (request.province != null) entity.province = request.province
        if (request.city != null) entity.city = request.city
        entity.universities = request.universities.toMutableList()
        entity.specialties = request.specialties.toMutableList()
        entity.associatedClubIds = request.associatedClubIds.toMutableList()
        entity.associatedFieldOfStudyIds = request.associatedFieldOfStudyIds.toMutableList()
        entity.associatedStudentOrgIds = request.associatedStudentOrgIds.toMutableList()
        entity.instructorIds = request.instructorIds.toMutableList()
        entity.manualInstructors.clear()
        entity.manualInstructors.addAll(request.manualInstructors.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) })
        entity.adminIds = request.adminIds.toMutableList()
        entity.updatedAt = Instant.now()
        val saved: Institution = institutionRepository.save(entity)
        // Update user's cached institution fields
        val owner: User = entity.owner!!
        owner.institutionLogoUrl = saved.logoUrl
        owner.institutionName = saved.name
        userRepository.save(owner)
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
        val institutionId: UUID = entity.id!!
        
        // Fetch all approved courses for this institution
        val approvedCourses = courseRepository.findByInstitutionIdOrOrganizerId(institutionId, entity.owner!!.id!!, org.springframework.data.domain.Pageable.unpaged()).content
            .filter { it.status == com.iliyadev.springboot.models.CourseStatus.APPROVED }
            
        var totalTrainingHours = 0
        var totalPersonHours = 0
        var totalRevenue: Long = 0
        
        for (course in approvedCourses) {
            val durationHours = java.time.Duration.between(course.startsAt, course.endsAt).toHours().toInt()
            val enrolled = enrollmentRepository.countByCourseIdAndIsActiveTrue(course.id!!).toInt()
            
            totalTrainingHours += durationHours
            totalPersonHours += (durationHours * enrolled)
            totalRevenue += (course.priceRials * enrolled)
        }

        val totalStudents: Long = enrollmentRepository.countTotalEnrollmentsForInstitution(institutionId)
        val avgRating: Double = courseRepository.calculateAverageRatingForInstitution(institutionId) ?: 0.0
        val totalReviews: Int = courseRepository.countTotalReviewsForInstitution(institutionId)
        val approvedCourseCount: Long = approvedCourses.size.toLong()
        
        val totalTeachersCount = entity.instructorIds.size + entity.manualInstructors.size
        val totalCollaborations = entity.associatedClubIds.size + entity.associatedFieldOfStudyIds.size + entity.associatedStudentOrgIds.size

        // Hide revenue for non-owners
        val authentication = SecurityContextHolder.getContext().authentication
        var isOwner = false
        if (authentication != null && authentication.principal is UserPrincipal) {
            val principal = authentication.principal as UserPrincipal
            if (principal.id == entity.owner?.id) {
                isOwner = true
            }
        }
        val finalRevenue: Long? = if (isOwner) totalRevenue else null

        return InstitutionResponse(
            id = institutionId,
            name = entity.name,
            type = entity.type.name,
            registrationNumber = entity.registrationNumber,
            contactPhone = entity.contactPhone,
            contactEmail = entity.contactEmail,
            province = entity.province,
            city = entity.city,
            address = entity.address,
            logoUrl = entity.logoUrl,
            description = entity.description,
            isSubsidiary = entity.isSubsidiary,
            dependencyDescription = entity.dependencyDescription,
            universities = entity.universities.toList(),
            specialties = entity.specialties.toList(),
            associatedClubIds = entity.associatedClubIds.toList(),
            associatedFieldOfStudyIds = entity.associatedFieldOfStudyIds.toList(),
            associatedStudentOrgIds = entity.associatedStudentOrgIds.toList(),
            instructorIds = entity.instructorIds.toList(),
            manualInstructors = entity.manualInstructors.map { ManualInstructorDto(it.name, it.avatarUrl, it.resume) },
            adminIds = entity.adminIds.toList(),
            ownerId = entity.owner!!.id!!,
            channelId = entity.channel?.id,
            verificationStatus = entity.verificationStatus.name,
            adminNote = entity.adminNote,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            followerCount = entity.owner?.id?.let { userFollowRepository.countFollowersByUserId(it) } ?: 0,
            followingCount = entity.owner?.id?.let { userFollowRepository.countFollowingByUserId(it) } ?: 0,
            courseCount = approvedCourseCount.toInt(),
            studentCount = totalStudents.toInt(),
            totalTrainingHours = totalTrainingHours,
            totalPersonHours = totalPersonHours,
            totalTeachersCount = totalTeachersCount,
            totalCollaborations = totalCollaborations,
            totalRevenue = finalRevenue,
            rating = avgRating,
            averageRating = avgRating,
            reviewCount = totalReviews,
            honors = entity.honors.map { it.toDto() }
        )
    }

    fun getHonors(institutionId: UUID): List<InstitutionHonorDto> {
        val entity = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        return entity.honors.map { it.toDto() }
    }

    fun getTeachers(institutionId: UUID): List<UserDto> {
        val entity = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        return userRepository.findAllById(entity.instructorIds).map { it.toDto() }
    }

    fun getAdmins(institutionId: UUID): List<UserDto> {
        val entity = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        return userRepository.findAllById(entity.adminIds).map { it.toDto() }
    }

    @Transactional
    fun addHonor(institutionId: UUID, ownerId: UUID, title: String, description: String?, imageUrl: String?, date: java.time.LocalDate?): InstitutionHonorDto {
        val entity = institutionRepository.findById(institutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Institution not found") }
        if (entity.owner?.id != ownerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can add honors")
        }
        val honor = InstitutionHonor(
            institution = entity,
            title = title,
            description = description,
            imageUrl = imageUrl,
            date = date
        )
        entity.honors.add(honor)
        institutionRepository.save(entity)
        return honor.toDto()
    }
}
