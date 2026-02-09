package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import com.iliyadev.springboot.websocket.WebSocketMessageHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Profile Details Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ProfileDetailsService(
    private val profileDetailsRepository: UserProfileDetailsRepository,
    private val userRepository: UserRepository
) {
    fun getProfileDetails(userId: UUID): ProfileDetailsDto? {
        return profileDetailsRepository.findByUserId(userId)?.toDto()
    }
    @Transactional
    fun updateProfileDetails(userId: UUID, request: UpdateProfileDetailsRequest): ProfileDetailsDto {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val existing = profileDetailsRepository.findByUserId(userId)
        val details = existing ?: UserProfileDetails(user = user)
        request.university?.let { details.university = it }
        request.fieldOfStudy?.let { details.fieldOfStudy = it }
        request.education?.let { details.education = it }
        request.interests?.let { details.interests = it.joinToString(",") }
        request.achievements?.let { details.achievements = it.joinToString(",") }
        request.skills?.let { details.skills = it.joinToString(",") }
        request.workExperience?.let { details.workExperience = it }
        details.updatedAt = Instant.now()
        return profileDetailsRepository.save(details).toDto()
    }
    fun hasProfileDetails(userId: UUID): Boolean {
        return profileDetailsRepository.existsByUserId(userId)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Follow Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class FollowService(
    private val followRepository: UserFollowRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService,
    private val webSocketMessageHandler: WebSocketMessageHandler
) {
    @Transactional
    fun followUser(followerId: UUID, followingId: UUID): Boolean {
        if (followerId == followingId) return false
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) return false
        val follower = userRepository.findById(followerId).orElseThrow { IllegalArgumentException("Follower not found") }
        val following = userRepository.findById(followingId).orElseThrow { IllegalArgumentException("User to follow not found") }
        val follow = UserFollow(
            follower = follower,
            following = following,
            status = FollowStatus.ACCEPTED
        )
        followRepository.save(follow)
        notificationService.createFollowNotification(followerId, followingId)
        return true
    }
    @Transactional
    fun unfollowUser(followerId: UUID, followingId: UUID): Boolean {
        if (!followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) return false
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId)
        return true
    }
    fun getFollowers(userId: UUID, page: Int, size: Int): FollowListResponse {
        val pageable = PageRequest.of(page, size)
        val followersPage = followRepository.findFollowersByUserId(userId, pageable)
        return FollowListResponse(
            users = followersPage.content.map { it.toFollowerDto() },
            totalCount = followersPage.totalElements.toInt(),
            hasMore = followersPage.hasNext()
        )
    }
    fun getFollowing(userId: UUID, page: Int, size: Int): FollowListResponse {
        val pageable = PageRequest.of(page, size)
        val followingPage = followRepository.findFollowingByUserId(userId, pageable)
        return FollowListResponse(
            users = followingPage.content.map { it.toFollowingDto() },
            totalCount = followingPage.totalElements.toInt(),
            hasMore = followingPage.hasNext()
        )
    }
    fun getFollowCounts(userId: UUID): FollowCountsDto {
        return FollowCountsDto(
            followerCount = followRepository.countFollowersByUserId(userId),
            followingCount = followRepository.countFollowingByUserId(userId)
        )
    }
    fun isFollowing(followerId: UUID, followingId: UUID): Boolean {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class CollaborationService(
    private val collaborationRepository: CollaborationRequestRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    @Transactional
    fun sendRequest(senderId: UUID, request: SendCollaborationRequest): CollaborationRequestDto {
        val sender = userRepository.findById(senderId).orElseThrow { IllegalArgumentException("Sender not found") }
        val receiver = userRepository.findById(request.receiverId).orElseThrow { IllegalArgumentException("Receiver not found") }
        if (collaborationRepository.existsBySenderIdAndReceiverIdAndStatus(senderId, request.receiverId, CollaborationStatus.PENDING)) {
            throw IllegalStateException("Pending request already exists")
        }
        val collaboration = CollaborationRequest(
            sender = sender,
            receiver = receiver,
            title = request.title,
            message = request.message
        )
        val saved = collaborationRepository.save(collaboration)
        notificationService.createCollaborationRequestNotification(senderId, request.receiverId, saved.id!!)
        return saved.toDto()
    }
    @Transactional
    fun acceptRequest(requestId: UUID, userId: UUID): CollaborationRequestDto {
        val request = collaborationRepository.findById(requestId).orElseThrow { IllegalArgumentException("Request not found") }
        if (request.receiver?.id != userId) throw SecurityException("Not authorized")
        if (request.status != CollaborationStatus.PENDING) throw IllegalStateException("Request already processed")
        request.status = CollaborationStatus.ACCEPTED
        request.respondedAt = Instant.now()
        val saved = collaborationRepository.save(request)
        notificationService.createCollaborationResponseNotification(request.sender?.id!!, userId, requestId, true)
        return saved.toDto()
    }
    @Transactional
    fun rejectRequest(requestId: UUID, userId: UUID): CollaborationRequestDto {
        val request = collaborationRepository.findById(requestId).orElseThrow { IllegalArgumentException("Request not found") }
        if (request.receiver?.id != userId) throw SecurityException("Not authorized")
        if (request.status != CollaborationStatus.PENDING) throw IllegalStateException("Request already processed")
        request.status = CollaborationStatus.REJECTED
        request.respondedAt = Instant.now()
        val saved = collaborationRepository.save(request)
        notificationService.createCollaborationResponseNotification(request.sender?.id!!, userId, requestId, false)
        return saved.toDto()
    }
    fun getReceivedRequests(userId: UUID, page: Int, size: Int): CollaborationListResponse {
        val pageable = PageRequest.of(page, size)
        val requestsPage = collaborationRepository.findReceivedByUserId(userId, pageable)
        return CollaborationListResponse(
            requests = requestsPage.content.map { it.toDto() },
            totalCount = requestsPage.totalElements.toInt(),
            hasMore = requestsPage.hasNext()
        )
    }
    fun getSentRequests(userId: UUID, page: Int, size: Int): CollaborationListResponse {
        val pageable = PageRequest.of(page, size)
        val requestsPage = collaborationRepository.findSentByUserId(userId, pageable)
        return CollaborationListResponse(
            requests = requestsPage.content.map { it.toDto() },
            totalCount = requestsPage.totalElements.toInt(),
            hasMore = requestsPage.hasNext()
        )
    }
    fun getPendingCount(userId: UUID): Int {
        return collaborationRepository.countPendingByReceiverId(userId)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler
) {
    @Transactional
    fun createNotification(
        userId: UUID,
        type: NotificationType,
        title: String,
        body: String,
        relatedEntityId: UUID? = null,
        actorId: UUID? = null
    ): NotificationDto {
        val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException("User not found") }
        val actor = actorId?.let { userRepository.findById(it).orElse(null) }
        val notification = Notification(
            user = user,
            type = type,
            title = title,
            body = body,
            relatedEntityId = relatedEntityId,
            actorId = actorId,
            actorName = actor?.displayName,
            actorAvatarUrl = actor?.avatarUrl
        )
        val saved = notificationRepository.save(notification)
        sendNotificationViaWebSocket(userId, saved.toDto())
        return saved.toDto()
    }
    fun createFollowNotification(followerId: UUID, followingId: UUID) {
        val follower = userRepository.findById(followerId).orElse(null) ?: return
        createNotification(
            userId = followingId,
            type = NotificationType.FOLLOW,
            title = "دنبال‌کننده جدید",
            body = "${follower.displayName} شما را دنبال کرد",
            actorId = followerId
        )
    }
    fun createCollaborationRequestNotification(senderId: UUID, receiverId: UUID, requestId: UUID) {
        val sender = userRepository.findById(senderId).orElse(null) ?: return
        createNotification(
            userId = receiverId,
            type = NotificationType.COLLABORATION_REQUEST,
            title = "درخواست همکاری",
            body = "${sender.displayName} درخواست همکاری ارسال کرد",
            relatedEntityId = requestId,
            actorId = senderId
        )
    }
    fun createCollaborationResponseNotification(receiverId: UUID, responderId: UUID, requestId: UUID, isAccepted: Boolean) {
        val responder = userRepository.findById(responderId).orElse(null) ?: return
        val type = if (isAccepted) NotificationType.COLLABORATION_ACCEPTED else NotificationType.COLLABORATION_REJECTED
        val action = if (isAccepted) "پذیرفت" else "رد کرد"
        createNotification(
            userId = receiverId,
            type = type,
            title = "پاسخ درخواست همکاری",
            body = "${responder.displayName} درخواست همکاری شما را $action",
            relatedEntityId = requestId,
            actorId = responderId
        )
    }
    fun getNotifications(userId: UUID, page: Int, size: Int): NotificationListResponse {
        val pageable = PageRequest.of(page, size)
        val notificationsPage = notificationRepository.findByUserId(userId, pageable)
        val unreadCount = notificationRepository.countUnreadByUserId(userId)
        return NotificationListResponse(
            notifications = notificationsPage.content.map { it.toDto() },
            totalCount = notificationsPage.totalElements.toInt(),
            unreadCount = unreadCount,
            hasMore = notificationsPage.hasNext()
        )
    }
    @Transactional
    fun markAsRead(notificationId: UUID, userId: UUID): Boolean {
        val notification = notificationRepository.findById(notificationId).orElse(null) ?: return false
        if (notification.user?.id != userId) return false
        notification.isRead = true
        notificationRepository.save(notification)
        return true
    }
    @Transactional
    fun markAllAsRead(userId: UUID): Int {
        val pageable = PageRequest.of(0, 100)
        val unread = notificationRepository.findUnreadByUserId(userId, pageable)
        unread.content.forEach { it.isRead = true }
        notificationRepository.saveAll(unread.content)
        return unread.content.size
    }
    fun getUnreadCount(userId: UUID): Int {
        return notificationRepository.countUnreadByUserId(userId)
    }
    private fun sendNotificationViaWebSocket(userId: UUID, notification: NotificationDto) {
        try {
            webSocketMessageHandler.sendNotification(userId, notification)
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }
}
