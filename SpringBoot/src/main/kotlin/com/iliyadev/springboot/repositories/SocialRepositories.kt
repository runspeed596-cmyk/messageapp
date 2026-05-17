package com.iliyadev.springboot.repositories

import com.iliyadev.springboot.models.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 User Profile Details Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface UserProfileDetailsRepository : JpaRepository<UserProfileDetails, UUID> {
    fun findByUserId(userId: UUID): UserProfileDetails?
    fun existsByUserId(userId: UUID): Boolean
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 User Follow Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface UserFollowRepository : JpaRepository<UserFollow, UUID> {
    fun findByFollowerIdAndFollowingId(followerId: UUID, followingId: UUID): UserFollow?
    fun existsByFollowerIdAndFollowingId(followerId: UUID, followingId: UUID): Boolean
    fun deleteByFollowerIdAndFollowingId(followerId: UUID, followingId: UUID)
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :userId AND uf.status = 'ACCEPTED'")
    fun findFollowersByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<UserFollow>
    @Query("SELECT uf FROM UserFollow uf WHERE uf.follower.id = :userId AND uf.status = 'ACCEPTED'")
    fun findFollowingByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<UserFollow>
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.following.id = :userId AND uf.status = 'ACCEPTED'")
    fun countFollowersByUserId(@Param("userId") userId: UUID): Int
    @Query("SELECT COUNT(uf) FROM UserFollow uf WHERE uf.follower.id = :userId AND uf.status = 'ACCEPTED'")
    fun countFollowingByUserId(@Param("userId") userId: UUID): Int
    @Query("SELECT uf FROM UserFollow uf WHERE uf.following.id = :userId AND uf.status = 'PENDING'")
    fun findPendingFollowRequests(@Param("userId") userId: UUID, pageable: Pageable): Page<UserFollow>
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration Request Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface CollaborationRequestRepository : JpaRepository<CollaborationRequest, UUID> {
    @Query("SELECT cr FROM CollaborationRequest cr WHERE cr.receiver.id = :userId ORDER BY cr.createdAt DESC")
    fun findReceivedByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<CollaborationRequest>
    @Query("SELECT cr FROM CollaborationRequest cr WHERE cr.sender.id = :userId ORDER BY cr.createdAt DESC")
    fun findSentByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<CollaborationRequest>
    @Query("SELECT cr FROM CollaborationRequest cr WHERE cr.receiver.id = :userId AND cr.status = 'PENDING' ORDER BY cr.createdAt DESC")
    fun findPendingByReceiverId(@Param("userId") userId: UUID, pageable: Pageable): Page<CollaborationRequest>
    @Query("SELECT COUNT(cr) FROM CollaborationRequest cr WHERE cr.receiver.id = :userId AND cr.status = 'PENDING'")
    fun countPendingByReceiverId(@Param("userId") userId: UUID): Int
    fun existsBySenderIdAndReceiverIdAndStatus(senderId: UUID, receiverId: UUID, status: CollaborationStatus): Boolean
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    fun findByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Notification>
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    fun countUnreadByUserId(@Param("userId") userId: UUID): Int
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    fun findUnreadByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Notification>
    fun findByUserIdAndRelatedEntityId(userId: UUID, relatedEntityId: UUID): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.type NOT IN (com.iliyadev.springboot.models.NotificationType.COLLABORATION_REQUEST, com.iliyadev.springboot.models.NotificationType.COLLABORATION_ACCEPTED, com.iliyadev.springboot.models.NotificationType.COLLABORATION_REJECTED, com.iliyadev.springboot.models.NotificationType.ADMIN_INVITE, com.iliyadev.springboot.models.NotificationType.TEACHER_INVITE) ORDER BY n.createdAt DESC")
    fun findMainNotificationsByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Notification>

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false AND n.type NOT IN (com.iliyadev.springboot.models.NotificationType.COLLABORATION_REQUEST, com.iliyadev.springboot.models.NotificationType.COLLABORATION_ACCEPTED, com.iliyadev.springboot.models.NotificationType.COLLABORATION_REJECTED, com.iliyadev.springboot.models.NotificationType.ADMIN_INVITE, com.iliyadev.springboot.models.NotificationType.TEACHER_INVITE)")
    fun countUnreadMainNotificationsByUserId(@Param("userId") userId: UUID): Int

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.type IN (com.iliyadev.springboot.models.NotificationType.COLLABORATION_REQUEST, com.iliyadev.springboot.models.NotificationType.COLLABORATION_ACCEPTED, com.iliyadev.springboot.models.NotificationType.COLLABORATION_REJECTED, com.iliyadev.springboot.models.NotificationType.ADMIN_INVITE, com.iliyadev.springboot.models.NotificationType.TEACHER_INVITE) ORDER BY n.createdAt DESC")
    fun findMosbatElmNotificationsByUserId(@Param("userId") userId: UUID, pageable: Pageable): Page<Notification>
}
