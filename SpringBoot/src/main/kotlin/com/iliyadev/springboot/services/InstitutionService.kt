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
    private val enrollmentRepository: CourseEnrollmentRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val notificationService: NotificationService,
    private val notificationRepository: NotificationRepository
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
            instructorIds = mutableListOf(),
            manualInstructors = request.manualInstructors.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) }.toMutableList(),
            adminIds = mutableListOf(),
            owner = owner,
            verificationStatus = VerificationStatus.APPROVED
        )
        val saved: Institution = institutionRepository.save(institution)
        
        // Auto-create official Elm Club channel
        val autoChannel = Channel(
            name = saved.name,
            description = "کانال رسمی ${saved.name}",
            avatarUrl = saved.logoUrl,
            isPublic = true,
            owner = owner,
            classification = ChannelClassification.ELM_CLUB_INSTITUTION,
            institutionId = saved.id
        )
        val savedChannel: Channel = channelRepository.save(autoChannel)
        saved.channel = savedChannel
        val finalSaved = institutionRepository.save(saved)
        
        // Subscribe owner as Admin of the new channel
        val ownerSub = ChannelSubscriber(
            channel = savedChannel,
            user = owner,
            isAdmin = true,
            subscribedAt = Instant.now()
        )
        channelSubscriberRepository.save(ownerSub)
        
        // Upgrade owner role if Normal, and link institution
        if (owner.role == UserRole.NORMAL) {
            owner.role = UserRole.INSTITUTION
        }
        owner.institutionId = finalSaved.id
        owner.institutionLogoUrl = finalSaved.logoUrl
        owner.institutionName = finalSaved.name
        userRepository.save(owner)
        
        request.instructorIds.forEach { instructorId ->
            sendInvite(instructorId, ownerId, finalSaved, NotificationType.TEACHER_INVITE)
        }
        request.adminIds.forEach { adminId ->
            sendInvite(adminId, ownerId, finalSaved, NotificationType.ADMIN_INVITE)
        }
        
        return mapToResponse(finalSaved)
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
        val currentInstructors = entity.instructorIds.toList()
        entity.instructorIds.clear()
        entity.instructorIds.addAll(currentInstructors.filter { request.instructorIds.contains(it) })
        request.instructorIds.forEach { instructorId ->
            if (!currentInstructors.contains(instructorId)) {
                sendInvite(instructorId, ownerId, entity, NotificationType.TEACHER_INVITE)
            }
        }

        entity.manualInstructors.clear()
        entity.manualInstructors.addAll(request.manualInstructors.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) })

        val currentAdmins = entity.adminIds.toList()
        entity.adminIds.clear()
        entity.adminIds.addAll(currentAdmins.filter { request.adminIds.contains(it) })
        request.adminIds.forEach { adminId ->
            if (!currentAdmins.contains(adminId)) {
                sendInvite(adminId, ownerId, entity, NotificationType.ADMIN_INVITE)
            }
        }
        entity.updatedAt = Instant.now()
        val saved: Institution = institutionRepository.save(entity)
        val owner: User = entity.owner!!
        
        if (saved.channel == null) {
            val autoChannel = Channel(
                name = saved.name,
                description = "کانال رسمی ${saved.name}",
                avatarUrl = saved.logoUrl,
                isPublic = true,
                owner = owner,
                classification = ChannelClassification.ELM_CLUB_INSTITUTION,
                institutionId = saved.id
            )
            val savedChannel: Channel = channelRepository.save(autoChannel)
            saved.channel = savedChannel
            institutionRepository.save(saved)
            
            val ownerSub = ChannelSubscriber(
                channel = savedChannel,
                user = owner,
                isAdmin = true,
                subscribedAt = Instant.now()
            )
            channelSubscriberRepository.save(ownerSub)
        } else {
            val channel = saved.channel!!
            var channelChanged = false
            if (channel.name != saved.name) {
                channel.name = saved.name
                channel.description = "کانال رسمی ${saved.name}"
                channelChanged = true
            }
            if (channel.avatarUrl != saved.logoUrl) {
                channel.avatarUrl = saved.logoUrl
                channelChanged = true
            }
            if (channelChanged) {
                channelRepository.save(channel)
            }
        }
        
        if (owner.role == UserRole.NORMAL) {
            owner.role = UserRole.INSTITUTION
        }
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

    private fun parseDurationTextToMinutes(text: String?): Int {
        if (text.isNullOrBlank()) return 0
        return try {
            val normalized = text.lowercase().trim()
            var totalMinutes = 0
            val hourPattern = java.util.regex.Pattern.compile("(\\d+)\\s*h")
            val minPattern = java.util.regex.Pattern.compile("(\\d+)\\s*m")
            val hourMatcher = hourPattern.matcher(normalized)
            if (hourMatcher.find()) {
                totalMinutes += hourMatcher.group(1).toInt() * 60
            }
            val minMatcher = minPattern.matcher(normalized)
            if (minMatcher.find()) {
                totalMinutes += minMatcher.group(1).toInt()
            }
            if (totalMinutes == 0) {
                val digitOnly = normalized.filter { it.isDigit() }
                if (digitOnly.isNotEmpty()) {
                    if (normalized.contains("hour") || normalized.contains("ساعت")) {
                        totalMinutes = digitOnly.toInt() * 60
                    } else {
                        totalMinutes = digitOnly.toInt()
                    }
                }
            }
            totalMinutes
        } catch (e: Exception) {
            0
        }
    }

    fun mapToResponse(entity: Institution): InstitutionResponse {
        val institutionId: UUID = entity.id!!
        
        // Fetch all approved and active courses for this institution
        val approvedCourses = courseRepository.findByInstitutionIdOrOrganizerId(institutionId, entity.owner!!.id!!, org.springframework.data.domain.Pageable.unpaged()).content
            .filter { it.status == com.iliyadev.springboot.models.CourseStatus.APPROVED || it.status == com.iliyadev.springboot.models.CourseStatus.ACTIVE }
            
        var totalTrainingHours = 0
        var totalPersonHours = 0
        var totalRevenue: Long = 0
        var totalViews: Long = 0
        var totalClicks: Long = 0
        
        for (course in approvedCourses) {
            var courseDurationMinutes = 0
            if (course.chapters.isNotEmpty()) {
                for (chapter in course.chapters) {
                    if (chapter.sessionStartTime != null && chapter.sessionEndTime != null) {
                        val chapterDurationMinutes = java.time.Duration.between(chapter.sessionStartTime, chapter.sessionEndTime).toMinutes().toInt()
                        if (chapterDurationMinutes > 0) {
                            courseDurationMinutes += chapterDurationMinutes
                        }
                    } else {
                        val parsedMinutes = parseDurationTextToMinutes(chapter.durationText)
                        if (parsedMinutes > 0) {
                            courseDurationMinutes += parsedMinutes
                        }
                    }
                }
            }
            
            if (courseDurationMinutes == 0 && !course.syllabusDuration.isNullOrBlank()) {
                courseDurationMinutes = parseDurationTextToMinutes(course.syllabusDuration)
            }
            
            val durationHours = if (courseDurationMinutes > 0) {
                val computedHours = (courseDurationMinutes + 30) / 60
                if (computedHours > 0) computedHours else 1
            } else {
                java.time.Duration.between(course.startsAt, course.endsAt).toHours().toInt()
            }
            
            val enrolled = enrollmentRepository.countByCourseIdAndIsActiveTrue(course.id!!).toInt()
            
            totalTrainingHours += durationHours
            totalPersonHours += (durationHours * enrolled)
            val discountedPrice = course.priceRials * (100 - course.discountPercentage) / 100
            totalRevenue += (discountedPrice * enrolled)
            totalViews += course.viewCount
            totalClicks += course.clickCount
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
            totalViews = totalViews,
            totalClicks = totalClicks,
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

    private fun sendInvite(invitedUserId: UUID, actorId: UUID, institution: Institution, type: NotificationType) {
        val existing = notificationRepository.findByUserIdAndRelatedEntityId(invitedUserId, institution.id!!)
            .any { it.type == type && it.status == "PENDING" }
        if (existing) return

        val roleName = if (type == NotificationType.TEACHER_INVITE) "استاد" else "ادمین"
        val inviteBody = if (type == NotificationType.ADMIN_INVITE) {
            "به عنوان ادمین دعوت شدید به آکادمی «${institution.name}»."
        } else {
            "شما به عنوان ${roleName} به آکادمی «${institution.name}» دعوت شده‌اید."
        }
        notificationService.createNotification(
            userId = invitedUserId,
            type = type,
            title = "دعوت به همکاری در آکادمی",
            body = inviteBody,
            relatedEntityId = institution.id,
            actorId = actorId
        )
    }
}
