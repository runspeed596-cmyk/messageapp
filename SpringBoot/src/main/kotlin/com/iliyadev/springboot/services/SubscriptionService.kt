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

data class SubscriptionPlanResponse(
    val id: UUID,
    val name: String,
    val tier: SubscriptionTier,
    val priceRials: Long,
    val durationDays: Int,
    val maxPromotions: Int,
    val features: String,
    val isActive: Boolean
)

data class UserSubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val planName: String,
    val tier: SubscriptionTier,
    val startsAt: Instant,
    val expiresAt: Instant,
    val isActive: Boolean,
    val autoRenew: Boolean
)

data class SubscribeToPlanRequest(
    val planId: UUID,
    val autoRenew: Boolean = false
)

data class CreateSubscriptionPlanRequest(
    val name: String,
    val tier: SubscriptionTier,
    val priceRials: Long,
    val durationDays: Int,
    val maxPromotions: Int = 1,
    val features: String = "{}"
)

@Service
class SubscriptionService(
    private val planRepository: SubscriptionPlanRepository,
    private val subscriptionRepository: UserSubscriptionRepository,
    private val walletService: WalletService,
    private val userRepository: UserRepository
) {
    fun getActivePlans(): List<SubscriptionPlanResponse> {
        return planRepository.findByIsActiveTrue().map { mapPlanToResponse(it) }
    }

    fun getMySubscription(userId: UUID): UserSubscriptionResponse? {
        val sub: UserSubscription = subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
            ?: return null
        return mapSubToResponse(sub)
    }

    fun getSubscriptionHistory(userId: UUID, pageable: Pageable): Page<UserSubscriptionResponse> {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { mapSubToResponse(it) }
    }

    @Transactional
    fun subscribeToPlan(userId: UUID, request: SubscribeToPlanRequest): UserSubscriptionResponse {
        val existingSub: UserSubscription? = subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
        if (existingSub != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already has an active subscription. Cancel first.")
        }
        val plan: SubscriptionPlan = planRepository.findById(request.planId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found") }
        if (!plan.isActive) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is no longer available")
        }
        // Deduct from wallet
        val transaction: WalletTransaction = walletService.executeInternalPurchase(
            userId = userId,
            amount = plan.priceRials,
            description = "Subscription: ${plan.name}",
            referenceId = plan.id,
            referenceType = "SUBSCRIPTION"
        )
        val now: Instant = Instant.now()
        val sub = UserSubscription(
            user = userRepository.findById(userId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") },
            plan = plan,
            startsAt = now,
            expiresAt = now.plusSeconds(plan.durationDays.toLong() * 86400L),
            isActive = true,
            autoRenew = request.autoRenew,
            transactionId = transaction.id
        )
        val saved: UserSubscription = subscriptionRepository.save(sub)
        return mapSubToResponse(saved)
    }

    @Transactional
    fun cancelSubscription(userId: UUID): UserSubscriptionResponse {
        val sub: UserSubscription = subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found")
        sub.isActive = false
        sub.autoRenew = false
        val saved: UserSubscription = subscriptionRepository.save(sub)
        return mapSubToResponse(saved)
    }

    fun isUserSubscribed(userId: UUID): Boolean {
        val sub: UserSubscription? = subscriptionRepository.findByUserIdAndIsActiveTrue(userId)
        if (sub == null) return false
        if (sub.expiresAt.isBefore(Instant.now())) {
            // Auto-expire
            sub.isActive = false
            subscriptionRepository.save(sub)
            return false
        }
        return true
    }

    fun getUserSubscriptionTier(userId: UUID): SubscriptionTier {
        val sub: UserSubscription = subscriptionRepository.findByUserIdAndIsActiveTrue(userId) ?: return SubscriptionTier.NONE
        if (sub.expiresAt.isBefore(Instant.now())) return SubscriptionTier.NONE
        return sub.plan?.tier ?: SubscriptionTier.NONE
    }

    // ── Admin: CRUD for plans ──

    @Transactional
    fun createPlan(request: CreateSubscriptionPlanRequest): SubscriptionPlanResponse {
        val plan = SubscriptionPlan(
            name = request.name,
            tier = request.tier,
            priceRials = request.priceRials,
            durationDays = request.durationDays,
            maxPromotions = request.maxPromotions,
            features = request.features,
            isActive = true
        )
        val saved: SubscriptionPlan = planRepository.save(plan)
        return mapPlanToResponse(saved)
    }

    @Transactional
    fun deactivatePlan(planId: UUID): SubscriptionPlanResponse {
        val plan: SubscriptionPlan = planRepository.findById(planId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found") }
        plan.isActive = false
        val saved: SubscriptionPlan = planRepository.save(plan)
        return mapPlanToResponse(saved)
    }

    private fun mapPlanToResponse(plan: SubscriptionPlan): SubscriptionPlanResponse {
        return SubscriptionPlanResponse(
            id = plan.id!!,
            name = plan.name,
            tier = plan.tier,
            priceRials = plan.priceRials,
            durationDays = plan.durationDays,
            maxPromotions = plan.maxPromotions,
            features = plan.features,
            isActive = plan.isActive
        )
    }

    private fun mapSubToResponse(sub: UserSubscription): UserSubscriptionResponse {
        return UserSubscriptionResponse(
            id = sub.id!!,
            userId = sub.user!!.id!!,
            planName = sub.plan?.name ?: "Unknown",
            tier = sub.plan?.tier ?: SubscriptionTier.NONE,
            startsAt = sub.startsAt,
            expiresAt = sub.expiresAt,
            isActive = sub.isActive,
            autoRenew = sub.autoRenew
        )
    }
}
