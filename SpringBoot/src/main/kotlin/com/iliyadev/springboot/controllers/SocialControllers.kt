package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 📋 Profile Details Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api")
class ProfileDetailsController(
    private val profileDetailsService: ProfileDetailsService
) {
    @GetMapping("/users/{userId}/profile-details")
    fun getProfileDetails(@PathVariable userId: UUID): ResponseEntity<ApiResponse<ProfileDetailsDto>> {
        val details = profileDetailsService.getProfileDetails(userId)
        return if (details != null) {
            ResponseEntity.ok(ApiResponse(true, "Profile details retrieved", details))
        } else {
            ResponseEntity.ok(ApiResponse(true, "No profile details found", null))
        }
    }
    @PutMapping("/users/me/profile-details")
    fun updateProfileDetails(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: UpdateProfileDetailsRequest
    ): ResponseEntity<ApiResponse<ProfileDetailsDto>> {
        val details = profileDetailsService.updateProfileDetails(userId, request)
        return ResponseEntity.ok(ApiResponse(true, "Profile details updated", details))
    }
    @GetMapping("/users/me/profile-details")
    fun getMyProfileDetails(
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<ApiResponse<ProfileDetailsDto>> {
        val details = profileDetailsService.getProfileDetails(userId)
        return if (details != null) {
            ResponseEntity.ok(ApiResponse(true, "Profile details retrieved", details))
        } else {
            ResponseEntity.ok(ApiResponse(true, "No profile details found", null))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Follow Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/users")
class FollowController(
    private val followService: FollowService
) {
    @PostMapping("/{targetUserId}/follow")
    fun followUser(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable targetUserId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val success = followService.followUser(userId, targetUserId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "User followed successfully", true))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "Could not follow user", false))
        }
    }
    @DeleteMapping("/{targetUserId}/follow")
    fun unfollowUser(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable targetUserId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val success = followService.unfollowUser(userId, targetUserId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "User unfollowed successfully", true))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "Could not unfollow user", false))
        }
    }
    @GetMapping("/{targetUserId}/followers")
    fun getFollowers(
        @PathVariable targetUserId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<FollowListResponse> {
        return ResponseEntity.ok(followService.getFollowers(targetUserId, page, size))
    }
    @GetMapping("/{targetUserId}/following")
    fun getFollowing(
        @PathVariable targetUserId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<FollowListResponse> {
        return ResponseEntity.ok(followService.getFollowing(targetUserId, page, size))
    }
    @GetMapping("/{targetUserId}/follow-counts")
    fun getFollowCounts(@PathVariable targetUserId: UUID): ResponseEntity<FollowCountsDto> {
        return ResponseEntity.ok(followService.getFollowCounts(targetUserId))
    }
    @GetMapping("/{targetUserId}/is-following")
    fun isFollowing(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable targetUserId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val isFollowing = followService.isFollowing(userId, targetUserId)
        return ResponseEntity.ok(ApiResponse(true, "Check completed", isFollowing))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🤝 Collaboration Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/collaboration")
class CollaborationController(
    private val collaborationService: CollaborationService
) {
    @PostMapping("/request")
    fun sendRequest(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: SendCollaborationRequest
    ): ResponseEntity<ApiResponse<CollaborationRequestDto>> {
        return try {
            val result = collaborationService.sendRequest(userId, request)
            ResponseEntity.ok(ApiResponse(true, "Request sent successfully", result))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "Error", null))
        }
    }
    @PostMapping("/{requestId}/accept")
    fun acceptRequest(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable requestId: UUID
    ): ResponseEntity<ApiResponse<CollaborationRequestDto>> {
        return try {
            val result = collaborationService.acceptRequest(requestId, userId)
            ResponseEntity.ok(ApiResponse(true, "Request accepted", result))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "Error", null))
        }
    }
    @PostMapping("/{requestId}/reject")
    fun rejectRequest(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable requestId: UUID
    ): ResponseEntity<ApiResponse<CollaborationRequestDto>> {
        return try {
            val result = collaborationService.rejectRequest(requestId, userId)
            ResponseEntity.ok(ApiResponse(true, "Request rejected", result))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "Error", null))
        }
    }
    @GetMapping("/received")
    fun getReceivedRequests(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CollaborationListResponse> {
        return ResponseEntity.ok(collaborationService.getReceivedRequests(userId, page, size))
    }
    @GetMapping("/sent")
    fun getSentRequests(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<CollaborationListResponse> {
        return ResponseEntity.ok(collaborationService.getSentRequests(userId, page, size))
    }
    @GetMapping("/pending-count")
    fun getPendingCount(
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<ApiResponse<Int>> {
        val count = collaborationService.getPendingCount(userId)
        return ResponseEntity.ok(ApiResponse(true, "Count retrieved", count))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔔 Notification Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    @GetMapping
    fun getNotifications(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<NotificationListResponse> {
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, size))
    }
    @GetMapping("/mosbat-elm")
    fun getMosbatElmNotifications(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<NotificationListResponse> {
        return ResponseEntity.ok(notificationService.getMosbatElmNotifications(userId, page, size))
    }
    @PostMapping("/{notificationId}/read")
    fun markAsRead(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val success = notificationService.markAsRead(notificationId, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "Notification marked as read", true))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "Could not mark as read", false))
        }
    }
    @PostMapping("/read-all")
    fun markAllAsRead(
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<ApiResponse<Int>> {
        val count = notificationService.markAllAsRead(userId)
        return ResponseEntity.ok(ApiResponse(true, "Marked $count notifications as read", count))
    }
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<UnreadCountResponse> {
        return ResponseEntity.ok(UnreadCountResponse(notificationService.getUnreadCount(userId)))
    }

    @PostMapping("/{notificationId}/accept-invite")
    fun acceptInvite(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val success = notificationService.acceptInvite(notificationId, userId)
        return ResponseEntity.ok(ApiResponse(success, if (success) "دعوت با موفقیت پذیرفته شد" else "خطا در پذیرش دعوت", success))
    }

    @PostMapping("/{notificationId}/reject-invite")
    fun rejectInvite(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable notificationId: UUID
    ): ResponseEntity<ApiResponse<Boolean>> {
        val success = notificationService.rejectInvite(notificationId, userId)
        return ResponseEntity.ok(ApiResponse(success, if (success) "دعوت رد شد" else "خطا در رد دعوت", success))
    }
}
