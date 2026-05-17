package com.iliyadev.springboot.services

import com.iliyadev.springboot.config.JwtTokenUtils
import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import com.iliyadev.springboot.websocket.WebSocketMessageHandler
import com.iliyadev.springboot.websocket.WsChatEvent
import com.iliyadev.springboot.websocket.WsChatParticipant
import com.iliyadev.springboot.websocket.WsMessage
import com.iliyadev.springboot.websocket.WsReactionEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import com.iliyadev.springboot.util.MessageUtils

// ═══════════════════════════════════════════════════════════════════════════════
// 🔐 Auth Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val otpCodeRepository: OtpCodeRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userSessionRepository: UserSessionRepository,
    private val jwtTokenUtils: JwtTokenUtils,
    private val najvaSmsService: NajvaSmsService,
    @Value("\${najva.sms.enabled:false}") private val isSmsEnabled: Boolean
) {
    companion object {
        private const val OTP_EXPIRY_MINUTES = 5L
        private const val REFRESH_TOKEN_EXPIRY_DAYS = 30L
    }
    @Transactional
    fun sendOtp(phoneNumber: String): SendOtpResponse {
        val normalizedPhone = normalizePhoneNumber(phoneNumber)
        val code = generateOtpCode()
        val expiresAt = Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60)
        val otpCode = OtpCode().apply {
            this.phoneNumber = normalizedPhone
            this.code = code
            this.expiresAt = expiresAt
            this.createdAt = Instant.now()
        }
        otpCodeRepository.save(otpCode)
        
        // SERVER MODE: Send via Najva SMS
        val isSent = najvaSmsService.sendOtpViaSms(normalizedPhone, code)
        
        if (!isSent) {
            return SendOtpResponse(
                success = false,
                message = "خطا در ارسال پیامک. لطفاً دقایقی دیگر تلاش کنید.",
                expiresInSeconds = 0
            )
        }

        return SendOtpResponse(
            success = true,
            message = "کد تأیید ارسال شد",
            expiresInSeconds = (OTP_EXPIRY_MINUTES * 60).toInt()
        )
    }
    @Transactional
    fun verifyOtp(request: VerifyOtpRequest): AuthResponse {
        val normalizedPhone = normalizePhoneNumber(request.phoneNumber)
        val otpCode = if (!isSmsEnabled && request.code == "123456") {
            null
        } else {
            val dbOtp = otpCodeRepository.findByPhoneNumberAndCodeAndIsUsedFalse(
                normalizedPhone, request.code
            ) ?: return AuthResponse(
                success = false,
                message = "کد تأیید نامعتبر است"
            )
            if (dbOtp.expiresAt.isBefore(Instant.now())) {
                return AuthResponse(
                    success = false,
                    message = "کد تأیید منقضی شده است"
                )
            }
            dbOtp
        }
        if (otpCode != null) {
            otpCode.isUsed = true
            otpCodeRepository.save(otpCode)
        }
        var user = userRepository.findByPhoneNumber(normalizedPhone)
        val isNewUser = user == null
        if (user == null) {
            user = User().apply {
                this.phoneNumber = normalizedPhone
                this.username = generateUsername(normalizedPhone)
                this.displayName = "کاربر جدید"
                this.createdAt = Instant.now()
            }
            userRepository.save(user)
        } else {
            user.isOnline = true
            user.lastSeen = Instant.now()
            userRepository.save(user)
        }
        // Create session
        val session = UserSession().apply {
            this.userId = user.id!!
            this.deviceName = request.deviceName ?: "Unknown Device"
            this.platform = request.platform ?: "Android"
            this.osVersion = request.osVersion ?: "Unknown"
            this.appVersion = request.appVersion ?: "1.0.0"
            this.lastActiveAt = Instant.now()
            this.isActive = true
        }
        val savedSession = userSessionRepository.save(session)

        val accessToken = jwtTokenUtils.generateToken(user.id.toString(), savedSession.id.toString())
        val refreshToken = createRefreshToken(user, savedSession.id)

        return AuthResponse(
            success = true,
            message = if (isNewUser) "ثبت‌نام با موفقیت انجام شد" else "ورود موفق",
            accessToken = accessToken,
            refreshToken = refreshToken.token,
            user = user.toDto(),
            isNewUser = isNewUser
        )
    }
    @Transactional
    fun refreshToken(request: RefreshTokenRequest): AuthResponse {
        val storedToken = refreshTokenRepository.findByTokenAndIsRevokedFalse(request.refreshToken)
            ?: return AuthResponse(success = false, message = "توکن نامعتبر است")
        if (storedToken.expiresAt.isBefore(Instant.now())) {
            storedToken.isRevoked = true
            refreshTokenRepository.save(storedToken)
            return AuthResponse(success = false, message = "توکن منقضی شده است")
        }
        val user = storedToken.user ?: return AuthResponse(success = false, message = "کاربر یافت نشد")
        storedToken.isRevoked = true
        refreshTokenRepository.save(storedToken)
        // Note: For refresh token, we don't necessarily have the sessionId here unless we store it with the refresh token.
        // For now, we generate a token without sessionId claim or let the filter handle it if it can.
        // Better: We should probably keep the sessionId in the refresh token too.
        val newAccessToken = jwtTokenUtils.generateToken(user.id.toString(), storedToken.sessionId?.toString())
        val newRefreshToken = createRefreshToken(user, storedToken.sessionId)
        return AuthResponse(
            success = true,
            message = "توکن بروزرسانی شد",
            accessToken = newAccessToken,
            refreshToken = newRefreshToken.token,
            user = user.toDto()
        )
    }
    @Transactional
    fun logout(userId: UUID): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        user.isOnline = false
        user.lastSeen = Instant.now()
        userRepository.save(user)
        val tokens = refreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
        tokens.forEach { token ->
            token.isRevoked = true
            refreshTokenRepository.save(token)
        }
        // Invalidate all active sessions for this user on logout
        val sessions = userSessionRepository.findByUserIdAndIsActiveTrue(userId)
        sessions.forEach { it.isActive = false; userSessionRepository.save(it) }
        
        return true
    }

    fun getActiveSessions(userId: UUID, currentSessionId: UUID? = null): List<DeviceSessionDto> {
        return userSessionRepository.findByUserIdAndIsActiveTrue(userId).map {
            DeviceSessionDto(
                id = it.id.toString(),
                deviceName = it.deviceName ?: "Unknown",
                platform = it.platform ?: "Android",
                osVersion = it.osVersion ?: "Unknown",
                appVersion = it.appVersion ?: "1.0.0",
                lastActiveIp = it.lastActiveIp ?: "0.0.0.0",
                lastActiveAt = it.lastActiveAt.toString(),
                isCurrent = it.id == currentSessionId
            )
        }
    }
    
    @Transactional
    fun updateSessionActivity(userId: UUID, sessionId: UUID) {
        val session = userSessionRepository.findByUserIdAndId(userId, sessionId)
        if (session != null && session.isActive) {
            session.lastActiveAt = Instant.now()
            userSessionRepository.save(session)
        }
    }

    @Transactional
    fun terminateSession(userId: UUID, sessionId: UUID): Boolean {
        val session = userSessionRepository.findByUserIdAndId(userId, sessionId) ?: return false
        session.isActive = false
        userSessionRepository.save(session)
        return true
    }

    @Transactional
    fun terminateAllOtherSessions(userId: UUID, currentSessionId: UUID?): Boolean {
        val sessions = if (currentSessionId != null) {
            userSessionRepository.findByUserIdAndIsActiveTrueAndIdNot(userId, currentSessionId)
        } else {
            userSessionRepository.findByUserIdAndIsActiveTrue(userId)
        }
        sessions.forEach { 
            it.isActive = false
            userSessionRepository.save(it)
        }
        return true
    }
    private fun generateOtpCode(): String = (100000..999999).random().toString()
    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
            .let { if (it.startsWith("98")) it.substring(2) else it }
            .let { if (it.startsWith("0")) it else "0$it" }
    }
    private fun generateUsername(phone: String): String {
        return "user_${phone.takeLast(4)}_${Random.nextInt(1000, 9999)}"
    }
    private fun createRefreshToken(user: User, sessionId: UUID? = null): RefreshToken {
        val token = RefreshToken().apply {
            this.user = user
            this.token = UUID.randomUUID().toString()
            this.sessionId = sessionId
            this.expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_DAYS * 24 * 60 * 60)
            this.createdAt = Instant.now()
        }
        return refreshTokenRepository.save(token)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 👤 User Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class UserService(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val specialFolderService: SpecialFolderService,
    private val channelService: ChannelService,
    private val channelRepository: com.iliyadev.springboot.repositories.ChannelRepository
) {
    fun getUserById(userId: UUID): User? = userRepository.findById(userId).orElse(null)
    fun getUserByPhoneNumber(phoneNumber: String): User? = userRepository.findByPhoneNumber(phoneNumber)
    @Transactional
    fun updateUser(userId: UUID, request: UpdateUserRequest): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        if (request.username != null && request.username != user.username) {
            if (userRepository.existsByUsername(request.username)) {
                throw IllegalArgumentException("این نام کاربری قبلاً استفاده شده است")
            }
            user.username = request.username
        }
        if (request.displayName != null) user.displayName = request.displayName
        if (request.firstName != null) user.firstName = request.firstName
        if (request.lastName != null) user.lastName = request.lastName
        if (request.nationalCode != null) {
            if (user.nationalCode != request.nationalCode && userRepository.existsByNationalCode(request.nationalCode)) {
                throw java.lang.IllegalArgumentException("این کد ملی قبلاً ثبت شده است")
            }
            user.nationalCode = request.nationalCode
        }
        if (request.educationalRole != null) {
            user.educationalRole = request.educationalRole
        }
        if (request.gradeLevel != null) user.gradeLevel = request.gradeLevel
        if (request.major != null) user.major = request.major
        if (request.faculty != null) user.faculty = request.faculty
        if (request.bio != null) user.bio = request.bio
        if (request.birthDate != null) user.birthDate = request.birthDate

        // Profile Enhancements - Persistent update
        var profileFieldsChanged = false
        if (request.university != null || request.fieldOfStudy != null || request.education != null || 
            request.skills != null || request.interests != null || request.workExperience != null || 
            request.achievements != null || request.isTeacher != null || request.teachingField != null ||
            request.teachingUniversity != null || request.province != null || request.city != null ||
            request.faculty != null || request.universities != null || request.fieldsOfStudy != null || request.isGraduated != null) {
            
            var details = user.profileDetails
            if (details == null) {
                details = UserProfileDetails(user = user)
                user.profileDetails = details
            }
            
            if (request.university != null) { details.university = request.university; profileFieldsChanged = true }
            if (request.fieldOfStudy != null) { details.fieldOfStudy = request.fieldOfStudy; profileFieldsChanged = true }
            if (request.universities != null) { details.universities = request.universities.toMutableSet(); profileFieldsChanged = true }
            if (request.fieldsOfStudy != null) { details.fieldsOfStudy = request.fieldsOfStudy.toMutableSet(); profileFieldsChanged = true }
            if (request.isGraduated != null) { details.isGraduated = request.isGraduated }
            if (request.education != null) { details.education = request.education; profileFieldsChanged = true }
            if (request.faculty != null) { details.faculty = request.faculty; profileFieldsChanged = true }
            if (request.skills != null) details.skills = request.skills
            if (request.interests != null) details.interests = request.interests
            if (request.workExperience != null) details.workExperience = request.workExperience
            if (request.achievements != null) details.achievements = request.achievements
            if (request.isTeacher != null) details.isTeacher = request.isTeacher
            if (request.teachingField != null) details.teachingField = request.teachingField
            if (request.teachingUniversity != null) details.teachingUniversity = request.teachingUniversity
            if (request.province != null) { details.province = request.province; profileFieldsChanged = true }
            if (request.city != null) { details.city = request.city; profileFieldsChanged = true }
            details.updatedAt = Instant.now()
        }

        // Feature 3: Bio channels
        if (request.bioChannelId1 != null) user.bioChannelId1 = request.bioChannelId1
        if (request.bioChannelId2 != null) user.bioChannelId2 = request.bioChannelId2

        val savedUser: User = userRepository.save(user)
        // Auto-subscribe to official channels/groups when profile fields change
        if (profileFieldsChanged) {
            specialFolderService.autoSubscribeUser(savedUser)
        }
        // Auto-create channel when user becomes a teacher
        val isTeacherRole = request.educationalRole == "TEACHER" || request.isTeacher == true
        if (isTeacherRole) {
            val existingChannels = channelRepository.findByOwnerId(savedUser.id!!)
            if (existingChannels.isEmpty()) {
                val channelRequest = CreateChannelRequest(
                    name = "کانال رسمی استاد ${savedUser.displayName}",
                    description = "کانال رسمی استاد ${savedUser.displayName} در پیام‌رسان کلاسور",
                    isPublic = true
                )
                val createdChannel = channelService.createChannel(savedUser.id!!, channelRequest)
                if (createdChannel != null) {
                    // Update classification and verified status
                    val channelEntity = channelRepository.findById(createdChannel.id).orElse(null)
                    if (channelEntity != null) {
                        channelEntity.classification = ChannelClassification.VERIFIED_TEACHER
                        channelEntity.isVerifiedTeacher = true
                        channelEntity.isOfficial = true
                        channelEntity.officialCategory = OfficialChannelCategory.MY_FIELD
                        channelEntity.displayMode = OfficialDisplayMode.TAB
                        channelRepository.save(channelEntity)
                    }
                    savedUser.officialChannelId = createdChannel.id
                    userRepository.save(savedUser)
                    println("📢 Auto-created teacher channel for user ${savedUser.id}: ${createdChannel.id}")
                }
            }
        }
        return savedUser
    }

    @Transactional
    fun updatePrivacy(userId: UUID, request: UpdatePrivacyRequest): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        request.profileVisibility?.let { user.profileVisibility = it }
        request.onlineVisibility?.let { user.onlineVisibility = it }
        request.phoneVisibility?.let { user.phoneVisibility = it }
        return userRepository.save(user)
    }
    @Transactional
    fun updateAvatar(userId: UUID, avatarUrl: String): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        user.avatarUrl = avatarUrl
        return userRepository.save(user)
    }
    fun searchUsers(query: String, page: Int, size: Int): UserSearchResult {
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        val users = userRepository.searchByNameOrUsername(query, pageable)
        return UserSearchResult(
            // Apply privacy filtering - use toRestrictedDto with isContact=false
            // since we don't know the relationship in search context
            users = users.content.map { it.toRestrictedDto(isContact = false) },
            totalCount = users.totalElements.toInt()
        )
    }
    @Transactional
    fun updateOnlineStatus(userId: UUID, isOnline: Boolean): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        user.isOnline = isOnline
        if (!isOnline) user.lastSeen = Instant.now()
        return userRepository.save(user)
    }
    /**
     * Match phone numbers from device contacts with registered users.
     * Normalizes phone numbers before matching.
     */
    fun matchContacts(phoneNumbers: List<String>): UserSearchResult {
        // Normalize all phone numbers
        val normalizedNumbers = phoneNumbers.map { normalizePhoneNumber(it) }.distinct()
        val users = userRepository.findByPhoneNumberIn(normalizedNumbers)
        return UserSearchResult(
            // These are device contacts, so they ARE contacts - use isContact=true
            users = users.map { it.toRestrictedDto(isContact = true) },
            totalCount = users.size
        )
    }
    /**
     * Normalizes a phone number by removing non-digit characters
     * and converting to a standard format.
     */
    private fun normalizePhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
            .let { if (it.startsWith("98")) it.substring(2) else it }
            .let { if (it.startsWith("0")) it else "0$it" }
    }
    
    /**
     * Update user's privacy settings.
     */
    
    /**
     * Check if two users are contacts (have an existing chat with messages).
     */
    fun areUsersContacts(userId1: UUID, userId2: UUID): Boolean {
        return chatRepository.findPrivateChatBetween(userId1, userId2).isNotEmpty()
    }

    fun getTotalUserCount(): Long = userRepository.count()
    @Transactional
    fun setUsername(userId: UUID, username: String): User? {
        val user = userRepository.findById(userId).orElse(null) ?: return null
        val normalizedUsername = username.lowercase().trim()
        if (normalizedUsername.length < 3) {
            throw IllegalArgumentException("نام کاربری باید حداقل ۳ کاراکتر باشد")
        }
        if (!normalizedUsername.matches(Regex("^[a-z0-9_]+$"))) {
            throw IllegalArgumentException("نام کاربری فقط می‌تواند شامل حروف انگلیسی، اعداد و زیرخط باشد")
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw IllegalArgumentException("این نام کاربری قبلاً استفاده شده است")
        }
        user.username = normalizedUsername
        return userRepository.save(user)
    }
    fun isUsernameTaken(username: String): Boolean {
        return userRepository.existsByUsername(username.lowercase().trim())
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 💬 Chat Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val groupMessageRepository: GroupMessageRepository,
    private val channelPostRepository: ChannelPostRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler
) {
    fun getSharedContent(userId: UUID, targetId: UUID, scope: String, type: String, page: Int, size: Int): MessageListResponse {
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        val types = mapTypeStringToMessageTypes(type)
        println("DEBUG: getSharedContent - User: $userId, Target: $targetId, Scope: $scope, RequestedType: $type, MappedTypes: $types")

        return when (scope.uppercase()) {
            "USER" -> {
                var chats = chatRepository.findPrivateChatBetween(userId, targetId)
                
                if (chats.isEmpty()) {
                    println("DEBUG: getSharedContent - USER scope search failed for User: $userId and Target: $targetId")
                    val totalChatsInDb = chatRepository.count()
                    println("DEBUG: getSharedContent - TOTAL CHATS IN DATABASE: $totalChatsInDb")
                    
                    if (totalChatsInDb > 0) {
                        println("DEBUG: getSharedContent - LISTING ALL CHATS IN DB FOR DIAGNOSIS:")
                        chatRepository.findAll().forEach { c ->
                            println("DEBUG:   - Chat ${c.id} | Type: ${c.type} | Participants: ${c.participants.map { it.id }}")
                        }
                    }
                    
                    // FALLBACK 1: Maybe targetId is already a chatId?
                    val fallbackChat = chatRepository.findById(targetId).orElse(null)
                    if (fallbackChat != null) {
                        println("DEBUG: getSharedContent - FALLBACK: Found Chat by ID directly: ${fallbackChat.id} (Type: ${fallbackChat.type})")
                        chats = listOf(fallbackChat)
                    } else {
                        // FALLBACK 2: Exhaustive dump of user's chats for diagnosis
                        val allUserChats = chatRepository.findByParticipantId(userId, org.springframework.data.domain.PageRequest.of(0, 20))
                        println("DEBUG: getSharedContent - DIAGNOSTIC: User $userId has ${allUserChats.totalElements} total chats. Listing first 5:")
                        allUserChats.content.take(5).forEach { c ->
                            println("DEBUG:   - Chat ${c.id} | Type: ${c.type} | Participants: ${c.participants.map { it.id }}")
                        }
                        return MessageListResponse(emptyList(), 0, false)
                    }
                }
                
                val chat = chats.first()
                println("DEBUG: getSharedContent - Proceeding with Chat ${chat.id}, searching for messages of types $types")
                
                val includeLinks = types.contains(MessageType.LINK)
                val messages = messageRepository.findSharedContent(chat.id!!, types, includeLinks, pageable)
                println("DEBUG: getSharedContent - Final Query Result: ${messages.totalElements} messages found for types $types")
                
                MessageListResponse(
                    messages = messages.content.map { it.toDto(userId) },
                    totalCount = messages.totalElements.toInt(),
                    hasMore = messages.hasNext()
                )
            }
            "GROUP" -> {
                val includeLinks = types.contains(MessageType.LINK)
                val messages = groupMessageRepository.findSharedContent(targetId, types, includeLinks, pageable)
                println("DEBUG: getSharedContent - Found ${messages.totalElements} messages for Group $targetId")
                MessageListResponse(
                    messages = messages.content.map { it.toMessageDto(userId) },
                    totalCount = messages.totalElements.toInt(),
                    hasMore = messages.hasNext()
                )
            }
            "CHANNEL" -> {
                val includeLinks = types.contains(MessageType.LINK)
                val posts = channelPostRepository.findSharedContent(targetId, types, includeLinks, pageable)
                println("DEBUG: getSharedContent - Found ${posts.totalElements} posts for Channel $targetId")
                MessageListResponse(
                    messages = posts.content.map { it.toMessageDto(userId) },
                    totalCount = posts.totalElements.toInt(),
                    hasMore = posts.hasNext()
                )
            }
            else -> throw IllegalArgumentException("Invalid scope: $scope")
        }
    }

    private fun mapTypeStringToMessageTypes(typeString: String): List<MessageType> {
        val allTypes = MessageType.values().toList()
        return when (typeString.uppercase()) {
            "IMAGE" -> listOfNotNull(
                safeMessageType("IMAGE"),
                safeMessageType("STICKER"),
                safeMessageType("GIF")
            )
            "VIDEO" -> listOfNotNull(safeMessageType("VIDEO"))
            "LINK" -> listOfNotNull(safeMessageType("LINK"))
            "FILE" -> listOfNotNull(safeMessageType("FILE"))
            "AUDIO" -> listOfNotNull(
                safeMessageType("AUDIO"),
                safeMessageType("VOICE")
            )
            "GIF" -> listOfNotNull(
                safeMessageType("IMAGE"),
                safeMessageType("GIF")
            )
            "STICKER" -> listOfNotNull(safeMessageType("STICKER"))
            "CONTACT" -> listOfNotNull(safeMessageType("CONTACT"))
            else -> allTypes
        }
    }

    private fun safeMessageType(name: String): MessageType? {
        return try {
            MessageType.valueOf(name)
        } catch (e: Exception) {
            println("WARN: MessageType $name not found in Enum, skipping filter")
            null
        }
    }

    fun getChatsForUser(userId: UUID, page: Int, size: Int): ChatListResponse {
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        val chats = chatRepository.findActiveChatsForUser(userId, pageable)
        println("DEBUG: getChatsForUser - User: $userId, Found ${chats.totalElements} active chats")
        if (chats.totalElements == 0L) {
             val allChatsCount = chatRepository.count()
             println("DEBUG: getChatsForUser - DIAGNOSTIC: Total chats in DB: $allChatsCount")
        }
        return ChatListResponse(
            chats = chats.content.map { chat ->
                val lastMessage = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.id!!)
                val unreadCount = messageRepository.countUnreadMessages(chat.id!!, userId)
                ChatDto(
                    id = chat.id!!,
                    type = chat.type,
                    title = getChatTitle(chat, userId),
                    avatarUrl = getChatAvatar(chat, userId),
                    lastMessage = lastMessage?.toDto(userId),
                    unreadCount = unreadCount.toInt(),
                    isPinned = chat.isPinned,
                    isMuted = chat.isMuted,
                    isArchived = chat.isArchived,
                    participants = chat.participants.map { it.toDto() },
                    updatedAt = chat.updatedAt
                )
            },
            totalCount = chats.totalElements.toInt()
        )
    }
    
    @Transactional
    fun createPrivateChat(userId: UUID, participantId: UUID): ChatDto? {
        val user = userRepository.findById(userId).orElse(null)
        val participant = userRepository.findById(participantId).orElse(null)
        
        if (user == null || participant == null) {
            val totalUsersInDb = userRepository.count()
            println("DEBUG: createPrivateChat - FAILED. UserFound: ${user != null}, ParticipantFound: ${participant != null}")
            println("DEBUG: createPrivateChat - ParticipantId searched: $participantId")
            println("DEBUG: createPrivateChat - TOTAL USERS IN DATABASE: $totalUsersInDb")
            return null
        }

        val existingChats = chatRepository.findPrivateChatBetween(userId, participantId)
        if (existingChats.isNotEmpty()) {
            return chatToDto(existingChats.first(), userId)
        }
        val chat = Chat().apply {
            type = ChatType.PRIVATE
            title = ""
            participants = mutableListOf(user, participant)
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
        val savedChat = chatRepository.save(chat)
        val chatDto = chatToDto(savedChat, userId)
        
        // NOTE: We do NOT notify the other participant here.
        // The chat should only appear in the recipient's chat list after the first message is sent.
        // This prevents empty chats from appearing when someone just clicks on a profile.
        
        return chatDto
    }
    
    /**
     * Notify a participant about a new chat being created.
     * The chat event is sent from the perspective of the recipient.
     */
    private fun notifyParticipantAboutNewChat(chat: Chat, recipientId: UUID, initiatorId: UUID) {
        val chatEvent = WsChatEvent(
            event = "CHAT_CREATED",
            id = chat.id!!,
            type = chat.type.name,
            title = getChatTitle(chat, recipientId),
            avatarUrl = getChatAvatar(chat, recipientId),
            participants = chat.participants.map { user ->
                WsChatParticipant(
                    id = user.id!!,
                    username = user.username,
                    displayName = user.displayName,
                    phoneNumber = user.phoneNumber,
                    avatarUrl = user.avatarUrl,
                    isOnline = user.isOnline
                )
            },
            lastMessageContent = null,
            lastMessageTime = null,
            unreadCount = 0,
            isPinned = chat.isPinned,
            isMuted = chat.isMuted,
            isArchived = chat.isArchived,
            updatedAt = chat.updatedAt.toEpochMilli()
        )
        webSocketMessageHandler.notifyNewChat(recipientId, chatEvent)
    }
    fun getChatById(chatId: UUID, userId: UUID): ChatDto? {
        // First try to find as a direct Chat ID
        val chat = chatRepository.findById(chatId).orElse(null)
        if (chat != null) {
            if (chat.participants.none { it.id == userId }) return null
            return chatToDto(chat, userId)
        }
        // FALLBACK: chatId might be a userId (e.g., Saved Messages passes user.id as chatId)
        // Try to find or create a private chat between currentUser and the given ID
        val targetUser = userRepository.findById(chatId).orElse(null) ?: return null
        val existingChats = chatRepository.findPrivateChatBetween(userId, chatId)
        if (existingChats.isNotEmpty()) {
            return chatToDto(existingChats.first(), userId)
        }
        // Auto-create self-chat or private chat if not found
        return createPrivateChat(userId, chatId)
    }
    @Transactional
    fun updateChatSettings(chatId: UUID, userId: UUID, isPinned: Boolean?, isMuted: Boolean?, isArchived: Boolean?): ChatDto? {
        val chat = chatRepository.findById(chatId).orElse(null) ?: return null
        if (chat.participants.none { it.id == userId }) return null
        if (isPinned != null) chat.isPinned = isPinned
        if (isMuted != null) chat.isMuted = isMuted
        if (isArchived != null) chat.isArchived = isArchived
        val savedChat = chatRepository.save(chat)
        return chatToDto(savedChat, userId)
    }
    private fun chatToDto(chat: Chat, userId: UUID): ChatDto {
        val lastMessage = messageRepository.findTopByChatIdOrderByCreatedAtDesc(chat.id!!)
        val unreadCount = messageRepository.countUnreadMessages(chat.id!!, userId)
        return ChatDto(
            id = chat.id!!,
            type = chat.type,
            title = getChatTitle(chat, userId),
            avatarUrl = getChatAvatar(chat, userId),
            lastMessage = lastMessage?.toDto(userId),
            unreadCount = unreadCount.toInt(),
            isPinned = chat.isPinned,
            isMuted = chat.isMuted,
            isArchived = chat.isArchived,
            // For private chats, check if users have a chat together = they are considered contacts
            // This correctly applies privacy: if phone visibility is CONTACTS and they have a chat, show phone
            // If phone visibility is NOBODY, hide phone even in private chats
            participants = chat.participants.map { participant ->
                if (participant.id == userId) participant.toDto()
                else {
                    // In private chats, having a chat between users = they are contacts
                    // So isContact should be true for private chats (they're chatting!)
                    // The key insight: existing chat = contacts relationship for privacy purposes
                    val isPrivateChat = chat.type == ChatType.PRIVATE
                    participant.toRestrictedDto(isContact = isPrivateChat)
                }
            },
            updatedAt = chat.updatedAt
        )
    }
    private fun getChatTitle(chat: Chat, currentUserId: UUID): String {
        if (chat.title.isNotBlank()) return chat.title
        val otherParticipant = chat.participants.find { it.id != currentUserId }
        return otherParticipant?.displayName ?: "چت"
    }
    private fun getChatAvatar(chat: Chat, currentUserId: UUID): String? {
        if (chat.avatarUrl != null) return chat.avatarUrl
        val otherParticipant = chat.participants.find { it.id != currentUserId } ?: return null
        
        return when (otherParticipant.profileVisibility) {
            VisibilityOption.NOBODY -> null
            VisibilityOption.EVERYONE -> otherParticipant.avatarUrl
            VisibilityOption.CONTACTS -> otherParticipant.avatarUrl // Assume allowed
            null -> otherParticipant.avatarUrl
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📨 Message Service
// ═══════════════════════════════════════════════════════════════════════════════

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val messageReactionRepository: MessageReactionRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val webSocketMessageHandler: WebSocketMessageHandler,
    private val pollRepository: PollRepository,
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val groupMessageRepository: GroupMessageRepository,
    private val channelRepository: ChannelRepository,
    private val channelSubscriberRepository: ChannelSubscriberRepository,
    private val channelPostRepository: ChannelPostRepository
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(MessageService::class.java)
    @Transactional(readOnly = true)
    fun getMessagesForChat(chatId: UUID, userId: UUID, page: Int, size: Int): MessageListResponse {
        val chat = chatRepository.findById(chatId).orElse(null)
            ?: throw IllegalArgumentException("چت یافت نشد")
        if (chat.participants.none { it.id == userId }) {
            throw IllegalAccessException("شما به این چت دسترسی ندارید")
        }
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        val messages = messageRepository.findWithReactionsByChatId(chatId, pageable)
        return MessageListResponse(
            messages = messages.content.map { it.toDto(userId) },
            totalCount = messages.totalElements.toInt(),
            hasMore = messages.hasNext()
        )
    }

    fun searchMessages(chatId: UUID, userId: UUID, query: String?, types: List<MessageType>?, page: Int, size: Int): MessageListResponse {
        val chat = chatRepository.findById(chatId).orElse(null)
            ?: throw IllegalArgumentException("چت یافت نشد")
        if (chat.participants.none { it.id == userId }) {
            throw IllegalAccessException("شما به این چت دسترسی ندارید")
        }
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        
        // If types is null or empty, we search ALL types. But the query expects a list. 
        // If query is empty and types is empty, it returns everything (like getMessages).
        // If types is empty/null, we pass all enums for safety or handle in query?
        // My query logic: (:types IS NULL OR m.type IN :types)
        // But Spring Data JPA doesn't like passing NULL for IN clause sometimes.
        // Let's pass all types if null/empty.
        val searchTypes = if (types.isNullOrEmpty()) MessageType.values().toList() else types

        val messages = messageRepository.searchMessages(chatId, query, searchTypes, pageable)
        return MessageListResponse(
            messages = messages.content.map { it.toDto(userId) },
            totalCount = messages.totalElements.toInt(),
            hasMore = messages.hasNext()
        )
    }
    
    @Transactional
    fun sendMessage(chatId: UUID, senderId: UUID, request: SendMessageRequest): MessageDto? {
        logger.info("📨 sendMessage called: chatId=$chatId, senderId=$senderId, content=${request.content.take(20)}...")
        
        val chat = chatRepository.findById(chatId).orElse(null)
        if (chat == null) {
            val totalChatsInDb = chatRepository.count()
            logger.warn("❌ Chat not found: $chatId. TOTAL CHATS IN DATABASE: $totalChatsInDb")
            return null
        }
        
        val sender = userRepository.findById(senderId).orElse(null)
        if (sender == null) {
            val totalUsers = userRepository.count()
            logger.warn("❌ Sender not found: $senderId. TOTAL USERS IN DATABASE: $totalUsers")
            return null
        }
        
        if (chat.participants.none { it.id == senderId }) {
            logger.warn("❌ Sender is not a participant in chat")
            return null
        }
        
        var replyTo: Message? = null
        if (request.replyToMessageId != null) {
            replyTo = messageRepository.findById(request.replyToMessageId).orElse(null)
        }
        val message = Message().apply {
            this.chat = chat
            this.sender = sender
            
            // Auto-detect Link type if TEXT
            var finalType = request.type
            if (finalType == MessageType.TEXT && MessageUtils.isLink(request.content)) {
                finalType = MessageType.LINK
            }
            
            this.type = finalType
            this.content = request.content
            this.mediaUrl = request.mediaUrl
            this.replyTo = replyTo
            this.createdAt = Instant.now()
            
            if (request.pollId != null) {
                val poll = pollRepository.findById(request.pollId).orElse(null)
                if (poll != null) {
                    this.poll = poll
                    this.type = MessageType.POLL
                }
            }
            if (request.amplitudes != null) {
                // Limit to 200 samples to prevent DB issues
                this.amplitudes = request.amplitudes.take(200).toMutableList()
            }
        }
        val savedMessage = messageRepository.save(message)
        chat.updatedAt = Instant.now()
        chatRepository.save(chat)
        
        logger.info("✅ Message saved to DB: ${savedMessage.id}")
        
        val messageDto = savedMessage.toDto()
        
        // Broadcast message to other participants via WebSocket for instant delivery
        logger.info("📡 Broadcasting to participants...")
        broadcastMessageToRecipients(chat, savedMessage, senderId)
        
        // Also notify about chat update (for chat list preview updates)
        logger.info("📡 Notifying chat update...")
        notifyChatUpdateToParticipants(chat, savedMessage, senderId)
        
        return messageDto
    }
    
    /**
     * Broadcast the message to all chat participants except the sender.
     * This ensures real-time delivery without requiring topic subscription.
     */
    private fun broadcastMessageToRecipients(chat: Chat, message: Message, senderId: UUID) {
        val wsMessage = WsMessage(
            id = message.id!!,
            chatId = chat.id!!,
            senderId = message.sender?.id ?: senderId,
            senderName = message.sender?.displayName ?: "User",
            content = message.content,
            type = message.type,
            mediaUrl = message.mediaUrl,   // CRITICAL: Include media URL for audio/video/image/file
            poll = message.poll?.toDto(senderId),   // CRITICAL: Include poll for poll messages
            amplitudes = message.amplitudes, // Include waveform for voice/audio
            timestamp = message.createdAt
        )
        
        val recipientIds = chat.participants
            .mapNotNull { it.id }
            .filter { it != senderId }
        
        webSocketMessageHandler.broadcastMessageToChat(chat.id!!, wsMessage, recipientIds)
    }
    
    /**
     * Notify participants about chat update (new message arrived).
     * This updates the chat list preview in real-time.
     */
    private fun notifyChatUpdateToParticipants(chat: Chat, message: Message, senderId: UUID) {
        chat.participants
            .mapNotNull { it.id }
            .filter { it != senderId }
            .forEach { recipientId ->
                val unreadCount = messageRepository.countUnreadMessages(chat.id!!, recipientId)
                val chatEvent = WsChatEvent(
                    event = "CHAT_UPDATED",
                    id = chat.id!!,
                    type = chat.type.name,
                    title = getChatTitleForUser(chat, recipientId),
                    avatarUrl = getChatAvatarForUser(chat, recipientId),
                    participants = chat.participants.map { user ->
                        WsChatParticipant(
                            id = user.id!!,
                            username = user.username,
                            displayName = user.displayName,
                            phoneNumber = user.phoneNumber,
                            avatarUrl = user.avatarUrl,
                            isOnline = user.isOnline
                        )
                    },
                    lastMessageContent = message.content,
                    lastMessageTime = message.createdAt.toEpochMilli(),
                    unreadCount = unreadCount.toInt(),
                    isPinned = chat.isPinned,
                    isMuted = chat.isMuted,
                    isArchived = chat.isArchived,
                    updatedAt = chat.updatedAt.toEpochMilli()
                )
                webSocketMessageHandler.notifyChatUpdate(recipientId, chatEvent)
            }
    }
    
    private fun getChatTitleForUser(chat: Chat, userId: UUID): String {
        if (chat.title.isNotBlank()) return chat.title
        val otherParticipant = chat.participants.find { it.id != userId }
        return otherParticipant?.displayName ?: "چت"
    }
    
    private fun getChatAvatarForUser(chat: Chat, userId: UUID): String? {
        if (chat.avatarUrl != null) return chat.avatarUrl
        val otherParticipant = chat.participants.find { it.id != userId } ?: return null
        
        return when (otherParticipant.profileVisibility) {
            VisibilityOption.NOBODY -> null
            VisibilityOption.EVERYONE -> otherParticipant.avatarUrl
            VisibilityOption.CONTACTS -> otherParticipant.avatarUrl
            null -> otherParticipant.avatarUrl
        }
    }
    @Transactional
    fun editMessage(messageId: UUID, userId: UUID, request: EditMessageRequest): MessageDto? {
        val message = messageRepository.findById(messageId).orElse(null) ?: return null
        if (message.sender?.id != userId) return null
        message.content = request.content
        message.isEdited = true
        message.editedAt = Instant.now()
        val savedMessage = messageRepository.save(message)
        return savedMessage.toDto()
    }
    @Transactional
    fun deleteMessage(messageId: UUID, userId: UUID, deleteForEveryone: Boolean): Boolean {
        val message = messageRepository.findById(messageId).orElse(null) ?: return false
        if (message.sender?.id != userId) return false
        val chat = message.chat
        val chatId = chat?.id ?: return false
        // Get recipient IDs before deleting
        val recipientIds = chat.participants
            .mapNotNull { it.id }
            .filter { it != userId }
        if (deleteForEveryone) {
            messageRepository.delete(message)
            // Broadcast deletion to all participants for real-time removal
            webSocketMessageHandler.broadcastMessageDeletion(chatId, messageId, recipientIds)
        } else {
            messageRepository.delete(message)
        }
        return true
    }
    @Transactional
    fun markAsRead(messageId: UUID, userId: UUID): Boolean {
        val message = messageRepository.findById(messageId).orElse(null) ?: return false
        if (message.sender?.id == userId) return true
        val chat = message.chat ?: return false
        if (chat.participants.none { it.id == userId }) return false
        
        val previousStatus = message.status
        message.status = MessageStatus.READ
        messageRepository.save(message)
        
        // Notify the original sender that their message was read (for blue ticks)
        val senderId = message.sender?.id
        if (senderId != null && previousStatus != MessageStatus.READ) {
            logger.info("👁️ Message $messageId marked as READ by $userId, notifying sender $senderId")
            notifyMessageRead(chat.id!!, messageId, senderId, userId)
        }
        
        return true
    }
    
    /**
     * Notify the original message sender that their message was read.
     * This enables real-time "blue tick" updates on the sender's device.
     */
    private fun notifyMessageRead(chatId: UUID, messageId: UUID, senderId: UUID, readerId: UUID) {
        val readEvent = mapOf(
            "type" to "MESSAGE_READ",
            "chatId" to chatId.toString(),
            "messageId" to messageId.toString(),
            "readerId" to readerId.toString(),
            "timestamp" to Instant.now().toEpochMilli()
        )
        
        // Send to the original sender's user queue
        webSocketMessageHandler.sendReadReceiptToUser(senderId, readEvent)
    }
    @Transactional
    fun reactToMessage(messageId: UUID, userId: UUID, reaction: String?): Boolean {
        val message = messageRepository.findById(messageId).orElse(null) ?: return false
        val chat = message.chat ?: return false
        if (chat.participants.none { it.id == userId }) return false
        val existingReaction = messageReactionRepository.findByMessageIdAndUserId(messageId, userId)
        if (reaction == null) {
            if (existingReaction != null) {
                messageReactionRepository.delete(existingReaction)
            }
        } else {
            if (existingReaction != null) {
                existingReaction.reaction = reaction
                messageReactionRepository.save(existingReaction)
            } else {
                val user = userRepository.findById(userId).orElse(null) ?: return false
                val newReaction = MessageReaction().apply {
                    this.message = message
                    this.user = user
                    this.reaction = reaction
                    this.createdAt = Instant.now()
                }
                messageReactionRepository.save(newReaction)
            }
        }
        notifyChatUpdateToParticipants(chat, message, userId)
        // Broadcast real-time reaction update to all chat participants
        val user = userRepository.findById(userId).orElse(null)
        val reactionEvent = WsReactionEvent(
            messageId = messageId,
            chatId = chat.id!!,
            userId = userId,
            userName = user?.displayName ?: "User",
            reaction = reaction
        )
        val recipientIds = chat.participants
            .mapNotNull { it.id }
            .filter { it != userId }
        webSocketMessageHandler.broadcastReactionUpdate(chat.id!!, reactionEvent, recipientIds)
        return true
    }
    // ═══ PIN MESSAGE ═══
    @Transactional
    fun pinMessage(messageId: UUID, userId: UUID, isPinned: Boolean): MessageDto? {
        val message = messageRepository.findById(messageId).orElse(null) ?: return null
        val chat = message.chat ?: return null
        if (chat.participants.none { it.id == userId }) return null
        message.isPinned = isPinned
        message.pinnedAt = if (isPinned) Instant.now() else null
        message.pinnedById = if (isPinned) userId else null
        val saved = messageRepository.save(message)
        return saved.toDto(userId)
    }
    fun getPinnedMessages(chatId: UUID, userId: UUID): List<MessageDto> {
        val chat = chatRepository.findById(chatId).orElse(null) ?: return emptyList()
        if (chat.participants.none { it.id == userId }) return emptyList()
        return messageRepository.findByChatIdAndIsPinnedTrue(chatId).map { it.toDto(userId) }
    }
    // ═══ FORWARD MESSAGE ═══
    @Transactional
    fun forwardMessages(userId: UUID, request: ForwardMessageRequest): Boolean {
        val sender = userRepository.findById(userId).orElse(null) ?: return false
        // Try to find original messages from any source (chat, group, channel)
        val originals = findOriginalMessages(request.messageIds)
        if (originals.isEmpty()) return false
        when (request.targetType.uppercase()) {
            "CHAT" -> forwardToChat(userId, sender, originals, request.targetChatId ?: return false)
            "GROUP" -> forwardToGroup(userId, sender, originals, request.targetGroupId ?: return false)
            "CHANNEL" -> forwardToChannel(userId, sender, originals, request.targetChannelId ?: return false)
            else -> return false
        }
        return true
    }
    private data class OriginalMessageData(
        val type: MessageType,
        val content: String,
        val mediaUrl: String?,
        val senderName: String
    )
    private fun findOriginalMessages(messageIds: List<UUID>): List<OriginalMessageData> {
        val results = mutableListOf<OriginalMessageData>()
        for (id in messageIds) {
            // Try chat messages first
            val chatMsg = messageRepository.findById(id).orElse(null)
            if (chatMsg != null) {
                results.add(OriginalMessageData(chatMsg.type, chatMsg.content, chatMsg.mediaUrl, chatMsg.sender?.displayName ?: "فروارد شده"))
                continue
            }
            // Try group messages
            val groupMsg = groupMessageRepository.findById(id).orElse(null)
            if (groupMsg != null) {
                results.add(OriginalMessageData(groupMsg.type, groupMsg.content, groupMsg.mediaUrl, groupMsg.sender?.displayName ?: "فروارد شده"))
                continue
            }
            // Try channel posts
            val channelPost = channelPostRepository.findById(id).orElse(null)
            if (channelPost != null) {
                results.add(OriginalMessageData(channelPost.type, channelPost.content, channelPost.mediaUrl, channelPost.channel?.name ?: "فروارد شده"))
                continue
            }
        }
        return results
    }
    private fun forwardToChat(userId: UUID, sender: User, originals: List<OriginalMessageData>, targetChatId: UUID) {
        val chat = chatRepository.findById(targetChatId).orElse(null) ?: return
        if (chat.participants.none { it.id == userId }) return
        originals.forEach { original ->
            val forwarded = Message().apply {
                this.chat = chat
                this.sender = sender
                this.type = original.type
                this.content = original.content
                this.mediaUrl = original.mediaUrl
                this.createdAt = Instant.now()
                this.forwardedFrom = original.senderName
            }
            val saved = messageRepository.save(forwarded)
            // WebSocket: broadcast to other participants
            broadcastMessageToRecipients(chat, saved, userId)
            notifyChatUpdateToParticipants(chat, saved, userId)
        }
        chat.updatedAt = Instant.now()
        chatRepository.save(chat)
    }
    private fun forwardToGroup(userId: UUID, sender: User, originals: List<OriginalMessageData>, targetGroupId: UUID) {
        val group = groupRepository.findById(targetGroupId).orElse(null) ?: return
        val membership = groupMemberRepository.findByGroupIdAndUserId(targetGroupId, userId) ?: return
        originals.forEach { original ->
            val forwarded = GroupMessage().apply {
                this.group = group
                this.sender = sender
                this.type = original.type
                this.content = original.content
                this.mediaUrl = original.mediaUrl
                this.createdAt = Instant.now()
                this.forwardedFrom = original.senderName
            }
            val saved = groupMessageRepository.save(forwarded)
            // WebSocket: broadcast to group members
            val wsMessage = WsMessage(
                id = saved.id!!,
                chatId = targetGroupId,
                senderId = sender.id!!,
                senderName = sender.displayName,
                senderAvatar = sender.avatarUrl,
                content = saved.content,
                type = saved.type,
                mediaUrl = saved.mediaUrl,
                timestamp = saved.createdAt
            )
            val memberIds = groupMemberRepository.findByGroupId(targetGroupId)
                .mapNotNull { it.user?.id }
                .filter { it != userId }
            webSocketMessageHandler.broadcastGroupMessage(targetGroupId, wsMessage, memberIds)
        }
    }
    private fun forwardToChannel(userId: UUID, sender: User, originals: List<OriginalMessageData>, targetChannelId: UUID) {
        val channel = channelRepository.findById(targetChannelId).orElse(null) ?: return
        // Only owner or admin can post to channel
        val isOwner = channel.owner?.id == userId
        if (!isOwner) {
            val subscription = channelSubscriberRepository.findByChannelIdAndUserId(targetChannelId, userId)
            if (subscription == null || !subscription.isAdmin) return
        }
        originals.forEach { original ->
            val post = ChannelPost().apply {
                this.channel = channel
                this.type = original.type
                this.content = original.content
                this.mediaUrl = original.mediaUrl
                this.createdAt = Instant.now()
                this.forwardedFrom = original.senderName
            }
            val saved = channelPostRepository.save(post)
            // WebSocket: broadcast to all subscribers
            val subscriberIds = channelSubscriberRepository.findByChannelId(targetChannelId)
                .mapNotNull { it.user?.id }
            webSocketMessageHandler.broadcastChannelPost(targetChannelId, saved.toDto(null), subscriberIds)
        }
    }
    // ═══ SCHEDULE MESSAGE ═══
    @Transactional
    fun scheduleMessage(chatId: UUID, userId: UUID, request: ScheduleMessageRequest): MessageDto? {
        val chat = chatRepository.findById(chatId).orElse(null) ?: return null
        val sender = userRepository.findById(userId).orElse(null) ?: return null
        if (chat.participants.none { it.id == userId }) return null
        val message = Message().apply {
            this.chat = chat
            this.sender = sender
            this.type = request.type
            this.content = request.content
            this.mediaUrl = request.mediaUrl
            this.scheduledAt = request.scheduledAt
            this.createdAt = Instant.now()
            this.status = MessageStatus.SCHEDULED
            if (request.amplitudes != null) {
                this.amplitudes = request.amplitudes.take(200).toMutableList()
            }
        }
        val saved = messageRepository.save(message)
        return saved.toDto(userId)
    }
}
