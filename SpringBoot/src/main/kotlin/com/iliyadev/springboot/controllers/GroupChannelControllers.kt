package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 👥 Group Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService,
    private val fileUploadService: FileUploadService
) {
    @GetMapping
    fun getGroups(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<GroupListResponse> {
        val response = groupService.getGroupsForUser(userId, page, size)
        return ResponseEntity.ok(response)
    }
    @PostMapping
    fun createGroup(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreateGroupRequest
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.createGroup(userId, request)
        return if (group != null) {
            ResponseEntity.ok(ApiResponse(true, "گروه ایجاد شد", group))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ایجاد گروه"))
        }
    }
    @GetMapping("/{id}")
    fun getGroupById(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.getGroupById(id, userId)
        return if (group != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", group))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/{id}")
    fun updateGroup(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateGroupRequest
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.updateGroup(id, userId, request)
        return if (group != null) {
            ResponseEntity.ok(ApiResponse(true, "گروه بروزرسانی شد", group))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @DeleteMapping("/{id}")
    fun deleteGroup(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = groupService.deleteGroup(id, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "گروه حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/{id}/members")
    fun getGroupMembers(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<GroupMemberDto>>> {
        val members = groupService.getGroupMembers(id, userId)
        return if (members != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", members))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/{id}/members")
    fun addMembers(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: AddGroupMembersRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = groupService.addMembers(id, userId, request.memberIds)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "اعضا اضافه شدند"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @DeleteMapping("/{id}/members/{memberId}")
    fun removeMember(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable memberId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = groupService.removeMember(id, userId, memberId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "عضو حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/members/{memberId}/role")
    fun changeRole(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable memberId: UUID,
        @RequestBody request: ChangeRoleRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = groupService.changeRole(id, userId, memberId, request)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "نقش تغییر یافت"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/{id}/messages")
    fun getGroupMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<GroupMessageListResponse> {
        return try {
            val response = groupService.getGroupMessages(id, userId, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        }
    }
    @GetMapping("/{id}/messages/search")
    fun searchGroupMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam query: String?,
        @RequestParam(required = false) types: List<MessageType>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<GroupMessageListResponse> {
        return try {
            val response = groupService.searchGroupMessages(id, userId, query, types, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        }
    }
    @PostMapping("/{id}/messages")
    fun sendGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: SendGroupMessageRequest
    ): ResponseEntity<ApiResponse<GroupMessageDto>> {
        val message = groupService.sendGroupMessage(id, userId, request)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام ارسال شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ارسال پیام یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/messages/{messageId}/reactions")
    fun reactToGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable messageId: UUID,
        @RequestBody request: ReactionRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        // id is groupId, but we don't strictly need it if messageId is unique, but let's conform to path
        val success = groupService.reactToGroupMessage(messageId, userId, request.reaction)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, if (request.reaction != null) "واکنش ثبت شد" else "واکنش حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @PutMapping("/{id}/messages/{messageId}")
    fun editGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable messageId: UUID,
        @RequestBody request: EditMessageRequest
    ): ResponseEntity<ApiResponse<GroupMessageDto>> {
        val message = groupService.editGroupMessage(id, userId, messageId, request.content)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام ویرایش شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @DeleteMapping("/{id}/messages/{messageId}")
    fun deleteGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable messageId: UUID,
        @RequestParam(defaultValue = "true") deleteForEveryone: Boolean
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = groupService.deleteGroupMessage(id, userId, messageId, deleteForEveryone)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "پیام حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/settings")
    fun updateGroupSettings(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateGroupSettingsRequest
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.updateGroupSettings(id, userId, request)
        return if (group != null) {
            ResponseEntity.ok(ApiResponse(true, "تنظیمات بروزرسانی شد", group))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/invite-link/toggle")
    fun toggleInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam enabled: Boolean
    ): ResponseEntity<ApiResponse<InviteLinkResponse>> {
        val result = groupService.toggleInviteLink(id, userId, enabled)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, "لینک دعوت بروزرسانی شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/invite-link/regenerate")
    fun regenerateInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<InviteLinkResponse>> {
        val result = groupService.regenerateInviteLink(id, userId)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, "لینک جدید ایجاد شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/join/{inviteCode}")
    fun joinByInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable inviteCode: String
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val group = groupService.joinByInviteLink(inviteCode, userId)
        return if (group != null) {
            ResponseEntity.ok(ApiResponse(true, "به گروه پیوستید", group))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "لینک نامعتبر یا غیرفعال است"))
        }
    }
    // ═══ PIN GROUP MESSAGE ═══
    @PutMapping("/{id}/messages/{messageId}/pin")
    fun pinGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable messageId: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<GroupMessageDto>> {
        val message = groupService.pinGroupMessage(id, messageId, userId, pinned)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "پیام سنجاق شد" else "سنجاق برداشته شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/{id}/messages/pinned")
    fun getPinnedGroupMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<GroupMessageDto>>> {
        val messages = groupService.getPinnedGroupMessages(id, userId)
        return ResponseEntity.ok(ApiResponse(true, "موفق", messages))
    }
    // ═══ SCHEDULE GROUP MESSAGE ═══
    @PostMapping("/{id}/messages/schedule")
    fun scheduleGroupMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: ScheduleMessageRequest
    ): ResponseEntity<ApiResponse<GroupMessageDto>> {
        val message = groupService.scheduleGroupMessage(id, userId, request)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام زمان‌بندی شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در زمان‌بندی"))
        }
    }
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔔 Mute/Pin/Archive Settings
    // ═══════════════════════════════════════════════════════════════════════════════
    @PutMapping("/{id}/mute")
    fun muteGroup(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam muted: Boolean
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val result = groupService.updateMemberSettings(id, userId, isMuted = muted, isPinned = null, isArchived = null)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (muted) "بی‌صدا شد" else "صدا فعال شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/pin")
    fun pinGroup(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val result = groupService.updateMemberSettings(id, userId, isMuted = null, isPinned = pinned, isArchived = null)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "سنجاق شد" else "سنجاق برداشته شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/archive")
    fun archiveGroup(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam archived: Boolean
    ): ResponseEntity<ApiResponse<GroupDto>> {
        val result = groupService.updateMemberSettings(id, userId, isMuted = null, isPinned = null, isArchived = archived)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (archived) "آرشیو شد" else "از آرشیو خارج شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/channels")
class ChannelController(
    private val channelService: ChannelService,
    private val fileUploadService: FileUploadService
) {
    @GetMapping
    fun getChannels(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ChannelListResponse> {
        val response = channelService.getChannelsForUser(userId, page, size)
        return ResponseEntity.ok(response)
    }
    @PostMapping
    fun createChannel(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreateChannelRequest
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val channel = channelService.createChannel(userId, request)
        return if (channel != null) {
            ResponseEntity.ok(ApiResponse(true, "کانال ایجاد شد", channel))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ایجاد کانال"))
        }
    }
    @GetMapping("/{id}")
    fun getChannelById(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val channel = channelService.getChannelById(id, userId)
        return if (channel != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", channel))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/{id}")
    fun updateChannel(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateChannelRequest
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val channel = channelService.updateChannel(id, userId, request)
        return if (channel != null) {
            ResponseEntity.ok(ApiResponse(true, "کانال بروزرسانی شد", channel))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @DeleteMapping("/{id}")
    fun deleteChannel(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.deleteChannel(id, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "کانال حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/subscribe")
    fun subscribe(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.subscribe(id, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "عضو شدید"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در عضویت"))
        }
    }
    @DeleteMapping("/{id}/subscribe")
    fun unsubscribe(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.unsubscribe(id, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "لغو عضویت شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در لغو عضویت"))
        }
    }
    @GetMapping("/{id}/posts")
    fun getPosts(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<PostListResponse> {
        return try {
            val response = channelService.getPosts(id, userId, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    @GetMapping("/{id}/posts/search")
    fun searchChannelPosts(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam query: String?,
        @RequestParam(required = false) types: List<MessageType>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<PostListResponse> {
        return try {
            val response = channelService.searchChannelPosts(id, userId, query, types, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/{id}/posts")
    fun createPost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<ApiResponse<ChannelPostDto>> {
        val post = channelService.createPost(id, userId, request)
        return if (post != null) {
            ResponseEntity.ok(ApiResponse(true, "پست ایجاد شد", post))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @PutMapping("/{id}/posts/{postId}")
    fun editPost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<ApiResponse<ChannelPostDto>> {
        val post = channelService.editPost(id, userId, postId, request.content)
        return if (post != null) {
            ResponseEntity.ok(ApiResponse(true, "پست ویرایش شد", post))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @DeleteMapping("/{id}/posts/{postId}")
    fun deletePost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestParam(defaultValue = "true") deleteForEveryone: Boolean
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.deletePost(id, userId, postId, deleteForEveryone)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "پست حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @PostMapping("/{id}/posts/{postId}/reactions")
    fun reactToPost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestBody request: ReactionRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.reactToPost(id, userId, postId, request.reaction)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, if (request.reaction != null) "واکنش ثبت شد" else "واکنش حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }



    @PostMapping("/{id}/posts/{postId}/comments")
    fun addComment(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestBody request: CreateCommentRequest
    ): ResponseEntity<ApiResponse<ChannelPostCommentDto>> {
        val comment = channelService.addComment(id, userId, postId, request.content)
        return if (comment != null) {
            ResponseEntity.ok(ApiResponse(true, "نظر ثبت شد", comment))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ثبت نظر"))
        }
    }

    @GetMapping("/{id}/posts/{postId}/comments")
    fun getComments(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<CommentListResponse> {
        return try {
            val response = channelService.getComments(id, userId, postId, page, size)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
             ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/search")
    fun searchChannels(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ChannelListResponse> {
        val result = channelService.searchPublicChannels(query, page, size)
        return ResponseEntity.ok(result)
    }
    @PostMapping("/{id}/admins")
    fun addAdmin(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: ChannelAdminRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.addAdmin(id, userId, request.userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "ادمین اضافه شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @DeleteMapping("/{id}/admins/{targetId}")
    fun removeAdmin(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable targetId: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.removeAdmin(id, userId, targetId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "ادمین حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/{id}/subscribers")
    fun getSubscribers(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<ChannelSubscriberDto>>> {
        val subscribers = channelService.getChannelSubscribers(id, userId)
        return if (subscribers != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", subscribers))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }

    @PostMapping("/{id}/members")
    fun addMembers(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: AddChannelMembersRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = channelService.addMembers(id, userId, request.memberIds)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "اعضا اضافه شدند"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/invite-link/toggle")
    fun toggleInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam enabled: Boolean
    ): ResponseEntity<ApiResponse<InviteLinkResponse>> {
        val result = channelService.toggleInviteLink(id, userId, enabled)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, "لینک دعوت بروزرسانی شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/{id}/invite-link/regenerate")
    fun regenerateInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<InviteLinkResponse>> {
        val result = channelService.regenerateInviteLink(id, userId)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, "لینک جدید ایجاد شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PostMapping("/join/{inviteCode}")
    fun joinByInviteLink(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable inviteCode: String
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val channel = channelService.joinByInviteLink(inviteCode, userId)
        return if (channel != null) {
            ResponseEntity.ok(ApiResponse(true, "به کانال پیوستید", channel))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "لینک نامعتبر یا غیرفعال است"))
        }
    }
    // ═══ PIN CHANNEL POST ═══
    @PutMapping("/{id}/posts/{postId}/pin")
    fun pinChannelPost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @PathVariable postId: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<ChannelPostDto>> {
        val post = channelService.pinChannelPost(id, postId, userId, pinned)
        return if (post != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "پست سنجاق شد" else "سنجاق برداشته شد", post))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/{id}/posts/pinned")
    fun getPinnedChannelPosts(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<List<ChannelPostDto>>> {
        val posts = channelService.getPinnedChannelPosts(id, userId)
        return ResponseEntity.ok(ApiResponse(true, "موفق", posts))
    }
    // ═══ SCHEDULE CHANNEL POST ═══
    @PostMapping("/{id}/posts/schedule")
    fun scheduleChannelPost(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: ScheduleMessageRequest
    ): ResponseEntity<ApiResponse<ChannelPostDto>> {
        val post = channelService.scheduleChannelPost(id, userId, request)
        return if (post != null) {
            ResponseEntity.ok(ApiResponse(true, "پست زمان‌بندی شد", post))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در زمان‌بندی"))
        }
    }
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔔 Mute/Pin/Archive Settings
    // ═══════════════════════════════════════════════════════════════════════════════
    @PutMapping("/{id}/mute")
    fun muteChannel(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam muted: Boolean
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val result = channelService.updateSubscriberSettings(id, userId, isMuted = muted, isPinned = null, isArchived = null)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (muted) "بی‌صدا شد" else "صدا فعال شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/pin")
    fun pinChannel(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val result = channelService.updateSubscriberSettings(id, userId, isMuted = null, isPinned = pinned, isArchived = null)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "سنجاق شد" else "سنجاق برداشته شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @PutMapping("/{id}/archive")
    fun archiveChannel(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam archived: Boolean
    ): ResponseEntity<ApiResponse<ChannelDto>> {
        val result = channelService.updateSubscriberSettings(id, userId, isMuted = null, isPinned = null, isArchived = archived)
        return if (result != null) {
            ResponseEntity.ok(ApiResponse(true, if (archived) "آرشیو شد" else "از آرشیو خارج شد", result))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📁 File Upload Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/files")
class FileUploadController(
    private val fileUploadService: FileUploadService,
    private val thumbnailService: ThumbnailService
) {
    @PostMapping("/upload")
    fun uploadFile(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<String>> {
        val url = fileUploadService.uploadFile(
            file.bytes,
            file.originalFilename ?: "file",
            file.contentType ?: "application/octet-stream"
        )
        return ResponseEntity.ok(ApiResponse(true, "فایل آپلود شد", url))
    }

    /**
     * Serve a resized thumbnail of an uploaded image.
     * Falls back to 404 if file is not found or not a valid image.
     */
    @GetMapping("/thumbnail/{filename}")
    fun getThumbnail(
        @PathVariable filename: String,
        @RequestParam(defaultValue = "200") w: Int,
        @RequestParam(defaultValue = "60") q: Int
    ): ResponseEntity<ByteArray> {
        val thumbBytes = thumbnailService.getThumbnail(filename, w, q)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .header("Content-Type", "image/jpeg")
            .header("Cache-Control", "public, max-age=31536000, immutable")
            .body(thumbBytes)
    }
}
