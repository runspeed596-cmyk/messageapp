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
import java.time.temporal.ChronoUnit
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// Request / Response DTOs
// ═══════════════════════════════════════════════════════════════════════════════

data class CreateHashtagRequest(
    val tag: String,
    val displayNameFa: String,
    val category: String? = null,
    val nationalChannelId: UUID? = null,
    val universityChannelId: UUID? = null,
    val branchChannelId: UUID? = null
)

data class HashtagResponse(
    val id: UUID,
    val tag: String,
    val displayNameFa: String,
    val category: String?,
    val nationalChannelId: UUID?,
    val universityChannelId: UUID?,
    val branchChannelId: UUID?,
    val isActive: Boolean,
    val createdAt: Instant
)

data class SubmitPromotionRequest(
    val hashtagId: UUID,
    val contentText: String,
    val mediaUrls: List<String> = emptyList()
)

data class ModeratePromotionRequest(
    val status: ModerationStatus, // APPROVED or REJECTED
    val rejectionReason: String? = null
)

data class PromotionResponse(
    val id: UUID,
    val hashtagId: UUID,
    val hashtagTag: String,
    val userId: UUID,
    val contentText: String,
    val mediaUrls: List<String>,
    val moderationStatus: ModerationStatus,
    val rejectionReason: String?,
    val publishedAt: Instant?,
    val createdAt: Instant
)

// ═══════════════════════════════════════════════════════════════════════════════
// Hashtag Promotion Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class HashtagPromotionService(
    private val hashtagRepository: OfficialHashtagRepository,
    private val promotionRepository: HashtagPromotionRepository,
    private val subscriptionService: SubscriptionService,
    private val userRepository: UserRepository
) {
    companion object {
        const val MAX_DAILY_PROMOTIONS_FREE: Long = 2
        const val MAX_DAILY_PROMOTIONS_PREMIUM: Long = 10
    }

    // ── Hashtag CRUD (Admin) ──

    @Transactional
    fun createHashtag(request: CreateHashtagRequest): HashtagResponse {
        if (hashtagRepository.findByTag(request.tag) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Hashtag already exists")
        }
        val hashtag = OfficialHashtag(
            tag = request.tag,
            displayNameFa = request.displayNameFa,
            category = request.category,
            nationalChannelId = request.nationalChannelId,
            universityChannelId = request.universityChannelId,
            branchChannelId = request.branchChannelId,
            isActive = true
        )
        val saved: OfficialHashtag = hashtagRepository.save(hashtag)
        return mapHashtagToResponse(saved)
    }

    @Transactional
    fun toggleHashtag(hashtagId: UUID, isActive: Boolean): HashtagResponse {
        val hashtag: OfficialHashtag = hashtagRepository.findById(hashtagId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Hashtag not found") }
        hashtag.isActive = isActive
        val saved: OfficialHashtag = hashtagRepository.save(hashtag)
        return mapHashtagToResponse(saved)
    }

    fun getAllHashtags(): List<HashtagResponse> {
        return hashtagRepository.findAll().map { mapHashtagToResponse(it) }
    }

    fun getActiveHashtags(): List<HashtagResponse> {
        return hashtagRepository.findByIsActiveTrue().map { mapHashtagToResponse(it) }
    }

    fun getHashtagsByCategory(category: String): List<HashtagResponse> {
        return hashtagRepository.findByCategory(category).map { mapHashtagToResponse(it) }
    }

    // ── Promotion Submission ──

    @Transactional
    fun submitPromotion(userId: UUID, request: SubmitPromotionRequest): PromotionResponse {
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val hashtag: OfficialHashtag = hashtagRepository.findById(request.hashtagId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Hashtag not found") }
        if (!hashtag.isActive) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Hashtag is not active")
        }
        // Rate limiting based on subscription tier
        val since: Instant = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val todayCount: Long = promotionRepository.countTodayPromotions(userId, since)
        val subscription: UserSubscriptionResponse? = subscriptionService.getMySubscription(userId)
        val maxDaily: Long = if (subscription != null && subscription.isActive) MAX_DAILY_PROMOTIONS_PREMIUM else MAX_DAILY_PROMOTIONS_FREE
        if (todayCount >= maxDaily) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily promotion limit reached ($maxDaily/day)")
        }
        val promotion = HashtagPromotion(
            hashtag = hashtag,
            user = user,
            contentText = request.contentText,
            mediaUrls = request.mediaUrls.toMutableList(),
            moderationStatus = ModerationStatus.PENDING,
            subscriptionId = subscription?.id
        )
        val saved: HashtagPromotion = promotionRepository.save(promotion)
        return mapPromotionToResponse(saved)
    }

    fun getMyPromotions(userId: UUID, pageable: Pageable): Page<PromotionResponse> {
        return promotionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { mapPromotionToResponse(it) }
    }

    // ── Moderation (Admin) ──

    fun getPendingPromotions(pageable: Pageable): Page<PromotionResponse> {
        return promotionRepository.findByModerationStatus(ModerationStatus.PENDING, pageable)
            .map { mapPromotionToResponse(it) }
    }

    @Transactional
    fun moderatePromotion(promotionId: UUID, moderatorId: UUID, request: ModeratePromotionRequest): PromotionResponse {
        val promotion: HashtagPromotion = promotionRepository.findById(promotionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found") }
        if (promotion.moderationStatus != ModerationStatus.PENDING) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Promotion already moderated")
        }
        promotion.moderationStatus = request.status
        promotion.moderatedBy = moderatorId
        promotion.moderatedAt = Instant.now()
        if (request.status == ModerationStatus.REJECTED) {
            promotion.rejectionReason = request.rejectionReason
        }
        if (request.status == ModerationStatus.APPROVED) {
            promotion.publishedAt = Instant.now()
        }
        val saved: HashtagPromotion = promotionRepository.save(promotion)
        return mapPromotionToResponse(saved)
    }

    fun getApprovedPromotions(hashtagId: UUID, pageable: Pageable): Page<PromotionResponse> {
        return promotionRepository.findByHashtagIdAndModerationStatus(hashtagId, ModerationStatus.APPROVED, pageable)
            .map { mapPromotionToResponse(it) }
    }

    // ── Private Helpers ──

    private fun mapHashtagToResponse(h: OfficialHashtag): HashtagResponse {
        return HashtagResponse(
            id = h.id!!,
            tag = h.tag,
            displayNameFa = h.displayNameFa,
            category = h.category,
            nationalChannelId = h.nationalChannelId,
            universityChannelId = h.universityChannelId,
            branchChannelId = h.branchChannelId,
            isActive = h.isActive,
            createdAt = h.createdAt
        )
    }

    private fun mapPromotionToResponse(p: HashtagPromotion): PromotionResponse {
        return PromotionResponse(
            id = p.id!!,
            hashtagId = p.hashtag!!.id!!,
            hashtagTag = p.hashtag!!.tag,
            userId = p.user!!.id!!,
            contentText = p.contentText,
            mediaUrls = p.mediaUrls,
            moderationStatus = p.moderationStatus,
            rejectionReason = p.rejectionReason,
            publishedAt = p.publishedAt,
            createdAt = p.createdAt
        )
    }
}
