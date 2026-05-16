package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.scheduling.annotation.Scheduled
import com.iliyadev.springboot.websocket.WebSocketMessageHandler
import com.iliyadev.springboot.websocket.WsMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CourseChapterDto(
    val title: String,
    val durationText: String,
    val sessionStartTime: Instant? = null,
    val sessionEndTime: Instant? = null
)

data class CreateCourseRequest(
    val title: String,
    val slogan: String? = null,
    val description: String? = null,
    val adminIds: List<UUID> = emptyList(),
    val teacherIds: List<UUID> = emptyList(),
    val institutionId: UUID? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: Instant,
    val endsAt: Instant,
    val enrollmentLimit: Int? = null,
    val capacity: Int? = null,
    val discountPercentage: Int? = null,
    val syllabusDuration: String? = null,
    val collaborators: List<UUID> = emptyList(),
    val isPublic: Boolean = true,
    val priceRials: Long = 0,
    val tags: List<String> = emptyList(),
    val suitableFor: List<String> = emptyList(),
    val chapters: List<CourseChapterDto> = emptyList(),
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val organizerDescription: String? = null,
    val scientificAssociationName: String? = null,
    val isVerticalPoster: Boolean = false
)

data class UpdateCourseRequest(
    val title: String? = null,
    val slogan: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val enrollmentLimit: Int? = null,
    val capacity: Int? = null,
    val discountPercentage: Int? = null,
    val syllabusDuration: String? = null,
    val collaborators: List<UUID>? = null,
    val isPublic: Boolean? = null,
    val priceRials: Long? = null,
    val tags: List<String>? = null,
    val suitableFor: List<String>? = null,
    val chapters: List<CourseChapterDto>? = null,
    val manualInstructors: List<ManualInstructorDto>? = null,
    val organizerDescription: String? = null,
    val scientificAssociationName: String? = null,
    val isVerticalPoster: Boolean? = null
)

data class CourseResponse(
    val id: UUID,
    val title: String,
    val slogan: String?,
    val description: String?,
    val favoritesCount: Int,
    val teachers: List<UserDto>,
    val admins: List<UserDto>,
    val organizerId: UUID,
    val organizerName: String?,
    val organizerAvatarUrl: String?,
    val organizerDescription: String?,
    val scientificAssociationName: String?,
    val institutionId: UUID?,
    val academyUniversities: List<String> = emptyList(),
    val channelId: UUID?,
    val groupId: UUID?,
    val coverImageUrl: String?,
    val fieldOfStudy: String?,
    val educationLevel: String?,
    val startsAt: Instant,
    val endsAt: Instant,
    val enrollmentLimit: Int?,
    val capacity: Int?,
    val discountPercentage: Int?,
    val syllabusDuration: String?,
    val manualInstructors: List<ManualInstructorDto> = emptyList(),
    val collaborators: List<UUID>,
    val enrolledCount: Long,
    val isPublic: Boolean,
    val status: CourseStatus,
    val adminNote: String?,
    val priceRials: Long,
    val tags: List<String>,
    val suitableFor: List<String>,
    val chapters: List<CourseChapterDto>,
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val isVerticalPoster: Boolean = false,
    val hasOnlineClass: Boolean = false,
    val organizerType: String? = null,
    val managerId: UUID,
    val managerName: String,
    val managerAvatarUrl: String?,
    val createdAt: Instant
)

data class EnrollmentRequest(
    val paymentType: String // WALLET, ONLINE
)

data class EnrollmentResponse(
    val id: UUID,
    val courseId: UUID,
    val courseTitle: String,
    val userId: UUID,
    val enrolledAt: Instant,
    val isActive: Boolean,
    val course: CourseResponse? = null
)

data class AddMaterialRequest(
    val title: String,
    val description: String? = null,
    val contentUrl: String? = null,
    val contentType: String? = null, // VIDEO, PDF, AUDIO, TEXT
    val sortOrder: Int = 0,
    val isLocked: Boolean = false
)

data class UpdateMaterialRequest(
    val title: String? = null,
    val description: String? = null,
    val contentUrl: String? = null,
    val contentType: String? = null,
    val sortOrder: Int? = null,
    val isLocked: Boolean? = null
)

data class CourseMaterialResponse(
    val id: UUID,
    val courseId: UUID,
    val title: String,
    val description: String?,
    val contentUrl: String?,
    val contentType: String?,
    val sortOrder: Int,
    val isLocked: Boolean,
    val createdAt: Instant
)

data class CourseCommentResponse(
    val id: UUID,
    val courseId: UUID,
    val userId: UUID,
    val userDisplayName: String,
    val userAvatarUrl: String?,
    val content: String,
    val rating: Int,
    val replyToCommentId: UUID?,
    val createdAt: Instant
)

data class AddCommentRequest(
    val content: String,
    val rating: Int = 0,
    val replyToCommentId: UUID? = null
)

// ═══════════════════════════════════════════════════════════════════════════════
// Course Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: CourseEnrollmentRepository,
    private val aiBotRepository: AiBotRepository,
    private val aiBotMessageRepository: AiBotMessageRepository,
    private val materialRepository: CourseMaterialRepository,
    private val commentRepository: CourseCommentRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val collaborationService: CollaborationService,
    private val collaborationRepository: CollaborationRequestRepository,
    private val institutionRepository: InstitutionRepository,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler,
    private val bigBlueButtonService: BigBlueButtonService
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(CourseService::class.java)

    private fun syncManualInstructorsToInstitution(institutionId: UUID?, manualInstructors: List<ManualInstructorDto>) {
        if (institutionId != null && manualInstructors.isNotEmpty()) {
            try {
                val institution = institutionRepository.findById(institutionId).orElse(null)
                if (institution != null) {
                    val existingNames = institution.manualInstructors.map { it.name }.toSet()
                    val newManuals = manualInstructors
                        .filter { it.name !in existingNames }
                        .map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) }
                    
                    if (newManuals.isNotEmpty()) {
                        institution.manualInstructors.addAll(newManuals)
                        institutionRepository.save(institution)
                        logger.info("Synced ${newManuals.size} manual instructors to institution $institutionId")
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to sync manual instructors to institution: ${e.message}")
            }
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    lateinit var jdbcTemplate: org.springframework.jdbc.core.JdbcTemplate

    @jakarta.annotation.PostConstruct
    fun fixCourseStatusConstraint() {
        try {
            jdbcTemplate.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_status_check")
            jdbcTemplate.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_status_check1")
        } catch (e: Exception) {
            // Ignore
        }
    }

    // ── Course CRUD ──

    @Transactional
    fun createCourse(organizerId: UUID, request: CreateCourseRequest): CourseResponse {
        val organizer: User = userRepository.findById(organizerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        // All users can create a course, it will go into PENDING status
        if (request.startsAt.isAfter(request.endsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date")
        }
        // Auto-create a discussion group for the course
        val group = Group(
            name = "گروه ${request.title}",
            description = "گروه بحث و گفتگو دوره ${request.title}",
            isPublic = false,
            isOfficial = true,
            officialCategory = OfficialGroupCategory.COURSE_GROUP,
            displayMode = OfficialDisplayMode.TAB,
            createdBy = organizer
        )
        val savedGroup: Group = groupRepository.save(group)
        // Add organizer as group owner
        val ownerMember = GroupMember(
            group = savedGroup,
            user = organizer,
            role = MemberRole.OWNER
        )
        groupMemberRepository.save(ownerMember)
        val teachers = userRepository.findAllById(request.teacherIds).toMutableList()
        val admins = userRepository.findAllById(request.adminIds).toMutableList()
        
        // Send collaboration requests to other teachers
        request.teacherIds.forEach { tId ->
            if (tId != organizerId) {
                try {
                    val alreadyExists = collaborationRepository.existsBySenderIdAndReceiverIdAndStatus(organizerId, tId, CollaborationStatus.PENDING)
                    if (!alreadyExists) {
                        collaborationService.sendRequest(
                            senderId = organizerId,
                            request = SendCollaborationRequest(
                                receiverId = tId,
                                title = "دعوت به تدریس",
                                message = "شما به عنوان مدرس در دوره ${request.title} دعوت شده‌اید."
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        
        val course = Course(
            title = request.title,
            slogan = request.slogan,
            description = request.description,
            organizer = organizer,
            teachers = teachers,
            admins = admins,
            institutionId = request.institutionId,
            group = savedGroup,
            coverImageUrl = request.coverImageUrl,
            fieldOfStudy = request.fieldOfStudy,
            educationLevel = request.educationLevel,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            enrollmentLimit = request.enrollmentLimit,
            capacity = request.capacity,
            discountPercentage = request.discountPercentage ?: 0,
            syllabusDuration = request.syllabusDuration,
            collaborators = request.collaborators.map { it.toString() }.toMutableList(),
            isPublic = request.isPublic,
            status = CourseStatus.PENDING,
            priceRials = request.priceRials,
            tags = request.tags.toMutableList(),
            suitableFor = request.suitableFor.toMutableList(),
            chapters = request.chapters.map { CourseChapter(title = it.title, durationText = it.durationText, sessionStartTime = it.sessionStartTime, sessionEndTime = it.sessionEndTime) }.toMutableList(),
            manualInstructors = request.manualInstructors.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) }.toMutableList(),
            organizerDescription = request.organizerDescription,
            scientificAssociationName = request.scientificAssociationName,
            isVerticalPoster = request.isVerticalPoster
        )
        val saved: Course = courseRepository.save(course)
        
        // Sync manual instructors to institution
        syncManualInstructorsToInstitution(request.institutionId, request.manualInstructors)
        
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun updateCourse(courseId: UUID, organizerId: UUID, request: UpdateCourseRequest): CourseResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        request.title?.let { course.title = it }
        request.description?.let { course.description = it }
        request.coverImageUrl?.let { course.coverImageUrl = it }
        request.fieldOfStudy?.let { course.fieldOfStudy = it }
        request.educationLevel?.let { course.educationLevel = it }
        request.startsAt?.let { course.startsAt = it }
        request.endsAt?.let { course.endsAt = it }
        request.enrollmentLimit?.let { course.enrollmentLimit = it }
        request.capacity?.let { course.capacity = it }
        request.discountPercentage?.let { course.discountPercentage = it }
        request.syllabusDuration?.let { course.syllabusDuration = it }
        request.collaborators?.let { course.collaborators = it.map { uuid -> uuid.toString() }.toMutableList() }
        request.isPublic?.let { course.isPublic = it }
        request.priceRials?.let { course.priceRials = it }
        request.tags?.let { course.tags = it.toMutableList() }
        request.slogan?.let { course.slogan = it }
        request.suitableFor?.let { course.suitableFor = it.toMutableList() }
        request.chapters?.let { dtos ->
            course.chapters.clear()
            course.chapters.addAll(dtos.map { CourseChapter(title = it.title, durationText = it.durationText, sessionStartTime = it.sessionStartTime, sessionEndTime = it.sessionEndTime) })
        }
        request.organizerDescription?.let { course.organizerDescription = it }
        request.manualInstructors?.let { dtos ->
            course.manualInstructors.clear()
            course.manualInstructors.addAll(dtos.map { ManualInstructor(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) })
        }
        request.scientificAssociationName?.let { course.scientificAssociationName = it }
        request.isVerticalPoster?.let { course.isVerticalPoster = it }
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        
        // Sync manual instructors to institution
        request.manualInstructors?.let { syncManualInstructorsToInstitution(course.institutionId, it) }
        
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun deleteCourse(courseId: UUID, organizerId: UUID) {
        val course: Course = getOwnedCourse(courseId, organizerId)
        if (course.status == CourseStatus.APPROVED || course.status == CourseStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete an approved course. Archive it first.")
        }
        // Delete related enrollments
        enrollmentRepository.deleteByCourseId(courseId)
        // Delete related comments
        commentRepository.deleteByCourseId(courseId)
        // Delete related materials
        materialRepository.deleteByCourseId(courseId)
        // Delete the course itself
        courseRepository.delete(course)
    }

    @Transactional
    fun publishCourse(courseId: UUID, organizerId: UUID): CourseResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        if (course.status != CourseStatus.DRAFT && course.status != CourseStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT or PENDING courses can be published/submitted")
        }
        course.status = CourseStatus.PENDING
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun reviewCourse(courseId: UUID, adminId: UUID, status: CourseStatus, adminNote: String?): CourseResponse {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        if (course.status != CourseStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only PENDING courses can be reviewed")
        }
        if (status != CourseStatus.APPROVED && status != CourseStatus.REJECTED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status for review")
        }
        course.status = status
        course.adminNote = adminNote
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun archiveCourse(courseId: UUID, organizerId: UUID): CourseResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        course.status = CourseStatus.COMPLETED
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        return mapCourseToResponse(saved)
    }

    fun getCourseById(courseId: UUID): CourseResponse {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        return mapCourseToResponse(course)
    }

    /**
     * Explicitly creates a Kelasor Online (BBB) room for a course.
     * Only the organizer/admin can call this.
     */
    @Transactional
    fun createKelasorOnline(courseId: UUID, organizerId: UUID): CourseResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        if (course.bbbMeetingId != null) {
            return mapCourseToResponse(course)
        }
        course.bbbMeetingId = "kelasor-${course.id}"
        course.bbbAttendeePassword = UUID.randomUUID().toString().substring(0, 8)
        course.bbbModeratorPassword = UUID.randomUUID().toString().substring(0, 8)
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        logger.info("Kelasor Online created for course ${course.title} (${course.id})")
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun getJoinClassUrl(courseId: UUID, userId: UUID, fullName: String): String {
        val course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        if (course.bbbMeetingId == null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "کلاسور آنلاین هنوز ساخته نشده است")
        }
        // Check if user is teacher, admin, or organizer (moderator)
        val isModerator = course.teachers.any { it.id == userId } ||
                          course.admins.any { it.id == userId } ||
                          course.organizer?.id == userId
        if (!isModerator) {
            if (!isEnrolled(courseId, userId)) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "شما در این دوره ثبت‌نام نکرده‌اید")
            }
        }
        if (isModerator) {
            // Moderator: create the BBB meeting (idempotent) and join as moderator
            val success = bigBlueButtonService.createMeeting(
                meetingId = course.bbbMeetingId!!,
                name = course.title,
                attendeePw = course.bbbAttendeePassword!!,
                moderatorPw = course.bbbModeratorPassword!!,
                record = true
            )
            if (!success) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "خطا در اتصال به سرور کلاس آنلاین")
            }
            return bigBlueButtonService.getJoinUrl(
                meetingId = course.bbbMeetingId!!,
                fullName = fullName,
                password = course.bbbModeratorPassword!!,
                isFarsi = true
            )
        } else {
            // Student: check if meeting is running (SkyRoom behavior)
            val isRunning = bigBlueButtonService.isMeetingRunning(course.bbbMeetingId!!)
            if (!isRunning) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "برگزارکننده هنوز کلاس را شروع نکرده است. لطفاً دقایقی دیگر تلاش کنید.")
            }
            return bigBlueButtonService.getJoinUrl(
                meetingId = course.bbbMeetingId!!,
                fullName = fullName,
                password = course.bbbAttendeePassword!!,
                isFarsi = true
            )
        }
    }

    fun getMyCourses(organizerId: UUID, pageable: Pageable): Page<CourseResponse> {
        return courseRepository.findByOrganizerId(organizerId, pageable)
            .map { mapCourseToResponse(it) }
    }

    fun getPendingCourses(pageable: Pageable): Page<CourseResponse> {
        return courseRepository.findByStatus(CourseStatus.PENDING, pageable)
            .map { mapCourseToResponse(it) }
    }

    fun getPublicCourses(pageable: Pageable): Page<CourseResponse> {
        return courseRepository.findByStatusAndIsPublicTrue(CourseStatus.APPROVED, pageable)
            .map { mapCourseToResponse(it) }
    }

    fun getUpcomingCourses(pageable: Pageable): Page<CourseResponse> {
        return courseRepository.findUpcomingCourses(Instant.now(), pageable)
            .map { mapCourseToResponse(it) }
    }

    fun getInstitutionCourses(institutionId: UUID, pageable: Pageable): Page<CourseResponse> {
        // Find the institution owner to also include courses created by them (before institutionId was set)
        val institution: Institution? = institutionRepository.findById(institutionId).orElse(null)
        return if (institution != null && institution.owner != null) {
            courseRepository.findByInstitutionIdOrOrganizerId(institutionId, institution.owner!!.id!!, pageable)
                .map { mapCourseToResponse(it) }
        } else {
            courseRepository.findByInstitutionId(institutionId, pageable)
                .map { mapCourseToResponse(it) }
        }
    }

    @Transactional
    fun toggleFavorite(courseId: UUID, userId: UUID): Boolean {
        val course = courseRepository.findById(courseId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        val user = userRepository.findById(userId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        
        val isFavorite = user.favoriteCourseIds.contains(courseId)
        if (isFavorite) {
            user.favoriteCourseIds.remove(courseId)
            course.favoritesCount = maxOf(0, course.favoritesCount - 1)
        } else {
            user.favoriteCourseIds.add(courseId)
            course.favoritesCount += 1
        }
        
        userRepository.save(user)
        courseRepository.save(course)
        
        return !isFavorite
    }

    // ── Enrollment ──

    @Transactional
    fun enroll(courseId: UUID, userId: UUID, paymentType: String = "WALLET"): EnrollmentResponse {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        if (course.status != CourseStatus.APPROVED && course.status != CourseStatus.ACTIVE) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Course is not accepting enrollments")
        }
        if (enrollmentRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this course")
        }
        val currentCount: Long = enrollmentRepository.countByCourseIdAndIsActiveTrue(courseId)
        if (course.enrollmentLimit != null && currentCount >= course.enrollmentLimit!!) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Course enrollment limit reached")
        }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        // Charge if paid course
        if (course.priceRials > 0) {
            if (paymentType == "WALLET") {
                walletService.executeInternalPurchase(
                    userId = user.id!!,
                    amount = course.priceRials,
                    description = "ثبت‌نام در دوره: ${course.title}",
                    referenceId = course.id!!,
                    referenceType = "COURSE"
                )
            } else {
                // Online Payment Simulation
                logger.info("User $userId selected ONLINE payment for course $courseId. Simulating success.")
            }
        }
        val enrollment = CourseEnrollment(
            course = course,
            user = user,
            isActive = true
        )
        val saved: CourseEnrollment = enrollmentRepository.save(enrollment)
        // Auto-add user to the course group
        if (course.group != null) {
            val isAlreadyMember: Boolean = groupMemberRepository.existsByGroupIdAndUserId(course.group!!.id!!, userId)
            if (!isAlreadyMember) {
                val member = GroupMember(
                    group = course.group,
                    user = user,
                    role = MemberRole.MEMBER
                )
                groupMemberRepository.save(member)
            }
        }
        // Send bot notification
        try {
            sendEnrollmentBotNotification(user, course)
        } catch (e: Exception) {
            logger.warn("Failed to send enrollment bot notification: ${e.message}")
        }
        
        // Broadcast course capacity update
        try {
            enrollmentRepository.flush()
            val newCount = enrollmentRepository.countByCourseIdAndIsActiveTrue(courseId).toInt()
            val capacity = course.enrollmentLimit?.toInt() ?: -1
            webSocketMessageHandler.broadcastCourseCapacityUpdate(courseId, newCount, capacity)
        } catch (e: Exception) {
            logger.warn("Failed to broadcast course capacity update: ${e.message}")
        }
        
        return mapEnrollmentToResponse(saved)
    }

    @Transactional
    fun unenroll(courseId: UUID, userId: UUID) {
        val enrollment: CourseEnrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found")
        enrollment.isActive = false
        enrollmentRepository.save(enrollment)
        
        // Broadcast course capacity update
        try {
            enrollmentRepository.flush()
            val course = courseRepository.findById(courseId).orElse(null)
            val newCount = enrollmentRepository.countByCourseIdAndIsActiveTrue(courseId).toInt()
            val capacity = course?.enrollmentLimit?.toInt() ?: -1
            webSocketMessageHandler.broadcastCourseCapacityUpdate(courseId, newCount, capacity)
        } catch (e: Exception) {
            logger.warn("Failed to broadcast course capacity update: ${e.message}")
        }
    }

    fun getMyEnrollments(userId: UUID, pageable: Pageable): Page<EnrollmentResponse> {
        return enrollmentRepository.findByUserIdAndIsActiveTrue(userId, pageable)
            .map { mapEnrollmentToResponse(it) }
    }

    fun isEnrolled(courseId: UUID, userId: UUID): Boolean {
        val enrollment: CourseEnrollment? = enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
        return enrollment != null && enrollment.isActive
    }

    // ── Similar Courses ──

    fun getSimilarCourses(courseId: UUID, pageable: Pageable): Page<CourseResponse> {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        val tags: List<String> = if (course.tags.isEmpty()) listOf("__NO_MATCH__") else course.tags
        return courseRepository.findSimilarCourses(courseId, course.fieldOfStudy, tags, pageable)
            .map { mapCourseToResponse(it) }
    }

    fun isFavorite(courseId: UUID, userId: UUID): Boolean {
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        return user.favoriteCourseIds.contains(courseId)
    }

    fun getFavoriteCourses(userId: UUID): List<CourseResponse> {
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (user.favoriteCourseIds.isEmpty()) return emptyList()
        val courses = courseRepository.findAllById(user.favoriteCourseIds)
        return courses.map { mapCourseToResponse(it) }
    }

    // ── Materials ──

    @Transactional
    fun addMaterial(courseId: UUID, organizerId: UUID, request: AddMaterialRequest): CourseMaterialResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        val material = CourseMaterial(
            course = course,
            title = request.title,
            description = request.description,
            contentUrl = request.contentUrl,
            contentType = request.contentType,
            sortOrder = request.sortOrder,
            isLocked = request.isLocked
        )
        val saved: CourseMaterial = materialRepository.save(material)
        return mapMaterialToResponse(saved)
    }

    @Transactional
    fun updateMaterial(materialId: UUID, organizerId: UUID, request: UpdateMaterialRequest): CourseMaterialResponse {
        val material: CourseMaterial = materialRepository.findById(materialId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Material not found") }
        val course: Course = material.course!!
        validateOwnership(course, organizerId)
        request.title?.let { material.title = it }
        request.description?.let { material.description = it }
        request.contentUrl?.let { material.contentUrl = it }
        request.contentType?.let { material.contentType = it }
        request.sortOrder?.let { material.sortOrder = it }
        request.isLocked?.let { material.isLocked = it }
        val saved: CourseMaterial = materialRepository.save(material)
        return mapMaterialToResponse(saved)
    }

    @Transactional
    fun deleteMaterial(materialId: UUID, organizerId: UUID) {
        val material: CourseMaterial = materialRepository.findById(materialId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Material not found") }
        validateOwnership(material.course!!, organizerId)
        materialRepository.delete(material)
    }

    fun getCourseMaterials(courseId: UUID): List<CourseMaterialResponse> {
        return materialRepository.findByCourseIdOrderBySortOrderAsc(courseId)
            .map { mapMaterialToResponse(it) }
    }

    // ── Comments ──

    @Transactional
    fun addComment(courseId: UUID, userId: UUID, request: AddCommentRequest): CourseCommentResponse {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val comment = CourseComment(
            course = course,
            user = user,
            content = request.content,
            rating = request.rating.coerceIn(0, 5),
            replyToCommentId = request.replyToCommentId
        )
        val saved: CourseComment = commentRepository.save(comment)
        if (saved.rating > 0) {
            val oldRatingSum = course.averageRating * course.reviewCount
            course.reviewCount += 1
            course.averageRating = (oldRatingSum + saved.rating) / course.reviewCount
            courseRepository.save(course)
            courseRepository.flush() // Force flush so the native/JPQL query below sees the updated course rating
            // Also update the institution's aggregated rating or organizer's rating
            if (course.institutionId != null) {
                try {
                    val institution: Institution? = institutionRepository.findById(course.institutionId!!).orElse(null)
                    if (institution != null) {
                        // Recalculate from all courses instead of incremental (fixes averaging bug)
                        val avgFromCourses: Double = courseRepository.calculateAverageRatingForInstitution(course.institutionId!!) ?: 0.0
                        val totalReviews: Int = courseRepository.findByInstitutionId(course.institutionId!!, org.springframework.data.domain.Pageable.unpaged())
                            .content.sumOf { it.reviewCount }
                        institution.averageRating = avgFromCourses
                        institution.reviewCount = totalReviews
                        institutionRepository.save(institution)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to update institution rating: ${e.message}")
                }
            } else if (course.organizer != null) {
                try {
                    val organizer: User = course.organizer!!
                    val oldOrgRatingSum = organizer.averageRating * organizer.reviewCount
                    organizer.reviewCount += 1
                    organizer.averageRating = (oldOrgRatingSum + saved.rating) / organizer.reviewCount
                    userRepository.save(organizer)
                } catch (e: Exception) {
                    logger.warn("Failed to update organizer rating: ${e.message}")
                }
            }

            // Update rating for all teachers of the course
            try {
                if (course.teachers.isNotEmpty()) {
                    course.teachers.forEach { teacher ->
                        val oldTeacherRatingSum = teacher.averageRating * teacher.reviewCount
                        teacher.reviewCount += 1
                        teacher.averageRating = (oldTeacherRatingSum + saved.rating) / teacher.reviewCount
                        userRepository.save(teacher)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Failed to update teachers rating: ${e.message}")
            }
        }
        return mapCommentToResponse(saved)
    }

    fun getComments(courseId: UUID, pageable: Pageable): Page<CourseCommentResponse> {
        return commentRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable)
            .map { mapCommentToResponse(it) }
    }

    private fun mapCommentToResponse(comment: CourseComment): CourseCommentResponse {
        return CourseCommentResponse(
            id = comment.id!!,
            courseId = comment.course!!.id!!,
            userId = comment.user!!.id!!,
            userDisplayName = comment.user!!.displayName,
            userAvatarUrl = comment.user!!.avatarUrl,
            content = comment.content,
            rating = comment.rating,
            replyToCommentId = comment.replyToCommentId,
            createdAt = comment.createdAt
        )
    }

    // ── Private Helpers ──

    private fun getOwnedCourse(courseId: UUID, organizerId: UUID): Course {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        validateOwnership(course, organizerId)
        return course
    }

    private fun validateOwnership(course: Course, organizerId: UUID) {
        if (course.organizer?.id != organizerId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this course")
        }
    }

    private fun mapCourseToResponse(course: Course): CourseResponse {
        val enrolledCount: Long = enrollmentRepository.countByCourseIdAndIsActiveTrue(course.id!!)
        // Use Institution (academy) name/logo if available, otherwise fall back to organizer User profile
        val institution: Institution? = course.institutionId?.let { instId ->
            try { institutionRepository.findById(instId).orElse(null) } catch (e: Exception) { null }
        } ?: try { institutionRepository.findByOwnerId(course.organizer!!.id!!).firstOrNull() } catch (e: Exception) { null }
        val resolvedOrganizerName: String? = institution?.name ?: course.organizer?.institutionName ?: course.organizer?.displayName
        val resolvedOrganizerAvatarUrl: String? = institution?.logoUrl ?: course.organizer?.institutionLogoUrl ?: course.organizer?.avatarUrl
        return CourseResponse(
            id = course.id!!,
            title = course.title,
            slogan = course.slogan,
            description = course.description,
            favoritesCount = course.favoritesCount,
            teachers = course.teachers.map { it.toDto() },
            admins = course.admins.map { it.toDto() },
            organizerId = course.organizer!!.id!!,
            organizerName = resolvedOrganizerName,
            organizerAvatarUrl = resolvedOrganizerAvatarUrl,
            institutionId = institution?.id,
            academyUniversities = institution?.universities ?: emptyList(),
            channelId = course.channel?.id,
            groupId = course.group?.id,
            coverImageUrl = course.coverImageUrl,
            fieldOfStudy = course.fieldOfStudy,
            educationLevel = course.educationLevel,
            startsAt = course.startsAt,
            endsAt = course.endsAt,
            enrollmentLimit = course.enrollmentLimit,
            capacity = course.capacity ?: course.enrollmentLimit, // Fallback if capacity is null
            discountPercentage = course.discountPercentage,
            syllabusDuration = course.syllabusDuration,
            collaborators = course.collaborators.mapNotNull { try { java.util.UUID.fromString(it) } catch (e: Exception) { null } },
            enrolledCount = enrolledCount,
            isPublic = course.isPublic,
            status = course.status,
            adminNote = course.adminNote,
            priceRials = course.priceRials,
            tags = course.tags,
            suitableFor = course.suitableFor,
            chapters = course.chapters.map { CourseChapterDto(title = it.title, durationText = it.durationText, sessionStartTime = it.sessionStartTime, sessionEndTime = it.sessionEndTime) },
            manualInstructors = course.manualInstructors.map { ManualInstructorDto(name = it.name, avatarUrl = it.avatarUrl, resume = it.resume) },
            organizerDescription = course.organizerDescription,
            scientificAssociationName = course.scientificAssociationName,
            isVerticalPoster = course.isVerticalPoster,
            hasOnlineClass = course.bbbMeetingId != null,
            organizerType = institution?.type?.name,
            managerId = course.organizer?.id ?: java.util.UUID.randomUUID(),
            managerName = course.organizer?.displayName ?: "Unknown",
            managerAvatarUrl = course.organizer?.avatarUrl,
            createdAt = course.createdAt
        )
    }


    private fun mapEnrollmentToResponse(enrollment: CourseEnrollment): EnrollmentResponse {
        val course = enrollment.course!!
        return EnrollmentResponse(
            id = enrollment.id!!,
            courseId = course.id!!,
            courseTitle = course.title,
            userId = enrollment.user!!.id!!,
            enrolledAt = enrollment.enrolledAt,
            isActive = enrollment.isActive,
            course = mapCourseToResponse(course)
        )
    }

    private fun mapMaterialToResponse(material: CourseMaterial): CourseMaterialResponse {
        return CourseMaterialResponse(
            id = material.id!!,
            courseId = material.course!!.id!!,
            title = material.title,
            description = material.description,
            contentUrl = material.contentUrl,
            contentType = material.contentType,
            sortOrder = material.sortOrder,
            isLocked = material.isLocked,
            createdAt = material.createdAt
        )
    }
    /**
     * Sends an automated notification message to the user via the Mosbat Elm Bot
     * after successful course enrollment.
     */
    private fun sendEnrollmentBotNotification(user: User, course: Course) {
        val botUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val botUser = userRepository.findById(botUserId).orElse(null) ?: run {
            logger.error("System Bot User not found! Check migration.")
            return
        }
        
        val priceToman: Long = course.priceRials / 10
        val priceText: String = if (course.priceRials > 0) {
            "مبلغ ${java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("fa")).format(priceToman)} تومان از کیف پول شما کسر شد."
        } else {
            "این دوره رایگان بود و مبلغی کسر نشد."
        }
        val groupText: String = if (course.group != null) {
            "\n\n📂 شما به گروه «${course.group!!.name}» اضافه شدید. از بخش پیام‌ها به گروه دسترسی داشته باشید."
        } else ""
        
        val firstChapter = course.chapters.firstOrNull()
        val sessionInfo = if (firstChapter?.sessionStartTime != null) {
            val start = firstChapter.sessionStartTime!!
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                .withZone(java.time.ZoneId.of("Asia/Tehran"))
            "\n\n📅 اولین جلسه: ${formatter.format(start)}\n📖 سرفصل: ${firstChapter.title}"
        } else ""

        val messageContent: String = """
✅ ثبت‌نام موفق!

سلام ${user.displayName} عزیز،
شما با موفقیت در دوره «${course.title}» ثبت‌نام کردید.

💰 $priceText$groupText$sessionInfo

⏰ مدت کل دوره: ${course.syllabusDuration ?: "نامشخص"}

با آرزوی موفقیت — اطلاع‌رسانی مثبت علم 🎓
        """.trimIndent()

        // Find or Create Chat
        var chats = chatRepository.findPrivateChatBetween(botUserId, user.id!!)
        val chat = if (chats.isNotEmpty()) {
            chats.first()
        } else {
            val newChat = Chat(
                type = ChatType.PRIVATE,
                participants = mutableListOf(botUser, user),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            chatRepository.save(newChat)
        }
        
        // Send Standard Message
        val message = Message(
            chat = chat,
            sender = botUser,
            content = messageContent,
            type = MessageType.TEXT,
            status = MessageStatus.SENT,
            createdAt = Instant.now(),
            actionLabel = "مشاهده جزئیات دوره",
            actionUrl = "course_details/${course.id}",
            timerTargetAt = firstChapter?.sessionStartTime
        )
        val savedMessage = messageRepository.save(message)
        
        // Update chat for sorting
        chat.updatedAt = Instant.now()
        chatRepository.save(chat)

        // Notify via WebSocket for real-time delivery
        try {
            val wsMessage = WsMessage(
                id = savedMessage.id!!,
                chatId = chat.id!!,
                senderId = botUserId,
                senderName = botUser.displayName,
                senderAvatar = botUser.avatarUrl,
                content = messageContent,
                type = MessageType.TEXT,
                timestamp = savedMessage.createdAt,
                actionLabel = savedMessage.actionLabel,
                actionUrl = savedMessage.actionUrl,
                timerTargetAt = savedMessage.timerTargetAt
            )
            webSocketMessageHandler.sendPrivateMessage(user.id!!, wsMessage)
        } catch (e: Exception) {
            logger.warn("Failed to send WebSocket notification for bot message: ${e.message}")
        }

        logger.info("Enrollment notification sent from Bot User to user ${user.id} for course ${course.title}")
    }

    /**
     * Periodically check for courses starting in ~1 hour and send reminders.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    fun checkAndSendCourseReminders() {
        val now = Instant.now()
        val oneHourLater = now.plusSeconds(3600)
        
        val soonStartingEnrollments = enrollmentRepository.findEnrollmentsForSoonStartingCourses(now, oneHourLater)
        if (soonStartingEnrollments.isEmpty()) return
        
        val botUserId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val botUser = userRepository.findById(botUserId).orElse(null) ?: return
        
        logger.info("⏰ Found ${soonStartingEnrollments.size} enrollments for courses starting soon. Sending reminders...")
        
        soonStartingEnrollments.forEach { enrollment ->
            val user = enrollment.user ?: return@forEach
            val course = enrollment.course ?: return@forEach
            
            val reminderContent = """
🔔 یادآوری شروع دوره!

دوره «${course.title}» کمتر از یک ساعت دیگر شروع می‌شود.
آماده‌ای؟ 🚀

📍 جزییات دوره را بررسی کنید.
            """.trimIndent()
            
            // Find or Create Chat
            val chats = chatRepository.findPrivateChatBetween(botUserId, user.id!!)
            val chat = if (chats.isNotEmpty()) chats.first() else {
                chatRepository.save(Chat(type = ChatType.PRIVATE, participants = mutableListOf(botUser, user)))
            }
            
            val message = messageRepository.save(Message(
                chat = chat,
                sender = botUser,
                content = reminderContent,
                type = MessageType.TEXT,
                status = MessageStatus.SENT,
                createdAt = Instant.now(),
                actionLabel = "ورود به دوره / جزییات",
                actionUrl = "course_details/${course.id}"
            ))
            
            chat.updatedAt = Instant.now()
            chatRepository.save(chat)
            
            // Mark as sent (removed reminderSent logic)
            enrollmentRepository.save(enrollment)
            
            // Notify via WebSocket
            try {
                webSocketMessageHandler.sendPrivateMessage(user.id!!, WsMessage(
                    id = message.id!!,
                    chatId = chat.id!!,
                    senderId = botUserId,
                    senderName = botUser.displayName,
                    senderAvatar = botUser.avatarUrl,
                    content = reminderContent,
                    timestamp = message.createdAt,
                    actionLabel = message.actionLabel,
                    actionUrl = message.actionUrl,
                    timerTargetAt = course.startsAt
                ))
            } catch (e: Exception) {}
        }
    }
}
