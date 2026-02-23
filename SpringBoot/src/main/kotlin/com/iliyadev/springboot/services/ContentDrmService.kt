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

data class UploadLockedContentRequest(
    val channelId: UUID,
    val title: String,
    val description: String? = null,
    val contentType: String, // VIDEO, AUDIO, FILE, TEXT, IMAGE
    val storageKey: String,
    val encryptionKey: String,
    val thumbnailUrl: String? = null,
    val priceRials: Long = 0
)

data class UpdateLockedContentRequest(
    val title: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val priceRials: Long? = null,
    val lockStatus: ContentLockStatus? = null
)

data class LockedContentResponse(
    val id: UUID,
    val channelId: UUID,
    val uploaderId: UUID,
    val title: String,
    val description: String?,
    val contentType: String,
    val thumbnailUrl: String?,
    val priceRials: Long,
    val lockStatus: ContentLockStatus,
    val viewCount: Int,
    val purchaseCount: Int,
    val createdAt: Instant,
    val hasPurchased: Boolean
)

data class ContentAccessResponse(
    val contentId: UUID,
    val storageKey: String,
    val encryptionKey: String,
    val contentType: String,
    val title: String
)

data class ContentPurchaseResponse(
    val id: UUID,
    val contentId: UUID,
    val contentTitle: String,
    val contentType: String,
    val purchasedAt: Instant,
    val expiresAt: Instant?
)

// ═══════════════════════════════════════════════════════════════════════════════
// Content DRM Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ContentDrmService(
    private val lockedContentRepository: LockedContentRepository,
    private val contentPurchaseRepository: ContentPurchaseRepository,
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val walletService: WalletService
) {
    // ── Content Upload & Management ──

    @Transactional
    fun uploadContent(uploaderId: UUID, request: UploadLockedContentRequest): LockedContentResponse {
        val uploader: User = userRepository.findById(uploaderId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        if (uploader.role != UserRole.TEACHER && uploader.role != UserRole.INSTITUTION && uploader.role != UserRole.ADMIN) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers, institutions, or admins can upload locked content")
        }
        val channel: Channel = channelRepository.findById(request.channelId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Channel not found") }
        val content = LockedContent(
            channel = channel,
            uploader = uploader,
            title = request.title,
            description = request.description,
            contentType = request.contentType,
            storageKey = request.storageKey,
            encryptionKey = request.encryptionKey,
            thumbnailUrl = request.thumbnailUrl,
            priceRials = request.priceRials,
            lockStatus = ContentLockStatus.LOCKED
        )
        val saved: LockedContent = lockedContentRepository.save(content)
        // Creator does NOT auto-get access. They must purchase like anyone else.
        val hasPurchased: Boolean = false
        return mapContentToResponse(saved, hasPurchased)
    }

    @Transactional
    fun updateContent(contentId: UUID, uploaderId: UUID, request: UpdateLockedContentRequest): LockedContentResponse {
        val content: LockedContent = getOwnedContent(contentId, uploaderId)
        request.title?.let { content.title = it }
        request.description?.let { content.description = it }
        request.thumbnailUrl?.let { content.thumbnailUrl = it }
        request.priceRials?.let { content.priceRials = it }
        request.lockStatus?.let { content.lockStatus = it }
        content.updatedAt = Instant.now()
        val saved: LockedContent = lockedContentRepository.save(content)
        val hasPurchased: Boolean = contentPurchaseRepository.existsByContentIdAndUserId(saved.id!!, uploaderId)
        return mapContentToResponse(saved, hasPurchased)
    }

    @Transactional
    fun deleteContent(contentId: UUID, uploaderId: UUID) {
        val content: LockedContent = getOwnedContent(contentId, uploaderId)
        lockedContentRepository.delete(content)
    }

    fun getContentById(contentId: UUID, userId: UUID?): LockedContentResponse {
        val content: LockedContent = lockedContentRepository.findById(contentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found") }
        val hasPurchased: Boolean = if (userId != null) {
            contentPurchaseRepository.existsByContentIdAndUserId(contentId, userId)
        } else {
            false
        }
        return mapContentToResponse(content, hasPurchased)
    }

    fun getChannelContent(channelId: UUID, userId: UUID?, pageable: Pageable): Page<LockedContentResponse> {
        return lockedContentRepository.findByChannelId(channelId, pageable).map { content ->
            val hasPurchased: Boolean = if (userId != null) {
                contentPurchaseRepository.existsByContentIdAndUserId(content.id!!, userId)
            } else {
                false
            }
            mapContentToResponse(content, hasPurchased)
        }
    }

    fun getMyUploadedContent(uploaderId: UUID, pageable: Pageable): Page<LockedContentResponse> {
        return lockedContentRepository.findByUploaderId(uploaderId, pageable)
            .map { content ->
                val hasPurchased: Boolean = contentPurchaseRepository.existsByContentIdAndUserId(content.id!!, uploaderId)
                mapContentToResponse(content, hasPurchased)
            }
    }

    // ── Purchase Flow ──

    @Transactional
    fun purchaseContent(contentId: UUID, userId: UUID): ContentPurchaseResponse {
        val content: LockedContent = lockedContentRepository.findById(contentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found") }
        if (content.lockStatus != ContentLockStatus.LOCKED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is not available for purchase")
        }
        // Creator CAN self-purchase (for testing). No owner exemption.
        if (contentPurchaseRepository.existsByContentIdAndUserId(contentId, userId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Already purchased")
        }
        // Free content — skip wallet
        if (content.priceRials > 0) {
            val isCreatorSelfTest: Boolean = content.uploader?.id == userId
            val description: String = if (isCreatorSelfTest) {
                "Internal test purchase: ${content.title}"
            } else {
                "Content purchase: ${content.title}"
            }
            walletService.executeInternalPurchase(
                userId = userId,
                amount = content.priceRials,
                description = description,
                referenceId = contentId,
                referenceType = if (isCreatorSelfTest) "INTERNAL_TEST_PURCHASE" else "CONTENT"
            )
        }
        val user: User = userRepository.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        val purchase = ContentPurchase(
            content = content,
            user = user,
            purchasedAt = Instant.now()
        )
        val saved: ContentPurchase = contentPurchaseRepository.save(purchase)
        // Update purchase count
        content.purchaseCount += 1
        lockedContentRepository.save(content)
        return mapPurchaseToResponse(saved)
    }

    // ── Content Access (Streaming Middleware) ──

    fun getContentAccess(contentId: UUID, userId: UUID): ContentAccessResponse {
        val content: LockedContent = lockedContentRepository.findById(contentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found") }
        // ZERO-TRUST: No owner bypass. Everyone must have a purchase record.
        if (content.lockStatus == ContentLockStatus.ARCHIVED) {
            throw ResponseStatusException(HttpStatus.GONE, "Content has been archived")
        }
        if (content.lockStatus == ContentLockStatus.LOCKED) {
            val hasPurchased: Boolean = contentPurchaseRepository.existsByContentIdAndUserId(contentId, userId)
            if (!hasPurchased) {
                throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Purchase required to access this content")
            }
            // Check expiration
            val purchase: ContentPurchase? = contentPurchaseRepository.findByContentIdAndUserId(contentId, userId)
            if (purchase?.expiresAt != null && purchase.expiresAt!!.isBefore(Instant.now())) {
                throw ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Purchase expired. Re-purchase to access.")
            }
        }
        // Increment view count
        content.viewCount += 1
        lockedContentRepository.save(content)
        return ContentAccessResponse(
            contentId = content.id!!,
            storageKey = content.storageKey,
            encryptionKey = content.encryptionKey,
            contentType = content.contentType,
            title = content.title
        )
    }

    // ── Purchase History ──

    fun getMyPurchases(userId: UUID, pageable: Pageable): Page<ContentPurchaseResponse> {
        return contentPurchaseRepository.findByUserIdOrderByPurchasedAtDesc(userId, pageable)
            .map { mapPurchaseToResponse(it) }
    }

    fun hasPurchased(contentId: UUID, userId: UUID): Boolean {
        return contentPurchaseRepository.existsByContentIdAndUserId(contentId, userId)
    }

    // ── Private Helpers ──

    private fun getOwnedContent(contentId: UUID, uploaderId: UUID): LockedContent {
        val content: LockedContent = lockedContentRepository.findById(contentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Content not found") }
        if (content.uploader?.id != uploaderId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this content")
        }
        return content
    }

    private fun mapContentToResponse(content: LockedContent, hasPurchased: Boolean): LockedContentResponse {
        return LockedContentResponse(
            id = content.id!!,
            channelId = content.channel!!.id!!,
            uploaderId = content.uploader!!.id!!,
            title = content.title,
            description = content.description,
            contentType = content.contentType,
            thumbnailUrl = content.thumbnailUrl,
            priceRials = content.priceRials,
            lockStatus = content.lockStatus,
            viewCount = content.viewCount,
            purchaseCount = content.purchaseCount,
            createdAt = content.createdAt,
            hasPurchased = hasPurchased
        )
    }

    private fun mapPurchaseToResponse(purchase: ContentPurchase): ContentPurchaseResponse {
        return ContentPurchaseResponse(
            id = purchase.id!!,
            contentId = purchase.content!!.id!!,
            contentTitle = purchase.content!!.title,
            contentType = purchase.content!!.contentType,
            purchasedAt = purchase.purchasedAt,
            expiresAt = purchase.expiresAt
        )
    }
}
