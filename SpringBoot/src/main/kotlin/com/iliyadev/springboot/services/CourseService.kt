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

data class CourseChapterDto(
    val title: String,
    val durationText: String
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
    val createdAt: Instant
)

data class EnrollmentResponse(
    val id: UUID,
    val courseId: UUID,
    val courseTitle: String,
    val userId: UUID,
    val enrolledAt: Instant,
    val isActive: Boolean
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
    private val materialRepository: CourseMaterialRepository,
    private val commentRepository: CourseCommentRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val collaborationService: CollaborationService,
    private val collaborationRepository: CollaborationRequestRepository
) {
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
            endsAt = request.endsAt ?: request.startsAt.plusSeconds(30 * 24 * 3600L),
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
            chapters = request.chapters.map { CourseChapter(title = it.title, durationText = it.durationText) }.toMutableList(),
            organizerDescription = request.organizerDescription,
            scientificAssociationName = request.scientificAssociationName,
            isVerticalPoster = request.isVerticalPoster
        )
        val saved: Course = courseRepository.save(course)
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
            course.chapters.addAll(dtos.map { CourseChapter(title = it.title, durationText = it.durationText) })
        }
        request.organizerDescription?.let { course.organizerDescription = it }
        request.scientificAssociationName?.let { course.scientificAssociationName = it }
        request.isVerticalPoster?.let { course.isVerticalPoster = it }
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
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
        return courseRepository.findByInstitutionId(institutionId, pageable)
            .map { mapCourseToResponse(it) }
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
    fun enroll(courseId: UUID, userId: UUID): EnrollmentResponse {
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
        // Charge if paid course
        if (course.priceRials > 0) {
            walletService.executeInternalPurchase(
                userId = userId,
                amount = course.priceRials,
                description = "Course enrollment: ${course.title}",
                referenceId = courseId,
                referenceType = "COURSE"
            )
        }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
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
        return mapEnrollmentToResponse(saved)
    }

    @Transactional
    fun unenroll(courseId: UUID, userId: UUID) {
        val enrollment: CourseEnrollment = enrollmentRepository.findByCourseIdAndUserId(courseId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found")
        enrollment.isActive = false
        enrollmentRepository.save(enrollment)
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
        return CourseResponse(
            id = course.id!!,
            title = course.title,
            slogan = course.slogan,
            description = course.description,
            favoritesCount = course.favoritesCount,
            teachers = course.teachers.map { it.toDto() },
            admins = course.admins.map { it.toDto() },
            organizerId = course.organizer!!.id!!,
            organizerName = course.organizer!!.displayName,
            organizerAvatarUrl = course.organizer!!.avatarUrl,
            institutionId = course.institutionId,
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
            chapters = course.chapters.map { CourseChapterDto(title = it.title, durationText = it.durationText) },
            organizerDescription = course.organizerDescription,
            scientificAssociationName = course.scientificAssociationName,
            isVerticalPoster = course.isVerticalPoster,
            createdAt = course.createdAt
        )
    }

    private fun mapEnrollmentToResponse(enrollment: CourseEnrollment): EnrollmentResponse {
        return EnrollmentResponse(
            id = enrollment.id!!,
            courseId = enrollment.course!!.id!!,
            courseTitle = enrollment.course!!.title,
            userId = enrollment.user!!.id!!,
            enrolledAt = enrollment.enrolledAt,
            isActive = enrollment.isActive
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
}
