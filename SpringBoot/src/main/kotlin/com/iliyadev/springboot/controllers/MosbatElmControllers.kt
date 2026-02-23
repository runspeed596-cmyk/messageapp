package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.services.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

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
    private val service: InstitutionService
) {
    @PostMapping("/register")
    fun register(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: InstitutionRegisterRequest
    ): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.registerInstitution(principal.id, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Institution registered", data = result))
    }

    @GetMapping("/my")
    fun getMyInstitutions(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<InstitutionResponse>>> {
        val result: List<InstitutionResponse> = service.getMyInstitutions(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: java.util.UUID): ResponseEntity<ApiResponse<InstitutionResponse>> {
        val result: InstitutionResponse = service.getInstitutionById(id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = result))
    }

    @GetMapping
    fun getActive(pageable: Pageable): ResponseEntity<ApiResponse<Page<InstitutionResponse>>> {
        val result: Page<InstitutionResponse> = service.getActiveInstitutions(pageable)
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
