package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 Auth Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/send-otp")
    fun sendOtp(@RequestBody request: SendOtpRequest): ResponseEntity<SendOtpResponse> {
        val response = authService.sendOtp(request.phoneNumber)
        return ResponseEntity.ok(response)
    }
    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody request: VerifyOtpRequest): ResponseEntity<AuthResponse> {
        val response = authService.verifyOtp(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }
    @PostMapping("/refresh-token")
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        val response = authService.refreshToken(request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.badRequest().body(response)
        }
    }
    @PostMapping("/logout")
    fun logout(@RequestAttribute("userId") userId: UUID): ResponseEntity<ApiResponse<Unit>> {
        val success = authService.logout(userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "خروج موفق"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در خروج"))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val fileUploadService: FileUploadService
) {
    @GetMapping("/me")
    fun getCurrentUser(@RequestAttribute("userId") userId: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUserById(userId)
        return if (user != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", user.toDto()))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/me")
    fun updateCurrentUser(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val user = userService.updateUser(userId, request)
            if (user != null) {
                ResponseEntity.ok(ApiResponse(true, "پروفایل بروزرسانی شد", user.toDto()))
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "خطا"))
        }
    }

    @PutMapping("/me/privacy")
    fun updatePrivacy(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: UpdatePrivacyRequest
    ): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.updatePrivacy(userId, request)
        return if (user != null) {
            ResponseEntity.ok(ApiResponse(true, "تنظیمات حریم خصوصی بروزرسانی شد", user.toDto()))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/avatar")
    fun uploadAvatar(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam("file") file: org.springframework.web.multipart.MultipartFile
    ): ResponseEntity<ApiResponse<UserDto>> {
        val avatarUrl = fileUploadService.uploadFile(
            file.bytes,
            file.originalFilename ?: "avatar.jpg",
            file.contentType ?: "image/jpeg"
        )
        val user = userService.updateAvatar(userId, avatarUrl)
        return if (user != null) {
            ResponseEntity.ok(ApiResponse(true, "آواتار بروزرسانی شد", user.toDto()))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @GetMapping("/{id}")
    fun getUserById(
        @RequestAttribute("userId") viewerId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUserById(id)
        if (user == null) {
            return ResponseEntity.notFound().build()
        }
        // If viewing own profile, return full info
        if (viewerId == id) {
            return ResponseEntity.ok(ApiResponse(true, "موفق", user.toDto()))
        }
        // Check if viewer is a contact of the target user (simplified: check if they have a chat)
        val isContact = userService.areUsersContacts(viewerId, id)
        println("PRIVACY_LOG: getUserById - Viewer=$viewerId, Target=$id, areUsersContacts=$isContact")
        return ResponseEntity.ok(ApiResponse(true, "موفق", user.toRestrictedDto(isContact)))
    }
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<UserSearchResult> {
        val result = userService.searchUsers(query, page, size)
        return ResponseEntity.ok(result)
    }
    @PostMapping("/contacts")
    fun matchContacts(@RequestBody phoneNumbers: List<String>): ResponseEntity<UserSearchResult> {
        val result = userService.matchContacts(phoneNumbers)
        return ResponseEntity.ok(result)
    }
    

    
    @GetMapping("/me/privacy")
    fun getPrivacy(@RequestAttribute("userId") userId: UUID): ResponseEntity<ApiResponse<UserDto>> {
        val user = userService.getUserById(userId)
        return if (user != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", user.toDto()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/count")
    fun getTotalUserCount(): ResponseEntity<ApiResponse<Long>> {
        val count = userService.getTotalUserCount()
        return ResponseEntity.ok(ApiResponse(true, "موفق", count))
    }
    @PutMapping("/me/username")
    fun setUsername(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: SetUsernameRequest
    ): ResponseEntity<ApiResponse<UserDto>> {
        return try {
            val user = userService.setUsername(userId, request.username)
            if (user != null) {
                ResponseEntity.ok(ApiResponse(true, "نام کاربری تنظیم شد", user.toDto()))
            } else {
                ResponseEntity.badRequest().body(ApiResponse(false, "کاربر یافت نشد"))
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse(false, e.message ?: "خطا"))
        }
    }
    @GetMapping("/username/check")
    fun checkUsernameAvailability(
        @RequestParam username: String
    ): ResponseEntity<UsernameAvailabilityResponse> {
        val isAvailable = !userService.isUsernameTaken(username)
        return ResponseEntity.ok(UsernameAvailabilityResponse(username, isAvailable))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api/chats")
class ChatController(
    private val chatService: ChatService
) {
    @GetMapping
    fun getChats(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ChatListResponse> {
        val response = chatService.getChatsForUser(userId, page, size)
        return ResponseEntity.ok(response)
    }
    @PostMapping
    fun createChat(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: CreateChatRequest
    ): ResponseEntity<ApiResponse<ChatDto>> {
        val chat = chatService.createPrivateChat(userId, request.participantId)
        return if (chat != null) {
            ResponseEntity.ok(ApiResponse(true, "چت ایجاد شد", chat))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ایجاد چت"))
        }
    }
    @GetMapping("/{id}")
    fun getChatById(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ChatDto>> {
        val chat = chatService.getChatById(id, userId)
        return if (chat != null) {
            ResponseEntity.ok(ApiResponse(true, "موفق", chat))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/{id}/pin")
    fun pinChat(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<ChatDto>> {
        val chat = chatService.updateChatSettings(id, userId, isPinned = pinned, isMuted = null, isArchived = null)
        return if (chat != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "پین شد" else "از پین برداشته شد", chat))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/{id}/mute")
    fun muteChat(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam muted: Boolean
    ): ResponseEntity<ApiResponse<ChatDto>> {
        val chat = chatService.updateChatSettings(id, userId, isPinned = null, isMuted = muted, isArchived = null)
        return if (chat != null) {
            ResponseEntity.ok(ApiResponse(true, if (muted) "بی‌صدا شد" else "صدا فعال شد", chat))
        } else {
            ResponseEntity.notFound().build()
        }
    }
    @PutMapping("/{id}/archive")
    fun archiveChat(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam archived: Boolean
    ): ResponseEntity<ApiResponse<ChatDto>> {
        val chat = chatService.updateChatSettings(id, userId, isPinned = null, isMuted = null, isArchived = archived)
        return if (chat != null) {
            ResponseEntity.ok(ApiResponse(true, if (archived) "آرشیو شد" else "از آرشیو برداشته شد", chat))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/shared-content")
    fun getSharedContent(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam targetId: UUID,
        @RequestParam scope: String,
        @RequestParam type: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<ApiResponse<List<MessageDto>>> {
        val response = chatService.getSharedContent(userId, targetId, scope, type, page, size)
        return ResponseEntity.ok(ApiResponse(true, "موفق", response.messages))
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Controller
// ═══════════════════════════════════════════════════════════════════════════════

@RestController
@RequestMapping("/api")
class MessageController(
    private val messageService: MessageService
) {
    @GetMapping("/chats/{chatId}/messages")
    fun getMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<MessageListResponse> {
        return try {
            val response = messageService.getMessagesForChat(chatId, userId, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    @GetMapping("/chats/{chatId}/messages/search")
    fun searchMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestParam query: String?,
        @RequestParam(required = false) types: List<MessageType>?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<MessageListResponse> {
        return try {
            val response = messageService.searchMessages(chatId, userId, query, types, page, size)
            ResponseEntity.ok(response)
        } catch (e: IllegalAccessException) {
            ResponseEntity.status(403).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
    @PostMapping("/chats/{chatId}/messages")
    fun sendMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val message = messageService.sendMessage(chatId, userId, request)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام ارسال شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ارسال پیام"))
        }
    }
    @PutMapping("/messages/{id}")
    fun editMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: EditMessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val message = messageService.editMessage(id, userId, request)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام ویرایش شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در ویرایش پیام"))
        }
    }
    @DeleteMapping("/messages/{id}")
    fun deleteMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam(defaultValue = "true") deleteForEveryone: Boolean
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = messageService.deleteMessage(id, userId, deleteForEveryone)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "پیام حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در حذف پیام"))
        }
    }
    @PostMapping("/messages/{id}/read")
    fun markAsRead(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = messageService.markAsRead(id, userId)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "خوانده شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا"))
        }
    }
    @PostMapping("/messages/{id}/reactions")
    fun reactToMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: ReactionRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = messageService.reactToMessage(id, userId, request.reaction)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, if (request.reaction != null) "واکنش ثبت شد" else "واکنش حذف شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    // ═══ PIN MANAGEMENT ═══
    @PutMapping("/messages/{id}/pin")
    fun pinMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID,
        @RequestParam pinned: Boolean
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val message = messageService.pinMessage(id, userId, pinned)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, if (pinned) "پیام سنجاق شد" else "سنجاق برداشته شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا یا دسترسی ندارید"))
        }
    }
    @GetMapping("/chats/{chatId}/messages/pinned")
    fun getPinnedMessages(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable chatId: UUID
    ): ResponseEntity<ApiResponse<List<MessageDto>>> {
        val messages = messageService.getPinnedMessages(chatId, userId)
        return ResponseEntity.ok(ApiResponse(true, "موفق", messages))
    }
    // ═══ FORWARD MESSAGES ═══
    @PostMapping("/messages/forward")
    fun forwardMessages(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: ForwardMessageRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        val success = messageService.forwardMessages(userId, request)
        return if (success) {
            ResponseEntity.ok(ApiResponse(true, "پیام فروارد شد"))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در فروارد"))
        }
    }
    // ═══ SCHEDULE MESSAGES ═══
    @PostMapping("/chats/{chatId}/messages/schedule")
    fun scheduleMessage(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable chatId: UUID,
        @RequestBody request: ScheduleMessageRequest
    ): ResponseEntity<ApiResponse<MessageDto>> {
        val message = messageService.scheduleMessage(chatId, userId, request)
        return if (message != null) {
            ResponseEntity.ok(ApiResponse(true, "پیام زمان‌بندی شد", message))
        } else {
            ResponseEntity.badRequest().body(ApiResponse(false, "خطا در زمان‌بندی"))
        }
    }
}
