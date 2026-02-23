package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.AdRepository
import com.iliyadev.springboot.repositories.ChannelPostRepository
import com.iliyadev.springboot.repositories.ChannelRepository
import com.iliyadev.springboot.repositories.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AdService(
    private val adRepository: AdRepository,
    private val userRepository: UserRepository,
    private val channelRepository: ChannelRepository,
    private val channelPostRepository: ChannelPostRepository
) {
    @Transactional
    fun createAdRequest(userId: UUID, request: CreateAdRequest): AdRequestDto? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val channel = channelRepository.findById(request.targetChannelId).orElse(null) ?: return null
        val adRequest = AdRequest(
            requester = user,
            sourceMessageId = request.sourceMessageId,
            sourceType = request.sourceType,
            sourceId = request.sourceId,
            targetChannel = channel,
            messageContent = request.messageContent,
            messageMediaUrl = request.messageMediaUrl,
            messageType = try { MessageType.valueOf(request.messageType) } catch (e: Exception) { MessageType.TEXT },
            status = AdRequestStatus.PENDING
        )
        val saved = adRepository.save(adRequest)
        return saved.toDto()
    }

    fun getAdRequests(status: String?): List<AdRequestDto> {
        val requests = if (status != null) {
            try {
                adRepository.findByStatusOrderByCreatedAtDesc(AdRequestStatus.valueOf(status))
            } catch (e: Exception) {
                adRepository.findAllByOrderByCreatedAtDesc()
            }
        } else {
            adRepository.findAllByOrderByCreatedAtDesc()
        }
        return requests.map { it.toDto() }
    }

    @Transactional
    fun approveAd(adId: UUID, adminId: UUID): Boolean {
        val adRequest = adRepository.findById(adId).orElse(null) ?: return false
        if (adRequest.status != AdRequestStatus.PENDING) return false
        val channel = adRequest.targetChannel ?: return false
        // Create ad post in the target channel
        val adPost = ChannelPost(
            channel = channel,
            type = adRequest.messageType,
            content = adRequest.messageContent,
            mediaUrl = adRequest.messageMediaUrl,
            isAd = true,
            adLabel = "تبلیغات",
            adSourceChannelId = adRequest.sourceId?.let {
                try { UUID.fromString(it) } catch (e: Exception) { null }
            },
            forwardedFrom = adRequest.requester?.displayName
        )
        channelPostRepository.save(adPost)
        // Update ad request status
        adRequest.status = AdRequestStatus.APPROVED
        adRequest.reviewedAt = Instant.now()
        adRequest.reviewedBy = adminId
        adRepository.save(adRequest)
        return true
    }

    @Transactional
    fun rejectAd(adId: UUID, adminId: UUID): Boolean {
        val adRequest = adRepository.findById(adId).orElse(null) ?: return false
        if (adRequest.status != AdRequestStatus.PENDING) return false
        adRequest.status = AdRequestStatus.REJECTED
        adRequest.reviewedAt = Instant.now()
        adRequest.reviewedBy = adminId
        adRepository.save(adRequest)
        return true
    }
}
