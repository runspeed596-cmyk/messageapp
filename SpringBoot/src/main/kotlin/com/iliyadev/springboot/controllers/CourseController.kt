package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📚 Course Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService
) {
    // ── Course CRUD ──

    @PostMapping
    fun createCourse(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateCourseRequest
    ): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.createCourse(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course created", data = result))
    }

    @PutMapping("/{id}")
    fun updateCourse(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: UpdateCourseRequest
    ): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.updateCourse(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course updated", data = result))
    }

    @PostMapping("/{id}/publish")
    fun publishCourse(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.publishCourse(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course published", data = result))
    }

    @PostMapping("/{id}/archive")
    fun archiveCourse(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.archiveCourse(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course archived", data = result))
    }

    @GetMapping("/{id}")
    fun getCourse(@PathVariable id: UUID): ResponseEntity<ApiResponse<CourseResponse>> {
        val result: CourseResponse = courseService.getCourseById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/my")
    fun getMyCourses(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<CourseResponse>>> {
        val result: Page<CourseResponse> = courseService.getMyCourses(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping
    fun getPublicCourses(pageable: Pageable): ResponseEntity<ApiResponse<Page<CourseResponse>>> {
        val result: Page<CourseResponse> = courseService.getPublicCourses(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/upcoming")
    fun getUpcomingCourses(pageable: Pageable): ResponseEntity<ApiResponse<Page<CourseResponse>>> {
        val result: Page<CourseResponse> = courseService.getUpcomingCourses(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/institution/{institutionId}")
    fun getInstitutionCourses(
        @PathVariable institutionId: UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<CourseResponse>>> {
        val result: Page<CourseResponse> = courseService.getInstitutionCourses(institutionId, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @DeleteMapping("/{id}")
    fun deleteCourse(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        courseService.deleteCourse(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Course deleted", data = Unit))
    }

    // ── Enrollment ──

    @PostMapping("/{id}/enroll")
    fun enroll(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<EnrollmentResponse>> {
        val result: EnrollmentResponse = courseService.enroll(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Enrolled successfully", data = result))
    }

    @PostMapping("/{id}/unenroll")
    fun unenroll(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        courseService.unenroll(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Unenrolled successfully", data = Unit))
    }

    @GetMapping("/enrollments")
    fun getMyEnrollments(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<EnrollmentResponse>>> {
        val result: Page<EnrollmentResponse> = courseService.getMyEnrollments(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{id}/enrolled")
    fun isEnrolled(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val result: Boolean = courseService.isEnrolled(id, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    // ── Materials ──

    @PostMapping("/{id}/materials")
    fun addMaterial(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID,
        @RequestBody request: AddMaterialRequest
    ): ResponseEntity<ApiResponse<CourseMaterialResponse>> {
        val result: CourseMaterialResponse = courseService.addMaterial(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Material added", data = result))
    }

    @PutMapping("/materials/{materialId}")
    fun updateMaterial(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable materialId: UUID,
        @RequestBody request: UpdateMaterialRequest
    ): ResponseEntity<ApiResponse<CourseMaterialResponse>> {
        val result: CourseMaterialResponse = courseService.updateMaterial(materialId, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Material updated", data = result))
    }

    @DeleteMapping("/materials/{materialId}")
    fun deleteMaterial(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable materialId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        courseService.deleteMaterial(materialId, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Material deleted", data = Unit))
    }

    @GetMapping("/{id}/materials")
    fun getCourseMaterials(@PathVariable id: UUID): ResponseEntity<ApiResponse<List<CourseMaterialResponse>>> {
        val result: List<CourseMaterialResponse> = courseService.getCourseMaterials(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}
