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

data class CreateCourseRequest(
    val title: String,
    val description: String? = null,
    val institutionId: UUID? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: Instant,
    val endsAt: Instant,
    val enrollmentLimit: Int? = null,
    val isPublic: Boolean = true,
    val priceRials: Long = 0,
    val tags: List<String> = emptyList()
)

data class UpdateCourseRequest(
    val title: String? = null,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val fieldOfStudy: String? = null,
    val educationLevel: String? = null,
    val startsAt: Instant? = null,
    val endsAt: Instant? = null,
    val enrollmentLimit: Int? = null,
    val isPublic: Boolean? = null,
    val priceRials: Long? = null,
    val tags: List<String>? = null
)

data class CourseResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val organizerId: UUID,
    val organizerName: String?,
    val institutionId: UUID?,
    val channelId: UUID?,
    val groupId: UUID?,
    val coverImageUrl: String?,
    val fieldOfStudy: String?,
    val educationLevel: String?,
    val startsAt: Instant,
    val endsAt: Instant,
    val enrollmentLimit: Int?,
    val enrolledCount: Long,
    val isPublic: Boolean,
    val status: CourseStatus,
    val priceRials: Long,
    val tags: List<String>,
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

// ═══════════════════════════════════════════════════════════════════════════════
// Course Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class CourseService(
    private val courseRepository: CourseRepository,
    private val enrollmentRepository: CourseEnrollmentRepository,
    private val materialRepository: CourseMaterialRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository
) {
    // ── Course CRUD ──

    @Transactional
    fun createCourse(organizerId: UUID, request: CreateCourseRequest): CourseResponse {
        val organizer: User = userRepository.findById(organizerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (organizer.role != UserRole.TEACHER && organizer.role != UserRole.INSTITUTION && organizer.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers, institutions, or admins can create courses")
        }
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
        val course = Course(
            title = request.title,
            description = request.description,
            organizer = organizer,
            institutionId = request.institutionId,
            group = savedGroup,
            coverImageUrl = request.coverImageUrl,
            fieldOfStudy = request.fieldOfStudy,
            educationLevel = request.educationLevel,
            startsAt = request.startsAt,
            endsAt = request.endsAt,
            enrollmentLimit = request.enrollmentLimit,
            isPublic = request.isPublic,
            status = CourseStatus.DRAFT,
            priceRials = request.priceRials,
            tags = request.tags.toMutableList()
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
        request.isPublic?.let { course.isPublic = it }
        request.priceRials?.let { course.priceRials = it }
        request.tags?.let { course.tags = it.toMutableList() }
        course.updatedAt = Instant.now()
        val saved: Course = courseRepository.save(course)
        return mapCourseToResponse(saved)
    }

    @Transactional
    fun publishCourse(courseId: UUID, organizerId: UUID): CourseResponse {
        val course: Course = getOwnedCourse(courseId, organizerId)
        if (course.status != CourseStatus.DRAFT) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only DRAFT courses can be published")
        }
        course.status = CourseStatus.PUBLISHED
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

    fun getPublicCourses(pageable: Pageable): Page<CourseResponse> {
        return courseRepository.findByStatusAndIsPublicTrue(CourseStatus.PUBLISHED, pageable)
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
    fun deleteCourse(courseId: UUID, organizerId: UUID) {
        val course: Course = getOwnedCourse(courseId, organizerId)
        if (course.status == CourseStatus.PUBLISHED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete a published course. Archive it first.")
        }
        courseRepository.delete(course)
    }

    // ── Enrollment ──

    @Transactional
    fun enroll(courseId: UUID, userId: UUID): EnrollmentResponse {
        val course: Course = courseRepository.findById(courseId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found") }
        if (course.status != CourseStatus.PUBLISHED) {
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
            description = course.description,
            organizerId = course.organizer!!.id!!,
            organizerName = course.organizer!!.displayName,
            institutionId = course.institutionId,
            channelId = course.channel?.id,
            groupId = course.group?.id,
            coverImageUrl = course.coverImageUrl,
            fieldOfStudy = course.fieldOfStudy,
            educationLevel = course.educationLevel,
            startsAt = course.startsAt,
            endsAt = course.endsAt,
            enrollmentLimit = course.enrollmentLimit,
            enrolledCount = enrolledCount,
            isPublic = course.isPublic,
            status = course.status,
            priceRials = course.priceRials,
            tags = course.tags,
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
