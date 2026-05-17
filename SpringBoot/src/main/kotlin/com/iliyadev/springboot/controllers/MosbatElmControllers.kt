package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.repositories.HomeBannerRepository
import com.iliyadev.springboot.repositories.CourseRepository
import com.iliyadev.springboot.repositories.InstitutionRepository
import com.iliyadev.springboot.repositories.UserRepository
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Instant

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Teacher Verification Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/teacher-verification")
class TeacherVerificationController(
    private val service: TeacherVerificationService
) {
    @PostMapping("/submit")
    fun submitRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: TeacherVerificationSubmitRequest
    ): ResponseEntity<ApiResponse<TeacherVerificationResponse>> {
        val result: TeacherVerificationResponse = service.submitVerificationRequest(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Verification request submitted", data = result))
    }

    @GetMapping("/status")
    fun getMyStatus(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<TeacherVerificationResponse?>> {
        val result: TeacherVerificationResponse? = service.getMyVerificationStatus(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💰 Wallet Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/wallet")
class WalletController(
    private val walletService: WalletService
) {
    @GetMapping
    fun getWallet(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<WalletResponse>> {
        val result: WalletResponse = walletService.getWalletInfo(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/deposit")
    fun deposit(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: DepositRequest
    ): ResponseEntity<ApiResponse<WalletTransactionResponse>> {
        val result: WalletTransactionResponse = walletService.deposit(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Deposit successful", data = result))
    }

    @PostMapping("/withdraw")
    fun withdraw(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: WithdrawRequest
    ): ResponseEntity<ApiResponse<WalletTransactionResponse>> {
        val result: WalletTransactionResponse = walletService.withdraw(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Withdrawal successful", data = result))
    }

    @GetMapping("/transactions")
    fun getTransactions(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<WalletTransactionResponse>>> {
        val result: Page<WalletTransactionResponse> = walletService.getTransactionHistory(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💳 Subscription Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val service: SubscriptionService
) {
    @GetMapping("/plans")
    fun getPlans(): ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> {
        val result: List<SubscriptionPlanResponse> = service.getActivePlans()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/my")
    fun getMySubscription(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserSubscriptionResponse?>> {
        val result: UserSubscriptionResponse? = service.getMySubscription(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/subscribe")
    fun subscribe(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SubscribeToPlanRequest
    ): ResponseEntity<ApiResponse<UserSubscriptionResponse>> {
        val result: UserSubscriptionResponse = service.subscribeToPlan(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Subscription activated", data = result))
    }

    @PostMapping("/cancel")
    fun cancel(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UserSubscriptionResponse>> {
        val result: UserSubscriptionResponse = service.cancelSubscription(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Subscription cancelled", data = result))
    }

    @GetMapping("/history")
    fun getHistory(
        @AuthenticationPrincipal principal: UserPrincipal,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<UserSubscriptionResponse>>> {
        val result: Page<UserSubscriptionResponse> = service.getSubscriptionHistory(principal.id, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🏛️ Institution (Elm Club) Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/institutions")
class InstitutionController(
    private val service: InstitutionService,
    private val fileUploadService: FileUploadService
) {
    @PostMapping("/register")
    fun register(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: InstitutionRegisterRequest
    ): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.registerInstitution(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Institution registered", data = result))
    }

    @PostMapping("/upload-logo")
    fun uploadLogo(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam("file") file: org.springframework.web.multipart.MultipartFile
    ): ResponseEntity<ApiResponse<String>> {
        val logoUrl: String = fileUploadService.uploadFile(
            file.bytes,
            file.originalFilename ?: "institution_logo.jpg",
            file.contentType ?: "image/jpeg"
        )
        return ResponseEntity.ok(ApiResponse(success = true, message = "Logo uploaded", data = logoUrl))
    }

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: java.util.UUID,
        @RequestBody request: InstitutionRegisterRequest
    ): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.updateInstitution(id, principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Institution updated", data = result))
    }

    @GetMapping("/my")
    fun getMyInstitutions(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<InstitutionResponse>>> {
        val result: List<InstitutionResponse> = service.getMyInstitutions(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/active")
    fun getActive(pageable: Pageable): ResponseEntity<ApiResponse<Page<InstitutionResponse>>> {
        val result: Page<InstitutionResponse> = service.getActiveInstitutions(pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: java.util.UUID): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.getInstitutionById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/{id}/link-channel")
    fun linkChannel(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: java.util.UUID,
        @RequestParam channelId: java.util.UUID
    ): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.linkChannel(id, channelId, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Channel linked", data = result))
    }

    @GetMapping("/{id}/honors")
    fun getHonors(@PathVariable id: java.util.UUID): ResponseEntity<ApiResponse<List<InstitutionHonorDto>>> {
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = service.getHonors(id)))
    }

    @GetMapping("/{id}/teachers")
    fun getTeachers(@PathVariable id: java.util.UUID): ResponseEntity<ApiResponse<List<UserDto>>> {
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = service.getTeachers(id)))
    }

    @GetMapping("/{id}/admins")
    fun getAdmins(@PathVariable id: java.util.UUID): ResponseEntity<ApiResponse<List<UserDto>>> {
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = service.getAdmins(id)))
    }

    @PostMapping("/{id}/honors")
    fun addHonor(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: java.util.UUID,
        @RequestBody request: InstitutionHonorDto
    ): ResponseEntity<ApiResponse<InstitutionHonorDto>> {
        val result = service.addHonor(id, principal.id, request.title, request.description, request.imageUrl, request.date)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Honor added", data = result))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 Smart Folder Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/smart-folders")
class SmartFolderController(
    private val service: SmartFolderEngineService
) {
    @GetMapping
    fun getSmartFolders(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<SmartFolderResponse>>> {
        val result: List<SmartFolderResponse> = service.computeSmartFolders(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }
}

@RestController
@RequestMapping("/api/mosbat-elm/home")
class MosbatElmHomeController(
    private val bannerRepository: HomeBannerRepository,
    private val institutionService: InstitutionService,
    private val institutionRepository: InstitutionRepository,
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {
    @GetMapping
    fun getHomeData(): ResponseEntity<ApiResponse<MosbatElmHomeDataDto>> {
        val bannerEntities = bannerRepository.findAllBySectionAndIsActiveTrueOrderByCreatedAtDesc("MOSBAT_ELM")
        val banners: List<HomeBannerDto> = bannerEntities.map { b -> b.toDto() }
        
        val institutionsPage = institutionService.getActiveInstitutions(PageRequest.of(0, 10))
        val featuredInstitutions: List<InstitutionResponse> = institutionsPage.content
        
        // Fetch popular institutions
        val popularInstitutionsPage = institutionRepository.findPopularInstitutions(PageRequest.of(0, 10))
        val popularInstitutions = popularInstitutionsPage.content.map { inst -> inst.toResponse() }

        // Fetch popular teachers
        val popularTeachersPage = userRepository.findPopularTeachers(PageRequest.of(0, 10))
        val popularTeachers = popularTeachersPage.content.map { user -> user.toDto() }

        // Fetch upcoming approved courses
        val coursesPage = courseRepository.findByStatus(CourseStatus.APPROVED, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
        val instIds: List<java.util.UUID> = coursesPage.content.mapNotNull { course -> course.institutionId }.distinct()
        val institutions = institutionRepository.findAllById(instIds).associateBy { inst -> inst.id }
        val upcomingCourses: List<CourseDto> = coursesPage.content.map { c: Course -> 
            c.toDto(institutions[c.institutionId]?.type?.name) 
        }
        
        val data = MosbatElmHomeDataDto(
            banners = banners,
            featuredInstitutions = featuredInstitutions,
            popularInstitutions = popularInstitutions,
            upcomingCourses = upcomingCourses,
            popularTeachers = popularTeachers
        )
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = data))
    }
}

@RestController
@RequestMapping("/api/mosbat-elm/popular")
class MosbatElmPopularController(
    private val userRepository: UserRepository,
    private val institutionRepository: InstitutionRepository,
    private val courseRepository: CourseRepository
) {
    @GetMapping("/teachers")
    fun getPopularTeachers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<Page<UserDto>>> {
        val result = userRepository.findPopularTeachers(PageRequest.of(page, size))
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result.map { user -> user.toDto() }))
    }

    @GetMapping("/organizers")
    fun getPopularOrganizers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<Page<InstitutionResponse>>> {
        val result = institutionRepository.findPopularInstitutions(PageRequest.of(page, size))
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result.map { it.toResponse() }))
    }

    @GetMapping("/teachers/{id}/courses")
    fun getTeacherCourses(
        @PathVariable id: java.util.UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<Page<CourseDto>>> {
        // We find courses where this user is either a teacher or organizer
        val coursesPage = courseRepository.findByTeachersIdOrOrganizerId(id, id, PageRequest.of(page, size))
        val instIds: List<java.util.UUID> = coursesPage.content.mapNotNull { it.institutionId }.distinct()
        val institutions = institutionRepository.findAllById(instIds).associateBy { it.id }
        
        val dtos = coursesPage.map { c: com.iliyadev.springboot.models.Course -> 
            c.toDto(institutions[c.institutionId]?.type?.name) 
        }
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = dtos))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Course Collaboration Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/course-collaborations")
class CourseCollaborationController(
    private val collaborationService: CourseCollaborationService
) {
    @PostMapping("/course/{courseId}/request")
    fun requestCollaboration(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable courseId: java.util.UUID,
        @RequestBody request: CreateCollaborationRequest
    ): ResponseEntity<ApiResponse<CourseCollaborationRequestDto>> {
        val result = collaborationService.createRequest(principal.id, courseId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Collaboration requested", data = result))
    }

    @GetMapping("/academy/{academyId}/pending")
    fun getPendingRequests(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable academyId: java.util.UUID,
        pageable: Pageable
    ): ResponseEntity<ApiResponse<Page<CourseCollaborationRequestDto>>> {
        val result = collaborationService.getPendingRequests(principal.id, academyId, pageable)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @PostMapping("/{requestId}/accept")
    fun acceptRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: java.util.UUID
    ): ResponseEntity<ApiResponse<CourseCollaborationRequestDto>> {
        val result = collaborationService.acceptRequest(principal.id, requestId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Collaboration accepted", data = result))
    }

    @PostMapping("/{requestId}/reject")
    fun rejectRequest(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable requestId: java.util.UUID
    ): ResponseEntity<ApiResponse<CourseCollaborationRequestDto>> {
        val result = collaborationService.rejectRequest(principal.id, requestId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Collaboration rejected", data = result))
    }
}
